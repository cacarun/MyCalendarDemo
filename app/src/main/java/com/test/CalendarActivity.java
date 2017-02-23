package com.test;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.test.calendar.CalendarAdapter;
import com.test.calendar.CalendarBean;
import com.test.calendar.CalendarDateView;
import com.test.calendar.CalendarView;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.test.calendar.CalendarUtil.getDisplayYMD;

/**
 * 日历控件 + RecyclerView 和 ListView 支持
 *
 */
public class CalendarActivity extends AppCompatActivity {

    @BindView(R.id.calendarDateView)
    CalendarDateView viewCalendarDateView;
//    @BindView(R.id.list)
//    ListView mList;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.title)
    TextView tvTitle;

    private Date dateNow;
    private String currentDateStr;

    @OnClick(R.id.back)
    public void onClick() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);


        currentDateStr = getString(R.string.work_schedule_year_month_day);

        dateNow = new Date();

        String[] currentYMD = getDisplayYMD(dateNow);
        tvTitle.setText(String.format(currentDateStr, currentYMD[0], currentYMD[1], currentYMD[2]));


        viewCalendarDateView.setInitData(dateNow, new CalendarAdapter() {
            @Override
            public View getView(View convertView, ViewGroup parentView, CalendarBean bean) {

                if (convertView == null) {

                    convertView = LayoutInflater.from(parentView.getContext()).inflate(R.layout.item_calendar, null);

                    ViewGroup.LayoutParams params = new ViewGroup
                            .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    convertView.setLayoutParams(params);

                }

                TextView tvDay = (TextView) convertView.findViewById(R.id.tv_day);
                TextView tvDot = (TextView) convertView.findViewById(R.id.tv_dot);

                tvDay.setText(bean.getDisplayDay());

                if (bean.monthFlag == CalendarBean.MONTH_FLAG.CURRENT) {
                    tvDay.setTextColor(ContextCompat.getColor(CalendarActivity.this, R.color.main_color));
                } else {
                    tvDay.setTextColor(ContextCompat.getColor(CalendarActivity.this, R.color.main_sub_color));
                }

                if (bean.isToday) {
                    tvDay.setBackgroundResource(R.drawable.selector_calendar_item_today_bg);
                } else {
                    tvDay.setBackgroundResource(R.drawable.selector_calendar_item_bg);
                }

                return convertView;
            }
        });

        viewCalendarDateView.setOnItemClickListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CalendarBean bean, boolean isOnPageSelected) {

                // 1.在当前月份点击不同日期，更新计划
                // 2.在当前月份点击之前月份或之后月份日期，ViewPage 左右滑动，选中选中的日期，更新日历和对应日期的计划
                // 3.ViewPage 左右滑动，当前月选中当前日期，其他月显示1号日期，同时更新日历和对应日期的计划
                if (bean != null) {

                    TextView tvDay = (TextView)(((LinearLayout)view).getChildAt(0));
                    if (bean.monthFlag == CalendarBean.MONTH_FLAG.CURRENT) {
                        tvDay.setTextColor(ContextCompat.getColorStateList(CalendarActivity.this, R.color.selector_calendar_item_color_one));
                    } else {
                        tvDay.setTextColor(ContextCompat.getColorStateList(CalendarActivity.this, R.color.selector_calendar_item_color_two));
                    }

                    if (isOnPageSelected) {

                        // ViewPage 左右滑动，更新标题，弹出 loading 对话框请求当月日历和当天计划

                        String[] displayYMD = getDisplayYMD(bean.year, bean.moth, bean.day);
                        tvTitle.setText(String.format(currentDateStr, displayYMD[0], displayYMD[1],
                                displayYMD[2]));

                        testDayFlagStyle();
                        updateData();

                    } else {

                        if (bean.monthFlag == CalendarBean.MONTH_FLAG.CURRENT) {

                            // 在当前月内切换日期，更新标题，没有对话框，请求当天计划

                            String[] displayYMD = getDisplayYMD(bean.year, bean.moth, bean.day);
                            tvTitle.setText(String.format(currentDateStr, displayYMD[0], displayYMD[1],
                                    displayYMD[2]));

                            updateData();

                        } else {
                            // 根据 flag 切换到上个月或下个月的那一天，最终会调用 if (isOnPageSelected) 里面的逻辑
                            viewCalendarDateView.setCurrentCalendarViewItem(bean);
                        }
                    }

                }
            }
        });

        viewCalendarDateView.setOnDayFlagChangeListener(new CalendarView.OnDayFlagChangeListener() {
            /**
             * 根据 DAY_FLAG 状态更新当前一屏日期底部点的样式
             *
             * DAY_FLAG：
             * BACKUP  在组里面但是没有自己的任务
             * WORK    在组里面同时有自己的任务
             * REST    当天没有任务
             *
             */
            @Override
            public void onDayFlagChange(View view, CalendarBean bean) {
                if (bean != null) {
                    TextView tvDot = (TextView)((LinearLayout)view).getChildAt(1);
                    if (bean.dayFlag == CalendarBean.DAY_FLAG.WORK) {
                        tvDot.setBackgroundResource(R.drawable.shape_calendar_item_dot_work);
                    } else if (bean.dayFlag == CalendarBean.DAY_FLAG.BACKUP) {
                        tvDot.setBackgroundResource(R.drawable.shape_calendar_item_dot_backup);
                    } else if (bean.dayFlag == CalendarBean.DAY_FLAG.REST) {
                        tvDot.setBackgroundResource(0);
                    }
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new TestAdapter());


        testDayFlagStyle();


//        mList.setAdapter(new BaseAdapter() {
//            @Override
//            public int getCount() {
//                return 100;
//            }
//
//            @Override
//            public Object getItem(int position) {
//                return null;
//            }
//
//            @Override
//            public long getItemId(int position) {
//                return 0;
//            }
//
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                if (convertView == null) {
//                    convertView = LayoutInflater.from(CalendarActivity.this).inflate(android.R.layout.simple_list_item_1, null);
//                }
//
//                TextView textView = (TextView) convertView;
//                textView.setText("item" + position);
//
//                return convertView;
//            }
//        });

    }

    private void updateData() {
        // 请求数据返回操作
    }

    private void testDayFlagStyle() {
        // test
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // update flag status
                viewCalendarDateView.updateCurrentCalendarViewDayFlagStyle();
            }
        }, 3000);
    }

    class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

        @Override
        public TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TestViewHolder holder = new TestViewHolder(LayoutInflater.from(
                    CalendarActivity.this).inflate(R.layout.item_view, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(TestViewHolder holder, int position) {
            holder.tv.setText("item position: " + position);
        }

        @Override
        public int getItemCount() {
            return 100;
        }

        class TestViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            public TestViewHolder(View view) {
                super(view);
                tv = (TextView) view.findViewById(R.id.tv_text);
            }
        }
    }

}
