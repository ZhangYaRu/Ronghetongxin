package com.zhbd.beidoucommunication.view;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.zhbd.beidoucommunication.R;
import com.zhbd.beidoucommunication.manager.AudioManager;
import com.zhbd.beidoucommunication.manager.DialogManager;

public class AudioRecorderButton extends android.support.v7.widget.AppCompatButton {

    private static final int STATE_NORMAL = 1;// 默认的状态
    private static final int STATE_RECORDING = 2;// 正在录音
    private static final int STATE_WANT_TO_CANCEL = 3;// 希望取消

    private int mCurrentState = STATE_NORMAL; // 当前的状态
    private boolean isRecording = false;// 已经开始录音

    private static final int DISTANCE_Y_CANCEL = 50;

    private float mTime;
    // 是否触发longClick
    private boolean mReady;

    DialogManager mDialogManager;
    private AudioManager mAudioManager;

    private static final int MSG_AUDIO_PREPARED = 0x110;
    private static final int MSG_VOICE_CHANGED = 0x111;
    private static final int MSG_DIALOG_DIMISS = 0x112;
    private static final int MSG_RECORDER_FINISH = 0x113;

    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                    if (mTime >= 8f) {
                        //Log.e("error", "我倒呀看看到底发了几遍");
                        mHandler.sendEmptyMessage(MSG_RECORDER_FINISH);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
//                case MSG_AUDIO_PREPARED:
//
//                    break;

                case MSG_VOICE_CHANGED:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel());
                    break;

                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimissDialog();
                    break;
                case MSG_RECORDER_FINISH:
                    if (!mReady) {
                        reset();
                        return;
                    }
                    if (mCurrentState == STATE_RECORDING) {
                        mDialogManager.dimissDialog();
                        mAudioManager.release();
                        // 录音结束的回调
                        if (audioFinishRecorderListener != null) {
                            audioFinishRecorderListener.onFinish(mTime, mAudioManager.getCurrentFilePath());
                        }
                        // 想要取消
                    } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                        mDialogManager.dimissDialog();
                        mAudioManager.cancel();
                    }
                    reset();
                    break;

            }

            super.handleMessage(msg);
        }
    };
    private int action;

    /**
     * 以下2个方法是构造方法
     */
    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new DialogManager(context);

//        String dir = "/storage/sdcard0/my_weixin";
//        String dir = Environment.getExternalStorageDirectory()+/my_weixin;
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BeiDouCommunication/voiceFiles/";
// String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BeiDouCommunication/voiceFiles";
//        Log.e("rowid",dir);
//        Log.e("rowid","--------------------");
//        Log.e("rowid",context.getDatabasePath("sms").getAbsolutePath());
        mAudioManager = AudioManager.getInstance(dir);
        // 录音准备工作
        mAudioManager.prepareAudio();
//        mAudioManager.setOnAudioStateListener(new AudioManager.AudioStateListener() {
//
//            public void wellPrepared() {
//                mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
//            }
//        });
        mReady = true;

    }

    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    /**
     * 录音完成后的回调
     */
    public interface AudioFinishRecorderListener {
        void onFinish(float seconds, String filePath);
    }

    private AudioFinishRecorderListener audioFinishRecorderListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        audioFinishRecorderListener = listener;
    }

    /**
     * 屏幕的触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        action = event.getAction();
        int x = (int) event.getX();// 获得x轴坐标
        int y = (int) event.getY();// 获得y轴坐标

        switch (action) {
            // 按下时
            case MotionEvent.ACTION_DOWN:
                // 显示對話框在开始录音以后
                mAudioManager.prepareAudio();
                mDialogManager.showRecordingDialog();
                isRecording = true;
                // 开启一个线程
                new Thread(mGetVoiceLevelRunnable).start();
                mAudioManager.startTheAudio();
                // 状态改变为录音中
                changeState(STATE_RECORDING);
                break;
            // 移动手指时
            case MotionEvent.ACTION_MOVE:
                // 判断如果为正在录音
                if (isRecording) {
                    // 如果想要取消，根据x,y的坐标看是否需要取消
                    if (wantToCancle(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }

                break;
            // 抬起手指时
            case MotionEvent.ACTION_UP:
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                // 时间过短时
                //Log.e("err",mTime + "------------------");
                if (mTime < 0.8f) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    // 延迟显示对话框
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 300);
                    // 正在录音的时候，结束
                } else if (mCurrentState == STATE_RECORDING) {
                    mDialogManager.dimissDialog();
                    mAudioManager.mIsRecording = false;
                    mAudioManager.release();
                    // 录音结束的回调
                    if (audioFinishRecorderListener != null) {
                        audioFinishRecorderListener.onFinish(mTime, mAudioManager.getCurrentFilePath());
                    }
                    // 想要取消
                } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
                }
                reset();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 恢复状态及标志位
     */
    private void reset() {
        isRecording = false;
        mTime = 0;
        mReady = false;
        changeState(STATE_NORMAL);
    }

    /**
     * 取消发送语音做的操作
     *
     * @param x
     * @param y
     * @return
     */
    private boolean wantToCancle(int x, int y) {
        // 如果x坐标移动超过按钮的宽度
        if (x < 0 || x > getWidth()) {
            return true;
        }
        // 如果y坐标移动超过按钮的高度
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }
        return false;
    }

    /**
     * 改变其状态,设置对应的界面显示
     *
     * @param state
     */
    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (state) {
                // 状态为默认.没有任何操作
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.shape_bg_recorder_normal);
                    setText(getResources().getString(R.string.button_push_to_talk));
                    mDialogManager.dimissDialog();
                    break;
                // 状态为说话中,录音中
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.shape_bg_recorder_recording);
                    setText(getResources().getString(R.string.button_push_to_talk));
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                // 状态为想要取消
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.shape_bg_recorder_recording);
                    setText(getResources().getString(R.string.button_loosen_to_over));
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }
}