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

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;

public interface Emulator {

	EmulatorInfo getInfo();

	void start(GfxProfile cfg, SfxProfile sfx, EmulatorSettings settings);

	void setVolume(float volume);

	GfxProfile getActiveGfxProfile();

	SfxProfile getActiveSfxProfile();

	void reset();

	void saveState(int slot);

	void loadState(int slot);

	void loadHistoryState(int pos);

	void processCommand(String command);

	int getInt(String name);

	int getHistoryItemCount();

	void renderHistoryScreenshot(Bitmap bmp, int pos);

	void setBaseDir(String baseDir);

	void loadGame(String fileName, String batterySaveDir,
			String batterySaveFullPath);

	void onEmulationResumed();

	void onEmulationPaused();

	void enableCheat(String gg, int n);

	void enableRawCheat(int addr, int val, int comp);

	boolean isGameLoaded();

	GameInfo getLoadedGame();

	void setKeyPressed(int port, int key, boolean isPressed);

	void setTurboEnabled(int port, int key, boolean isEnabled);

	void setViewPortSize(int w, int h);

	void resetKeys();

	void fireZapper(float x, float y);

	void setFastForwardEnabled(boolean enabled);

	void setFastForwardFrameCount(int frames);

	void emulateFrame(int numFramesToSkip);

	void readSfxData();

	void renderSfx();

	void setFrameListener(FrameListener listener);

	void readPalette(int[] palette);

	void renderGfx();

	void renderGfxGL();

	void draw(Canvas canvas, int x, int y);

	void stop();

	boolean isReady();

	GfxProfile autoDetectGfx(GameDescription game);

	SfxProfile autoDetectSfx(GameDescription game);

}
