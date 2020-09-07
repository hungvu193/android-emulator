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
import java.nio.ByteBuffer;
import java.util.LinkedList;

import android.util.Pair;
import android.view.KeyEvent;

import com.nostalgiaemulators.framework.remote.wifi.WifiControllerServer.PACKET_TYPE;
import com.nostalgiaemulators.framework.utils.Log;

public class WifiControllerClient {
	private static final String TAG = "com.nostalgiaemulators.framework.remote.wifi.WifiControllerClient";
	private InetAddress ip;
	public static final int PACKET_SIZE = 32;
	private int keysStates = 0;

	ByteBuffer byteBuffer = ByteBuffer.allocate(PACKET_SIZE);

	public WifiControllerClient(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
		byteBuffer.clear();
		keysStates = 0;
	}

	private class SenderThread extends Thread {

		@Override
		public void run() {
			Log.d(TAG, "Wifi client thread start");
			while (running) {
				if (needsSend) {
					synchronized (keyFifo) {
						KeyEvent event = keyFifo.poll();
						while (event != null) {
							sendAndroidKeyEvent(event);
							event = keyFifo.poll();
						}
					}
					sendKeyStates(port);
					if (textEvent != null) {
						synchronized (textEvent) {
							sendTextEvent(textEvent);
							textEvent = null;
						}
					}
					if (commandEvent != null) {
						synchronized (commandEvent) {
							Pair<Integer, Integer> params = commandEvent.second;
							sendCommandEvent(commandEvent.first, params.first,
									params.second);
							commandEvent = null;
						}
					}

					needsSend = false;
					counter = 0;
				} else {
					counter += DELAY;
				}
				if (counter >= 300) {
					needsSend = true;
				}
				try {
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {

				}
			}
			Log.d(TAG, "Wifi client thread stop");
		}

		public void forceSend() {
			needsSend = true;
		}

		public void finish() {
			running = false;
		}

		public void sendAndroidKeyEventForce(KeyEvent event) {
			synchronized (keyFifo) {
				keyFifo.add(event);
			}
			needsSend = true;
		}

		public void sendTextEventForce(String event) {
			textEvent = event;
			needsSend = true;
		}

		public void sendCommandEventForce(int command, int param0, int param1) {
			commandEvent = new Pair<Integer, Pair<Integer, Integer>>(command,
					new Pair<Integer, Integer>(param0, param1));
			needsSend = true;
		}

		private LinkedList<KeyEvent> keyFifo = new LinkedList<KeyEvent>();
		private String textEvent = null;
		private Pair<Integer, Pair<Integer, Integer>> commandEvent = null;
		private final int DELAY = 20;
		private int counter;
		private boolean needsSend = false;
		private volatile boolean running = true;
	}

	private int port;
	private SenderThread thread;

	
	public synchronized void sendControllerAndroidKeyEvent(KeyEvent event) {
		thread.sendAndroidKeyEventForce(event);
	}

	
	public synchronized void sendControllerTextEvent(String text) {
		thread.sendTextEventForce(text);
	}

	
	public synchronized void sendControllerCommandEvent(int command,
			int param0, int param1) {
		thread.sendCommandEventForce(command, param0, param1);
	}

	
	public synchronized void sendControllerEmulatorKeyEvent(final int action,
			final int keyCode) {

		if (action == 1) {
			keysStates = keysStates | (1 << keyCode);
		} else if (action == 0) {
			keysStates = keysStates & ~(1 << keyCode);
		}
		thread.forceSend();

	}

	long lastSendTimestamp = 0;
	private void sendKeyStates(final int port) {
		try {
			lastSendTimestamp = System.currentTimeMillis();
			Log.i(TAG, "send new event:" + Integer.toBinaryString(keysStates)
					+ " port:" + port + " ip:" + ip);
			byteBuffer.clear();
			byteBuffer.putInt(0, PACKET_TYPE.EMULATOR_KEY_PACKET.ordinal());
			byteBuffer.putInt(4, port);
			byteBuffer.putInt(8, keysStates);

			DatagramPacket sendPacket = new DatagramPacket(byteBuffer.array(),
					PACKET_SIZE, ip, WifiControllerServer.SERVER_PORT);
			DatagramSocket clientSocket = null;
			try {
				clientSocket = new DatagramSocket();
				clientSocket.send(sendPacket);
			} finally {
				if (clientSocket != null) {
					clientSocket.close();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}

	}

	private void sendAndroidKeyEvent(final KeyEvent event) {
		try {
			byteBuffer.clear();
			byteBuffer.putInt(0, PACKET_TYPE.ANDROID_KEY_PACKET.ordinal());
			byteBuffer.putInt(4, event.getKeyCode());
			byteBuffer.putInt(8, event.getAction());

			DatagramPacket sendPacket = new DatagramPacket(byteBuffer.array(),
					PACKET_SIZE, ip, WifiControllerServer.SERVER_PORT);
			DatagramSocket clientSocket = null;
			try {
				clientSocket = new DatagramSocket();
				clientSocket.send(sendPacket);
			} finally {
				if (clientSocket != null) {
					clientSocket.close();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}

	}

	private void sendTextEvent(final String text) {
		try {
			Log.i(TAG, "send new text event:" + text + " ip:" + ip);
			byteBuffer.clear();
			byte[] textData = text.getBytes();
			byteBuffer.putInt(0, PACKET_TYPE.TEXT_PACKET.ordinal());
			byteBuffer.putInt(4, textData.length);

			DatagramSocket clientSocket = null;
			try {
				clientSocket = new DatagramSocket();

				DatagramPacket sendPacket = new DatagramPacket(
						byteBuffer.array(), PACKET_SIZE, ip,
						WifiControllerServer.SERVER_PORT);
				clientSocket.send(sendPacket);

				sendPacket = new DatagramPacket(textData, textData.length, ip,
						WifiControllerServer.SERVER_PORT);
				clientSocket.send(sendPacket);
			} finally {
				if (clientSocket != null) {
					clientSocket.close();
				}
			}

		} catch (Exception e) {
			Log.e(TAG, "", e);
		}

	}

	private void sendCommandEvent(int command, int param0, int param1) {
		try {
			Log.i(TAG, "send command event:" + command + " " + param0 + " "
					+ param1);
			byteBuffer.clear();

			byteBuffer.putInt(0, PACKET_TYPE.COMMAND_PACKET.ordinal());
			byteBuffer.putInt(4, command);
			byteBuffer.putInt(8, param0);
			byteBuffer.putInt(12, param1);

			DatagramSocket clientSocket = null;
			try {
				clientSocket = new DatagramSocket();

				DatagramPacket sendPacket = new DatagramPacket(
						byteBuffer.array(), PACKET_SIZE, ip,
						WifiControllerServer.SERVER_PORT);
				clientSocket.send(sendPacket);

			} finally {
				if (clientSocket != null) {
					clientSocket.close();
				}
			}

		} catch (Exception e) {
			Log.e(TAG, "", e);
		}

	}

	public void onResume() {
		if (thread != null) {
			thread.finish();
		}
		thread = new SenderThread();
		thread.start();
	}

	public void onPause() {
		if (thread != null) {
			thread.finish();
		}
	}

	public void onStop() {
		if (thread != null) {
			thread.finish();
		}
	}

}
