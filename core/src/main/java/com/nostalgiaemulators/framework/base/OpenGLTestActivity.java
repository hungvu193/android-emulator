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

import android.app.Activity;
import android.os.Bundle;

public class OpenGLTestActivity extends Activity implements
		OpenGLTestView.Callback {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new OpenGLTestView(this, this);
		setContentView(view);
	}

	@Override
	public void onDetected(final int i) {
		runOnUiThread(new Runnable() {
			public void run() {
				setResult(i);
				finish();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (view != null) {
			view.onPause();
		}
	}

	OpenGLTestView view;
}
