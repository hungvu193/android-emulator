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

package com.nostalgiaemulators.framework.ui.gamegallery;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.nostalgiaemulators.framework.base.EmulatorUtils;
import com.nostalgiaemulators.framework.utils.DatabaseHelper;
import com.nostalgiaemulators.framework.utils.FileUtils;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.SDCardUtil;
import com.nostalgiaemulators.framework.utils.Utils;

public class RomsFinder extends Thread {
	private static final String TAG = "com.nostalgiaemulators.framework.ui.gamegallery.RomsFinder";

	public interface OnRomsFinderListener {

		
		public void onRomsFinderStart(boolean searchNew);

		
		public void onRomsFinderFoundGamesInCache(
				ArrayList<GameDescription> oldRoms);

		
		public void onRomsFinderFoundFile(String name);

		
		public void onRomsFinderZipPartStart(int countEntries);

		
		public void onRomsFinderFoundZipEntry(String message, int skipEntries);

		
		public void onRomsFinderNewGames(ArrayList<GameDescription> roms);

		
		public void onRomsFinderEnd(boolean searchNew);

		
		public void onRomsFinderCancel(boolean searchNew);
	}

	private FilenameExtFilter filenameExtFilter;
	private FilenameExtFilter inZipfilenameExtFilter;
	private String androidAppDataFolder = "";
	private HashMap<String, GameDescription> oldGames = new HashMap<String, GameDescription>();
	private ArrayList<GameDescription> games = new ArrayList<GameDescription>();
	private OnRomsFinderListener listener;
	private BaseGameGalleryActivity activity;
	private boolean searchNew = true;
	private File selectedFolder;

	public RomsFinder(Set<String> exts, Set<String> inZipExts,
			BaseGameGalleryActivity activity, OnRomsFinderListener listener,
			boolean searchNew, File selectedFolder, String biosName) {
		this.listener = listener;
		this.biosName = biosName;
		this.activity = activity;
		this.searchNew = searchNew;
		this.selectedFolder = selectedFolder;
		filenameExtFilter = new FilenameExtFilter(exts, true, false);
		inZipfilenameExtFilter = new FilenameExtFilter(inZipExts, true, false);
		androidAppDataFolder = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/Android";
		biosFilter = new BiosFilter();
	}

	private class BiosFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (biosName != null && !pathname.isDirectory()
					&& pathname.getName().toLowerCase().equals(biosName)) {
				return true;
			}
			return false;
		}

	}

	private FileFilter biosFilter;
	private AtomicBoolean running = new AtomicBoolean(false);

	private class DirInfo {
		public File file;
		public int level;

		public DirInfo(File f, int level) {
			this.level = level;
			this.file = f;
		}
	}

	private String biosName;
	private void getRomAndPackedFiles(File root, List<File> result,
			HashSet<String> usedPaths) {
		String dirPath = null;
		Stack<DirInfo> dirStack = new Stack<DirInfo>();
		dirStack.removeAllElements();
		dirStack.add(new DirInfo(root, 0));
		final int MAX_LEVEL = 12;
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}

		while (running.get() && !dirStack.empty()) {

			DirInfo dir = dirStack.remove(0);
			try {
				dirPath = dir.file.getCanonicalPath();
			} catch (IOException e1) {
				Log.e(TAG, "search error", e1);
			}
			if (dirPath != null && !usedPaths.contains(dirPath)
					&& dir.level <= MAX_LEVEL) {
				usedPaths.add(dirPath);
				File[] biosFiles = dir.file.listFiles(biosFilter);
				if (biosFiles != null) {
					for (File biosFile : biosFiles) {
						copyBios(biosFile);
					}
				}

				File[] files = dir.file.listFiles(filenameExtFilter);

				if (files != null) {

					for (File file : files) {

						if (file.isDirectory()) {
							String canonicalPath = null;
							try {
								canonicalPath = file.getCanonicalPath();
							} catch (IOException e) {
								Log.e(TAG, "search error", e);
							}
							if (canonicalPath != null
									&& (!usedPaths.contains(canonicalPath))) {

								if (canonicalPath.equals(androidAppDataFolder)) {
									Log.i(TAG, "ignore " + androidAppDataFolder);
								} else {
									dirStack.add(new DirInfo(file,
											dir.level + 1));
								}
							} else {
								Log.i(TAG, "cesta " + canonicalPath
										+ " jiz byla prohledana");
							}
						} else {
							result.add(file);
						}
					}
				}
			} else {
				Log.i(TAG, "cesta " + dirPath + " jiz byla prohledana");
			}
		}
	}

	private void copyBios(File file) {
		File target = getBiosTargetFile();
		if (target == null
				|| (target.exists() && target.length() == file.length())) {
			return;
		}
		try {
			FileUtils.copyFile(file, getBiosTargetFile());
		} catch (Exception e) {
		}
	}

	private void copyBios(InputStream is) {
		File target = getBiosTargetFile();
		if (target == null || target.exists()) {
			return;
		}
		try {
			FileUtils.copyFile(is, getBiosTargetFile());
		} catch (Exception e) {
		}
	}

	private File getBiosTargetFile() {
		return new File(EmulatorUtils.getBaseDir(activity) + "/" + biosName);
	}

	public static ArrayList<GameDescription> getAllGames(DatabaseHelper helper) {
		return helper.selectObjsFromDb(GameDescription.class, false,
				"GROUP BY checksum", null);
	}

	@Override
	public void run() {

		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		running.set(true);
		Log.i(TAG, "start");
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listener.onRomsFinderStart(searchNew);
			}
		});
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(activity);
		ArrayList<GameDescription> oldRoms = getAllGames(dbHelper);

		oldRoms = removeNonExistRoms(oldRoms);

		final ArrayList<GameDescription> roms = oldRoms;

		Log.i(TAG, "old games " + oldRoms.size());
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listener.onRomsFinderFoundGamesInCache(roms);
			}
		});

		if (searchNew) {

			for (GameDescription desc : oldRoms) {
				oldGames.put(desc.path, desc);
			}

			startFileSystemMode(oldRoms);

		} else {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listener.onRomsFinderEnd(false);
				}
			});
		}
	}

	private void checkZip(File zipFile) {
		File externalCache = activity.getExternalCacheDir();
		if (externalCache != null) {
			DatabaseHelper dbHelper = DatabaseHelper.getInstance(activity);
			String cacheDir = externalCache.getAbsolutePath();

			Log.i(TAG, "check zip" + zipFile.getAbsolutePath());
			String hash = ZipRomFile.computeZipHash(zipFile);
			ZipRomFile zipRomFile = dbHelper.selectObjFromDb(ZipRomFile.class,
					"WHERE hash=\"" + hash + "\"");
			ZipFile zip = null;
			if (zipRomFile == null) {
				zipRomFile = new ZipRomFile();
				zipRomFile.path = zipFile.getAbsolutePath();
				zipRomFile.hash = hash;
				try {

					ZipEntry ze;
					File dir = new File(cacheDir);
					int counterRoms = 0;
					int counterEntry = 0;
					zip = new ZipFile(zipFile);
					int max = zip.size();
					Enumeration<? extends ZipEntry> entries = zip.entries();
					while (entries.hasMoreElements()) {
						ze = entries.nextElement();
						counterEntry++;
						if (running.get() && (!ze.isDirectory())) {
							String filename = ze.getName();

							if (biosName != null
									&& filename.toLowerCase().equals(biosName)) {
								InputStream is = null;
								try {
									is = zip.getInputStream(ze);
									copyBios(is);
								} catch (Exception e) {

								} finally {
									if (is != null) {
										is.close();
									}
								}
							}

							if (inZipfilenameExtFilter.accept(dir, filename)) {
								counterRoms++;

								InputStream is = zip.getInputStream(ze);
								String checksum = Utils
										.getMD5Checksum(is, true);
								try {
									if (is != null) {
										is.close();
									}
								} catch (Exception e) {
								}

								is = zip.getInputStream(ze);
								String oldChecksum = Utils.getMD5Checksum(is,
										false);
								try {
									if (is != null) {
										is.close();
									}
								} catch (Exception e) {
								}
								GameDescription game = new GameDescription(
										ze.getName(), "", checksum);
								game.inserTime = System.currentTimeMillis();
								game.oldChecksum = oldChecksum;
								zipRomFile.games.add(game);

								games.add(game);
							}
						}
						if (counterEntry > 20 && counterRoms == 0) {
							final String msg = zipFile.getName() + "\n"
									+ ze.getName();
							final int num = max - 20 - 1;
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									listener.onRomsFinderFoundZipEntry(msg, num);

								}
							});

							Log.i(TAG,
									"Predcasne ukonceni prohledavani zipu. V prvnich 20 zaznamech v zipu neni ani jeden rom");
							break;
						} else {
							String name = ze.getName();
							int idx = name.lastIndexOf('/');
							if (idx != -1) {
								name = name.substring(idx + 1);
							}
							if (name.length() > 20) {
								name = name.substring(0, 20);
							}

							onRomsFinderFoundZipEntry(zipFile.getName() + "\n"
									+ name, 0);
						}

					}
					if (running.get()) {
						dbHelper.insertObjToDb(zipRomFile);
					}

				} catch (FileNotFoundException e) {
					Log.e(TAG, "", e);
				} catch (Exception e) {
					Log.e(TAG, "", e);
				} finally {
					try {
						if (zip != null)
							zip.close();
					} catch (IOException e) {
						Log.e(TAG, "", e);
					}
				}
			} else {
				games.addAll(zipRomFile.games);
				onRomsFinderFoundZipEntry(zipFile.getName(),
						zipRomFile.games.size());
				Log.i(TAG, "found zip in cache " + zipRomFile.games.size());
			}
		} else {
			Log.e(TAG, "external cache dir is null");
			activity.showSDcardFailed();
		}
	}

	private void onRomsFinderFoundZipEntry(final String s, final int n) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				listener.onRomsFinderFoundZipEntry(s, n);
			}
		});
	}

	@SuppressLint("DefaultLocale")
	private void startFileSystemMode(ArrayList<GameDescription> oldRoms) {
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(activity);
		HashSet<File> roots = new HashSet<File>();
		if (selectedFolder == null) {
			roots = SDCardUtil.getAllStorageLocations();
		} else {
			roots.add(selectedFolder);
		}
		ArrayList<File> result = new ArrayList<File>();

		

		long startTime = System.currentTimeMillis();
		Log.i(TAG, "start searching in file system");
		HashSet<String> usedPaths = new HashSet<String>();
		for (File root : roots) {
			Log.i(TAG, "exploring " + root.getAbsolutePath());
			getRomAndPackedFiles(root, result, usedPaths);
		}
		Log.i(TAG, "found " + result.size() + " files");

		Log.i(TAG, "compute checksum");

		int zipEntriesCount = 0;
		ArrayList<File> zips = new ArrayList<File>();
		for (File file : result) {
			String path = file.getAbsolutePath();
			if (running.get()) {
				String ext = Utils.getExt(path).toLowerCase();
				if (ext.equals("zip")) {
					zips.add(file);
					ZipFile zzFile = null;
					try {
						zzFile = new ZipFile(file);
						zipEntriesCount += zzFile.size();
					} catch (ZipException e) {
						Log.e(TAG, "", e);
					} catch (Exception e) {
						Log.e(TAG, "", e);
					} finally {
						if (zzFile != null) {
							try {
								zzFile.close();
							} catch (Exception e) {
							}
						}

					}

					continue;
				}

				GameDescription game = null;
				if (oldGames.containsKey(path)) {
					game = oldGames.get(path);
				} else {
					String checksum = Utils.getMD5Checksum(file, true);
					String oldChecksum = Utils.getMD5Checksum(file, false);
					game = new GameDescription(file, checksum);
					game.inserTime = System.currentTimeMillis();
					game.oldChecksum = oldChecksum;
					dbHelper.insertObjToDb(game);
					onRomsFinderFoundFile(game.name);
				}
				games.add(game);
			}
		}

		for (File zip : zips) {
			if (running.get()) {
				onRomsFinderZipPartStart(zipEntriesCount);
				checkZip(zip);
			}
		}

		if (running.get()) {
			Log.i(TAG, "found games: " + games.size());
			games = removeNonExistRoms(games);
		}
		Log.i(TAG, "compute checksum- done");
		if (running.get()) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listener.onRomsFinderNewGames(games);
					listener.onRomsFinderEnd(true);
				}
			});
		}

		Log.i(TAG, "time:" + ((System.currentTimeMillis() - startTime) / 1000));
	}

	private void onRomsFinderFoundFile(final String name) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				listener.onRomsFinderFoundFile(name);
			}
		});
	}

	private void onRomsFinderZipPartStart(final int n) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				listener.onRomsFinderZipPartStart(n);
			}
		});
	}

	private void onRomsFinderCancel(final boolean b) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				listener.onRomsFinderCancel(b);
			}
		});
	}

	public void stopSearch() {
		if (running.get()) {
			onRomsFinderCancel(true);
		}
		running.set(false);
		Log.i(TAG, "cancel search");
	}

	private ArrayList<GameDescription> removeNonExistRoms(
			ArrayList<GameDescription> roms) {
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(activity);
		HashSet<String> hashs = new HashSet<String>();
		ArrayList<GameDescription> newRoms = new ArrayList<GameDescription>(
				roms.size());

		HashMap<Long, ZipRomFile> zipsMap = new HashMap<Long, ZipRomFile>();
		for (ZipRomFile zip : dbHelper.selectObjsFromDb(ZipRomFile.class,
				false, null, null)) {
			File zipFile = new File(zip.path);
			if (zipFile.exists()) {
				zipsMap.put(zip._id, zip);
			} else {
				dbHelper.deleteObjFromDb(zip);
				dbHelper.deleteObjsFromDb(GameDescription.class,
						"where zipfile_id=" + zip._id);
			}
		}

		for (GameDescription game : roms) {
			if (!game.isInArchive()) {
				File path = new File(game.path);
				if (path.exists()) {
					if (!hashs.contains(game.checksum)) {
						newRoms.add(game);
						hashs.add(game.checksum);
					}
				} else {
					dbHelper.deleteObjFromDb(game);
				}
			} else {
				ZipRomFile zip = zipsMap.get(game.zipfile_id);
				if (zip != null) {
					if (!hashs.contains(game.checksum)) {
						newRoms.add(game);
						hashs.add(game.checksum);
					}
				}
			}
		}
		return newRoms;
	}
}
