package com.harsh.trainfooddelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.adapters.RestaurantAdapter;
import com.harsh.trainfooddelivery.databinding.PassenegerHomePageBinding;
import com.harsh.trainfooddelivery.listeners.RestaurantListener;
import com.harsh.trainfooddelivery.models.Restaurant;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PassengerHomePage extends AppCompatActivity implements RestaurantListener {
    private PassenegerHomePageBinding binding;
    private PreferenceManager preferenceManager;
    private List<Restaurant> restaurantList;
    private Toolbar toolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=PassenegerHomePageBinding.inflate(getLayoutInflater());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());

        toolbar=binding.toolbar;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.grey));
            toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.grey));
            actionBar.setTitle("Welcome, "+preferenceManager.getString(Constants.KEY_PASSENGER_NAME));
            actionBar.setSubtitle(preferenceManager.getString(Constants.KEY_PASSENGER_STATION));
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        binding.tab.addTab(binding.tab.newTab().setText("   Available Restaurants   "));
        getRestaurants();
    }

    private void getRestaurants(){
        loading(true);
        String[] str=preferenceManager.getString(Constants.KEY_PASSENGER_STATION).split("\\s+");
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(str[0])
                .get()
                .addOnCompleteListener(v->{
                    loading(false);
                    if(v.isSuccessful() && v.getResult()!=null){
                        restaurantList=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot: v.getResult()){
                            Restaurant restaurant=new Restaurant();
                            restaurant.name=queryDocumentSnapshot.getString(Constants.KEY_RESTAURANT_NAME);
                            restaurant.image=queryDocumentSnapshot.getString(Constants.KEY_RESTAURANT_IMAGE);
                            restaurant.restaurateurName=queryDocumentSnapshot.getString(Constants.KEY_RESTAURATEUR_NAME);
                            restaurant.lat=queryDocumentSnapshot.getDouble(Constants.KEY_RESTAURANT_LATITUDE);
                            restaurant.lon=queryDocumentSnapshot.getDouble(Constants.KEY_RESTAURANT_LONGITUDE);
                            restaurant.address=queryDocumentSnapshot.getString(Constants.KEY_RESTAURANT_ADDRESS);
                            restaurant.city=queryDocumentSnapshot.getString(Constants.KEY_RESTAURANT_CITY);
                            restaurant.postalCode=queryDocumentSnapshot.getString(Constants.KEY_RESTAURANT_POSTAL_CODE);
                            restaurant.duration=queryDocumentSnapshot.getDouble(Constants.KEY_RESTAURANT_DURATION);
                            restaurant.id=queryDocumentSnapshot.getId();
                            try {
                                restaurant.phoneNumber= AESCrypt.decrypt("frmeihafokfso", queryDocumentSnapshot.getString(Constants.KEY_RESTAURANT_PHONE_NUMBER));
                            } catch (GeneralSecurityException e) {
                                showToast("Encryption Error");
                            }
                            restaurantList.add(restaurant);
                        }
                        if (restaurantList.size()>0){
                            RestaurantAdapter restaurantAdapter=new RestaurantAdapter(restaurantList, this);
                            binding.restaurantRecyclerView.setAdapter(restaurantAdapter);
                            binding.restaurantRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else {
                            showErrorMessage();
                        }
                    }
                    else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "Currently, No Restaurants are Available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRestaurantClicked(Restaurant restaurant) {
        Intent intent=new Intent(getApplicationContext(), PassengerRestaurantMenuActivity.class);
        intent.putExtra("Restaurant", restaurant);
        startActivity(intent);
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
                    database.collection(Constants.KEY_PASSENGER_COLLECTION).document(
                            preferenceManager.getString(Constants.KEY_PASSENGER_ID)
                    );
            HashMap<String, Object> updates=new HashMap<>();
            documentReference.update(updates)
                    .addOnSuccessListener(unused->{
                        preferenceManager.clear();
                        startActivity(new Intent(getApplicationContext(), PassengerSignUp.class));
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
