package com.yasir.ecom.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.yasir.ecom.R;


/**
 * Created by maliahmed on 15/12/2017.
 */

public class ProgressBarViewHolder extends RecyclerView.ViewHolder {

    public ProgressBar progressBar;

    public ProgressBarViewHolder(View itemView) {
        super(itemView);

        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
    }
}
