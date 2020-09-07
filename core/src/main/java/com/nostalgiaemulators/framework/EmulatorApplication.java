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

package com.nostalgiaemulators.framework;

import org.acra.ACRA;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

abstract public class EmulatorApplication extends Application {

	private static final String TAG = ".AppVersionChangeHandler";
	private static final String CURRENT_APP_VERSION = "app_version";
	private static final String PREVIOUS_APP_VERSION = "previous_app_version";
	private static final String PREF_NAME = "AppVersionChangeHandler.pref";
	public static final int FIRST_INSTALLATION = -1;

	private int previousVersion = -1;
	private int currentVersion = -1;
	private boolean isFirstRunAfterUpdate = false;
	private boolean isFirstRunEver = false;

	private void initVersionCodes() {
		SharedPreferences pref = getSharedPreferences(PREF_NAME, 0);

		int currentVersionPref = pref.getInt(CURRENT_APP_VERSION,
				FIRST_INSTALLATION);

		previousVersion = pref.getInt(PREVIOUS_APP_VERSION, FIRST_INSTALLATION);

		SharedPreferences.Editor editor = pref.edit();

		try {
			currentVersion = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
			if (currentVersionPref != currentVersion) {
				if (currentVersionPref == FIRST_INSTALLATION) {
					isFirstRunEver = true;
				} else {
					isFirstRunAfterUpdate = true;
				}
				previousVersion = currentVersionPref;

			}

		} catch (NameNotFoundException e) {
			Log.e(TAG, "Very weird fail", e);
		}
		editor.putInt(CURRENT_APP_VERSION, currentVersion);
		editor.putInt(PREVIOUS_APP_VERSION, previousVersion);
		editor.commit();

	}

	public int getPreviousVersionCode() {
		return previousVersion;
	}

	public int getCurrentVersionCode() {
		return currentVersion;
	}

	public void onCreate() {
		boolean debug = Utils.isDebuggable(this);
		Log.setDebugMode(debug);
		initVersionCodes();
		if (!debug) {
			try {
				ACRA.init(this);
				ApplicationInfo ai;
				try {
					ai = getPackageManager()
							.getApplicationInfo(this.getPackageName(),
									PackageManager.GET_META_DATA);

					Bundle bundle = ai.metaData;
					if (bundle != null) {
						githash = bundle.getString("svnversion");
						if (!debug) {

							if (githash != null) {
								ACRA.getErrorReporter().putCustomData(
										"svnversion", githash);
							}
						}
					}
				} catch (NameNotFoundException e) {

				}

			} catch (IllegalStateException e) {
			}
		}

		super.onCreate();
	}

	protected String githash;

	private Boolean isAdVersion = null;
	public final boolean isAdvertisingVersion() {
		if (isAdVersion == null) {
			isAdVersion = !getApplicationContext().getPackageName().endsWith("full");
		}
		return isAdVersion;
	}


	public int[] getGalleryHelpIds() {
		return new int[] { R.string.help_video_mode,
				R.string.help_dynamic_dpad, R.string.help_speed,
				R.string.help_customize_controll, R.string.help_state_share,
				R.string.help_cheats, R.string.help_sav_files, };
	}

	public int[] getCheatHelpIds() {
		return new int[] { R.string.help_cheats };
	}

	public int[] getSlotHelpIds() {
		return new int[] { R.string.help_state_share_detail };
	}

	public String getAppWallUrl() {
		return null;
	}

	abstract public String getPackFileSuffix();

	public String getStoreUrl() {
		return "https://play.google.com/store/apps/details?id="
				+ getPackageName();
	}

	public String getFullStoreUrl() {
		return "https://play.google.com/store/apps/details?id="
				+ getFullVersionPackage();
	}

	public String getAdUnitId() {
		return null;
	}

	public abstract AdProvider getAdProvider();

	
	public String getFullVersionPackage() {
		String pkg = getPackageName();
		if (pkg.endsWith("lite")) {
			pkg = pkg.substring(0, pkg.length() - 4);
			pkg += "full";
		}
		return pkg;
	}

	
	public abstract boolean hasGameMenu();

}
