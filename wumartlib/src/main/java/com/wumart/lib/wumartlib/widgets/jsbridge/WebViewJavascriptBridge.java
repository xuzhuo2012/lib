package com.wumart.lib.wumartlib.widgets.jsbridge;


public interface WebViewJavascriptBridge {
	public void send(String data);
	public void send(String data, CallBackFunction responseCallback);
}
