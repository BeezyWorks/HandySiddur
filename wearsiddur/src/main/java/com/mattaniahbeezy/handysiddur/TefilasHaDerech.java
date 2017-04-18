package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 1/23/2015.
 */
public class TefilasHaDerech extends BasicSection {
    public TefilasHaDerech(Context context) {
        super(context);
        buildBasedOnNusach();
    }

    @Override
    protected void buildAshkenaz() {
        idList.add(new TextComponent(R.string.Tefilas_haderech));
    }

    @Override
    protected void buildStupid() {

    }

    @Override
    protected void buildSfardi() {

    }
}
