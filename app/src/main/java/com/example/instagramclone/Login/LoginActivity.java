package com.example.instagramclone.Login;

import android.content.Context;
import android.content.Intent;
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

import com.example.instagramclone.Home.HomeActivity;
import com.example.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG="Login Activity";


    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private Context mContext;
    private ProgressBar progressBar;
    private EditText mEmail,mPassword;
    private TextView pleaseWait;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext=LoginActivity.this;

        progressBar=(ProgressBar)findViewById(R.id.loginRequestProgressBar);
        progressBar.setVisibility(View.GONE);

        pleaseWait=(TextView)findViewById(R.id.tvPleaseWait);

        mEmail=(EditText)findViewById(R.id.inputEmail);
        mPassword=(EditText)findViewById(R.id.inputPassword);

        pleaseWait.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull:checking if string is null");

        if(string==null||string.equals("")||string.isEmpty()){
            return true;
        }else return false;
    }

    private void init(){
        //initilize the button for logging in

        Button btnLogin=(Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick:attempt to login");
                String email=mEmail.getText().toString();
                final String password=mPassword.getText().toString();

                if(isStringNull(email)&&isStringNull(password)){
                    Toast.makeText(mContext,"You must fill out all the fields",Toast.LENGTH_LONG).show();
                }else
                {
                    progressBar.setVisibility(View.VISIBLE);
                    pleaseWait.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    FirebaseUser user=mAuth.getCurrentUser();


                                    Log.d(TAG,"signInWithEmail:Successfull login");
                                    if (task.isSuccessful()) {

                                        try {
                                            if(user.isEmailVerified()){
                                                Log.d(TAG, "onComplete: success. email is verified");
                                                Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                                                startActivity(intent);
                                            }else{
                                                Toast.makeText(mContext,"Email is not verified",Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                                pleaseWait.setVisibility(View.GONE);
                                                mAuth.signOut();
                                            }

                                        }catch(NullPointerException e) {
                                            Log.e(TAG,"Null Pointer Exception"+e.getMessage());
                                        }
                                    } else {
                                        Log.d(TAG,"signInWithEmail:failed"+task.getException());
                                        // If sign in fails, display a message to the user.
                                       // Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                                Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        pleaseWait.setVisibility(View.GONE);
                                    }

                                    // ...
                                }
                            });
                }
            }
        });

        TextView linkSignup=(TextView)findViewById(R.id.link_signup);
        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        if(mAuth.getCurrentUser()!=null){

            Intent intent =new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();

        }
    }

    //********************firebase section*******************//





    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebaseAuth ");
        mAuth=FirebaseAuth.getInstance();
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


