package com.mattaniahbeezy.handysiddur;

import android.content.Context;

import com.mattaniahbeezy.icecreamlibrary.DaveningInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Mattaniah on 1/23/2015.
 */
public abstract class BasicSection {
    TextComponentList idList;
    DaveningInfo jDate;
    MainActivity.Nusach nusach= MainActivity.Nusach.ASHKENAZ;

    public BasicSection(Context context) {
        idList = new TextComponentList();
        jDate = new DaveningInfo(context);
    }

    public TextComponent[] getIdList() {
        TextComponent[] retArray = new TextComponent[idList.size()];
        for(int i=0;i<retArray.length;i++)
            retArray[i]=idList.get(i);
        return retArray;
    }

    protected void buildBasedOnNusach() {
        switch (nusach){
            case ASHKENAZ:
                buildAshkenaz();
                break;
            case STUPID:
                buildStupid();
                break;
            case SFARDI:
                buildSfardi();
                break;
        }
    }

    protected abstract void buildAshkenaz();
    protected abstract void buildStupid();
    protected abstract void buildSfardi();

    protected int alHaNisim() {
        if (jDate.isChanukah())
            return R.string.alHaNisimChaunuka;
        if (jDate.isPurim())
            return R.string.alHaNisimPurim;

        return 0;
    }

    protected int yaleVyavo() {
        if (jDate.isRoshChodesh())
            return R.string.yaleVyavoRosh;
        if (jDate.isPesach())
            return R.string.yaleVyavoPesach;
        if (jDate.isSuccos())
            return R.string.yaleVyavoSuccos;
        return 0;
    }

    public class TextComponentList extends LinkedList<TextComponent>{


        public boolean add(int stringId){
            return add(new TextComponent(stringId));
        }

        public boolean add(int stringId, TextComponent.SPECIAL special){
            return add(new TextComponent(stringId, special));
        }
    }
}
