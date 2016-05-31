package com.alangeorge.android.retrofitinvestigation;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.alangeorge.android.retrofitinvestigation.component.DaggerNetServiceComponent;
import com.alangeorge.android.retrofitinvestigation.component.NetServiceComponent;
import com.alangeorge.android.retrofitinvestigation.module.NetServiceModule;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class RetrofitService extends BaseService {
    private static final long UPDATE_INTERVAL = 10000L;
    private NetServiceComponent netServiceComponent;
    private boolean isRunning = false;
    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
    private List<URL> urls = new ArrayList<>();
    private PublishSubject<PingResult> pingSubject = PublishSubject.create();
    private RetrofitServiceBinder binder = new RetrofitServiceBinder();

    @Inject
    OkHttpClient okHttpClient;

    public RetrofitService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();

        netServiceComponent.inject(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void add(URL url) {
        if (BuildConfig.DEBUG) Timber.d("add() url = [ %s ]", url);
        urls.add(url);
        startTimer();
    }

    public void remove(URL url) {
        urls.remove(url);
        if (urls.size() == 0) {
            stopTimer();
        }
    }

    public Observable<PingResult> getPingObservable() {
        return pingSubject;
    }

    private void startTimer() {
        if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated()) {
            Timber.d("thread pool not active, starting new one");
            threadPool = new ScheduledThreadPoolExecutor(1, new ServiceThreadFactory());
        }

        if (! isRunning) {
            Timber.d("isRunning false, scheduling timer thread");
            threadPool.scheduleAtFixedRate(new Thread(){ public void run() {onTimerTick();}}, 1000L, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
            isRunning = true;
        } else {
            Timber.d("service thread already scheduled");
        }
    }
    private void onTimerTick() {
        Timber.d("tick: %s", urls.toString());

        for (final URL url : urls) {
            Timber.d("ping: %s", url.toString());
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url.toString())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(okHttpClient)
                    .build();

            Ping ping = retrofit.create(Ping.class);

            try {
                ping.ping()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.newThread())
                        .subscribe(new Subscriber<Response<ResponseBody>>() {
                            @Override
                            public void onCompleted() {
                                Timber.d("onCompleted()");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "onError() e = [ %s ]", e.getLocalizedMessage());
                            }

                            @Override
                            public void onNext(Response<ResponseBody> responseBodyResponse) {
                                Timber.d("onNext() responseBodyResponse = [ %s ]", responseBodyResponse);

                                pingSubject.onNext(new PingResult(url, responseBodyResponse.isSuccessful()));
                            }
                        });
            } catch (Exception e) {
                Timber.e(e, "something bad happened: %s", e.getLocalizedMessage());
            } finally {
                Timber.d("finally");
            }
        }
    }

    private void stopTimer() {
        if (threadPool == null || threadPool.isShutdown() || threadPool.isTerminated()) {
            Timber.d("thread pool already stopped");
        } else {
            threadPool.shutdown();
            threadPool = null;
            isRunning = false;
        }
    }

    @Override
    protected void initializeInjector() {
        netServiceComponent = DaggerNetServiceComponent.builder()
                .applicationComponent(getApplicationComponent())
                .netServiceModule(new NetServiceModule(new NetServiceModule.HttpHostInterceptor("google.com", -1, false)))
                .build();
    }

    public class RetrofitServiceBinder extends Binder {
        public RetrofitService getService() {
            return RetrofitService.this;
        }
    }

    private class ServiceThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(@SuppressWarnings("NullableProblems") Runnable runnable) {
            ThreadFactory wrappedFactory = Executors.defaultThreadFactory();

            Thread thread = wrappedFactory.newThread(runnable);

            thread.setName("BloodHoundServiceThread");

            return thread;
        }
    }

    public class PingResult {
        private URL url;
        private boolean isSuccess = false;

        public PingResult(URL url, boolean isSuccess) {
            this.url = url;
            this.isSuccess = isSuccess;
        }

        @Override
        public String toString() {
            return "PingResult{" +
                    "url=" + url +
                    ", isSuccess=" + isSuccess +
                    '}';
        }
    }

    public interface Ping {
        @GET("/")
        Single<Response<ResponseBody>> ping();
    }
}
