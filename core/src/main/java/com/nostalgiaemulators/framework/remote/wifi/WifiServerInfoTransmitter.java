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

package com.nostalgiaemulators.framework.remote.wifi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;


public class WifiServerInfoTransmitter {
	private static final String TAG = "com.nostalgiaemulators.framework.remote.wifi.BroadcastThread";
	private static final int SLEEP_TIME = 3000;
	private static final int SLEEP_TIME_AFTER_EXCEPTION = 15000;
	public static final int BROADCAST_PORT = 64313;
	public static final String MESSAGE_PREFIX = "EMUDROID";

	private WifiServerInfoTransmitter() {

	}

	public static void onResume(Activity activity, String sessionDescription) {
		if (instance == null) {
			instance = new WifiServerInfoTransmitter();
		}
		instance.startSending(activity.getApplicationContext(),
				sessionDescription);
		instance.setKillTime(-1);
	}

	public static void onPause() {
		if (instance != null) {
			instance.setKillTime(System.currentTimeMillis() + 4000);
		}
	}

	private void setKillTime(long time) {
		if (thread != null) {
			thread.setKillTime(time);
		}
	}

	public static void halt() {
		if (instance != null) {
			instance.forceStop();
		}
	}

	static WifiServerInfoTransmitter instance;

	
	private boolean startSending(Context applicationContext,
			String sessionDescription) {

		if (PreferenceUtil.isWifiServerEnable(applicationContext)
				&& Utils.isWifiAvailable(applicationContext)) {
			if (thread == null || !thread.running.get()) {
				thread = new BroadCastThread();
				thread.start();
			}
			thread.update(applicationContext, sessionDescription);

			return true;
		} else {
			if (thread != null) {
				thread.stopSending();
				thread = null;
			}
			return false;
		}
	}

	private void forceStop() {
		if (thread != null) {
			thread.stopSending();
			thread = null;
		}
	}

	private BroadCastThread thread = null;

	private static class BroadCastThread extends Thread {

		private AtomicBoolean running = new AtomicBoolean();

		DatagramSocket serverSocket = null;
		CountDownLatch socketInitLatch = new CountDownLatch(1);

		long killTime = -1;

		public void setKillTime(long killTime) {
			this.killTime = killTime;
		}

		private void stopSending() {
			if (running.compareAndSet(true, false)) {
				try {
					socketInitLatch.await();
				} catch (Exception e) {
				}
				serverSocket.close();
				try {
					join();
				} catch (Exception e) {
				}
			}
		}

		private Object lock = new Object();

		public synchronized void update(Context context,
				String sessionDescription) {
			synchronized (lock) {
				this.applicationContext = context.getApplicationContext();

				String manufacturer = Build.MANUFACTURER;
				String model = Build.MODEL;

				sendData = (MESSAGE_PREFIX + "%" + manufacturer + " " + model
						+ "%" + Utils.getDeviceType(applicationContext).name()
						+ "%" + sessionDescription + "%").getBytes();

			}
		}

		Context applicationContext;

		byte[] sendData;

		public void run() {

			try {
				running.set(true);
				serverSocket = new DatagramSocket();
				serverSocket.setBroadcast(true);
				socketInitLatch.countDown();

				Log.i(TAG, "Start sending broadcast");
				InetAddress broadcastAddress = null;

				synchronized (lock) {
					broadcastAddress = Utils
							.getBroadcastAddress(applicationContext);

				}
				int counter = 0;

				while (running.get()) {
					if (killTime != -1 && System.currentTimeMillis() > killTime) {
						Log.i(TAG, "killing broadcast thread");
						running.set(false);
						continue;
					}
					Log.i(TAG, "send broadcast " + (counter++) + " to "
							+ broadcastAddress);

					try {

						synchronized (lock) {
							DatagramPacket sendPacket = new DatagramPacket(
									sendData, sendData.length,
									broadcastAddress, BROADCAST_PORT);
							serverSocket.send(sendPacket);
						}

					} catch (Exception e) {
						try {
							Thread.sleep(SLEEP_TIME_AFTER_EXCEPTION);
						} catch (InterruptedException ie) {

						}
					}
					try {
						Thread.sleep(SLEEP_TIME);
					} catch (InterruptedException e) {
						Log.e(TAG, "wtf", e);
					}
				}
				Log.i(TAG, "Stop sending");

			} catch (Exception e) {
				Log.e(TAG, "", e);

			} finally {
				if (running.compareAndSet(true, false)) {
					if (serverSocket != null) {
						serverSocket.close();
					}
				}
			}
		}
	}
}
