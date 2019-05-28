package com.bnrc.busapp.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.bnrc.busapp.R;
import com.bnrc.busapp.constant.Constants;
import com.bnrc.busapp.model.Child;

public class SelectPicPopupWindow extends PopupWindow {
	private static final String TAG = SelectPicPopupWindow.class
			.getSimpleName();
	private ImageView mHome, mWork, mOther, mDel;// 弹窗上的选项
	private ImageView mHomeSelected, mWorkSelected, mOtherSelected;// 弹窗上的选项
	private Button mCancelBtn;
	private View mMenuView;

	public SelectPicPopupWindow(Activity context, Child child,
                                OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.fragment_news_pop, null);
		mHome = (ImageView) mMenuView.findViewById(R.id.iv_home);
		mWork = (ImageView) mMenuView.findViewById(R.id.iv_work);
		mOther = (ImageView) mMenuView.findViewById(R.id.iv_other);
		mDel = (ImageView) mMenuView.findViewById(R.id.iv_del);
		mCancelBtn = (Button) mMenuView.findViewById(R.id.btn_cancel);
		mHomeSelected = (ImageView) mMenuView
				.findViewById(R.id.iv_selected_home);
		mWorkSelected = (ImageView) mMenuView
				.findViewById(R.id.iv_selected_work);
		mOtherSelected = (ImageView) mMenuView
				.findViewById(R.id.iv_selected_other);
		Log.i(TAG, "child type " + child.getType());
		switch (child.getType()) {
		case Constants.TYPE_WORK:
			mWorkSelected.setVisibility(View.VISIBLE);
			break;
		case Constants.TYPE_HOME:
			mHomeSelected.setVisibility(View.VISIBLE);
			break;
		case Constants.TYPE_OTHER:
			mOtherSelected.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		mHome.setOnClickListener(itemsOnClick);
		mWork.setOnClickListener(itemsOnClick);
		mOther.setOnClickListener(itemsOnClick);
		mDel.setOnClickListener(itemsOnClick);
		mCancelBtn.setOnClickListener(itemsOnClick);
		// ����SelectPicPopupWindow��View
		this.setContentView(mMenuView);
		// ����SelectPicPopupWindow��������Ŀ�
		this.setWidth(LayoutParams.MATCH_PARENT);
		// ����SelectPicPopupWindow��������ĸ�
		this.setHeight(LayoutParams.WRAP_CONTENT);
		// ����SelectPicPopupWindow��������ɵ��
		this.setFocusable(true);
		// ����SelectPicPopupWindow�������嶯��Ч��
		this.setAnimationStyle(R.style.AnimBottom);
		// ʵ����һ��ColorDrawable��ɫΪ��͸��
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// ����SelectPicPopupWindow��������ı���
		this.setBackgroundDrawable(dw);
		// mMenuView���OnTouchListener�����жϻ�ȡ����λ�������ѡ������������ٵ�����
		mMenuView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});

	}
}
