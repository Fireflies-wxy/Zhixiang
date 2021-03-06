package com.bnrc.bnrcbus.ui.expandablelistview;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * 
 * @author baoyz
 * @date 2014-8-23
 * 
 */
public class SwipeMenuView extends LinearLayout implements OnClickListener {

	private SwipeMenuListView mListView;
	private SwipeMenuLayout mLayout;
	private SwipeMenu mMenu;
	private OnSwipeItemClickListener onItemClickListener;
	private Animation mAppearAnimation;
	private Animation mDisappearAnimation;
	private int position;
	private int groupPosition;
	private int mItemCount;

	public void setGroupPosition(int groupPosition) {
		this.groupPosition = groupPosition;
	}

	public int getGroupPosition() {
		return groupPosition;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public SwipeMenuView(SwipeMenu menu) {
		super(menu.getContext());
		mMenu = menu;
		List<SwipeMenuItem> items = menu.getMenuItems();
		int id = 0;
		for (SwipeMenuItem item : items) {
			addItem(item, id++);
		}
		mItemCount = id;
	}

	public SwipeMenuView(SwipeMenu menu, SwipeMenuListView listView) {
		super(menu.getContext());
		mListView = listView;
		mMenu = menu;
		List<SwipeMenuItem> items = menu.getMenuItems();
		int id = 0;
		for (SwipeMenuItem item : items) {
			addItem(item, id++);
		}
		mItemCount = id;
	}

	private void addItem(SwipeMenuItem item, int id) {
		LayoutParams params = new LayoutParams(item.getWidth(),
				LayoutParams.MATCH_PARENT);
		params.leftMargin = item.getLeftMargin();
		params.rightMargin = item.getRightMargin();
		params.topMargin = item.getTopMargin();
		params.bottomMargin = item.getBottomMargin();
		LinearLayout parent = new LinearLayout(getContext());
		parent.setId(id);
		parent.setGravity(Gravity.CENTER);
		parent.setOrientation(LinearLayout.VERTICAL);
		parent.setLayoutParams(params);
		parent.setBackgroundDrawable(item.getBackground());
		parent.setOnClickListener(this);
		addView(parent);

		if (item.getIcon() != null) {
			parent.addView(createIcon(item));
		}
		if (!TextUtils.isEmpty(item.getTitle())) {
			parent.addView(createTitle(item));
		}
		if (item.getAppearAnimation()!=null) {
			mAppearAnimation = item.getAppearAnimation();
		}
		if (item.getDisappearAnimation()!=null) {
			mDisappearAnimation = item.getDisappearAnimation();
		}

	}

	private ImageView createIcon(SwipeMenuItem item) {
		ImageView iv = new ImageView(getContext());
		iv.setImageDrawable(item.getIcon());
		return iv;
	}

	private TextView createTitle(SwipeMenuItem item) {
		TextView tv = new TextView(getContext());
		tv.setText(item.getTitle());
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,item.getTitleSize());
		tv.setTextColor(item.getTitleColor());
		return tv;
	}

	@Override
	public void onClick(View v) {
		if (onItemClickListener != null && mLayout.isOpen()) {
			onItemClickListener.onItemClick(this, mMenu, v.getId());
		}
	}

	public OnSwipeItemClickListener getOnSwipeItemClickListener() {
		return onItemClickListener;
	}

	public void setOnSwipeItemClickListener(OnSwipeItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public void setLayout(SwipeMenuLayout mLayout) {
		this.mLayout = mLayout;
	}

	public static interface OnSwipeItemClickListener {
		void onItemClick(SwipeMenuView view, SwipeMenu menu, int index);
	}

	public int getItemCount() {
		return mItemCount;
	}

	public Animation getDisappearAnimation() {
		return mDisappearAnimation;
	}

	public Animation getAppearAnimation() {
		return mAppearAnimation;
	}

	public void startAppearAnimation(long offset) {
		if(mAppearAnimation!=null) {
			mAppearAnimation.setStartOffset(offset);
			mAppearAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationEnd(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			this.startAnimation(mAppearAnimation);
		}
	}

	public void startDisappearAnimation(long offset) {
		if(mDisappearAnimation!=null) {
			mDisappearAnimation.setStartOffset(offset);
			mDisappearAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			this.startAnimation(mDisappearAnimation);
		}
	}
}
