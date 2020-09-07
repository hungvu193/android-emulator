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

package com.nostalgiaemulators.framework.utils;

public class Log {
	public static void setDebugMode(boolean debug) {
		if (debug) {
			WTF = true;
			E = true;
			W = true;
			D = true;
			I = true;
			V = true;
		}
	}
	private static boolean WTF = true;
	private static boolean E = true;
	private static boolean W = true;
	private static boolean D = false;
	private static boolean I = false;
	private static boolean V = false;

	public static void e(String tag, String msg) {
		if (E)
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable e) {
		if (E)
			android.util.Log.e(tag, msg, e);
	}

	public static void d(String tag, String msg) {
		if (D)
			android.util.Log.d(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (W)
			android.util.Log.w(tag, msg);
	}

	public static void i(String tag, String msg) {
		if (I)
			android.util.Log.i(tag, msg);
	}

	public static void v(String tag, String msg) {
		if (V)
			android.util.Log.i(tag, msg);
	}

	public static void wtf(String tag, String msg) {
		if (WTF)
			android.util.Log.wtf(tag, msg);
	}

	public static void wtf(String tag, String msg, Throwable th) {
		if (WTF)
			android.util.Log.wtf(tag, msg, th);
	}
}
