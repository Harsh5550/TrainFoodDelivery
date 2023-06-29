package com.harsh.trainfooddelivery.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.harsh.trainfooddelivery.R;
import com.harsh.trainfooddelivery.activities.RestaurantSignIn;
import com.harsh.trainfooddelivery.databinding.HomePageBinding;
import com.harsh.trainfooddelivery.databinding.RestaurantSignUpBinding;
import com.harsh.trainfooddelivery.utilities.Constants;
import com.harsh.trainfooddelivery.utilities.PreferenceManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private HomePageBinding binding;
    PreferenceManager preferenceManager;
    private ActivityResultLauncher<Intent> locationSettingsLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=HomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());

        locationSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Check if the user has enabled location services or not
                        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                        if (isLocationEnabled) {
                            // User has enabled location services
                            // Proceed with your app logic here
                            if (preferenceManager.getBoolean(Constants.KEY_ORDER_GENERATED)){
                                startActivity(new Intent(getApplicationContext(), PassengerOrderActivity.class));
                                finish();
                            } else if (preferenceManager.getBoolean(Constants.KEY_DELIVERY_ORDER_GENERATED)) {
                                startActivity(new Intent(getApplicationContext(), DeliveryOrderActivity.class));
                                finish();
                            }
                            else if (preferenceManager.getBoolean(Constants.KEY_PASSENGER_SIGNED_IN)){
                                startActivity(new Intent(getApplicationContext(), PassengerHomePage.class));
                                finish();
                            }
                            else if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
                                startActivity(new Intent(getApplicationContext(), RestaurantHomePage.class));
                                finish();
                            }
                            else if(preferenceManager.getBoolean(Constants.KEY_DELIVERY_SIGNED_IN)){
                                startActivity(new Intent(getApplicationContext(), DeliveryHomeActivity.class));
                                finish();
                            }
                        } else {
                            // User has not enabled location services
                            Toast.makeText(MainActivity.this, "Location services are still disabled.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Dexter.withContext(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            preferenceManager.putBoolean(Constants.KEY_PERMISSION_FLAG, true);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(error -> {
                    Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                }).onSameThread().check();

        if (preferenceManager.getBoolean(Constants.KEY_PERMISSION_FLAG)){
            checkLocation();
            listener();
        }
    }


    private void checkLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isInternetAvailable()) {
            android.app.AlertDialog.Builder alertDialog1 = new android.app.AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialogTheme);
            alertDialog1.setMessage("Internet access is required to use this application. The app will now close.");
            alertDialog1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Close the app
                    finish();
                }
            });
            alertDialog1.setCancelable(false); // Prevent the user from dismissing the dialog
            android.app.AlertDialog alertDialog2 = alertDialog1.create();
            alertDialog2.show();
        }
        else if (!isLocationEnabled) {
            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this, R.style.CustomAlertDialogTheme);
            alertDialogBuilder.setMessage("Please enable your location to use this application.");
            alertDialogBuilder.setNegativeButton("Enable Location", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Open location settings for the user to enable their location
                    Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    locationSettingsLauncher.launch(locationSettingsIntent);
                }
            });
            alertDialogBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Handle cancellation or any other action if needed
                    android.app.AlertDialog.Builder alertDialog1 = new android.app.AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialogTheme);
                    alertDialog1.setMessage("Location access is required to use this application. The app will now close.");
                    alertDialog1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Close the app
                            finish();
                        }
                    });
                    alertDialog1.setCancelable(false); // Prevent the user from dismissing the dialog
                    android.app.AlertDialog alertDialog2 = alertDialog1.create();
                    alertDialog2.show();
                }
            });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            if (preferenceManager.getBoolean(Constants.KEY_ORDER_GENERATED)){
                startActivity(new Intent(getApplicationContext(), PassengerOrderActivity.class));
                finish();
            } else if (preferenceManager.getBoolean(Constants.KEY_DELIVERY_ORDER_GENERATED)) {
                startActivity(new Intent(getApplicationContext(), DeliveryOrderActivity.class));
                finish();
            }
            else if (preferenceManager.getBoolean(Constants.KEY_PASSENGER_SIGNED_IN)){
                startActivity(new Intent(getApplicationContext(), PassengerHomePage.class));
                finish();
            }
            else if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
                startActivity(new Intent(getApplicationContext(), RestaurantHomePage.class));
                finish();
            }
            else if(preferenceManager.getBoolean(Constants.KEY_DELIVERY_SIGNED_IN)){
                startActivity(new Intent(getApplicationContext(), DeliveryHomeActivity.class));
                finish();
            }
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;

    }

    private void listener(){
        binding.restaurateur.setOnClickListener(v->
        {
            if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
                startActivity(new Intent(getApplicationContext(), RestaurantHomePage.class));
                finish();
            }
            else {
                Intent intent= new Intent(getApplicationContext(), RestaurantSignIn.class);
                startActivity(intent);
                finish();
            }
        });
        binding.passenger.setOnClickListener(v->{
            if (preferenceManager.getBoolean(Constants.KEY_PASSENGER_SIGNED_IN)){
                startActivity(new Intent(getApplicationContext(), PassengerHomePage.class));
                finish();
            }
            else {
                startActivity(new Intent(getApplicationContext(), PassengerSignUp.class));
                finish();
            }
        });
        binding.deliveryPersonnel.setOnClickListener(v->{
            if(preferenceManager.getBoolean(Constants.KEY_DELIVERY_SIGNED_IN)){
                startActivity(new Intent(getApplicationContext(), DeliveryHomeActivity.class));
                finish();
            }
            else {
                Intent intent= new Intent(getApplicationContext(), DeliverySignIn.class);
                startActivity(intent);
                finish();
            }
        });
    }
}