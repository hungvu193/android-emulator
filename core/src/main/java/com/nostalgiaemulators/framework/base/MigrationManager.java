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
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nostalgiaemulators.framework.KeyboardProfile;
import com.nostalgiaemulators.framework.ui.cheats.Cheat;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.gamegallery.RomsFinder;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchLayer;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.DatabaseHelper;
import com.nostalgiaemulators.framework.utils.FileUtils;

public class MigrationManager {

	public static void doExport(Context context, String baseDir) {
		for (Migrator migrator : getMigrators()) {
			migrator.doExport(context, baseDir);
		}
	}

	public static void doImport(Context context, String baseDir) {
		for (Migrator migrator : getMigrators()) {
			migrator.doImport(context, baseDir);
		}
	}

	public static Migrator[] getMigrators() {
		return new Migrator[] { new SaveStatesMigrator(),
				new GeneralPrefMigrator(), new GamePrefMigrator(),
				new KeyboardProfile.PreferenceMigrator(),
				new MultitouchLayer.PreferenceMigrator(), };
	}

	private static class SaveStatesMigrator implements Migrator {

		@Override
		public void doExport(Context context, String targetDir) {
			String sSource = EmulatorUtils.getBaseDir(context);
			File source = new File(sSource);
			File[] files = source.listFiles();
			try {
				for (File file : files) {
					if (file.isDirectory()) {
						continue;
					}
					File newFile = new File(targetDir, file.getName());
					FileUtils.copyFile(file, newFile);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void doImport(Context context, String sourceDir) {
			File source = new File(sourceDir);
			File[] files = source.listFiles();

			String targetDir = EmulatorUtils.getBaseDir(context);
			try {
				for (File file : files) {
					String name = file.getName();
					if (name.endsWith(".state") || name.endsWith(".png")
							|| name.endsWith(".sav") || name.endsWith(".rom")) {
						File newFile = new File(targetDir, name);
						FileUtils.copyFile(file, newFile);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

	}

	private static class GamePrefMigrator implements Migrator {

		@Override
		public void doExport(Context context, String baseDir) {
			DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
			ArrayList<GameDescription> games = RomsFinder.getAllGames(dbHelper);
			for (GameDescription game : games) {
				PreferenceUtil.exportPreferences(context.getSharedPreferences(
						game.checksum + Cheat.CHEAT_PREF_SUFFIX,
						Context.MODE_PRIVATE), new File(baseDir, game.checksum
						+ Cheat.CHEAT_PREF_SUFFIX));
				PreferenceUtil.exportPreferences(context.getSharedPreferences(
						game.checksum + PreferenceUtil.GAME_PREF_SUFFIX,
						Context.MODE_PRIVATE), new File(baseDir, game.checksum
						+ PreferenceUtil.GAME_PREF_SUFFIX));
			}

		}

		@Override
		public void doImport(Context context, String baseDir) {
			doImport(baseDir, context, Cheat.CHEAT_PREF_SUFFIX);
			doImport(baseDir, context, PreferenceUtil.GAME_PREF_SUFFIX);

		}

		private void doImport(String baseDir, Context context,
				final String importSuffix) {
			File dir = new File(baseDir);
			String[] files = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(importSuffix);
				}
			});

			for (String file : files) {
				String prefName = file;
				SharedPreferences pref = context.getSharedPreferences(prefName,
						Context.MODE_PRIVATE);
				PreferenceUtil.importPreferences(pref, new File(baseDir, file),
						PreferenceUtil.NotFoundHandling.FAIL);
			}

		}

	}

	public static class GeneralPrefMigrator implements Migrator {

		private final String EXPORT_FILE = "general__preferences";

		@Override
		public void doExport(Context context, String baseDir) {
			PreferenceUtil.exportPreferences(
					PreferenceManager.getDefaultSharedPreferences(context),
					new File(baseDir, EXPORT_FILE));
		}

		@Override
		public void doImport(Context context, String baseDir) {
			PreferenceUtil.importPreferences(
					PreferenceManager.getDefaultSharedPreferences(context),
					new File(baseDir, EXPORT_FILE),
					PreferenceUtil.NotFoundHandling.IGNORE);
		}

	}

}
