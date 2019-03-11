package com.wumart.lib.wumartlib.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
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

import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class AgentWebView extends WebView {
    private ProgressBar progressBar;
    private TextView titleTv;
    private Map<String, String> mMap;
    private WebViewLoadInterface mLoadFinish;
    private WebViewFileChooseInterface fileChooseInterface;
    private WebViewVideoInterface viewVideoInterface;
    private ValueCallback<Uri> valueCallback;
    private ValueCallback<Uri[]> valueCallback2;
    private int FILECHOOSER_RESULTCODE = 1;
    private int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;

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

    public void setTitleTv(TextView titleTv) {
        this.titleTv = titleTv;
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

    private void setActivitTitle(String title) {
        if (null != this.titleTv) {
            this.titleTv.setText(title);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (null == this.progressBar) {
            super.onScrollChanged(l, t, oldl, oldt);
        } else {
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) this.progressBar.getLayoutParams();
            if (lp != null) {
                lp.x = l;
                lp.y = t;
                this.progressBar.setLayoutParams(lp);
            }
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
            setActivitTitle("数据加载中");
            isError = false;
            super.onPageStarted(webView, s, bitmap);
        }

        @Override
        public void onPageFinished(WebView webView, String s) {
            super.onPageFinished(webView, s);
            if (mLoadFinish != null) {
                mLoadFinish.onLoadFinish();
            }
            setActivitTitle(this.isError ? "数据加载失败" : webView.getTitle());
        }

        @Override
        public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
            this.isError = true;
            setActivitTitle("数据加载失败");
            super.onReceivedError(webView, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
            this.isError = true;
            setActivitTitle("数据加载失败");
            super.onReceivedError(webView, webResourceRequest, webResourceError);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            if (url == null) {
                return false;
            }
            if (url.startsWith("tel:")) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                getContext().startActivity(intent);
                return true;
            } else if (url.startsWith("http:")) {
                loadUrl(url, mMap);
                return true;
            } else {
                return false;
            }
        }

        @TargetApi(21)
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            String scheme = webResourceRequest.getUrl().getScheme();
            if (scheme == null) {
                return false;
            }
            if (scheme.startsWith("tel")) {
                Intent intent = new Intent("android.intent.action.VIEW", webResourceRequest.getUrl());
                getContext().startActivity(intent);
                return true;
            } else if (scheme.startsWith("http")) {
                loadUrl(webResourceRequest.getUrl().toString(), mMap);
                return true;
            } else {
                return false;
            }
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

    public void setViewVideoInterface(WebViewVideoInterface viewVideoInterface) {
        this.viewVideoInterface = viewVideoInterface;
    }

    public interface WebViewVideoInterface {
        void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback);

        void onHideCustomView();

        boolean isVideoState();
    }

    public class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (viewVideoInterface != null) {
                viewVideoInterface.onShowCustomView(view, callback);
            }
        }

        @Override
        public void onHideCustomView() {
            if (viewVideoInterface != null) {
                viewVideoInterface.onHideCustomView();
            }
        }

        @Override
        public void onProgressChanged(WebView webView, int newProgress) {
            if (null != progressBar) {
                if (newProgress == 100) {
                    progressBar.setVisibility(GONE);
                } else {
                    if (progressBar.getVisibility() == GONE) {
                        progressBar.setVisibility(VISIBLE);
                    }
                    progressBar.setProgress(newProgress);
                }
            }
            super.onProgressChanged(webView, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult jsResult) {
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
                AgentWebView.this.valueCallback = valueCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                fileChooseInterface.openFileChooserImplForAndroidLow(Intent.createChooser(intent, "文件选择"), FILECHOOSER_RESULTCODE);
            }
        }

        //Android5.0 以上版本
        private void openFileChooserImplForAndroid2(ValueCallback<Uri[]> valueCallback2) {
            if (fileChooseInterface != null) {
                AgentWebView.this.valueCallback2 = valueCallback2;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                fileChooseInterface.openFileChooserImplForAndroid5(Intent.createChooser(intent, "文件选择"), FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
            }
        }
    }

    public void onFileChooseResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == valueCallback)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            valueCallback.onReceiveValue(result);
            valueCallback = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {
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
}
