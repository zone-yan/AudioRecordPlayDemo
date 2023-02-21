package com.ormosia.audio;

import android.text.TextUtils;
import android.util.Log;


public class Logger {

    /* 调试级别
     * */
    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            if (TextUtils.isEmpty(message)) {
                message = "**null**";
            }
            int logStrLength = message.length();
            int maxLogSize = 1000;
            for (int i = 0; i <= logStrLength / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > logStrLength ? logStrLength : end;
                Log.i(tag, message.substring(start, end));
            }
        }
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }
    public static void e( String message) {
        e("Audio-Library", message);
    }
}
