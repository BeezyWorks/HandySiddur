package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 2/23/2015.
 */
public class BirkosHaShachar extends BasicSection {
    public BirkosHaShachar(Context context) {
        super(context);
        buildBasedOnNusach();
    }

    @Override
    protected void buildAshkenaz() {
        idList.add(R.string.birkashashachar1);
        idList.add(R.string.birkashashachar2);
        idList.add(R.string.birkashashachar3);
        if (jDate.isTisha())
            idList.add(R.string.birkashashachar4_tisha);
        else
            idList.add(R.string.birkashashachar4);
        idList.add(R.string.birkashashachar5);
        idList.add(R.string.birkashashachar6);
        idList.add(R.string.birkashashachar7);
        idList.add(R.string.birkashashachar8);
        idList.add(R.string.korbanos);
    }

    @Override
    protected void buildStupid() {

    }

    @Override
    protected void buildSfardi() {
        idList.add(R.string.birkas_hashachar_sfardi);
        idList.add(R.string.birkas_hashachar_sfardi2);
        idList.add(R.string.korbanos_sfardi);
    }
}
