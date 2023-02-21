package com.ormosia.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import net.iaround.utils.Mp3Lame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Description: 音频转码的线程
 */

public class AudioRecordThread extends Thread {
    // 音频获取源
    public static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率:44100, 22050，16000，11025
    public static int sampleRateInHz = 16000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    public static int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    // 文件完整完整路径
    private String filePath = "";
    // 文件夹路径
    private String audioPath;
    // 录音开始时间
    private long recordStartTime = 0;
    // 录音回调接口
    private AudioRecordCallBack callback = null;
    // 录音器
    private AudioRecord audioRecord = null;
    // 线程控制开关
    public boolean bActive = true;
    // 快速傅里叶变换对象（计算音量）构造快速傅里叶变换对象，用以计算录音音量
    private static FFT fft = new FFT((int) (Math.log((double) bufferSizeInBytes / 2) / Math.log((double) 2)));

    public AudioRecordThread(AudioRecord audioRecord, String audioPath, long recordStartTime, AudioRecordCallBack callback) {
        // 创建录音器
        this.audioRecord = audioRecord;
        // 初始化lame
//        Mp3Lame.initEncoder(2, sampleRateInHz, 128, 1, 3);
        this.audioPath = audioPath;
        this.filePath = audioPath + recordStartTime + ".mp3";
        this.recordStartTime = recordStartTime;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            //创建文件夹
            File dir = new File(audioPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 创建文件
            FileOutputStream fos = null;
            File file = new File(filePath);
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件

            // 开始录制
            Logger.d("AudioRecordThread", "callback.AudioRecordStart()");
            callback.AudioRecordStart(recordStartTime, filePath);

            // 准备缓冲区
            byte[] pcmData = new byte[bufferSizeInBytes];
            byte[] mp3Data = new byte[bufferSizeInBytes];
            // 这个用计算读取数据失败的情况,如果次数大于10次那么认为这个录音没获得权限,小米手机就算开了权限,开始的时候也会读不到数据,所以加个次数限制
            int roundTimes = 0;
            // 保存数据
            while (bActive == true) {
                int readsize = audioRecord.read(pcmData, 0, bufferSizeInBytes);
                Logger.d("AudioRecordThread", "audioRecord.read=" + readsize);
                if (readsize > 0) {
                    try {

                        if (readsize / 2 >= fft.FFT_N) {
                            float realIO[] = new float[fft.FFT_N];
                            int i, j;
                            for (i = j = 0; i < fft.FFT_N; i++, j += 2) {
                                realIO[i] = (pcmData[j + 1] << 8 | pcmData[j] & 0xff) / 32768.0f;
                            }
                            double dVolume = 0.0f;
                            for (i = 0; i < realIO.length; i++) {
                                dVolume += Math.abs(realIO[i]);
                            }
                            dVolume = 60 * Math.log10(1 + dVolume); // 分贝值

                            if (dVolume <= 0) {
                                roundTimes++;
                                if (roundTimes >= 10) {
                                    bActive = false;
                                    callback.AudioRecordError(recordStartTime);
                                    break;
                                }
                            } else {
                                callback.AudioVolumeFeedback(dVolume);
                            }
                        }

                        // 压缩成mp3格式
                        int mp3len = Mp3Lame.encodeBuffer(pcmData, readsize, mp3Data, bufferSizeInBytes);
                        if (mp3len > 0) {
                            // 写文件
                            fos.write(mp3Data, 0, mp3len);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (readsize == AudioRecord.ERROR_INVALID_OPERATION) {// 录音权限未获取
                    roundTimes++;
                    if (roundTimes >= 10) {
                        bActive = false;
                        callback.AudioRecordError(recordStartTime);
                        break;
                    }
                }
            }
            Logger.d("AudioRecordThread", "out of");
            fos.close();// 关闭写入流
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            callback.AudioRecordEnd(recordStartTime, filePath);
            audioRecord = null;
            callback = null;
        }
    }

}
