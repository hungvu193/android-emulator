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

package com.nostalgiaemulators.nes;

import android.util.Log;

import com.nostalgiaemulators.framework.AdProvider;
import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.utils.Utils;

public class NesApplication extends EmulatorApplication {

	private static final String TAG="NesApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			long crc = Utils.getCrc(getApplicationInfo().sourceDir,
					"classes.dex");

			String versionName = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
			int versionCode = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;

			Log.v(TAG, "Start Nostalgia.NES Pro vn:" + versionName + " vc:"
					+ versionCode + " cn:" + githash + " " + crc);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}

	}


	@Override
	public String getPackFileSuffix() {
		return NesEmulator.PACK_SUFFIX;
	}

	@Override
	public AdProvider getAdProvider() {
		return AdProvider.MOPUB;
	}

	@Override
	public boolean hasGameMenu() {
		return true;
	}
}
