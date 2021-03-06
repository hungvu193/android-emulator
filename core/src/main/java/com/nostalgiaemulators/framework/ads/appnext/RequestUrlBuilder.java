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

package com.nostalgiaemulators.framework.ads.appnext;

public class RequestUrlBuilder {
	public static String buildUrl(String placementId, int count,
			boolean useImage) {
		StringBuilder sb = new StringBuilder();
		sb.append("https://admin.appnext.com/offerWallApi.aspx");
		sb.append("?id=");
		sb.append(placementId);
		sb.append("&cnt=");
		sb.append(count);
		if (useImage) {
			sb.append("&pimg=1");
		} else {
			sb.append("&pimp=1");
		}
		return sb.toString();

	}
}
