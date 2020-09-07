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

import android.app.Activity;
import android.app.Application;
import android.graphics.Canvas;
import android.view.SurfaceView;
import android.view.View;

import com.nostalgiaemulators.framework.Emulator;

class UnacceleratedView extends SurfaceView implements EmulatorView {

	public UnacceleratedView(Activity context, Emulator emulator, int paddingTop) {
		super(context);
		this.emulator = emulator;
		this.context = context.getApplication();
		setWillNotDraw(false);
		this.paddingTop = paddingTop;
	}

	@Override
	public void onPause() {
	}

	private Application context;

	@Override
	public void onResume() {

	}

	@Override
	public void setQuality(int quality) {
	}

	@Override
	public View asView() {
		return this;
	}

	public ViewPort getViewPort() {
		return viewPort;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		ViewPort vp = ViewUtils.loadOrComputeViewPort(context, emulator, w, h,
				0, paddingTop, false);

		x = vp.x;
		y = vp.y;
		emulator.setViewPortSize(vp.width, vp.height);

		startTime = System.currentTimeMillis();
		viewPort = vp;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (emulator == null) {
			return;
		}
		long endTime = System.currentTimeMillis();
		long delay = DELAY_PER_FRAME - (endTime - startTime);
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}
		}

		startTime = System.currentTimeMillis();
		emulator.renderGfx();
		emulator.draw(canvas, x, y);

		invalidate();
	}

	private static final int DELAY_PER_FRAME = 40;
	private long startTime;
	private int x;
	private int y;
	private Emulator emulator;
	private int paddingTop;
	private int paddingLeft;
	private ViewPort viewPort;
}
