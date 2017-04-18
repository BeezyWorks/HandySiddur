package com.mattaniahbeezy.handysiddur;

/**
 * Created by Mattaniah on 2/23/2015.
 */
public class Sections {
    String title;
    TextComponent[] stringIds;
    String[] keywords;
    int drawableId;

    Sections(String title, TextComponent[] stringIds, String[] keywords, int drawableId) {
        this.title = title;
        this.stringIds = stringIds;
        this.keywords = keywords;
        this.drawableId = drawableId;
    }

    Sections(String title, int stringId, String[] keywords, int drawableId) {
        this.title = title;
        this.stringIds = new TextComponent[]{new TextComponent(stringId)};
        this.keywords = keywords;
        this.drawableId = drawableId;
    }

    Sections(String title, int stringId){
        this.title = title;
        this.stringIds = new TextComponent[]{new TextComponent(stringId)};
    }



    Sections(String title, TextComponent[] stringIds) {
        this.title = title;
        this.stringIds = stringIds;
    }

    public String getTitle() {
        return title;
    }

    public TextComponent[] getStringIds() {
        return stringIds;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public int getDrawable() {
        return drawableId;
    }
}
