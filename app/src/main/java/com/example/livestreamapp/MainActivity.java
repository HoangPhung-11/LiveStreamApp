package com.example.livestreamapp;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
//import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;
import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.os.PowerManager;
import android.net.Uri;
import android.provider.Settings;
import android.os.Build;


public class MainActivity extends AppCompatActivity implements ConnectCheckerRtsp, SurfaceHolder.Callback {
    //Set up live stream
    public static String URL_LIVE_STREAM = "";
    public static final String STREAM_NAME = "f73a79fd";
    public static final String PREFIX_FILE_PATH = "/demo_live_stream";
    public static final int RETRY_COUNT = 10;

    //Camera
    private RtspCamera1 rtspCamera;

    //Saving Destination
    File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat(PREFIX_FILE_PATH));
    private String currentMillis = "";

    //Permission List
    private String[] permissions = new String[]{
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.BLUETOOTH"
    };

    /**  Start of check permission functions **/

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //RUn in background
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
            //Others
            for (String permission : permissions) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, permission)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** End of check permission **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Take the url
        URL_LIVE_STREAM = getIntent().getStringExtra("URL_LIVE_STREAM");

        //Camera init
        SurfaceView cameraView = findViewById(R.id.cameraView);
        rtspCamera = new RtspCamera1(cameraView, this);
        rtspCamera.setReTries(RETRY_COUNT);
        cameraView.getHolder().addCallback(this);

        //Button innit
        Button btn_live = findViewById(R.id.btn_live);
        Button btn_switch_camera = findViewById(R.id.btn_switch_camera);
        Button btn_record = findViewById(R.id.btn_record);

        //Check Permission
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        //Live function
        btn_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rtspCamera.isStreaming()) {
                    btn_live.setText("start_live");
                    rtspCamera.stopStream();
                } else {
                    if (rtspCamera.isRecording() || (rtspCamera.prepareAudio() && rtspCamera.prepareVideo())) {
                        btn_live.setText("stop_live");
                        rtspCamera.startStream(URL_LIVE_STREAM);
                    } else {
                        Toast.makeText(
                                MainActivity.this, "Error preparing stream, This device can't do it",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            }
        });

        btn_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    rtspCamera.switchCamera();
                } catch (CameraOpenException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!rtspCamera.isRecording()) {
                    try {
                        if (!filePath.exists()) {
                        filePath.mkdir();
                        }
                        currentMillis = String.valueOf(System.currentTimeMillis());
                        if (!rtspCamera.isStreaming()) {
                            if (rtspCamera.prepareAudio() && rtspCamera.prepareVideo()) {
                                rtspCamera.startRecord(
                                filePath.getAbsolutePath()
                                + "/"
                                + currentMillis
                                + ".mp4"
                                );
                                btn_record.setText("stop_record_video");
                                Toast.makeText(MainActivity.this, "Recording... ", Toast.LENGTH_SHORT)
                                .show();
                            } else {
                                Toast.makeText(
                                MainActivity.this,
                                "Error preparing stream, This device can't do it",
                                Toast.LENGTH_SHORT
                                ).show();
                            }
                        } else {
                            rtspCamera.startRecord(
                            filePath.getAbsolutePath()
                                + "/"
                                + currentMillis
                                + ".mp4"
                            );
                            btn_record.setText("stop_record_video");
                            Toast.makeText(MainActivity.this, "Recording... ", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        rtspCamera.stopRecord();
                        btn_record.setText("start_record_video");
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    rtspCamera.stopRecord();
                    btn_record.setText("start_record_video");
                    Toast.makeText(
                    MainActivity.this, ("file "
                        + currentMillis
                        + ".mp4 saved in "
                        + filePath.getAbsolutePath()), Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });


        }
        /**
        @Override
        protected void onDestroy() {
        super.onDestroy();
        rtspCamera.stopStream();
        rtspCamera.stopRecord();
        rtspCamera.stopPreview();
        }
        **/
    @Override
    public void onConnectionSuccessRtsp() {

    }

    @Override
    public void onConnectionFailedRtsp(String reason) {

    }

    @Override
    public void onDisconnectRtsp() {

    }

    @Override
    public void onAuthErrorRtsp() {

    }

    @Override
    public void onAuthSuccessRtsp() {

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        rtspCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void onNewBitrateRtsp(long l) {

    }
}