package com.example.bingoarena.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.bingoarena.fragments.FriendsListFragment;
import com.example.bingoarena.fragments.RequestsFragment;

public class FriendsPagerAdapter extends FragmentStateAdapter {

    public FriendsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new FriendsListFragment();
        } else {
            return new RequestsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
