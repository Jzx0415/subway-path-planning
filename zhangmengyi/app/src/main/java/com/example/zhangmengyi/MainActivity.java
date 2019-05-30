package com.example.zhangmengyi;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    private MapView mapView =null;
    public BaiduMap baiduMap;
    private BroadcastReceiver receiver;
    public LocationClient mLocationClient;
    private boolean isFirstLocate=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        mapView = (MapView) findViewById(R.id.bmapView);
        RadioGroup leixing = (RadioGroup) findViewById(R.id.leixing);
        baiduMap=mapView.getMap();//获取地图控制器
        registerSDKCheckReceiver();
        leixing.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.putong:baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);baiduMap.setTrafficEnabled(false);baiduMap.setBaiduHeatMapEnabled(false);break;
                    case R.id.weixing:baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);baiduMap.setTrafficEnabled(false);baiduMap.setBaiduHeatMapEnabled(false);break;
                    case R.id.kongbai:baiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);baiduMap.setTrafficEnabled(false);baiduMap.setBaiduHeatMapEnabled(false);break;
                    case R.id.shikuang:baiduMap.setTrafficEnabled(true);baiduMap.setBaiduHeatMapEnabled(false);break;
                    case R.id.reli:baiduMap.setBaiduHeatMapEnabled(true);baiduMap.setTrafficEnabled(false);break;
                }
            }
        });
        baiduMap.setMyLocationEnabled(false);
        //positionText= (TextView) findViewById(R.id.position_text_view);
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[]permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            requestLocation();
        }

    }
    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }
    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());//LatLng类用于存放经纬度
            // 第一个参数是纬度值，第二个参数是精度值。这里输入的是本地位置。
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);//将LatLng对象传入
            baiduMap.animateMapStatus(update);
            update= MapStatusUpdateFactory.zoomTo(16f);//百度地图缩放范围，限定在3-19之间，可以去小数点位值
            // 值越大，地图显示的信息越精细
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;//防止多次调用animateMapStatus()方法，以为将地图移动到我们当前位置只需在程序
            // 第一次定位的时候调用一次就可以了。
        }
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);//获取我们的当地位置
    }
    private void initLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);//表示每5秒更新一下当前位置
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        // Hight_Accuracy表示高精确度模式，会在GPS信号正常的情况下优先使用GPS定位，在无法接收GPS信号的时候使用网络定位。
        // Battery_Saving表示节电模式，只会使用网络进行定位。
        // Device_Sensors表示传感器模式，只会使用GPS进行定位。
        mLocationClient.setLocOption(option);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();//销毁之前，用stop()来停止定位
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        unregisterReceiver(receiver);
        baiduMap.setMyLocationEnabled(false);
    }
    private void registerSDKCheckReceiver(){
        receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                if (SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR.equals(action)){
                    Toast.makeText(getApplicationContext(),"网络错误",Toast.LENGTH_SHORT).show();
                }else if (SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR.equals(action)){
                    Toast.makeText(getApplicationContext(),"KEY验证失败",Toast.LENGTH_SHORT).show();
                }
            }
        };
        IntentFilter filter=new IntentFilter();
        //监听网络错误
        filter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        //监听百度地图SDK的key是否正确
        filter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        registerReceiver(receiver,filter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }//onRequestPermissionsResult()方法中，对权限申请结果进行逻辑判断。这里使用一个循环对每个权限进行判断，
        // 如果有任意一个权限被拒绝了，那么就会直接调用finish()方法关闭程序，只有当所有的权限被用户同意了，才会
        // 调用requestPermissions()方法开始地理位置定位。
    }
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }
        }
    }

}