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

package com.nostalgiaemulators.framework.ui.tipsdialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.utils.FontUtil;

abstract public class TipsDialog extends Dialog {

	private LayoutInflater inflater;
	protected View nextButton;
	protected View prevButton;
	protected View content;
	protected View bottomLine;
	ViewGroup tipContent;
	private int tipIdx = 0;
	private ArrayList<Integer> tipsLayoutRIDs = new ArrayList<Integer>();
	protected Typeface font;
	protected TextView label, counter;

	
	public TipsDialog(Context context, int layoutRID, int nextBtnRID,
			int prevBtnRID, int labelRID, int counterRID, int tipContentRID,
			int bottomLineRID, int[] tipsLayoutRIDs) {
		super(context, R.style.GameDialogTheme);
		setCanceledOnTouchOutside(true);
		this.tipsLayoutRIDs.clear();
		for (int id : tipsLayoutRIDs)
			this.tipsLayoutRIDs.add(id);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		content = inflater.inflate(layoutRID, null);
		setContentView(content);

		font = FontUtil.createFontFace(context);

		nextButton = content.findViewById(nextBtnRID);
		if (nextButton == null) {
			throw new IllegalArgumentException("nextBtnRID is wrong reference");
		} else {
			nextButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					nextTip();
				}

			});
		}

		prevButton = content.findViewById(prevBtnRID);
		if (prevButton == null) {
			throw new IllegalArgumentException("prevBtnRID is wrong reference");
		} else {
			prevButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					prevTip();
				}

			});
		}

		tipContent = (ViewGroup) content.findViewById(tipContentRID);
		if (tipContent == null) {
			throw new IllegalArgumentException(
					"tipContentRID is wrong reference");
		}

		label = (TextView) content.findViewById(labelRID);
		counter = (TextView) content.findViewById(counterRID);
		bottomLine = content.findViewById(bottomLineRID);
		initDialog();
		if (this.tipsLayoutRIDs.size() != 0)
			createTip();
	}

	public void addTip(int layoutRID) {
		tipsLayoutRIDs.add(layoutRID);
	}

	
	public void nextTip() {
		tipIdx++;
		if (tipIdx >= tipsLayoutRIDs.size()) {
			tipIdx = 0;
		}
		createTip();
	}

	
	public void prevTip() {
		tipIdx--;
		if (tipIdx < 0) {
			tipIdx = tipsLayoutRIDs.size() - 1;
		}
		createTip();
	}

	
	protected void createTip() {

		if (tipsLayoutRIDs.size() == 1) {
			counter.setVisibility(View.GONE);
			prevButton.setVisibility(View.GONE);
			nextButton.setVisibility(View.GONE);
			bottomLine.setVisibility(View.GONE);
		} else {
			counter.setVisibility(View.VISIBLE);
			prevButton.setVisibility(View.VISIBLE);
			nextButton.setVisibility(View.VISIBLE);
			bottomLine.setVisibility(View.VISIBLE);
		}

		View oldTip = tipContent.getChildAt(0);
		if (oldTip != null) {
			Animation anim = AnimationUtils.loadAnimation(getContext(),
					android.R.anim.fade_out);
			oldTip.startAnimation(anim);
		}
		int rid = tipsLayoutRIDs.get(tipIdx);
		View tip = inflater.inflate(rid, null);
		if (tip != null) {
			initTip(rid, tip);
			tipContent.removeAllViews();
			tipContent.addView(tip);
			Animation anim = AnimationUtils.loadAnimation(getContext(),
					android.R.anim.fade_in);
			tip.startAnimation(anim);
		}

		if (counter != null) {
			counter.setText((tipIdx + 1) + "/" + tipsLayoutRIDs.size());
		}
	}

	
	public abstract void initTip(int rid, View tipLayout);

	protected void initDialog() {

	}

	public int getTipIdx() {
		return tipIdx;
	}

}
