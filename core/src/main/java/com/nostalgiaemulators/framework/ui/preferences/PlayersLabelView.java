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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class PlayersLabelView extends View {

	public PlayersLabelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PlayersLabelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlayersLabelView(Context context) {
		this(context, null, 0);
	}

	Paint paint = new Paint();
	float textSize = 0;
	int[] offsets = new int[] { 0, 300, 800 };
	int offset = 0;

	private void init() {
		paint.setColor(0xffffffff);
		Resources r = getResources();
		float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				25, r.getDisplayMetrics());
		paint.setTextSize(textSize);
		paint.setAntiAlias(true);
	}

	public void setPlayersOffsets(int[] offsets) {
		this.offsets = offsets;
		invalidate();
	}

	public void setOffset(int offset) {
		this.offset = offset;
		invalidate();
	}

	private static final String TAG = "com.nostalgiaemulators.framework.ui.preferences.PlayersLabelView";

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.save();
		canvas.translate(0, 40);
		canvas.rotate(-90, 0, 0);
		for (int i = 0; i < offsets.length; i++) {
			String label = "PLAYER " + (i + 1);
			float width = paint.measureText(label);
			int off = (int) (offset - width - offsets[i] + 40);
			boolean active = false;
			if (i < (offsets.length - 1)) {
				active = offsets[i] <= offset && offset < offsets[i + 1];
			} else {
				active = offsets[i] <= offset && offset < offsets[i] + 20000;
			}

			if (active && (offset > (40 - width)))
				off = (int) (40 - width);

			paint.setColor(0xff000000);
			paint.setStyle(Style.FILL);
			canvas.drawRect(off - 2, 0, off + width, getMeasuredWidth(), paint);
			paint.setColor(0xffffffff);
			canvas.drawText(label, off, 40, paint);

		}
		canvas.restore();
	}

}
