package com.zehin.video;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.zehin.video.view.VideoLayout;
import com.zehin.videosdk.Video;
import com.zehin.videosdk.VideoClickListener;

public class LiveVideoActivity extends Activity implements VideoClickListener{

    // 视频控件
    private VideoLayout videoLayout;

    private Video video = null;

    private Handler videoHandler = null;

    private String IP = "218.201.111.234";
    private String userName = "admin";
    private int camId = 1062043;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        videoLayout = (VideoLayout) findViewById(R.id.video);
        videoLayout.setProgressBarVisibility(View.VISIBLE);

        video = Video.getInstance();
        video.setOnVideoClickListener(this);

        handlerVideoMessage();

        new Thread(){
            @Override
            public void run() {
                super.run();
                if(video.initVideo()){
                    video.setVideoParams(IP, IP);
                    if (video.connetVideo()){
                        if (video.loginVideo(userName)){
                            video.playVideo(camId, 0, userName);
                        }
                    }
                }
            }
        }.start();
    }

    private void handlerVideoMessage() {
        videoHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case VIDEO_ERROR_STATE_INIT:
                    case VIDEO_ERROR_STATE_CONNET:
                    case VIDEO_ERROR_STATE_LOGIN:
                    case VIDEO_ERROR_STATE_PLAY:
                        videoLayout.setProgressBarVisibility(View.GONE);
                        // 添加重播button
                        break;
                    case VIDEO_STATE_NOINIT:
                        // 恢复未初始化状态

                        break;
                    case VIDEO_STATE_PLAY:
                        // 开始播放
                        break;
                }
            }
        };
    }

    @Override
    public void initVideo(boolean arg0) {
        System.out.println("------------>initVideo"+arg0);
        if (arg0)
            video.videoState = video.VIDEO_STATE_INIT;
    }

    @Override
    public void connetVideo(boolean arg0) {
        System.out.println("------------>connetVideo"+arg0);
        if (arg0)
            video.videoState = video.VIDEO_STATE_CONNET;
    }

    @Override
    public void loginVideo(boolean arg0) {
        System.out.println("------------>loginVideo"+arg0);
        if (arg0)
            video.videoState = video.VIDEO_STATE_LOGIN;
    }

    @Override
    public void playVideo(boolean arg0) {
        System.out.println("------------>playVideo"+arg0);
        if (arg0)
            video.videoState = video.VIDEO_STATE_PLAY;
    }

    @Override
    public void videoMessageData(int width, int height, byte[] data) {
        if(video.videoState == video.VIDEO_STATE_PLAY){ // 播放状态
            videoLayout.upDateRenderer(width,height,data);
            if(!video.videoIsPlay) {
                video.videoIsPlay = true;
                // 开始播放
                videoHandler.sendEmptyMessage(VIDEO_STATE_PLAY);
            }
        }
    }

    @Override
    public void videoErrorListener(int keyError) {
        System.out.println("------------>keyError"+keyError);
        videoHandler.sendEmptyMessage(keyError);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        videoHandler.sendEmptyMessage(VIDEO_STATE_NOINIT);
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 视频状态：
     * 10000：未初始化
     * 10001：初始化
     * 10002：连接
     * 10003：登录
     * 10004：播放
     * 10005：暂停
     */
    public static final int VIDEO_STATE_NOINIT = 10000;
    public static final int VIDEO_STATE_INIT = 10001;
    public static final int VIDEO_STATE_CONNET = 10002;
    public static final int VIDEO_STATE_LOGIN = 10003;
    public static final int VIDEO_STATE_PLAY = 10004;
    public static final int VIDEO_STATE_PAUSE = 10005;
    /**
     * 播放错误：
     * 20001：初始化
     * 20002：连接
     * 20003：登录
     * 20004：播放
     * 20005：暂停
     */
    public static final int VIDEO_ERROR_STATE_INIT = 20001;
    public static final int VIDEO_ERROR_STATE_CONNET = 20002;
    public static final int VIDEO_ERROR_STATE_LOGIN = 20003;
    public static final int VIDEO_ERROR_STATE_PLAY = 20004;
    public static final int VIDEO_ERROR_STATE_PAUSE = 20005;
}
