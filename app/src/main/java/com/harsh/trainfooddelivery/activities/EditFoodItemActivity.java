package com.harsh.trainfooddelivery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.harsh.trainfooddelivery.databinding.AddfooditemBinding;
import com.harsh.trainfooddelivery.databinding.EditfooditemBinding;
import com.harsh.trainfooddelivery.models.Menu;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class EditFoodItemActivity extends AppCompatActivity {
    private EditfooditemBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private ArrayAdapter<String> adapter2;
    private ArrayAdapter<String> adapter;
    private Menu originalMenu;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=EditfooditemBinding.inflate(getLayoutInflater());
        preferenceManager=new PreferenceManager(getApplicationContext());
        Intent intent = getIntent();
        originalMenu= (Menu) intent.getSerializableExtra("Menu");
        setContentView(binding.getRoot());
        listener();
        setData();
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
    private void listener(){
        spinner();
        binding.Save.setOnClickListener(v->{
            if (isValidSignUpDetails()){
                updateItem();
            }
        });
        binding.foodImage.setOnClickListener(v->{
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private boolean isValidSignUpDetails(){
        if(binding.foodName.getText().toString().trim().isEmpty()){
            showToast("Enter your Food Item's Name");
            return false;
        }
        else if(binding.price.getText().toString().trim().isEmpty()){
            showToast("Enter price of Food Item");
            return false;
        }
        else if(binding.quantity.getText().toString().trim().isEmpty()){
            showToast("Enter food quantity");
            return false;
        }
        else if(binding.Serving.getText().toString().trim().isEmpty()){
            showToast("Enter number of Servings");
            return false;
        }
        else{
            return true;
        }
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.Save.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.Save.setVisibility(View.VISIBLE);
        }
    }
    private void updateItem(){
        loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String, Object> map=new HashMap<>();
        map.put(Constants.KEY_RESTAURANT_ID, preferenceManager.getString(Constants.KEY_RESTAURANT_ID));
        map.put(Constants.KEY_FOOD_NAME, binding.foodName.getText().toString());
        if (encodedImage!=null){
            map.put(Constants.KEY_FOOD_IMAGE, encodedImage);
        }
        map.put(Constants.KEY_FOOD_PRICE, binding.price.getText().toString());
        map.put(Constants.KEY_FOOD_TYPE, binding.foodType.getSelectedItem().toString());
        map.put(Constants.KEY_FOOD_QUANTITY, binding.quantity.getText().toString()+binding.quantityType.getSelectedItem().toString());
        map.put(Constants.KEY_FOOD_SERVING, binding.Serving.getText().toString());
        map.put(Constants.KEY_FOOD_AVAILABILITY, "1");
        database.collection(Constants.KEY_COLLECTION_MENU)
                .document(originalMenu.id)
                .update(map)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    showToast("Item updated successfully to the Menu");
                    Intent freshIntent=new Intent(getApplicationContext(), RestaurantHomePage.class);
                    freshIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(freshIntent);
                })
                .addOnFailureListener(exception->{
                    loading(false);
                    showToast(exception.getMessage());
                });
    }
    private void spinner(){
        ArrayList<String> spinner = new ArrayList<>();
        spinner.add("Vegetarian");
        spinner.add("Non-Vegetarian");
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinner);
        binding.foodType.setAdapter(adapter);

        ArrayList<String> spinner2 = new ArrayList<>();
        spinner2.add("ml");
        spinner2.add("gm");
        spinner2.add("pc");
        adapter2=new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinner2);
        binding.quantityType.setAdapter(adapter2);
    }
    private void setData(){
        binding.foodName.setText(originalMenu.name);
        binding.price.setText(originalMenu.price);
        binding.quantity.setText(originalMenu.quantity.substring(0, originalMenu.quantity.length() - 2));
        binding.quantityType.setSelection(adapter2.getPosition(originalMenu.quantity.substring(originalMenu.quantity.length()-2)));
        byte [] bytes= Base64.decode(originalMenu.image, Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        binding.textAddImage.setVisibility(View.GONE);
        binding.Serving.setText(originalMenu.serving);
        binding.foodType.setSelection(adapter.getPosition(originalMenu.foodType));
    }
}
