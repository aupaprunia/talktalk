package com.hackillionis.talktalk.live.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.hackillionis.talktalk.live.Constants;


public class PrefManager {
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }
}
