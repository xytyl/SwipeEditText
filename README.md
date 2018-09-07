---
title: 一个可以随手势拖动的EditText，点击更改内容，附带解决软键盘遮挡终极方法
grammar_cjkRuby: true
---

CSDN传送门：https://blog.csdn.net/bjyanxin/article/details/82496182



最近产品同学的需求，要求定制一个可拖拽可编辑的文本，原本觉得还挺简单，不就是写个EditText处理一下touch事件么，后来做了发现还有些小坑，记录一下，顺便给大家做个参考

### 试错

首先我尝试自定义一个EditText，重写onTouchEvent（）方法，在其中做随手势的操作，代码比较简单就不贴了，但是重写之后点击无法唤出软键盘，看了一下在TextView的源码中有关于软键盘的部分，尝试了一下自行唤醒未果，由于开发时间紧张所以没有深入研究，后期会补上，于是换了另一种方式。

### 实现方法

自定义一个FrameLayout，在里面放一个EditText，触摸事件交给外层FrameLayout处理，这里采用内部拦截法。

首先是父View的onInterceptTouchEvent（）方法，用来拦截事件：
```java?linenums
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastX = (int) ev.getRawX();
            lastY = (int) ev.getRawY();
            return false;
        }
        return true;
    }
```

然后是EditText的dispatchTouchEvent（）方法，内部判断是否需要请求父View不要拦截：
```java?linenums
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
```

实现了以上两步，就可以点击EditText正常编辑内容，而拖拽触摸操作会交给父View处理：
```java?linenums
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

				//判断边界，不能拖拽出范围外
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
```

到这里，基本功能就已经实现了，可以正常拖拽以及编辑，后面对一些细节进行处理。

**点击EditText光标始终在最后面**

重写EditText的onSelectionChanged（）方法即可：
```java?linenums
        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            super.onSelectionChanged(selStart, selEnd);
            if (selStart == selEnd) {//防止不能多选
                setSelection(getText().length());
            }
        }
```

**点击EditText以外的内容令软键盘隐藏，并且令光标消失**：

在父View的xml中设置`android:focusableInTouchMode="true"`，并重写onTouchEvent（）方法，令软键盘隐藏，最后再清除掉EditText的焦点：

```java?linenums
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
```

**解决软键盘遮挡问题**：

重头戏来了，因为我们项目用的沉浸式，因此设置adjustPan也会有遮挡问题，那么既然系统处理不好，我就自己处理，思路和adjustPan差不多，也是**唤出软键盘时将View向上平移**。

首先监测监听最外层布局控件来检测软键盘是否弹出，接着比对可用空间与EditText底部到父View的距离差，如果可用空间不够说明遮挡了，这时候让父控件向上平移相应距离即可：

```java?linenums
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
```

另外，如果有多个EditText也没问题，可以通过`getFocusedChild()`来获取当前EditText，从而算出应该平移的距离。