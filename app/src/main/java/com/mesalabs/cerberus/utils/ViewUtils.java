package com.mesalabs.cerberus.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.mesalabs.cerberus.base.AppBarActivity;

/*
 * Cerberus Core App
 *
 * Coded by BlackMesa @2019
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * ULTRA-MEGA-PRIVATE SOURCE CODE. SHARING TO DEVKINGS TEAM
 * EXTERNALS IS PROHIBITED AND WILL BE PUNISHED WITH ANAL ABUSE.
 */

public class ViewUtils {

    public static int dp2px(Context context, float f) {
        try {
            return (int) ((context.getResources().getDisplayMetrics().density * f) + 0.5f);
        } catch (Exception e) {
            return (int) (f + 0.5f);
        }
    }

    private static double getDensity(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        Display display = windowManager == null ? null : windowManager.getDefaultDisplay();
        if (display != null) {
            display.getRealMetrics(metrics);
        }
        if (display == null) {
            return 1.0d;
        }
        return ((double) configuration.densityDpi) / ((double) metrics.densityDpi);
    }

    public static float getDIPForPX(AppBarActivity activity, int i) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) i, activity.getResources().getDisplayMetrics());
    }

    public static int getPortraitOrientation(Context context) {
        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            if (windowManager == null) {
                return 0;
            }


            return windowManager.getDefaultDisplay().getRotation();
        } catch (Exception unused) {
            LogUtils.e("ViewUtils", "cannot get portrait orientation");
            return 0;
        }
    }

    public static int getSmallestDeviceWidthDp(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(displayMetrics);
        LogUtils.d("ViewUtils", "metrics = " + displayMetrics);
        return Math.round(Math.min(((float) displayMetrics.heightPixels) / displayMetrics.density, ((float) displayMetrics.widthPixels) / displayMetrics.density));
    }

    public static int getStatusbarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelOffset(resourceId);
        }
        return 0;
    }

    public static int getWindowWidth(Context context) {
        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            if (windowManager == null) {
                return 0;
            }

            Point point = new Point();
            windowManager.getDefaultDisplay().getSize(point);
            return point.x;
        } catch (Exception unused) {
            LogUtils.e("ViewUtils", "cannot get window width");
            return 0;
        }
    }

    public static int getWindowHeight(Context context) {
        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            if (windowManager == null) {
                return 0;
            }

            Point point = new Point();
            windowManager.getDefaultDisplay().getSize(point);
            return point.y;
        } catch (Exception unused) {
            LogUtils.e("ViewUtils", "cannot get window width");
            return 0;
        }
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isMultiWindowMinSize(Context context, int minSizeDp, boolean isWidth) {
        Configuration configuration = context.getResources().getConfiguration();
        return ((int) (((double) (isWidth ? configuration.screenWidthDp : configuration.screenHeightDp)) * getDensity(context))) <= minSizeDp;
    }

    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isRTLMode(Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static boolean isVisibleNaviBar(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "navigationbar_hide_bar_enabled", 0) == 0;
    }

}