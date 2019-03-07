package com.wumart.lib.wumartlib.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wumart.lib.wumartlib.R;

public class ToastUtils {
    private static Context mContext;
    private static volatile ToastUtils mInstance;
    private static Toast mToast;
    private View layout;
    private TextView tv;
    private ImageView mImageView;

    public ToastUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    private static ToastUtils getInstance(Context context) {
        if (mInstance == null) {
            Class var1 = ToastUtils.class;
            synchronized(ToastUtils.class) {
                if (mInstance == null) {
                    mInstance = new ToastUtils(context);
                }
            }
        }
        return mInstance;
    }

    private static void getToast(int duration) {
        if (mToast == null) {
            mToast = new Toast(mContext);
            mToast.setGravity(16, 0, 0);
            mToast.setDuration(duration);
        }
    }

    public static void textToast(Context context, String text) {
        if (!TextUtils.isEmpty(text)) {
            textToast(context, text, 0, 0);
        }

    }

    public static void textToastError(Context context, String text) {
        textToast(context, text, R.drawable.toast_error);
    }

    public static void textToast(Context context, String text, int imageId) {
        if (!TextUtils.isEmpty(text)) {
            textToast(context, text, imageId, 0);
        }

    }

    public static void textToast(Context context, String text, int resId, int duration) {
        getInstance(context);
        getToast(duration);
        if (mInstance.layout == null || mInstance.tv == null) {
            mInstance.layout = LayoutInflater.from(mContext).inflate(R.layout.toast_layout, (ViewGroup)null);
            mInstance.tv = (TextView)mInstance.layout.findViewById(R.id.toast_text);
            mInstance.mImageView = (ImageView)mInstance.layout.findViewById(R.id.toast_image);
            mToast.setView(mInstance.layout);
        }

        mInstance.tv.setText(text);
        if (resId == 0) {
            resId = R.drawable.toast_success;
        }

        mInstance.mImageView.setImageResource(resId);
        mToast.show();
    }
}
