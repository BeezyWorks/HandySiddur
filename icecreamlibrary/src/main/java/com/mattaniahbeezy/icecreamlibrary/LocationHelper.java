package com.mattaniahbeezy.icecreamlibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import wearprefs.WearPrefs;

/**
 * Created by Mattaniah on 5/25/2015.
 */
public class LocationHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    GoogleApiClient googleApiClient;
    Context context;
    LocationHandled locationHandled;

    public static final int SAVED = 0;
    public static final int RETRIEVED = 1;

    public LocationHelper(Context context, LocationHandled locationHandled) {
        this.context = context;
        WearPrefs.init(context);
        this.locationHandled = locationHandled;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect() {
        googleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(10);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("here is your location: ", location.toString());
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Resources res = context.getResources();
        editor.putString(SettingsHelper.longitudeKey, String.valueOf(location.getLongitude()));
        editor.putString(SettingsHelper.latitudeKey, String.valueOf(location.getLatitude()));
        editor.putString(SettingsHelper.elevationKey, String.valueOf(location.getAltitude()));
        editor.apply();
        locationHandled.locationAvailable(location, RETRIEVED);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        SettingsHelper settingsHelper = new SettingsHelper(context);
        Resources res = context.getResources();
        double longitude = Double.valueOf(settingsHelper.getString(SettingsHelper.longitudeKey, "35.235806"));
        double latitude = Double.valueOf(settingsHelper.getString(SettingsHelper.latitudeKey, "31.777972"));
        double elevation = Double.valueOf(settingsHelper.getString(SettingsHelper.elevationKey, "0"));
        if (elevation <= 0d)
            elevation = 1d;
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(elevation);
        locationHandled.locationAvailable(location, SAVED);
    }

    public interface LocationHandled {
        public void locationAvailable(Location location, int locationSource);
    }

    public void disconnect() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
        googleApiClient.disconnect();
    }
}
