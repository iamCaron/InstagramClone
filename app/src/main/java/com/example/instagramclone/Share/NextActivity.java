package com.example.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.FirebaseMethods;
import com.example.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NextActivity extends AppCompatActivity {

    private static final String TAG="Next Activity";

    //firebase

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseMethods firebaseMethods;
    private DatabaseReference mRef;

    //widget
    private EditText mCaption;

    //vars
    private  String mAppend="file:/";
    private int imageCount=0;
    private String imgUrl;
    private Intent intent;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        Log.d(TAG, "onCreate: got the choosen image "+getIntent().getStringExtra(getString(R.string.selected_image)));
        firebaseMethods=new FirebaseMethods(NextActivity.this);
        setupFirebaseAuth();
        mCaption=(EditText)findViewById(R.id.etCaption);
        ImageView backArrow=(ImageView)findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Closing the gallery fragment");
                finish();
            }
        });

        TextView share=(TextView)findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating to final share screen");
                //upload the image in firebase

                Toast.makeText(NextActivity.this,"Attempting to upload new photo",Toast.LENGTH_LONG).show();
                String caption=mCaption.getText().toString();

                if(intent.hasExtra(getString(R.string.selected_image))){
                    imgUrl=intent.getStringExtra(getString(R.string.selected_image));
                    firebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,imgUrl,null);


                }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    bitmap=(Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    firebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,null,bitmap);

                }

            }
        });
        setImage();
    }


    private void setImage(){

        intent=getIntent();
        ImageView image=(ImageView)findViewById(R.id.imageShare);

        if(intent.hasExtra(getString(R.string.selected_image))){
            imgUrl=intent.getStringExtra(getString(R.string.selected_image));
            UniversalImageLoader.setImage(imgUrl,image,null,mAppend);
            Log.d(TAG, "setImage: set image: got new image url "+imgUrl);

        }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            bitmap=(Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: set image bitmap ");
            image.setImageBitmap(bitmap);
        }

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

                imageCount=firebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count "+imageCount);
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

