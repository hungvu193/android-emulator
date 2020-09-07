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
import java.io.FilenameFilter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostalgiaemulators.framework.base.BatterySaveUtils;
import com.nostalgiaemulators.framework.base.Benchmark;
import com.nostalgiaemulators.framework.base.EmulatorUtils;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.FileUtils;
import com.nostalgiaemulators.framework.utils.Log;

public class EmulatorRunner {
	private static final String TAG = "com.nostalgiaemulators.framework.EmulatorRunner";

	public EmulatorRunner(Emulator emulator, Context context) {
		this.emulator = emulator;
		emulator.setBaseDir(EmulatorUtils.getBaseDir(context));
		this.context = context.getApplicationContext();

		fixBatterySaveBug();
	}
	private void fixBatterySaveBug() {
		if (PreferenceUtil.isBatterySaveBugFixed(context)) {
			return;
		}
		File dir = context.getExternalCacheDir();
		if (dir == null) {
			return;
		}
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.toLowerCase().endsWith(".sav");
			}
		};
		String cacheDir = context.getExternalCacheDir().getAbsolutePath();
		String baseDir = EmulatorUtils.getBaseDir(context);
		String[] fileNames = dir.list(filter);
		for (String filename : fileNames) {
			File source = new File(cacheDir, filename);
			File dest = new File(baseDir, filename);
			try {
				FileUtils.copyFile(source, dest);
				source.delete();
				Log.d("SAV", "copying: " + source + " " + dest);
			} catch (Exception e) {
			}
		}
		PreferenceUtil.setBatterySaveBugFixed(context);
	}

	private boolean isDestroyed = false;

	public void destroy() {
		isDestroyed = true;
		if (audioPlayer != null) {
			audioPlayer.destroy();
			try {
				audioPlayer.join();
			} catch (Exception e) {
			}
			audioPlayer = null;
		}
		if (updater != null) {
			updater.destroy();
			try {
				updater.join();
			} catch (Exception e) {
			}
			updater = null;
		}
	}

	public boolean pauseEmulation() {
		if (isDestroyed) {
			return false;
		}

		synchronized (pauseLock) {
			if (updater == null) {
				return false;
			}
			if (!isPaused.get()) {
				Log.i(TAG, "--PAUSE EMULATION--");
				isPaused.set(true);

				emulator.onEmulationPaused();

				updater.pause();

				saveAutoState();
				return true;
			}
			return false;
		}
	}

	public void resumeEmulation() {
		if (isDestroyed) {
			return;
		}
		synchronized (pauseLock) {
			if (isPaused.get()) {
				Log.i(TAG, "--UNPAUSE EMULATION--");

				emulator.onEmulationResumed();

				updater.unpause();

				isPaused.set(false);
			}
		}
	}

	private Object pauseLock = new Object();

	public void stopGame() {
		if (isDestroyed) {
			return;
		}
		resumeEmulation();

		if (audioPlayer != null) {
			audioPlayer.destroy();
			try {
				audioPlayer.join();
			} catch (Exception e) {
			}
			audioPlayer = null;
		}
		if (updater != null) {
			updater.destroy();
			try {
				updater.join();
			} catch (Exception e) {
			}
			updater = null;
		}

		saveAutoState();

		synchronized (lock) {
			emulator.stop();
		}
	}

	public void resetEmulator() {
		if (isDestroyed) {
			return;
		}
		synchronized (lock) {
			emulator.reset();
			if (audioPlayer != null) {
				audioPlayer.onGameReset();
			}
		}
	}

	public void processCommand(String command) {
		if (isDestroyed) {
			return;
		}
		synchronized (lock) {
			emulator.processCommand(command);
		}
	}

	public int getInt(String name) {
		if (isDestroyed) {
			return 0;
		}
		synchronized (lock) {
			return emulator.getInt(name);
		}
	}

	public void startGame(GameDescription game) {
		if (isDestroyed) {
			return;
		}
		isPaused.set(false);

		if (audioPlayer != null) {
			audioPlayer.destroy();
			try {
				audioPlayer.join();
			} catch (Exception e) {
			}
		}

		if (updater != null) {
			updater.destroy();
			try {
				updater.join();
			} catch (Exception e) {
			}
		}

		synchronized (lock) {
			GfxProfile gfx = PreferenceUtil.getVideoProfile(context, emulator,
					game);
			PreferenceUtil.setLastGfxProfile(context, gfx);
			EmulatorSettings settings = new EmulatorSettings();

			settings.zapperEnabled = PreferenceUtil.isZapperEnabled(context,
					game.checksum);
			settings.historyEnabled = PreferenceUtil
					.isTimeshiftEnabled(context);

			settings.loadSavFiles = PreferenceUtil.isLoadSavFiles(context);

			settings.saveSavFiles = PreferenceUtil.isSaveSavFiles(context);

			volumeModifier = PreferenceUtil.getSoundVolume(context);
			volumeModifier = (float) ((Math.exp(volumeModifier) - 1) / (Math.E - 1));

			List<SfxProfile> profiles = emulator.getInfo()
					.getAvailableSfxProfiles();
			SfxProfile sfx = null;
			int desiredQuality = PreferenceUtil.getEmulationQuality(context);
			settings.quality = desiredQuality;

			desiredQuality = Math.min(profiles.size() - 1, desiredQuality);

			sfx = profiles.get(desiredQuality);

			if (!PreferenceUtil.isSoundEnabled(context)) {
				sfx = null;
			}
			audioEnabled = sfx != null;

			emulator.start(gfx, sfx, settings);

			BatterySaveUtils.createSavFileCopyIfNeeded(context, game.path);

			String batteryDir = BatterySaveUtils.getBatterySaveDir(context,
					game.path);

			String possibleBatteryFileFullPath = batteryDir + "/"
					+ FileUtils.getFileNameWithoutExt(new File(game.path))
					+ ".sav";
			emulator.loadGame(game.path, batteryDir,
					possibleBatteryFileFullPath );
			emulator.emulateFrame(0);

		}
		updater = new EmulatorThread();
		updater.setFps(emulator.getActiveGfxProfile().fps);
		updater.start();

		if (audioEnabled) {
			audioPlayer = new AudioPlayer();
			audioPlayer.start();
		}

	}

	public void enableCheat(String gg, int n) {
		if (isDestroyed) {
			return;
		}
		checkGameLoaded();
		synchronized (lock) {
			emulator.enableCheat(gg, n);
		}
	}

	public void enableRawCheat(int addr, int val, int comp) {
		if (isDestroyed) {
			return;
		}
		checkGameLoaded();
		synchronized (lock) {
			emulator.enableRawCheat(addr, val, comp);
		}
	}

	public void saveState(int slot) {
		if (isDestroyed) {
			return;
		}
		if (emulator.isGameLoaded()) {
			synchronized (lock) {
				emulator.saveState(slot);
			}
		}
	}

	public int getHistoryItemCount() {
		if (isDestroyed) {
			return 0;
		}
		synchronized (lock) {
			return emulator.getHistoryItemCount();
		}
	}

	public void loadHistoryState(int pos) {
		if (isDestroyed) {
			return;
		}
		synchronized (lock) {
			emulator.emulateFrame(-1);
			emulator.loadHistoryState(pos);
		}
	}

	public void renderHistoryScreenshot(Bitmap bmp, int pos) {
		if (isDestroyed) {
			return;
		}
		synchronized (lock) {
			emulator.renderHistoryScreenshot(bmp, pos);
		}
	}

	public void loadState(int slot) {
		if (isDestroyed) {
			return;
		}
		if (!emulator.isGameLoaded()) {
			return;
		}
		checkGameLoaded();
		synchronized (lock) {
			emulator.emulateFrame(-1);
			emulator.loadState(slot);
		}
	}

	private void checkGameLoaded() {
		if (!emulator.isGameLoaded()) {
			throw new EmulatorException("unexpected");
		}
	}

	private void saveAutoState() {
		saveState(AUTO_SAVE_SLOT);
	}

	private boolean audioEnabled;

	public void setBenchmark(Benchmark benchmark) {
		this.benchmark = benchmark;
	}

	private Benchmark benchmark;

	private class EmulatorThread extends Thread {

		public void setFps(int fps) {
			exactDelayPerFrameE1 = (int) ((1000 / (float) fps) * 10);
			delayPerFrame = (int) (exactDelayPerFrameE1 / 10f + 0.5);
		}

		@Override
		public void run() {
			setName("emudroid:gameLoop #" + (int) (Math.random() * 1000));
			Log.i(TAG, getName() + " started");
			long skippedTime = 0;

			unpause();
			expectedTimeE1 = 0;
			int cnt = 0;
			while (isRunning.get()) {
				if (benchmark != null) {
					benchmark.notifyFrameEnd();
				}
				long time1 = System.currentTimeMillis();

				synchronized (pauseLock) {
					while (isPaused) {
						try {
							pauseLock.wait();
						} catch (InterruptedException e) {
						}
						if (benchmark != null) {
							benchmark.reset();
						}
						time1 = System.currentTimeMillis();
					}
				}
				int numFramesToSkip = 0;
				long realTime = (time1 - startTime);
				long diff = ((expectedTimeE1 / 10) - realTime);
				long delay = +diff;
				if (delay > 0) {
					try {
						Thread.sleep(delay);
					} catch (Exception e) {
					}
				} else {
					try {
						Thread.sleep(1);
					} catch (Exception e) {
					}
				}

				skippedTime = -diff;

				if (skippedTime >= delayPerFrame * 3) {
					numFramesToSkip = (int) (skippedTime / delayPerFrame) - 1;
					int originalSkipped = numFramesToSkip;
					numFramesToSkip = Math.min(originalSkipped, 8);
					expectedTimeE1 += (numFramesToSkip * exactDelayPerFrameE1);
				}
				if (benchmark != null) {
					benchmark.notifyFrameStart();
				}
				synchronized (lock) {
					emulator.emulateFrame(numFramesToSkip);
					cnt += 1 + numFramesToSkip;
					if (audioEnabled && cnt >= 3) {
						emulator.readSfxData();
						synchronized (sfxReadyLock) {
							sfxReady = true;
							sfxReadyLock.notifyAll();
						}
						cnt = 0;
					}
				}

				expectedTimeE1 += exactDelayPerFrameE1;

			}
			Log.i(TAG, getName() + " finished");
		}

		public void unpause() {
			synchronized (pauseLock) {
				startTime = System.currentTimeMillis();
				expectedTimeE1 = 0;
				isPaused = false;
				pauseLock.notifyAll();
			}
		}

		public void pause() {
			synchronized (pauseLock) {
				isPaused = true;
			}
		}

		public void destroy() {
			isRunning.set(false);
			unpause();
		}

		private long expectedTimeE1;
		private int exactDelayPerFrameE1;
		private long startTime;
		private boolean isPaused = true;
		private AtomicBoolean isRunning = new AtomicBoolean(true);
		private Object pauseLock = new Object();
		private int delayPerFrame;

	}

	private class AudioPlayer extends Thread {

		public void onGameReset() {
			isAfterReset.set(true);
		}

		private AtomicBoolean isAfterReset = new AtomicBoolean();

		@Override
		public void run() {
			isRunning.set(true);

			setName("emudroid:audioPlayer");

			float volume = 0f;
			emulator.setVolume(0);

			long startTime = System.currentTimeMillis();
			while (isRunning.get()) {

				synchronized (sfxReadyLock) {
					while (!sfxReady) {
						try {
							sfxReadyLock.wait();
						} catch (Exception e) {
							break;
						}
					}
					sfxReady = false;
				}
				if (isRunning.get() && !isPaused.get()) {
					if (isAfterReset.compareAndSet(true, false)) {
						volume = 0f;
						emulator.setVolume(0);
						try {
							Thread.sleep(150);
						} catch (Exception e) {

						}
						startTime = System.currentTimeMillis();
					}
					emulator.renderSfx();
					if (volume < 1f) {
						volume = (float) ((System.currentTimeMillis() - startTime) / 1000f);
						final float vol = volume * volumeModifier;
						emulator.setVolume(vol);
					}
				}

			}

			try {
				emulator.setVolume(0);
				Thread.sleep(150);
			} catch (Exception e) {

			}
		}
		public void destroy() {
			isRunning.set(false);
			synchronized (sfxReadyLock) {
				sfxReady = true;
				sfxReadyLock.notifyAll();
			}
		}

		protected AtomicBoolean isRunning = new AtomicBoolean();
	}

	private Object sfxReadyLock = new Object();
	private boolean sfxReady = false;
	protected Emulator emulator;

	protected final Object lock = new Object();
	private AudioPlayer audioPlayer;
	private AtomicBoolean isPaused = new AtomicBoolean();

	private EmulatorThread updater;
	private static final int AUTO_SAVE_SLOT = 0;
	private Context context;
	private float volumeModifier;

}
