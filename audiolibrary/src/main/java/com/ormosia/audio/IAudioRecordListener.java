package com.ormosia.audio;


public abstract class IAudioRecordListener {

    /**
     * 录制结束
     *
     * @param audioPath 语音文件路径
     * @param duration  语音文件时长
     */
    public abstract void onFinish(String audioPath, int duration);



}
