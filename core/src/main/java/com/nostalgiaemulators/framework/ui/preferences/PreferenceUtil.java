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

package com.nostalgiaemulators.framework.ui.preferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.opengl.GLES20;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.base.ViewPort;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;

public class PreferenceUtil {

	public static final int EXPORT = 1;
	public static final int IMPORT = 2;

	private static String escapedI = "{escapedI:-)}";
	private static String escapedN = "{escapedN:-)}";
	private static String escapedNull = "{escapedNULL:-)}";

	public enum NotFoundHandling {
		IGNORE, FAIL,
	}

	public static boolean isImmersiveModeEnabled(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean res = pref.getBoolean("general_pref_immersive_mode", false);
		return res;
	}

	public static boolean isBatterySaveBugFixed(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("bs_bug_fixed", false);
	}

	public static void setBatterySaveBugFixed(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("bs_bug_fixed", true);
		editor.commit();
	}

	public static void setPlayStoreAppVersion(Context context, int versionCode) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putInt("__online_app_version", versionCode);
		editor.commit();
	}

	public static int getPlayStoreAppVersion(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int res = pref.getInt("__online_app_version", -1);
		return res;
	}

	public static boolean canRestart(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		long lastTime = pref.getLong("__last_restart_time", 0);
		return System.currentTimeMillis() - lastTime > 5 * 1000;
	}

	public static void setLastRestartTime(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();
		edit.putLong("__last_restart_time", System.currentTimeMillis());
		edit.commit();
	}

	public static void setNewVersionCheckTime(Context context, long timeInMillis) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putLong("__new_version_check_time", timeInMillis);
		editor.commit();
	}

	public static boolean canCheckNewVersion(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long checkTime = pref.getLong("__new_version_check_time", -1);
		return currentTime > checkTime;
	}

	public static boolean canRemind(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long checkTime = pref.getLong("__remind_time", -1);
		return currentTime > checkTime;
	}

	public static void setRemindTime(Context context, long timeInMillis) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putLong("__remind_time", timeInMillis);
		editor.commit();
	}

	public static boolean isQuickSaveEnabled(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_quicksave", false);
	}

	public static int getFastForwardFrameCount(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int speed = pref.getInt("general_pref_ff_speed", 4);
		int res = 0;
		res = (speed + 1) * 2;
		return res;
	}

	public static int getFiltering(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean isFiltering = pref.getBoolean("general_pref_smoothing", false);
		int res = isFiltering ? GLES20.GL_LINEAR : GLES20.GL_NEAREST;
		return res;
	}

	public static void migratePreferences(int type, SharedPreferences pref,
			File file) {
		migratePreferences(type, pref, file, NotFoundHandling.FAIL);
	}

	public static long getControllerLayoutTimestamp(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		long timestamp = pref.getLong("general_cl_timestamp", 0);
		return timestamp;
	}

	public static void setControllerLayoutTimestamp(Context context, long value) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();
		edit.putLong("general_cl_timestamp", value);
		edit.commit();
	}

	public static void migratePreferences(int type, SharedPreferences pref,
			File file, NotFoundHandling handling) {
		if (type == EXPORT) {
			exportPreferences(pref, file);
		} else if (type == IMPORT) {
			importPreferences(pref, file, handling);
		} else
			throw new IllegalArgumentException();
	}

	public static void copyPreferences(SharedPreferences source,
			SharedPreferences target) {
		Map<String, ?> prefs = source.getAll();
		if (prefs.size() == 0) {
			return;
		}

		Editor editor = target.edit();
		for (Entry<String, ?> entry : prefs.entrySet()) {
			Object o = entry.getValue();
			if (o.getClass() == Integer.class) {
				editor.putInt(entry.getKey(), (Integer) o);
			}
			if (o.getClass() == Long.class) {
				editor.putLong(entry.getKey(), (Long) o);
			}
			if (o.getClass() == String.class) {
				editor.putString(entry.getKey(), (String) o);
			}
			if (o.getClass() == Float.class) {
				editor.putFloat(entry.getKey(), (Float) o);
			}
			if (o.getClass() == Boolean.class) {
				editor.putBoolean(entry.getKey(), (Boolean) o);
			}
		}
		editor.commit();
	}

	public static void exportPreferences(SharedPreferences pref, File file) {
		Map<String, ?> prefs = pref.getAll();
		if (prefs.size() == 0) {
			return;
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file);
			for (Entry<String, ?> entry : prefs.entrySet()) {
				Object o = entry.getValue();
				String type = null;
				Object value = entry.getValue();
				if (o.getClass() == Integer.class) {
					type = "I";
				}
				if (o.getClass() == Long.class) {
					type = "L";
				}
				if (o.getClass() == String.class) {
					type = "S";

					boolean cn = pref.getBoolean("__cn", false);
					String val = (String) value;
					val += cn ? "|" : "";
					val = val.replace("|", escapedI);
					val = val.replace("\n", escapedN);
					value = val;
				}
				if (o.getClass() == Float.class) {
					type = "F";
				}
				if (o.getClass() == Boolean.class) {
					type = "B";
				}
				if (type == null) {
					throw new RuntimeException("unknown type");
				}
				if (value == null) {
					value = escapedNull;
				}
				String name = entry.getKey();
				if (!name.startsWith("__")) {
					name = name.replace("|", escapedI);
					writer.write(type + "|" + name + "|" + value + "\n");
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	public static void importPreferences(SharedPreferences pref, File file,
			NotFoundHandling handling) {
		if (handling == NotFoundHandling.IGNORE && !file.exists()) {
			return;
		}
		BufferedReader reader = null;
		try {
			FileReader r = new FileReader(file);
			reader = new BufferedReader(r);
			String line = null;
			Editor editor = pref.edit();

			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\\|");
				String type = parts[0];
				String name = parts[1];

				name = name.replace(escapedI, "|");

				if (parts.length < 3) {
					Log.e("IMPORT", "chybi value pro " + name);
					continue;
				}
				String value = parts[2];

				Log.i("IMPORT", name + " --> " + value);

				if (value.equals(escapedNull)) {
					value = null;
				}
				if (type.equals("I")) {
					editor.putInt(name, value != null ? Integer.parseInt(value)
							: null);
				}
				if (type.equals("B")) {
					editor.putBoolean(name,
							value != null ? Boolean.parseBoolean(value) : null);
				}
				if (type.equals("F")) {
					editor.putFloat(name,
							value != null ? Float.parseFloat(value) : null);
				}
				if (type.equals("L")) {
					editor.putLong(name, value != null ? Long.parseLong(value)
							: null);
				}
				if (type.equals("S")) {
					if (value != null) {
						value = value.replace(escapedI, "|");
						value = value.replace(escapedN, "\n");
					}
					editor.putString(name, value);
				}

			}
			editor.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
				}
			}
		}

	}

	public static void setLastAdJSON(Context context, String json) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putString("__last_ad_json", json);
		editor.commit();
	}

	public static String getLastAdJSON(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getString("__last_ad_json", null);
	}

	public static void setLastInhouseAdTime(Context context, long time) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putLong("__last_inhouse_time", time);
		editor.commit();
	}

	public static long getLastInhouseAdTime(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getLong("__last_inhouse_time", 0);
	}

	public static boolean useSystemFont(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_use_system_font", false);
	}

	public static void setUseSystemFont(Context context, boolean value) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("general_pref_use_system_font", value);
		editor.commit();
	}

	public static boolean lastUseSystemFont(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("__last_use_system_font", false);
	}

	public static void setLastUseSystemFont(Context context, boolean value) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("__last_use_system_font", value);
		editor.commit();
	}

	public static void setRateAppTime(Context context, long time) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);

		Editor editor = pref.edit();
		editor.putLong("__rate_app_time", time);
		editor.commit();
	}

	public static long getRateAppTime(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		long time = pref.getLong("__rate_app_time", 0);
		return time;
	}
	public static void setCN(Context context, boolean cn) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("__cn", cn);
		editor.commit();
	}

	public static void setViewPort(Context context, ViewPort vp,
			int physicalScreenWidth, int physicalScreenHeight) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();
		if (physicalScreenWidth % 2 != 0) {
			physicalScreenWidth -= 1;
		}

		if (physicalScreenHeight % 2 != 0) {
			physicalScreenHeight -= 1;
		}
		String oldTmp = physicalScreenWidth + "x" + physicalScreenHeight;
		if (pref.getInt("vp-x-" + oldTmp, -1) != -1) {
			for (String key : pref.getAll().keySet()) {
				if (key.endsWith(oldTmp)) {
					edit.remove(key);
				}
			}
		}
		String tmp = "asp"
				+ (int) Math
						.floor(((float) physicalScreenWidth / physicalScreenHeight) * 10000);
		String x = "vp-x-" + tmp;
		String y = "vp-y-" + tmp;
		String width = "vp-width-" + tmp;
		String height = "vp-height-" + tmp;
		String version = "vp-version-" + tmp;
		edit.putFloat(x, ((float) vp.x / physicalScreenWidth));
		edit.putFloat(y, ((float) vp.y / physicalScreenHeight));
		edit.putFloat(width, ((float) vp.width / physicalScreenWidth));
		edit.putFloat(height, ((float) vp.height / physicalScreenHeight));
		edit.putInt(version, vp.version);
		edit.commit();
		Log.i("PreferenceUtil", "vp " + tmp + " saved");
	}

	
	public static void removeViewPortSave(Context context) {
		Log.d("", "removing view port save!!!");
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();

		for (String key : pref.getAll().keySet()) {
			if (key.startsWith("vp-")) {
				edit.remove(key);
			}
		}
		edit.commit();
	}
	public static ViewPort getViewPort(Context context,
			int physicalScreenWidth, int physicalScreenHeight) {
		if (physicalScreenWidth % 2 != 0) {
			physicalScreenWidth -= 1;
		}

		if (physicalScreenHeight % 2 != 0) {
			physicalScreenHeight -= 1;
		}


		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);

		String tmp = "asp"
				+ (int) Math
						.floor(((float) physicalScreenWidth / physicalScreenHeight) * 10000);

		float correctionW = 1f;
		float correctionH = 1f;
		if (pref.getInt("vp-version-" + tmp, -1) == -1) {
			int candidateWidth = physicalScreenWidth;
			int candidateHeight = physicalScreenHeight;
			for (String key : pref.getAll().keySet()) {
				if (key != null && key.startsWith("vp-x-")) {
					String resolution = key.substring(("vp-x-").length());
					if (resolution.startsWith("asp")) {
						continue;
					}
					String fws = resolution.substring(0,
							resolution.indexOf("x"));

					String fhs = resolution
							.substring(resolution.indexOf("x") + 1);
					int w = Integer.parseInt(fws);
					int h = Integer.parseInt(fhs);

					float foundAspect = (float) w / h;
					float desiredAspect = (float) physicalScreenWidth
							/ physicalScreenHeight;
					if (Math.abs(foundAspect - desiredAspect) < 0.001f) {
						candidateWidth = w;
						candidateHeight = h;
						correctionW = (float) physicalScreenWidth / w;
						correctionH = (float) physicalScreenHeight / h;
						break;
					}

				}
			}

			int w = candidateWidth;
			int h = candidateHeight;
			tmp = w + "x" + h;
		}

		String x = "vp-x-" + tmp;
		String y = "vp-y-" + tmp;
		String width = "vp-width-" + tmp;
		String height = "vp-height-" + tmp;
		ViewPort vp = new ViewPort();
		vp.version = pref.getInt("vp-version-" + tmp, 0);
		if (!tmp.startsWith("asp")) {
			vp.x = (int) (pref.getInt(x, -1) * correctionW);
			vp.y = (int) (pref.getInt(y, -1) * correctionH);
			vp.width = (int) (pref.getInt(width, -1) * correctionW);
			vp.height = (int) (pref.getInt(height, -1) * correctionH);
		} else {
			vp.x = (int) (pref.getFloat(x, 0) * physicalScreenWidth);
			vp.y = (int) (pref.getFloat(y, 0) * physicalScreenHeight);
			vp.width = (int) (pref.getFloat(width, 0) * physicalScreenWidth);
			vp.height = (int) (pref.getFloat(height, 0) * physicalScreenHeight);
		}
		if (vp.x == -1 || vp.y == -1 || vp.width == -1 || vp.height == -1) {
			return null;
		}
		Log.i("PreferenceUtil", "vp " + tmp + " loaded");
		return vp;
	}

	public static int getFragmentShader(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int shader = pref.getInt("fragment_shader", -1);
		return shader;
	}

	public static void setFragmentShader(Context context, int shader) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putInt("fragment_shader", shader);
		editor.commit();
	}

	public static boolean isSoundEnabled(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean muted = pref.getBoolean("general_pref_mute", false);
		return !muted;
	}

	public static String getWorkingDirParent(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String dir = pref.getString("general_pref_working_dir", null);
		return dir;
	}

	public static void setWorkingDirParent(Context context, String dir) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putString("general_pref_working_dir", dir);
		editor.commit();
	}

	public static void setWorkingDirCopyContent(Context context, boolean copy) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("general_pref_working_dir_copy_content", copy);
		editor.commit();
	}

	public static boolean isWorkingDirCopyContent(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_working_dir_copy_content", true);
	}

	public static float getSoundVolume(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		float volume = (pref.getInt("general_pref_sound_volume", 100) / (float) 100);
		return volume;
	}

	public static void setSoundVolume(Context context, float volume) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putFloat("general_pref_sound_volume", volume);
		editor.commit();
	}

	public static boolean isLoadSavFiles(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_load_sav_files", true);

	}

	public static boolean isSaveSavFiles(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_save_sav_files", true );

	}

	public static boolean isBenchmarked(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("is_benchmarked", false);
	}

	public static void setBenchmarked(Context context, boolean value) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();
		edit.putBoolean("is_benchmarked", value);
		edit.commit();
	}

	public static int getVibrationDuration(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return getVibrationDuration(context, pref);
	}

	public static void setEmulationQuality(Context context, int quality) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();
		edit.putString("general_pref_quality", quality + "");
		edit.commit();

	}

	public static int getEmulationQuality(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);

		return Integer.parseInt(pref.getString("general_pref_quality", "1"));
	}

	public static boolean isTurboEnabled(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_turbo", false);
	}

	public static boolean isABButtonEnabled(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_ab_button", false);
	}

	public static boolean isFullScreenEnabled(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_fullscreen", false);
	}

	public static int getControlsOpacity(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return (int) ((pref.getInt("general_pref_ui_opacity", 100) / 100f) * 255);
	}

	public static boolean isAutoHideControls(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_ui_autohide", true);
	}

	private static int getVibrationDuration(Context context,
			SharedPreferences pref) {
		return pref.getInt("game_pref_ui_strong_vibration", 0) * 10;
	}

	public static GfxProfile getVideoProfile(Context context,
			Emulator emulator, GameDescription game) {
		String gfxProfileName = getVideoMode(context, emulator, game.checksum);
		GfxProfile gfx = null;
		if (gfxProfileName != null) {
			for (GfxProfile profile : EmulatorInfoHolder.getInfo()
					.getAvailableGfxProfiles()) {
				if (profile.name.toLowerCase(Locale.ENGLISH).equals(
						gfxProfileName.toLowerCase(Locale.ENGLISH))) {
					gfx = profile;
					break;
				}
			}
		}
		if (gfx == null && emulator != null) {
			gfx = emulator.autoDetectGfx(game);
		}
		return gfx;
	}

	public static GfxProfile getLastGfxProfile(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String name = pref.getString("_lastGfx", null);
		try {
			List<GfxProfile> profiles = EmulatorInfoHolder.getInfo()
					.getAvailableGfxProfiles();
			for (GfxProfile profile : profiles) {
				if (profile.name.equals(name)) {
					return profile;
				}
			}
		} catch (Exception e) {
		}
		return EmulatorInfoHolder.getInfo().getDefaultGfxProfile();
	}

	public static void setLastGfxProfile(Context context, GfxProfile profile) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();
		edit.putString("_lastGfx", profile.name);
		edit.commit();
	}

	public static String getVideoMode(Context context, Emulator emulator,
			String gameHash) {
		if (gameHash == null) {
			return null;
		} else {
			SharedPreferences pref = context.getSharedPreferences(gameHash
					+ GAME_PREF_SUFFIX, Context.MODE_PRIVATE);
			return getVideoMode(context, emulator, pref);
		}
	}

	private static String getVideoMode(Context context, Emulator emulator,
			SharedPreferences pref) {
		return pref
				.getString("game_pref_ui_pal_ntsc_switch", null );
	}

	public static boolean isZapperEnabled(Context context, String gameHash) {

		SharedPreferences pref = context.getSharedPreferences(gameHash
				+ GAME_PREF_SUFFIX, Context.MODE_PRIVATE);
		return isZapperEnable(context, pref);

	}

	private static boolean isZapperEnable(Context context,
			SharedPreferences pref) {
		return pref.getBoolean("game_pref_zapper", false);
	}

	public enum ROTATION {
		AUTO, PORT, LAND, SENSOR,
	}

	public static final String GAME_PREF_SUFFIX = ".gamepref";

	public static ROTATION getDisplayRotation(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return getDisplayRotation(context, pref);
	}

	public enum Shader {
		NEAREST,
		LINEAR,
		SCALE2X,
		SCALE2X_HQ,
		SUPER_EAGLE,
		SUPER2XSAI,
	}

	public static Shader getShader(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return getShader(context, pref);
	}

	public static void setDefaultShader(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putString("general_pref_shader", "0");
		editor.commit();
	}

	
	public static int getLastGalleryTab(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int i = pref.getInt("LastGalleryTab", 0);
		return i;
	}

	
	public static void saveLastGalleryTab(Context context, int tabIdx) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putInt("LastGalleryTab", tabIdx);
		editor.commit();
	}

	private static ROTATION getDisplayRotation(Context context,
			SharedPreferences pref) {
		int i = Integer.parseInt(pref.getString("general_pref_rotation", "0"));
		return ROTATION.values()[i];
	}

	private static Shader getShader(Context context, SharedPreferences pref) {
		int i = Integer.parseInt(pref.getString("general_pref_shader", "0"));
		return Shader.values()[i];
	}

	public static boolean isTimeshiftEnabled(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return isTimeshiftEnable(context, pref);
	}

	private static boolean isTimeshiftEnable(Context context,
			SharedPreferences pref) {
		return pref.getBoolean("game_pref_ui_timeshift", false);
	}

	public static boolean isWifiServerEnable(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return isWifiServerEnable(context, pref);
	}

	private static boolean isWifiServerEnable(Context context,
			SharedPreferences pref) {
		return pref.getBoolean("general_pref_wifi_server_enable", false);
	}

	public static void setWifiServerEnable(Context context, boolean enable) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		setWifiServerEnable(context, pref, enable);
	}

	private static void setWifiServerEnable(Context context,
			SharedPreferences pref, boolean enable) {
		Editor edit = pref.edit();
		edit.putBoolean("general_pref_wifi_server_enable", enable);
		edit.commit();
	}

	public static boolean isOpenGLEnable(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return isOpenGLEnable(context, pref);
	}

	private static boolean isOpenGLEnable(Context context,
			SharedPreferences pref) {
		return pref.getBoolean("general_pref_opengl", true);
	}

	public static boolean isDynamicDPADEnable(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_ddpad", false);
	}

	public static void setDynamicDPADEnable(Context context, boolean enable) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("general_pref_ddpad", enable);
		editor.commit();
	}

	public static void setDynamicDPADUsed(Context context, boolean used) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("general_pref_ddpad_used", used);
		editor.commit();
	}

	public static boolean isDynamicDPADUsed(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_ddpad_used", false);
	}

	public static boolean isFastForwardUsed(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_fastforward_used", false);
	}

	public static void setFastForwardUsed(Context context, boolean used) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("general_pref_fastforward_used", used);
		editor.commit();
	}

	public static boolean isScreenLayoutUsed(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_screen_layout_used", false);
	}

	public static void setScreenLayoutUsed(Context context, boolean used) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("general_pref_screen_layout_used", used);
		editor.commit();
	}

	public static boolean isFastForwardEnabled(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_fastforward", false);
	}

	public static boolean isScreenSettingsSaved(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		for (String key : pref.getAll().keySet()) {
			if (key.startsWith("vp-"))
				return true;
		}
		return false;
	}

	public static boolean isFastForwardToggleable(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return pref.getBoolean("general_pref_fastforward_toggle", true);
	}

	public static void setFastForwardEnable(Context context, boolean enable) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = pref.edit();
		editor.putBoolean("general_pref_fastforward", enable);
		editor.commit();
	}

	public static void printPreferences(Context context) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		for (Entry<String, ?> entry : pref.getAll().entrySet()) {
			Log.i("PreferenceUtil", entry.getKey() + " --> "
					+ entry.getValue().toString());
		}
	}

}
