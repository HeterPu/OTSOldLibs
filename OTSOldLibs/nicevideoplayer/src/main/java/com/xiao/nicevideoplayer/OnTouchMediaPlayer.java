//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xiao.nicevideoplayer;

import android.os.Build;
import android.support.annotation.RequiresApi;

import static android.media.MediaPlayer.SEEK_CLOSEST;

public class OnTouchMediaPlayer extends ThisAndroidMediaPlayer {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void seekTo(long msec) throws IllegalStateException {
        this.mInternalMediaPlayer.seekTo((int)msec,SEEK_CLOSEST);
    }

}
