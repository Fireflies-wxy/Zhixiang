package com.bnrc.busapp.ui.expandablelistview;

import android.annotation.SuppressLint;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * @author XieHao
 * 
 */
@SuppressLint({ "ClickableViewAccessibility", "Recycle" })
public class FrontViewToMove {
	private final String TAG = FrontViewToMove.class.getSimpleName();
	private View frontView;// ��Ҫ��������ͼ
	private int downX;// ��ָ����ʱ��x����
	private boolean hasMoved = false;// �ж���ͼ�Ƿ��ƶ�
	private int xToMove = 120;// ��ͼ��Ҫ���ƶ��ľ��룬Ĭ��200
	private ListView listView;// ��������ƶ�����ͼΪListView���������item�������ͼ���������������¹���

	/**
	 * @param frontView
	 *            ��Ҫ��������ͼ
	 */
	public FrontViewToMove(View frontView) {
		this.frontView = frontView;
		moveListener();
	}

	/**
	 * @param frontView
	 *            ��Ҫ��������ͼ
	 * @param xToMove
	 *            ��ͼ��Ҫ���ƶ��ľ���
	 */
	public FrontViewToMove(View frontView, int xToMove) {
		this.frontView = frontView;
		this.xToMove = xToMove;
		moveListener();
	}

	/**
	 * @param frontView
	 *            ��Ҫ��������ͼ
	 * @param listView
	 *            ��Ҫ��������ͼ������
	 */
	public FrontViewToMove(View frontView, ListView listView) {
		this.frontView = frontView;
		this.listView = listView;
		moveListener();
	}

	/**
	 * @param frontView
	 *            ��Ҫ��������ͼ
	 * @param listView
	 *            ��Ҫ��������ͼ������
	 * @param xToMove
	 *            ��ͼ��Ҫ���ƶ��ľ���
	 */
	public FrontViewToMove(View frontView, ListView listView, int xToMove) {
		this.frontView = frontView;
		this.listView = listView;
		this.xToMove = xToMove;
		moveListener();
	}

	/**
	 * ����frontView��OnTouch������ʹ����������Ķ���Ч��
	 */
	public void moveListener() {
		frontView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {

				switch (MotionEventCompat.getActionMasked(motionEvent)) {
				case MotionEvent.ACTION_DOWN: {

					downX = (int) motionEvent.getRawX();
					if (hasMoved) {
						downX = downX + xToMove;
					} else {
						view.onTouchEvent(motionEvent);// ����ͼû�б��ƶ��������¼���ʹ����¼����á�
					}
					return true;
				}

				case MotionEvent.ACTION_UP: {

					float deltaX = motionEvent.getRawX() - downX;
					boolean swap = false;

					if ((deltaX > -xToMove / 2 && hasMoved)
							|| (deltaX < -xToMove / 2 && !hasMoved)) {
						swap = true;
					}

					if (swap) {
						if (!hasMoved) {
							generateRevealAnimate(frontView, -xToMove);
							hasMoved = true;
						} else {
							generateRevealAnimate(frontView, 0);
							hasMoved = false;
						}
					} else {
						if (hasMoved) {
							generateRevealAnimate(frontView, -xToMove);
						} else {
							generateRevealAnimate(frontView, 0);
						}
					}

					break;
				}

				case MotionEvent.ACTION_MOVE: {
					float deltaX = motionEvent.getRawX() - downX;

					MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);

					cancelEvent.setAction(MotionEvent.ACTION_CANCEL
							| (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
					Log.i(TAG, "deltaX: " + deltaX + " " + (deltaX < -10));
					if (deltaX < -10) {
						view.onTouchEvent(cancelEvent);// ������ʱ��ո���ͼ�ĵ���¼�
						if (null != listView) {// ����ͼ����ʱ����listView�����¹���
							listView.requestDisallowInterceptTouchEvent(false);
							listView.onTouchEvent(cancelEvent);
						}
					}

					if (!(deltaX > 0 && !hasMoved)) {
						setTranslationX(frontView, deltaX);
						Log.i(TAG, "setTranslationX");
					}
					return true;
				}
				}
				return false;
			}
		});

	}

	/**
	 * @param view
	 *            ��Ҫ�ƶ�����ͼ
	 * @param deltaX
	 *            �����ƶ��ľ���
	 */
	private void generateRevealAnimate(final View view, float deltaX) {
		int moveTo = 0;
		moveTo = (int) deltaX;
		animate(view).translationX(moveTo).setDuration(10)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {

					}
				});
	}

	public void swipeBack() {
		setTranslationX(frontView, 0);
		downX = 0;
		hasMoved = false;
	}

}
