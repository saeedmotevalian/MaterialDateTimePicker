package com.wdullaer.materialdatetimepicker.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by saeed on 5/5/18.
 * Email: saeed.motevalian@gmail.com
 * Phone: +989124309658
 */

public class PersianCalendar extends GregorianCalendar {

    private static final long serialVersionUID = 5541422440580682494L;

    private int persianYear;
    private int persianMonth;
    private int persianDay;
    // use to seperate PersianDate's field and also Parse the DateString based on this delimiter
    private String delimiter = "/";

    /**
     * default constructor
     * <p>
     * most of the time we don't care about TimeZone when we persisting Date
     * or doing some calculation on date.
     * <strong> Default TimeZone was set to "GMT" </strong> in order to make developer to work more convenient
     * with the library; however you can change the TimeZone as you do in GregorianCalendar by calling
     * setTimeZone()
     */
    public PersianCalendar() {
        setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private long convertToMilis(long julianDate) {
        return PersianCalendarConstants.MILLIS_JULIAN_EPOCH + julianDate * PersianCalendarConstants.MILLIS_OF_A_DAY
                + PersianCalendarUtils.ceil(getTimeInMillis() - PersianCalendarConstants.MILLIS_JULIAN_EPOCH, PersianCalendarConstants.MILLIS_OF_A_DAY);
    }

    /**
     * Calculate persian date from current Date and
     * populates the corresponding fields(persianYear, persianMonth, persianDay)
     */
    protected void calculatePersianDate() {
        long julianDate = ((long) Math.floor((getTimeInMillis() - PersianCalendarConstants.MILLIS_JULIAN_EPOCH)) /
                PersianCalendarConstants.MILLIS_OF_A_DAY);
        long PersianRowDate = PersianCalendarUtils.julianToPersian(julianDate);
        long year = PersianRowDate >> 16;
        int month = (int) (PersianRowDate & 0xff00) >> 8;
        int day = (int) (PersianRowDate & 0xff);
        this.persianYear = (int) (year > 0 ? year : year - 1);
        this.persianMonth = month;
        this.persianDay = day;
    }

    /**
     * Determines if the given year is a leap year in persian calendar.
     * Returns true if the given year is a leap year.
     *
     * @return boolean
     */
    public boolean isPersianLeapYear() {
        //calculatePersianDate();
        return PersianCalendarUtils.isPersianLeapYear(this.persianYear);
    }


    /**
     * set the persian date
     * it converts PersianDate to the Julian and assigned equivalent milliseconds to the instance
     *
     * @param persianYear
     * @param persianMonth
     * @param persianDay
     */
    public void setPersianDate(int persianYear, int persianMonth, int persianDay) {
        this.persianYear = persianYear;
        this.persianMonth = persianMonth;
        this.persianDay = persianDay;
        setTimeInMillis(convertToMilis(PersianCalendarUtils.persianToJulian(this.persianYear > 0 ? this.persianYear
                : this.persianYear + 1, this.persianMonth, this.persianDay)));
    }

    public int getPersianYear() {
        //calculatePersianDate();
        return this.persianYear;
    }

    /**
     * @return int    persian month number
     */
    public int getPersianMonth() {
        //calculatePersianDate();
        return this.persianMonth;
    }

    /**
     * @return String   persian month name
     */
    public String getPersianMonthName() {
        //calculatePersianDate();
        return PersianCalendarConstants.persianMonthNames[this.persianMonth];
    }

    /**
     * @return int Persian day in month
     */
    public int getPersianDay() {
        //calculatePersianDate();
        return this.persianDay;
    }

    /**
     * @return String Name of the day in week
     */
    public String getPersianWeekDayName() {
        switch (get(DAY_OF_WEEK)) {
            case SATURDAY:
                return PersianCalendarConstants.persianWeekDays[0];
            case SUNDAY:
                return PersianCalendarConstants.persianWeekDays[1];
            case MONDAY:
                return PersianCalendarConstants.persianWeekDays[2];
            case TUESDAY:
                return PersianCalendarConstants.persianWeekDays[3];
            case WEDNESDAY:
                return PersianCalendarConstants.persianWeekDays[4];
            case THURSDAY:
                return PersianCalendarConstants.persianWeekDays[5];
            default:
                return PersianCalendarConstants.persianWeekDays[6];
        }

    }


    /**
     * @return String of Persian Date
     * ex: شنبه  01  خرداد  1361
     */
    public String getPersianLongDate() {
        return getPersianWeekDayName() + "  " + formatToMilitary(this.persianDay) + "  "
                + getPersianMonthName() + "  " + this.persianYear;

    }

    /**
     * @return String of persian date formatted by 'YYYY[delimiter]mm[delimiter]dd'
     * default delimiter is '/'
     */
    public String getPersianShortDate() {
        //calculatePersianDate();
        return "" + formatToMilitary(this.persianYear) + delimiter + formatToMilitary(getPersianMonth()) + delimiter
                + formatToMilitary(this.persianDay);
    }

    private String formatToMilitary(int i) {
        return (i <= 9) ? "0" + i : String.valueOf(i);
    }

    /**
     * add specific amout of fields to the current date
     * for now doesnt handle before 1 farvardin hejri (before epoch)
     *
     * @param field
     * @param amount
     */
    //
    public void addPersianDate(int field, int amount) {
        if (amount == 0) {
            return;   // Do nothing!
        }

        if (field < 0 || field >= ZONE_OFFSET) {
            throw new IllegalArgumentException();
        }

        if (field == YEAR) {
            setPersianDate(this.persianYear + amount, getPersianMonth(), this.persianDay);
            return;
        } else if (field == MONTH) {
            setPersianDate(this.persianYear + ((getPersianMonth() + amount) / 12), (getPersianMonth() + amount) % 12, this.persianDay);
            return;
        }
        add(field, amount);
        calculatePersianDate();
    }

    /**
     * <pre>
     *    use <code>{@link PersianDateParser}</code> to parse string
     *    and get the Persian Date.
     * </pre>
     *
     * @param dateString
     * @see PersianDateParser
     */
    public void parse(String dateString) {
        PersianCalendar p = new PersianDateParser(dateString, delimiter).getPersianDate();
        setPersianDate(p.getPersianYear(), p.getPersianMonth(), p.getPersianDay());
    }


    public String getDelimiter() {
        return delimiter;
    }


    /**
     * assign delimiter to use as a separator of date fields.
     *
     * @param delimiter
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }


    @Override
    public String toString() {
        String str = super.toString();
        return str.substring(0, str.length() - 1) + ",PersianDate="
                + getPersianShortDate() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void set(int field, int value) {
        super.set(field, value);
        calculatePersianDate();
    }

    @Override
    public void setTimeInMillis(long millis) {
        super.setTimeInMillis(millis);
        calculatePersianDate();
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        calculatePersianDate();
    }
}
