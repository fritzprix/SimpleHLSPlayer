// IPlayerInterface.aidl
package com.example.innocentevil.hlsplayer.player;

import com.example.innocentevil.hlsplayer.player.IPlayerEventListener;
// Declare any non-default types here with import statements

interface IPlayerInterface {
    void prepare(String uri, boolean playWhenReady);
    void addEventListener(IPlayerEventListener listener);
    void play();
    void stop();
    void pause();
    boolean isBusy();
    void seekTo(long position);
    void seekToRelative(long relativePosition);
    long getCurrentPosition();
    void setVolume(float volume);
    float getVolume();
}
