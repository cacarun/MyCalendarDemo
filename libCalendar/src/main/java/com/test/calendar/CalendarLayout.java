package com.test.calendar;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

public class CalendarLayout extends FrameLayout {

    private static final String TAG = "CalendarLayout";

    private static final int INDEX_VIEW1 = 0;
    private static final int INDEX_VIEW2 = 1;

    private View view1;
    private ViewGroup view2;
    private CalendarTopView mTopView;
    //展开
    public static final int TYPE_OPEN = 0;
    //折叠
    public static final int TYPE_FOLD = 1;
    public int type = TYPE_FOLD;

    //是否处于滑动中
    private boolean isSlide = false;

    private int topHeight;
    private int itemHeight;
    private int bottomViewTopHeight;
    private int maxDistance;

    private ScrollerCompat mScroller;
    private float mMaxVelocity;
    private float mMinVelocity;
    private int activePointerId;

    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public CalendarLayout(Context context) {
        super(context);
        init();
    }

    public CalendarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onFinishInflate() { // 当View及其子View从XML文件中加载完成后会被调用
        super.onFinishInflate();

        final CalendarTopView viewPager = (CalendarTopView) getChildAt(INDEX_VIEW1);

        mTopView = viewPager;
        view1 = (View) viewPager;
        view2 = (ViewGroup) getChildAt(INDEX_VIEW2);

        mTopView.setCalendarTopViewChangeListener(new CalendarTopViewChangeListener() {
            @Override
            public void onLayoutChange(CalendarTopView topView) {
                CalendarLayout.this.requestLayout();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        itemHeight = mTopView.getItemHeight(); // 日历一行高度
        topHeight = view1.getMeasuredHeight(); // 日历当前总高度

        maxDistance = topHeight - itemHeight;

        switch (type) {
            case TYPE_FOLD:
                bottomViewTopHeight = itemHeight;
                break;
            case TYPE_OPEN:
                bottomViewTopHeight = topHeight;
                break;
        }

        // 重设 list 高度（不然数据少的时候，日历会露出来）
        view2.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec
                .getSize(heightMeasureSpec) - mTopView.getItemHeight(), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        view2.offsetTopAndBottom(bottomViewTopHeight);

        if (type == TYPE_FOLD) {
            int[] selectRct = getSelectRect(); // 得到当前选中的日期的位置
            view1.offsetTopAndBottom(-selectRct[1]); // 定位到当前选中天的位置
        }
    }

    private void init() {

        final ViewConfiguration vc = ViewConfiguration.get(getContext());

        mMaxVelocity = vc.getScaledMaximumFlingVelocity(); // 最大加速率
        mMinVelocity = vc.getScaledMinimumFlingVelocity(); // 最小加速率
        mScroller = ScrollerCompat.create(getContext(), sInterpolator);
    }

    float oy, ox;
    boolean isClickBottomView = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isFlag = false;

        //上下运动进行拦截
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oy = ev.getY();
                ox = ev.getX();

                isClickBottomView = isClickView(view2, ev);

                cancel();

                activePointerId = ev.getPointerId(0); // 活跃的触点

                int top = view2.getTop();

                if (top < topHeight) {
                    type = TYPE_FOLD;
                } else {
                    type = TYPE_OPEN;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                float x = ev.getX();

                float xDiff = x - ox;
                float yDiff = y - oy;
                if (Math.abs(yDiff) > 5 && Math.abs(yDiff) > Math.abs(xDiff)) { // 有效的上下滑动

                    isFlag = true;
                    if (isClickBottomView) {
                        boolean isScroll = isScroll(view2);

                        if (yDiff > 0) {
                            //向下
                            if (type == TYPE_OPEN) {
                                return super.onInterceptTouchEvent(ev);
                            } else {
                                if (isScroll) {
                                    return super.onInterceptTouchEvent(ev);
                                }

                            }
                        } else {
                            //向上
                            if (type == TYPE_FOLD) {
                                return super.onInterceptTouchEvent(ev);
                            } else {
                                if (isScroll) {
                                    return super.onInterceptTouchEvent(ev);
                                }
                            }
                        }

                    }
                }
                ox = x;
                oy = y;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }

        return isSlide || isFlag || super.onInterceptTouchEvent(ev);
    }

    // 判断列表是否正在滑动中，支持 ListView 和 RecyclerView
    private boolean isScroll(ViewGroup view2) {
        View fistChildView = view2.getChildAt(INDEX_VIEW1);
        if (fistChildView == null) {
            return false;
        }

        if (view2 instanceof ListView) {
            AbsListView list = (AbsListView) view2;
            if (fistChildView.getTop() != 0) {
                return true;
            } else {
                if (list.getPositionForView(fistChildView) != 0) {
                    return true;
                }
            }
        } else if (view2 instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view2;
            if (fistChildView.getTop() != 0) {
                return true;
            } else {
                if (recyclerView.getChildAdapterPosition(fistChildView) != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // 位置是否在当前 view 区域
    public boolean isClickView(View view, MotionEvent ev) {
        Rect rect = new Rect();
        view.getHitRect(rect);
        return rect.contains((int) ev.getX(), (int) ev.getY());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        processTouchEvent(event);
        return true;
    }

    private VelocityTracker mVelocityTracker; // 速度跟踪，通过跟踪一连串事件实时计算出当前的速度


    public void processTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSlide) {
                    return;
                }
                float cy = event.getY();
                int dy = (int) (cy - oy);

                if (dy == 0) {
                    return;
                }
                oy = cy;
                move(dy);

                break;
            case MotionEvent.ACTION_UP:

                if (isSlide) {
                    cancel();
                    return;
                }

                //判断速度
                final int pointerId = activePointerId;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                float currentV = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);

                if (Math.abs(currentV) > 2000) {
                    if (currentV > 0) {
                        open();
                    } else {
                        flod();
                    }
                    cancel();
                    return;
                }

                int top = view2.getTop() - topHeight;
                int maxD = maxDistance;


                if (Math.abs(top) < maxD / 2) {
                    open();
                } else {
                    flod();
                }
                cancel();

                break;
            case MotionEvent.ACTION_CANCEL:
                cancel();
                break;
        }
    }

    public void open() {
        startScroll(view2.getTop(), topHeight);
    }

    public void flod() {
        startScroll(view2.getTop(), topHeight - maxDistance);
    }

    private int[] getSelectRect() {
        return mTopView.getCurrentSelectPosition();
    }

    private void move(int dy) {

        int[] selectRect = getSelectRect();
        int itemHeight = mTopView.getItemHeight();

        int dy1 = getAreaValue(view1.getTop(), dy, -selectRect[1], 0);
        int dy2 = getAreaValue(view2.getTop() - topHeight, dy, -(topHeight - itemHeight), 0);

        if (dy1 != 0) {
            ViewCompat.offsetTopAndBottom(view1, dy1);
        }

        if (dy2 != 0) {
            ViewCompat.offsetTopAndBottom(view2, dy2);
        }

    }

    private int getAreaValue(int top, int dy, int minValue, int maxValue) {

        if (top + dy < minValue) {
            return minValue - top;
        }

        if (top + dy > maxValue) {
            return maxValue - top;
        }
        return dy;
    }

    private void startScroll(int startY, int endY) {

        float distance = endY - startY;
        float t = distance / maxDistance * 600;

        mScroller.startScroll(0, 0, 0, endY - startY, (int) Math.abs(t));
        postInvalidate();
    }

    int oldY = 0;

    @Override
    public void computeScroll() {
        super.computeScroll();

        bottomViewTopHeight = view2.getTop();
        if (mScroller.computeScrollOffset()) {
            isSlide = true;
            int cy = mScroller.getCurrY();
            int dy = cy - oldY;
            move(dy);
            oldY = cy;
            postInvalidate();
        } else {
            oldY = 0;
            isSlide = false;
        }

    }

    public void cancel() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

}
