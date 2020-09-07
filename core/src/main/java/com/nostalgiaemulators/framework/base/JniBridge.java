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

package com.nostalgiaemulators.framework.base;

import android.graphics.Bitmap;

public class JniBridge {

	public native boolean setBaseDir(String path);

	public native boolean start(int gfx, int sfx, int general);

	public native boolean reset();

	public native boolean loadGame(String fileName, String batteryDir,
			String strippedName);

	public native boolean loadState(String fileName, int slot);

	public native boolean processCommand(String command);

	public native int getInt(String name);

	public native boolean saveState(String fileName, int slot);

	public native int readSfxBuffer(short[] data);

	public native boolean enableCheat(String gg, int type);

	public native boolean enableRawCheat(int addr, int val, int comp);

	public native boolean fireZapper(int x, int y);

	public native boolean render(Bitmap bitmap);

	public native boolean renderVP(Bitmap bitmap, int vw, int vh);

	public native boolean renderHistory(Bitmap bitmap, int item, int vw, int vh);

	public native boolean renderGL();

	public native boolean emulate(int keys, int turbos, int numFramesToSkip);

	public native boolean readPalette(int[] result);

	public native boolean setViewPortSize(int w, int h);

	public native boolean stop();

	public native int getHistoryItemCount();

	public native boolean loadHistoryState(int pos);

}
