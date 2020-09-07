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

package com.nostalgiaemulators.nes;

import java.io.File;

import android.widget.Toast;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.base.EmulatorUtils;
import com.nostalgiaemulators.framework.base.GameMenu;
import com.nostalgiaemulators.framework.base.GameMenu.GameMenuItem;

public class NesEmulatorActivity extends EmulatorActivity {

	@Override
	public Emulator getEmulatorInstance() {
		return NesEmulator.getInstance();
	}

	@Override
	public void onGameMenuCreate(GameMenu menu) {
		menu.add(R.string.game_menu_fds_eject_disk);
		menu.add(R.string.game_menu_fds_insert_disk);
		menu.add(R.string.game_menu_fds_switch_side);
		super.onGameMenuCreate(menu);
	}

	@Override
	public void onGameMenuPrepare(GameMenu menu) {
		super.onGameMenuPrepare(menu);

		boolean isFds = (game.name.toLowerCase().endsWith(".fds"))
				&& isFdsInited();

		menu.getItem(R.string.game_menu_fds_eject_disk).setVisible(isFds);
		menu.getItem(R.string.game_menu_fds_eject_disk).setEnable(
				isDiskInserted());

		menu.getItem(R.string.game_menu_fds_insert_disk).setVisible(isFds);
		menu.getItem(R.string.game_menu_fds_insert_disk).setEnable(
				isDiskEjected());

		GameMenuItem switchSideItem = menu
				.getItem(R.string.game_menu_fds_switch_side);
		switchSideItem.setVisible(isFds);
		switchSideItem.setEnable(isDiskEjected() && getTotalSides() > 1);

	}

	@Override
	public void onGameMenuItemSelected(GameMenu menu, GameMenuItem item) {
		super.onGameMenuItemSelected(menu, item);
		try {
			if (item.getId() == R.string.game_menu_fds_eject_disk
					|| item.getId() == R.string.game_menu_fds_insert_disk) {
				manager.processCommand("FDS_INSERT_EJECT");
				int disk = getInsertedDisk();
				showFdsToast("Inserted", disk, "");
			} else if (item.getId() == R.string.game_menu_fds_switch_side) {
				manager.processCommand("FDS_SWITCH_SIDE");
				int disk = getSelectedDisk();
				showFdsToast("Selected", disk, " (not inserted)");
			}
		} catch (com.nostalgiaemulators.framework.EmulatorException e) {
			handleException(e);
		}
	}

	@Override
	protected boolean onFailedToLoadGame() {
		if (game != null && game.name.toLowerCase().endsWith(".fds")) {
			String disksysPath = EmulatorUtils.getBaseDir(this)
					+ "/disksys.rom";
			if (!new File(disksysPath).exists()) {

				showErrorAlert("In order to run FDS games, you need to provide the FDS BIOS file called 'disksys.rom'. Put it into any folder, tap 'Search Device For ROMs' and re-run the game.");

				return true;
			}
		}
		return false;
	}

	int getInsertedDisk() {
		if (!isFdsInited()) {
			return -1;
		}
		int insertedDisk = manager.getInt("FDS_INSERTED_DISK");
		return insertedDisk;
	}

	boolean isDiskInserted() {
		return isFdsInited() && getInsertedDisk() != 255;
	}

	boolean isDiskEjected() {
		return isFdsInited() && getInsertedDisk() == 255;
	}

	boolean isFdsInited() {
		return manager.getInt("FDS_INITED") == 1;
	}

	int getTotalSides() {
		if (!isFdsInited()) {
			return -1;
		}
		return manager.getInt("FDS_TOTAL_SIDES");
	}

	int getSelectedDisk() {
		if (!isFdsInited()) {
			return -1;
		}
		return manager.getInt("FDS_SELECTED_DISK");
	}

	void showFdsToast(final String prefix, final int disk, final String suffix) {
		String message = null;
		if (disk == 255) {
			message = "Disk ejected";
		} else {
			message = prefix + " disk: " + (disk >> 1) + " Side: "
					+ ((disk & 1) == 0 ? "A" : "B") + suffix;
		}
		final String msg = message;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(NesEmulatorActivity.this, msg, Toast.LENGTH_LONG)
						.show();
			}
		});

	}
}
