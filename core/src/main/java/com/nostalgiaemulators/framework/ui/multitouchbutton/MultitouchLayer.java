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

package com.nostalgiaemulators.framework.ui.multitouchbutton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.base.Migrator;
import com.nostalgiaemulators.framework.base.ViewPort;
import com.nostalgiaemulators.framework.base.ViewUtils;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.FontUtil;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

import org.acra.ACRA;

public class MultitouchLayer extends RelativeLayout implements OnTouchListener {
    private static final String TAG = "package com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchLayer";

    private ArrayList<View> btns = new ArrayList<View>();
    private SparseIntArray pointerMap = new SparseIntArray();
    private int touchMapWidth;
    private int touchMapHeight;
    private SparseIntArray ridToIdxMap = new SparseIntArray();
    private static final int EMPTY_COLOR = 0x00;

    private Paint editElementPaint = new Paint();
    private Bitmap resizeIcon;

    private static final int BUTTON_MIN_SIZE_DP = 20;
    private float buttonMinSizePx = 0;

    Paint videoModeLabelPaint = new Paint();

    private static final int[] VIDEOMODE_COLORS = new int[]{0xffff8800,
            0xff99cc00};

    private interface OnEditItemClickListener {
        void onClick();
    }

    public static class PreferenceMigrator implements Migrator {

        @Override
        public void doExport(Context context, String baseDir) {
            migrate(PreferenceUtil.EXPORT, context, baseDir);
        }

        @Override
        public void doImport(Context context, String baseDir) {
            migrate(PreferenceUtil.IMPORT, context, baseDir);
        }

        private void migrate(int type, Context context, String baseDir) {
            SharedPreferences pref1 = context.getSharedPreferences(
                    getPrefName(0), Context.MODE_PRIVATE);
            SharedPreferences pref2 = context.getSharedPreferences(
                    getPrefName(1), Context.MODE_PRIVATE);
            PreferenceUtil.migratePreferences(type, pref1, new File(baseDir,
                    getPrefName(0)), PreferenceUtil.NotFoundHandling.IGNORE);
            PreferenceUtil.migratePreferences(type, pref2, new File(baseDir,
                    getPrefName(1)), PreferenceUtil.NotFoundHandling.IGNORE);
        }

    }

    
    private class EditElement {

        RectF boundingbox = new RectF();

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<RectF> offsets = new ArrayList<>();
        ArrayList<RectF> resizeRects = new ArrayList<>();

        boolean movable = true;
        RectF boundingboxHistory = new RectF();
        ArrayList<Rect> boundingboxsHistory = new ArrayList<>();
        ArrayList<RectF> offsetshistory = new ArrayList<>();

        boolean validPosition = true;

        float minimalSize;
        boolean isScreenElement = false;

        public EditElement(int rid, boolean movable, float minimalSize) {
            int idx = ridToIdxMap.get(rid);
            if (idx != -1) {
                ids.add(idx);
                boundingbox.set(boundingBoxs[idx]);
                boundingboxHistory.set(boundingbox);
            }
            computeOffsets();
            this.movable = movable;
            this.minimalSize = minimalSize;
        }

        public EditElement(Rect viewPort) {
            isScreenElement = true;
            boundingbox.set(viewPort);
            boundingboxHistory.set(viewPort);
            computeOffsets();
            this.movable = true;
            this.minimalSize = 200;

        }

        private RectF resizingBox = new RectF();

        public RectF getResizingBox() {
            final int K = resizeIcon.getHeight() / (isScreenElement ? 1 : 2);

            resizingBox.set(boundingbox.right - K, boundingbox.bottom - K,
                    boundingbox.right + K, boundingbox.bottom + K);

            return resizingBox;
        }

        public void add(int rid) {
            int idx = ridToIdxMap.get(rid);
            ids.add(idx);
            RectF tmp = new RectF();
            tmp.set(boundingBoxs[idx]);
            boundingbox.union(tmp);
            boundingboxHistory.set(boundingbox);
            computeOffsets();
        }

        public void computeOffsets() {
            offsets.clear();
            if (isScreenElement) {
                RectF offset = new RectF(boundingbox.left, boundingbox.top, 0,
                        0);
                offsets.add(offset);
            } else {
                for (Integer id : ids) {
                    Rect r = boundingBoxs[id];
                    RectF offset = new RectF(r.left - boundingbox.left, r.top
                            - boundingbox.top, 0, 0);
                    offsets.add(offset);
                }
            }
        }

        public void computeBoundingBox() {
            if (!isScreenElement) {
                boundingbox.set(boundingBoxs[ids.get(0)]);
                for (Integer id : ids) {
                    Rect r = boundingBoxs[id];
                    RectF tmp = new RectF();
                    tmp.set(r);
                    boundingbox.union(tmp);
                }
            }
        }

        public EditElement saveHistory() {
            boundingboxsHistory.clear();
            offsetshistory.clear();
            if (isScreenElement) {

            } else {
                for (int i = 0; i < offsets.size(); i++) {
                    int id = ids.get(i);
                    boundingboxsHistory.add(new Rect(boundingBoxs[id]));
                    offsetshistory.add(new RectF(offsets.get(i)));
                }
            }
            return this;
        }

        private OnEditItemClickListener listener = null;

        public void setOnClickListener(OnEditItemClickListener listener) {
            this.listener = listener;
        }

    }

    private ArrayList<EditElement> editElements = new ArrayList<MultitouchLayer.EditElement>();

    
    private byte[][] maps;

    
    private Rect[] boundingBoxs;

    
    private Bitmap[] buttonsBitmaps = new Bitmap[0];

    
    private Bitmap[] pressedButtonsBitmaps = new Bitmap[0];

    
    private ArrayList<Integer> dpadRIDs = new ArrayList<>();

    
    @Deprecated()
    
    @SuppressLint("NewApi")
    public MultitouchLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MultitouchLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultitouchLayer(Context context) {
        super(context);
        init();
    }

    int lastW = 0, lastH = 0;

    LinearLayout touchLayer;
    Vibrator vibrator;
    Paint paint = new Paint();
    Paint bitmapRectPaint = new Paint();

    public void setOpacity(int alpha) {
        if (!isInEditMode()) {
            paint.setAlpha(alpha);
        }
    }

    @SuppressLint("UseSparseArrays")
    private void init() {
        updateHandler = new UpdateHandler(this);
        dpadRIDs.add(R.id.button_center);
        dpadRIDs.add(R.id.button_down);
        dpadRIDs.add(R.id.button_up);
        dpadRIDs.add(R.id.button_left);
        dpadRIDs.add(R.id.button_right);
        dpadRIDs.add(R.id.button_up_left);
        dpadRIDs.add(R.id.button_up_right);
        dpadRIDs.add(R.id.button_down_left);
        dpadRIDs.add(R.id.button_down_right);

        btnIdMap.add(R.id.button_a);
        btnIdMap.add(R.id.button_a_turbo);
        btnIdMap.add(R.id.button_b);
        btnIdMap.add(R.id.button_b_turbo);
        btnIdMap.add(R.id.button_ab);

        Map<Integer, Integer> mapping = EmulatorInfoHolder.getInfo().getKeyMapping();

        if (getMappedKeyCode(mapping, EmulatorController.KEY_SELECT) != -1) {
            btnIdMap.add(R.id.button_select);
        }

        if (getMappedKeyCode(mapping, EmulatorController.KEY_L) != -1) {
            btnIdMap.add(R.id.button_l);
        }

        if (getMappedKeyCode(mapping, EmulatorController.KEY_R) != -1) {
            btnIdMap.add(R.id.button_r);
        }


        btnIdMap.add(R.id.button_start);
        btnIdMap.add(R.id.button_menu);

        btnIdMap.add(R.id.button_down);
        btnIdMap.add(R.id.button_up);
        btnIdMap.add(R.id.button_left);
        btnIdMap.add(R.id.button_right);
        btnIdMap.add(R.id.button_up_left);
        btnIdMap.add(R.id.button_up_right);
        btnIdMap.add(R.id.button_down_left);
        btnIdMap.add(R.id.button_down_right);
        btnIdMap.add(R.id.button_center);

        btnIdMap.add(R.id.button_fast_forward);

        if (!isInEditMode()) {
            initScreenElement(false);
        }

        setBackgroundColor(0x01000000);
        paint.setFilterBitmap(true);

        editElementPaint.setColor(getContext().getResources().getColor(
                R.color.main_color));
        editElementPaint.setStyle(Style.STROKE);
        DashPathEffect dashPathEffect = new DashPathEffect(
                new float[]{1, 4}, 0);
        editElementPaint.setPathEffect(dashPathEffect);

        bitmapRectPaint.setStyle(Style.STROKE);
        bitmapRectPaint.setColor(0xffcccccc);

        resizeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.resize_icon);

        buttonMinSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, BUTTON_MIN_SIZE_DP, getResources()
                        .getDisplayMetrics());

        if (!isInEditMode()) {
            ViewTreeObserver vto = getViewTreeObserver();
            touchLayer = new LinearLayout(getContext());
            final Context context = getContext();
            vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    int w = getMeasuredWidth();
                    int h = getMeasuredHeight();
                    long timestamp = PreferenceUtil
                            .getControllerLayoutTimestamp(context);
                    if (w != lastW || h != lastH || timestamp != lastTimestamp) {
                        lastTimestamp = timestamp;
                        lastW = w;
                        lastH = h;
                        inited = initMultiTouchMap();
                    }
                }

            });

            vibrator = (Vibrator) getContext().getSystemService(
                    Context.VIBRATOR_SERVICE);
        }

        float videoModeLabelSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 14, getResources()
                        .getDisplayMetrics());

        videoModeLabelPaint.setTextSize(videoModeLabelSize);
        videoModeLabelPaint.setStyle(Style.STROKE);
        videoModeLabelPaint.setTypeface(FontUtil.createFontFace(getContext(),
                true));

    }

    private int getMappedKeyCode(Map<Integer, Integer> mapping, int original) {
        if (mapping != null && mapping.containsKey(original)) {
            Integer code = mapping.get(original);
            return code == null ? -1 : code;
        } else {
            return -1;
        }
    }

    long lastTimestamp;

    
    private ArrayList<Integer> btnIdMap = new ArrayList<>();

    static Rect getBoundingBox(View btn, View rootView) {
        int btnW = btn.getMeasuredWidth();
        int btnH = btn.getMeasuredHeight();
        int btnX = getRelativeLeft(btn, rootView);
        int btnY = getRelativeTop(btn, rootView);
        return new Rect(btnX, btnY, btnX + btnW, btnY + btnH);
    }

    private boolean initMultiTouchMap() {
        for (View btn : btns) {
            if (btn.getVisibility() != View.GONE) {
                if (btn.getMeasuredWidth() <= 0 || btn.getMeasuredHeight() <= 0
                        || btn.getWidth() <= 0 || btn.getHeight() <= 0) {
                    Log.e(TAG, "fail: " + btn);
                    return false;
                }
                Rect bb = getBoundingBox(btn, this);
                if (bb.width() <= 0 || bb.height() <= 0) {
                    Log.e(TAG, "fail: " + btn);
                    return false;
                }
            }
        }

        for (int i = 0; i < 100; i++)
            pointerMap.put(i, EMPTY_COLOR);

        ridToIdxMap.clear();

        Log.d(TAG, " create touch map width " + getMeasuredWidth() + " height:"
                + getMeasuredHeight());

        touchMapWidth = getMeasuredWidth();
        touchMapHeight = getMeasuredHeight();

        Rect r = new Rect();

        if (btns.size() == 0) {
            getAllImageButtons(this, btns);
        }

        int btnsCount = btns.size();
        Log.i(TAG, " found " + btnsCount + " multitouch btns");

        maps = new byte[btnsCount][];

        if (buttonsBitmaps != null) {
            for (Bitmap bitmap : buttonsBitmaps) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }

        if (buttonsBitmaps != null) {
            for (Bitmap bitmap : buttonsBitmaps) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }

        boundingBoxs = new Rect[btnsCount];
        buttonsBitmaps = new Bitmap[btnsCount];
        pressedButtonsBitmaps = new Bitmap[btnsCount];

        int idx = 0;
        for (View btn : btns) {
            ridToIdxMap.append(btn.getId(), idx);
            if (btn.getVisibility() != View.GONE) {

                btn.getLocalVisibleRect(r);

                boundingBoxs[idx] = getBoundingBox(btn, this);
                int btnX = boundingBoxs[idx].left;
                int btnY = boundingBoxs[idx].top;
                int btnW = boundingBoxs[idx].width();
                int btnH = boundingBoxs[idx].height();

                r.offsetTo(btnX, btnY);
                if (btnW > 0 && btnH > 0) {

                    Bitmap buttonBitmap = Bitmap.createBitmap(btnW, btnH,
                            Bitmap.Config.ARGB_8888);

                    if (buttonBitmap.isRecycled()) {
                        Log.wtf(TAG, "co se to kurva deje");
                        throw new RuntimeException("netusim");
                    }
                    Canvas buttonCanvas = new Canvas(buttonBitmap);
                    btn.draw(buttonCanvas);

                    if (!(btn instanceof MultitouchTwoButtonArea)) {
                        Bitmap pressedButtonBitmap = Bitmap.createBitmap(btnW,
                                btnH, Bitmap.Config.ARGB_8888);
                        Canvas pressedButtonCanvas = new Canvas(
                                pressedButtonBitmap);
                        btn.setPressed(true);
                        btn.draw(pressedButtonCanvas);
                        btn.setPressed(false);

                        pressedButtonsBitmaps[idx] = pressedButtonBitmap;
                        buttonsBitmaps[idx] = buttonBitmap;
                    } else {
                        buttonsBitmaps[idx] = buttonBitmap;
                        pressedButtonsBitmaps[idx] = null;
                    }

                }
            }
            idx++;
        }

        if (touchLayer.getParent() != null) {
            ViewGroup parent = (ViewGroup) touchLayer.getParent();
            parent.removeView(touchLayer);
        }
        touchLayer.setOnTouchListener(this);
        removeAllViews();

        addView(touchLayer, LinearLayout.LayoutParams.MATCH_PARENT,
                getMeasuredHeight());

        Map<Integer, Integer> mapping = EmulatorInfoHolder.getInfo().getKeyMapping();

        if (getMappedKeyCode(mapping, EmulatorController.KEY_SELECT) != -1) {
            editElements.add(new EditElement(R.id.button_select, true,
                    buttonMinSizePx).saveHistory());
        }

        if (getMappedKeyCode(mapping, EmulatorController.KEY_L) != -1) {
            editElements.add(new EditElement(R.id.button_l, true,
                    buttonMinSizePx).saveHistory());
        }

        if (getMappedKeyCode(mapping, EmulatorController.KEY_R) != -1) {
            editElements.add(new EditElement(R.id.button_r, true,
                    buttonMinSizePx).saveHistory());
        }

        editElements.add(new EditElement(R.id.button_start, true,
                buttonMinSizePx).saveHistory());
        EditElement dpad = new EditElement(R.id.button_center, true,
                buttonMinSizePx * 5);
        dpad.add(R.id.button_down);
        dpad.add(R.id.button_up);
        dpad.add(R.id.button_left);
        dpad.add(R.id.button_right);

        dpad.add(R.id.button_up_left);
        dpad.add(R.id.button_up_right);
        dpad.add(R.id.button_down_left);
        dpad.add(R.id.button_down_right);
        dpad.saveHistory();

        editElements.add(dpad);
        editElements.add(new EditElement(R.id.button_a, true, buttonMinSizePx)
                .saveHistory());
        editElements.add(new EditElement(R.id.button_b, true, buttonMinSizePx)
                .saveHistory());

        editElements.add(new EditElement(R.id.button_a_turbo, true,
                buttonMinSizePx).saveHistory());
        editElements.add(new EditElement(R.id.button_b_turbo, true,
                buttonMinSizePx).saveHistory());

        editElements.add(new EditElement(R.id.button_ab, true, buttonMinSizePx)
                .saveHistory());

        editElements.add(new EditElement(R.id.button_fast_forward, true,
                buttonMinSizePx).saveHistory());

        EditElement menu = new EditElement(R.id.button_menu, false,
                buttonMinSizePx).saveHistory();
        menu.setOnClickListener(new OnEditItemClickListener() {
            @Override
            public void onClick() {
                if (editMode != EDIT_MODE.NONE) {
                    ((Activity) getContext()).openOptionsMenu();
                }
            }
        });
        editElements.add(menu);
        menuElement = menu;

        reloadTouchProfile();
        setEnableStaticDPAD(staticDPADEnabled);

        return true;
    }

    boolean firstRun = true;

    EditElement screenElement = null;
    EditElement menuElement = null;

    public void reloadTouchProfile() {
        if (loadEditElements() || firstRun || (!isTouchMapsValid())) {
            firstRun = btns.size() == 0;

            int idx = 0;
            for (View btn : btns) {
                if (btn.getVisibility() != View.GONE) {
                    Rect bb = boundingBoxs[idx];
                    if (bb.width() <= 0 || bb.height() <= 0) {
                        Log.w(TAG, "tohle se nemuze stat");
                        firstRun = true;
                        return;
                    }
                }
                idx++;
            }

            idx = 0;
            for (View btn : btns) {
                if (btn.getVisibility() != View.GONE) {
                    Rect bb = boundingBoxs[idx];
                    if (btn.getId() == R.id.button_fast_forward) {
                        Log.i(TAG, "fast f btn " + idx + " bb " + bb);
                    }
                    int btnW = bb.width();
                    int btnH = bb.height();
                    Bitmap origButtonBitmap = buttonsBitmaps[idx];
                    Bitmap origPressedButtonBitmap = pressedButtonsBitmaps[idx];
                    if (origPressedButtonBitmap != null) {
                        Bitmap pressedBitmap = Bitmap.createScaledBitmap(
                                origPressedButtonBitmap, btnW, btnH, true);
                        origPressedButtonBitmap.recycle();
                        pressedButtonsBitmaps[idx] = pressedBitmap;
                    }

                    if (origButtonBitmap != null) {
                        Bitmap buttonBitmap = Bitmap.createScaledBitmap(
                                origButtonBitmap, btnW, btnH, true);
                        origButtonBitmap.recycle();
                        buttonsBitmaps[idx] = buttonBitmap;

                        int[] buttonPixels = new int[btnW * btnH];
                        buttonBitmap.getPixels(buttonPixels, 0, btnW, 0, 0,
                                btnW, btnH);

                        byte[] map = new byte[buttonPixels.length];
                        for (int i = 0; i < buttonPixels.length; i++) {
                            int pixel = buttonPixels[i];
                            map[i] = pixel == 0 ? 0 : (byte) (idx + 1);
                        }
                        maps[idx] = map;

                        if (btn instanceof MultitouchTwoButtonArea) {
                            buttonBitmap.recycle();
                            buttonsBitmaps[idx] = null;
                        }
                    }
                }
                idx++;
            }

        } else {
            Log.i(TAG, hashCode() + " nic se nezmenilo");
        }
    }

    
    private boolean isTouchMapsValid() {
        int idx = 0;
        for (View btn : btns) {
            if (btn.getVisibility() != View.GONE) {
                Rect bb = boundingBoxs[idx];
                int len = bb.width() * bb.height();
                byte[] map = maps[idx];
                if (map == null || (map.length != len)) {
                    return false;
                }
            }
            idx++;
        }
        return true;
    }

    private void getAllImageButtons(ViewGroup root, ArrayList<View> allButtons) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View v = root.getChildAt(i);

            if (v instanceof ViewGroup) {
                getAllImageButtons((ViewGroup) v, allButtons);
            } else if (v instanceof MultitouchBtnInterface) {
                allButtons.add(v);
            }

        }
    }

    private static int getRelativeLeft(View myView, View rootView) {
        ViewParent parent = myView.getParent();
        if (parent == null || parent == rootView)
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) parent, rootView);
    }

    private static int getRelativeTop(View myView, View rootView) {
        ViewParent parent = myView.getParent();

        if (parent == null || parent == rootView)
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) parent, rootView);
    }
    private static final int MAX_POINTERS = 6;
    private static final int COUNT_SKIP_MOVE_EVENT = 3;

    private int[] optimCounters = new int[MAX_POINTERS];

    private void handleTouchEvent(int x, int y, int pointerId, MotionEvent event) {
        if (pointerId < MAX_POINTERS
                && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (optimCounters[pointerId] < COUNT_SKIP_MOVE_EVENT) {
                optimCounters[pointerId]++;
                return;
            }
            optimCounters[pointerId] = 0;

        }

        if (x < 0 || y < 0 || x >= touchMapWidth || y >= touchMapHeight) {
            return;
        }
        int newBtnIdx = EMPTY_COLOR;
        for (int i = maps.length - 1; i >= 0; i--) {
            Rect boundingBox = boundingBoxs[i];
            if (boundingBox != null && boundingBox.contains(x, y)
                    && btns.get(i).isEnabled()) {
                byte[] map = maps[i];
                int newx = x - boundingBox.left;
                int newy = y - boundingBox.top;
                if (map == null) {
                    boolean debug = Utils.isDebuggable(getContext());
                    if (!debug) {
                        IllegalStateException e = new IllegalStateException(
                                "button touch map neni nainicializovany");
                        ACRA.getErrorReporter().handleSilentException(e);
                    }
                    newBtnIdx = i;
                    break;
                } else {
                    int idx = newx + newy * boundingBox.width();
                    if (idx < map.length) {
                        int btnIdx = map[idx];
                        if (btnIdx != 0) {
                            newBtnIdx = btnIdx;
                            break;
                        }
                    }
                }
            }
        }

        int oldBtnIdx = pointerMap.get(pointerId);
        if (newBtnIdx != 0) {
            if (oldBtnIdx != newBtnIdx) {
                if (oldBtnIdx != EMPTY_COLOR) {
                    onTouchExit(oldBtnIdx - 1, event);
                }
                onTouchEnter(newBtnIdx - 1, event);
                if (vibrationDuration > 0) {
                    vibrator.vibrate(vibrationDuration);
                }
            }
        } else if (oldBtnIdx != EMPTY_COLOR) {
            onTouchExit(oldBtnIdx - 1, event);

        }
        pointerMap.put(pointerId, newBtnIdx);
    }

    public void setVibrationDuration(int duration) {
        this.vibrationDuration = duration;
    }

    private int vibrationDuration = 100;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (editMode == EDIT_MODE.NONE) {
            if ((event.getActionMasked() == MotionEvent.ACTION_MOVE)) {
                int pointrcount = event.getPointerCount();

                for (int pointerIdx = 0; pointerIdx < pointrcount; pointerIdx++) {
                    int id = event.getPointerId(pointerIdx);
                    int x = (int) event.getX(pointerIdx);
                    int y = (int) event.getY(pointerIdx);
                    handleTouchEvent(x, y, id, event);
                }
            } else if ((event.getActionMasked() == MotionEvent.ACTION_UP)
                    || (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
                    || (event.getActionMasked() == MotionEvent.ACTION_CANCEL)) {
                int id = event.getPointerId(event.getActionIndex());
                int oldBtnIdx = pointerMap.get(id);
                if (oldBtnIdx != EMPTY_COLOR) {
                    onTouchExit(oldBtnIdx - 1, event);
                }
                pointerMap.put(id, EMPTY_COLOR);

            } else if ((event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    || (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)) {
                int pointerId = event.getPointerId(event.getActionIndex());
                int pointerIdx = event.findPointerIndex(pointerId);
                if (pointerIdx != -1) {
                    int x = (int) event.getX(pointerIdx);
                    int y = (int) event.getY(pointerIdx);
                    handleTouchEvent(x, y, pointerId, event);
                }

            }

        } else {
            onTouchInEditMode(event);
        }
        return true;

    }

    public boolean isPointerHandled(int pointerId) {
        return pointerMap.get(pointerId) != EMPTY_COLOR;
    }

    private boolean inited = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!inited) {
            inited = initMultiTouchMap();
            if (!inited) {
                return;
            }
        }

        if (!isInEditMode() && editMode == EDIT_MODE.NONE) {
            for (int idx = 0; idx < boundingBoxs.length; idx++) {
                MultitouchBtnInterface btn = (MultitouchBtnInterface) btns
                        .get(idx);
                btn.removeRequestRepaint();
                if (btn.getVisibility() == View.VISIBLE) {
                    Bitmap b = btn.isPressed() ? pressedButtonsBitmaps[idx]
                            : buttonsBitmaps[idx];
                    if (b != null) {
                        Rect bb = boundingBoxs[idx];
                        canvas.drawBitmap(b, bb.left, bb.top, paint);
                        if (editMode != EDIT_MODE.NONE) {
                            Paint pp = new Paint();
                            pp.setColor(0x5500ff00);
                            canvas.drawRect(bb, pp);
                        }

                    }
                }
            }
        }

        if (editMode != EDIT_MODE.NONE) {
            onDrawInEditMode(canvas);
        }
    }

    Paint editPaint = new Paint();


    private void onDrawInEditMode(Canvas canvas) {

        Paint p = new Paint();
        p.setColor(0xff888888);

        p.setAlpha(255);

        RectF dstScreenShotrect = new RectF();
        if (viewPortsEnvelops.size() > 1 && lastGfxProfileName != null) {
            RectF bb = screenElement.boundingbox;
            RectF env = null;
            int counter = 0;

            for (Entry<String, RectF> entry : viewPortsEnvelops.entrySet()) {
                if (entry.getKey().equals(lastGfxProfileName)) {
                    env = entry.getValue();
                    break;
                }
                counter++;
            }

            if(env != null) {
                dstScreenShotrect.left = bb.left + env.left * bb.width()
                        + (counter * 2) + 2;
                dstScreenShotrect.top = bb.top + env.top * bb.height()
                        + (counter * 2) + 2;
                dstScreenShotrect.right = bb.right - env.right * bb.width()
                        - ((counter * 2) + 1);
                dstScreenShotrect.bottom = bb.bottom - env.bottom * bb.height()
                        - ((counter * 2) + 1);
            }
        } else {
            dstScreenShotrect.set(screenElement.boundingbox);
        }

        if (lastGameScreenshot != null && !lastGameScreenshot.isRecycled()) {
            Rect src = new Rect(0, 0, lastGameScreenshot.getWidth(),
                    lastGameScreenshot.getHeight());
            canvas.drawBitmap(lastGameScreenshot, src, dstScreenShotrect, p);
        } else {
            canvas.drawRect(dstScreenShotrect, p);
        }

        if (editMode == EDIT_MODE.TOUCH) {
            canvas.drawRect(screenElement.boundingbox, bitmapRectPaint);
        } else if (editMode == EDIT_MODE.SCREEN && viewPortsEnvelops.size() > 1) {
            RectF rect = new RectF();
            RectF bb = screenElement.boundingbox;
            int counter = 0;
            for (Entry<String, RectF> entry : viewPortsEnvelops.entrySet()) {
                RectF env = entry.getValue();
                rect.left = bb.left + env.left * bb.width() + (counter * 2) + 2;
                rect.top = bb.top + env.top * bb.height() + (counter * 2) + 2;
                rect.right = bb.right - env.right * bb.width()
                        - ((counter * 2) + 1);
                rect.bottom = bb.bottom - env.bottom * bb.height()
                        - ((counter * 2) + 1);

                videoModeLabelPaint.setColor(VIDEOMODE_COLORS[counter
                        % VIDEOMODE_COLORS.length]);
                canvas.drawRect(rect, videoModeLabelPaint);
                videoModeLabelPaint.setTextAlign(counter % 2 == 0 ? Align.LEFT
                        : Align.RIGHT);
                canvas.drawText(
                        entry.getKey(),
                        counter % 2 == 0 ? (rect.left + videoModeLabelPaint
                                .getTextSize() / 4) : rect.right
                                - videoModeLabelPaint.getTextSize() / 4,
                        rect.bottom - videoModeLabelPaint.getTextSize() / 4,
                        videoModeLabelPaint);
                counter++;
            }
        }

        for (int idx = 0; idx < boundingBoxs.length; idx++) {
            MultitouchBtnInterface btn = (MultitouchBtnInterface) btns.get(idx);
            if (btn.getId() == R.id.button_menu) {
                paint.setAlpha(255);
            } else {
                paint.setAlpha(editMode == EDIT_MODE.SCREEN ? 64 : 255);
            }
            btn.removeRequestRepaint();
            Bitmap b = btn.isPressed() ? pressedButtonsBitmaps[idx]
                    : buttonsBitmaps[idx];
            if (b != null) {
                Rect bb = boundingBoxs[idx];
                Rect bRect = new Rect(0, 0, b.getWidth(), b.getHeight());
                canvas.drawBitmap(b, bRect, bb, paint);
            }
        }

        editPaint.setColor(0x55ff0000);

        if (editMode == EDIT_MODE.TOUCH) {

            for (EditElement e : editElements) {
                if (e.movable) {
                    if (!e.validPosition) {
                        canvas.drawRect(e.boundingbox, editPaint);
                    }
                    canvas.drawRect(e.boundingbox, editElementPaint);
                    RectF r = e.getResizingBox();
                    canvas.drawBitmap(resizeIcon, r.left, r.top,
                            editElementPaint);

                }

            }
        } else {
            EditElement e = screenElement;
            if (e.movable) {
                if (!e.validPosition) {
                    canvas.drawRect(e.boundingbox, editPaint);
                }
                canvas.drawRect(e.boundingbox, editElementPaint);
                RectF r = e.getResizingBox();
                canvas.drawBitmap(resizeIcon, r.left, r.top, editElementPaint);

            }
        }
    }

    private void onTouchInEditMode(MotionEvent event) {
        if (!isResizing) {
            onTouchInEditModeMove(event);
        } else {
            onTouchInEditModeResize(event);
        }
    }

    int selectIdx = -1;
    float selectW;
    float selectH;
    float startDragX = 0;
    float startDragY = 0;
    
    float startDragXoffset = 0;

    
    float startDragYoffset = 0;
    int startTouchX = 0;
    int startTouchY = 0;
    float startDistance = 0;

    private boolean onTouchCheck(EditElement e, int idx, int x, int y) {
        RectF boundingBox = e.boundingbox;
        RectF resizingAnchor = e.getResizingBox();
        if (e.listener != null && boundingBox.contains(x, y)) {
            e.listener.onClick();
        } else if ((resizingAnchor.contains(x, y) || boundingBox.contains(x, y))
                && e.movable) {

            lastValidBB.set(e.boundingbox);
            isResizing = resizingAnchor.contains(x, y);

            selectIdx = idx;
            selectW = boundingBox.width();
            selectH = boundingBox.height();
            startDragX = boundingBox.left;
            startDragY = boundingBox.top;
            startTouchX = x;
            startTouchY = y;

            startDragXoffset = boundingBox.right - x;
            startDragYoffset = boundingBox.bottom - y;

            if (isResizing) {
                e.resizeRects.clear();
                for (int i = 0; i < e.ids.size(); i++) {
                    int id = e.ids.get(i);
                    e.resizeRects.add(new RectF(boundingBoxs[id]));
                }
            }

            Rect invalR = new Rect();
            boundingBox.round(invalR);
            invalidate(invalR);
            return true;
        }
        return false;
    }

    private void onTouchInEditModeMove(MotionEvent event) {

        int action = event.getAction();
        int x = (int) (event.getX() + 0.5f);
        int y = (int) (event.getY() + 0.5f);
        switch (action) {

            case MotionEvent.ACTION_DOWN: {
                int idx = 0;

                if (editMode == EDIT_MODE.TOUCH) {
                    for (EditElement e : editElements) {
                        if (onTouchCheck(e, idx, x, y)) {
                            break;
                        }
                        idx++;
                    }
                } else {
                    onTouchCheck(screenElement, 0, x, y);
                    onTouchCheck(menuElement, 0, x, y);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:

                if (selectIdx != -1) {
                    EditElement element = null;
                    if (editMode == EDIT_MODE.TOUCH) {
                        element = editElements.get(selectIdx);
                    } else {
                        element = screenElement;
                    }

                    RectF elementBb = element.boundingbox;

                    int vx = x - startTouchX;
                    int vy = y - startTouchY;
                    RectF r = new RectF(elementBb);
                    float left = startDragX + vx;
                    float top = startDragY + vy;
                    r.set(left - 2, top - 2, left + selectW + 2, top + selectH + 2);

                    element.validPosition = isRectValid(r, element);
                    if (element.validPosition) {
                        lastValidBB.set(left, top, left + selectW, top + selectH);
                    }

                    r.set(left - 10, top - 10, left + selectW + 10, top + selectH
                            + 10);

                    Rect tempRect = new Rect();
                    r.round(tempRect);
                    invalidate(tempRect);
                    element.boundingbox.set(r.left + 10, r.top + 10, r.right - 10,
                            r.bottom - 10);
                    if (editMode == EDIT_MODE.TOUCH)
                        recomputeBtn(element);

                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE: {
                endMovementCheck();
            }
            break;
        }

    }

    RectF lastValidBB = new RectF();
    int lastTouchX = 0;
    int lastTouchY = 0;

    private void onTouchInEditModeResize(MotionEvent event) {

        int action = event.getAction();
        int x = (int) (event.getX() + 0.5f);
        lastTouchX = x;
        lastTouchY = Math.round(event.getY());
        switch (action) {

            case MotionEvent.ACTION_MOVE:
                if (selectIdx != -1) {
                    EditElement element = editMode == EDIT_MODE.TOUCH ? editElements
                            .get(selectIdx) : screenElement;
                    RectF elementBb = element.boundingbox;

                    float newW = x - startDragX + startDragXoffset;
                    float scaleFactorW = (newW / selectW);
                    elementBb.set(startDragX, startDragY, x + startDragXoffset,
                            startDragY + selectH * scaleFactorW);
                    if (editMode == EDIT_MODE.TOUCH)
                        recomputeBtn(element);

                    element.validPosition = isRectValid(elementBb, element);
                    if (element.validPosition) {
                        lastValidBB.set(element.boundingbox);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE: {
                isResizing = false;
                endMovementCheck();
            }
            break;
        }

    }

    
    private void recomputeBtn(EditElement element) {
        float scaleFactor = element.boundingbox.width()
                / element.boundingboxHistory.width();
        for (int i = 0; i < element.ids.size(); i++) {
            int id = element.ids.get(i);
            RectF offset = new RectF(element.offsetshistory.get(i));
            RectF bb = new RectF(element.boundingboxsHistory.get(i));
            RectF elemBB = element.boundingboxHistory;
            bb.offset(-elemBB.left, -elemBB.top);
            bb.left *= scaleFactor;
            bb.top *= scaleFactor;
            bb.right *= scaleFactor;
            bb.bottom *= scaleFactor;

            offset.left *= scaleFactor;
            offset.top *= scaleFactor;
            element.offsets.get(i).set(offset);
            bb.offset(element.boundingbox.left, element.boundingbox.top);
            bb.round(boundingBoxs[id]);
        }
    }

    private void endMovementCheck() {
        if (selectIdx != -1) {

            EditElement element = editMode == EDIT_MODE.TOUCH ? editElements
                    .get(selectIdx) : screenElement;
            if (!element.validPosition) {
                element.boundingbox.set(lastValidBB);
            }

            if (editMode == EDIT_MODE.TOUCH)
                recomputeBtn(element);
            element.validPosition = true;
            selectIdx = -1;

        }
        invalidate();
    }

    private boolean isRectValid(RectF r, EditElement element) {
        boolean isvalid = true;
        RectF globalBox = new RectF(0, 0, touchMapWidth, touchMapHeight);
        if (globalBox.contains(r)) {
            if (editMode == EDIT_MODE.TOUCH) {
                for (EditElement el : editElements) {

                    if (el != element && RectF.intersects(r, el.boundingbox)) {
                        isvalid = false;
                        break;
                    }
                }
            }
        } else {
            isvalid = false;
        }

        if (element.boundingbox.width() < element.minimalSize
                || element.boundingbox.height() < element.minimalSize) {
            isvalid = false;
        }

        return isvalid;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (Bitmap b : buttonsBitmaps) {
            if (b != null) {
                b.recycle();
            }
        }

        for (Bitmap b : pressedButtonsBitmaps) {
            if (b != null) {
                b.recycle();
            }
        }
        buttonsBitmaps = null;
        pressedButtonsBitmaps = null;

        Log.i(TAG, "on detach");
    }

    private void onTouchEnter(int btnIdx, MotionEvent event) {
        MultitouchBtnInterface btn = (MultitouchBtnInterface) btns.get(btnIdx);

        btn.onTouchEnter(event);
        btn.requestRepaint();
        invalidate(boundingBoxs[btnIdx]);

        if (btn instanceof MultitouchTwoButtonArea) {
            MultitouchTwoButtonArea mtba = (MultitouchTwoButtonArea) btn;
            int idx1 = ridToIdxMap.get(mtba.getFirstBtnRID());
            int idx2 = ridToIdxMap.get(mtba.getSecondBtnRID());
            invalidate(boundingBoxs[idx1]);
            invalidate(boundingBoxs[idx2]);

        } else if (btn instanceof MultitouchTwoButton) {
            MultitouchTwoButton mtba = (MultitouchTwoButton) btn;
            int idx1 = ridToIdxMap.get(mtba.getFirstBtnRID());
            int idx2 = ridToIdxMap.get(mtba.getSecondBtnRID());
            invalidate(boundingBoxs[idx1]);
            invalidate(boundingBoxs[idx2]);
        }
    }

    private void onTouchExit(int btnIdx, MotionEvent event) {
        MultitouchBtnInterface btn = (MultitouchBtnInterface) btns.get(btnIdx);

        btn.onTouchExit(event);
        invalidate(boundingBoxs[btnIdx]);
        btn.requestRepaint();

        if (btn instanceof MultitouchTwoButtonArea) {
            MultitouchTwoButtonArea mtba = (MultitouchTwoButtonArea) btn;
            int idx1 = ridToIdxMap.get(mtba.getFirstBtnRID());
            int idx2 = ridToIdxMap.get(mtba.getSecondBtnRID());
            invalidate(boundingBoxs[idx1]);
            invalidate(boundingBoxs[idx2]);
        } else if (btn instanceof MultitouchTwoButton) {
            MultitouchTwoButton mtba = (MultitouchTwoButton) btn;
            int idx1 = ridToIdxMap.get(mtba.getFirstBtnRID());
            int idx2 = ridToIdxMap.get(mtba.getSecondBtnRID());
            invalidate(boundingBoxs[idx1]);
            invalidate(boundingBoxs[idx2]);
        }
    }

    private Bitmap lastGameScreenshot;
    private String lastGfxProfileName = null;

    public void setLastgameScreenshot(Bitmap bitmap, String gfxProfileName) {
        Log.i(TAG, "set last profile:" + gfxProfileName);
        lastGameScreenshot = bitmap;
        lastGfxProfileName = gfxProfileName;

        initScreenElement(false);
        invalidate();
    }

    public enum EDIT_MODE {
        NONE, TOUCH, SCREEN
    }

    EDIT_MODE editMode = EDIT_MODE.NONE;
    int counter = 0;

    public void setEditMode(EDIT_MODE mode) {
        this.editMode = mode;
        invalidate();
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                counter++;

                editElementPaint.setPathEffect(new DashPathEffect(new float[]{
                        4, 4}, counter % 8));

                updateHandler.sendEmptyMessage(0);
            }
        }, 0, 50);

        if (editMode == EDIT_MODE.SCREEN) {
            resizeIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.resize_icon_2);
        }
    }

    HashMap<String, RectF> viewPortsEnvelops = new HashMap<String, RectF>();

    private void initScreenElement(boolean reset) {
        getPref();
        int topPadding = 0;
        if (currentRotation == 0) {
            topPadding = getResources().getDimensionPixelSize(
                    R.dimen.top_panel_touchcontroler_height);
        }

        ViewPort vport;
        HashMap<String, ViewPort> viewPorts;
        if (reset) {
            vport = ViewUtils.computeInitViewPort(getContext(), cacheW, cacheH,
                    0, topPadding);

            viewPorts = ViewUtils.computeAllInitViewPorts(getContext(), cacheW,
                    cacheH, 0, topPadding);

        } else {
            vport = ViewUtils.loadOrComputeViewPort(getContext(), null, cacheW,
                    cacheH, 0, topPadding, true);
            viewPorts = ViewUtils.loadOrComputeAllViewPorts(getContext(),
                    cacheW, cacheH, 0, topPadding);

        }

        Rect viewPort = new Rect(vport.x, vport.y, vport.x + vport.width - 1,
                vport.y + vport.height - 1);

        if (editMode != EDIT_MODE.NONE) {
            if (editMode == EDIT_MODE.SCREEN) {
                for (ViewPort port : viewPorts.values()) {
                    viewPort.left = port.x < viewPort.left ? port.x
                            : viewPort.left;
                    viewPort.top = port.y < viewPort.top ? port.y
                            : viewPort.top;

                    int right = port.x + port.width - 1;
                    viewPort.right = right > viewPort.right ? right
                            : viewPort.right;

                    int bottom = port.y + port.height - 1;
                    viewPort.bottom = bottom > viewPort.bottom ? bottom
                            : viewPort.bottom;
                }
            } else if (lastGfxProfileName != null) {
                ViewPort port = viewPorts.get(lastGfxProfileName);
                if (port != null) {
                    viewPort.left = port.x;
                    viewPort.top = port.y;
                    viewPort.right = port.x + port.width - 1;
                    viewPort.bottom = port.y + port.height - 1;
                }
            }

            viewPortsEnvelops = new HashMap<>(viewPorts.size());

            for (Entry<String, ViewPort> entry : viewPorts.entrySet()) {
                ViewPort port = entry.getValue();
                float w = viewPort.width();
                float h = viewPort.height();

                float relativeLeft = (-viewPort.left + port.x) / w;
                float relativeTop = (-viewPort.top + port.y) / h;
                float relativeRight = (viewPort.right - (port.x + port.width))
                        / w;
                float relativeBottom = (viewPort.bottom - (port.y + port.height))
                        / h;

                RectF envelop = new RectF(relativeLeft, relativeTop,
                        relativeRight, relativeBottom);
                viewPortsEnvelops.put(entry.getKey(), envelop);
            }

        }

        int topOffset = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources()
                        .getDisplayMetrics());

        viewPort.top -= currentRotation == 0 ? topOffset : 0;
        viewPort.bottom -= currentRotation == 0 ? topOffset : 0;

        Log.i(TAG,
                "init screenlayout "
                        + EmulatorInfoHolder.getInfo().getDefaultGfxProfile().name
                        + " vp:" + viewPort.left + "," + viewPort.top + ","
                        + viewPort.width() + "," + viewPort.height());
        screenElement = new EditElement(viewPort);
    }

    boolean isResizing = false;
    UpdateHandler updateHandler;

    private static class UpdateHandler extends Handler {
        public UpdateHandler(MultitouchLayer layer) {
            super(Looper.getMainLooper());
            this.layer = layer;
        }

        public void handleMessage(android.os.Message msg) {
            layer.invalidate();
        }

        ;

        MultitouchLayer layer;
    }

    ;

    public void stopEditMode() {
        timer.cancel();
    }

    Timer timer = new Timer();

    public void resetEditElement() {
        for (EditElement element : editElements) {
            element.boundingbox.set(element.boundingboxHistory);
            for (int i = 0; i < element.ids.size(); i++) {
                Rect bb = boundingBoxs[element.ids.get(i)];
                bb.set(element.boundingboxsHistory.get(i));
                element.offsets.get(i).set(element.offsetshistory.get(i));
            }
        }
        invalidate();

        Editor edit = getPref().edit();
        edit.clear();
        edit.commit();
    }

    public void resetScreenElement() {
        initScreenElement(true);
        PreferenceUtil.removeViewPortSave(getContext());
    }

    private boolean loadingSettings = true;

    public void disableLoadSettings() {
        loadingSettings = false;
        for (EditElement element : editElements) {
            element.boundingbox.set(element.boundingboxHistory);
            for (int i = 0; i < element.ids.size(); i++) {
                Rect bb = boundingBoxs[element.ids.get(i)];
                bb.set(element.boundingboxsHistory.get(i));
                element.offsets.get(i).set(element.offsetshistory.get(i));
            }
        }
        invalidate();

    }

    static String getPrefName(int rot) {
        String prefName = "-mtl-".concat(Integer.toString(rot)).concat(
                ".settings");
        return prefName;
    }

    
    int cacheRotation = -1;
    int currentRotation = -1;
    int cacheW = -1;
    int cacheH = -1;

    private SharedPreferences getPref() {
        if (cacheRotation == -1) {
            WindowManager mWindowManager = (WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            Display mDisplay = mWindowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(metrics);

            cacheW = Utils.getDisplayWidth(mDisplay);
            cacheH = Utils.getDisplayHeight(mDisplay);
            cacheRotation = (mDisplay.getRotation()) % 2;
            currentRotation = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? 0
                    : 1;
            Log.d(TAG, "cacheW x cacheH: " + cacheW + " x " + cacheH);
            fixBug(cacheRotation, currentRotation, cacheW, cacheH);
        }

        return getContext().getSharedPreferences(getPrefName(cacheRotation),
                Context.MODE_PRIVATE);
    }
    private void fixBug(int oldVersionRotation, int realRotation,
                        int screenWidth, int screenHeight) {
        ViewPort viewPort = PreferenceUtil.getViewPort(getContext(),
                screenWidth, screenHeight);
        if (viewPort == null) {
            Log.d(TAG, "fix bug: vp not found");
            return;
        }
        if (viewPort.version == 0) {
            viewPort.version = 1;
            if (oldVersionRotation != realRotation) {
                int top = getResources().getDimensionPixelSize(
                        R.dimen.top_panel_touchcontroler_height);
                if (realRotation == 0) {
                    if (viewPort.y == 0) {
                        viewPort.y += top;
                    }
                } else {
                    if (viewPort.y >= top) {
                        viewPort.y -= top;
                        viewPort.height += top;
                    }
                }
            } else {
                Log.d(TAG, "no vp bug");
            }
            PreferenceUtil.setViewPort(getContext(), viewPort, screenWidth,
                    screenHeight);
        }

    }

    public void saveScreenElement() {
        endMovementCheck();
        RectF bb = screenElement.boundingbox;
        RectF env = viewPortsEnvelops.get(EmulatorInfoHolder.getInfo()
                .getDefaultGfxProfile().name);

        Rect rect = new Rect();
        rect.left = Math.round(bb.left + env.left * bb.width());
        rect.top = Math.round(bb.top + env.top * bb.height());
        rect.right = Math.round(bb.right - env.right * bb.width());
        rect.bottom = Math.round(bb.bottom - env.bottom * bb.height());

        int topOffset = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, getResources()
                        .getDisplayMetrics());

        ViewPort vp = new ViewPort();
        vp.x = rect.left;
        vp.y = rect.top + (currentRotation == 0 ? topOffset : 0);
        vp.width = rect.width();
        vp.height = rect.height();

        Log.i(TAG, "save screenlayout "
                + EmulatorInfoHolder.getInfo().getDefaultGfxProfile().name
                + " vp:" + vp.x + "," + vp.y + "," + vp.width + "," + vp.height);

        PreferenceUtil.setViewPort(getContext(), vp, cacheW, cacheH);
    }

    public void saveEditElements() {
        endMovementCheck();
        SharedPreferences pref = getPref();
        Editor editor = pref.edit();

        editor.putInt("referenceWidth", touchMapWidth);
        editor.putInt("referenceHeight", touchMapHeight);
        for (int i = 0; i < btns.size(); i++) {
            View btn = btns.get(i);
            Rect offset = boundingBoxs[i];
            String s = offset.left + "-" + offset.top + "-" + offset.right
                    + "-" + offset.bottom;
            int id = btnIdMap.indexOf(btn.getId());
            editor.putString(id + "", s);
        }

        editor.commit();
        lastTimestamp = System.currentTimeMillis();
        PreferenceUtil
                .setControllerLayoutTimestamp(getContext(), lastTimestamp);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

    }

    
    private boolean loadEditElements() {
        if (touchMapWidth <= 0 || touchMapHeight <= 0) {
            return false;
        }
        if (!loadingSettings) {
            return false;
        }
        SharedPreferences pref = getPref();

        Map<String, ?> prefMap = pref.getAll();
        for (String key : prefMap.keySet()) {
            if (key.equals("referenceWidth") || key.equals("referenceHeight")) {
                continue;
            }
            try {
                Integer id = Integer.parseInt(key);
                if (id > 100) {
                    Log.i(TAG, "Detect old MTL format(" + id
                            + ")!\nTrying repaire it");

                    Editor editor = pref.edit();
                    editor.clear();
                    editor.commit();
                    break;
                }
            } catch (NumberFormatException e) {
                Editor editor = pref.edit();
                editor.clear();
                editor.commit();
                break;
            }
        }

        pref = getPref();
        int referenceWidth = pref.getInt("referenceWidth", touchMapWidth);
        int referenceHeight = pref.getInt("referenceHeight", touchMapHeight);
        float offsetX = 0f;
        float correctionX = 1f;
        float offsetY = 0f;
        float correctionY = 1f;
        if (referenceWidth != touchMapWidth
                || referenceHeight != touchMapHeight) {
            float savedAspect = (float) referenceWidth / referenceHeight;
            float currentAspect = (float) touchMapWidth / touchMapHeight;

            if (Math.abs(savedAspect - currentAspect) > 0.001f) {

                if (currentAspect >= 2.0f || currentAspect <= 0.5f) {
                    if (currentAspect >= 2.0f) {
                        float swo = savedAspect * touchMapHeight;
                        offsetX = (touchMapWidth - swo) / 2;
                        correctionX = (swo / touchMapWidth);
                    } else {
                        float swh = touchMapWidth / savedAspect;
                        offsetY = (touchMapHeight - swh) / 2;
                        correctionY = (swh / touchMapHeight);
                    }
                } else {
                    pref.edit().clear();
                    pref.edit().commit();
                }
            }
        }

        if (pref.getAll().isEmpty()) {
            Log.i(TAG, "neni ulozene nastaveni");
            for (EditElement elem : editElements) {
                elem.computeBoundingBox();
                elem.computeOffsets();
            }
            return false;
        } else {
            boolean isNew = false;

            boolean failure = false;
            Map<Rect, Rect> loadedPositions = new HashMap<Rect, Rect>();
            Rect screenRect = new Rect(0, 0, touchMapWidth, touchMapHeight);
            for (int i = 0; i < btns.size(); i++) {
                View btn = btns.get(i);
                int id = btnIdMap.indexOf(btn.getId());
                String s = pref.getString("" + id, "");
                if (!s.equals("")) {
                    String[] sa = s.split("-");
                    Rect bb = boundingBoxs[ridToIdxMap.get(btn.getId())];
                    int savedLeft = 0;
                    int savedTop = 0;
                    int savedRight = 0;
                    int savedBottom = 0;

                    try {
                        savedLeft = Integer.parseInt(sa[0]);
                        savedTop = Integer.parseInt(sa[1]);
                        savedRight = Integer.parseInt(sa[2]);
                        savedBottom = Integer.parseInt(sa[3]);
                    } catch (Exception e) {
                        failure = true;
                    }
                    int left = (int) (offsetX + correctionX
                            * (savedLeft / (float) referenceWidth)
                            * touchMapWidth);
                    int top = (int) (offsetY + correctionY
                            * (savedTop / (float) referenceHeight)
                            * touchMapHeight);
                    int right = (int) (offsetX + correctionX
                            * (savedRight / (float) referenceWidth)
                            * touchMapWidth);
                    int bottom = (int) (offsetY + correctionY
                            * (savedBottom / (float) referenceHeight)
                            * touchMapHeight);

                    Rect positionRect = new Rect(left, top, right, bottom);
                    if ((touchMapWidth > 0 && !screenRect
                            .contains(positionRect))
                            || positionRect.width() <= 0
                            || positionRect.height() <= 0) {
                        failure = true;
                    }

                    if (bb.left != left || bb.top != top || bb.right != right
                            || bb.bottom != bottom) {
                        loadedPositions.put(bb, positionRect);
                        Log.i(TAG, hashCode() + " detect change layout");

                        isNew = true;
                    }
                }
            }

            if (!failure) {
                for (Rect key : loadedPositions.keySet()) {
                    key.set(loadedPositions.get(key));
                }
                for (EditElement elem : editElements) {
                    elem.computeBoundingBox();
                    elem.computeOffsets();
                }

                Log.i(TAG, hashCode() + " isNew:" + isNew + " " + btns.size()
                        + " " + boundingBoxs);

                checkFastForwardButton();
                return isNew;

            } else {
                return false;
            }

        }

    }

    private boolean staticDPADEnabled = true;

    
    private void checkFastForwardButton() {
        if (boundingBoxs != null) {
            int idx = ridToIdxMap.get(R.id.button_fast_forward);
            Rect ff_bb = boundingBoxs[idx];

            Log.i(TAG, "fast forward btn " + idx + " rect " + ff_bb);
            for (Rect bb2 : boundingBoxs) {
                if (ff_bb != bb2 && Rect.intersects(ff_bb, bb2)) {
                    Log.i(TAG, "colision with " + bb2);

                    int w = getMeasuredWidth();
                    int h = getMeasuredHeight();

                    boolean wrongPosition = false;
                    for (int i = 0; i < 300; i++) {
                        wrongPosition = false;
                        ff_bb.offset(10, 0);
                        if (ff_bb.right >= w) {
                            ff_bb.offsetTo(0, ff_bb.top + 10);
                            if (ff_bb.bottom >= h) {
                                break;
                            }
                        }

                        Log.i(TAG, i + " new rect " + ff_bb);
                        for (Rect bb3 : boundingBoxs) {
                            if (ff_bb != bb3 && Rect.intersects(ff_bb, bb3)) {
                                Log.i(TAG, "colision with " + bb3);
                                wrongPosition = true;
                                break;
                            }
                        }
                        if (!wrongPosition) {
                            break;
                        }
                    }
                    if (wrongPosition) {
                        Log.i(TAG, "Nepodarilo se najit vhodnou pozici");
                        resetEditElement();
                    } else {
                        Log.i(TAG,
                                "Podarilo se najit vhodnou pozici "
                                        + ff_bb
                                        + " "
                                        + boundingBoxs[btnIdMap
                                        .indexOf(R.id.button_fast_forward)]);
                        for (EditElement elem : editElements) {
                            elem.computeBoundingBox();
                            elem.computeOffsets();
                        }
                    }

                }

            }
        }
    }

    
    public void setEnableStaticDPAD(boolean isEnable) {
        staticDPADEnabled = isEnable;
        for (View btn : btns) {
            if (dpadRIDs.contains(btn.getId())) {
                btn.setVisibility(isEnable ? View.VISIBLE : View.INVISIBLE);
                btn.setEnabled(isEnable);
            }
        }
        invalidate();
    }

}
