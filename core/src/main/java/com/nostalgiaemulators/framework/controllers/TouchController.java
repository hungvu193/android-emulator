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

package com.nostalgiaemulators.framework.controllers;

import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchBtnInterface;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchButton;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchImageButton;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchLayer;
import com.nostalgiaemulators.framework.ui.multitouchbutton.OnMultitouchEventListener;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.FontUtil;
import com.nostalgiaemulators.framework.utils.Utils;

public class TouchController implements EmulatorController,
        OnMultitouchEventListener {

    public TouchController(EmulatorActivity emulatorActivity) {
        this.emulatorActivity = emulatorActivity;

    }

    public void onResume() {
        if (multitouchLayer != null) {
            multitouchLayer.setVibrationDuration(PreferenceUtil
                    .getVibrationDuration(emulatorActivity));
        }
        emulator.resetKeys();
        multitouchLayer.reloadTouchProfile();
        multitouchLayer.setOpacity(PreferenceUtil
                .getControlsOpacity(emulatorActivity));
        multitouchLayer.setEnableStaticDPAD(!PreferenceUtil
                .isDynamicDPADEnable(emulatorActivity));

    }

    public void onPause() {

    }

    private static final String TAG = "com.nostalgiaemulators.framework.controllers.TouchController";

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    }

    public void onDestroy() {
        multitouchLayer = null;
        emulatorActivity = null;
    }

    private Emulator emulator;

    private EmulatorActivity emulatorActivity;

    private int port;

    @Override
    public void connectToEmulator(int port, Emulator emulator) {
        this.emulator = emulator;
        this.port = port;
        mapping = emulator.getInfo().getKeyMapping();

    }

    private Map<Integer, Integer> mapping;

    private SparseIntArray resIdMapping = new SparseIntArray();

    private MultitouchLayer multitouchLayer;

    private ImageView remoteIc, zapperIc, palIc, ntscIc, muteIc;

    private View view;

    private MultitouchImageButton aTurbo, bTurbo, abButton, fastForward;

    boolean isPointerHandled(int pointerId) {
        return multitouchLayer.isPointerHandled(pointerId);
    }

    private View createView() {
        LayoutInflater inflater = (LayoutInflater) emulatorActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.controler_layout, null);

        multitouchLayer = layout
                .findViewById(R.id.touch_layer);

        MultitouchImageButton up = multitouchLayer
                .findViewById(R.id.button_up);
        up.setOnMultitouchEventlistener(this);
        resIdMapping
                .put(R.id.button_up, getMappedKeyCode(EmulatorController.KEY_UP));

        MultitouchImageButton down = multitouchLayer
                .findViewById(R.id.button_down);
        down.setOnMultitouchEventlistener(this);
        resIdMapping.put(R.id.button_down,
                getMappedKeyCode(EmulatorController.KEY_DOWN));

        MultitouchImageButton left = multitouchLayer
                .findViewById(R.id.button_left);
        left.setOnMultitouchEventlistener(this);
        resIdMapping.put(R.id.button_left,
                getMappedKeyCode(EmulatorController.KEY_LEFT));

        MultitouchImageButton right = multitouchLayer
                .findViewById(R.id.button_right);
        right.setOnMultitouchEventlistener(this);
        resIdMapping.put(R.id.button_right,
                getMappedKeyCode(EmulatorController.KEY_RIGHT));

        MultitouchImageButton a = multitouchLayer
                .findViewById(R.id.button_a);
        a.setOnMultitouchEventlistener(this);
        resIdMapping.put(R.id.button_a, getMappedKeyCode(EmulatorController.KEY_A));

        MultitouchImageButton b = multitouchLayer
                .findViewById(R.id.button_b);
        b.setOnMultitouchEventlistener(this);
        resIdMapping.put(R.id.button_b, getMappedKeyCode(EmulatorController.KEY_B));


        MultitouchImageButton leftBtn = multitouchLayer
                .findViewById(R.id.button_l);
        if(leftBtn!= null) {
            leftBtn.setOnMultitouchEventlistener(this);
            resIdMapping.put(R.id.button_l, getMappedKeyCode(EmulatorController.KEY_L));
        }

        MultitouchImageButton rightBtn = multitouchLayer
                .findViewById(R.id.button_r);
        if(rightBtn!= null) {
            rightBtn.setOnMultitouchEventlistener(this);
            resIdMapping.put(R.id.button_r, getMappedKeyCode(EmulatorController.KEY_R));
        }

        aTurbo = multitouchLayer
                .findViewById(R.id.button_a_turbo);
        aTurbo.setOnMultitouchEventlistener(this);
        resIdMapping.put(R.id.button_a_turbo,
                getMappedKeyCode(EmulatorController.KEY_A_TURBO));

        bTurbo = multitouchLayer
                .findViewById(R.id.button_b_turbo);
        bTurbo.setOnMultitouchEventlistener(this);
        resIdMapping.put(R.id.button_b_turbo,
                getMappedKeyCode(EmulatorController.KEY_B_TURBO));

        abButton = multitouchLayer
                .findViewById(R.id.button_ab);

        fastForward = multitouchLayer
                .findViewById(R.id.button_fast_forward);
        fastForward
                .setOnMultitouchEventlistener(new OnMultitouchEventListener() {
                    @Override
                    public void onMultitouchExit(MultitouchBtnInterface btn) {
                        emulatorActivity.onFastForwardUp();
                    }

                    @Override
                    public void onMultitouchEnter(MultitouchBtnInterface btn) {
                        emulatorActivity.onFastForwardDown();

                    }
                });

        Typeface font = FontUtil.createFontFace(emulatorActivity, true);

        MultitouchButton select = layout
                .findViewById(R.id.button_select);
        if (select != null) {
            select.setTypeface(font);

            select.setOnMultitouchEventlistener(new OnMultitouchEventListener() {
                @Override
                public void onMultitouchExit(MultitouchBtnInterface btn) {
                    emulator.setKeyPressed(port,
                            getMappedKeyCode(EmulatorController.KEY_SELECT), false);
                }

                @Override
                public void onMultitouchEnter(MultitouchBtnInterface btn) {
                    emulator.setKeyPressed(port,
                            getMappedKeyCode(EmulatorController.KEY_SELECT), true);
                }
            });
        }
        MultitouchButton start = layout
                .findViewById(R.id.button_start);
        start.setTypeface(font);
        start.setOnMultitouchEventlistener(new OnMultitouchEventListener() {
            @Override
            public void onMultitouchExit(MultitouchBtnInterface btn) {
                emulator.setKeyPressed(port,
                        getMappedKeyCode(EmulatorController.KEY_START), false);
            }

            @Override
            public void onMultitouchEnter(MultitouchBtnInterface btn) {
                emulator.setKeyPressed(port,
                        getMappedKeyCode(EmulatorController.KEY_START), true);
            }
        });

        MultitouchImageButton menu = layout
                .findViewById(R.id.button_menu);
        menu.setOnMultitouchEventlistener(new OnMultitouchEventListener() {
            @Override
            public void onMultitouchExit(MultitouchBtnInterface btn) {
            }

            @Override
            public void onMultitouchEnter(MultitouchBtnInterface btn) {
                emulatorActivity.openGameMenu();
            }
        });

        View center = layout.findViewById(R.id.button_center);

        View[] views = new View[]{menu, select, start, up, down, right, left,
                a, b, center};
        for (View view : views) {
            if (view != null) {
                view.setFocusable(false);
            }
        }

        remoteIc = layout.findViewById(R.id.ic_game_remote);
        zapperIc = layout.findViewById(R.id.ic_game_zapper);
        palIc = layout.findViewById(R.id.ic_game_pal);
        ntscIc = layout.findViewById(R.id.ic_game_ntsc);
        muteIc = layout.findViewById(R.id.ic_game_muted);
        return layout;
    }

    private int getMappedKeyCode(int original) {
        if (mapping != null && mapping.containsKey(original)) {
            Integer code = mapping.get(original);
            return code == null ? -1 : code;
        } else {
            return -1;
        }
    }

    @Override
    public View getView() {
        if (view == null) {
            view = createView();
        }
        return view;

    }

    
    @Override
    public void onMultitouchEnter(MultitouchBtnInterface btn) {
        emulator.setKeyPressed(port, resIdMapping.get(btn.getId()), true);
    }

    @Override
    public void onMultitouchExit(MultitouchBtnInterface btn) {
        emulator.setKeyPressed(port, resIdMapping.get(btn.getId()), false);
    }

    @Override
    public void onGameStarted(GameDescription game) {
        GfxProfile gfxProfile = emulator.getActiveGfxProfile();

        zapperIc.setVisibility(PreferenceUtil.isZapperEnabled(emulatorActivity,
                game.checksum) ? View.VISIBLE : View.GONE);

        palIc.setVisibility(gfxProfile != null && gfxProfile.name.equals("PAL") ? View.VISIBLE
                : View.GONE);
        ntscIc.setVisibility(gfxProfile != null
                && gfxProfile.name.equals("NTSC") ? View.VISIBLE : View.GONE);

        boolean remoteVisible = PreferenceUtil
                .isWifiServerEnable(emulatorActivity)
                && Utils.isWifiAvailable(emulatorActivity);
        remoteIc.setVisibility(remoteVisible ? View.VISIBLE : View.INVISIBLE);

        muteIc.setVisibility(PreferenceUtil.isSoundEnabled(emulatorActivity) ? View.GONE
                : View.VISIBLE);

        if (PreferenceUtil.isTurboEnabled(emulatorActivity)) {
            aTurbo.setVisibility(View.VISIBLE);
            bTurbo.setVisibility(View.VISIBLE);
            aTurbo.setEnabled(true);
            bTurbo.setEnabled(true);
        } else {
            aTurbo.setVisibility(View.INVISIBLE);
            bTurbo.setVisibility(View.INVISIBLE);
            aTurbo.setEnabled(false);
            bTurbo.setEnabled(false);
        }

        if (PreferenceUtil.isFastForwardEnabled(emulatorActivity)) {
            fastForward.setVisibility(View.VISIBLE);
            fastForward.setEnabled(true);
        } else {
            fastForward.setVisibility(View.INVISIBLE);
            fastForward.setEnabled(false);
        }

        abButton.setVisibility(PreferenceUtil
                .isABButtonEnabled(emulatorActivity) ? View.VISIBLE
                : View.INVISIBLE);
        abButton.setEnabled(PreferenceUtil.isABButtonEnabled(emulatorActivity));
        multitouchLayer.invalidate();
    }

    @Override
    public void onGamePaused(GameDescription game) {

    }

    public void hide() {
        if (!hidden) {
            emulatorActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.GONE);

                }
            });
            hidden = true;
        }
    }

    public void show() {
        if (hidden) {
            emulatorActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.VISIBLE);
                }
            });
            hidden = false;
        }

    }

    private boolean hidden = false;
}
