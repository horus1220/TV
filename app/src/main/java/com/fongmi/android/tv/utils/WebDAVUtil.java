package com.fongmi.android.tv.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.util.Base64;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

/**
 * WebDAV 操作工具类
 * 封装 Sardine 库实现文件上传、下载、列表等操作
 */
public class WebDAVUtil {

    private static final String TAG = "WebDAVUtil";

    /**
     * 创建已认证的 Sardine 实例（通过拦截器注入Basic Auth头实现预认证，兼容坚果云）
     */
    public static Sardine getSardine() {
        String username = WebDavConfig.getUsername();
        String password = WebDavConfig.getPassword();

        if (username != null && !username.isEmpty()) {
            // 构建Basic Auth凭据
            String credentials = username + ":" + (password != null ? password : "");
            String auth = "Basic " + Base64.encodeToString(credentials.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

            // 用拦截器在每个请求中预注入Authorization头
            Interceptor authInterceptor = chain -> {
                Request request = chain.request().newBuilder()
                        .header("Authorization", auth)
                        .build();
                return chain.proceed(request);
            };

            OkHttpSardine sardine = new OkHttpSardine(new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .protocols(java.util.Collections.singletonList(okhttp3.Protocol.HTTP_1_1))
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .build());
            sardine.setCredentials(username, password);
            return sardine;
        }

        return new OkHttpSardine();
    }

    /**
     * 获取完整的远程路径（无前导斜杠，避免与URL拼接产生双斜杠）
     */
    public static String getRemotePath() {
        String path = WebDavConfig.getPath();
        if (path.startsWith("/")) path = path.substring(1);
        if (!path.endsWith("/")) path += "/";
        return path;
    }

    /**
     * 检查连接是否可用
     */
    public static boolean testConnection() throws Exception {
        Sardine sardine = getSardine();
        String url = normalizeUrl(WebDavConfig.getUrl());
        String path = getRemotePath();
        
        // 尝试列出目录来验证连接
        try {
            List<DavResource> resources = sardine.list(url + path);
            return true;
        } catch (Exception e) {
            // 如果目录不存在，尝试创建
            if (e.getMessage() != null && (e.getMessage().contains("404") || e.getMessage().contains("Not Found"))) {
                sardine.createDirectory(url + path);
                return true;
            }
            throw e;
        }
    }

    /**
     * 上传备份文件到 WebDAV
     */
    public static void uploadBackup(File file, ProgressCallback callback) throws Exception {
        Sardine sardine = getSardine();
        String url = normalizeUrl(WebDavConfig.getUrl());
        String remotePath = getRemotePath();
        String remoteFile = url + remotePath + file.getName();

        // 确保远程目录存在
        ensureDirectory(sardine, url, remotePath);

        // 上传文件
        callback.onProgress(0, 100);
        sardine.put(remoteFile, file, null, true);
        callback.onProgress(100, 100);
        callback.onSuccess("上传完成: " + file.getName());
    }

    /**
     * 从 WebDAV 下载备份文件
     */
    public static File downloadBackup(String fileName, File localDir, ProgressCallback callback) throws Exception {
        Sardine sardine = getSardine();
        String url = normalizeUrl(WebDavConfig.getUrl());
        String remotePath = getRemotePath();
        String remoteFile = url + remotePath + fileName;
        File localFile = new File(localDir, fileName);

        // 下载文件
        try (InputStream is = sardine.get(remoteFile);
             FileOutputStream fos = new FileOutputStream(localFile)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            
            // 先获取远程文件大小（如果可能）
            long fileSize = -1;
            try {
                List<DavResource> resources = sardine.list(url + remotePath);
                for (DavResource res : resources) {
                    if (res.getPath().endsWith(fileName)) {
                        fileSize = res.getContentLength();
                        break;
                    }
                }
            } catch (Exception ignored) {}

            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                if (fileSize > 0) {
                    int progress = (int) ((totalBytesRead * 100) / fileSize);
                    callback.onProgress(progress, 100);
                }
            }
            
            callback.onSuccess("下载完成: " + fileName);
            return localFile;
        }
    }

    /**
     * 列出远程备份文件
     */
    public static List<RemoteFileInfo> listBackups() throws Exception {
        Sardine sardine = getSardine();
        String url = normalizeUrl(WebDavConfig.getUrl());
        String remotePath = getRemotePath();
        List<RemoteFileInfo> result = new ArrayList<>();

        List<DavResource> resources = sardine.list(url + remotePath);
        if (resources == null || resources.isEmpty()) return result;

        // 第一个资源是目录本身，跳过
        for (int i = 1; i < resources.size(); i++) {
            DavResource res = resources.get(i);
            String name = new File(res.getPath()).getName();
            // 只显示备份文件
            if (name.startsWith("tv") && (name.endsWith(".bk.gz"))) {
                RemoteFileInfo info = new RemoteFileInfo(
                        name,
                        res.getModified().getTime(),
                        res.getContentLength()
                );
                result.add(info);
            }
        }

        // 按修改时间倒序排列
        Collections.sort(result, (a, b) -> Long.compare(b.lastModified, a.lastModified));

        return result;
    }

    /**
     * 删除远程备份文件
     */
    public static void deleteBackup(String fileName) throws Exception {
        Sardine sardine = getSardine();
        String url = normalizeUrl(WebDavConfig.getUrl());
        String remotePath = getRemotePath();
        String remoteFile = url + remotePath + fileName;

        sardine.delete(remoteFile);
    }

    /**
     * 确保远程目录存在，不存在则递归创建（不使用exists检查，避免额外请求导致403）
     */
    private static void ensureDirectory(Sardine sardine, String baseUrl, String path) throws Exception {
        // 去除路径首尾斜杠，避免与baseUrl拼接产生双斜杠
        if (path.startsWith("/")) path = path.substring(1);
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

        // 确保baseUrl以/结尾
        String current = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";

        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            current += part + "/";
            try {
                sardine.createDirectory(current);
            } catch (Exception e) {
                // 405=已存在(MKCOL不允许), 409=冲突(已存在), 301/302=重定向(某些服务器), 都视为已存在
                String msg = e.getMessage();
                if (msg == null || !(msg.contains("405") || msg.contains("409") || msg.contains("301") || msg.contains("302")
                        || msg.toLowerCase().contains("exists") || msg.toLowerCase().contains("method not allowed")
                        || msg.contains("already exists"))) {
                    throw e;
                }
            }
        }
    }

    /**
     * 规范化URL，确保以/结尾
     */
    private static String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        url = url.trim();
        if (!url.endsWith("/")) url += "/";
        return url;
    }

    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        void onProgress(int progress, int max);
        void onSuccess(String message);
    }

    /**
     * 远程文件信息
     */
    public static class RemoteFileInfo {
        private String name;
        private long lastModified;
        private long size;

        public RemoteFileInfo(String name, long lastModified, long size) {
            this.name = name;
            this.lastModified = lastModified;
            this.size = size;
        }

        public String getName() { return name; }
        public long getLastModified() { return lastModified; }
        public long getSize() { return size; }

        public String getSizeString() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024));
        }

        public String getDateString() {
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date(lastModified));
        }
    }
}
