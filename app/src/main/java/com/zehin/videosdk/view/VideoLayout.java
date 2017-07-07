package com.zehin.videosdk.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zehin.video.R;
import com.zehin.videosdk.utils.DateUtil;
import com.zehin.videosdk.adapter.ListAdapter_Video;
import com.zehin.videosdk.Video;
import com.zehin.videosdk.VideoClickListener;
import com.zehin.videosdk.entity.VideoPlayRecord;
import com.zehin.videosdk.VideoSDK;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.zehin.videosdk.constants.VideoConstants.LOG;

/**
 * Created by wlf on 2017/6/5.
 * 视频控件
 */
public class VideoLayout extends RelativeLayout implements View.OnClickListener,SeekBar.OnSeekBarChangeListener, VideoClickListener {

    // 视频
    private Video video = null;

    /**
     * 时间
     */
    private DateUtil dateUtil = new DateUtil();
    private Date startTime; // 开始时间
    private Date endTime; // 结束时间
    private Date nowTime; // 现在时间
    private Date tempTime = new Date();
    private int mYear;
    private int mMonth;
    private int mDay;

    private String centerIP = ""; // 中心IP
    private String stunIP = ""; // 打洞IP
    private int camId = 0; // 镜头ID
    private int streamType = 0; // 码流类型
    private String userName = "";

    // 视频列表
    private List<VideoPlayRecord> data;
    private ListAdapter_Video adapter;

    /**
     * 构造方法
     * @param context
     * @param attrs
     */
    public VideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        // 获取视频单例
        video = Video.getInstance();
        video.setOnVideoClickListener(this);

        // 获取布局属性值
//        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoLayout);
        // 释放
//        ta.recycle();

        // 初始化布局
        initLayout(context);
        // 初始化参数
        initParams(context);
    }

    /**
     * 点击事件监听
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public void onClick(View v) {
        if(v == playButtonBig){ // 重播
            if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
                Log.v(LOG,"重播");
                // 动画
                animationPlayButtonRestart.setFillAfter(true);
                animationPlayButtonRestart.setFillBefore(false);
                playButtonBig.setImageResource(R.drawable.video_start_button);
                playButtonBig.setVisibility(View.GONE);
                playButtonBig.startAnimation(animationPlayButtonRestart);
            } else if (videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK) { // 回放
                Log.v(LOG,"回放");
                animationPlayButtonRestart.setFillAfter(true);
                animationPlayButtonRestart.setFillBefore(false);
                playButtonBig.setImageResource(R.drawable.video_start_big_button);
                playButtonBig.setVisibility(View.GONE);
                playButtonBig.startAnimation(animationPlayButtonRestart);
            }
            // 加载
            progressBar.setVisibility(View.VISIBLE);
            // 请求播放
            startPlayVideo();
        } else if(v == playButton){ // 播放/暂停
            Log.v(LOG,"playButton:"+videoIsPlay);
            if(videoIsPlay){
                playButton.setEnabled(true);//可用
                if(playButton.isChecked()){ // 恢复播放
                    if (videoState == VIDEO_STATE_PAUSE){
                        playButton.setChecked(true);
                        videoState = VIDEO_STATE_PLAY_WAIT;
                        VideoSDK.vPaasSDK_BackStreamControl(0,1,camId,0);
                    }
                } else { // 暂停播放
                    if (videoState == VIDEO_STATE_PLAY){
                        playButton.setChecked(false);
                        videoState = VIDEO_STATE_PAUSE;
                        VideoSDK.vPaasSDK_BackStreamControl(0,0,camId,0);
                    }
                }
            } else {
                playButton.setChecked(false);
                playButton.setEnabled(false);//不可用
            }
        } else if(v == smallScreenFullButton){ // 全屏
            listener.videoPlayFullScreenClickLinstener();
        } else if(v == fullScreenBackButton){ // 退出
            exitPlayVideo();
            listener.videoPlayExitClickLinstener();
        } else if(v == fullScreenDateButton){ // 日历
            // 显示日历
            new DatePickerDialog(context, setDateListener, mYear, mMonth, mDay).show();
        } else if(v == fullScreenMoreButton){ // 更多
            switch (videoLayoutState){
                case VIDEOLAYOUT_STATE_HEAD_AND_TAIL:
                    videoLayoutState = VIDEOLAYOUT_STATE_VIDEOLIST;
                    videoPlayListLayout.setVisibility(View.VISIBLE);
                    videoPlayListLayout.startAnimation(animationVideoPlayDataListOpen);
                    break;
                case VIDEOLAYOUT_STATE_VIDEOLIST:
                    videoLayoutState = VIDEOLAYOUT_STATE_HEAD_AND_TAIL;
                    videoPlayListLayout.setVisibility(View.GONE);
                    videoPlayListLayout.startAnimation(animationVideoPlayDataListClose);
                    break;
            }
        }
    }

    /**
     * 消息处理
     * ---------------------------------------------------------------------------------------------
     */
    private void handlerVideoMessage() {
        videoHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case VIDEO_ERROR_STATE_INIT:
                        Toast.makeText(context, "初始化失败！", Toast.LENGTH_SHORT).show();
                        // progressBar隐藏
                        progressBar.setVisibility(View.GONE);
                        // 显示重播按钮
                        if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
                            playButtonBig.setImageResource(R.drawable.video_stop_button);
                        } else if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
                            playButtonBig.setImageResource(R.drawable.video_stop_big_button);
                        }
                        animationPlayButtonRestart.setFillBefore(true);
                        animationPlayButtonRestart.setFillAfter(false);
                        playButtonBig.setVisibility(View.VISIBLE);
                        Log.e(LOG,"初始化失败");
                        break;
                    case VIDEO_ERROR_STATE_CONNET:
                        Toast.makeText(context, "连接服务失败！", Toast.LENGTH_SHORT).show();
                        // progressBar隐藏
                        progressBar.setVisibility(View.GONE);
                        // 显示重播按钮
                        if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
                            playButtonBig.setImageResource(R.drawable.video_stop_button);
                        } else if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
                            playButtonBig.setImageResource(R.drawable.video_stop_big_button);
                        }
                        animationPlayButtonRestart.setFillBefore(true);
                        animationPlayButtonRestart.setFillAfter(false);
                        playButtonBig.setVisibility(View.VISIBLE);
                        Log.e(LOG,"连接服务失败");
                        break;
                    case VIDEO_ERROR_STATE_LOGIN:
                    case VIDEO_ERROR_STATE_PLAY:
                        Toast.makeText(context, "连接超时!", Toast.LENGTH_SHORT).show();
                        // progressBar隐藏
                        progressBar.setVisibility(View.GONE);
                        // 显示重播按钮
                        if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
                            playButtonBig.setImageResource(R.drawable.video_stop_button);
                        } else if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
                            playButtonBig.setImageResource(R.drawable.video_stop_big_button);
                        }
                        animationPlayButtonRestart.setFillBefore(true);
                        animationPlayButtonRestart.setFillAfter(false);
                        playButtonBig.setVisibility(View.VISIBLE);
                        Log.e(LOG,"连接超时");
                        break;
                    case VIDEO_ERROR_STATE_QUERY: // 查询视频列表失败
                        Toast.makeText(context,"获取视频列表失败!",Toast.LENGTH_SHORT).show();
                        // progressBar隐藏
                        progressBar.setVisibility(View.GONE);
                        break;
                    case VIDEO_ERROR_STATE_SEARCH1:
                        Toast.makeText(context,"云终端不在线!",Toast.LENGTH_SHORT).show();
                        // progressBar隐藏
                        progressBar.setVisibility(View.GONE);
                        break;
                    case VIDEO_ERROR_STATE_SEARCH2:
                        Toast.makeText(context,"镜头不在线!",Toast.LENGTH_SHORT).show();
                        // progressBar隐藏
                        progressBar.setVisibility(View.GONE);
                        break;
                    case VIDEO_STATE_SEARCH: // 查找视频列表成功
                        adapter.notifyDataSetChanged(); //刷新布局
                        // 修改状态
                        videoIsPlay = false;
                        progressBar.setVisibility(View.GONE);
                        if(data.size() == 0){ // 没有数据
                            Toast.makeText(context, "没有视频记录!", Toast.LENGTH_SHORT).show();
                        } else {
                            // 修改布局
                            switch (videoLayoutState){
                                case VIDEOLAYOUT_STATE_INIT:
                                    fullScreenTopBarLayout.setVisibility(View.VISIBLE);
                                    fullScreenBottomBarLayout.setVisibility(View.VISIBLE);
                                    videoPlayListLayout.setVisibility(View.VISIBLE);
                                    videoPlayListLayout.startAnimation(animationVideoPlayDataListOpen);
                                    videoLayoutState = VIDEOLAYOUT_STATE_VIDEOLIST;
                                    break;
                                case VIDEOLAYOUT_STATE_HEAD_AND_TAIL:
                                    videoPlayListLayout.setVisibility(View.VISIBLE);
                                    videoPlayListLayout.startAnimation(animationVideoPlayDataListOpen);
                                    videoLayoutState = VIDEOLAYOUT_STATE_VIDEOLIST;
                                    break;
                            }
                        }
                        break;
                    case VIDEO_STATE_PLAY: // 开始播放
                        progressBar.setVisibility(View.GONE);
                        playButton.setEnabled(true);//可用
                        playButton.setChecked(true);
                        break;
                    case VIDEO_STATE_STOP: // 停止播放
                        if(0 != VideoSDK.vPaasSDK_StopPlay()){
                            Log.e(LOG, "stopPlayStream fail");
                        }
                        break;
                    case VIDEO_STATE_UPDATE: // 更新时间  进度条
                        // 修改播放时间
                        playTime.setText(dateUtil.getStringDate(nowTime,DateUtil.DATE_FORMAT_HMS)+"/"+dateUtil.getStringDate(endTime,DateUtil.DATE_FORMAT_HMS));
                        // 更新进度条
                        if((endTime.getTime()-startTime.getTime() == 0)){
                            seekBar.setProgress(0);
                        } else {
                            seekBar.setProgress((int)((nowTime.getTime()-startTime.getTime())*100/(endTime.getTime()-startTime.getTime())));
                        }
                        break;
                    case VIDEO_STATE_UPLIST: // 更新视频列表
                        adapter.notifyDataSetChanged(); //刷新布局
                        break;
                }
            }
        };
    }

    /**
     * 方法定义
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * 设置播放类型
     * @param type
     */
    public void setVideoPlayType(int type){
        videoPlayType = type;
        // 加载布局
        switch (type){
            case VIDEOLAYOUT_PLAY_TYPE_LIVE: // 直播
                // ProgressBar控件
                progressBar.setVisibility(View.VISIBLE);
                break;
            case VIDEOLAYOUT_PLAY_TYPE_PLAYBACK: // 回放
                // 全屏 上边栏 布局
                fullScreenTopBarLayout.setVisibility(View.VISIBLE);
                // 全屏 下边栏 布局
                fullScreenBottomBarLayout.setVisibility(View.VISIBLE);
                videoLayoutState = VIDEOLAYOUT_STATE_HEAD_AND_TAIL;
                // 显示日历
                new DatePickerDialog(context, setDateListener, mYear, mMonth, mDay).show();
                // 视频数据
                data = new ArrayList<VideoPlayRecord>();
                adapter = new ListAdapter_Video(context, data);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                            videoState = VIDEO_STATE_PLAY_WAIT;
                            progressBar.setVisibility(View.VISIBLE);
                            playButtonBig.setVisibility(View.GONE);
                            playButton.setEnabled(false);//不可用
                            playButton.setChecked(false);
                            seekBar.setProgress(0);
                            // 请求播放
                            playSelectVideo(position);
                        }
                    }
                });
                break;
        }
    }

    /**
     * 日历回调监听
     */
    private DatePickerDialog.OnDateSetListener setDateListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            // 修改日期
            dateText.setText(dateUtil.getStringDate(dateUtil.getIntToDate(mYear,mMonth+1,mDay),"yyyy-MM-dd"));
            // 修改布局
            progressBar.setVisibility(View.VISIBLE);
            // 中间暂停button如果显示，改为隐藏
            playButtonBig.setVisibility(View.GONE);
            // 设置当前时间
            nowTime = dateUtil.getIntToDate(mYear,mMonth+1,mDay);
            video.setNowTime(nowTime);
            // 停流
            if (videoIsPlay || isBackPlayVideoSuccess){
                videoHandler.sendEmptyMessage(VIDEO_STATE_STOP);
                videoState = VIDEO_STATE_LOGIN;
            }
            // 修改状态
            videoIsPlay = false;
            isBackPlayVideoSuccess = false;
            // 请求播放
            startPlayVideo();
        }
    };

    /**
     * 设置参数
     * @param stunIP
     * @param centerIP
     * @param camId
     */
    public void setVideoPlayParams(String stunIP, String centerIP, int camId, int streamType){
        this.stunIP = stunIP;
        this.centerIP = centerIP;
        this.camId = camId;
        this.streamType = streamType;
    }

    /**
     * 请求播放
     */
    public void startPlayVideo(){
        Log.d(LOG,"请求播放");
        new Thread(){
            @Override
            public void run() {
                super.run();
                switch (videoState){
                    case VIDEO_STATE_NOINIT:
                        if(VideoSDK.vPaasSDK_Init()){
                            VideoSDK.vPaasSDK_SetStunIP(stunIP);
                            VideoSDK.vPaasSDK_SetCenterIP(centerIP);
                            VideoSDK.vPaasSDK_SetCenterCmdPort(10000);
                            VideoSDK.vPaasSDK_SetCenterHeartPort(20000);
                            VideoSDK.vPaasSDK_SetCmdCallBack();
                            videoState = VIDEO_STATE_INIT;
                            Log.d(LOG, "初始化");
                        } else {
                            videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_INIT);
                            break;
                        }
                    case VIDEO_STATE_INIT:
                        if(VideoSDK.vPaasSDK_ConnetStunSer()) {
                            videoState = VIDEO_STATE_CONNET;
                            Log.d(LOG, "连接");
                        } else {
                            Log.d(LOG, "连接失败");
                            videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_CONNET);
                            break;
                        }
                    case VIDEO_STATE_CONNET:
                        userName = UUID.randomUUID().toString();
                        if(1000 == VideoSDK.vPaasSDK_Login(userName, "admin", 2, 2)){
                            videoState = VIDEO_STATE_LOGIN;
                            Log.d(LOG, "登录");
                        } else {
                            Log.d(LOG, "登录失败");
                            videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_LOGIN);
                            break;
                        }
                    case VIDEO_STATE_LOGIN:
                        if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
                            if(VideoSDK.vPaasSDK_GetZehinTransferId(camId, streamType, 2, userName, 1, 0, 0)){
                                Log.d(LOG, "直播");
                                videoState = VIDEO_STATE_PLAY_WAIT;
                            } else {
                                Log.d(LOG, "直播失败");
                                videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_PLAY);
                            }
                        } else if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
                            // 查询视频列表
                            Log.v(LOG, "查询视频列表");
                            if(!VideoSDK.vPaasSDK_BackSearch(camId, mYear*10000+(mMonth+1)*100+mDay, 000000, 235959)){
                                Log.v(LOG, "查询视频列表失败");
                                videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_QUERY); // 查询失败
                            }
                            // 修改状态
                            videoIsPlay = false;
                        }
                        break;
                }
            }
        }.start();
    }

    /**
     * 播放视频记录
     * 0:请求回放  1:视频跳转
     * @param position 从0开始
     */
    private void playSelectVideo(int position){
        // 修改状态
        videoIsPlay = false;
        if(data.size()>0){
            startTime = data.get(position).getStartTime();
            endTime = data.get(position).getStopTime();
            nowTime = data.get(position).getStartTime();
            // 修改播放时间
            playTime.setText(dateUtil.getStringDate(nowTime,DateUtil.DATE_FORMAT_HMS)+"/"+dateUtil.getStringDate(endTime,DateUtil.DATE_FORMAT_HMS));
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    if(isBackPlayVideoSuccess){ // 跳转
                        boolean backValue = VideoSDK.vPaasSDK_BackStreamControl(1,dateUtil.timeToInt(startTime),camId,0);// 跳转
                        Log.v(LOG,"------>跳转："+backValue);
                    } else { // 请求回放
                        if(VideoSDK.vPaasSDK_GetZehinTransferId(camId, streamType, 2, userName, 2, dateUtil.dateToInt(startTime),dateUtil.timeToInt(startTime))){
                            Log.v(LOG, "回放");
                            videoState = VIDEO_STATE_PLAY_WAIT;
                        } else {
                            Log.v(LOG, "回放失败");
                            videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_PLAY);
                        }
                    }
                }
            }.start();
        }else{ // 没有数据
            Toast.makeText(context, "未查询到视频记录", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * 退出播放
     */
    public void exitPlayVideo(){
        switch (videoState){
            case VIDEO_STATE_NOINIT:
            case VIDEO_STATE_INIT:
            case VIDEO_STATE_CONNET:
            case VIDEO_STATE_LOGIN:
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        // 0:登录超时; -1:连接中心失败; -2:用户名参数不对; -3:发送函数失败
                        int logout = VideoSDK.vPaasSDK_Logout();
                        Log.v(LOG, "vPaasSDK_Logout:"+logout);
                    }
                }.start();
                break;
            case VIDEO_STATE_PLAY_WAIT:
            case VIDEO_STATE_PLAY:
            case VIDEO_STATE_PAUSE:
            case VIDEO_STATE_STOP:
            case VIDEO_STATE_SEARCH:
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        // 停止播放
                        if(0 != VideoSDK.vPaasSDK_StopPlay()){
                            Log.e(LOG, "stopPlayStream fail");
                        }
                        // 0:登录超时; -1:连接中心失败; -2:用户名参数不对; -3:发送函数失败
                        int logout1 = VideoSDK.vPaasSDK_Logout();
                        Log.v(LOG, "vPaasSDK_Logout:"+logout1);
                    }
                }.start();
                break;
        }
    }

    /**
     * 接口回调
     * ---------------------------------------------------------------------------------------------
     */
    // Video listener
    private VideoLayoutClickListener listener;

    /**
     * Video listener 接口
     */
    public interface VideoLayoutClickListener{
        //  全屏button
        public void videoPlayFullScreenClickLinstener();
        // 全屏 退出button
        public void videoPlayExitClickLinstener();
    }

    /**
     * 设置Video listener
     * @param listener
     */
    public void setOnVideoLayoutClickListener(VideoLayoutClickListener listener){
        this.listener = listener;
    }

    /**
     * 进度条回调
     * ---------------------------------------------------------------------------------------------
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 在播放或暂停状态时，可以更新进度条
        if(videoState == VIDEO_STATE_PLAY || videoState == VIDEO_STATE_PAUSE){
            tempTime.setTime(startTime.getTime()+(long)((endTime.getTime()-startTime.getTime())*seekBar.getProgress()/100));
            // 修改状态
            videoState = VIDEO_STATE_PLAY_WAIT;
            progressBar.setVisibility(View.VISIBLE);
            playButtonBig.setVisibility(View.GONE);
            playButton.setEnabled(false);//不可用
            playButton.setChecked(false);
            videoIsPlay = false;
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    boolean backValue = VideoSDK.vPaasSDK_BackStreamControl(1,dateUtil.timeToInt(tempTime),camId,0);// 跳转
                    nowTime = tempTime;
                    Log.v(LOG,"------>跳转："+backValue);
                }
            }.start();
        }
    }

    /**
     * 屏幕滑动监听
     * ---------------------------------------------------------------------------------------------
     */

    private float startX,moveX,offsetX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    offsetX = event.getX()-startX;
                    if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
                        if(videoLayoutState == VIDEOLAYOUT_STATE_INIT){ // 当前不显示，改为显示下边栏
                            smallScreenBottomBarLayout.setVisibility(View.VISIBLE);
                            smallScreenBottomBarLayout.startAnimation(animationVideoLayoutBottomBarOpen);
                            videoLayoutState = VIDEOLAYOUT_STATE_HEAD_AND_TAIL; // 修改状态
                        } else if(videoLayoutState == VIDEOLAYOUT_STATE_HEAD_AND_TAIL){ // 当前显示下边栏，改为隐藏下边栏
                            smallScreenBottomBarLayout.setVisibility(View.GONE);
                            smallScreenBottomBarLayout.startAnimation(animationVideoLayoutBottomBarClose);
                            videoLayoutState = VIDEOLAYOUT_STATE_INIT; // 修改状态
                        }
                    } else if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
                        videoPlayFoward.setVisibility(View.GONE);
                        if(Math.abs(offsetX)<20){ // 点击
                            switch (videoLayoutState){
                                case VIDEOLAYOUT_STATE_INIT: // 当前不显示，改为显示上下边栏
                                    fullScreenTopBarLayout.setVisibility(View.VISIBLE);
                                    fullScreenTopBarLayout.startAnimation(animationVideoLayoutTopBarOpen);
                                    fullScreenBottomBarLayout.setVisibility(View.VISIBLE);
                                    fullScreenBottomBarLayout.startAnimation(animationVideoLayoutBottomBarOpen);
                                    videoLayoutState = VIDEOLAYOUT_STATE_HEAD_AND_TAIL;
                                    break;
                                case VIDEOLAYOUT_STATE_VIDEOLIST:// 当前显示上下边栏和视频列表，改为隐藏上下边栏
                                    videoPlayListLayout.setVisibility(View.GONE);
                                    videoPlayListLayout.startAnimation(animationVideoPlayDataListClose);
                                case VIDEOLAYOUT_STATE_HEAD_AND_TAIL: // 当前显示上下边栏，改为隐藏上下边栏
                                    fullScreenTopBarLayout.setVisibility(View.GONE);
                                    fullScreenTopBarLayout.startAnimation(animationVideoLayoutTopBarClose);
                                    fullScreenBottomBarLayout.setVisibility(View.GONE);
                                    fullScreenBottomBarLayout.startAnimation(animationVideoLayoutBottomBarClose);
                                    videoLayoutState = VIDEOLAYOUT_STATE_INIT;
                                    break;
                            }
                        } else { // 快进/后退
                            if(videoState == VIDEO_STATE_PLAY && videoIsHaveTime){ // 只有正在播放时，视频源有返回时间时，才可以快进
                                // 修改状态
                                videoState = VIDEO_STATE_PLAY_WAIT;
                                progressBar.setVisibility(View.VISIBLE);
                                playButtonBig.setVisibility(View.GONE);
                                playButton.setEnabled(false);//不可用
                                playButton.setChecked(false);
                                videoIsPlay = false;
                                new Thread(){
                                    @Override
                                    public void run() {
                                        super.run();
                                        boolean backValue = VideoSDK.vPaasSDK_BackStreamControl(1,dateUtil.timeToInt(tempTime),camId,0);// 跳转
                                        nowTime = tempTime;
                                        Log.v(LOG,"------>跳转："+backValue);
                                    }
                                }.start();
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveX = event.getX()-startX;
                    if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
                        if(Math.abs(moveX)>20){
                            if(videoState == VIDEO_STATE_PLAY && videoIsHaveTime){ // 只有正在播放时，视频源有返回时间时，才可以快进
                                videoPlayFoward.setVisibility(View.VISIBLE);
                                if(moveX > 20){
                                    fowardImage.setImageResource(android.R.drawable.ic_media_ff);
                                    if(nowTime.getTime()+(int)moveX*1000 < endTime.getTime()){ // 快进
                                        tempTime.setTime(video.nowTime.getTime()+(int)moveX*1000);
                                    } else { // 终点
                                        tempTime.setTime(endTime.getTime());
                                    }
                                } else {
                                    fowardImage.setImageResource(android.R.drawable.ic_media_rew);
                                    if(nowTime.getTime()+(int)moveX*1000 > startTime.getTime()){// 后退
                                        tempTime.setTime(video.nowTime.getTime()+(int)moveX*1000);
                                    } else { // 起始点
                                        tempTime.setTime(startTime.getTime());
                                    }
                                }
                                fowardText.setText(dateUtil.getStringDate(tempTime,DateUtil.DATE_FORMAT_HMS)+"/"+dateUtil.getStringDate(endTime,DateUtil.DATE_FORMAT_HMS));
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(LOG, e.toString());
        }
        return true;
    }

    /**
     * Video监听
     * ---------------------------------------------------------------------------------------------
     */
    @Override // 返回视频记录列表
    public void videoPlayRecord(List<VideoPlayRecord> list) {
        data.clear();
        data.addAll(list);
        videoHandler.sendEmptyMessage(VIDEO_STATE_SEARCH);
        // 修改状态
        videoIsPlay = false;
    }

    @Override  // 收到播放流
    public void videoMessageData(int width, int height, byte[] data) {
        try {
            if(videoState == VIDEO_STATE_PLAY){
                mRenderer.updata(width, height, data);
                glSurfaceView.requestRender();
                if(!videoIsPlay){ // 进入播放状态
                    videoIsPlay = true;
                    videoHandler.sendEmptyMessage(VIDEO_STATE_PLAY);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(LOG, e.toString());
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(LOG, e.toString());
        }
    }
    int tempNum = 0;
    long date1 = 0;
    long date2 = 0;
    long date3 = 0;
    @Override // 视频播放时间
    public void videoUpDateTime(Date date) {
        Log.v(LOG,date.toString());
        isBackPlayVideoSuccess = true; // 播放成功
        if (videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
            isWhetherHaveTime = true;
            videoIsHaveTime = false;
            videoState = VIDEO_STATE_PLAY;
        } else if (videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK) { // 回放
            if (isWhetherHaveTime){ // 已判断出是否有时间
                // 处理数据
                if (videoIsHaveTime){ // 有实时时间  判断时间差  收到时间和现在播放时间差小于5分钟，或时间差是24小时 就播放 否则缓冲等待
                    if (Math.abs(nowTime.getTime()-date.getTime())<5*60*1000 || Math.abs(nowTime.getTime()-date.getTime())>23.9*60*60*1000){
                        // 更新时间
                        nowTime = date;
                        if (videoState == VIDEO_STATE_PLAY_WAIT){
                            videoState = VIDEO_STATE_PLAY;
                        }
                        videoHandler.sendEmptyMessage(VIDEO_STATE_UPDATE);
                        // 该时间段播放完，播放下一个时间段
                        if(date.getTime() > endTime.getTime()){
                            for(int i=0; i<data.size(); i++){
                                if("1".equals(data.get(i).getStatus())){
                                    data.get(i).setStatus("0");
                                    if(data.size() > i+1){
                                        data.get(i+1).setStatus("1");
                                        // 修改开始结束时间
                                        startTime = data.get(i+1).getStartTime();
                                        endTime = data.get(i+1).getStopTime();
                                    }
                                    videoHandler.sendEmptyMessage(VIDEO_STATE_UPLIST);
                                    break;
                                }
                            }
                        }
                    }
                } else { // 播放   没有实时时间
                    if (videoState == VIDEO_STATE_PLAY_WAIT){
                        videoState = VIDEO_STATE_PLAY;
                    }
                }
            } else { // 判断是否有实时时间
                if (tempNum == 0){
                    date1 = date.getTime();
                    tempNum++;
                } else if (tempNum == 1){
                    date2 = date.getTime();
                    if (date1 != date2 && Math.abs(date1-date2)<23*60*60*1000){
                        isWhetherHaveTime = true; // 判断完成
                        videoIsHaveTime = true; // 有实时时间
                        // 处理数据
                        Log.v(LOG,"有实时时间");
                        if (Math.abs(nowTime.getTime()-date.getTime())<5*60*1000 || Math.abs(nowTime.getTime()-date.getTime())>23.9*60*60*1000){
                            // 更新时间
                            nowTime = date;
                            if (videoState == VIDEO_STATE_PLAY_WAIT){
                                videoState = VIDEO_STATE_PLAY;
                            }
                            videoHandler.sendEmptyMessage(VIDEO_STATE_UPDATE);
                        }
                    } else {
                        tempNum++;
                    }
                } else if(tempNum == 2){
                    date3 = date.getTime();
                    if (date1 != date3 && Math.abs(date1-date3)<23*60*60*1000){
                        videoIsHaveTime = true; // 有实时时间
                        Log.v(LOG,"有实时时间");
                        if (Math.abs(nowTime.getTime()-date.getTime())<5*60*1000 || Math.abs(nowTime.getTime()-date.getTime())>23.9*60*60*1000){
                            // 更新时间
                            nowTime = date;
                            if (videoState == VIDEO_STATE_PLAY_WAIT){
                                videoState = VIDEO_STATE_PLAY;
                            }
                            videoHandler.sendEmptyMessage(VIDEO_STATE_UPDATE);
                        }
                    } else {
                        videoIsHaveTime = false; // 没有实时时间
                        Log.v(LOG,"没有实时时间");
                        if (videoState == VIDEO_STATE_PLAY_WAIT){
                            videoState = VIDEO_STATE_PLAY;
                        }
                    }
                    isWhetherHaveTime = true; // 判断完成
                }
            }
        }
    }

    @Override
    public void videoErrorListener(int keyError) {
        switch (keyError){
            case VIDEO_ERROR_STATE_PLAY: // 连接超时
                videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_PLAY);
                break;
            case VIDEO_ERROR_STATE_SEARCH1: // 云终端不在线
                videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_SEARCH1);
                break;
            case VIDEO_ERROR_STATE_SEARCH2: // 镜头不在线
                videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_SEARCH2);
                break;
            case VIDEO_ERROR_STATE_SUCCESS: // 大洞成功

                break;
        }
    }

    /**
     * 变量声明
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * 视频播放类型
     */
    public int videoPlayType;
    /**
     * 当前视频状态
     */
    public int videoState;
    /**
     * 视频布局状态
     */
    public int videoLayoutState;
    /**
     * 是否在播放
     */
    public boolean videoIsPlay = false;
    /**
     * 视频是否有实时时间
     */
    public boolean videoIsHaveTime = true;
    /**
     * 是否已判断有时间
     */
    public boolean isWhetherHaveTime = false;
    /**
     * 记录是否回放成功过，成功后下次直接使用跳转命令
     */
    public boolean isBackPlayVideoSuccess = false;
    /**
     * 消息处理
     */
    private Handler videoHandler = null;
    /**
     * 上下文
     */
    private Context context;


    /**
     * 静态变量定义
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * 视频播放类型：
     * 1000：直播
     * 1001: 回放
     */
    public static final int VIDEOLAYOUT_PLAY_TYPE_LIVE = 1000;
    public static final int VIDEOLAYOUT_PLAY_TYPE_PLAYBACK = 1001;

    /**
     * 视频状态：
     * 10000：未初始化
     * 10001：初始化
     * 10002：连接
     * 10003：登录
     * 10004：播放
     * 10005：暂停
     * 10006: 查询视频记录成功
     * 10007: 停止
     * 10008: 播放等待
     */
    public static final int VIDEO_STATE_NOINIT = 10000;
    public static final int VIDEO_STATE_INIT = 10001;
    public static final int VIDEO_STATE_CONNET = 10002;
    public static final int VIDEO_STATE_LOGIN = 10003;
    public static final int VIDEO_STATE_PLAY = 10004;
    public static final int VIDEO_STATE_PAUSE = 10005;
    public static final int VIDEO_STATE_SEARCH = 10006;
    public static final int VIDEO_STATE_STOP = 10007;
    public static final int VIDEO_STATE_PLAY_WAIT = 10008;

    /**
     * 播放错误：
     * 20001：初始化
     * 20002：连接
     * 20003：登录
     * 20004：播放
     * 20005：暂停
     * 20006: 查询命令错误
     * 20007：跳转
     * 20008：查询云终端不在线
     * 20009：查询镜头不在线
     * 20010: 大洞成功
     */
    public static final int VIDEO_ERROR_STATE_INIT = 20001;
    public static final int VIDEO_ERROR_STATE_CONNET = 20002;
    public static final int VIDEO_ERROR_STATE_LOGIN = 20003;
    public static final int VIDEO_ERROR_STATE_PLAY = 20004;
    public static final int VIDEO_ERROR_STATE_PAUSE = 20005;
    public static final int VIDEO_ERROR_STATE_QUERY = 20006;
    public static final int VIDEO_ERROR_STATE_SEEK = 20007;
    public static final int VIDEO_ERROR_STATE_SEARCH1 = 20008;
    public static final int VIDEO_ERROR_STATE_SEARCH2 = 20009;
    public static final int VIDEO_ERROR_STATE_SUCCESS = 20010;

    /**
     * 其他状态
     * 30001：更新时间
     * 30002: 更新视频列表
     */
    public static final int VIDEO_STATE_UPDATE = 30001;
    public static final int VIDEO_STATE_UPLIST = 30002;

    /**
     * 视频布局状态：
     * 2000：布局初始状态
     * 2001：布局上下栏
     * 2002：视频列表布局
     */
    public static final int VIDEOLAYOUT_STATE_INIT = 2000;
    public static final int VIDEOLAYOUT_STATE_HEAD_AND_TAIL = 2001;
    public static final int VIDEOLAYOUT_STATE_VIDEOLIST = 2002;


    /**
     * 布局声明
     * ---------------------------------------------------------------------------------------------
     */
    // 全屏 上边栏 布局
    private LinearLayout fullScreenTopBarLayout;
    // 全屏 下边栏 布局
    private LinearLayout fullScreenBottomBarLayout;
    // 小屏 下边栏 布局
    private LinearLayout smallScreenBottomBarLayout;
    // 全屏 视频列表
    private LinearLayout videoPlayListLayout;

    // ProgressBar控件
    private ProgressBar progressBar;
    // 播放/暂停button
    private ImageView playButtonBig;
    // 快进布局
    private LinearLayout videoPlayFoward;
    // 快进 image
    private ImageView fowardImage;
    // 快进 时间
    private TextView fowardText;

    // 全屏 上边栏 返回button
    private LinearLayout fullScreenBackButton;
    // 全屏 上边栏 日历button
    private LinearLayout fullScreenDateButton;
    // 全屏 上边栏 日历时间
    private TextView dateText = null;
    // 全屏 上边栏 更多button
    private LinearLayout fullScreenMoreButton;

    // 小屏 下边栏 全屏button
    private RelativeLayout smallScreenFullButton;
    private ImageView smaillScreenFullScreenImage;
    // 全屏 下边栏 播放/暂停button
    public CheckBox playButton;
    // 进度条
    private SeekBar seekBar;
    // 播放时间
    private TextView playTime;

    // 视频列表
    private ListView listView;

    // gl控件
    private GLSurfaceView glSurfaceView;
    private SimpleRenderer mRenderer;

    /**
     * 动画声明
     * ---------------------------------------------------------------------------------------------
     */
    // 上边栏 动画
    private Animation animationVideoLayoutTopBarOpen;
    private Animation animationVideoLayoutTopBarClose;
    // 下边栏 动画
    private Animation animationVideoLayoutBottomBarOpen;
    private Animation animationVideoLayoutBottomBarClose;
    // 视频列表 动画
    private Animation animationVideoPlayDataListOpen;
    private Animation animationVideoPlayDataListClose;
    // 播放button 动画
    private Animation animationPlayButtonRestart;


    /**
     * 初始化布局
     * ---------------------------------------------------------------------------------------------
     * @param context
     */
    private void initLayout(Context context){
        /**
         * 添加gl布局
         */
        glSurfaceView = new GLSurfaceView(context);
        LayoutParams glSurfaceViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        glSurfaceViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL, TRUE);
        addView(glSurfaceView,glSurfaceViewParams);

        /**
         * 添加progressBar布局
         */
        progressBar = new ProgressBar(context);
        LayoutParams progressBarParam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressBarParam.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(progressBar,progressBarParam);

        /**
         * 添加播放/暂停button布局
         */
        playButtonBig = new ImageView(context);
        playButtonBig.setImageResource(R.drawable.video_stop_button);
        playButtonBig.setOnClickListener(this);
        LayoutParams videoPlayButtonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoPlayButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        videoPlayButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(playButtonBig, videoPlayButtonParams);

        /**
         * 添加快进布局
         */
        videoPlayFoward = new LinearLayout(context);
        videoPlayFoward.setBackgroundColor(Color.parseColor("#80383838"));
        videoPlayFoward.setPadding(20,20,20,20);
        videoPlayFoward.setGravity(Gravity.CENTER);
        videoPlayFoward.setOrientation(LinearLayout.VERTICAL);
        LayoutParams videoPlayFowardParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoPlayFowardParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        LinearLayout center = new LinearLayout(context);
        center.setId(R.id.VideoLayout_Center_Id);
        LayoutParams centerParams = new LayoutParams(100,100);
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        centerParams.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(center,centerParams);
        videoPlayFowardParams.addRule(RelativeLayout.ABOVE,center.getId());
        addView(videoPlayFoward,videoPlayFowardParams);
        /**
         * 添加快进image
         */
        fowardImage = new ImageView(context);
        fowardImage.setImageResource(android.R.drawable.ic_media_ff);
        LinearLayout.LayoutParams fowardImageParams = new LinearLayout.LayoutParams(100,80);
        videoPlayFoward.addView(fowardImage,fowardImageParams);
        /**
         * 添加快进Text
         */
        fowardText = new TextView(context);
        fowardText.setTextColor(Color.parseColor("#ffffff"));
        fowardText.setTextSize(16);
        LinearLayout.LayoutParams fowardTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoPlayFoward.addView(fowardText,fowardTextParams);

        /**
         * 添加 小屏 下边栏布局
         */
        smallScreenBottomBarLayout = new LinearLayout(context);
        smallScreenBottomBarLayout.setBackgroundColor(Color.parseColor("#80383838"));
        smallScreenBottomBarLayout.setGravity(Gravity.RIGHT);
        LayoutParams smallScreenBottomBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 90);
        smallScreenBottomBarLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        smallScreenBottomBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(smallScreenBottomBarLayout, smallScreenBottomBarLayoutParams);
        /**
         * 添加 小屏 全屏button
         */
        smallScreenFullButton = new RelativeLayout(context);
        smallScreenFullButton.setGravity(Gravity.CENTER);
        smallScreenFullButton.setOnClickListener(this);
        LayoutParams smaillScreenFullButtonParams = new LayoutParams(100, ViewGroup.LayoutParams.MATCH_PARENT);
        smallScreenBottomBarLayout.addView(smallScreenFullButton, smaillScreenFullButtonParams);
        smaillScreenFullScreenImage = new ImageView(context);
        smaillScreenFullScreenImage.setImageResource(R.drawable.video_fullscreen);
        LayoutParams smaillScreenFullImageParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        smallScreenFullButton.addView(smaillScreenFullScreenImage, smaillScreenFullImageParams);

        /**
         * 添加 全屏 下边栏布局
         */
        fullScreenBottomBarLayout = new LinearLayout(context);
        fullScreenBottomBarLayout.setBackgroundColor(Color.parseColor("#80383838"));
        fullScreenBottomBarLayout.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams fullScreenBottomBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        fullScreenBottomBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        fullScreenBottomBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(fullScreenBottomBarLayout, fullScreenBottomBarLayoutParams);

        /**
         * 添加播放/暂停button
         */
        playButton = new CheckBox(context);
        playButton.setChecked(false); // 暂停
        playButton.setButtonDrawable(null);
        playButton.setBackgroundResource(R.drawable.video_play_button);
        playButton.setOnClickListener(this);
        LinearLayout.LayoutParams playButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        playButtonParams.setMargins(20,0,18,0);
        fullScreenBottomBarLayout.addView(playButton,playButtonParams);
        /**
         * 添加进度条
         */
        seekBar = new SeekBar(context);
        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        seekBarParams.weight = 1;
        fullScreenBottomBarLayout.addView(seekBar,seekBarParams);
        seekBar.setOnSeekBarChangeListener(this);
        /**
         * 播放时间
         */
        playTime = new TextView(context);
        playTime.setTextColor(Color.parseColor("#B3ffffff"));
        playTime.setOnClickListener(this);
        LinearLayout.LayoutParams playTimeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        playTimeParams.setMargins(10,0,10,0);
        fullScreenBottomBarLayout.addView(playTime,playTimeParams);

        /**
         * 添加 全屏 上边栏 布局
         */
        fullScreenTopBarLayout =  new LinearLayout(context);
        fullScreenTopBarLayout.setBackgroundColor(Color.parseColor("#80383838"));
        fullScreenTopBarLayout.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams fullScreenTopBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        fullScreenTopBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        fullScreenTopBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(fullScreenTopBarLayout, fullScreenTopBarLayoutParams);
        /**
         * 添加返回button
         */
        fullScreenBackButton = new LinearLayout(context);
        fullScreenBackButton.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fullScreenBackButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fullScreenTopBarLayout.addView(fullScreenBackButton,fullScreenBackButtonParams);
        fullScreenBackButton.setOnClickListener(this);
        ImageView backImage = new ImageView(context);
        backImage.setImageResource(R.drawable.video_play_back_image);
        LinearLayout.LayoutParams backImageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        backImageParams.setMargins(20,0,20,0);
        fullScreenBackButton.addView(backImage,backImageParams);
        TextView backText = new TextView(context);
        backText.setText("历史回放");
        backText.setTextColor(Color.parseColor("#ffffff"));
        fullScreenBackButton.addView(backText);

        /**
         * 添加日历Layout
         */
        LinearLayout dateButtonLayout = new LinearLayout(context);
        dateButtonLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dateButtonLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        dateButtonLayoutParams.weight = 1;
        fullScreenTopBarLayout.addView(dateButtonLayout,dateButtonLayoutParams);
        /**
         * 添加日历
         */
        fullScreenDateButton = new LinearLayout(context);
        fullScreenDateButton.setPadding(40,0,40,0);
        fullScreenDateButton.setGravity(Gravity.CENTER);
        fullScreenDateButton.setBackgroundResource(R.drawable.video_play_date_button);
        fullScreenDateButton.setOnClickListener(this);
        LinearLayout.LayoutParams fullScreenDateButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fullScreenDateButtonParams.setMargins(0,10,0,10);
        dateButtonLayout.addView(fullScreenDateButton,fullScreenDateButtonParams);
        ImageView dateImage = new ImageView(context);
        dateImage.setImageResource(R.drawable.video_play_date);
        fullScreenDateButton.addView(dateImage);
        /**
         * 添加日历text
         */
        dateText = new TextView(context);
        dateText.setTextColor(Color.parseColor("#ffffff"));
        dateText.setTextSize(16);
        LinearLayout.LayoutParams dateTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateTextParams.setMargins(16,0,0,0);
        fullScreenDateButton.addView(dateText,dateTextParams);

        /**
         * 添加更多button
         */
        fullScreenMoreButton = new LinearLayout(context);
        fullScreenMoreButton.setGravity(Gravity.CENTER);
        fullScreenMoreButton.setOnClickListener(this);
        LinearLayout.LayoutParams fullScreenMoreButtonParams = new LinearLayout.LayoutParams(120, ViewGroup.LayoutParams.MATCH_PARENT);
        fullScreenTopBarLayout.addView(fullScreenMoreButton,fullScreenMoreButtonParams);
        ImageView moreImage = new ImageView(context);
        moreImage.setImageResource(R.drawable.video_play_more);
        fullScreenMoreButton.addView(moreImage);

        /**
         * 添加 右边 视频列表 布局
         */
        videoPlayListLayout = new LinearLayout(context);
        videoPlayListLayout.setPadding(10,10,10,10);
        videoPlayListLayout.setBackgroundColor(Color.parseColor("#806a6a70"));
        LayoutParams videoPlayListLayoutParams = new LayoutParams(420, ViewGroup.LayoutParams.MATCH_PARENT);
        videoPlayListLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        videoPlayListLayoutParams.setMargins(0,100,0,100);
        addView(videoPlayListLayout,videoPlayListLayoutParams);

        // 添加视频列表
        listView = new ListView(context);
        listView.setDrawingCacheBackgroundColor(Color.parseColor("#00000000"));
        listView.setDividerHeight(2);
        LinearLayout.LayoutParams listViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoPlayListLayout.addView(listView,listViewParams);

    }

    /**
     * 初始化参数
     */
    private void initParams(Context context) {
        // gl初始化
        initGLSurfaceView(context);

        // 点击播放动画
        animationPlayButtonRestart = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_restart);
        // 下边栏动画
        animationVideoLayoutBottomBarOpen = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_layout_bottombar_open);
        animationVideoLayoutBottomBarClose = (Animation) AnimationUtils.loadAnimation(context,R.anim.animation_video_layout_bottombar_close);
        // 上边栏动画
        animationVideoLayoutTopBarOpen = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_layout_topbar_open);
        animationVideoLayoutTopBarClose = (Animation) AnimationUtils.loadAnimation(context,R.anim.animation_video_layout_topbar_close);
        // 视频列表动画
        animationVideoPlayDataListOpen = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_datalist_open);
        animationVideoPlayDataListClose = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_datalist_close);

        // 小屏 下边栏 布局
        smallScreenBottomBarLayout.setVisibility(View.GONE);
        // 全屏 下边栏 布局
        fullScreenBottomBarLayout.setVisibility(View.GONE);
        // 全屏 上边栏 布局
        fullScreenTopBarLayout.setVisibility(View.GONE);
        // 视频列表
        videoPlayListLayout.setVisibility(View.GONE);

        // 菊花转
        progressBar.setVisibility(View.GONE);
        // 播放/暂停button Big
        playButtonBig.setVisibility(View.GONE);
        // 快进布局
        videoPlayFoward.setVisibility(View.GONE);
        // 快进时间
        fowardText.setText("00:00:00/00:00:00");

        // 播放/暂停 button
        playButton.setChecked(false); // 暂停
        // 播放时间
        playTime.setText("00:00:00/00:00:00");
        // 日历时间
        dateText.setText(new DateUtil().getStringDate(new Date(),"yyyy-MM-dd"));

        // 初始化时间
        mYear = dateUtil.year;
        mMonth = dateUtil.month;
        mDay = dateUtil.day;

        // 当前状态未初始化
        videoState = VIDEO_STATE_NOINIT;

        // 布局状态
        videoLayoutState = VIDEOLAYOUT_STATE_INIT;

        // 初始化消息
        handlerVideoMessage();
    }

    /**
     * gl初始化
     * @param context
     */
    private void initGLSurfaceView(Context context){
        glSurfaceView.setDrawingCacheEnabled(true);
        glSurfaceView.setDrawingCacheEnabled(true);
        glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8); // this line is required for clipping
        mRenderer = new SimpleRenderer(context);
        glSurfaceView.setRenderer(mRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
