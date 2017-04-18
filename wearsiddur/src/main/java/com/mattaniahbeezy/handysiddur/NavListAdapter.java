package com.mattaniahbeezy.handysiddur;

import android.app.Activity;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mattaniah on 2/23/2015.
 */
public class NavListAdapter extends WearableListView.Adapter {
    Activity context;
    List<Sections> mDataset;
    SectionHandler sectionHandler;

    interface SectionHandler{
        public void handleClick(Sections section);
    }

    public NavListAdapter(Activity activity, List<Sections> mDataset, SectionHandler sectionHandler) {
        this.context = activity;
        this.mDataset = mDataset;
        this.sectionHandler=sectionHandler;
    }

    public class ViewHolder extends WearableListView.ViewHolder {
        public ViewGroup mViewGroup;
        public TextView mTextView;
        public CircledImageView circledImageView;

        public ViewHolder(ViewGroup v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.navTitle);
            circledImageView = (CircledImageView) v.findViewById(R.id.navImage);
            circledImageView.setCircleRadius(circledImageView.getWidth() / 2);
            circledImageView.setCircleColor(context.getResources().getColor(R.color.blue));
            circledImageView.setVisibility(View.GONE);
            mViewGroup = v;
        }
    }



    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        ViewGroup v = (ViewGroup) context.getLayoutInflater().inflate(R.layout.navitemlayout, null);
        NavListAdapter.ViewHolder vh = new NavListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        final Sections section = mDataset.get(position);
        NavListAdapter.ViewHolder mHolder = (ViewHolder) holder;
        mHolder.mTextView.setText(section.getTitle());
        int drawableId = section.getDrawable();
        if (drawableId != 0)
            mHolder.circledImageView.setImageResource(drawableId);

        mHolder.mViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sectionHandler.handleClick(section);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}