package com.easz.kenappkotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "GeofenceBroadcastReceiv"

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Toast.makeText(context, "Geofence triggered", Toast.LENGTH_SHORT).show()

        var geofencingEvent: GeofencingEvent? = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent!!.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event")
        }

        var geofenceList: List<Geofence> = geofencingEvent.triggeringGeofences
        for (geofence in geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.requestId)
        }

        var transitionType: Int = geofencingEvent.geofenceTransition

        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> Toast.makeText(context, "Geofence ENTER", Toast.LENGTH_SHORT).show()
            Geofence.GEOFENCE_TRANSITION_DWELL -> Toast.makeText(context, "Inside Geofence (DWELL)", Toast.LENGTH_SHORT).show()
            Geofence.GEOFENCE_TRANSITION_EXIT -> Toast.makeText(context, "Geofence EXIT", Toast.LENGTH_SHORT).show()
        }
    }
}
