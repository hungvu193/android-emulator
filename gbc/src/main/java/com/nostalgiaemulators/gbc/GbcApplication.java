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

package com.nostalgiaemulators.gbc;

import com.nostalgiaemulators.framework.AdProvider;
import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.R;

public class GbcApplication extends EmulatorApplication {


	@Override
	public String getAppWallUrl() {
		return "";
	}

	@Override
	public String getPackFileSuffix() {
		return GbcEmulator.PACK_SUFFIX;
	}

	@Override
	public int[] getGalleryHelpIds() {
		return new int[] {
				com.nostalgiaemulators.framework.R.string.help_dynamic_dpad,
				com.nostalgiaemulators.framework.R.string.help_customize_controll,
				com.nostalgiaemulators.framework.R.string.help_sav_files,
				R.string.help_cheats,
				com.nostalgiaemulators.framework.R.string.help_state_share, };
	}

	@Override
	public AdProvider getAdProvider() {
		return null;
	}

	@Override
	public boolean hasGameMenu() {
		return false;
	}

}