package me.roger.zen.utils;

import me.roger.zen.application.ZenApplication;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

public class ZenAdsManager {
	private static final int ZEN_TIME_TO_SHOW = 10;
	private static ZenAdsManager instance;
	private Context mContext;
	
	private int counter;

	public static ZenAdsManager getInstance(Context context) {
		if (instance == null) {
			instance = new ZenAdsManager();
		}
		instance.mContext = context;
		return instance;
	}

	public ZenAdsManager() {
		counter = 1;
	}

	// ================================================================================
	// MobiSage Stuff
	// ================================================================================
	
}
