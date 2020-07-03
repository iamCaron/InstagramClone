package com.example.instagramclone.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.instagramclone.Login.LoginActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.instagramclone.Utils.MainfeedListAdapter;
import com.example.instagramclone.Utils.SectionsPagerAdapter;
import com.example.instagramclone.Utils.UniversalImageLoader;
import com.example.instagramclone.Utils.ViewCommentsFragment;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HomeActivity extends AppCompatActivity implements MainfeedListAdapter.OnLoadMoreItemsListener {

    private static final String TAG="Home Activity";

    private Context mContext = HomeActivity.this;

    private static final int ACTIVITY_NUM =0;

    ViewPager mViewPager;

    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();
        setupFirebaseAuth();


    }

    public void onCommentThreadSelected(Photo photo, UserAccountSettings settings){
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");

        ViewCommentsFragment fragment=new ViewCommentsFragment();
        Bundle args=new Bundle();
        args.putParcelable(getString(R.string.bundle_photo),photo);
        args.putParcelable(getString(R.string.bundle_user_account_settings),settings);
        fragment.setArguments(args);

        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }



    private void setupBottomNavigationView(){
      Log.d(TAG,"Setup Bottom Navigation View:SETTING UP bottom navigation view");
        BottomNavigationViewEx bottomNavigationViewEx=(BottomNavigationViewEx)findViewById(R.id.bottomNavigationBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

     //Responsible for adding 3 tabs: Camera,Home,Messages
    private void setupViewPager(){
        SectionsPagerAdapter adapter=new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new MessagesFragment());
        ViewPager viewPager=(ViewPager)findViewById(R.id.viewpager_container);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);

        TabLayout tabLayout=(TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera_alt_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_instagram_logo);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_messages);
    }
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader=new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }


    //********************firebase section*******************//


    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG,"checkCurrentUser:checking if current user is logged in");
            if(user==null){
                Intent intent=new Intent(mContext, LoginActivity.class);
                    startActivity(intent);

            }
        }


    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebaseAuth ");
        mAuth=FirebaseAuth.getInstance();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                //check if the user is logged in

                checkCurrentUser(user);


                if(user!=null){
                    //user is signed in
                    Log.d(TAG,"onAuthStateChanged"+user.getUid());

                }else {
                    //user is signed out
                    Log.d(TAG,"onAuthStateChanged:signed_out");

                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(authStateListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();

        if(authStateListener!=null){
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: Displaying more photos");

        HomeFragment fragment=(HomeFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.viewpager_container+":"+mViewPager.getCurrentItem());
        if (fragment!=null){
            fragment.displayMorePhotos();
        }
        
    }
}
