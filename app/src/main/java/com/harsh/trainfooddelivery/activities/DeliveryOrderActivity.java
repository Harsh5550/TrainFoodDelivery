package com.harsh.trainfooddelivery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.databinding.DeliveryOrderActivityBinding;
import com.harsh.trainfooddelivery.databinding.PassengerOrderActivityBinding;
import com.harsh.trainfooddelivery.models.Order;
import com.harsh.trainfooddelivery.models.Restaurant;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class DeliveryOrderActivity extends AppCompatActivity {
    private DeliveryOrderActivityBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DeliveryOrderActivityBinding.inflate(getLayoutInflater());
        preferenceManager=new PreferenceManager(getApplicationContext());
        preferenceManager.putBoolean(Constants.KEY_DELIVERY_ORDER_GENERATED, true);
        setContentView(binding.getRoot());
        setOrderDetails();
        setListener();
    }

    private void setListener(){
        binding.confirmButton.setOnClickListener(v->{
            FirebaseFirestore database=FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_ORDER)
                    .document(preferenceManager.getString(Constants.KEY_DELIVERY_ORDER_ID))
                    .get()
                    .addOnSuccessListener(task->{
                        try {
                            if (binding.otp.getText().toString().equals(AESCrypt.decrypt("frmeihafokfso", task.getString(Constants.KEY_ORDER_OTP)))){
                                redirect();
                            }
                            else {
                                Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                            }
                        } catch (GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .addOnFailureListener(Throwable::printStackTrace);
        });

        binding.callPassenger.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + binding.numberPassenger.getText().toString()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        binding.callSupport.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + binding.numberSupport.getText().toString()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "Cannot go back at this stage", Toast.LENGTH_SHORT).show();
            return true; // Ignore the back button press
        }
        return super.onKeyDown(keyCode, event);
    }


    private void redirect(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Toast.makeText(DeliveryOrderActivity.this, "Redirecting in 30 seconds", Toast.LENGTH_SHORT).show();
                preferenceManager.putBoolean(Constants.KEY_DELIVERY_ORDER_GENERATED, false);
                Intent intent1 = new Intent(getApplicationContext(), DeliveryHomeActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent1);
            }
        }, 30000);
    }

    @SuppressLint("SetTextI18n")
    private void setOrderDetails(){
        final String[] restaurantId = new String[1];
        final String[] passengerId = new String[1];
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ORDER)
                .document(preferenceManager.getString(Constants.KEY_DELIVERY_ORDER_ID))
                .get()
                .addOnSuccessListener(task->{
                    restaurantId[0] =task.getString(Constants.KEY_ORDER_RESTAURANT_ID);
                    passengerId[0] =task.getString(Constants.KEY_ORDER_PASSENGER_ID);
                    binding.orderId.setText(task.getId());
                    binding.date.setText(task.getString(Constants.KEY_ORDER_TIMESTAMP));
                    binding.amount.setText("₹"+task.getString(Constants.KEY_ORDER_AMOUNT));
                    int sum = 0;
                    int i=1;
                    StringBuilder orderMeal = new StringBuilder("");
                    while (task.getString(Constants.KEY_ORDER_FOOD_ITEM+i)!=null) {
                        String[] dissectedValues;
                        dissectedValues = dissectSequence(Objects.requireNonNull(task.getString(Constants.KEY_ORDER_FOOD_ITEM + i)));
                        orderMeal.append(dissectedValues[0]).append(" X ").append(dissectedValues[1]).append("\n");
                        sum = sum + Integer.parseInt(dissectedValues[0]);
                        i++;
                    }
                    binding.orderedMeals.setText(orderMeal.substring(0, orderMeal.toString().length()-1));
                    binding.totalQuantity.setText(String.valueOf(sum));
                    binding.nameDeveloper.setText("Train Food Delivery");
                    binding.numberSupport.setText("1231231231");
                })
                .addOnFailureListener(Throwable::printStackTrace);

        database.collection(preferenceManager.getString(Constants.KEY_DELIVERY_CITY))
                .document(restaurantId[0])
                .get()
                .addOnSuccessListener(task->{
                    binding.restaurantName.setText(task.getString(Constants.KEY_RESTAURANT_NAME));
                })
                .addOnFailureListener(Throwable::printStackTrace);

        database.collection(Constants.KEY_PASSENGER_COLLECTION)
                .document(passengerId[0])
                .get()
                .addOnSuccessListener(task->{
                    binding.passengerName.setText(task.getString(Constants.KEY_PASSENGER_NAME));
                    binding.numberPassenger.setText(task.getString(Constants.KEY_PASSENGER_PHONE_NUMBER));
                    binding.stationName.setText(task.getString(Constants.KEY_PASSENGER_STATION));
                    binding.seatDetails.setText("Coach Number: "+task.getString(Constants.KEY_PASSENGER_COACH_NUMBER)+"\nSeat Number: "+task.getString(Constants.KEY_PASSENGER_SEAT_NUMBER));
                    binding.eta.setText(Constants.KEY_PASSENGER_ETA);
                    binding.trainDetails.setText("Train Name: "+task.getString(Constants.KEY_PASSENGER_TRAIN_NAME)+"\nTrain Number: "+task.getString(Constants.KEY_PASSENGER_TRAIN_NUMBER));
                })
                .addOnFailureListener(Throwable::printStackTrace);
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
}
