package com.harsh.trainfooddelivery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.harsh.trainfooddelivery.databinding.DeliverySignUpBinding;
import com.harsh.trainfooddelivery.databinding.RestaurantSignUpBinding;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeliverySignUp extends AppCompatActivity {
    private DeliverySignUpBinding binding;
    private String verificationId;
    private FirebaseAuth mAuth;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DeliverySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth= FirebaseAuth.getInstance();
        preferenceManager=new PreferenceManager(getApplicationContext());
        intent=getIntent();
        binding.inputAddress.setText(intent.getStringExtra("address"));
        binding.inputAddress.setSelected(true);
        binding.inputPinCode.setText(intent.getStringExtra("PostalCode"));
        listeners();
    }
    private void listeners(){
        binding.textSignIn.setOnClickListener(v -> {
            Intent intent1=new Intent(getApplicationContext(), DeliverySignIn.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        });
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
        binding.layoutImage.setOnClickListener(v->{
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private final ActivityResultLauncher<Intent> pickImage=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result->{
                if(result.getResultCode()==RESULT_OK){
                    if(result.getData()!=null){
                        Uri imageUri=result.getData().getData();
                        try{
                            InputStream inputStream=getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage=encodeImage(bitmap);
                        }
                        catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte [] bytes= byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private boolean isValidSignUpDetails(){
        if(encodedImage==null){
            showToast("Select Profile Image");
            return false;
        }
        else if(binding.name.getText().toString().trim().isEmpty()){
            showToast("Enter your name");
            return false;
        }
        else if(binding.inputLicenceNumber.getText().toString().trim().isEmpty()){
            showToast("Enter your Driving Licence Number");
            return false;
        }
        else if(binding.inputVehicleNumber.getText().toString().trim().isEmpty()){
            showToast("Enter your Vehicle Number");
            return false;
        }
//        else if(!validateLicenseNumber(binding.inputLicenceNumber.getText().toString())){
//            showToast("Enter valid Driving Licence Number");
//            return false;
//        }
//        else if(!validateLicensePlate(binding.inputVehicleNumber.getText().toString())){
//            showToast("Enter valid Driving Licence Number");
//            return false;
//        }
        else if(binding.inputRCNumber.getText().toString().trim().isEmpty()){
            showToast("Enter your RC Number");
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
        else if (binding.inputPassword.getText().toString().trim().isEmpty())
        {
            showToast("Enter Password");
            return false;
        }
        else if(!isPasswordValid()){
            showToast("Enter valid Password");
            return false;
        }
        else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm your Password");
            return false;
        }
        else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString()))
        {
            showToast("Password and Confirm Password must be same");
            return false;
        }
        else{
            return true;
        }
    }
    private boolean isPasswordValid(){
        String regex = "^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])" + "(?=.*[*@#$%^&+=])" + "(?=\\S+$).{8,20}$";
        Pattern p=Pattern.compile(regex);
        Matcher m=p.matcher(binding.inputPassword.getText().toString());
        return m.matches();
    }

    private boolean validateLicenseNumber(String licenseNumber) {
        // Regular expression pattern for Indian driving license number
        String regex = "^[A-Z]{2}\\d{13}$";

        // Create a pattern object and compile the regular expression
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher object and match the license number
        Matcher matcher = pattern.matcher(licenseNumber);

        // Return true if the license number matches the pattern, otherwise false
        return matcher.matches();
    }


    private boolean validateLicensePlate(String licensePlate) {
        // Regular expression pattern for Indian license plate
        String regex = "^[A-Z]{2}\\d{1,2}[A-Z]{1,3}\\d{1,4}$";

        // Create a pattern object and compile the regular expression
        Pattern pattern = Pattern.compile(regex);

        // Create a matcher object and match the license plate
        Matcher matcher = pattern.matcher(licensePlate);

        // Return true if the license plate matches the pattern, otherwise false
        return matcher.matches();
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
    private void signUp(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String, Object> map=new HashMap<>();
        map.put(Constants.KEY_DELIVERY_NAME, binding.name.getText().toString());
        map.put(Constants.KEY_RESTAURANT_IMAGE, encodedImage);
        map.put(Constants.KEY_DELIVERY_NUMBER_PLATE, binding.inputVehicleNumber.getText().toString());
        map.put(Constants.KEY_DELIVERY_CITY, intent.getStringExtra("city"));
        map.put(Constants.KEY_DELIVERY_POSTAL_CODE, intent.getStringExtra("PostalCode"));
        map.put(Constants.KEY_DELIVERY_LATITUDE, intent.getDoubleExtra("latitude", 0));
        map.put(Constants.KEY_DELIVERY_LONGITUDE, intent.getDoubleExtra("longitude", 0));
        map.put(Constants.KEY_DELIVERY_ADDRESS, intent.getStringExtra("address"));
        try {
            map.put(Constants.KEY_DELIVERY_PHONE_NUMBER, AESCrypt.encrypt("frmeihafokfso", "+91"+binding.inputNumber.getText().toString()));
            map.put(Constants.KEY_DELIVERY_PASSWORD, AESCrypt.encrypt("frmeihafokfso", binding.inputPassword.getText().toString()));
            map.put(Constants.KEY_DELIVERY_LICENCE, AESCrypt.encrypt("frmeihafokfso",binding.inputLicenceNumber.getText().toString()));
            map.put(Constants.KEY_DELIVERY_RC_BOOK, AESCrypt.encrypt("frmeihafokfso",binding.inputRCNumber.getText().toString()));
        } catch (GeneralSecurityException e) {
            showToast("Encryption Error");
        }
        database.collection(Constants.KEY_COLLECTION_DELIVERY)
                .add(map)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_DELIVERY_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_DELIVERY_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_DELIVERY_NAME, binding.name.getText().toString());
                    preferenceManager.putString(Constants.KEY_DELIVERY_IMAGE, encodedImage);
                    preferenceManager.putString(Constants.KEY_DELIVERY_PHONE_NUMBER, "+91"+binding.inputNumber.getText().toString());
                    preferenceManager.putString(Constants.KEY_DELIVERY_CITY, intent.getStringExtra("city"));
                    preferenceManager.putString(Constants.KEY_DELIVERY_POSTAL_CODE, intent.getStringExtra("PostalCode"));

                    Intent freshIntent=new Intent(getApplicationContext(), DeliveryHomeActivity.class);
                    freshIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(freshIntent);
                })
                .addOnFailureListener(exception->{
                    loading(false);
                    showToast(exception.getMessage());
                });
    }
}
