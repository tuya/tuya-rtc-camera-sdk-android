# TuyaRTC - p2p设备音视频操作基础库

## 功能概述
TuyaRTC SDK是一套基于WebRTC技术的音视频综合解决方案，通过本SDK可以方便的接入Tuya IoT Colud，然后对Tuya IoT设备
进行一些列的交互操作，尤其是对音视频处理和控制是本SDK的核心功能。
通过该sdk可以完成以下核心功能
- 预览
- 录制
- 交互操作


### 通用SDK扩展包

在集成Home SDK的前提下, 通过选择集成扩展包, 获取相关基础能力支持相关业务开发.

## 集成SDK

### 配置build.gradle

build.gradle文件依赖声明中配置相关依赖

``` gradle
dependencies {
    implementation fileTree(dir: 'libs', include: rtcPackageName)
    implementation group: 'org.eclipse.paho', name: 'org.eclipse.paho.client.mqttv3', version: '1.2.5'
}
```

## 获取相关扩展包能力

通过以下代码来获取相关面板多语言SDK的能力

``` java
IPanelI18n i18nManager = PluginManager.service(IPanelI18n.class);
```

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


## 用例代码
- prepare
``` java

``` 