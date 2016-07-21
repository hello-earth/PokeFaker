package com.github.jinsen47.pokefaker.app;

import android.content.Context;
import android.database.DataSetObserver;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 * Created by Jinsen on 16/7/14.
 */
public class MockProvider {

    private static final String TAG = MockProvider.class.getSimpleName();

    private final String mProvider;
    private final Context mContext;
    private static float accuracy = 0.0f;
    private static float altitude = 0.0f;
    private LocationManager mLocationManager;
    private DataSetObserver mDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            LatLng latLng = LocationHolder.getInstance(mContext).pollLatLng();
            postLocation(latLng.latitude, latLng.longitude);
        }
    };

    /**
     *
     * @param context
     * @param provider
     * @throws IllegalArgumentException if provider is empty or null
     */
    public MockProvider(@NonNull Context context, @NonNull String provider) {
        if (TextUtils.isEmpty(provider)) {
            throw new IllegalArgumentException("Provider should not be null or empty!");
        }
        if (context == null) {
            throw new IllegalArgumentException("Context should not be null");
        }
        mContext = context;
        mProvider = provider;
        mLocationManager = ((LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE));
        mLocationManager.addTestProvider(mProvider, false, false, false, false, true /* allow speed */, false, false, 0, 5);
        Random rd = new Random(SystemClock.currentThreadTimeMillis());
        if(accuracy==0.0f)accuracy = 6.0f+rd.nextFloat()*12;
        if(altitude==0.0f)altitude = 37.0f+rd.nextFloat()*19;
    }

    public synchronized void start() {
        LocationHolder.getInstance(mContext).registerObserver(mDataObserver);
        mLocationManager.setTestProviderEnabled(mProvider, true);
        mLocationManager.setTestProviderStatus(mProvider, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
    }

    public synchronized void pause() {
        LocationHolder.getInstance(mContext).unregisterObserver(mDataObserver);
        mLocationManager.setTestProviderEnabled(mProvider, false);
    }

    public synchronized void remove() {
        LocationHolder.getInstance(mContext).unregisterObserver(mDataObserver);
        try {
            if (mLocationManager.getProvider(mProvider) != null) {
                mLocationManager.removeTestProvider(mProvider);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, mProvider + "has already been removed!");
        }
    }

    public void postLocation(double latitude, double longitude) {
        Location l = new Location(mProvider);
        l.setLatitude(latitude);
        l.setLongitude(longitude);
        l.setAccuracy(accuracy);
        l.setAltitude(altitude);
        l.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            l.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        postLocation(l);
    }

    private void postLocation(Location l) {
        mLocationManager.setTestProviderLocation(mProvider, l);
    }

}
