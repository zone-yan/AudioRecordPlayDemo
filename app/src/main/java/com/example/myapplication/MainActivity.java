package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ormosia.audio.AudioPlayManager;
import com.ormosia.audio.AudioRecordManagerNew;
import com.ormosia.audio.IAudioPlayListener;
import com.ormosia.audio.IAudioRecordListener;

public class MainActivity extends AppCompatActivity {

    TextView sample_text;
    TextView play;
    RelativeLayout rl_root;
    AudioRecordManagerNew mAudioRecordManagerNew;
    String mAudioPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sample_text = findViewById(R.id.sample_text);
        play = findViewById(R.id.play);
        rl_root = findViewById(R.id.rl_root);
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        mAudioRecordManagerNew = AudioRecordManagerNew.getInstance(this);


        sample_text.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mAudioRecordManagerNew.startRecord();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                mAudioRecordManagerNew.stopRecord();
                return true;
            }
            return false;
        });
        play.setOnClickListener(view -> {
            if (TextUtils.isEmpty(mAudioPath)){
                Toast.makeText(this,"请先录音",Toast.LENGTH_LONG).show();
                return;
            }
            AudioPlayManager.getInstance().startPlay(this, mAudioPath, new IAudioPlayListener() {
                @Override
                public void onStart(String playPath) {
                    Log.e("ffffff", "onStart" + "  playPath==" +playPath);

                }

                @Override
                public void onStop(String playPath) {

                }

                @Override
                public void onComplete(String playPath) {

                }
            });
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 100) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initRecord();
            } else {
                Toast.makeText(this,"需要录音权限", Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initRecord(){
        Log.e("ddd","initRecord");
        mAudioRecordManagerNew.setShowRootView(rl_root);
        mAudioRecordManagerNew.initAudioRecord();
        mAudioRecordManagerNew.setIAudioRecordListener(new IAudioRecordListener() {
            @Override
            public void onFinish(String audioPath, int duration) {
                mAudioPath = audioPath;
                Log.e("ffffff", "录制成功" + "  audioPath==" +audioPath+ ", duration=="+ duration);
            }
        });
    }
}