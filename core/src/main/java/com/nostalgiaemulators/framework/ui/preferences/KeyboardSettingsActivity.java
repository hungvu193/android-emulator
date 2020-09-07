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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nostalgiaemulators.framework.KeyboardProfile;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.controllers.KeyboardController;
import com.nostalgiaemulators.framework.remote.ControllableActivity;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

public class KeyboardSettingsActivity extends ControllableActivity implements
		OnItemClickListener {

	private ListView list = null;
	private KeyboardProfile profile;

	
	private SparseIntArray inverseMap = new SparseIntArray();
	private ArrayList<String> profilesNames;
	private Adapter adapter;

	public static final String EXTRA_PROFILE_NAME = "EXTRA_PROFILE_NAME";
	public static final String EXTRA_NEW_BOOL = "EXTRA_NEW_BOOL";
	public static final int RESULT_NAME_CANCEL = 645943;

	private static final String TAG = "com.nostalgiaemulators.framework.ui.preferences.KeyboardSettingsActivity";

	private boolean newProfile = false;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.activity_keyboard_settings);

		profilesNames = KeyboardProfile.getProfilesNames(this);

		list = (ListView) findViewById(R.id.act_keyboard_settings_list);

		String profileToLoad = getIntent().getStringExtra(EXTRA_PROFILE_NAME);

		boolean isNew = getIntent().getBooleanExtra(EXTRA_NEW_BOOL, false);

		profile = KeyboardProfile.load(this, profileToLoad);

		inverseMap.clear();
		SparseIntArray keyMap = profile.keyMap;
		for (Integer code : KeyboardProfile.getButtonKeyEventCodes()) {
			inverseMap.append(code, 0);
		}
		for (int i = 0; i < keyMap.size(); i++) {
			inverseMap.append(keyMap.valueAt(i), keyMap.keyAt(i));
		}

		if (isNew) {
			profile.name = NEW_PROFILE;
			newProfile = true;
			showDialog(0);
		}

		setTitle(String.format(getText(R.string.key_profile_pref).toString(),
				profile.name));

		adapter = new Adapter();
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		final PlayersLabelView plv = (PlayersLabelView) findViewById(R.id.act_keyboard_settings_plv);
		if (EmulatorInfoHolder.getInfo().isMultiPlayerSupported()) {
			plv.setPlayersOffsets(adapter.getPlayersOffset());

			list.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					View v = list.getChildAt(0);
					if (v != null) {
						int currentY = 0;
						for (int i = 0; i < list.getFirstVisiblePosition(); i++) {

							currentY += adapter.getRowHeight();
						}

						int scrollY = -list.getChildAt(0).getTop() + currentY;

						plv.setOffset(scrollY);

					}
				}
			});
		} else {
			plv.setVisibility(View.GONE);
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		final EditText editText = new EditText(this);
		editText.setHint("Insert profile name");
		editText.setPadding(10, 10, 10, 10);
		alertDialogBuilder.setView(editText);
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						profile.name = editText.getText().toString();
						setTitle(String.format(
								getText(R.string.key_profile_pref).toString(),
								profile.name));

						Intent data = new Intent();
						data.putExtra(EXTRA_PROFILE_NAME, profile.name);
						setResult(RESULT_OK, data);
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								setResult(RESULT_NAME_CANCEL);
								finish();
							}
						});
		final AlertDialog alertDialog = alertDialogBuilder.create();
		final Pattern pattern = Pattern.compile("[a-zA-Z0-9]");

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Button ok = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				String txt = s.toString();
				Matcher m = pattern.matcher(txt);
				if (!profilesNames.contains(txt) && !txt.equals("")
						&& m.replaceAll("").length() == 0) {
					ok.setEnabled(true);
				} else {
					ok.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		alertDialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				Button ok = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				ok.setEnabled(false);
			}
		});
		return alertDialog;

	}

	private class Adapter extends BaseAdapter {

		LayoutInflater inflater;

		public Adapter() {
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_keyboard_settings,
						null);
			}

			TextView name = (TextView) convertView
					.findViewById(R.id.row_keyboard_name);
			TextView desc = (TextView) convertView
					.findViewById(R.id.row_keyboard_desc);
			TextView keyName = (TextView) convertView
					.findViewById(R.id.row_keyboard_key_name);
			convertView.setEnabled(true);

			boolean isAdVersion = Utils.isAdvertisingVersion(KeyboardSettingsActivity.this);
			if (position < KeyboardProfile.getButtonNames().length) {

				String nameStr = KeyboardProfile.getButtonNames()[position];
				if (isAdVersion && nameStr.contains("REWIND")) {
					nameStr += " (Pro Version Only)";
					name.setEnabled(false);
				} else {
					name.setEnabled(true);
				}
				name.setText(nameStr);
				int keyCode = inverseMap.get(KeyboardProfile
						.getButtonKeyEventCodes()[position]);
				if (keyCode >= KeyboardController.PLAYER2_OFFSET) {
					keyCode -= KeyboardController.PLAYER2_OFFSET;
				}

				String label = getKeyLabel(keyCode);
				keyName.setText(label);
				keyName.setVisibility(View.VISIBLE);

			} else {

				name.setText(KeyboardProfile.isDefaultProfile(profile.name) ? getText(R.string.pref_keyboard_settings_restore_def)
						: getText(R.string.pref_keyboard_settings_delete_prof));
				desc.setVisibility(View.GONE);
				keyName.setVisibility(View.GONE);
			}
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return KeyboardProfile.getButtonNames().length
					+ (newProfile ? 0 : 1);
		}

		private int heightCache = -1;

		public int getRowHeight() {
			if (heightCache < 0) {
				View convertView = inflater.inflate(
						R.layout.row_keyboard_settings, null);
				convertView
						.measure(MeasureSpec.makeMeasureSpec(0,
								MeasureSpec.UNSPECIFIED), MeasureSpec
								.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				heightCache = convertView.getMeasuredHeight();
			}

			return heightCache;
		}

		public int[] getPlayersOffset() {
			ArrayList<Integer> result = new ArrayList<Integer>();
			String lastDesc = "";
			int h = getRowHeight();

			for (int i = 0; i < KeyboardProfile.getButtonNames().length; i++) {
				String desc = KeyboardProfile.getButtonDescriptions()[i];
				if (!lastDesc.equals(desc)) {
					result.add(i * h);
					lastDesc = desc;
				}
			}
			int[] res = new int[result.size()];
			int i = 0;
			for (i = 0; i < result.size(); i++)
				res[i] = result.get(i);
			return res;
		}

	};

	private static SparseArray<String> NON_PRINTABLE_KEY_LABELS = new SparseArray<String>();

	static {
		initNonPrintMap();
	}

	@SuppressLint("InlinedApi")
	private static void initNonPrintMap() {
		NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_ENTER, "Enter");
		NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_SPACE, "Space");
		NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_DPAD_LEFT, "Left");
		NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_DPAD_RIGHT, "Right");
		NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_DPAD_UP, "Up");
		NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_DPAD_DOWN, "Down");

		NON_PRINTABLE_KEY_LABELS.put(KeyboardController.KEY_XPERIA_CIRCLE,
				"Circle");
		if (Build.VERSION.SDK_INT > 8) {

			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_A, "A");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_B, "B");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_C, "C");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_X, "X");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_Y, "Y");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_Z, "Z");

			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_SELECT,
					"Select");
			NON_PRINTABLE_KEY_LABELS
					.put(KeyEvent.KEYCODE_BUTTON_START, "Start");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_MODE, "MODE");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_THUMBL,
					"THUMBL");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_THUMBR,
					"THUMBR");

			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_1, "1");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_2, "2");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_3, "3");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_4, "4");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_5, "5");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_6, "6");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_7, "7");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_8, "8");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_9, "9");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_10, "10");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_11, "11");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_12, "12");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_13, "13");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_14, "14");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_15, "15");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_16, "16");

			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_R1, "R1");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_R2, "R2");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_L1, "L1");
			NON_PRINTABLE_KEY_LABELS.put(KeyEvent.KEYCODE_BUTTON_L2, "L2");
		}
	}

	public static String getKeyLabel(int keyCode) {

		if (keyCode == 0) {
			return "";
		}
		String text = NON_PRINTABLE_KEY_LABELS.get(keyCode);
		if (text != null) {
			return text;
		} else {
			KeyEvent event = new KeyEvent(0, keyCode);
			char ch = (char) event.getUnicodeChar();
			if (ch != 0) {
				return ch + "";
			} else {
				return "key-" + keyCode;
			}
		}
	}

	private boolean deleted = false;

	@Override
	protected void onResume() {
		super.onResume();
		deleted = false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
			long arg3) {

		if (position == KeyboardProfile.getButtonNames().length) {
			if (KeyboardProfile.isDefaultProfile(profile.name)) {
				KeyboardProfile.restoreDefaultProfile(profile.name, this);
			} else {
				profile.delete(this);
			}
			deleted = true;
			finish();
		} else {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(String.format(
					getResources().getString(R.string.press_key),
					KeyboardProfile.getButtonNames()[position]));
			builder.setNegativeButton("Cancel", null);
			builder.setNeutralButton("Clear",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							proccessKeyEvent("", dialog, -1, position);
						}
					});
			final AlertDialog d = builder.create();

			d.setOnShowListener(new DialogInterface.OnShowListener() {

				@Override
				public void onShow(DialogInterface dialog) {

					Button negative = d.getButton(AlertDialog.BUTTON_NEGATIVE);
					negative.setFocusable(false);
					negative.setFocusableInTouchMode(false);

					Button neutral = d.getButton(AlertDialog.BUTTON_NEUTRAL);
					neutral.setFocusable(false);
					neutral.setFocusableInTouchMode(false);

				}
			});

			d.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						if (event.isAltPressed()) {
							keyCode = KeyboardController.KEY_XPERIA_CIRCLE;
						}
						if (android.os.Build.VERSION.SDK_INT >= 9) {
							if ((event.getSource() & 1025) == 1025) {
								keyCode = KeyEvent.KEYCODE_BUTTON_SELECT;
							}
						}

					}

					String txt = getKeyLabel(keyCode);
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						return proccessKeyEvent(txt, dialog, keyCode, position);
					} else {
						return false;
					}
				}
			});

			d.show();
		}

	}

	private boolean proccessKeyEvent(String txt, DialogInterface dialog,
			int keyCode, int position) {
		Log.i(TAG, "txt:" + txt);

		if ((!txt.equals("") || keyCode == -1)
				&& keyCode != KeyEvent.KEYCODE_BACK) {
			int player = KeyboardProfile.getButtonDescriptions()[position]
					.equals(KeyboardProfile.getButtonDescriptions()[0]) ? 0 : 1;
			if (player == 1 && keyCode != -1) {
				keyCode += KeyboardController.PLAYER2_OFFSET;
			}

			if (keyCode != -1) {
				int previousPosition = inverseMap.indexOfValue(keyCode);
				if (previousPosition >= 0) {
					inverseMap.put(inverseMap.keyAt(previousPosition), 0);
				}
			}

			if (keyCode == -1) {
				inverseMap.put(
						KeyboardProfile.getButtonKeyEventCodes()[position], 0);

			} else {
				inverseMap.append(
						KeyboardProfile.getButtonKeyEventCodes()[position],
						keyCode);
			}
			Log.i(TAG, "isert " + KeyboardProfile.getButtonNames()[position]
					+ " :" + keyCode);
			adapter.notifyDataSetChanged();
			try {
				dialog.dismiss();
			} catch (Exception e) {
			}
			return true;
		} else {
			return false;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (!profile.name.equals(NEW_PROFILE) && !deleted) {
			profile.keyMap.clear();
			for (int i = 0; i < inverseMap.size(); i++) {
				profile.keyMap.append(inverseMap.valueAt(i),
						inverseMap.keyAt(i));
			}
			profile.save(this);
		}

	}

	final String NEW_PROFILE = "[new profile]";
}
