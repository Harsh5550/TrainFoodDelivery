package com.harsh.trainfooddelivery.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.databinding.ActivityDeliveryHomeMapsBinding;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DeliveryHomeActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ActivityDeliveryHomeMapsBinding binding;
    private FirebaseFirestore database;
    private List <Address> addresses;
    private HashMap<String, LatLng> map;
    private HashMap<String, String> map1;
    private StringBuilder str;
    private Toolbar toolbar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryHomeMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database=FirebaseFirestore.getInstance();
        addresses=new ArrayList<>();
        map=new HashMap<>();
        map.put("Ahmedabad", new LatLng(23.027210514574897, 72.60101821599227));
        map.put("Vadodara", new LatLng(22.310715946809783, 73.18082812409733));
        map.put("Surat", new LatLng(21.204950505141824, 72.84103299981118));
        map.put("Rajkot", new LatLng(22.31327488549474, 70.80246717929595));
        map.put("Mountain View", new LatLng(37.3861, -122.083));

        map1=new HashMap<>();
        map1.put("Ahmedabad", "Ahmedabad Junction (ADI)");
        map1.put("Vadodara", "Vadodara Junction (BRC)");
        map1.put("Surat", "Surat (ST)");
        map1.put("Rajkot", "Rajkot Junction (RJT)");

        preferenceManager=new PreferenceManager(getApplicationContext());

        toolbar=binding.toolbar;
        setActionBar(toolbar);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.primary));
            toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.primary_text));
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("Welcome, "+preferenceManager.getString(Constants.KEY_DELIVERY_NAME));
            actionBar.setSubtitle(preferenceManager.getString(Constants.KEY_DELIVERY_CITY));
        }

        listener("1HLM1GNXNHBeazCS1fFS");

        binding.button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary_text)));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private void listener(String id){
        binding.button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.delivery)));
        binding.button.setOnClickListener(v->{
            binding.pickup.setVisibility(View.VISIBLE);
            binding.button.setVisibility(View.GONE);
            HashMap<String, Object> map=new HashMap<>();
            map.put(Constants.KEY_ORDER_STATUS, Constants.KEY_ASSIGNED_STATUS);
            map.put(Constants.KEY_ORDER_DELIVERY_ID, preferenceManager.getString(Constants.KEY_DELIVERY_ID));
            FirebaseFirestore database=FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_ORDER)
                    .document(id)
                    .update(map);
        });
        binding.pickup.setOnClickListener(v->{
            FirebaseFirestore database=FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_ORDER)
                    .document(id)
                    .get()
                    .addOnSuccessListener(task->{
                        if (task!=null) {
                            StringBuilder stringBuilder=new StringBuilder("");
                            int i = 1;
                            while (task.getString((Constants.KEY_ORDER_FOOD_ITEM + i)) != null) {
                                String[] dissectedValues;
                                dissectedValues = dissectSequence(Objects.requireNonNull(task.getString((Constants.KEY_ORDER_FOOD_ITEM + i))));
                                stringBuilder.append(dissectedValues[0]).append(" X ").append(dissectedValues[1]).append("\n");
                                i++;
                            }
                            AlertDialog.Builder delDialog = new AlertDialog.Builder(this);
                            delDialog.setTitle("PickUp?");
                            delDialog.setIcon(R.drawable.ic_warning);
                            delDialog.setMessage("Enter OTP while collecting order\n"+stringBuilder.substring(0, stringBuilder.length()-1)+"\nTotal Amount: "+task.getString(Constants.KEY_ORDER_AMOUNT));

                            View view = LayoutInflater.from(this).inflate(R.layout.dialog_otp, null);
                            delDialog.setView(view);
                            EditText otpEditText = view.findViewById(R.id.edit_text_otp);
                            otpEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            delDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        if (Objects.equals(task.getString(Constants.KEY_RESTAURANT_OTP), AESCrypt.encrypt("frmeihafokfso", otpEditText.getText().toString()))){
                                            confirm(id);
                                        }
                                        else {
                                            Toast.makeText(DeliveryHomeActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            delDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            delDialog.show();
                        }
                    })
                    .addOnFailureListener(exception->{

                    });
        });
    }

    private void confirm(String id){
        Intent intent=new Intent(getApplicationContext(), DeliveryOrderActivity.class);
        preferenceManager.putString(Constants.KEY_DELIVERY_ORDER_ID, id);
        startActivity(intent);
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
        mMap.setOnMarkerClickListener(this);
        FusedLocationProviderClient fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(DeliveryHomeActivity.this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location!=null){
                            try {
                                Geocoder geocoder = new Geocoder(DeliveryHomeActivity.this, Locale.getDefault());
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                int i = 0;
                                str = new StringBuilder();
                                while (addresses.get(0).getAddressLine(i) != null) {
                                    str.append(addresses.get(0).getAddressLine(i));
                                    i++;
                                }
                                mMap.addMarker(new MarkerOptions().position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude())).title(addresses.get(0).getLocality()).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.delivery, 200, 300)));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude())));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()), 16f));

                                List<String> passengerList=new ArrayList<>();
                                HashMap<String, LatLng> restaurantLocation=new HashMap<>();
                                HashMap<String, String> restaurantName=new HashMap<>();
                                if (validity()){
                                    database.collection(addresses.get(0).getLocality())
                                            .get()
                                            .addOnSuccessListener(task->{
                                                if (!task.isEmpty() && task.size()>0){
                                                    for (DocumentSnapshot documentSnapshot: task.getDocuments()){
                                                        if (distanceValid(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()),
                                                                new LatLng((Double)(Objects.requireNonNull(documentSnapshot.get(Constants.KEY_RESTAURANT_LATITUDE))),
                                                                        (Double)(Objects.requireNonNull(documentSnapshot.get(Constants.KEY_RESTAURANT_LONGITUDE)))), 5100)){
                                                                restaurantLocation.put(documentSnapshot.getId(),  new LatLng((Double)(Objects.requireNonNull(documentSnapshot.get(Constants.KEY_RESTAURANT_LATITUDE))),
                                                                        (Double)(Objects.requireNonNull(documentSnapshot.get(Constants.KEY_RESTAURANT_LONGITUDE)))));
                                                                restaurantName.put(documentSnapshot.getId(), documentSnapshot.getString(Constants.KEY_RESTAURANT_NAME));
                                                                database.collection(Constants.KEY_COLLECTION_ORDER)
                                                                        .whereEqualTo(Constants.KEY_ORDER_CITY, documentSnapshot.getString(Constants.KEY_RESTAURANT_CITY))
                                                                        .whereEqualTo(Constants.KEY_ORDER_STATUS, Constants.KEY_ACCEPTED_STATUS)
                                                                        .get()
                                                                        .addOnSuccessListener(v->{
                                                                            String id=null;
                                                                            if (v.size()>0){
                                                                                HashMap<String, String> map= new HashMap<>();
                                                                                for (int k=0; k<v.getDocuments().size(); k++){
                                                                                    map.put(String.valueOf(k), v.getDocuments().get(k).getString(Constants.KEY_ORDER_TIMESTAMP));
                                                                                }
                                                                                id=getLowerTimestamp(map);
                                                                                for (int k=0; k<v.getDocuments().size(); k++){
                                                                                    if (v.getDocuments().get(k).getId().equals(id)){
                                                                                        passengerList.add(v.getDocuments().get(k).getString(Constants.KEY_ORDER_PASSENGER_ID));
                                                                                    }
                                                                                }
                                                                            }
                                                                            if (id!=null){
                                                                                Marker marker=mMap.addMarker(new MarkerOptions().position(new LatLng(documentSnapshot.getDouble(Constants.KEY_RESTAURANT_LATITUDE), documentSnapshot.getDouble(Constants.KEY_RESTAURANT_LONGITUDE))).title(documentSnapshot.getString(Constants.KEY_RESTAURANT_NAME)).snippet("ETA: "+v.getDocuments().get(Integer.parseInt(id)).getString(Constants.KEY_ORDER_ETA)).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.restaurant, 200, 200)));
                                                                                Objects.requireNonNull(marker).setTag(v.getDocuments().get(Integer.parseInt(id)).getId());
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(Throwable::printStackTrace);
                                                        }
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(Throwable::printStackTrace);

                                    database.collection(Constants.KEY_COLLECTION_ORDER)
                                            .whereIn(Constants.KEY_ORDER_PASSENGER_ID, Collections.singletonList(passengerList))
                                            .whereEqualTo(Constants.KEY_ORDER_STATUS, Constants.KEY_ACCEPTED_STATUS)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    if (value!=null && error==null){
                                                        for (DocumentChange documentChange: value.getDocumentChanges()){
                                                            if (documentChange.getType()== DocumentChange.Type.REMOVED){
                                                                database.collection(Constants.KEY_COLLECTION_ORDER)
                                                                        .whereEqualTo(Constants.KEY_ORDER_RESTAURANT_ID, Objects.requireNonNull(documentChange.getDocument().getString(Constants.KEY_ORDER_RESTAURANT_ID)))
                                                                        .get()
                                                                        .addOnSuccessListener(v->{
                                                                            String id=null;
                                                                            if (!v.isEmpty() && v.size()>0){
                                                                                HashMap<String, String> map= new HashMap<>();
                                                                                for (int k=0; k<v.getDocuments().size(); k++){
                                                                                    map.put(String.valueOf(k), v.getDocuments().get(k).getString(Constants.KEY_ORDER_TIMESTAMP));
                                                                                }
                                                                                id=getLowerTimestamp(map);
                                                                                for (int k=0; k<v.getDocuments().size(); k++){
                                                                                    if (v.getDocuments().get(k).getId().equals(id)){
                                                                                        passengerList.add(v.getDocuments().get(k).getString(Constants.KEY_ORDER_PASSENGER_ID));
                                                                                    }
                                                                                }
                                                                            }
                                                                            if (id!=null){
                                                                                Marker marker=mMap.addMarker(new MarkerOptions().position(Objects.requireNonNull(restaurantLocation.get(Constants.KEY_ORDER_RESTAURANT_ID))).title(restaurantName.get(Constants.KEY_ORDER_RESTAURANT_ID)).snippet("ETA: "+v.getDocuments().get(Integer.parseInt(id)).getString(Constants.KEY_ORDER_ETA)).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.restaurant, 200, 200)));
                                                                                Objects.requireNonNull(marker).setTag(v.getDocuments().get(Integer.parseInt(id)).getId());
                                                                                mMap.moveCamera(CameraUpdateFactory.newLatLng(Objects.requireNonNull(restaurantLocation.get(Constants.KEY_ORDER_RESTAURANT_ID))));
                                                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Objects.requireNonNull(restaurantLocation.get(Constants.KEY_ORDER_RESTAURANT_ID)), 16f));
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                }
                    }
                    catch (Exception e){
                        Toast.makeText(DeliveryHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(DeliveryHomeActivity.this, "Network connection is slow. Please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        })
        .addOnFailureListener(v->{
            Toast.makeText(DeliveryHomeActivity.this, "Network connection is slow. Please try again later", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Handle marker click event here
        // You can also call the onMarkerSelected() method here
        onMarkerSelected(marker);

        // Return true to consume the event and prevent the default marker click behavior
        return true;
    }

    public void onMarkerSelected(Marker marker) {
        // Retrieve the associated data (tag) for the selected marker
        Object data = marker.getTag();
        if (data != null) {
            listener(data.toString());
        }
    }

    private boolean validity(){

        LatLng destination=map.getOrDefault(addresses.get(0).getLocality(), null);
        if(destination==null){
            Toast.makeText(this, "Services are not available in your City", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            boolean flag=distanceValid(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()),
                    new LatLng(destination.latitude, destination.longitude), 21000);
            if (!flag){
                Toast.makeText(this, "Too far from Railway Station", Toast.LENGTH_SHORT).show();
            }
            return flag;
        }
    }

    private boolean distanceValid(LatLng source, LatLng destination, float dist){
        Location locationA = new Location("");
        locationA.setLatitude(addresses.get(0).getLatitude());
        locationA.setLongitude(addresses.get(0).getLongitude());
        Location locationB = new Location("");
        locationB.setLatitude(destination.latitude);
        locationB.setLongitude(destination.longitude);
        return (locationA.distanceTo(locationB))<dist;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int height, int width) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, width, height);
        }
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (vectorDrawable != null) {
            vectorDrawable.draw(canvas);
        }
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private String getLowerTimestamp(HashMap<String, String> timestamps) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String lowestTimestamp = null;
        try {
            Date lowestDate = null;
            for (String timestamp : timestamps.values()) {
                Date currentDate = format.parse(timestamp);
                if (lowestDate == null || currentDate.before(lowestDate)) {
                    lowestDate = currentDate;
                    lowestTimestamp = timestamp;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // Return a default value or throw an exception, depending on your requirement
        }

        String key=null;
        for (Map.Entry<String, String> entry : timestamps.entrySet()) {
            if (Objects.requireNonNull(lowestTimestamp).equals(entry.getValue())) {
                key= entry.getKey();
            }
        }
        return key;
    }

    private String[] dissectSequence(CharSequence sequence) {
        StringBuilder digits = new StringBuilder();
        StringBuilder characters = new StringBuilder();

        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);

            if (Character.isDigit(c)) {
                digits.append(c);
            } else {
                characters.append(c);
            }
        }

        String[] dissectedValues = new String[2];
        dissectedValues[0] = digits.toString();
        dissectedValues[1] = characters.toString();
        return dissectedValues;
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId=item.getItemId();

        if(itemId==R.id.signOut){
            showToast("Signing Out.......");
            FirebaseFirestore database=FirebaseFirestore.getInstance();
            DocumentReference documentReference=
                    database.collection(Constants.KEY_COLLECTION_DELIVERY).document(
                            preferenceManager.getString(Constants.KEY_DELIVERY_ID)
                    );
            HashMap<String, Object> updates=new HashMap<>();
            documentReference.update(updates)
                    .addOnSuccessListener(unused->{
                        preferenceManager.clear();
                        startActivity(new Intent(getApplicationContext(), DeliverySignIn.class));
                        finish();
                    })
                    .addOnFailureListener(e->showToast("Unable to Sign Out"));
        }
        else if (itemId==R.id.aboutUs) {
            Toast.makeText(this, "Team TrainFoodDelivery", Toast.LENGTH_SHORT).show();
        }
        else if (itemId==R.id.home) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}