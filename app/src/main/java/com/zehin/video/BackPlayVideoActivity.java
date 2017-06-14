package com.zehin.video;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.zehin.video.utils.DateUtil;
import com.zehin.video.view.VideoLayout;
import com.zehin.videosdk.ListAdapter_Video;
import com.zehin.videosdk.Video;
import com.zehin.videosdk.VideoClickListener;
import com.zehin.videosdk.VideoPlayRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.zehin.video.constants.Constants.LOG;

/**
 * Created by wlf on 2017/6/11.
 */

public class BackPlayVideoActivity extends Activity implements VideoClickListener, VideoLayout.VideoLayoutClickListener {

    // 视频控件
    private VideoLayout videoLayout;

    // 视频
    private Video video = null;

    // 消息处理
    private Handler videoHandler = null;
    private static final int VIDEO_CHANGE_TIME = 5000; // 时间更新
    private static final int VIDEO_CHANGE_SEEKBAR = 5001; // 进度条更新

    //
    private String IP = "218.201.111.234";
//    private String IP = "123.234.227.107";
//    private String IP = "192.168.3.158";
    private String userName = UUID.randomUUID().toString();
    private int camId = 1062043;
//    private int camId = 5126; // 交通公司
//    private int camId = 13558;
//    private int camId = 1062091;
//    private int camId = 1061419;
    private int streamType = 0;

    // 时间
    private DateUtil dateUtil = new DateUtil();
    private int mYear;
    private int mMonth;
    private int mDay;

    // 视频列表
    private List<VideoPlayRecord> data;
    private ListAdapter_Video adapter;
    private ListView videoListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_backplay);
        initVideoLayout();

        // 获取视频单例
        video = Video.getInstance();
        video.setOnVideoClickListener(this);

        // 消息处理
        handlerVideoMessage();

        // 显示日历
        showCalendar();

        // 请求登录
        requestLoginVideo();
    }

    /**
     * 初始化视频布局
     */
    private void initVideoLayout() {
        // 获取视频布局
        videoLayout = (VideoLayout) findViewById(R.id.video);
        videoLayout.setOnVideoLayoutClickListener(this);
        // 播放类型-回放
        videoLayout.setVideoPlayType(VideoLayout.VIDEOLAYOUT_PLAY_TYPE_PLAYBACK);
        // 设置参数
        videoLayout.setVideoPlayParams(IP, IP, camId, 0);
        // 开始播放
        videoLayout.startPlayVideo();

        // 获取listView
        data = new ArrayList<VideoPlayRecord>();
        adapter = new ListAdapter_Video(BackPlayVideoActivity.this, data);
        videoListView = videoLayout.getVideoLayoutListView();
        videoListView.setAdapter(adapter);
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position > -1){
                    for(int i=0; i<data.size(); i++){
                        if(i != position){
                            data.get(i).setStatus("0");
                        }
                    }
                    data.get(position).setStatus("1");
                    adapter.notifyDataSetChanged(); //刷新布局
                    playSelectVideo(position);
                }
            }
        });

        // 初始化时间
        mYear = dateUtil.year;
        mMonth = dateUtil.month;
        mDay = dateUtil.day;
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
                    case Video.VIDEO_ERROR_STATE_INIT:
                    case Video.VIDEO_ERROR_STATE_CONNET:
                        Log.v(LOG,"handlerVideoMessage连接服务失败！");
                        Toast.makeText(BackPlayVideoActivity.this, "连接服务失败！", Toast.LENGTH_SHORT).show();
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON);
                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON;
                        break;
                    case Video.VIDEO_ERROR_STATE_LOGIN:
                    case Video.VIDEO_ERROR_STATE_PLAY:
                        Log.v(LOG,"handlerVideoMessage连接超时!");
                        Toast.makeText(BackPlayVideoActivity.this, "连接超时!", Toast.LENGTH_SHORT).show();
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON);
                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON;
                        break;
                    case Video.VIDEO_ERROR_STATE_QUERY:
                        Toast.makeText(BackPlayVideoActivity.this, "获取视频列表失败!", Toast.LENGTH_SHORT).show();
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
                        break;
                    case Video.VIDEO_ERROR_STATE_SEARCH1: // 云终端不在线
                        Toast.makeText(BackPlayVideoActivity.this, "云终端不在线!", Toast.LENGTH_SHORT).show();
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
                        break;
                    case Video.VIDEO_ERROR_STATE_SEARCH2: // 镜头不在线
                        Toast.makeText(BackPlayVideoActivity.this, "镜头不在线!", Toast.LENGTH_SHORT).show();
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
                        break;
                    case Video.VIDEO_STATE_NOINIT: // 恢复未初始化状态
                        Log.v(LOG,"handlerVideoMessage恢复未初始化状态!");
                        videoResumeNoInfoState();
                        break;
                    case Video.VIDEO_STATE_SEARCH: // 返回视频记录列表
                        adapter.notifyDataSetChanged(); //刷新布局
                        if(data.size() == 0){ // 没有数据
                            Toast.makeText(BackPlayVideoActivity.this, "未查询到视频记录!", Toast.LENGTH_SHORT).show();
                            videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
                        } else {
                            // 修改布局
                            switch (videoLayout.videoLayoutState){
                                case VideoLayout.VIDEOLAYOUT_STATE_INIT:
                                    videoLayout.setShowVideoLayoutState(VideoLayout.VIDEOLAYOUT_STATE_HEAD_AND_TAIL);
                                    videoLayout.setShowVideoLayoutState(VideoLayout.VIDEOLAYOUT_STATE_VIDEOLIST_SHOW);
                                    videoLayout.videoLayoutState = VideoLayout.VIDEOLAYOUT_STATE_VIDEOLIST_SHOW;
                                    break;
                                case VideoLayout.VIDEOLAYOUT_STATE_HEAD_AND_TAIL:
                                    videoLayout.setShowVideoLayoutState(VideoLayout.VIDEOLAYOUT_STATE_VIDEOLIST_SHOW);
                                    videoLayout.videoLayoutState = VideoLayout.VIDEOLAYOUT_STATE_VIDEOLIST_SHOW;
                                    break;
                                case VideoLayout.VIDEOLAYOUT_STATE_VIDEOLIST_SHOW:
                                    break;
                                case VideoLayout.VIDEOLAYOUT_STATE_VIDEOLIST_HIDE:
                                    videoLayout.setShowVideoLayoutState(VideoLayout.VIDEOLAYOUT_STATE_VIDEOLIST_SHOW);
                                    videoLayout.videoLayoutState = VideoLayout.VIDEOLAYOUT_STATE_VIDEOLIST_SHOW;
                                    break;
                            }
                            // 播放第一条视频记录
                            playSelectVideo(0);
                        }
                        break;
                    case Video.VIDEO_STATE_PLAY: // 开始播放
                        Log.v(LOG,"handlerVideoMessage开始播放!");
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE;
                        videoLayout.playButton.setChecked(true);
                        break;
                    case VIDEO_CHANGE_TIME: // 更新时间
                        videoLayout.setVideoLayoutTime(dateUtil.getStringDate(video.nowTime,DateUtil.DATE_FORMAT_HMS)+"/"+dateUtil.getStringDate(video.endTime,DateUtil.DATE_FORMAT_HMS));
                        break;
                    case VIDEO_CHANGE_SEEKBAR: // 更新进度条
                        if((video.endTime.getTime()-video.startTime.getTime() == 0)){
                            videoLayout.setVideoLayoutSeekBar(0);
                        } else if(video.nowTime.getTime()>video.endTime.getTime()){ // 播放下一条
                            for(int i=0; i<data.size(); i++){
                                if("1".equals(data.get(i).getStatus())){
                                    if(data.size() > i+2){
                                        data.get(i).setStatus("0");
                                        data.get(i+1).setStatus("1");
                                        adapter.notifyDataSetChanged(); //刷新布局
                                        playSelectVideo(i+1);
                                    }
                                    break;
                                }
                            }
                        } else {
                            videoLayout.setVideoLayoutSeekBar((int)((video.nowTime.getTime()-video.startTime.getTime())*100/(video.endTime.getTime()-video.startTime.getTime())));
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * Video监听
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    public void initVideo(boolean arg0) {
        Log.v(LOG, "loginVideo:"+arg0);
        if (arg0)
            video.videoState = Video.VIDEO_STATE_INIT;
    }

    @Override
    public void connetVideo(boolean arg0) {
        Log.v(LOG, "connetVideo:"+arg0);
        if (arg0)
            video.videoState = Video.VIDEO_STATE_CONNET;
    }

    @Override
    public void loginVideo(boolean arg0) {
        Log.v(LOG, "loginVideo:"+arg0);
        if (arg0)
            video.videoState = Video.VIDEO_STATE_LOGIN;
    }

    @Override
    public void playVideo(boolean arg0) {
        Log.v(LOG, "playVideo:"+arg0);
        if (arg0)
            video.videoState = Video.VIDEO_STATE_PLAY;
    }

    @Override // 返回视频记录列表
    public void videoPlayRecord(List<VideoPlayRecord> list) {
        data.clear();
        data.addAll(list);
        videoHandler.sendEmptyMessage(Video.VIDEO_STATE_SEARCH);
    }

    @Override // 收到播放流
    public void videoMessageData(int width, int height, byte[] data) {
        if(video.videoState == Video.VIDEO_STATE_PLAY){ // 播放状态
            videoLayout.upDateRenderer(width,height,data);
            if(!video.videoIsPlay) {
                video.videoIsPlay = true;
                // 开始播放
                videoHandler.sendEmptyMessage(Video.VIDEO_STATE_PLAY);
            }
        }
    }

    @Override // 视频播放时间
    public void videoUpDateTime(Date date) {
        // 更新下边栏时间
        videoHandler.sendEmptyMessage(VIDEO_CHANGE_TIME);
        // 更新进度条
        videoHandler.sendEmptyMessage(VIDEO_CHANGE_SEEKBAR);

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

    @Override //  重播button
    public void videoPlayStartClickLinstener() {
        if(video.videoIsPlay){ // 恢复
            if(video.videoPlayControl(0, 1, camId)){
                Log.v(LOG, "videoPlayControl:播放true");
            } else {
                Log.v(LOG, "videoPlayControl:播放false");
            }
            videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON);
            videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON;
        } else { // 重播
            videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR);
            videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR;
            requestStartPlayVideo();
        }
    }

    @Override // 小屏 全屏button
    public void videoPlayFullScreenClickLinstener() {

    }

    @Override // 全屏 退出button
    public void videoPlayExitClickLinstener() {
        videoResumeNoInfoState();
        finish();
    }

    @Override // 全屏 日历button
    public void videoPlayDateClickLinstener() {
        showCalendar();
    }

    @Override // 全屏 下边栏playButton
    public void videoBottomPlayButtonClickLinstener(boolean isChecked) {
        if(isChecked){ // 恢复播放
            if(video.videoPlayControl(0, 1, camId)){
                Log.v(LOG, "videoPlayControl:播放true");
            } else {
                Log.v(LOG, "videoPlayControl:播放false");
            }
            videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON);
            videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON;
        } else {  // 暂停播放
            if(video.videoPlayControl(0, 0, camId)){
                Log.v(LOG, "videoPlayControl:暂停true");
            }else{
                Log.v(LOG, "videoPlayControl:暂停false");
            }
            videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON);
            videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON;
        }
    }

    @Override // 进度条更新
    public void onStopTrackingTouch(SeekBar seekBar) { // 根据时间差 判断是否更新进度条
        if(video.videoIsPlay){
            video.videoPlayControl(0, 1, camId); // 恢复播放
            videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
            video.nowTime.setTime(video.startTime.getTime()+(long)((video.endTime.getTime()-video.startTime.getTime())*seekBar.getProgress()/100));
            // 请求跳转
            video.videoPlayControl(1,dateUtil.timeToInt(video.nowTime),camId);
        }
    }

    @Override // 滑动屏幕
    public void onTouchEventClickLinstener() {
        video.videoPlayControl(0, 1, camId); // 恢复播放
        // 请求跳转
        video.videoPlayControl(1,dateUtil.timeToInt(video.tempTime),camId);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        videoResumeNoInfoState();
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 请求登录
     */
    public void requestLoginVideo(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                switch (video.videoState){
                    case Video.VIDEO_STATE_NOINIT:
                        if(video.initVideo()){
                            video.setVideoParams(IP, IP);
                        } else {
                            break;
                        }
                    case Video.VIDEO_STATE_INIT:
                        if(!video.connetVideo()) {
                            break;
                        }
                    case Video.VIDEO_STATE_CONNET:
                        userName = UUID.randomUUID().toString();
                        if(!video.loginVideo(userName)){
                            break;
                        }
                }
            }
        }.start();
    }

    /**
     * 请求播放
     */
    private void requestStartPlayVideo(){
        Log.v(LOG,"requestStartPlayVideo");
        new Thread(){
            @Override
            public void run() {
                super.run();
                switch (video.videoState){
                    case Video.VIDEO_STATE_NOINIT:
                        Log.v(LOG,"VIDEO_STATE_NOINIT");
                        if(video.initVideo()){
                            video.setVideoParams(IP, IP);
                        } else {
                            break;
                        }
                    case Video.VIDEO_STATE_INIT:
                        Log.v(LOG,"VIDEO_STATE_INIT");
                        if(!video.connetVideo()) {
                            break;
                        }
                    case Video.VIDEO_STATE_CONNET:
                        Log.v(LOG,"VIDEO_STATE_CONNET");
                        userName = UUID.randomUUID().toString();
                        if(!video.loginVideo(userName)){
                            break;
                        }
                    case Video.VIDEO_STATE_LOGIN:
                        Log.v(LOG,"VIDEO_STATE_LOGIN");
                        video.searchVideoList(camId,mYear*10000+(mMonth+1)*100+mDay);
                        break;
                }
            }
        }.start();
    }

    /**
     * 播放视频记录
     * @param index 第几条  从0开始
     */
    private void playSelectVideo(int index) {
        video.videoIsPlay = false;
        if(data.size()>0){
            video.startTime = data.get(index).getStartTime();
            video.endTime = data.get(index).getStopTime();
            video.nowTime = data.get(index).getStartTime();
            Log.v(LOG,"startTime"+video.startTime);
            Log.v(LOG,"endTime"+video.endTime);
            Log.v(LOG,"nowTime"+video.nowTime);
            new Thread(){
                public void run() {
					if(video.videoIsPlay){ // 视频跳转
                        video.videoPlayControl(0, 1, camId); // 恢复播放
						if(video.videoPlayControl(1, dateUtil.timeToInt(video.startTime), camId)){
                            Log.d(LOG,"videoPlayControl:true");
					    }else{
					    	videoHandler.sendEmptyMessage(Video.VIDEO_STATE_NOINIT);
					    }
					} else { // 请求回放
                        switch (video.videoState){
                            case Video.VIDEO_STATE_NOINIT:
                            case Video.VIDEO_STATE_INIT:
                            case Video.VIDEO_STATE_CONNET:
                                requestStartPlayVideo();
                                break;
                            case Video.VIDEO_STATE_LOGIN:
                            case Video.VIDEO_STATE_PLAY:
                            case Video.VIDEO_STATE_PAUSE:
                            case Video.VIDEO_STATE_SEARCH:
                                video.playVideo(camId,streamType,userName,2,dateUtil.dateToInt(video.startTime),dateUtil.timeToInt(video.startTime));
                                break;
                        }
					}
                }
            }.start();
        }else{
            Toast.makeText(BackPlayVideoActivity.this, "未查询到视频记录", Toast.LENGTH_SHORT).show();
            videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
        }
    }

    /**
     * 显示日历
     */
    public void showCalendar(){
        new DatePickerDialog(BackPlayVideoActivity.this, setDateListener, mYear, mMonth, mDay).show();
    }

    // 日历回调监听
    private DatePickerDialog.OnDateSetListener setDateListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        // 修改日期
        videoLayout.setVideoLayoutDate(dateUtil.getStringDate(dateUtil.getIntToDate(mYear,mMonth+1,mDay),"yyyy-MM-dd"));
        // 修改布局
        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR);
        // 请求播放
        requestStartPlayVideo();
        }
    };

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
