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
import java.util.HashMap;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nostalgiaemulators.framework.R;

public class HelpDialog extends TipsDialog {

	ArrayList<Integer> helpStringRIDs = new ArrayList<Integer>();
	HashMap<Integer, Boolean> includeLinkMap = new HashMap<Integer, Boolean>();

	private HelpDialog(Context context, int layoutRID, int nextBtnRID,
			int prevBtnRID, int labelRID, int counterRID, int tipContentRID,
			int bottomLineRID, int[] tipsLayoutRIDs) {
		super(context, layoutRID, nextBtnRID, prevBtnRID, labelRID, counterRID,
				tipContentRID, bottomLineRID, tipsLayoutRIDs);
	}

	public static HelpDialog create(Context context, int[] helpStringsIDs) {
		int[] layouts = new int[helpStringsIDs.length];

		for (int i = 0; i < layouts.length; i++)
			layouts[i] = R.layout.simple_text_tip;

		HelpDialog hd = new HelpDialog(context, R.layout.tips_dialog,
				R.id.tips_dialog_next, R.id.tips_dialog_prev,
				R.id.tips_dialog_label, R.id.tips_dialog_counter,
				R.id.tips_dialog_content, R.id.tips_dialog_bottom_line, layouts);
		hd.helpStringRIDs.clear();

		for (int id : helpStringsIDs) {
			hd.helpStringRIDs.add(id);
		}
		if (helpStringsIDs.length != 0)
			hd.createTip();
		return hd;
	}

	@Override
	public void initTip(int rid, View tipLayout) {
		if (helpStringRIDs != null) {
			TextView tv = (TextView) tipLayout;
			tv.setTypeface(font);

			int idx = getTipIdx();
			Integer id = helpStringRIDs.get(idx);
			if (id != null) {
				String txt = getContext().getString(id);
				boolean includeLink = txt.startsWith("$");
				if (txt.startsWith("$")) {
					txt = txt.substring(1);
				}
				if (includeLink) {
					tv.setText(Html.fromHtml(txt));
					tv.setMovementMethod(LinkMovementMethod.getInstance());
				} else {
					tv.setText(txt);
				}
			}
		}
	}

	@Override
	protected void initDialog() {
		super.initDialog();
		((Button) nextButton).setTypeface(font);
		((Button) prevButton).setTypeface(font);
		label.setTypeface(font);
		counter.setTypeface(font);
	}

	public void addHelp(int helpStringRID ) {

		addTip(R.layout.simple_text_tip);
		helpStringRIDs.add(helpStringRID);
		createTip();
	}

}
