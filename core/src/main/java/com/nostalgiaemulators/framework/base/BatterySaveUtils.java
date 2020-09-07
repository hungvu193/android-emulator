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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import android.content.Context;
import android.util.Log;

import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.utils.FileUtils;
import com.nostalgiaemulators.framework.utils.Utils;
public class BatterySaveUtils {
	private BatterySaveUtils() {

	}

	public static void createSavFileCopyIfNeeded(Context context,
			String gameFilePath) {
		createFileCopyIfNeeded(context, gameFilePath, ".sav");
		createFileCopyIfNeeded(context, gameFilePath, ".fds.sav");
	}

	private static void createFileCopyIfNeeded(Context context,
			String gameFilePath, String ext) {
		File gameFile = new File(gameFilePath);
		File batterySavFile = new File(gameFile.getParent(),
				Utils.stripExtension(gameFile.getName()) + ext);

		if (!batterySavFile.exists()) {
			return;
		}
		if (batterySavFile.canWrite()) {
			return;
		}
		String sourceMD5 = Utils.getMD5Checksum(batterySavFile, false);
		if (needsRewrite(context, batterySavFile, sourceMD5)) {
			File copyFile = new File(EmulatorUtils.getBaseDir(context),
					batterySavFile.getName());
			try {

				FileUtils.copyFile(batterySavFile, copyFile);
				saveMD5Meta(context, batterySavFile, sourceMD5);
			} catch (Exception e) {
			}
		}
	}

	private static void saveMD5Meta(Context context, File batterySavFile,
			String md5) {
		File metaFile = getMetaFile(context, batterySavFile);
		FileWriter fw = null;
		try {
			metaFile.delete();
			metaFile.createNewFile();
			fw = new FileWriter(metaFile);
			fw.write(md5);
		} catch (Exception e) {
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (Exception e) {
			}
		}
	}
	private static boolean needsRewrite(Context context,
			File sourceBatteryFile, String sourceMD5) {
		String previousSourceMD5 = null;
		File metaFile = getMetaFile(context, sourceBatteryFile);
		File targetFile = new File(EmulatorUtils.getBaseDir(context),
				sourceBatteryFile.getName());
		if (!metaFile.exists() || !targetFile.exists()) {
			return true;
		} else {
			FileReader fileReader = null;
			BufferedReader br = null;
			try {
				fileReader = new FileReader(metaFile);
				br = new BufferedReader(fileReader);
				previousSourceMD5 = br.readLine();
			} catch (Exception e) {
				return true;
			} finally {
				try {
					if (fileReader != null) {
						fileReader.close();
					}
					if (br != null) {
						br.close();
					}
				} catch (Exception e) {
				}
			}
		}
		Log.d("MD5", "source: " + sourceMD5 + " old: " + previousSourceMD5);
		return !sourceMD5.equals(previousSourceMD5);
	}

	private static File getMetaFile(Context context, File batterySavFile) {
		return new File(EmulatorUtils.getBaseDir(context),
				batterySavFile.getName() + ".meta");
	}

	public static String getBatterySaveDir(Context context, String gameFilePath) {
		File f = new File(gameFilePath);
		String directory = f.getParent();
		String batteryPath = directory;
		boolean isWriteable = new File(batteryPath).canWrite();
		if (context.getExternalCacheDir() == null) {
			throw new EmulatorException(R.string.gallery_sd_card_not_mounted);
		}
		if (!isWriteable
				|| directory.equals(context.getExternalCacheDir()
						.getAbsolutePath())) {
			batteryPath = EmulatorUtils.getBaseDir(context);
		}

		return batteryPath;
	}

}
