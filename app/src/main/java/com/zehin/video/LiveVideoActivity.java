package com.zehin.video;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.zehin.video.utils.APPScreen;
import com.zehin.video.view.VideoLayout;
import com.zehin.videosdk.Video;
import com.zehin.videosdk.VideoClickListener;
import com.zehin.videosdk.VideoPlayRecord;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.zehin.video.constants.Constants.LOG;

public class LiveVideoActivity extends Activity implements VideoClickListener, VideoLayout.VideoLayoutClickListener {

    // 视频控件
    private VideoLayout videoLayout;

    // 视频
    private Video video = null;

    // 消息处理
    private Handler videoHandler = null;

    private APPScreen screen = null;

    private String IP = "";
    private int camId = 0;
    private int streamType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_live);
        initVideoLayout();
        screen = new APPScreen(this); // 旋转屏幕
    }

    /**
     * 初始化视频布局
     */
    private void initVideoLayout() {
        IP = getIntent().getStringExtra("IP");
        camId = getIntent().getIntExtra("camId",0);
        streamType = getIntent().getIntExtra("streamType",0);
        // 获取视频布局
        videoLayout = (VideoLayout) findViewById(R.id.video);
        videoLayout.setOnVideoLayoutClickListener(this);
        // 播放类型-直播
        videoLayout.setVideoPlayType(VideoLayout.VIDEOLAYOUT_PLAY_TYPE_LIVE);
        // 设置参数
        videoLayout.setVideoPlayParams(IP, IP, camId, streamType);
        // 开始播放
        videoLayout.startPlayVideo();
    }

    /**
     * 消息处理
     */
    private void handlerVideoMessage() {
        videoHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
//                switch (msg.what){
//                    case Video.VIDEO_ERROR_STATE_INIT:
//                    case Video.VIDEO_ERROR_STATE_CONNET:
//                        Toast.makeText(LiveVideoActivity.this, "连接服务失败！", Toast.LENGTH_SHORT).show();
//                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON);
//                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON;
//                        break;
//                    case Video.VIDEO_ERROR_STATE_LOGIN:
//                    case Video.VIDEO_ERROR_STATE_PLAY:
//                        Toast.makeText(LiveVideoActivity.this, "连接超时!", Toast.LENGTH_SHORT).show();
//                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON);
//                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON;
//                        break;
//                    case Video.VIDEO_STATE_NOINIT: // 恢复未初始化状态
//                        videoResumeNoInfoState();
//                        break;
//                    case Video.VIDEO_STATE_PLAY: // 开始播放
//                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
//                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE;
//                        break;
//                    default:
//                        break;
//                }
            }
        };
    }

    /**
     * Video监听
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    public void videoPlayRecord(List<VideoPlayRecord> list) {

    }

    @Override
    public void videoMessageData(int width, int height, byte[] data) {

    }

    @Override
    public void videoUpDateTime(Date date) {

    }

    @Override
    public void videoErrorListener(int keyError) {
        Log.e(LOG, "keyError:"+keyError);
        videoHandler.sendEmptyMessage(keyError);
    }

    /**
     * VideoLayout监听
     *----------------------------------------------------------------------------------------------
     */

    @Override
    public void videoPlayStartClickLinstener() {
        Log.d(LOG, "videoPlayStartClickLinstener");
    }

    @Override // 全屏
    public void videoPlayFullScreenClickLinstener() {
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 设置小屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            videoLayout.setLayoutParams(new RelativeLayout.LayoutParams(screen.getAPPScreenWidth(), 500));
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 设置全屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            videoLayout.setLayoutParams(new RelativeLayout.LayoutParams(screen.getAPPScreenHeight(), screen.getAPPScreenWidth()));
        }
    }

    @Override
    public void videoPlayExitClickLinstener() {

    }

    @Override
    public void videoPlayDateClickLinstener() {

    }

    @Override
    public void videoBottomPlayButtonClickLinstener(boolean isChecked) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onTouchEventClickLinstener() {

    }

    /*
    ------------------------------------------------------------------------------------------------
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 设置小屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            videoLayout.setLayoutParams(new RelativeLayout.LayoutParams(screen.getAPPScreenWidth(), 500));
            return true;
        } else {
            // 退出播放
            videoLayout.exitPlayVideo();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 恢复未初始化状态
     */
    private void videoResumeNoInfoState(){
        // 退出登录
        switch (video.videoState){
            case Video.VIDEO_STATE_LOGIN: // 退出登录
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        video.logoutVideo();
                    }
                }.start();
                break;
            case Video.VIDEO_STATE_PLAY: // 停止播放，退出登录
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
        // 修改状态
        video.videoState = Video.VIDEO_STATE_NOINIT;
        video.videoIsPlay = false;
    }
}
