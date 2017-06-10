package com.zehin.video;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.zehin.video.view.VideoLayout;
import com.zehin.videosdk.Video;
import com.zehin.videosdk.VideoClickListener;

public class VideoActivity extends Activity implements VideoClickListener{

    // 视频控件
    private VideoLayout videoLayout;

    private Video video = null;

    private String IP = "218.201.111.234";
    private String userName = "admin";
    private int camId = 1062043;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        videoLayout = (VideoLayout) findViewById(R.id.video);
        video = Video.getInstance();
        video.setOnVideoClickListener(this);

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

    @Override
    public void initVideo(boolean arg0) {
        System.out.println("------------>initVideo"+arg0);
    }

    @Override
    public void connetVideo(boolean arg0) {
        System.out.println("------------>connetVideo"+arg0);
    }

    @Override
    public void loginVideo(boolean arg0) {
        System.out.println("------------>loginVideo"+arg0);
    }

    @Override
    public void playVideo(boolean arg0) {
        System.out.println("------------>playVideo"+arg0);
    }

    @Override
    public void videoErrorListener(int keyError) {
        System.out.println("------------>keyError"+keyError);
    }

    @Override
    public void videoMessageData(int width, int height, byte[] data) {
        videoLayout.upDateRenderer(width,height,data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                video.stopVideo();
                video.logoutVideo();
            }
        }.start();
        return super.onKeyDown(keyCode, event);
    }
}
