package com.dyn.robot.utils;

import java.util.EventObject;

/**
 * This class lets the listener know when the change occured and what object was
 * changed.
 */
public class BooleanChangeEvent extends EventObject {

	private final BooleanChangeDispatcher dispatcher;

	public BooleanChangeEvent(BooleanChangeDispatcher dispatcher) {
		super(dispatcher);
		this.dispatcher = dispatcher;
	}

	// type safe way to get source (as opposed to getSource of EventObject
	public BooleanChangeDispatcher getDispatcher() {
		return dispatcher;
	}
}
