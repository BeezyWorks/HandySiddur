package com.mattaniahbeezy.handysiddur;

import android.content.Context;

/**
 * Created by Mattaniah on 1/23/2015.
 */
public class Bentching extends BasicSection {
    public Bentching(Context context) {
        super(context);
        buildBasedOnNusach();
    }

    @Override
    protected void buildAshkenaz() {
        idList.add(R.string.zimun, TextComponent.SPECIAL.SMALL);
        idList.add(R.string.bentching1);

        //Modim
        idList.add(R.string.bentching2One);
        if (alHaNisim() != 0)
            idList.add(alHaNisim(), TextComponent.SPECIAL.COLOR);
        idList.add(R.string.bentching2Two);

        //Yerushaleim
        idList.add(R.string.bentching3One);
        if (yaleVyavo() != 0)
            idList.add(yaleVyavo(), TextComponent.SPECIAL.COLOR);
        idList.add(R.string.bentching3Two);

        //Tov U'Meitiv
        idList.add(R.string.bentching4);

        //HaRachamans
//        if (jDate.isSuccos())
//            harachamanComponent = new TextComponent(R.string.bentching_succosHarachaman, res);
//        else if (jDate.isRoshChodesh())
//            harachamanComponent = new TextComponent(R.string.bentching_roshHarachaman, res);
//        else
//            harachamanComponent = new TextComponent(" ");
//
//        strings.add(harachamanComponent);

        if (jDate.isMussaf())
            idList.add(R.string.bentching5_Mussaf);
        else
        idList.add(R.string.bentching5);


        idList.add(R.string.Bentching_6);
    }

    @Override
    protected void buildStupid() {
        buildAshkenaz();
    }

    @Override
    protected void buildSfardi() {

    }


}
