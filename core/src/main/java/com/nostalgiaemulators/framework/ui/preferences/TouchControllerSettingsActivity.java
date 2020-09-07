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

package com.nostalgiaemulators.framework.ui.preferences;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.SlotInfo;
import com.nostalgiaemulators.framework.base.EmulatorUtils;
import com.nostalgiaemulators.framework.base.GameMenu;
import com.nostalgiaemulators.framework.base.GameMenu.GameMenuItem;
import com.nostalgiaemulators.framework.base.GameMenu.OnGameMenuListener;
import com.nostalgiaemulators.framework.base.ImmersiveActivity;
import com.nostalgiaemulators.framework.base.SlotUtils;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchButton;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchLayer;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchLayer.EDIT_MODE;
import com.nostalgiaemulators.framework.utils.DatabaseHelper;
import com.nostalgiaemulators.framework.utils.FontUtil;

public class TouchControllerSettingsActivity extends ImmersiveActivity
		implements OnGameMenuListener {

	MultitouchLayer mtLayer;
	String gameHash = "";
	Bitmap lastGameScreenshot;
	private GameMenu gameMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controler_layout);

		gameMenu = new GameMenu(this, this);

		mtLayer = findViewById(R.id.touch_layer);

		Typeface font = FontUtil.createFontFace(this);
		if (font != null) {
			MultitouchButton start =  mtLayer
					.findViewById(R.id.button_start);
			MultitouchButton select =  mtLayer
					.findViewById(R.id.button_select);

			start.setTypeface(font);
			if (select != null) {
				select.setTypeface(font);
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Build.VERSION.SDK_INT >= 9) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
		mtLayer.setEditMode(EDIT_MODE.TOUCH);
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
		GameDescription game = dbHelper.selectObjFromDb(GameDescription.class,
				"where lastGameTime!=0 ORDER BY lastGameTime DESC LIMIT 1");

		GfxProfile gfxProfile;
		lastGameScreenshot = null;
		if (game != null) {
			SlotInfo info = SlotUtils.getSlot(EmulatorUtils.getBaseDir(this),
					game.checksum, 0);
			lastGameScreenshot = info.screenShot;
		}

		gfxProfile = PreferenceUtil.getLastGfxProfile(this);
		mtLayer.setLastgameScreenshot(lastGameScreenshot,
				gfxProfile == null ? null : gfxProfile.name);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mtLayer.saveEditElements();
		mtLayer.stopEditMode();
		if (lastGameScreenshot != null) {
			lastGameScreenshot.recycle();
			lastGameScreenshot = null;
		}
	}


	@Override
	public void onGameMenuCreate(GameMenu menu) {
		menu.add(R.string.act_tcs_reset, R.drawable.ic_restart);
	}

	@Override
	public void onGameMenuPrepare(GameMenu menu) {
	}

	@Override
	public void onGameMenuOpened(GameMenu menu) {
	}

	@Override
	public void onGameMenuClosed(GameMenu menu) {
	}

	@Override
	public void onGameMenuItemSelected(GameMenu menu, GameMenuItem item) {
		runOnUiThread(new Runnable() {
			public void run() {
				mtLayer.resetEditElement();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			openGameMenu();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public void openGameMenu() {
		gameMenu.open(true);
	}

	@Override
	public void openOptionsMenu() {
		gameMenu.open(true);
	}

}
