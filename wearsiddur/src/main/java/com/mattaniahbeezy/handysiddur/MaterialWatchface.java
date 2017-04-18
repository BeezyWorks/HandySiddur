/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattaniahbeezy.handysiddur;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.crashlytics.android.Crashlytics;
import com.mattaniahbeezy.icecreamlibrary.DaveningInfo;
import com.mattaniahbeezy.icecreamlibrary.LocationHelper;
import com.mattaniahbeezy.icecreamlibrary.SettingsHelper;
import com.mattaniahbeezy.icecreamlibrary.ZmanGetter;

import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

/**
 * Sample digital watch face with blinking colons and seconds. In ambient mode, the seconds are
 * replaced with an AM/PM indicator and the colons don't blink. On devices with low-bit ambient
 * mode, the text is drawn without anti-aliasing in ambient mode. On devices which require burn-in
 * protection, the hours are drawn in normal rather than bold. The time is drawn with less contrast
 * and without seconds in mute mode.
 */
public class MaterialWatchface extends CanvasWatchFaceService implements
        LocationHelper.LocationHandled {
    private static final String TAG = "MaterialWatchface";


    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create("sans-serif-thin", Typeface.NORMAL);
    private static final Typeface BOLD_TYPEFACE =
            Typeface.create("sans-serif-light", Typeface.NORMAL);
    private static final Typeface EXTRA_BOLD_TYPEFACE =
            Typeface.create("sans-serif", Typeface.BOLD);

    /**
     * Update rate in milliseconds for normal (not ambient and not mute) mode. We update twice
     * a second to blink the colons.
     */
    private static final long NORMAL_UPDATE_RATE_MS = 500;

    /**
     * Update rate in milliseconds for mute mode. We update every minute, like in ambient mode.
     */
    private static final long MUTE_UPDATE_RATE_MS = TimeUnit.MINUTES.toMillis(1);

    long nightTime;
    long lateAfternoonTime;
    long afternoonTime;
    long daytime;
    long morningTime;
    long earlyMorningTime;

    SettingsHelper settingsHelper;
    ZmanGetter zmanGetter;
    DaveningInfo jDate;
    LocationHelper locationHelper;
    long shaaZmanis;
    Date dayStart;
    Date dayEnd;

    @Override
    public Engine onCreateEngine() {
        Fabric.with(MaterialWatchface.this, new Crashlytics());
        settingsHelper = new SettingsHelper(this);
        return new Engine();
    }


    private void setHighlightTimes() {
        nightTime = zmanGetter.getTzais().getTime();
        lateAfternoonTime = zmanGetter.getPlagHamincha().getTime();
        afternoonTime = zmanGetter.getMinchaKetana().getTime();
        daytime = zmanGetter.getSofZmanTfilaGRA().getTime();
        morningTime = zmanGetter.getSeaLevelSunrise().getTime();
        earlyMorningTime = zmanGetter.getAlosHashachar().getTime();
    }


    @Override
    public void locationAvailable(Location location, int locationSource) {
        zmanGetter = ZmanGetter.getCZC(this, Calendar.getInstance(), location);
        locationHelper.disconnect();
        shaaZmanis = zmanGetter.getShaahZmanisGra();
        dayEnd = zmanGetter.getSeaLevelSunset();
        dayStart = zmanGetter.getSeaLevelSunrise();
        setHighlightTimes();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        static final String COLON_STRING = ":";

        /**
         * Alpha value for drawing time when in mute mode.
         */
        static final int MUTE_ALPHA = 100;

        /**
         * Alpha value for drawing time when not in mute mode.
         */
        static final int NORMAL_ALPHA = 255;

        static final int MSG_UPDATE_TIME = 0;

        /**
         * How often {@link #mUpdateTimeHandler} ticks in milliseconds.
         */
        long mInteractiveUpdateRateMs = NORMAL_UPDATE_RATE_MS;

        /**
         * Handler to update the time periodically in interactive mode.
         */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "updating time");
                        }
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs =
                                    mInteractiveUpdateRateMs - (timeMs % mInteractiveUpdateRateMs);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };


        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.setTimeZone(TimeZone.getDefault());
                if (jDate != null)
                    jDate.setDate(mTime);
                locationHelper.connect();
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaintColor;
        Paint mBackgroundPaintWhite;
        Paint mHourPaint;
        Paint mMinutePaint;
        Paint mSolarTimePaint;
        Paint mColonPaint;
        Paint mDatePaint;


        float mColonWidth;

        int earlyMorningColor = getResources().getColor(R.color.earlyMorningColor);
        int morningColor = getResources().getColor(R.color.morningColor);
        int daytimeColor = getResources().getColor(R.color.daytimeColor);
        int afternoonColor = getResources().getColor(R.color.afternoonColor);
        int laterAfternoonColor = getResources().getColor(R.color.lateAfternoonColor);
        int nighttimeColor = getResources().getColor(R.color.nighttimeColor);


        boolean mMute;
        Calendar mTime;
        long timeSinceZero;
        HebrewDateFormatter hFat;
        SimpleDateFormat sFat;
        float mXOffset;
        float mYOffset;
        String dateString;
        String dateStringTomorrow;
        String mAmString;
        String mPmString;
        int mInteractiveWhiteColor = Color.WHITE;
        int mInteractiveHighlightColor = daytimeColor;
        int mInteractiveGrayColor = getResources().getColor(R.color.material_blue_grey_800);
        int mInteractiveSecondDigitsColor = DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_SECOND_DIGITS;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        boolean afterSunset;
        int updateIndex;

        @Override
        public void onCreate(SurfaceHolder holder) {
            locationHelper = new LocationHelper(MaterialWatchface.this, MaterialWatchface.this);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onCreate");
            }
            super.onCreate(holder);
            zmanGetter = ZmanGetter.getCZC(MaterialWatchface.this);
            shaaZmanis = zmanGetter.getShaahZmanisGra();
            dayEnd = zmanGetter.getSeaLevelSunset();
            dayStart = zmanGetter.getSeaLevelSunrise();
            setHighlightTimes();
            jDate = new DaveningInfo(MaterialWatchface.this);
            hFat = new HebrewDateFormatter();
            sFat = new SimpleDateFormat("d MMM");
            hFat.setUseGershGershayim(false);
            hFat.setHebrewFormat(true);
            setWatchFaceStyle(new WatchFaceStyle.Builder(MaterialWatchface.this)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_HIDDEN)
                    .setStatusBarGravity(Gravity.RIGHT)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = MaterialWatchface.this.getResources();

            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaintColor = new Paint();
            mBackgroundPaintColor.setColor(mInteractiveHighlightColor);
            mBackgroundPaintWhite = new Paint();
            mBackgroundPaintColor.setColor(mInteractiveWhiteColor);
            mHourPaint = createTextPaint(mInteractiveHighlightColor);
            mMinutePaint = createTextPaint(mInteractiveGrayColor);
            mSolarTimePaint = createTextPaint(mInteractiveSecondDigitsColor);
            mColonPaint = createTextPaint(mInteractiveGrayColor);
            mDatePaint = createTextPaint(mInteractiveWhiteColor);

            mTime = Calendar.getInstance();
            locationHelper.connect();
            setDateString();
            adjustColors();
        }

        private void setDateString() {
            jDate.setDate(mTime);
            zmanGetter.setCalendar(mTime);
            dateString = sFat.format(mTime.getTime()) + " " + hFat.formatHebrewNumber(jDate.getJewishDayOfMonth()) + " " + hFat.formatMonth(jDate);
            dateStringTomorrow = sFat.format(mTime.getTime()) + " " + hFat.formatHebrewNumber(jDate.getjTomorrow().getJewishDayOfMonth()) + " " + hFat.formatMonth(jDate.getjTomorrow());
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int defaultInteractiveColor) {
            return createTextPaint(defaultInteractiveColor, NORMAL_TYPEFACE);
        }

        private Paint createTextPaint(int defaultInteractiveColor, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(defaultInteractiveColor);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onVisibilityChanged: " + visible);
            }
            super.onVisibilityChanged(visible);

            if (visible) {

                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.setTimeZone(TimeZone.getDefault());
                mTime.setTimeInMillis(System.currentTimeMillis());
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_DATE_CHANGED);
            MaterialWatchface.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MaterialWatchface.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onApplyWindowInsets: " + (insets.isRound() ? "round" : "square"));
            }
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MaterialWatchface.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            float dateSize = resources.getDimension(isRound
                    ? R.dimen.date_text_size : R.dimen.date_text_size);
            float solarSize = resources.getDimension(isRound
                    ? R.dimen.digital_solar_text_size : R.dimen.digital_solar_text_size);

            mHourPaint.setTextSize(textSize);
            mMinutePaint.setTextSize(textSize);
            mSolarTimePaint.setTextSize(solarSize);
            mColonPaint.setTextSize(textSize);
            mDatePaint.setTextSize(dateSize);
            mColonWidth = mColonPaint.measureText(COLON_STRING);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mHourPaint.setTypeface(burnInProtection ? NORMAL_TYPEFACE : BOLD_TYPEFACE);
            mSolarTimePaint.setTypeface(burnInProtection ? NORMAL_TYPEFACE : EXTRA_BOLD_TYPEFACE);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onPropertiesChanged: burn-in protection = " + burnInProtection
                        + ", low-bit ambient = " + mLowBitAmbient);
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            }
            updateIndex++;
            if (updateIndex % 5 == 0)
                adjustColors();
            if (updateIndex == 50) {
                locationHelper.connect();
                setDateString();
                updateIndex = 0;
            }

            invalidate();
        }

        private int getHighlightColor() {
            long currentTime = mTime.getTimeInMillis();
            if (currentTime > nightTime)
                return nighttimeColor;
            if (currentTime > lateAfternoonTime)
                return laterAfternoonColor;
            if (currentTime > afternoonTime)
                return afternoonColor;
            if (currentTime > daytime)
                return daytimeColor;
            if (currentTime > morningTime)
                return morningColor;
            if (currentTime > earlyMorningTime)
                return earlyMorningColor;
            return nighttimeColor;
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);
            }
            adjustColors();
            // Actually, the seconds are not rendered in the ambient mode, so we could pass just any
            // value as ambientColor here.


            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                mSolarTimePaint.setAntiAlias(antiAlias);
                mColonPaint.setAntiAlias(antiAlias);
                mDatePaint.setAntiAlias(antiAlias);
            }
            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        private void adjustColors() {
            mInteractiveHighlightColor = getHighlightColor();
            adjustPaintColorToCurrentMode(mBackgroundPaintColor, mInteractiveHighlightColor, DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);
            adjustPaintColorToCurrentMode(mBackgroundPaintWhite, mInteractiveWhiteColor, DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);
            adjustPaintColorToCurrentMode(mHourPaint, mInteractiveHighlightColor, DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS);
            adjustPaintColorToCurrentMode(mMinutePaint, mInteractiveGrayColor, DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);
            adjustPaintColorToCurrentMode(mColonPaint, mInteractiveGrayColor, DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);
            adjustPaintColorToCurrentMode(mDatePaint, mInteractiveWhiteColor, DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);
            adjustPaintColorToCurrentMode(mSolarTimePaint, mInteractiveSecondDigitsColor, mInteractiveHighlightColor);
        }

        private void adjustPaintColorToCurrentMode(Paint paint, int interactiveColor,
                                                   int ambientColor) {
            paint.setColor(isInAmbientMode() ? ambientColor : interactiveColor);
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onInterruptionFilterChanged: " + interruptionFilter);
            }
            super.onInterruptionFilterChanged(interruptionFilter);

            boolean inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE;
            // We only need to update once a minute in mute mode.
            setInteractiveUpdateRateMs(inMuteMode ? MUTE_UPDATE_RATE_MS : NORMAL_UPDATE_RATE_MS);

            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                int alpha = inMuteMode ? MUTE_ALPHA : NORMAL_ALPHA;
                mHourPaint.setAlpha(alpha);
                mMinutePaint.setAlpha(alpha);
                mColonPaint.setAlpha(alpha);
                invalidate();
            }
        }

        public void setInteractiveUpdateRateMs(long updateRateMs) {
            if (updateRateMs == mInteractiveUpdateRateMs) {
                return;
            }
            mInteractiveUpdateRateMs = updateRateMs;

            // Stop and restart the timer so the new update rate takes effect immediately.
            if (shouldTimerBeRunning()) {
                updateTimer();
            }
        }

        private void updatePaintIfInteractive(Paint paint, int interactiveColor) {
            if (!isInAmbientMode() && paint != null) {
                paint.setColor(interactiveColor);
            }
        }


        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        private int convertTo12Hour(int hour) {
            int result = hour % 12;
            return (result == 0) ? 12 : result;
        }

        private String getAmPmString(int hour) {
            return (hour < 12) ? mAmString : mPmString;
        }

        private long getTimeSinceZero() {
            long currentTime = mTime.getTimeInMillis();
            if (afterSunset)
                return currentTime - dayEnd.getTime();
            if (!zmanGetter.isAfter(zmanGetter.getSeaLevelSunrise()))
                return currentTime - zmanGetter.getYesterdaySunset().getTime();
            return currentTime - dayStart.getTime();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            try {
                Rect hourBounds = new Rect();
                mHourPaint.getTextBounds("a", 0, 1, hourBounds);
                afterSunset = zmanGetter.isAfterSunset();
                mTime.setTimeInMillis(System.currentTimeMillis());
                timeSinceZero = getTimeSinceZero();
                int yOffset = (int) (mYOffset * 2 + hourBounds.height());
                // Draw the background.
                int colorBoundry = (int) (bounds.height() * .6);
                canvas.drawRect(0, colorBoundry, bounds.width(), bounds.height(), mBackgroundPaintColor);
                canvas.drawRect(0, 0, bounds.width(), colorBoundry, mBackgroundPaintWhite);

                float x = bounds.width() / 2;
                // Draw the hours.
                String hourString = String.valueOf(settingsHelper.isTwentyFourHours() ? mTime.get(Calendar.HOUR_OF_DAY) : convertTo12Hour(mTime.get(Calendar.HOUR_OF_DAY)));
                String minuteString = formatTwoDigitNumber(mTime.get(Calendar.MINUTE));
                x -= (mHourPaint.measureText(hourString) + mColonWidth + mMinutePaint.measureText(minuteString
                )) / 2;
                canvas.drawText(hourString, x, yOffset, mHourPaint);
                x += mHourPaint.measureText(hourString);
                canvas.drawText(COLON_STRING, x, yOffset, mColonPaint);

                x += mColonWidth;
                // Draw the minutes.
                canvas.drawText(minuteString, x, yOffset, mMinutePaint);

                //draw solar time
                String solarTimeString = solarTimeString();
                int solarTimeXOffset = (int) (bounds.centerX() - (mSolarTimePaint.measureText(solarTimeString) / 2));
                Rect solarBounds = new Rect();
                mSolarTimePaint.getTextBounds("0", 0, 1, solarBounds);
                yOffset += solarBounds.height() + mYOffset;
                canvas.drawText(solarTimeString, solarTimeXOffset, yOffset, mSolarTimePaint);

//Draw the date
                int dateXOffset = (int) (bounds.centerX() - (mDatePaint.measureText(dateString) / 2));
                Rect dateBounds = new Rect();
                mDatePaint.getTextBounds("a", 0, 1, dateBounds);
                int dateYOffset = (int) (colorBoundry + mYOffset + dateBounds.height());
                String dateToWrite = (afterSunset ? dateStringTomorrow : dateString);
                canvas.drawText(dateToWrite, dateXOffset, dateYOffset, mDatePaint);
                dateYOffset += dateBounds.height() + mYOffset;

                //Draw upcoming Zman
                ZmanGetter.DAILY_ZMANIM approachingZman = zmanGetter.getApproachingZman();
                String upcomingZmanString = approachingZman.getName() + " " + approachingZman.getTimeString(settingsHelper.getTimePatern(), zmanGetter);
                int zmanXOffset = (int) (bounds.centerX() - (mDatePaint.measureText(upcomingZmanString) / 2));
                canvas.drawText(upcomingZmanString, zmanXOffset, dateYOffset, mDatePaint);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        private String solarTimeString() {
            long minuteInMilis = 60 * 1000;
            long hourInMilis = minuteInMilis * 60;
            long zHour = (!afterSunset || !zmanGetter.isAfter(zmanGetter.getSeaLevelSunrise())) ? shaaZmanis : (hourInMilis * 2 - shaaZmanis);
            int hour = (int) (timeSinceZero / zHour);
            int minute = (int) ((int) ((timeSinceZero % zHour) * 60) / zHour);
            if (minute < 10)
                return String.valueOf(hour) + ":0" + String.valueOf(minute);
            return String.valueOf(hour) + ":" + String.valueOf(minute);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "updateTimer");
            }
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }


    }
}
