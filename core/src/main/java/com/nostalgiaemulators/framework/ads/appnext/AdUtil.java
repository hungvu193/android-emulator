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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgiaemulators.framework.R;

public class AdUtil {
	private static final String TAG = "AdUtil";

	public static void bindToView(View adsView, Ad ad) {
		if (adsView != null) {
			if (ad != null) {
				if (!AdFetcher.SHOW_ICON || ad.getIconUrl() == null) {
					adsView.setVisibility(View.VISIBLE);
				} else {
					adsView.setVisibility(View.GONE);
				}
				ad.reportImpression();

				TextView adsTitle = (TextView) adsView
						.findViewById(R.id.next_ads_title);
				TextView adsContent = (TextView) adsView
						.findViewById(R.id.next_ads_content);
				ImageView adsIcon = (ImageView) adsView
						.findViewById(R.id.next_ads_icon);

				if (ad.getTitle() != null) {
					adsTitle.setText(ad.getTitle());
				}

				if (ad.getDescription() != null) {
					adsContent.setText(ad.getDescription());
				}

				if (AdFetcher.SHOW_ICON) {
					if (ad.getIconUrl() != null) {
						adsIcon.setVisibility(View.VISIBLE);
						IconDownloadTask iconTask = new IconDownloadTask(
								adsView, adsIcon, ad.getIconUrl());
						iconTask.execute();
					} else {
						adsIcon.setVisibility(View.GONE);
					}
				} else {
					adsIcon.setVisibility(View.GONE);
				}

				adsView.setTag(ad);

				adsView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Context context = v.getContext();
						Object ad = v.getTag();
						if (context != null && context instanceof Activity
								&& ad != null && ad instanceof Ad) {
							((Ad) ad).open((Activity) context);
						}
					}
				});

			} else {
				adsView.setVisibility(View.GONE);
			}
		}
	}

	private static class IconDownloadTask extends AsyncTask<Void, Void, Bitmap> {
		WeakReference<ImageView> iconRef;
		WeakReference<View> adsViewRef;
		String url;

		public IconDownloadTask(View adsView, ImageView icon, String url) {
			this.adsViewRef = new WeakReference<View>(adsView);
			this.iconRef = new WeakReference<ImageView>(icon);
			this.url = url;
		}

		@Override
		protected void onPreExecute() {
			ImageView icon = iconRef.get();
			if (icon != null) {
				icon.setImageBitmap(null);
			}
		}

		Bitmap loadFromCache(String url) {
			if (adsViewRef.get() == null) {
				return null;
			}
			File cacheDir = adsViewRef.get().getContext().getExternalCacheDir();
			String imageName = url.substring(url.lastIndexOf('/') + 1);
			File cacheFile = new File(cacheDir, imageName);
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
			} finally {

			}
			return bitmap;
		}

		void saveToCache(Bitmap bitmap, String url) {
			if (adsViewRef.get() == null) {
				return;
			}
			File cacheDir = adsViewRef.get().getContext().getExternalCacheDir();
			String imageName = url.substring(url.lastIndexOf('/') + 1);
			File cacheFile = new File(cacheDir, imageName);

			FileOutputStream out = null;
			try {
				out = new FileOutputStream(cacheFile.getAbsolutePath());
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap = loadFromCache(url);

			if (bitmap == null) {
				try {
					bitmap = BitmapFactory.decodeStream((InputStream) new URL(
							url).getContent());

					saveToCache(bitmap, url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			ImageView icon = iconRef.get();
			View adsView = adsViewRef.get();
			if (adsView != null && icon != null) {
				if (result != null) {
					adsView.setVisibility(View.VISIBLE);
					icon.setVisibility(View.VISIBLE);
					icon.setImageBitmap(result);
				} else {
					AdFetcher.invalidate();
					icon.setVisibility(View.GONE);
				}
			}

		}

	}

}
