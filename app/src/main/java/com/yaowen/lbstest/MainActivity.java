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
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int SHOW_LOCATION = 0;
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

    private void showLocation(final Location location) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 组装反向地理编码的接⼝地址
                StringBuilder url = new StringBuilder();
                url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
                url.append(location.getLatitude()).append(",");
                url.append(location.getLongitude());
                url.append("&sensor=false");
                HttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url.toString());
                // 在请求消息头中指定语⾔，保证服务器会返回中⽂数据
                httpGet.addHeader("Accept-Language", "zh-CN");
                try {
                    HttpResponse httpResponse = client.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        String response = EntityUtils.toString(httpEntity,
                                "utf-8");
                        JSONObject jsonObject = new JSONObject(response);
// 获取results节点下的位置信息
                        JSONArray resultArray = jsonObject.getJSONArray
                                ("results");
                        if (resultArray.length() > 0) {
                            JSONObject subObject = resultArray.
                                    getJSONObject(0);
// 取出格式化后的位置信息
                            String address = subObject.getString
                                    ("formatted_address");
                            Message message = new Message();
                            message.what = SHOW_LOCATION;
                            message.obj = address;
                            handler.sendMessage(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_LOCATION:
                    String currentPosition = (String) msg.obj;
                    locationText.setText(currentPosition);
                    break;
                default:
                    break;
            }
        }
    };
}
