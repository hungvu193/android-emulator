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

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Build;
import android.os.Debug;

public class MemoryUtil {
	private static final String TAG = "com.nostalgiaemulators.framework.utils.MemoryUtil";

	@SuppressLint("NewApi")
	public static void printMemoryInfo(Context context) {
		System.gc();

		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);

		long availableMegs = mi.availMem / 1048576L;
		Log.i(TAG, "Memory report " + context.getClass().getSimpleName());
		Log.i(TAG, "act available memory:" + availableMegs + "MB");
		Log.i(TAG,
				"memory: native heap alloc: "
						+ Debug.getNativeHeapAllocatedSize());
		Log.i(TAG, "memory: native heap free: " + Debug.getNativeHeapFreeSize());
		if (Build.VERSION.SDK_INT > 15) {
			long totalMegs = mi.totalMem / 1048576L;
			long usedMegs = totalMegs - availableMegs;
			Log.i(TAG, "act total memory:" + totalMegs + "MB");
			Log.i(TAG, "act used memory:" + usedMegs + "MB");
		}

		List<RunningAppProcessInfo> runningAppProcesses = activityManager
				.getRunningAppProcesses();

		for (RunningAppProcessInfo i : runningAppProcesses) {
			if (i.processName.equals(context.getPackageName())) {

				android.os.Debug.MemoryInfo[] mem = activityManager
						.getProcessMemoryInfo(new int[] { i.pid });
				Log.i(TAG, i.processName + " pss:"
						+ (mem[0].getTotalPss() / 1024) + "MB");
			}
		}

		try {
			Runtime info = Runtime.getRuntime();
			availableMegs = info.freeMemory() / 1048576L;
			long totalMegs = info.totalMemory() / 1048576L;
			long usedMegs = totalMegs - availableMegs;

			Log.i(TAG, "runtime available memory:" + availableMegs + "MB");
			Log.i(TAG, "runtime total memory:" + totalMegs + "MB");
			Log.i(TAG, "runtime used memory:" + usedMegs + "MB");
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
		Log.i(TAG, "----------------------------------------");
	}
}
