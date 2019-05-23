package com.gbksoft.debugview.appinfilib.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.gbksoft.debugview.appinfolib.AppInfo;
import com.gbksoft.debugview.appinfolib.R;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class InfoDialogFragment extends DialogFragment {

    private List<String> sharedPreferences = new ArrayList<>();

    private String tag;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_dialog, container, false);


        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupClickListener();

        setupHeader();
        setupInfoContent();
        setupFooter();
    }

    public void showDialogStickyImmersion(Activity activity, String tag) {
        this.activity = activity;
        this.tag = tag;
        show(this.activity.getFragmentManager(), this.tag);
        this.activity.getFragmentManager().executePendingTransactions();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    @SuppressLint("CheckResult")
    private void setupClickListener() {
        getView().findViewById(R.id.close).setOnClickListener(v -> {
            AppInfo.hideInfo();
            dismissAllowingStateLoss();
            getActivity().getFragmentManager().popBackStack();
        });
    }

    private void setupHeader() {
        try {
            PackageInfo pInfo = AppInfo.getContext().getPackageManager().getPackageInfo(AppInfo.getContext().getPackageName(), 0);
            ((TextView)getView().findViewById(R.id.infoVersionCode)).setText(String.format(getString(R.string.versionCode), "" + pInfo.versionCode));
            ((TextView)getView().findViewById(R.id.infoVersionName)).setText(String.format(getString(R.string.versionName), pInfo.versionName));
            ((TextView)getView().findViewById(R.id.infoPackageName)).setText(String.format(getString(R.string.packageName), pInfo.packageName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setupInfoContent() {
        StringBuilder sb = new StringBuilder();

        String permissions = setupPermissions();
        String buildConfig = setupBuildConfig();
        String sharedPrefereces = setupSharedPrefereces();
        String customData = setupCustomData();

        sb.append("<h3>Permissions</h3>");
        sb.append(permissions);
        if (!isEmpty(permissions)) {
            sb.append("<br>");
        }

        sb.append("<h3>BuildConfig</h3>");
        sb.append(buildConfig);
        if (!isEmpty(buildConfig)) {
            sb.append("<br>");
        }

        sb.append("<h3>SharedPreferences</h3>");
        sb.append(sharedPrefereces);
        if (!isEmpty(sharedPrefereces)) {
            sb.append("<br>");
        }
        sb.append(customData);

        ((TextView)getView().findViewById(R.id.info)).setText(Html.fromHtml(sb.toString()));
    }

    private String setupPermissions() {
        String[] permissions;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            PackageInfo pi = AppInfo.getContext().getPackageManager().getPackageInfo(AppInfo.getContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = pi.requestedPermissions;
            if (permissions != null) {
                for (String permission : permissions) {
                    stringBuilder.append(permission).append("\n");
                }
            } else {
                stringBuilder.append("Application doesn't require permissions");
            }
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }

        return stringBuilder.toString();
    }

    private String setupBuildConfig() {
        try {
            StringBuilder str = new StringBuilder();
            Class<?> clazz = Class.forName(AppInfo.getContext().getPackageName() + ".BuildConfig");
            Field[] fields = clazz.getFields();

            for (Field f : fields) {
                String paramName = f.getName();
                String value = String.valueOf(clazz.getDeclaredField(f.getName()).get(clazz));
                str.append(String.format("<b>%s</b>: %s", paramName, value)).append("<br>");
            }

            return str.toString();
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String setupSharedPrefereces() {
        sharedPreferences.add(AppInfo.getContext().getPackageName() + "_preferences");

        ContextWrapper cw = new ContextWrapper(AppInfo.getContext());
        File prefsDir = new File(cw.getFilesDir().getParent(), "shared_prefs");

        if (prefsDir.exists() && prefsDir.isDirectory()) {
            for (File f : prefsDir.listFiles()) {
                sharedPreferences.add(f.getName().replaceFirst("[.][^.]+$", ""));
            }
        }

        Map<String, Map<String, ?>> map = new HashMap<>();
        for (String key : sharedPreferences) {
            SharedPreferences sharedPreferences = AppInfo.getContext().getSharedPreferences(key, Context.MODE_PRIVATE);
            map.put(key, sharedPreferences.getAll());
        }

        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, Map<String, ?>> entry : map.entrySet()) {
            str.append(String.format("<b>%s</b>", entry.getKey())).append("<br>");
            for (Map.Entry<String, ?> entry1 : entry.getValue().entrySet()) {
                str.append(String.format("<b>%s</b>: %s", entry1.getKey(), entry1.getValue())).append("<br>");
            }
            str.append("<br>");
        }

        return str.toString();
    }

    private String setupCustomData() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, Map<String, ?>> entry : AppInfo.getAllCustomData().entrySet()) {
            str.append(String.format("<b>%s</b>", entry.getKey())).append("<br>");
            for (Map.Entry<String, ?> entry1 : entry.getValue().entrySet()) {
                str.append(String.format("<b>%s</b>: %s", entry1.getKey(), entry1.getValue())).append("<br>");
            }
            str.append("<br>");
        }

        return str.toString();
    }

    private void setupFooter() {

    }
}
