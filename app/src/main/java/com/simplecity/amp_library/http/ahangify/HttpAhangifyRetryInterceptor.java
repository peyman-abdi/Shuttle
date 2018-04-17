package com.simplecity.amp_library.http.ahangify;

import android.support.annotation.NonNull;
import android.util.Log;

import com.simplecity.amp_library.download.DownloadHelper;
import com.simplecity.amp_library.http.HttpClient;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.simplecity.amp_library.http.HttpClient.isAhangifyFileUrl;

/**
 * Created by peyman on 4/16/18.
 */
public class HttpAhangifyRetryInterceptor implements Interceptor {

    public static final String TAG = "HttpRetryInterceptor";

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        String requestUrl = request.url().url().toString();
        if (HttpClient.isAhangifyRequest(requestUrl) && HttpClient.isAhangifyFileUrl(requestUrl)) {
            AhangifyWaitForSeconds result = this.getWaitingInterface(request, response);
            if (result != null) {
                if (result.status == 1) {
                    Log.d(TAG, "Download not ready yet...");

                    DownloadHelper.getInstance().addWaitingRequest(result, request);
                    return new Response.Builder()
                            .request(request)
                            .code(500)
                            .message("Resource not ready yet")
                            .body(ResponseBody.create(MediaType.parse("application/text"), "Resource not ready yet"))
                            .protocol(response.protocol())
                            .headers(response.headers())
                            .build();
                } else {
                    Log.d(TAG, "Download ready and needs redirect");

                    Request redirected = request.newBuilder()
                            .headers(response.headers())
                            .method(request.method(), request.body())
                            .url(result.route)
                            .build();

                    return chain.proceed(redirected);
                }
            }
        }

        return response;
    }

    private AhangifyWaitForSeconds getWaitingInterface(Request request, Response response) {
        boolean is_json = response.body() != null && (response.body().contentType() != null && (response.body().contentType().toString().equals("application/json")));
        if (is_json && isAhangifyFileUrl(request.url().url().toString())) {
            String body = null;
            try {
                body = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return HttpClient.getInstance().gson.fromJson(body, AhangifyWaitForSeconds.class);
        }
        return null;
    }


}
