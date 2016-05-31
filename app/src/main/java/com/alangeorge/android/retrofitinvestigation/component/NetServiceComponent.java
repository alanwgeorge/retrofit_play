package com.alangeorge.android.retrofitinvestigation.component;

import com.alangeorge.android.retrofitinvestigation.RetrofitService;
import com.alangeorge.android.retrofitinvestigation.module.NetServiceModule;

import dagger.Component;

@ServiceScope
@Component(dependencies = ApplicationComponent.class, modules = NetServiceModule.class)
public interface NetServiceComponent {
    void inject(RetrofitService retrofitService);

}
