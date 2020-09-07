// Copyright (c) 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 Radek Lzicar & Ales Lanik
//
// This file is part of Nostalgia Emulator Framework.
//
// Nostalgia Emulator Framework is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nostalgia Emulator Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Nostalgia Emulator Framework. If not, see <http://www.gnu.org/licenses/>.

package com.nostalgiaemulators.framework.remote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.view.KeyEvent;
import android.view.Window;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;

import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.remote.wifi.WifiControllerServer;
import com.nostalgiaemulators.framework.utils.Log;

public class VirtualDPad implements OnControllerEventListener {
	private static final String TAG = "com.nostalgiaemulators.framework.remote.VirtualDPad";

	public static interface OnVirtualDPEventsListener {
		public void onVirtualDPadTextEvent(String text);

		public void onVirtualDPadCommandEvent(int command, int param0,
				int param1);
	}

	private VirtualDPad() {
	}

	public static VirtualDPad getInstance() {
		return instance;
	}

	public void attachToWindow(Window window) {
		server = WifiControllerServer.getInstance(window.getContext(), null);
		server.setControllerEventListener(this);
		setInputConnection(new BaseInputConnection(window.getDecorView(), false));
	}

	public void detachFromWindow() {
		if (timer != null) {
			timer.cancel();
		}
		setInputConnection(null);
	}

	public void onResume(Window window) {
		attachToWindow(window);
		onResume();
	}

	public void onResume() {
		server.onResume();
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
	}

	public void onPause() {
		server.onPause();
		timer.cancel();
		longPresHandlers.clear();
	}

	Timer timer;

	@Override
	public void onControllerEmulatorKeyEvent(final ControllerKeyEvent event) {
		if (connection == null) {
			return;
		}
		boolean pressJustOnce = false;
		int kc = -1;
		switch (event.keyCode) {
		case EmulatorController.KEY_LEFT:
			kc = KeyEvent.KEYCODE_DPAD_LEFT;
			break;
		case EmulatorController.KEY_RIGHT:
			kc = KeyEvent.KEYCODE_DPAD_RIGHT;
			break;
		case EmulatorController.KEY_UP:
			kc = KeyEvent.KEYCODE_DPAD_UP;
			break;
		case EmulatorController.KEY_DOWN:
			kc = KeyEvent.KEYCODE_DPAD_DOWN;
			break;
		case EmulatorController.KEY_START:
			kc = KeyEvent.KEYCODE_DPAD_CENTER;
			break;
		case EmulatorController.KEY_A:
			kc = KeyEvent.KEYCODE_DPAD_CENTER;
			break;
		case EmulatorController.KEY_B:
			kc = KeyEvent.KEYCODE_DPAD_CENTER;
			break;
		default:
			return;
		}
		int action = event.action == EmulatorController.ACTION_DOWN ? KeyEvent.ACTION_DOWN
				: KeyEvent.ACTION_UP;
		final KeyEvent keyEvent = new KeyEvent(action, kc);

		if (action == EmulatorController.ACTION_DOWN) {
			if (!pressJustOnce) {
				TimerTask task = longPresHandlers.get(event.keyCode);
				if (task == null) {
					task = new TimerTask() {
						@Override
						public void run() {
							sendKeyEvent(keyEvent);
						}
					};
					longPresHandlers.put(event.keyCode, task);
					timer.schedule(task, 800, 100);
				}
			}
			sendKeyEvent(keyEvent);

		} else {
			if (!pressJustOnce) {
				longPresHandlers.get(event.keyCode).cancel();
				longPresHandlers.put(event.keyCode, null);
			}
			sendKeyEvent(keyEvent);
		}

	}

	private void sendKeyEvent(KeyEvent event) {
		if (connection != null) {
			connection.sendKeyEvent(event);
		}
	}

	@Override
	public void onControllerAndroidKeyEvent(KeyEvent event) {
		Log.i(TAG, "android event:" + event);
		sendKeyEvent(event);
	}

	@Override
	public void onControllerTextEvent(String text) {
		for (OnVirtualDPEventsListener listener : textListeners) {
			listener.onVirtualDPadTextEvent(text);
		}
	}

	@Override
	public void onControllerCommandEvent(int command, int param0, int param1) {
		for (OnVirtualDPEventsListener listener : textListeners) {
			Log.i(TAG, "command " + command + " " + param0 + " " + param1);
			listener.onVirtualDPadCommandEvent(command, param0, param1);
		}
	}

	@SuppressLint("UseSparseArrays")
	HashMap<Integer, TimerTask> longPresHandlers = new HashMap<Integer, TimerTask>();

	public long downTime;

	private void setInputConnection(InputConnection connection) {
		this.connection = connection;
	}

	private static VirtualDPad instance = new VirtualDPad();
	private InputConnection connection;
	private ControllerEventSource server;
	private HashSet<OnVirtualDPEventsListener> textListeners = new HashSet<VirtualDPad.OnVirtualDPEventsListener>();

	public void addOnTextChangeListener(OnVirtualDPEventsListener listener) {
		textListeners.add(listener);
	}

	public void removeOnTextChangeListener(OnVirtualDPEventsListener listener) {
		textListeners.remove(listener);
	}

}
