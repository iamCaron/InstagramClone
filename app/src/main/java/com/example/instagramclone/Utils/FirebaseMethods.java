package com.example.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.instagramclone.Home.HomeActivity;
import com.example.instagramclone.Profile.AccountSettingsActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {

    private static  final String TAG="Firebase Methods";

    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRef;
    private StorageReference mStorageReference;


    //vars
    private Context mContext;
    private double mPhotoUploadProgress=0;

    private String userId;



    public FirebaseMethods(Context context){
        mAuth=FirebaseAuth.getInstance();

        firebaseDatabase=FirebaseDatabase.getInstance();
        mRef=firebaseDatabase.getReference();
        mStorageReference= FirebaseStorage.getInstance().getReference();
        mContext=context;

        if(mAuth.getCurrentUser()!=null){
            userId=mAuth.getCurrentUser().getUid();

        }

    }

    public void uploadNewPhoto(String photoType, final String caption, int count, final String imgUrl,Bitmap bm){
        Log.d(TAG, "uploadNewPhoto: Attempting to upload new photo");

        FilePaths filePaths=new FilePaths();


        //case1 new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading new photo");
            String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference=mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE+"/"+user_id+"/photo"+(count+1));

            //convert an image url to a bitmap
            if(bm==null) {
               bm=ImageManager.getBitmap(imgUrl);
            }


            byte[] bytes=ImageManager.getBytesFromBitmap(bm,100);


            UploadTask uploadTask=null;
            uploadTask=storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageReference firebaseUrlRef=taskSnapshot.getMetadata().getReference();
                    firebaseUrlRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Toast.makeText(mContext,"Photo Upload Successful",Toast.LENGTH_SHORT).show();

                            //add the new photo to the 'photos' node and 'user_photos' node
                            addPhotoToDatabase(caption,uri.toString());

                            //navigate to the main feed so that the user can see thier photo

                            Intent intent=new Intent(mContext, HomeActivity.class);
                            mContext.startActivity(intent);

                       }


                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo Upload Failed.");
                    Toast.makeText(mContext,"Photo Upload Failed",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress + " %done");


                }
            });

        }
        //case 2 profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");



            String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference=mStorageReference.child(filePaths.FIREBASE_IMAGE_STORAGE+"/"+user_id+"/profile_photo");

            //convert an image url to a bitmap

            if(bm==null) {
                bm=ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes=ImageManager.getBytesFromBitmap(bm,100);


            UploadTask uploadTask=null;
            uploadTask=storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {


                            Toast.makeText(mContext,"Photo upload success",Toast.LENGTH_SHORT).show();

                    //add the new photo to the 'user_account_settings' node
                    setProfilePhoto(uri.toString());

                            ((AccountSettingsActivity)mContext).setViewPager(
                                    ((AccountSettingsActivity)mContext).pagerAdapter
                                            .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                            );




                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo Upload Failed.");
                    Toast.makeText(mContext,"Photo Upload Failed",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                    double progress=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                    if(progress-15>mPhotoUploadProgress){
                        Toast.makeText(mContext,"photo upload progress: "+String.format("%.0f",progress),Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress=progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: "+progress+" %done");

                }
            });
        }
    }

    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image : "+url);
        mRef.child(mContext.getString(R.string.dbname_user_account_settings)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private String getTimeStamp(){

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());


    }

    private void addPhotoToDatabase(String caption, String url) {

        Log.d(TAG, "addPhotoToDatabase: adding photo to the database");

        String tags=StringManupulations.getTags(caption);
        String newPhotoKey=mRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo=new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);
        //insert into database
        mRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);
        mRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }

    public void updateUserAccountSettings(String displayName,String website,String description,long phoneNumber){
        Log.d(TAG, "updateUserAccountSettings: updating user account settings");

        if(displayName!=null){
        mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.field_display_name))
                .setValue(displayName);}

        if(website!=null){
        mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.field_website))
                .setValue(website);}


        if(description!=null){
        mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.field_description))
                .setValue(description);}

        if(phoneNumber!=0){
        mRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.field_phone_number))
                .setValue(phoneNumber);}

    }



    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to "+username);
        mRef.child(mContext.getString(R.string.dbname_users)).child(userId).child(mContext.getString(R.string.field_username)).setValue(username);

        mRef.child(mContext.getString(R.string.dbname_user_account_settings)).child(userId).child(mContext.getString(R.string.field_username)).setValue(username);

    }


    public void updateEmail(String email){
        Log.d(TAG, "updateEmail: updating email to "+email);
        mRef.child(mContext.getString(R.string.dbname_users)).child(userId).child(mContext.getString(R.string.field_email)).setValue(email);


    }



//    public boolean checkIfUsernameExixts(String username, DataSnapshot snapshot) {
//        Log.d(TAG, "checkIfUsernameExists: check if" + username + " exists");
//        User user = new User();
//
//        for (DataSnapshot ds : snapshot.child(userId).getChildren()) {
//            Log.d(TAG, "check if username exists:Datasnapshot " + ds);
//            user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "check if username exists:username" + user.getUsername());
//
//            if (StringManupulations.expandUsername(user.getUsername()).equals(username)) {
//
//                Log.d(TAG, "checkIfUsernameExists:FOUND A MATCH");
//                return true;
//
//            }
//
//
//        }
//        return false;
//    }



    public void addNewUser(String email,String username,String description,String website,String profile_photo){

        User user=new User(userId,1,email,StringManupulations.condenseUsername(username));

        mRef.child(mContext.getString(R.string.dbname_users)).child(userId).setValue(user);

        UserAccountSettings settings=new UserAccountSettings(description,username,0,0,0,profile_photo,StringManupulations.condenseUsername(username),website,userId);
        mRef.child(mContext.getString(R.string.dbname_user_account_settings)).child(userId).setValue(settings);


    }


    public void registerEmail(final String email,String password,final String username){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            sendVerificationEmail();

                            userId = mAuth.getCurrentUser().getUid();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });

    }

    public void sendVerificationEmail(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }else{
                                Toast.makeText(mContext,"couldnt send verification email.",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }
    //retrives the ACCOUNT settings of the currently logged in user
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: RETRIVING USER ACCOUNT SETTINGS FROM FIREBASE");

        UserAccountSettings userAccountSettings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            //user account settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot" + ds);


                try {
                    userAccountSettings.setDisplayName(ds.child(userId).getValue(UserAccountSettings.class).getDisplay_Name());
                    userAccountSettings.setUsername(ds.child(userId).getValue(UserAccountSettings.class).getUsername());
                    userAccountSettings.setWebsite(ds.child(userId).getValue(UserAccountSettings.class).getWebsite());
                    userAccountSettings.setDescription(ds.child(userId).getValue(UserAccountSettings.class).getDescription());
                    userAccountSettings.setProfile_photo(ds.child(userId).getValue(UserAccountSettings.class).getProfile_photo());
                    userAccountSettings.setPosts(ds.child(userId).getValue(UserAccountSettings.class).getPosts());
                    userAccountSettings.setFollowers(ds.child(userId).getValue(UserAccountSettings.class).getFollowers());
                    userAccountSettings.setFollowing(ds.child(userId).getValue(UserAccountSettings.class).getFollowing());
                    Log.d(TAG, "getUserAccountSettings: retrived user account settings information" + userAccountSettings.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: Null pointer exception  : " + e.getMessage());
                }

            }

                //user node
                if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                    Log.d(TAG, "getUserAccountSettings: datasnapshot" + ds);

                    user.setUsername(ds.child(userId).getValue(User.class).getUsername());

                    user.setEmail(ds.child(userId).getValue(User.class).getEmail());
                    user.setPhone_number(ds.child(userId).getValue(User.class).getPhone_number());
                    user.setUser_id(ds.child(userId).getValue(User.class).getUser_id());


                    Log.d(TAG, "getUserAccountSettings: retrived user information" + user.toString());


                }

            }



        return new UserSettings(user,userAccountSettings);


    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int count=0;
        for (DataSnapshot ds:dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()){
                count++;
        }
        return count;
    }


}
