package com.dyn.robot.utils;

import java.util.EventListener;

import com.rabbit.gui.show.Show;

/**
 * Listener interface for classes interested in knowing about a boolean flag
 * change.
 */
public interface BooleanChangeListener extends EventListener {

	public void stateChanged(BooleanChangeEvent event, Show show);

}