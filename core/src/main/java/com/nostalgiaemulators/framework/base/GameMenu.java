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

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.FontUtil;
import com.nostalgiaemulators.framework.utils.Utils;

public class GameMenu {

	public class GameMenuItem {
		String title = "";
		int id;
		int iconRID = -1;
		boolean enable = true;
		boolean visible = true;

		public String getTitle() {
			return title;
		}

		public int getId() {
			return id;
		}

		public void setEnable(boolean en) {
			enable = en;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public void set(String title, int id) {
			this.title = title;
			this.id = id;
		}
	}

	public interface OnGameMenuListener {

		
		public void onGameMenuCreate(GameMenu menu);

		
		public void onGameMenuPrepare(GameMenu menu);

		
		public void onGameMenuOpened(GameMenu menu);

		
		public void onGameMenuClosed(GameMenu menu);

		public void onGameMenuItemSelected(GameMenu menu, GameMenuItem item);
	}

	public GameMenuItem findGameMenuItem(int id) {
		for (GameMenuItem item : items) {
			if (item.id == id) {
				return item;
			}
		}
		return null;
	}

	ArrayList<GameMenuItem> items = new ArrayList<GameMenu.GameMenuItem>();

	Activity context;
	OnGameMenuListener listener;
	Typeface font;
	LayoutInflater inflater;

	public GameMenu(Activity context, OnGameMenuListener listener) {
		this.context = context;
		this.listener = listener;
		font = FontUtil.createFontFace(context);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		listener.onGameMenuCreate(this);
	}

	public GameMenuItem add(String label, int iconRID) {
		GameMenuItem item = new GameMenuItem();
		item.id = items.size();
		item.title = label;
		item.iconRID = iconRID;
		items.add(item);
		return item;
	}

	public GameMenuItem add(String label) {
		return add(label, -1);
	}

	public GameMenuItem add(int labelRID) {
		GameMenuItem item = add((String) context.getText(labelRID), -1);
		item.id = labelRID;
		return item;
	}

	public GameMenuItem add(int labelRID, int iconRID) {
		GameMenuItem item = add((String) context.getText(labelRID), iconRID);
		item.id = labelRID;
		return item;
	}

	private Dialog dialog = null;

	public void dismiss() {
		DialogUtils.dismiss(dialog);
	}

	public boolean isOpen() {
		return dialog != null && dialog.isShowing();
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void open(boolean showAds) {
		if (isOpen()) {
			Log.w("gamemenu", "already showing");
			return;
		}
		DialogUtils.dismiss(dialog);

		dialog = new Dialog(context, R.style.GameDialogTheme);

		listener.onGameMenuPrepare(this);

		RelativeLayout surroundContainer = (RelativeLayout) inflater.inflate(
				R.layout.game_menu_surround, null);
		surroundContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialog != null)
					dialog.cancel();
			}
		});

		LinearLayout container = (LinearLayout) surroundContainer
				.findViewById(R.id.game_menu_container);
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) container
				.getLayoutParams();

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = (int) Utils.getDisplayWidth(display);

		int px = width / 10;

		params.setMargins(px, 0, px, 0);
		container.setLayoutParams(params);

		int padding = context.getResources().getDimensionPixelSize(
				R.dimen.dialog_bck_pading);
		container.setPadding(padding, padding, padding, padding);

		int margin = context.getResources().getDimensionPixelSize(
				R.dimen.dilog_button_margin);
		boolean landsacpe = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).visible) {
				continue;
			}
			if (landsacpe) {

				LayoutParams pp = new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT, 1);
				pp.gravity = Gravity.CENTER_VERTICAL;

				LinearLayout menuRow = new LinearLayout(context);
				GameMenuItem item = items.get(i);
				menuRow.addView(createButton(item, margin, dialog), pp);
				i++;
				if (i < items.size()) {

					LinearLayout lineSeparator = new LinearLayout(context);
					lineSeparator.setBackgroundColor(0xffffffff);
					menuRow.addView(lineSeparator, 1, LayoutParams.FILL_PARENT);
					GameMenuItem item2 = items.get(i);

					menuRow.addView(createButton(item2, margin, dialog), pp);
				}
				container.addView(menuRow);
			} else {
				GameMenuItem item = items.get(i);
				container.addView(createButton(item, margin, dialog),
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			}

			if (i < (items.size() - 1)) {
				LinearLayout linSeperator = new LinearLayout(context);
				linSeperator.setBackgroundColor(0xffffffff);
				container.addView(linSeperator, LayoutParams.FILL_PARENT, 1);
			}
		}

		dialog.setContentView(surroundContainer);

		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface d) {
				if (dialog != null) {
					listener.onGameMenuClosed(GameMenu.this);
					dialog = null;
				}
			}
		});

		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface d) {
				if (dialog != null) {
					listener.onGameMenuClosed(GameMenu.this);
					dialog = null;
				}
			}
		});

		if (showAds) {
		}

		DialogUtils.show(dialog, true);
		listener.onGameMenuOpened(this);
	}

	private View createButton(final GameMenuItem item, int margin,
			final Dialog dialog) {

		View view = inflater.inflate(R.layout.game_menu_item, null);

		TextView label = (TextView) view
				.findViewById(R.id.game_menu_item_label);
		label.setTypeface(font);
		label.setText(item.getTitle());

		ImageView iconView = (ImageView) view
				.findViewById(R.id.game_menu_item_icon);

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialog != null && dialog.isShowing()) {
					listener.onGameMenuItemSelected(GameMenu.this, item);
					DialogUtils.dismiss(dialog);
				}

			}
		});
		int iconRID = item.iconRID;

		if (iconRID > 0) {
			iconView.setImageResource(iconRID);
		}
		view.setFocusable(true);
		view.setEnabled(item.enable);
		label.setEnabled(item.enable);

		return view;
	}

	public GameMenuItem getItem(int id) {
		for (GameMenuItem item : items) {
			if (item.id == id) {
				return item;
			}
		}
		return null;
	}

}
