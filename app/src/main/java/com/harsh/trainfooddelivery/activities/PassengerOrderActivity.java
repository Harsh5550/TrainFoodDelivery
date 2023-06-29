package com.harsh.trainfooddelivery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.databinding.PassengerOrderActivityBinding;
import com.harsh.trainfooddelivery.models.Order;
import com.harsh.trainfooddelivery.models.Restaurant;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PassengerOrderActivity extends AppCompatActivity {
    private PassengerOrderActivityBinding binding;
    private PreferenceManager preferenceManager;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=PassengerOrderActivityBinding.inflate(getLayoutInflater());
        preferenceManager=new PreferenceManager(getApplicationContext());
        preferenceManager.putBoolean(Constants.KEY_ORDER_GENERATED, true);
        setContentView(binding.getRoot());
        setOrderDetails();
        updateOrderDetails();
        setListener();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "Cannot go back at this stage", Toast.LENGTH_SHORT).show();
            return true; // Ignore the back button press
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setListener(){

        binding.callRestaurant.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + binding.numberRestaurant.getText().toString()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        binding.callDelivery.setOnClickListener(v->{
            if (!binding.numberDeliveryEx.equals("--")){
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + binding.numberDeliveryEx.getText().toString()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
            else {
                Toast.makeText(this, "Yet to be Assigned", Toast.LENGTH_SHORT).show();
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

    private void updateOrderDetails(){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ORDER)
                .whereEqualTo(Constants.KEY_PASSENGER_ID, preferenceManager.getString(Constants.KEY_PASSENGER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener=(value, error)->{
        if (error==null && value!=null){
            for(DocumentChange documentChange: value.getDocumentChanges()) {
                if (documentChange.getType()== DocumentChange.Type.MODIFIED){
                    String orderStatus=documentChange.getDocument().getString(Constants.KEY_ORDER_STATUS);
                    binding.orderStatusText.setText(orderStatus);
                    if (Objects.equals(orderStatus, Constants.KEY_ACCEPTED_STATUS)){
                        binding.orderStatusImage.setImageResource(R.drawable.confirm_order);
                    } else if (Objects.equals(orderStatus, Constants.KEY_ASSIGNED_STATUS)) {
                        binding.orderStatusImage.setImageResource(R.drawable.confirm_order);
                        FirebaseFirestore database=FirebaseFirestore.getInstance();
                        database.collection(Constants.KEY_COLLECTION_ORDER)
                                .document(preferenceManager.getString(Constants.KEY_PASSENGER_ORDER_ID))
                                .get()
                                .addOnSuccessListener(task->{
                                    if (task!=null) {
                                        if (task.getString(Constants.KEY_ORDER_DELIVERY_ID)!=null){
                                            database.collection(Constants.KEY_COLLECTION_DELIVERY)
                                                    .document(Objects.requireNonNull(task.getString(Constants.KEY_ORDER_DELIVERY_ID)))
                                                    .get()
                                                    .addOnSuccessListener(v->{
                                                        binding.nameDeliveryExecutive.setText(v.getString(Constants.KEY_DELIVERY_NAME));
                                                        binding.numberDeliveryEx.setText(v.getString(Constants.KEY_DELIVERY_PHONE_NUMBER));
                                                    })
                                                    .addOnFailureListener(Throwable::printStackTrace);
                                        }
                                    }
                                })
                                .addOnFailureListener(Throwable::printStackTrace);
                    } else if (Objects.equals(orderStatus, Constants.KEY_OUT_FOR_DELIVERY_STATUS)){
                        binding.orderStatusImage.setImageResource(R.drawable.delivery);
                    }
                    else if (Objects.equals(orderStatus, Constants.KEY_REJECTED_STATUS)){
                        binding.orderStatusImage.setImageResource(R.drawable.rejected_order);
                        redirect(Constants.KEY_REJECTED_STATUS);
                    }
                    else if (Objects.equals(orderStatus, Constants.KEY_DELIVERED_STATUS)){
                        binding.orderStatusImage.setImageResource(R.drawable.order_delivered);
                        redirect(Constants.KEY_DELIVERED_STATUS);
                    }
                }
            }
        }
    };

    private void redirect(String orderStatus){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (orderStatus.equals(Constants.KEY_REJECTED_STATUS)) {
                    Toast.makeText(PassengerOrderActivity.this, "Redirecting in 30 seconds", Toast.LENGTH_SHORT).show();
                    preferenceManager.putBoolean(Constants.KEY_ORDER_GENERATED, false);
                    Intent intent1 = new Intent(getApplicationContext(), PassengerHomePage.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                }
                else {
                    signOut();
                }
            }
        }, 30000);
    }

    private void signOut(){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_PASSENGER_COLLECTION).document(
                        preferenceManager.getString(Constants.KEY_PASSENGER_ID)
                );
        documentReference.delete()
                .addOnSuccessListener(unused->{
                    preferenceManager.clear();
                    Toast.makeText(PassengerOrderActivity.this, "Happy Journey", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent1);
                    finish();
                });
    }

    @SuppressLint("SetTextI18n")
    private void setOrderDetails(){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ORDER)
                .document(preferenceManager.getString(Constants.KEY_PASSENGER_ORDER_ID))
                .get()
                .addOnSuccessListener(order->{
                    if (order!=null) {
                        try {
                            binding.otp.setText("Delivery OTP: "+AESCrypt.decrypt("frmeihafokfso", order.getString(Constants.KEY_ORDER_OTP)));
                        } catch (GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                        binding.orderStatusText.setText(order.getString(Constants.KEY_ORDER_STATUS));
                        binding.orderId.setText(order.getId());
                        binding.date.setText(order.getString(Constants.KEY_ORDER_TIMESTAMP));
                        int sum = 0;
                        int i=1;
                        StringBuilder orderMeal = new StringBuilder("");
                        while (order.getString(Constants.KEY_ORDER_FOOD_ITEM+i)!=null) {
                            String[] dissectedValues;
                            dissectedValues = dissectSequence(Objects.requireNonNull(order.getString(Constants.KEY_ORDER_FOOD_ITEM + i)));
                            orderMeal.append(dissectedValues[0]).append(" X ").append(dissectedValues[1]).append("\n");
                            sum = sum + Integer.parseInt(dissectedValues[0]);
                            i++;
                        }
                        binding.orderedMeals.setText(orderMeal.substring(0, orderMeal.toString().length()-1));
                        binding.totalQuantity.setText(String.valueOf(sum));
                        binding.amount.setText("₹"+ order.getString(Constants.KEY_ORDER_AMOUNT));

                        binding.nameDeveloper.setText("Train Food Delivery");
                        binding.numberSupport.setText("1231231231");
                        binding.nameDeliveryExecutive.setText("Yet to be Assigned");
                        binding.numberDeliveryEx.setText("--");

                        binding.restaurantName.setText(preferenceManager.getString(Constants.KEY_PASSENGER_RESTAURANT_NAME));
                        binding.restaurantAddress.setText(preferenceManager.getString(Constants.KEY_PASSENGER_RESTAURANT_ADDRESS));
                        binding.numberRestaurant.setText(preferenceManager.getString(Constants.KEY_PASSENGER_RESTAURANT_NUMBER));
                    }
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
