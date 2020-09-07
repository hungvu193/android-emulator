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

package com.nostalgiaemulators.framework.ui.cheats;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.remote.ControllableActivity;
import com.nostalgiaemulators.framework.ui.tipsdialog.HelpDialog;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.FontUtil;

public class CheatsActivity extends ControllableActivity {

	public static final String EXTRA_IN_GAME_HASH = "EXTRA_IN_GAME_HASH";

	private ListView list;
	private CheatsListAdapter adapter;
	private Typeface font;
	private String gameHash;
	private ArrayList<Cheat> cheats;
	Button save;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cheats);
		font = FontUtil.createFontFace(this);

		gameHash = getIntent().getStringExtra(EXTRA_IN_GAME_HASH);

		list = (ListView) findViewById(R.id.act_cheats_list);
		cheats = Cheat.getAllCheats(this, gameHash);

		adapter = new CheatsListAdapter(this, cheats, font);
		list.setAdapter(adapter);

		TextView label = (TextView) findViewById(R.id.act_cheats_label);
		label.setTypeface(font);

		ImageButton back = (ImageButton) findViewById(R.id.act_cheats_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		ImageButton add = (ImageButton) findViewById(R.id.act_cheats_add);
		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openCheatDetailDialog(null);
			}
		});

		ImageButton help = (ImageButton) findViewById(R.id.act_cheats_help);
		help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showHelpDialog();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void openCheatDetailDialog(final Cheat cheat) {

		final Dialog dialog = new Dialog(this, R.style.DialogTheme);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.dialog_new_cheat, null);

		Toast.makeText(
				this,
				"For a multi-line code, use a plus sign (+) to separate each line",
				Toast.LENGTH_LONG).show();

		dialog.setContentView(content);

		final EditText chars = (EditText) content
				.findViewById(R.id.dialog_new_cheat_chars);
		final EditText desc = (EditText) content
				.findViewById(R.id.dialog_new_cheat_desc);
		TextView title = (TextView) content
				.findViewById(R.id.dialog_new_cheat_label);
		save = (Button) content.findViewById(R.id.dialog_new_cheat_save);

		title.setTypeface(font);
		chars.setTypeface(font);
		desc.setTypeface(font);
		save.setTypeface(font);

		if (cheat != null) {
			chars.setText(cheat.chars);
			desc.setText(cheat.desc);

		}

		if (chars.getText().toString().equals("")) {
			save.setEnabled(false);
		}

		chars.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@SuppressLint({ "NewApi", "DefaultLocale" })
			@Override
			public void afterTextChanged(Editable arg0) {
				String s = arg0.toString();
				Locale locale = Locale.getDefault();
				if (!s.equals(s.toUpperCase(locale))) {
					s = s.toUpperCase(locale);
					chars.setSelection(s.length());
				}

				if (Build.VERSION.SDK_INT > 8) {
					String newText = s.replaceAll(
							"\\p{InCombiningDiacriticalMarks}+", "");
					if (!newText.equals(s)) {
						chars.setText(newText);
						chars.setSelection(newText.length());
					}
					s = newText;
				}

				String newText = s.replaceAll(EmulatorInfoHolder.getInfo()
						.getCheatInvalidCharsRegex(), "");
				if (!newText.equals(s)) {
					chars.setText(newText);
					chars.setSelection(newText.length());
				}

				if (newText.equals("")) {
					save.setEnabled(false);
				} else {
					save.setEnabled(true);
				}

			}
		});

		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cheat == null) {
					cheats.add(new Cheat(chars.getText().toString(), desc
							.getText().toString(), true));
				} else {
					cheat.chars = chars.getText().toString();
					cheat.desc = desc.getText().toString();
				}

				adapter.notifyDataSetChanged();
				Cheat.saveCheats(CheatsActivity.this, gameHash, cheats);
				dialog.cancel();
			}
		});

		dialog.show();
	}

	public void removeCheat(Cheat cheat) {
		cheats.remove(cheat);
		adapter.notifyDataSetChanged();
		Cheat.saveCheats(CheatsActivity.this, gameHash, cheats);
	}

	public void editCheat(Cheat cheat) {
		openCheatDetailDialog(cheat);
	}

	public void saveCheats() {
		Cheat.saveCheats(CheatsActivity.this, gameHash, cheats);
	}

	private HelpDialog helpDialog = null;

	protected void showHelpDialog() {
		if (helpDialog == null) {
			helpDialog = HelpDialog.create(this,
					((EmulatorApplication) getApplication()).getCheatHelpIds());
		}
		if (!helpDialog.isShowing())
			DialogUtils.show(helpDialog, true);
	}

}
