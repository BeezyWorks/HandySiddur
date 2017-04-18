package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 2/23/2015.
 */
public class MaarivShma extends BasicSection {
    public MaarivShma(Context context) {
        super(context);
        buildBasedOnNusach();
    }

    @Override
    protected void buildAshkenaz() {
        idList.add(R.string.maariv_shma1);
        idList.add(R.string.maariv_shma2);
        idList.add(R.string.maariv_shma3);

        idList.add(R.string.kriasShma1);
        idList.add(R.string.kriasShma2);
        idList.add(R.string.kriasShma3);

        idList.add(R.string.maariv_shma4);
        idList.add(R.string.maariv_shma5);
        idList.add(R.string.maariv_shma6);
    }

    @Override
    protected void buildStupid() {

    }

    @Override
    protected void buildSfardi() {

    }
}
