package com.xiao.nicevideoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.xiao.nicevideoplayer.NiceVideoPlayer.STATE_BUFFERING_PAUSED;
import static com.xiao.nicevideoplayer.NiceVideoPlayer.STATE_BUFFERING_PLAYING;
import static com.xiao.nicevideoplayer.NiceVideoPlayer.STATE_COMPLETED;
import static com.xiao.nicevideoplayer.NiceVideoPlayer.STATE_ERROR;
import static com.xiao.nicevideoplayer.NiceVideoPlayer.STATE_IDLE;
import static com.xiao.nicevideoplayer.NiceVideoPlayer.STATE_PAUSED;
import static com.xiao.nicevideoplayer.NiceVideoPlayer.STATE_PREPARED;
import static com.xiao.nicevideoplayer.NiceVideoPlayer.STATE_PREPARING;

/**
 * Created by XiaoJianjun on 2017/6/21.
 * 仿腾讯视频热点列表页播放器控制器.
 */
public class GLVideoPlayerController
        extends FrameLayout
        implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        ChangeClarityDialog.OnClarityChangedListener {

    public ImageView mCenterStart;
    protected TextView mTime;
    protected TextView mPosition;
    protected TextView mDuration;
    protected SeekBar mSeek;
    protected ImageView mFullScreen;
    protected LinearLayout mChangePositon;
    protected TextView mChangePositionCurrent;
    protected ProgressBar mChangePositionProgress;
    Handler mHander = new Handler();
    private Context mContext;
    private ImageView mImage;
    private LinearLayout mTop;
    private ImageView mBack;
    private TextView mTitle;
    private LinearLayout mBatteryTime;
    private ImageView mBattery;
    private LinearLayout mBottom;
    private ImageView mRestartPause;
    private TextView mClarity;
    private TextView mLength;
    private LinearLayout mLoading;
    private TextView mLoadText;
    private LinearLayout mChangeBrightness;
    private ProgressBar mChangeBrightnessProgress;
    private LinearLayout mChangeVolume;
    private ProgressBar mChangeVolumeProgress;
    private LinearLayout mError;
    private TextView mRetry;
    private LinearLayout mCompleted;
    private TextView mReplay;
    private TextView mShare;
    /*处于4g状态*/
    private LinearLayout layou_4g;
    private boolean topBottomVisible;
    private CountDownTimer mDismissTopBottomCountDownTimer;
    private List<Clarity> clarities;
    private int defaultClarityIndex;
    private ChangeClarityDialog mClarityDialog;
    private boolean hasRegisterBatteryReceiver; // 是否已经注册了电池广播
    private boolean isShowButtomView = true;
    /**
     * 电池状态即电量变化广播接收器
     */
    private BroadcastReceiver mBatterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN);
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                // 充电中
                mBattery.setImageResource(R.drawable.battery_charging);
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                // 充电完成
                mBattery.setImageResource(R.drawable.battery_full);
            } else {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int percentage = (int) (((float) level / scale) * 100);
                if (percentage <= 10) {
                    mBattery.setImageResource(R.drawable.battery_10);
                } else if (percentage <= 20) {
                    mBattery.setImageResource(R.drawable.battery_20);
                } else if (percentage <= 50) {
                    mBattery.setImageResource(R.drawable.battery_50);
                } else if (percentage <= 80) {
                    mBattery.setImageResource(R.drawable.battery_80);
                } else if (percentage <= 100) {
                    mBattery.setImageResource(R.drawable.battery_100);
                }
            }
        }
    };
    /**
     * 记录拖动的时候是否是播放状态  拖动的时候保持暂停状态
     */
    private boolean isOnTrackingState = false;
    private boolean isTracking = false;

    public GLVideoPlayerController(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public void setShowButtomView(boolean showButtomView) {
        isShowButtomView = showButtomView;
    }

    protected void init() {
        LayoutInflater.from(mContext).inflate(R.layout.tx_video_palyer_controller, this, true);

        mCenterStart = (ImageView) findViewById(R.id.center_start);
        mImage = (ImageView) findViewById(R.id.image);

        mTop = (LinearLayout) findViewById(R.id.top);
        mBack = (ImageView) findViewById(R.id.back);
        mTitle = (TextView) findViewById(R.id.title);
        mBatteryTime = (LinearLayout) findViewById(R.id.battery_time);
        mBattery = (ImageView) findViewById(R.id.battery);
        mTime = (TextView) findViewById(R.id.time);

        mBottom = (LinearLayout) findViewById(R.id.bottom);
        mRestartPause = (ImageView) findViewById(R.id.restart_or_pause);
        mPosition = (TextView) findViewById(R.id.position);
        mDuration = (TextView) findViewById(R.id.duration);
        mSeek = (SeekBar) findViewById(R.id.seek);
        mFullScreen = (ImageView) findViewById(R.id.full_screen);
        mClarity = (TextView) findViewById(R.id.clarity);
        mLength = (TextView) findViewById(R.id.length);

        mLoading = (LinearLayout) findViewById(R.id.loading);
        mLoadText = (TextView) findViewById(R.id.load_text);

        mChangePositon = (LinearLayout) findViewById(R.id.change_position);
        mChangePositionCurrent = (TextView) findViewById(R.id.change_position_current);
        mChangePositionProgress = (ProgressBar) findViewById(R.id.change_position_progress);

        mChangeBrightness = (LinearLayout) findViewById(R.id.change_brightness);
        mChangeBrightnessProgress = (ProgressBar) findViewById(R.id.change_brightness_progress);

        mChangeVolume = (LinearLayout) findViewById(R.id.change_volume);
        mChangeVolumeProgress = (ProgressBar) findViewById(R.id.change_volume_progress);

        mError = (LinearLayout) findViewById(R.id.error);
        mRetry = (TextView) findViewById(R.id.retry);

        mCompleted = (LinearLayout) findViewById(R.id.completed);


         /*处于4g状态*/
        layou_4g = (LinearLayout) findViewById(R.id.layou_4g);

        mReplay = (TextView) findViewById(R.id.replay);
        mShare = (TextView) findViewById(R.id.share);

        mTitle.setVisibility(GONE);
        mBatteryTime.setVisibility(GONE);
        mCenterStart.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mRestartPause.setOnClickListener(this);
        mFullScreen.setOnClickListener(this);
        mClarity.setOnClickListener(this);
        mRetry.setOnClickListener(this);
        mReplay.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mSeek.setOnSeekBarChangeListener(this);
        this.setOnClickListener(this);
        //RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mBottom.getLayoutParams();
        //layoutParams.bottomMargin=10;

    }

    public void show4GPlayLayout() {
        // cancelUpdateProgressTimer();
        setTopBottomVisible(false);
        mImage.setVisibility(View.GONE);
        mCompleted.setVisibility(View.GONE);
        layou_4g.setVisibility(VISIBLE);
        mCenterStart.setVisibility(GONE);
        mError.setVisibility(View.GONE);
        TextView tvPlayer = findViewById(R.id.id_tv_player);
        tvPlayer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public ImageView getmImage() {
        return mImage;
    }

    public void showVideoLayout() {
        layou_4g.setVisibility(GONE);
        mError.setVisibility(View.GONE);
        mCenterStart.setVisibility(GONE);
    }

    public void status4G() {
        show4GPlayLayout();
    }

    public void statusWifi() {
        showVideoLayout();
    }

    public void statusNoNetWork() {
        showNetErrorLayout();
    }

    protected void showNetErrorLayout() {
        // cancelUpdateProgressTimer();
        setTopBottomVisible(false);
        // mCurrentState = ;
        // onPlayStateChanged(STATE_ERROR);
        mImage.setVisibility(View.GONE);
        mCompleted.setVisibility(View.GONE);
        layou_4g.setVisibility(GONE);
        mCenterStart.setVisibility(GONE);
        mTop.setVisibility(GONE);
        mError.setVisibility(View.VISIBLE);
        mCenterStart.setVisibility(GONE);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public ImageView imageView() {
        return mImage;
    }

    public void setImage(@DrawableRes int resId) {
        mImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mImage.setImageResource(resId);
    }

    public void setLenght(long length) {
        mLength.setText(NiceUtil.formatTime(length));
    }

    public void setNiceVideoPlayer(NiceVideoPlayer niceVideoPlayer) {
    }

    /**
     * 设置清晰度
     *
     * @param clarities 清晰度及链接
     */
    public void setClarity(List<Clarity> clarities, int defaultClarityIndex) {
        if (clarities != null && clarities.size() > 1) {
            this.clarities = clarities;
            this.defaultClarityIndex = defaultClarityIndex;

            List<String> clarityGrades = new ArrayList<>();
            for (Clarity clarity : clarities) {
                clarityGrades.add(clarity.grade + " " + clarity.p);
            }
            mClarity.setText(clarities.get(defaultClarityIndex).grade);
            // 初始化切换清晰度对话框
            mClarityDialog = new ChangeClarityDialog(mContext);
            mClarityDialog.setClarityGrade(clarityGrades, defaultClarityIndex);
            mClarityDialog.setOnClarityCheckedListener(this);
        }
    }

    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case STATE_IDLE:
                break;
            case STATE_PREPARING:
                mImage.setVisibility(View.GONE);
                mLoading.setVisibility(View.VISIBLE);
                mLoadText.setText("正在准备...");
                mError.setVisibility(View.GONE);
                mCompleted.setVisibility(View.GONE);
                mTop.setVisibility(View.GONE);
                mBottom.setVisibility(View.GONE);
                mCenterStart.setVisibility(View.GONE);
                mLength.setVisibility(View.GONE);
                layou_4g.setVisibility(GONE);
                break;
            case STATE_PREPARED:
                /**
                 * 显示进度条 再隐藏
                 */
                setTopBottomVisible(true);
                mHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setTopBottomVisible(false);
                    }
                }, 2000);
                break;
            case NiceVideoPlayer.STATE_PLAYING:
                startDismissTopBottomTimer();
                showVideoLayout();
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.video_stop_btn);
                break;
            case STATE_PAUSED:
                //showVideoLayout();
                cancelDismissTopBottomTimer();
                mLoading.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.video_play_btn);
                break;
            case STATE_BUFFERING_PLAYING:
                startDismissTopBottomTimer();
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.video_stop_btn);
                mLoadText.setText("正在缓冲...");
                showVideoLayout();
                break;
            case STATE_BUFFERING_PAUSED:
                cancelDismissTopBottomTimer();
                mLoading.setVisibility(View.VISIBLE);
                mRestartPause.setImageResource(R.drawable.video_play_btn);
                mLoadText.setText("正在缓冲...");
                break;
            case STATE_ERROR:
                setTopBottomVisible(false);
                mTop.setVisibility(View.VISIBLE);
                mError.setVisibility(View.VISIBLE);
                showNetErrorLayout();
                break;
            case STATE_COMPLETED:
                setTopBottomVisible(false);
                mImage.setVisibility(View.VISIBLE);
                mCompleted.setVisibility(View.GONE);
                mRestartPause.setImageResource(R.drawable.video_play_btn);
                //reset();
                break;
        }
    }

    protected void onPlayModeChanged(int playMode) {
        switch (playMode) {
            case NiceVideoPlayer.MODE_NORMAL:
                mBack.setVisibility(View.GONE);
                mTitle.setVisibility(GONE);
                mFullScreen.setImageResource(R.drawable.full_screen_icon);
                mFullScreen.setVisibility(View.VISIBLE);
                mClarity.setVisibility(View.GONE);
                mBatteryTime.setVisibility(View.GONE);
                if (hasRegisterBatteryReceiver) {
                    mContext.unregisterReceiver(mBatterReceiver);
                    hasRegisterBatteryReceiver = false;
                }
                break;
            case NiceVideoPlayer.MODE_FULL_SCREEN:
                mBatteryTime.setVisibility(View.GONE);
                mBack.setVisibility(View.VISIBLE);
                mTitle.setVisibility(VISIBLE);
                mFullScreen.setVisibility(View.VISIBLE);
                mFullScreen.setImageResource(R.drawable.full_screen_icon);
                if (clarities != null && clarities.size() > 1) {
                    mClarity.setVisibility(View.VISIBLE);
                }
                //mBatteryTime.setVisibility(View.VISIBLE);
                if (!hasRegisterBatteryReceiver) {
                    mContext.registerReceiver(mBatterReceiver,
                            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    hasRegisterBatteryReceiver = true;
                }
                break;
            case NiceVideoPlayer.MODE_TINY_WINDOW:
                mBack.setVisibility(View.VISIBLE);
                mClarity.setVisibility(View.GONE);
                break;
        }
    }

    protected void reset() {
        topBottomVisible = false;
        cancelDismissTopBottomTimer();
        mSeek.setProgress(0);
        mSeek.setSecondaryProgress(0);

        // mCenterStart.setVisibility(View.VISIBLE);

        mImage.setVisibility(View.GONE);

        mBottom.setVisibility(View.GONE);
        mFullScreen.setImageResource(R.drawable.full_screen_icon);
        mPosition.setText("00:00");

        mLength.setVisibility(View.GONE);

        mTop.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.GONE);

        mLoading.setVisibility(View.GONE);
        mError.setVisibility(View.GONE);
        mCompleted.setVisibility(View.GONE);
    }

    /**
     * 尽量不要在onClick中直接处理控件的隐藏、显示及各种UI逻辑。
     * UI相关的逻辑都尽量到{@link #onPlayStateChanged}和{@link #onPlayModeChanged}中处理.
     */
    @Override
    public void onClick(View v) {
        if (v == mCenterStart) {

        } else if (v == mBack) {

        } else if (v == mRestartPause) {

        } else if (v == mClarity) {
            setTopBottomVisible(false); // 隐藏top、bottom
            mClarityDialog.show();     // 显示清晰度对话框
        } else if (v == mRetry) {

        } else if (v == mReplay) {
            mRetry.performClick();
        } else if (v == mShare) {
            Toast.makeText(mContext, "分享", Toast.LENGTH_SHORT).show();
        } else if (v == this) {
            setTopBottomVisible(!topBottomVisible);
        }
    }

    @Override
    public void onClarityChanged(int clarityIndex) {
        // 根据切换后的清晰度索引值，设置对应的视频链接地址，并从当前播放位置接着播放
        Clarity clarity = clarities.get(clarityIndex);
        mClarity.setText(clarity.grade);
    }

    @Override
    public void onClarityNotChanged() {
        // 清晰度没有变化，对话框消失后，需要重新显示出top、bottom
        setTopBottomVisible(true);
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    protected void setTopBottomVisible(boolean visible) {
        mTop.setVisibility(visible ? View.VISIBLE : View.GONE);

        if (isShowButtomView) {
            mBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
            topBottomVisible = visible;
        } else {
            mBottom.setVisibility(View.GONE);
            topBottomVisible = true;
        }
        if (visible) {
        } else {
            cancelDismissTopBottomTimer();
        }
    }

    public LinearLayout getmBottom() {
        return mBottom;
    }

    /**
     * 开启top、bottom自动消失的timer
     */
    protected void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (isTracking) {
        }
        LogUtil.i("seekBar==onProgressChanged");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        LogUtil.i("seekBar==onStartTrackingTouch");
        isTracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTracking = false;
    }


    protected void updateProgress() {

    }

    protected void showChangePosition(long duration, int newPositionProgress) {
        mChangePositon.setVisibility(View.VISIBLE);
        long newPosition = (long) (duration * newPositionProgress / 100f);
        mChangePositionCurrent.setText(NiceUtil.formatTime(newPosition));
        mChangePositionProgress.setProgress(newPositionProgress);
        mSeek.setProgress(newPositionProgress);
        mPosition.setText(NiceUtil.formatTime(newPosition));
    }

    protected void hideChangePosition() {
        mChangePositon.setVisibility(View.GONE);
    }

    protected void showChangeVolume(int newVolumeProgress) {
        mChangeVolume.setVisibility(View.VISIBLE);
        mChangeVolumeProgress.setProgress(newVolumeProgress);
    }

    protected void hideChangeVolume() {
        mChangeVolume.setVisibility(View.GONE);
    }

    protected void showChangeBrightness(int newBrightnessProgress) {
        mChangeBrightness.setVisibility(View.VISIBLE);
        mChangeBrightnessProgress.setProgress(newBrightnessProgress);
    }

    protected void hideChangeBrightness() {
        mChangeBrightness.setVisibility(View.GONE);
    }
}
