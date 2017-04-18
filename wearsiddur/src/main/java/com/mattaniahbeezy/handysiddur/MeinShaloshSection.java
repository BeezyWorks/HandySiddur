package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 1/23/2015.
 */
public class MeinShaloshSection extends BasicSection {
    public MeinShaloshSection(Context context) {
        super(context);
        buildBasedOnNusach();
    }

    @Override
    protected void buildAshkenaz() {
        idList.add(new TextComponent(R.string.mein_Shalosh));
    }

    @Override
    protected void buildStupid() {
        buildAshkenaz();
    }

    @Override
    protected void buildSfardi() {
        idList.add(new TextComponent(R.string.mein_shalosh_sfardi));
    }


}
