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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;
import android.view.KeyEvent;

import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.base.Migrator;
import com.nostalgiaemulators.framework.controllers.KeyboardController;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.Log;

public class KeyboardProfile implements Serializable {
	private static final long serialVersionUID = 5817859819275903370L;

	
	private static String[] sButtonNames;

	public static String[] getButtonNames() {
		if (sButtonNames == null) {
			sButtonNames = EmulatorInfoHolder.getInfo()
					.getDeviceKeyboardNames();
		}
		return sButtonNames;
	}

	private Boolean requiresTwoGamepads = null;

	public boolean requiresTwoGamepads() {
		if (requiresTwoGamepads != null) {
			return (boolean) requiresTwoGamepads;
		}

		for (int i = 0; i < keyMap.size(); i++) {
			int key = keyMap.keyAt(i);
			if (keyMap.get(key + KeyboardController.PLAYER2_OFFSET, -1) != -1) {
				requiresTwoGamepads = true;
				return true;
			}
		}
		requiresTwoGamepads = false;
		return false;
	}

	
	private static String[] sButtonDescriptions = null;

	public static String[] getButtonDescriptions() {
		if (sButtonDescriptions == null) {
			sButtonDescriptions = EmulatorInfoHolder.getInfo()
					.getDeviceKeyboardDescriptions();
		}
		return sButtonDescriptions;
	}

	
	private static int[] sButtonKeyEventCodes = null;

	public static int[] getButtonKeyEventCodes() {
		if (sButtonKeyEventCodes == null) {
			sButtonKeyEventCodes = EmulatorInfoHolder.getInfo()
					.getDeviceKeyboardCodes();
		}
		return sButtonKeyEventCodes;
	}

	private static final String KEYBOARD_PROFILES_SETTINGS = "keyboard_profiles_pref";

	private static final String KEYBOARD_PROFILE_POSTFIX = "_keyboard_profile";

	public static final String[] DEFAULT_PROFILES_NAMES = new String[] { "default" };

	public String name;
	public SparseIntArray keyMap = new SparseIntArray();
	public boolean loadedFromDisk;

	public static class PreferenceMigrator implements Migrator {

		@Override
		public void doExport(Context context, String baseDir) {
			migrate(PreferenceUtil.EXPORT, context, baseDir);
		}

		@Override
		public void doImport(Context context, String baseDir) {
			migrate(PreferenceUtil.IMPORT, context, baseDir);
		}

		private void migrate(int type, Context context, String baseDir) {
			File file = new File(baseDir, KEYBOARD_PROFILES_SETTINGS);
			SharedPreferences pref = context.getSharedPreferences(
					KEYBOARD_PROFILES_SETTINGS, Context.MODE_PRIVATE);
			PreferenceUtil.migratePreferences(type, pref, file,
					PreferenceUtil.NotFoundHandling.IGNORE);
			ArrayList<String> names = getProfilesNames(context);
			for (String name : names) {
				SharedPreferences keyPref = context.getSharedPreferences(name
						+ KEYBOARD_PROFILE_POSTFIX, Context.MODE_PRIVATE);

				PreferenceUtil.migratePreferences(type, keyPref, new File(
						baseDir, name + KEYBOARD_PROFILE_POSTFIX),
						PreferenceUtil.NotFoundHandling.IGNORE);

			}

		}

	}

	void setMapping(int player, int keyCode, int mapping) {
		int offset = player == 1 ? KeyboardController.PLAYER2_OFFSET : 0;
		keyMap.put(keyCode + offset, mapping + offset);
	}

	

	@SuppressLint("InlinedApi")
	public static KeyboardProfile createNes30Profile() {
		KeyboardProfile profile = new KeyboardProfile();
		profile.name = DEFAULT;
		for (int player = 0; player < 2; player++) {
			profile.setMapping(player, KeyEvent.KEYCODE_DPAD_LEFT,
					EmulatorController.KEY_LEFT);
			profile.setMapping(player, KeyEvent.KEYCODE_DPAD_RIGHT,
					EmulatorController.KEY_RIGHT);
			profile.setMapping(player, KeyEvent.KEYCODE_DPAD_UP,
					EmulatorController.KEY_UP);
			profile.setMapping(player, KeyEvent.KEYCODE_DPAD_DOWN,
					EmulatorController.KEY_DOWN);

			if (Build.VERSION.SDK_INT > 8) {
				profile.setMapping(player, KeyEvent.KEYCODE_BUTTON_A,
						EmulatorController.KEY_A);
				profile.setMapping(player, KeyEvent.KEYCODE_BUTTON_B,
						EmulatorController.KEY_B);
				profile.setMapping(player, KeyEvent.KEYCODE_BUTTON_X,
						EmulatorController.KEY_A_TURBO);
				profile.setMapping(player, KeyEvent.KEYCODE_BUTTON_Y,
						EmulatorController.KEY_B_TURBO);

				profile.setMapping(player, KeyEvent.KEYCODE_BUTTON_START,
						EmulatorController.KEY_START);
				profile.setMapping(player, KeyEvent.KEYCODE_BUTTON_SELECT,
						EmulatorController.KEY_SELECT);

				profile.setMapping(player, KeyEvent.KEYCODE_BUTTON_L1,
						KeyboardController.KEY_MENU);

				profile.setMapping(player, KeyEvent.KEYCODE_BUTTON_R1,
						KeyboardController.KEY_FAST_FORWARD);
			}
		}
		return profile;
	}

	public static KeyboardProfile createWiimoteProfile() {
		KeyboardProfile profile = new KeyboardProfile();
		profile.name = "wiimote";
		profile.keyMap.put(KeyEvent.KEYCODE_DPAD_LEFT,
				EmulatorController.KEY_LEFT);
		profile.keyMap.put(KeyEvent.KEYCODE_DPAD_RIGHT,
				EmulatorController.KEY_RIGHT);
		profile.keyMap.put(KeyEvent.KEYCODE_DPAD_UP, EmulatorController.KEY_UP);
		profile.keyMap.put(KeyEvent.KEYCODE_DPAD_DOWN,
				EmulatorController.KEY_DOWN);

		profile.keyMap.put(KeyEvent.KEYCODE_P, EmulatorController.KEY_START);

		profile.keyMap.put(KeyEvent.KEYCODE_M, EmulatorController.KEY_SELECT);
		profile.keyMap.put(KeyEvent.KEYCODE_1, EmulatorController.KEY_B);
		profile.keyMap.put(KeyEvent.KEYCODE_2, EmulatorController.KEY_A);

		profile.keyMap.put(23, KeyboardController.KEY_MENU);
		profile.keyMap.put(KeyEvent.KEYCODE_H, KeyboardController.KEY_BACK);

		profile.keyMap
				.put(KeyEvent.KEYCODE_O + KeyboardController.PLAYER2_OFFSET,
						EmulatorController.KEY_LEFT
								+ KeyboardController.PLAYER2_OFFSET);
		profile.keyMap.put(KeyEvent.KEYCODE_J
				+ KeyboardController.PLAYER2_OFFSET,
				EmulatorController.KEY_RIGHT
						+ KeyboardController.PLAYER2_OFFSET);
		profile.keyMap.put(KeyEvent.KEYCODE_I
				+ KeyboardController.PLAYER2_OFFSET, EmulatorController.KEY_UP
				+ KeyboardController.PLAYER2_OFFSET);
		profile.keyMap
				.put(KeyEvent.KEYCODE_K + KeyboardController.PLAYER2_OFFSET,
						EmulatorController.KEY_DOWN
								+ KeyboardController.PLAYER2_OFFSET);

		profile.keyMap.put(KeyEvent.KEYCODE_PLUS
				+ KeyboardController.PLAYER2_OFFSET,
				EmulatorController.KEY_START
						+ KeyboardController.PLAYER2_OFFSET);

		profile.keyMap.put(KeyEvent.KEYCODE_MINUS
				+ KeyboardController.PLAYER2_OFFSET,
				EmulatorController.KEY_SELECT
						+ KeyboardController.PLAYER2_OFFSET);

		profile.keyMap.put(KeyEvent.KEYCODE_COMMA
				+ KeyboardController.PLAYER2_OFFSET, EmulatorController.KEY_B);
		profile.keyMap.put(KeyEvent.KEYCODE_PERIOD
				+ KeyboardController.PLAYER2_OFFSET, EmulatorController.KEY_A);

		return profile;
	}

	public int getMapping(int player, int keyCode) {
		if (player == 0) {
			return keyMap.get(keyCode, -1);
		}
		int res = keyMap.get(keyCode + KeyboardController.PLAYER2_OFFSET, -1);
		return res;
	}

	public boolean delete(Context context) {
		Log.i(TAG, "delete profile " + name);
		SharedPreferences pref = context.getSharedPreferences(
				name + ".keyprof", Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.clear();
		editor.commit();

		pref = context.getSharedPreferences(KEYBOARD_PROFILES_SETTINGS,
				Context.MODE_PRIVATE);
		editor = pref.edit();
		editor.remove(name);
		editor.commit();

		return true;
	}

	public boolean save(Context context) {
		SharedPreferences pref = context.getSharedPreferences(name
				+ KEYBOARD_PROFILE_POSTFIX, Context.MODE_PRIVATE);
		Log.i(TAG, "save profile " + name + " " + keyMap);
		Editor editor = pref.edit();
		editor.clear();

		for (int i = 0; i < getButtonNames().length; i++) {
			int value = getButtonKeyEventCodes()[i];
			int idx = keyMap.indexOfValue(value);

			int key = idx == -1 ? 0 : keyMap.keyAt(idx);

			if (key != 0) {
				Log.i(TAG, "save " + getButtonNames()[i] + " " + key + "->"
						+ value);
				editor.putInt(key + "", value);
			}
		}
		editor.commit();

		
		if (!name.equals(DEFAULT)) {
			pref = context.getSharedPreferences(KEYBOARD_PROFILES_SETTINGS,
					Context.MODE_PRIVATE);
			editor = pref.edit();
			editor.putBoolean(name, true);
			editor.remove(DEFAULT);
			editor.commit();
		}
		return true;
	}

	public static final String DEFAULT = "default";

	public static KeyboardProfile getSelectedProfile(String gameHash,
			Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String name = pref.getString("pref_game_keyboard_profile", DEFAULT);
		return load(context, name);
	}

	private static final String TAG = "com.nostalgiaemulators.framework.KeyboardProfile";

	public static KeyboardProfile load(Context context, String name) {
		if (name != null) {
			SharedPreferences pref = context.getSharedPreferences(name
					+ KEYBOARD_PROFILE_POSTFIX, Context.MODE_PRIVATE);
			if (pref.getAll().size() != 0) {
				KeyboardProfile profile = new KeyboardProfile();
				profile.name = name;
				profile.loadedFromDisk = true;
				for (Entry<String, ?> entry : pref.getAll().entrySet()) {
					String key = entry.getKey();
					Integer value = (Integer) entry.getValue();
					int nkey = Integer.parseInt(key);
					int nvalue = value;

					if (nvalue >= KeyboardController.PLAYER2_OFFSET
							&& nkey < KeyboardController.PLAYER2_OFFSET) {
						nkey += KeyboardController.PLAYER2_OFFSET;
					}

					profile.keyMap.put(nkey, nvalue);
				}

				return profile;
			} else {
				return createNes30Profile();
			}
		} else {
			return createNes30Profile();
		}

	}

	
	public static ArrayList<String> getProfilesNames(Context context) {
		SharedPreferences pref = context.getSharedPreferences(
				KEYBOARD_PROFILES_SETTINGS, Context.MODE_PRIVATE);
		Set<String> prefNames = pref.getAll().keySet();
		ArrayList<String> names = new ArrayList<String>();

		for (String defName : DEFAULT_PROFILES_NAMES) {
			if (!prefNames.contains(defName))
				names.add(defName);
		}
		names.addAll(prefNames);
		return names;
	}

	public static boolean isDefaultProfile(String name) {
		boolean defProf = false;
		for (String defName : KeyboardProfile.DEFAULT_PROFILES_NAMES) {
			if (defName.equals(name)) {
				defProf = true;
			}
		}
		return defProf;
	}

	public static void restoreDefaultProfile(String name, Context context) {
		KeyboardProfile prof = null;
		if (name.equals(DEFAULT)) {
			prof = createNes30Profile();
		}
		if (prof != null) {
			prof.save(context);
		} else {
			Log.e(TAG, "Keyboard profile " + name + " is unknown!!");
		}

	}

}
