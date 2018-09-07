package com.mapleaf.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ShareTextView extends FrameLayout {

    private int lastX, lastY;
    private int parentWidth, parentHeight;
    private ShareEditText mEditText;

    public ShareTextView(Context context) {
        this(context, null);
    }

    public ShareTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShareTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.share_text);
        int size = array.getInteger(R.styleable.share_text_textSize, 0);
        String text = array.getString(R.styleable.share_text_text);
        array.recycle();

        mEditText = new ShareEditText(getContext());
        mEditText.setText(text);
        mEditText.setGravity(Gravity.CENTER);
        mEditText.setTextSize(size);
        mEditText.setTextColor(getContext().getResources().getColor(R.color.colorWhite));
        mEditText.setBackground(null);
        addView(mEditText);
    }

    public void setText(String text) {
        mEditText.setText(text);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastX = (int) ev.getRawX();
            lastY = (int) ev.getRawY();
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) event.getRawX() - lastX;
                int deltaY = (int) event.getRawY() - lastY;

                if (parentWidth == 0) {
                    ViewGroup mViewGroup = (ViewGroup) getParent();
                    parentWidth = mViewGroup.getWidth();
                    parentHeight = mViewGroup.getHeight();
                }

                if (getTranslationX() < -getLeft() && deltaX < 0) deltaX = 0;
                else if (getTranslationX() > (parentWidth - getRight()) && deltaX > 0) deltaX = 0;
                if (getTranslationY() < -getTop() && deltaY < 0) deltaY = 0;
                else if (getTranslationY() > (parentHeight - getBottom()) && deltaY > 0) deltaY = 0;

                setTranslationX(getTranslationX() + deltaX);
                setTranslationY(getTranslationY() + deltaY);

                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private static class ShareEditText extends android.support.v7.widget.AppCompatEditText {

        private int downX, downY;

        public ShareEditText(Context context) {
            super(context);
        }

        public ShareEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ShareEditText(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = (int) event.getRawX();
                    downY = (int) event.getRawY();
                    getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int upX = (int) event.getRawX() - downX;
                    int upY = (int) event.getRawY() - downY;
                    if (Math.abs(upX) <= ViewConfiguration.get(getContext()).getScaledTouchSlop() && Math.abs(upY) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    break;
            }
            return super.dispatchTouchEvent(event);
        }

        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            super.onSelectionChanged(selStart, selEnd);
            //保证光标始终在最后面
            if (selStart == selEnd) {//防止不能多选
                setSelection(getText().length());
            }
        }
    }
}
