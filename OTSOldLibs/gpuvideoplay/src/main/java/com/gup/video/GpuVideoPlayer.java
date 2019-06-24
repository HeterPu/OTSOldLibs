package com.gup.video;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by guoh on 2018/9/17.
 * 功能描述：
 * 需要的参数：
 */
public class GpuVideoPlayer extends FrameLayout{

    private RelativeLayout mContainer;
    private GPUVideoPreviewView mPreviewView;

    public VideoPlayerController getmController() {
        return mController;
    }

    private VideoPlayerController mController;


    public GpuVideoPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GpuVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GpuVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mContainer=new RelativeLayout(context);
        mContainer.setBackgroundColor(Color.BLACK);
        mController=new VideoPlayerController(context);
        this.addView(mContainer);

        mPreviewView=new GPUVideoPreviewView(context);
        mPreviewView.setVideoPlayerController(mController);

        addPreviewView();
    }

    public void addControllerView(int width,int height) {
        mContainer.removeView(mController);
        LayoutParams params = new LayoutParams(
                width,
                height);
        mContainer.addView(mController, params);
    }

    public void setControllerVisibility(int visibility){
        mController.setControllerVisibility(visibility);
    }

    private void addPreviewView(){
        mContainer.removeView(mPreviewView);
        mContainer.setBackgroundColor(Color.BLACK);
        mContainer.addView(mPreviewView);
    }

    public void startPlayWithPath(String path){
        mPreviewView.setVideoPath(path);
    }

    public boolean isPlaying(){
        return mPreviewView.isPlaying();
    }

    public void resetVideoPath(String path){
        mPreviewView.chageVideo(path);
    }

    public void pause(){
        mPreviewView.onPause();
    }

    public GPUVideoPreviewView getVideoPlayer(){
        return mPreviewView;
    }

    public void resume(){
        mPreviewView.onResume();
    }

    public void destroy(){
        mPreviewView.onDestroy();
    }

    public int getDuration(){
        return mPreviewView.getMediaPlayer().getDuration();
    }

    public int getVideoDuration() {
        return mPreviewView.getMediaPlayer().getCurVideoDuration();
    }

    public void setVideoFilter(GPUImageFilterGroup filterGroup){
        mPreviewView.setFilter(filterGroup);
    }

    public void seekTo(int ms){
        mPreviewView.getMediaPlayer().seekTo(ms);
    }

    public void release(){
        if (mPreviewView.getMediaPlayer()!=null){
            mPreviewView.getMediaPlayer().release();
        }
    }

    public int getCurrentPosition() {
        return mPreviewView.getMediaPlayer().getCurrentProgress();
    }
}
