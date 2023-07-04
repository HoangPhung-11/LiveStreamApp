package com.example.livestreamapp;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class StreamJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        // Check if the device has an active internet connection
        String msg = String.valueOf(!isInternetConnected());
        if (isInternetConnected()) {
            startStream();
        }
        else if (isInternetConnected() != true) {
            stopStream();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Job stopped before completion, reschedule it
        return false;
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } else {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }

    private void startStream() {
        MainActivity.startStream();
    }

    private void stopStream() {
        MainActivity.stopStream();
    }
}
