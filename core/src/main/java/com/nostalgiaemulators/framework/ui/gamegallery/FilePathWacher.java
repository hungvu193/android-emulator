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

import java.util.HashSet;

import android.content.Context;
import android.os.Environment;
import android.os.FileObserver;

import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

public class FilePathWacher extends FileObserver {
	private static final String TAG = "com.nostalgiaemulators.framework.ui.gamegallery.FilePathWacher";

	public interface OnSDCardChangeListener {
		public void onSDCardChange();
	}

	private HashSet<String> exts = new HashSet<String>();
	private OnSDCardChangeListener listener;
	private static int flags = 0;
	static {
		flags = FileObserver.ALL_EVENTS;
	}

	public FilePathWacher(Context context, HashSet<String> exts,
			OnSDCardChangeListener listener) {
		super(Environment.getExternalStorageDirectory().getAbsolutePath(),
				flags);

		Log.i(TAG, "create watcher "
				+ Environment.getExternalStorageDirectory().getAbsolutePath()
				+ " " + Integer.toBinaryString(flags));
		this.exts = exts;
		this.listener = listener;
	}

	@Override
	public void startWatching() {
		Log.i(TAG, "start");
		super.startWatching();
	}

	@Override
	public void stopWatching() {
		Log.i(TAG, "stop");
		super.stopWatching();
	}

	@Override
	public void onEvent(int event, String path) {
		Log.i(TAG, Integer.toBinaryString(event) + " " + path);
		if (path != null) {
			String ext = Utils.getExt(path);
			if (exts.contains(ext)) {
				Log.i(TAG, "SD card filesystem change");
				listener.onSDCardChange();
			}
		}
	}

}
