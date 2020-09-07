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

package com.nostalgiaemulators.framework.base;

import java.util.Calendar;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.UrlDownloader;

public class VersionChecker {

	private static final String TAG = "com.nostalgiaemulators.framework.base.VersionChecker";

	private VersionChecker() {
	}
	public static boolean isNewVersionAvailable(Context context) {

		try {
			boolean shouldCheckOnline = PreferenceUtil
					.canCheckNewVersion(context);

			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			int currentVersionCode = info.versionCode;
			int playStoreVersionCode = PreferenceUtil
					.getPlayStoreAppVersion(context);
			Log.d(TAG, "current app version: " + currentVersionCode);
			Log.d(TAG, "online app version: " + playStoreVersionCode);

			if (shouldCheckOnline) {
				downloadPlayStoreVersionCode(context);
			} else {
				Log.d(TAG, "skipping version check");
			}

			boolean needsUpdate = playStoreVersionCode > currentVersionCode;
			return needsUpdate;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return false;
	}

	private static void downloadPlayStoreVersionCode(Context context) {
		String url = "[versioncode url]";		new CheckVersionTask(context).execute(url);
	}

	private static class CheckVersionTask extends
			AsyncTask<String, Void, String> {

		public CheckVersionTask(Context context) {
			this.context = context.getApplicationContext();
		}

		private Context context;

		@Override
		protected String doInBackground(String... urls) {
			String url = urls[0];
			Log.d(TAG, "url: " + url);
			String response = UrlDownloader.download(url);
			return response;
		}

		@Override
		public void onPostExecute(String result) {

			try {
				if (result != null) {

					int playStoreVersionCode = Integer.parseInt(result);
					PreferenceUtil.setPlayStoreAppVersion(context,
							playStoreVersionCode);
					Log.d(TAG, "Play Store version: " + playStoreVersionCode);
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			} finally {
				try {
					Calendar c = Calendar.getInstance();
					c.add(Calendar.DAY_OF_MONTH, 3);
					PreferenceUtil.setNewVersionCheckTime(context,
							c.getTimeInMillis());
					Log.d(TAG, "next version check: " + c.toString());
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
		}
	}
}
