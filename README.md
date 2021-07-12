
## Tuya RTC Camera SDK — RTC Camera Audio and Video Development Kit



[English](README.md) | [中文版](README-ZH.md)

## Overview
Tuya RTC Camera SDK is a comprehensive solution for audio and video development based on the WebRTC technology. With this SDK, you can easily access the Tuya IoT Cloud and implement interactions with `Powered by Tuya` devices. The core feature of this SDK is audio and video processing and control. Specifically, this SDK allows users to:
- Preview content from cameras.
- Record content from cameras.
- Capture and save images in the JPEG format.
- Interact with cameras.

## Integrate with the SDK
### Step 1
Modify the following parameters in `MainActivity.java` as required.
``` java
    ...
    private String clientId = "input your client id";
    private String secret  = "input your secret id";
    private String deviceId = "input your device id";
    private String authCode = "input your auth code";
    ...
```
### Step 2
Configure `build.gradle`: Find `build.gradle` in the application directory and add required dependencies to the file.

``` gradle
dependencies {
    implementation fileTree(dir: 'libs', include: rtcPackageName)
    implementation group: 'org.eclipse.paho', name: 'org.eclipse.paho.client.mqttv3', version: '1.2.5'
}
```

### Step 3
Copy the library files:
1. If no directories are created to include library files for your project, run `mkdir libs` to create a directory.
2. Copy the `aar.` file from one of the library versions and paste it to the newly created `libs` directory.

### Step 4
Set `regionCode` to the value that matches your region. For more information, see *RegionCode Comparison Table*.
In the following example, `regionCode` is set to `cn` for the region (Hangzhou, Zhejiang Province, China).
```java
tuyaRTCEngine.initRtcEngine(appContext, eglBase,
           clientId, secret, authCode, "cn", (TuyaRTCEngineHandler)caller);
```


## Capabilities

**API description**

**TuyaRTCEngine**

| Parameter | Description |
| :------------ | :------------------------------------------------------------------- |
| initRtcEngine | Initializes the engine. |
| destroyRtcEngine | Destroys the engine.
| createTuyaCamera | Creates an object of `TuyaRTCCamera`. The object corresponds to a camera or a stream. |
| destoryTuyaCamera | Destroys an object of `TuyaRTCCamera`. |
| setLogConfigure | Sets the log output of the SDK. |
| getSdkVersion | Returns information about the SDK version. |
| getBuildTime | Returns the build time of the SDK. |


**TuyaRTCCamera**
| Parameter | Description |
| :------------ | :------------------------------------------------------------------- |
| startPreview | Starts previewing content from a camera. |
| stopPreview | Stops previewing content from a camera. |
| startRecord | Starts recording content from a camera. |
| stopRecord | Stops recording content from a camera. |
| snapShot | Captures an image from a camera. |
| genMp4Thumbnail | Generates a cover for an MP4 file. |
| muteAudio | Disables camera sound. |
| muteVideo | Switches a camera video screen on or off. |
| getRemoteAudioMute | Returns the mute status of a camera. |
| getRemoteVideoMute | Returns the video screen status of a camera. |


**TuyaRTCEngineHandler**
| Parameter | Description |
| :------------ | :------------------------------------------------------------------- |
| onLogMessage | The log output callback function in the SDK. |
| onInitialized | The callback function of successful SDK initialization. |
| onDestoryed | The callback function of successful destruction with the SDK. |

**TuyaRTCCameraHandler**
| Parameter | Description |
| :------------ | :------------------------------------------------------------------- |
| onVideoFrame | The video data callback function for the current camera. |
| onFirstVideoFrame | The callback function for the first video frame from the current camera. |
| onResolutionChanged | The callback function that is executed when the video resolution of the current camera changes. |

## RegionCode Comparison Table
| Region abbreviation | Region |
| :------------ | :------------------------------------------------------------------- |
| cn | China |
| us | America |
| eu | Europe |
| in | India |
| ue | America Azure |
| we | Europe MS |

## References
For more information, see the API documentation located in the `doc/html` directory. Before you read these documents, download the sample code to your local directory and open `index.html` in your browser.

## Constraints
- Only one `TuyaRTCEngine` engine can be used for an application.
- Multiple objects of `TuyaRTCCamera` can be created with different values of `Device Identity`.
- The application must be in the preview state during the recording, snapshot, or mute operations. Otherwise, the application might not run as expected.

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
Version 1.0.0.1

