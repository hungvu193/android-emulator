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

package com.nostalgiaemulators.framework.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.view.Window;
import android.webkit.WebView;

import com.nostalgiaemulators.framework.R;

public class WhatIsNewDialog extends Dialog {

	public static final String EXTRA_WHAT_IS_NEW_LAST_VERSION = "EXTRA_WHAT_IS_NEW_LAST_VERSION";
	public static final String RESOURCE_PREFIX = "whats_new_v";

	private static final String TAG = "com.nostalgiaemulators.framework.utils.WhatIsNewDialog";

	private int version = -1;
	private Resources res;
	private int resID = 0;

	public WhatIsNewDialog(Context context) {
		super(context);
		getWindow();
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		res = context.getResources();
		try {
			version = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "", e);
		}
		resID = res.getIdentifier(RESOURCE_PREFIX + version, "string",
				context.getPackageName());
		if (resID != 0) {
			int color = res.getColor(R.color.main_color);
			int color2 = res.getColor(R.color.ads_color);
			String str = "<span style='color: #"
					+ Integer.toHexString(color & 0xffffff)
					+ ";  background-color: #000000; font:bold 1em Arial'>"
					+ "<h1 style='color:"
					+ Integer.toHexString(color2 & 0xffffff) + "'>"
					+ res.getString(R.string.whats_new_title) + "</h1>"
					+ res.getText(resID).toString() + "</span>";
			WebView webView = new WebView(context);
			webView.setBackgroundColor(0xff000000);
			webView.loadDataWithBaseURL(null, str, "text/html", "utf-8", null);
			setContentView(webView);
		}
	}

	public boolean check() {
		if (resID == 0) {
			return false;
		} else {

			SharedPreferences pref = getContext().getSharedPreferences(
					"what_is_new", Context.MODE_PRIVATE);
			int last = pref.getInt(EXTRA_WHAT_IS_NEW_LAST_VERSION, -1);
			if (last < version) {
				Editor editor = pref.edit();
				editor.putInt(EXTRA_WHAT_IS_NEW_LAST_VERSION, version);
				editor.commit();
				return true;
			} else {
				return false;
			}
		}
	}

}
