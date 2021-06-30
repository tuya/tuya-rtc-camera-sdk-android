# TuyaRTCCameraSDK - RTCCamera 音视频开发套件

## 功能概述
TuyaRTCCamera SDK是一套基于WebRTC技术的音视频综合解决方案，通过本SDK可以方便的接入Tuya IoT Colud，然后对Tuya IoT设备
进行一些列的交互操作，尤其是对音视频处理和控制是本SDK的核心功能。
通过该sdk可以完成以下核心功能
- 预览Camera的内容
- 录制Camera的内容
- JPEG 抓屏
- 支持与摄像头进行交互操作

## 集成SDK的步骤
### 第一步
修改MainActivity.java中的一些参数，修改为合适的参数
``` java
    ...
    private String clientId = "input your client id";
    private String secret  = "input your secret id";
    private String deviceId = "input your device id";
    private String authCode = "input your auth code"; 
    ...
``` 

### 第二步
配置build.gradle
在应用目录下找到build.gradle, 并在文件中添加相关依赖

``` gradle
dependencies {
    implementation fileTree(dir: 'libs', include: rtcPackageName)
    implementation group: 'org.eclipse.paho', name: 'org.eclipse.paho.client.mqttv3', version: '1.2.5'
}
```

### 第三步
copy aar文件到相应的目录
-- app
   -- libs
       -- copy到此处 ……
   -- src

## 能力概述

**接口说明**

**TuyaRTCEngine接口说明**

| 参数 | 说明 |
| :------------ | :------------------------------------------------------------------- |
| initRtcEngine | 引擎初始化 |
| destoryRtcEngine | 销毁引擎 |
| createTuyaCamera | 创建一个TuyaRTCCamera对象，每个对象对应一个Camera或者Stream |
| destoryTuyaCamera | 销毁一个TuyaRTCCamera对象 |
| setLogConfigure | 设置SDK的log输出. |
| getSdkVersion | 获取SDK版本信息 |
| getBuildTime | 获取SDK编译时间 |


**TuyaRTCCamera接口说明**
| 参数 | 说明 |
| :------------ | :------------------------------------------------------------------- |
| startPreview | 开始预览Camera的内容 |
| stopPreview | 停止预览Camera的内容 |
| startRecord | 开始录制Camera的内容 |
| stopRecord | 停止录制Camera的内容 |
| snapShot | 抓拍Camera的画面 |
| genMp4Thumbnail | 生成MP4文件的封面 |
| muteAudio | 对Camera声音进行静音操作 |
| muteVideo | 对Camera画面进行开关操作 |
| getRemoteAudioMute     | 获取Camera声音的静状态 |
| getRemoteVideoMute | 获取Camera视频的开关状态 |


**TuyaRTCEngineHandler接口说明**
| 参数 | 说明 |
| :------------ | :------------------------------------------------------------------- |
| onLogMessage | SDK中的log输出回调函数 |
| onInitialized | SDK初始化成功时候的回调函数 |
| onDestoryed | SDK销毁成功时候的回调函数 |

**TuyaRTCCameraHandler接口说明**
| 参数 | 说明 |
| :------------ | :------------------------------------------------------------------- |
| onVideoFrame | 当前Camera的的视频数据回调函数 |
| onFirstVideoFrame | 当前Camera的第一帧视频数据回调函数 |
| onResolutionChanged | 当前Camera的视频分辨率变化时候的回调函数 |

## RegionCode 对照表
| 区域缩写 | 范围 |
| :------------ | :------------------------------------------------------------------- |
| cn | China |
| us | America |
| eu | Europe |
| in | India |
| ue | EasternAmerica |
| we | WesternEurope |

## Docs
请参阅[doc/cn](doc/cn/index.html)

## 用例代码

``` java

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
``` 