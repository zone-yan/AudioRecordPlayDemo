package com.ormosia.audio;


public abstract class IAudioRecordListener {

    /**
     * 初始化提示视图
     */
    public  void initTipView(){}

    /**
     * 设置倒计时提示视图
     *
     * @param counter 10秒倒计时
     */
    public void setTimeoutTipView(int counter){}

    /**
     * 设置正在录制提示视图
     */
    public void setRecordingTipView(){}

    /**
     * 设置语音长度太短提示视图
     */
    public void setAudioShortTipView(){}

    /**
     * 设置取消提示视图
     */
    public void setCancelTipView(){}

    /**
     * 销毁提示视图
     */
    public void destroyTipView(){}

    /**
     * 开始录制
     * 如果是做IM的话，这里可以发送一个消息，如：对方正在讲话
     */
    public void onStartRecord(){}

    /**
     * 分贝改变
     *
     * @param db 分贝
     */
    public void onAudioDBChanged(int db){}

    /**
     * 录制结束
     *
     * @param audioPath 语音文件路径
     * @param duration  语音文件时长
     */
    public abstract void onFinish(String audioPath, int duration);



}
