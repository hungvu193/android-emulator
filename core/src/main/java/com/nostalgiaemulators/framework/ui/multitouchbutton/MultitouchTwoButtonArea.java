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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nostalgiaemulators.framework.R;

public class MultitouchTwoButtonArea extends MultitouchImageButton {

	public MultitouchTwoButtonArea(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public MultitouchTwoButtonArea(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		if (!isInEditMode()) {
			setVisibility(View.INVISIBLE);
		}
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.MultitouchTwoButtonArea, 0, 0);

		try {
			firstButtonRID = a.getResourceId(
					R.styleable.MultitouchTwoButtonArea_first_button, -1);
			secondButtonRID = a.getResourceId(
					R.styleable.MultitouchTwoButtonArea_second_button, -1);
		} finally {
			a.recycle();
		}

	}

	@Override
	public void onTouchEnter(MotionEvent event) {
		if (holder.firstButton == null) {
			initHolder();
		}
		holder.firstButton.onTouchEnter(event);
		holder.secondButton.onTouchEnter(event);
	}

	@Override
	public void onTouchExit(MotionEvent event) {
		if (holder.firstButton == null) {
			initHolder();
		}
		holder.firstButton.onTouchExit(event);
		holder.secondButton.onTouchExit(event);
	}

	private void initHolder() {
		holder.firstButton = (MultitouchBtnInterface) getRootView()
				.findViewById(firstButtonRID);

		holder.secondButton = (MultitouchBtnInterface) getRootView()
				.findViewById(secondButtonRID);
	}

	protected int firstButtonRID = -1;
	protected int secondButtonRID = -1;
	private ViewHolder holder = new ViewHolder();

	private static class ViewHolder {
		public MultitouchBtnInterface firstButton;
		public MultitouchBtnInterface secondButton;
	}

	public int getFirstBtnRID() {
		return firstButtonRID;
	}

	public int getSecondBtnRID() {
		return secondButtonRID;
	}

	@Override
	public void requestRepaint() {
		super.requestRepaint();
		holder.firstButton.requestRepaint();
		holder.secondButton.requestRepaint();
	}
}
