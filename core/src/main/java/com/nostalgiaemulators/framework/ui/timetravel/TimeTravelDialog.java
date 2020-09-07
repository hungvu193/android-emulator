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

package com.nostalgiaemulators.framework.ui.timetravel;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.nostalgiaemulators.framework.EmulatorException;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.Manager;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.utils.FontUtil;

public class TimeTravelDialog extends Dialog implements OnSeekBarChangeListener {

	private ImageView img;
	private TextView label;
	private Manager manager;
	private Bitmap bitmap;
	private GameDescription game;
	private Typeface font;
	private int max = 0;

	public TimeTravelDialog(final Context context, Manager manager,
			GameDescription game) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.manager = manager;
		this.game = game;
		bitmap = Bitmap.createBitmap(256, 256, Config.ARGB_8888);

		font = FontUtil.createFontFace(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.dialog_time_travel, null);
		setContentView(content);

		final SeekBar seekBar = (SeekBar) content
				.findViewById(R.id.dialog_time_seek);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setNextFocusDownId(R.id.dialog_time_wheel_btn_ok);
		seekBar.setNextFocusLeftId(R.id.dialog_time_seek);
		seekBar.setNextFocusRightId(R.id.dialog_time_seek);
		seekBar.setKeyProgressIncrement(2);

		Button cancel = (Button) content
				.findViewById(R.id.dialog_time_btn_cancel);
		cancel.setTypeface(font);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
		cancel.setFocusable(true);

		img = (ImageView) content.findViewById(R.id.dialog_time_img);
		label = (TextView) content.findViewById(R.id.dialog_time_label);
		label.setTypeface(font);

		max = manager.getHistoryItemCount() - 1;
		seekBar.setMax(max);
		seekBar.setProgress(max);

		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TimeTravelDialog.this.manager
						.startGame(TimeTravelDialog.this.game);
				TimeTravelDialog.this.manager.loadHistoryState(max
						- seekBar.getProgress());
				try {
					TimeTravelDialog.this.manager.enableCheats(context,
							TimeTravelDialog.this.game);
				} catch (EmulatorException e) {
				}
				dismiss();
			}
		};

		seekBar.setOnClickListener(listener);

		Button ok = (Button) content
				.findViewById(R.id.dialog_time_wheel_btn_ok);
		ok.setNextFocusUpId(R.id.dialog_time_seek);

		ok.setTypeface(font);
		ok.setOnClickListener(listener);
		ok.setFocusable(true);

		TextView title = (TextView) content
				.findViewById(R.id.dialog_time_title);
		title.setTypeface(font);

		manager.pauseEmulation();
		manager.renderHistoryScreenshot(bitmap, 0);
		img.setImageBitmap(bitmap);

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		label.setText(String.format("-%02.2fs", (max - progress) / 4f));
		manager.renderHistoryScreenshot(bitmap, max - progress);
		img.setImageBitmap(bitmap);
		img.invalidate();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

}
