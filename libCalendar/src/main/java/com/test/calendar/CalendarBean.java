package com.test.calendar;

public class CalendarBean {

    /**
     * BEFORE  42格当月之前的日期
     * CURRENT 42当月日期
     * AFTER   42格当月之后的日期
     */
    public enum MONTH_FLAG {
        BEFORE, CURRENT, AFTER
    }

    /**
     * BACKUP  在组里面但是没有自己的任务
     * WORK    在组里面同时有自己的任务
     * REST    当天没有任务
     */
    public enum DAY_FLAG {
        BACKUP, WORK, REST
    }

    public int year;
    public int moth;
    public int day;
    public int week;

    public boolean isToday;

    public MONTH_FLAG monthFlag = MONTH_FLAG.CURRENT;

    public DAY_FLAG dayFlag = DAY_FLAG.REST;

    //显示
    public String chinaMonth;
    public String chinaDay;

    public CalendarBean(int year, int moth, int day) {
        this.year = year;
        this.moth = moth;
        this.day = day;
    }

    public String getDisplayDay() {
        return "" + day;
    }

}