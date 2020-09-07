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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;

import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.R;

public class FileUtils {

	public static String loadTextFromResource(Context ctx, int id) {
		try {
			InputStream is = ctx.getResources().openRawResource(id);
			byte[] b = new byte[is.available()];
			is.read(b);
			return new String(b);
		} catch (Exception e) {
			return null;
		}
	}

	public static String readAsset(Context context, String asset)
			throws IOException {
		InputStream is = context.getAssets().open(asset);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		try {
			String line = null;
			StringBuffer buffer = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			return buffer.toString();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public static String getFileNameWithoutExt(File file) {
		String name = file.getName();
		int lastIdx = name.lastIndexOf(".");
		if (lastIdx == -1) {
			return name;
		}
		return name.substring(0, lastIdx);
	}

	public static void copyFile(File from, File to) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(from);
			copyFile(fis, to);
		} finally {
			if (fis != null)
				fis.close();

		}

	}

	public static void copyFile(InputStream is, File to) throws IOException {

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(to);
			int count = 0;
			byte[] buffer = new byte[1024];
			while ((count = is.read(buffer)) != -1) {
				fos.write(buffer, 0, count);
			}
		} finally {
			if (fos != null)
				fos.close();
		}

	}

	public static void cleanDirectory(File directory) throws IOException {
		if (directory != null) {
			File[] files = directory.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory()) {
					cleanDirectory(file);

				}
				file.delete();
			}
		}
	}

	public static void saveStringToFile(String text, File file)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(text.getBytes());
		fos.close();
	}

	public static String loadFileToString(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return sb.toString();
		} catch (IOException e) {
			return "";
		}
	}

	public static boolean isSDCardRWMounted() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	public static File getExternalCacheDir(Context context,
			boolean throwOnErrorAndNull) {
		boolean fail = false;
		File cacheDir = null;
		try {
			cacheDir = context.getExternalCacheDir();
		} catch (Exception e) {
			fail = true;
		}
		fail = fail || cacheDir == null;
		if (fail && throwOnErrorAndNull) {
			throw new EmulatorException(R.string.gallery_sd_card_not_mounted);
		}
		return cacheDir;
	}

}
