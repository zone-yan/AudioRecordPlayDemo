package com.ormosia.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import net.iaround.utils.Mp3Lame;

import java.io.File;

/**
 * 录音管理
 */
public class AudioRecordManagerNew implements AudioRecordCallBack {

    private static final String TAG = "AudioRecordManagerNew";
    private int RECORD_MAX_INTERVAL = 60;//录制最大时长
    private int RECORD_MIN_INTERVAL = 3; //录制最小时长
    private int COUNTDOWN_DURATION = 10;
    private Context mContext;
    private Handler mHandler;

    private IAudioRecordListener mAudioRecordListener;
    private View mRoot;//popWindow显示的View
    public static AudioRecordManagerNew mInstance;
    private RecordVoicePopWindow mRecordVoicePopWindow;
    private AudioRecordThread mAudioRecordThread;
    // 录音器
    private AudioRecord mAudioRecord = null;
    // 初始化录制开始时间
    private long mRecordStartTime;
    private String mAudioPath;
    private IAudioRecordListener mIAudioRecordListener;
    private int mCountdown;
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAfChangeListener;
    private boolean isError = false;
    private boolean isWillCancel = false;

    public static AudioRecordManagerNew getInstance(Context context) {
        if (mInstance == null) {
            synchronized (AudioRecordManagerNew.class) {
                if (mInstance == null) {
                    mInstance = new AudioRecordManagerNew(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 实例化 AudioRecordManager
     *
     * @param context
     */
    private AudioRecordManagerNew(Context context) {
        this.mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    /***录音部分***/

    public void initAudioRecord() {
        Logger.d(TAG, "initAudioRecord() into");
        // 创建录音器
        mAudioRecord = new AudioRecord(AudioRecordThread.audioSource, AudioRecordThread.sampleRateInHz, AudioRecordThread.channelConfig, AudioRecordThread.audioFormat, AudioRecordThread.bufferSizeInBytes);
        // 初始化lame，第一个参数是声道数量，要配合实例new AudioRecord中的参数channelConfig的单/双声道来对应，不然录音很怪
        Mp3Lame.initEncoder(1, AudioRecordThread.sampleRateInHz, 128, 1, 3);

        initAudioFocusChangeListener();
    }

    /**
     * 判断AudioRecord是否正在录音
     */
    private boolean isRecording() {
        if (mAudioRecord == null)
            return false;
        return mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    /**
     * 设置音频保存路径
     *
     * @param filePath
     */
    public void setAudioSavePath(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            this.mAudioPath = filePath;
        }

    }

    /**
     * 设置popWindow显示的View
     *
     * @param view
     */
    public void setShowRootView(View view) {
        this.mRoot = view;
    }

    private void showPopupView() {
        if (mRoot != null) {
            if (mRecordVoicePopWindow == null) {
                mRecordVoicePopWindow = new RecordVoicePopWindow(mContext);
            }
            mRecordVoicePopWindow.showAtLocation(mRoot, Gravity.CENTER, 0, 0);
        }
    }

    /**
     * 准备取消录音
     */
    public void willCancelRecord() {
        isWillCancel = true;
        if (mRecordVoicePopWindow != null) {
            mRecordVoicePopWindow.showCancelTipView();
        }
    }

    /**
     * 继续录音
     */
    public void continueRecord() {

        if (isWillCancel) {
            if (mCountdown < COUNTDOWN_DURATION) {//此时显示倒计时的情况
                setTimeoutView(mCountdown);
            } else {
                if (mRecordVoicePopWindow != null) {
                    mRecordVoicePopWindow.showRecordingTipView();
                }
            }
        }
        isWillCancel = false;
    }

    /**
     * 倒计时
     *
     * @param counter
     */
    private void setTimeoutView(int counter) {
        if (!isWillCancel) {//如果 没有 显示即将取消的布局（上滑取消，则显示倒计时，否则不显示
            if (mRecordVoicePopWindow != null) {
                mRecordVoicePopWindow.showTimeOutTipView(counter);
            }
        }

    }

    /**
     * 录制时间太短
     */
    private void setAudioShortTipView() {
        if (mRecordVoicePopWindow != null) {
            mRecordVoicePopWindow.showRecordTooShortTipView();
        }
    }

    private void initAudioFocusChangeListener() {
        this.mAudioManager = (AudioManager) this.mContext.getSystemService(Context.AUDIO_SERVICE);
        if (this.mAfChangeListener != null) {
            this.mAudioManager.abandonAudioFocus(this.mAfChangeListener);
            this.mAfChangeListener = null;
        }

        this.mAfChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                Logger.d(TAG, "OnAudioFocusChangeListener " + focusChange + "  thread: " + (Looper.myLooper() == Looper.getMainLooper()));
                if (focusChange == -1) {
                    mAudioManager.abandonAudioFocus(mAfChangeListener);
                    mAfChangeListener = null;
                    isError = true;
                    stopRecord();

                }

            }
        };
    }

    private void muteAudioFocus(AudioManager audioManager, boolean bMute) {
        if (Build.VERSION.SDK_INT < 8) {
            Logger.d(TAG, "muteAudioFocus Android 2.1 and below can not stop music");
        } else {
            if (bMute) {
                audioManager.requestAudioFocus(mAfChangeListener, 3, 2);
            } else {
                audioManager.abandonAudioFocus(mAfChangeListener);
                mAfChangeListener = null;
            }
        }
    }

    /**
     * 外部调用开始录音 开始录音
     */
    public void startRecord() {
        Logger.d(TAG, "startRecording() into");

        if (isRecording()) {
            Logger.d(TAG, "startRecording() record null or recording");
            return;
        }

        muteAudioFocus(mAudioManager, true);

        // 开始录音
        mAudioRecord.startRecording();

        mRecordStartTime = System.currentTimeMillis();
        if (TextUtils.isEmpty(mAudioPath)) {
            mAudioPath = mContext.getCacheDir().getAbsolutePath() + "voice/";
        }
        // 开启音频文件编码写入线程
        mAudioRecordThread = new AudioRecordThread(mAudioRecord, mAudioPath, mRecordStartTime, this);
        mAudioRecordThread.start();
        mHandler.postDelayed(mRecordTimeout, RECORD_MAX_INTERVAL * 1000);//最大时长
        mHandler.postDelayed(mRecordCountdown, RECORD_MAX_INTERVAL * 1000 - COUNTDOWN_DURATION * 1000);//倒计时
        mCountdown = COUNTDOWN_DURATION;
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        Logger.d(TAG, "stopRecord() into");
        mHandler.removeCallbacks(mRecordTimeout);
        mHandler.removeCallbacks(mRecordCountdown);
        muteAudioFocus(mAudioManager, false);
        if (isRecording()) {
            mAudioRecordThread.bActive = false;
            mAudioRecord.stop();// 停止录制
        }
    }

    /**
     * 释放录音器资源
     */
    public void releaseAudioRecorder() {
        Logger.d(TAG, "releaseAudioRecorder() into");
        if (mAudioRecord != null) {
            if (isRecordInit()) {
                if (isRecording()) {
                    stopRecord();// 停止录制,判断是否正在录音
                }
            }
            mAudioRecord.release();// 释放资源
            mAudioRecord = null;
            // 析构lame
            Mp3Lame.destroyEncoder();
        }
    }

    /**
     * 判断AudioRecord是否初始化
     */
    private boolean isRecordInit() {
        if (mAudioRecord == null)
            return false;
        return mAudioRecord.getRecordingState() == AudioRecord.STATE_INITIALIZED;
    }


    /**
     * 录制到了最大录音时长
     */
    private Runnable mRecordTimeout = new Runnable() {
        @Override
        public void run() {
            stopRecord();
        }
    };

    /**
     * 倒计时
     */
    private Runnable mRecordCountdown = new Runnable() {
        @Override
        public void run() {
            mCountdown--;
            if (mCountdown < 0) {
                mCountdown = 0;
                return;
            }
            setTimeoutView(mCountdown);
            mHandler.postDelayed(mRecordCountdown, 1000);

        }
    };

    @Override
    public void AudioRecordStart(long recordStartTime, String filePath) {
        Logger.d(TAG, "AudioRecordStart  filePath: " + filePath + "  recordStartTime：" + recordStartTime);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showPopupView();
                if (mRecordVoicePopWindow != null) {
                    mRecordVoicePopWindow.showRecordingTipView();
                }
            }
        });

    }

    @Override
    public void AudioVolumeFeedback(final double volume) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (mCountdown >= COUNTDOWN_DURATION) {

                    if (mRecordVoicePopWindow != null) {
                        mRecordVoicePopWindow.updateCurrentVolume((int) Math.round(volume / 16.7));
                    }
                }
            }
        });

    }

    @Override
    public void AudioRecordError(long recordStartTime) {
        isError = true;

    }

    @Override
    public void AudioRecordEnd(long recordStartTime, final String filePath) {
        Logger.d(TAG, "AudioRecordEnd  filePath: " + filePath);

        final int duration = getRecordTime();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isError || isWillCancel) {
                    deleteAudioFile();
                    isError = false;
                    isWillCancel = false;
                } else {
                    if (duration < RECORD_MIN_INTERVAL) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mRecordVoicePopWindow != null) {
                                    mRecordVoicePopWindow.dismiss();
                                }
                            }
                        }, 500);
                        setAudioShortTipView();
                        deleteAudioFile();
                        return;
                    } else {
                        if (mIAudioRecordListener != null) {
                            mIAudioRecordListener.onFinish(filePath, duration);
                        }
                    }
                }

                if (mRecordVoicePopWindow != null) {
                    mRecordVoicePopWindow.dismiss();
                }
            }
        });


    }

    /**
     * 删除文件
     */
    private void deleteAudioFile() {
        Logger.d(TAG, "deleteAudioFile");
        if (mAudioPath != null) {
            File file = new File(mAudioPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 返回录音的时长(秒)
     *
     * @return
     */
    private int getRecordTime() {
        int time = Math.round((System.currentTimeMillis() - mRecordStartTime) / 1000);
        if (time < 1) {
            time = 0;
        }
        if (time > RECORD_MAX_INTERVAL) {
            time = RECORD_MAX_INTERVAL;
        }
        return time;
    }

    public void setIAudioRecordListener(IAudioRecordListener listener) {
        mIAudioRecordListener = listener;
    }
}
