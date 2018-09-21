package com.yasir.ecom.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yasir.ecom.Model.PicturesModel;
import com.yasir.ecom.R;
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by AliAh on 15/01/2018.
 */

public class AdPicturesAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    public static ArrayList<PicturesModel> pictures;

    public AdPicturesAdapter(Context context, ArrayList<PicturesModel> pictures) {
        this.context = context;
        this.pictures = pictures;
    }

    @Override
    public int getCount() {
        return pictures.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view==(LinearLayout)object);

    }
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.ad_pictures_slider_layout,container,false);
        ImageView imageView=view.findViewById(R.id.images);
        Glide.with(context)
                .load(pictures.get(position).getImageUrl()).placeholder(R.drawable.placeholder)
                .into(imageView);

        imageView.setOnTouchListener(new ImageMatrixTouchHandler(context));



        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}
