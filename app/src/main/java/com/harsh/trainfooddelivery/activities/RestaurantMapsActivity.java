package com.harsh.trainfooddelivery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.databinding.ActivityMapsBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RestaurantMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List <Address> addresses;
    private float distance;
    private StringBuilder str;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private void listener(){
        binding.confirmLocation.setOnClickListener(v->{
            if (distanceValid()){
                double duration=((distance/30)*60)+20+10;
                Intent intent=new Intent(getApplicationContext(), RestaurantSignUp.class);
                intent.putExtra("distance", distance);
                intent.putExtra("duration", duration);
                intent.putExtra("address", str.toString());
                intent.putExtra("city", addresses.get(0).getLocality());
                intent.putExtra("latitude", addresses.get(0).getLatitude());
                intent.putExtra("longitude", addresses.get(0).getLongitude());
                intent.putExtra("PostalCode", addresses.get(0).getPostalCode());
                startActivity(intent);
            }
        });
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(this, "Set your Restaurant's Location", Toast.LENGTH_SHORT).show();
        try {
            Intent intent1 = getIntent();
            Geocoder geocoder = new Geocoder(RestaurantMapsActivity.this, Locale.getDefault());
            addresses = geocoder.getFromLocation(intent1.getDoubleExtra("latitude", 0), intent1.getDoubleExtra("longitude", 0), 1);
            int i = 0;
            str = new StringBuilder();
            while (addresses.get(0).getAddressLine(i) != null) {
                str.append(addresses.get(0).getAddressLine(i));
                i++;
            }
            binding.inputAddress.setText(str);
            binding.inputAddress.setSelected(true);
            mMap.addMarker(new MarkerOptions().position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude())).title(addresses.get(0).getLocality()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()), 16f));
            listener();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                mMap.clear();
                try {
                    Geocoder geocoder = new Geocoder(RestaurantMapsActivity.this, Locale.getDefault());
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    int i = 0;
                    str = new StringBuilder();
                    while (addresses.get(0).getAddressLine(i) != null) {
                        str.append(addresses.get(0).getAddressLine(i));
                        i++;
                    }
                    mMap.addMarker(new MarkerOptions().position(latLng).title(addresses.get(0).getAddressLine(0)));
                    binding.inputAddress.setText(str);
                    binding.inputAddress.setSelected(true);
                    listener();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private boolean distanceValid(){
        HashMap<String, LatLng> map=new HashMap<>();
        map.put("Ahmedabad", new LatLng(23.027210514574897, 72.60101821599227));
        map.put("Vadodara", new LatLng(22.310715946809783, 73.18082812409733));
        map.put("Surat", new LatLng(21.204950505141824, 72.84103299981118));
        map.put("Rajkot", new LatLng(22.31327488549474, 70.80246717929595));
        map.put("Mountain View", new LatLng(37.3861, -122.083));

        LatLng destination=map.getOrDefault(addresses.get(0).getLocality(), null);
        if(destination!=null){
            Location locationA = new Location("");
            locationA.setLatitude(addresses.get(0).getLatitude());
            locationA.setLongitude(addresses.get(0).getLongitude());
            Location locationB = new Location("");
            locationB.setLatitude(destination.latitude);
            locationB.setLongitude(destination.longitude);
            distance = (locationA.distanceTo(locationB) / 1000);
            if(distance>11){
                Toast.makeText(this, "Restaurant must be within 10 KMs from Railway Station", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        else{
            Toast.makeText(this, "Services are not available in your City", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}