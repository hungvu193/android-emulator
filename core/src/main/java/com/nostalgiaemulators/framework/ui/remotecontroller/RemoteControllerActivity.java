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

package com.nostalgiaemulators.framework.ui.remotecontroller;

import java.net.InetAddress;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nostalgiaemulators.framework.EmulatorController;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorInfoHolder;
import com.nostalgiaemulators.framework.controllers.RemoteController;
import com.nostalgiaemulators.framework.remote.wifi.WifiControllerClient;
import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoReceiver;
import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoReceiver.BroadcastReceiverListener;
import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoReceiver.DetectionResult;
import com.nostalgiaemulators.framework.remote.wifi.WifiServerInfoTransmitter;
import com.nostalgiaemulators.framework.ui.gamegallery.GalleryActivity;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchBtnInterface;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchImageButton;
import com.nostalgiaemulators.framework.ui.multitouchbutton.MultitouchLayer;
import com.nostalgiaemulators.framework.ui.multitouchbutton.OnMultitouchEventListener;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.ActivitySwitcher;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.FontUtil;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

public class RemoteControllerActivity extends Activity {

	private static final String TAG = "com.nostalgiaemulators.framework.ui.remotecontroller.RemoteControllerActivity";

	SparseIntArray resToKeyCode = new SparseIntArray();
	Typeface font;
	TextView portIndicator;
	EditText searchBox;
	boolean searchMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		font = FontUtil.createFontFace(this);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		setContentView(R.layout.activity_remote_controller);
		resToKeyCode.put(R.id.button_left, EmulatorController.KEY_LEFT);
		resToKeyCode.put(R.id.button_right, EmulatorController.KEY_RIGHT);
		resToKeyCode.put(R.id.button_up, EmulatorController.KEY_UP);
		resToKeyCode.put(R.id.button_down, EmulatorController.KEY_DOWN);
		resToKeyCode.put(R.id.button_select, EmulatorController.KEY_SELECT);
		resToKeyCode.put(R.id.button_start, EmulatorController.KEY_START);
		resToKeyCode.put(R.id.button_a, EmulatorController.KEY_A);
		resToKeyCode.put(R.id.button_b, EmulatorController.KEY_B);
		resToKeyCode.put(R.id.button_back, RemoteController.KEY_BACK);
		resToKeyCode.put(R.id.button_menu, RemoteController.KEY_MENU);

		MultitouchLayer mtl =  findViewById(R.id.act_remote_mtl);
		mtl.disableLoadSettings();

		mtl.setVibrationDuration(PreferenceUtil.getVibrationDuration(this));

		MultitouchImageButton up =  findViewById(R.id.button_up);
		up.setOnMultitouchEventlistener(emulatorKeysListener);

		MultitouchImageButton down =  findViewById(R.id.button_down);
		down.setOnMultitouchEventlistener(emulatorKeysListener);

		MultitouchImageButton left =  findViewById(R.id.button_left);
		left.setOnMultitouchEventlistener(emulatorKeysListener);

		MultitouchImageButton right =  findViewById(R.id.button_right);
		right.setOnMultitouchEventlistener(emulatorKeysListener);

		MultitouchImageButton a =  findViewById(R.id.button_a);
		a.setOnMultitouchEventlistener(emulatorKeysListener);

		MultitouchImageButton b =  findViewById(R.id.button_b);
		b.setOnMultitouchEventlistener(emulatorKeysListener);

		MultitouchImageButton select =  findViewById(R.id.button_select);
		if (select != null) {
			select.setOnMultitouchEventlistener(emulatorKeysListener);
		}

		MultitouchImageButton l =  findViewById(R.id.button_l);
		if (l != null) {
			l.setOnMultitouchEventlistener(emulatorKeysListener);
			resToKeyCode.put(R.id.button_l, EmulatorController.KEY_L);
		}

		MultitouchImageButton r =  findViewById(R.id.button_r);
		if (r != null) {
			r.setOnMultitouchEventlistener(emulatorKeysListener);
			resToKeyCode.put(R.id.button_r, EmulatorController.KEY_R);
		}

		MultitouchImageButton start =  findViewById(R.id.button_start);
		start.setOnMultitouchEventlistener(emulatorKeysListener);

		MultitouchImageButton back =  findViewById(R.id.button_back);
		back.setOnMultitouchEventlistener(new OnMultitouchEventListener() {

			@Override
			public void onMultitouchEnter(MultitouchBtnInterface btn) {
				if (client != null) {
					client.sendControllerAndroidKeyEvent(new KeyEvent(
							KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				}
			}

			@Override
			public void onMultitouchExit(MultitouchBtnInterface btn) {
				if (client != null)
					client.sendControllerAndroidKeyEvent(new KeyEvent(
							KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
			}
		});

		MultitouchImageButton menu = findViewById(R.id.button_menu);
		menu.setOnMultitouchEventlistener(new OnMultitouchEventListener() {

			@Override
			public void onMultitouchEnter(MultitouchBtnInterface btn) {
				if (client != null) {
					client.sendControllerAndroidKeyEvent(new KeyEvent(
							KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
				}
			}

			@Override
			public void onMultitouchExit(MultitouchBtnInterface btn) {
				if (client != null)
					client.sendControllerAndroidKeyEvent(new KeyEvent(
							KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
			}
		});

		MultitouchImageButton search =  findViewById(R.id.button_search);
		search.setOnMultitouchEventlistener(new OnMultitouchEventListener() {
			@Override
			public void onMultitouchExit(MultitouchBtnInterface btn) {
			}

			@Override
			public void onMultitouchEnter(MultitouchBtnInterface btn) {
				((View) searchBox.getParent()).setVisibility(View.VISIBLE);
				searchBox.requestFocus();
			}
		});

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		ip = prefs.getString("IP", "10.0.0.5");
		setPort(prefs.getInt("port", 0));
		ActivitySwitcher.animationIn(findViewById(R.id.root),
				getWindowManager());

		MultitouchImageButton connect =  findViewById(R.id.button_connect);
		connect.setOnMultitouchEventlistener(new OnMultitouchEventListener() {
			@Override
			public void onMultitouchExit(MultitouchBtnInterface btn) {
				DialogUtils.dismiss(dialog);
				dialog = openSelectServerDialog();
			}

			@Override
			public void onMultitouchEnter(MultitouchBtnInterface btn) {
			}
		});

		portIndicator =  findViewById(R.id.port_indicator);
		portIndicator.setTypeface(font);

		searchBox =  findViewById(R.id.search_editbox);
		searchBox.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				searchBox.post(new Runnable() {
					@Override
					public void run() {
						InputMethodManager imm = (InputMethodManager) RemoteControllerActivity.this
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						if (hasFocus) {
							imm.showSoftInput(searchBox,
									InputMethodManager.SHOW_FORCED);
						} else {
							imm.hideSoftInputFromWindow(
									searchBox.getWindowToken(), 0);
						}
					}
				});
				if (!hasFocus) {
					((View) searchBox.getParent())
							.setVisibility(View.INVISIBLE);
				}
				searchMode = hasFocus;
				if (client != null) {
					client.sendControllerCommandEvent(
							GalleryActivity.COMMAND_SEARCHMODE, hasFocus ? 1
									: 0, 0);
				}
			}
		});

		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (client != null)
					client.sendControllerTextEvent(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		DialogUtils.dismiss(dialog);
		dialog = openSelectServerDialog();
	}

	Dialog dialog;

	@Override
	protected void onPause() {
		super.onPause();
		if (client != null) {
			client.onPause();
		}
		broadcastReceiver.stop();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (client != null) {
			client.onStop();
		}
		broadcastReceiver.stop();
		DialogUtils.dismiss(dialog);
	}

	OnMultitouchEventListener emulatorKeysListener = new OnMultitouchEventListener() {

		@Override
		public void onMultitouchEnter(MultitouchBtnInterface btn) {
			if (client != null) {
				client.sendControllerEmulatorKeyEvent(1,
						resToKeyCode.get(btn.getId()));
			}

		}

		@Override
		public void onMultitouchExit(MultitouchBtnInterface btn) {
			if (client != null)
				client.sendControllerEmulatorKeyEvent(0,
						resToKeyCode.get(btn.getId()));
		}
	};

	private WifiControllerClient client;

	String ip = "";
	int port = 0;
	WifiServerInfoReceiver broadcastReceiver = new WifiServerInfoReceiver();

	private Dialog openSelectServerDialog() {

		final Dialog dialog = new Dialog(this, R.style.DialogTheme);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.dialog_select_server, null);

		final ListView listView =  content
				.findViewById(R.id.dialog_select_server_list);
		final ArrayList<DetectionResult> values = new ArrayList<DetectionResult>();
		final ServerSelectAdapter listAdapter = new ServerSelectAdapter(this,
				values);
		listView.setAdapter(listAdapter);

		dialog.setContentView(content);

		TextView title =  content
				.findViewById(R.id.dialog_select_server_title);
		title.setTypeface(font);

		Button cancel =  content
				.findViewById(R.id.dialog_select_server_btn_cancel);
		cancel.setTypeface(font);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
				broadcastReceiver.stop();
				finish();
			}
		});

		Button manually =  content
				.findViewById(R.id.dialog_select_server_btn_manually);

		manually.setTypeface(font);
		manually.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogUtils.dismiss(dialog);
				openSelectIpDialog();
			}
		});
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				broadcastReceiver.stop();
				finish();
			}
		});
		dialog.show();
		broadcastReceiver.startExploring(RemoteControllerActivity.this,
				new BroadcastReceiverListener() {
					@Override
					public void onServerDetect(DetectionResult result) {
						int pos = values.indexOf(result);

						if (pos != -1) {
							values.set(pos, result);
						} else {
							values.add(result);
						}
						listAdapter.notifyDataSetChanged();
					}
				});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				DetectionResult result = values.get(arg2);

				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(RemoteControllerActivity.this);
				Editor editor = prefs.edit();
				Log.i(TAG, result.address.getHostAddress());
				ip = result.address.getHostAddress();
				editor.putString("IP", result.address.getHostAddress());
				editor.commit();
				broadcastReceiver.stop();
				DialogUtils.dismiss(dialog);
				openPortDialog();
			}
		});
		return dialog;
	}

	@Override
	public void onResume() {
		super.onResume();
		WifiServerInfoTransmitter.halt();
	}

	private void openSelectIpDialog() {

		final Dialog dialog = new Dialog(this, R.style.DialogTheme);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.dialog_manually_set_server,
				null);
		dialog.setContentView(content);

		final TextView prefix =  content
				.findViewById(R.id.dialog_manually_server_set_ip_prefix);
		final EditText input =  content
				.findViewById(R.id.dialog_manually_server_set);
		final Button okBtn =  content
				.findViewById(R.id.dialog_select_server_btn_ok);
		final Button cancel =  content
				.findViewById(R.id.dialog_select_server_btn_cancel);
		TextView title =  content
				.findViewById(R.id.dialog_select_server_title);

		prefix.setTypeface(font);
		input.setTypeface(font);
		title.setTypeface(font);
		cancel.setTypeface(font);
		okBtn.setTypeface(font);

		String prefixS = Utils.getNetPrefix(this) + ".";
		prefix.setText(prefixS);

		String iptxt = ip;
		if (iptxt.startsWith(prefixS)) {
			iptxt = iptxt.replace(prefixS, "");
		} else {
			iptxt = "1";
		}
		input.setText(iptxt);
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				try {
					int num = Integer.parseInt(s.toString());
					if (num > 0 && num < 256) {
						input.setTextColor(0xffffffff);
						okBtn.setEnabled(true);
					} else {
						input.setTextColor(0xffff0000);
						okBtn.setEnabled(false);
					}
				} catch (NumberFormatException e) {
					input.setTextColor(0xffff0000);
					okBtn.setEnabled(false);
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

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
				broadcastReceiver.stop();
				finish();
			}
		});

		okBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					dialog.dismiss();
				} catch (Exception e) {
				}
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(RemoteControllerActivity.this);
				Editor editor = prefs.edit();
				ip = prefix.getText().toString() + input.getText().toString();
				editor.putString("IP", ip);
				editor.commit();
				openPortDialog();

			}
		});

		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				broadcastReceiver.stop();
				finish();
			}
		});
		dialog.show();

	}

	private void openPortDialog() {

		if (EmulatorInfoHolder.getInfo().isMultiPlayerSupported()) {

			final Dialog dialog = new Dialog(this, R.style.DialogTheme);

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View content = inflater.inflate(R.layout.dialog_select_port, null);

			dialog.setContentView(content);

			TextView title =  content
					.findViewById(R.id.dialog_select_server_title);
			title.setTypeface(font);

			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					broadcastReceiver.stop();
					finish();
				}
			});

			dialog.show();
			int[] ids = new int[] { R.id.dialog_select_port_1,
					R.id.dialog_select_port_2, R.id.dialog_select_port_3,
					R.id.dialog_select_port_4 };

			for (int i = 0; i < ids.length; i++) {
				Button b =  content.findViewById(ids[i]);
				b.setTypeface(font);
				final int portIdx = i;
				b.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						DialogUtils.dismiss(dialog);
						start(portIdx);
					}
				});
			}
		} else {
			start(0);
		}

	}

	private void start(int portIdx) {
		setPort(portIdx);
		portIndicator.setText((port + 1) + "");
		try {

			if (client != null) {
				client.onStop();
			}

			client = new WifiControllerClient(InetAddress.getByName(ip), port);
			client.onResume();
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	private void setPort(int port) {
		this.port = port;

		if (client != null) {

		}
	}

	@Override
	public void finish() {
		ActivitySwitcher.animationOut(findViewById(R.id.root),
				getWindowManager(),
				new ActivitySwitcher.AnimationFinishedListener() {
					@Override
					public void onAnimationFinished() {
						RemoteControllerActivity.super.finish();
						overridePendingTransition(0, 0);
					}
				});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && searchMode) {
			((View) searchBox.getParent()).setVisibility(View.INVISIBLE);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}
