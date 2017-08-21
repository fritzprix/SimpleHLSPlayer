// IPlayerEventListener.aidl
package com.example.innocentevil.hlsplayer.player;

// Declare any non-default types here with import statements

interface IPlayerEventListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onIdle();
    void onPlay();
    void onPuase();
    void onError(String errorMsg);
}
