package com.example.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagramclone.Profile.AccountSettingsActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Utils.FilePaths;
import com.example.instagramclone.Utils.FileSearch;
import com.example.instagramclone.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.sql.Array;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private static final String TAG="Gallery Fragment";

    //widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;

    //consts

    private static final int NUM_GRID_COLUMNS=3;


    //vars

    private ArrayList<String> directories;
    private  String mAppend="file:/";
    private String mSelectedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_gallery,container,false);

        galleryImage=(ImageView)view.findViewById(R.id.galleryImageView);
        gridView=(GridView)view.findViewById(R.id.grid_view);
        directorySpinner=(Spinner)view.findViewById(R.id.spinner_directory);
        mProgressBar=(ProgressBar)view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        directories=new ArrayList<>();

        Log.d(TAG, "onCreateView: started");

        ImageView shareClose=(ImageView)view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the gallery fragment");
                getActivity().finish();
            }
        });

        TextView nextScreen=(TextView)view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                if (isRootTask()){


                    Intent intent=new Intent(getActivity(),NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    startActivity(intent);
                }else {

                    Intent intent=new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();

                }
            }
        });

        init();
        return view;
    }

    private boolean isRootTask(){
        if(((ShareActivity)getActivity()).getTask()==0){
            return true;
        }else {
            return false;
        }
    }

    private void init(){


        //check for other folders inside "/storage/emulated/0/pictures"

        FilePaths filePaths=new FilePaths();

        if(FileSearch.getDirectoryPaths(filePaths.PICTURES)!=null){
            directories=FileSearch.getDirectoryPaths(filePaths.PICTURES);

        }
        ArrayList<String> directoryNames=new ArrayList<>();

        for(int i=0;i<directories.size();i++){
            int index=directories.get(i).lastIndexOf("/");
            String string=directories.get(i).substring(index);
            directoryNames.add(string);
        }

        directories.add(filePaths.CAMERA);

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected "+directories.get(position));

                //setup image grid for directory chosen
                setupGridView(directories.get(position));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
        });
    }

    private void setupGridView(String selectedDirectory){
        Log.d(TAG, "setupGridView: "+selectedDirectory);
        final ArrayList<String> imgUrls = FileSearch.getFilePaths(selectedDirectory);

        //set the grid column width

        int gridWidth=getResources().getDisplayMetrics().widthPixels;
        int imageWidth=gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        //use the adapter to adapt the images to the grid view

        GridImageAdapter gridImageAdapter=new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,mAppend,imgUrls);
        gridView.setAdapter(gridImageAdapter);

        //set the first image to be displayed when the activity fragment view is inflated


        try{


        }catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG, "setupGridView: arrayIndexOutOfBoundsException"+e.getMessage() );
        }
        setImage(imgUrls.get(0),galleryImage,mAppend);
        mSelectedImage=imgUrls.get(0);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: Selected an image "+imgUrls.get(position));

                setImage(imgUrls.get(position),galleryImage,mAppend);
                mSelectedImage=imgUrls.get(position);
            }
        });
    }

    private void setImage(String imgUrl,ImageView image,String append){
        Log.d(TAG, "setImage: setting image");
        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));

        imageLoader.displayImage(append + imgUrl, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
