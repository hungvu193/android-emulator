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

package com.nostalgiaemulators.framework.ui.cheats;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nostalgiaemulators.framework.R;

public class CheatsListAdapter extends ArrayAdapter<Cheat> {

	LayoutInflater inflater;
	Typeface font;
	CheatsActivity cheatsActivity;

	public CheatsListAdapter(CheatsActivity context, List<Cheat> objects,
			Typeface font) {
		super(context, 0, objects);

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.font = font;
		this.cheatsActivity = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		TextView chars = null;
		TextView desc = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.row_cheat_list_item, null);
			chars = (TextView) convertView.findViewById(R.id.row_cheat_chars);
			desc = (TextView) convertView.findViewById(R.id.row_cheat_desc);

			chars.setTypeface(font);
			desc.setTypeface(font);
		} else {
			chars = (TextView) convertView.findViewById(R.id.row_cheat_chars);
			desc = (TextView) convertView.findViewById(R.id.row_cheat_desc);
		}

		final Cheat cheat = getItem(position);

		CheckBox enable = (CheckBox) convertView
				.findViewById(R.id.row_cheat_enable);
		ImageButton edit = (ImageButton) convertView
				.findViewById(R.id.row_cheat_edit);
		ImageButton remove = (ImageButton) convertView
				.findViewById(R.id.row_cheat_remove);

		chars.setText(cheat.chars);
		desc.setText(cheat.desc);

		enable.setOnCheckedChangeListener(null);
		enable.setChecked(cheat.enable);

		edit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cheatsActivity.editCheat(cheat);
			}
		});

		remove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cheatsActivity.removeCheat(cheat);
			}
		});

		enable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (position < 0 || position >= getCount()) {
					return;
				}
				cheat.enable = isChecked;
				cheatsActivity.saveCheats();
			}
		});

		return convertView;
	}

}
