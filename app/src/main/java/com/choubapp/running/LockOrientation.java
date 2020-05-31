package com.choubapp.running;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class LockOrientation extends Application {
// le but de ce code est de assurer que tous les menus de l'application s'ouvrent en mode portrait et de vérouiller cette orientation
    @Override
    public void onCreate() {
        super.onCreate();

        // register to be informed of activities starting up
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onActivityStarted(Activity activity) {
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }

            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
    }
}
