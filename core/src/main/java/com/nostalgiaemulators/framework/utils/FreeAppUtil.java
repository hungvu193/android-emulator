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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.ui.advertising.AdBlockerDetector;

public class FreeAppUtil {

	public static void startActivity(Activity activity, Intent intent) {
		if (check(activity)) {
			activity.startActivity(intent);
		}
	}

	public static void startActivityForResult(Activity activity, Intent intent,
			int requestCode) {
		if (check(activity)) {
			activity.startActivityForResult(intent, requestCode);
		}
	}

	public static boolean check(Activity activity) {
		return true;
	}

	private static void showOfflineDialog(final Activity activity) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				if (AdBlockerDetector.isBlockerInstalled(activity)) {
					builder.setMessage(R.string.adblocker_installed);
				} else {
					builder.setMessage(R.string.internet_needed);
				}
				DialogUtils.show(builder.create(), true);
			}
		});

	}
}
