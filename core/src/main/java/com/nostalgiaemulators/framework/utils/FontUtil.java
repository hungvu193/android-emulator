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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Typeface;

import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;

public class FontUtil {
	private static final String TAG = "com.nostalgiaemulators.framework.utils.FontUtil";

	public static Typeface createFontFace(Context context) {
		return createFontFace("font.ttf", context, false);
	}

	public static Typeface createFontFace(Context context, boolean force) {
		return createFontFace("font.ttf", context, force);
	}

	public static Typeface createHintFontFace(Context context) {
		return createFontFace("font2.ttf", context, false);
	}

	private static Typeface createFontFace(String name, Context context,
			boolean force) {
		if (!force && PreferenceUtil.useSystemFont(context)) {
			return Typeface.DEFAULT;
		}

		File cacheDir = FileUtils.getExternalCacheDir(context, true);
		File file = new File(cacheDir, name);
		if (!file.exists()) {
			try {
				InputStream is = null;
				FileOutputStream fos = null;
				try {
					is = context.getAssets().open(name);
					fos = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int count = 0;
					while ((count = is.read(buffer)) != -1) {
						fos.write(buffer, 0, count);
					}
				} catch (IOException e) {
					Log.e(TAG, "", e);
				} finally {
					if (is != null) {
						is.close();
					}
					if (fos != null) {
						fos.close();
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
		}

		Typeface font = null;
		if (file.exists()) {
			try {
				font = Typeface.createFromFile(file);
			} catch (Exception e) {
				font = Typeface.DEFAULT;
			}
		} else {
			font = Typeface.DEFAULT;
		}

		return font;
	}

}
