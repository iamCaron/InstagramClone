package com.example.instagramclone.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagramclone.R;
import com.example.instagramclone.Share.ShareActivity;
import com.example.instagramclone.Utils.FirebaseMethods;
import com.example.instagramclone.Utils.UniversalImageLoader;
import com.example.instagramclone.dailogs.ConfirmPasswordDailog;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDailog.OnConfirmPasswordListener {

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: get the password "+password);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // Get auth credentials from the user for re-authentication. The example below shows
            // email and password credentials but there are multiple possible providers,
            // such as GoogleAuthProvider or FacebookAuthProvider.
            AuthCredential credential = EmailAuthProvider
                            .getCredential(mAuth.getCurrentUser().getEmail(),password);

            // Prompt the user to re-provide their sign-in credentials
                    mAuth.getCurrentUser().reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "User re-authenticated.");
                                    if(task.isSuccessful()){
                                        Log.d(TAG, "onComplete: User is Reauthenticated");

                                        //check to see if email is already present in the database
                                        mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {


                                                if(task.isSuccessful()){

                                                    try {


                                                        if (task.getResult().getSignInMethods().size() == 1) {
                                                            Log.d(TAG, "onComplete: this email is already in use");
                                                            Toast.makeText(getActivity(), "This email is already in use", Toast.LENGTH_LONG).show();

                                                        } else if (task.getResult().getSignInMethods().size() == 0) {
                                                            Log.d(TAG, "onComplete: This email is available");
                                                            //the email available to update is
                                                            mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                Log.d(TAG, "User email address updated.");
                                                                                mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                                Toast.makeText(getActivity(), "The email is updated", Toast.LENGTH_LONG).show();

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }catch (NullPointerException e){
                                                        Log.e(TAG, "onComplete: Null Pointer Exception "+e.getMessage());
                                                    }
                                                }
                                            }
                                        });
                                    }else{
                                        Log.d(TAG, "onComplete: reauthentication failed");
                                    }
                                }
                            });
    }

    private static final String TAG="EditProfileFragment";



    //vars
    private UserSettings mUserSettings;

    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference mRef;
    private String userId;



    //EditProfile Fragment widgets


    private EditText mDisplayName,mUsername,mWebsite,mDescription,mEmail,mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private de.hdodenhof.circleimageview.CircleImageView editProfileImage;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        setupFirebaseAuth();

        View view=inflater.inflate(R.layout.fragment_edit_profile,container,false);
        editProfileImage=(de.hdodenhof.circleimageview.CircleImageView)view.findViewById(R.id.new_profile_photo);
        mDisplayName=(EditText)view.findViewById(R.id.displayName);
        mUsername=(EditText)view.findViewById(R.id.userName);
        mWebsite=(EditText)view.findViewById(R.id.website);
        mDescription=(EditText)view.findViewById(R.id.description);
        mEmail=(EditText)view.findViewById(R.id.email);
        mPhoneNumber=(EditText)view.findViewById(R.id.phone);
        mChangeProfilePhoto =(TextView)view.findViewById(R.id.tvChangeProfilePhoto);
        mFirebaseMethods=new FirebaseMethods(getActivity());
        userId=mAuth.getCurrentUser().getUid();


      





            //setProfileImage();


            //navigate back to profile activity
        ImageView backarrow = (ImageView)view.findViewById(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        ImageView checkmark=(ImageView)view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes");
                saveProfileSettings();
            }
        });


        return view;
    }

   //retrives the data contained in the widget and submits to the database
    //before submitting to the database checks that the data is unique

    private void saveProfileSettings(){
      final String displayName=mDisplayName.getText().toString();
        final String username=mUsername.getText().toString();
        final String website=mWebsite.getText().toString();
        final String descriptione=mDescription.getText().toString();
        final String email=mEmail.getText().toString();
        final long phoneNumber=Long.parseLong(mPhoneNumber.getText().toString());




                //if user made change to the username
                if(!mUserSettings.getUser().getUsername().equals(username)){

                    checkIfUserNameExists(username);

                }

                //if user image change to thier email

                if(!mUserSettings.getUser().getEmail().equals(email)){

                    //step 1 : Reauthenticate
                    //           confirm the password and email

                    ConfirmPasswordDailog dailog=new ConfirmPasswordDailog();
                    dailog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
                    dailog.setTargetFragment(EditProfileFragment.this,1);
                    //step 2: check if email is already registered\
                    //         fetchProviderForEmail(String email)
                    //step 3: change the email
                    //        submit the new email to the database and authentication
                }

                if(!mUserSettings.getSettings().getDisplay_Name().equals(displayName)){
                    //update displayname
                    mFirebaseMethods.updateUserAccountSettings(displayName,null,null,0);
                }

                if(!mUserSettings.getSettings().getWebsite().equals(website)){
                    //update website
                    mFirebaseMethods.updateUserAccountSettings(null,website,null,0);

                }

                if(!mUserSettings.getSettings().getDescription().equals(descriptione)){
                    //update description
                    mFirebaseMethods.updateUserAccountSettings(null,null,descriptione,0);

                }

                if(!mUserSettings.getSettings().getProfile_photo().equals(phoneNumber)){
                    //update description
                    mFirebaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);

                }



            }




    //check if username already exists in database
    private void checkIfUserNameExists(final String username) {
        Log.d(TAG, "checkIfUserNameExists: Checking if username "+username+" already exists.");
        DatabaseReference reference=firebaseDatabase.getReference();
        Query query=reference.child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                    mFirebaseMethods.updateUsername(username);

                    Toast.makeText(getActivity(), "The username already exists.", Toast.LENGTH_SHORT).show();


                }

                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUserNameExists: FOUND A MATCH "+singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "The username already exists.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
      //  Log.d(TAG, "setProfileWidgets: "+userSettings.toString());

        mUserSettings=userSettings;

        User user=userSettings.getUser();
        UserAccountSettings userAccountSettings=userSettings.getSettings();

        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(),editProfileImage,null ,"");

        mDisplayName.setText(userAccountSettings.getDisplay_Name());
        mUsername.setText(userAccountSettings.getUsername());
        mWebsite.setText(userAccountSettings.getWebsite());
        mDescription.setText(userAccountSettings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent=new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });






    }




    //********************firebase section*******************//




    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebaseAuth ");
        mAuth= FirebaseAuth.getInstance();
        firebaseDatabase= FirebaseDatabase.getInstance();
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
