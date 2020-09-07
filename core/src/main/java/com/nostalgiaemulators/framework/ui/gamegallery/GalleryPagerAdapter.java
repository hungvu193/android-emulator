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

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nostalgiaemulators.framework.AdProvider;
import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.ui.gamegallery.GalleryAdapter.ROW_TYPE;
import com.nostalgiaemulators.framework.ui.gamegallery.GalleryAdapter.RowItem;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.Utils;

public class GalleryPagerAdapter extends PagerAdapter {

	public interface OnItemClickListener {
		public void onItemClick(GameDescription game, boolean clickOnAds);
	}

	private final static int[] LABELS_RID = new int[] {
			R.string.gallery_page_byname, R.string.gallery_page_most,
			R.string.gallery_page_last_played, R.string.gallery_page_last };

	private final static int[] SORT_TYPES = new int[] {
			GalleryAdapter.SORT_BY_NAME, GalleryAdapter.SORT_BY_MOST_PLAYED,
			GalleryAdapter.SORT_BY_LAST_PLAYED,
			GalleryAdapter.SORT_BY_INSERT_DATE };

	private int[] yOffsets = new int[LABELS_RID.length];
	private ListView[] lists = new ListView[LABELS_RID.length];

	private GalleryAdapter[] listAdapters = new GalleryAdapter[LABELS_RID.length];

	private Activity activity;
	private OnItemClickListener listener;

	public GalleryPagerAdapter(Activity activity, OnItemClickListener listener) {
		this.activity = activity;
		this.listener = listener;
		boolean isLeadbolt = ((EmulatorApplication) activity.getApplication())
				.getAdProvider() == AdProvider.LEADBOLT;
		for (int i = 0; i < LABELS_RID.length; i++) {
			GalleryAdapter adapter = listAdapters[i] = new GalleryAdapter(
					activity, Utils.isAdvertisingVersion(activity)
							&& isLeadbolt);
			adapter.setSortType(SORT_TYPES[i]);
		}
	}

	@Override
	public int getCount() {
		return LABELS_RID.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return activity.getText(LABELS_RID[position]);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0.equals(arg1);
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		final ListView list = new ListView(activity);
		list.setCacheColorHint(0x00000000);
		list.setFastScrollEnabled(true);
		list.setSelector(R.drawable.row_game_item_list_selector);
		list.setAdapter(listAdapters[position]);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				RowItem item = (RowItem) listAdapters[position].getItem(arg2);
				listener.onItemClick(item.game, item.type == ROW_TYPE.ADDS);
			}
		});

		list.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.i("list", position + ":" + scrollState + "");
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					yOffsets[position] = list.getFirstVisiblePosition();
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		list.setSelection(yOffsets[position]);

		lists[position] = list;
		container.addView(list);
		return list;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	public void setGames(ArrayList<GameDescription> games) {
		for (GalleryAdapter adapter : listAdapters) {
			adapter.setGames(new ArrayList<GameDescription>(games));
		}
	}

	
	public int addGames(ArrayList<GameDescription> newGames) {
		int result = 0;
		for (GalleryAdapter adapter : listAdapters) {
			result = adapter.addGames(new ArrayList<GameDescription>(newGames));
		}
		return result;
	}

	public void setFilter(String filter) {
		for (GalleryAdapter adapter : listAdapters) {
			adapter.setFilter(filter);
		}
	}

	@Override
	public void notifyDataSetChanged() {
		for (int i = 0; i < LABELS_RID.length; i++) {
			GalleryAdapter adapter = listAdapters[i];
			adapter.notifyDataSetChanged();
			if (lists[i] != null)
				lists[i].setSelection(yOffsets[i]);
		}
		super.notifyDataSetChanged();
	}

	public static final String EXTRA_POSITIONS = "EXTRA_POSITIONS";

	public void onSaveInstanceState(Bundle outState) {
		outState.putIntArray(EXTRA_POSITIONS, yOffsets);
	}

	public void onRestoreInstanceState(Bundle inState) {
		if (inState != null) {
			yOffsets = inState.getIntArray(EXTRA_POSITIONS);
			if (yOffsets == null)
				yOffsets = new int[LABELS_RID.length];
		}
	}

}