package com.wumart.lib.wumartlib.net;

import android.app.Application;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.wumart.lib.net.TokenInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

public class OkGoUtil {

    private static class LazyHolder {
        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder();
    }

    public static void httpGet(String url, Map<String, String> params, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        OkGo.get(url).params(params, true).tag(tag).execute(callback);
    }

    public static void httpGet(String url, Map<String, String> params, int retryCount, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        OkGo.get(url).params(params, true).retryCount(retryCount).tag(tag).execute(callback);
    }

    /**
     * 设置timeout超时时间，不会影响全局配置
     */
    public static void httpGet(String url, Map<String, String> params, long timeout, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        GetRequest getRequest = OkGo.get(url).params(params, true);
        if (timeout != 0L) {
            OkHttpClient.Builder builder = LazyHolder.BUILDER;
            builder.readTimeout(timeout, TimeUnit.SECONDS);
            builder.writeTimeout(timeout, TimeUnit.SECONDS);
            builder.connectTimeout(timeout, TimeUnit.SECONDS);
            getRequest.client(builder.build());
        }
        getRequest.tag(tag).execute(callback);
    }

    public static void httpGet(String url, Map<String, String> params, long timeout, int retryCount, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        GetRequest getRequest = OkGo.get(url).params(params, true);
        if (timeout != 0L) {
            OkHttpClient.Builder builder = LazyHolder.BUILDER;
            builder.readTimeout(timeout, TimeUnit.SECONDS);
            builder.writeTimeout(timeout, TimeUnit.SECONDS);
            builder.connectTimeout(timeout, TimeUnit.SECONDS);
            getRequest.client(builder.build());
        }
        getRequest.retryCount(retryCount).tag(tag).execute(callback);
    }

    public static void httpPost(String url, Map<String, String> params, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        OkGo.post(url).params(params, true).tag(tag).execute(callback);
    }

    public static void httpPost(String url, Map<String, String> params, int retryCount, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        OkGo.post(url).params(params, true).retryCount(retryCount).tag(tag).execute(callback);
    }

    /**
     * 设置timeout超时时间，不会影响全局配置
     */
    public static void httpPost(String url, Map<String, String> params, long timeout, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        PostRequest postRequest = OkGo.post(url).params(params, true);
        if (timeout != 0L) {
            OkHttpClient.Builder builder = LazyHolder.BUILDER;
            builder.readTimeout(timeout, TimeUnit.SECONDS);
            builder.writeTimeout(timeout, TimeUnit.SECONDS);
            builder.connectTimeout(timeout, TimeUnit.SECONDS);
            postRequest.client(builder.build());
        }
        postRequest.tag(tag).execute(callback);
    }

    public static void httpPost(String url, Map<String, String> params, long timeout, int retryCount, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        PostRequest postRequest = OkGo.post(url).params(params, true);
        if (timeout != 0L) {
            OkHttpClient.Builder builder = LazyHolder.BUILDER;
            builder.readTimeout(timeout, TimeUnit.SECONDS);
            builder.writeTimeout(timeout, TimeUnit.SECONDS);
            builder.connectTimeout(timeout, TimeUnit.SECONDS);
            postRequest.client(builder.build());
        }
        postRequest.retryCount(retryCount).tag(tag).execute(callback);
    }

    public static void httpJson(String url, Object params, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        OkGo.post(url).upJson(new Gson().toJson(params)).tag(tag).execute(callback);
    }

    public static void httpJson(String url, Object params, int retryCount, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        OkGo.post(url).upJson(new Gson().toJson(params)).retryCount(retryCount).tag(tag).execute(callback);
    }

    /**
     * 设置timeout超时时间，不会影响全局配置
     */
    public static void httpJson(String url, Object params, long timeout, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        PostRequest postRequest = OkGo.post(url).upJson(new Gson().toJson(params));
        if (timeout != 0L) {
            OkHttpClient.Builder builder = LazyHolder.BUILDER;
            builder.readTimeout(timeout, TimeUnit.SECONDS);
            builder.writeTimeout(timeout, TimeUnit.SECONDS);
            builder.connectTimeout(timeout, TimeUnit.SECONDS);
            postRequest.client(builder.build());
        }
        postRequest.tag(tag).execute(callback);
    }

    public static void httpJson(String url, Object params, long timeout, int retryCount, OkGoCallback callback) {
        Object tag = "Okhttp";
        if (callback.getHttpInterface() != null) {
            tag = callback.getHttpInterface();
        }
        PostRequest postRequest = OkGo.post(url).upJson(new Gson().toJson(params));
        if (timeout != 0L) {
            OkHttpClient.Builder builder = LazyHolder.BUILDER;
            builder.readTimeout(timeout, TimeUnit.SECONDS);
            builder.writeTimeout(timeout, TimeUnit.SECONDS);
            builder.connectTimeout(timeout, TimeUnit.SECONDS);
            postRequest.client(builder.build());
        }
        postRequest.retryCount(retryCount).tag(tag).execute(callback);
    }

    public static void initHttpClient(Application application, TokenInterceptor tokenInterceptor) {
        OkHttpClient.Builder builder = LazyHolder.BUILDER;
        //log相关
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor);                                 //添加OkGo默认debug日志
        //https相关设置
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        //超时时间设置，默认60秒
        builder.readTimeout(30L, TimeUnit.SECONDS);      //全局的读取超时时间
        builder.writeTimeout(30L, TimeUnit.SECONDS);     //全局的写入超时时间
        builder.connectTimeout(30L, TimeUnit.SECONDS);   //全局的连接超时时间
        //添加拦截器
        builder.addInterceptor(tokenInterceptor);
        //添加网络拦截器
        builder.addNetworkInterceptor(new OkGoInterceptor());
        OkGo.getInstance().init(application).setRetryCount(2).setOkHttpClient(builder.build());
    }
}
