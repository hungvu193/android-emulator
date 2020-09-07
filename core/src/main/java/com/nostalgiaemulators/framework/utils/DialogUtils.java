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

package com.nostalgiaemulators.framework.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.Window;

import com.nostalgiaemulators.framework.remote.VirtualDPad;

public class DialogUtils {

	private DialogUtils() {

	}

	public static void dismiss(final Dialog dialog) {
		if (dialog != null && dialog.isShowing()) {
			try {
				dialog.dismiss();
			} catch (Exception e) {
			}
		}
	}

	public static void show(final Dialog dialog, final boolean cancelable) {
		dialog.setOnShowListener(listener);
		dialog.setCanceledOnTouchOutside(cancelable);
		dialog.show();
	}

	private static DialogInterface.OnShowListener listener = new DialogInterface.OnShowListener() {

		@Override
		public void onShow(DialogInterface d) {
			Window window = ((Dialog) d).getWindow();
			VirtualDPad.getInstance().attachToWindow(window);
			VirtualDPad.getInstance().onResume(window);
		}
	};

}
