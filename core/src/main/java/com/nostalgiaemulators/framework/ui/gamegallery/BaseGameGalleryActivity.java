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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.Manifest;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorActivity;
import com.nostalgiaemulators.framework.remote.ControllableActivity;
import com.nostalgiaemulators.framework.ui.gamegallery.RomsFinder.OnRomsFinderListener;
import com.nostalgiaemulators.framework.ui.tipsdialog.HelpDialog;
import com.nostalgiaemulators.framework.utils.DatabaseHelper;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.FileUtils;
import com.nostalgiaemulators.framework.utils.Log;

abstract public class BaseGameGalleryActivity extends ControllableActivity
		implements OnRomsFinderListener {

	public static final String MAIN_INDEX_FILE = "main_index.txt";

	protected static final int PERMISSIONS_REQUEST = 555;

	@SuppressWarnings("unused")
	private static final String TAG = "com.nostalgiaemulators.framework.ui.gamegallery.BaseGameGalleryActivity";

	protected Set<String> exts;
	protected Set<String> inZipExts;
	private RomsFinder romsFinder = null;
	protected boolean reloadGames = true;
	protected boolean reloading = false;
	private HelpDialog helpDialog = null;
	private String biosName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		HashSet<String> exts = new HashSet<String>(getRomExtensions());
		exts.addAll(getArchiveExtensions());

		biosName = getBiosName();

		SharedPreferences pref = getSharedPreferences("android50comp",
				Context.MODE_PRIVATE);
		String androidVersion = Build.VERSION.RELEASE;
		if (!pref.getString("androidVersion", "").equals(androidVersion)) {
			DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			try {
				dbHelper.onUpgrade(db, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
			} finally {
				db.close();
			}
			Editor editor = pref.edit();
			editor.putString("androidVersion", androidVersion);
			editor.commit();
			Log.i(TAG, "Reinit DB " + androidVersion);
		}

		reloadGames = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!FileUtils.isSDCardRWMounted()) {
			showSDcardFailed();
		}
		try {
			if (getExternalCacheDir() == null) {
				showSDcardFailed();
			}
		} catch (Exception e) {
			showSDcardFailed();
		}
	}

	protected void showHelpDialog() {
		if (helpDialog == null) {

			helpDialog = HelpDialog.create(this,
					((EmulatorApplication) getApplication())
							.getGalleryHelpIds());

		}
		if (!helpDialog.isShowing())
			DialogUtils.show(helpDialog, true);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (romsFinder != null) {
			romsFinder.stopSearch();
		}
	}

	protected void reloadGames(boolean searchNew, File selectedFolder) {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
						PERMISSIONS_REQUEST);

		}else {

			if (romsFinder == null) {
				reloadGames = false;
				reloading = searchNew;
				romsFinder = new RomsFinder(exts, inZipExts, this, this, searchNew,
						selectedFolder, biosName);
				romsFinder.start();
			}
		}
	}

	protected void importFromLite() {
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length == 0) {
			return;
		}
		if(requestCode == PERMISSIONS_REQUEST && grantResults[0]==PackageManager.PERMISSION_GRANTED){
			importFromLite();
		}

	}

	@Override
	public void onRomsFinderFoundGamesInCache(ArrayList<GameDescription> oldRoms) {
		setLastGames(oldRoms);
	}

	@Override
	public void onRomsFinderNewGames(ArrayList<GameDescription> roms) {
		setNewGames(roms);
	}

	@Override
	public void onRomsFinderEnd(boolean searchNew) {
		romsFinder = null;
		reloading = false;
	}

	@Override
	public void onRomsFinderCancel(boolean searchNew) {
		romsFinder = null;
		reloading = false;
	}

	protected void stopRomsFinding() {
		if (romsFinder != null) {
			romsFinder.stopSearch();
		}
	}

	public void showSDcardFailed() {
		showError(getResources()
				.getString(R.string.gallery_sd_card_not_mounted));

	}

	protected void showError(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Builder builder = new Builder(BaseGameGalleryActivity.this);

				builder.setTitle(R.string.error);
				builder.setMessage(message);
				builder.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
				builder.setPositiveButton(R.string.exit, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

				DialogUtils.show(builder.create(), true);
			}
		});
	}

	
	public abstract Class<? extends EmulatorActivity> getEmulatorActivityClass();

	abstract public void setLastGames(ArrayList<GameDescription> games);

	abstract public void setNewGames(ArrayList<GameDescription> games);

	abstract protected Set<String> getRomExtensions();

	protected String getBiosName() {
		return null;
	}

	public abstract Emulator getEmulatorInstance();

	
	final public Dialog getAboutDialog() {
		Dialog dialog = new Dialog(this);
		dialog.setTitle(R.string.about);
		WebView webView = new WebView(this);
		String text = getText(R.string.about_header).toString();
		String versionName = null;

		int versionCode = -1;
		try {
			versionName = getPackageManager().getPackageInfo(getString(R.string.flavour_package),
					0).versionName;
			versionCode = getPackageManager().getPackageInfo(getPackageName(),
					0).versionCode;

		} catch (NameNotFoundException e) {
			Log.e(TAG, "", e);
		}

		text = text.replace("|app_name|", getText(R.string.app_name));

		text = text.replace("|build|", versionCode == -1 ? "" : versionName
				+ "(" + versionCode + ")");

		String license = getPackageName().contains("gba") ? "" : "(<a href='https://www.gnu.org/licenses/gpl-3.0.html'>license</a>)";
		text = text.replace("|licence|", license);
		text = text.replace("|content|", getText(R.string.about_content));
		webView.loadData(text, "text/html; charset=UTF-8", null);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view,
					String url) {
				WebResourceResponse response = super.shouldInterceptRequest(
						view, url);
				if (url.endsWith("")) {
					try {
						String asset = "yep.html";
						response = new WebResourceResponse("application/html",
								"UTF8", BaseGameGalleryActivity.this
										.getAssets().open(asset));
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				return response;
			}
		});

		dialog.addContentView(webView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		return dialog;
	}

	protected Set<String> getArchiveExtensions() {
		HashSet<String> set = new HashSet<String>();
		set.add("zip");
		return set;
	}

}
