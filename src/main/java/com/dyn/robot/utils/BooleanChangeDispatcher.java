package com.dyn.robot.utils;

import com.rabbit.gui.show.Show;

public interface BooleanChangeDispatcher {

	public void addBooleanChangeListener(BooleanChangeListener listener);

	public void addBooleanChangeListener(BooleanChangeListener listener, Show show);

	public boolean getFlag();

	public boolean removeBooleanChangeListener(BooleanChangeListener listener);

	public boolean removeBooleanChangeListener(Show show);

	public void setFlag(boolean flag);

}
