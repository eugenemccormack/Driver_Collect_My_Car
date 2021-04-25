package com.example.collect_my_car

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.example.collect_my_car.Model.Common
import com.example.collect_my_car.Model.EventBus.DriverRequestReceived
import com.example.collect_my_car.Model.EventBus.NotifyUserEvent
import com.example.collect_my_car.Model.TripPlanModel
import com.example.collect_my_car.Model.UserModel
import com.example.collect_my_car.Remote.IGoogleAPI
import com.example.collect_my_car.Remote.RetroFitClient
import com.example.collect_my_car.Utils.UserUtils
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.kusu.library.LoadingButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_graph.*
import kotlinx.android.synthetic.main.activity_maps.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.math.absoluteValue

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener { //AppCompatActivity(), OnMapReadyCallback, LocationListener, SensorEventListener {

    companion object{

        val MESSGAE = "Message"
        val MESSGAE2 = "Message2"
    }

    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var image_avatar : ImageView

    private var cityName: String = ""

    private lateinit var mMap: GoogleMap

    private lateinit var mapFragment: SupportMapFragment

    private val LOCATION_REQUEST_CODE = 101

    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2

    var sensor:Sensor?=null
    var sensorManager: SensorManager?=null

    var xold = 0.0
    var yold = 0.0
    var zold = 0.0

    var threadShould = 3000.0

    var oldTime:Long = 0

    //Routes

    private val compositeDisposable = CompositeDisposable()

    private lateinit var iGoogleAPI: IGoogleAPI

    private var blackPolyline: Polyline?= null
    private var greyPolyline: Polyline?= null
    private var polylineOptions: PolylineOptions?= null
    private var blackPolylineOptions: PolylineOptions?= null
    private var polylineList: ArrayList<LatLng?>?= null

    //Views

    private lateinit var chip_decline: Chip
    private lateinit var layout_accept: CardView
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var txt_estimate_time: TextView
    private lateinit var txt_estimate_distance: TextView

    private lateinit var txt_rating: TextView
    private lateinit var txt_type: TextView
    private lateinit var image_round: ImageView
    private lateinit var start_journey: CardView
    private lateinit var txt_user_name: TextView
    private lateinit var txt_start_estimate_distance: TextView
    private lateinit var txt_start_estimate_time: TextView
    private lateinit var image_phone: ImageView
    private lateinit var start_journey_button: LoadingButton
    private lateinit var complete_journey_button: LoadingButton
    private lateinit var take_Pictures: LoadingButton
    private lateinit var complete_Pictures: LoadingButton
    private lateinit var hard_braking: LoadingButton

    private lateinit var  layout_notify_user: LinearLayout
    private lateinit var txt_notify_user: TextView
    private lateinit var progress_notify: ProgressBar

    private var pickupGeoFire: GeoFire ?= null
    private var pickupGeoQuery: GeoQuery ?= null

    private var destinationGeoFire: GeoFire ?= null
    private var destinationGeoQuery: GeoQuery ?= null

    private val pickupGeoQueryListener = object : GeoQueryEventListener{
        override fun onKeyEntered(key: String?, location: GeoLocation?) {

            Log.d("MapActivity", "onKeyEntered")

            take_Pictures.isEnabled = true

            //Before Camera Function Added //start_journey_button.isEnabled = true

            UserUtils.sendNotifyToUser(this@MapsActivity, root_layout, key)

            if(pickupGeoQuery != null){

                //Remove
                pickupGeoFire!!.removeLocation(key)
                pickupGeoFire = null
                pickupGeoQuery!!.removeAllListeners()

            }

        }

        override fun onKeyExited(key: String?) {

                take_Pictures.isEnabled = false

            //Before Camera Function Added //start_journey_button.isEnabled = false

        }

        override fun onKeyMoved(key: String?, location: GeoLocation?) {



        }

        override fun onGeoQueryReady() {



        }

        override fun onGeoQueryError(error: DatabaseError?) {



        }


    }

    private val destinationGeoQueryListener = object : GeoQueryEventListener{
        override fun onKeyEntered(key: String?, location: GeoLocation?) {

            Toast.makeText(this@MapsActivity, "Destination Entered", Toast.LENGTH_SHORT).show()

            complete_Pictures.isEnabled = true

            //Before Complete Photos Added //complete_journey_button.isEnabled = true

            if(destinationGeoQuery != null){

                destinationGeoFire!!.removeLocation(key)
                destinationGeoFire = null
                destinationGeoQuery!!.removeAllListeners()

            }

        }

        override fun onKeyExited(key: String?) {

        }

        override fun onKeyMoved(key: String?, location: GeoLocation?) {

        }

        override fun onGeoQueryReady() {

        }

        override fun onGeoQueryError(error: DatabaseError?) {

        }


    }

    private var waiting_timer: CountDownTimer ?= null

    private var isTripStart = false
    private var onlineSystemAlreadyRegister = false

    private var tripNumberId: String ?= ""



    private lateinit var database: FirebaseDatabase
    private lateinit var driverInfoRef: DatabaseReference

    //Location

    private var locationRequest: LocationRequest ?= null
    private var locationCallback: LocationCallback ?= null
    private var fusedLocationProviderClient: FusedLocationProviderClient ?= null


    //Online System

    private lateinit var onlineRef: DatabaseReference
    private var currentUserRef: DatabaseReference? = null
    private lateinit var driversLocationRef: DatabaseReference
    private lateinit var geofire: GeoFire

    //Decline Collection

    private var driverRequestReceived : DriverRequestReceived ?= null
    private var countDownEvent : Disposable ?= null

    private val onlineValueEventListener = object:ValueEventListener{
        override fun onDataChange(p0: DataSnapshot) {

           if(p0.exists() && currentUserRef !=null)

               currentUserRef!!.onDisconnect().removeValue()
        }

        override fun onCancelled(p0: DatabaseError) {

            Toast.makeText(this@MapsActivity, p0.message, Toast.LENGTH_SHORT).show()
            //Snackbar.make(mapFragment.requireView(), p0.message,Snackbar.LENGTH_LONG).show()

        }


    }

    override fun onStart() {

        super.onStart()

        if (!EventBus.getDefault().isRegistered(this))

        EventBus.getDefault().register(this)


    }


    override fun onDestroy() {

        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)

        geofire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueEventListener)

        compositeDisposable.clear()

        onlineSystemAlreadyRegister = false

        if (EventBus.getDefault().hasSubscriberForEvent(MapsActivity::class.java))

            EventBus.getDefault().removeStickyEvent(MapsActivity::class.java)

        if (EventBus.getDefault().hasSubscriberForEvent(NotifyUserEvent::class.java))

            EventBus.getDefault().removeStickyEvent(NotifyUserEvent::class.java)

        EventBus.getDefault().unregister(this)

        super.onDestroy()
    }

    override fun onResume() {

        super.onResume()
        registerOnlineSystem()
    }

    private fun registerOnlineSystem() {

        if (!onlineSystemAlreadyRegister) {

            onlineRef.addValueEventListener(onlineValueEventListener)
            onlineSystemAlreadyRegister = true

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         setContentView(R.layout.activity_maps)



/*        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/

/*        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        *//*sensor=sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)*//*
        sensorManager!!.registerListener(this,
                sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)*/

        toggle = ActionBarDrawerToggle(this, drawerLayoutMaps, R.string.open, R.string.closed)

        drawerLayoutMaps.addDrawerListener(toggle)

        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navViewMaps.setNavigationItemSelectedListener {

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

        val headerView = navViewMaps.getHeaderView(0)
        val navName = headerView.findViewById<android.view.View>(R.id.nav_name) as TextView
        val navPhone = headerView.findViewById<android.view.View>(R.id.nav_phone) as TextView
        image_avatar = headerView.findViewById<android.view.View>(R.id.nav_imageView) as ImageView

        navName.setText(Common.buildNavMessage())
        navPhone.setText(Common.currentUser!!.phone)

        if(Common.currentUser != null && Common.currentUser!!.image != null){

            Glide.with(this).load(Common.currentUser!!.image).into(image_avatar)

        }




/*        locationButton.setOnClickListener {
            getLocation()
        }*/

        initViews()//(root)

        init()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun initViews() {

        chip_decline = findViewById(R.id.decline_chip) as Chip
        layout_accept = findViewById(R.id.accept_layout) as CardView
        circularProgressBar = findViewById(R.id.progress_bar) as CircularProgressBar
        txt_estimate_distance = findViewById(R.id.txt_estimate_distance) as TextView
        txt_estimate_time = findViewById(R.id.txt_estimate_time) as TextView

        txt_rating  = findViewById(R.id.txt_rating) as TextView
        txt_type = findViewById(R.id.txt_type) as TextView
        image_round = findViewById(R.id.image_round) as ImageView
        start_journey = findViewById(R.id.start_journey) as CardView
        txt_user_name= findViewById(R.id.txt_user_name) as TextView
        txt_start_estimate_distance= findViewById(R.id.txt_start_estimate_distance) as TextView
        txt_start_estimate_time= findViewById(R.id.txt_start_estimate_time) as TextView
        image_phone = findViewById(R.id.image_phone) as ImageView
        start_journey_button = findViewById(R.id.start_journey_button) as LoadingButton
        complete_journey_button = findViewById(R.id.complete_journey_button) as LoadingButton
        take_Pictures = findViewById(R.id.take_Pictures) as LoadingButton
        complete_Pictures = findViewById(R.id.complete_Pictures) as LoadingButton
        hard_braking = findViewById(R.id.hard_braking) as LoadingButton


        layout_notify_user = findViewById(R.id.layout_notify_user) as LinearLayout
        txt_notify_user = findViewById(R.id.txt_notify_user) as TextView
        progress_notify = findViewById(R.id.progress_notify) as ProgressBar

        //Decline Event

        chip_decline.setOnClickListener {

            if(driverRequestReceived != null){

                if(TextUtils.isEmpty(tripNumberId)){

                    if (countDownEvent != null)

                        countDownEvent!!.dispose()

                    chip_decline.visibility = View.GONE
                    layout_accept.visibility = View.GONE

                    mMap.clear()

                    circularProgressBar.progress = 0f

                    UserUtils.sendDeclineRequest(this, root_layout, driverRequestReceived!!.key!!)

                    driverRequestReceived = null

                    //this@RequestDriverActivity,
                    //        main_layout,
                    //        foundDriver,
                    //        target)

                }

                else{

                    if (ActivityCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(this@MapsActivity, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()


                        return@setOnClickListener
                    }
                    fusedLocationProviderClient!!.lastLocation
                            .addOnFailureListener { e->

                                Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                            }
                            .addOnSuccessListener { location ->

                                chip_decline.visibility = View.GONE
                                layout_accept.visibility = View.GONE
                                start_journey.visibility = View.GONE

                                mMap.clear()

                                UserUtils.sendDeclineRemoveTripRequest(this, root_layout, driverRequestReceived!!.key!!, tripNumberId)

                                //Set Trip ID to Empty After Removing it
                                tripNumberId = ""
                                driverRequestReceived = null

                                makeDriverOnline(location)

                            }


                }




            }

        }

        take_Pictures.setOnClickListener {


            val cameraMode = "collectionPhotos"

            val intent = Intent(this, CameraActivity::class.java)

            intent.putExtra(MESSGAE, cameraMode)
            intent.putExtra(MESSGAE2, tripNumberId)

            startActivity(intent)
            /*intent = Intent(this, CameraActivity::class.java)

            startActivity(intent)*/

            take_Pictures.visibility = View.GONE

            start_journey_button.visibility = View.VISIBLE

            start_journey_button.isEnabled = true





        }

        start_journey_button.setOnClickListener{

            if(blackPolyline != null){

                blackPolyline!!.remove()

            }

            if(greyPolyline != null){

                greyPolyline!!.remove()

            }

            if(waiting_timer != null)

                waiting_timer!!.cancel()

            layout_notify_user.visibility = View.GONE

            if(driverRequestReceived != null){

                val destinationLatLng = LatLng(

                        driverRequestReceived!!.destinationLocation!!.split(",")[0].toDouble(),
                        driverRequestReceived!!.destinationLocation!!.split(",")[1].toDouble()

                )

                mMap.addMarker(MarkerOptions().position(destinationLatLng)
                        .title(driverRequestReceived!!.destinationLocationString)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

                //Draw Path to Destination

                drawPathFromCurrentLocation(driverRequestReceived!!.destinationLocation)

            }

            startAccelerometer()

            start_journey_button.visibility = View.GONE
            chip_decline.visibility = View.GONE
           //Before Complete Photos Added// complete_journey_button.visibility = View.VISIBLE
            complete_Pictures.visibility = View.VISIBLE
            //complete_Pictures.setBackgroundResource(R.color.dark_Blue)
            //complete_Pictures.setTextColor(getColor(R.color.white))



        }

        complete_Pictures.setOnClickListener {


            sensorManager!!.unregisterListener(this)

            val cameraMode = "dropOffPhotos"

            val intent = Intent(this, CameraActivity::class.java)

            intent.putExtra(MESSGAE, cameraMode)
            intent.putExtra(MESSGAE2, tripNumberId)

            startActivity(intent)

            complete_Pictures.visibility = View.GONE

            complete_journey_button.visibility = View.VISIBLE

            complete_journey_button.isEnabled = true



        }



        complete_journey_button.setOnClickListener{

            //Toast.makeText(this@MapsActivity, "Complete Journey", Toast.LENGTH_SHORT).show()

            val update_trip = HashMap<String, Any>()

            update_trip.put("isdone", true)
            FirebaseDatabase.getInstance()
                    .getReference(Common.TRIP)
                    .child(tripNumberId!!)
                    .updateChildren(update_trip)
                    .addOnFailureListener { e ->  Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()
                    }.addOnSuccessListener {

                        fusedLocationProviderClient!!.lastLocation
                                .addOnFailureListener { e->

                                    Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                                }.addOnSuccessListener { location ->

                                    UserUtils.sendCompleteTripToUser(root_layout, this , driverRequestReceived!!.key, tripNumberId)

                                    //Reset View
                                    mMap.clear()

                                    tripNumberId = ""
                                    isTripStart = false

                                    chip_decline.visibility = View.GONE
                                    layout_accept.visibility = View.GONE
                                    circularProgressBar.progress = 0.toFloat()

                                    start_journey.visibility = View.GONE

                                    layout_notify_user.visibility = View.GONE
                                    progress_notify.progress = 0

                                    complete_journey_button.isEnabled = false
                                    complete_journey_button.visibility = View.GONE

                                    start_journey_button.isEnabled = false
                                    start_journey_button.visibility = View.GONE

                                    take_Pictures.isEnabled = false
                                    take_Pictures.visibility = View.VISIBLE

                                    //Before Camera Function Added //start_journey_button.isEnabled = false
                                   //Before Camera Function Added  //start_journey_button.visibility = View.VISIBLE

                                    destinationGeoFire = null
                                    pickupGeoFire = null


                                    driverRequestReceived = null

                                    makeDriverOnline(location)



                                }


                    }



        }

    }

    private fun drawPathFromCurrentLocation(destinationLocation: String?) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this@MapsActivity, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()

            return
        }
        fusedLocationProviderClient!!.lastLocation
                .addOnFailureListener { e->

                    Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                }

                .addOnSuccessListener { location->

                    compositeDisposable.add(iGoogleAPI.getDirections(
                            "driving", "less_driving",
                            StringBuilder()
                                    .append(location.latitude)
                                    .append(",")
                                    .append(location.longitude)
                                    .toString(),
                            destinationLocation,
                            getString(R.string.google_api_key))
                    !!.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { returnResult ->

                                Log.d("API_RETURN", returnResult)

                                try {

                                    val jsonObject = JSONObject(returnResult)

                                    val jsonArray = jsonObject.getJSONArray("routes")

                                    for (i in 0 until jsonArray.length()) {

                                        val route = jsonArray.getJSONObject(i)

                                        val poly = route.getJSONObject("overview_polyline")

                                        val polyline = poly.getString("points")

                                        polylineList = Common.decodePoly(polyline)

                                    }

                                    polylineOptions = PolylineOptions()
                                    polylineOptions!!.color(R.color.blue)
                                    polylineOptions!!.width(12f)
                                    polylineOptions!!.startCap(SquareCap())
                                    polylineOptions!!.jointType(JointType.ROUND)
                                    polylineOptions!!.addAll(polylineList)
                                    greyPolyline = mMap.addPolyline(polylineOptions)

                                    blackPolylineOptions = PolylineOptions()
                                    blackPolylineOptions!!.color(R.color.black)
                                    blackPolylineOptions!!.width(5f)
                                    blackPolylineOptions!!.startCap(SquareCap())
                                    blackPolylineOptions!!.jointType(JointType.ROUND)
                                    blackPolylineOptions!!.addAll(polylineList)
                                    blackPolyline = mMap.addPolyline(blackPolylineOptions)



                                    val origin = LatLng(location.latitude, location.longitude)

                                    val destination = LatLng(destinationLocation!!.split(",")[0].toDouble(),
                                            destinationLocation!!.split(",")[1].toDouble())

                                    val latLngBound = LatLngBounds.Builder().include(origin)
                                            .include(destination)
                                            .build()


                                    Log.d("MapActivity", "createGeoFirePickupLocation")

                                    createGeoFireDestinationLocation(driverRequestReceived!!.key, destination)


                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBound, 160))
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))

                                } catch (e: java.lang.Exception) {

                                    Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                                }

                            })

                }



    }

    private fun createGeoFireDestinationLocation(key: String?, destination: LatLng) {

        val ref = FirebaseDatabase.getInstance().getReference(Common.TRIP_DESTINATION_LOCATION_REF)

        destinationGeoFire = GeoFire(ref)
        destinationGeoFire!!.setLocation(key!!,
        GeoLocation(destination.latitude, destination.longitude), {key1, error ->




        })

    }

    private fun init(){

        iGoogleAPI = RetroFitClient.instance!!.create(IGoogleAPI::class.java)

        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected")

        //If Permission is Not Allowed Do Not init, Let the User Allow Permissions First

        if (ActivityCompat.checkSelfPermission(
                        this@MapsActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@MapsActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(this@MapsActivity, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()

            return
        }

       buildLocationRequest()

        buildLocationCallback()

        updateLocation()


    }

    private fun updateLocation() {

        if(fusedLocationProviderClient == null){

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this!!)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(this@MapsActivity, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()

            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

        }

    }

    private fun buildLocationCallback() {

        if(locationCallback == null){

            locationCallback = object: LocationCallback(){

                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)

                    val newPos = LatLng(locationResult!!.lastLocation.latitude, locationResult!!.lastLocation.longitude)

                    Log.d("MapActivity", pickupGeoFire.toString())

                    if (pickupGeoFire != null){

                        Log.d("MapActivity", "pickupGeoFire != null")

                        pickupGeoQuery =
                                pickupGeoFire!!.queryAtLocation(GeoLocation(locationResult.lastLocation.latitude,
                                locationResult.lastLocation.longitude), Common.MIN_RANGE_PICKUP_KM)

                        Log.d("MapActivity", "pickupGeoQuery " + pickupGeoQuery.toString())

                        pickupGeoQuery!!.addGeoQueryEventListener(pickupGeoQueryListener )

                    }

                    if (destinationGeoFire != null){

                        Log.d("MapActivity", "destinationGeoFire != null")

                        destinationGeoQuery =
                                destinationGeoFire!!.queryAtLocation(GeoLocation(locationResult.lastLocation.latitude,
                                        locationResult.lastLocation.longitude), Common.MIN_RANGE_PICKUP_KM)

                        Log.d("MapActivity", "destinationGeoQuery " + destinationGeoQuery.toString())

                        destinationGeoQuery!!.addGeoQueryEventListener(destinationGeoQueryListener)

                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))

                    /*   val saveCityName = cityName // Save Old City Name to Variable

                       cityName = LocationUtils.getAddressFromLocation(requireContext(), location)*/

                    if(!isTripStart) {

                        makeDriverOnline(locationResult.lastLocation!!)

                    }

                    else{

                        if(!TextUtils.isEmpty(tripNumberId)){

                            val update_data = HashMap<String, Any>()

                            update_data["currentLat"] = locationResult.lastLocation.latitude
                            update_data["currentLng"] = locationResult.lastLocation.longitude

                            FirebaseDatabase.getInstance().getReference(Common.TRIP)
                                .child(tripNumberId!!)
                                .updateChildren(update_data)
                                .addOnFailureListener { e->

                                    Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                                }
                                .addOnSuccessListener {  }


                        }

                    }



                }
            }

        }

    }

    private fun makeDriverOnline(location: Location) {

        val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())

        val addressList: List<Address>?

        try {

            addressList = geoCoder.getFromLocation(
                    location.latitude,
                    location.longitude, 1
            )

            val cityName = addressList[0].countryName

            //postalCode(D08AD6R)
            // featureName(10)
            // adminArea(country Dublin)
            //premises(Upper Cross Road)
            //thoroughfare(10)
            //subThoroughfare(10)
            // countryCode(Ireland)
            // countryName(Ireland)
            // subLocality(Rialto)

            // url(Crash)
            //phone(Crash)
            //getAddressLine(3)(Crash)
            //maxAddressLineIndex(Crash)
            // locale(Crash)
            // locality(Crashes)
            // subAdminArea(Crashes)


            driversLocationRef = FirebaseDatabase.getInstance()
                    .getReference(Common.DRIVERS_LOCATION_REFERENCE).child(cityName)

            currentUserRef =
                    driversLocationRef.child(FirebaseAuth.getInstance().currentUser!!.uid)

            geofire = GeoFire(driversLocationRef)

            //Update Location

            geofire.setLocation(

                    FirebaseAuth.getInstance().currentUser!!.uid,
                    GeoLocation(
                            location.latitude,
                            location.longitude
                    )
            ) { key: String?, error: DatabaseError? ->

                if (error != null)

                    Toast.makeText(
                            this@MapsActivity,
                            error.message,
                            Toast.LENGTH_SHORT
                    ).show()

                //Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()

                /*else

            //Snackbar.make(mapFragment.requireView(), "Your Online", Snackbar.LENGTH_SHORT).show()
                Toast.makeText(this@MapsActivity, "Your Online", Toast.LENGTH_SHORT).show()*/

            }

            registerOnlineSystem()
        } catch (e: IOException) {

            Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

        }

    }

    private fun buildLocationRequest() {

        if(locationRequest == null){

            locationRequest = LocationRequest()
            locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locationRequest!!.setFastestInterval(15000)//(3000) 15000 15 Seconds
            locationRequest!!.interval = 10000//5000 10 Seconds
            locationRequest!!.setSmallestDisplacement(50f)//10f) 50f 50 Meters

        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Request Permission

        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object:PermissionListener{

                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    //Enable Button First

                    if (ActivityCompat.checkSelfPermission(
                                    this@MapsActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                    this@MapsActivity,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        Toast.makeText(this@MapsActivity, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()

                        return
                    }

                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener {

                        Toast.makeText(this@MapsActivity, "CLICKED BUTTON" , Toast.LENGTH_LONG ).show()

                        fusedLocationProviderClient!!.lastLocation
                            .addOnFailureListener { e ->

                                Toast.makeText(this@MapsActivity, e.message + "TEST ACCEPTED", Toast.LENGTH_LONG ).show()

                            }.addOnSuccessListener { location ->

                                val userLatLng = LatLng(location.latitude, location.longitude)
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f))

                            }
                        true

                    }

                    //Location

                    buildLocationRequest()

                    buildLocationCallback()

                    updateLocation()




                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                    Toast.makeText(this@MapsActivity,"Permission " +p0!!.permissionName+" was Denied", Toast.LENGTH_LONG ).show()

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }


            }).check()
/*
        val myLatitude = intent.getDoubleExtra(HomeActivity.MESSGAE1, -1.0)
        val myLongitude = intent.getDoubleExtra(HomeActivity.MESSGAE2, -2.0)

        Log.d("Location Latitude", " CURRENT GPS Latitude : " + myLatitude)
        Log.d("Location Longitude", " CURRENT GPS Longitude : " + myLongitude)

        // Add a marker in Sydney and move the camera

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/location/$uid").push()

        val user = locationSave(myLatitude, myLongitude)

        ref.setValue(user)

        val myCurrentLocation = LatLng(myLatitude, myLongitude)*/
        
   /*     mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.addMarker(MarkerOptions().position(myCurrentLocation).title("Current Location"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 15F))*/


/*        if (mMap != null) {
            val permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)

            if (permission == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true


            } else {
                requestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        LOCATION_REQUEST_CODE)
            }
        }*/
        Toast.makeText(this@MapsActivity, "Your Online", Toast.LENGTH_SHORT).show()
    }

/*    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5f, this)
    }
   override fun onLocationChanged(location: Location) {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/currentLocation/$uid")//"/users/location/$uid").push()

        val user = locationchanged(location.latitude , location.longitude)

        ref.setValue(user)
        //tvGpsLocation = findViewById(R.id.textView)
      //  tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude
    }*/




/*    private fun requestPermission(permissionType: String,
                                   requestCode: Int) {

        ActivityCompat.requestPermissions(this,
                arrayOf(permissionType), requestCode
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                             permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "Unable to show location - permission required",
                            Toast.LENGTH_LONG).show()
                } else {

                    val mapFragment = supportFragmentManager
                            .findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
            }
        }
    }*/

    private fun startAccelerometer(){

        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        /*sensor=sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)*/
        sensorManager!!.registerListener(this,
                sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)



    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }



    override fun onSensorChanged(event: SensorEvent?) {

        var x = event!!.values[0]
        var y = event.values[1]
        var z = event.values[2]



        var currentTime = System.currentTimeMillis()

        if((currentTime - oldTime) > 100){

            var timeDiff = currentTime - oldTime
            oldTime = currentTime
            var speed= Math.abs(x+y+z-xold-yold-zold)/timeDiff*10000

            if(speed > threadShould){

               // var v=getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
               // v.vibrate(500)
               // Toast.makeText(this, "Vibrate Shake", Toast.LENGTH_LONG).show()

            }

            else if (y > 14.0){//10.0){ //previously x

                var v=getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                //v.vibrate(500)
                //Toast.makeText(this, "Braking Hard -" + x.absoluteValue , Toast.LENGTH_LONG).show()

                val braking = save(y) //x

             FirebaseDatabase.getInstance()
                    .getReference(Common.TRIP)
                    .child(tripNumberId!!).child("Braking")
                     .push().setValue(braking)
                    .addOnFailureListener { e ->  Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()
                    }.addOnSuccessListener {

                         complete_Pictures.visibility = View.GONE

                         hard_braking.visibility = View.VISIBLE


         /*                 Handler(Looper.getMainLooper()).postDelayed({

                              }, 3000)*/

                         hard_braking.visibility = View.GONE
                         complete_Pictures.visibility = View.VISIBLE

                         Toast.makeText(this, "Hard Braking Detected - " + y.absoluteValue , Toast.LENGTH_LONG).show()

                    }

/*                val uid = FirebaseAuth.getInstance().uid ?: ""
                val ref = FirebaseDatabase.getInstance().getReference("/DriverInfo/mapSensor/$uid").push()
                //val ref = FirebaseDatabase.getInstance().getReference("/UserInfo/mapSensor/$uid").push()

                val braking = save(x)

                ref.setValue(braking)*/

            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){

            return true

        }



        return super.onOptionsItemSelected(item)
    }

/*    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }*/


/*    private class locationSave(val Latitude: Double, val Longitude: Double) {

    }
    private class locationchanged(val latitude: Double, val longitude: Double) {

    }*/
    private class save(val sensor: Float) {

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public fun onDriverRequestReceived(event: DriverRequestReceived){

        driverRequestReceived = event

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this@MapsActivity, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()

            return
        }
        fusedLocationProviderClient!!.lastLocation
                .addOnFailureListener { e->

                    Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                }

                .addOnSuccessListener { location->

                    compositeDisposable.add(iGoogleAPI.getDirections(
                            "driving", "less_driving",
                           StringBuilder()
                                   .append(location.latitude)
                                   .append(",")
                                   .append(location.longitude)
                                   .toString(),
                            event.pickupLocation,
                            getString(R.string.google_api_key))
                    !!.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { returnResult ->

                                Log.d("API_RETURN", returnResult)

                                try {

                                    val jsonObject = JSONObject(returnResult)

                                    val jsonArray = jsonObject.getJSONArray("routes")

                                    for (i in 0 until jsonArray.length()) {

                                        val route = jsonArray.getJSONObject(i)

                                        val poly = route.getJSONObject("overview_polyline")

                                        val polyline = poly.getString("points")

                                        polylineList = Common.decodePoly(polyline)

                                    }

                                    polylineOptions = PolylineOptions()
                                    polylineOptions!!.color(R.color.light_Blue)
                                    polylineOptions!!.width(12f)
                                    polylineOptions!!.startCap(SquareCap())
                                    polylineOptions!!.jointType(JointType.ROUND)
                                    polylineOptions!!.addAll(polylineList)
                                    greyPolyline = mMap.addPolyline(polylineOptions)

                                    blackPolylineOptions = PolylineOptions()
                                    blackPolylineOptions!!.color(R.color.dark_Blue)
                                    blackPolylineOptions!!.width(5f)
                                    blackPolylineOptions!!.startCap(SquareCap())
                                    blackPolylineOptions!!.jointType(JointType.ROUND)
                                    blackPolylineOptions!!.addAll(polylineList)
                                    blackPolyline = mMap.addPolyline(blackPolylineOptions)

                                    //Animator

                                    val valueAnimator = ValueAnimator.ofInt(0,100)

                                    valueAnimator.duration = 1100
                                    valueAnimator.repeatCount = ValueAnimator.INFINITE
                                    valueAnimator.interpolator = LinearInterpolator()
                                    valueAnimator.addUpdateListener { value ->

                                        val points = greyPolyline!!.points

                                        val percentValue = value.animatedValue.toString().toInt()

                                        val size = points.size

                                        val newPoints = (size * ((percentValue/100.0f)).toInt())

                                        val p = points.subList(0, newPoints)

                                        blackPolyline!!.points = (p)

                                    }

                                    valueAnimator.start()

                                    val origin = LatLng(location.latitude, location.longitude)

                                    val destination = LatLng(event.pickupLocation!!.split(",")[0].toDouble(),
                                    event.pickupLocation!!.split(",")[1].toDouble())

                                    val latLngBound = LatLngBounds.Builder().include(origin)
                                            .include(destination)
                                            .build()

                                    //Add Car Icon for Origin

                                    val objects = jsonArray.getJSONObject(0)

                                    val legs = objects.getJSONArray("legs")

                                    val legsObject = legs.getJSONObject(0)

                                    val time = legsObject.getJSONObject("duration")
                                    val duration = time.getString("text")

                                    val distanceEstimate = legsObject.getJSONObject("distance")
                                    val distance = distanceEstimate.getString("text")

                                    txt_estimate_time.setText(duration)
                                    txt_estimate_distance.setText(distance)

                                    mMap.addMarker(MarkerOptions().position(destination).icon(BitmapDescriptorFactory.defaultMarker()).title("Collection Location"))

                                    Log.d("MapActivity", "createGeoFirePickupLocation")

                                    createGeoFirePickupLocation(event.key, destination)


                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBound, 160))
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))

                                    //Display Layout

                                    chip_decline.visibility = View.VISIBLE
                                    layout_accept.visibility = View.VISIBLE

                                    //Countdown Timer

                                    countDownEvent = Observable.interval(100, TimeUnit.MILLISECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnNext { x ->

                                                circularProgressBar.progress += 1f

                                            }
                                            .takeUntil{aLong -> aLong == "100".toLong()} //10 Seconds
                                            .doOnComplete{

                                                //Toast.makeText(this@MapsActivity, "Collection Accepted", Toast.LENGTH_LONG).show()
                                                createTripPlan(event, duration, distance)

                                            }.subscribe()


                                } catch (e: java.lang.Exception) {

                                    Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                                }

                            })

                }


    }

    private fun createGeoFirePickupLocation(key: String?, destination: LatLng) {

        Log.d("MapActivity", "Entered createGeoFirePickupLocation")

        val ref = FirebaseDatabase.getInstance()
                .getReference(Common.TRIP_PICKUP_REF)

        Log.d("MapActivity", ref.toString())

        pickupGeoFire = GeoFire(ref)
        pickupGeoFire!!.setLocation(key, GeoLocation(destination.latitude, destination.longitude),
           { key1, error ->

            if (error != null)

                Toast.makeText(this@MapsActivity, error.message, Toast.LENGTH_SHORT).show()
            else

                Log.d("MapActivity", key1 + " was Successfully Created")


        })

       //Log.d("MapActivity", "pickupgeofire" + pickupGeoFire)

        //start_journey_button.isEnabled = true //Temporally Put Here For Testing

        //UserUtils.sendNotifyToUser(this, root_layout, key)//Temporally Put Here For Testing

    }

    private fun createTripPlan(event: DriverRequestReceived, duration: String, distance: String) {

        setLayoutProcess(true)

        //Sync the Sever Time with the Device

        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val timeOffset = snapshot.getValue(Long::class.java)

                    //Load User Information

                    FirebaseDatabase.getInstance()
                        .getReference(Common.USER_INFO)
                        .child(event!!.key!!)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onDataChange(snapshot: DataSnapshot) {

                                if(snapshot.exists()){

                                    val userModel = snapshot.getValue(UserModel::class.java)

                                    //Get Location

                                    if (ActivityCompat.checkSelfPermission(
                                            this@MapsActivity,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            this@MapsActivity,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {

                                        Toast.makeText(this@MapsActivity, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()

                                        return
                                    }
                                    fusedLocationProviderClient!!.lastLocation
                                        .addOnFailureListener { e ->

                                            Toast.makeText(
                                                this@MapsActivity,
                                                e.message,
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }
                                        .addOnSuccessListener { location ->

                                            val current = LocalDateTime.now()

                                            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                            val formatted = current.format(formatter)

                                            //Create Trip Planner

                                            val tripPlanModel = TripPlanModel()

                                            tripPlanModel.driver = FirebaseAuth.getInstance().currentUser!!.uid
                                            tripPlanModel.user = event!!.key
                                            tripPlanModel.driverInfoModel = Common.currentUser
                                            tripPlanModel.userModel = userModel
                                            tripPlanModel.origin = event.pickupLocation
                                            tripPlanModel.originString = event.pickupLocationString
                                            tripPlanModel.destination = event.destinationLocation
                                            tripPlanModel.destinationString = event.destinationLocationString
                                            tripPlanModel.distancePickup = distance
                                            tripPlanModel.durationPickup = duration
                                            tripPlanModel.currentLat = location.latitude
                                            tripPlanModel.currentLng = location.longitude
                                            tripPlanModel.time = formatted
                                            /*tripPlanModel.collectionPhotos = null
                                            tripPlanModel.dropOffPhotos = null*/


                                            tripNumberId = Common.createUniqueTripIdNumber(timeOffset)

                                            tripPlanModel.collectionNumber = tripNumberId

                                            //Submit to Firebase

                                            FirebaseDatabase.getInstance().getReference(Common.TRIP)
                                                .child(tripNumberId!!)
                                                .setValue(tripPlanModel)
                                                .addOnFailureListener { e->

                                                    Toast.makeText(this@MapsActivity, e.message, Toast.LENGTH_SHORT).show()

                                                }
                                                .addOnSuccessListener { aVoid ->

                                                    txt_user_name.setText(userModel!!.name)
                                                    txt_start_estimate_distance.setText(distance)
                                                    txt_start_estimate_time.setText(duration)

                                                    setOfflineModeFroDriver(event, duration, distance)


                                                }

                                           /* FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
                                                    .child(event!!.key!!)
                                                    .child("Collections")
                                                    .child(tripNumberId!!)
                                                    .setValue(tripPlanModel)*/

                                        }

                                }

                                else{

                                    Toast.makeText(this@MapsActivity, getString(R.string.user_not_found) + " " + event!!.key!!, Toast.LENGTH_SHORT).show()

                                }

                            }

                            override fun onCancelled(error: DatabaseError) {

                                Toast.makeText(this@MapsActivity, error.message, Toast.LENGTH_SHORT).show()

                            }


                        })

                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(this@MapsActivity, error.message, Toast.LENGTH_SHORT).show()

                }


            })



    }

    private fun setOfflineModeFroDriver(event: DriverRequestReceived, duration: String, distance: String) {

        UserUtils.sendAcceptRequestToDriver(this, root_layout, event.key!!, tripNumberId)


        //Go to Offline
        if (currentUserRef != null)

            currentUserRef!!.removeValue()

        setLayoutProcess(false)
        layout_accept.visibility = View.VISIBLE
        start_journey.visibility = View.VISIBLE

        isTripStart = true

    }

    private fun setLayoutProcess(process: Boolean) {

        var color = -1

        if(process){

            color = ContextCompat.getColor(this , R.color.lightOrange)
            circularProgressBar.indeterminateMode = true
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_star_24_gray, 0)

        }
        else{

            color = ContextCompat.getColor(this , R.color.black)
            circularProgressBar.indeterminateMode = false
            circularProgressBar.progress = 0f
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_star_24, 0)

        }

        txt_estimate_time.setTextColor(color)
        txt_estimate_distance.setTextColor(color)
        txt_rating.setTextColor(color)
        txt_type.setTextColor(color)
        ImageViewCompat.setImageTintList(image_round, ColorStateList.valueOf(color))

    }



    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onNotifyUser(event: NotifyUserEvent){

        layout_notify_user!!.visibility = View.VISIBLE
        progress_notify.max = Common.WAIT_TIME_MIN * 60

        val countDownTimer = object: CountDownTimer((progress_notify.max*1000).toLong(), 1000) {
            override fun onTick(l: Long) {

                progress_notify.progress +=1

                txt_notify_user.text = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l)),
                        TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)))

                Log.d("MapActivity", txt_notify_user.text as String)

            }

            override fun onFinish() {

                Toast.makeText(this@MapsActivity, getString(R.string.times_up), Toast.LENGTH_LONG ).show()

            }


        }
                .start()
                Log.d("MapActivity", "CountDown Timer Started")



    }


}



