package com.zehin.video;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.zehin.videosdk.view.VideoLayout;

/**
 * Created by wlf on 2017/6/11.
 */

public class BackPlayVideoActivity extends Activity implements VideoLayout.VideoLayoutClickListener {

    // 视频控件
    private VideoLayout videoLayout;

    private String stunIP = "";
    private String centerIP = "";
    private int camId = 0;
    private int streamType = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_backplay);
        initVideoLayout();
    }

    /**
     * 初始化视频布局
     */
    private void initVideoLayout() {
        stunIP = getIntent().getStringExtra("stunIP");
        centerIP = getIntent().getStringExtra("centerIP");
        camId = getIntent().getIntExtra("camId",0);
        streamType = getIntent().getIntExtra("streamType",0);
        // 获取视频布局
        videoLayout = (VideoLayout) findViewById(R.id.video);
        videoLayout.setOnVideoLayoutClickListener(this);
        // 播放类型-回放
        videoLayout.setVideoPlayType(VideoLayout.VIDEOLAYOUT_PLAY_TYPE_PLAYBACK);
        // 设置参数
        videoLayout.setVideoPlayParams(stunIP, centerIP, camId, streamType);
    }

    /**
     * VideoLayout监听
     *----------------------------------------------------------------------------------------------
     */

    @Override // 小屏 全屏button
    public void videoPlayFullScreenClickLinstener() {
    }

    @Override // 全屏 退出button
    public void videoPlayExitClickLinstener() {
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 退出播放
        videoLayout.exitPlayVideo();
        return super.onKeyDown(keyCode, event);
    }
}
