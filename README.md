
## Tuya RTC Camera SDK



[English](README.md)|[中文版](README-ZH.md)

## Features Overview
TuyaRTCCamera SDK is a comprehensive solution for audio and video based on WebRTC technology,
through this SDK you can easily access Tuya IoT Colud and then perform a number of interactive
operations on Tuya IoT devices.
This SDK allows you to easily access the Tuya IoT Colud and perform a number of interactive
operations, especially for audio and video processing and control is the core function of this SDK.
- Preview Camera's content
- Recording Camera's content
- JPEG screen capture
- Support for interacting with the camera

## Steps to integrate the SDK
### First step
Modify some parameters in MainActivity.java to the appropriate ones
``` java
    ...
    private String clientId = "input your client id";
    private String secret  = "input your secret id";
    private String deviceId = "input your device id";
    private String authCode = "input your auth code";
    ...
```
### Step 2
Configure build.gradle
Find build.gradle in the application directory, and add the relevant dependencies to the file

``` gradle
dependencies {
    implementation fileTree(dir: 'libs', include: rtcPackageName)
    implementation group: 'org.eclipse.paho', name: 'org.eclipse.paho.client.mqttv3', version: '1.2.5'
}
```

### Step 3
Copy the library files
- In the current project directory, if there is no directory for the library files, execute `mkdir libs` to create a new directory
- Copy the aar file from one of the Libraries versions to the created libs directory

### Step 4
- According to your region, fill in the appropriate regionCode, you can refer to the following content `RegionCode Comparison Table`.
- Here is the parameter that should be filled in with "cn" for my region (Hangzhou, Zhejiang Province, China)
```java
tuyaRTCEngine.initRtcEngine(appContext, eglBase,
           clientId, secret, authCode, "cn", (TuyaRTCEngineHandler)caller);
```


## Capabilities Overview

**Interface Description**

**TuyaRTCEngine Interface Description**

| Parameters | Description |
| :------------ | :------------------------------------------------------------------- |
| initRtcEngine | Engine initialization |
| destroyRtcEngine | Destroy the engine
| createTuyaCamera | Creates a TuyaRTCCamera object, each object corresponds to a Camera or Stream.
| destoryTuyaCamera | Destroy a TuyaRTCCamera object |
| setLogConfigure | Set the log output of the SDK. | getSdkVersion
| getSdkVersion | Get the SDK version information.
| getBuildTime | Get the SDK build time |


**TuyaRTCCamera interface description**
| parameters | description |
| :------------ | :------------------------------------------------------------------- |
| startPreview | Start previewing the contents of the Camera |
| stopPreview | Stop previewing the content of the camera.
| startRecord | Start recording the contents of the camera |
| stopRecord | stop recording the content of the Camera
| snapShot | Snap a picture of the camera
| genMp4Thumbnail | Generate the cover of an MP4 file |
| muteAudio | Mute the camera's sound
| muteVideo | Switch the camera screen on/off
| getRemoteAudioMute | Get the mute state of the camera sound.
| getRemoteVideoMute | Get the on/off state of the Camera video


**TuyaRTCEngineHandler interface description**
| parameters | description |
| :------------ | :------------------------------------------------------------------- |
| onLogMessage | The log output callback function in the SDK |
| onInitialized | Callback function for successful SDK initialization |
| onDestoryed | Callback function for successful destruction of the SDK

**TuyaRTCCameraHandler interface description**
| parameters | description |
| :------------ | :------------------------------------------------------------------- |
| onVideoFrame | The video data callback function for the current camera.
| onFirstVideoFrame | The callback function for the first video frame of the current Camera.
| onResolutionChanged | The callback function when the video resolution of the current Camera changes.

## RegionCode Comparison Table
| region abbreviation | range |
| :------------ | :------------------------------------------------------------------- |
| cn | China |
| us | America |
| eu | Europe |
| in | India |
| we | | ue | EasternAmerica |
| we | WesternEurope |

## Docs
Please refer to [API Reference](doc/index/index.html). Before reading, please download the code locally and open it in your browser



## Sample code 


- Header file
``` java
package com.tuya.rtccamera;

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

    public void destroy() {
        tuyaRTCEngine.destroyRtcEngine();
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
        return -1;

    }

    public int startRecord(String deviceId, String mp4File) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.startRecord(mp4File);
        }
        return -1;
    }

    public int stopRecord(String deviceId) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.stopRecord();
        }
        return -1;
    }

    public int snapshot(String deviceId, String jpgFile, int width, int height) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.snapshot(jpgFile, width, height);
        }
        return -1;
    }

    public int muteAudio(String deviceId, boolean mute) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.muteAudio(mute);
        }
        return -1;
    }

    public int muteVideo(String deviceId, boolean mute) {
        P2PCamera camera = p2pCameraMap.get(deviceId);
        if (camera != null) {
            return camera.muteVideo(mute);
        }
        return -1;
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
            tuyaRTCEngine.destroyTuyaCamera(did);
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
        public int snapshot(String jpgFile, int width, int height) {
            tuyaRTCCamera.snapShot(jpgFile, width, height);
            return 0;
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

``` 


## Latest version
1.0.0.1

