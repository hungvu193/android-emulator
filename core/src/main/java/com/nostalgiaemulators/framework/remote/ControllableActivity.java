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

package com.nostalgiaemulators.framework.remote;

import android.app.Activity;

import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoTransmitter;

public abstract class ControllableActivity extends Activity {

	@Override
	protected void onResume() {
		super.onResume();
		startWifiListening();

	}
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (IllegalStateException e) {
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopWifiListening();
	}

	protected void startWifiListening() {
		VirtualDPad.getInstance().onResume(getWindow());
		WifiServerInfoTransmitter.onResume(this, "");
	}

	protected void stopWifiListening() {
		VirtualDPad.getInstance().onPause();
		WifiServerInfoTransmitter.onPause();
	}

}
