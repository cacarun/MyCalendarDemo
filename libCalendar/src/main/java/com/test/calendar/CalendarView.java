package com.test.calendar;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.List;

public class CalendarView extends ViewGroup {

    private static final String TAG = "CalendarView";

    private int selectPosition = -1;

    private CalendarAdapter adapter;
    private List<CalendarBean> data;
    private OnItemClickListener onItemClickListener;
    private OnDayFlagChangeListener onDayFlagChangeListener;

    private int row = 6;
    private int column = 7;
    private int itemWidth;
    private int itemHeight;

    private boolean isCurrentMonth;

    private Date selectedDate;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, CalendarBean bean, boolean isOnPageSelected);
    }

    public interface OnDayFlagChangeListener {
        void onDayFlagChange(View view, CalendarBean bean);
    }

    public CalendarView(Context context, int row, Date selectedDate) {
        super(context);
        this.row = row;
        this.selectedDate = selectedDate;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnDayFlagChangeListener(OnDayFlagChangeListener onDayFlagChangeListener) {
        this.onDayFlagChangeListener = onDayFlagChangeListener;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public void setAdapter(CalendarAdapter adapter) {
        this.adapter = adapter;
    }

    public void setData(List<CalendarBean> data, boolean isCurrentMonth) {
        this.data = data;
        this.isCurrentMonth = isCurrentMonth;
        setItem();
        requestLayout();
    }

    private void setItem() {

        selectPosition = -1;

        if (adapter == null) {
            throw new RuntimeException("adapter is null, please set adapter");
        }

        for (int i = 0; i < data.size(); i++) {

            CalendarBean bean = data.get(i);

            // 根据 data 里面是否有当天显示选中效果
            if (isCurrentMonth && selectPosition == -1) {

                int[] date = CalendarUtil.getYMD(selectedDate);

                if (bean.year == date[0] && bean.moth == date[1] && bean.day == date[2]) {
                    // 当天日期就选中当天的位置
                    selectPosition = i;
                    bean.isToday = true;
                }
            } else {
                if (selectPosition == -1 && bean.day == 1) {
                    // 其他日期就选中1号的位置
                    selectPosition = i;
                }
            }

            View view = getChildAt(i);
            View childView = adapter.getView(view, this, bean);

            if (view == null || view != childView) {
                addViewInLayout(childView, i, childView.getLayoutParams(), true);
            }

            childView.setSelected(selectPosition == i);

            setItemClick(childView, i, bean);
        }

    }

    public Object[] getSelect() {
        return new Object[]{getChildAt(selectPosition), selectPosition, data.get(selectPosition)};
    }

    public void setItemClick(final View view, final int position, final CalendarBean bean) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectPosition != -1) {
                    getChildAt(selectPosition).setSelected(false);
                    getChildAt(position).setSelected(true);
                }
                selectPosition = position;

                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, position, bean, false);
                }
            }
        });
    }

    public int[] getSelectPositionRect() {
        Rect rect = new Rect();
        try {
            getChildAt(selectPosition).getHitRect(rect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{rect.left, rect.top, rect.right, rect.top};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY));

        itemWidth = parentWidth / column;
        itemHeight = itemWidth;

        View view = getChildAt(0); // 需要判断是否有 view
        if (view == null) {
            return;
        }
        LayoutParams params = view.getLayoutParams();
        if (params != null && params.height > 0) {
            itemHeight = params.height;
        }

        int sizeInPixels = (int) getResources().getDimension(R.dimen.libcalendar_calendar_view_padding_bottom);
        setMeasuredDimension(parentWidth, itemHeight * row + sizeInPixels);

        // measure children
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            childView.measure(MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY));
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            layoutChild(getChildAt(i), i, l, t, r, b);
        }
    }

    private void layoutChild(View view, int position, int l, int t, int r, int b) {

        int cc = position % column;
        int cr = position / column;

        int itemWidth = view.getMeasuredWidth();
        int itemHeight = view.getMeasuredHeight();

        l = cc * itemWidth;
        t = cr * itemHeight;
        r = l + itemWidth;
        b = t + itemHeight;
        view.layout(l, t, r, b);

    }

    public List<CalendarBean> getData() {
        return data;
    }

    public void updateDayFlagStyle() {
        for (int i = 0; i < data.size(); i++) {
            CalendarBean bean = data.get(i);
            View view = getChildAt(i);
            onDayFlagChangeListener.onDayFlagChange(view, bean);
        }
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }
}
