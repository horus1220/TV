package com.fongmi.android.tv;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.HandlerCompat;

import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.WebDavConfig;
import com.fongmi.hook.Hook;
import com.github.catvod.Init;
import com.github.catvod.utils.Prefers;
import com.google.gson.Gson;

public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private static volatile App instance;

    private static final long AUTO_BACKUP_INTERVAL = 10 * 60 * 1000L; // 自动备份最小间隔5分钟
    private static final String KEY_LAST_AUTO_BACKUP = "lastAutoBackupTime";

    private final Handler handler;
    private final Gson gson;
    private final long time;

    private Activity activity;
    private Hook hook;
    private int activityCount;

    public App() {
        instance = this;
        gson = new Gson();
        time = System.currentTimeMillis();
        handler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    public static App get() {
        return instance;
    }

    public static Gson gson() {
        return get().gson;
    }

    public static long time() {
        return get().time;
    }

    public static Activity activity() {
        return get().activity;
    }

    public static void post(Runnable runnable) {
        get().handler.post(runnable);
    }

    public static void post(Runnable runnable, long delayMillis) {
        get().handler.removeCallbacks(runnable);
        if (delayMillis >= 0) get().handler.postDelayed(runnable, delayMillis);
    }

    public static void removeCallbacks(Runnable runnable) {
        get().handler.removeCallbacks(runnable);
    }

    public static void removeCallbacks(Runnable... runnable) {
        for (Runnable r : runnable) get().handler.removeCallbacks(r);
    }

    public void setHook(Hook hook) {
        this.hook = hook;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Init.set(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notify.createChannel();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public PackageManager getPackageManager() {
        return hook != null ? hook : getBaseContext().getPackageManager();
    }

    @Override
    public String getPackageName() {
        return hook != null ? hook.getPackageName() : getBaseContext().getPackageName();
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (activity != activity()) this.activity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (activity == activity()) this.activity = null;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activityCount++;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activityCount--;
        // 所有 Activity 都已停止（应用进入后台），触发 WebDAV 自动备份
        if (activityCount == 0) autoBackupToWebDAV();
    }

    /**
     * 应用退出后台时自动备份到 WebDAV（仅当已配置且距上次备份超过间隔时间）
     */
    private void autoBackupToWebDAV() {
        if (!WebDavConfig.isValid()) return;
        long lastTime = Prefers.getLong(KEY_LAST_AUTO_BACKUP, 0);
        if (System.currentTimeMillis() - lastTime < AUTO_BACKUP_INTERVAL) return;
        Prefers.put(KEY_LAST_AUTO_BACKUP, System.currentTimeMillis());
        AppDatabase.backupToWebDAV(new com.fongmi.android.tv.impl.Callback() {
            @Override public void success() { }
            @Override public void error() { }
        });
    }
}