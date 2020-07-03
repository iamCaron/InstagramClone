package com.example.instagramclone.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.instagramclone.Utils.Permissions;
import com.example.instagramclone.Utils.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG="Share Activity";

    private Context mContext =ShareActivity.this;

    private static final int ACTIVITY_NUM=2;
    private static final int VERIFY_PERMISSIONS_REQUEST=1;

    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        if(checkPermissionsArray(Permissions.PERMISSIONS)){

            setupViewPager();

        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }

    }


    //verify all the permissions passed to the array

    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    //check an array of permissions

    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");
        for(int i=0;i<permissions.length;i++){
            String check =permissions[i];
            if(!checkPermissions(check)){
                return false;
            }
        }

        return true;
    }

    //check a single permission if it has been verified
    public boolean checkPermissions(String permission){
        Log.d(TAG, "checkPermissions: checking permission :"+permission);
        int permissionRequest= ActivityCompat.checkSelfPermission(ShareActivity.this,permission);
        if(permissionRequest!= PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission was not granted for :"+permission);
            return false;
        }else{

            Log.d(TAG, "checkPermissions: \n Permission was not granted for :"+permission);
            return true;
        }
    }



    private void setupViewPager(){
        SectionsPagerAdapter adapter=new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager=(ViewPager)findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout=(TabLayout)findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));

    }


    //return the current tab number
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    public int getTask(){
        Log.d(TAG, "getTask: TASK: "+getIntent().getFlags());
        return getIntent().getFlags();
    }
}
