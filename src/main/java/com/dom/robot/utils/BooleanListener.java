package com.dom.robot.utils;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.rabbit.gui.show.Show;

/**
 * This class uses the EventQueue to process its events, but you should only
 * really do this if the changes you make have an impact on part of a GUI eg.
 * adding a button to a JFrame.
 *
 * Otherwise, you should create your own event dispatch thread that can handle
 * change events
 */
public class BooleanListener implements BooleanChangeDispatcher {

	private boolean flag;
	private List<Pair<BooleanChangeListener, Show>> listeners;

	public BooleanListener(boolean initialFlagState) {
		flag = initialFlagState;
		listeners = new ArrayList<>();
	}

	@Override
	public void addBooleanChangeListener(BooleanChangeListener listener) {
		listeners.add(new ImmutablePair(listener, null));
	}

	@Override
	public void addBooleanChangeListener(BooleanChangeListener listener, Show show) {
		listeners.add(new ImmutablePair(listener, show));
	}

	private void dispatchEvent() {
		final BooleanChangeEvent event = new BooleanChangeEvent(this);
		for (Pair<BooleanChangeListener, Show> l : listeners) {
			dispatchRunnableOnEventQueue(l, event);
		}
	}

	private void dispatchRunnableOnEventQueue(final Pair<BooleanChangeListener, Show> listener,
			final BooleanChangeEvent event) {
		EventQueue.invokeLater(() -> listener.getLeft().stateChanged(event, listener.getRight()));
	}

	@Override
	public boolean getFlag() {
		return flag;
	}

	@Override
	public boolean removeBooleanChangeListener(BooleanChangeListener listener) {
		boolean removed = false;
		List<Pair<BooleanChangeListener, Show>> listenersToRemove = new ArrayList<>();
		for (Pair<BooleanChangeListener, Show> l : listeners) {
			if (l.getLeft().equals(listener)) {
				listenersToRemove.add(l);
				removed = true;
			}
		}
		listeners.removeAll(listenersToRemove);
		return removed;
	}

	@Override
	public boolean removeBooleanChangeListener(Show show) {
		boolean removed = false;
		List<Pair<BooleanChangeListener, Show>> listenersToRemove = new ArrayList<>();
		for (Pair<BooleanChangeListener, Show> l : listeners) {
			if (l.getRight().equals(show)) {
				listenersToRemove.add(l);
				removed = true;
			}
		}
		listeners.removeAll(listenersToRemove);
		return removed;
	}

	@Override
	public void setFlag(boolean flag) {
		if (this.flag != flag) {
			this.flag = flag;
			dispatchEvent();
		}
	}
}
