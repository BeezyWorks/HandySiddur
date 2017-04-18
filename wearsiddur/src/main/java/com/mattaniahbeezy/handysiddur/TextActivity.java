package com.mattaniahbeezy.handysiddur;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by Mattaniah on 1/23/2015.
 */
public class TextActivity extends Activity {
    public static final String SECTION_KEY = "secktionKei";
    TextComponent[] ids;
    Typeface hebrewTypeface;
    int textSize = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        hebrewTypeface = Typeface.createFromAsset(getAssets(), getString(R.string.taameiTypeface));

        ids = (TextComponent[]) getIntent().getSerializableExtra(SECTION_KEY);
        if (ids == null)
            ids = new MeinShaloshSection(this).getIdList();

        final ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) MainActivity.dipToPixels(16, this);
        linearLayout.setPadding(padding, 0, padding, 0);
        scrollView.addView(linearLayout);
        for (TextComponent textId : ids) {
            TextView textView = new TextView(this);
            textView.setTextSize(textSize);
            textView.setText(textId.getStringId());
            textView.setTypeface(hebrewTypeface);
            if (textId.getSpecial() != TextComponent.SPECIAL.NOTHING)
                handleSpecial(textView, textId.getSpecial());
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(textView);
        }

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.contentFrame);
                frameLayout.addView(scrollView);
            }
        });

    }

    private void handleSpecial(TextView textView, TextComponent.SPECIAL special) {
        int specialColor = getResources().getColor(R.color.lateAfternoonColor);
        switch (special) {

            case COLOR:
                textView.setTextColor(specialColor);
                break;
            case LARGE:
                textView.setTextSize(textSize + 5);
                break;
            case SMALL:
                textView.setTextSize(textSize - 5);
                break;
        }
    }
}
