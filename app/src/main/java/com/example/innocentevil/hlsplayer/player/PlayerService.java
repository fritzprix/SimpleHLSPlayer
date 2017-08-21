package com.example.innocentevil.hlsplayer.player;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by innocentevil on 17. 8. 21.
 */

public class PlayerService extends Service {

    private static class PlayerServiceImpl extends IPlayerInterface.Stub implements Player.EventListener {

        private static final String USR_AGENT = "NUGU-HLS-player";
        private static final String TAG = PlayerServiceImpl.class.getCanonicalName();
        private static final Logger LOGGER = LoggerFactory.getLogger(PlayerService.class);

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
            synchronized (PlayerServiceImpl.this) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        break;
                    case Player.STATE_ENDED:
                        LOGGER.error("STATE ENDED!");
                        break;
                    case Player.STATE_IDLE:
                        if(!playWhenReady) {
                            if(mPlayerState == PlayerState.PAUSE) {
                                return;
                            }
                            mPlayerState = PlayerState.PAUSE;
                            LOGGER.error("STATE PAUSE!");
                            if(mIPlayerEventListener != null) {
                                try {
                                    mIPlayerEventListener.onPuase();
                                } catch (RemoteException e) {
                                    LOGGER.error(e.getLocalizedMessage());
                                }
                            }
                        } else {
                            if(mPlayerState == PlayerState.PLAY) {
                                return;
                            }
                            mPlayerState = PlayerState.PLAY;
                            LOGGER.error("STATE PLAY!");
                            if(mIPlayerEventListener != null) {
                                try {
                                    mIPlayerEventListener.onPlay();
                                } catch (RemoteException e) {
                                    LOGGER.error(e.getLocalizedMessage());
                                }
                            }
                        }
                        break;
                    case Player.STATE_READY:
                        break;

                }
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            LOGGER.error("%s Error : %s", TAG, error.getLocalizedMessage());
            if(mIPlayerEventListener != null) {
                try {
                    mIPlayerEventListener.onError(error.getLocalizedMessage());
                } catch (RemoteException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }

        @Override
        public void onPositionDiscontinuity() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        private enum PlayerState {
            IDLE(0, "IDLE"),
            PLAY(2, "PLAY"),
            PAUSE(3, "PAUSE");

            final int state;
            final String tostring;
            PlayerState(int state, String tostring) {
                this.state = state;
                this.tostring = tostring;
            }

        }
        private ExoPlayer mExoPlayer;
        private PlayerState mPlayerState;
        private BandwidthMeter mBandwidthMeter;
        private Handler mHandler;
        private AudioManager mAudioManager;
        private IPlayerEventListener mIPlayerEventListener;

        PlayerServiceImpl(Context context) {
            mPlayerState = PlayerState.IDLE;
            mHandler = new Handler();
            mBandwidthMeter = new DefaultBandwidthMeter();
            mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        }


        @Override
        public void prepare(String uri, boolean playWhenReady) throws RemoteException {
            synchronized (PlayerServiceImpl.this) {
                switch(mPlayerState){
                    case IDLE:
                        break;
                    default:
                        throw new IllegalStateException(String.format(Locale.getDefault(),"Current State is %s, State has to be in %s before call prepare()", mPlayerState, PlayerState.IDLE));
                }
            }
            Uri srcUri = Uri.parse(uri);
            DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(USR_AGENT, new TransferListener<DataSource>() {
                @Override
                public void onTransferStart(DataSource source, DataSpec dataSpec) {
                    LOGGER.debug("TransferStart : %s", dataSpec.toString());
                }

                @Override
                public void onBytesTransferred(DataSource source, int bytesTransferred) {

                }

                @Override
                public void onTransferEnd(DataSource source) {

                }
            });

            HlsMediaSource hlsMediaSource = new HlsMediaSource(srcUri, httpDataSourceFactory, mHandler, new AdaptiveMediaSourceEventListener() {
                @Override
                public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {

                }

                @Override
                public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
                }

                @Override
                public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
                    LOGGER.error("onLoadCanceled %s",dataSpec.toString());
                    synchronized (PlayerServiceImpl.this) {
                        goToIdle();
                    }
                }

                @Override
                public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
                    LOGGER.error("onLoadError %s",dataSpec.toString());
                    synchronized (PlayerServiceImpl.this) {
                        goToIdle();
                    }
                }

                @Override
                public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

                }

                @Override
                public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {

                }
            });

            AdaptiveTrackSelection.Factory trackFactory = new AdaptiveTrackSelection.Factory(mBandwidthMeter);
            mExoPlayer = ExoPlayerFactory.newInstance(new Renderer[]{new MediaCodecAudioRenderer(MediaCodecSelector.DEFAULT)}, new DefaultTrackSelector(trackFactory));
            mExoPlayer.addListener(this);
            mExoPlayer.setPlayWhenReady(playWhenReady);
            mExoPlayer.prepare(hlsMediaSource);
        }

        @Override
        public void addEventListener(IPlayerEventListener listener) throws RemoteException {
            mIPlayerEventListener = IPlayerEventListener.Stub.asInterface(listener.asBinder());
        }

        private void goToIdle() {
            mPlayerState = PlayerState.IDLE;
            if(mExoPlayer == null) {
                return;
            }
            mExoPlayer.release();
            mExoPlayer = null;
            if(mIPlayerEventListener != null) {
                try {
                    mIPlayerEventListener.onIdle();
                } catch (RemoteException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }

        @Override
        public void play() throws RemoteException {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void stop() throws RemoteException {
            mExoPlayer.stop();
            LOGGER.error("STATE IDLE!");
            goToIdle();

        }

        @Override
        public void pause() throws RemoteException {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public boolean isBusy() throws RemoteException {
            return mPlayerState != PlayerState.IDLE;
        }


        @Override
        public void seekTo(long position) throws RemoteException {
            mExoPlayer.seekTo(position);
        }

        @Override
        public void seekToRelative(long relativePosition) throws RemoteException {
            final long curPos = mExoPlayer.getCurrentPosition();
            mExoPlayer.seekTo(curPos + relativePosition);
        }

        @Override
        public void setVolume(float volume) throws RemoteException {
            final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int nVolume = Math.round((float) maxVolume * volume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nVolume, 0);
        }

        @Override
        public float getVolume() throws RemoteException {
            return (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    / (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public long getCurrentPosition() throws RemoteException {
            return mExoPlayer.getCurrentPosition();
        }
    }

    private PlayerServiceImpl mPlayerService;
    @Override
    public void onCreate() {
        super.onCreate();
        mPlayerService = new PlayerServiceImpl(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerService;
    }
}
