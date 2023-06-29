package com.harsh.trainfooddelivery.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.databinding.PassengerFoodContainerBinding;
import com.harsh.trainfooddelivery.listeners.MenuListener;
import com.harsh.trainfooddelivery.models.Menu;

import java.util.List;

public class PassengerMenuAdapter extends RecyclerView.Adapter<PassengerMenuAdapter.PassengerMenuViewHolder> {
    private PassengerFoodContainerBinding binding;
    private final List<Menu> menuList;
    private final MenuListener menuListener;
    private int count=0;
    private Context context;

    public PassengerMenuAdapter(List<Menu> menuList, MenuListener menuListener) {
        this.menuList = menuList;
        this.menuListener = menuListener;
    }

    @NonNull
    @Override
    public PassengerMenuAdapter.PassengerMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=PassengerFoodContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        context=parent.getContext();
        return new PassengerMenuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerMenuAdapter.PassengerMenuViewHolder holder, int position) {
        holder.setData(menuList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public class PassengerMenuViewHolder extends RecyclerView.ViewHolder{
        PassengerFoodContainerBinding binding;
        int [] count=new int[menuList.size()];
        public PassengerMenuViewHolder(PassengerFoodContainerBinding passengerFoodContainerBinding) {
            super(passengerFoodContainerBinding.getRoot());
            binding=passengerFoodContainerBinding;
        }

        void setData(Menu menu, int position){
            binding.foodName.setText(menu.name);
            if (menu.foodType.equals("Vegetarian")){
                binding.vegNonVeg.setText(" Veg");
            }
            else{
                binding.vegNonVeg.setText(" Non-Veg");
            }
            binding.price.setText("₹"+menu.price);
            binding.quantity.setText("Qty: "+menu.quantity);
            binding.Serving.setText("Serves: "+menu.serving);
            binding.imageFood.setImageBitmap(getUserImage(menu.image));
            binding.itemCount.setText("0");
            binding.remove.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary_text)));
            binding.add.setOnClickListener(v->{
                if (count[position]<5) {
                    count[position]++;
                    if (count[position] == 5) {
                        binding.add.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary_text)));
                    } else {
                        binding.remove.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.passenger)));
                    }
                    binding.itemCount.setText(String.valueOf(count[position]));
                    menuListener.onCount(count[position], position);
                }
            });
            binding.remove.setOnClickListener(v->{
                if(count[position]>0){
                    count[position]--;
                    if (count[position]==0){
                        binding.remove.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary_text)));
                    }
                    else {
                        binding.add.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.passenger)));
                    }
                    binding.itemCount.setText(String.valueOf(count[position]));
                    menuListener.onCount(count[position], position);
                }
            });
        }
    }
    private Bitmap getUserImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
