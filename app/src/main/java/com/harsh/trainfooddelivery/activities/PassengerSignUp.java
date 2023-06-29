package com.harsh.trainfooddelivery.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.harsh.trainfooddelivery.databinding.PassengerSignUpBinding;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PassengerSignUp extends AppCompatActivity {
    private PassengerSignUpBinding binding;
    PreferenceManager preferenceManager;
    private String verificationId;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=PassengerSignUpBinding.inflate(getLayoutInflater());
        preferenceManager=new PreferenceManager(getApplicationContext());
        mAuth= FirebaseAuth.getInstance();
        setContentView(binding.getRoot());
        listener();
    }
    private void listener(){
        spinner();
        binding.getOTP.setOnClickListener(v->{
            if(isValidSignUpDetails()){
                sendVerificationCode("+91"+binding.inputNumber.getText().toString());
            }
        });
        binding.signUp.setOnClickListener(v->{
            if(binding.inputOTP.getText().toString().trim().isEmpty()){
                showToast("Enter OTP");
            }
            else if(binding.inputOTP.getText().toString().length()!=6){
                showToast("Enter valid OTP");
            }
            else{
                verifyCode(binding.inputOTP.getText().toString());
            }
        });
        binding.inputETATime.setOnClickListener(v->{
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        binding.inputETATime.setText( selectedHour + ":" + selectedMinute+":"+"00");
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Expected Time of Arrival");
                mTimePicker.show();
        });
    }
    private void signUp(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String, Object> map=new HashMap<>();
        map.put(Constants.KEY_PASSENGER_NAME, binding.passengerName.getText().toString());
        map.put(Constants.KEY_PASSENGER_TRAIN_NUMBER, binding.inputTrainNumber.getText().toString());
        map.put(Constants.KEY_PASSENGER_TRAIN_NAME, binding.inputTrainName.getText().toString());
        map.put(Constants.KEY_PASSENGER_PNR_NUMBER, binding.inputPNRNumber.getText().toString());
        map.put(Constants.KEY_PASSENGER_STATION, binding.dropdownMenu.getSelectedItem().toString());
        map.put(Constants.KEY_PASSENGER_ETA, binding.inputETATime.getText().toString());
        map.put(Constants.KEY_PASSENGER_COACH_NUMBER, binding.inputCoach.getText().toString());
        map.put(Constants.KEY_PASSENGER_SEAT_NUMBER, binding.inputSeatNumber.getText().toString());
        try {
            map.put(Constants.KEY_PASSENGER_PHONE_NUMBER, AESCrypt.encrypt("frmeihafokfso", "+91"+binding.inputNumber.getText().toString()));
        } catch (GeneralSecurityException e) {
            showToast("Encryption Error");
        }
        database.collection(Constants.KEY_PASSENGER_COLLECTION)
                .add(map)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_PASSENGER_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_PASSENGER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_PASSENGER_NAME, binding.passengerName.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSENGER_PHONE_NUMBER, "+91"+binding.inputNumber.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSENGER_STATION, binding.dropdownMenu.getSelectedItem().toString());
                    preferenceManager.putString(Constants.KEY_PASSENGER_TRAIN_NUMBER, binding.inputTrainNumber.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSENGER_TRAIN_NAME, binding.inputTrainName.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSENGER_PNR_NUMBER, binding.inputPNRNumber.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSENGER_ETA, binding.inputETATime.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSENGER_COACH_NUMBER, binding.inputCoach.getText().toString());
                    preferenceManager.putString(Constants.KEY_PASSENGER_SEAT_NUMBER, binding.inputSeatNumber.getText().toString());

                    Intent freshIntent=new Intent(getApplicationContext(), PassengerHomePage.class);
                    freshIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(freshIntent);
                })
                .addOnFailureListener(exception->{
                    loading(false);
                    showToast(exception.getMessage());
                });
    }
    private void spinner(){
        ArrayList<String> spinner = new ArrayList<>();
        spinner.add("Ahmedabad Junction (ADI)");
        spinner.add("Vadodara Junction (BRC)");
        spinner.add("Surat (ST)");
        spinner.add("Rajkot Junction (RJT)");
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinner);
        binding.dropdownMenu.setAdapter(adapter);
    }

    private void sendVerificationCode(String phoneNumber){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override

        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            final String code=phoneAuthCredential.getSmsCode();
            if(code!=null){
                binding.inputOTP.setText(code);
            }
            verifyCode(code);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            showToast(e.getMessage());
        }
    };
    private void verifyCode(String code){
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }
    private void signInWithCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        signUp();
                    }
                    else{
                        showToast(Objects.requireNonNull(task.getException()).getMessage());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            showToast("The verification code entered was invalid");
                        }
                    }
                });
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.signUp.setVisibility(View.INVISIBLE);
            binding.progressBar2.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar2.setVisibility(View.INVISIBLE);
            binding.signUp.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidSignUpDetails(){
        if(binding.passengerName.getText().toString().trim().isEmpty()){
            showToast("Enter your Passenger's Name");
            return false;
        }
        else if(binding.inputTrainNumber.getText().toString().trim().isEmpty()){
            showToast("Enter your Train Number");
            return false;
        }
        else if(binding.inputTrainNumber.getText().toString().length()!=5){
            showToast("Enter valid Train Number");
            return false;
        }
        else if(binding.inputTrainName.getText().toString().trim().isEmpty()){
            showToast("Enter your Train Name");
            return false;
        }
        else if(binding.inputPNRNumber.getText().toString().trim().isEmpty()){
            showToast("Enter your PNR Number");
            return false;
        }
        else if (binding.inputPNRNumber.getText().toString().length()!=10) {
            showToast("PNR must be of 10-digits");
            return false;
        }
        else if(binding.inputETATime.getText().toString().trim().isEmpty()){
            showToast("Enter Expected Time of Arrival of Train");
            return false;
        }
        else if(binding.inputCoach.getText().toString().trim().isEmpty()){
            showToast("Enter your Coach Number");
            return false;
        }
        else if (binding.inputCoach.getText().toString().length()>3){
            showToast("Coach Number cannot be greater than 3 characters");
            return false;
        }
        else if (!(binding.inputCoach.getText().toString().charAt(0)>='A' && binding.inputCoach.getText().toString().charAt(0)<='Z')) {
            showToast("First character must be a Capital Alphabet");
            return false;
        }
        else if (!(binding.inputCoach.getText().toString().charAt(1)>='1' && binding.inputCoach.getText().toString().charAt(1)<='9')) {
            showToast("Second character must be a digit");
            return false;
        }
        else if(binding.inputCoach.getText().toString().length()==3 && !(binding.inputCoach.getText().toString().charAt(2)>='1' && binding.inputCoach.getText().toString().charAt(2)<='9')){
                showToast("Third character must be a digit");
                return false;
        }
        else if(binding.inputSeatNumber.getText().toString().trim().isEmpty()){
            showToast("Enter your Seat Number");
            return false;
        }
        else if(Integer.parseInt(binding.inputSeatNumber.getText().toString())>180){
            showToast("Enter valid Seat Number");
            return false;
        }
        else if(binding.inputNumber.getText().toString().trim().isEmpty()){
            showToast("Enter Phone Number");
            return false;
        }
        else if(!Patterns.PHONE.matcher(binding.inputNumber.getText().toString()).matches()){
            showToast("Enter valid phone number");
            return false;
        }
        else if ((binding.inputNumber.getText().toString().length())!=10){
            showToast("Phone Number must be of 10 digits");
            return false;
        }
        else{
            return true;
        }
    }
}
