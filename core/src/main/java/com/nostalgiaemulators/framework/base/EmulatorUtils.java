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

import java.io.File;

import android.content.Context;
import android.widget.Toast;

import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.FileUtils;

public class EmulatorUtils {

	public static boolean tryChangeWorkingDir(Context context, File parentDir) {
		boolean isReset = false;
		File fullWorkingDir = null;

		if (parentDir == null) {
			isReset = true;

			File baseDir = context.getExternalFilesDir(null);
			if (baseDir == null) {
				return false;
			}
			if (!baseDir.exists()) {
				if (!baseDir.mkdirs()) {
					return false;
				}
			}

			fullWorkingDir = baseDir;
		}

		if (!isReset) {
			fullWorkingDir = new File(parentDir.getAbsolutePath(), context
					.getApplicationContext().getPackageName());
			if (parentDir.getName().startsWith("com.nostalgiaemulators.")) {
				Toast.makeText(context, "Cannot create nested working dir",
						Toast.LENGTH_LONG).show();
				return false;
			}

			if (fullWorkingDir.getAbsolutePath().equals(getBaseDir(context))) {
				return false;
			}

			if (!fullWorkingDir.exists()) {

				if (!fullWorkingDir.mkdirs()) {
					PreferenceUtil.setWorkingDirParent(context, null);
					Toast.makeText(context,
							"Cannot create working dir in " + parentDir,
							Toast.LENGTH_LONG).show();
					return false;
				}
			}
		}

		boolean copyContent = PreferenceUtil.isWorkingDirCopyContent(context);
		if (copyContent) {
			File previousWorkingDir = new File(getBaseDir(context));
			File[] files = previousWorkingDir.listFiles();
			try {
				for (File file : files) {
					if (file.isDirectory()) {
						continue;
					}
					File newFile = new File(fullWorkingDir, file.getName());
					FileUtils.copyFile(file, newFile);
				}
				Toast.makeText(context,
						"Files succesfully copied to new working directory",
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(context,
						"Error copying files to new working directory",
						Toast.LENGTH_LONG).show();
			}
		}
		if (!isReset) {
			PreferenceUtil.setWorkingDirParent(context,
					parentDir.getAbsolutePath());
		} else {
			PreferenceUtil.setWorkingDirParent(context, null);
		}
		return true;
	}

	public static String getBaseDir(Context context) {
		String workingDirParent = PreferenceUtil.getWorkingDirParent(context);
		File dir = null;
		if (workingDirParent != null) {
			dir = new File(workingDirParent, context.getApplicationContext()
					.getPackageName());
		} else {
			dir = context.getExternalFilesDir(null);
		}

		if (dir == null) {
			throw new EmulatorException(
					com.nostalgiaemulators.framework.R.string.gallery_sd_card_not_mounted);
		}

		File baseDir = new File(dir.getAbsolutePath());

		if (!baseDir.exists()) {
			if (!baseDir.mkdirs()) {
				throw new EmulatorException(
						"could not create working directory");
			}
		}
		return baseDir.getAbsolutePath();
	}

}
