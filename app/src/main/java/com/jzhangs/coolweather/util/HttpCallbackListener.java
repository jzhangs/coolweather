package com.jzhangs.coolweather.util;

public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
