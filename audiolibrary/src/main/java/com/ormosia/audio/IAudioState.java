package com.ormosia.audio;

public abstract class IAudioState {
    public IAudioState() {
    }

    void enter() {
    }

    abstract void handleMessage(AudioStateMessage msg);
}