package com.harsh.trainfooddelivery.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
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

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.adapters.PassengerMenuAdapter;
import com.harsh.trainfooddelivery.databinding.PassengerRestaurantMenuActivityBinding;
import com.harsh.trainfooddelivery.listeners.MenuListener;
import com.harsh.trainfooddelivery.models.Menu;
import com.harsh.trainfooddelivery.models.Order;
import com.harsh.trainfooddelivery.models.Restaurant;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class PassengerRestaurantMenuActivity extends AppCompatActivity implements MenuListener {
    private PassengerRestaurantMenuActivityBinding binding;
    private PreferenceManager preferenceManager;
    private Intent intent;
    private Restaurant restaurant;
    private List<Menu> menuList;
    private PassengerMenuAdapter passengerMenuAdapter;
    private boolean flag;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=PassengerRestaurantMenuActivityBinding.inflate(getLayoutInflater());
        preferenceManager=new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        intent=getIntent();
        restaurant= (Restaurant) intent.getSerializableExtra("Restaurant");
        menuList= new ArrayList<>();
        passengerMenuAdapter=new PassengerMenuAdapter(menuList, this);

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
        binding.restaurantRecyclerView.setAdapter(passengerMenuAdapter);
        binding.restaurantRecyclerView.setVisibility(View.VISIBLE);
        listener();
        updateMenu();
    }

    private void listener(){
        binding.confirmOrder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary_text)));
        binding.confirmOrder.setOnClickListener(v->{
            if (flag) {
                placeOrder();
            }
        });

    }

    private void updateMenu(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_MENU)
                .whereEqualTo(Constants.KEY_RESTAURANT_ID, restaurant.id)
                .whereEqualTo(Constants.KEY_FOOD_AVAILABILITY, "1")
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener= (value, error)->{
        if(error!=null){
            loading(false);
            showErrorMessage();
        }
        if(value!=null){
            loading(false);
            for(DocumentChange documentChange: value.getDocumentChanges()){
                if(documentChange.getType()==DocumentChange.Type.ADDED){
                    Menu menu1=new Menu();
                    menu1.name=documentChange.getDocument().getString(Constants.KEY_FOOD_NAME);
                    menu1.price=documentChange.getDocument().getString(Constants.KEY_FOOD_PRICE);
                    menu1.image=documentChange.getDocument().getString(Constants.KEY_FOOD_IMAGE);
                    menu1.foodType=documentChange.getDocument().getString(Constants.KEY_FOOD_TYPE);
                    menu1.serving=documentChange.getDocument().getString(Constants.KEY_FOOD_SERVING);
                    menu1.quantity=documentChange.getDocument().getString(Constants.KEY_FOOD_QUANTITY);
                    menu1.availability=documentChange.getDocument().getString(Constants.KEY_FOOD_AVAILABILITY);
                    menu1.count=0;
                    menu1.id=documentChange.getDocument().getId();
                    menuList.add(menu1);
                }
                else if (documentChange.getType()== DocumentChange.Type.MODIFIED){
                    String id=documentChange.getDocument().getId();
                    for (int i=0; i<menuList.size(); i++){
                        if(Objects.equals(menuList.get(i).id, id)){
                            menuList.get(i).name=documentChange.getDocument().getString(Constants.KEY_FOOD_NAME);
                            menuList.get(i).price=documentChange.getDocument().getString(Constants.KEY_FOOD_PRICE);
                            menuList.get(i).image=documentChange.getDocument().getString(Constants.KEY_FOOD_IMAGE);
                            menuList.get(i).foodType=documentChange.getDocument().getString(Constants.KEY_FOOD_TYPE);
                            menuList.get(i).serving=documentChange.getDocument().getString(Constants.KEY_FOOD_SERVING);
                            menuList.get(i).quantity=documentChange.getDocument().getString(Constants.KEY_FOOD_QUANTITY);
                            menuList.get(i).availability=documentChange.getDocument().getString(Constants.KEY_FOOD_AVAILABILITY);
                            menuList.get(i).count=0;
                        }
                    }
                }
                else if (documentChange.getType()== DocumentChange.Type.REMOVED){
                    String id=documentChange.getDocument().getId();
                    menuList.removeIf(menu1 -> menu1.id.equals(id));
                }
            }
            passengerMenuAdapter.notifyDataSetChanged();
            binding.restaurantRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "No food Item available"));
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

    @Override
    public void onEditClicked(Menu menu) {
        //No Action Required
    }

    @Override
    public void onDeleteClicked(Menu menu, int position) {
        //No Action Required
    }

    @Override
    public void onCount(int count, int position) {
        Menu newMenu=menuList.get(position);
        newMenu.count= count;
        menuList.set(position, newMenu);
        flag=check();
    }
    private boolean check(){
        int check = 0;
        for(Menu menu: menuList){
            if (menu.count!=0){
                check++;
            }
        }
        if (check>0){
            binding.confirmOrder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.passenger)));
            return true;
        }
        else {
            binding.confirmOrder.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary_text)));
            return false;
        }
    }

    private void placeOrder(){
        loading(true);
        HashMap <String, Object> map=new HashMap<>();
        Order order=new Order();
        double amount=0;
        int i=0;
        String timestamp=getCurrentDateTimeAsString();
        String otp=String.valueOf(generateRandomNumber());
        map.put(Constants.KEY_ORDER_PASSENGER_ID, preferenceManager.getString(Constants.KEY_PASSENGER_ID));
        map.put(Constants.KEY_RESTAURANT_ID, restaurant.id);
        map.put(Constants.KEY_ORDER_STATUS, Constants.KEY_PENDING_STATUS);
        map.put(Constants.KEY_ORDER_TIMESTAMP, timestamp);
        map.put(Constants.KEY_ORDER_CITY, restaurant.city);
        map.put(Constants.KEY_ORDER_ETA, preferenceManager.getString(Constants.KEY_PASSENGER_ETA));

        order.passengerId=preferenceManager.getString(Constants.KEY_PASSENGER_ID);
        order.orderStatus=Constants.KEY_PENDING_STATUS;
        order.restaurantId=restaurant.id;
        order.timestamp=timestamp;
        try {
            map.put(Constants.KEY_ORDER_OTP, AESCrypt.encrypt("frmeihafokfso", otp));
            map.put(Constants.KEY_RESTAURANT_OTP, AESCrypt.encrypt("frmeihafokfso", String.valueOf(generateRandomNumber())));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        order.foodItem= new ArrayList<String>();
        for (Menu menu: menuList){
            if (menu.count>0){
                i++;
                map.put(Constants.KEY_ORDER_FOOD_ITEM+i, menu.count+menu.name);
                order.foodItem.add(menu.count+menu.name);
                amount= (amount+(menu.count*Double.parseDouble(menu.price)));
            }
        }
        map.put(Constants.KEY_ORDER_AMOUNT, String.valueOf(amount));

        order.orderOtp=otp;
        order.amount=String.valueOf(amount);

        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ORDER)
                .add(map)
                .addOnSuccessListener(v->{
                    loading(false);
                    preferenceManager.putString(Constants.KEY_ORDER_ID, v.getId());
                    order.id=v.getId();
                    Intent intent1=new Intent(getApplicationContext(), PassengerOrderActivity.class);
                    preferenceManager.putString(Constants.KEY_PASSENGER_RESTAURANT_ADDRESS, restaurant.address);
                    preferenceManager.putString(Constants.KEY_PASSENGER_ORDER_ID, order.id);
                    preferenceManager.putString(Constants.KEY_PASSENGER_RESTAURANT_NAME, restaurant.name);
                    preferenceManager.putString(Constants.KEY_PASSENGER_RESTAURANT_NUMBER, restaurant.phoneNumber);
                    startActivity(intent1);
                })
                .addOnFailureListener(v->{

                });
    }

    public static int generateRandomNumber() {
        int min = 100000; // Minimum 6-digit number
        int max = 999999; // Maximum 6-digit number

        Random random = new Random();

        return random.nextInt(max - min + 1) + min;
    }

    public static String getCurrentDateTimeAsString() {
        // Set the desired date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Get the current date and time
        Date currentDate = new Date();

        // Format the date and time as string

        return dateFormat.format(currentDate);
    }


    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
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
