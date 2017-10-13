package com.dyn.rjm.api;

// TODO: getHeight() should check block queue

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.dyn.rjm.RaspberryJamMod;
import com.dyn.rjm.events.MCEventHandler;
import com.dyn.rjm.util.WSServer;

public class APIServer {
	private static final int MAX_CONNECTIONS = 64;
	private ServerSocket serverSocket;
	private MCEventHandler eventHandler;
	private boolean listening = true;
	private List<Socket> socketList;
	private boolean controlServer;
	private int portNumber;
	private WSServer ws;

	public APIServer(MCEventHandler eventHandler, int startPort, int endPort, int wsPort, boolean clientSide)
			throws IOException {
		socketList = new ArrayList<>();
		this.eventHandler = eventHandler;
		controlServer = !clientSide;
		serverSocket = null;

		ws = null;
		if (wsPort != 0) {
			try {
				RaspberryJamMod.logger.info("Opening websocket server on " + wsPort);
				ws = new WSServer(eventHandler, wsPort, clientSide);
				ws.start();
			} catch (Exception e) {
				RaspberryJamMod.logger.error("Error " + e);
				ws = null;
			}
		}

		for (portNumber = startPort;; portNumber++) {
			try {
				if (RaspberryJamMod.allowRemote) {
					serverSocket = new ServerSocket(portNumber);
				} else {
					serverSocket = new ServerSocket(portNumber, 50, InetAddress.getByName("127.0.0.1"));
				}
				RaspberryJamMod.logger.info("RaspberryJamMod listening on port " + portNumber);
				return;
			} catch (IOException e) {
				if (portNumber == endPort) {
					portNumber = -1;
					throw (e);
				}
			}
		}
	}

	public void close() {
		RaspberryJamMod.logger.info("Closing sockets");
		listening = false;
		synchronized (socketList) {
			for (Socket s : socketList) {
				try {
					s.close();
				} catch (IOException e) {
				}
			}
		}
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}
		if (ws != null) {
			try {
				ws.stop();
			} catch (IOException e) {
			} catch (InterruptedException e) {
			}
		}
	}

	public void communicate() throws IOException {
		while (listening) {
			if (!RaspberryJamMod.concurrent) {
				try {
					socketCommunicate(serverSocket.accept());
				} catch (Exception e) {
				}
			} else {
				try {
					int numSockets;
					synchronized (socketList) {
						numSockets = socketList.size();
					}
					if (numSockets < APIServer.MAX_CONNECTIONS) {
						final Socket socket = serverSocket.accept();
						new Thread(() -> socketCommunicate(socket)).start();
					} else {
						// Too many connections: sleep hoping one or more go
						// away
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
				}
			}
		}
	}

	public int getPortNumber() {
		return portNumber;
	}

	private void socketCommunicate(Socket connectionSocket) {
		PrintWriter writer = null;
		BufferedReader reader = null;
		synchronized (socketList) {
			socketList.add(connectionSocket);
		}

		APIHandler api = null;

		try {
			String clientSentence;

			reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			writer = new PrintWriter(connectionSocket.getOutputStream());

			api = controlServer ? new APIHandler(eventHandler, writer) : new APIHandlerClientOnly(eventHandler, writer);

			while (null != (clientSentence = reader.readLine())) {
				api.process(clientSentence);
			}
		} catch (Exception e) {
			RaspberryJamMod.logger.error("" + e);
		} finally {
			if (api != null) {
				api.close();
			}

			synchronized (socketList) {
				socketList.remove(connectionSocket);
			}
			try {
				connectionSocket.close();
			} catch (IOException e) {
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			if (writer != null) {
				writer.close();
			}
		}
	}
}
