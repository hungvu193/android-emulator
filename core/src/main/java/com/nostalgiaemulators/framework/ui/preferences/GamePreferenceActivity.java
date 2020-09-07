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

package com.nostalgiaemulators.framework.ui.preferences;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.remote.VirtualDPad;
import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoTransmitter;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;

public class GamePreferenceActivity extends AppCompatActivity {

	public static final String EXTRA_GAME = "EXTRA_GAME";
	private GameDescription game;
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (IllegalStateException e) {
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			finish();
			return true;
		}else {
			return super.onOptionsItemSelected(item);
		}
	}


	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		game = (GameDescription) getIntent().getSerializableExtra(EXTRA_GAME);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new GamePreferenceFragment()).commit();
	}

	static void initZapper(Preference zapper, PreferenceCategory zapperCategory) {
		if (!EmulatorInfoHolder.getInfo().hasZapper()) {
			zapperCategory.removePreference(zapper);
		}
	}

	static void initVideoPreference(ListPreference preference,
			PreferenceCategory category, PreferenceScreen screen) {
		List<GfxProfile> profiles = EmulatorInfoHolder.getInfo()
				.getAvailableGfxProfiles();

		if (profiles.size() > 1) {
			CharSequence[] res = new CharSequence[EmulatorInfoHolder.getInfo()
					.getAvailableGfxProfiles().size() + 1];

			res[0] = "Auto";
			int i = 1;
			for (GfxProfile gfx : profiles) {
				res[i] = gfx.name;
				i++;
			}
			preference.setEntries(res);
			preference.setEntryValues(res);
			if (preference.getValue() == null) {
				preference.setValue("Auto");
			}

		} else {
			category.removePreference(preference);
			screen.removePreference(category);

		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		WifiServerInfoTransmitter.onResume(this, game.name);
		VirtualDPad.getInstance().onResume(getWindow());
	}

	@Override
	protected void onPause() {
		super.onPause();
		WifiServerInfoTransmitter.onPause();
		VirtualDPad.getInstance().onPause();
	}



}
