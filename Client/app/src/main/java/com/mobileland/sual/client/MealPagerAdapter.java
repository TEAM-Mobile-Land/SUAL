package com.mobileland.sual.client;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MealPagerAdapter extends FragmentStateAdapter {

    public MealPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return MealFragment.newInstance("breakfast");
            case 1: return MealFragment.newInstance("lunch_korean");
            case 2: return MealFragment.newInstance("lunch_special");
            case 3: return MealFragment.newInstance("dinner");
            default: return MealFragment.newInstance("breakfast");
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}