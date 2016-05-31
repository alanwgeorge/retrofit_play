package com.alangeorge.android.retrofitinvestigation;

import android.app.Application;

import com.alangeorge.android.retrofitinvestigation.component.ApplicationComponent;
import com.alangeorge.android.retrofitinvestigation.component.DaggerApplicationComponent;
import com.alangeorge.android.retrofitinvestigation.component.HasComponent;
import com.alangeorge.android.retrofitinvestigation.module.ApplicationModule;

import timber.log.Timber;

public class App extends Application implements HasComponent<ApplicationComponent> {
    protected ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        initializeInjector();
    }

    protected void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    @Override
    public ApplicationComponent getComponent() {
        return applicationComponent;
    }
}
