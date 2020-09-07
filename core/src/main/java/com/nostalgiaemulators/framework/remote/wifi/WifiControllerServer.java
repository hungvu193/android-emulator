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
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.KeyEvent;

import com.nostalgiaemulators.framework.remote.ControllerEventSource;
import com.nostalgiaemulators.framework.remote.ControllerKeyEvent;
import com.nostalgiaemulators.framework.remote.OnControllerEventListener;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

public class WifiControllerServer implements ControllerEventSource {
	private static final String TAG = "com.nostalgiaemulators.framework.remote.wifi.WifiControllerServer";

	public enum PACKET_TYPE {
		PING_PACKET, EMULATOR_KEY_PACKET, ANDROID_KEY_PACKET, TEXT_PACKET, COMMAND_PACKET
	};

	String sessionDescription;

	private WifiControllerServer(Context context, String sessionDescription) {
		this.sessionDescription = sessionDescription;
		this.context = context.getApplicationContext();
	}

	private Context context;

	public static WifiControllerServer getInstance(Context context,
			String sessionDescription) {
		if (instance == null) {
			instance = new WifiControllerServer(context, sessionDescription);
		}
		instance.sessionDescription = sessionDescription;
		return instance;
	}

	public void setSessionDescription(String s) {
		sessionDescription = s;
	}

	@Override
	public void setControllerEventListener(OnControllerEventListener listener) {
		this.listener = listener;
		if (serverThread != null) {
			serverThread.setListener(listener);
		}
	}

	private ServerThread serverThread;
	private OnControllerEventListener listener;

	@Override
	public void onResume() {

		if (PreferenceUtil.isWifiServerEnable(context)
				&& Utils.isWifiAvailable(context)) {
			if (serverThread == null || !serverThread.running.get()) {
				serverThread = new ServerThread();
				serverThread.startListening(listener);
			}

		} else {
			if (serverThread != null) {
				serverThread.stopListening();
				serverThread = null;
			}
		}

	}

	@Override
	public void onPause() {
		if (serverThread != null) {
			serverThread.setListener(null);
		}
	}

	private static class ServerThread extends Thread {
		private OnControllerEventListener eventListener;

		public void setListener(OnControllerEventListener listener) {
			synchronized (listenerLock) {
				this.eventListener = listener;
			}
		}

		protected AtomicBoolean running = new AtomicBoolean(false);
		private byte[] buffer = new byte[WifiControllerClient.PACKET_SIZE];
		private Object listenerLock = new Object();

		public void startListening(OnControllerEventListener eventListener) {
			synchronized (listenerLock) {
				this.eventListener = eventListener;
			}
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
			synchronized (listenerLock) {
				eventListener = null;
			}
		}

		DatagramSocket serverSocket = null;
		CountDownLatch socketInitLatch = new CountDownLatch(1);

		public void run() {
			running.set(true);
			Log.d(TAG, "Starting remote controller SERVER THREAD " + getName());
			SparseIntArray lastKeyStates = new SparseIntArray();

			try {
				DatagramChannel channel = DatagramChannel.open();
				serverSocket = channel.socket();
				serverSocket.setReuseAddress(true);
				serverSocket.bind(new InetSocketAddress(SERVER_PORT));

				serverSocket.setSoTimeout(0);

				Log.i(TAG, "Start listening on");
				socketInitLatch.countDown();
				while (running.get()) {
					DatagramPacket packet = new DatagramPacket(buffer,
							WifiControllerClient.PACKET_SIZE);
					try {
						serverSocket.receive(packet);

						byte[] data = packet.getData();
						ByteBuffer bb = ByteBuffer.wrap(data);
						PACKET_TYPE packetType = PACKET_TYPE.values()[bb
								.getInt(0)];

						switch (packetType) {
						case PING_PACKET:
							DatagramPacket responsePacket = new DatagramPacket(
									new byte[] { 1, 1, 1, 1 }, 4,
									packet.getAddress(), packet.getPort());
							Log.i(TAG, "sending response packet");
							serverSocket.send(responsePacket);
							break;
						case EMULATOR_KEY_PACKET: {
							int port = bb.getInt(4);
							int keyStates = bb.getInt(8);
							int lastKeyState = lastKeyStates.get(port);
							if (lastKeyState != keyStates) {
								lastKeyStates.put(port, keyStates);
								int mask = 1;
								for (int i = 0; i < 32; i++) {
									int s = keyStates & mask;
									int l = lastKeyState & mask;
									if (s != l) {
										ControllerKeyEvent res = new ControllerKeyEvent();
										res.action = s == mask ? 0 : 1;
										res.keyCode = i;
										res.port = port;
										synchronized (listenerLock) {
											if (eventListener != null) {
												eventListener
														.onControllerEmulatorKeyEvent(res);
											}
										}

									}

									mask = (mask << 1);
								}
							}
							break;
						}
						case ANDROID_KEY_PACKET: {
							int keyCode = bb.getInt(4);
							int keyAction = bb.getInt(8);
							KeyEvent event = new KeyEvent(keyAction, keyCode);
							synchronized (listenerLock) {
								if (eventListener != null) {
									eventListener
											.onControllerAndroidKeyEvent(event);
								}
							}

							break;
						}
						case TEXT_PACKET: {
							int len = bb.getInt(4);
							byte[] txtbuffer = new byte[len];
							DatagramPacket txtpacket = new DatagramPacket(
									txtbuffer, len);
							serverSocket.receive(txtpacket);
							byte[] arr = txtpacket.getData();

							String txt = new String(arr, 0, len);
							synchronized (listenerLock) {
								if (eventListener != null) {
									eventListener.onControllerTextEvent(txt);
								}
							}

							break;
						}
						case COMMAND_PACKET: {
							int command = bb.getInt(4);
							int param1 = bb.getInt(8);
							int param2 = bb.getInt(12);

							synchronized (listenerLock) {
								if (eventListener != null) {
									eventListener.onControllerCommandEvent(
											command, param1, param2);
								}
							}

							break;
						}

						}

					} catch (SocketTimeoutException e) {
						Log.e(TAG, "timeout");
					} catch (SocketException e) {
						if (running.get()) {
							Log.e(TAG, "socket close", e);
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						Log.e(TAG, "Non supported packet", e);
					} catch (Exception e) {
						Log.e(TAG, "", e);
					}
				}
				Log.d(TAG, "Stopping remote controller SERVER THREAD "
						+ getName());

			} catch (Exception e) {
				Log.e(TAG, "Error: SERVER STOPPED " + getName());
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

	public static final int SERVER_PORT = 53216;

	private static WifiControllerServer instance;

}
