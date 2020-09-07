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

package com.nostalgiaemulators.nes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nostalgiaemulators.framework.BasicEmulatorInfo;
import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.EmulatorInfo;
import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.SfxProfile;
import com.nostalgiaemulators.framework.SfxProfile.SoundEncoding;
import com.nostalgiaemulators.framework.base.JniBridge;
import com.nostalgiaemulators.framework.base.JniEmulator;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;

public class NesEmulator extends JniEmulator {

	private NesEmulator() {
	}

	public static JniEmulator getInstance() {
		if (instance == null) {
			instance = new NesEmulator();
		}
		return instance;
	}

	@Override
	public GfxProfile autoDetectGfx(GameDescription game) {
		String name = game.getCleanName();
		name = name.toLowerCase();
		if (name.contains("(e)") || name.contains("(europe)")
				|| name.contains("(f)") || name.contains("(g)")
				|| name.contains("(i)") || name.contains("(pal)")
				|| name.contains("[e]") || name.contains("[f]")
				|| name.contains("[g]") || name.contains("[i]")
				|| name.contains("[europe]") || name.contains("[pal]")) {
			return Info.pal;
		} else {
			for (String pal : palExclusiveKeywords) {
				if (pal.startsWith("$")) {
					pal = pal.substring(1);
					if (name.startsWith(pal)) {
						return Info.pal;
					}
				} else {
					String[] kws = new String[1];
					kws[0] = pal;
					if (pal.startsWith(".")) {
						pal = pal.substring(1);
						kws = pal.split("\\|");
					}
					for (String keyWord : kws) {
						if (name.contains(keyWord)) {
							return Info.pal;
						}
					}
				}
			}
		}
		if (Arrays.asList(palHashes).contains(game.checksum)) {
			return Info.pal;
		}
		return getInfo().getDefaultGfxProfile();
	}

	public String[] palExclusiveKeywords = new String[] { ".beauty|beast",
			".hammerin|harry", ".noah|ark", ".rockets|rivals",
			".formula|sensation", ".trolls|crazyland", "asterix", "elite",
			"smurfs", "international cricket", "turrican", "valiant",
			"aladdin", "aussie rules", "banana prince", "chevaliers",
			"crackout", "devil world", "kick off", "hyper soccer", "ufouria",
			"lion king", "gimmick", "dropzone", "drop zone", "$mario bros",
			"road fighter", "rodland", "parasol stars", "parodius",
			"over horizon", "championship rally", "aussio rules", };

	public String[] palHashes = new String[] {
			"85ce1107c922600990884d63c75cfec4",
			"6f6d5cc27354e1527fc88ec97c8b7c27",
			"83c8b2142884965c2214196f3f71f6ec",
			"caf9d44ae71fa8ade852fb453d797798",
			"fe36a09cd6c94916d48ea61776978cc8",
			"3eb49813c3c5b6088bfed3f1d7ecaa0e",
			"b40b25a9bc54eb8f46310fae45723759",
			"d91a5f3e924916eb16bb6a3255f532bc", };

	@Override
	public SfxProfile autoDetectSfx(GameDescription game) {
		return getInfo().getDefaultSfxProfile();
	}

	@Override
	public JniBridge getBridge() {
		return Core.getInstance();
	}

	@Override
	public EmulatorInfo getInfo() {
		if (info == null) {
			info = new Info();
		}
		return info;
	}

	private static EmulatorInfo info;
	private static NesEmulator instance;

	private static class Info extends BasicEmulatorInfo {

		public boolean hasZapper() {
			return true;
		}

		@Override
		public Map<Integer, Integer> getKeyMapping() {
			HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();
			mapping.put(EmulatorController.KEY_A, 0x01);
			mapping.put(EmulatorController.KEY_B, 0x02);
			mapping.put(EmulatorController.KEY_SELECT, 0x04);
			mapping.put(EmulatorController.KEY_START, 0x08);
			mapping.put(EmulatorController.KEY_UP, 0x10);
			mapping.put(EmulatorController.KEY_DOWN, 0x20);
			mapping.put(EmulatorController.KEY_LEFT, 0x40);
			mapping.put(EmulatorController.KEY_RIGHT, 0x80);

			mapping.put(EmulatorController.KEY_A_TURBO, 0x01 + 1000);
			mapping.put(EmulatorController.KEY_B_TURBO, 0x02 + 1000);

			return mapping;
		}

		@Override
		public String getName() {
			return "Nostalgia.NES";
		}

		@Override
		public GfxProfile getDefaultGfxProfile() {
			return ntsc;
		}

		@Override
		public SfxProfile getDefaultSfxProfile() {
			return sfxProfiles.get(0);
		}

		@Override
		public List<GfxProfile> getAvailableGfxProfiles() {
			return gfxProfiles;
		}

		@Override
		public List<SfxProfile> getAvailableSfxProfiles() {
			return sfxProfiles;
		}

		private static List<SfxProfile> sfxProfiles = new ArrayList<SfxProfile>();
		private static List<GfxProfile> gfxProfiles = new ArrayList<GfxProfile>();

		private static class NesGfxProfile extends GfxProfile {
			@Override
			public int toInt() {
				return fps == 50 ? 1 : 0;
			}
		}

		private static class NesSfxProfile extends SfxProfile {
			@Override
			public int toInt() {
				int x = rate / 11025;
				x += quality * 100;
				return x;
			}
		}

		private static GfxProfile pal;
		private static GfxProfile ntsc;

		static {

			pal = new NesGfxProfile();
			pal.fps = 50;
			pal.name = "PAL";
			pal.originalScreenWidth = 256;
			pal.originalScreenHeight = 240;

			ntsc = new NesGfxProfile();
			ntsc.fps = 60;
			ntsc.name = "NTSC";
			ntsc.originalScreenWidth = 256;
			ntsc.originalScreenHeight = 224;

			gfxProfiles.add(ntsc);
			gfxProfiles.add(pal);
			SfxProfile low = new NesSfxProfile();
			low.name = "low";
			low.bufferSize = 2048 * 8 * 2;
			low.encoding = SoundEncoding.PCM16;
			low.isStereo = true;
			low.rate = 11025;
			low.quality = 0;
			sfxProfiles.add(low);

			SfxProfile medium = new NesSfxProfile();
			medium.name = "medium";
			medium.bufferSize = 2048 * 8 * 2;
			medium.encoding = SoundEncoding.PCM16;
			medium.isStereo = true;
			medium.rate = 22050;
			medium.quality = 1;
			sfxProfiles.add(medium);

			SfxProfile high = new NesSfxProfile();
			high.name = "high";
			high.bufferSize = 2048 * 8 * 2;
			high.encoding = SoundEncoding.PCM16;
			high.isStereo = true;
			high.rate = 44100;
			high.quality = 2;
			sfxProfiles.add(high);
		}

		public boolean supportsRawCheats() {
			return true;
		}

		@Override
		public int getNumQualityLevels() {
			return 3;
		}

	}

	public static final String PACK_SUFFIX = "nness";
}
