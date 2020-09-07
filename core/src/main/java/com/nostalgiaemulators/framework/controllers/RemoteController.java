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

package com.nostalgiaemulators.framework.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.os.PowerManager.WakeLock;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.remote.ControllerEventSource;
import com.nostalgiaemulators.framework.remote.ControllerKeyEvent;
import com.nostalgiaemulators.framework.remote.OnControllerEventListener;
import com.nostalgiaemulators.framework.remote.wifi.WifiControllerServer;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;

public class RemoteController implements EmulatorController {

	public interface OnRemoteControllerWarningListener {
		public void onZapperCollision();
	}

	InputConnection inputConnection;

	WakeLock wakeLock;

	OnRemoteControllerWarningListener warningListener = null;

	public RemoteController(EmulatorActivity activity) {
		emulatorActivity = activity;
		start();
		inputConnection = new BaseInputConnection(activity.getWindow()
				.getDecorView(), false);

		systemKeyMapping = new SparseIntArray();
		systemKeyMapping.put(RemoteController.KEY_MENU, KeyEvent.KEYCODE_MENU);
		systemKeyMapping.put(RemoteController.KEY_BACK, KeyEvent.KEYCODE_BACK);
	}

	@Override
	public void onResume() {
		start();
		emulator.resetKeys();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

	}

	private void start() {
		server = WifiControllerServer.getInstance(emulatorActivity, null);
		server.setControllerEventListener(eventDispatcher);
		eventDispatcher.addController(this);
		if (this == eventDispatcher.getController(0)) {
			server.onResume();
		}
		zapperWarningShow = false;

	}

	@Override
	public void onPause() {
		if (server != null) {
			server.onPause();
			server = null;
		}
		eventDispatcher.removeController(this);
	}

	EmulatorActivity emulatorActivity;

	@Override
	public void connectToEmulator(int port, Emulator emulator) {
		this.emulator = emulator;
		this.port = port;
		mapping = emulator.getInfo().getKeyMapping();
	}

	

	@Override
	public View getView() {
		return null;
	}

	public void setOnRemoteControllerWarningListener(
			OnRemoteControllerWarningListener list) {
		warningListener = list;
	}

	@Override
	public void onDestroy() {
		emulator = null;
		emulatorActivity = null;
	}

	private int port;
	private ControllerEventSource server;
	private Emulator emulator;
	private Map<Integer, Integer> mapping;
	private SparseIntArray systemKeyMapping;
	private boolean zapperWarningShow = false;

	private void processEmulatorKeyEvent(ControllerKeyEvent event) {
		if (event.port == this.port) {
			emulator.setKeyPressed(event.port, mapping.get(event.keyCode),
					event.action == EmulatorController.ACTION_DOWN);
			emulatorActivity.hideTouchController();
		}

		if (event.port != 0 && warningListener != null
				&& zapperWarningShow == false) {
			String hash = emulator.getLoadedGame().md5;
			if (PreferenceUtil.isZapperEnabled(emulatorActivity, hash)) {
				warningListener.onZapperCollision();
			}
			zapperWarningShow = true;
		}
	}

	private void processAndroidKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& !specialKeys.contains(event.getKeyCode())) {
			specialKeys.add(event.getKeyCode());
			inputConnection.sendKeyEvent(event);
		} else if (event.getAction() == KeyEvent.ACTION_UP
				&& specialKeys.contains(event.getKeyCode())) {
			specialKeys.remove(event.getKeyCode());
			inputConnection.sendKeyEvent(event);
		}
	}

	private void processTextEvent(String text) {
	}

	private void processCommandEvent(int command, int param0, int param1) {
	}

	private HashSet<Integer> specialKeys = new HashSet<Integer>();

	public static final int KEY_MENU = 11;
	public static final int KEY_BACK = 10;

	private static class Dispatcher implements OnControllerEventListener {

		@Override
		public void onControllerEmulatorKeyEvent(ControllerKeyEvent event) {
			for (RemoteController controller : controllers) {
				controller.processEmulatorKeyEvent(event);
			}
		}

		@Override
		public void onControllerAndroidKeyEvent(KeyEvent event) {
			for (RemoteController controller : controllers) {
				controller.processAndroidKeyEvent(event);
			}
		}

		@Override
		public void onControllerTextEvent(String text) {
			for (RemoteController controller : controllers) {
				controller.processTextEvent(text);
			}
		}

		@Override
		public void onControllerCommandEvent(int command, int param0, int param1) {
			for (RemoteController controller : controllers) {
				controller.processCommandEvent(command, param0, param1);
			}
		}

		public void addController(RemoteController rc) {
			if (!controllers.contains(rc)) {
				controllers.add(rc);
			}
		}

		public void removeController(RemoteController rc) {
			if (controllers.contains(rc)) {
				controllers.remove(rc);
			}
		}

		public EmulatorController getController(int i) {
			return controllers.get(i);
		}

		private List<RemoteController> controllers = new ArrayList<RemoteController>();

	}

	private static Dispatcher eventDispatcher = new Dispatcher();

	@Override
	public void onGameStarted(GameDescription game) {

	}

	@Override
	public void onGamePaused(GameDescription game) {

	}

}
