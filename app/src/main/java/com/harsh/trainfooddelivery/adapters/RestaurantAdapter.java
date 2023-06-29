package com.harsh.trainfooddelivery.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.trainfooddelivery.databinding.PassengerRestaurantContainerBinding;
import com.harsh.trainfooddelivery.listeners.RestaurantListener;
import com.harsh.trainfooddelivery.models.Restaurant;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    private PassengerRestaurantContainerBinding binding;
    private final List<Restaurant> restaurantList;
    private final RestaurantListener restaurantListener;

    public RestaurantAdapter(List<Restaurant> restaurantList, RestaurantListener restaurantListener) {
        this.restaurantList = restaurantList;
        this.restaurantListener = restaurantListener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=PassengerRestaurantContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new RestaurantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.setData(restaurantList.get(position));
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder {
        PassengerRestaurantContainerBinding binding;
        public RestaurantViewHolder(PassengerRestaurantContainerBinding passengerRestaurantContainerBinding) {
            super(passengerRestaurantContainerBinding.getRoot());
            binding=passengerRestaurantContainerBinding;
        }

        void setData(Restaurant restaurant){
            binding.restaurantName.setText(restaurant.name);
            binding.city.setText(restaurant.city+", ");
            binding.postalCode.setText(restaurant.postalCode);
            binding.imageRestaurant.setImageBitmap(getUserImage(restaurant.image));
            binding.duration.setText(restaurant.duration.toString()+" mins away, ");
            binding.contact.setText("Ph: "+restaurant.phoneNumber);
            binding.getRoot().setOnClickListener(v->{
                restaurantListener.onRestaurantClicked(restaurant);
            });
        }


    }
    private Bitmap getUserImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
