package com.example.collect_my_car

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.collect_my_car.Model.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*


private const val PERMISSION_REQUEST = 10

class HomeActivity: AppCompatActivity(), SensorEventListener {

    companion object{

        val MESSGAE1 = "Message1"
        val MESSGAE2 = "Message2"
    }

    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var image_avatar : ImageView

    lateinit var gpsNetwork: TextView
    lateinit var gpsNetworkLon: TextView
    lateinit var gpsNetworkLat: TextView

    lateinit var gps: TextView
    lateinit var gpsLon: TextView
    lateinit var gpsLat: TextView




    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    private var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

    var sensor:Sensor?=null
    var sensorManager:SensorManager?=null

    var xold = 0.0
    var yold = 0.0
    var zold = 0.0

    var threadShould = 3000.0

    var oldTime:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        toggle = ActionBarDrawerToggle(this, drawerLayoutHome, R.string.open, R.string.closed)

        drawerLayoutHome.addDrawerListener(toggle)

        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navViewHome.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.mItem1 -> {

                    intent = Intent(this, MapsActivity::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Maps", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem2 -> {

                    intent = Intent(this, NavigationDrawerDriver::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Navigation Activity", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem3 -> {

                    intent = Intent(this, HomeActivity::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Sensor Activity", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem4 -> {

                    intent = Intent(this, GraphActivity::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Graph Activity", Toast.LENGTH_SHORT).show()
                }
                R.id.mItem5 -> {

                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("Logout")
                        .setMessage("Do you really want to Logout?")
                        .setNegativeButton("Cancel", {dialogInterface, _ -> dialogInterface.dismiss()})

                        .setPositiveButton("Logout") { dialogInterface, _ ->

                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(this, LoginActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()

                        }.setCancelable(false)

                    val dialog = builder.create()
                    dialog.setOnShowListener {

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.red))
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.blueInk))

                    }

                    dialog.show()
                }



            }

            true

        }

        val headerView = navViewHome.getHeaderView(0)
        val navName = headerView.findViewById<android.view.View>(R.id.nav_name) as TextView
        val navPhone = headerView.findViewById<android.view.View>(R.id.nav_phone) as TextView
        image_avatar = headerView.findViewById<android.view.View>(R.id.nav_imageView) as ImageView

        navName.setText(Common.buildNavMessage())
        navPhone.setText(Common.currentUser!!.phone)

        if(Common.currentUser != null && Common.currentUser!!.image != null){

            Glide.with(this).load(Common.currentUser!!.image).into(image_avatar)

        }

        val navBarTitle = intent.getStringExtra(LoginActivity.MESSGAE)
        supportActionBar?.title = navBarTitle

        gpsNetwork = findViewById(R.id.gpsNetwork);
        gpsNetworkLon = findViewById(R.id.gpsNetworkLon);
        gpsNetworkLat = findViewById(R.id.gpsNetworkLat);

        gps = findViewById(R.id.gps);
        gpsLon = findViewById(R.id.gpsLon);
        gpsLat = findViewById(R.id.gpsLat);

        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        /*sensor=sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)*/
        sensorManager!!.registerListener(this,
                sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)

        openMapsButton.setOnClickListener {

            openMaps()

        }

        graphButton.setOnClickListener {

            graphOpen()

        }

        disableView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                enableView()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            enableView()
        }




    }


    private fun disableView() {
        goggleMapsButton.isEnabled = false
        goggleMapsButton.alpha = 0.5F
    }

    private fun enableView() {
        goggleMapsButton.isEnabled = true
        goggleMapsButton.alpha = 1F
        goggleMapsButton.setOnClickListener { getLocation()}
        Toast.makeText(this, "GPS Location Successful", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            if (hasGps) {
                Log.d("HasGPS", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (location != null) {
                            locationGps = location
                            gps.text = ("\nGPS Test 1")
                            gpsLat.text = ("\nLatitude : " + locationGps!!.latitude)
                            gpsLon.text = ("\nLongitude : " + locationGps!!.longitude)
                            Log.d("GPS Latitude", " GPS Latitude : " + locationGps!!.latitude)
                            Log.d("GPS Longitude", " GPS Longitude : " + locationGps!!.longitude)
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String) {

                    }

                    override fun onProviderDisabled(provider: String) {

                    }

                })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation
            }
            if (hasNetwork) {
                Log.d("HasGPS", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (location != null) {
                            locationNetwork = location
                            gpsNetwork.text=("\nNetwork Test 1 ")
                           // gpsResult.append("\nLatitude : " + locationNetwork!!.latitude)
                            gpsNetworkLat.text=("\nLatitude : " + locationNetwork!!.latitude)
                            gpsNetworkLon.text=("\nLongitude : " + locationNetwork!!.longitude)
                            Log.d("Network Latitude", " Network Latitude : " + locationNetwork!!.latitude)
                            Log.d("Network Longitude", " Network Longitude : " + locationNetwork!!.longitude)
                        }
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                    }

                    override
                    fun onProviderEnabled(provider: String) {

                    }

                    override fun onProviderDisabled(provider: String) {

                    }

                })

                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

            if(locationGps!= null && locationNetwork!= null){
                if(locationGps!!.accuracy > locationNetwork!!.accuracy){
                    gpsNetwork.text=("\nNetwork Test 2 ")
                    // gpsResult.append("\nLatitude : " + locationNetwork!!.latitude)
                    gpsNetworkLat.text=("\nLatitude : " + locationNetwork!!.latitude)
                    gpsNetworkLon.text=("\nLongitude : " + locationNetwork!!.longitude)
                    Log.d("Network Latitude", " Network Latitude : " + locationNetwork!!.latitude)
                    Log.d("Network Longitude ", " Network Longitude : " + locationNetwork!!.longitude)
                }else{
                    gps.text = ("\nGPS Test 2")
                    gpsLat.text = ("\nLatitude : " + locationGps!!.latitude)
                    gpsLon.text = ("\nLongitude : " + locationGps!!.longitude)
                    Log.d("Location Latitude", " GPS Latitude : " + locationGps!!.latitude)
                    Log.d("Location Longitude", " GPS Longitude : " + locationGps!!.longitude)
                }
            }

        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(this, "GPS Permission Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "ERROR - Please Enable GPS Permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                enableView()
        }
    }





    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }



    override fun onSensorChanged(event: SensorEvent?) {

        var x = event!!.values[0]
        var y = event.values[1]
        var z = event.values[2]

        accelerometer_data. text = "X = ${event!!.values[0]}\n\n" +
                "Y = ${event.values[1]}\n\n" +
                "Z = ${event.values[2]}"

        var currentTime = System.currentTimeMillis()

        if((currentTime - oldTime) > 100){

            var timeDiff = currentTime - oldTime
            oldTime = currentTime
            var speed= Math.abs(x+y+z-xold-yold-zold)/timeDiff*10000

            if(speed > threadShould){

                var v=getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(500)
                Toast.makeText(this, "Vibrate Shake", Toast.LENGTH_LONG).show()

            }

            else if (x > 0.1){//10.0){

                var v=getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                //v.vibrate(500)
                //Toast.makeText(this, "Braking Hard -" + x.absoluteValue , Toast.LENGTH_LONG).show()

                val uid = FirebaseAuth.getInstance().uid ?: ""
                val ref = FirebaseDatabase.getInstance().getReference("/UserInfo/mapSensor/$uid").push()

                val user = save(x)

                ref.setValue(user)

/*                getLocation()

                var latitude = locationGps!!.latitude
                var longitude = locationGps!!.longitude





                val intent = Intent(this, MapsActivity::class.java)

                intent.putExtra(MESSGAE1, latitude)
                intent.putExtra(MESSGAE2, longitude)

                startActivity(intent)*/

            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){

            return true

        }



        return super.onOptionsItemSelected(item)
    }


    private fun googleMaps(){

        val intent = Intent(this, MapsActivity::class.java)

        startActivity(intent)

    }

    private fun openMaps(){

        getLocation()

        var latitude = locationGps!!.latitude
        var longitude = locationGps!!.longitude

        val intent = Intent(this, MapsActivity::class.java)

        intent.putExtra(MESSGAE1, latitude)
        intent.putExtra(MESSGAE2, longitude)

        startActivity(intent)


    }

    private fun graphOpen() {

        val intent = Intent(this, GraphActivity::class.java)
        startActivity(intent)
    }




    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    private class save(val sensor: Float) {

    }



}