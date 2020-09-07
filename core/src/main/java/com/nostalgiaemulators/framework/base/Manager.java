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

import java.io.File;

import android.content.Context;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.EmulatorRunner;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.ui.cheats.Cheat;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.utils.FileUtils;

public class Manager extends EmulatorRunner {

	public Manager(Emulator emulator, Context context) {
		super(emulator, context);
		this.context = context.getApplicationContext();
	}

	public void setFastForwardEnabled(boolean enabled) {
		synchronized (lock) {
			emulator.setFastForwardEnabled(enabled);
		}
	}

	public void setFastForwardFrameCount(int frames) {
		synchronized (lock) {
			emulator.setFastForwardFrameCount(frames);
		}
	}

	public void copyAutoSave(int slot) {
		if (!emulator.isGameLoaded()) {
			throw new EmulatorException("game not loaded");
		}
		synchronized (lock) {
			String md5 = emulator.getLoadedGame().md5;
			String base = EmulatorUtils.getBaseDir(context);
			String source = SlotUtils.getSlotPath(base, md5, 0);
			String target = SlotUtils.getSlotPath(base, md5, slot);
			String sourcePng = SlotUtils.getScreenshotPath(base, md5, 0);
			String targetPng = SlotUtils.getScreenshotPath(base, md5, slot);
			try {
				FileUtils.copyFile(new File(source), new File(target));
				FileUtils.copyFile(new File(sourcePng), new File(targetPng));
			} catch (Exception e) {
				throw new EmulatorException(
						R.string.act_emulator_save_state_failed);
			}
		}
	}

	public int enableCheats(Context ctx, GameDescription game) {
		int numCheats = 0;
		for (String cheatChars : Cheat.getAllEnableCheats(ctx, game.checksum)) {
			if (cheatChars == null) {
				numCheats++;
			} else {

				if (cheatChars.contains(":")) {
					if (EmulatorInfoHolder.getInfo().supportsRawCheats()) {
						int[] rawValues = null;
						try {
							rawValues = Cheat.rawToValues(cheatChars);
						} catch (Exception e) {
							throw new EmulatorException(
									R.string.act_emulator_invalid_cheat,
									cheatChars);
						}
						if (rawValues != null) {
							enableRawCheat(rawValues[0], rawValues[1],
									rawValues[2]);
						}
					} else {
						throw new EmulatorException(
								R.string.act_emulator_invalid_cheat, cheatChars);
					}

				} else {
					enableCheat(cheatChars.toUpperCase(), numCheats);
				}
			}
		}
		return numCheats;

	}

	private Context context;
}
