package com.simplecity.amp_library.http;


import android.util.Log;

import com.simplecity.amp_library.http.ahangify.AhangifyService;
import com.simplecity.amp_library.http.itunes.ItunesService;
import com.simplecity.amp_library.http.lastfm.LastFmService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {

    public static final String TAG = "HttpClient";

    private static final String URL_LAST_FM = "https://ws.audioscrobbler.com/2.0/";
    private static final String URL_ITUNES = "https://itunes.apple.com/search/";
    private static final String URL_AHANGIFY = "http://10.0.2.2:1022";

    private static HttpClient sInstance;

    public OkHttpClient okHttpClient;

    public LastFmService lastFmService;

    public ItunesService itunesService;

    public AhangifyService  ahangifyService;

    public static final String TAG_ARTWORK = "artwork";

    public static synchronized HttpClient getInstance() {
        if (sInstance == null) {
            sInstance = new HttpClient();
        }
        return sInstance;
    }

    private HttpClient() {

        okHttpClient = new OkHttpClient.Builder()
//                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.3", 8888)))
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    if (request.url().url().toString().contains(URL_AHANGIFY)) {
                        Log.d(TAG, "AHANGIFY");
                        Log.d(TAG, response.body().toString());
                    }
                    return  response;
                })
                .build();

        Retrofit lastFmRestAdapter = new Retrofit.Builder()
                .baseUrl(URL_LAST_FM)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        lastFmService = lastFmRestAdapter.create(LastFmService.class);

        Retrofit itunesRestAdapter = new Retrofit.Builder()
                .baseUrl(URL_ITUNES)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        itunesService = itunesRestAdapter.create(ItunesService.class);

        Retrofit ahangifyRestAdapter = new Retrofit.Builder()
                .baseUrl(URL_AHANGIFY)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

                .build();
        ahangifyService = ahangifyRestAdapter.create(AhangifyService.class);
    }
}