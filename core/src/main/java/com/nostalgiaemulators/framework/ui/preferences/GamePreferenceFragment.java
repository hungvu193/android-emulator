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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;


@SuppressLint("NewApi")
public class GamePreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		GameDescription game = (GameDescription) getActivity().getIntent()
				.getSerializableExtra(GamePreferenceActivity.EXTRA_GAME);

		PreferenceManager prefMgr = getPreferenceManager();
		prefMgr.setSharedPreferencesName(game.checksum + ".gamepref");

		addPreferencesFromResource(R.xml.game_preferences);

		final ListPreference videoProfile = (ListPreference) findPreference("game_pref_ui_pal_ntsc_switch");
		final PreferenceCategory videoProfileCategory = (PreferenceCategory) findPreference("game_pref_ui_pal_ntsc_switch_category");
		final PreferenceCategory zapperCategory = (PreferenceCategory) findPreference("game_pref_other_category");
		final Preference zapper = findPreference("game_pref_zapper");
		GamePreferenceActivity.initZapper(zapper, zapperCategory);
		GamePreferenceActivity.initVideoPreference(videoProfile,
				videoProfileCategory, getPreferenceScreen());

	}
}
