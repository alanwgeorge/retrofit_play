package com.alangeorge.android.retrofitinvestigation.module;

import com.alangeorge.android.retrofitinvestigation.component.ServiceScope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class NetServiceModule {
    private HttpHostInterceptor httpHostInterceptor;

    public NetServiceModule(HttpHostInterceptor httpHostInterceptor) {
        this.httpHostInterceptor = httpHostInterceptor;
    }

    @Provides
    @ServiceScope
    HttpHostInterceptor provideHttpHostInterceptor() {
        return httpHostInterceptor;
    }

    @Provides
    @ServiceScope
    OkHttpClient provideOkHttpClient(HttpHostInterceptor httpHostInterceptor) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(loggingInterceptor)
                .addInterceptor(httpHostInterceptor)
                .build();
    }

    @Provides
    @ServiceScope
    Gson provideGson() {
        return new GsonBuilder().create();
    }


    public static class HttpHostInterceptor implements Interceptor {
        private String host;
        private int port = -1;
        private boolean isSsl = true;

        public HttpHostInterceptor(String host, int port, boolean isSsl) {
            this.host = host;
            this.port = port;
            this.isSsl = isSsl;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setSsl(boolean ssl) {
            isSsl = ssl;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            String host = this.host;
            if (host != null) {
                HttpUrl.Builder builder = request.url().newBuilder().host(host);
                if (isSsl) {
                    builder.scheme("https");
                } else {
                    builder.scheme("http");
                }
                if (port > 0) builder.port(port);

                HttpUrl newUrl = builder.build();

                request = request.newBuilder()
                        .url(newUrl)
                        .build();
            }

            return chain.proceed(request);
        }
    }
}
