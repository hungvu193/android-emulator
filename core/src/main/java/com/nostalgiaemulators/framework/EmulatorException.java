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

package com.nostalgiaemulators.framework;

import android.content.Context;
public class EmulatorException extends RuntimeException {

	public EmulatorException(String msg) {
		super(msg);
	}

	public EmulatorException(int stringResId) {
		this.stringResId = stringResId;
	}

	public EmulatorException(int stringResId, String t) {
		this.stringResId = stringResId;
		this.formatArg = t;
	}

	public String getMessage(Context context) {
		if (stringResId != -1) {
			String resource = context.getResources().getString(stringResId);
			if (formatArg != null) {
				return String.format(resource, formatArg);
			} else {
				return resource;
			}
		}
		return getMessage();
	}

	private int stringResId = -1;
	private String formatArg;
	private static final long serialVersionUID = 1L;

}
