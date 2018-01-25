package edu.depaul.csc472.lanny.xul_final.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;

import edu.depaul.csc472.lanny.xul_final.R;
import edu.depaul.csc472.lanny.xul_final.helpers.PermissionUtils;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    GoogleMap mMap;
    //set latitude for Chicago
    private static final double
            CHICAGO_LAT = 41.884178,
            CHICAGO_LNG = -87.633242;
    private Marker marker1, marker2;
    private Polyline line;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //implement map function
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        gotoLocation(CHICAGO_LAT, CHICAGO_LNG, 13);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        enableLongClick();
        enableMarkerClick();
        enableMarkerDrag();
    }

    //update the location
    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void geoLocate(View v) throws IOException {

        hideSoftKeyboard(v);

        TextView tv = findViewById(R.id.eT1);
        String searchString = tv.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list.size() > 0) {
            Address add = list.get(0);
            String locality = add.getLocality();
            Toast.makeText(this, "Found: " + locality, Toast.LENGTH_SHORT).show();

            double lat = add.getLatitude();
            double lng = add.getLongitude();
            gotoLocation(lat, lng, 15);

            if (marker1 != null){
                marker1.remove();
            }
            addMarker(add, lat, lng);
        }
    }

    //method to add markers
    private void addMarker (Address add, double lat, double lng){
        MarkerOptions options = new MarkerOptions()
                .title(add.getLocality()).position(new LatLng(lat, lng)).draggable(true);

        String country = add.getCountryName();
        if (country.length() > 0){
            options.snippet(country);
        }

        if (marker1 == null) {
            marker1 = mMap.addMarker(options);
        } else if (marker2 == null) {
            marker2 = mMap.addMarker(options);
            drawLine();
        } else {
            removeEverything();
            marker1 = mMap.addMarker(options);
        }
    }

    //current location methods
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }
    //ask for permission to access location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }


    //Displays a dialog with error message explaining that the location permission is missing.
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
    //Enable long click function
    private void enableLongClick () {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Geocoder gc = new Geocoder(MapActivity.this);
                List<Address> list = null;

                try {
                    list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Address add = list.get(0);
                MapActivity.this.addMarker(add, latLng.latitude, latLng.longitude);
            }
        });
    }
    //Enable put marker function
    private void enableMarkerClick () {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String msg = marker.getTitle() + " (" +
                        marker.getPosition().latitude + ", " +
                        marker.getPosition().longitude + ")";
                Toast.makeText(MapActivity.this, msg, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
    //Enable drag marker function
    private void enableMarkerDrag () {
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Geocoder gc = new Geocoder(MapActivity.this);
                List<Address> list = null;
                LatLng ll = marker.getPosition();
                try {
                    list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Address add = list.get(0);
                marker.setTitle(add.getLocality());
                marker.setSnippet(add.getCountryName());
                marker.showInfoWindow();
            }
        });
    }
    //Enable draw line function
    private void drawLine() {
        PolylineOptions lineOptions = new PolylineOptions()
                .add(marker1.getPosition())
                .add(marker2.getPosition());
        line = mMap.addPolyline(lineOptions);
    }

    private void removeEverything() {
        marker1.remove();
        marker1 = null;
        marker2.remove();
        marker2 = null;
        if (line != null) {
            line.remove();
            line = null;
        }
    }
}
