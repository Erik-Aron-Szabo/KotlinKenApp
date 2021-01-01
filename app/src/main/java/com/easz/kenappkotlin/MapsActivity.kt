package com.easz.kenappkotlin

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    GoogleMap.OnMapClickListener {

    private val TAG = "MapsActivity"
    val FINE_LOCATION_REQUEST_CODE = 1
    val BACKGROUND_ACCESS_LOCATION_REQUEST_CODE = 2
    private lateinit var mMap: GoogleMap

    private var geofencingClient: GeofencingClient? = null
    private var geofenceHelper: GeofenceHelper? = null

    private val GEOFENCE_DESTINATION_RADIUS: Double = 15.24
    private val GEOFENCE_DESTINATION_RADIUS_FLOAT: Float = 15.24F
    private val GEOFENCE_ID: String = "GEOFENCE_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Toast.makeText(this, "MapsActivity", Toast.LENGTH_SHORT).show()

        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceHelper = GeofenceHelper(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        enableUserLocation()

        mMap.setOnMapLongClickListener(this)
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // HAVE permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_REQUEST_CODE)
                } else {
                    mMap.setMyLocationEnabled(true)
                }
            } else {
                // DON'T have permission
                Toast.makeText(this, "Enable Location permission", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == BACKGROUND_ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // HAVE permission
                Toast.makeText(this, "You can add Geofences", Toast.LENGTH_SHORT).show()
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), BACKGROUND_ACCESS_LOCATION_REQUEST_CODE)
                } else {
                    mMap.setMyLocationEnabled(true)
                }
            } else {
                // DON'T have permission
                Toast.makeText(this, "Enable Background Location Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapLongClick(latLng: LatLng?) {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                tryAddingGeofence(latLng!!)
            } else {
                // Check
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_ACCESS_LOCATION_REQUEST_CODE
                )
            }
        } else {
            tryAddingGeofence(latLng!!)
        }
    }

    private fun tryAddingGeofence(latLng: LatLng) {
        mMap.clear()
        addMarker(latLng)
        addCircle(latLng)
        addGeofence(latLng, GEOFENCE_DESTINATION_RADIUS_FLOAT)
    }

    private fun addGeofence(latLng: LatLng, radius: Float) {
        var geofence: Geofence = geofenceHelper!!.getGeofence(
            GEOFENCE_ID,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        var geofencingRequest: GeofencingRequest? = geofenceHelper!!.getGeofencingRequest(geofence)
        var pendingIntent: PendingIntent = geofenceHelper!!.getPendingIntent()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_REQUEST_CODE
            )
        } else {
            geofencingClient?.addGeofences(geofencingRequest, pendingIntent)
                ?.addOnSuccessListener { Log.d(TAG, "onSuccess: Geofence Added") }
                ?.addOnFailureListener { e ->
                    val errorMessage = geofenceHelper?.getErrorString(e)
                    Log.d(TAG, "onFailure: " + errorMessage)
                }
        }
    }

    private fun addMarker(latLng: LatLng) {
        var markerOptions: MarkerOptions = MarkerOptions().position(latLng)
        mMap.addMarker(markerOptions)
    }

    private fun addCircle(latLng: LatLng) {
        var circleOptions: CircleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(GEOFENCE_DESTINATION_RADIUS)
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
        circleOptions.fillColor(Color.argb(121, 255, 0, 0))
        circleOptions.strokeWidth(4F)
        mMap.addCircle(circleOptions)
    }

    override fun onMapClick(p0: LatLng?) {

    }
}