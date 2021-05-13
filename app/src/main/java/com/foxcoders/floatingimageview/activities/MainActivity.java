package com.foxcoders.floatingimageview.activities;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.foxcoders.floatingimageview.R;
import com.foxcoders.floatingimageview.databinding.ActivityMainBinding;
import com.foxcoders.floatingimageview.service.FloatingImageViewService;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 463;
    private ActivityMainBinding mBind;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        // TODO require permission  android.permission.SYSTEM_ALERT_WINDOW

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(this)){
                mBind.tvPermission.setText("Permission Granted ...");
                startService(true); // Permission granted
                mBind.tvPermission.setBackgroundColor(Color.GREEN);
            }else {
                mBind.tvPermission.setBackgroundColor(Color.RED);
                mBind.tvPermission.setText("Permission Required Click to Grant");
                mBind.tvPermission.setOnClickListener(v-> showRequirePermissionDialog() );
                showRequirePermissionDialog();
                // Permission denied take ser to app setting to grant permission
            }
        }else {
            startService(true);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void showRequirePermissionDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Permission Required.!")
                .setMessage("Permission SYSTEM_ALERT_WINDOW required to start show floating icon.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    Intent perIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    perIntent.setData(Uri.parse("package:"+getPackageName()));
                    startActivityForResult(perIntent, PERMISSION_REQUEST_CODE);
                    dialog.dismiss();
                }).show();
    }
    void startService(boolean isStart){
        if(isStart){
            if(isServiceRunning(FloatingImageViewService.class))
                return; // to avoid crash check if service running or not to avoid add view twice

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, FloatingImageViewService.class));
            }else {
                startService(new Intent(this, FloatingImageViewService.class));
            }
        }else
            stopService(new Intent(this, FloatingImageViewService.class));

    }

    boolean isServiceRunning(Class<?> serviceClass){

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> mRunningServices = manager.getRunningServices(Integer.MAX_VALUE);
        for(ActivityManager.RunningServiceInfo appService : mRunningServices){
            if(serviceClass.getName().equals(appService.service.getClassName()))
                return true;
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PERMISSION_REQUEST_CODE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(Settings.canDrawOverlays(this)){
                    startService(true);
                    mBind.tvPermission.setText("Permission Granted ...");
                    startService(true); // Permission granted
                    mBind.tvPermission.setBackgroundColor(Color.GREEN);
                }

            }
        }
    }
}