package com.apptivitylab.learn.whereami;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by aarief on 13/10/2016.
 */

public class CurrentLocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mMapFragment;
    private GoogleMap mGoogleMap;

    private Marker mLocationMarker;
    private Circle mLocationCircle;

    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mAccuracyTextView;
    private Button mStopButton;
    private Button mStartButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_location, container, false);

        mLatitudeTextView = (TextView) view.findViewById(R.id.fragment_current_location_tv_latitude);
        mLongitudeTextView = (TextView) view.findViewById(R.id.fragment_current_location_tv_longitude);
        mAccuracyTextView = (TextView) view.findViewById(R.id.fragment_current_location_tv_accuracy);
        mStopButton = (Button) view.findViewById(R.id.fragment_current_location_button_stop);
        mStartButton = (Button) view.findViewById(R.id.fragment_current_location_button_start);

        if (savedInstanceState == null) {
            setupGoogleMapsFragment();
        } else {
            mMapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_current_location_vg_map);
        }

        return view;
    }

    private void setupGoogleMapsFragment() {
        mMapFragment = SupportMapFragment.newInstance();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_current_location_vg_map, mMapFragment)
                .commit();

        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;

                LatLng officeLatLng = new LatLng(4.2105, 101.9758);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(officeLatLng, 6);

                mGoogleMap.moveCamera(cameraUpdate);
            }
        });

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext(), this, this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdates();
            }
        });

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdates();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    locationRequest,
                    this
            );
        } else {
            Snackbar.make(getView(), "GoogleApiClient is not ready yet", Snackbar.LENGTH_LONG).show();
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                CurrentLocationFragment.this
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    //region GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    //endregion


    //region GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //endregion

    //region LocationListener
    @Override
    public void onLocationChanged(Location location) {
        Snackbar.make(getView(), "Location update received!", Snackbar.LENGTH_SHORT).show();

        String latitudeString = "Latitude: "+ String.valueOf(location.getLatitude());
        String longitudeString = "Longitude: " + String.valueOf(location.getLongitude());
        String accuracyString = "Accuracy: " + String.valueOf(location.getAccuracy()) + "m";

        mLatitudeTextView.setText(latitudeString);
        mLongitudeTextView.setText(longitudeString);
        mAccuracyTextView.setText(accuracyString);

        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (mLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions().position(userLatLng).title("Here I am!");
            mLocationMarker = mGoogleMap.addMarker(markerOptions);
        } else {
            mLocationMarker.setPosition(userLatLng);
        }

        if (mLocationCircle == null) {
            CircleOptions circleOptions = new CircleOptions().center(userLatLng).radius(location.getAccuracy());
            mLocationCircle = mGoogleMap.addCircle(circleOptions);
        } else {
            mLocationCircle.setCenter(userLatLng);
            mLocationCircle.setRadius(location.getAccuracy());
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLatLng, 16);
        mGoogleMap.moveCamera(cameraUpdate);
    }
    //endregion
}
