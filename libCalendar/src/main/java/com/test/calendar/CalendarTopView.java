package com.test.calendar;

public interface CalendarTopView {

    int[] getCurrentSelectPosition();

    int getItemHeight();

    void setCalendarTopViewChangeListener(CalendarTopViewChangeListener listener);

}
