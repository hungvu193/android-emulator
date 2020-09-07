package com.nostalgiaemulators.framework.ui.gamegallery;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

// https://github.com/JakeWharton/ViewPagerIndicator/pull/257
public class FixedViewPager extends ViewPager {

	public FixedViewPager(Context context) {
		super(context);
	}

	public FixedViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// prevent NPE if fake dragging and touching ViewPager
		if (isFakeDragging())
			return false;
		return super.onInterceptTouchEvent(ev);
	}
}
