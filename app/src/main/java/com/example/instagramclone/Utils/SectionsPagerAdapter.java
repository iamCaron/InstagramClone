package com.example.instagramclone.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

//class that stores fragments for tabs

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG="Sections Pager Adapter";

    private final List<Fragment> mFragmenttList= new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm){

        super(fm);

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmenttList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmenttList.size();
    }

    public void addFragment(Fragment fragment){
        mFragmenttList.add(fragment);
    }
}
