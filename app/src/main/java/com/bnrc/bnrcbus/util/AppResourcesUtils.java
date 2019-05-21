package com.bnrc.bnrcbus.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by keitacai on 2015/3/27.
 */
public class AppResourcesUtils {

	public static final String SUFFIX_NIGHT_MODEL = "_night";
//	private static SdkResourcesUtils.SkinStyle mStyle = SdkResourcesUtils.SkinStyle.day;
	private static Resources mResources;
	private static String mPackageName;
	private static Context mContext;
	private static SparseIntArray mResIdMap = new SparseIntArray();
	private static LayoutInflater mInflater;
	// static {
	// mContext = NaviApplication.getContext();
	// mResources = mContext.getResources();
	// mPackageName = mContext.getPackageName();
	// TNStyleManager.getInstance().addOnSkinStyleChangedListener(new
	// OnSkinStyleChangedListener() {
	// @Override
	// public void onSkinStyleChanged(SdkResourcesUtils.SkinStyle skinStyle) {
	// setStyle(skinStyle);
	// }
	// });
	// setStyle(TNStyleManager.getInstance().getCurrentStyle());
	// mInflater = LayoutInflater.from(mContext);
	// }

	public static void init(Context context) {
		mContext = context.getApplicationContext();
		mResources = mContext.getResources();
		mPackageName = mContext.getPackageName();
		// TNStyleManager.getInstance().addOnSkinStyleChangedListener(new
		// OnSkinStyleChangedListener() {
		// @Override
		// public void onSkinStyleChanged(SdkResourcesUtils.SkinStyle skinStyle)
		// {
		// setStyle(skinStyle);
		// }
		// });
		// setStyle(TNStyleManager.getInstance().getCurrentStyle());
		mInflater = LayoutInflater.from(mContext);
	}

	// private static void setStyle(SdkResourcesUtils.SkinStyle style) {
	// mResIdMap.clear();
	// mStyle = style;
	// }
	//
	// private static SdkResourcesUtils.SkinStyle getStyle(){
	// return mStyle;
	// }

	public static void clearCacheResId() {
		mResIdMap.clear();
	}

	private static String getResourceNameById(int resId) {
		String name = "";
		if (mResources != null) {
			try {
				name = mResources.getResourceEntryName(resId);
			} catch (Resources.NotFoundException e) {
				name = "";
			}
		}
		return name;
	}

	public static Drawable getDrawable(int resId) {
		// return getDrawable(resId, mStyle);
		Drawable d;
		try {
			d = mResources.getDrawable(resId);
		} catch (Throwable e) {
			d = null;
		}
		return d;
	}

//	private static Drawable getDrawable(int resId, SdkResourcesUtils.SkinStyle style) {
//		if (mResources == null || style == null) {
//			return null;
//		}
//		if (style == SdkResourcesUtils.SkinStyle.night) {
//			int cacheResId = mResIdMap.get(resId, -1);
//			if (cacheResId != -1) {
//				resId = cacheResId;
//			} else {
//				String resName = getResourceNameById(resId);
//				resName = resName + SUFFIX_NIGHT_MODEL;
//				int resIdInNight = mResources.getIdentifier(resName, "drawable", mPackageName);
//				if (resIdInNight != 0) {
//					mResIdMap.put(resId, resIdInNight);
//				}
//				resId = resIdInNight == 0 ? resId : resIdInNight;
//			}
//		}
//
//		Drawable d;
//		try {
//			d = mResources.getDrawable(resId);
//		} catch (Throwable e) {
//			d = null;
//		}
//		return d;
//	}

	public static int getColor(int resId) {
		int color = 0;
		try {
			color = mResources.getColor(resId);
		} catch (Resources.NotFoundException e) {
			color = 0;
		}
		return color;
//		return getColor(resId, mStyle);
	}

//	private static int getColor(int resId, SdkResourcesUtils.SkinStyle style) {
//		if (mResources == null) {
//			return 0;
//		}
//		if (style == SdkResourcesUtils.SkinStyle.night) {
//			int cacheResId = mResIdMap.get(resId, -1);
//			if (cacheResId != -1) {
//				resId = cacheResId;
//			} else {
//				String resName = getResourceNameById(resId);
//				resName = resName + SUFFIX_NIGHT_MODEL;
//				int resIdInNight = mResources.getIdentifier(resName, "color", mPackageName);
//				if (resIdInNight != 0) {
//					mResIdMap.put(resId, resIdInNight);
//				}
//				resId = resIdInNight == 0 ? resId : resIdInNight;
//			}
//		}
//		int color = 0;
//		try {
//			color = mResources.getColor(resId);
//		} catch (Resources.NotFoundException e) {
//			color = 0;
//		}
//		return color;
//	}

	public static String getString(int resId) {
		if (mResources == null) {
			return "";
		}
		String retStr;
		try {
			retStr = mResources.getString(resId);
		} catch (Resources.NotFoundException e) {
			retStr = "";
		}
		return retStr;
	}

	public static String getString(int resId, Object... format) {
		if (mResources == null) {
			return "";
		}
		String retStr;
		try {
			retStr = mResources.getString(resId, format);
		} catch (Resources.NotFoundException e) {
			retStr = "";
		}
		return retStr;
	}

	public static int getDimension(int resId) {
		if (mResources == null) {
			return 0;
		}
		int retDim;
		try {
			retDim = (int) mResources.getDimension(resId);
		} catch (Resources.NotFoundException e) {
			retDim = 0;
		}
		return retDim;
	}

	public static String[] getStringArray(int resId) {
		if (mResources == null) {
			return null;
		}
		String[] retArray;
		try {
			retArray = mResources.getStringArray(resId);
		} catch (Resources.NotFoundException e) {
			retArray = null;
		}
		return retArray;
	}

	public static boolean getBoolean(int resId) {
		if (mResources == null) {
			return false;
		}
		boolean retBol;
		try {
			retBol = mResources.getBoolean(resId);
		} catch (Resources.NotFoundException e) {
			retBol = false;
		}
		return retBol;
	}

	public static ColorStateList getColorStateList(int resId) {
		ColorStateList colorStateList = null;
		if (mResources == null) {
			return null;
		}
		try {
			colorStateList = mResources.getColorStateList(resId);
		} catch (Resources.NotFoundException e) {
			colorStateList = null;
		}
		return colorStateList;
	}

	public static Bitmap getBitmap(int resId) {
		if (mResources == null) {
			return null;
		}
		Bitmap retBmp;
		try {
			retBmp = BitmapFactory.decodeResource(mResources, resId);
		} catch (Throwable e) {
			retBmp = null;
		}
		return retBmp;
	}

	public static View inflate(int resId, ViewGroup viewGroup) {
		return mInflater.inflate(resId, viewGroup);
	}

	public static View inflate(int resId, ViewGroup viewGroup, boolean isAttachedRoot) {
		return mInflater.inflate(resId, viewGroup, isAttachedRoot);
	}

	public static Animation loadAnimation(int resId) {
		return AnimationUtils.loadAnimation(mContext, resId);
	}

	public static Interpolator loadInterpolator(int resId) {
		return AnimationUtils.loadInterpolator(mContext, resId);
	}

	public static LayoutAnimationController loadLayoutAnimation(int resId) {
		return AnimationUtils.loadLayoutAnimation(mContext, resId);
	}

	public static void setBackgroud(View view, int resId) {
		if (view == null) {
			return;
		}
//		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//			view.setBackground(getDrawable(resId));
//		} else {
//			view.setBackgroundDrawable(getDrawable(resId));
//		}
		view.setBackgroundDrawable(getDrawable(resId));
	}

	public static void setImageSrc(ImageView view, int resId) {
		if (view == null) {
			return;
		}
		view.setImageDrawable(getDrawable(resId));
	}

	public static void setTextColor(TextView view, int resId) {
		if (view == null) {
			return;
		}
		view.setTextColor(getColor(resId));
	}

	public static void setTextColor(Button view, int resId) {
		if (view == null) {
			return;
		}
		view.setTextColor(getColor(resId));
	}

	public static void setBackgroudColor(View view, int resId) {
		if (view == null) {
			return;
		}
		view.setBackgroundColor(getColor(resId));
	}

}
