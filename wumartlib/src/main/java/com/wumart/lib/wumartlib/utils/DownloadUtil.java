package com.wumart.lib.wumartlib.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.CookieManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtil {
    private static final int DOWNLOAD_FAIL = 0;
    private static final int DOWNLOAD_PROGRESS = 1;
    private static final int DOWNLOAD_SUCCESS = 2;
    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private DownloadUtil.OnDownloadListener listener;
    private DownloadUtil.DownloadUtilHandler mHandler = new DownloadUtil.DownloadUtilHandler(this);

    public static DownloadUtil getInstance() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }

        return downloadUtil;
    }

    private DownloadUtil() {
    }

    private void handle(Message message) {
        switch(message.what) {
            case 0:
                this.listener.onDownloadFailed();
                break;
            case 1:
                this.listener.onDownloading((Integer)message.obj);
                break;
            case 2:
                this.listener.onDownloadSuccess((String)message.obj);
        }

    }

    public void download(String url, String saveDir, DownloadUtil.OnDownloadListener listener) {
        if (TextUtils.isEmpty(url)) {
            this.sendFailMessage();
        } else {
            this.listener = listener;
            String savePath = this.isExistDir(saveDir);
            String fileName = this.getNameFromUrl(url);
            final File file = new File(savePath, fileName);
            CookieManager cookieManager = CookieManager.getInstance();
            String cookieStr = cookieManager.getCookie(url);
            Request.Builder builder = new Request.Builder();
            if (!TextUtils.isEmpty(cookieStr)) {
                builder.addHeader("Cookie", cookieStr);
            }

            Request request = builder.url(url).build();
            this.okHttpClient.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    DownloadUtil.this.sendFailMessage();
                }

                public void onResponse(Call call, Response response) throws IOException {
                    InputStream is = null;
                    byte[] buf = new byte[1024];
                    FileOutputStream fos = null;

                    try {
                        if (response.isSuccessful()) {
                            is = response.body().byteStream();
                            long total = response.body().contentLength();
                            fos = new FileOutputStream(file);
                            long sum = 0L;

                            int len;
                            while((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                sum += (long)len;
                                int progress = (int)((float)sum * 1.0F / (float)total * 100.0F);
                                Message message = Message.obtain();
                                message.what = 1;
                                message.obj = progress;
                                DownloadUtil.this.mHandler.sendMessage(message);
                            }

                            fos.flush();
                            DownloadUtil.this.sendSuccess(file);
                        } else {
                            DownloadUtil.this.sendFailMessage();
                        }
                    } catch (Exception var21) {
                        var21.printStackTrace();
                        DownloadUtil.this.sendFailMessage();
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }

                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException var20) {
                            var20.printStackTrace();
                        }

                    }

                }
            });
        }
    }

    private void sendSuccess(File file) {
        Message message = Message.obtain();
        message.what = 2;
        message.obj = file.getAbsolutePath();
        this.mHandler.sendMessage(message);
    }

    private void sendFailMessage() {
        Message message = Message.obtain();
        message.what = 0;
        this.mHandler.sendMessage(message);
    }

    private String isExistDir(String saveDir) {
        File downloadFile = new File(Environment.getExternalStorageDirectory() + File.separator + saveDir);
        if (!downloadFile.exists()) {
            downloadFile.mkdir();
        }

        return downloadFile.getAbsolutePath();
    }

    @NonNull
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        void onDownloadSuccess(String var1);

        void onDownloading(int var1);

        void onDownloadFailed();
    }

    private static class DownloadUtilHandler extends Handler {
        private WeakReference<DownloadUtil> mWeakReference;

        private DownloadUtilHandler(DownloadUtil activity) {
            this.mWeakReference = new WeakReference(activity);
        }

        public void handleMessage(Message message) {
            if (this.mWeakReference != null && this.mWeakReference.get() != null) {
                ((DownloadUtil)this.mWeakReference.get()).handle(message);
            }
        }
    }
}
