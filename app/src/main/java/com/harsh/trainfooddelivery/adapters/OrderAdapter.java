package com.harsh.trainfooddelivery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.trainfooddelivery.databinding.RestaurantOrderContainerBinding;
import com.harsh.trainfooddelivery.listeners.OrderListener;
import com.harsh.trainfooddelivery.models.Order;
import com.harsh.trainfooddelivery.utilities.Constants;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private final OrderListener orderListener;
    private Context context;

    public OrderAdapter(List<Order> orderList, OrderListener orderListener){
        this.orderList = orderList;
        this.orderListener = orderListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RestaurantOrderContainerBinding orderContainerBinding=RestaurantOrderContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        context=parent.getContext();
        return new OrderAdapter.OrderViewHolder(orderContainerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.setData(orderList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        RestaurantOrderContainerBinding binding;
        public OrderViewHolder(@NonNull RestaurantOrderContainerBinding restaurantOrderContainerBinding) {
            super(restaurantOrderContainerBinding.getRoot());
            binding=restaurantOrderContainerBinding;
        }

        @SuppressLint("SetTextI18n")
        void setData(Order order, int position){
            binding.orderId.setText("Order Id: "+order.id);
            binding.amount.setText("Total Amount: "+order.amount);
            binding.otp.setText("OTP: "+order.restaurantOtp);
            binding.orderStatusText.setText("Order Status: "+order.orderStatus);
            binding.deliveryTime.setText("Time of Delivery: "+TimeAdjuster(order.timestamp));
            StringBuilder stringBuilder=new StringBuilder("");
            for (String item : order.foodItem) {
                String[] dissectedValues;
                dissectedValues = dissectSequence(item);
                stringBuilder.append(dissectedValues[0]).append(" X ").append(dissectedValues[1]).append("\n");
            }
            binding.foodItems.setText(stringBuilder.substring(0, stringBuilder.length()-1));
            if (order.orderStatus.equals(Constants.KEY_PENDING_STATUS)) {
                binding.declineLayout.setOnClickListener(v -> {
                    orderListener.onRejected(order, position);
                });
                binding.acceptLayout.setOnClickListener(v -> {
                    orderListener.onAccepted(order);
                });
            }
            else{
                    binding.layout.setVisibility(View.GONE);
            }
        }
    }

    private String TimeAdjuster(String dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalTime time = LocalTime.parse(dateTime, formatter).plusMinutes(20);

        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
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
