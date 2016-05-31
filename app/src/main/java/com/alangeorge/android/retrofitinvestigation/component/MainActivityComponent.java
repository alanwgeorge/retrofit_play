package com.alangeorge.android.retrofitinvestigation.component;

import com.alangeorge.android.retrofitinvestigation.MainActivity;
import com.alangeorge.android.retrofitinvestigation.module.ActivityModule;

import dagger.Component;

@ActivityScope
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface MainActivityComponent extends BaseActivityComponent {
    void inject(MainActivity mainActivity);
}
