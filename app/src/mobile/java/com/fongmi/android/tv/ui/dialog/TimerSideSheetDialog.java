package com.fongmi.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.DialogTimerBinding;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Timer;
import com.fongmi.android.tv.utils.Util;

import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerSideSheetDialog extends BaseSideSheetDialog implements Timer.Callback {

    private final StringBuilder builder;
    private final Formatter formatter;
    private DialogTimerBinding binding;

    public TimerSideSheetDialog() {
        builder = new StringBuilder();
        formatter = new Formatter(builder, Locale.getDefault());
    }

    public static TimerSideSheetDialog create() {
        return new TimerSideSheetDialog();
    }

    public void show(FragmentActivity activity) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments()) if (f instanceof TimerSideSheetDialog) return;
        show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogTimerBinding.inflate(inflater, container, false);
    }

    @Override
    protected int getWidth() {
        int minWidth = ResUtil.dp2px(150);
        int maxWidth = ResUtil.getScreenWidth() / 3;
        return Math.min(minWidth, maxWidth);
    }

    @Override
    protected boolean isCenterVertical() {
        return true;
    }

    @Override
    protected void initView() {
        onTick(Timer.get().getTick());
        binding.list.setVisibility(Timer.get().isRunning() ? View.GONE : View.VISIBLE);
        binding.timer.setVisibility(Timer.get().isRunning() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initEvent() {
        Timer.get().setCallback(this);
        binding.delay.setOnClickListener(this::onDelay);
        binding.reset.setOnClickListener(this::onReset);
        binding.time1.setOnClickListener(this::setTimer);
        binding.time2.setOnClickListener(this::setTimer);
        binding.time3.setOnClickListener(this::setTimer);
        binding.time4.setOnClickListener(this::setTimer);
        binding.time5.setOnClickListener(this::setEpisodeTimer);
    }

    private void setTimer(View view) {
        int minutes = Integer.parseInt(view.getTag().toString());
        Timer.get().set(TimeUnit.MINUTES.toMillis(minutes));
        dismiss();
    }

    private void setEpisodeTimer(View view) {
        Timer.get().setEpisodeMode(true);
        dismiss();
    }

    private void onDelay(View view) {
        Timer.get().delay();
    }

    private void onReset(View view) {
        Timer.get().reset();
        dismiss();
    }

    @Override
    public void onTick(long tick) {
        binding.tick.setText(Util.format(builder, formatter, tick));
    }

    @Override
    public void onFinish() {
        dismiss();
    }

    @Override
    public void dismiss() {
        Timer.get().setCallback(null);
        super.dismiss();
    }
}
