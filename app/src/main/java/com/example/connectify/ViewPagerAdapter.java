package com.example.connectify;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    int tabCount;
    public ViewPagerAdapter(@NonNull FragmentManager fm, int tabCount) {
        super(fm, tabCount);
        this.tabCount = tabCount;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                NotificationFragment notificationFragment = new NotificationFragment();
                return notificationFragment;
            case 1:
                VoteTalliesFragment voteTalliesFragment = new VoteTalliesFragment();
                return voteTalliesFragment;
            case 2:
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 3:
                TeamMembersFragment teamMembersFragment = new TeamMembersFragment();
                return teamMembersFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
