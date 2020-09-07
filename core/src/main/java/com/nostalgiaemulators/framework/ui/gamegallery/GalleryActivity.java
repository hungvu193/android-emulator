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

package com.nostalgiaemulators.framework.ui.gamegallery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipException;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coderplus.filepicker.FilePickerActivity;
import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.ads.appnext.Ad;
import com.nostalgiaemulators.framework.ads.appnext.AdFetcher;
import com.nostalgiaemulators.framework.ads.appnext.AdListener;
import com.nostalgiaemulators.framework.ads.appnext.AdUtil;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.base.EmulatorUtils;
import com.nostalgiaemulators.framework.base.GameMenu;
import com.nostalgiaemulators.framework.base.GameMenu.GameMenuItem;
import com.nostalgiaemulators.framework.base.GameMenu.OnGameMenuListener;
import com.nostalgiaemulators.framework.base.LiteExportActivity;
import com.nostalgiaemulators.framework.base.MigrationManager;
import com.nostalgiaemulators.framework.base.RestarterActivity;
import com.nostalgiaemulators.framework.base.SlotUtils;
import com.nostalgiaemulators.framework.base.VersionChecker;
import com.nostalgiaemulators.framework.remote.VirtualDPad;
import com.nostalgiaemulators.framework.remote.VirtualDPad.OnVirtualDPEventsListener;
import com.nostalgiaemulators.framework.ui.advertising.AppWallActivity;
import com.nostalgiaemulators.framework.ui.cheats.Cheat;
import com.nostalgiaemulators.framework.ui.gamegallery.GalleryPagerAdapter.OnItemClickListener;
import com.nostalgiaemulators.framework.ui.preferences.GeneralPreferenceActivity;
import com.nostalgiaemulators.framework.ui.preferences.GeneralPreferenceFragment;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.ui.preferences.TouchControllerSettingsActivity;
import com.nostalgiaemulators.framework.ui.remotecontroller.RemoteControllerActivity;
import com.nostalgiaemulators.framework.utils.ActivitySwitcher;
import com.nostalgiaemulators.framework.utils.DatabaseHelper;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.FileUtils;
import com.nostalgiaemulators.framework.utils.FontUtil;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.SDCardUtil;
import com.nostalgiaemulators.framework.utils.Utils;
import com.nostalgiaemulators.framework.utils.Utils.ServerType;
import com.nostalgiaemulators.framework.utils.ZipUtils;
import com.viewpagerindicator.TitlePageIndicator;

public abstract class GalleryActivity extends BaseGameGalleryActivity implements
        OnItemClickListener, OnGameMenuListener, OnVirtualDPEventsListener,
        AdListener {

    private static final String TAG = "GalleryActivity";

    private ProgressBar progress;
    private ImageButton searchBtn;
    private ViewPager pager = null;
    private LinearLayout searchContainer;
    private EditText searchEditText;
    private TextView activityLabel;
    private ImageView backIcon;
    private LinearLayout noGamesLabelContainer;
    private Typeface font;
    private GameMenu gameMenu;
    private GalleryPagerAdapter adapter;

    private boolean visible = false;

    
    private boolean importing = false;
    private boolean failure = false;

    @Override
    public void onFailedToFetchAd() {
        Log.d(TAG, "failed to load ad");
        AdUtil.bindToView(findViewById(R.id.next_ads_view),
                Ad.createFallbackAd(this));
    }

    @Override
    public void onAdFetched(Ad ad) {
        if (activityPaused) {
            return;
        }

        if (Utils.isAppInstalled(this, ad.getPackageName())
                && (!ad.getPackageName().contains("nostalgiaemulators") || ad.getPackageName().endsWith("lite"))) {
            AdUtil.bindToView(findViewById(R.id.next_ads_view),
                    Ad.createFallbackAd(this));
        } else {
            AdUtil.bindToView(findViewById(R.id.next_ads_view), ad);
        }
        handler.postDelayed(adFetching, 60000);

    }

    private static String sAppNextId;

    private String getAppNextId() {
        if (sAppNextId == null) {
            EmulatorApplication app = (EmulatorApplication) getApplication();
            sAppNextId = app.getAdUnitId();
        }
        return sAppNextId;
    }

    private boolean activityPaused = false;
    Runnable adFetching = new Runnable() {
        public void run() {
            if (!activityPaused) {
                AdFetcher.fetchAd(getAppNextId(), GalleryActivity.this, false);

            }
        }
    };

    Handler handler = new Handler();

    public static GameDescription findGameByOldHash(Context context,
                                                    String oldMD5) {
        DatabaseHelper helper = DatabaseHelper.getInstance(context);
        List<GameDescription> games = helper.selectObjsFromDb(
                GameDescription.class, false, "WHERE oldChecksum='" + oldMD5
                        + "'", "GROUP BY oldChecksum", null);
        if (games.size() != 1) {
            return null;
        }
        return games.get(0);
    }

    void fixMD5Bug(GameDescription game) {
        if (!game.checksum.startsWith("V2_")) {
            return;
        }
        String baseDir = EmulatorUtils.getBaseDir(this);
        if (SlotUtils.autoSaveExists(baseDir, game.checksum)) {
            return;
        }

        String oldMD5 = game.oldChecksum;
        if (!SlotUtils.autoSaveExists(baseDir, oldMD5)) {
            return;
        }
        String newMD5 = game.checksum;

        GameDescription g = findGameByOldHash(this, oldMD5);

        if (g == null) {
            return;
        }

        if (!g.checksum.equals(newMD5)) {
            return;
        }

        for (int i = 0; i < 11; i++) {
            String oldSlotPath = SlotUtils.getSlotPath(baseDir, oldMD5, i);
            String newSlotPath = SlotUtils.getSlotPath(baseDir, newMD5, i);
            String oldScreenshotPath = SlotUtils.getScreenshotPath(baseDir,
                    oldMD5, i);
            String newScreenshotPath = SlotUtils.getScreenshotPath(baseDir,
                    newMD5, i);
            boolean oldExists = new File(oldSlotPath).exists();
            boolean newExists = new File(newSlotPath).exists();
            if (oldExists && !newExists) {
                try {
                    FileUtils.copyFile(new File(oldSlotPath), new File(
                            newSlotPath));
                    FileUtils.copyFile(new File(oldScreenshotPath), new File(
                            newScreenshotPath));

                    new File(oldSlotPath).delete();
                    new File(oldScreenshotPath).delete();
                } catch (Exception e) {
                }
            }
        }
        File savFile = new File(baseDir + "/" + oldMD5 + ".sav");
        File fdsFile = new File(baseDir + "/" + oldMD5 + ".fds.sav");
        File newSavFile = new File(baseDir + "/" + newMD5 + ".sav");
        File newFdsFile = new File(baseDir + "/" + newMD5 + ".fds.sav");

        try {
            FileUtils.copyFile(savFile, newSavFile);
        } catch (Exception e) {
        }

        try {
            FileUtils.copyFile(fdsFile, newFdsFile);
        } catch (Exception e) {
        }
        SharedPreferences source = getSharedPreferences(game.oldChecksum
                + Cheat.CHEAT_PREF_SUFFIX, Context.MODE_PRIVATE);
        SharedPreferences target = getSharedPreferences(game.checksum
                + Cheat.CHEAT_PREF_SUFFIX, Context.MODE_PRIVATE);
        PreferenceUtil.copyPreferences(source, target);
        source = getSharedPreferences(game.oldChecksum
                + PreferenceUtil.GAME_PREF_SUFFIX, Context.MODE_PRIVATE);
        target = getSharedPreferences(game.checksum
                + PreferenceUtil.GAME_PREF_SUFFIX, Context.MODE_PRIVATE);
        PreferenceUtil.copyPreferences(source, target);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        failure = false;
        try {
            font = FontUtil.createFontFace(this);
            gameMenu = new GameMenu(this, this);
        } catch (EmulatorException e) {

            failure = true;
            return;
        }

        try {

            setContentView(R.layout.activity_gallery);
        } catch (InflateException e) {
            PackageManager pm = getPackageManager();
            try {
                pm.getPackageInfo("com.google.android.webview",
                        PackageManager.GET_META_DATA);
                showError("Unexpected error, please try again later.");
            } catch (PackageManager.NameNotFoundException ee) {
                showError("It seems that a required system component is being updated, please try again later.");
            }

            failure = true;
            return;
        }

        processPleaseRateDialog();

        boolean isNewVersionAvailableOnStore = VersionChecker
                .isNewVersionAvailable(this);
        if (isNewVersionAvailableOnStore) {
            Log.d(TAG, "NEW VERSION AVAILABLE");
            if (PreferenceUtil.canRemind(this)) {
                showPleaseUpdateDialog();
            }
        } else {
            Log.d(TAG, "App is up-to-date");
        }

        progress = (ProgressBar) findViewById(R.id.game_gallery_progressbar);
        progress.setVisibility(View.GONE);

        adapter = new GalleryPagerAdapter(this, this);
        adapter.onRestoreInstanceState(savedInstanceState);

        pager = (ViewPager) findViewById(R.id.game_gallery_pager);
        pager.setAdapter(adapter);
        if (savedInstanceState != null) {
            pager.setCurrentItem(savedInstanceState.getInt(EXTRA_TABS_IDX, 0));
        } else {
            pager.setCurrentItem(PreferenceUtil.getLastGalleryTab(this));
        }
        Resources res = getResources();

        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.game_gallery_pager_indicator);
        indicator.setViewPager(pager);
        indicator.setTextColor(res.getColor(R.color.main_color));
        indicator.setSelectedColor(res.getColor(R.color.main_color));
        indicator.setTypeface(font);
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 11,
                res.getDisplayMetrics());
        indicator.setTextSize(px);

        ImageButton menuBtn = (ImageButton) findViewById(R.id.game_gallery_menu_btn);
        menuBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openGameMenu();
            }
        });

        searchContainer = (LinearLayout) findViewById(R.id.act_gallery_search_container);
        searchEditText = (EditText) findViewById(R.id.act_gallery_search_editbox);
        searchBtn = (ImageButton) findViewById(R.id.game_gallery_search_btn);
        searchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchBoxVisible(searchEditText.getVisibility() == View.GONE);
            }
        });
        searchBtn.setFocusable(false);

        searchEditText.setTypeface(font);
        searchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, final boolean hasFocus) {
                if (!hasFocus && searchEditText.getText().length() == 0) {
                    setSearchBoxVisible(false);
                }
                searchEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        if (getResources().getConfiguration().keyboard != Configuration.KEYBOARD_QWERTY) {
                            InputMethodManager imm = (InputMethodManager) GalleryActivity.this
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (hasFocus) {
                                imm.showSoftInput(searchEditText,
                                        InputMethodManager.SHOW_IMPLICIT);
                            } else {
                                imm.hideSoftInputFromWindow(
                                        searchEditText.getWindowToken(), 0);

                            }
                        }
                    }
                });
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                setFilter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageView zipIndicator = (ImageView) findViewById(R.id.game_gallery_zip_indicator);
        zipIndicator.setVisibility(View.GONE);

        activityLabel = (TextView) findViewById(R.id.game_gallery_zip_indicator_label);

        activityLabel.setText(getTitle());
        activityLabel.setTypeface(font);

        backIcon = (ImageView) findViewById(R.id.game_gallery_back);
        backIcon.setVisibility(View.GONE);
        backIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        noGamesLabelContainer = (LinearLayout) findViewById(R.id.act_gallery_no_games_container);
        TextView nogamesLabel = (TextView) findViewById(R.id.act_gallery_no_games_label);
        ImageButton nogamesRefresh = (ImageButton) findViewById(R.id.act_gallery_no_games_refresh);

        nogamesLabel.setTypeface(font);
        nogamesRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadGames(true, null);
            }
        });

        exts = getRomExtensions();
        exts.addAll(getArchiveExtensions());
        inZipExts = getRomExtensions();

        importFromLite();

    }


    @Override
    protected void importFromLite() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (!Utils.isAdvertisingVersion(this)) {

            SharedPreferences pref = getSharedPreferences(importPref,
                    MODE_PRIVATE);

            EmulatorApplication app = (EmulatorApplication) getApplication();
            if (app.getPreviousVersionCode() != -1
                    && (app.getCurrentVersionCode() > app
                    .getPreviousVersionCode())) {
                pref.edit().putBoolean("import", true);
            }

            if (!pref.contains("import")) {

                String action = getPackageName().replace("full", "lite") + ".EXPORT";


                if (Utils.isIntentAvailable(GalleryActivity.this, action)) {
                    Intent i = new Intent(action);
                    try {
                        Log.i(TAG, "start import activity");
                        importing = true;
                        startActivityForResult(i, REQUEST_IMPORT);
                    } catch (ActivityNotFoundException e) {
                        Log.w(TAG, "lite version not found");
                        Editor editor = pref.edit();
                        editor.putBoolean("import", true);
                        editor.commit();
                    }
                } else {
                    Editor editor = pref.edit();
                    editor.putBoolean("import", true);
                    editor.commit();
                }
            } else {

            }
        }
    }


    private void processPleaseRateDialog() {
        if (PreferenceUtil.getRateAppTime(this) == -1) {
            return;
        }
        if (PreferenceUtil.getRateAppTime(this) == 0) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 30);
            PreferenceUtil.setRateAppTime(this, c.getTimeInMillis());
            return;
        }

        if (Calendar.getInstance().getTimeInMillis() < PreferenceUtil
                .getRateAppTime(this)) {
            return;
        }
        PreferenceUtil.setRateAppTime(this, -1);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Do you like this app?");
        dialogBuilder
                .setMessage("If you like the emulator, please support us by rating it on Google Play.");
        dialogBuilder.setPositiveButton("Rate the app",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent goToMarket = null;
                        goToMarket = new Intent(Intent.ACTION_VIEW, Uri
                                .parse("market://details?id="
                                        + getApplicationContext()
                                        .getPackageName()));
                        startActivity(goToMarket);
                    }
                });

        dialogBuilder.setNegativeButton("No, thanks",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog = dialogBuilder.show();
    }

    private void showPleaseUpdateDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("This version is getting old");
        dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        dialogBuilder
                .setMessage("It seems you're not using the latest version of the app. Please update.");
        dialogBuilder.setPositiveButton("Update!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent goToMarket = null;
                        goToMarket = new Intent(Intent.ACTION_VIEW, Uri
                                .parse("market://details?id="
                                        + getApplicationContext()
                                        .getPackageName()));
                        startActivity(goToMarket);
                    }
                });

        dialogBuilder.setNegativeButton("Remind me later",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar remindCalendar = Calendar.getInstance();
                        remindCalendar.add(Calendar.DAY_OF_MONTH, 1);
                        PreferenceUtil.setRemindTime(getApplicationContext(),
                                remindCalendar.getTimeInMillis());
                    }
                });
        alertDialog = dialogBuilder.show();
    }

    AlertDialog alertDialog;

    @Override
    protected void onDestroy() {
        DialogUtils.dismiss(alertDialog);
        super.onDestroy();
    }

    private static final int REQUEST_IMPORT = 2;
    private static final int REQUEST_OPEN_DIR = 3;

    private static final String importPref = "import_pref";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMPORT: {

                if (resultCode == RESULT_OK) {

                    String sSource = null;
                    try {
                        SharedPreferences pref = getSharedPreferences(importPref,
                                MODE_PRIVATE);

                        sSource = data.getStringExtra("PATH");

                        Uri uri = data.getData();
                        if (uri != null) {
                            File importDir = new File(getApplicationContext().getFilesDir(), "importTemp");
                            importDir.mkdirs();
                            sSource = importDir.getAbsolutePath();

                            InputStream is = getContentResolver().openInputStream(uri);

                            if (!ZipUtils.unzip(sSource, is)) {
                                throw new RuntimeException();
                            }
                            is.close();
                        }

                        Log.e(TAG, "PATH:" + sSource);


                        MigrationManager.doImport(this, sSource);

                        Editor editor = pref.edit();
                        editor.putBoolean("import", true);
                        editor.commit();

                        Toast.makeText(this, getText(R.string.export_lite_ok),
                                Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, getText(R.string.export_lite_error),
                                Toast.LENGTH_LONG).show();
                    } finally {
                        try {
                            File[] files = new File(sSource).listFiles();
                            for (File file : files) {
                                file.delete();
                            }
                            new File(sSource).delete();
                        } catch (Exception ee) {
                        }
                    }
                } else {
                    Toast.makeText(this, getText(R.string.export_lite_error),
                            Toast.LENGTH_LONG).show();
                }

                importing = false;
                reloadGames(true, null);

                
            }
            break;
            case REQUEST_OPEN_DIR:
                if (resultCode == RESULT_OK) {
                    @SuppressWarnings("unchecked")
                    List<File> dir = (List<File>) data
                            .getSerializableExtra(FilePickerActivity.EXTRA_FILE_PATH);
                    if (dir != null && dir.size() != 0) {
                        reloadGames(true, dir.get(0));
                    }
                }
                break;

        }

    }

    private boolean isSearchBoxVisible = false;

    private void setSearchBoxVisible(boolean visible) {
        isSearchBoxVisible = visible;
        if (visible) {
            searchEditText.setVisibility(View.VISIBLE);
            searchContainer
                    .setBackgroundResource(R.drawable.blue_boundary_shape);
            searchEditText.requestFocus();
            setFilter(searchEditText.getText().toString());
            activityLabel.setVisibility(View.INVISIBLE);
            backIcon.setVisibility(View.GONE);

        } else {
            searchEditText.setVisibility(View.GONE);
            searchContainer.setBackgroundColor(0x00ffffff);
            setFilter("");
            activityLabel.setVisibility(View.VISIBLE);
        }
    }

    private void setFilter(String filter) {
        adapter.setFilter(filter);
    }

    private boolean rotateAnim = false;

    void restartProcess() {
        Intent intent = new Intent(this, RestarterActivity.class);
        intent.putExtra(RestarterActivity.EXTRA_PID, android.os.Process.myPid());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();


		

        activityPaused = false;
        if (failure) {
            return;
        }

        if (PreferenceUtil.lastUseSystemFont(this) != PreferenceUtil
                .useSystemFont(this)) {
            PreferenceUtil.setLastUseSystemFont(this,
                    PreferenceUtil.useSystemFont(this));

            restartProcess();
            return;
        }

        String filter = searchEditText.getText().toString();
        setSearchBoxVisible(filter != null && !"".equals(filter));

        EmulatorApplication ea = (EmulatorApplication) getApplication();

        if (ea.isAdvertisingVersion()) {

            Random r = new Random();
            boolean canFetchInhouse = r.nextInt(10) != 0;
            AdFetcher.fetchAd(getAppNextId(), this, canFetchInhouse);

            if (canFetchInhouse) {
                PreferenceUtil.setLastInhouseAdTime(this,
                        System.currentTimeMillis());
            }
        }
        if (ea.isAdvertisingVersion()
                && PreferenceUtil.isDynamicDPADEnable(this)
                && PreferenceUtil.isDynamicDPADUsed(this)
                && (!Utils.isBeta(this))) {
            PreferenceUtil.setDynamicDPADEnable(this, false);
            Toast.makeText(this, R.string.general_pref_ddpad_deactivate,
                    Toast.LENGTH_LONG).show();
        }

        if (ea.isAdvertisingVersion()
                && PreferenceUtil.isFastForwardEnabled(this)
                && PreferenceUtil.isFastForwardUsed(this)
                && (!Utils.isBeta(this))) {
            PreferenceUtil.setFastForwardEnable(this, false);
            Toast.makeText(this, R.string.general_pref_fastforward_deactivate,
                    Toast.LENGTH_LONG).show();
        }

        if (ea.isAdvertisingVersion()
                && PreferenceUtil.isScreenSettingsSaved(this)
                && PreferenceUtil.isScreenLayoutUsed(this)) {

            PreferenceUtil.setScreenLayoutUsed(this, false);
            PreferenceUtil.removeViewPortSave(this);
            Toast.makeText(this,
                    R.string.general_pref_screen_layout_deactivate,
                    Toast.LENGTH_LONG).show();
        }

        if (rotateAnim) {
            ActivitySwitcher.animationIn(
                    findViewById(R.id.act_gallery_main_container),
                    getWindowManager());
            rotateAnim = false;
        }

        adapter.notifyDataSetChanged();
        VirtualDPad.getInstance().addOnTextChangeListener(this);
        visible = true;

        boolean showNewVersionDialog = true;

        if (reloadGames && !importing) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            boolean isDBEmpty = dbHelper.countObjsInDb(GameDescription.class,
                    null) == 0;
            reloadGames(isDBEmpty, null);
            showNewVersionDialog = !isDBEmpty;
        }

        Log.i(TAG, "show new version dialog " + showNewVersionDialog
                + " reloading " + reloading);

    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
        if (failure) {
            return;
        }
        handler.removeCallbacks(adFetching);
        PreferenceUtil.saveLastGalleryTab(this, pager.getCurrentItem());

        VirtualDPad.getInstance().removeOnTextChangeListener(this);
        visible = false;
    }

    @Override
    public void onGameMenuCreate(GameMenu menu) {
        if (Utils.getDeviceType(this) == ServerType.mobile) {
            menu.add(R.string.gallery_menu_remote_control,
                    R.drawable.ic_gamepad);
        }
        menu.add(R.string.gallery_menu_reload, R.drawable.ic_reset);

        menu.add(R.string.gallery_menu_load_from_dir, R.drawable.ic_folder);

        menu.add(R.string.gallery_menu_pref, R.drawable.ic_game_settings);

        menu.add(R.string.gallery_menu_recommend, R.drawable.ic_recommend);

        menu.add(R.string.gallery_menu_rate_us, R.drawable.ic_star);

        menu.add(R.string.gallery_menu_help, R.drawable.ic_help_mnu);

        menu.add(R.string.about, R.drawable.ic_about);

    }

    @Override
    public void onGameMenuPrepare(GameMenu menu) {
        if (Utils.getDeviceType(this) == ServerType.mobile) {
            GameMenuItem item = menu
                    .getItem(R.string.gallery_menu_remote_control);
            item.setEnable(Utils.isWifiAvailable(this));
        }

    }

    

    @Override
    public void onGameMenuClosed(GameMenu menu) {

    }

    @Override
    public void onGameMenuOpened(GameMenu menu) {

    }

    @SuppressLint("InlinedApi")
    @Override
    public void onGameMenuItemSelected(GameMenu menu, GameMenuItem item) {
        if (item.getId() == R.string.gallery_menu_pref) {
            Intent i = new Intent(this, GeneralPreferenceActivity.class);
            if (Build.VERSION.SDK_INT >= 11) {
                i.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                        GeneralPreferenceFragment.class.getName());
                i.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            }
            startActivity(i);

        } else if (item.getId() == R.string.gallery_menu_remote_control) {
            final Intent intent = new Intent(GalleryActivity.this,
                    RemoteControllerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            ActivitySwitcher.animationOut(
                    findViewById(R.id.act_gallery_main_container),
                    getWindowManager(),
                    new ActivitySwitcher.AnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            startActivity(intent);
                            rotateAnim = true;
                        }
                    });
        } else if (item.getId() == R.string.gallery_menu_reload) {
            reloadGames(true, null);
        } else if (item.getId() == R.string.gallery_menu_load_from_dir) {
            Intent intent = new Intent(GalleryActivity.this,
                    FilePickerActivity.class);

            SDCardUtil.prepareFilePickerIntent(intent);

            intent.putExtra(FilePickerActivity.EXTRA_SELECT_DIRECTORIES_ONLY,
                    true);

            intent.putExtra(FilePickerActivity.EXTRA_TOAST_TEXT,
                    getString(R.string.gallery_file_choosing_long_click));

            startActivityForResult(intent, REQUEST_OPEN_DIR);

        } else if (item.getId() == R.string.gallery_menu_wifi_on) {
            PreferenceUtil.setWifiServerEnable(this, false);
            Toast.makeText(this, R.string.gallery_stop_wifi_control_server,
                    Toast.LENGTH_LONG).show();
            stopWifiListening();
        } else if (item.getId() == R.string.gallery_menu_wifi_off) {
            PreferenceUtil.setWifiServerEnable(this, true);
            Toast.makeText(
                    this,
                    String.format(
                            getString(R.string.gallery_start_wifi_control_server),
                            Utils.getIpAddr(this)), Toast.LENGTH_LONG).show();
            startWifiListening();
        } else if (item.getId() == R.string.about) {

            runOnUiThread(new Runnable() {
                public void run() {
                    DialogUtils.show(getAboutDialog(), true);
                }

                ;
            });

        } else if (item.getId() == R.string.game_menu_cheats) {
            Intent i = new Intent(this, TouchControllerSettingsActivity.class);
            startActivity(i);
        } else if (item.getId() == R.string.gallery_menu_rate_us) {
            Intent goToMarket = null;
            goToMarket = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + getApplicationContext().getPackageName()));
            try {
                startActivity(goToMarket);
            } catch (Exception e) {
            }
        } else if (item.getId() == R.string.gallery_menu_recommend) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey, check out this cool emulator! I think you might like it!\n"
                            + ((EmulatorApplication) getApplication())
                            .getStoreUrl());
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, EmulatorInfoHolder
                    .getInfo().getName());

            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (item.getId() == R.string.gallery_menu_help) {
            showHelpDialog();
        }

    }

    @Override
    public void onItemClick(GameDescription game, boolean clickOnAds) {
        if (clickOnAds) {
            Intent intent = new Intent(GalleryActivity.this,
                    AppWallActivity.class);
            startActivity(intent);
        } else {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            File gameFile = new File(game.path);
            Log.i(TAG, "select " + game);

            if (game.isInArchive()) {
                gameFile = new File(getExternalCacheDir(), game.checksum);
                game.path = gameFile.getAbsolutePath();
                ZipRomFile zipRomFile = dbHelper
                        .selectObjFromDb(ZipRomFile.class, "WHERE _id="
                                + game.zipfile_id, false);
                File zipFile = new File(zipRomFile.path);
                if (!gameFile.exists()) {
                    try {
                        Utils.extractFile(zipFile, game.name, gameFile);
                    } catch (ZipException e) {
                        Log.e(TAG, "", e);
                    } catch (IOException e) {
                        Log.e(TAG, "", e);
                    }
                }
            }

            if (gameFile.exists()) {
                game.lastGameTime = System.currentTimeMillis();
                game.runCount++;

                dbHelper.updateObjToDb(game, new String[]{"lastGameTime",
                        "runCount"});

                fixMD5Bug(game);
                onGameSelected(game, 0);
            } else {
                Log.w(TAG, "rom file:" + gameFile.getAbsolutePath()
                        + " does not exist");
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        GalleryActivity.this);
                AlertDialog dialog = builder
                        .setMessage(getString(R.string.gallery_rom_not_found))
                        .setTitle(R.string.error)
                        .setPositiveButton(
                                R.string.gallery_rom_not_found_reload,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        reloadGames(true, null);
                                    }
                                }).create();
                dialog.setCancelable(false);
                dialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        reloadGames(true, null);
                    }
                });
                dialog.show();
            }

        }
    }

    public boolean onGameSelected(GameDescription game, int slot) {
        Intent intent = new Intent(this, getEmulatorActivityClass());
        intent.putExtra(EmulatorActivity.EXTRA_GAME, game);
        intent.putExtra(EmulatorActivity.EXTRA_SLOT, slot);
        intent.putExtra(EmulatorActivity.EXTRA_FROM_GALLERY, true);
        startActivity(intent);
        return true;
    }

    @Override
    public void setLastGames(ArrayList<GameDescription> games) {
        adapter.setGames(games);
        pager.setVisibility(games.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        noGamesLabelContainer.setVisibility(games.isEmpty() ? View.VISIBLE
                : View.INVISIBLE);
    }

    @Override
    public void setNewGames(ArrayList<GameDescription> games) {
        boolean isListEmpty = adapter.addGames(games) == 0;

        pager.setVisibility(isListEmpty ? View.INVISIBLE : View.VISIBLE);
        noGamesLabelContainer.setVisibility(isListEmpty ? View.VISIBLE
                : View.INVISIBLE);
    }

    ProgressDialog searchDialog = null;

    @SuppressLint("NewApi")
    private void showSearchProgressDialog(final boolean zipMode) {
        final Activity act = this;

        runOnUiThread(new Runnable() {
            public void run() {
                searchDialog = new ProgressDialog(act);
                searchDialog
                        .setMessage(getString(zipMode ? R.string.gallery_zip_search_label
                                : R.string.gallery_sdcard_search_label));
                searchDialog.setMax(100);
                searchDialog.setCancelable(false);
                searchDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                searchDialog.setIndeterminate(true);
                searchDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                stopRomsFinding();
                            }
                        });

                DialogUtils.show(searchDialog, false);
                if (Build.VERSION.SDK_INT > 13) {
                    searchDialog.setProgressNumberFormat("");
                    searchDialog.setProgressPercentFormat(null);
                }
            }
        });
    }

    public void onSearchingEnd(final int count, final boolean showToast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
                if (searchDialog != null) {
                    try {
                        searchDialog.dismiss();
                    } catch (Exception e) {
                    }
                    searchDialog = null;

                }
                if (showToast) {
                    if (count > 0) {
                        Toast.makeText(
                                GalleryActivity.this,
                                String.format(
                                        getText(
                                                R.string.gallery_count_of_found_games)
                                                .toString(), count),
                                Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

    }
    @Override
    public void onRomsFinderStart(final boolean searchNew) {
        runOnUiThread(new Runnable() {
            public void run() {
                progress.setVisibility(View.VISIBLE);
                if (searchNew) {
                    showSearchProgressDialog(false);
                }
            }
        });
    }

    @Override
    public void onRomsFinderZipPartStart(final int countEntries) {
        if (searchDialog != null) {
            runOnUiThread(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    if (searchDialog != null) {
                        if (Build.VERSION.SDK_INT > 10) {
                            searchDialog.setProgressNumberFormat("%1d/%2d");
                            searchDialog.setProgressPercentFormat(NumberFormat
                                    .getPercentInstance());
                        }
                        searchDialog
                                .setMessage(getString(R.string.gallery_start_sip_search_label));
                        searchDialog.setIndeterminate(false);
                        searchDialog.setMax(countEntries);
                    }
                }
            });
        }
    }

    @Override
    public void onRomsFinderCancel(boolean searchNew) {
        super.onRomsFinderCancel(searchNew);
        onSearchingEnd(0, searchNew);

    }

    @Override
    public void onRomsFinderEnd(boolean searchNew) {
        super.onRomsFinderEnd(searchNew);
        onSearchingEnd(0, searchNew);

    }

    @Override
    public void onRomsFinderNewGames(ArrayList<GameDescription> roms) {
        super.onRomsFinderNewGames(roms);
        onSearchingEnd(roms.size(), true);
    }

    @Override
    public void onRomsFinderFoundZipEntry(final String message,
                                          final int skipEntries) {
        if (searchDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (searchDialog != null) {
                        searchDialog.setMessage(message);
                        searchDialog.setProgress(searchDialog.getProgress() + 1
                                + skipEntries);
                    }
                }
            });
        }
    }

    @Override
    public void onRomsFinderFoundFile(final String name) {
        if (searchDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (searchDialog != null) {
                        searchDialog.setMessage(name);
                    }
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openGameMenu();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            setSearchBoxVisible(!isSearchBoxVisible);
        } else if (searchEditText != null && event != null && !searchEditText.hasFocus() && event.isPrintingKey()) {
            searchEditText.setText("" + ((char) event.getUnicodeChar()));
            searchEditText.setSelection(1);
            searchEditText.requestFocus();
            setSearchBoxVisible(true);

        }
        return super.onKeyDown(keyCode, event);
    }

    public void openGameMenu() {
        gameMenu.open(false);
    }

    @Override
    public void onVirtualDPadTextEvent(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isSearchBoxVisible) {
                    setSearchBoxVisible(true);
                }
                searchEditText.setText(text);
                searchEditText.setSelection(text.length());

            }
        });
    }

    public static final int COMMAND_SEARCHMODE = 1;

    @Override
    public void onVirtualDPadCommandEvent(int command, int param0, int param1) {
        switch (command) {
            case COMMAND_SEARCHMODE:
                final boolean show = param0 == 1;
                runOnUiThread(new Runnable() {
                    public void run() {
                        setSearchBoxVisible(show);
                    }

                    ;
                });
                break;

            default:
                break;
        }
    }

    public static final String EXTRA_TABS_IDX = "EXTRA_TABS_IDX";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pager != null && adapter != null) {
            outState.putInt(EXTRA_TABS_IDX, pager.getCurrentItem());
            adapter.onSaveInstanceState(outState);
        }
    }

}
