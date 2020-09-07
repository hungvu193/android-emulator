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
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.EmulatorSettings;
import com.nostalgiaemulators.framework.FrameListener;
import com.nostalgiaemulators.framework.GameInfo;
import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.SfxProfile;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

public abstract class JniEmulator implements Emulator {
	private static final String TAG = "com.nostalgiaemulators.framework.base.JniEmulator";

	public JniEmulator() {
		this.jni = getBridge();

	}

	public abstract JniBridge getBridge();

	@Override
	public abstract GfxProfile autoDetectGfx(GameDescription game);

	@Override
	public abstract SfxProfile autoDetectSfx(GameDescription game);

	@Override
	public int getHistoryItemCount() {
		return jni.getHistoryItemCount();
	}

	@Override
	public void setFastForwardFrameCount(int frames) {
		numFastForwardFrames = frames;
	}

	@Override
	public void loadHistoryState(int pos) {
		if (!jni.loadHistoryState(pos)) {
			throw new EmulatorException("load history state failed");
		}

	}

	public void processCommand(String command) {
		if (!jni.processCommand(command)) {
			throw new EmulatorException("process command failed");
		}
	}

	public int getInt(String name) {
		return jni.getInt(name);
	}

	@Override
	public void renderHistoryScreenshot(Bitmap bmp, int pos) {
		if (!jni.renderHistory(bmp, pos, bmp.getWidth(), bmp.getHeight())) {
			throw new EmulatorException("render history failed");
		}
	}

	@Override
	public boolean isGameLoaded() {
		synchronized (loadLock) {
			return gameInfo != null;
		}
	}

	@Override
	public GameInfo getLoadedGame() {
		synchronized (loadLock) {
			return gameInfo;
		}
	}
	@Override
	public void start(GfxProfile gfx, SfxProfile sfx, EmulatorSettings settings) {
		ready.set(false);
		setFastForwardEnabled(false);
		if (sfx != null) {
			sfxBuffer = new short[sfx.bufferSize];
			initSound(sfx);
		}

		this.sfx = sfx;
		this.gfx = gfx;

		if (!jni.start(gfx.toInt(), sfx == null ? -1 : sfx.toInt(),
				settings.toInt())) {
			throw new EmulatorException("init failed");
		}
		synchronized (loadLock) {
			gameInfo = null;
		}
		ready.set(true);
	}

	@Override
	public GfxProfile getActiveGfxProfile() {
		return gfx;
	}

	@Override
	public SfxProfile getActiveSfxProfile() {
		return sfx;
	}

	@Override
	public void reset() {
		ready.set(false);
		synchronized (sfxLock) {
			if (track != null) {
				track.pause();
				track.flush();
			}
		}
		if (!jni.reset()) {
			throw new EmulatorException("reset failed");
		}
		ready.set(true);
	}

	AtomicBoolean ready = new AtomicBoolean();
	AtomicBoolean paused = new AtomicBoolean();
	private String baseDir;

	public void setBaseDir(String path) {
		this.baseDir = path;
		if (!jni.setBaseDir(path)) {
			throw new EmulatorException("could not set base dir");
		}
	}

	@Override
	public void saveState(int slot) {
		String fileName = SlotUtils.getSlotPath(baseDir, getMD5(null), slot);

		Bitmap screen = null;
		try {
			screen = Bitmap.createBitmap(gfx.originalScreenWidth,
					gfx.originalScreenHeight, Config.ARGB_8888);
		} catch (OutOfMemoryError wtf) {

		}
		if (screen != null) {
			if (!jni.renderVP(screen, gfx.originalScreenWidth,
					gfx.originalScreenHeight)) {
				throw new EmulatorException(R.string.act_game_screenshot_failed);
			}
		}

		if (!jni.saveState(fileName, slot )) {
			throw new EmulatorException(R.string.act_emulator_save_state_failed);
		}

		if (screen != null) {
			String pngFileName = SlotUtils.getScreenshotPath(baseDir,
					getMD5(null), slot);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(pngFileName);
				screen.compress(CompressFormat.PNG, 60, out);
			} catch (Exception e) {
				throw new EmulatorException(R.string.act_game_screenshot_failed);
			} finally {
				if (out != null) {
					try {
						out.flush();
						out.close();
					} catch (Exception e) {
					}
				}
			}
			File file = new File(pngFileName);
			Log.i(TAG, "SCREEN: " + file.length());
			screen.recycle();
		} else {
			throw new EmulatorException(R.string.act_game_screenshot_failed);
		}
	}

	@Override
	public void loadState(int slot) {
		String fileName = SlotUtils.getSlotPath(baseDir, getMD5(null), slot);
		if (!new File(fileName).exists()) {
			return;
		}
		if (!jni.loadState(fileName, slot )) {
			throw new EmulatorException(R.string.act_emulator_load_state_failed);
		}
	}

	@Override
	public void loadGame(String fileName, String batteryDir,
			String batterySaveFullPath) {
		if (!jni.loadGame(fileName, batteryDir, batterySaveFullPath)) {
			synchronized (loadLock) {
				loadFailed = true;
				loadLock.notifyAll();
			}
			throw new EmulatorException(R.string.act_emulator_load_game_failed);
		}
		GameInfo gi = new GameInfo();
		gi.path = fileName;
		gi.md5 = getMD5(fileName);

		synchronized (loadLock) {
			loadFailed = false;
			gameInfo = gi;
			loadLock.notifyAll();
		}

	}

	@Override
	public void setKeyPressed(int port, int key, boolean isPressed) {
		int n = port * 8;

		if (key >= 1000) {
			key -= 1000;
			setTurboEnabled(port, key, isPressed);
		}

		if (isPressed) {
			keys |= (key << n);
		} else {
			keys &= ~(key << n);
		}
	}

	public void setTurboEnabled(int port, int key, boolean isEnabled) {
		int n = port * 8;
		int t = ~turbos;
		if (isEnabled) {
			t |= (key << n);
		} else {
			t &= ~(key << n);
		}
		turbos = ~t;
	}

	private boolean loadFailed = false;

	public void readPalette(int[] result) {
		synchronized (loadLock) {
			if (gameInfo == null && !loadFailed) {
				try {
					loadLock.wait();
				} catch (InterruptedException e) {

				}
			}
		}

		if (result == null) {
			throw new IllegalArgumentException();
		}
		if (gameInfo != null) {
			if (!jni.readPalette(result)) {
				throw new EmulatorException("error reading palette");
			}
		}
	}

	@Override
	public void setViewPortSize(int w, int h) {

		if (!jni.setViewPortSize(w, h)) {
			throw new EmulatorException("set view port size failed");
		}
		synchronized (viewPortLock) {
			viewPortWidth = w;
			viewPortHeight = h;
		}
	}
	public void setVolume(float vol) {
		synchronized (sfxLock) {
			float volume = vol
					* (AudioTrack.getMaxVolume() - AudioTrack.getMinVolume())
					+ AudioTrack.getMinVolume();
			track.setStereoVolume(volume, volume);
		}
	}

	public void stop() {
		ready.set(false);
		if (bitmap != null) {
			bitmap.recycle();
			Log.d(TAG, "bitmap recycled");
		}
		synchronized (sfxLock) {
			if (track != null) {
				try {
					track.pause();
					track.flush();
					track.stop();
				} catch (IllegalStateException e) {
				}
				track.release();
				track = null;
			}
		}
		jni.stop();
		synchronized (loadLock) {
			gameInfo = null;
		}
		bitmap = null;
	}

	public boolean isReady() {
		return ready.get();
	}

	private Object loadLock = new Object();

	@Override
	public void fireZapper(float x, float y) {
		int emuX;
		int emuY;
		if (x == -1 || y == -1) {
			emuX = -1;
			emuY = -1;
		} else {
			emuX = (int) (getActiveGfxProfile().originalScreenWidth * x);
			emuY = (int) (getActiveGfxProfile().originalScreenHeight * y);
		}
		if (!jni.fireZapper(emuX, emuY)) {
			throw new EmulatorException("firezapper failed");
		}
	}

	@Override
	public void resetKeys() {
		keys = 0;
	}

	@Override
	public void emulateFrame(int numFramesToSkip) {
		if (!ready.get()) {
			return;
		}
		if (fastForward && numFramesToSkip > -1) {
			numFramesToSkip = numFastForwardFrames;
		}

		if (!jni.emulate(keys, turbos, numFramesToSkip)) {
			throw new EmulatorException("emulateframe failed");
		}
		if (listener != null) {
			listener.onFrameReady();
		}
	}

	FrameListener listener;

	public void setFrameListener(FrameListener listener) {
		this.listener = listener;
	}

	@Override
	public void renderGfx() {
		if (!jni.render(bitmap)) {
			createBitmap(viewPortWidth, viewPortHeight);
			if (!jni.render(bitmap)) {
				throw new EmulatorException("render failed");
			}
		}
	}

	@Override
	public void renderGfxGL() {
		if (!jni.renderGL()) {
			throw new EmulatorException("render failed");
		}
	}

	@Override
	public void draw(Canvas canvas, int x, int y) {
		if (useOpenGL) {
			throw new IllegalStateException();
		}
		if (bitmap == null || bitmap.isRecycled()) {
			return;
		}
		canvas.drawBitmap(bitmap, x, y, null);
	}

	@Override
	public void enableCheat(String gg, int n) {
		if (!jni.enableCheat(gg, n)) {
			throw new EmulatorException(R.string.act_emulator_invalid_cheat, gg);
		}
	}

	@Override
	public void enableRawCheat(int addr, int val, int comp) {
		if (!jni.enableRawCheat(addr, val, comp)) {
			throw new EmulatorException(R.string.act_emulator_invalid_cheat,
					Integer.toHexString(addr) + ":" + Integer.toHexString(val));
		}
	}

	@Override
	public void readSfxData() {
		if (!ready.get()) {
			return;
		}

		int length = jni.readSfxBuffer(sfxBuffer);
		int slen;
		int back;
		synchronized (audioBuffers) {
			back = cur;
			slen = audioBufferLens[back];

			if (length > 0) {
				if (slen + length < SIZE) {
					System.arraycopy(sfxBuffer, 0, audioBuffers[back], 0,
							length);
					audioBufferLens[back] = length;
				} else {
					audioBufferLens[back] = 0;
				}
			}

		}
	}

	private int cur = 0;
	private int[] audioBufferLens = new int[2];

	public void onEmulationResumed() {
		if (paused.compareAndSet(true, false)) {
			if (track != null) {
				track.play();
			}
		}
	}

	public void onEmulationPaused() {
		if (paused.compareAndSet(false, true)) {
			if (track != null) {
				track.pause();
			}
		}
	}

	@Override
	public void setFastForwardEnabled(boolean enabled) {
		fastForward = enabled;
	}

	private boolean fastForward;
	private int numFastForwardFrames;

	long startTime = -1;
	@Override
	public void renderSfx() {
		if (track == null) {
			return;
		}
		int slen;
		int cur = this.cur;
		synchronized (audioBuffers) {
			slen = audioBufferLens[cur];
			if (slen > 0) {
				audioBufferLens[cur] = 0;
				this.cur = cur == 0 ? 1 : 0;
			}
		}
		if (slen > 0) {
			synchronized (sfxLock) {
				track.flush();
				track.write(audioBuffers[cur], 0, slen);
			}
		}

	}

	private static final int SIZE = 32768 * 2;
	private short[][] audioBuffers = new short[2][SIZE];
	private Object sfxLock = new Object();

	private void createBitmap(int w, int h) {
		if (bitmap != null) {
			bitmap.recycle();
		}
		bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
	}

	private void initSound(SfxProfile sfx) {
		synchronized (sfxLock) {
			int format = sfx.isStereo ? AudioFormat.CHANNEL_OUT_STEREO
					: AudioFormat.CHANNEL_OUT_MONO;
			int encoding = sfx.encoding == SfxProfile.SoundEncoding.PCM8 ? AudioFormat.ENCODING_PCM_8BIT
					: AudioFormat.ENCODING_PCM_16BIT;

			int minSize = AudioTrack.getMinBufferSize(sfx.rate, format,
					encoding);

			synchronized (audioBuffers) {
				audioBufferLens[0] = 0;
				audioBufferLens[1] = 0;
				for (int i = 0; i < SIZE; i++) {
					audioBuffers[0][i] = 0;
					audioBuffers[1][i] = 0;
				}
			}

			if (track != null) {
				try {
					track.pause();
					track.flush();
					track.stop();
				} catch (IllegalStateException e) {
				}
				track.release();
				track = null;
			}
			track = new AudioTrack(AudioManager.STREAM_MUSIC, sfx.rate, format,
					encoding, minSize, AudioTrack.MODE_STREAM);
			int numTrials = 15;
			boolean ok = false;
			for (int i = 0; !ok && i < numTrials; i++) {

				try {
					track.play();
					ok = true;
				} catch (Exception e) {
					try {
						Thread.sleep(100);
					} catch (Exception ie) {
					}
					if (i == numTrials - 1) {
						throw new EmulatorException("Sound init failed");
					}
				}

			}
			Log.d(TAG, "sound init OK");
		}
	}

	private String getMD5(String path) {
		if (path == null) {
			path = getLoadedGame().path;
		}
		if (!md5s.containsKey(path)) {
			String md5 = Utils.getMD5Checksum(new File(path), true);
			md5s.put(path, md5);
		}
		return md5s.get(path);
	}

	private static Map<String, String> md5s = new HashMap<String, String>();

	private boolean useOpenGL;
	private GameInfo gameInfo;
	private Bitmap bitmap;
	private SfxProfile sfx;
	private GfxProfile gfx;
	private AudioTrack track;
	private short[] sfxBuffer;
	private JniBridge jni;
	private int keys;
	private int turbos = ~0;
	private Object viewPortLock = new Object();
	private int viewPortWidth;
	private int viewPortHeight;

}
