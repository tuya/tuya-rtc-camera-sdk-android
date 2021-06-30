
## Tuya RTC Camera SDK



[中文版](README-zh.md)|[English](README.md)

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
Copy the aar file to the appropriate directory
-- app
   -- libs
       -- copy to here ......
   -- src


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
| we | WesternEurope
| we | WesternEurope |



## Latest version
1.0.0.1

