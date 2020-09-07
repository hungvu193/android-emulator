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

import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.KeyboardProfile;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;

public class KeyboardController implements EmulatorController {

	private static final String TAG = "com.nostalgiaemulators.framework.controller.KeyboardController";

	String gameHash;
	private static String sLastHash;

	public KeyboardController(Emulator emulator, Context context,
			String gameHash, EmulatorActivity activity) {
		this.context = context.getApplicationContext();
		this.gameHash = gameHash;
		this.emulatorActivity = activity;
		this.emulator = emulator;
		this.keyMapping = emulator.getInfo().getKeyMapping();
	}

	boolean isRewindingEnabled;

	@Override
	public void onResume() {
		isRewindingEnabled = PreferenceUtil
				.isTimeshiftEnabled(context);
		mismatchCount = 0;
		if (!gameHash.equals(sLastHash)) {
			player0GamepadId = -1;
			player1GamepadId = -1;
			sLastHash = gameHash;
		}
		profile = KeyboardProfile.getSelectedProfile(gameHash, context);
		twoGamepadsRequired = profile.requiresTwoGamepads();

		emulator.resetKeys();
		for (int i = 0; i < loadingOrSaving.length; i++) {
			loadingOrSaving[i] = false;
		}
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

	}

	@Override
	public void onGameStarted(GameDescription game) {
	}

	@Override
	public void onGamePaused(GameDescription game) {
	}

	@Override
	public void connectToEmulator(int port, Emulator emulator) {
		throw new UnsupportedOperationException();
	}

	private static int keysToMultiCode(int key1, int key2) {
		return key1 * 1000 + key2 + 10000;
	}

	private void multiToKeys(int mapValue, int[] keys) {
		mapValue -= 10000;
		int key1 = mapValue / 1000;
		mapValue -= (key1 * 1000);
		int key2 = mapValue;
		keys[0] = key1;
		keys[1] = key2;
	}

	private static boolean isMulti(int mapValue) {
		return mapValue >= 10000;
	}

	private static int getMappingSingleKeyboard(KeyboardProfile profile,
			int keyCode) {
		int mapping = profile.getMapping(0, keyCode);
		if (mapping == -1) {
			mapping = profile.getMapping(1, keyCode);
		}
		return mapping;
	}

	private int[] tmpKeys = new int[2];

	private int mismatchCount;
	private int player0GamepadId = -1;
	private int player1GamepadId = -1;
	private boolean twoGamepadsRequired = false;

	@Override
	public View getView() {

		return new View(context) {

			SparseIntArray mDpadXStates = new SparseIntArray();
			SparseIntArray mDpadYStates = new SparseIntArray();

			@SuppressLint("InlinedApi")
			@Override
			public boolean onGenericMotionEvent(MotionEvent event) {
				if (android.os.Build.VERSION.SDK_INT >= 9) {
					int source = event.getSource();
					boolean gamepad = ((source & 1025) == 1025)
							|| ((source & 16777232) == 16777232);
					if (!gamepad) {
						return false;
					}
				}
				if (android.os.Build.VERSION.SDK_INT >= 12) {
					int deviceId = event.getDeviceId();
					float x = event.getAxisValue(MotionEvent.AXIS_HAT_X);
					float y = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
					if (x == 0) {
						x = event.getX();
					}
					if (y == 0) {
						y = event.getY();
					}

					processHat(x, y, deviceId);
					return true;
				} else {
					return false;
				}
			}

			private void processHat(float x, float y, int deviceId) {

				int intX = (int) ((Math.abs(x) < 0.5f) ? 0 : Math.signum(x));
				int intY = (int) ((Math.abs(y) < 0.5f) ? 0 : Math.signum(y));

				int lastDpadX = mDpadXStates.get(deviceId);
				if (lastDpadX != intX) {
					if (lastDpadX > 0) {
						keyUp(KeyEvent.KEYCODE_DPAD_RIGHT, deviceId, false, 0);
					} else if (lastDpadX < 0) {
						keyUp(KeyEvent.KEYCODE_DPAD_LEFT, deviceId, false, 0);
					}
				}

				if (intX > 0) {
					keyDown(KeyEvent.KEYCODE_DPAD_RIGHT, deviceId, false, 0);
				} else if (intX < 0) {
					keyDown(KeyEvent.KEYCODE_DPAD_LEFT, deviceId, false, 0);
				}
				mDpadXStates.put(deviceId, intX);
				int lastDpadY = mDpadYStates.get(deviceId);
				if (lastDpadY != intY) {
					if (lastDpadY > 0) {
						keyUp(KeyEvent.KEYCODE_DPAD_DOWN, deviceId, false, 0);
					} else if (lastDpadY < 0) {
						keyUp(KeyEvent.KEYCODE_DPAD_UP, deviceId, false, 0);
					}
				}

				if (intY > 0) {
					keyDown(KeyEvent.KEYCODE_DPAD_DOWN, deviceId, false, 0);
				} else if (intY < 0) {
					keyDown(KeyEvent.KEYCODE_DPAD_UP, deviceId, false, 0);
				}
				mDpadYStates.put(deviceId, intY);
			}

			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if ((event.getFlags() & KeyEvent.FLAG_FALLBACK) != 0) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
							|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
							|| keyCode == KeyEvent.KEYCODE_DPAD_UP
							|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						return true;
					}
				}
				int source = 0;
				if (event.getRepeatCount() > 0) {
					return super.onKeyDown(keyCode, event);
				}
				if (android.os.Build.VERSION.SDK_INT >= 9) {
					source = event.getSource();
				}
				boolean res = keyDown(keyCode, event.getDeviceId(),
						event.isAltPressed(), source);
				if (!res) {
					return super.onKeyDown(keyCode, event);
				}
				return true;
			}

			private boolean keyDown(int keyCode, int deviceId, boolean alt,
					int source) {

				int mapValue = -1;
				if (player0GamepadId == -1
						&& profile.getMapping(0, keyCode) != -1) {
					if (!twoGamepadsRequired
							|| (twoGamepadsRequired && player1GamepadId != deviceId)) {
						player0GamepadId = deviceId;
					}
				}
				if (player1GamepadId == -1
						&& profile.getMapping(1, keyCode) != -1) {
					if (!twoGamepadsRequired
							|| (twoGamepadsRequired && player0GamepadId != deviceId)) {
						player1GamepadId = deviceId;
					}
				}

				if (!twoGamepadsRequired && player0GamepadId != -1
						&& player1GamepadId != -1
						&& player0GamepadId != player1GamepadId) {
					twoGamepadsRequired = true;
				}

				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (alt) {
						keyCode = KEY_XPERIA_CIRCLE;
					}
					if (android.os.Build.VERSION.SDK_INT >= 9) {
						if (((source & 1025) == 1025)
								|| ((source & 16777232) == 16777232)) {
							keyCode = KeyEvent.KEYCODE_BUTTON_SELECT;
						}
					}
				}

				if (profile != null) {
					int player = deviceId == player0GamepadId ? 0 : 1;
					if (twoGamepadsRequired) {
						mapValue = profile.getMapping(player, keyCode);
					} else {
						mapValue = getMappingSingleKeyboard(profile, keyCode);
					}

					processKey(mapValue, true);

					return true;
				} else {
					return false;
				}
			}

			@Override
			public boolean onKeyUp(int keyCode, KeyEvent event) {
				int source = 0;
				if ((event.getFlags() & KeyEvent.FLAG_FALLBACK) != 0) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
							|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
							|| keyCode == KeyEvent.KEYCODE_DPAD_UP
							|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						return true;
					}
				}
				if (android.os.Build.VERSION.SDK_INT >= 9) {
					source = event.getSource();
				}
				boolean res = keyUp(keyCode, event.getDeviceId(),
						event.isAltPressed(), source);
				if (!res) {
					return super.onKeyUp(keyCode, event);
				}
				return true;
			}

			private boolean keyUp(int keyCode, int deviceId, boolean alt,
					int source) {

				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (alt) {
						keyCode = KEY_XPERIA_CIRCLE;
					}
					if (android.os.Build.VERSION.SDK_INT >= 9) {
						if (((source & 1025) == 1025)
								|| ((source & 16777232) == 16777232) ) {
							keyCode = KeyEvent.KEYCODE_BUTTON_SELECT;
						}
					}
				}
				if (profile != null) {
					int mapValue = -1;

					int player = deviceId == player0GamepadId ? 0 : 1;

					if (twoGamepadsRequired) {
						mapValue = profile.getMapping(player, keyCode);
					} else {
						mapValue = getMappingSingleKeyboard(profile, keyCode);
					}
					processKey(mapValue, false);
					if (mapValue != -1) {
						emulatorActivity.hideTouchController();
					}
					return true;
				} else {
					return false;
				}

			}
		};
	}

	public static final int PLAYER2_OFFSET = 100000;

	public void processKey(int mapValue, boolean pressed) {
		if (mapValue == -1) {
			return;
		}
		int port = 0;
		if (mapValue >= PLAYER2_OFFSET) {

			mapValue -= PLAYER2_OFFSET;
			port = 1;
		}

		if (mapValue == KEY_BACK) {
			if (pressed) {
				emulatorActivity.finish();
			}
		} else if (mapValue == KEY_SAVE_SLOT_0) {
			save(1, pressed);
		} else if (mapValue == KEY_SAVE_SLOT_1) {
			save(2, pressed);
		} else if (mapValue == KEY_SAVE_SLOT_2) {
			save(3, pressed);
		} else if (mapValue == KEY_LOAD_SLOT_0) {
			load(1, pressed);
		} else if (mapValue == KEY_LOAD_SLOT_1) {
			load(2, pressed);
		} else if (mapValue == KEY_LOAD_SLOT_2) {
			load(3, pressed);
		}

		else if (mapValue == KEY_FAST_FORWARD) {
			if (pressed) {
				emulatorActivity.onFastForwardDown();
			} else {
				emulatorActivity.onFastForwardUp();
			}
		} else if (mapValue == KEY_MENU) {
			if (pressed) {
				emulatorActivity.openGameMenu();
			}
		} else if (mapValue == KEY_OPEN_REWINDING_DIALOG) {

			if (pressed) {
				emulatorActivity.openTimeTravelDialog(true);
			}

		} else if (mapValue == KEY_REWINDING) {

			if (pressed) {
				if (!isRewindingEnabled) {
					emulatorActivity.runOnUiThread(new Runnable(){
						public void run() {
							Toast.makeText(context,
									"Rewinding is disabled in preferences",
									Toast.LENGTH_SHORT).show();
						}

					});
				}
				emulatorActivity.onRewindDown();

			} else {
				emulatorActivity.onRewindUp();
			}

		}
		else if (isMulti(mapValue)) {
			multiToKeys(mapValue, tmpKeys);
			emulator.setKeyPressed(port, keyMapping.get(tmpKeys[0]), pressed);
			emulator.setKeyPressed(port, keyMapping.get(tmpKeys[1]), pressed);
		} else {
			int value = keyMapping.get(mapValue);
			emulator.setKeyPressed(port, value, pressed);
		}

	}

	private boolean[] loadingOrSaving = new boolean[4];

	private void save(int slot, boolean isKeyPressed) {
		if (isKeyPressed && !loadingOrSaving[slot]) {
			loadingOrSaving[slot] = true;
			emulatorActivity.getManager().saveState(slot);
		}
		if (!isKeyPressed) {
			loadingOrSaving[slot] = false;
		}
	}

	private void load(int slot, boolean isKeyPressed) {
		if (isKeyPressed && !loadingOrSaving[slot]) {
			loadingOrSaving[slot] = true;
			emulatorActivity.getManager().loadState(slot);
		}
		if (!isKeyPressed) {
			loadingOrSaving[slot] = false;
		}
	}

	@Override
	public void onDestroy() {
		context = null;
		emulatorActivity = null;
	}

	private EmulatorActivity emulatorActivity;
	private KeyboardProfile profile;
	private Emulator emulator;
	private Map<Integer, Integer> keyMapping;
	private Context context;

	public static int KEYS_RIGHT_AND_UP = keysToMultiCode(
			EmulatorController.KEY_RIGHT, EmulatorController.KEY_UP);
	public static int KEYS_RIGHT_AND_DOWN = keysToMultiCode(
			EmulatorController.KEY_RIGHT, EmulatorController.KEY_DOWN);
	public static int KEYS_LEFT_AND_DOWN = keysToMultiCode(
			EmulatorController.KEY_LEFT, EmulatorController.KEY_DOWN);

	public static int KEYS_A_AND_B = keysToMultiCode(EmulatorController.KEY_A,
			EmulatorController.KEY_B);

	public static int KEYS_LEFT_AND_UP = keysToMultiCode(
			EmulatorController.KEY_LEFT, EmulatorController.KEY_UP);

	public static final int KEY_XPERIA_CIRCLE = 2068987562;

	public static final int KEY_MENU = 902;
	public static final int KEY_BACK = 900;
	public static final int KEY_RESET = 901;
	public static final int KEY_FAST_FORWARD = 903;
	public static final int KEY_OPEN_REWINDING_DIALOG = 910;
	public static final int KEY_REWINDING = 911;

	public static final int KEY_SAVE_SLOT_0 = 904;
	public static final int KEY_LOAD_SLOT_0 = 905;

	public static final int KEY_SAVE_SLOT_1 = 906;
	public static final int KEY_LOAD_SLOT_1 = 907;

	public static final int KEY_SAVE_SLOT_2 = 908;
	public static final int KEY_LOAD_SLOT_2 = 909;

}
