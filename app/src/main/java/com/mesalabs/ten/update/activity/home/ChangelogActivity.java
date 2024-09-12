package com.mesalabs.ten.update.activity.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;

import com.mesalabs.cerberus.base.BaseAppBarActivity;
import com.mesalabs.cerberus.ui.callback.OnSingleClickListener;
import com.mesalabs.cerberus.ui.utils.ActionBarUtils;
import com.mesalabs.cerberus.ui.widget.RoundNestedScrollView;
import com.mesalabs.cerberus.utils.StateUtils;
import com.mesalabs.cerberus.utils.Utils;
import com.mesalabs.ten.update.R;
import com.mesalabs.ten.update.ota.utils.PreferencesUtils;
import com.mesalabs.ten.update.utils.LogUtils;
import com.samsung.android.ui.appbar.SeslAppBarLayout;

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

public class ChangelogActivity extends BaseAppBarActivity {
    private boolean isBgColorDark;

    private ImageView mImageView;
    private RoundNestedScrollView mNSVLayout;
    private FrameLayout mProgressLayout;
    private ConstraintLayout mContainerLayout;
    private TextView mContainerTitle;
    private TextView mContainerDate;
    private View mDivider;
    private TextView mContentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init
        if (!StateUtils.isNetworkConnected(mContext)) {
            Toast.makeText(mContext, R.string.mesa_no_network_connection, Toast.LENGTH_LONG).show();
            finish();
        }

        setBaseContentView(R.layout.mesa_ota_activity_changelogactivity_layout);

        appBar = new ActionBarUtils(this);
        appBar.initAppBar(true);
        appBar.getAppBarLayout().addOnOffsetChangedListener(new CustomAppBarListener() {
            @Override
            public void onStateChanged(SeslAppBarLayout appBarLayout, int state) {
                setLightStatusBar(state == STATE_COLLAPSED ? !Utils.isNightMode(mContext) : !isBgColorDark);
            }
        });
        appBar.setHomeAsUpButton(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                onBackPressed();
            }
        });

        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.sesl_appbar_height_proportion, outValue, true);

        if (outValue.getFloat() == 0.0f)
            setFullscreen();

        mImageView = findViewById(R.id.mesa_appbarbg_changelogactivity);
        mNSVLayout = findViewById(R.id.mesa_nsv_changelogactivity);
        mProgressLayout = findViewById(R.id.mesa_progresscontainer_changelogactivity);
        mContainerLayout = findViewById(R.id.mesa_changelogcontainer_changelogactivity);
        mContainerTitle = findViewById(R.id.mesa_title_container_changelogactivity);
        mContainerDate = findViewById(R.id.mesa_date_container_changelogactivity);
        mDivider = findViewById(R.id.mesa_divider_container_changelogactivity);
        mContentText = findViewById(R.id.mesa_contenttext_container_changelogactivity);

        new LoadChangelogTask().execute(PreferencesUtils.ROM.getChangelogHeaderImgUrl(), PreferencesUtils.ROM.getChangelogUrl());
    }

    private void setLightStatusBar(boolean enable) {
        View decorView = getWindow().getDecorView();
        int flags = decorView.getSystemUiVisibility();

        if (enable) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        decorView.setSystemUiVisibility(flags);
    }

    private void setFullscreen() {
        if (!Utils.isInMultiWindowMode(this)) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            Utils.genericInvokeMethod(params, "semAddExtensionFlags", 1 /* WindowManager.LayoutParams.SEM_EXTENSION_FLAG_RESIZE_FULLSCREEN_WINDOW_ON_SOFT_INPUT */);
            getWindow().setAttributes(params);
        } else {
            appBar.getAppBarLayout().setFitsSystemWindows(false);
        }
    }


    private abstract class CustomAppBarListener implements SeslAppBarLayout.OnOffsetChangedListener {
        public int STATE_EXPANDED = 0;
        public int STATE_COLLAPSED = 1;

        private int mCurrentState = STATE_EXPANDED;

        @Override
        public void onOffsetChanged(SeslAppBarLayout layout, int verticalOffset) {
            int totalScrollRange = layout.getTotalScrollRange();
            int inputMethodWindowVisibleHeight = (int) Utils.genericInvokeMethod(InputMethodManager.class, mContext.getSystemService(INPUT_METHOD_SERVICE), "getInputMethodWindowVisibleHeight");

            if (Math.abs(verticalOffset) >= layout.getTotalScrollRange()) {
                if (mCurrentState != STATE_COLLAPSED) {
                    onStateChanged(layout, STATE_COLLAPSED);
                }
                mCurrentState = STATE_COLLAPSED;
            } else {
                if (mCurrentState != STATE_EXPANDED) {
                    onStateChanged(layout, STATE_EXPANDED);
                }
                mCurrentState = STATE_EXPANDED;
            }

            mImageView.setImageAlpha((int) (255 * (1.0f - (float) verticalOffset / -totalScrollRange)));

            if (mProgressLayout != null && mProgressLayout.getVisibility() == View.VISIBLE) {
                if (totalScrollRange != 0) {
                    mProgressLayout.setTranslationY(((float) (Math.abs(verticalOffset) - totalScrollRange)) / 2.0f);
                } else if (Utils.isInMultiWindowMode(ChangelogActivity.this)) {
                    mProgressLayout.setTranslationY(0.0f);
                } else {
                    mProgressLayout.setTranslationY(((float) (Math.abs(verticalOffset) - inputMethodWindowVisibleHeight)) / 2.0f);
                }
            }
        }

        public abstract void onStateChanged(SeslAppBarLayout layout, int state);
    }

    private class LoadChangelogTask extends AsyncTask<String, Void, Bitmap> {
        String title;
        String date;
        String content = "";

        @Override
        protected Bitmap doInBackground(String... urls) {
            String headerBgUrl = urls[0];
            String changelogUrl = urls[1];
            Bitmap mBg = null;
            String line;

            try {
                InputStream in = new URL(headerBgUrl).openStream();
                mBg = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                LogUtils.e("Exception", e.getMessage());
            }

            title = PreferencesUtils.ROM.getRomName() + " v" + PreferencesUtils.ROM.getVersionName();

            try {
                InputStream in = new URL(changelogUrl).openConnection().getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                while ((line = br.readLine()) != null) {
                    content += line + "\n";
                }
                br.close();
            } catch (Exception e) {
                LogUtils.e("Exception", e.getMessage());
            }

            return mBg;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                mImageView.setImageBitmap(result);
                isBgColorDark = isColorDark(getDominantColor(result));
            } else {
                LogUtils.e("DownloadHeaderImageTask", "result is null!!!");
            }
            mNSVLayout.setBackgroundColor(mContext.getResources().getColor(Utils.isNightMode(mContext) ? R.color.sesl_background_color_dark : R.color.sesl_background_color_light, null));

            mProgressLayout.setVisibility(View.GONE);
            mContainerLayout.setVisibility(View.VISIBLE);
            mContainerTitle.setText(title);
            mContainerDate.setText(date);
            mDivider.setVisibility(View.VISIBLE);
            mContentText.setText(content);
        }

        private int getDominantColor(Bitmap bitmap) {
            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
            final int color = newBitmap.getPixel(0, 0);
            newBitmap.recycle();
            return color;
        }

        private boolean isColorDark(int color) {
            return ColorUtils.calculateLuminance(color) < 0.5;
        }
    }
}
