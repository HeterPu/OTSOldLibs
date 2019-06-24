package com.video.cut.interfaces;

public interface TrimVideoListener {
    void onStartTrim(long startPosition,long endPosition);

    void onFinishTrim(String url);

    void onCancel();

    void onFailed();
}
