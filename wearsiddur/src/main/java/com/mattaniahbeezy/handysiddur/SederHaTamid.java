package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 2/23/2015.
 */
public class SederHaTamid extends BasicSection {
    public SederHaTamid(Context context) {
        super(context);
        buildBasedOnNusach();
    }

    @Override
    protected void buildAshkenaz() {
        idList.add(R.string.korbanos_noBrocha);
    }

    @Override
    protected void buildStupid() {
        idList.add(R.string.mincha_korbanos_stupid);
    }

    @Override
    protected void buildSfardi() {
        idList.add(R.string.korbanos_mincha_sfardi);
    }
}
