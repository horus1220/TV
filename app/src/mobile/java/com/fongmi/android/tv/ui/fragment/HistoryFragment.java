package com.fongmi.android.tv.ui.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.databinding.FragmentHistoryBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.ui.adapter.HistoryAdapter;
import com.fongmi.android.tv.ui.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class HistoryFragment extends BaseFragment implements HistoryAdapter.OnClickListener {

    private FragmentHistoryBinding mBinding;
    private HistoryAdapter mAdapter;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        EventBus.getDefault().register(this);
        return mBinding = FragmentHistoryBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mAdapter = new HistoryAdapter(this);
        mAdapter.setSize(Product.getSpec(requireContext()));
        mBinding.recycler.setHasFixedSize(true);
        mBinding.recycler.setItemAnimator(null);
        int columnCount = Product.getColumn(requireContext());
        mBinding.recycler.setLayoutManager(new GridLayoutManager(getContext(), columnCount));
        mBinding.recycler.setAdapter(mAdapter);
        mBinding.progressLayout.showContent();
    }

    @Override
    protected void initEvent() {
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event.getType().equals(RefreshEvent.Type.HISTORY) && isAdded()) loadData();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded()) loadData();
    }

    private void loadData() {
        List<History> items = History.get();
        if (items.isEmpty()) {
            mBinding.progressLayout.showEmpty();
        } else {
            mBinding.progressLayout.showContent();
            mAdapter.setItems(items);
        }
    }

    @Override
    protected void initMenu() {
    }

    @Override
    public boolean canBack() {
        return false;
    }

    @Override
    public void onItemClick(History item) {
        VideoActivity.cast(requireActivity(), item);
    }

    @Override
    public void onItemDelete(History item) {
        item.delete();
        loadData();
    }

    @Override
    public boolean onLongClick() {
        mAdapter.setDelete(!mAdapter.isDelete());
        return true;
    }
}
