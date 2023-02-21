package com.ormosia.audio;

/**
 * @Description: 录音过程中的接口回调
 */

public interface AudioRecordCallBack {
    //录音开始
    void AudioRecordStart(long recordStartTime, String filePath);

    //录音音量反馈
    void AudioVolumeFeedback(double volume);

    //录音异常的回调(权限受限..)
    void AudioRecordError(long recordStartTime);

    //录音结束
    void AudioRecordEnd(long recordStartTime, String filePath);
}
