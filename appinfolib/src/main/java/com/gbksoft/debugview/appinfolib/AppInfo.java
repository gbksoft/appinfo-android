package com.gbksoft.debugview.appinfolib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.gbksoft.debugview.appinfilib.fragments.InfoDialogFragment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class AppInfo {

    private static Context context;
    private static boolean isShake;
    private static boolean isEnabled;
    private static boolean isShow = false;

    private static Map<String, Map<String, ?>> customData = new HashMap<>();

    public static void showAppInfo(Activity activity) {
        if (isEnabled) {
            InfoDialogFragment annotationAddEditDialog = new InfoDialogFragment();
            annotationAddEditDialog.showDialogStickyImmersion(activity, "InfoDialogFragment");
        }
    }

    public static Context getContext() {
        return context;
    }

    public static void hideInfo() {
        isShow = false;
    }

    @SuppressLint("PrivateApi")
    @SuppressWarnings("unchecked")
    private static Activity getActivity() {
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
            return null;
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, Map<String, ?>> getAllCustomData() {
        return customData;
    }

    public static Map<String, ?> getCustomData(String key) {
        return customData.get(key);
    }

    public static void setCustomData(Map<String, Map<String, ?>> customData) {
        AppInfo.customData = customData;
    }

    public static void addCustomData(String key, Map<String, ?> value) {
        customData.put(key, value);
    }

    public static void removeCustomData(String key) {
        customData.remove(key);
    }

    public static void clearCustomData() {
        customData.clear();
    }



    public static class Builder {
        private final Context context;
        private boolean isShake = false;
        private boolean isEnabled = false;

        public Builder(Context appContext) {
            context = appContext;
        }

        public Builder isShake() {
            return isShake(4.0f);
        }

        public Builder isShake(float boundarySensorValue) {
            isShake = true;

            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(
                    new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent event) {
                            if (!isShow && (event.values[0] > boundarySensorValue || event.values[0] < -boundarySensorValue)) {
                                isShow = true;
                                showAppInfo(getActivity());
                            }
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int accuracy) {

                        }
                    },
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

            return this;
        }

        public Builder isEnabled(boolean value) {
            AppInfo.isEnabled = value;
            return this;
        }

        public AppInfo build() {
            return new AppInfo(this);
        }
    }

    private AppInfo(Builder builder) {
        context = builder.context;
        isShake = builder.isShake;
        isEnabled = builder.isEnabled;
    }
}
