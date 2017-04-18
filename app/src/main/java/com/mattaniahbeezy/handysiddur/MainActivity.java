package com.mattaniahbeezy.handysiddur;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.mattaniahbeezy.icecreamlibrary.SettingsHelper;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Mattaniah on 8/9/2015.
 */
public class MainActivity extends AppCompatActivity {
    SettingsHelper settingsHelper;

    LinearLayout mainLinearLayout;

    View editTextLayout;
    View timeFormatLayout;

    TextView timeFormatTitle;
    SwitchCompat timeFormatSwitch;

    EditText offsetInput;
    String versionNumber="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        settingsHelper = new SettingsHelper(this);
        setContentView(R.layout.main_activity_layout);

        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.findViewById(R.id.contactButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactUs();
            }
        });

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionNumber = pInfo.versionName;
            toolbar.setSubtitle("Version "+versionNumber);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        editTextLayout = getLayoutInflater().inflate(R.layout.edittext_option, mainLinearLayout, false);
        offsetInput = (EditText) editTextLayout.findViewById(R.id.editText);

        timeFormatLayout = getLayoutInflater().inflate(R.layout.switch_options, mainLinearLayout, false);
        timeFormatTitle= (TextView) timeFormatLayout.findViewById(R.id.title);
        timeFormatSwitch = (SwitchCompat) timeFormatLayout.findViewById(R.id.switchview);
        timeFormatSwitch.setChecked(settingsHelper.isTwentyFourHours());
        final String twelveHour="12 Hrs";
        final String twentyFourHours="24 Hrs";
        timeFormatTitle.setText(settingsHelper.isTwentyFourHours()?twentyFourHours:twelveHour);

        timeFormatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeFormatSwitch.setChecked(!timeFormatSwitch.isChecked());
//                settingsHelper.setTimePattern(timeFormatSwitch.isChecked());
//                timeFormatTitle.setText(timeFormatSwitch.isChecked() ? twentyFourHours : twelveHour);
            }
        });

        timeFormatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsHelper.setTimePattern(isChecked);
                timeFormatTitle.setText(isChecked ? twentyFourHours : twelveHour);
            }
        });

        offsetInput.setText(String.valueOf(settingsHelper.getInt(SettingsHelper.candelLightKey, 18)));


        mainLinearLayout.addView(editTextLayout);
        mainLinearLayout.addView(timeFormatLayout);
    }

    private void contactUs(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"mattaniahbeezy@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Handy Siddur v" + versionNumber);
        try {
            startActivity(i);
        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Error sending email", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            int newOffset = Integer.valueOf(offsetInput.getText().toString());
            settingsHelper.setInt(SettingsHelper.candelLightKey, newOffset);
        }
        catch (NumberFormatException e){
            e.printStackTrace();
        }
    }
}
