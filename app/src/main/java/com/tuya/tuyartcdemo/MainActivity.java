package com.tuya.tuyartcdemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.tuya.rtc.TuyaRTCCamera;
import com.tuya.rtc.TuyaRTCCameraHandler;
import com.tuya.rtc.TuyaRTCEngine;
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

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener ,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "MainActivity";

    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.CAMERA",
            "android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private Button sdkInitBtn;
    private Button startPreivewBtn;
    private Button subscribeTopicBtn;
    private Button startRecorBtn;


    private CheckBox muteLocalAudiocheckbox;
    private CheckBox muteLocalVideocheckbox;


    private LayoutInflater inflater;


    private RelativeLayout localSurfaceLayout;
    private LinearLayout remoteFeedContainer;
    private LinearLayout feedContainer;

    private ConcurrentHashMap<String, ViewGroup> feedWindows = new ConcurrentHashMap<>();

    private boolean audioMuted;
    private boolean videoMuted;

    private boolean isSdkInit;
    private boolean isStartPreview;
    private                  boolean         isSubscrbingTopic;
    private boolean isStartRecord;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();


    //For test arguments.
    private String clientId = "jct4wjjgtppxth9vpjeq";
    private String secret  = "ns45erx7y9ut8trygwwnfu549eghrmqg";
    private String deviceId = "6ceeb5b251fb016f2aamtp";
    private String authCode = "b64775352efa99b7cf904fc8c7720d74";


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
        startPreivewBtn = findViewById(R.id.startPreview);
        startPreivewBtn.setOnClickListener(this);
        subscribeTopicBtn = findViewById(R.id.subscribeTopic);
        subscribeTopicBtn.setOnClickListener(this);
        startRecorBtn = findViewById(R.id.startRecord);
        startRecorBtn.setOnClickListener(this);
        Button shareMediaBtn = findViewById(R.id.shareMedia);
        shareMediaBtn.setOnClickListener(this);
        Button switchCameraBtn = findViewById(R.id.switchCamera);
        switchCameraBtn.setOnClickListener(this);

        muteLocalAudiocheckbox = findViewById(R.id.muteLocalAudio);
        muteLocalVideocheckbox = findViewById(R.id.muteLocalVideo);

        muteLocalAudiocheckbox.setOnCheckedChangeListener(this);
        muteLocalVideocheckbox.setOnCheckedChangeListener(this);

        localSurfaceLayout = findViewById(R.id.rtc_local_surfaceview);
        remoteFeedContainer = findViewById(R.id.rtc_remote_feeds_container);
        feedContainer = remoteFeedContainer;


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
                TuyaRTCEngine.setLogConfigure(null,0);
                p2PEngine = new P2PEngine(this);
                executor.execute(() ->{
                    p2PEngine.initialize(clientId, secret, authCode);
                });

                sdkInitBtn.setText("引擎销毁");
                isSdkInit = true;
            } else {
                if (isStartPreview) {
                    startPreivewBtn.setText("停止预览");
                    isStartPreview = false;
                }
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
        } else if (v.getId() == R.id.startPreview) {
        } else if (v.getId() == R.id.subscribeTopic) {
            if (p2PEngine  != null && (!isSubscrbingTopic)) {
                SurfaceViewRenderer viewRenderer = new SurfaceViewRenderer(this);
                addFeedWindow(deviceId, viewRenderer);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        p2PEngine.startPreview(deviceId, viewRenderer);
                    }
                });
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
            String mp4File = getApplicationContext().getFilesDir().getAbsolutePath()+"/a.mp4";
            shareMedia(this, mp4File, false);
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
        if (buttonView.getId() == R.id.muteLocalAudio) {
            executor.execute(() ->{
                p2PEngine.muteAudio(deviceId, !audioMuted);
                //audioMuted = !audioMuted;
                //tuyaRTCCamera.snapShot(this.getFilesDir().getAbsolutePath() + "/a.jpg", 0, 0);

                Log.e(TAG, "getRemoteAudioMute " + p2PEngine.getAudioMute(deviceId));
            });
        } else if (buttonView.getId() == R.id.muteLocalVideo) {
            executor.execute(()->{
                executor.execute(() ->{
                    p2PEngine.muteVideo(deviceId, !videoMuted);
                    videoMuted = !videoMuted;
                    Log.e(TAG, "getRemoteVideoMute " +p2PEngine.getVideoMute(deviceId));

                });
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
     * @param permissions
     * @return
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
            }
        }
        boolean finish = handlePermissionResult(requestCode, granted);
        if (!finish){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 处理请求危险权限的结果
     * @return
     */
    private boolean handlePermissionResult(int requestCode, boolean granted) {
        //Notice 这里要自定义处理权限申请。
        Log.e(TAG, "handlePermissionResult " + " requestCode " + requestCode + " granted " + granted);
        return false;


    }




}