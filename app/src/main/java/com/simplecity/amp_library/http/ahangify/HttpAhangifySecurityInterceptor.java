package com.simplecity.amp_library.http.ahangify;

import com.simplecity.amp_library.http.HttpClient;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by peyman on 4/16/18.
 */
public class HttpAhangifySecurityInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        boolean isAhangifyRequest = HttpClient.isAhangifyRequest(request.url().url().toString());
        if (isAhangifyRequest) {
            //@todo add authorization token
        }
        return chain.proceed(request);
    }

}
