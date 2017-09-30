package com.dom.rjm.api;

import java.io.IOException;
import java.io.PrintWriter;

import com.dom.rjm.events.MCEventHandler;

// This class is meant to provide most of the APIHandler facility while one is connected to a
// server. Of course, any block changes won't get written back to the server.

public class APIHandlerClientOnly extends APIHandler {

	public APIHandlerClientOnly(MCEventHandler eventHandler, PrintWriter writer) throws IOException {
		super(eventHandler, writer);
		Python2MinecraftApi.setUseClientMethods(true);
	}
}
