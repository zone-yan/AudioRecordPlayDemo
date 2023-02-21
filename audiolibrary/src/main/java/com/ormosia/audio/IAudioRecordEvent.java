package com.ormosia.audio;

public class IAudioRecordEvent {
    /**
     * 开始录音
     */
    public static final int AUDIO_RECORD_EVENT_TRIGGER = 1;

    /**
     * 抽样(一直发送回调)
     */
    public static final int AUDIO_RECORD_EVENT_SAMPLING = 2;
    /**
     * 将要取消录音
     */
    public static final int AUDIO_RECORD_EVENT_WILL_CANCEL = 3;
    /**
     * 继续录音
     */
    public static final int AUDIO_RECORD_EVENT_CONTINUE = 4;
    /**
     * 释放录音
     */
    public static final int AUDIO_RECORD_EVENT_RELEASE = 5;
    /**
     * 终止录音
     */
    public static final int AUDIO_RECORD_EVENT_ABORT = 6;
    /**
     * 录音超时
     */
    public static final int AUDIO_RECORD_EVENT_TIME_OUT = 7;
    /**
     * 录音倒计时
     */
    public static final int AUDIO_RECORD_EVENT_TICKER = 8;
    /**
     * 录音完成生成文件
     */
    public static final int AUDIO_RECORD_EVENT_SEND_FILE = 9;

    public IAudioRecordEvent() {
    }
}
