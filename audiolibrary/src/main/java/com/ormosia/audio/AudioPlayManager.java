package com.ormosia.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;

import java.io.IOException;

/**
 * 播放管理
 */
public class AudioPlayManager implements SensorEventListener {

    private static final String TAG = "AudioPlayManager";

    private MediaPlayer _mediaPlayer;
    private IAudioPlayListener _playListener;
    private String _playingPath;
    private Sensor _sensor;
    private SensorManager _sensorManager;
    private AudioManager _audioManager;
    private PowerManager _powerManager;
    private PowerManager.WakeLock _wakeLock;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private Context context;
    private boolean _isPlaying;

    public static AudioPlayManager getInstance() {
        return AudioPlayManager.SingletonHolder.sInstance;
    }

    private AudioPlayManager() {
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float range = event.values[0];
        if (this._sensor != null && this._mediaPlayer != null) {
            if (this._mediaPlayer.isPlaying()) {
                if ((double) range > 0.0D) {
                    if (this._audioManager.getMode() == AudioManager.MODE_NORMAL) {
                        return;
                    }

                    this._audioManager.setMode(AudioManager.MODE_NORMAL);
                    this._audioManager.setSpeakerphoneOn(true);
                    final int positions = this._mediaPlayer.getCurrentPosition();

                    try {
                        this._mediaPlayer.reset();
                        this._mediaPlayer.setAudioStreamType(3);
                        this._mediaPlayer.setVolume(1.0F, 1.0F);
                        setPlayDataSource(this._playingPath);
                        this._mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            public void onPrepared(MediaPlayer mp) {
                                mp.seekTo(positions);
                            }
                        });
                        this._mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            public void onSeekComplete(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                        this._mediaPlayer.prepareAsync();
                    } catch (IOException var5) {
                        var5.printStackTrace();
                    }

                    this.setScreenOn();
                } else {
                    this.setScreenOff();
                    if (Build.VERSION.SDK_INT >= 11) {
                        if (this._audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                            return;
                        }

                        this._audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    } else {
                        if (this._audioManager.getMode() == AudioManager.MODE_IN_CALL) {
                            return;
                        }

                        this._audioManager.setMode(AudioManager.MODE_IN_CALL);
                    }

                    this._audioManager.setSpeakerphoneOn(false);
                    this.replay();
                }
            } else if ((double) range > 0.0D) {
                if (this._audioManager.getMode() == AudioManager.MODE_NORMAL) {
                    return;
                }

                this._audioManager.setMode(AudioManager.MODE_NORMAL);
                this._audioManager.setSpeakerphoneOn(true);
                this.setScreenOn();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @SuppressLint("InvalidWakeLockTag")
    private void setScreenOff() {
        if (this._wakeLock == null) {
            if (Build.VERSION.SDK_INT >= 21) {
                this._wakeLock = this._powerManager.newWakeLock(32, "AudioPlayManager");
            } else {
                Logger.e(TAG, "Does not support on level " + Build.VERSION.SDK_INT);
            }
        }
        if (this._wakeLock != null) {
            this._wakeLock.acquire();
        }
    }

    private void setScreenOn() {
        if (this._wakeLock != null) {
            this._wakeLock.setReferenceCounted(false);
            this._wakeLock.release();
            this._wakeLock = null;
        }
    }

    private void replay() {
        try {
            this._mediaPlayer.reset();
            this._mediaPlayer.setAudioStreamType(0);
            this._mediaPlayer.setVolume(1.0F, 1.0F);
            setPlayDataSource(this._playingPath);
            this._mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            this._mediaPlayer.prepareAsync();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }


    /**
     * 开始播放
     *
     * @param context
     * @param playingPath
     * @param playListener
     */
    public void startPlay(Context context, String playingPath, IAudioPlayListener playListener) {
        if (context != null && !TextUtils.isEmpty(playingPath)) {
            this.context = context;
            if (this._playListener != null && this._playingPath != null) {
                _isPlaying = false;
                this._playListener.onStop(this._playingPath);
            }

            this.resetMediaPlayer();
            this.afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    Logger.d(TAG, "OnAudioFocusChangeListener " + focusChange);
                    if (_audioManager != null && focusChange == -1) {
                        _audioManager.abandonAudioFocus(afChangeListener);
                        afChangeListener = null;
                        resetMediaPlayer();
                    }

                }
            };

            try {
                this._powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                this._audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                /**去掉距离感应监听**/
//                if (!this._audioManager.isWiredHeadsetOn()) {
//                    this._sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//                    this._sensor = this._sensorManager.getDefaultSensor(8);
//                    this._sensorManager.registerListener(this, this._sensor, 3);
//                }
                /**去掉距离感应监听**/

                this.muteAudioFocus(this._audioManager, true);
                this._playListener = playListener;
                this._playingPath = playingPath;
                this._mediaPlayer = new MediaPlayer();
                this._mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        if (_playListener != null) {
                            _playListener.onComplete(_playingPath);
                            _playListener = null;
                            AudioPlayManager.this.context = null;
                            _isPlaying = false;
                        }

                        reset();
                    }
                });
                this._mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        reset();
                        return true;
                    }
                });
                setPlayDataSource(_playingPath);
                this._mediaPlayer.setAudioStreamType(3);
                this._mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        _mediaPlayer.start();
                        if (_playListener != null) {
                            _isPlaying = true;
                            _playListener.onStart(_playingPath);
                        }
                    }
                });
                this._mediaPlayer.prepareAsync();
            } catch (Exception var5) {
                var5.printStackTrace();
                if (this._playListener != null) {
                    this._playListener.onStop(playingPath);
                    this._playListener = null;
                    _isPlaying = false;
                }

                this.reset();
            }

        } else {
            Logger.e(TAG, "startPlay context or audioUri is null.");
        }
    }

    private void setPlayDataSource(String playPath) throws IOException {
        if (playPath.startsWith("http://") || playPath.startsWith("https://")) {
            this._mediaPlayer.setDataSource(playPath);
        } else {
            Uri uri = Uri.parse(this._playingPath);
            this._mediaPlayer.setDataSource(this.context, uri);
        }
    }



    /**
     * 停止播放
     */
    public void stopPlay() {
        if (this._playListener != null && this._playingPath != null) {
            _isPlaying = false;
            this._playListener.onStop(this._playingPath);
        }

        this.reset();
    }


    private void reset() {
        this.resetMediaPlayer();
        this.resetAudioPlayManager();
    }

    private void resetAudioPlayManager() {
        if (this._audioManager != null) {
            this.muteAudioFocus(this._audioManager, false);
        }

        if (this._sensorManager != null) {
            this._sensorManager.unregisterListener(this);
        }

        this._sensorManager = null;
        this._sensor = null;
        this._powerManager = null;
        this._audioManager = null;
        this._wakeLock = null;
        this._playListener = null;
        this._playingPath = null;
        this._isPlaying = false;
    }

    private void resetMediaPlayer() {
        if (this._mediaPlayer != null) {
            try {
                _isPlaying = false;
                this._mediaPlayer.stop();
                this._mediaPlayer.reset();
                this._mediaPlayer.release();
                this._mediaPlayer = null;
            } catch (IllegalStateException var2) {
                ;
            }
        }
    }

    private void muteAudioFocus(AudioManager audioManager, boolean bMute) {
        if (Build.VERSION.SDK_INT < 8) {
            Logger.d(TAG, "muteAudioFocus Android 2.1 and below can not stop music");
        } else {
            if (bMute) {
                audioManager.requestAudioFocus(this.afChangeListener, 3, 2);
            } else {
                audioManager.abandonAudioFocus(this.afChangeListener);
                this.afChangeListener = null;
            }

        }
    }

    public MediaPlayer getMediaPlayer(){
        return this._mediaPlayer;
    }

    public boolean isPlaying() {
        return _isPlaying;
    }

    static class SingletonHolder {
        static AudioPlayManager sInstance = new AudioPlayManager();

        SingletonHolder() {
        }
    }
}
