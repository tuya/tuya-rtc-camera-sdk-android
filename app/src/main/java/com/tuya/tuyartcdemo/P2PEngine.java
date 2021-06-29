package com.tuya.tuyartcdemo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.tuya.rtc.TuyaRTCCamera;
import com.tuya.rtc.TuyaRTCCameraHandler;
import com.tuya.rtc.TuyaRTCEngine;
import com.tuya.rtc.TuyaRTCEngineHandler;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.util.concurrent.ConcurrentHashMap;

public class P2PEngine {
    private static final String TAG = "P2PEngine";

    private final    TuyaRTCEngine                        tuyaRTCEngine;
    private final    Context                              appContext;
    private final    EglBase                              eglBase;
    private final Activity caller;
    private          ConcurrentHashMap<String, P2PCamera> p2pCameraMap = new ConcurrentHashMap<>();

    public P2PEngine(Activity caller) {
        TuyaRTCEngine.setLogConfigure(null, 0);
        Log.e(TAG, "SDK info: " + TuyaRTCEngine.getSdkVersion() + ", Built at : " + TuyaRTCEngine.getBuildTime());
        this.caller = caller;
        eglBase = EglBase.create(null /* sharedContext */, EglBase.CONFIG_PLAIN);
        appContext = caller;
        tuyaRTCEngine = new TuyaRTCEngine();

    }

    public void initialize(String clientId, String secret, String authCode) {
        tuyaRTCEngine.initRtcEngine(appContext, eglBase,
                clientId, secret, authCode, "cn", (TuyaRTCEngineHandler)caller);
    }

    public void destory() {
        tuyaRTCEngine.destoryRtcEngine();
    }

    public int startPreview(String did, SurfaceViewRenderer renderer) {
        P2PCamera camera = new P2PCamera(caller, tuyaRTCEngine, eglBase, did);
        camera.startPreview(renderer);
        p2pCameraMap.put(did, camera);
        return 0;
    }

    public int stopPreview(String did) {
        P2PCamera camera = p2pCameraMap.get(did);
        if (camera != null) {
            camera.destroy(did);
            camera.stopPreview();
        }
        return 0;

    }

    public int startRecord(String deviceId, String mp4File) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.startRecord(mp4File);
        }
        return 0;
    }

    public int stopRecord(String deviceId) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.stopRecord();
        }
        return 0;
    }


    public int muteAudio(String deviceId, boolean mute) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.muteAudio(mute);
        }
        return 0;
    }

    public int muteVideo(String deviceId, boolean mute) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.muteVideo(mute);
        }
        return 0;
    }

    public boolean getAudioMute(String deviceId) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.getAudioMute();
        }
        return false;
    }


    public boolean getVideoMute(String deviceId) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.getVideoMute();
        }
        return false;
    }




    static class P2PCamera {
        private       TuyaRTCCamera       tuyaRTCCamera;
        private       TuyaRTCEngine       tuyaRTCEngine;
        private final String              deviceId;
        private final EglBase             eglBase;
        private       SurfaceViewRenderer viewRenderer;
        private Activity activityCaller;

        public P2PCamera(Activity activity, TuyaRTCEngine engine, EglBase egl, String did) {
            this.activityCaller = activity;
            this.eglBase = egl;
            tuyaRTCEngine = engine;
            deviceId = did;
            tuyaRTCCamera = tuyaRTCEngine.createTuyaCamera(did);
        }


        public void destroy(String did) {
            tuyaRTCEngine.destoryTuyaCamera(did);
        }

        public int startPreview(SurfaceViewRenderer renderer) {
            viewRenderer = renderer;
            activityCaller.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewRenderer.setKeepScreenOn(true);
                    viewRenderer.setZOrderMediaOverlay(true);
                    viewRenderer.setZOrderOnTop(false);
                    viewRenderer.init(eglBase.getEglBaseContext(), null);
                    viewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT, RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);


                }
            });

            tuyaRTCCamera.startPreview((VideoSink)renderer, new TuyaRTCCameraHandler() {
                @Override
                public void onVideoFrame(VideoFrame videoFrame) {

                }

                @Override
                public void onFirstVideoFrame(int i, int i1) {

                }

                @Override
                public void onResolutionChanged(int i, int i1, int i2, int i3) {

                }
            });
            return 0;
        }

        public int stopPreview() {
            if (viewRenderer != null) {
                activityCaller.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewRenderer.release();
                    }
                });
            }
            return tuyaRTCCamera.stopPreview();
        }

        public int startRecord(String mp4File) {
            boolean ret = tuyaRTCCamera.startRecord(mp4File);
            return ret ? 0 : -1;
        }

        public int stopRecord() {
            boolean ret = tuyaRTCCamera.stopRecord();
            return ret ? 0 : -1;
        }

        public int muteAudio(boolean mute) {
            return tuyaRTCCamera.muteAudio(mute);
        }

        public int muteVideo(boolean mute) {
            return tuyaRTCCamera.muteVideo(mute);
        }

        public boolean getAudioMute() {
            return tuyaRTCCamera.getRemoteAudioMute();
        }

        public boolean getVideoMute() {
            return tuyaRTCCamera.getRemoteVideoMute();
        }
    }
}
