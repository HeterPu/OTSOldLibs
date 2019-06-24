package com.xiao.nicevideoplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

public class MediaPlayerService extends Service {
    public static final String BROADCAST_TO_SERVICE = "com.mediaplayer.playerfunction";
    public static final String SERVICE_TO_ACTIVITY = "com.mediaplayer.currentPlayerStatus";
    public static final String PLAYER_FUNCTION_TYPE = "playerfunction";
    public static final String PLAYER_TRACK_URL = "trackURL";
    public static final int PLAY_MEDIA_PLAYER = 1;
    public static final int PAUSE_MEDIA_PLAYER = 2;
    public static final int RESUME_MEDIA_PLAYER = 3;
    public static final int STOP_MEDIA_PLAYER = 4;
    public static final int CHANGE_PLAYER_TRACK = 5;
    public static final String PLAYER_STATUS_KEY = "PlayerCurrentStatus";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter(BROADCAST_TO_SERVICE);
        registerReceiver(playerReceiver, intentFilter);
        if (mPlayer != null && mPlayer.isPlaying()) {
            sendPlayerStatus("playing");
        }
        return START_STICKY;
    }

    private BroadcastReceiver playerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BROADCAST_TO_SERVICE.equalsIgnoreCase(action)) {
                String trackURL = intent.hasExtra(PLAYER_TRACK_URL) ? intent.getStringExtra(PLAYER_TRACK_URL) : "";
                int function = intent.getIntExtra(PLAYER_FUNCTION_TYPE, 0);
                switch (function) {
                    case CHANGE_PLAYER_TRACK:
                        changeTrack(trackURL);
                        break;
                    case STOP_MEDIA_PLAYER:
                        stopPlayer();
                        break;
                    case PLAY_MEDIA_PLAYER:
                        startMediaPlayer(trackURL);
                        break;
                    case PAUSE_MEDIA_PLAYER:
                        pausePlayer();
                        break;
                    case RESUME_MEDIA_PLAYER:
                        resumePlayer();
                        break;
                }

            }
        }
    };
    private MediaPlayer mPlayer;

    private void pausePlayer() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            sendPlayerStatus("pause");
        }
    }

    private void resumePlayer() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
            sendPlayerStatus("playing");
        }
    }

    private void changeTrack(String url) {
        stopPlayer();
        startMediaPlayer(url);

    }

    private void stopPlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            sendPlayerStatus("stopped");

        }
    }

    public void startMediaPlayer(String url) {
        if (TextUtils.isEmpty(url))
            return;
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(url);
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (extra == MediaPlayer.MEDIA_ERROR_SERVER_DIED
                            || extra == MediaPlayer.MEDIA_ERROR_MALFORMED) {
                        sendPlayerStatus("erroronplaying");
                    } else if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                        sendPlayerStatus("erroronplaying");
                        return false;
                    }
                    return false;
                }
            });
            mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    Log.e("onBufferingUpdate", "" + percent);

                }
            });
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    mPlayer.start();
                    sendPlayerStatus("playing");
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.e("onCompletion", "Yes");
                    sendPlayerStatus("completed");
                }
            });
            mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPlayerStatus(String status) {
        Intent intent = new Intent();
        intent.setAction(SERVICE_TO_ACTIVITY);
        intent.putExtra(PLAYER_STATUS_KEY, status);
        sendBroadcast(intent);
    }


    /**
     * TO start media player.It will send broadcast to Service & from service player will start
     *
     * @param url
     */
    public void openStartMediaPlayer(String url) {
        startService(new Intent(this, MediaPlayerService.class));
        Intent intent = new Intent();
        intent.setAction(MediaPlayerService.BROADCAST_TO_SERVICE);
        intent.putExtra(MediaPlayerService.PLAYER_FUNCTION_TYPE, MediaPlayerService.PLAY_MEDIA_PLAYER);
        intent.putExtra(MediaPlayerService.PLAYER_TRACK_URL, url);
        sendBroadcast(intent);
    }
}