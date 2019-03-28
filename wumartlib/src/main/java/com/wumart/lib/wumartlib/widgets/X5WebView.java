package com.wumart.lib.wumartlib.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Keep;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.widget.AbsoluteLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.wumart.lib.wumartlib.R;
import com.wumart.lib.wumartlib.utils.DownloadUtil;
import com.wumart.lib.wumartlib.utils.ToastUtils;
import com.wumart.lib.wumartlib.widgets.jsbridge.CompletionHandler;
import com.wumart.lib.wumartlib.widgets.jsbridge.OnReturnValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class X5WebView extends WebView {
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

    private int callID = 0;
    private static final String BRIDGE_NAME = "_dsbridge";
    private static final String LOG_TAG = "dsBridge";
    private static boolean isDebug = false;
    private Map<String, Object> javaScriptNamespaceInterfaces = new HashMap<String, Object>();
    private volatile boolean alertBoxBlock = true;
    private JavascriptCloseWindowListener javascriptCloseWindowListener = null;
    private ArrayList<CallInfo> callInfoList;
    private InnerJavascriptInterface innerJavascriptInterface = new InnerJavascriptInterface();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public X5WebView(Context context) {
        super(context);
        initWebView();
    }

    public X5WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebView();
    }

    public X5WebView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        initWebView();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
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
        addInternalJavascriptObject();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            super.addJavascriptInterface(innerJavascriptInterface, BRIDGE_NAME);
        } else {
            settings.setUserAgentString(settings.getUserAgentString() + " _dsbridge");
        }
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
            }
            return super.shouldOverrideUrlLoading(webView, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            String url = webResourceRequest.getUrl().toString();
            if (url.startsWith("http:")) {
                loadUrl(url, mMap);
                return true;
            } else if (url.startsWith("tel:")) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                getContext().startActivity(intent);
                return true;
            }
            return super.shouldOverrideUrlLoading(webView, webResourceRequest);
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
        public boolean onJsPrompt(WebView webView, String s, String s1, String s2, com.tencent.smtt.export.external.interfaces.JsPromptResult jsPromptResult) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                String prefix = "_dsbridge=";
                if (s1.startsWith(prefix)) {
                    jsPromptResult.confirm(innerJavascriptInterface.call(s1.substring(prefix.length()), s2));
                }
            }
            return true;
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
            X5WebView.this.valueCallback = valueCallback;
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
            X5WebView.this.valueCallback = valueCallback;
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
            X5WebView.this.valueCallback2 = valueCallback2;
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
            X5WebView.this.valueCallback2 = valueCallback2;
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

    /********************************************JsBridge*******************************************/

    private class InnerJavascriptInterface {

        private void PrintDebugInfo(String error) {
            Log.d(LOG_TAG, error);
            if (isDebug) {
                evaluateJavascript(String.format("alert('%s')", "DEBUG ERR MSG:\\n" + error.replaceAll("\\'", "\\\\'")));
            }
        }

        @Keep
        @JavascriptInterface
        public String call(String methodName, String argStr) {
            String error = "Js bridge  called, but can't find a corresponded " +
                    "JavascriptInterface object , please check your code!";
            String[] nameStr = parseNamespace(methodName.trim());
            methodName = nameStr[1];
            Object jsb = javaScriptNamespaceInterfaces.get(nameStr[0]);
            JSONObject ret = new JSONObject();
            try {
                ret.put("code", -1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsb == null) {
                PrintDebugInfo(error);
                return ret.toString();
            }
            Object arg = null;
            Method method = null;
            String callback = null;

            try {
                JSONObject args = new JSONObject(argStr);
                if (args.has("_dscbstub")) {
                    callback = args.getString("_dscbstub");
                }
                if (args.has("data")) {
                    arg = args.get("data");
                }
            } catch (JSONException e) {
                error = String.format("The argument of \"%s\" must be a JSON object string!", methodName);
                PrintDebugInfo(error);
                e.printStackTrace();
                return ret.toString();
            }


            Class<?> cls = jsb.getClass();
            boolean asyn = false;
            try {
                method = cls.getMethod(methodName,
                        new Class[]{Object.class, CompletionHandler.class});
                asyn = true;
            } catch (Exception e) {
                try {
                    method = cls.getMethod(methodName, new Class[]{Object.class});
                } catch (Exception ex) {

                }
            }

            if (method == null) {
                error = "Not find method \"" + methodName + "\" implementation! please check if the  signature or namespace of the method is right ";
                PrintDebugInfo(error);
                return ret.toString();
            }


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                JavascriptInterface annotation = method.getAnnotation(JavascriptInterface.class);
                if (annotation == null) {
                    error = "Method " + methodName + " is not invoked, since  " +
                            "it is not declared with JavascriptInterface annotation! ";
                    PrintDebugInfo(error);
                    return ret.toString();
                }
            }

            Object retData;
            method.setAccessible(true);
            try {
                if (asyn) {
                    final String cb = callback;
                    method.invoke(jsb, arg, new CompletionHandler() {

                        @Override
                        public void complete(Object retValue) {
                            complete(retValue, true);
                        }

                        @Override
                        public void complete() {
                            complete(null, true);
                        }

                        @Override
                        public void setProgressData(Object value) {
                            complete(value, false);
                        }

                        private void complete(Object retValue, boolean complete) {
                            try {
                                JSONObject ret = new JSONObject();
                                ret.put("code", 0);
                                ret.put("data", retValue);
                                //retValue = URLEncoder.encode(ret.toString(), "UTF-8").replaceAll("\\+", "%20");
                                if (cb != null) {
                                    //String script = String.format("%s(JSON.parse(decodeURIComponent(\"%s\")));", cb, retValue);
                                    String script = String.format("%s(%s);", cb, ret.toString());
                                    if (complete) {
                                        script += "delete window." + cb;
                                    }
                                    //Log.d(LOG_TAG, "complete " + script);
                                    evaluateJavascript(script);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    retData = method.invoke(jsb, arg);
                    ret.put("code", 0);
                    ret.put("data", retData);
                    return ret.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = String.format("Call failed：The parameter of \"%s\" in Java is invalid.", methodName);
                PrintDebugInfo(error);
                return ret.toString();
            }
            return ret.toString();
        }

    }

    Map<Integer, OnReturnValue> handlerMap = new HashMap<>();

    public interface JavascriptCloseWindowListener {
        /**
         * @return If true, close the current activity, otherwise, do nothing.
         */
        boolean onClose();
    }

    public static void setWebContentsDebuggingEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(enabled);
        }
        isDebug = enabled;
    }

    private String[] parseNamespace(String method) {
        int pos = method.lastIndexOf('.');
        String namespace = "";
        if (pos != -1) {
            namespace = method.substring(0, pos);
            method = method.substring(pos + 1);
        }
        return new String[]{namespace, method};
    }

    @Keep
    private void addInternalJavascriptObject() {
        addJavascriptObject(new Object() {
            @Keep
            @JavascriptInterface
            public boolean hasNativeMethod(Object args) throws JSONException {
                JSONObject jsonObject = (JSONObject) args;
                String methodName = jsonObject.getString("name").trim();
                String type = jsonObject.getString("type").trim();
                String[] nameStr = parseNamespace(methodName);
                Object jsb = javaScriptNamespaceInterfaces.get(nameStr[0]);
                if (jsb != null) {
                    Class<?> cls = jsb.getClass();
                    boolean asyn = false;
                    Method method = null;
                    try {
                        method = cls.getMethod(nameStr[1],
                                new Class[]{Object.class, CompletionHandler.class});
                        asyn = true;
                    } catch (Exception e) {
                        try {
                            method = cls.getMethod(nameStr[1], new Class[]{Object.class});
                        } catch (Exception ex) {

                        }
                    }
                    if (method != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            JavascriptInterface annotation = method.getAnnotation(JavascriptInterface.class);
                            if (annotation == null) {
                                return false;
                            }
                        }
                        if ("all".equals(type) || (asyn && "asyn".equals(type) || (!asyn && "syn".equals(type)))) {
                            return true;
                        }

                    }
                }
                return false;
            }

            @Keep
            @JavascriptInterface
            public String closePage(Object object) throws JSONException {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (javascriptCloseWindowListener == null
                                || javascriptCloseWindowListener.onClose()) {
                            Context context = getContext();
                            if (context instanceof Activity) {
                                ((Activity) context).onBackPressed();
                            }
                        }
                    }
                });
                return null;
            }

            @Keep
            @JavascriptInterface
            public void disableJavascriptDialogBlock(Object object) throws JSONException {
                JSONObject jsonObject = (JSONObject) object;
                alertBoxBlock = !jsonObject.getBoolean("disable");
            }

            @Keep
            @JavascriptInterface
            public void dsinit(Object jsonObject) {
                X5WebView.this.dispatchStartupQueue();
            }

            @Keep
            @JavascriptInterface
            public void returnValue(final Object obj) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = (JSONObject) obj;
                        Object data = null;
                        try {
                            int id = jsonObject.getInt("id");
                            boolean isCompleted = jsonObject.getBoolean("complete");
                            OnReturnValue handler = handlerMap.get(id);
                            if (jsonObject.has("data")) {
                                data = jsonObject.get("data");
                            }
                            if (handler != null) {
                                handler.onValue(data);
                                if (isCompleted) {
                                    handlerMap.remove(id);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, "_dsb");
    }

    private void _evaluateJavascript(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super.evaluateJavascript(script, null);
        } else {
            super.loadUrl("javascript:" + script);
        }
    }

    public void evaluateJavascript(final String script) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                _evaluateJavascript(script);
            }
        });
    }

    @Override
    public void loadUrl(final String url) {
        if (url != null && url.startsWith("javascript:")) {
            super.loadUrl(url);
        } else {
            callInfoList = new ArrayList<>();
            super.loadUrl(url);
        }
    }

    @Override
    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {
        if (url != null && url.startsWith("javascript:")) {
            super.loadUrl(url, additionalHttpHeaders);
        } else {
            callInfoList = new ArrayList<>();
            super.loadUrl(url, additionalHttpHeaders);
        }
    }

    @Override
    public void reload() {
        callInfoList = new ArrayList<>();
        super.reload();
    }

    public void setJavascriptCloseWindowListener(JavascriptCloseWindowListener listener) {
        javascriptCloseWindowListener = listener;
    }

    private static class CallInfo {
        private String data;
        private int callbackId;
        private String method;

        CallInfo(String handlerName, int id, Object[] args) {
            if (args == null) args = new Object[0];
            data = new JSONArray(Arrays.asList(args)).toString();
            callbackId = id;
            method = handlerName;
        }

        @Override
        public String toString() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("method", method);
                jo.put("callbackId", callbackId);
                jo.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo.toString();
        }
    }

    private synchronized void dispatchStartupQueue() {
        if (callInfoList != null) {
            for (CallInfo info : callInfoList) {
                dispatchJavascriptCall(info);
            }
            callInfoList = null;
        }
    }

    private void dispatchJavascriptCall(CallInfo info) {
        evaluateJavascript(String.format("window._handleMessageFromNative(%s)", info.toString()));
    }

    public synchronized <T> void callHandler(String method, Object[] args, final OnReturnValue<T> handler) {

        CallInfo callInfo = new CallInfo(method, ++callID, args);
        if (handler != null) {
            handlerMap.put(callInfo.callbackId, handler);
        }

        if (callInfoList != null) {
            callInfoList.add(callInfo);
        } else {
            dispatchJavascriptCall(callInfo);
        }

    }

    public void callHandler(String method, Object[] args) {
        callHandler(method, args, null);
    }

    public <T> void callHandler(String method, OnReturnValue<T> handler) {
        callHandler(method, null, handler);
    }

    public void hasJavascriptMethod(String handlerName, OnReturnValue<Boolean> existCallback) {
        callHandler("_hasJavascriptMethod", new Object[]{handlerName}, existCallback);
    }

    public void addJavascriptObject(Object object, String namespace) {
        if (namespace == null) {
            namespace = "";
        }
        if (object != null) {
            javaScriptNamespaceInterfaces.put(namespace, object);
        }
    }

    public void removeJavascriptObject(String namespace) {
        if (namespace == null) {
            namespace = "";
        }
        javaScriptNamespaceInterfaces.remove(namespace);
    }

    public void disableJavascriptDialogBlock(boolean disable) {
        alertBoxBlock = !disable;
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
            return;
        }
        mainHandler.post(runnable);
    }
}
