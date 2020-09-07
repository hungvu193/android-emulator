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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.SlotInfo;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.base.SlotUtils;
import com.nostalgiaemulators.framework.remote.ControllableActivity;
import com.nostalgiaemulators.framework.ui.tipsdialog.HelpDialog;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.FontUtil;
import com.nostalgiaemulators.framework.utils.Log;
import com.u1aryz.android.lib.newpopupmenu.MenuItem;
import com.u1aryz.android.lib.newpopupmenu.PopupMenu;
import com.u1aryz.android.lib.newpopupmenu.PopupMenu.OnItemSelectedListener;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

public class SlotSelectionActivity extends ControllableActivity {
	private static final String TAG = "com.nostalgiaemulators.framework.ui.gamegallery.SlotSelectionActivity";

	Typeface font = null;
	View[] slots = new View[8];

	public static final String EXTRA_GAME = "EXTRA_GAME";
	public static final String EXTRA_BASE_DIRECTORY = "EXTRA_BASE_DIR";
	public static final String EXTRA_SLOT = "EXTRA_SLOT";
	public static final String EXTRA_DIALOG_TYPE_INT = "EXTRA_DIALOG_TYPE_INT";
	public static final int DIALOAG_TYPE_LOAD = 1;
	public static final int DIALOAG_TYPE_SAVE = 2;

	private final static int SEND_SLOT = 0;
	private final static int REMOVE_SLOT = 1;


	Drawable clearIcon, sendIcon;

	private void initSlot(final SlotInfo slotInfo, final int idx,
			final String labelS, String messageS, final String dateS,
			final String timeS) {
		final View slotView = slots[idx];
		final boolean isUsed = slotInfo.isUsed;
		Bitmap screenshotBitmap = slotInfo.screenShot;

		TextView label = (TextView) slotView.findViewById(R.id.row_slot_label);
		final TextView message = (TextView) slotView
				.findViewById(R.id.row_slot_message);
		TextView date = (TextView) slotView.findViewById(R.id.row_slot_date);
		TextView time = (TextView) slotView.findViewById(R.id.row_slot_time);
		final ImageView screenshot = (ImageView) slotView
				.findViewById(R.id.row_slot_screenshot);

		label.setText(labelS);
		label.setTypeface(font);
		message.setText(messageS);
		message.setTypeface(font);
		date.setText(dateS);
		date.setTypeface(font);
		time.setText(timeS);
		time.setTypeface(font);

		final View.OnClickListener afterEraseClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSelected(game, idx + 1, false);
			}
		};
		slotView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSelected(game, idx + 1, isUsed);
			}
		});

		final Activity context = this;
		if (isUsed) {
			slotView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					PopupMenu menu = new PopupMenu(SlotSelectionActivity.this);
					menu.setHeaderTitle(labelS);
					menu.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(MenuItem item) {
							if (item.getItemId() == REMOVE_SLOT) {
								String path = slotInfo.path;
								File savestate = new File(path);
								String fn = savestate.getAbsolutePath();

								int index = fn.lastIndexOf(".");
								savestate.delete();

								if (index > 0) {
									fn = fn.substring(0, index);
									File screenshot = new File(fn + ".png");
									screenshot.delete();
								}
								slotView.setOnLongClickListener(null);
								slotView.setOnClickListener(afterEraseClickListener);
								message.setText("EMPTY");
								message.setVisibility(View.VISIBLE);
								screenshot
										.setImageResource(android.R.color.transparent);
							}
							if (item.getItemId() == SEND_SLOT) {
								String name = game.getCleanName();

								String msgFormat = getResources()
										.getString(
												com.nostalgiaemulators.framework.R.string.send_slot_message);
								String message = String
										.format(msgFormat,
												name,
												dateS,
												timeS,
												EmulatorInfoHolder.getInfo()
														.getName(),
												((EmulatorApplication) getApplication())
														.getStoreUrl());

								String subjectFormat = getResources()
										.getString(
												com.nostalgiaemulators.framework.R.string.send_slot_subject);
								String subject = String.format(subjectFormat,
										name, EmulatorInfoHolder.getInfo()
												.getName());

								try {
									File tempFile = SlotUtils.createPackFile(
											SlotSelectionActivity.this,
											slotInfo, game);


									Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", tempFile);
									Intent intent = ShareCompat.IntentBuilder.from(context)
											.setStream(uri)
											.setType("application/octet-stream")
											.getIntent()
											.putExtra(Intent.EXTRA_TEXT, message)
											.putExtra(Intent.EXTRA_SUBJECT, subject)
											.setAction(Intent.ACTION_SEND)
											.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);



									startActivity(Intent.createChooser(
											intent,
											getResources().getString(
													R.string.send_slot)));
								} catch (Exception e) {
									Log.e(TAG, "", e);
								}

							}
						}
					});

					menu.add(REMOVE_SLOT, R.string.act_slot_popup_menu_delete)
							.setIcon(clearIcon);
					menu.add(SEND_SLOT, R.string.act_slot_popup_menu_send)
							.setIcon(sendIcon);

					menu.show(slotView);
					return true;
				}
			});
		}
		if (screenshotBitmap != null) {
			screenshot.setImageBitmap(screenshotBitmap);
			message.setVisibility(View.INVISIBLE);
		}

	}

	private void onSelected(GameDescription game, int slot, boolean isUsed) {
		if (type == DIALOAG_TYPE_LOAD && (!isUsed)) {
			return;
		}
		Intent data = new Intent();
		data.putExtra(EXTRA_GAME, game);
		data.putExtra(EXTRA_SLOT, slot);
		setResult(RESULT_OK, data);
		finish();
	}

	GameDescription game;
	int type;
	int loadFocusIdx = 0;
	int saveFocusIdx = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		clearIcon = getResources().getDrawable(R.drawable.ic_clear_slot);
		sendIcon = getResources().getDrawable(R.drawable.ic_send_slot);

		game = (GameDescription) getIntent().getSerializableExtra(EXTRA_GAME);
		String baseDir = getIntent().getStringExtra(EXTRA_BASE_DIRECTORY);

		List<SlotInfo> slotInfos = SlotUtils.getSlots(baseDir, game.checksum);

		font = FontUtil.createFontFace(this);
		setContentView(R.layout.activity_slot_selection);

		TextView labelTv = (TextView) findViewById(R.id.act_slot_label);
		type = getIntent()
				.getIntExtra(EXTRA_DIALOG_TYPE_INT, DIALOAG_TYPE_LOAD);

		labelTv.setText(type == DIALOAG_TYPE_LOAD ? "LOAD STATE" : "SAVE STATE");
		labelTv.setTypeface(font);

		ImageButton help = (ImageButton) findViewById(R.id.help_btn);
		help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showHelpDialog();
			}
		});

		slots[0] = findViewById(R.id.slot_0);
		slots[1] = findViewById(R.id.slot_1);
		slots[2] = findViewById(R.id.slot_2);
		slots[3] = findViewById(R.id.slot_3);
		slots[4] = findViewById(R.id.slot_4);
		slots[5] = findViewById(R.id.slot_5);
		slots[6] = findViewById(R.id.slot_6);
		slots[7] = findViewById(R.id.slot_7);

		java.text.DateFormat dateFormat = DateFormat.getDateFormat(this);
		java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
		Calendar dd = Calendar.getInstance();
		dd.set(1970, 10, 10);
		String emptyDate = dateFormat.format(dd.getTime());
		emptyDate = emptyDate.replace("7", "-");
		emptyDate = emptyDate.replace('0', '-');
		emptyDate = emptyDate.replace('1', '-');
		emptyDate = emptyDate.replace('9', '-');

		long focusTime = 0;
		saveFocusIdx = -1;

		for (int i = 0; i < SlotUtils.NUM_SLOTS; i++) {
			String message = "EMPTY";
			SlotInfo slotInfo = slotInfos.get(i);
			if (slotInfo.isUsed) {
				message = "USED";
			}
			String label = "SLOT  " + (i + 1);
			Date time = new Date(slotInfo.lastModified);

			String dateString = slotInfo.lastModified == -1 ? emptyDate
					: dateFormat.format(time);
			String timeString = slotInfo.lastModified == -1 ? "--:--"
					: timeFormat.format(time);

			initSlot(slotInfo, i, label, message, dateString, timeString);
			if (focusTime < slotInfo.lastModified) {
				loadFocusIdx = i;
				focusTime = slotInfo.lastModified;
			}

			if (!slotInfo.isUsed && saveFocusIdx == -1) {
				saveFocusIdx = i;
			}
		}

		if (loadFocusIdx < 0)
			loadFocusIdx = 0;
		if (saveFocusIdx < 0)
			saveFocusIdx = 0;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		super.onResume();

		
	}

	private HelpDialog helpDialog = null;

	protected void showHelpDialog() {
		if (helpDialog == null) {
			helpDialog = HelpDialog.create(this,
					((EmulatorApplication) getApplication()).getSlotHelpIds());
		}
		if (!helpDialog.isShowing())
			DialogUtils.show(helpDialog, true);
	}
}
