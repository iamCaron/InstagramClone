package com.example.instagramclone.dailogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.instagramclone.R;

public class ConfirmPasswordDailog extends DialogFragment {

    private static final String TAG = "ConfirmPasswordDailog";

    public interface OnConfirmPasswordListener{
        public void onConfirmPassword(String password);

    }

    OnConfirmPasswordListener onConfirmPasswordListener;

    TextView mPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.dailog_confirm_password,container,false);
        Log.d(TAG, "onCreateView: started");

        mPassword=(TextView)view.findViewById(R.id.confirmPassword);

        TextView confirmDailog=(TextView)view.findViewById(R.id.dailog_confirm);
        confirmDailog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.d(TAG, "onClick: captured password and confirming");

                String password=mPassword.getText().toString();
                if(!password.equals("")) {
                    onConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }else
                    Toast.makeText(getActivity(),"You must enter a password",Toast.LENGTH_LONG).show();
                
            }
        });

        TextView cancelDailog=(TextView)view.findViewById(R.id.dailog_cancel);
        cancelDailog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the dailog");
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            onConfirmPasswordListener=(OnConfirmPasswordListener)getTargetFragment();


        }catch(ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException "+e.getMessage());
        }
    }
}
