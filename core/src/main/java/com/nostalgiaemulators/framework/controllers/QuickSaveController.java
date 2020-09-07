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

import androidx.core.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;

public class QuickSaveController implements EmulatorController {

	public QuickSaveController(EmulatorActivity activity,
			TouchController touchController) {
		this.activity = activity;
		this.touchController = touchController;
		this.gestureDetector = new GestureDetectorCompat(activity,
				new GestureListener());
	}

	TouchController touchController;
	EmulatorActivity activity;

	@Override
	public void onResume() {
		isEnabled = PreferenceUtil.isQuickSaveEnabled(activity);
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
	}

	private GestureDetectorCompat gestureDetector;

	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			float x = e.getX();
			if (x < screenCenterX) {
				activity.quickLoad();
			} else if (x > screenCenterX) {
				activity.quickSave();
			}
			return true;
		}
	}

	private int screenCenterX;
	private boolean isEnabled;

	@Override
	public View getView() {
		return new View(activity) {

			@Override
			protected void onSizeChanged(int w, int h, int oldw, int oldh) {
				super.onSizeChanged(w, h, oldw, oldh);
				screenCenterX = w / 2;
			}

			@Override
			public boolean onTouchEvent(MotionEvent event) {
				if (!isEnabled) {
					return true;
				}
				int pointerId = event.getPointerId(event.getActionIndex());
				if (!touchController.isPointerHandled(pointerId)) {
					return gestureDetector.onTouchEvent(event);
				}
				return true;
			}

		};

	}

	@Override
	public void onDestroy() {
		activity = null;
		touchController = null;
	}

}
