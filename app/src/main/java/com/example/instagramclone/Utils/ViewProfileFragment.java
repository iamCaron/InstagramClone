package com.example.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.instagramclone.Profile.AccountSettingsActivity;
import com.example.instagramclone.Profile.ProfileActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.models.Comment;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileFragment extends Fragment {

    private static final String TAG="ProfileFragment";

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener onGridImageSelectedListener;


    private TextView mPosts,mFollowers,mFollowing,mDisplayName,mUsername,mWebsite,mDescription,mFollow,mUnfollow;
    private ProgressBar mProgressbar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu , mBackArrow;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView editProfile;


    //vars
    private User mUser;
    private int mFollowersCount=0;
    private int mFollowingCount=0;
    private int mPostsCount=0;


    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRef;


    private Context mContext;
    private static final int ACTIVITY_NUM=4;
    private static final int NUM_GRID_COLUMNS=3;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_view_profile,container,false);

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
        mFollow=(TextView)view.findViewById(R.id.follow);
        mUnfollow=(TextView)view.findViewById(R.id.unfollow);
        editProfile=(TextView) view.findViewById(R.id.textEditProfile);
        mBackArrow=(ImageView)view.findViewById(R.id.backArrow);

        mContext=getActivity();

        Log.d(TAG, "onCreateView: started");

        try{
            mUser=getUserFromBundle();
            init();
        }catch(NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: "+e.getMessage() );
            Toast.makeText(mContext, "Something wet wrong", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();

        }

        setupBottomNavigationView();


        setupFirebaseAuth();
        isFollowing();

        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now following "+mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(mUser.getUser_id());


                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                setFollowing();
            }
        });

        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now unfollowing "+mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();


                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .removeValue();

                setUnfollowing();
            }
        });


        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to "+mContext.getString(R.string.edit_profile_fragment));

                Intent intent=new Intent(getActivity(),AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        return view;
    }

    private User getUserFromBundle(){
        Log.d(TAG, "getUserFromBundle: arguments "+getArguments());

        Bundle bundle=this.getArguments();
        if(bundle!=null){
            return bundle.getParcelable(getString(R.string.intent_user));

        }else {
            return null;
        }
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
       mPosts.setText(String.valueOf(userAccountSettings.getPosts()));
       mFollowers.setText(String.valueOf(userAccountSettings.getFollowers()));
       mFollowing.setText(String.valueOf(userAccountSettings.getFollowing()));
       mProgressbar.setVisibility(View.GONE);


       mBackArrow.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.d(TAG, "onClick: navigating back");
               getActivity().getSupportFragmentManager().popBackStack();
               getActivity().finish();
           }
       });


   }





    private void init(){
        //set the profile widgets

        DatabaseReference reference1= FirebaseDatabase.getInstance().getReference();
        Query query1=reference1.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:+"+singleSnapshot.getValue(UserAccountSettings.class).toString());

                    UserSettings settings=new UserSettings();
                    settings.setUser(mUser);
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));
                    setProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //get the users profile photos

        DatabaseReference reference2=FirebaseDatabase.getInstance().getReference();
        Query query2=reference2.child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<Photo> photos= new ArrayList<Photo>();

                for (DataSnapshot singleSnapshot:dataSnapshot.getChildren()){

                    Photo photo=new Photo();
                    Map<String,Object> objectMap=(HashMap<String,Object>) singleSnapshot.getValue();
                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> comments=new ArrayList<>();
                    for(DataSnapshot dSnapshot:singleSnapshot.child(getString(R.string.field_comments)).getChildren()){
                        Comment comment=new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        comments.add(comment);
                    }
                    photo.setComments(comments);

                    List<Like> likesList = new ArrayList<Like>();
                    for(DataSnapshot ds:singleSnapshot.child(getString(R.string.field_likes)).getChildren()){
                        Like like=new Like();
                        like.setUser_id(ds.getValue(Like.class).getUser_id());
                        likesList.add(like);

                    }
                    photo.setLikes(likesList);
                    photos.add(photo);


                }
                setupImageGrid(photos);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query cancelled");

            }
        });

    }

    private void isFollowing(){
        Log.d(TAG, "isFollowing: checking if following the user");
        setUnfollowing();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:+"+singleSnapshot.getValue(UserAccountSettings.class).toString());

                 setFollowing();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowersCount(){
        mFollowersCount=0;
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    //Log.d(TAG, "onDataChange: found user:+"+singleSnapshot.getValue(UserAccountSettings.class).toString());

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
                .child(mUser.getUser_id());
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
                .child(mUser.getUser_id());
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



    private void setFollowing(){
        Log.d(TAG, "setFollowing: updating ui for following this user");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);
    }
    private void setUnfollowing(){
        Log.d(TAG, "setFollowing: updating ui for unfollowing this user");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);
    }
    private void setCurrentUsersProfile(){
        Log.d(TAG, "setFollowing: updating ui for showing the user thiwe own profile");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);
    }

    private void setupImageGrid(final ArrayList<Photo> photos){
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
