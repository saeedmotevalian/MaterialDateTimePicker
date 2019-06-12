/*
 * Copyright (C) 2017 Wouter Dullaert
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

package com.wdullaer.materialdatetimepicker.date;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wdullaer.materialdatetimepicker.Utils;

import com.wdullaer.materialdatetimepicker.util.PersianCalendar;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.TreeSet;

class DefaultDateRangeLimiter implements DateRangeLimiter {
    private static final int DEFAULT_START_YEAR = 1300;
    private static final int DEFAULT_END_YEAR = 1500;

    private transient DatePickerController mController;
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private PersianCalendar mMinDate;
    private PersianCalendar mMaxDate;
    private TreeSet<PersianCalendar> selectableDays = new TreeSet<>();
    private HashSet<PersianCalendar> disabledDays = new HashSet<>();

    DefaultDateRangeLimiter() {}

    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public DefaultDateRangeLimiter(Parcel in) {
        mMinYear = in.readInt();
        mMaxYear = in.readInt();
        mMinDate = (PersianCalendar) in.readSerializable();
        mMaxDate = (PersianCalendar) in.readSerializable();
        selectableDays = (TreeSet<PersianCalendar>) in.readSerializable();
        disabledDays = (HashSet<PersianCalendar>) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mMinYear);
        out.writeInt(mMaxYear);
        out.writeSerializable(mMinDate);
        out.writeSerializable(mMaxDate);
        out.writeSerializable(selectableDays);
        out.writeSerializable(disabledDays);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @SuppressWarnings("WeakerAccess")
    public static final Parcelable.Creator<DefaultDateRangeLimiter> CREATOR
            = new Parcelable.Creator<DefaultDateRangeLimiter>() {
        public DefaultDateRangeLimiter createFromParcel(Parcel in) {
            return new DefaultDateRangeLimiter(in);
        }

        public DefaultDateRangeLimiter[] newArray(int size) {
            return new DefaultDateRangeLimiter[size];
        }
    };

    void setSelectableDays(@NonNull PersianCalendar[] days) {
        for (PersianCalendar selectableDay : days) {
            this.selectableDays.add(Utils.trimToMidnight((PersianCalendar) selectableDay.clone()));
        }
    }

    void setDisabledDays(@NonNull PersianCalendar[] days) {
        for (PersianCalendar disabledDay : days) {
            this.disabledDays.add(Utils.trimToMidnight((PersianCalendar) disabledDay.clone()));
        }
    }

    void setMinDate(@NonNull PersianCalendar calendar) {
        mMinDate = Utils.trimToMidnight((PersianCalendar) calendar.clone());
    }

    void setMaxDate(@NonNull PersianCalendar calendar) {
        mMaxDate = Utils.trimToMidnight((PersianCalendar) calendar.clone());
    }

    void setController(@NonNull DatePickerController controller) {
        mController = controller;
    }

    void setYearRange(int startYear, int endYear) {
        if (endYear < startYear) {
            throw new IllegalArgumentException("Year end must be larger than or equal to year start");
        }

        mMinYear = startYear;
        mMaxYear = endYear;
    }

    @Nullable PersianCalendar getMinDate() {
        return mMinDate;
    }

    @Nullable PersianCalendar getMaxDate() {
        return mMaxDate;
    }

    @Nullable PersianCalendar[] getSelectableDays() {
         return selectableDays.isEmpty() ? null : selectableDays.toArray(new PersianCalendar[0]);
    }

    @Nullable PersianCalendar[] getDisabledDays() {
        return disabledDays.isEmpty() ? null : disabledDays.toArray(new PersianCalendar[0]);
    }

    @Override
    public int getMinYear() {
        if (!selectableDays.isEmpty()) return selectableDays.first().getPersianYear();
        // Ensure no years can be selected outside of the given minimum date
        return mMinDate != null && mMinDate.getPersianYear() > mMinYear ? mMinDate.getPersianYear() : mMinYear;
    }

    @Override
    public int getMaxYear() {
        if (!selectableDays.isEmpty()) return selectableDays.last().getPersianYear();
        // Ensure no years can be selected outside of the given maximum date
        return mMaxDate != null && mMaxDate.getPersianYear() < mMaxYear ? mMaxDate.getPersianYear() : mMaxYear;
    }

    @Override
    public @NonNull PersianCalendar getStartDate() {
        if (!selectableDays.isEmpty()) return (PersianCalendar) selectableDays.first().clone();
        if (mMinDate != null) return (PersianCalendar) mMinDate.clone();
        TimeZone timeZone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
        PersianCalendar output = new PersianCalendar();
        output.setPersianDate(mMinYear, 0, 1);
        return output;
    }

    @Override
    public @NonNull PersianCalendar getEndDate() {
        if (!selectableDays.isEmpty()) return (PersianCalendar) selectableDays.last().clone();
        if (mMaxDate != null) return (PersianCalendar) mMaxDate.clone();
        TimeZone timeZone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
        PersianCalendar output = new PersianCalendar();
        output.setPersianDate(mMaxYear, 11, 31);
        return output;
    }

    /**
     * @return true if the specified year/month/day are within the selectable days or the range set by minDate and maxDate.
     * If one or either have not been set, they are considered as Integer.MIN_VALUE and
     * Integer.MAX_VALUE.
     */
    @Override
    public boolean isOutOfRange(int year, int month, int day) {
        TimeZone timezone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
        PersianCalendar date = new PersianCalendar();
        date.setPersianDate(year, month, day);
        return isOutOfRange(date);
    }

    private boolean isOutOfRange(@NonNull PersianCalendar calendar) {
        Utils.trimToMidnight(calendar);
        return isDisabled(calendar) || !isSelectable(calendar);
    }

    private boolean isDisabled(@NonNull PersianCalendar c) {
        return disabledDays.contains(Utils.trimToMidnight(c)) || isBeforeMin(c) || isAfterMax(c);
    }

    private boolean isSelectable(@NonNull PersianCalendar c) {
        return selectableDays.isEmpty() || selectableDays.contains(Utils.trimToMidnight(c));
    }

    private boolean isBeforeMin(@NonNull PersianCalendar calendar) {
        return mMinDate != null && calendar.before(mMinDate) || calendar.getPersianYear() < mMinYear;
    }

    private boolean isAfterMax(@NonNull PersianCalendar calendar) {
        return mMaxDate != null && calendar.after(mMaxDate) || calendar.getPersianYear() > mMaxYear;
    }

    @Override
    public @NonNull PersianCalendar setToNearestDate(@NonNull PersianCalendar calendar) {
        if (!selectableDays.isEmpty()) {
            PersianCalendar newCalendar = null;
            PersianCalendar higher = selectableDays.ceiling(calendar);
            PersianCalendar lower = selectableDays.lower(calendar);

            if (higher == null && lower != null) newCalendar = lower;
            else if (lower == null && higher != null) newCalendar = higher;

            if (newCalendar != null || higher == null) {
                newCalendar = newCalendar == null ? calendar : newCalendar;
                TimeZone timeZone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
                newCalendar.setTimeZone(timeZone);
                return (PersianCalendar) newCalendar.clone();
            }

            long highDistance = Math.abs(higher.getTimeInMillis() - calendar.getTimeInMillis());
            long lowDistance = Math.abs(calendar.getTimeInMillis() - lower.getTimeInMillis());

            if (lowDistance < highDistance) return (PersianCalendar) lower.clone();
            else return (PersianCalendar) higher.clone();
        }

        if (!disabledDays.isEmpty()) {
            PersianCalendar forwardDate = isBeforeMin(calendar) ? getStartDate() : (PersianCalendar) calendar.clone();
            PersianCalendar backwardDate = isAfterMax(calendar) ? getEndDate() : (PersianCalendar) calendar.clone();
            while (isDisabled(forwardDate) && isDisabled(backwardDate)) {
                forwardDate.addPersianDate(PersianCalendar.DAY_OF_MONTH, 1);
                backwardDate.addPersianDate(PersianCalendar.DAY_OF_MONTH, -1);
            }
            if (!isDisabled(backwardDate)) {
                return backwardDate;
            }
            if (!isDisabled(forwardDate)) {
                return forwardDate;
            }
        }

        TimeZone timezone = mController == null ? TimeZone.getDefault() : mController.getTimeZone();
        if (isBeforeMin(calendar)) {
            if (mMinDate != null) return (PersianCalendar) mMinDate.clone();
            PersianCalendar output = new PersianCalendar();
            output.setPersianDate(mMaxYear, 0, 1);
            return Utils.trimToMidnight(output);
        }

        if (isAfterMax(calendar)) {
            if (mMaxDate != null) return (PersianCalendar) mMaxDate.clone();
            PersianCalendar output = new PersianCalendar();
            output.setPersianDate(mMaxYear, 11, 31);
            return Utils.trimToMidnight(output);
        }

        return calendar;
    }
}