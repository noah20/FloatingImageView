package com.foxcoders.floatingimageview.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.foxcoders.floatingimageview.R;
import com.foxcoders.floatingimageview.databinding.FloatingFooterLayoutBinding;
import com.foxcoders.floatingimageview.databinding.FloatingImageLayoutBinding;

public class FloatingImageViewService extends Service {
    private static final String TAG = "FloatingImageViewServic";
    public FloatingImageViewService() {
    }

    private static final String NOTIFICATION_CHANNEL = "floatingChannel";
    private static final int NOTIFICATION_ID = 908;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams,mFooterLayoutParams;
    private FloatingImageLayoutBinding mFloatingView;
    private FloatingFooterLayoutBinding mFloatingFooter;
    private int mScreenH,mScreenW;

    @Override
    public void onCreate() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenH = metrics.heightPixels;
        mScreenW = metrics.widthPixels;
        Log.d(TAG, "onCreate: noah screen H*W -> "+mScreenH+" X "+mScreenW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);
            mFooterLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, PixelFormat.TRANSPARENT);
        } else {
            mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG|
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, // FLAG_DISMISS_KEYGUARD to allow icon appear on lock screen
                    PixelFormat.TRANSPARENT);

            mFooterLayoutParams =  new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG |
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                    PixelFormat.TRANSPARENT);
        }
        mLayoutParams.gravity = Gravity.END | Gravity.CENTER;
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;
        mFooterLayoutParams.gravity = Gravity.BOTTOM;
        mFooterLayoutParams.x = 0;
        mFooterLayoutParams.y = 0;

        mFloatingView = FloatingImageLayoutBinding.inflate(LayoutInflater.from(getBaseContext()));
        mFloatingView.floatingIcon.setImageResource(R.drawable.image);
        mFloatingFooter = FloatingFooterLayoutBinding.inflate(LayoutInflater.from(getBaseContext()));

        // TextView show no problem but imageView must setImage Resource programmatically
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mFloatingView.floatingIcon.setOnClickListener(v-> Toast.makeText(this, "clicked..", Toast.LENGTH_SHORT).show());

        mFloatingView.floatingIcon.setOnTouchListener(new View.OnTouchListener() {
            int mUpdateX, mUpdateY;
            float mTouchX, mTouchY;
            long mLastClickTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "onTouch: noah touch down");
                        mUpdateX = mLayoutParams.x;
                        mUpdateY = mLayoutParams.y;
                        mTouchX = event.getRawX();
                        mTouchY = event.getRawY();
                        mWindowManager.addView(mFloatingFooter.getRoot(), mFooterLayoutParams);
                        mLastClickTime = System.currentTimeMillis();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch: noah touch move ");

                        mLayoutParams.x = (int) (mUpdateX -(event.getRawX() - mTouchX)); // ths equation change with gravity position
                        mLayoutParams.y = (int) (mUpdateY +(event.getRawY() - mTouchY)); // ths equation change with gravity position
                        mWindowManager.updateViewLayout(mFloatingView.getRoot(),mLayoutParams);

                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: noah touch up");
                        if((mScreenH - event.getRawY())< 200 )
                            stopSelf();
                        mWindowManager.removeView(mFloatingFooter.getRoot());

                        // when you override onTouchListener you must handle normal Click and Long click
                        // because those methods depend on onTouchListener you can do like this ..
                        if(System.currentTimeMillis()-mLastClickTime < 100){
                            Log.d(TAG, "onTouch: noah Normal Click"+v.performClick());
                           /* notes you must assign return value from performClick() to invoke method otherwise
                             method dose not work*/
                        }else {
                            Log.d(TAG, "onTouch: noah long Click");
                          /*  boolean b = v.performLongClick();
                             you can move this block to ACTION_MOVE where check if normal click do something
                             if long click change position or allow drag  ...as you want */
                        }
                        break;

                }
                return true;
            }
        });
        mWindowManager.addView(mFloatingView.getRoot(), mLayoutParams);

        startForeground(NOTIFICATION_ID, getNotification());

        return START_NOT_STICKY;
    }

    Notification getNotification() {
        return new NotificationCompat.Builder(getBaseContext(), NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Floating Image Service Is running.....")
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mFloatingView.getRoot());
        mFloatingFooter = null;
        mFooterLayoutParams = null;
        mFloatingView = null;
        mLayoutParams = null;
    }
}