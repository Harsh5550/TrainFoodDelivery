package com.harsh.trainfooddelivery.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.harsh.trainfooddelivery.fragments.MenuFragment;
import com.harsh.trainfooddelivery.fragments.RestaurantOrderFragment;

public class ViewPagerRestaurantAdapter extends FragmentPagerAdapter {
    public ViewPagerRestaurantAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position==0){
            return new MenuFragment();
        }
        else {
            return new RestaurantOrderFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position==0){
            return "My Menu";
        }
        else {
            return "Orders";
        }
    }
}
