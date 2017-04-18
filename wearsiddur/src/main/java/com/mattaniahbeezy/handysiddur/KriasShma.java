package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 2/23/2015.
 */
public class KriasShma extends BasicSection {
    public KriasShma(Context context) {
        super(context);
        idList.add(R.string.kriasShma1);
        idList.add(R.string.kriasShma2);
        idList.add(R.string.kriasShma3);
    }

    @Override
    protected void buildAshkenaz() {

    }

    @Override
    protected void buildStupid() {

    }

    @Override
    protected void buildSfardi() {

    }
}
