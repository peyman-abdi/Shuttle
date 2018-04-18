package com.simplecity.amp_library.http;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simplecity.amp_library.BuildConfig;
import com.simplecity.amp_library.ShuttleApplication;
import com.simplecity.amp_library.http.ahangify.AhangifyFile;
import com.simplecity.amp_library.http.ahangify.AhangifyService;
import com.simplecity.amp_library.http.ahangify.HttpAhangifyRetryInterceptor;
import com.simplecity.amp_library.http.ahangify.HttpAhangifySecurityInterceptor;
import com.simplecity.amp_library.http.itunes.ItunesService;
import com.simplecity.amp_library.http.lastfm.LastFmService;
import com.simplecity.amp_library.model.Song;
import com.simplecity.amp_library.utils.SettingsManager;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2downloaders.OkHttpDownloader;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {

    public static final String TAG = "HttpClient";

    private static final String URL_LAST_FM = "https://ws.audioscrobbler.com/2.0/";
    private static final String URL_ITUNES = "https://itunes.apple.com/search/";
    private static final String URL_AHANGIFY = "http://10.0.2.2:1022";
    private static final String AHANGIFY_DOWNLOAD_URL_PREFIX = "/api/download/";
    public static final int    AHANGIFY_WAIT_FOR_FILE_DEFAULT = 15;

    private static HttpClient sInstance;

    public OkHttpClient okHttpClient;

    public LastFmService lastFmService;

    public ItunesService itunesService;

    public AhangifyService  ahangifyService;

    public Fetch    downloaderService;

    public Gson     gson;

    public static final String TAG_ARTWORK = "artwork";

    public static synchronized HttpClient getInstance() {
        if (sInstance == null) {
            sInstance = new HttpClient();
        }
        return sInstance;
    }

    private HttpClient() {
        gson = new GsonBuilder().create();

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpAhangifySecurityInterceptor())
                .addInterceptor(new HttpAhangifyRetryInterceptor())
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

        downloaderService = new Fetch.Builder(ShuttleApplication.getInstance().getApplicationContext(), "Downloader")
                .setDownloadConcurrentLimit(SettingsManager.getInstance().getDownloadMaxConcurrent())
                .setGlobalNetworkType(SettingsManager.getInstance().getDownloadWifiOnly() ? NetworkType.WIFI_ONLY:NetworkType.ALL)
                .setDownloader(new OkHttpDownloader(okHttpClient))
                .enableLogging(BuildConfig.DEBUG)
                .enableRetryOnNetworkGain(true)
                .build();
    }

    public static String getAhangifyFileURL(Song song, AhangifyFile track) {
        if (track == null) {
            return URL_AHANGIFY + AHANGIFY_DOWNLOAD_URL_PREFIX + song.id;
        } else {
            //@todo: change to a real file base url
            return URL_AHANGIFY + AHANGIFY_DOWNLOAD_URL_PREFIX + song.id;
        }
    }
    public static String getAhangifyStreamURL(Song song, AhangifyFile track) {
        return URL_AHANGIFY + "/api/getfile/" + song.id;
    }
    public static boolean isAhangifyFileUrl(String url) {
        return url.contains(AHANGIFY_DOWNLOAD_URL_PREFIX);
    }
    public static boolean isAhangifyRequest(String url) {
        return url.contains(HttpClient.URL_AHANGIFY);
    }
}