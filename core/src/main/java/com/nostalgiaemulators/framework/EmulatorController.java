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

import android.view.View;

import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;

public interface EmulatorController {

	void onResume();

	void onPause();

	void onWindowFocusChanged(boolean hasFocus);

	void onGameStarted(GameDescription game);

	void onGamePaused(GameDescription game);

	void connectToEmulator(int port, Emulator emulator);

	View getView();

	void onDestroy();
	public static final int KEY_A = 0;
	public static final int KEY_B = 1;

	public static final int KEY_A_TURBO = 255;
	public static final int KEY_B_TURBO = 256;
	public static final int KEY_L = 2;
	public static final int KEY_R = 3;

	public static final int KEY_START = 4;
	public static final int KEY_SELECT = 5;
	public static final int KEY_UP = 6;
	public static final int KEY_DOWN = 7;
	public static final int KEY_LEFT = 8;
	public static final int KEY_RIGHT = 9;

	public static final int ACTION_DOWN = 0;
	public static final int ACTION_UP = 1;

}
