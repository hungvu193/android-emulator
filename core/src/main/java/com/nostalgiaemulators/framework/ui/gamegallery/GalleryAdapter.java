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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.utils.FontUtil;

@SuppressLint("DefaultLocale")
public class GalleryAdapter extends BaseAdapter implements SectionIndexer {

	public enum ROW_TYPE {
		NORMAL, ADDS
	};

	public class RowItem {
		ROW_TYPE type = ROW_TYPE.NORMAL;
		GameDescription game;
		char firstLetter;
	}

	private Typeface font;
	private LayoutInflater inflater;
	private Context context;
	private View adsView;
	private int mainColor, adsColor;

	SparseArray<ImageView> arrows = new SparseArray<ImageView>();
	HashMap<Character, Integer> alphaIndexer = new HashMap<Character, Integer>();

	private boolean withAds = true;

	public GalleryAdapter(Context context, boolean withAds) {
		this.withAds = withAds;
		this.context = context.getApplicationContext();
		font = FontUtil.createFontFace(context);

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		adsView = inflater.inflate(R.layout.row_ads_list, null);
		TextView label = (TextView) adsView
				.findViewById(R.id.row_game_item_name);
		label.setTypeface(font);
		mainColor = context.getResources().getColor(R.color.main_color);
		adsColor = context.getResources().getColor(R.color.ads_color);
	}

	private ArrayList<GameDescription> games = new ArrayList<GameDescription>();
	private ArrayList<RowItem> filterGames = new ArrayList<GalleryAdapter.RowItem>();

	@Override
	public int getCount() {
		return filterGames.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= filterGames.size()) {
			return filterGames.get(filterGames.size() - 1);
		}
		if (position < 0) {
			return filterGames.get(0);
		}
		return filterGames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RowItem item = (RowItem) filterGames.get(position);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.row_game_list, null);
		}

		GameDescription game = item.game;

		TextView name = (TextView) convertView
				.findViewById(R.id.row_game_item_name);
		ImageView arrowIcon = (ImageView) convertView
				.findViewById(R.id.game_item_arrow);
		ImageView bck = (ImageView) convertView
				.findViewById(R.id.game_item_bck);

		ProgressBar runIndicator = (ProgressBar) convertView
				.findViewById(R.id.row_game_item_progressBar);

		name.setTypeface(font);
		runIndicator.setMax(sumRuns);

		if (item.type == ROW_TYPE.NORMAL) {
			name.setText(game.getCleanName());

			if (convertView.getTag() == ROW_TYPE.ADDS) {
				arrowIcon.setImageResource(R.drawable.ic_next_arrow);
				arrowIcon.clearAnimation();
				name.setTextColor(mainColor);
				name.setShadowLayer(5, 0, 0, mainColor);
				name.setGravity(Gravity.CENTER_VERTICAL);

			}

			bck.setImageResource(R.drawable.game_item_small_bck);
			convertView.setTag(ROW_TYPE.NORMAL);
		} else {

			name.setText(R.string.todays_top_apps);
			name.setTextColor(adsColor);
			name.setShadowLayer(5, 0, 0, adsColor);
			name.setGravity(Gravity.CENTER);
			bck.setImageResource(R.drawable.ads_icon);
			arrowIcon.setImageResource(R.drawable.ic_ads_next);
			Animation anim = AnimationUtils.loadAnimation(context,
					R.anim.arow_right_anim);
			arrowIcon.startAnimation(anim);
			convertView.setTag(ROW_TYPE.ADDS);
		}

		return convertView;

	}

	ImageView lastArrow = null;

	public void setSelected(int pos) {
		ImageView icon = arrows.get(pos);
		if (icon != null) {
			if (lastArrow != null)
				lastArrow.setAnimation(null);
			Animation anim = AnimationUtils.loadAnimation(context,

			R.anim.arow_right_anim);
			icon.startAnimation(anim);
			lastArrow = icon;
		}

	}

	String filter = "";

	public void setFilter(String filter) {
		this.filter = filter.toLowerCase();
		filterGames();
	}

	public void setGames(ArrayList<GameDescription> games) {
		this.games = new ArrayList<GameDescription>();
		this.games.addAll(games);
		filterGames();
	}

	
	public int addGames(ArrayList<GameDescription> newGames) {
		for (GameDescription game : newGames) {
			if (!games.contains(game)) {
				games.add(game);
			}
		}
		filterGames();
		return games.size();
	}

	private static final int ADS_INTERVAL = 20;

	private Comparator<GameDescription> nameComparator = new Comparator<GameDescription>() {
		@Override
		public int compare(GameDescription lhs, GameDescription rhs) {
			return lhs.getSortName().compareTo(rhs.getSortName());
		}
	};

	private Comparator<GameDescription> insertDateComparator = new Comparator<GameDescription>() {
		@Override
		public int compare(GameDescription lhs, GameDescription rhs) {
			if (lhs.inserTime < rhs.inserTime) {
				return 1;
			} else if (lhs.inserTime > rhs.inserTime) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	private Comparator<GameDescription> lastPlayedDateComparator = new Comparator<GameDescription>() {
		@Override
		public int compare(GameDescription lhs, GameDescription rhs) {
			long dif = lhs.lastGameTime - rhs.lastGameTime;

			if (dif == 0) {
				return 0;
			} else if (dif < 0) {
				return 1;
			} else {
				return -1;
			}
		}
	};

	private Comparator<GameDescription> playedCountComparator = new Comparator<GameDescription>() {
		@Override
		public int compare(GameDescription lhs, GameDescription rhs) {
			return (int) (-lhs.runCount + rhs.runCount);
		}
	};

	private int sumRuns = 0;

	private void filterGames() {
		filterGames.clear();
		switch (sortType) {
		case SORT_BY_NAME:
			Collections.sort(games, nameComparator);
			break;
		case SORT_BY_INSERT_DATE:
			Collections.sort(games, insertDateComparator);
			break;
		case SORT_BY_MOST_PLAYED:
			Collections.sort(games, playedCountComparator);
			break;
		case SORT_BY_LAST_PLAYED:
			Collections.sort(games, lastPlayedDateComparator);
			break;
		}

		String containsFilter = " " + filter;
		char lastLetter = '0';
		int counter = 0;
		sumRuns = 0;
		for (GameDescription game : games) {
			sumRuns = game.runCount > sumRuns ? game.runCount : sumRuns;
			if (withAds) {
				if (counter % ADS_INTERVAL == 0) {
					RowItem item = new RowItem();
					item.type = ROW_TYPE.ADDS;
					item.firstLetter = lastLetter;
					filterGames.add(item);
					counter++;
				}
			}

			String name = game.getCleanName().toLowerCase();
			boolean secondCondition = true;
			if (sortType == SORT_BY_LAST_PLAYED
					|| sortType == SORT_BY_MOST_PLAYED) {
				secondCondition = game.lastGameTime != 0;
			}
			if ((name.startsWith(filter) || name.contains(containsFilter))
					& secondCondition) {
				RowItem item = new RowItem();
				item.type = ROW_TYPE.NORMAL;
				item.game = game;
				item.firstLetter = name.charAt(0);
				lastLetter = item.firstLetter;
				filterGames.add(item);
				counter++;
			}
		}
		alphaIndexer.clear();

		if (sortType == SORT_BY_NAME) {
			for (int i = 0; i < filterGames.size(); i++) {
				RowItem item = filterGames.get(i);
				char ch = item.firstLetter;
				if (!alphaIndexer.containsKey(ch)) {
					alphaIndexer.put(ch, i);
				}
			}
		}
		super.notifyDataSetChanged();
	}

	public static final int SORT_BY_NAME = 0;
	public static final int SORT_BY_INSERT_DATE = 1;
	public static final int SORT_BY_MOST_PLAYED = 2;
	public static final int SORT_BY_LAST_PLAYED = 3;

	private int sortType = SORT_BY_NAME;

	public void setSortType(int sortType) {
		this.sortType = sortType;
		filterGames();
	}

	@Override
	public int getPositionForSection(int section) {
		try {
			Character ch = Character.toLowerCase(sections[section]);
			Integer pos = alphaIndexer.get(ch);
			if (pos == null) {
				return 0;
			} else {
				return pos;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}

	@Override
	public int getSectionForPosition(int position) {
		RowItem item = (RowItem) getItem(position);
		char ch = Character.toUpperCase(item.firstLetter);
		for (int i = 0; i < sections.length; i++) {
			Character ch1 = sections[i];
			if (ch1.equals(ch)) {
				return i;
			}
		}
		return 1;
	}

	Character[] sections;

	@Override
	public Object[] getSections() {
		Set<Character> keyset = alphaIndexer.keySet();
		sections = new Character[keyset.size()];
		keyset.toArray(sections);
		Arrays.sort(sections, new Comparator<Character>() {
			@Override
			public int compare(Character lhs, Character rhs) {
				return lhs.compareTo(rhs);
			}
		});
		for (int i = 0; i < sections.length; i++)
			sections[i] = Character.toUpperCase(sections[i]);
		return sections;
	}

	@Override
	public void notifyDataSetChanged() {
		filterGames();
	}

}