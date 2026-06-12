package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogWebdavRestoreBinding;
import com.github.catvod.utils.Path;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.WebDAVUtil;
import com.fongmi.android.tv.ui.adapter.WebDARestoreAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

/**
 * WebDAV 远程备份恢复对话框
 * 显示远程服务器上的备份文件列表，支持选择恢复
 */
public class WebDARestoreDialog {

    private DialogWebdavRestoreBinding mBinding;
    private BottomSheetDialog dialog;
    private Activity activity;
    private Callback callback;
    private WebDARestoreAdapter adapter;

    public static WebDARestoreDialog create() {
        return new WebDARestoreDialog();
    }

    public WebDARestoreDialog show(Context context, Callback callback) {
        this.activity = (Activity) context;
        this.callback = callback;

        mBinding = DialogWebdavRestoreBinding.inflate(LayoutInflater.from(context));
        dialog = new BottomSheetDialog(context);
        dialog.setContentView(mBinding.getRoot());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        initView();
        
        // 加载远程备份列表
        loadRemoteBackups();

        dialog.show();
        BottomSheetBehavior.from((View) mBinding.getRoot().getParent()).setState(BottomSheetBehavior.STATE_EXPANDED);

        return this;
    }

    private void initView() {
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new WebDARestoreAdapter();
        mBinding.recyclerView.setAdapter(adapter);

        // 设置点击事件
        adapter.setOnItemClickListener(this::onItemClick);
        adapter.setOnDeleteClickListener(this::onDeleteClick);
    }

    /**
     * 加载远程备份列表
     */
    private void loadRemoteBackups() {
        Notify.progress(activity);
        
        new Thread(() -> {
            try {
                List<WebDAVUtil.RemoteFileInfo> backups = WebDAVUtil.listBackups();
                activity.runOnUiThread(() -> {
                    Notify.dismiss();
                    if (backups.isEmpty()) {
                        Notify.show(R.string.webdav_no_backup);
                        dismiss();
                    } else {
                        adapter.setData(backups);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> {
                    Notify.dismiss();
                    Notify.show(e.getMessage());
                });
            }
        }).start();
    }

    /**
     * 点击备份文件，执行恢复
     */
    private void onItemClick(WebDAVUtil.RemoteFileInfo item) {
        // 确认对话框
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_confirm_restore)
                .setMessage(item.getName())
                .setPositiveButton(R.string.setting_restore, (dialog, which) -> downloadAndRestore(item))
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    /**
     * 下载并恢复备份
     */
    private void downloadAndRestore(WebDAVUtil.RemoteFileInfo item) {
        Notify.progress(activity);
        
        new Thread(() -> {
            final File[] localFile = new File[1];
            try {
                // 1. 从 WebDAV 下载到本地缓存，返回实际文件路径
                localFile[0] = WebDAVUtil.downloadBackup(
                        item.getName(),
                        Path.cache(),
                        new WebDAVUtil.ProgressCallback() {
                            @Override
                            public void onProgress(int progress, int max) {
                            }

                            @Override
                            public void onSuccess(String message) {
                                activity.runOnUiThread(() -> {
                                    Notify.dismiss();
                                    // 2. 用下载的实际文件执行恢复
                                    AppDatabase.restore(localFile[0], callback);
                                });
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> {
                    Notify.dismiss();
                    Notify.show(e.getMessage());
                    if (callback != null) callback.error();
                });
            }
        }).start();
    }

    /**
     * 删除远程备份
     */
    private void onDeleteClick(WebDAVUtil.RemoteFileInfo item) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.dialog_confirm_delete)
                .setMessage(item.getName())
                .setPositiveButton(R.string.dialog_positive, (dialog, which) -> deleteBackup(item))
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    /**
     * 删除远程备份文件
     */
    private void deleteBackup(WebDAVUtil.RemoteFileInfo item) {
        new Thread(() -> {
            try {
                WebDAVUtil.deleteBackup(item.getName());
                activity.runOnUiThread(() -> {
                    Notify.show(R.string.webdav_delete_success);
                    // 刷新列表
                    loadRemoteBackups();
                });
            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> {
                    Notify.show(e.getMessage());
                });
            }
        }).start();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
