package com.wumart.lib.wumartlib.net;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.wumart.lib.net.HttpInterface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;

import okhttp3.Call;

public abstract class OkGoCallback<T> implements Callback<T> {
    private WeakReference<HttpInterface> weakReference;
    private boolean isShowLoading;
    private boolean isShowToast;
    private Type mType;
    public static final String RESPONSE_SUCCESS = "0000";
    public static final String RESPONSE_SUCCESS2 = "100";
    public static final String RESPONSE_UNKNOW = "0101";
    public static final String RESPONSE_ERROE = "-1"; // 接口返回的错误Code
    public static final String RESPONSE_NET = "-2";   // 网络异常情况下的Code

    public OkGoCallback(HttpInterface httpInterface) {
        this(httpInterface, true, true);
    }

    public OkGoCallback(HttpInterface httpInterface, boolean isShowLoading) {
        this(httpInterface, isShowLoading, true);
    }

    public OkGoCallback(HttpInterface httpInterface, boolean isShowLoading, boolean isShowToast) {
        this.weakReference = new WeakReference<>(httpInterface);
        this.isShowLoading = isShowLoading;
        this.isShowToast = isShowToast;
    }

    @Override
    public void onStart(Request<T, ? extends Request> request) {
        HttpInterface httpInterface = weakReference.get();
        if (httpInterface != null && isShowLoading) {
            httpInterface.showLoadingView();
        }
    }

    @Override
    public void onFinish() {
        HttpInterface httpInterface = weakReference.get();
        if (httpInterface != null && isShowLoading) {
            httpInterface.hideLoadingView();
        }
        onFinishCallback();
    }

    @Override
    public T convertResponse(okhttp3.Response response) throws Throwable {
        mType = getSuperclassTypeParameter(this.getClass());
        return this.mType != null && !this.mType.toString().equals(Void.class) ? (T) (new Gson()).fromJson(response.body().string(), this.mType) : null;
    }

    public Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            return null;
        } else {
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        }
    }

    @Override
    public void onSuccess(Response<T> response) {
        if (response == null) {
            onErrorCallback(RESPONSE_UNKNOW, "服务器异常，请稍后再试！");
            return;
        }
        onSuccessCallback(response.body());
    }

    @Override
    public void onError(Response<T> response) {
        String errorCode = RESPONSE_UNKNOW;
        String msg = "服务器异常，请稍后再试！";
        if (response == null) {
            onErrorCallback(errorCode, msg);
            return;
        }
        Call call = response.getRawCall();
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        Throwable throwable = response.getException();
        if (throwable instanceof SocketTimeoutException) {
            errorCode = RESPONSE_NET;
        } else if (throwable instanceof UnknownHostException) {
            errorCode = RESPONSE_NET;
        } else if (throwable instanceof UnknownServiceException) {
            errorCode = RESPONSE_NET;
        } else if (throwable instanceof IOException) {
            errorCode = RESPONSE_NET;
        } else if (throwable instanceof OkGoInterceptor.WuIOException) {
            OkGoInterceptor.WuIOException wuIOException = (OkGoInterceptor.WuIOException) throwable;
            errorCode = wuIOException.mCode;
            msg = wuIOException.getMessage();
        }
        showErrorToast(errorCode, msg);
        onErrorCallback(errorCode, msg);
    }

    private void showErrorToast(String errorCode, String msg) {
        HttpInterface httpInterface = weakReference.get();
        if (httpInterface != null && TextUtils.equals(RESPONSE_NET, errorCode) && isShowToast) {
            httpInterface.showFailToast(msg);
        }
    }

    public HttpInterface getHttpInterface() {
        return this.weakReference.get();
    }

    @Override
    public void onCacheSuccess(Response<T> response) {

    }

    @Override
    public void uploadProgress(Progress progress) {

    }

    @Override
    public void downloadProgress(Progress progress) {

    }

    public abstract void onSuccessCallback(T body);

    public void onErrorCallback(String errorCode, String msg) {

    }

    public void onFinishCallback() {

    }
}
