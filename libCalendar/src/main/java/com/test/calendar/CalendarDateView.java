package com.test.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.test.calendar.CalendarFactory.getMonthOfDayList;

public class CalendarDateView extends ViewPager implements CalendarTopView {

    private static final String TAG = CalendarDateView.class.getName();

    private static final int CALENDAR_ROW = 6;

    HashMap<Integer, CalendarView> views = new HashMap<>();
    private CalendarTopViewChangeListener mCalendarLayoutChangeListener;
    private CalendarView.OnItemClickListener onItemClickListener;
    private CalendarView.OnDayFlagChangeListener onDayFlagChangeListener;

    private LinkedList<CalendarView> cache = new LinkedList();

    private int row = 6;

    private CalendarAdapter mAdapter;
    private int calendarItemHeight = 0;

    private Date dateNow = new Date();

    public void setInitData(Date dateNow, CalendarAdapter adapter) {
        mAdapter = adapter;
        this.dateNow = dateNow;

        init();

        // 选中当前月
        setCurrentItem(Integer.MAX_VALUE / 2, false);
        getAdapter().notifyDataSetChanged();
    }

    public void setOnItemClickListener(CalendarView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnDayFlagChangeListener(CalendarView.OnDayFlagChangeListener onDayFlagChangeListener) {
        this.onDayFlagChangeListener = onDayFlagChangeListener;
    }

    public CalendarDateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.libcalendar_calendar_date_view);
        row = a.getInteger(R.styleable.libcalendar_calendar_date_view_libcalendar_row, CALENDAR_ROW);
        a.recycle();
//        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int calendarHeight = 0;
        if (getAdapter() != null) {
            CalendarView view = (CalendarView) getChildAt(0);
            if (view != null) {
                calendarHeight = view.getMeasuredHeight();
                calendarItemHeight = view.getItemHeight();
            }
        }
        // 设置对应的高度
        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(calendarHeight, MeasureSpec.EXACTLY));
    }

    private void init() {
        final int[] dateArr = CalendarUtil.getYMD(dateNow);

        setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {

                CalendarView view;

                if (!cache.isEmpty()) {
                    view = cache.removeFirst();
                } else {
                    view = new CalendarView(container.getContext(), row, dateNow);
                }

                view.setOnItemClickListener(onItemClickListener);
                view.setOnDayFlagChangeListener(onDayFlagChangeListener);
                view.setAdapter(mAdapter);

                view.setData(getMonthOfDayList(dateArr[0], dateArr[1] + position - Integer.MAX_VALUE / 2), position == Integer.MAX_VALUE / 2);

                container.addView(view);
                views.put(position, view);

                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
                cache.addLast((CalendarView) object);
                views.remove(position);
            }
        });

        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // 进入这里有两种情况：
                // 1.每次左右滑动选中当月的第一天或当天
                // 2.当月点击上个月的日期或下个月的日期，会滑动到上一页或下一页同时选中对应日期
                if (onItemClickListener != null) {
                    CalendarView view = views.get(position);
                    Object[] obs = view.getSelect();
                    onItemClickListener.onItemClick((View) obs[0], (int) obs[1], (CalendarBean) obs[2], true);
                }

                mCalendarLayoutChangeListener.onLayoutChange(CalendarDateView.this);
            }
        });

    }

    @Override
    public int[] getCurrentSelectPosition() {
        CalendarView view = views.get(getCurrentItem());
        if (view == null) {
            view = (CalendarView) getChildAt(0);
        }
        if (view != null) {
            return view.getSelectPositionRect();
        }
        return new int[4];
    }

    @Override
    public int getItemHeight() {
        return calendarItemHeight;
    }

    @Override
    public void setCalendarTopViewChangeListener(CalendarTopViewChangeListener listener) {
        mCalendarLayoutChangeListener = listener;
    }

    /**
     * 取得当前 page 的所有日期列表
     * @return
     */
    public List<CalendarBean> getCurrentCalendarBeanList() {
        CalendarView view = views.get(getCurrentItem());
        return view == null ? null : view.getData();
    }

    /**
     * 取得当前选中的位置
     * @return
     */
    public int getSelectCalendarBeanPosition() {
        CalendarView view = views.get(getCurrentItem());
        return view.getSelectPosition();
    }

    /**
     * 更新日期状态
     */
    public void updateCurrentCalendarViewDayFlagStyle() {
        CalendarView view = views.get(getCurrentItem());
        view.updateDayFlagStyle();
    }

    /**
     * 根据选中的日期跳到指定的月份的日期
     * @param bean
     */
    public void setCurrentCalendarViewItem(CalendarBean bean) {

        int currentPos = getCurrentItem();

        if (bean.monthFlag == CalendarBean.MONTH_FLAG.CURRENT) {
            // 第一次初始化调用
            if (onItemClickListener != null) {
                CalendarView view = views.get(currentPos);
                Object[] obs = view.getSelect();
                onItemClickListener.onItemClick((View) obs[0], (int) obs[1], (CalendarBean) obs[2],
                        true);
            }

        } else {
            int pos = bean.monthFlag == CalendarBean.MONTH_FLAG.BEFORE ? currentPos - 1
                    : currentPos + 1;

            CalendarView view = views.get(pos);
            List<CalendarBean> beanList = view.getData();

            int selectPosition = -1;
            for (int i = 0; i < beanList.size(); i++) {

                CalendarBean item = beanList.get(i);

                if (item.year == bean.year && item.moth == bean.moth && item.day == bean.day) {
                    selectPosition = i;
                    break;
                }
            }

            if (selectPosition != -1) {

                int originSelectPos = view.getSelectPosition();
                if (originSelectPos != selectPosition) { // 是否是同一个选中的日期

                    // 把之前日期的选中效果去掉
                    view.getChildAt(originSelectPos).setSelected(false);

                    // 更新状态
                    view.setSelectPosition(selectPosition);
                    // 为选中日期设置选中效果
                    view.getChildAt(selectPosition).setSelected(true);
                }
            }

            setCurrentItem(pos);
        }
    }
}
