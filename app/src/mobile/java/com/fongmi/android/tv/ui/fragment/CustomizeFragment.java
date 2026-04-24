package com.fongmi.android.tv.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.FragmentCustomizeBinding;
import com.fongmi.android.tv.ui.base.BaseFragment;

public class CustomizeFragment extends BaseFragment {

    private FragmentCustomizeBinding mBinding;

    public static CustomizeFragment newInstance() {
        return new CustomizeFragment();
    }

    private String getSwitch(boolean value) {
        return getString(value ? R.string.setting_on : R.string.setting_off);
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentCustomizeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mBinding.showProgressText.setText(getSwitch(Setting.isShowProgress()));
        mBinding.showTimeText.setText(getSwitch(Setting.isShowTime()));
        mBinding.showSpeedText.setText(getSwitch(Setting.isShowSpeed()));
    }

    @Override
    protected void initEvent() {
        mBinding.showProgress.setOnClickListener(this::setShowProgress);
        mBinding.showTime.setOnClickListener(this::setShowTime);
        mBinding.showSpeed.setOnClickListener(this::setShowSpeed);
    }

    private void setShowProgress(View view) {
        Setting.putShowProgress(!Setting.isShowProgress());
        mBinding.showProgressText.setText(getSwitch(Setting.isShowProgress()));
    }

    private void setShowTime(View view) {
        Setting.putShowTime(!Setting.isShowTime());
        mBinding.showTimeText.setText(getSwitch(Setting.isShowTime()));
    }

    private void setShowSpeed(View view) {
        Setting.putShowSpeed(!Setting.isShowSpeed());
        mBinding.showSpeedText.setText(getSwitch(Setting.isShowSpeed()));
    }
}
