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

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.base.ViewPort;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;

public class ZapperGun implements EmulatorController {

	public ZapperGun(Context context, EmulatorActivity activity) {
		this.context = context.getApplicationContext();
		this.emulatorActivity = activity;

	}

	@Override
	public void onResume() {

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

	}

	@Override
	public void onPause() {
	}

	@Override
	public void connectToEmulator(int port, Emulator emulator) {
		this.emulator = emulator;
	}

	private float startX;
	private float startY;
	private boolean startedInside = false;

	@Override
	public View getView() {
		return new View(context) {
			@Override
			public boolean onTouchEvent(MotionEvent event) {
				if (!isEnabled) {
					return true;
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					float x = event.getX();
					float y = event.getY();
					if (!startedInside
							&& (x >= minX && y >= minY && x <= maxX && y <= maxY)) {
						emulator.fireZapper(-1, -1);
					}
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (!inited) {
						ViewPort viewPort = emulatorActivity.getViewPort();
						if (viewPort == null) {
							return true;
						}
						minX = viewPort.x;
						minY = viewPort.y;
						maxX = minX + viewPort.width - 1;
						maxY = minY + viewPort.height - 1;
						vpw = viewPort.width;
						vph = viewPort.height;
						inited = true;
					}
					float x = event.getX();
					float y = event.getY();
					startedInside = false;
					if (x >= minX && y >= minY && x <= maxX && y <= maxY) {
						startedInside = true;
						float tx = (x - minX) / vpw;
						float ty = (y - minY) / vph;
						emulator.fireZapper(tx, ty);
					}
				}
				return true;
			}

		};
	}

	@Override
	public void onDestroy() {
		context = null;
		emulator = null;
		emulatorActivity = null;
	}

	private Context context;
	private Emulator emulator;
	private EmulatorActivity emulatorActivity;
	private float minX;
	private float maxX;
	private float minY;
	private float maxY;
	private float vpw;
	private float vph;
	private boolean inited = false;
	private boolean isEnabled = false;

	@Override
	public void onGameStarted(GameDescription game) {
		isEnabled = PreferenceUtil.isZapperEnabled(context, game.checksum);
	}

	@Override
	public void onGamePaused(GameDescription game) {

	}

}
