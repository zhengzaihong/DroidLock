package com.zzh.droidlock;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class WifiCleaner {


    private static final String TAG = "WifiCleaner";

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public static void clearAllConfiguredNetworks(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.e(TAG, "WifiManager is null");
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

        if (configuredNetworks == null || configuredNetworks.isEmpty()) {
            Log.i(TAG, "No configured networks found.");
            return;
        }

        for (WifiConfiguration config : configuredNetworks) {
            int netId = config.networkId;
            boolean removed = wifiManager.removeNetwork(netId);
            Log.i(TAG, "Removing network SSID: " + config.SSID + " | networkId: " + netId + " | success: " + removed);
        }

        boolean saved = wifiManager.saveConfiguration(); // 对于 Android 9 及以下可选
        Log.i(TAG, "saveConfiguration result: " + saved);
    }
}
