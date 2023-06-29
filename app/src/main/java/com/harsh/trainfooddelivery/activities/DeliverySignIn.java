package com.harsh.trainfooddelivery.activities;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.harsh.trainfooddelivery.databinding.DeliverySignInBinding;
import com.harsh.trainfooddelivery.databinding.RestaurantSignInBinding;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;

public class DeliverySignIn extends AppCompatActivity {
    private DeliverySignInBinding binding;
    private List<Address> addresses;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DeliverySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());
        getLocation();
        listeners();
    }
    private void listeners(){
        binding.signIn.setOnClickListener(v->{
            signIn();
        });
        binding.textForgetPassword.setOnClickListener(v->{
            Intent intent=new Intent(getApplicationContext(), DeliveryForgotPassword.class);
            intent.putExtra("city", addresses.get(0).getLocality());
            startActivity(intent);
        });
        binding.textCreateNewAccount.setOnClickListener(v->{
            Intent intent=new Intent(getApplicationContext(), DeliveryMapsActivity.class);
            intent.putExtra("latitude", addresses.get(0).getLatitude());
            intent.putExtra("longitude", addresses.get(0).getLongitude());
            startActivity(intent);
        });
    }
    @SuppressLint("MissingPermission")
    private void signIn() {
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        try{
            database.collection(Constants.KEY_COLLECTION_DELIVERY)
                    .whereEqualTo(Constants.KEY_DELIVERY_PHONE_NUMBER, AESCrypt.encrypt("frmeihafokfso", ("+91"+binding.inputNumber.getText().toString())))
                    .whereEqualTo(Constants.KEY_DELIVERY_PASSWORD, AESCrypt.encrypt("frmeihafokfso", binding.inputPassword.getText().toString()))
                    .get()
                    .addOnCompleteListener(task->{
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                            preferenceManager.putBoolean(Constants.KEY_DELIVERY_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_DELIVERY_ID, documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_DELIVERY_NAME, documentSnapshot.getString(Constants.KEY_DELIVERY_NAME));
                            preferenceManager.putString(Constants.KEY_DELIVERY_IMAGE, documentSnapshot.getString(Constants.KEY_DELIVERY_IMAGE));
                            try {
                                preferenceManager.putString(Constants.KEY_DELIVERY_PHONE_NUMBER, AESCrypt.decrypt("frmeihafokfso", documentSnapshot.getString(Constants.KEY_DELIVERY_PHONE_NUMBER)));
                            } catch (GeneralSecurityException e) {
                                throw new RuntimeException(e);
                            }
                            preferenceManager.putString(Constants.KEY_DELIVERY_CITY, documentSnapshot.getString(Constants.KEY_DELIVERY_CITY));
                            preferenceManager.putString(Constants.KEY_DELIVERY_POSTAL_CODE, documentSnapshot.getString(Constants.KEY_DELIVERY_POSTAL_CODE));
                            Intent intent = new Intent(getApplicationContext(), DeliveryHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                    }
                    else {
                        loading(false);
                        Toast.makeText(this, "Unable to SIGN IN", Toast.LENGTH_SHORT).show();
                    }
                    })
                    .addOnFailureListener(v->{
                        loading(false);
                        Toast.makeText(this, "Not Registered...Create a New Account", Toast.LENGTH_SHORT).show();
                    });
        }
        catch (GeneralSecurityException e){
            Toast.makeText(this, "Encryption Error", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("MissingPermission")
    private void getLocation(){
        FusedLocationProviderClient fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(DeliverySignIn.this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location!=null){
                            try {
                                Geocoder geocoder = new Geocoder(DeliverySignIn.this, Locale.getDefault());
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.signIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.signIn.setVisibility(View.VISIBLE);
        }
    }
}
