package com.alangeorge.android.retrofitinvestigation.component;

import android.app.Application;

import com.alangeorge.android.retrofitinvestigation.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    Application application();
}
