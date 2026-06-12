package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogWebdavBinding;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.WebDavConfig;
import com.fongmi.android.tv.utils.WebDAVUtil;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * WebDAV 配置设置对话框
 * 用于配置 WebDAV 服务器地址、用户名、密码和路径
 */
public class WebDAVDialog {

    private DialogWebdavBinding mBinding;
    private BottomSheetDialog dialog;
    private Activity activity;

    public static WebDAVDialog create() {
        return new WebDAVDialog();
    }

    public WebDAVDialog show(Context context, Callback callback) {
        this.activity = (Activity) context;
        
        mBinding = DialogWebdavBinding.inflate(LayoutInflater.from(context));
        dialog = new BottomSheetDialog(context);
        dialog.setContentView(mBinding.getRoot());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // 加载已保存的配置
        loadConfig();
        
        // 初始化事件
        initView();
        
        dialog.show();
        BottomSheetBehavior.from((View) mBinding.getRoot().getParent()).setState(BottomSheetBehavior.STATE_EXPANDED);
        
        return this;
    }

    private void loadConfig() {
        mBinding.editUrl.setText(WebDavConfig.getUrl());
        mBinding.editUsername.setText(WebDavConfig.getUsername());
        mBinding.editPassword.setText(WebDavConfig.getPassword());
        mBinding.editPath.setText(WebDavConfig.getPath());
    }

    private void initView() {
        mBinding.btnSave.setOnClickListener(v -> saveConfig());
        mBinding.btnTest.setOnClickListener(v -> testConnection());
        mBinding.btnClear.setOnClickListener(v -> clearConfig());
    }

    /**
     * 保存配置
     */
    private void saveConfig() {
        String url = mBinding.editUrl.getText().toString().trim();
        String username = mBinding.editUsername.getText().toString().trim();
        String password = mBinding.editPassword.getText().toString();
        String path = mBinding.editPath.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.webdav_url_empty);
            return;
        }

        // 规范化路径
        if (TextUtils.isEmpty(path)) {
            path = "/TV/";
        } else {
            if (!path.startsWith("/")) path = "/" + path;
            if (!path.endsWith("/")) path += "/";
        }

        // 保存配置
        WebDavConfig.setUrl(url);
        WebDavConfig.setUsername(username);
        WebDavConfig.setPassword(password);
        WebDavConfig.setPath(path);

        Notify.show(R.string.webdav_config_saved);
        dismiss();
    }

    /**
     * 测试连接
     */
    private void testConnection() {
        String url = mBinding.editUrl.getText().toString().trim();
        String username = mBinding.editUsername.getText().toString().trim();
        String password = mBinding.editPassword.getText().toString();

        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.webdav_url_empty);
            return;
        }

        // 临时保存配置用于测试
        WebDavConfig.setUrl(url);
        WebDavConfig.setUsername(username);
        WebDavConfig.setPassword(password);

        Notify.progress(activity);
        
        new Thread(() -> {
            try {
                boolean success = WebDAVUtil.testConnection();
                activity.runOnUiThread(() -> {
                    Notify.dismiss();
                    if (success) {
                        Notify.show(R.string.webdav_test_success);
                    } else {
                        Notify.show(R.string.webdav_test_fail);
                    }
                });
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    Notify.dismiss();
                    Notify.show(e.getMessage());
                });
            }
        }).start();
    }

    /**
     * 清除配置
     */
    private void clearConfig() {
        WebDavConfig.clear();
        loadConfig();
        Notify.show(R.string.webdav_config_cleared);
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
