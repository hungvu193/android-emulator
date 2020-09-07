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

package com.nostalgiaemulators.framework.ui.multitouchbutton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

public class MultitouchButton extends Button implements MultitouchBtnInterface {
	OnMultitouchEventListener listener;

	public MultitouchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MultitouchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void onTouchEnter(MotionEvent event) {
		setPressed(true);
		if (listener != null)
			listener.onMultitouchEnter(this);
	}

	public void onTouchExit(MotionEvent event) {
		setPressed(false);
		if (listener != null)
			listener.onMultitouchExit(this);
	}

	public void setOnMultitouchEventlistener(OnMultitouchEventListener listener) {
		this.listener = listener;
	}

	protected boolean repaint = true;

	@Override
	public void requestRepaint() {
		repaint = true;
	}

	@Override
	public void removeRequestRepaint() {
		repaint = false;
	}

	@Override
	public boolean isRepaintState() {
		return repaint;
	}

	@Override
	public void invalidate() {
		super.invalidate();

	}

}
