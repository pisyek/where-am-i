package com.apptivitylab.learn.whereami;

import android.*;
import android.Manifest;
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
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by aarief on 13/10/2016.
 */

public class CurrentLocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mAccuracyTextView;
    private Button mStopButton;
    private Button mStartButton;

    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_location, container, false);

        mLatitudeTextView = (TextView) view.findViewById(R.id.fragment_current_location_tv_latitude);
        mLongitudeTextView = (TextView) view.findViewById(R.id.fragment_current_location_tv_longitude);
        mAccuracyTextView = (TextView) view.findViewById(R.id.fragment_current_location_tv_accuracy);
        mStopButton = (Button) view.findViewById(R.id.fragment_current_location_button_stop);
        mStartButton = (Button) view.findViewById(R.id.fragment_current_location_button_start);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext(), this, this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, CurrentLocationFragment.this);
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
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
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

    private void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            // Marshmallow only: check for permissions
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // request for permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }

            // Create location request object
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(5000);

            // Request location updates from FusedLocationApi
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
        } else {
            Snackbar.make(getView(), "GoogleApiClient is not ready yet", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Snackbar.make(getView(), "Location update received!", Snackbar.LENGTH_SHORT).show();

        String latitudeString = "Latitude: "+ String.valueOf(location.getLatitude());
        String longitudeString = "Longitude: " + String.valueOf(location.getLongitude());
        String accuracyString = "Accuracy: " + String.valueOf(location.getAccuracy()) + "m";

        mLatitudeTextView.setText(latitudeString);
        mLongitudeTextView.setText(longitudeString);
        mAccuracyTextView.setText(accuracyString);
    }
}
