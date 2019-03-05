package com.wumart.lib.wumartlib.net;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkGoInterceptor implements Interceptor {

    public OkGoInterceptor() {
    }

    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        MediaType mediaType = MediaType.parse("application/json; chartset='utf-8'");
        String data = this.parseDataFromBody(response.body().string());
        return response.newBuilder().body(ResponseBody.create(mediaType, data)).build();
    }

    private String parseDataFromBody(String body) throws IOException {
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException var4) {
            var4.printStackTrace();
            throw new WuIOException("服务器异常，请稍后重试", OkGoCallback.RESPONSE_UNKNOW);
        }

        if (TextUtils.equals(OkGoCallback.RESPONSE_SUCCESS, json.optString("code")) ||
                TextUtils.equals(OkGoCallback.RESPONSE_SUCCESS2, json.optString("code"))) {
            return json.optString("data");
        } else {
            if (!TextUtils.isEmpty(json.optString("msg"))) {
                throw new WuIOException(json.optString("msg"), json.optString("code"));
            } else if (!TextUtils.isEmpty(json.optString("message"))) {
                throw new WuIOException(json.optString("message"), json.optString("code"));
            }
            throw new WuIOException("服务器异常，请稍后重试", OkGoCallback.RESPONSE_UNKNOW);
        }
    }

    public class WuIOException extends IOException {
        boolean mRepetition;
        String mCode;

        public WuIOException(String detailMessage, String code) {
            super(detailMessage);
            this.mCode = code;
            this.mRepetition = TextUtils.equals("201", code);
        }

        public WuIOException(String detailMessage) {
            super(detailMessage);
        }
    }
}
