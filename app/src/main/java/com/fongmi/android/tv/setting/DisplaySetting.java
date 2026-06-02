package com.fongmi.android.tv.setting;

import com.github.catvod.utils.Prefers;

public class DisplaySetting {

    private static final String KEY_SHOW_TIME = "display_show_time";
    private static final String KEY_SHOW_SPEED = "display_show_speed";
    private static final String KEY_SHOW_PROGRESS = "display_show_progress";

    public static boolean isShowTime() {
        return Prefers.getBoolean(KEY_SHOW_TIME, true);
    }

    public static void putShowTime(boolean show) {
        Prefers.put(KEY_SHOW_TIME, show);
    }

    public static boolean isShowSpeed() {
        return Prefers.getBoolean(KEY_SHOW_SPEED, true);
    }

    public static void putShowSpeed(boolean show) {
        Prefers.put(KEY_SHOW_SPEED, show);
    }

    public static boolean isShowProgress() {
        return Prefers.getBoolean(KEY_SHOW_PROGRESS, true);
    }

    public static void putShowProgress(boolean show) {
        Prefers.put(KEY_SHOW_PROGRESS, show);
    }
}
