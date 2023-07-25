package com.harsh.trainfooddelivery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorStateListDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.activities.RestaurantHomePage;
import com.harsh.trainfooddelivery.databinding.RestaurantFoodContainerBinding;
import com.harsh.trainfooddelivery.listeners.MenuListener;
import com.harsh.trainfooddelivery.models.Menu;
import com.harsh.trainfooddelivery.utilities.Constants;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder>{
    private final List<com.harsh.trainfooddelivery.models.Menu> menuList;
    private final MenuListener menuListener;
    private Context context;
    public MenuAdapter(List<Menu> menuList, MenuListener menuListener){
        this.menuList = menuList;
        this.menuListener = menuListener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RestaurantFoodContainerBinding foodContainerBinding=RestaurantFoodContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        context=parent.getContext();
        return new MenuViewHolder(foodContainerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.setData(menuList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    class MenuViewHolder extends RecyclerView.ViewHolder {
        RestaurantFoodContainerBinding binding;
        MenuViewHolder(RestaurantFoodContainerBinding foodContainerBinding){
            super(foodContainerBinding.getRoot());
            binding=foodContainerBinding;
        }
        @SuppressLint("SetTextI18n")
        void setData(Menu menu, int position){
            binding.foodName.setText(menu.name);
            if (menu.foodType.equals("Vegetarian")){
                binding.vegNonVeg.setText(" Veg");
                binding.foodTypeImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green)));
            }
            else{
                binding.vegNonVeg.setText(" Non-Veg");
                binding.foodTypeImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error)));
            }
            binding.price.setText("₹"+menu.price);
            binding.quantity.setText("Qty: "+menu.quantity);
            binding.Serving.setText("Serves: "+menu.serving);
            binding.imageFood.setImageBitmap(getUserImage(menu.image));
            binding.editLayout.setOnClickListener(v->{
                menuListener.onEditClicked(menu);
            });
            binding.deleteLayout.setOnClickListener(v->{
                menuListener.onDeleteClicked(menu, position);
            });
            binding.availabilityLayout.setOnClickListener(v->{
                onAvailabilityClicked(menu);
            });
        }

        @SuppressLint("SetTextI18n")
        private void onAvailabilityClicked(Menu menu){
            String avail;
            if(menu.availability.equals("1")){
                avail="0";
            }
            else{
                avail="1";
            }
            FirebaseFirestore database=FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_MENU)
                    .document(menu.id)
                    .update(Constants.KEY_FOOD_AVAILABILITY, avail)
                    .addOnSuccessListener(v->{
                        if(menu.availability.equals("1")){
                            menu.availability="0";
                            binding.availability.setImageResource(R.drawable.ic_unavailable);
                        binding.availability.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error)));
                            binding.availabilityText.setText("Unavailable");
                        }
                        else {
                            menu.availability="1";
                            binding.availability.setImageResource(R.drawable.ic_available);
                            binding.availability.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green)));
                            binding.availabilityText.setText("Available");
                        }
                    })
                    .addOnFailureListener(v->{
                        Toast.makeText(context, "Unable to change", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private Bitmap getUserImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}