package com.wumart.lib.wumartlib.widgets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wumart.lib.wumartlib.R;
import com.wumart.lib.wumartlib.utils.DownloadUtil;
import com.wumart.lib.wumartlib.utils.ToastUtils;
import com.wumart.lib.wumartlib.widgets.jsbridge.BridgeHandler;
import com.wumart.lib.wumartlib.widgets.jsbridge.BridgeUtil;
import com.wumart.lib.wumartlib.widgets.jsbridge.CallBackFunction;
import com.wumart.lib.wumartlib.widgets.jsbridge.DefaultHandler;
import com.wumart.lib.wumartlib.widgets.jsbridge.Message;
import com.wumart.lib.wumartlib.widgets.jsbridge.WebViewJavascriptBridge;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

@SuppressLint("SetJavaScriptEnabled")
public class AgentWebView extends WebView implements WebViewJavascriptBridge {
    private ProgressBar progressBar;
    private TextView titleTv;
    private Map<String, String> mMap;
    private WebViewLoadInterface mLoadFinish;
    private WebViewFileChooseInterface fileChooseInterface;
    private boolean isShowProgressBar = true;
    public boolean isChooseImage = false;
    private Uri imageUri;

    private ValueCallback<Uri> valueCallback;
    private ValueCallback<Uri[]> valueCallback2;
    private int FILECHOOSER_RESULTCODE = 1;
    private int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;

    private BridgeHandler defaultHandler = new DefaultHandler();
    private Map<String, BridgeHandler> messageHandlers = new HashMap<>();
    private Map<String, CallBackFunction> responseCallbacks = new HashMap<>();
    private long uniqueId = 0;
    private List<Message> startupMessage = new ArrayList<>();
    public static final String toLoadJs = "WebViewJavascriptBridge.js";

    public AgentWebView(Context context) {
        super(context);
        initWebView();
    }

    public AgentWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebView();
    }

    public AgentWebView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        initWebView();
    }

    private void initWebView() {
        this.clearCache(true);
        this.progressBar = new ProgressBar(this.getContext(), null, 16842872);
        this.progressBar.setProgressDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.webview_progress_bar));
        this.progressBar.setLayoutParams(new AbsoluteLayout.LayoutParams(-1, 5, 0, 0));
        this.addView(this.progressBar);
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setGeolocationEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        this.requestFocus();
        this.requestFocusFromTouch();
        this.setWebChromeClient(new MyWebChromeClient());
        this.setWebViewClient(new MyWebViewClient());
        this.setDownloadListener(new MyWebViewDownLoadListener());
    }

    @Override
    protected void onDetachedFromWindow() {
        this.mMap = null;
        this.mLoadFinish = null;
        this.progressBar = null;
        super.onDetachedFromWindow();
    }

    public void setChooseImage(boolean chooseImage) {
        this.isChooseImage = chooseImage;
    }

    public void setTitleTv(TextView titleTv) {
        this.titleTv = titleTv;
    }

    public void isShowProgressBar(boolean isShow) {
        this.isShowProgressBar = isShow;
    }

    @Override
    public void loadUrl(String data) {
        super.loadUrl(data);
    }

    @Override
    public void loadUrl(String data, Map<String, String> head) {
        super.loadUrl(data, head);
    }

    public void loadTextData(String data) {
        this.loadData(data, "text/html", "utf-8");
    }

    private void setActivityTitle(String title) {
        if (null != this.titleTv) {
            this.titleTv.setText(title);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (null == this.progressBar) {
            super.onScrollChanged(l, t, oldl, oldt);
        } else {
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) progressBar.getLayoutParams();
            lp.x = l;
            lp.y = t;
            progressBar.setLayoutParams(lp);
            super.onScrollChanged(l, t, oldl, oldt);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.canGoBack()) {
            this.goBack();
            if (null != this.mLoadFinish) {
                this.mLoadFinish.onBack();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public void setLoadFinish(WebViewLoadInterface loadInterface) {
        this.mLoadFinish = loadInterface;
    }

    public WebViewLoadInterface getLoadFinish() {
        return this.mLoadFinish;
    }

    public interface WebViewLoadInterface {
        void onLoadFinish();

        void onBack();
    }

    public class MyWebViewClient extends WebViewClient {
        private boolean isError = false;

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            setActivityTitle("数据加载中");
            isError = false;
            super.onPageStarted(webView, s, bitmap);
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            if (mLoadFinish != null) {
                mLoadFinish.onLoadFinish();
            }
            if (toLoadJs != null) {
                BridgeUtil.webViewLoadLocalJs(webView, toLoadJs);
            }

            if (getStartupMessage() != null) {
                for (Message m : getStartupMessage()) {
                    dispatchMessage(m);
                }
                setStartupMessage(null);
            }
            setActivityTitle(this.isError ? "数据加载失败" : webView.getTitle());
        }

        @Override
        public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
            this.isError = true;
            setActivityTitle("数据加载失败");
            super.onReceivedError(webView, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
            this.isError = true;
            setActivityTitle("数据加载失败");
            super.onReceivedError(webView, webResourceRequest, webResourceError);
        }

        @Override
        public void onLoadResource(WebView webView, String s) {
            super.onLoadResource(webView, s);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            if (url.startsWith("http:")) {
                loadUrl(url, mMap);
                return true;
            } else if (url.startsWith("tel:")) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                getContext().startActivity(intent);
                return true;
            } else if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
                try {
                    url = URLDecoder.decode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                handlerReturnData(url);
                return true;
            } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
                flushMessageQueue();
                return true;
            }
            return super.shouldOverrideUrlLoading(webView, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String url = request.getUrl().toString();
                if (url.startsWith("http:")) {
                    loadUrl(url, mMap);
                    return true;
                } else if (url.startsWith("tel:")) {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    getContext().startActivity(intent);
                    return true;
                } else if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) {
                    try {
                        url = URLDecoder.decode(url, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    handlerReturnData(url);
                    return true;
                } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
                    flushMessageQueue();
                    return true;
                }
            }
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    public void setFileChooseInterface(WebViewFileChooseInterface fileChooseInterface) {
        this.fileChooseInterface = fileChooseInterface;
    }

    public WebViewFileChooseInterface getFileChooseInterface() {
        return this.fileChooseInterface;
    }

    public interface WebViewFileChooseInterface {
        void openFileChooserImplForAndroidLow(Intent intent, int requestCode);

        void openFileChooserImplForAndroid5(Intent intent, int requestCode);
    }

    public class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView webView, int newProgress) {
            if (null != progressBar) {
                if (isShowProgressBar) {
                    if (newProgress == 100) {
                        progressBar.setVisibility(GONE);
                    } else {
                        if (progressBar.getVisibility() == GONE) {
                            progressBar.setVisibility(VISIBLE);
                        }
                        progressBar.setProgress(newProgress);
                    }
                } else {
                    progressBar.setVisibility(GONE);
                }
            }
            super.onProgressChanged(webView, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult jsResult) {
            ToastUtils.textToastError(webView.getContext(), message);
            jsResult.confirm();
            return true;
        }

        //扩展浏览器上传文件
        //3.0++版本
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooserImplForAndroid(uploadMsg);
        }

        //3.0--版本
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooserImplForAndroid(uploadMsg);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooserImplForAndroid(uploadMsg);
        }

        // For Android > 5.0

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
            openFileChooserImplForAndroid2(valueCallback);
            return true;
        }

        private void openFileChooserImplForAndroid(ValueCallback<Uri> valueCallback) {
            if (fileChooseInterface != null) {
                if (isChooseImage) {
                    chooseImageForAndroid(valueCallback);
                } else {
                    chooseFileForAndroid(valueCallback);
                }
            }
        }

        /**
         * 选择图片
         */
        private void chooseImageForAndroid(ValueCallback<Uri> valueCallback) {
            AgentWebView.this.valueCallback = valueCallback;
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ImplForAndroid");
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs();
            }
            File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
            imageUri = Uri.fromFile(file);
            final List<Intent> cameraIntents = new ArrayList<>();
            final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getContext().getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent i = new Intent(captureIntent);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                i.setPackage(packageName);
                i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraIntents.add(i);
            }
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            Intent intent = Intent.createChooser(i, "文件选择");
            intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
            fileChooseInterface.openFileChooserImplForAndroidLow(intent, FILECHOOSER_RESULTCODE);
        }

        /**
         * 选择文件
         */
        private void chooseFileForAndroid(ValueCallback<Uri> valueCallback) {
            AgentWebView.this.valueCallback = valueCallback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            fileChooseInterface.openFileChooserImplForAndroidLow(Intent.createChooser(intent, "文件选择"), FILECHOOSER_RESULTCODE);
        }

        //Android5.0 以上版本
        private void openFileChooserImplForAndroid2(ValueCallback<Uri[]> valueCallback2) {
            if (fileChooseInterface != null) {
                if (isChooseImage) {
                    chooseImageForAndroid2(valueCallback2);
                } else {
                    chooseFileForAndroid2(valueCallback2);
                }
            }
        }

        /**
         * 选择图片
         */
        private void chooseImageForAndroid2(ValueCallback<Uri[]> valueCallback2) {
            AgentWebView.this.valueCallback2 = valueCallback2;
            File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ImplForAndroid");
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs();
            }
            File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
            imageUri = Uri.fromFile(file);
            final List<Intent> cameraIntents = new ArrayList<>();
            final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getContext().getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent i = new Intent(captureIntent);
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                i.setPackage(packageName);
                i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraIntents.add(i);
            }
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            Intent intent = Intent.createChooser(i, "文件选择");
            intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
            fileChooseInterface.openFileChooserImplForAndroid5(intent, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
        }

        /**
         * 选择文件
         */
        private void chooseFileForAndroid2(ValueCallback<Uri[]> valueCallback2) {
            AgentWebView.this.valueCallback2 = valueCallback2;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            fileChooseInterface.openFileChooserImplForAndroid5(Intent.createChooser(intent, "文件选择"), FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
        }
    }

    public void onFileChooseResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (isChooseImage) {
                onImageChooseResult(requestCode, resultCode, intent);
            } else {
                if (null == valueCallback)
                    return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                valueCallback.onReceiveValue(result);
                valueCallback = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
            if (isChooseImage) {
                onImageChooseResult(requestCode, resultCode, intent);
            } else {
                if (null == valueCallback2)
                    return;
                Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
                if (result != null) {
                    valueCallback2.onReceiveValue(new Uri[]{result});
                } else {
                    valueCallback2.onReceiveValue(new Uri[]{});
                }
                valueCallback2 = null;
            }
        }
    }

    private void onImageChooseResult(int requestCode, int resultCode, Intent intent) {
        Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
        if (valueCallback2 != null) {
            if (requestCode != FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
                return;
            }
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (intent == null) {
                    results = new Uri[]{imageUri};
                } else {
                    String dataString = intent.getDataString();
                    ClipData clipData = intent.getClipData();
                    if (clipData != null) {
                        results = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            results[i] = item.getUri();
                        }
                    }
                    if (dataString != null)
                        results = new Uri[]{Uri.parse(dataString)};
                }
            }
            if (results != null) {
                valueCallback2.onReceiveValue(results);
                valueCallback2 = null;
            } else {
                results = new Uri[]{imageUri};
                valueCallback2.onReceiveValue(results);
                valueCallback2 = null;
            }
        } else if (valueCallback != null) {
            if (result == null) {
                valueCallback.onReceiveValue(imageUri);
                valueCallback = null;
            } else {
                valueCallback.onReceiveValue(result);
                valueCallback = null;
            }
        }
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            DownloadUtil.getInstance().download(url, "Download", new DownloadUtil.OnDownloadListener() {
                public void onDownloadSuccess(String path) {
                    ToastUtils.textToastError(getContext(), "下载成功，文件存于SD卡Download文件夹下。");
                }

                public void onDownloading(int progress) {
                }

                public void onDownloadFailed() {
                    ToastUtils.textToastError(getContext(), "下载失败，请刷新重试");
                }
            });
        }
    }


    /***********************************JsBridge**********************************/

    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     *                if js message has handler name, it will be handled by named handlers registered by native
     */
    public void setDefaultHandler(BridgeHandler handler) {
        this.defaultHandler = handler;
    }

    /**
     * 获取到CallBackFunction data执行调用并且从数据集移除
     *
     * @param url
     */
    void handlerReturnData(String url) {
        String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
        CallBackFunction f = responseCallbacks.get(functionName);
        String data = BridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            f.onCallBack(data);
            responseCallbacks.remove(functionName);
            return;
        }
    }

    @Override
    public void send(String data) {
        send(data, null);
    }

    @Override
    public void send(String data, CallBackFunction responseCallback) {
        doSend(null, data, responseCallback);
    }

    /**
     * 保存message到消息队列
     *
     * @param handlerName      handlerName
     * @param data             data
     * @param responseCallback CallBackFunction
     */
    private void doSend(String handlerName, String data, CallBackFunction responseCallback) {
        Message m = new Message();
        if (!TextUtils.isEmpty(data)) {
            m.setData(data);
        }
        if (responseCallback != null) {
            String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
            responseCallbacks.put(callbackStr, responseCallback);
            m.setCallbackId(callbackStr);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.setHandlerName(handlerName);
        }
        queueMessage(m);
    }

    /**
     * list<message> != null 添加到消息集合否则分发消息
     *
     * @param m Message
     */
    private void queueMessage(Message m) {
        if (startupMessage != null) {
            startupMessage.add(m);
        } else {
            dispatchMessage(m);
        }
    }

    /**
     * 分发message 必须在主线程才分发成功
     *
     * @param m Message
     */
    void dispatchMessage(Message m) {
        String messageJson = m.toJson();
        //escape special characters for json string  为json字符串转义特殊字符
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\')", "\\\\\'");
        messageJson = messageJson.replaceAll("%7B", URLEncoder.encode("%7B"));
        messageJson = messageJson.replaceAll("%7D", URLEncoder.encode("%7D"));
        messageJson = messageJson.replaceAll("%22", URLEncoder.encode("%22"));
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        // 必须要找主线程才会将数据传递出去 --- 划重点
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this.loadUrl(javascriptCommand);
        }
    }

    public List<Message> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<Message> startupMessage) {
        this.startupMessage = startupMessage;
    }

    /**
     * 刷新消息队列
     */
    private void flushMessageQueue() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA, new CallBackFunction() {

                @Override
                public void onCallBack(String data) {
                    // deserializeMessage 反序列化消息
                    List<Message> list = null;
                    try {
                        list = Message.toArrayList(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (list == null || list.size() == 0) {
                        return;
                    }
                    for (int i = 0; i < list.size(); i++) {
                        Message m = list.get(i);
                        String responseId = m.getResponseId();
                        // 是否是response  CallBackFunction
                        if (!TextUtils.isEmpty(responseId)) {
                            CallBackFunction function = responseCallbacks.get(responseId);
                            String responseData = m.getResponseData();
                            function.onCallBack(responseData);
                            responseCallbacks.remove(responseId);
                        } else {
                            CallBackFunction responseFunction = null;
                            // if had callbackId 如果有回调Id
                            final String callbackId = m.getCallbackId();
                            if (!TextUtils.isEmpty(callbackId)) {
                                responseFunction = new CallBackFunction() {
                                    @Override
                                    public void onCallBack(String data) {
                                        Message responseMsg = new Message();
                                        responseMsg.setResponseId(callbackId);
                                        responseMsg.setResponseData(data);
                                        queueMessage(responseMsg);
                                    }
                                };
                            } else {
                                responseFunction = new CallBackFunction() {
                                    @Override
                                    public void onCallBack(String data) {
                                        // do nothing
                                    }
                                };
                            }
                            // BridgeHandler执行
                            BridgeHandler handler;
                            if (!TextUtils.isEmpty(m.getHandlerName())) {
                                handler = messageHandlers.get(m.getHandlerName());
                            } else {
                                handler = defaultHandler;
                            }
                            if (handler != null) {
                                handler.handler(m.getData(), responseFunction);
                            }
                        }
                    }
                }
            });
        }
    }

    public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
        this.loadUrl(jsUrl);
        // 添加至 Map<String, CallBackFunction>
        responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl), returnCallback);
    }

    /**
     * register handler,so that javascript can call it
     * 注册处理程序,以便javascript调用它
     *
     * @param handlerName handlerName
     * @param handler     BridgeHandler
     */
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            // 添加至 Map<String, BridgeHandler>
            messageHandlers.put(handlerName, handler);
        }
    }

    /**
     * unregister handler
     *
     * @param handlerName
     */
    public void unregisterHandler(String handlerName) {
        if (handlerName != null) {
            messageHandlers.remove(handlerName);
        }
    }

    /**
     * call javascript registered handler
     * 调用javascript处理程序注册
     *
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    CallBackFunction
     */
    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack);
    }
}