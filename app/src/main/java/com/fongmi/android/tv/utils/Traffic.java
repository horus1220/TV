package com.fongmi.android.tv.utils;

import android.net.TrafficStats;
import android.view.View;
import android.widget.TextView;

import com.fongmi.android.tv.App;

import java.text.DecimalFormat;

public class Traffic {

    private static final DecimalFormat format = new DecimalFormat("#.0");
    private static final int UID = App.get().getApplicationInfo().uid;
    private static final String UNIT_KB = " KB/s";
    private static final String UNIT_MB = " MB/s";
    private static final String DEFAULT_SPEED = "0 KB/s";

    private static long lastTotalRxBytes;
    private static long lastTimeStamp;
    private static String cachedSpeedText = DEFAULT_SPEED;

    public static void setSpeed(TextView view) {
        if (TrafficStats.getUidRxBytes(UID) == TrafficStats.UNSUPPORTED) return;
        view.setVisibility(View.VISIBLE);
        updateSpeed();
        view.setText(cachedSpeedText);
    }

    private static void updateSpeed() {
        long nowTimeStamp = System.currentTimeMillis();
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(UID) / 1024;
        long speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / Math.max(nowTimeStamp - lastTimeStamp, 1);
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        cachedSpeedText = speed < 1000 ? speed + UNIT_KB : format.format(speed / 1024f) + UNIT_MB;
    }

    public static String getSpeedText() {
        // 返回上次计算的缓存速度，避免重复计算导致为0
        return cachedSpeedText;
    }

    public static void reset() {
        lastTotalRxBytes = 0;
        lastTimeStamp = 0;
        cachedSpeedText = DEFAULT_SPEED;
    }
}
