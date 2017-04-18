package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 1/23/2015.
 */
public class BirkasLevana extends BasicSection {
    public BirkasLevana(Context context) {
        super(context);
        buildBasedOnNusach();
    }

    @Override
    protected void buildAshkenaz() {
        idList.add(R.string.Kidush_levana);
    }

    @Override
    protected void buildStupid() {
        idList.add(R.string.kidush_levana_stupid);
    }

    @Override
    protected void buildSfardi() {
idList.add(R.string.kiddush_levana_sfardi);
    }
}
