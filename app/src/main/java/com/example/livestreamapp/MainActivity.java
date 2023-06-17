package com.example.livestreamapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;
import java.io.File;
import android.content.Intent;
import android.os.PowerManager;
import android.net.Uri;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity implements ConnectCheckerRtsp, SurfaceHolder.Callback {
    //Set up live stream
    public static String URL_LIVE_STREAM = "";
    public static final String PREFIX_FILE_PATH = "/demo_live_stream";
    public static final int RETRY_COUNT = 10;

    //Camera
    private static RtspCamera1 rtspCamera;

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
            //Run in background
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

    private JobScheduler jobScheduler;
    private static final int JOB_ID = 1;

    /** Automatically check internet connection init **/
    private static final int JOB_INTERVAL = 5000; // 20 seconds
    private Handler handler;
    private Runnable jobRunnable;

    /** Set up for black mode **/
    private View black_view;
    private int touchCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        // Initialize the JobScheduler
        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        // Initialize the Handler and Runnable
        handler = new Handler();
        jobRunnable = new Runnable() {
            @Override
            public void run() {
                // Schedule the job every 5 seconds
                // Schedule the job to check in StreamJobService
                JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, new ComponentName(MainActivity.this, StreamJobService.class));
                jobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
                jobBuilder.setPersisted(true);
                jobScheduler.schedule(jobBuilder.build());
                Log.d("Tag", "Hello Word");
                handler.postDelayed(this, JOB_INTERVAL);
            }
        };

        handler.postDelayed(jobRunnable, JOB_INTERVAL);

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
        Button btn_black = findViewById(R.id.black_mode_btn);

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
                }
                else {
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

        //Switch camera fucntion
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

        //Reconect function
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtspCamera.reConnect(5000);
            }
        });

        //Black mode
        black_view = findViewById(R.id.black_view);
        btn_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBlackScreen();
                touchCount =0;
            }
        });

        black_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                touchCount++;
                if (touchCount == 5) {
                    toggleBlackScreen();
                    return true; // Consume the touch event
                }
                return false;
            }
        });
    }

    //Start stream function
    public static void startStream() {
        if(!rtspCamera.isStreaming()){
            if (rtspCamera.prepareAudio() && rtspCamera.prepareVideo()) {
                rtspCamera.startStream(URL_LIVE_STREAM);
            }
        }
        else {
            String status = String.valueOf(rtspCamera.isStreaming());
            Log.d("msg", status);
        }
    }

    public static void stopStream() {
        rtspCamera.stopStream();
    }

    public void toggleBlackScreen() {
        if(black_view.getVisibility() == View.VISIBLE) {
            black_view.setVisibility(View.GONE);
        } else {
            black_view.setVisibility(View.VISIBLE);
        }
    }

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
        rtspCamera.startPreview();
        rtspCamera.switchCamera();
        rtspCamera.switchCamera();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void onNewBitrateRtsp(long l) {

    }

}