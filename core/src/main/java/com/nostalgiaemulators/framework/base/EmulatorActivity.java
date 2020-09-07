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

package com.nostalgiaemulators.framework.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.Benchmark.BenchmarkCallback;
import com.nostalgiaemulators.framework.base.GameMenu.GameMenuItem;
import com.nostalgiaemulators.framework.base.GameMenu.OnGameMenuListener;
import com.nostalgiaemulators.framework.controllers.DynamicDPad;
import com.nostalgiaemulators.framework.controllers.KeyboardController;
import com.nostalgiaemulators.framework.controllers.QuickSaveController;
import com.nostalgiaemulators.framework.controllers.RemoteController;
import com.nostalgiaemulators.framework.controllers.RemoteController.OnRemoteControllerWarningListener;
import com.nostalgiaemulators.framework.controllers.TouchController;
import com.nostalgiaemulators.framework.controllers.ZapperGun;
import com.nostalgiaemulators.framework.remote.VirtualDPad;
import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoTransmitter;
import com.nostalgiaemulators.framework.ui.cheats.CheatsActivity;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.gamegallery.SlotSelectionActivity;
import com.nostalgiaemulators.framework.ui.preferences.GamePreferenceActivity;
import com.nostalgiaemulators.framework.ui.preferences.GamePreferenceFragment;
import com.nostalgiaemulators.framework.ui.preferences.GeneralPreferenceActivity;
import com.nostalgiaemulators.framework.ui.preferences.GeneralPreferenceFragment;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.ui.timetravel.TimeTravelDialog;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.FreeAppUtil;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;
@SuppressWarnings("deprecation")
public abstract class EmulatorActivity extends ImmersiveActivity implements
        OnGameMenuListener {
    private static final String TAG = "com.nostalgiaemulators.framework.base.EmulatorActivity";

    public abstract Emulator getEmulatorInstance();

    private GameMenu gameMenu = null;

    protected GameDescription game = null;

    public int[] getTextureBounds(Emulator emulator) {
        return null;
    }

    public int getTextureType() {
        return GLES20.GL_RGBA;
    }

    public Manager getManager() {
        return manager;
    }

    private DynamicDPad dynamic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.getBoolean(EXTRA_FROM_GALLERY)) {
            setShouldPauseOnResume(false);
            getIntent().removeExtra(EXTRA_FROM_GALLERY);
        }
        canRestart = true;

        try {
            baseDir = EmulatorUtils.getBaseDir(this);
        } catch (EmulatorException e) {
            handleException(e);
            exceptionOccurred = true;
            return;
        }

        Log.d(TAG, "onCreate - baseActivity");
        boolean hasOpenGL20 = com.nostalgiaemulators.framework.utils.Utils
                .checkGL20Support(getApplicationContext());

        game = (GameDescription) getIntent().getSerializableExtra(EXTRA_GAME);

        if (game == null) {
            showErrorAlert("failed to load game");
            exceptionOccurred = true;
            return;
        }
        try {
            gameMenu = new GameMenu(this, this);
        } catch (EmulatorException e) {
            handleException(e);
            exceptionOccurred = true;
            return;
        }
        slotToRun = -1;

        WindowManager.LayoutParams wParams = getWindow().getAttributes();
        wParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        wParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(wParams);

        Emulator emulator = getEmulatorInstance();

        int defaultTopPadding = 0;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            defaultTopPadding = getResources().getDimensionPixelSize(
                    R.dimen.top_panel_touchcontroler_height);
        }

        OpenGLView openGLView = null;
        int quality = PreferenceUtil.getEmulationQuality(this);
        boolean alreadyBenchmarked = PreferenceUtil.isBenchmarked(this);

        boolean needsBenchmark = quality != 2 && !alreadyBenchmarked;

        if (!alreadyBenchmarked) {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                int w = 0;
                int h = 0;
                if (android.os.Build.VERSION.SDK_INT >= 13) {
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    w = size.x;
                    h = size.y;
                }
                if (Math.min(w, h) > 600) {
                    quality = 2;
                    needsBenchmark = false;
                    PreferenceUtil.setEmulationQuality(this, quality);
                    PreferenceUtil.setBenchmarked(this, true);
                }
            }
        }

        if (hasOpenGL20) {
            openGLView = new OpenGLView(this, emulator, defaultTopPadding);
            if (needsBenchmark) {
                openGLView.setBenchmark(new Benchmark(OPEN_GL_BENCHMARK, 200,
                        benchmarkCallback));
            }
        }

        emulatorView = openGLView != null ? openGLView : new UnacceleratedView(
                this, emulator, defaultTopPadding);

        controllers = new ArrayList<EmulatorController>();

        touchController = new TouchController(this);
        controllers.add(touchController);
        touchController.connectToEmulator(0, emulator);

        dynamic = new DynamicDPad(this, getWindowManager().getDefaultDisplay(),
                touchController);
        controllers.add(dynamic);
        dynamic.connectToEmulator(0, emulator);

        QuickSaveController qsc = new QuickSaveController(this, touchController);
        controllers.add(qsc);

        KeyboardController kc = new KeyboardController(emulator,
                getApplicationContext(), game.checksum, this);

        RemoteController rc1 = new RemoteController(this);
        RemoteController rc2 = new RemoteController(this);
        RemoteController rc3 = new RemoteController(this);
        RemoteController rc4 = new RemoteController(this);

        rc1.connectToEmulator(0, emulator);
        rc2.connectToEmulator(1, emulator);
        rc3.connectToEmulator(2, emulator);
        rc4.connectToEmulator(3, emulator);

        rc1.setOnRemoteControllerWarningListener(new OnRemoteControllerWarningListener() {
            @Override
            public void onZapperCollision() {
                showZapperCollisionDialog();
            }
        });
        ZapperGun zapper = new ZapperGun(getApplicationContext(), this);
        zapper.connectToEmulator(1, emulator);
        controllers.add(zapper);

        controllers.add(rc1);
        controllers.add(rc2);
        controllers.add(rc3);
        controllers.add(rc4);
        controllers.add(kc);

        group = new FrameLayout(this);

        ViewGroup.LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        group.setLayoutParams(params);
        group.addView(emulatorView.asView());
        controllerViews = new ArrayList<View>();

        for (EmulatorController controller : controllers) {
            View controllerView = controller.getView();
            if (controllerView != null) {
                controllerViews.add(controllerView);
                group.addView(controllerView);
            }
        }
        group.addView(new View(getApplicationContext()) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return true;
            }
        });

        setContentView(group);

        try {
            manager = new Manager(emulator, getApplicationContext());
            if (needsBenchmark) {
                manager.setBenchmark(new Benchmark(EMULATION_BENCHMARK, 1000,
                        benchmarkCallback));
            }

        } catch (EmulatorException e) {
            exceptionOccurred = true;
            handleException(e);
        }
    }

    private BenchmarkCallback benchmarkCallback = new BenchmarkCallback() {

        @Override
        public void onBenchmarkReset(Benchmark benchmark) {
        }

        @Override
        public void onBenchmarkEnded(Benchmark benchmark, int steps,
                                     long totalTime) {
            float millisPerFrame = totalTime / (float) steps;
            numTests++;
            if (benchmark.getName().equals(OPEN_GL_BENCHMARK)) {
                if (millisPerFrame < 17) {
                    numOk++;
                }
            }
            if (benchmark.getName().equals(EMULATION_BENCHMARK)) {
                if (millisPerFrame < 17) {
                    numOk++;
                }
            }
            if (numTests == 2) {
                PreferenceUtil.setBenchmarked(EmulatorActivity.this, true);
                if (numOk == 2) {
                    emulatorView.setQuality(2);
                    PreferenceUtil
                            .setEmulationQuality(EmulatorActivity.this, 2);
                } else {
                }
            }
        }

        private int numTests = 0;
        private int numOk = 0;
    };

    private static final String OPEN_GL_BENCHMARK = "openGL";
    private static final String EMULATION_BENCHMARK = "emulation";
    private TouchController touchController = null;

    private boolean autoHide;

    public void hideTouchController() {
        if (autoHide) {
            if (touchController != null) {
                touchController.hide();
            }
        }
    }

    public ViewPort getViewPort() {
        return emulatorView.getViewPort();
    }

    private static int oldConfig;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exceptionOccurred) {
            return;
        }
        emulatorView = null;

        oldConfig = getChangingConfigurations();

        group.removeAllViews();
        group = null;
        controllerViews.clear();

        try {
            manager.destroy();
        } catch (EmulatorException e) {
        }
        for (EmulatorController controller : controllers) {
            controller.onDestroy();
        }
        controllers.clear();
    }

    public static PackageManager pm;
    public static String pn;
    public static String sd;

    private static final int REQUEST_SAVE = 1;
    private static final int REQUEST_LOAD = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setShouldPauseOnResume(false);

        if (resultCode == RESULT_OK) {
            canRestart = false;
            int slotIdx = data
                    .getIntExtra(SlotSelectionActivity.EXTRA_SLOT, -1);
            switch (requestCode) {
                case REQUEST_SAVE:
                    slotToSave = slotIdx;
                    slotToRun = 0;
                    break;
                case REQUEST_LOAD:
                    slotToRun = slotIdx;
                    slotToSave = null;
                    break;
            }
        }

    }

    private void showZapperCollisionDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        EmulatorActivity.this);
                AlertDialog dialog = builder
                        .setMessage(getString(R.string.game_zapper_collision))
                        .setTitle(R.string.warning).create();
                dialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        manager.resumeEmulation();
                        VirtualDPad.getInstance().detachFromWindow();
                    }
                });
                manager.pauseEmulation();
                DialogUtils.show(dialog, true);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean res = super.dispatchTouchEvent(ev);
        if (touchController != null) {
            touchController.show();
        }
        if (controllerViews != null) {
            for (View controllerView : controllerViews) {
                controllerView.dispatchTouchEvent(ev);
            }
        }
        return res;
    }

    SparseArray<Pair<Long, Integer>> keyEventHistory = new SparseArray<Pair<Long, Integer>>();
    SparseArray<Pair<Long, Integer>> keyEventWAMap = new SparseArray<Pair<Long, Integer>>();

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent ev) {
        boolean res = super.dispatchKeyEvent(ev);
        if (controllerViews != null) {
            for (View controllerView : controllerViews) {
                controllerView.dispatchKeyEvent(ev);
            }
        }
        return res;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        boolean res = super.dispatchGenericMotionEvent(event);
        if (controllerViews != null) {
            for (View controllerView : controllerViews) {
                controllerView.dispatchGenericMotionEvent(event);
            }
        }
        return res;
    }

    public void setShouldPauseOnResume(boolean b) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();
        editor.putBoolean("emulator_activity_pause", b);
        editor.commit();
    }

    public boolean shouldPause() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                "emulator_activity_pause", false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        WifiServerInfoTransmitter.onPause();

        if (isRestarting) {
            finish();
            return;
        }
        if (exceptionOccurred) {
            return;
        }

        pm = null;
        if (gameMenu != null && gameMenu.isOpen()) {
            try {
                gameMenu.dismiss();
            } catch (Exception e) {
            }
        }

        DialogUtils.dismiss(dialog);

        for (EmulatorController controller : controllers) {
            controller.onPause();
            controller.onGamePaused(game);
        }
        try {
            manager.stopGame();
        } catch (EmulatorException e) {
            handleException(e);
        } finally {
            emulatorView.onPause();
        }

    }

    private boolean isFF = false;
    private boolean isToggleFF = false;
    private boolean isFFPressed = false;

    public void onFastForwardDown() {

        if (isToggleFF) {
            if (!isFFPressed) {
                isFFPressed = true;
                isFF = !isFF;
                manager.setFastForwardEnabled(isFF);
            }
        } else {
            manager.setFastForwardEnabled(true);
        }
    }

    public void onFastForwardUp() {
        if (!isToggleFF) {
            manager.setFastForwardEnabled(false);
        }
        isFFPressed = false;
    }

    private boolean exceptionOccurred;

    protected void handleException(EmulatorException e) {
        showErrorAlert(e.getMessage(this));
    }

    protected void showErrorAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.setMessage(msg).create();
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        finish();
                    }
                });
            }
        });
        runOnUiThread(new Runnable() {
            public void run() {
                DialogUtils.show(dialog, true);
            }
        });
    }

    boolean isRestarting;

    private void restartProcess(Class<?> activityToStartClass) {
        isRestarting = true;
        PreferenceUtil.setLastRestartTime(this);
        Intent intent = new Intent(this, RestarterActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra(RestarterActivity.EXTRA_PID, android.os.Process.myPid());
        String className = activityToStartClass.getName();
        intent.putExtra(RestarterActivity.EXTRA_CLASS, className);
        startActivity(intent);
    }

    private final int maxPRC = 10;

    private int decreaseResumesToRestart() {
        int prc = PreferenceManager.getDefaultSharedPreferences(this).getInt(
                "PRC", maxPRC);

        if (prc > 0) {
        }

        Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();
        editor.putInt("PRC", prc);
        editor.commit();
        return prc;
    }

    private void resetProcessResetCounter() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();
        editor.putInt("PRC", maxPRC);
        editor.commit();
    }

    boolean canRestart;

    public void quickSave() {
        manager.saveState(10);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EmulatorActivity.this, "state saved",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EmulatorActivity.this, toast, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    public void quickLoad() {
        manager.loadState(10);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onResume() {
        super.onResume();
        isRestarting = false;
        Bundle extras = getIntent().getExtras();
        boolean isAfterProcessRestart = extras != null
                && extras.getBoolean(RestarterActivity.EXTRA_AFTER_RESTART);
        getIntent().removeExtra(RestarterActivity.EXTRA_AFTER_RESTART);

        boolean shouldRestart = decreaseResumesToRestart() == 0;
        if (!isAfterProcessRestart && shouldRestart && canRestart
                && PreferenceUtil.canRestart(this)) {
            resetProcessResetCounter();
            restartProcess(this.getClass());
            return;
        }
        canRestart = true;

        if (exceptionOccurred) {
            return;
        }
        autoHide = PreferenceUtil.isAutoHideControls(this);
        isToggleFF = PreferenceUtil.isFastForwardToggleable(this);
        isFF = false;
        isFFPressed = false;

        switch (PreferenceUtil.getDisplayRotation(this)) {
            case AUTO:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case PORT:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case LAND:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case SENSOR:
                if (android.os.Build.VERSION.SDK_INT < 9) {
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                } else {
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
                break;
        }

        manager.setFastForwardFrameCount(PreferenceUtil
                .getFastForwardFrameCount(this));
        if (PreferenceUtil.isDynamicDPADEnable(this)) {
            if (!controllers.contains(dynamic)) {
                controllers.add(dynamic);
                controllerViews.add(dynamic.getView());
            }
            PreferenceUtil.setDynamicDPADUsed(this, true);
        } else {
            controllers.remove(dynamic);
            controllerViews.remove(dynamic.getView());

        }

        if (PreferenceUtil.isFastForwardEnabled(this)) {
            PreferenceUtil.setFastForwardUsed(this, true);
        }

        if (PreferenceUtil.isScreenSettingsSaved(this)) {
            PreferenceUtil.setScreenLayoutUsed(this, true);
        }
        VirtualDPad.getInstance().detachFromWindow();
        pm = getPackageManager();
        pn = getPackageName();
        for (EmulatorController controller : controllers) {
            controller.onResume();
        }
        boolean exceptionHandled = false;
        try {
            try {
                manager.startGame(game);
            } catch (EmulatorException e) {
                exceptionHandled = onFailedToLoadGame();
                throw e;
            }
            for (EmulatorController controller : controllers) {
                controller.onGameStarted(game);
            }
            if (slotToRun != -1) {
                manager.loadState(slotToRun);
            } else {
                if (SlotUtils.autoSaveExists(baseDir, game.checksum)) {
                    manager.loadState(0);
                }
            }
            if (slotToSave != null) {
                manager.copyAutoSave(slotToSave);
                slotToSave = null;
            }
            boolean wasRotated = (oldConfig & ActivityInfo.CONFIG_ORIENTATION) == ActivityInfo.CONFIG_ORIENTATION;
            oldConfig = 0;

            if (shouldPause() && !wasRotated) {

                gameMenu.open(true);
            }
            setShouldPauseOnResume(true);
            if (gameMenu != null && gameMenu.isOpen()) {
                manager.pauseEmulation();
            }
            slotToRun = 0;
            int quality = PreferenceUtil.getEmulationQuality(this);
            emulatorView.setQuality(quality);
            emulatorView.onResume();
            enableCheats();
            WifiServerInfoTransmitter.onResume(this, game.name);

            if (Build.VERSION.SDK_INT < 13) {
            }
        } catch (EmulatorException e) {
            if (!exceptionHandled) {
                handleException(e);
            }
        }
    }

    protected boolean onFailedToLoadGame() {
        return false;
    }

    private void enableCheats() {
        int numCheats = 0;
        try {
            numCheats = manager.enableCheats(this, game);
        } catch (final EmulatorException e) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(EmulatorActivity.this,
                            e.getMessage(EmulatorActivity.this),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (numCheats > 0) {
            final int fNumCheats = numCheats;
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(
                            EmulatorActivity.this,
                            String.format(
                                    getText(R.string.toast_cheats_enabled)
                                            .toString(), fNumCheats),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (exceptionOccurred) {
            return;
        }
        if (hasFocus) {
            for (EmulatorController controller : controllers) {
                controller.onResume();
            }
        }
    }

    public String getGamePath() {
        return game.path;
    }

    public static final String EXTRA_GAME = "game";
    public static final String EXTRA_SLOT = "slot";
    public static final String EXTRA_FROM_GALLERY = "fromGallery";

    private Integer slotToRun;
    private Integer slotToSave;

    private List<EmulatorController> controllers;

    protected Manager manager;
    private EmulatorView emulatorView;
    private List<View> controllerViews;
    private ViewGroup group;
    private String baseDir;

    public void openTimeTravelDialog(final boolean checkPro) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (game != null) {
                    if (checkPro
                            && Utils.isAdvertisingVersion(EmulatorActivity.this)) {
                        Toast.makeText(EmulatorActivity.this,
                                "Only available in Pro version",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    GameMenuItem item = gameMenu
                            .findGameMenuItem(R.string.game_menu_back_to_past);
                    if (!PreferenceUtil
                            .isTimeshiftEnabled(EmulatorActivity.this)) {
                        Toast.makeText(EmulatorActivity.this,
                                "Rewinding is disabled in preferences",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        onGameMenuItemSelected(gameMenu, item);
                    }
                }
            }
        });
    }

    public void openGameMenu() {

        runOnUiThread(new Runnable() {
            public void run() {
                if (game != null) {
                    gameMenu.open(true);
                }
            }

            ;
        });

    }

    @Override
    public void onGameMenuCreate(GameMenu menu) {
        menu.add(R.string.game_menu_reset, R.drawable.ic_reset);
        menu.add(R.string.game_menu_save, R.drawable.ic_save);
        menu.add(R.string.game_menu_load, R.drawable.ic_load);
        menu.add(R.string.game_menu_cheats, R.drawable.ic_cheats);
        menu.add(R.string.game_menu_back_to_past, R.drawable.ic_time_machine);
        menu.add(R.string.game_menu_screenshot, R.drawable.ic_make_screenshot);

        EmulatorApplication ea = (EmulatorApplication) getApplication();
        if (ea.hasGameMenu()) {
            menu.add(R.string.game_menu_settings, R.drawable.ic_game_settings);
        } else {
            menu.add(R.string.gallery_menu_pref, R.drawable.ic_game_settings);
        }

    }

    @Override
    public void onGameMenuPrepare(GameMenu menu) {
        GameMenuItem backToPast = menu.getItem(R.string.game_menu_back_to_past);
        backToPast.enable = PreferenceUtil.isTimeshiftEnabled(this);
        Log.i(TAG, "prepare menu");
    }

    

    @Override
    public void onGameMenuClosed(GameMenu menu) {
        try {
            if (!runTimeMachine) {
                if (!menu.isOpen()) {
                    manager.resumeEmulation();
                    for (EmulatorController controller : controllers) {
                        controller.onResume();
                    }

                }

            }
        } catch (EmulatorException e) {
            handleException(e);
        }
    }

    @Override
    public void onGameMenuOpened(GameMenu menu) {
        Log.i(TAG, "on game menu open");
        try {
            if (manager != null) {
                boolean success = manager.pauseEmulation();
                if (success) {
                    for (EmulatorController controller : controllers) {
                        controller.onGamePaused(game);
                    }
                } else {
                    try {
                        gameMenu.dismiss();
                    } catch (Exception e) {

                    }
                }
            }
        } catch (EmulatorException e) {
            handleException(e);
        }

    }

    // Author: Tomasz Dahms
    Timer RewindTimer;
    public void onRewindDown() {
        manager.pauseEmulation();

        RewindTimer = new Timer();
        RewindTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {


                int max = manager.getHistoryItemCount() - 1;
                int LoadHistory=1;
                if (max>LoadHistory) manager.loadHistoryState(LoadHistory);

            }
        }, 0, 100);

    }

    // Author: Tomasz Dahms
    public void onRewindUp() {
        RewindTimer.cancel();
        manager.resumeEmulation();
    }


    boolean runTimeMachine = false;
    TimeTravelDialog dialog;

    @Override
    public void startActivity(Intent intent) {
        setShouldPauseOnResume(false);
        super.startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent, Bundle options) {
        setShouldPauseOnResume(false);
        super.startActivity(intent, options);
    }

    private void freeStartActivityForResult(Activity activity, Intent intent,
                                            int requestCode) {
        setShouldPauseOnResume(false);
        FreeAppUtil.startActivityForResult(activity, intent, requestCode);
    }

    private void freeStartActivity(Activity activity, Intent intent) {
        setShouldPauseOnResume(false);
        FreeAppUtil.startActivity(activity, intent);
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onGameMenuItemSelected(GameMenu menu, GameMenuItem item) {
        try {
            if (item.id == R.string.game_menu_back_to_past) {
                if (manager.getHistoryItemCount() > 1) {
                    if (FreeAppUtil.check(this)) {
                        dialog = new TimeTravelDialog(this, manager, game);

                        dialog.setOnDismissListener(new OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                runTimeMachine = false;
                                try {
                                    manager.resumeEmulation();
                                } catch (EmulatorException e) {
                                    handleException(e);
                                }
                            }
                        });
                        DialogUtils.show(dialog, true);
                        runTimeMachine = true;
                    }
                }
            } else if (item.id == R.string.game_menu_reset) {
                manager.resetEmulator();
                enableCheats();
            } else if (item.id == R.string.game_menu_save) {
                Intent i = new Intent(this, SlotSelectionActivity.class);
                i.putExtra(SlotSelectionActivity.EXTRA_GAME, game);
                i.putExtra(SlotSelectionActivity.EXTRA_BASE_DIRECTORY, baseDir);
                i.putExtra(SlotSelectionActivity.EXTRA_DIALOG_TYPE_INT,
                        SlotSelectionActivity.DIALOAG_TYPE_SAVE);
                freeStartActivityForResult(this, i, REQUEST_SAVE);

            } else if (item.id == R.string.game_menu_load) {
                Intent i = new Intent(this, SlotSelectionActivity.class);
                i.putExtra(SlotSelectionActivity.EXTRA_GAME, game);
                i.putExtra(SlotSelectionActivity.EXTRA_BASE_DIRECTORY, baseDir);
                i.putExtra(SlotSelectionActivity.EXTRA_DIALOG_TYPE_INT,
                        SlotSelectionActivity.DIALOAG_TYPE_LOAD);
                freeStartActivityForResult(this, i, REQUEST_LOAD);
            } else if (item.id == R.string.game_menu_cheats) {
                Intent i = new Intent(this, CheatsActivity.class);
                i.putExtra(CheatsActivity.EXTRA_IN_GAME_HASH, game.checksum);
                freeStartActivity(this, i);
            } else if (item.id == R.string.game_menu_settings) {
                Intent i = new Intent(this, GamePreferenceActivity.class);
                if (Build.VERSION.SDK_INT >= 11) {
                    i.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                    i.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                            GamePreferenceFragment.class.getName());
                }
                i.putExtra(GamePreferenceActivity.EXTRA_GAME, game);
                startActivity(i);
            } else if (item.id == R.string.gallery_menu_pref) {
                Intent i = new Intent(this, GeneralPreferenceActivity.class);
                if (Build.VERSION.SDK_INT >= 11) {
                    i.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                    i.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                            GeneralPreferenceFragment.class.getName());
                }
                startActivity(i);
            } else if (item.id == R.string.game_menu_screenshot) {

                takeScreenshot();

            }

        } catch (EmulatorException e) {
            handleException(e);
        }

    }

    private void takeScreenshot() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            String name = game.getCleanName() + "-screenshot";
            File dir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    EmulatorInfoHolder.getInfo().getName()
                            .replace(' ', '_'));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File to = dir;
            int counter = 0;
            while (to.exists()) {
                String nn = name
                        + (counter == 0 ? "" : "(" + counter + ")")
                        + ".png";
                to = new File(dir, nn);
                counter++;
            }

            try {
                FileOutputStream fos = new FileOutputStream(to);
                Utils.createScreenshotBitmap(this, game,
                        Utils.isAdvertisingVersion(this)).compress(
                        CompressFormat.PNG, 90, fos);
                fos.close();
                Toast.makeText(
                        this,
                        String.format(
                                res(R.string.act_game_screenshot_saved),
                                to.getAbsolutePath()), Toast.LENGTH_LONG)
                        .show();
            } catch (IOException e) {
                Log.e(TAG, "", e);
                throw new EmulatorException(
                        res(R.string.act_game_screenshot_failed));
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_PERM_REQ);
        }
    }

    final static int WRITE_PERM_REQ = 1233254689;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_PERM_REQ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeScreenshot();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    private String res(int id) {
        return getResources().getString(id);
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (event.isAltPressed()) {
                    return true;
                }
                if (android.os.Build.VERSION.SDK_INT >= 9) {
                    if ((event.getSource() & 1025) == 1025) {
                        return true;
                    }
                }
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_3D_MODE:
            case KeyEvent.KEYCODE_APP_SWITCH:
                return super.onKeyUp(keyCode, event);
            default:
                return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                openGameMenu();
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (event.isAltPressed()) {
                    return true;
                }
                if (android.os.Build.VERSION.SDK_INT >= 9) {
                    if ((event.getSource() & 1025) == 1025) {
                        return true;
                    }
                }
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_3D_MODE:
            case KeyEvent.KEYCODE_APP_SWITCH:
                return super.onKeyDown(keyCode, event);
            default:
                return true;
        }
    }

    

}
