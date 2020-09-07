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
import java.util.zip.ZipException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.base.EmulatorUtils;
import com.nostalgiaemulators.framework.base.SlotUtils;
import com.nostalgiaemulators.framework.utils.DatabaseHelper;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

@SuppressWarnings("deprecation")
public abstract class SlotImportActivity extends Activity {

	private static final String TAG = "com.nostalgiaemulators.framework.ui.gamegallery.SlotImportActivity";
	private String gameHash = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Uri uri = intent.getData();
		String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {
			if (uri != null) {
				try {
					gameHash = SlotUtils.unpackFile(
							EmulatorUtils.getBaseDir(this), this, uri);

				} catch (Exception e) {
					Log.e(TAG, e.toString());
					showDialog(WRONG_FORMAT_DIALOG);
				}

			} else {
				Log.w(TAG, "Extras didn't contain data uri");
				finish();
			}
		} else {
			Log.w(TAG, "Wrong action:" + action);
			finish();
		}
	}

	boolean firstRun = true;

	@Override
	protected void onResume() {
		super.onResume();
		if (firstRun && gameHash != null) {
			FindTask findTask = new FindTask(gameHash);
			findTask.execute();
			firstRun = false;
		} else {
			finish();
		}
	}

	public static String getContentName(ContentResolver resolver, Uri uri) {
		Cursor cursor = resolver.query(uri, null, null, null, null);
		cursor.moveToFirst();
		try {
			int nameIndex = cursor
					.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
			if (nameIndex >= 0) {
				return cursor.getString(nameIndex);
			} else {
				return null;
			}
		} finally {
			cursor.close();
		}
	}

	private static final int PROGRESS_DIALOG = 1;
	private static final int NOTFOUND_DIALOG = 2;
	private static final int WRONG_FORMAT_DIALOG = 3;

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setIndeterminate(true);
			dialog.setMessage(getString(R.string.act_import_slot_search_label));
			dialog.setTitle(getString(R.string.act_import_slot_search_title));
			return dialog;
		case NOTFOUND_DIALOG: {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(getString(R.string.error));
			alertDialog
					.setMessage(getString(R.string.act_import_game_ot_found));
			alertDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
			});
			return alertDialog;
		}

		case WRONG_FORMAT_DIALOG: {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(getString(R.string.error));
			alertDialog.setMessage(getString(R.string.act_import_wrong_format));
			alertDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
			});
			return alertDialog;
		}

		default:
			return super.onCreateDialog(id);
		}
	}

	private class FindTask extends AsyncTask<Void, Void, GameDescription> {
		String gameHash;

		public FindTask(String gameHash) {
			this.gameHash = gameHash;
		}

		@Override
		protected GameDescription doInBackground(Void... params) {
			DatabaseHelper dbHelper = DatabaseHelper
					.getInstance(SlotImportActivity.this);

			GameDescription game;
			if (!gameHash.startsWith("V2_")) {
				game = GalleryActivity.findGameByOldHash(
						SlotImportActivity.this, gameHash);
			} else
			{
				String where = "where checksum=\"" + gameHash + "\"";
				game = dbHelper.selectObjFromDb(GameDescription.class, where);
			}

			if (game != null && game.isInArchive()) {
				File gameFile = new File(getExternalCacheDir(), game.checksum);
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
			return game;

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(PROGRESS_DIALOG);
		}

		@Override
		protected void onPostExecute(GameDescription game) {
			try {
				dismissDialog(PROGRESS_DIALOG);
			} catch (Exception e) {
			}
			if (game != null) {
				Log.i(TAG, "found game " + game.name);

				File gameFile = new File(game.path);

				if (gameFile.exists()) {
					Intent intent = new Intent(SlotImportActivity.this,
							getEmulatorActivityClass());
					intent.putExtra(EmulatorActivity.EXTRA_GAME, game);
					intent.putExtra(EmulatorActivity.EXTRA_SLOT, 0);
					startActivity(intent);
				} else {
					showDialog(NOTFOUND_DIALOG);
				}
			} else {
				showDialog(NOTFOUND_DIALOG);
			}
			super.onPostExecute(game);
		}

		@Override
		protected void onCancelled() {
			try {
				dismissDialog(PROGRESS_DIALOG);
			} catch (Exception e) {

			}
			super.onCancelled();
		}

	}

	public abstract Class<? extends EmulatorActivity> getEmulatorActivityClass();

	public abstract Emulator getEmulatorInstance();
}
