package edu.unm.vamshins.locationmarker;

import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapsAddressLocateActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_address_locate);
        setUpMapIfNeeded();

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        mMap.setMyLocationEnabled(true);
        Marker marker = null ;
        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providersList = locManager.getAllProviders();
        LocationProvider provider = locManager.getProvider(providersList.get(0));
        int precision = provider.getAccuracy();
        Criteria req = new Criteria();
        req.setAccuracy(Criteria.ACCURACY_FINE);
        boolean inside = false;

//        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        /*buildPolygon();
        drawPolygon();
        startLocalization();*/

//        GPSTracker mGPS = new GPSTracker(this);

        double latitude = 0;
        double longitude = 0;

//        TextView latlongposition = (TextView) findViewById(R.id.latlongposition);
        TextView addresslocation = (TextView) findViewById(R.id.addresslocation);
        GPSTracker mGPS = new GPSTracker(this);

        if (mGPS.canGetLocation) {
            mGPS.getLocation();
//            latitude = 35.086905;
//            longitude = -106.643520;
            latitude = mGPS.getLatitude();
            longitude = mGPS.getLongitude();
//            latlongposition.setText("Lat: " + latitude + "Lon: " + longitude);
//            mMap.addMarker(new MarkerOptions().position(new LatLng(mGPS.getLatitude(), mGPS.getLongitude())).title("Marker"));
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Marker"));
        } else {
//            latlongposition.setText("Unabletofind");
            System.out.println("Unable");
        }

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("Address:\n");
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                addresslocation.setText(strReturnedAddress.toString());
            } else {
                addresslocation.setText("No Address returned!");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            addresslocation.setText("Canont get Address!");
        }

        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(18)
//                .tilt(70)
                .build();

        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);

        animateMarker(marker, new LatLng(latitude, longitude), false);
    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}
