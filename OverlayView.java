package com.example.batteryoverlay;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.example.batteryoverlay.OverlayView;

public class OverlayView extends LinearLayout {
    private float initialX, initialY;
    private float initialTouchX, initialTouchY;
    private int snapThreshold;
    private int snapMargin;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    public OverlayView(Context context) {
        super(context);
        init(context);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.overlay, this);

        snapThreshold = getResources().getInteger(R.integer.snap_threshold);
        snapMargin = getResources().getInteger(R.integer.snap_margin);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // 确保视图可以接收触摸事件
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

        // 设置触摸监听
        setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return handleTouchEvent(event);
                }
            });

        // 在init()方法中添加：
        View dragHandle = findViewById(R.id.drag_handle);
        if (dragHandle != null) {
            dragHandle.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return handleTouchEvent(event);
                    }
                });
        }


    }

    public void setWindowManagerLayoutParams(WindowManager.LayoutParams params) {
        this.params = params;
    }

    private boolean handleTouchEvent(MotionEvent event) {
        if (params == null || windowManager == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                params.x = (int)(initialX + (event.getRawX() - initialTouchX));
                params.y = (int)(initialY + (event.getRawY() - initialTouchY));
                windowManager.updateViewLayout(this, params);
                return true;

            case MotionEvent.ACTION_UP:
                snapToEdge();
                return true;
        }
        return false;
    }

    private void snapToEdge() {
        if (params == null || windowManager == null) {
            return;
        }
            // 直接更新视图位置（移除动画部分）
            windowManager.updateViewLayout(OverlayView.this, params);
        }
    
    
}
