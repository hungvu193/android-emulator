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
import java.io.Serializable;

import android.annotation.SuppressLint;

import com.nostalgiaemulators.framework.utils.Utils;
import com.nostalgiaemulators.framework.utils.annotations.Column;
import com.nostalgiaemulators.framework.utils.annotations.Table;

@Table
public class GameDescription implements Serializable,
		Comparable<GameDescription> {

	private static final long serialVersionUID = -4166819653487858374L;

	@Column(hasIndex = true)
	public String name = "";

	@Column
	public String path = "";

	@Column(hasIndex = true)
	public String checksum = "";

	@Column(hasIndex = true)
	public String oldChecksum = "";

	@Column(isPrimaryKey = true)
	public long _id;

	@Column
	public long zipfile_id = -1;

	@Column(hasIndex = true)
	public long inserTime = 0;

	@Column(hasIndex = true)
	public long lastGameTime = 0;

	@Column
	public int runCount = 0;

	public GameDescription() {
	}

	public GameDescription(File file, String checksum) {
		name = file.getName();
		path = file.getAbsolutePath();
		this.checksum = checksum;
	}

	public GameDescription(String name, String path, String checksum) {
		this.name = name;
		this.path = path;
		this.checksum = checksum;
	}

	@Override
	public String toString() {
		return name + " " + checksum + " zipId:" + zipfile_id;
	}

	public boolean isInArchive() {
		return zipfile_id != -1;
	}

	private String cleanNameCache = null;

	
	public String getCleanName() {
		if (cleanNameCache == null) {
			String name = Utils.removeExt(this.name);
			int idx = name.lastIndexOf('/');
			if (idx != -1) {
				cleanNameCache = name.substring(idx + 1);
			} else {
				cleanNameCache = name;
			}
		}
		return cleanNameCache;
	}

	private String sortNameCache = null;

	
	@SuppressLint("DefaultLocale")
	public String getSortName() {
		if (sortNameCache == null) {
			sortNameCache = getCleanName().toLowerCase();
		}
		return sortNameCache;
	}

	@Override
	public int compareTo(GameDescription another) {
		return checksum.compareTo(another.checksum);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof GameDescription)) {
			return false;
		} else {
			GameDescription gd = (GameDescription) o;
			return gd.checksum == null ? false : checksum.equals(gd.checksum);
		}
	}

}
