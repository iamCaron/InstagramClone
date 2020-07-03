package com.example.instagramclone.Profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.instagramclone.Utils.FirebaseMethods;
import com.example.instagramclone.Utils.GridImageAdapter;
import com.example.instagramclone.Utils.UniversalImageLoader;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG="ProfileFragment";

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo,int activityNumber);
    }
    OnGridImageSelectedListener onGridImageSelectedListener;


    private TextView mPosts,mFollowers,mFollowing,mDisplayName,mUsername,mWebsite,mDescription;
    private ProgressBar mProgressbar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;


    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference mRef;


    //vars

    private int mFollowersCount=0;
    private int mFollowingCount=0;
    private int mPostsCount=0;




    private Context mContext;
    private static final int ACTIVITY_NUM=4;
    private static final int NUM_GRID_COLUMNS=3;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile,container,false);

        mDisplayName=(TextView)view.findViewById(R.id.display_name);
        mUsername=(TextView)view.findViewById(R.id.username);
        mWebsite=(TextView)view.findViewById((R.id.website));
        mDescription=(TextView)view.findViewById((R.id.discription));
        mProfilePhoto=(CircleImageView)view.findViewById(R.id.profileImage);
        mPosts=(TextView)view.findViewById((R.id.tvPosts));
        mFollowers=(TextView)view.findViewById((R.id.tvFollowers));
        mFollowing=(TextView)view.findViewById(R.id.tvFollowing);
        mProgressbar=(ProgressBar)view.findViewById(R.id.profileProgressBar);
        gridView=(GridView)view.findViewById(R.id.grid_view);
        toolbar=(Toolbar)view.findViewById(R.id.profileToolbar);
        profileMenu=(ImageView)view.findViewById(R.id.profileMenu);
        bottomNavigationViewEx=(BottomNavigationViewEx)view.findViewById(R.id.bottomNavigationBar);

        mFirebaseMethods=new FirebaseMethods(getActivity());
        mContext=getActivity();

        Log.d(TAG, "onCreateView: started");

        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();
        setupGridView();
        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        TextView editProfile=(TextView)view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to "+mContext.getString(R.string.edit_profile_fragment));

                Intent intent=new Intent(getActivity(),AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            onGridImageSelectedListener=(OnGridImageSelectedListener)getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: Class Cast Exception: "+e.getMessage() );
        }
    }

    private void setupToolbar(){

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);
        profileMenu.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               Log.d(TAG,"onClick: Navigating to Account Settings");
                                               Intent intent=new Intent(mContext,AccountSettingsActivity.class);
                                               startActivity(intent);

                                           }
                                       }
        );
    }
    private void setupBottomNavigationView(){
       Log.d(TAG,"Setup Bottom Navigatigation" +
                "on View:SETTING UP bottom navigation view");

       BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,getActivity(),bottomNavigationViewEx);
       Menu menu=bottomNavigationViewEx.getMenu();
       MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
   }

   private void setProfileWidgets(UserSettings userSettings){
       Log.d(TAG, "setProfileWidgets: "+userSettings.toString());


        //User user=userSettings.getUser();
       UserAccountSettings userAccountSettings=userSettings.getSettings();


       UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(),mProfilePhoto,null ,"");

       mDisplayName.setText(userAccountSettings.getDisplay_Name());
       mUsername.setText(userAccountSettings.getUsername());
       mWebsite.setText(userAccountSettings.getWebsite());
       mDescription.setText(userAccountSettings.getDescription());

       mProgressbar.setVisibility(View.GONE);



   }

   private void setupGridView(){
       Log.d(TAG, "setupGridView: setting up image grid view");

       final ArrayList<Photo> photos=new ArrayList<>();
       DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
       Query query=reference.child(getString(R.string.dbname_user_photos))
               .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
       query.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot singleSnapshot:dataSnapshot.getChildren()){

                   Photo photo=new Photo();
                   Map<String,Object> objectMap=(HashMap<String,Object>) singleSnapshot.getValue();

                   try {
                       photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                       photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                       photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                       photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                       photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                       photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                       List<Like> likesList = new ArrayList<Like>();
                       for (DataSnapshot ds : singleSnapshot.child(getString(R.string.field_likes)).getChildren()) {
                           Like like = new Like();
                           like.setUser_id(ds.getValue(Like.class).getUser_id());
                           likesList.add(like);

                       }
                       photo.setLikes(likesList);
                       photos.add(photo);

                   }catch (NullPointerException e){
                       Log.e(TAG, "onDataChange: NullPointerException "+e.getMessage() );
                   }


               }

               //setup our image grid
               int gridWidth=getResources().getDisplayMetrics().widthPixels;
               int imageWidth=gridWidth/NUM_GRID_COLUMNS;
               gridView.setColumnWidth(imageWidth);

               ArrayList<String> imgUrls=new ArrayList<String>();
               for(int i=0;i<photos.size();i++){
                   imgUrls.add(photos.get(i).getImage_path());
               }
               GridImageAdapter adapter=new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview
                       ,"",imgUrls);
               gridView.setAdapter(adapter);

               gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                   @Override
                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                       onGridImageSelectedListener.onGridImageSelected(photos.get(position),ACTIVITY_NUM);
                   }
               });
               
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {
               Log.d(TAG, "onCancelled: Query cancelled");

           }
       });
    }

    private void getFollowersCount(){
        mFollowersCount=0;
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                   // Log.d(TAG, "onDataChange: found user:+"+singleSnapshot.getValue(UserAccountSettings.class).toString());

                    mFollowersCount++;
                }

                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getFollowingCount(){
        mFollowingCount=0;
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:+"+singleSnapshot.getValue(UserAccountSettings.class).toString());

                    mFollowingCount++;
                }

                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void getPostsCount(){
        mPostsCount=0;
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:+"+singleSnapshot.getValue(UserAccountSettings.class).toString());

                    mPostsCount++;
                }

                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




    //********************firebase section*******************//




    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebaseAuth ");
        mAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        mRef=firebaseDatabase.getReference();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                //check if the user is logged in




                if(user!=null){
                    //user is signed in
                    Log.d(TAG,"onAuthStateChanged"+user.getUid());

                }else {
                    //user is signed out
                    Log.d(TAG,"onAuthStateChanged:signed_out");

                }
            }
        };

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //retrive user information from the database

                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrive images for the user in question
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(authStateListener);

    }

    @Override
    public void onStop() {
        super.onStop();

        if(authStateListener!=null){
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
}
