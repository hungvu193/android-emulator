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

package com.nostalgiaemulators.framework;

import com.nostalgiaemulators.framework.controllers.KeyboardController;

abstract public class BasicEmulatorInfo implements EmulatorInfo {
	public boolean hasZapper() {
		return true;
	}

	public boolean isMultiPlayerSupported() {
		return true;
	}

	@Override
	public KeyboardProfile getDefaultKeyboardProfile() {
		return KeyboardProfile.createNes30Profile();
	}

	@Override
	public int[] getDeviceKeyboardCodes() {

		int[] base = new int[] { EmulatorController.KEY_UP,
				EmulatorController.KEY_DOWN, EmulatorController.KEY_RIGHT,
				EmulatorController.KEY_LEFT, EmulatorController.KEY_START,
				EmulatorController.KEY_SELECT, EmulatorController.KEY_A,
				EmulatorController.KEY_B, EmulatorController.KEY_A_TURBO,
				EmulatorController.KEY_B_TURBO,

				KeyboardController.KEYS_LEFT_AND_UP,
				KeyboardController.KEYS_RIGHT_AND_UP,

				KeyboardController.KEYS_RIGHT_AND_DOWN,
				KeyboardController.KEYS_LEFT_AND_DOWN,

				KeyboardController.KEYS_A_AND_B,

				KeyboardController.KEY_SAVE_SLOT_0,
				KeyboardController.KEY_LOAD_SLOT_0,

				KeyboardController.KEY_SAVE_SLOT_1,
				KeyboardController.KEY_LOAD_SLOT_1,

				KeyboardController.KEY_SAVE_SLOT_2,
				KeyboardController.KEY_LOAD_SLOT_2,

				KeyboardController.KEY_MENU,
				KeyboardController.KEY_FAST_FORWARD,
				KeyboardController.KEY_OPEN_REWINDING_DIALOG,
				KeyboardController.KEY_REWINDING,

				KeyboardController.KEY_BACK };
		if (isMultiPlayerSupported()) {
			int[] res = new int[base.length * 2];
			for (int i = 0; i < base.length; i++) {
				res[i] = base[i];
			}
			for (int i = 0; i < base.length; i++) {
				res[i + base.length] = base[i]
						+ KeyboardController.PLAYER2_OFFSET;
			}
			return res;
		} else {
			return base;
		}
	}

	@Override
	public String[] getDeviceKeyboardNames() {
		String[] base = new String[] { "UP", "DOWN", "RIGHT", "LEFT", "START",
				"SELECT", "A", "B", "TURBO A", "TURBO B", "LEFT+UP",
				"RIGHT+UP", "RIGHT+DOWN", "LEFT+DOWN", "A+B", "SAVE STATE 1",				"LOAD STATE 1",				"SAVE STATE 2",				"LOAD STATE 2",				"SAVE STATE 3",				"LOAD STATE 3",
				"MENU", "FAST FORWARD", "REWIND", "QUICK REWIND", "EXIT", };

		if (isMultiPlayerSupported()) {
			String[] res = new String[base.length * 2];
			for (int i = 0; i < base.length; i++) {
				res[i] = base[i];
			}
			for (int i = 0; i < base.length; i++) {
				res[i + base.length] = base[i];
			}
			return res;
		} else {
			return base;
		}
	}

	@Override
	public String[] getDeviceKeyboardDescriptions() {
		int len = getDeviceKeyboardNames().length;
		String[] descs = new String[len];

		for (int i = 0; i < len; i++) {
			if (isMultiPlayerSupported()) {
				descs[i] = "Player 1";
			} else {
				descs[i] = "";
			}

			if (isMultiPlayerSupported() && i >= len / 2) {
				descs[i] = "Player 2";
			}
		}
		return descs;
	}

	@Override
	public String getCheatInvalidCharsRegex() {
		return "[^\\p{L}\\+\\?\\:\\p{N}]";
	}

}
