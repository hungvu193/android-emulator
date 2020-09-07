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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.coderplus.filepicker.FilePickerActivity;
import com.hlidskialf.android.preference.SeekBarPreference;
import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.KeyboardProfile;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.base.EmulatorUtils;
import com.nostalgiaemulators.framework.base.RestarterActivity;
import com.nostalgiaemulators.framework.remote.VirtualDPad;
import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoTransmitter;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.SDCardUtil;
import com.nostalgiaemulators.framework.utils.Utils;

public class GeneralPreferenceActivity extends AppCompatActivity {

    private String NEW_PROFILE = null;
    private String[] keyboardProfileNames = null;
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

        NEW_PROFILE = getText(R.string.key_profile_new).toString();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new GeneralPreferenceFragment()).commit();

    }

    void openDirPicker() {
        Intent intent = new Intent(GeneralPreferenceActivity.this,
                FilePickerActivity.class);

        SDCardUtil.prepareFilePickerIntent(intent);

        intent.putExtra(FilePickerActivity.EXTRA_SELECT_DIRECTORIES_ONLY, true);

        intent.putExtra(FilePickerActivity.EXTRA_TOAST_TEXT,
                getString(R.string.gallery_file_choosing_long_click));

        startActivityForResult(intent, 1);
    }

    void initWorkingDir(Preference pref, Preference resetPref,
                        Preference copyContentPref) {
        workingDirPreference = pref;
        workingDirResetPreference = resetPref;

        final String defaultS = getText(R.string.advanced_users_only)
                .toString();
        String dir = PreferenceUtil.getWorkingDirParent(this);
        if (dir == null) {
            dir = defaultS;
            workingDirResetPreference.setEnabled(false);
        } else {
            workingDirResetPreference.setEnabled(true);
        }

        workingDirPreference.setSummary(dir);
        resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean success = EmulatorUtils.tryChangeWorkingDir(
                        GeneralPreferenceActivity.this, null);
                if (success) {
                    workingDirPreference.setSummary(defaultS);
                    workingDirResetPreference.setEnabled(false);
                    restartProcess();
                }
                return false;
            }
        });

        pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                openDirPicker();
                return true;
            }
        });

        initProPreference(workingDirPreference, this);
        initProPreference(workingDirResetPreference, this);
        initProPreference(copyContentPref, this);
    }

    static void initSavFilePreferences(PreferenceScreen screen, PreferenceCategory cat, final Activity activity) {
        if (activity.getApplicationContext().getPackageName().contains("gba")) {
            screen.removePreference(cat);
        }
    }


    static void initVolume(SeekBarPreference volume, final Activity activity) {
        int progress = (int) (PreferenceUtil.getSoundVolume(activity) * 100);
        volume.setSummary(progress + "%");

        volume.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                if (preference == null || newValue == null) {
                    return false;
                }
                preference.setSummary(newValue + "%");
                return true;
            }
        });

        initProPreference(volume, activity);
    }

    static void initProPreference(Preference pref, final Activity activity) {
        boolean isBeta = Utils.isBeta(activity);
        if (((EmulatorApplication) activity.getApplication())
                .isAdvertisingVersion() && !isBeta) {
            CharSequence x = pref.getSummary();
            pref.setSummary((x == null ? "" : x + " ") + "(PRO version only)");
            pref.setEnabled(false);
        }
    }
    void refreshKeyboardProfilePreferences() {
        if (selectProfileListPreference != null) {
            initKeyboardProfiles();
            initProfiles(selectProfileListPreference, editProfilePreference);
            setNewProfile(selectProfileListPreference, editProfilePreference,
                    selectProfileListPreference.getValue());
        }

    }

    static void initDDPAD(CheckBoxPreference ddpad, final Activity activity) {

        boolean isBeta = Utils.isBeta(activity);

        if (!((EmulatorApplication) activity.getApplication())
                .isAdvertisingVersion() || isBeta) {

        } else {
            ddpad.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    if ((Boolean) newValue) {
                        PreferenceUtil.setDynamicDPADUsed(activity, false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                activity);
                        AlertDialog dialog = builder
                                .setMessage(
                                        R.string.general_pref_ddpad_activate)
                                .setPositiveButton(R.string.ok, null)
                                .setNeutralButton(
                                        R.string.general_pref_ddpad_goto_play,
                                        new OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Intent goToMarket = null;
                                                goToMarket = new Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("market://details?id="
                                                                + ((EmulatorApplication) activity
                                                                .getApplication())
                                                                .getFullVersionPackage()));
                                                activity.startActivity(goToMarket);
                                            }
                                        }).create();

                        DialogUtils.show(dialog, true);
                    }
                    return true;
                }
            });
        }

    }

    static void initScreenSettings(Preference screenSettings,
                                   final Activity activity) {

        boolean isBeta = Utils.isBeta(activity);

        if (!((EmulatorApplication) activity.getApplication())
                .isAdvertisingVersion() || isBeta) {

        } else {
            screenSettings
                    .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    activity);
                            AlertDialog dialog = builder
                                    .setMessage(
                                            R.string.general_pref_ddpad_activate)
                                    .setPositiveButton(R.string.ok,
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    Intent i = new Intent(
                                                            activity, GeneralPreferenceActivity.class);
                                                    activity.startActivity(i);
                                                }
                                            })
                                    .setNeutralButton(
                                            R.string.general_pref_ddpad_goto_play,
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    Intent goToMarket = null;
                                                    goToMarket = new Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("market://details?id="
                                                                    + ((EmulatorApplication) activity
                                                                    .getApplication())
                                                                    .getFullVersionPackage()));
                                                    activity.startActivity(goToMarket);
                                                }
                                            }).create();

                            DialogUtils.show(dialog, true);
                            return true;
                        }
                    });
        }

    }

    static void initInputMethodPreference(Preference imPreference,
                                          final Activity activity) {
        imPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        InputMethodManager imeManager = (InputMethodManager) activity
                                .getApplicationContext().getSystemService(
                                        INPUT_METHOD_SERVICE);
                        if (imeManager != null) {
                            imeManager.showInputMethodPicker();
                        } else {
                            Toast.makeText(
                                    activity,
                                    R.string.pref_keyboard_cannot_change_input_method,
                                    Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
    }

    static void initFastForward(CheckBoxPreference ff, final Activity activity) {

        boolean isBeta = Utils.isBeta(activity);

        if (!((EmulatorApplication) activity.getApplication())
                .isAdvertisingVersion() || isBeta) {

        } else {
            ff.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {
                    if ((Boolean) newValue) {
                        PreferenceUtil.setFastForwardUsed(activity, false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                activity);
                        AlertDialog dialog = builder
                                .setMessage(
                                        R.string.general_pref_ddpad_activate)
                                .setPositiveButton(R.string.ok, null)
                                .setNeutralButton(
                                        R.string.general_pref_ddpad_goto_play,
                                        new OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Intent goToMarket = null;
                                                goToMarket = new Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("market://details?id="
                                                                + ((EmulatorApplication) activity
                                                                .getApplication())
                                                                .getFullVersionPackage()));
                                                activity.startActivity(goToMarket);
                                            }
                                        }).create();

                        DialogUtils.show(dialog, true);
                    }
                    return true;
                }
            });
        }

    }

    static void initQuality(PreferenceCategory cat, Preference pref) {
        if (EmulatorInfoHolder.getInfo().getNumQualityLevels() == 0) {
            cat.removePreference(pref);
        }

    }



    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WifiServerInfoTransmitter.onResume(this, "");

        VirtualDPad.getInstance().onResume(getWindow());
        initKeyboardProfiles();

        boolean found = false;
        for (int i = 0; i < keyboardProfileNames.length; i++) {
            if (keyboardProfileNames[i].equals(selectProfileListPreference
                    .getValue())) {
                found = true;
                break;
            }
        }
        if (!found) {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(this);
            Editor edit = pref.edit();
            edit.putString("pref_game_keyboard_profile",
                    KeyboardProfile.DEFAULT);
            edit.commit();

            setNewProfile(selectProfileListPreference, editProfilePreference,
                    KeyboardProfile.DEFAULT);
            selectProfileListPreference.setValue(KeyboardProfile.DEFAULT);
            selectProfileListPreference.setEntries(keyboardProfileNames);
            selectProfileListPreference.setEntryValues(keyboardProfileNames);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        WifiServerInfoTransmitter.onPause();
        VirtualDPad.getInstance().onPause();
    }

    private void initKeyboardProfiles() {
        ArrayList<String> names = KeyboardProfile
                .getProfilesNames(getApplicationContext());
        keyboardProfileNames = new String[names.size() + 1];
        int i = 0;
        for (String name : names) {
            keyboardProfileNames[i++] = name;
        }
        keyboardProfileNames[names.size()] = NEW_PROFILE;

    }

    public void setNewProfile(ListPreference listProfile,
                              Preference editProfile, String name) {
        listProfile.setSummary(name);
        editProfile.setSummary(name);
        editProfile.setTitle(R.string.key_profile_edit);
        editProfile.getIntent().putExtra(
                KeyboardSettingsActivity.EXTRA_PROFILE_NAME, name);

    }

    void initProfiles(final ListPreference selectProfile,
                      final Preference editProfile) {

        if (keyboardProfileNames == null) {
            initKeyboardProfiles();
        }

        this.selectProfileListPreference = selectProfile;
        this.editProfilePreference = editProfile;

        selectProfile.setEntries(keyboardProfileNames);
        selectProfile.setEntryValues(keyboardProfileNames);

        selectProfile.setDefaultValue(KeyboardProfile.DEFAULT);
        if (selectProfile.getValue() == null) {
            selectProfile.setValue(KeyboardProfile.DEFAULT);
        }

        selectProfile
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                                                      Object newValue) {
                        Activity activity = GeneralPreferenceActivity.this;
                        if (newValue.equals(NEW_PROFILE)) {
                            Intent i = new Intent(activity,
                                    KeyboardSettingsActivity.class);
                            i.putExtra(
                                    KeyboardSettingsActivity.EXTRA_PROFILE_NAME,
                                    KeyboardProfile.DEFAULT);
                            i.putExtra(KeyboardSettingsActivity.EXTRA_NEW_BOOL,
                                    true);
                            activity.startActivityForResult(i, 0);
                            return false;
                        } else {
                            setNewProfile(selectProfile, editProfile,
                                    (String) newValue);
                            return true;
                        }
                    }
                });
    }

    ListPreference selectProfileListPreference;
    Preference editProfilePreference;
    Preference workingDirPreference;
    Preference workingDirResetPreference;

    void restartProcess() {
        Intent intent = new Intent(this, RestarterActivity.class);
        intent.putExtra(RestarterActivity.EXTRA_PID, android.os.Process.myPid());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == KeyboardSettingsActivity.RESULT_OK) {
                ArrayList<String> profileNames = KeyboardProfile
                        .getProfilesNames(this);
                keyboardProfileNames = new String[profileNames.size() + 1];
                int i = 0;
                for (String name : profileNames) {
                    keyboardProfileNames[i++] = name;
                }
                keyboardProfileNames[profileNames.size()] = NEW_PROFILE;
                String name = null;
                name = data
                        .getStringExtra(KeyboardSettingsActivity.EXTRA_PROFILE_NAME);

                setNewProfile(selectProfileListPreference,
                        editProfilePreference, name);
                initProfiles(selectProfileListPreference, editProfilePreference);
                selectProfileListPreference.setValue(name);
            }
        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                List<File> dir = (List<File>) data
                        .getSerializableExtra(FilePickerActivity.EXTRA_FILE_PATH);
                if (dir != null && dir.size() != 0) {
                    try {
                        File workingDir = dir.get(0);
                        boolean success = EmulatorUtils.tryChangeWorkingDir(
                                this, workingDir);
                        if (success) {
                            workingDirPreference.setSummary(workingDir
                                    .toString());
                            workingDirResetPreference.setEnabled(true);
                            restartProcess();

                        }
                    } catch (Exception e) {

                    }
                }

            }
        }
    }

}
