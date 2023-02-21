package com.ormosia.audio;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class RecordVoicePopWindow extends PopupWindow {

    private View mContentView;
    private TextView mTvRcStatus;
    private ImageView mIvRcStatus;
    private ImageView mIvRcVolume;
    private TextView mTvRcTime;

    public RecordVoicePopWindow(Context context) {
        super(context);
        mContentView = LayoutInflater.from(context).inflate(R.layout.layout_pop_record_voice,null);
        setContentView(mContentView);
        initView(mContentView);

        // 设置SelectPicPopupWindow弹出窗体的宽
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        setFocusable(true);
        setOutsideTouchable(false);
        setTouchable(false);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        setBackgroundDrawable(dw);
    }

    private void initView(View view){
        mTvRcStatus = view.findViewById(R.id.tv_rc_status);
        mIvRcStatus = view.findViewById(R.id.iv_rc_status);
        mIvRcVolume = view.findViewById(R.id.iv_rc_volume);
        mTvRcTime = view.findViewById(R.id.tv_rc_time);
    }


    /**
     * 即将超时
     *
     * @param remainder
     */
    public void showTimeOutTipView(int remainder) {
        mIvRcStatus.setVisibility(View.GONE);
        mIvRcVolume.setVisibility(View.GONE);
        mTvRcStatus.setVisibility(View.VISIBLE);
        mTvRcStatus.setText(R.string.chat_list_remove_above_cancel_send);
        mTvRcTime.setText(String.format("%s", remainder));
        mTvRcTime.setVisibility(View.VISIBLE);
    }

    /**
     * 正常录制
     */
    public void showRecordingTipView() {
        mIvRcStatus.setVisibility(View.VISIBLE);
        mIvRcVolume.setVisibility(View.VISIBLE);
        mIvRcStatus.setImageResource(R.drawable.ic_record_volume_microphone);
        mTvRcStatus.setVisibility(View.VISIBLE);
        mTvRcStatus.setText(R.string.chat_list_remove_above_cancel_send);
        mTvRcTime.setVisibility(View.GONE);
    }

    /**
     * 录制时间太短
     */
    public void showRecordTooShortTipView() {
        mIvRcStatus.setVisibility(View.VISIBLE);
        mIvRcVolume.setVisibility(View.GONE);
        mIvRcStatus.setImageResource(R.drawable.ic_record_volume_warning);
        mTvRcStatus.setText(R.string.chat_list_rec_voice_short);
    }

    /**
     * 松开手指，取消发送
     */
    public void showCancelTipView() {
        mIvRcStatus.setVisibility(View.VISIBLE);
        mIvRcVolume.setVisibility(View.GONE);
        mIvRcStatus.setImageResource(R.drawable.ic_record_volume_cancel);
        mTvRcStatus.setVisibility(View.VISIBLE);
        mTvRcStatus.setText(R.string.chat_list_loosen_cancel_send);
        mTvRcTime.setVisibility(View.GONE);
    }

    /**
     * 更新当前音量大小
     */
    public void updateCurrentVolume(int decibel) {
        switch (decibel) {
            case 0:
                mIvRcVolume.setImageResource(R.drawable.ic_record_volume_01);
                break;
            case 1:
                mIvRcVolume.setImageResource(R.drawable.ic_record_volume_01);
                break;
            case 2:
                mIvRcVolume.setImageResource(R.drawable.ic_record_volume_02);
                break;
            case 3:
                mIvRcVolume.setImageResource(R.drawable.ic_record_volume_03);
                break;
            case 4:
                mIvRcVolume.setImageResource(R.drawable.ic_record_volume_04);
                break;
            case 5:
                mIvRcVolume.setImageResource(R.drawable.ic_record_volume_05);
                break;
            case 6:
                mIvRcVolume.setImageResource(R.drawable.ic_record_volume_06);
                break;
            default:
                mIvRcVolume.setImageResource(R.drawable.ic_record_volume_06);
        }
    }
}
