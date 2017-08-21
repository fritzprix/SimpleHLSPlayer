package com.example.innocentevil.hlsplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.innocentevil.hlsplayer.player.IPlayerEventListener;
import com.example.innocentevil.hlsplayer.player.IPlayerInterface;
import com.example.innocentevil.hlsplayer.player.PlayerService;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements Player.EventListener, ServiceConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);
    protected static final String TAG = MainActivity.class.getCanonicalName();
    private static final String USR_AGENT = "HLS-PLAY";
    private static final String EBS_FOREIGN_FM_URI = "http://new_iradio.ebs.co.kr/iradio_skt_ai_rtsp/iradiolive_m4a/playlist.m3u8";   // checked OK!
    private static final String BBS_FM_URI = "http://bbslive.clouducs.com:1935/bbsradio-mlive/livestream/playlist.m3u8";  // checked OK!
    private static final String EBS_FM_URI = "http://ebsonairiosaod.ebs.co.kr/fm_skt_ai_hls/bandiappaac/playlist.m3u8";  // checked OK!
    private static final String YTN_URI = "http://live.slive.ytn.co.kr/live/_definst_/fmlive_0624_1.sdp/playlist.m3u8";  // checked OK!
    private static final String ARIRANG_FM_URI = "http://amdlive.ctnd.com.edgesuite.net/arirang_3ch/arirang_3ch_audio/playlist.m3u8"; // checked OK!
    private static final String TBS_URI = "http://tbs.hscdn.com/tbsradio/efm/playlist.m3u8";   // checked OK!
    private static final String MBC_URI = "http://1.234.58.172/sksfm/sksfm.stream/playlist.m3u8?id=2101&si=6&secure=NGQyYWZkOThjMzU4NmNkYmZhYmY0YWI4ZTMwMDc2ZWEyM2QyMDAzNWU5NjRjYTQyM2MxOGNhYWQ0ZDFiNmM4OWJkYjJjODQwYzNjOTI2N2EwMjBlMWJjZjE2MTMyZWM0Nzg2NmIwYTBhOWI1ZDgyYTBkZWRhY2UxMGQzNzRkZDM1M2M2OTk0MWRkMDgyNzRhMDQ5OWMwM2JhNjg0MGE3ZTQzOWU2YTlmMWI0MDc1ZDVlMDljNjM0OGM3MjZhODZlZTQ4OGJiYjVlZGY4NzY2ODMxOWI0NzM3YzU3OWQ5YzM1MjA0NjdmZjNiMTQzNzUxNDRmNGFjMDZlYWNkODM2ZGIyZmQ2NzUwNzJmMmRjMTJjMTEyOWQ1NTZjMWE2Y2JjY2FlNjI1ZmE2NDBmNjRlOA==&csu=false\",\"subStreamingUrl\":\"rtsp:\\/\\/1.234.58.172\\/sksfm\\/_definst_\\/sksfm.stream?id=2101&si=8&secure=MDRkMDc1OGYyMjc5MDJjYTYwYTY2MWIwOTU0M2QxODk0MDQ0NjA3NDc1MzU2OTJhNGRkNGY2OGNiZTg4NGJhZmJmMjQyYWI5YjFkZTA0ZjAzMzI5ZTBiM2JmOTBlYjc2OTM5YjViZDM5NTJkZjRiOTQ2NzQ3MmU4YjZhMjIxNjlkNDM2MmE0NGU4OWY2ZmE0NjZkMDYzOWQwMzZmOGM4OTMwOTdiMzc2Njk3MWE0NDYxNWU1M2Y4YjAxZmM3Yjg2YWEwOTNlNjZiNmRhMmVkM2IxYzkzZjVlN2FiOTM4NzBjYWIzN2I4YWZlMWNhNTgwOTYyZTEzYzIwOTAwMzYxODA2NTczNDE2ZWI3ODJmZWM=&csu=false";
    private IPlayerInterface playerInterface;

    private static class PlayerListener extends IPlayerEventListener.Stub {

        @Override
        public void onIdle() throws RemoteException {
            LOGGER.error("IDLE");
        }

        @Override
        public void onPlay() throws RemoteException {
            LOGGER.error("PLAY");
        }

        @Override
        public void onPuase() throws RemoteException {
            LOGGER.error("PAUSE");
        }

        @Override
        public void onError(String errorMsg) throws RemoteException {
            LOGGER.error("ERROR %s", errorMsg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent service = new Intent(getApplicationContext(), PlayerService.class);
        bindService(service,this, BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        playerInterface = IPlayerInterface.Stub.asInterface(service);
        try {
            playerInterface.addEventListener(new PlayerListener());
            playerInterface.prepare(EBS_FM_URI, false);
            playerInterface.play();
            Thread.sleep(1000L);
            playerInterface.pause();
            Thread.sleep(1000L);
            playerInterface.play();
            playerInterface.setVolume(0.5f);
            Thread.sleep(1000L);
            playerInterface.stop();
            Thread.sleep(5000L);
            playerInterface.prepare(TBS_URI, true);
            Thread.sleep(1000L);
            playerInterface.pause();
            Thread.sleep(1000L);
            playerInterface.play();
            Thread.sleep(1000L);
            playerInterface.stop();
        } catch (RemoteException e) {

        } catch (InterruptedException e) {

        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
