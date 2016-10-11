package com.jeek.calendar.widget.calendar.schedule;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.jeek.calendar.R;
import com.jeek.calendar.widget.calendar.CalendarUtils;
import com.jeek.calendar.widget.calendar.OnCalendarClickListener;
import com.jeek.calendar.widget.calendar.month.MonthCalendarView;
import com.jeek.calendar.widget.calendar.month.MonthView;
import com.jeek.calendar.widget.calendar.week.WeekCalendarView;
import com.jeek.calendar.widget.calendar.week.WeekView;

import java.util.Calendar;

/**
 * Created by Jimmy on 2016/10/7 0007.
 */
public class ScheduleLayout extends FrameLayout {

    private MonthCalendarView mcvCalendar;
    private WeekCalendarView wcvCalendar;
    private RelativeLayout rlMonthCalendar;
    private RelativeLayout rlScheduleList;
    private ScheduleRecyclerView rvScheduleList;

    private int mCurrentSelectYear;
    private int mCurrentSelectMonth;
    private int mCurrentSelectDay;
    private int mRowSize;
    private int mMinDistance;
    private int mAutoScrollDistance;
    private float mDownPosition[] = new float[2];
    private boolean mIsScrolling = false;

    private ScheduleState mState;
    private OnCalendarClickListener mOnCalendarClickListener;
    private GestureDetector mGestureDetector;

    public ScheduleLayout(Context context) {
        this(context, null);
    }

    public ScheduleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScheduleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs();
        initDate();
        initGestureDetector();
    }

    private void initAttrs() {
        mState = ScheduleState.OPEN;
        mRowSize = getResources().getDimensionPixelSize(R.dimen.week_calendar_height);
        mMinDistance = getResources().getDimensionPixelSize(R.dimen.calendar_min_distance);
        mAutoScrollDistance = getResources().getDimensionPixelSize(R.dimen.auto_scroll_distance);
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new OnScheduleScrollListener(this));
    }

    private void initDate() {
        Calendar calendar = Calendar.getInstance();
        resetCurrentSelectDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mcvCalendar = (MonthCalendarView) findViewById(R.id.mcvCalendar);
        wcvCalendar = (WeekCalendarView) findViewById(R.id.wcvCalendar);
        rlMonthCalendar = (RelativeLayout) findViewById(R.id.rlMonthCalendar);
        rlScheduleList = (RelativeLayout) findViewById(R.id.rlScheduleList);
        rvScheduleList = (ScheduleRecyclerView) findViewById(R.id.rvScheduleList);
        bindingMonthAndWeekCalendar();
    }

    private void bindingMonthAndWeekCalendar() {
        mcvCalendar.setOnCalendarClickListener(mMonthCalendarClickListener);
        wcvCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
        mcvCalendar.setVisibility(VISIBLE);
        wcvCalendar.setVisibility(GONE);
    }

    private void resetCurrentSelectDate(int year, int month, int day) {
        mCurrentSelectYear = year;
        mCurrentSelectMonth = month;
        mCurrentSelectDay = day;
    }

    private OnCalendarClickListener mMonthCalendarClickListener = new OnCalendarClickListener() {
        @Override
        public void onClickDate(int year, int month, int day) {
            wcvCalendar.setOnCalendarClickListener(null);
            int weeks = CalendarUtils.getWeeksAgo(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay, year, month, day);
            resetCurrentSelectDate(year, month, day);
            if (weeks != 0) {
                int position = wcvCalendar.getCurrentItem() + weeks;
                wcvCalendar.setCurrentItem(position, false);
            }
            resetWeekView();
            wcvCalendar.setOnCalendarClickListener(mWeekCalendarClickListener);
        }
    };

    private void resetWeekView() {
        WeekView weekView = wcvCalendar.getCurrentWeekView();
        if (weekView != null) {
            weekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            weekView.invalidate();
        } else {
            WeekView newWeekView = wcvCalendar.getWeekAdapter().instanceWeekView(wcvCalendar.getCurrentItem());
            newWeekView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            newWeekView.invalidate();
        }
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        }
    }

    private OnCalendarClickListener mWeekCalendarClickListener = new OnCalendarClickListener() {
        @Override
        public void onClickDate(int year, int month, int day) {
            mcvCalendar.setOnCalendarClickListener(null);
            int months = CalendarUtils.getMonthsAgo(mCurrentSelectYear, mCurrentSelectMonth, year, month);
            resetCurrentSelectDate(year, month, day);
            if (months != 0) {
                int position = mcvCalendar.getCurrentItem() + months;
                mcvCalendar.setCurrentItem(position, false);
            }
            resetMonthView();
            mcvCalendar.setOnCalendarClickListener(mMonthCalendarClickListener);
        }
    };

    private void resetMonthView() {
        MonthView monthView = mcvCalendar.getCurrentMonthView();
        if (monthView != null) {
            monthView.setSelectYearMonth(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
            monthView.invalidate();
        }
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        resetViewHeight(rlScheduleList, height - mRowSize);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void resetViewHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.height != height) {
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownPosition[0] = ev.getRawX();
                mDownPosition[1] = ev.getRawY();
                mGestureDetector.onTouchEvent(ev);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsScrolling) {
            return true;
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float x = ev.getRawX();
                float y = ev.getRawY();
                float distanceX = Math.abs(x - mDownPosition[0]);
                float distanceY = Math.abs(y - mDownPosition[1]);
                if (distanceY > mMinDistance && distanceY > distanceX * 2.0f) {
                    return (y > mDownPosition[1] && isRecyclerViewTouch()) || (y < mDownPosition[1] && mState == ScheduleState.OPEN);
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isRecyclerViewTouch() {
        return mState == ScheduleState.CLOSE && (rvScheduleList.getChildCount() == 0 || rvScheduleList.isScrollTop());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownPosition[0] = event.getRawX();
                mDownPosition[1] = event.getRawY();
                resetCalendarPosition();
                return true;
            case MotionEvent.ACTION_MOVE:
                transferEvent(event);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                transferEvent(event);
                changeCalendarState();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void transferEvent(MotionEvent event) {
        if (mState == ScheduleState.CLOSE) {
            mIsScrolling = true;
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
            mGestureDetector.onTouchEvent(event);
        } else {
            mIsScrolling = true;
            mGestureDetector.onTouchEvent(event);
        }
    }

    private void changeCalendarState() {
        if (rlScheduleList.getY() > mRowSize * 2 &&
                rlScheduleList.getY() < mcvCalendar.getHeight() - mRowSize) { // 位于中间
            ScheduleAnimation animation = new ScheduleAnimation(this, mState, mAutoScrollDistance);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    changeState();
                    resetScrollingState();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        } else if (rlScheduleList.getY() <= mRowSize * 2) { // 位于顶部
            ScheduleAnimation animation = new ScheduleAnimation(this, ScheduleState.OPEN, mAutoScrollDistance);
            animation.setDuration(50);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mState == ScheduleState.OPEN) {
                        changeState();
                    } else {
                        resetCalendar();
                    }
                    resetScrollingState();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        } else {
            ScheduleAnimation animation = new ScheduleAnimation(this, ScheduleState.CLOSE, mAutoScrollDistance);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mState == ScheduleState.CLOSE) {
                        mState = ScheduleState.OPEN;
                        resetScrollingState();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            rlScheduleList.startAnimation(animation);
        }
    }

    private void resetCalendarPosition() {
        if (mState == ScheduleState.OPEN) {
            rlMonthCalendar.setY(0);
            rlScheduleList.setY(mcvCalendar.getHeight());
        } else {
            rlMonthCalendar.setY(-CalendarUtils.getWeekRow(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay) * mRowSize);
            rlScheduleList.setY(mRowSize);
        }
    }

    private void resetCalendar() {
        if (mState == ScheduleState.OPEN) {
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
        } else {
            mcvCalendar.setVisibility(INVISIBLE);
            wcvCalendar.setVisibility(VISIBLE);
        }
    }

    private void changeState() {
        if (mState == ScheduleState.OPEN) {
            mState = ScheduleState.CLOSE;
            mcvCalendar.setVisibility(INVISIBLE);
            wcvCalendar.setVisibility(VISIBLE);
            rlMonthCalendar.setY((1 - mcvCalendar.getCurrentMonthView().getWeekRow()) * mRowSize);
        } else {
            mState = ScheduleState.OPEN;
            mcvCalendar.setVisibility(VISIBLE);
            wcvCalendar.setVisibility(INVISIBLE);
            rlMonthCalendar.setY(0);
        }
    }

    private void resetScrollingState() {
        mDownPosition[0] = 0;
        mDownPosition[1] = 0;
        mIsScrolling = false;
    }

    protected void onCalendarScroll(float distanceY) {
        MonthView monthView = mcvCalendar.getCurrentMonthView();
        distanceY = Math.min(distanceY, mAutoScrollDistance);
        float calendarDistanceY = distanceY / 5.0f;
        int row = monthView.getWeekRow() - 1;
        int calendarTop = -row * mRowSize;
        int scheduleTop = mRowSize;
        float calendarY = rlMonthCalendar.getY() - calendarDistanceY * row;
        calendarY = Math.min(calendarY, 0);
        calendarY = Math.max(calendarY, calendarTop);
        rlMonthCalendar.setY(calendarY);
        float scheduleY = rlScheduleList.getY() - distanceY;
        scheduleY = Math.min(scheduleY, mcvCalendar.getHeight());
        scheduleY = Math.max(scheduleY, scheduleTop);
        rlScheduleList.setY(scheduleY);
    }

    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }

    public ScheduleRecyclerView getSchedulerRecyclerView() {
        return rvScheduleList;
    }

    public MonthCalendarView getMonthCalendar() {
        return mcvCalendar;
    }

    public WeekCalendarView getWeekCalendar() {
        return wcvCalendar;
    }

    public int getCurrentSelectYear() {
        return mCurrentSelectYear;
    }

    public int getCurrentSelectMonth() {
        return mCurrentSelectMonth;
    }

    public int getCurrentSelectDay() {
        return mCurrentSelectDay;
    }
}
