package com.ormosia.audio;

public interface IAudioPlayListener {
    void onStart(String playPath);

    void onStop(String playPath);

    void onComplete(String playPath);
}