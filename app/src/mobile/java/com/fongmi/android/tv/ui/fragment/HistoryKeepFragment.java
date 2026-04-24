package com.fongmi.android.tv.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.bean.Keep;
import com.fongmi.android.tv.databinding.FragmentHistoryKeepBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.ui.activity.SearchActivity;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.ui.adapter.HistoryAdapter;
import com.fongmi.android.tv.ui.adapter.KeepAdapter;
import com.fongmi.android.tv.ui.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HistoryKeepFragment extends BaseFragment implements HistoryAdapter.OnClickListener, KeepAdapter.OnClickListener {

    private FragmentHistoryKeepBinding mBinding;
    private HistoryAdapter mHistoryAdapter;
    private KeepAdapter mKeepAdapter;
    private int mCurrentTab = 0;

    public static HistoryKeepFragment newInstance() {
        return new HistoryKeepFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentHistoryKeepBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        initTabLayout();
        initRecyclerViews();
        showProgress();
        loadData();
    }

    @Override
    protected void initEvent() {
        // no-op
    }

    private void initTabLayout() {
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(R.string.app_history));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(R.string.app_keep));
        mBinding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                mCurrentTab = tab.getPosition();
                switchTab();
                loadData();
            }
            @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });
    }

    private void initRecyclerViews() {
        int column = getColumn();
        int[] spec = getSpec();

        mBinding.historyRecycler.setHasFixedSize(true);
        mBinding.historyRecycler.setLayoutManager(new GridLayoutManager(requireContext(), column));
        mBinding.historyRecycler.setAdapter(mHistoryAdapter = new HistoryAdapter(this));
        if (spec != null) mHistoryAdapter.setSize(spec);

        mBinding.keepRecycler.setHasFixedSize(true);
        mBinding.keepRecycler.setLayoutManager(new GridLayoutManager(requireContext(), column));
        mBinding.keepRecycler.setAdapter(mKeepAdapter = new KeepAdapter(this));
        if (spec != null) mKeepAdapter.setSize(spec);
    }

    private void switchTab() {
        if (mCurrentTab == 0) {
            mBinding.historyRecycler.setVisibility(View.VISIBLE);
            mBinding.keepRecycler.setVisibility(View.GONE);
        } else {
            mBinding.historyRecycler.setVisibility(View.GONE);
            mBinding.keepRecycler.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        if (mCurrentTab == 0) getHistory();
        else getKeep();
    }

    private void refreshData() {
        if (mCurrentTab == 0) getHistory();
        else getKeep();
    }

    private int getColumn() {
        try {
            Object product = Class.forName("com.fongmi.android.tv.Product").newInstance();
            return (int) product.getClass().getMethod("getColumn", android.content.Context.class).invoke(product, requireContext());
        } catch (Exception e) {
            return 3;
        }
    }

    private int[] getSpec() {
        try {
            Object product = Class.forName("com.fongmi.android.tv.Product").newInstance();
            return (int[]) product.getClass().getMethod("getSpec", android.content.Context.class).invoke(product, requireContext());
        } catch (Exception e) {
            return null;
        }
    }

    private void showProgress() { mBinding.progress.getRoot().setVisibility(View.VISIBLE); }
    private void hideProgress() { mBinding.progress.getRoot().setVisibility(View.GONE); }

    private void getHistory() {
        mHistoryAdapter.setItems(History.get(), hasChange -> {
            hideProgress();
            if (hasChange) mBinding.historyRecycler.scrollToPosition(0);
        });
    }

    private void getKeep() {
        mKeepAdapter.setItems(Keep.getVod(), () -> hideProgress());
    }

    // ======== History click ========
    @Override
    public void onItemClick(History item) {
        VideoActivity.start(requireActivity(), item.getSiteKey(), item.getVodId(), item.getVodName(), item.getVodPic());
    }

    @Override
    public void onItemDelete(History item) {
        mHistoryAdapter.remove(item.delete(), () -> {
            if (mHistoryAdapter.getItemCount() == 0) mHistoryAdapter.setDelete(false);
        });
    }

    @Override
    public boolean onLongClick() {
        if (mCurrentTab == 0 && mHistoryAdapter != null) {
            mHistoryAdapter.setDelete(!mHistoryAdapter.isDelete());
            return true;
        } else if (mCurrentTab == 1 && mKeepAdapter != null) {
            mKeepAdapter.setDelete(!mKeepAdapter.isDelete());
            return true;
        }
        return false;
    }

    // ======== Keep click ========
    public void onItemClick(Keep item) {
        Config config = Config.find(item.getCid());
        if (config == null) SearchActivity.start(requireActivity(), item.getVodName());
        else if (item.getCid() != VodConfig.getCid()) {
            VodConfig.load(config, new Callback() {
                @Override public void success() { VideoActivity.start(requireActivity(), item.getSiteKey(), item.getVodId(), item.getVodName(), item.getVodPic()); }
                @Override public void error(String msg) {}
            });
        } else VideoActivity.start(requireActivity(), item.getSiteKey(), item.getVodId(), item.getVodName(), item.getVodPic());
    }

    public void onItemDelete(Keep item) {
        mKeepAdapter.remove(item.delete(), () -> {
            if (mKeepAdapter.getItemCount() == 0) mKeepAdapter.setDelete(false);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event.getType() == RefreshEvent.Type.HISTORY && mCurrentTab == 0) getHistory();
        else if (event.getType() == RefreshEvent.Type.KEEP && mCurrentTab == 1) getKeep();
    }

    @Override
    public boolean canBack() {
        if (mCurrentTab == 0 && mHistoryAdapter != null && mHistoryAdapter.isDelete()) {
            mHistoryAdapter.setDelete(false);
            return true;
        }
        if (mCurrentTab == 1 && mKeepAdapter != null && mKeepAdapter.isDelete()) {
            mKeepAdapter.setDelete(false);
            return true;
        }
        return super.canBack();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mHistoryAdapter = null;
        mKeepAdapter = null;
    }
}
