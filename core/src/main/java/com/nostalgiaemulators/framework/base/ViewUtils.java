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

import java.util.HashMap;

import android.content.Context;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;

public class ViewUtils {
	private ViewUtils() {
	}

	public static ViewPort computeViewPort(Emulator emulator, int screenWidth,
			int screenHeight, int paddingLeft, int paddingTop) {
		GfxProfile gfx = null;
		if (emulator != null) {
			gfx = emulator.getActiveGfxProfile();
		} else {
			gfx = EmulatorInfoHolder.getInfo().getDefaultGfxProfile();
		}
		return computeViewPort(gfx, screenWidth, screenHeight, paddingLeft,
				paddingTop);
	}

	public static ViewPort computeInitViewPort(Context context, int w, int h,
			int paddingLeft, int paddingTop) {
		GfxProfile gfx = EmulatorInfoHolder.getInfo().getDefaultGfxProfile();
		return ViewUtils.computeViewPort(gfx, w, h, paddingLeft, paddingTop);
	}

	public static HashMap<String, ViewPort> computeAllInitViewPorts(
			Context context, int w, int h, int paddingLeft, int paddingTop) {
		HashMap<String, ViewPort> res = new HashMap<String, ViewPort>();
		for (GfxProfile profile : EmulatorInfoHolder.getInfo()
				.getAvailableGfxProfiles()) {
			ViewPort vp = computeViewPort(profile, w, h, paddingLeft,
					paddingTop);
			res.put(profile.name, vp);
		}
		return res;
	}

	public static HashMap<String, ViewPort> loadOrComputeAllViewPorts(
			Context context, int w, int h, int paddingLeft, int paddingTop) {
		HashMap<String, ViewPort> res = computeAllInitViewPorts(context, w, h,
				paddingLeft, paddingTop);
		for (GfxProfile profile : EmulatorInfoHolder.getInfo()
				.getAvailableGfxProfiles()) {
			ViewPort vp = loadViewPort(context, w, h, profile);
			if (vp != null) {
				res.put(profile.name, vp);
			}
		}
		return res;
	}

	public static ViewPort loadOrComputeViewPort(Context context,
			Emulator emulator, int w, int h, int paddingLeft, int paddingTop,
			boolean ignoreFullscreenSettings) {
		ViewPort vp = null;

		GfxProfile profile = null;
		if (emulator != null) {
			profile = emulator.getActiveGfxProfile();
		} else {
			profile = EmulatorInfoHolder.getInfo().getDefaultGfxProfile();

		}

		if (!ignoreFullscreenSettings
				&& PreferenceUtil.isFullScreenEnabled(context)) {
			vp = new ViewPort();
			vp.height = h;
			vp.width = w;
			vp.x = 0;
			vp.y = 0;
		} else if (loadViewPort(context, w, h, profile) != null) {
			vp = loadViewPort(context, w, h, profile);
		} else {
			vp = ViewUtils.computeViewPort(profile, w, h, paddingLeft,
					paddingTop);
		}
		return vp;
	}

	private static ViewPort loadViewPort(Context context, int w, int h,
			GfxProfile profile) {
		ViewPort vp = PreferenceUtil.getViewPort(context, w, h);

		GfxProfile defaultProfile = EmulatorInfoHolder.getInfo()
				.getDefaultGfxProfile();

		if (vp != null && profile != defaultProfile) {
			int vpw = vp.width;
			int vph = vp.height;
			int ow = vpw;
			int oh = vph;
			float ratio = (float) profile.originalScreenHeight
					/ profile.originalScreenWidth;
			if (w < h) {
				vpw = vp.width;
				vph = (int) (vpw * ratio);
			} else {
				vph = vp.height;
				vpw = (int) (vph / ratio);
				vp.x += (ow - vpw) / 2;
			}
			vp.width = vpw;
			vp.height = vph;

		}
		return vp;
	}

	public static ViewPort computeViewPort(GfxProfile gfx, int screenWidth,
			int screenHeight, int paddingLeft, int paddingTop) {

		if (gfx == null) {
			gfx = EmulatorInfoHolder.getInfo().getDefaultGfxProfile();
		}

		int w = screenWidth - paddingLeft;
		int h = screenHeight - paddingTop;
		int vpw;
		int vph;
		float ratio = (float) gfx.originalScreenHeight
				/ gfx.originalScreenWidth;
		if (w < h) {
			vpw = w;
			vph = (int) (vpw * ratio);
		} else {
			vph = h;
			vpw = (int) (vph / ratio);
		}
		ViewPort result = new ViewPort();
		result.x = (w - vpw) / 2 + paddingLeft;
		result.y = 0 + paddingTop;
		result.height = vph;
		result.width = vpw;
		return result;
	}

}
