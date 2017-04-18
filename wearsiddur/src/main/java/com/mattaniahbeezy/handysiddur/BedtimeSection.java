package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 1/28/2015.
 */
public class BedtimeSection extends BasicSection {
    public BedtimeSection(Context context) {
        super(context);
        buildBasedOnNusach();
    }

    @Override
    protected void buildAshkenaz() {
        idList.add(R.string.Bedtime);
    }

    @Override
    protected void buildStupid() {
        idList.add(R.string.bedtime_stupid);
    }

    @Override
    protected void buildSfardi() {
        if (jDate.isNoTachanun())
            idList.add(R.string.bedtime_sfardi_notachanun);
        else
            idList.add(R.string.bedtime_sfardi_tachanun);
    }
}
