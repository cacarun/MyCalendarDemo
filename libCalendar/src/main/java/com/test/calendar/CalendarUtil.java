package com.test.calendar;

import java.util.Calendar;
import java.util.Date;

public class CalendarUtil {

    //获取一月的第一天是星期几
    public static int getDayOfWeek(int y, int m, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(y, m - 1, day);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    //获取一月最大天数
    public static int getDayOfMonth(int y, int m) {
        Calendar cal = Calendar.getInstance();
        cal.set(y, m - 1, 1);
        int dateOfMonth = cal.getActualMaximum(Calendar.DATE);
        return dateOfMonth;
    }

    public static int getMonthOfMonth(int y, int m) {
        Calendar cal = Calendar.getInstance();
        cal.set(y, m - 1, 1);
        int dateOfMonth = cal.get(Calendar.MONTH);
        return dateOfMonth + 1;
    }

    public static int[] getYMD(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};
    }

    public static String[] getDisplayYMD(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new String[] { "" + cal.get(Calendar.YEAR),
                getDisplayNumber(cal.get(Calendar.MONTH) + 1),
                getDisplayNumber(cal.get(Calendar.DATE))};
    }

    public static String[] getDisplayYMD(int year, int month, int day) {
        return new String[] { "" + year, getDisplayNumber(month), getDisplayNumber(day)};
    }

    // 2017-02-20
    public static String getDisplayDate(int year, int month, int day) {
        return year + "-" + getDisplayNumber(month) + "-" + getDisplayNumber(day);
    }

    private static String getDisplayNumber(int num) {
        return num < 10 ? "0" + num : "" + num;
    }
}
