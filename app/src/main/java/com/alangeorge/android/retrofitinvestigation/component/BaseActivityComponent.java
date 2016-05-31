package com.alangeorge.android.retrofitinvestigation.component;

import android.app.Activity;

import com.alangeorge.android.retrofitinvestigation.module.ActivityModule;

import dagger.Component;

@ActivityScope
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface BaseActivityComponent {
    Activity activity();
}
