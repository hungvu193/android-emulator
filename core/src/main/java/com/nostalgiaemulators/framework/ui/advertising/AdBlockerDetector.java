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

import android.content.Context;
import android.content.pm.PackageManager;

public class AdBlockerDetector {

	private static Boolean result = null;

	private static boolean isAppInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			installed = false;
		}
		return installed;
	}

	public static boolean isBlockerInstalled(Context context) {
		if (result == null) {
			result = false;
			for (String packageName : BLOCKER_PACKAGE_NAMES) {
				if (isAppInstalled(context, packageName)) {
					result = true;
					break;
				}

			}
		}
		return result;
	}

	private static final String[] BLOCKER_PACKAGE_NAMES = {
			"pl.adblocker.free", "com.pasvante.adblocker",
			"de.ub0r.android.adBlock", "org.adblockplus.android",
			"com.bigtincan.android.adfree", "org.adaway",
			"org.czzsunset.adblock", "com.jrummy.apps.ad.blocker",
			"com.perlapps.MyInternetSecurity", "net.xdevelop.adblocker_t",
			"net.xdevelop.adblocker", "com.atejapps.advanishlite",
			"com.atejapps.advanish", };

}
