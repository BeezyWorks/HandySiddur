package com.mattaniahbeezy.icecreamlibrary;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;


public class WatchZmanim extends Activity implements
        LocationHelper.LocationHandled {

    WearableListView listView;
    ZmanGetter czc;
    LocationHelper locationHelper;
    SettingsHelper settingsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        settingsHelper=new SettingsHelper(this);
        locationHelper= new LocationHelper(this, this);
        locationHelper.connect();
        listView = new WearableListView(this);
        czc = ZmanGetter.getCZC(this);
        listView.setAdapter(new ZmanListAdapter(czc.getFullLuach()));
        listView.setGreedyTouchMode(true);


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.contentFrame);
                frameLayout.addView(listView);
            }
        });

    }


    @Override
    public void locationAvailable(Location location, int locationSource) {
        czc=ZmanGetter.getCZC(this, Calendar.getInstance(), location);
        listView.setAdapter(new ZmanListAdapter(czc.getFullLuach()));
    }

    public class ZmanListAdapter extends WearableListView.Adapter {
        private List<ZmanGetter.DAILY_ZMANIM> mDataset;

        public class ViewHolder extends WearableListView.ViewHolder {
            public LinearLayout mLinearLayout;

            public ViewHolder(LinearLayout v) {
                super(v);
                mLinearLayout = v;
            }
        }

        public ZmanListAdapter(List<ZmanGetter.DAILY_ZMANIM> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {

            LinearLayout v = (LinearLayout) getLayoutInflater().inflate(R.layout.zmanrow, null);
            ZmanListAdapter.ViewHolder vh = new ZmanListAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            ZmanGetter.DAILY_ZMANIM zman = mDataset.get(position);
            ZmanListAdapter.ViewHolder mHolder = (ViewHolder) holder;
            TextView title = (TextView) mHolder.mLinearLayout.findViewById(R.id.zmanTitle);
            TextView time = (TextView) mHolder.mLinearLayout.findViewById(R.id.zmanTime);
            title.setText(zman.getName());
            time.setText(zman.getTimeString(settingsHelper.getTimePatern(), czc));
        }


        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }


}