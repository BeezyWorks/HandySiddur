package com.mattaniahbeezy.handysiddur;

import android.content.res.Resources;

import java.io.Serializable;

/**
 * Created by Mattaniah on 2/23/2015.
 */
public class TextComponent implements Serializable {
    int stringId;
    SPECIAL special;

    public TextComponent(int stringId, SPECIAL special) {
        this.stringId = stringId;
        this.special = special;
    }

    public TextComponent(int stringId) {
        this.stringId = stringId;
        special = SPECIAL.NOTHING;
    }

    public CharSequence getText(Resources res) {
        return res.getText(stringId);
    }

    public int getStringId() {
        return stringId;
    }

    public SPECIAL getSpecial(){
        return special;
    }

    enum SPECIAL {NOTHING, COLOR, LARGE, SMALL}
}
