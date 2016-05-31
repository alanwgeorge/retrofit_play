package com.alangeorge.android.retrofitinvestigation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.alangeorge.android.retrofitinvestigation.component.DaggerMainActivityComponent;
import com.alangeorge.android.retrofitinvestigation.component.MainActivityComponent;
import com.alangeorge.android.retrofitinvestigation.module.ActivityModule;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends BaseActivity {
    private MainActivityComponent mainActivityComponent;
    private CompositeSubscription compositeSubscription;

    private List<URL> pingTargets = new ArrayList<>();

    private RetrofitService retrofitService;
    private boolean isRetrofitServiceBound = false;
    private ServiceConnection retrofitServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RetrofitService.RetrofitServiceBinder binder = (RetrofitService.RetrofitServiceBinder) service;
            retrofitService = binder.getService();
            isRetrofitServiceBound = true;

            if (compositeSubscription == null) {
                compositeSubscription = new CompositeSubscription();
            }

            compositeSubscription.add(retrofitService.getPingObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<RetrofitService.PingResult>() {
                        @Override
                        public void onCompleted() {
                            Timber.d("onCompleted()");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.d(e, "onError() e = [ %s ]", e.getLocalizedMessage());
                        }

                        @Override
                        public void onNext(RetrofitService.PingResult pingResult) {
                            Timber.d("onNext() pingResult = [ %s ]", pingResult);
                        }
                    }));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            retrofitService = null;
            isRetrofitServiceBound = false;
        }
    };

    public MainActivity() {
        try {
            pingTargets.add(new URL("http://www.google.com"));
            pingTargets.add(new URL("http://apple.com"));
            pingTargets.add(new URL("http://amazon.com"));
            pingTargets.add(new URL("http://yahoo.com"));
            pingTargets.add(new URL("http://foo.com"));
        } catch (MalformedURLException e) {
            Timber.e(e, "really!: %s", e.getLocalizedMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String snackMessage;

                if (pingTargets.size() == 0) {
                    snackMessage = "exhausted URL list";
                } else {
                    URL url = pingTargets.remove(0);
                    retrofitService.add(url);
                    snackMessage = "adding " + url.toString() + " to ping service";
                }

                Snackbar.make(view, snackMessage, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initializeInjector() {
        mainActivityComponent = DaggerMainActivityComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(new ActivityModule((this)))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, RetrofitService.class);
        bindService(intent, retrofitServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(retrofitServiceConnection);
    }
}
