package com.dyn.robot.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.dyn.robot.RobotMod;
import com.dyn.robot.api.APIHandler;

public class WSServer extends WebSocketServer {
	private static boolean isLocal(InetAddress addr) {
		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
			return true;
		}
		try {
			return null != NetworkInterface.getByInetAddress(addr);
		} catch (Exception e) {
			return false;
		}
	}

	Map<WebSocket, APIHandler> handlers;

	public WSServer(int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
		RobotMod.logger.info("Websocket server on " + port);
		handlers = new HashMap<>();
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		RobotMod.logger.info("websocket closed for reason " + reason);
		APIHandler apiHandler = handlers.get(conn);
		if (apiHandler != null) {
			apiHandler.getWriter().close();
			handlers.remove(conn);
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		APIHandler apiHandler = handlers.get(conn);
		if (apiHandler != null) {
			apiHandler.process(message);
		}
	}

	@Override
	public void onOpen(final WebSocket conn, ClientHandshake handshake) {
		RobotMod.logger.info("websocket connect from " + conn.getRemoteSocketAddress().getHostName());
		if (!WSServer.isLocal(conn.getRemoteSocketAddress().getAddress())) {
			conn.closeConnection(1, "Remote connections not allowed");
			return;
		}
		Writer writer = new Writer() {
			@Override
			public void close() throws IOException {
			}

			@Override
			public void flush() throws IOException {
			}

			@Override
			public void write(char[] data, int start, int len) throws IOException {
				conn.send(new String(data, start, len));
			}
		};
		PrintWriter pw = new PrintWriter(writer);
		try {
			APIHandler apiHandler = new APIHandler(pw);

			handlers.put(conn, apiHandler);
		} catch (IOException e) {
		}
	}

	@Override
	public void stop() throws IOException, InterruptedException {
		super.stop();
	}
}
