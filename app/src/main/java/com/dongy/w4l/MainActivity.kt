package com.dongy.w4l

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.dongy.w4l.CardView.CardStackAdapter
import com.dongy.w4l.Retrofit2.GitHubService
import com.dongy.w4l.Retrofit2.Spots
import com.yuyakaido.android.cardstackview.*
import com.yuyakaido.android.cardstackview.sample.Spot
import com.yuyakaido.android.cardstackview.sample.SpotDiffCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class MainActivity : AppCompatActivity() , CardStackListener {

    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val cardStackView by lazy { findViewById<CardStackView>(R.id.card_stack_view) }
    private val manager by lazy { CardStackLayoutManager(this, this) }
    private val adapter by lazy { CardStackAdapter() }
    private var resultList= mutableListOf<Spots.Results>()
    private var lng : String? = null
    private var lat : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpPermission()
        setUpNetworkModule()
        setupNavigation()
        setupCardStackView()
        setupButton()
    }

    fun setUpPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    200)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
        } else {
            // Permission has already been granted
            setupLocationInfoModule()
        }
    }

    fun setUpNetworkModule(){

        val service = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.base_url_place))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)

        service.retrievePlaces(
            lat+","+lng
            , "10000"
            , "restaurant"
            , "ko"
            , resources.getString(R.string.goolge_map_key))
            .enqueue(object : Callback<Spots> {
                override fun onResponse(call: Call<Spots>, response: Response<Spots>) {
                    if(response.body()?.status == "OK") {
                        response.body()?.results?.forEach {
                            println("TAG_: $it")
                            resultList.add(it)
                        }

                        adapter.setSpots((createSpots()))
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<Spots>, t: Throwable) = t.printStackTrace()
            })
    }

    fun setupLocationInfoModule(){
        // Acquire a reference to the system Location Manager
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // GPS 프로바이더 사용가능여부
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // 네트워크 프로바이더 사용가능여부
//        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        Log.d("Main", "isGPSEnabled=$isGPSEnabled")
//        Log.d("Main", "isNetworkEnabled=$isNetworkEnabled")

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val lat = location.getLatitude()
                val lng = location.getLongitude()

                Log.d("latitude:", lat.toString())
                Log.d("longitude:", lng.toString())
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
//                logView.setText("onStatusChanged")
            }

            override fun onProviderEnabled(provider: String) {
//                logView.setText("onProviderEnabled")
            }

            override fun onProviderDisabled(provider: String) {
//                logView.setText("onProviderDisabled")
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Register the listener with the Location Manager to receive location updates
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
        }
        // 수동으로 위치 구하기
        val locationProvider = LocationManager.GPS_PROVIDER
        val lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)
        if (lastKnownLocation != null) {
            lng = String.format("%.6f",Math.abs(lastKnownLocation.longitude))
            lat = String.format("%.6f",Math.abs(lastKnownLocation.latitude))
            Log.d("Main", "longtitude=$lng, latitude=$lat")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
//            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
            200 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

//    https://maps.googleapis.com/maps/api/place/nearbysearch/json? 
//    location= 37.602592,%20126.654335
//      &radius=1000  
//    &type=restauran  t
//    &fields=contact
//      &key=AIzaSyD3H-668if2DLRN4CGKUFihpPH5Pgrv6_0

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        if (manager.topPosition == adapter.itemCount - 5) {
            paginate()
        }
    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }

    private fun setupNavigation() {
        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

//        // DrawerLayout
//        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
//        actionBarDrawerToggle.syncState()
//        drawerLayout.addDrawerListener(actionBarDrawerToggle)
//
//        // NavigationView
//        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
//        navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId) {
//                R.id.reload -> reload()
//                R.id.add_spot_to_first -> addFirst(1)
//                R.id.add_spot_to_last -> addLast(1)
//                R.id.remove_spot_from_first -> removeFirst(1)
//                R.id.remove_spot_from_last -> removeLast(1)
//                R.id.replace_first_spot -> replace()
//                R.id.swap_first_for_last -> swap()
//            }
//            drawerLayout.closeDrawers()
//            true
//        }
    }

    private fun setupCardStackView() {
        initialize()
    }

    private fun setupButton() {
        val skip = findViewById<View>(R.id.skip_button)
        skip.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }

        val rewind = findViewById<View>(R.id.rewind_button)
        rewind.setOnClickListener {
            val setting = RewindAnimationSetting.Builder()
                .setDirection(Direction.Bottom)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(DecelerateInterpolator())
                .build()
            manager.setRewindAnimationSetting(setting)
            cardStackView.rewind()
        }

        val like = findViewById<View>(R.id.like_button)
        like.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }
    }

    private fun initialize() {
        manager.setStackFrom(StackFrom.None)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    private fun paginate() {
        val old = adapter.getSpots()
        val new = old.plus(createSpots())
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun reload() {
        val old = adapter.getSpots()
        val new = createSpots()
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addFirst(size: Int) {
        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            for (i in 0 until size) {
                add(manager.topPosition, createSpot())
            }
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addLast(size: Int) {
        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            addAll(List(size) { createSpot() })
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeFirst(size: Int) {
        if (adapter.getSpots().isEmpty()) {
            return
        }

        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(manager.topPosition)
            }
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeLast(size: Int) {
        if (adapter.getSpots().isEmpty()) {
            return
        }

        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(this.size - 1)
            }
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun replace() {
        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            removeAt(manager.topPosition)
            add(manager.topPosition, createSpot())
        }
        adapter.setSpots(new)
        adapter.notifyItemChanged(manager.topPosition)
    }

    private fun swap() {
        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            val first = removeAt(manager.topPosition)
            val last = removeAt(this.size - 1)
            add(manager.topPosition, last)
            add(first)
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun createSpot(): Spot {
        return Spot(
            name = "Yasaka Shrine",
            city = "Kyoto",
            url = "https://source.unsplash.com/Xq1ntWruZQI/600x800"
        )
    }

    private fun createSpots(): List<Spot> {
        val spots = ArrayList<Spot>()

        for (result in resultList) {
            spots.add(
                Spot(
                    name = result.name
                    , city = result.vicinity
                    , url = if(result.photos != null)
                                resources.getString(R.string.base_url_photo)
                                + "?maxwidth=400" + "&photoreference="
                                + result.photos[0].photo_reference
                                + "&key="+resources.getString(R.string.goolge_map_key)
                            else
                                "" //기본이미지 넣어주기
                )
            )
        }

        return spots
    }

}
