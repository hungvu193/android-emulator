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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
public class RestarterActivity extends Activity {

	public static final String EXTRA_PID = "pid";
	public static final String EXTRA_CLASS = "class";
	public static final String EXTRA_AFTER_RESTART = "isAfterRestart";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setText("Loading...");
		setContentView(tv);
	}

	@Override
	protected void onResume() {
		super.onResume();

		int pid = getIntent().getExtras().getInt(EXTRA_PID);
		String className = getIntent().getExtras().getString(EXTRA_CLASS);
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (Exception e) {
		}

		Intent restartIntent = null;
		if (clazz != null) {
			restartIntent = new Intent(this, clazz);
			restartIntent.putExtras(getIntent());
		}
		thread = new RestarterThread(pid, restartIntent);
		thread.start();

	}

	RestarterThread thread;

	@Override
	public void onBackPressed() {
	}

	private class RestarterThread extends Thread {

		public RestarterThread(int pid, Intent intent) {
			this.intent = intent;
			this.pid = pid;
		}

		Intent intent;
		int pid;

		@Override
		public void run() {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			android.os.Process.killProcess(pid);
			try {
				Thread.sleep(300);
			} catch (Exception e) {
			}

			ActivityManager activityManager = (ActivityManager) getApplicationContext()
					.getSystemService(ACTIVITY_SERVICE);

			boolean killed = false;
			while (!killed) {
				List<RunningAppProcessInfo> appProcesses = activityManager
						.getRunningAppProcesses();
				killed = true;
				for (RunningAppProcessInfo info : appProcesses) {
					if (info.pid == pid) {
						killed = false;
						break;
					}
				}
				if (!killed) {
					try {
						Thread.sleep(30);
					} catch (Exception e) {
					}
				}
			}

			if (!cancelled.get()) {
				if (intent != null) {
					intent.putExtra(EXTRA_AFTER_RESTART, true);
					startActivity(intent);
				} else {
					finish();
				}
			}

		}

		public void cancel() {
			cancelled.set(true);
		}

		private AtomicBoolean cancelled = new AtomicBoolean(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (thread != null) {
			thread.cancel();
		}
		finish();
	}

}
