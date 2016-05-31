package com.alangeorge.android.retrofitinvestigation;

import android.app.Service;
import android.support.v7.app.AppCompatActivity;

import com.alangeorge.android.retrofitinvestigation.component.ApplicationComponent;
import com.alangeorge.android.retrofitinvestigation.component.HasComponent;

public abstract class BaseService extends Service {

    @SuppressWarnings("unchecked")
    protected ApplicationComponent getApplicationComponent() {
        return ((HasComponent<ApplicationComponent>) getApplication()).getComponent();
    }

    protected abstract void initializeInjector();
}
