package com.yaowen.lbstest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private TextView locationText;
    private String provider;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationText = (TextView) findViewById(R.id.locationText);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 获取所有可用的位置提供器
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if
                (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            // 当没有可用的位置提供器时，弹出Toast提⽰用户
            Toast.makeText(this, "当前设备没有开启定位模块或者该设备不支持定位！",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            // 显⽰当前设备的位置信息
            showLocation(location);
        }

        locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            // 关闭程序时将监听器移除
            locationManager.removeUpdates(locationListener);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle
                extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            // 更新当前设备的位置信息
            showLocation(location);
        }
    };

    private void showLocation(Location location) {
        String currentPosition = "维度： " + location.getLatitude()
                + "\n"
                + "经度：" + location.getLongitude();
        locationText.setText(currentPosition);
    }
}
