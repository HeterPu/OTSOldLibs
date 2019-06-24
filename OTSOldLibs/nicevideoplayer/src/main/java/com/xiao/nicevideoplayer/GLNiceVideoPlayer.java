package com.xiao.nicevideoplayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 优化逻辑后的播放器
 */
public class GLNiceVideoPlayer extends NiceVideoPlayer {

    public boolean isInitMediaPlayer = false;
    protected IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener
            = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            LogUtil.d("onVideoSizeChanged ——> width：" + width + "， height：" + height + "sar_num:" + sar_num + "sar_den:" + sar_den);
            LogUtil.d("onVideoSizeChanged ——> width：" + width + "， height：" + height);
        }
    };
    protected IMediaPlayer.OnInfoListener mOnInfoListener
            = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                mCurrentState = STATE_PLAYING;
                mController.onPlayStateChanged(mCurrentState);
                if (mPauseBeforePrepared) {
                    pause();
                    mPauseBeforePrepared = false;
                }
                if (!mController.isNeedPlay()) {
                    mMediaPlayer.pause();
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                // MediaPlayer暂时不播放，以缓冲更多的数据
                if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                    mCurrentState = STATE_BUFFERING_PAUSED;
                } else {
                    mCurrentState = STATE_BUFFERING_PLAYING;
                }
                mController.onPlayStateChanged(mCurrentState);
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                // 填充缓冲区后，MediaPlayer恢复播放/暂停
                if (mCurrentState == STATE_BUFFERING_PLAYING) {
                    mCurrentState = STATE_PLAYING;
                    mController.onPlayStateChanged(mCurrentState);
                }
                if (mCurrentState == STATE_BUFFERING_PAUSED) {
                    mCurrentState = STATE_PAUSED;
                    mController.onPlayStateChanged(mCurrentState);
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            } else if (what == IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
            } else {
            }
            return true;
        }
    };
    /**
     * get video info and store
     *
     * @param dataSource 视频播放的源文件
     */
    VideoInfo info;
    //是否准备完成前调用了暂停
    private int mPlayerType = TYPE_IJK;
    private AudioManager mAudioManager;
    private IMediaCallback iMediaCallback;

    public GLNiceVideoPlayer(Context context) {
        this(context, null);
    }

    public GLNiceVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setIMediaCallback(IMediaCallback iMediaCallback) {
        this.iMediaCallback = iMediaCallback;
    }

    public void setSurface(Surface mSurface) {
        this.mSurface = mSurface;
    }

    public void setSurfaceTexture(SurfaceTexture mSurfaceTexture) {
        //this.mSurfaceTexture = mSurfaceTexture;
        //initTextureView();
    }

    public void setDataSource(String dataSource) {
        this.mUrl = dataSource;
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        info = new VideoInfo();
        String path = dataSource;
        try {
            Uri uri = Uri.parse(path);
            retr.setDataSource(mContext, uri);
            String rotation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String duration = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            info.path = path;
            info.rotation = Integer.parseInt(rotation);
            info.width = Integer.parseInt(width);
            info.height = Integer.parseInt(height);
            info.duration = Integer.parseInt(duration);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }


    }

    public void startPlay(String url) {
        mUrl = url;
        mHeaders = new HashMap<>();
        setDataSource(url);
        // initTextureView();
        initAudioManager();
        initMediaPlayer();
        openMediaPlayer();
    }

    public void setController(NiceVideoPlayerController controller) {
        mContainer.removeView(mController);
        mController = controller;
        mController.reset();
        mController.setNiceVideoPlayer(this);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.setBackgroundColor(Color.RED);
        mContainer.addView(mController, params);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Toast.makeText(getContext(), "点击", Toast.LENGTH_LONG);
        return false;
    }

    @Override
    public void continueFromLastPosition(boolean continueFromLastPosition) {
    }

    @Override
    public void setSpeed(float speed) {
        if (mMediaPlayer instanceof IjkMediaPlayer) {
            ((IjkMediaPlayer) mMediaPlayer).setSpeed(speed);
        } else {
            LogUtil.d("只有IjkPlayer才能设置播放速度");
        }
    }

    public boolean isInitVideoManager() {
        return isInitVideoManager;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void start(long position) {
        super.start(position);
    }

    @Override
    protected void initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    @Override
    protected void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new NiceTextureView(mContext);
            // mTextureView.setSurfaceTextureListener(this);
        }
        addTextureView();
    }

    @Override
    protected void addTextureView() {
        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mContainer.addView(mTextureView, 0, params);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        throw new NullPointerException("不需要执行这儿 不能为空");
//        if (mSurfaceTexture == null) {
//            mSurfaceTexture = surfaceTexture;
//        } else {
//            mTextureView.setSurfaceTexture(mSurfaceTexture);
//        }
//        openMediaPlayer();
    }

    protected void openMediaPlayer() {
        try {
            // 屏幕常亮
            mContainer.setKeepScreenOn(true);
            // Uri uri = Uri.parse(mUrl);
            // 设置监听
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            // 设置dataSource
            LogUtil.i("mUrl   openMediaPlayer--------------" + mUrl);
            mMediaPlayer.setDataSource(mUrl);
            LogUtil.d("STATE_PREPARING");
            if (mSurface == null) {
                throw new NullPointerException("mSurface 不能为空");
                //mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            mController.onPlayStateChanged(mCurrentState);
            isInitMediaPlayer = true;
            if (iMediaCallback != null) {
                iMediaCallback.onVideoChanged(info);
            }

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return super.onSurfaceTextureDestroyed(surface);
    }

    public interface IMediaCallback {
        void onVideoChanged(VideoInfo info);
    }

    public class VideoInfo {
        public String path;//路径
        public int rotation;//旋转角度
        public int width;//宽
        public int height;//高
        public int bitRate;//比特率
        public int duration;//时长
        public int cutDuration;//剪切的时长
    }


}
