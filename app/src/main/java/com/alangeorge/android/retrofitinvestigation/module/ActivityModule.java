package com.alangeorge.android.retrofitinvestigation.module;

import android.app.Activity;

import com.alangeorge.android.retrofitinvestigation.component.ActivityScope;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {
    private final Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    Activity provideActivity() {
        return activity;
    }
}
