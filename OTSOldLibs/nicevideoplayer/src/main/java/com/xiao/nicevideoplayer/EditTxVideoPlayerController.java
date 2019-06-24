package com.xiao.nicevideoplayer;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by XiaoJianjun on 2017/6/21.
 * 仿腾讯视频热点列表页播放器控制器.
 */
public class EditTxVideoPlayerController
        extends TxVideoPlayerController {

    public EditTxVideoPlayerController(Context context) {
        super(context);
    }


    public void setNiceVideoPlayer(EditNiceVideoPlayer niceVideoPlayer) {
        mNiceVideoPlayer = niceVideoPlayer;
    }


    @Override
    protected void init() {
        super.init();
        mFullScreen.setVisibility(GONE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        LogUtil.i("onStartTouchEnd------" + seekBar.getProgress() / 100f + "duration is " + mNiceVideoPlayer.getDuration());
        long position;
        if (seekBar.getProgress() / 100f > 0.95) {
            position = (long) (mNiceVideoPlayer.getDuration());
        } else {
            position = (long) (mNiceVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        }

        //  long position = (long) (mNiceVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        if (mNiceVideoPlayer instanceof EditNiceVideoPlayer) {
            long startTime = ((EditNiceVideoPlayer) mNiceVideoPlayer).getmStartTime();
            // long endTime = ((EditNiceVideoPlayer) mNiceVideoPlayer).getmEndTime();
            // position *= (endTime - startTime);
            position += startTime;
            int currentInt = (int) Math.ceil((position - startTime) * 1.0 / 100);
            int durationInt = (int) Math.ceil((mNiceVideoPlayer.getDuration() / 100) * 1.0);
            float progress = (float) (currentInt * 1.00 / durationInt);
            //超过最大显示最大
            if (currentInt > durationInt) {
                progress = (float) 1.0;
            }

            position = startTime + (int) (progress * mNiceVideoPlayer.getDuration());
            LogUtil.i("onStartTouchEnd------" + ((EditNiceVideoPlayer) mNiceVideoPlayer).getmStartTime() + position);
        }
        LogUtil.i("onStartTouchEnd------" + ((EditNiceVideoPlayer) mNiceVideoPlayer).getmStartTime() + position);
        mNiceVideoPlayer.seekTo(position);
        startDismissTopBottomTimer();
        //mNiceVideoPlayer.pause();

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        LogUtil.i("onStartTouch------" + seekBar.getProgress() / 100f);
        long position = (long) (mNiceVideoPlayer.getDuration() * seekBar.getProgress() / 100f);
        if (mNiceVideoPlayer instanceof EditNiceVideoPlayer) {
            long startTime = ((EditNiceVideoPlayer) mNiceVideoPlayer).getmStartTime();
            position += startTime;

            LogUtil.i("onStartTouch------" + position + " starttme " + startTime);
        }

        mNiceVideoPlayer.seekTo(position);
        startDismissTopBottomTimer();
    }


    OnSeekBarChangeListener onSeekBarChangeListener;

    public interface OnSeekBarChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param progress The current progress level. This will be in the range min..max where min
         *                 and max were set by {@link ProgressBar#setMin(int)} and
         *                 {@link ProgressBar#setMax(int)}, respectively. (The default values for
         *                 min is 0 and max is 100.)
         */
        void onProgressChanged(long progress);

    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        this.onSeekBarChangeListener = onSeekBarChangeListener;
    }


    @Override
    protected void updateProgress() {
        long position = mNiceVideoPlayer.getCurrentPosition();
        long duration = mNiceVideoPlayer.getDuration();
        LogUtil.i("onStartTrackingTouch------" + position + "  vv   " + duration);
//        if (position != 0) {
////            //延迟500造成进度条不准确
////            position += 500;
//        } else {
//            return;
//        }
        int bufferPercentage = mNiceVideoPlayer.getBufferPercentage();
        mSeek.setSecondaryProgress(bufferPercentage);
        int currentInt = (int) Math.ceil(position * 1.0 / 1000);
        int durationInt = (int) Math.ceil((duration / 1000) * 1.0);
        int progress;
        if (durationInt != 0) {
            progress = currentInt * 100 / durationInt;
        } else {
            progress = 0;
        }
        //超过最大显示最大
        if (currentInt > durationInt) {
            currentInt = 0;
            progress = 0;
        }
        if (!mNiceVideoPlayer.isPaused()) {
            //防止跳针
            mSeek.setProgress(progress);
        }
        LogUtil.i("onStartTrackingTouch------" + progress / 100f + " ff " + currentInt + " dd " + durationInt);
        mPosition.setText(formatTime(currentInt * 1000));
        mDuration.setText(NiceUtil.formatTime(durationInt * 1000));
        // 更新时间
        mTime.setText(new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date()));
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onProgressChanged(position);
        }
    }

    /**
     * 将毫秒数格式化为"##:##"的时间
     *
     * @param milliseconds 毫秒数
     * @return ##:##
     */
    public static String formatTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "00:00";
        }
        long totalSeconds = milliseconds / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    @Override
    protected void showChangePosition(long duration, int newPositionProgress) {
        mChangePositon.setVisibility(View.VISIBLE);
        long newPosition = (long) (duration * newPositionProgress / 100f);
        mChangePositionCurrent.setText(NiceUtil.formatTime(newPosition));
        mChangePositionProgress.setProgress(newPositionProgress);
        mSeek.setProgress(newPositionProgress);
        mPosition.setText(NiceUtil.formatTime(newPosition));
    }


    @Override
    public void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        getmBottom().setVisibility(GONE);
    }

    @Override
    protected void reset() {
        super.reset();
        getmBottom().setVisibility(GONE);
    }

    @Override
    protected void setTopBottomVisible(boolean visible) {
        super.setTopBottomVisible(visible);
        getmBottom().setVisibility(GONE);
    }
}
