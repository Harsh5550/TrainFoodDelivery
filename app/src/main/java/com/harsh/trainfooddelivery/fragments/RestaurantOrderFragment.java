package com.harsh.trainfooddelivery.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.adapters.OrderAdapter;
import com.harsh.trainfooddelivery.listeners.OrderListener;
import com.harsh.trainfooddelivery.models.Order;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RestaurantOrderFragment extends Fragment implements OrderListener {

    private List<Order> orderList;
    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;

    private View rootView;

    public RestaurantOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_restaurant_order, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getOrders(view);
    }

    private void getOrders(View view){
        loading(true, view);
        PreferenceManager preferenceManager=new PreferenceManager(requireActivity());
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ORDER)
                .whereEqualTo(Constants.KEY_RESTAURANT_ID, preferenceManager.getString(Constants.KEY_RESTAURANT_ID))
                .get()
                .addOnCompleteListener(task->{
                    loading(false, view);
                    if(task.isSuccessful() && task.getResult() != null){
                        orderList= new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_ORDER_STATUS), Constants.KEY_PENDING_STATUS) ||
                                    Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_ORDER_STATUS), Constants.KEY_ACCEPTED_STATUS))
                            {
                                Order order = new Order();
                                order.id = queryDocumentSnapshot.getId();
                                order.orderStatus=queryDocumentSnapshot.getString(Constants.KEY_ORDER_STATUS);
                                order.amount = queryDocumentSnapshot.getString(Constants.KEY_ORDER_AMOUNT);
                                order.timestamp = queryDocumentSnapshot.getString(Constants.KEY_ORDER_TIMESTAMP);
                                int i = 1;
                                order.foodItem = new ArrayList<String>();
                                while (queryDocumentSnapshot.getString((Constants.KEY_ORDER_FOOD_ITEM + i)) != null) {
                                    order.foodItem.add(queryDocumentSnapshot.getString((Constants.KEY_ORDER_FOOD_ITEM + i)));
                                    i++;
                                }
                                try {
                                    order.restaurantOtp = AESCrypt.decrypt("frmeihafokfso", queryDocumentSnapshot.getString(Constants.KEY_RESTAURANT_OTP));
                                }
                                catch (GeneralSecurityException e) {
                                    throw new RuntimeException(e);
                                }
                                orderList.add(order);
                            }
                        }
                        if(orderList.size()>0){
                            orderAdapter=new OrderAdapter(orderList, this);
                            recyclerView=view.findViewById(R.id.orderRecyclerView);
                            recyclerView.setAdapter(orderAdapter);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage(view);
                        }
                    }
                    else{
                        showErrorMessage(view);
                    }
                });
    }
    private void showErrorMessage(View view){
        TextView textView=view.findViewById(R.id.textErrorMessage);
        textView.setText(String.format("%s", "No orders available"));
        textView.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading, View view){
        ProgressBar progressBar=view.findViewById(R.id.progressBar);
        if(isLoading){
            progressBar.setVisibility(View.VISIBLE);
        }
        else{
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAccepted(Order order) {
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ORDER)
                .document(order.id)
                .update(Constants.KEY_ORDER_STATUS, Constants.KEY_ACCEPTED_STATUS)
                .addOnSuccessListener(v->{
                    Toast.makeText(getActivity(), "Order Accepted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(v->{
                    Toast.makeText(getActivity(), "Unable to Accept", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRejected(Order order, int position) {
        AlertDialog.Builder delDialog=new AlertDialog.Builder(getActivity());
        delDialog.setTitle("Decline?");
        delDialog.setIcon(R.drawable.ic_warning);
        delDialog.setMessage("Are you sure you want to decline this order?");
        delDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseFirestore database=FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_ORDER)
                        .document(order.id)
                        .update(Constants.KEY_ORDER_STATUS, Constants.KEY_REJECTED_STATUS)
                        .addOnSuccessListener(v->{
                            orderList.remove(order);
                            orderAdapter.notifyItemRemoved(position);
                            if(orderList.size()==0){
                                recyclerView.setVisibility(View.INVISIBLE);
                                showErrorMessage(rootView);
                            }
                        })
                        .addOnFailureListener(v->{
                            Toast.makeText(getActivity(), "Order decline unsuccessful", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        delDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //for no option
            }
        });
        delDialog.show();
    }
}