package com.harsh.trainfooddelivery.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.harsh.trainfooddelivery.activities.AddFoodItemActivity;
import com.harsh.trainfooddelivery.activities.EditFoodItemActivity;
import com.harsh.trainfooddelivery.adapters.MenuAdapter;
import com.harsh.trainfooddelivery.listeners.MenuListener;
import com.harsh.trainfooddelivery.models.Menu;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment implements MenuListener {
    private com.google.android.material.floatingactionbutton.FloatingActionButton button;
    private List<Menu> menuList;
    private RecyclerView recyclerView;
    private MenuAdapter menuAdapter;
    private View rootView;
    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_menu, container, false);
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button=view.findViewById(R.id.addItem);
        button.setOnClickListener(v->{
            Intent intent=new Intent(getActivity(), AddFoodItemActivity.class);
            startActivity(intent);
        });
        getMenu(view);
    }

    private void getMenu(View view){
        loading(true, view);
        PreferenceManager preferenceManager=new PreferenceManager(requireActivity());
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_MENU)
                .get()
                .addOnCompleteListener(task->{
                    loading(false, view);
                    String currentRestaurantId=preferenceManager.getString(Constants.KEY_RESTAURANT_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        menuList= new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            if(!(currentRestaurantId.equals(queryDocumentSnapshot.getString(Constants.KEY_RESTAURANT_ID)))){
                                continue;
                            }
                            Menu menu=new Menu();
                            menu.name=queryDocumentSnapshot.getString(Constants.KEY_FOOD_NAME);
                            menu.price=queryDocumentSnapshot.getString(Constants.KEY_FOOD_PRICE);
                            menu.image=queryDocumentSnapshot.getString(Constants.KEY_FOOD_IMAGE);
                            menu.foodType=queryDocumentSnapshot.getString(Constants.KEY_FOOD_TYPE);
                            menu.serving=queryDocumentSnapshot.getString(Constants.KEY_FOOD_SERVING);
                            menu.quantity=queryDocumentSnapshot.getString(Constants.KEY_FOOD_QUANTITY);
                            menu.availability=queryDocumentSnapshot.getString(Constants.KEY_FOOD_AVAILABILITY);
                            menu.id=queryDocumentSnapshot.getId();
                            menuList.add(menu);
                        }
                        if(menuList.size()>0){
                            menuAdapter=new MenuAdapter(menuList, this);
                            recyclerView=view.findViewById(R.id.menuRecyclerView);
                            recyclerView.setAdapter(menuAdapter);
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
        textView.setText(String.format("%s", "No food Item available"));
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
    public void onEditClicked(Menu menu) {
        Intent intent=new Intent(getActivity(), EditFoodItemActivity.class);
        intent.putExtra("Menu", menu);
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(Menu menu, int position) {
        AlertDialog.Builder delDialog=new AlertDialog.Builder(getActivity());
        delDialog.setTitle("Delete?");
        delDialog.setIcon(R.drawable.ic_delete);
        delDialog.setMessage("Are you sure you want to delete this item?");
        delDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseFirestore database=FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_MENU)
                        .document(menu.id)
                        .delete()
                        .addOnSuccessListener(v->{
                            menuList.remove(menu);
                            menuAdapter.notifyItemRemoved(position);
                            if(menuList.size()==0){
                                recyclerView.setVisibility(View.INVISIBLE);
                                showErrorMessage(rootView);
                            }
                        })
                        .addOnFailureListener(v->{
                            Toast.makeText(getActivity(), "Item deletion unsuccessful", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onCount(int count, int position) {
        //No Action Required
    }
}