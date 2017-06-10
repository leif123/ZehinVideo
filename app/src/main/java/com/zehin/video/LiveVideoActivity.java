package com.zehin.video;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zehin.video.view.VideoLayout;
import com.zehin.videosdk.Video;
import com.zehin.videosdk.VideoClickListener;

import java.util.UUID;

import static com.zehin.video.constants.Constants.LOG;

public class LiveVideoActivity extends Activity implements VideoClickListener, VideoLayout.VideoLayoutClickListener {

    // 视频控件
    private VideoLayout videoLayout;

    // 视频
    private Video video = null;

    // 消息处理
    private Handler videoHandler = null;

    //
    private String IP = "218.201.111.234";
    private String userName = UUID.randomUUID().toString();
    private int camId = 1062043;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        // 获取视频布局
        videoLayout = (VideoLayout) findViewById(R.id.video);
        videoLayout.setOnVideoLayoutClickListener(this);
        videoLayout.setVideoPlayStateVisibility(1);

        // 获取视频单例
        video = Video.getInstance();
        video.setOnVideoClickListener(this);

        // 消息处理
        handlerVideoMessage();

        // 请求播放视频
        videoStartPlay();
    }

    /**
     * 消息处理
     */
    private void handlerVideoMessage() {
        videoHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case VIDEO_ERROR_STATE_INIT:
                    case VIDEO_ERROR_STATE_CONNET:
                        Toast.makeText(LiveVideoActivity.this, "连接服务失败！", Toast.LENGTH_SHORT).show();
                    case VIDEO_ERROR_STATE_LOGIN:
                    case VIDEO_ERROR_STATE_PLAY:
                        Toast.makeText(LiveVideoActivity.this, "连接超时!", Toast.LENGTH_SHORT).show();
                        videoLayout.setVideoPlayStateVisibility(2);
                        break;
                    case VIDEO_STATE_NOINIT: // 恢复未初始化状态
                        videoResumeNoInfoState();
                        break;
                    case VIDEO_STATE_PLAY: // 开始播放
                        videoLayout.setVideoPlayStateVisibility(0);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /*
    ------------------------------------------------------------------------------------------------
     */

    @Override
    public void initVideo(boolean arg0) {
        Log.v(LOG, "initVideo:"+arg0);
        if (arg0)
            video.videoState = video.VIDEO_STATE_INIT;
    }

    @Override
    public void connetVideo(boolean arg0) {
        Log.v(LOG, "connetVideo:"+arg0);
        if (arg0)
            video.videoState = video.VIDEO_STATE_CONNET;
    }

    @Override
    public void loginVideo(boolean arg0) {
        Log.v(LOG, "loginVideo:"+arg0);
        if (arg0)
            video.videoState = video.VIDEO_STATE_LOGIN;
    }

    @Override
    public void playVideo(boolean arg0) {
        Log.v(LOG, "playVideo:"+arg0);
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
        Log.e(LOG, "keyError:"+keyError);
        videoHandler.sendEmptyMessage(keyError);
    }

    /*
    ------------------------------------------------------------------------------------------------
     */

    @Override
    public void videoPlayButtonRestartSmailClickLinstener() {
        Log.d(LOG, "videoPlayButtonRestartSmailClickLinstener");
        videoLayout.setVideoPlayStateVisibility(3);
        videoLayout.setVideoPlayStateVisibility(1);
        videoStartPlay();
    }

    /*
    ------------------------------------------------------------------------------------------------
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        videoHandler.sendEmptyMessage(VIDEO_STATE_NOINIT);
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 请求播放
     */
    private void videoStartPlay(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                switch (video.videoState){
                    case VIDEO_STATE_NOINIT:
                        if(video.initVideo()){
                            video.setVideoParams(IP, IP);
                        } else {
                          break;
                        }
                    case VIDEO_STATE_INIT:
                        if(!video.connetVideo()) {
                            break;
                        }
                    case VIDEO_STATE_CONNET:
                        userName = UUID.randomUUID().toString();
                        if(!video.loginVideo(userName)){
                            break;
                        }
                    case VIDEO_STATE_LOGIN:
                        video.playVideo(camId, 0, userName);
                        break;
                }
            }
        }.start();
    }

    /**
     * 恢复未初始化状态
     */
    private void videoResumeNoInfoState(){
        // 修改状态
        video.videoState = VIDEO_STATE_NOINIT;
        // 退出登录
        switch (video.videoState){
            case VIDEO_STATE_LOGIN: // 退出登录
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        video.logoutVideo();
                    }
                }.start();
                break;
            case VIDEO_STATE_PLAY: // 停止播放，退出登录
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        video.stopVideo();
                        video.logoutVideo();
                    }
                }.start();
                break;
            default:
                break;
        }
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
