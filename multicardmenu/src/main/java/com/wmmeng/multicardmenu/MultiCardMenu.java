package com.wmmeng.multicardmenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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

    private int currentIndex = -1;

    private int mActionDownIndex = -1;

    private int mMarginTop = 10;

    private int mChildCount;

    private boolean isOpen;

    private boolean isAnimating;

    private View mTouchingCard;

    private float mTouchCardOriginY;

    private VelocityTracker mVelocityTracker;
    public MultiCardMenu(Context context) {
        super(context);
    }

    public MultiCardMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiCardMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(){
        mVelocityTracker = mVelocityTracker.obtain();
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


    public void openMenu(int index){
        if(index < 0 || index >= getChildCount()){
            throw  new IllegalArgumentException("index is out of child count");
        }
        if(isAnimating) {
            return;
        }
        isOpen = true;
        currentIndex = index;
        int childCount = mChildCount;
        int marginTop = dip2px(mMarginTop);
        final View showCard = getChildAt(index);
        ArrayList<Animator> animators = new ArrayList<>();
        AnimatorSet animatorSet = new AnimatorSet();
//        ValueAnimator displayAnimator = ValueAnimator.ofFloat(showCard.getY(), marginTop);
//        displayAnimator.setInterpolator(openInterpolator);
//        displayAnimator.setTarget(showCard);
//        displayAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                showCard.setY((Float) animation.getAnimatedValue());
//            }
//        });
        ObjectAnimator displayAnimator = ObjectAnimator.ofInt(showCard, "top", showCard
                .getTop(),
                marginTop);
        int j = 1;
        for (int i = 0; i < childCount; i++) {
            if(i != index) {
                final View c = getChildAt(i);
                ValueAnimator animator = ValueAnimator.ofFloat(c.getY(),
                        (getMeasuredHeight() - (childCount - j) * (titleBarHeightOfDisplay)));
                animator.setTarget(getChildAt(i));
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        c.setY((Float) animation.getAnimatedValue());
                    }
                });
                animators.add(animator);
                j++;
            }
        }

        animators.add(displayAnimator);
        animatorSet.playTogether(animators);
        animatorSet.setInterpolator(openInterpolator);
        animatorSet.setDuration(300);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
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

    private void closeMenu(){
        Log.d(TAG, "closeMenu: currentIndex->" +currentIndex);
        if(currentIndex == -1 || isAnimating) {
            return;
        }
        isOpen = false;
        ArrayList<Animator> animators = new ArrayList<>();
        View showCard = getChildAt(currentIndex);
        int showCardTop = getMeasuredHeight() - (mChildCount - currentIndex) * titleBarHeight;
        ObjectAnimator closeAnimator = ObjectAnimator.ofInt(showCard, "top", showCard.getTop(),
                showCardTop);
        animators.add(closeAnimator);
        for (int i = 0; i < mChildCount; i++) {
            if(i != currentIndex) {
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
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentIndex = -1;
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
        float downX = event.getX();
        float downY = event.getY();
        lastY = downY;
        int index = pointToPosition(downX, downY);
        Log.d(TAG, "handleActionDown: index->" + index);
        mActionDownIndex = index;
        if(index == INVAILD_POSITION) {
            return false;
        }
        mTouchingCard = getChildAt(index);
        mTouchCardOriginY = mTouchingCard.getY();
        return true;
    }

    private float lastY;
    private void handleActionMove(MotionEvent event){
        if(mActionDownIndex == INVAILD_POSITION) {
            return;
        }
        float distanceY = event.getY() - lastY;
        int originTop = getMeasuredHeight() - (mChildCount - mActionDownIndex) * titleBarHeight;
        int marginTop = dip2px(mMarginTop);
        if(mTouchingCard.getY() + distanceY >= originTop || mTouchingCard.getY() + distanceY < marginTop) {
            distanceY = 0;
        }
//        dragView.setY(dragView.getY() + distanceY);
        mTouchingCard.offsetTopAndBottom((int) distanceY);
        lastY = event.getY();
    }


    private void handleActionUpOrCancel(MotionEvent ev) {
        int mTouchingCardTop = mTouchingCard.getTop();
        if(isOpen) {
            if(Math.abs(ev.getY() - mTouchCardOriginY) > 50 && mActionDownIndex != -1){
                currentIndex = mActionDownIndex;
                closeMenu();
            } else if(mActionDownIndex != -1){
                openMenu(mActionDownIndex);
            }
        } else {
            if(Math.abs(ev.getY() - mTouchCardOriginY) > 50 && mActionDownIndex != -1){
                openMenu(mActionDownIndex);
            } else if(mActionDownIndex != -1){
                currentIndex = mActionDownIndex;
                closeMenu();
            }
        }
        mTouchCardOriginY = 0;
        mActionDownIndex = -1;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean isConsume = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "dispatchTouchEvent: ACTION_DOWN");
                isConsume = handleActionDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleActionUpOrCancel(ev);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
//                Log.d(TAG, "onTouchEvent: ACTION_DOWN  isOpen->" + isOpen);
//                if(isOpen) {
//                    closeMenu();
//                }else{
//                    currentIndex = pointToPosition(event.getX(), event.getY());
//                    if(currentIndex == -1) {
//                        Log.d(TAG, "onTouchEvent: currentIndex == -1");
//                    } else {
//                        openMenu(currentIndex);
//                    }
//                }
                break;
        }
        return true;
    }

    private int pointToPosition(float x, float y){
        int childCount = mChildCount;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int left = child.getLeft();
            int right = child.getRight();
            int top = child.getTop();
            int titleBottom = child.getTop() + titleBarHeight;
            if(x > left && x < right && y > top && y < titleBottom) {
                Log.d(TAG, "onTouchEvent: currentIndex ->" + i);
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

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public int px2dip(float pxValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
