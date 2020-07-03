package com.example.instagramclone.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentsList = new ArrayList<>();
    private final HashMap<Fragment,Integer> mFragments=new HashMap<>();
    private final HashMap<String,Integer> mFragmentNumbers=new HashMap<>();
    private final HashMap<Integer,String> mFragmentsName=new HashMap<>();

    public SectionsStatePagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentsList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentsList.size();
    }

    public void addFragment(Fragment fragment,String fragmentName){
        mFragmentsList.add(fragment);
        mFragments.put(fragment,mFragmentsList.size()-1);
        mFragmentNumbers.put(fragmentName,mFragmentsList.size()-1);
        mFragmentsName.put(mFragmentsList.size()-1,fragmentName);
    }


    //returns  the fragment with the name @param
    public Integer getFragmentNumber(String fragmentName){
        if (mFragmentNumbers.containsKey(fragmentName)) {
            return mFragmentNumbers.get(fragmentName);
        }else return null;
    }

    //returns  the fragment with the name @param
    public Integer getFragmentNumber(Fragment fragment){
        if (mFragmentNumbers.containsKey(fragment)) {
            return mFragmentNumbers.get(fragment);
        }else return null;
    }

    //returns  the fragment with the name @param
    public String getFragmentName(Integer fragmentNumber){
        if (mFragmentsName.containsKey(fragmentNumber)) {
            return mFragmentsName.get(fragmentNumber);
        }else return null;
    }
}
