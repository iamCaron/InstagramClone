package com.example.instagramclone.Login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.FirebaseMethods;
import com.example.instagramclone.Utils.StringManupulations;
import com.example.instagramclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {


    private final String TAG="RegisterActivity" ;

    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;

    private Context mContext;

    private String email,username,password;
    private EditText mEmail,mPassword,mUserName;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;

    private String  append="";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.d(TAG,"OnCreate :Started");
        mContext=RegisterActivity.this;
        firebaseMethods=new FirebaseMethods(mContext);
        initWidgets();
        setupFirebaseAuth();
        init();

    }
    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email=mEmail.getText().toString();
                username=mUserName.getText().toString();
                password=mPassword.getText().toString();
                if (checkInputs(email,username,password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);

                    firebaseMethods.registerEmail(email,password,username);
                }
            }
        });
    }

    private boolean checkInputs(String email,String username,String password) {
        Log.d(TAG,"CheckInputs:checking inputs for null views");
        if(email.equals("")||username.equals("")||password.equals("")){
            Toast.makeText(mContext, "All fields need to be filled out", Toast.LENGTH_SHORT).show();
            return false;
        }else return true;

    }

    private void initWidgets() {
        Log.d(TAG,"Initilazing widgets");
        mEmail=(EditText)findViewById(R.id.inputEmail);
        mUserName=(EditText)findViewById(R.id.input_username);
        btnRegister=(Button)findViewById(R.id.btnRegister);
        mProgressBar=(ProgressBar)findViewById(R.id.registerRequestProgressBar);
        loadingPleaseWait=(TextView)findViewById(R.id.tvPleaseWait);
        mPassword=(EditText)findViewById(R.id.inputPassword);
        mEmail=(EditText)findViewById(R.id.inputEmail);
        mContext=RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);
    }


    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull:checking if string is null");

        if(string==null||string.equals("")||string.isEmpty()){
            return true;
        }else return false;
    }
    //********************firebase section*******************//





    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebaseAuth ");
        mAuth= FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mRef=mFirebaseDatabase.getReference();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                //check if the user is logged in




                if(user!=null){
                    //user is signed in
                    Log.d(TAG,"onAuthStateChanged"+user.getUid());
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //first check username is not already in use

                            checkIfUserNameExists(username);


                            //add new user_account_settings to the database
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    finish();

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


    //check if username already exists in database
    private void checkIfUserNameExists(final String username) {
        Log.d(TAG, "checkIfUserNameExists: Checking if username "+username+" already exists.");
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for(DataSnapshot singleSnapshot:dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUserNameExists: FOUND A MATCH "+singleSnapshot.getValue(User.class).getUsername());
                        append=mRef.push().getKey().substring(3,10);
                        Log.d(TAG,"onDataChanged:Username already exists. Appending Random String to name "+append);

                    }
                }

                //1st check make sure the username is not already in use

                String mUsername="";
                mUsername=username+append;
                //add new user to the database

                firebaseMethods.addNewUser(email,mUsername,"","","");

                Toast.makeText(mContext,"Signup Successfull:Sending verification Email",Toast.LENGTH_LONG);

                mAuth.signOut();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}



