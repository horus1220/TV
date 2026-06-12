package com.fongmi.android.tv.utils;

import com.fongmi.android.tv.BuildConfig;
import com.github.catvod.utils.Prefers;

/**
 * WebDAV 配置管理类
 * 存储 WebDAV 服务器连接信息到 SharedPreferences
 */
public class WebDavConfig {

    private static final String KEY_URL = "webdav_url";
    private static final String KEY_USERNAME = "webdav_username";
    private static final String KEY_PASSWORD = "webdav_password";
    private static final String KEY_PATH = "webdav_path";

    /**
     * 获取服务器URL
     */
    public static String getUrl() {
        return Prefers.getString(KEY_URL, "");
    }

    public static void setUrl(String url) {
        Prefers.put(KEY_URL, url);
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        return Prefers.getString(KEY_USERNAME, "");
    }

    public static void setUsername(String username) {
        Prefers.put(KEY_USERNAME, username);
    }

    /**
     * 获取密码
     */
    public static String getPassword() {
        return Prefers.getString(KEY_PASSWORD, "");
    }

    public static void setPassword(String password) {
        Prefers.put(KEY_PASSWORD, password);
    }

    /**
     * 获取远程路径（如 /TV_Release/ 或 /TV_Debug/）
     */
    public static String getPath() {
        return Prefers.getString(KEY_PATH, "/" + (BuildConfig.DEBUG ? "TV_Debug" : "TV_Release") + "/");
    }

    public static void setPath(String path) {
        Prefers.put(KEY_PATH, path);
    }

    /**
     * 检查配置是否完整
     */
    public static boolean isValid() {
        String url = getUrl();
        return url != null && !url.isEmpty();
    }

    /**
     * 清除所有配置
     */
    public static void clear() {
        setUrl("");
        setUsername("");
        setPassword("");
        setPath("/" + (BuildConfig.DEBUG ? "TV_Debug" : "TV_Release") + "/");
    }
}
