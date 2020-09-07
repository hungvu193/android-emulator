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
import java.util.HashSet;
import java.util.Set;

public class FilenameExtFilter implements java.io.FilenameFilter {

	Set<String> exts;
	boolean showDir = false;
	boolean showHiden = false;

	public FilenameExtFilter(String[] exts, boolean showDirs, boolean showHiden) {
		Set<String> tmp = new HashSet<String>();
		for (String ext : exts)
			tmp.add(ext);
		showDir = showDirs;
		this.showHiden = showHiden;
		this.exts = addDots(tmp);
	}

	public FilenameExtFilter(Set<String> exts, boolean showDirs,
			boolean showHiden) {
		showDir = showDirs;
		this.showHiden = showHiden;
		this.exts = addDots(exts);
	}

	public FilenameExtFilter(String... exts) {
		this(exts, false, false);
	}

	public FilenameExtFilter(String ext) {
		this(new String[] { ext }, false, false);
	}

	private Set<String> addDots(Set<String> exts) {
		Set<String> temp = new HashSet<String>();
		for (String ext : exts) {
			temp.add("." + ext);
		}
		return temp;
	}

	public boolean accept(File dir, String filename) {
		if ((!showHiden) && (filename.charAt(0) == '.'))
			return false;

		if (showDir) {
			File f = new File(dir, filename);
			if (f.isDirectory())
				return true;
		}

		
		String fnLower = filename.toLowerCase();
		for (String ext : exts) {
			if (fnLower.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

}