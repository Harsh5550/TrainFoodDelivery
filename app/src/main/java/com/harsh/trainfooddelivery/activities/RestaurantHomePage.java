package com.harsh.trainfooddelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.adapters.ViewPagerRestaurantAdapter;
import com.harsh.trainfooddelivery.databinding.RestaurantHomePageBinding;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;

import java.util.HashMap;

public class RestaurantHomePage extends AppCompatActivity {
    private RestaurantHomePageBinding binding;
    private PreferenceManager preferenceManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=RestaurantHomePageBinding.inflate(getLayoutInflater());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());

        toolbar=binding.toolbar;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.grey));
            toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.grey));
            actionBar.setTitle("Welcome, "+preferenceManager.getString(Constants.KEY_RESTAURATEUR_NAME));
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setSubtitle(preferenceManager.getString(Constants.KEY_RESTAURANT_NAME)+", "+preferenceManager.getString(Constants.KEY_RESTAURANT_CITY));
        }
        ViewPagerRestaurantAdapter adapter=new ViewPagerRestaurantAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(adapter);
        binding.tab.setupWithViewPager(binding.viewPager);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
                    database.collection(preferenceManager.getString(Constants.KEY_RESTAURANT_CITY)).document(
                            preferenceManager.getString(Constants.KEY_RESTAURANT_ID)
                    );
            HashMap<String, Object> updates=new HashMap<>();
            documentReference.update(updates)
                    .addOnSuccessListener(unused->{
                        preferenceManager.clear();
                        startActivity(new Intent(getApplicationContext(), RestaurantSignIn.class));
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
