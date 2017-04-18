package com.mattaniahbeezy.handysiddur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.crashlytics.android.Crashlytics;
import com.mattaniahbeezy.icecreamlibrary.DaveningInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Mattaniah on 1/23/2015.
 */
public class MainActivity extends Activity implements NavListAdapter.SectionHandler {
    WearableListView navList;
    DaveningInfo jDate;
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final String LOGTAG = "wearsiddur Tag";

    public static final String zmanimTitle = "לוח זמנים";
    public static final String listenTitle = "שומע אני";
    public static final String tefilosTitle="שלוש תפילות";

    final static String[] bentchingKeywords = {"amazon", "bench", "grace", "french", "stench", "drench", "ברכת", "מזון"};
    final static String[] zmanimKeywords = {"time", "sun", "זמנ", "זמן", "זמנים", "זמני", "לוח"};
    final static String[] levanaKeywords = {"moon", "sanctification", "קידוש", "לבנה"};
    final static String[] derechKeywords = {"traveler", "way", "journey", "דרך", "הדרך"};
    final static String[] bedtimeKeywords = {"bed", "sleep", "שמע", "מיטה"};
    final static String[] beisMedrashKeywords = {"beit", "midrash", "learn", "student", "talmud", "מדרש"};
    final static String[] meinShaloshKeywords = {"three", "faceted", "snack", "שלוש", "מעין"};

    public enum Nusach {
        ASHKENAZ(0), STUPID(1), SFARDI(2);
        private final int value;

        private Nusach(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static String getTitle(Nusach nusach) {
            switch (nusach) {
                case ASHKENAZ:
                    return "אשכנז";
                case STUPID:
                    return "ספרד";
                case SFARDI:
                    return "עדות המזרח";
                default:
                    return "I am error";
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_watch);
        jDate = new DaveningInfo(this);
        navList = new WearableListView(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        navList.setLayoutManager(layoutManager);
        navList.setAdapter(new NavListAdapter(this, getNavList(), this));


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
        if (section.getTitle().equals(listenTitle)) {
            displaySpeechRecognizer();
            return;
        }
        if (section.getTitle().equals(zmanimTitle))
            startActivity(new Intent(MainActivity.this, ZmanimActivity.class));
        else if(section.getTitle().equals(tefilosTitle))
            startActivity(new Intent(MainActivity.this, TefilaActivity.class));
        else {
            Intent intent = new Intent(MainActivity.this, TextActivity.class);
            intent.putExtra(TextActivity.SECTION_KEY, section.getStringIds());
            startActivity(intent);
        }
    }

    private List<Sections> getNavList() {
     List<Sections> retList = new ArrayList<>();
        retList.add(new Sections(listenTitle, null, null, 0));
        retList.add(new Sections(zmanimTitle, null, zmanimKeywords, 0));
        retList.add(new Sections("ברכת המזון", new Bentching(this).getIdList(), bentchingKeywords, 0));
        retList.add(new Sections(tefilosTitle, null, null, 0));
        if (jDate.isBirkasLevana())
            retList.add(new Sections("ברכת הלבנה", new BirkasLevana(this).getIdList(), levanaKeywords, 0));
        retList.add(new Sections("ליקוטי ברכות", R.string.LikuteiBrochos, null, 0));
        retList.add(new Sections("תפילת הדרך", new TefilasHaDerech(this).getIdList(), derechKeywords, 0));
        retList.add(new Sections("מעין שלוש", new MeinShaloshSection(this).getIdList(), meinShaloshKeywords, 0));
        retList.add(new Sections("תפילת בית מדרש", R.string.tefilas_beis_medrash, beisMedrashKeywords, 0));
        retList.add(new Sections("ק''ש על המיטה", new BedtimeSection(this).getIdList(), bedtimeKeywords, 0));
        if (!jDate.isRain())
            retList.add(new Sections("תפילת הביננו", R.string.Tefilas_haderech, null, 0));
        retList.add(new Sections("אגרת הרמב''ן", R.string.igeres_haramban));

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

            for (Sections section : getNavList()) {
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
}



