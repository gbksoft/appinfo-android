package com.gbksoft.debugview.appinfo.activities;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.gbksoft.debugview.appinfo.databinding.ActivityMainBinding;
import com.gbksoft.debugview.appinfolib.AppInfo;
import com.gbksoft.debugview.appinfo.R;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity {

    static final String STATE_COLOR = "fonColor";

    private ActivityMainBinding layout;

    private int backgroundColor;

    private Disposable d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_main);

        FragmentManager fm = getFragmentManager();

        if (savedInstanceState != null) {
            Log.d("QQQ", "onCreate: restore");
            backgroundColor = savedInstanceState.getInt(STATE_COLOR);
            layout.root.setBackgroundColor(backgroundColor);
        }

        SharedPreferences preferences2 = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor2 = preferences2.edit();
        editor2.putString("testKeyDefault", "testValueDefault");
        editor2.commit();

        SharedPreferences preferences = this.getSharedPreferences("TEST", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("testKey", "testValue");
        editor.commit();

        SharedPreferences preferences1 = this.getSharedPreferences("QWERTY", 0);
        SharedPreferences.Editor editor1 = preferences1.edit();
        editor1.putString("qwertyKey", "qwertyValue");
        editor1.commit();

        RxView.clicks(layout.root).subscribe(v -> {
            backgroundColor = getResources().getColor(R.color.colorAccent);
            layout.root.setBackgroundColor(backgroundColor);
        });
        RxView.clicks(layout.setCustomData).subscribe(v -> {
            Map<String, String> customValue = new HashMap<>();
            customValue.put("value 1", "value 1");
            customValue.put("value 2", "value 2");
            customValue.put("value 3", "value 3");
            Map<String, Map<String, ?>> customData = new HashMap<>();
            customData.put("SET CUSTOM 1", customValue);
            AppInfo.setCustomData(customData);
        });
        RxView.clicks(layout.addCustomData).subscribe(v -> {
            Map<String, String> customData = new HashMap<>();
            customData.put("add value 1", "add value 1");
            AppInfo.addCustomData("ADD CUSTOM 2", customData);
        });
        d = RxView.clicks(layout.showAppinfo).subscribe(v -> AppInfo.showAppInfo(this));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_COLOR, backgroundColor);
        Log.d("QQQ", "onSaveInstanceState: ");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (d != null && !d.isDisposed()) {
            d.dispose();
        }
    }
}
