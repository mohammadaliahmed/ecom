package com.yasir.ecom.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.yasir.ecom.Activities.AdPage;
import com.yasir.ecom.Model.AdDetails;
import com.yasir.ecom.Model.PicturesModel;
import com.yasir.ecom.R;
import com.yasir.ecom.Utils.GetAdAddress;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by maliahmed on 15/12/2017.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    Context context;
    ArrayList<AdDetails> itemList;
    LayoutInflater layoutInflater;
    DatabaseReference mDatabase;
    public static final int GOOGLE_AD_LAYOUT = 1;
    public static final int AD_LAYOUT = 0;


    public ItemAdapter(Context context, ArrayList<AdDetails> itemList) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.itemList = itemList;


    }

    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == GOOGLE_AD_LAYOUT) {
            View view = layoutInflater.inflate(R.layout.ad_in_list_layout, parent, false);
            ItemAdapter.ViewHolder viewHolder = new ItemAdapter.ViewHolder(view);
            return viewHolder;
        } else {
            View view = layoutInflater.inflate(R.layout.item_layout, parent, false);
            ItemAdapter.ViewHolder viewHolder = new ItemAdapter.ViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(final ItemAdapter.ViewHolder holder, int position) {


        int viewType = getItemViewType(position);
        switch (viewType) {
            case AD_LAYOUT:
                final AdDetails model = itemList.get(position);
                DecimalFormat formatter = new DecimalFormat("#,###,###");
                String formattedPrice = formatter.format(model.getPrice());
                holder.title.setText(model.getTitle());
                holder.location.setText(GetAdAddress.getAddress(context, model.getLattitude(), model.getLongitude()));

                holder.price.setText("Rs " + formattedPrice);
                holder.time.setText(getFormattedDate(context, model.getTime()));
                mDatabase.child("ads").child("" + model.getAdId()).child("pictures").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            PicturesModel model1 = childSnapshot.getValue(PicturesModel.class);
                            if (model1.getPosition() == 0) {
                                Glide.with(context).load(model1.getImageUrl()).into(holder.thumbnail);
                            } else {
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, AdPage.class);
                        i.putExtra("adId", "" + model.getAdId());
                        context.startActivity(i);
                    }
                });

                break;
            case GOOGLE_AD_LAYOUT:
                AdRequest adRequest = new AdRequest.Builder().build();
                holder.adView.loadAd(adRequest);
                break;
            default:

        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0 && position % 10 == 0)
            return GOOGLE_AD_LAYOUT;
        return AD_LAYOUT;

//        return super.getItemViewType(position);
    }

    public String getFormattedDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "dd MMM ";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "" + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return "Yesterday ";
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("dd MMM , h:mm aa", smsTime).toString();
        }
    }

    @Override
    public int getItemCount() {

        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, price, time, location;
        public ImageView thumbnail;
        public AdView adView;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleItem);
            price = itemView.findViewById(R.id.price);
            time = itemView.findViewById(R.id.time);
            adView=itemView.findViewById(R.id.adView);

            location = itemView.findViewById(R.id.location);
            thumbnail = itemView.findViewById(R.id.thumbnail);

        }
    }
}