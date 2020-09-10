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

import android.os.AsyncTask;
import android.util.Log;

import com.nostalgiaemulators.framework.utils.UrlDownloader;

import java.util.ArrayList;
import java.util.Random;

public class AdFetcher {

	public static final boolean SHOW_ICON = true;

	public static void fetchAd(String appNextId, AdListener listener,
			boolean canFetchInhouse) {
		AdFetcherTask task = new AdFetcherTask(appNextId, listener,
				canFetchInhouse);
		task.execute();
	}

	public static void invalidate() {
		AdFetcherTask.sLastFetchTime = 0;

	}

	private static class AdFetcherTask extends
			AsyncTask<Void, Void, ArrayList<Ad>> {

		private static final String TAG = "AdFetcherTask";
		private static String sLastJson = null;
		private static long sLastFetchTime = 0;
		private static String sLastInhouseJson = null;
		private boolean canFetchInhouse = false;

		public AdFetcherTask(String placementId, AdListener listener,
				boolean canFetchInhouse) {
			this.adListener = listener;
			this.placementId = placementId;
			this.canFetchInhouse = canFetchInhouse;
			Log.d(TAG, placementId + "");
		}

		private String placementId;

		@Override
		protected ArrayList<Ad> doInBackground(Void... params) {

			long minDelay = (60 * 30);

			boolean shouldFetchNewAds = (System.currentTimeMillis() - sLastFetchTime) / 1000 > minDelay;

			String json = null;
			String inhouseJson = null;

			inhouseJson = sLastInhouseJson;
			json = sLastJson;
			if (shouldFetchNewAds) {
				int numAds = 5;
				String url = RequestUrlBuilder.buildUrl(placementId, numAds,
						SHOW_ICON);
				json = UrlDownloader.download(url);
				if (json != null) {
					sLastFetchTime = System.currentTimeMillis();
				}
				sLastJson = json;
			}

			if (canFetchInhouse) {
				String url = "[ad url]";				inhouseJson = UrlDownloader.download(url);
				if (inhouseJson != null) {
					sLastInhouseJson = inhouseJson;
				}
			}

			ArrayList<Ad> appNextAds = Ad.allFromJSON(json);
			ArrayList<Ad> inhouseAds = null;
			if (canFetchInhouse) {
				inhouseAds = Ad.allFromJSON(inhouseJson);
			}
			ArrayList<Ad> result = new ArrayList<Ad>();
			if (appNextAds != null) {
				result.addAll(appNextAds);
			}
			if (inhouseAds != null) {
				result.addAll(inhouseAds);
			}
			return result;
		}

		AdListener adListener;

		@Override
		public void onPostExecute(ArrayList<Ad> result) {
			if (result != null && result.size() > 0) {
				int random = new Random().nextInt(result.size());

				Ad ad = result.get(random);

				adListener.onAdFetched(ad);
			} else {
				adListener.onFailedToFetchAd();
			}
		}

	}
}
