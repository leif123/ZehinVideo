package com.zehin.video.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
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

import com.zehin.video.BackPlayVideoActivity;
import com.zehin.video.LiveVideoActivity;
import com.zehin.video.R;
import com.zehin.video.utils.DateUtil;
import com.zehin.videosdk.Video;
import com.zehin.videosdk.VideoSDK;

import java.util.Date;
import java.util.UUID;

import static com.zehin.video.constants.Constants.LOG;

/**
 * Created by wlf on 2017/6/5.
 * 视频控件
 */
public class VideoLayout extends RelativeLayout implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {

    /**
     * 视频播放类型
     */
    public int videoPlayType;
    /**
     * 当前视频状态
     */
    public int videoState;

    // 视频
    private Video video = null;

    // 时间
    private DateUtil dateUtil = new DateUtil();
    private int mYear;
    private int mMonth;
    private int mDay;

    private String centerIP = ""; // 中心IP
    private String stunIP = ""; // 打洞IP
    private int camId = 0; // 镜头ID
    private int streamType = 0; // 码流类型
    private String userName = "";

    // 消息处理
    private Handler videoHandler = null;

    // 上下文
    private Context context;

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

        // 获取布局属性值
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoLayout);
        // 释放
        ta.recycle();

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
        if(v == playButtonBig){ // 重播/恢复播放
            if(videoPlayState == VIDEO_STATE_PAUSE){ // 恢复播放

            } else { // 重播
                if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
                    // 动画
                    animationPlayButtonRestart.setFillAfter(true);
                    animationPlayButtonRestart.setFillBefore(false);
                    playButtonBig.setImageResource(R.drawable.video_start_button);
                    playButtonBig.setVisibility(View.VISIBLE);
                    playButtonBig.startAnimation(animationPlayButtonRestart);
                } else if (videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK) { // 回放
                    animationPlayButtonRestart.setFillAfter(true);
                    animationPlayButtonRestart.setFillBefore(false);
                    playButtonBig.setImageResource(R.drawable.video_start_big_button);
                    playButtonBig.setVisibility(View.VISIBLE);
                    playButtonBig.startAnimation(animationPlayButtonRestart);
                }
                // 加载
                progressBar.setVisibility(View.VISIBLE);
                // 请求播放
                startPlayVideo();
            }
            switch (videoPlayState){
                case VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON:
                    setVideoPlayLoadStateVisibility(VIDEOLAYOUT_CENTER_STATE_SMAILSTARTBUTTON);
                    break;
                case VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON:
                    setVideoPlayLoadStateVisibility(VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON);
                    break;
                default:
                    return;
            }
            if(video.videoIsPlay){
                playButton.setChecked(true);
            } else {
                setVideoPlayLoadStateVisibility(VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR);
                videoPlayState = VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR;
            }
            listener.videoPlayStartClickLinstener();
        } else if(v == playButton){ // 播放/暂停
            Log.v(LOG,"playButton:"+video.videoIsPlay);
            if(video.videoIsPlay){
                playButton.setEnabled(true);//可用
                listener.videoBottomPlayButtonClickLinstener(playButton.isChecked());
                if(playButton.isChecked()){ // 恢复播放
                    playButton.setChecked(true);
                } else { // 暂停播放
                    playButton.setChecked(false);
                }
            } else {
                playButton.setChecked(false);
                playButton.setEnabled(false);//不可用
            }
        } else if(v == smallScreenFullButton){ // 全屏
            listener.videoPlayFullScreenClickLinstener();
        } else if(v == fullScreenBackButton){ // 退出
            listener.videoPlayExitClickLinstener();
        } else if(v == fullScreenDateButton){ // 日历
            listener.videoPlayDateClickLinstener();
        } else if(v == fullScreenMoreButton){ // 更多
            switch (videoLayoutState){
                case VIDEOLAYOUT_STATE_HEAD_AND_TAIL:
                case VIDEOLAYOUT_STATE_VIDEOLIST_HIDE:
                    videoLayoutState = VIDEOLAYOUT_STATE_VIDEOLIST_SHOW;
                    break;
                case VIDEOLAYOUT_STATE_VIDEOLIST_SHOW:
                    videoLayoutState = VIDEOLAYOUT_STATE_VIDEOLIST_HIDE;
                    break;
            }
            setShowVideoLayoutState(videoLayoutState);
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
                        break;
                    case Video.VIDEO_ERROR_STATE_CONNET:
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
                        break;
                    case Video.VIDEO_ERROR_STATE_LOGIN:
                    case Video.VIDEO_ERROR_STATE_PLAY:
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
                        break;
                    case Video.VIDEO_STATE_NOINIT: // 恢复未初始化状态
//                        videoResumeNoInfoState();
                        break;
                    case Video.VIDEO_STATE_PLAY: // 开始播放
//                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
//                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE;
                        break;
                    default:
                        break;
                }
            }
        };
    }

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
                // 显示日历
                new DatePickerDialog(context, setDateListener, mYear, mMonth, mDay).show();
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
                        } else {
                            videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_INIT);
                            break;
                        }
                    case VIDEO_STATE_INIT:
                        if(VideoSDK.vPaasSDK_ConnetStunSer()) {
                            videoState = VIDEO_STATE_CONNET;
                        } else {
                            videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_CONNET);
                            break;
                        }
                    case VIDEO_STATE_CONNET:
                        userName = UUID.randomUUID().toString();
                        if(1000 == VideoSDK.vPaasSDK_Login(userName, "admin", 2, 2)){
                            videoState = VIDEO_STATE_LOGIN;
                        } else {
                            videoHandler.sendEmptyMessage(VIDEO_ERROR_STATE_LOGIN);
                            break;
                        }
                    case VIDEO_STATE_LOGIN:
                        if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
                            if(VideoSDK.vPaasSDK_GetZehinTransferId(camId, streamType, 2, userName, 1, 0, 0)){

                            }
                        } else if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
                            // 查询视频列表
                        }
                        video.playVideo(camId, streamType, userName, 1, 0, 0);
                        break;
                }
            }
        }.start();
    }

    /**
     * 布局控制操作
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * gl布局控制
     */

    /**
     * 更新gl数据
     * @param width 宽
     * @param height 高
     * @param data 数据
     */
    public void upDateRenderer(int width,int height, byte[] data){
        try {
            mRenderer.updata(width, height, data);
            glSurfaceView.requestRender();
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(LOG, e.toString());
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(LOG, e.toString());
        }
    }

    /**
     * content布局控制
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * 播放视频控件的状态
     * @param type
     * VIDEOLAYOUT_CENTER_STATE_HIDE: 全隐藏
     * VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR: ProgressBar 缓冲bar
     * VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON: stopButtonSmail
     * VIDEOLAYOUT_CENTER_STATE_SMAILSTARTBUTTON: startButtonSmail （小屏 播放暂停）
     * VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON: stopButtonFull
     * VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON: startButtonFull （全屏 播放暂停）
     * VIDEOLAYOUT_CENTER_STATE_FOWARDBUTTON: 快进
     */
    public void setVideoPlayLoadStateVisibility(int type){
        progressBar.setVisibility(View.GONE);
        playButtonBig.setVisibility(View.GONE);
        videoPlayFoward.setVisibility(View.GONE);
        switch (type){
            case VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR:
                progressBar.setVisibility(View.VISIBLE);
                Log.v(LOG, "videoPlayButtonSmail1");
                break;
            case VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON:
                animationPlayButtonRestart.setFillBefore(true);
                animationPlayButtonRestart.setFillAfter(false);
                playButtonBig.setImageResource(R.drawable.video_stop_button);
                playButtonBig.setVisibility(View.VISIBLE);
                Log.v(LOG, "videoPlayButtonSmail2");
                break;
            case VIDEOLAYOUT_CENTER_STATE_SMAILSTARTBUTTON:
                animationPlayButtonRestart.setFillAfter(true);
                animationPlayButtonRestart.setFillBefore(false);
                playButtonBig.setImageResource(R.drawable.video_start_button);
                playButtonBig.setVisibility(View.VISIBLE);
                playButtonBig.startAnimation(animationPlayButtonRestart);
                Log.v(LOG, "videoPlayButtonSmail3");
                break;
            case VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON:
                animationPlayButtonRestart.setFillBefore(true);
                animationPlayButtonRestart.setFillAfter(false);
                playButtonBig.setImageResource(R.drawable.video_stop_big_button);
                playButtonBig.setVisibility(View.VISIBLE);
                Log.v(LOG, "videoPlayButtonSmail4");
                break;
            case VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON:
                animationPlayButtonRestart.setFillAfter(true);
                animationPlayButtonRestart.setFillBefore(false);
                playButtonBig.setImageResource(R.drawable.video_start_big_button);
                playButtonBig.setVisibility(View.VISIBLE);
                playButtonBig.startAnimation(animationPlayButtonRestart);
                Log.v(LOG, "videoPlayButtonSmail5");
                break;
            case VIDEOLAYOUT_CENTER_STATE_FOWARDBUTTON:
                videoPlayFoward.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * top布局控制
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * bottom布局控制
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * right布局控制
     * ---------------------------------------------------------------------------------------------
     */

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
        //  重播button
        public void videoPlayStartClickLinstener();
        //  全屏button
        public void videoPlayFullScreenClickLinstener();

        // 全屏 退出button
        public void videoPlayExitClickLinstener();
        // 全屏 日历button
        public void videoPlayDateClickLinstener();

        // 全屏 下边栏playButton
        public void videoBottomPlayButtonClickLinstener(boolean isChecked);

        // 进度条更新
        public void onStopTrackingTouch(SeekBar seekBar);

        // 滑动屏幕
        public void onTouchEventClickLinstener();
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
        listener.onStopTrackingTouch(seekBar);
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
                    videoPlayFoward.setVisibility(View.GONE);
                    if(Math.abs(offsetX)<20){ // 点击
                        switch (videoLayoutState){
                            case VIDEOLAYOUT_STATE_INIT:
                                videoLayoutState = VIDEOLAYOUT_STATE_HEAD_AND_TAIL;
                                break;
                            case VIDEOLAYOUT_STATE_HEAD_AND_TAIL:
                            case VIDEOLAYOUT_STATE_VIDEOLIST_SHOW:
                            case VIDEOLAYOUT_STATE_VIDEOLIST_HIDE:
                                videoLayoutState = VIDEOLAYOUT_STATE_INIT;
                                break;
                        }
                        setShowVideoLayoutState(videoLayoutState);
                    } else { // 快进/后退
                        if(video.videoIsPlay){
                            setVideoPlayLoadStateVisibility(VIDEOLAYOUT_CENTER_STATE_HIDE);
                            videoPlayState = VIDEOLAYOUT_CENTER_STATE_HIDE;
                            playButton.setChecked(true);
                            listener.onTouchEventClickLinstener();
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveX = event.getX()-startX;
                    if(Math.abs(moveX)>20){
                        if(video.videoIsPlay){
                            setVideoPlayLoadStateVisibility(VIDEOLAYOUT_CENTER_STATE_FOWARDBUTTON);
                            videoPlayState = VIDEOLAYOUT_CENTER_STATE_FOWARDBUTTON;
                            if(moveX > 20){
                                fowardImage.setImageResource(android.R.drawable.ic_media_ff);
                                if(video.nowTime.getTime()+(int)moveX*1000 < video.endTime.getTime()){ // 快进
                                    video.tempTime.setTime(video.nowTime.getTime()+(int)moveX*1000);
                                } else { // 终点
                                    video.tempTime.setTime(video.endTime.getTime());
                                }
                            } else {
                                fowardImage.setImageResource(android.R.drawable.ic_media_rew);
                                if(video.nowTime.getTime()+(int)moveX*1000 > video.startTime.getTime()){// 后退
                                    video.tempTime.setTime(video.nowTime.getTime()+(int)moveX*1000);
                                } else { // 起始点
                                    video.tempTime.setTime(video.startTime.getTime());
                                }
                            }
                            playTime.setText(dateUtil.getStringDate(video.tempTime,DateUtil.DATE_FORMAT_HMS)+"/"+dateUtil.getStringDate(video.endTime,DateUtil.DATE_FORMAT_HMS));
                        }
                    }
            }
        } catch (Exception e) {
            Log.e(LOG, e.toString());
        }
        return true;
    }

    /**
     * 设置布局显示的状态
     * @param videoLayoutState 
     * VIDEOLAYOUT_STATE_INIT: 无
     * VIDEOLAYOUT_STATE_HEAD_AND_TAIL: 上下栏
     * VIDEOLAYOUT_STATE_VIDEOLIST: 上下栏，视频列表
     */
    public void setShowVideoLayoutState(int videoLayoutState){
        if (videoPlayType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
            switch (videoLayoutState){
                case VIDEOLAYOUT_STATE_INIT:
                    smallScreenBottomBarLayout.setVisibility(View.GONE);
                    smallScreenBottomBarLayout.startAnimation(animationVideoLayoutBottomBarClose);
                    break;
                case VIDEOLAYOUT_STATE_HEAD_AND_TAIL:
                    smallScreenBottomBarLayout.setVisibility(View.VISIBLE);
                    smallScreenBottomBarLayout.startAnimation(animationVideoLayoutBottomBarOpen);
                    break;
            }
        } else if(videoPlayType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
            switch (videoLayoutState){
                case VIDEOLAYOUT_STATE_INIT:
                    fullScreenBottomBarLayout.setVisibility(View.GONE);
                    fullScreenBottomBarLayout.startAnimation(animationVideoLayoutBottomBarClose);
                    fullScreenTopBarLayout.setVisibility(View.GONE);
                    fullScreenTopBarLayout.startAnimation(animationVideoLayoutTopBarClose);
                    videoPlayListLayout.setVisibility(View.GONE);
                    break;
                case VIDEOLAYOUT_STATE_HEAD_AND_TAIL:
                    fullScreenBottomBarLayout.setVisibility(View.VISIBLE);
                    fullScreenBottomBarLayout.startAnimation(animationVideoLayoutBottomBarOpen);
                    fullScreenTopBarLayout.setVisibility(View.VISIBLE);
                    fullScreenTopBarLayout.startAnimation(animationVideoLayoutTopBarOpen);
                    videoPlayListLayout.setVisibility(View.GONE);
                    break;
                case VIDEOLAYOUT_STATE_VIDEOLIST_SHOW:
                    videoPlayListLayout.setVisibility(View.VISIBLE);
                    videoPlayListLayout.startAnimation(animationVideoPlayDataListOpen);
                    break;
                case VIDEOLAYOUT_STATE_VIDEOLIST_HIDE:
                    videoPlayListLayout.setVisibility(View.GONE);
                    videoPlayListLayout.startAnimation(animationVideoPlayDataListClose);
            }
        }
    }

    /**
     * 设置日历时间
     * @param date 时间：格式yyyy-MM-dd
     */
    public void setVideoLayoutDate(String date){
        dateText.setText(date);
    }

    /**
     * 设置进度条的值
     * @param value 范围0-100
     */
    public void setVideoLayoutSeekBar(int value){
        seekBar.setProgress(value);
    }

    /**
     * 设置下边栏播放时间
     * @param time
     */
    public void setVideoLayoutTime(String time){
        playTime.setText(time);
    }

    /**
     * 获取视频列表布局
     * @return
     */
    public ListView getVideoLayoutListView(){
        return listView;
    }

    /**
     * 视频布局状态：
     * 2000：布局初始状态
     * 2001：布局上下栏
     * 2002：视频列表布局show
     * 2003: 视频列表布局hide
     */
    public static final int VIDEOLAYOUT_STATE_INIT = 2000;
    public static final int VIDEOLAYOUT_STATE_HEAD_AND_TAIL = 2001;
    public static final int VIDEOLAYOUT_STATE_VIDEOLIST_SHOW = 2002;
    public static final int VIDEOLAYOUT_STATE_VIDEOLIST_HIDE = 2003;

    // 视频布局状态
    public static int videoLayoutState = VIDEOLAYOUT_STATE_INIT;

    /**
     * 播放视频控件center布局状态
     * 3000：全隐藏
     * 3001：ProgressBar 缓冲bar
     * 3002: stopButtonSmail （小屏 暂停）
     * 3003: startButtonSmail （小屏 播放）
     * 3004: stopButtonFull （全屏 暂停）
     * 3005: startButtonFull （全屏 播放）
     * 3006: 快进
     */
    public static final int VIDEOLAYOUT_CENTER_STATE_HIDE = 3000;
    public static final int VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR = 3001;
    public static final int VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON = 3002;
    public static final int VIDEOLAYOUT_CENTER_STATE_SMAILSTARTBUTTON = 3003;
    public static final int VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON = 3004;
    public static final int VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON = 3005;
    public static final int VIDEOLAYOUT_CENTER_STATE_FOWARDBUTTON = 3006;
    // 播放视频控件center布局状态
    public static int videoPlayState = VIDEOLAYOUT_CENTER_STATE_HIDE;








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
     */
    public static final int VIDEO_STATE_NOINIT = 10000;
    public static final int VIDEO_STATE_INIT = 10001;
    public static final int VIDEO_STATE_CONNET = 10002;
    public static final int VIDEO_STATE_LOGIN = 10003;
    public static final int VIDEO_STATE_PLAY = 10004;
    public static final int VIDEO_STATE_PAUSE = 10005;
    public static final int VIDEO_STATE_SEARCH = 10006;


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
        ImageView smaillScreenFullScreenImage = new ImageView(context);
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
