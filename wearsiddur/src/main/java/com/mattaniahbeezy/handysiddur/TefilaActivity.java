package com.mattaniahbeezy.handysiddur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mattaniahbeezy.icecreamlibrary.DaveningInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Mattaniah on 2/23/2015.
 */
public class TefilaActivity extends Activity implements NavListAdapter.SectionHandler {
    WearableListView navList;
    DaveningInfo jDate;
    private static final int SPEECH_REQUEST_CODE = 0;
    private List<Sections> mDataset;
    private static final String LOGTAG = "wearsiddur Tag";
    MainActivity.Nusach nusach= MainActivity.Nusach.ASHKENAZ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);
        jDate = new DaveningInfo(this);
        mDataset = getNavList();
        navList = new WearableListView(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        navList.setLayoutManager(layoutManager);
        navList.setAdapter(new NavListAdapter(this,mDataset,this));


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.contentFrame);
                frameLayout.addView(navList);
            }
        });

        Set<String> categories = getIntent().getCategories();
        if (categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
            Log.i(LOGTAG, "app started via voice");
            displaySpeechRecognizer();
        } else {
            Log.i(LOGTAG, "app started with user tap");
        }
    }

    public void handleClick(Sections section) {
        Intent intent = new Intent(TefilaActivity.this, TextActivity.class);
        intent.putExtra(TextActivity.SECTION_KEY, section.getStringIds());
        startActivity(intent);
    }

    private List<Sections> getNavList() {
        LinkedList<Sections> retList = new LinkedList<>();
        retList.add(new Sections("ברכות השחר", new BirkosHaShachar(this).getIdList()));
        retList.add(new Sections("מודים דרבנן", R.string.Modim_drabanan));
        retList.add(new Sections("עלינו", R.string.aleinu));
        retList.add(new Sections("סדר התמיד", new SederHaTamid(this).getIdList()));
        retList.add(new Sections("ק''ש ערבית", new MaarivShma(this).getIdList()));
        retList.add(new Sections("אשרי", R.string.ashrei));
        retList.add(new Sections("קדיש יתום", getKadishYesom()));
        retList.add(new Sections("קריאת שמע", new KriasShma(this).getIdList()));
        return retList;
    }

    public static float dipToPixels(float dipValue, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            for (Sections section : mDataset) {
                try {
                    for (String keyword : section.getKeywords()) {
                        try {
                            if (spokenText.contains(keyword.toLowerCase())) {
                                handleClick(section);
                                return;
                            }
                        } catch (NullPointerException e) {
                        }
                    }
                } catch (NullPointerException e) {
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private int getKadishYesom(){
        switch (nusach){

            case ASHKENAZ:
                return R.string.kadish_yesom;
            case STUPID:
                return R.string.kadish_yesom_stupid;
            case SFARDI:
                return R.string.kadish_yesom_sfardi;
        }
        return R.string.kadish_yesom;
    }
}