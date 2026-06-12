package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.WebDAVUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * WebDAV 远程备份文件列表适配器
 */
public class WebDARestoreAdapter extends RecyclerView.Adapter<WebDARestoreAdapter.ViewHolder> {

    private List<WebDAVUtil.RemoteFileInfo> items = new ArrayList<>();
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(WebDAVUtil.RemoteFileInfo item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(WebDAVUtil.RemoteFileInfo item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView dateText;
        TextView sizeText;
        View deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.item_name);
            dateText = itemView.findViewById(R.id.item_date);
            sizeText = itemView.findViewById(R.id.item_size);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setData(List<WebDAVUtil.RemoteFileInfo> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_webdav_backup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WebDAVUtil.RemoteFileInfo item = items.get(position);
        
        holder.nameText.setText(item.getName());
        holder.dateText.setText(item.getDateString());
        holder.sizeText.setText(item.getSizeString());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
        
        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
