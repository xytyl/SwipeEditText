package com.mapleaf.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends AppCompatActivity {

    ShareTextView mTvContent;
    ShareTextView mTvAuthor;
    ConstraintLayout mClMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {
        mTvContent = findViewById(R.id.tv_content);
        mTvAuthor = findViewById(R.id.tv_author);
        mClMain = findViewById(R.id.cl_main);

        mClMain.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            mClMain.getWindowVisibleDisplayFrame(rect);
            int availableHeight = rect.bottom - mClMain.getTop();
            View focusedChild = mClMain.getFocusedChild();
            float contentHeight = 0;
            if (focusedChild != null) {
                contentHeight = focusedChild.getY() + focusedChild.getHeight();
            }
            int deltaY = (int) (availableHeight - contentHeight);

            if (deltaY <0) {
                mClMain.setTranslationY(deltaY);
            } else {
                mClMain.setTranslationY(0);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null) {
                if (getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mTvContent.clearFocus();
                    mTvAuthor.clearFocus();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    protected void setFullScreen() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}