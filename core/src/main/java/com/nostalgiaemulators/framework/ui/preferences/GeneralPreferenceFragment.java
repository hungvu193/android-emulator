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
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.hlidskialf.android.preference.SeekBarPreference;
import com.nostalgiaemulators.framework.R;


@SuppressLint("NewApi")
public class GeneralPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		addPreferencesFromResource(R.xml.general_preferences);

		SeekBarVibrationPreference vibration = (SeekBarVibrationPreference) findPreference("game_pref_ui_strong_vibration");

		String vs = Context.VIBRATOR_SERVICE;
		Vibrator mVibrator = (Vibrator) getActivity().getSystemService(vs);

		vibration.setEnabled(mVibrator.hasVibrator());
		if (!mVibrator.hasVibrator()) {
			vibration.setSummary(R.string.game_pref_ui_vibration_no_vibrator);
		}

		final ListPreference selectProfile = (ListPreference) findPreference("pref_game_keyboard_profile");
		final Preference editProfile = findPreference("pref_game_keyboard_edit_profile");

		GeneralPreferenceActivity activity = (GeneralPreferenceActivity) getActivity();

		PreferenceCategory savCat = (PreferenceCategory) findPreference("general_pref_sav_cat");
		activity.initSavFilePreferences(getPreferenceScreen(), savCat, getActivity());

		activity.initProfiles(selectProfile, editProfile);
		activity.setNewProfile(selectProfile, editProfile,
				selectProfile.getValue());
		activity.initWorkingDir(findPreference("general_pref_working_dir"),
				findPreference("general_pref_working_dir_change_to_default"),
				findPreference("general_pref_working_dir_copy_content"));

		PreferenceCategory cat = (PreferenceCategory) findPreference("pref_general_settings_cat");
		Preference quality = findPreference("general_pref_quality");

		GeneralPreferenceActivity.initQuality(cat, quality);

		Preference immersive = findPreference("general_pref_immersive_mode");
		GeneralPreferenceActivity.initProPreference(immersive, getActivity());

		Preference quicksave = findPreference("general_pref_quicksave");
		GeneralPreferenceActivity.initProPreference(quicksave, getActivity());

		Preference shader = findPreference("general_pref_shader");
		GeneralPreferenceActivity.initProPreference(shader, getActivity());

		Preference autoHide = findPreference("general_pref_ui_autohide");
		GeneralPreferenceActivity.initProPreference(autoHide, getActivity());

		Preference font = findPreference("general_pref_use_system_font");
		GeneralPreferenceActivity.initProPreference(font, getActivity());

		SeekBarPreference volume = (SeekBarPreference) findPreference("general_pref_sound_volume");
		GeneralPreferenceActivity.initVolume(volume, getActivity());

		Preference opacity = findPreference("general_pref_ui_opacity");
		GeneralPreferenceActivity.initProPreference(opacity, getActivity());

		CheckBoxPreference ddpad = (CheckBoxPreference) findPreference("general_pref_ddpad");
		GeneralPreferenceActivity.initDDPAD(ddpad, getActivity());

		Preference screen = (Preference) findPreference("general_pref_screen_layout");
		GeneralPreferenceActivity.initScreenSettings(screen, getActivity());

		CheckBoxPreference ff = (CheckBoxPreference) findPreference("general_pref_fastforward");
		GeneralPreferenceActivity.initFastForward(ff, getActivity());

		PreferenceCategory keyCat = (PreferenceCategory) findPreference("pref_keyboard_cat");
		Preference inputMethod = keyCat
				.findPreference("pref_game_keyboard_select_input_method");
		GeneralPreferenceActivity.initInputMethodPreference(inputMethod,
				getActivity());

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		GeneralPreferenceActivity act = (GeneralPreferenceActivity) getActivity();
		act.refreshKeyboardProfilePreferences();

		super.onActivityCreated(savedInstanceState);
	}

}
