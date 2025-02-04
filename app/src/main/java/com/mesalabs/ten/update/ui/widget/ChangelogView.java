package com.mesalabs.ten.update.ui.widget;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.icu.text.SimpleDateFormat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.ParseException;
import java.util.Locale;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.mesalabs.cerberus.utils.StateUtils;
import com.mesalabs.cerberus.utils.ViewUtils;
import com.mesalabs.ten.update.R;
import com.mesalabs.ten.update.ota.utils.PreferencesUtils;
import com.samsung.android.ui.util.SeslRoundedCorner;

/*
 * 십 Update
 *
 * Coded by BlackMesa123 @2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

public class ChangelogView extends LinearLayout {
    private Context mContext;
    SeslRoundedCorner mSeslRoundedCorner;

    private FrameLayout mMainContainer;
    private TextView mSuperText;
    private LinearLayout mHeaderContainer;
    private LinearLayout mHeaderImgContainer;
    private TextView mErrorText;

    private AlphaAnimation mFadeInAnim;

    public ChangelogView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        setBackgroundColor(getResources().getColor(R.color.mesa_changelogview_super_bg_color, mContext.getTheme()));
        setOrientation(VERTICAL);

        mSeslRoundedCorner = new SeslRoundedCorner(mContext, true);
        mSeslRoundedCorner.setRoundedCorners(15);
        mSeslRoundedCorner.setRoundedCornerColor(15, ViewUtils.getRoundAndBgColor(mContext));

        init();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mSeslRoundedCorner.drawRoundedCorner(canvas);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setHeaderImgContainerDimen(newConfig);
    }

    @Override
    public void setOnClickListener(View.OnClickListener ocl) {
        mHeaderContainer.setOnClickListener(ocl);
    }

    private void init() {
        removeAllViews();

        LayoutTransition itemLayoutTransition = new LayoutTransition();
        Animator scaleDown = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("alpha", 1, 0));
        Animator scaleUp = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("alpha", 0, 1));
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp);
        itemLayoutTransition.setDuration(LayoutTransition.APPEARING, 650);
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        itemLayoutTransition.setDuration(LayoutTransition.CHANGE_APPEARING, 150);
        itemLayoutTransition.setInterpolator(LayoutTransition.CHANGE_APPEARING, new FastOutSlowInInterpolator());
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, scaleDown);
        itemLayoutTransition.setDuration(LayoutTransition.DISAPPEARING, 80);

        LayoutInflater.from(mContext).inflate(R.layout.mesa_ota_view_changelogview_layout, this);

        mSuperText = findViewById(R.id.mesa_supertextview_changelogview);
        mMainContainer = findViewById(R.id.mesa_container_changelogview);
        mHeaderContainer = findViewById(R.id.mesa_header_changelogview);
        mHeaderImgContainer = findViewById(R.id.mesa_img_container_header_changelogview);
        mErrorText = findViewById(R.id.mesa_errortextview_changelogview);

        setLayoutTransition(itemLayoutTransition);
        mMainContainer.setLayoutTransition(itemLayoutTransition);
        setHeaderImgContainerDimen(getResources().getConfiguration());

        initAnimationFields();
    }

    private void initAnimationFields() {
        mFadeInAnim = new AlphaAnimation(0.0f, 1.0f);
        mFadeInAnim.setDuration(500);
    }

    private SpannableStringBuilder getSuperHeaderText() {
        String SEPARATOR = getResources().getString(R.string.mesa_whats_new_supertext_separator) + " ";
        String NEW_LINE = System.getProperty("line.separator");
        String TWO_DOTS = ": ";
        int COLORPRIMARYDARK = getResources().getColor(R.color.mesa_ota_primary_dark_color_dark, null);

        String romVersionNumber = PreferencesUtils.ROM.getVersionName();
        String romVersionLine = SEPARATOR + getResources().getString(R.string.mesa_whats_new_supertext_rom_version) + TWO_DOTS + romVersionNumber;
        String newdate = String.valueOf(PreferencesUtils.ROM.getNewDate());
        String updateSizeNumber = Formatter.formatFileSize(mContext, PreferencesUtils.ROM.getFileSize());
        String updateSizeLine = SEPARATOR + getResources().getString(R.string.mesa_whats_new_supertext_file_size) + TWO_DOTS + updateSizeNumber;

        SpannableStringBuilder span = new SpannableStringBuilder();
        span.append(romVersionLine);
        span.setSpan(new ForegroundColorSpan(COLORPRIMARYDARK), span.length() - romVersionNumber.length(), span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.append(NEW_LINE);
        span.append(updateSizeLine);
        span.setSpan(new ForegroundColorSpan(COLORPRIMARYDARK), span.length() - updateSizeNumber.length(), span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return span;
    }

    private void setHeaderImgContainerDimen(Configuration newConfig) {
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHeaderImgContainer.getLayoutParams();
        lp.weight = isLandscape ? 5.0f : 3.0f;
        mHeaderContainer.setWeightSum(isLandscape ? 7.0f : 5.0f);
        mHeaderImgContainer.setLayoutParams(lp);
    }

    public void start() {
        this.setVisibility(View.VISIBLE);
        if (StateUtils.isNetworkConnected(mContext) && !PreferencesUtils.ROM.getChangelogUrl().equals("null")) {
            mSuperText.setVisibility(View.VISIBLE);
            mSuperText.setText(getSuperHeaderText());
            mHeaderContainer.setVisibility(View.VISIBLE);
            mSuperText.startAnimation(mFadeInAnim);
            mHeaderContainer.startAnimation(mFadeInAnim);
        } else {
            mErrorText.setVisibility(View.VISIBLE);
            mErrorText.startAnimation(mFadeInAnim);
        }
    }

    public void stop() {
        this.setVisibility(View.GONE);
        mSuperText.setVisibility(View.GONE);
        mHeaderContainer.setVisibility(View.GONE);
        mErrorText.setVisibility(View.GONE);
    }
}
