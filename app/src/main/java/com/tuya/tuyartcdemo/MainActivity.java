package com.tuya.tuyartcdemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.tuya.rtc.TuyaRTCEngineHandler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.webrtc.SurfaceViewRenderer;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener ,
        CompoundButton.OnCheckedChangeListener, TuyaRTCEngineHandler{
    private static final String TAG = "MainActivity";


    private volatile boolean                              isInitalized;
    private volatile boolean                              isDestroyed;
    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.CAMERA",
            "android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private Button sdkInitBtn;
    private Button subscribeTopicBtn;
    private Button startRecorBtn;


    private LayoutInflater inflater;


    private LinearLayout feedContainer;

    private final ConcurrentHashMap<String, ViewGroup> feedWindows = new ConcurrentHashMap<>();

    private boolean audioMuted;
    private boolean videoMuted;

    private boolean isSdkInit;
    private                  boolean         isSubscrbingTopic;
    private boolean isStartRecord;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();


    //For test arguments.
    private String clientId = "input your client id";
    private String secret  = "input your secret id";
    private String deviceId = "input your device id";
    private String authCode = "input your auth code";


    private P2PEngine p2PEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRes();

    }

    private void initRes() {
        inflater = LayoutInflater.from(this);

        sdkInitBtn = findViewById(R.id.sdkInit);
        sdkInitBtn.setOnClickListener(this);
        subscribeTopicBtn = findViewById(R.id.subscribeTopic);
        subscribeTopicBtn.setOnClickListener(this);
        startRecorBtn = findViewById(R.id.startRecord);
        startRecorBtn.setOnClickListener(this);
        Button shareMediaBtn = findViewById(R.id.shareMedia);
        shareMediaBtn.setOnClickListener(this);
        Button switchCameraBtn = findViewById(R.id.switchCamera);
        switchCameraBtn.setOnClickListener(this);

        CheckBox muteRemoteAudiocheckbox = findViewById(R.id.muteRemoteAudio);
        CheckBox muteRemoteVideocheckbox = findViewById(R.id.muteRemoteVideo);

        muteRemoteAudiocheckbox.setOnCheckedChangeListener(this);
        muteRemoteVideocheckbox.setOnCheckedChangeListener(this);

        feedContainer = findViewById(R.id.rtc_remote_feeds_container);


    }


    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private void addFeedWindow(String streamName, SurfaceViewRenderer surfaceView) {
        if (!feedWindows.containsKey(streamName)) {
            RelativeLayout feedWindow = (RelativeLayout) inflater.inflate(R.layout.feed_window, feedContainer, false);
            ViewGroup.LayoutParams params = new RelativeLayout.LayoutParams(dip2px(this,320),dip2px(this,180));
            surfaceView.setLayoutParams(params);
            //surfaceView.setBackgroundColor(Color.GRAY);
            feedWindow.addView(surfaceView,0);
            feedContainer.addView(feedWindow);
            feedWindows.put(streamName,feedWindow);
            TextView streamNameTV = feedWindow.findViewById(R.id.streamName_tv);
            streamNameTV.setText(streamName);

        } else {
            Log.e(TAG, "addFeedWindow fail repeat streamName = " + streamName);
        }
    }

    private void deleteFeedWindow(String streamName) {
        if (feedWindows.containsKey(streamName)) {
            ViewGroup viewGroup = feedWindows.get(streamName);
            viewGroup.removeAllViewsInLayout();
            feedWindows.remove(streamName);
            feedContainer.removeView(viewGroup);

        } else {
            Log.e(TAG,"deleteFeedWindow fail no uid = "+streamName);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sdkInit) {
            if (!isSdkInit) {
                requestDangerousPermissions(MANDATORY_PERMISSIONS, 1);
                p2PEngine = new P2PEngine(this);
                executor.execute(() -> p2PEngine.initialize(clientId, secret, authCode));
                sdkInitBtn.setText("引擎销毁");

                isSdkInit = true;
            } else {
                if (isSubscrbingTopic) {
                    p2PEngine.stopPreview(deviceId);
                    deleteFeedWindow(deviceId);
                    subscribeTopicBtn.setText("订阅内容");
                    isSubscrbingTopic = false;
                }
                if (isStartRecord) {
                    p2PEngine.stopRecord(deviceId);
                }
                p2PEngine.destory();
                sdkInitBtn.setText("引擎初始化");
                isSdkInit = false;
            }
        } else {
            if (!isInitalized) {
                Log.e(TAG, "请先初始化引擎。");
                Toast.makeText(this, "请先初始化引擎.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (v.getId() == R.id.subscribeTopic) {
                if (p2PEngine  != null && (!isSubscrbingTopic)) {
                    SurfaceViewRenderer viewRenderer = new SurfaceViewRenderer(this);
                    addFeedWindow(deviceId, viewRenderer);
                    executor.execute(() -> p2PEngine.startPreview(deviceId, viewRenderer));
                    subscribeTopicBtn.setText("退订内容");
                    isSubscrbingTopic = true;
                } else {
                    deleteFeedWindow(deviceId);
                    p2PEngine.stopPreview(deviceId);
                    subscribeTopicBtn.setText("订阅内容");
                    isSubscrbingTopic = false;
                }

            } else if (v.getId() == R.id.startRecord) {
                if ((p2PEngine !=  null) && (!isStartRecord)) {
                    p2PEngine.startRecord(deviceId, getApplicationContext().getFilesDir().getAbsolutePath()+"/a.mp4");
                    //tuyaRTCCamera.changeVideoResolution(true);
                    startRecorBtn.setText("停止录制");
                    isStartRecord = true;
                } else {
                    //tuyaRTCCamera.changeVideoResolution(false);

                    p2PEngine.stopRecord(deviceId);
                    startRecorBtn.setText("开始录制");
                    isStartRecord = false;
                }
            } else if (v.getId() == R.id.shareMedia) {
                //String mp4File = getApplicationContext().getFilesDir().getAbsolutePath()+"/a.mp4";
                //shareMedia(this, mp4File, false);
                p2PEngine.snapshot(deviceId,getApplicationContext().getFilesDir().getAbsolutePath() + "/a.jpg", 640, 360);

            }
        }

    }

    private int shareMedia(Context context, String mediaPath, boolean isPhoto) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(context,context.getPackageName() + ".provider" , new File(mediaPath));
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            if (isPhoto) {
                intent.setType("image/jpeg");
            } else {
                intent.setType("video/mp4");
            }
            startActivity(intent);
        } else {
            Uri uri = Uri.fromFile(new File(mediaPath));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            if (isPhoto) {
                intent.setType("image/jpeg");
            } else {
                intent.setType("video/mp4");
            }
            startActivity(intent);
        }

        return 0;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.muteRemoteAudio) {
            executor.execute(() ->{
                p2PEngine.muteAudio(deviceId, !audioMuted);
                audioMuted = !audioMuted;
                Log.e(TAG, "getRemoteAudioMute " + p2PEngine.getAudioMute(deviceId));
            });
        } else if (buttonView.getId() == R.id.muteRemoteVideo) {
            executor.execute(()->{
                p2PEngine.muteVideo(deviceId, !videoMuted);
                videoMuted = !videoMuted;
                Log.e(TAG, "getRemoteVideoMute " +p2PEngine.getVideoMute(deviceId));

            });
        }
    }


    /**
     * 请求权限
     */
    public void requestDangerousPermissions(String[] permissions, int requestCode) {
        if (checkDangerousPermissions(permissions)){
            handlePermissionResult(requestCode, true);
            return;
        }
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    /**
     * 检查是否已被授权危险权限
     * @param permissions 权限合集
     * @return 成功true，失败false
     */
    private boolean checkDangerousPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        boolean finish = handlePermissionResult(requestCode, granted);
        if (!finish){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 处理请求危险权限的结果
     * @return true
     */
    private boolean handlePermissionResult(int requestCode, boolean granted) {
        //Notice 这里要自定义处理权限申请。
        Log.e(TAG, "handlePermissionResult " + " requestCode " + requestCode + " granted " + granted);
        return true;


    }

    @Override
    public void onLogMessage(String s) {
        Log.e(TAG, "===>" + s);
    }

    @Override
    public void onInitialized() {
        isInitalized = true;
        Log.e(TAG, "engine has been initalized.");

    }

    @Override
    public void onDestoryed() {
        isDestroyed = true;
        Log.e(TAG, "engine has been destoryed." + isDestroyed);
    }




}