package com.bnrc.bnrcbus.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.List;

public class GetToMarket {

	public static Intent getIntent(Context paramContext) {
		StringBuilder localStringBuilder = new StringBuilder()
				.append("market://details?id=");
		String str = paramContext.getPackageName();
		localStringBuilder.append(str);
		Uri localUri = Uri.parse(localStringBuilder.toString());
		/*根据localUri打开对应activity*/
		return new Intent("android.intent.action.VIEW", localUri);
	}

	public static void start(Context paramContext, String paramString) {
		Uri localUri = Uri.parse(paramString);
		Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
		localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		paramContext.startActivity(localIntent);
	}

	public static boolean judge(Context paramContext, Intent paramIntent) {
		List localList = paramContext.getPackageManager()
				.queryIntentActivities(paramIntent,
						PackageManager.GET_INTENT_FILTERS);
		if ((localList != null) && (localList.size() > 0)) {
			return false;
		} else {
			return true;
		}
	}
	// ---------------------调用代码--------------------------------
	// GetToMarket gu = new GetToMarket();
	// Intent i = gu.getIntent(mContext);
	// boolean b = gu.judge(mContext, i);
	// if (b == false) {
	// startActivity(i);
	// }
}
