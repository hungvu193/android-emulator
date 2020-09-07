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

import java.util.HashSet;
import java.util.Set;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.remote.wifi.WifiControllerServer;
import com.nostalgiaemulators.framework.ui.gamegallery.GalleryActivity;

public class GGGalleryActivity extends GalleryActivity {

	WifiControllerServer wifiControllerServer;

	private static final int REQUEST_CHECK_OPENGL = 200;

	@Override
	public Emulator getEmulatorInstance() {
		return GGEmulator.getInstance();
	}

	@Override
	public Class<? extends EmulatorActivity> getEmulatorActivityClass() {
		return GGEmulatorActivity.class;
	}

	@Override
	protected Set<String> getRomExtensions() {
		HashSet<String> set = new HashSet<String>();
		set.add("gg");
		return set;
	}

}
