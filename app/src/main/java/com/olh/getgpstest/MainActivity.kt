package com.olh.getgpstest

import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    // 定义一个请求代码常量，确保它是唯一的
    private val REQUEST_LOCATION_CODE = 1
    private lateinit var text2: TextView
    private lateinit var text_time : TextView

    private lateinit var  adapter : ArrayAdapter<String>
    private lateinit var  satellitesListView : ListView
    var satellitesItems = mutableListOf("",)


    var latitude = 0.0
    var longitude = 0.0
    var altitude = 0.0 // 添加海拔高度
    var accuracy = 0f // 添加位置精度
    var speed = 0f // 添加移动速度
    var bearing = 0f // 添加方向角度
    var time = System.currentTimeMillis() // 添加位置获取的时间戳
    var provider = "unknown" // 添加位置提供者信息，默认未知

    private lateinit var locationManager: LocationManager
    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateUIWithLocation(location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // 位置提供者状态变化
            Log.v("Location", "位置提供者状态变化: $status")
        }

        override fun onProviderEnabled(provider: String) {
            // 位置提供者被启用
            if (provider == LocationManager.GPS_PROVIDER) {
                // GPS提供者被启用，可以获取位置
                Log.v("Location", "GPS提供者被启用")

            }
        }

        override fun onProviderDisabled(provider: String) {
            // 位置提供者被禁用
            if (provider == LocationManager.GPS_PROVIDER) {
                // GPS提供者被禁用，提示用户或关闭功能
                text2.text = "GPS被禁用"
            }
        }
    }
    private val gnssStatusLocationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            val satelliteCount = status.satelliteCount
            var use_svid_count = 0
            var unuse_svid_count = 0
//            Log.d("SatelliteInfo", "Total satellites: $satelliteCount")
            satellitesItems.clear()
            for (i in 0 until satelliteCount) {
                satellitesItems.add("Satellite ${status.getSvid(i)}: ${status.getCn0DbHz(i)} dBHz")
                if (status.usedInFix(i)) {
                    // 卫星被用于定位
                    use_svid_count++
                    val snr = status.getCn0DbHz(i)
//                    Log.d("SatelliteInfo", "Satellite ${status.getSvid(i)} used in fix, SNR: $snr dBHz")
                } else {
                    // 卫星未被用于定位
                    unuse_svid_count++
//                    Log.d("SatelliteInfo", "Satellite ${status.getSvid(i)} not used in fix")
                }
            }


        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        text2 = findViewById(R.id.textView4)
        text_time = findViewById(R.id.text_time)
        val button: Button = findViewById(R.id.button)

        adapter = ArrayAdapter(this, R.layout.list_item, satellitesItems)
        satellitesListView = findViewById(R.id.list_satellite_info)
        satellitesListView.adapter = adapter
        satellitesListView.setOnItemClickListener { parent, view, position, id ->
            Toast.makeText(this, "Clicked on item: ${satellitesItems[position]}", Toast.LENGTH_SHORT).show()
        }


        button.setOnClickListener {
            if (!checkLocationPermission()) {
                // 提示用户开启位置服务
                text2.text = "请开启GPS"
            } else {
                adapter.notifyDataSetChanged()
                for (item in satellitesItems) {
                    Log.d("satellites", "satellitesItems: $satellitesItems")
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        Log.v("Location", "onResume ${checkLocationPermission()}")
        // 检查权限
        if (checkLocationPermission()) {
            // 注册LocationListener并请求位置更新
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0.0f,locationListener)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.0f, locationListener);
            gnssStatusLocationManager.registerGnssStatusCallback(gnssStatusCallback)
        }
    }
    private fun checkLocationPermission(): Boolean {
        // 检查位置权限是否被授予
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun updateUIWithLocation(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        altitude = location.altitude
        accuracy = location.accuracy
        speed = location.speed
        bearing = location.bearing
        time = location.time
        provider = location.provider.toString()

        updateUIWithTime(location.time)
        if (provider == LocationManager.GPS_PROVIDER) {
            // GPS提供者，更新UI
            text2.text = "纬度: $latitude\n经度: $longitude\n海拔: $altitude\n精度: $accuracy\n速度: $speed\n方向: $bearing\n时间: $time"
//            Log.v("Location", "纬度: $latitude, 经度: $longitude, 海拔: $altitude, 精度: $accuracy, 速度: $speed, 方向: $bearing, 时间: $time")
        }
    }

    private fun updateUIWithTime(timeInMillis: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.getDefault())
        val timeText = "时间\n${dateFormat.format(Date(timeInMillis))}"
        text_time.text = timeText
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 用Toast弹出提示消息
                } else {
                    // 权限被拒绝，提示用户或关闭功能
                    text2.text = "权限被拒绝"
                }
                return
            }
        }
    }


}