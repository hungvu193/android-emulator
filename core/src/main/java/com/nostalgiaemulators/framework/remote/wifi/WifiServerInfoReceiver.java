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
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils.ServerType;

public class WifiServerInfoReceiver {

	private static final String TAG = "com.nostalgiaemulators.framework.remote.wifi.BroadcastReceiverService";

	public class DetectionResult {
		public InetAddress address;
		public String desc;
		public String sessionDescription = "";
		public SparseArray<String> slots = new SparseArray<String>();
		public ServerType type = ServerType.mobile;
		long lastDetect = 0;

		public DetectionResult(InetAddress address, String desc,
				String sessionDescription, ServerType type) {
			this.address = address;
			this.desc = desc;
			this.sessionDescription = sessionDescription;
			this.type = type;
			lastDetect = System.currentTimeMillis();
		}

		@Override
		public String toString() {
			return desc;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof DetectionResult) {
				DetectionResult oo = (DetectionResult) o;
				if (desc.equals(oo.desc) && oo.address.equals(address)) {
					return true;
				} else {
					return false;
				}
			} else
				return false;
		}
	}

	BroadcastReceiverThread broadcastReceiverThread;

	public interface BroadcastReceiverListener {
		public void onServerDetect(DetectionResult result);
	}

	BroadcastReceiverListener listener;

	public void startExploring(final Context context,
			BroadcastReceiverListener listener) {
		this.listener = listener;

		stop();

		broadcastReceiverThread = new BroadcastReceiverThread();
		broadcastReceiverThread.startListening();
	}

	public void stop() {
		if (broadcastReceiverThread != null) {
			broadcastReceiverThread.stopListening();
		}
	}

	private class BroadcastReceiverThread extends Thread {
		protected AtomicBoolean running = new AtomicBoolean();

		public void startListening() {
			stopListening();
			start();
		}

		public void stopListening() {
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

		DatagramSocket serverSocket = null;
		CountDownLatch socketInitLatch = new CountDownLatch(1);

		public void run() {
			running.set(true);
			byte[] recvBuf = new byte[300];

			SendHandler sendHandler = new SendHandler(listener);
			try {
				DatagramChannel channel = DatagramChannel.open();
				serverSocket = channel.socket();
				serverSocket.setReuseAddress(true);
				serverSocket.bind(new InetSocketAddress("0.0.0.0",
						WifiServerInfoTransmitter.BROADCAST_PORT));

				serverSocket.setSoTimeout(0);
				socketInitLatch.countDown();
				Log.i(TAG, "Start listening broadcast:" + running);
				while (running.get()) {
					try {
						DatagramPacket packet = new DatagramPacket(recvBuf,
								recvBuf.length);

						serverSocket.receive(packet);
						Log.i(TAG, "recive from:"
								+ packet.getAddress().getHostAddress());

						String data = new String(packet.getData());
						String[] items = data.split("%");
						if (items.length >= 3) {
							if (items[0]
									.equals(WifiServerInfoTransmitter.MESSAGE_PREFIX)) {

								Message msg = new Message();

								msg.obj = new DetectionResult(
										packet.getAddress(), items[1],
										items[3], ServerType.valueOf(items[2]));
								sendHandler.sendMessage(msg);
							}
						}

					} catch (SocketTimeoutException e) {
						Log.i(TAG, "timeout");
					} catch (SocketException e) {
						if (running.get()) {
							Log.i(TAG, "socket close");
						}
					} catch (Exception e) {
						Log.e(TAG, "", e);
					}
				}
				Log.i(TAG, "Stop listening");

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

	private static class SendHandler extends Handler {
		public SendHandler(BroadcastReceiverListener listener) {
			super(Looper.getMainLooper());
			this.listener = listener;
		}

		public void handleMessage(android.os.Message msg) {
			listener.onServerDetect((DetectionResult) msg.obj);
		};

		BroadcastReceiverListener listener;
	}

}
