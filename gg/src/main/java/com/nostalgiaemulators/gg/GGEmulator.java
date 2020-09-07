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

package com.nostalgiaemulators.gg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nostalgiaemulators.framework.BasicEmulatorInfo;
import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.EmulatorInfo;
import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.KeyboardProfile;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.SfxProfile;
import com.nostalgiaemulators.framework.SfxProfile.SoundEncoding;
import com.nostalgiaemulators.framework.base.JniBridge;
import com.nostalgiaemulators.framework.base.JniEmulator;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;

public class GGEmulator extends JniEmulator {

	private GGEmulator() {

	}

	public static GGEmulator getInstance() {
		if (instance == null) {
			instance = new GGEmulator();
		}
		return instance;
	}

	private static GGEmulator instance;

	@Override
	public EmulatorInfo getInfo() {
		if (info == null) {
			info = new Info();
		}
		return info;
	}

	@Override
	public JniBridge getBridge() {
		return Core.getInstance();
	}

	@Override
	public void enableCheat(String gg, int c) {
		int addrVal = -1;
		int valVal = -1;
		gg = gg.replace("-", "");

		if (gg.startsWith("00") && gg.length() == 8) {
			String addr = gg.substring(2, 5 + 1);
			String val = gg.substring(6);
			try {
				addrVal = Integer.parseInt(addr, 16);
				valVal = Integer.parseInt(val, 16);
			} catch (Exception e) {
			}
		}
		if ((addrVal < 0 || valVal < 0)
				|| !getBridge().enableRawCheat(addrVal, valVal, -1)) {
			throw new EmulatorException(R.string.act_emulator_invalid_cheat, gg);
		}
	}

	@Override
	public GfxProfile autoDetectGfx(GameDescription game) {
		return getInfo().getDefaultGfxProfile();
	}

	@Override
	public SfxProfile autoDetectSfx(GameDescription game) {
		return getInfo().getDefaultSfxProfile();
	}

	private static EmulatorInfo info = new Info();

	private static class Info extends BasicEmulatorInfo {

		@Override
		public boolean hasZapper() {
			return false;
		}

		@Override
		public boolean isMultiPlayerSupported() {
			return false;
		}

		@Override
		public String getName() {
			return "Nostalgia.GG";
		}

		@Override
		public GfxProfile getDefaultGfxProfile() {
			return profiles.get(0);
		}

		@Override
		public SfxProfile getDefaultSfxProfile() {
			return sfxProfiles.get(0);
		}

		@Override
		public KeyboardProfile getDefaultKeyboardProfile() {
			return null;
		}

		@Override
		public List<GfxProfile> getAvailableGfxProfiles() {
			return profiles;
		}

		@Override
		public List<SfxProfile> getAvailableSfxProfiles() {
			return sfxProfiles;
		}

		@Override
		public boolean supportsRawCheats() {
			return false;
		}

		@Override
		public Map<Integer, Integer> getKeyMapping() {
			HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();

			mapping.put(EmulatorController.KEY_UP, 0x01);
			mapping.put(EmulatorController.KEY_DOWN, 0x02);
			mapping.put(EmulatorController.KEY_LEFT, 0x04);
			mapping.put(EmulatorController.KEY_RIGHT, 0x08);

			mapping.put(EmulatorController.KEY_A, 0x10);
			mapping.put(EmulatorController.KEY_B, 0x20);

			mapping.put(EmulatorController.KEY_START, 0x80);
			mapping.put(EmulatorController.KEY_SELECT, -1);

			mapping.put(EmulatorController.KEY_A_TURBO, 0x10 + 1000);
			mapping.put(EmulatorController.KEY_B_TURBO, 0x20 + 1000);

			return mapping;
		}

		@Override
		public int[] getDeviceKeyboardCodes() {
			int[] codes = super.getDeviceKeyboardCodes();
			int[] newCodes = new int[codes.length - 1];
			int i = 0;
			for (int code : codes) {
				if (code != EmulatorController.KEY_SELECT) {
					newCodes[i++] = code;
				}
			}
			return newCodes;
		}

		@Override
		public String[] getDeviceKeyboardNames() {
			String[] allNames = super.getDeviceKeyboardNames();
			String[] names = new String[allNames.length - 1];
			int j = 0;
			for (int i = 0; i < allNames.length; i++) {
				if (!allNames[i].equals("SELECT")) {
					names[j++] = allNames[i];
				}
			}

			for (int i = 0; i < names.length; i++) {
				String name = names[i];
				if (name.equals("A")) {
					names[i] = "1";
				}
				if (name.equals("B")) {
					names[i] = "2";
				}
				if (name.startsWith("TURBO")) {
					names[i] = name.replace(" A", " 1").replace(" B", " 2");
				}
				if (name.equals("A+B")) {
					names[i] = "1+2";
				}
			}
			return names;
			

		}

		@Override
		public int getNumQualityLevels() {
			return 3;
		}

		private static class GGGfxProfile extends GfxProfile {
			@Override
			public int toInt() {
				return 0;
			}
		}

		private static class GGSfxProfile extends SfxProfile {
			@Override
			public int toInt() {
				return this.rate;
			}
		}

		static List<GfxProfile> profiles = new ArrayList<GfxProfile>();
		static List<SfxProfile> sfxProfiles = new ArrayList<SfxProfile>();
		static {
			GfxProfile prof = new GGGfxProfile();
			prof.fps = 60;
			prof.name = "default";
			prof.originalScreenWidth = 160;
			prof.originalScreenHeight = 144;
			profiles.add(prof);
			SfxProfile sfx = new GGSfxProfile();
			sfx.name = "low";
			sfx.isStereo = true;
			sfx.encoding = SoundEncoding.PCM16;
			sfx.bufferSize = 2048 * 8 * 2;
			sfx.quality = 0;
			sfx.rate = 22050;
			sfxProfiles.add(sfx);

			sfx = new GGSfxProfile();
			sfx.name = "medium";
			sfx.isStereo = true;
			sfx.encoding = SoundEncoding.PCM16;
			sfx.bufferSize = 2048 * 8 * 2;
			sfx.rate = 44100;
			sfx.quality = 1;
			sfxProfiles.add(sfx);

			sfx = new GGSfxProfile();
			sfx.name = "high";
			sfx.isStereo = true;
			sfx.encoding = SoundEncoding.PCM16;
			sfx.bufferSize = 2048 * 8 * 2;
			sfx.rate = 44100;
			sfx.quality = 2;
			sfxProfiles.add(sfx);

		}

	}

	public final static String PACK_SUFFIX = "nggs";

}
