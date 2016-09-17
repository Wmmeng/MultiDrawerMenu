package com.wmmeng.multicardmenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * Created by wmmeng on 16/9/16.
 */
public class MultiCardMenu extends ViewGroup {

    private static final String TAG = "wmm";
    public static final int INVAILD_POSITION = -1;
    private Interpolator openInterpolator = new AccelerateInterpolator();
    private Interpolator closeInterpolator = new DecelerateInterpolator();
    private int titleBarHeight = dip2px(60);
    private int titleBarHeightOfDisplay = dip2px(20);

    private int mShowingDrawerIndex = INVAILD_POSITION;

    private int mActionDownIndex = INVAILD_POSITION;

    private int mMarginTop = 10;

    private int mChildCount;

    private boolean isOpen;

    private boolean isAnimating;

    private View mTouchingCard;

    private float mTouchCardOriginY;

    private VelocityTracker mVelocityTracker;

    private float xVelocity;

    private float yVelocity;

    private float mEventDownX;

    private float mEventDownY;

    private float mLastEventY;

    public MultiCardMenu(Context context) {
        super(context);
    }

    public MultiCardMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiCardMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initVelocityTracker(MotionEvent event) {
        if(mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if(mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void computeVelocity(MotionEvent event){
        mVelocityTracker.addMovement(event);
        xVelocity = mVelocityTracker.getXVelocity();
        yVelocity = mVelocityTracker.getYVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        mChildCount = getChildCount();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed) {
            for (int i = 0; i < mChildCount; i++) {
                View child = getChildAt(i);
                int top = getMeasuredHeight() - (mChildCount - i) * (titleBarHeight);
                child.layout(0, top, child.getMeasuredWidth(), child.getMeasuredHeight() + top);
            }
        }
    }


    public void openDrawer(int index){
        if(index < 0 || index >= getChildCount()){
            throw  new IllegalArgumentException("index is out of child count");
        }
        if(isAnimating) {
            return;
        }
        mShowingDrawerIndex = index;
        int childCount = mChildCount;
        int marginTop = dip2px(mMarginTop);
        final View showCard = getChildAt(index);
        ArrayList<Animator> animators = new ArrayList<>();
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator displayTopAnimator = ValueAnimator.ofFloat(showCard.getY(), marginTop);
        displayTopAnimator.setInterpolator(openInterpolator);
        displayTopAnimator.setTarget(showCard);
        displayTopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                showCard.setY((Float) animation.getAnimatedValue());
            }
        });
        animators.add(displayTopAnimator);

        int j = 1;
        for (int i = 0; i < childCount; i++) {
            if(i != index) {
                final View c = getChildAt(i);
                animators.add(ObjectAnimator.ofFloat(c, "y", c.getY(), getMeasuredHeight() -
                        (childCount - j) *
                titleBarHeightOfDisplay));
                j++;
            }
        }


        animatorSet.playTogether(animators);
        animatorSet.setInterpolator(openInterpolator);
        animatorSet.setDuration(300);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
                isOpen = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();
    }

    private void closeDrawer(){
        if(isAnimating) {
            return;
        }

        ArrayList<Animator> animators = new ArrayList<>();
        if(mShowingDrawerIndex != -1) {
            final View showCard = getChildAt(mShowingDrawerIndex);
            int showCardTop = getMeasuredHeight() - (mChildCount - mShowingDrawerIndex) * titleBarHeight;
            ValueAnimator closeAnimator = ValueAnimator.ofFloat(showCard.getY(), showCardTop);
            closeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    showCard.setY((Float) animation.getAnimatedValue());
                }
            });
            animators.add(closeAnimator);
        }

        for (int i = 0; i < mChildCount; i++) {
            if(i != mShowingDrawerIndex) {
                View c = getChildAt(i);
                int top = getMeasuredHeight() - (mChildCount - i) * titleBarHeight;
                ObjectAnimator animator = ObjectAnimator.ofFloat(c, "y", c.getY(), top);
                animators.add(animator);
            }
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.setInterpolator(closeInterpolator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
                isOpen = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mShowingDrawerIndex = -1;
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.setDuration(300);
        animatorSet.start();
    }

    private boolean handleActionDown(MotionEvent event){
        if(isAnimating) {
            return false;
        }
        float downX = event.getX();
        float downY = event.getY();
        mEventDownX = downX;
        mEventDownY = mLastEventY = downY;
        int index = pointToPosition(downX, downY);
        if(index == INVAILD_POSITION) {
            return false;
        }
        mActionDownIndex = index;
        mTouchingCard = getChildAt(index);
        mTouchCardOriginY = mTouchingCard.getY();
        return true;
    }


    private void handleActionMove(MotionEvent event){
        if(mActionDownIndex == INVAILD_POSITION || isAnimating) {
            return;
        }

        if(isOpen && mActionDownIndex != mShowingDrawerIndex) {
            return;
        }

        computeVelocity(event);
        if(Math.abs(yVelocity) < Math.abs(xVelocity)) return;
        float deltaY = event.getY() - mLastEventY;
        int originTop = getMeasuredHeight() - (mChildCount - mActionDownIndex) * titleBarHeight;
        int marginTop = dip2px(mMarginTop);
        if(mTouchingCard.getY() + deltaY >= originTop) {
            mTouchingCard.offsetTopAndBottom((int) (originTop - mTouchingCard.getY()));
        } else if(mTouchingCard.getY() + deltaY <= marginTop){
            mTouchingCard.offsetTopAndBottom((int) (marginTop - mTouchingCard.getY()));
        }else{
            mTouchingCard.offsetTopAndBottom((int) deltaY);
        }

        mLastEventY = event.getY();
    }


    private void handleActionUpOrCancel(MotionEvent ev) {
        if(isAnimating) return;
        if(isOpen) {
            if(Math.abs(ev.getY() - mEventDownY) > 50 && mActionDownIndex != -1){
                closeDrawer();
            } else if(mActionDownIndex != -1){
                openDrawer(mActionDownIndex);
            }
        } else {
            if(Math.abs(ev.getY() - mEventDownY) > 50 && mActionDownIndex != -1){
                openDrawer(mActionDownIndex);
            } else if(mActionDownIndex != -1){
                closeDrawer();
            }
        }
        mTouchCardOriginY = 0;
        mActionDownIndex = -1;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        initVelocityTracker(ev);
        boolean isConsume = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isConsume = handleActionDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleActionUpOrCancel(ev);
                releaseVelocityTracker();
                break;
        }
        return isConsume || super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!isOpen){
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }


    private int pointToPosition(float x, float y){
        int childCount = mChildCount;
        Rect frame = new Rect();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.getHitRect(frame);
            frame.bottom = frame.top + titleBarHeight;
            if(frame.contains((int)x, (int)y)) {
                return i;
            }
        }
        return INVAILD_POSITION;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
