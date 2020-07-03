package com.example.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.ViewCommentsFragment;
import com.example.instagramclone.Utils.ViewPostFragment;
import com.example.instagramclone.Utils.ViewProfileFragment;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener,
ViewProfileFragment.OnGridImageSelectedListener {

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener: selected a comment thread");
        ViewCommentsFragment fragment=new ViewCommentsFragment();
        Bundle args=new Bundle();
        args.putParcelable(getString(R.string.photo),photo);
        fragment.setArguments(args);

        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    private static final String TAG="Profile Activity";

    private Context mContext =ProfileActivity.this;

    ImageView profilePhoto;


    private static final int ACTIVITY_NUM=4;
    private static final int NUM_GRID_COLUMNS=3;
    ProgressBar mProgressbar;

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: Selected an image gridview "+photo.toString());
        ViewPostFragment fragment=new ViewPostFragment();
        Bundle args=new Bundle();
        args.putParcelable(getString(R.string.photo),photo);
        args.putInt(getString(R.string.activity_number),activityNumber);
        fragment.setArguments(args);

        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_post__profile_fragment));
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);




        init();
      }

      public void init(){
        Log.d(TAG,"init:inflating"+getString(R.string.profile_fragment));
          Intent intent=getIntent();

          if (intent.hasExtra(getString(R.string.calling_activity))){
              Log.d(TAG, "init: searchng for user object attached as intent extra ");

              if (intent.hasExtra(getString(R.string.intent_user))){
                  User user=intent.getParcelableExtra(getString(R.string.intent_user));
                  if(!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                      Log.d(TAG, "init: Inflating view profile");
                      ViewProfileFragment fragment=new ViewProfileFragment();
                      Bundle args=new Bundle();
                      args.putParcelable(getString(R.string.intent_user),intent.getParcelableExtra(getString(R.string.intent_user)));
                      fragment.setArguments(args);

                      FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
                      transaction.replace(R.id.container,fragment);
                      transaction.addToBackStack(getString(R.string.view_profile_fragment));
                      transaction.commit();
                  }else{
                      Log.d(TAG, "init: inflating profile");
                      ProfileFragment fragment=new ProfileFragment();
                      FragmentTransaction transaction=ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                      transaction.replace(R.id.container,fragment);
                      transaction.addToBackStack(getString(R.string.profile_fragment));
                      transaction.commit();
                  }

              }else {
                  Toast.makeText(mContext,"Something Wint Wrong",Toast.LENGTH_LONG).show();
                  Log.d(TAG, "init: Something went wrong");
              }

          }else{
              Log.d(TAG, "init: inflating profile");
              ProfileFragment fragment=new ProfileFragment();
              FragmentTransaction transaction=ProfileActivity.this.getSupportFragmentManager().beginTransaction();
              transaction.replace(R.id.container,fragment);
              transaction.addToBackStack(getString(R.string.profile_fragment));
              transaction.commit();
          }



      }





}
