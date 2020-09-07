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

package com.nostalgiaemulators.framework.ui.cheats;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.nostalgiaemulators.framework.utils.Log;

public class Cheat {
	String chars = "";
	String desc = "";
	boolean enable = false;

	public static final String CHEAT_PREF_SUFFIX = ".cheats";

	public Cheat(String chars, String desc, boolean enable) {
		this.chars = chars;
		this.desc = desc;
		this.enable = enable;
	}

	
	public static ArrayList<Cheat> getAllCheats(Context context, String gameHash) {
		ArrayList<Cheat> result = new ArrayList<Cheat>();
		SharedPreferences pref = context.getSharedPreferences(gameHash
				+ CHEAT_PREF_SUFFIX, Context.MODE_PRIVATE);
		@SuppressWarnings("unchecked")
		Map<String, String> all = (Map<String, String>) pref.getAll();
		for (Entry<String, String> item : all.entrySet()) {
			String[] pom = item.getValue().split("\\|");
			if (pom.length > 0) {
				Boolean enable = pom[0].equals("1");
				String desc = pom.length > 1 ? pom[1] : "";
				result.add(new Cheat(item.getKey(), desc, enable));
			}
		}
		return result;
	}

	public static int[] rawToValues(String raw) {
		String comp = null;
		String addr = raw.split("\\:")[0];
		String val = raw.split("\\:")[1];

		if (addr.contains("?")) {
			String[] segments = addr.split("\\?");
			addr = segments[0];
			comp = segments[1];
		}

		int iaddr = Integer.parseInt(addr, 16);
		int ival = Integer.parseInt(val, 16);
		int icomp = -1;
		if (comp != null) {
			icomp = Integer.parseInt(comp, 16);
		}
		Log.i("cheat", "cheat " + valuesToRaw(iaddr, ival, icomp));
		return new int[] { iaddr, ival, icomp };
	}

	public static String valuesToRaw(int addr, int val, int comp) {
		String hexAddr = Integer.toHexString(addr);
		String hexVal = Integer.toHexString(val);
		hexAddr = "0000".substring(hexAddr.length()) + hexAddr;
		hexVal = "00".substring(hexVal.length()) + hexVal;
		String hexComp = null;
		if (comp != -1) {
			hexComp = Integer.toHexString(comp);
			hexComp = "00".substring(hexComp.length()) + hexComp;
		}

		String res = hexAddr + (hexComp != null ? "?" + hexComp + ":" : ":")
				+ hexVal;
		return res;
	}

	
	public static ArrayList<String> getAllEnableCheats(Context context,
			String gameHash) {
		ArrayList<Cheat> cheats = getAllCheats(context, gameHash);
		ArrayList<String> result = new ArrayList<String>();
		StringBuilder cheatLine = new StringBuilder();
		for (Cheat cheat : cheats) {
			if (cheat.enable) {
				String cheatString = cheat.chars;
				cheatLine.setLength(0);

				for (int i = 0; i < cheatString.length(); i++) {
					char c = cheatString.charAt(i);
					if (c == '+') {
						if (cheatLine.length() > 0) {
							result.add(cheatLine.toString());
						}
						cheatLine.setLength(0);
					} else {
						cheatLine.append(c);
					}
				}
				if (cheatLine.length() > 0) {
					result.add(cheatLine.toString());
				}
				result.add(null);
			}
		}
		return result;
	}

	
	public static void saveCheats(Context context, String gameHash,
			ArrayList<Cheat> items) {
		SharedPreferences pref = context.getSharedPreferences(gameHash
				+ CHEAT_PREF_SUFFIX, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.clear();
		for (Cheat cheat : items) {
			if (!cheat.chars.equals("")) {
				editor.putString(cheat.chars, (cheat.enable ? "1" : "0") + "|"
						+ cheat.desc + "|");
			}
		}
		editor.commit();
	}
}
