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

package com.nostalgiaemulators.framework.ads.appnext;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.utils.UrlDownloader;

public class Ad {
	private static final String TAG = "Ad";

	private Ad() {

	}

	public static Ad createFallbackAd(Context context) {
		Ad ad = new Ad();
		EmulatorApplication app = ((EmulatorApplication) context
				.getApplicationContext());

		ad.appUrl = app.getFullStoreUrl();
		int r = new Random().nextInt(3);
		switch (r) {
		case 0:
			ad.title = "Hate ads?";
			ad.description = "We have an ad-free version, too.";
			break;
		case 1:
			ad.title = "Do you like this emulator?";
			ad.description = "The full version is even better :) Check it out.";
			break;
		default:
			ad.title = "Remove ads";
			ad.description = "Purchase the Pro version to remove ads and support the continued development of the emu.";
			break;
		}

		return ad;
	}
	public static ArrayList<Ad> allFromJSON(String json) {
		if (json == null) {
			return null;
		}

		ArrayList<Ad> result = new ArrayList<Ad>();

		try {
			JSONObject jsonObj = new JSONObject(json);
			JSONArray array = jsonObj.getJSONArray("apps");
			int count = array.length();
			for (int i = 0; i < count; i++) {
				JSONObject o = (JSONObject) array.get(i);
				Ad ad = new Ad();
				ad.title = (String) o.get("title");
				ad.appUrl = (String) o.get("urlApp");
				if (o.has("pixelImp")) {
					ad.impressionUrl = (String) o.get("pixelImp");
				}
				ad.description = (String) o.get("desc");
				if (o.has("urlImg")) {
					ad.iconUrl = (String) o.get("urlImg");
				}
				if (o.has("supportedVersion")) {
					ad.supportedVersion = (String) o.get("supportedVersion");
				}
				if (o.has("androidPackage")) {
					ad.packageName = (String) o.get("androidPackage");
				}
				if (ad.isSupported()) {
					result.add(ad);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	public void reportImpression() {
		if (impressionUrl == null) {
			return;
		}
		impressionReported = true;
		new ReportImpressionTask().execute(impressionUrl);
	}

	private boolean impressionReported = false;

	private boolean canOpen() {
		boolean needsImpressionReporting = impressionUrl != null;
		return !needsImpressionReporting
				|| (needsImpressionReporting && impressionReported);
	}
	public void open(Activity context) {
		if (!canOpen()) {
			Log.e(TAG, "impression");
			return;
		}
		try {
			String url = appUrl;
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			context.startActivity(i);
		} catch (Exception e) {
			Log.e(TAG, "failed to open ad target url");
			e.printStackTrace();
		}
	}

	private static class ReportImpressionTask extends
			AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String impressionUrl = params[0];
			Log.d(TAG, "reporting impression");
			String result = UrlDownloader.download(impressionUrl);
			if (result == null) {
				Log.e(TAG, "failed to report impression");
			} else {
				Log.i(TAG, "impression successfully reported");
			}
			return null;
		}
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getPackageName() {
		return packageName;
	}

	private boolean isSupported() {
		String androidVersion = android.os.Build.VERSION.RELEASE;
		try {
			int compare = compareVersions(androidVersion, supportedVersion);
			if (compare == -1) {

				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

    // http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
	static int compareVersions(String version1, String version2) {
		version1 = version1.replaceAll("[^\\d.]", "");
		version2 = version2.replaceAll("[^\\d.]", "");
		Scanner s1 = new Scanner(version1);
		Scanner s2 = new Scanner(version2);
		s1.useDelimiter("\\.");
		s2.useDelimiter("\\.");

		while (s1.hasNextInt() && s2.hasNextInt()) {
			int v1 = s1.nextInt();
			int v2 = s2.nextInt();
			if (v1 < v2) {
				return -1;
			} else if (v1 > v2) {
				return 1;
			}
		}

		if (s1.hasNextInt())
			return 1;
		return 0;
	}

	private String appUrl;
	private String title;
	private String description;
	private String impressionUrl;
	private String iconUrl;
	private String supportedVersion;
	private String packageName;
}
