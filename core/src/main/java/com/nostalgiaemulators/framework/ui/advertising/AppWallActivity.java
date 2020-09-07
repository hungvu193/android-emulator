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

package com.nostalgiaemulators.framework.ui.advertising;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.utils.DialogUtils;

@SuppressLint("SetJavaScriptEnabled")
public class AppWallActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_wall);
		WebView view = (WebView) findViewById(R.id.appwallwebview);
		client = new Client();
		view.setWebViewClient(client);
		view.setBackgroundColor(Color.TRANSPARENT);
		view.getSettings().setJavaScriptEnabled(true);
		view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		view.loadUrl(((EmulatorApplication) getApplication()).getAppWallUrl());

	}

	@Override
	protected void onPause() {
		super.onPause();
		client.onPause();
	}

	private Client client;

	private class Client extends WebViewClient {

		public void onPause() {
			if (dialog != null) {
				dialog.dismiss();
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (!AppWallActivity.this.isFinishing()) {
						dialog = new ProgressDialog(AppWallActivity.this);
						dialog.setMessage("Loading...");
						DialogUtils.show(dialog, true);
					}
				}
			});
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (dialog != null) {
						dialog.dismiss();
						dialog = null;
					}
				}

			});
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}

		private ProgressDialog dialog;
	}
}
