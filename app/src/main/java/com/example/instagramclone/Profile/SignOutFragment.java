package com.example.instagramclone.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagramclone.Login.LoginActivity;
import com.example.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignOutFragment extends Fragment {

    private static final String TAG="SignOutFragment";



    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private ProgressBar progressBar;

    private TextView tvSignout,tvSigningOut;
        
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_signout,container,false);

        tvSignout=(TextView)view.findViewById(R.id.tvConfirmSignout);
        tvSigningOut=(TextView)view.findViewById(R.id.tvSigningOut);
        progressBar=(ProgressBar)view.findViewById(R.id.progressBarSignOut);
        Button btnConfirmSignout=view.findViewById(R.id.btnConfirmSignout);
        setupFirebaseAuth();

        tvSigningOut.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        btnConfirmSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvSigningOut.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                Log.d(TAG,"onClick:attempting to signout");
                mAuth.signOut();
                getActivity().finish();
            }
        });

        return view;
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


                    Log.d(TAG,"Navigating back to login screen");

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);

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
