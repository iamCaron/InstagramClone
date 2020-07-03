package com.example.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.instagramclone.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class GridImageAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResouce;
    private String mAppend;
    private ArrayList<String> imageUrls;

    public GridImageAdapter(Context context,int layoutResouce,String append,ArrayList<String> imageUrls) {
        super(context,layoutResouce,imageUrls);

        mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;

        this.layoutResouce=layoutResouce;
        mAppend=append;
        this.imageUrls=imageUrls;
    }

       //view holder build pattern simillar to recycler view



            private static class ViewHolder{
              SquareImageView image;
              ProgressBar mProgressBar;

            }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;

        if(convertView==null){
            convertView=mInflater.inflate(layoutResouce,parent,false);
            viewHolder=new ViewHolder();
            viewHolder.mProgressBar=(ProgressBar)convertView.findViewById(R.id.grid_image_progress_bar);
            viewHolder.image=(SquareImageView) convertView.findViewById(R.id.grid_image_view);
            convertView.setTag(viewHolder);

            }else {
            viewHolder=(ViewHolder)convertView.getTag();
        }

        String imageUrl=getItem(position);

        ImageLoader imageLoader=ImageLoader.getInstance();

        imageLoader.displayImage(mAppend + imageUrl, viewHolder.image,  new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(viewHolder.mProgressBar!=null){
                    viewHolder.mProgressBar.setVisibility(View.VISIBLE)
                    ;
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(viewHolder.mProgressBar!=null){
                    viewHolder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(viewHolder.mProgressBar!=null){
                    viewHolder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(viewHolder.mProgressBar!=null){
                    viewHolder.mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        return convertView;
        }




}
