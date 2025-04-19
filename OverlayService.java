package com.example.batteryoverlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.TextView;
import com.example.batteryoverlay.R;

public class OverlayService extends Service {
    private static final String CHANNEL_ID = "BatteryOverlayChannel";
    private static final int NOTIFICATION_ID = 1;

    private OverlayView overlayView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private boolean isOverlayAdded = false;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                removeOverlayIfNeeded();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                initOverlay();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenReceiver, filter);

        initOverlay();
        registerBatteryReceiver();
    }

    private void removeOverlayIfNeeded() {
        if (isOverlayAdded && overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
                isOverlayAdded = false;
            } catch (IllegalArgumentException e) {
                // 视图可能已经被移除
            }
        }
    }

    private void initOverlay() {
        if (isOverlayAdded) {
            return;
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = new OverlayView(this);

        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

        params.x = 1;
        params.y = 1;

        if (windowManager != null) {
            try {
                windowManager.addView(overlayView, params);
                isOverlayAdded = true;
                overlayView.setWindowManagerLayoutParams(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerBatteryReceiver() {
        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                if (overlayView != null) {
                    TextView tv = overlayView.findViewById(R.id.battery_percent);
                    if (tv != null) {
                        tv.setText(level + "%");
                    }
                }
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeOverlayIfNeeded();
        try {
            unregisterReceiver(screenReceiver);
        } catch (IllegalArgumentException e) {
            // 接收器可能未被注册
        }
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
