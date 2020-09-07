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

package com.nostalgiaemulators.framework.ui.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.utils.FontUtil;
import com.nostalgiaemulators.framework.utils.Log;

public class HintDialog extends Dialog {

	private static final String TAG = "com.nostalgiaemulators.framework.ui.widget.HintDialog";

	public HintDialog(Activity context, View anchor, int textRID) {
		super(context, R.style.HintTheme);
		RelativeLayout container = new RelativeLayout(context);

		View root = context.getWindow().getDecorView();

		int rootW = root.getMeasuredWidth();
		int rootH = root.getMeasuredHeight();

		int w = anchor.getMeasuredWidth();
		int h = anchor.getMeasuredHeight();
		w = h = w > h ? w : h;
		int x = getRelativeLeft(anchor, root);
		int y = getRelativeTop(anchor, root);

		Log.i(TAG, x + "x" + y + "-" + w + "x" + h);

		View v = new View(context);
		v.setBackgroundResource(R.drawable.hint_focus);
		RelativeLayout.LayoutParams params = new LayoutParams(w, h);
		params.topMargin = y;
		params.leftMargin = x;
		v.setLayoutParams(params);
		container.addView(v, params);

		View left = new View(context);
		params = new LayoutParams(x, rootH);
		left.setLayoutParams(params);
		left.setBackgroundResource(R.color.hint_bck_color);
		container.addView(left, params);

		View right = new View(context);
		params = new LayoutParams(rootW - (x + w), rootH);
		params.leftMargin = x + w;
		right.setLayoutParams(params);
		right.setBackgroundResource(R.color.hint_bck_color);
		container.addView(right, params);

		View top = new View(context);
		params = new LayoutParams(w, y);
		params.leftMargin = x;
		top.setLayoutParams(params);
		top.setBackgroundResource(R.color.hint_bck_color);
		container.addView(top, params);

		View bottom = new View(context);
		params = new LayoutParams(w, rootH - (y + h));
		params.leftMargin = x;
		params.topMargin = y + h;
		bottom.setLayoutParams(params);
		bottom.setBackgroundResource(R.color.hint_bck_color);
		container.addView(bottom, params);

		Typeface font = FontUtil.createHintFontFace(context);

		Resources res = context.getResources();
		int size = res.getDimensionPixelSize(R.dimen.slot_hint_text_size);

		TextView tv = new TextView(context);
		tv.setTextColor(res.getColor(R.color.ads_color));
		tv.setText(textRID);
		tv.setTextSize(size);
		tv.setTypeface(font);
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.topMargin = y + h + size;
		params.leftMargin = size;
		params.rightMargin = size;
		tv.setLayoutParams(params);
		container.addView(tv, params);

		TextView skip = new TextView(context);
		skip.setTextColor(res.getColor(R.color.ads_color));
		skip.setText("OK");
		skip.setPadding(size * 2, size * 2, size * 2, size * 2);
		skip.setTextSize(size);
		skip.setTypeface(font);
		params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.rightMargin = size;
		params.bottomMargin = size;

		skip.setLayoutParams(params);
		skip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		skip.setBackgroundResource(R.drawable.hint_button_bck);
		skip.setGravity(Gravity.CENTER);

		container.addView(skip, params);

		setContentView(container);

	}

	private int getRelativeLeft(View myView, View rootView) {
		if (myView.getParent() == rootView)
			return myView.getLeft();
		else
			return myView.getLeft()
					+ getRelativeLeft((View) myView.getParent(), rootView);
	}

	private int getRelativeTop(View myView, View rootView) {
		if (myView.getParent() == rootView)
			return myView.getTop();
		else
			return myView.getTop()
					+ getRelativeTop((View) myView.getParent(), rootView);
	}

}
