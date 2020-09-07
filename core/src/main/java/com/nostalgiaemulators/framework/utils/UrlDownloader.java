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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.nostalgiaemulators.framework.ads.appnext.HttpClient;

public class UrlDownloader {
	private static final String TAG = "urldownloader";

	public static String download(String url) {

		HttpResponse httpResponse = null;
		OutputStream os = null;
		String data = null;
		try {
			DefaultHttpClient client = new HttpClient();
			Log.d(TAG, "downloading: " + url);
			HttpGet httpget = new HttpGet(url);
			httpResponse = client.execute(httpget);
			int code = httpResponse.getStatusLine().getStatusCode();

			if (code == HttpStatus.SC_OK) {
				os = new ByteArrayOutputStream();
				httpResponse.getEntity().writeTo(os);
				data = os.toString();
			} else {

				Log.e(TAG, "error code: " + code);
			}

		} catch (Exception e) {
			Log.e(TAG, url + " " + e.toString(), e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
		return data;
	}
}
