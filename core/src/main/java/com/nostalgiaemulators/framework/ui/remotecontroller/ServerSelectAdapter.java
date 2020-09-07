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

package com.nostalgiaemulators.framework.ui.remotecontroller;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoReceiver.DetectionResult;
import com.nostalgiaemulators.framework.utils.FontUtil;

public class ServerSelectAdapter extends ArrayAdapter<DetectionResult> {

	Typeface font;

	public ServerSelectAdapter(Context context, List<DetectionResult> objs) {
		super(context, R.layout.row_server_select_item, objs);
		font = FontUtil.createFontFace(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.row_server_select_item,
					null);
		}

		DetectionResult item = getItem(position);

		TextView label1 = (TextView) convertView
				.findViewById(R.id.row_select_server_label1);
		label1.setText(item.desc);
		label1.setTypeface(font);

		TextView label2 = (TextView) convertView
				.findViewById(R.id.row_select_server_label2);
		label2.setText(item.sessionDescription);
		label2.setVisibility(item.sessionDescription.equals("") ? View.GONE
				: View.VISIBLE);
		label2.setTypeface(font);

		ImageView icon = (ImageView) convertView
				.findViewById(R.id.row_select_server_icon);
		switch (item.type) {
		case mobile:
			icon.setImageResource(R.drawable.ic_mobile);
			break;
		case tablet:
			icon.setImageResource(R.drawable.ic_tablet);
			break;
		case tv:
			icon.setImageResource(R.drawable.ic_tv);
			break;
		}

		return convertView;
	}

}
