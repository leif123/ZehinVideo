package com.zehin.video.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zehin.video.R;
import com.zehin.video.utils.DateUtil;
import com.zehin.videosdk.Video;

import java.util.Date;

import static com.zehin.video.constants.Constants.LOG;

/**
 * Created by wlf on 2017/6/5.
 * 视频控件
 */
public class VideoLayout extends RelativeLayout implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {

    // 视频
    private Video video = null;
    // 时间
    private DateUtil dateUtil = new DateUtil();

    /**
     * gl布局
     * ---------------------------------------------------------------------------------------------
     */
    // gl控件
    private GLSurfaceView glSurfaceView;
    private SimpleRenderer mRenderer;
    // gl属性
    private LayoutParams glSurfaceViewParams;

    /**
     * content布局
     * ---------------------------------------------------------------------------------------------
     */

    /*
     缓冲
      */
    // ProgressBar控件
    private ProgressBar progressBar;
    // ProgressBar属性
    private LayoutParams progressBarParam;

    /*
     停止按钮，用于重播
     */
    // 播放暂停button控件
    private ImageView videoPlayButton;
    // 播放暂停button属性
    private LayoutParams videoPlayButtonParams;

    // 播放button放大效果
    private Animation animationPlayButtonRestart;

    /*
     快进布局
     */
    private LinearLayout videoPlayFoward;
    private ImageView fowardImage;


    /**
     * top布局
     * ---------------------------------------------------------------------------------------------
     */
    // 全屏上边栏布局
    private LinearLayout fullScreenTopBarLayout;
    // 全屏上边栏布局属性
    private LayoutParams fullScreenTopBarLayoutParams;

    // 全屏上边栏返回button
    private LinearLayout fullScreenBackButton;
    // 全屏上边栏返回button属性
    private LinearLayout.LayoutParams fullScreenBackButtonParams;

    // 全屏上边栏日历button
    private LinearLayout fullScreenDateButton;
    // 全屏上边栏日历button
    private LinearLayout.LayoutParams fullScreenDateButtonParams;
    // 全屏上边栏日历时间
    private TextView dateText = null;

    // 全屏上边栏更多button
    private LinearLayout fullScreenMoreButton;
    // 全屏上边栏更多button属性
    private LinearLayout.LayoutParams fullScreenMoreButtonParams;

    /**
     * bottom布局
     * ---------------------------------------------------------------------------------------------
     */

    /*
    小屏bottom布局
     */
    // 小屏下边栏布局
    private LinearLayout smallScreenBottomBarLayout;
    // 小屏下边栏布局属性
    private LayoutParams smallScreenBottomBarLayoutParams;

    // 小屏全屏时下边栏button布局
    private RelativeLayout smallScreenFullButton;
    private LayoutParams smaillScreenFullButtonParams;
    private ImageView smaillScreenFullScreenImage;
    private LayoutParams smaillScreenFullImageParams;

    /*
    全屏bottomBar布局
     */

    // 全屏下边栏布局
    private LinearLayout fullScreenBottomBarLayout;
    // 全屏下边栏布局属性
    private LayoutParams fullScreenBottomBarLayoutParams;

    // 全屏下边栏播放暂停button
    private CheckBox playButton;
    // 全屏下边栏播放暂停button属性
    private LinearLayout.LayoutParams playButtonParams;

    // 进度条
    private SeekBar seekBar;
    // 进度条属性
    private LinearLayout.LayoutParams seekBarParams;

    // 播放时间
    private TextView playTime;
    // 播放时间属性
    private LinearLayout.LayoutParams playTimeParams;

    // 上边栏布局动画效果
    private Animation animationVideoLayoutTopBarOpen;
    private Animation animationVideoLayoutTopBarClose;

    // 下边栏布局动画效果
    private Animation animationVideoLayoutBottomBarOpen;
    private Animation animationVideoLayoutBottomBarClose;

    /**
     * right布局
     * ---------------------------------------------------------------------------------------------
     */

    // 视频列表布局
    private LinearLayout videoPlayListLayout;
    // 视频列表布局属性
    private LayoutParams videoPlayListLayoutParams;
    // 视频列表缩放效果
    private Animation animationVideoPlayDataListOpen;
    private Animation animationVideoPlayDataListClose;

    // 视频列表
    private ListView listView;
    // 视频列表属性
    private LinearLayout.LayoutParams listViewParams;

    /**
     * 构造方法
     * @param context
     * @param attrs
     */
    public VideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 获取视频单例
        video = Video.getInstance();


        // 获取布局属性值
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoLayout);
        // 释放
        ta.recycle();

        /*
        --------------------------------------------------------------------------------------------
         */
        /**
         * 添加gl布局
         */
        glSurfaceView = new GLSurfaceView(context);
        glSurfaceViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        glSurfaceViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL, TRUE);
        addView(glSurfaceView,glSurfaceViewParams);
        // gl初始化
        initGLSurfaceView(context);

        /**
         * 添加content布局
         */
        // 添加progressBar布局
        progressBar = new ProgressBar(context);
        progressBar.setVisibility(View.VISIBLE);
        progressBarParam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressBarParam.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(progressBar,progressBarParam);

        // 添加小屏播放暂停button布局
        videoPlayButton = new ImageView(context);
        videoPlayButton.setImageResource(R.drawable.video_stop_button);
        videoPlayButton.setOnClickListener(this);
        videoPlayButton.setVisibility(View.GONE);
        videoPlayButtonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoPlayButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        videoPlayButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(videoPlayButton, videoPlayButtonParams);
        animationPlayButtonRestart = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_restart);

        // 添加快进布局
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
        videoPlayFoward.setVisibility(View.GONE);
        // 添加快进image
        fowardImage = new ImageView(context);
        fowardImage.setImageResource(android.R.drawable.ic_media_ff);
        LinearLayout.LayoutParams fowardImageParams = new LinearLayout.LayoutParams(100,80);
        videoPlayFoward.addView(fowardImage,fowardImageParams);
        // 添加快进Text
        TextView fowardText = new TextView(context);
        fowardText.setText("00:00:00/12:00:00");
        fowardText.setTextColor(Color.parseColor("#ffffff"));
        fowardText.setTextSize(16);
        LinearLayout.LayoutParams fowardTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoPlayFoward.addView(fowardText,fowardTextParams);

        /**
         * 添加下边栏布局
         */
        /*
           添加小屏下边栏布局
         */
        smallScreenBottomBarLayout = new LinearLayout(context);
        smallScreenBottomBarLayout.setBackgroundColor(Color.parseColor("#80383838"));
        smallScreenBottomBarLayout.setGravity(Gravity.RIGHT);
        smallScreenBottomBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 90);
        smallScreenBottomBarLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        smallScreenBottomBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(smallScreenBottomBarLayout, smallScreenBottomBarLayoutParams);
        smallScreenFullButton = new RelativeLayout(context);
        smallScreenFullButton.setGravity(Gravity.CENTER);
        smallScreenFullButton.setOnClickListener(this);
        smaillScreenFullButtonParams = new LayoutParams(100, ViewGroup.LayoutParams.MATCH_PARENT);
        smallScreenBottomBarLayout.addView(smallScreenFullButton, smaillScreenFullButtonParams);
        smaillScreenFullScreenImage = new ImageView(context);
        smaillScreenFullScreenImage.setImageResource(R.drawable.video_fullscreen);
        smaillScreenFullImageParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        smallScreenFullButton.addView(smaillScreenFullScreenImage, smaillScreenFullImageParams);
        smallScreenBottomBarLayout.setVisibility(View.GONE);
        animationVideoLayoutBottomBarOpen = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_layout_bottombar_open);
        animationVideoLayoutBottomBarClose = (Animation) AnimationUtils.loadAnimation(context,R.anim.animation_video_layout_bottombar_close);

        /*
           添加全屏下边栏布局
         */
        fullScreenBottomBarLayout = new LinearLayout(context);
        fullScreenBottomBarLayout.setBackgroundColor(Color.parseColor("#80383838"));
        fullScreenBottomBarLayout.setGravity(Gravity.CENTER_VERTICAL);
        fullScreenBottomBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        fullScreenBottomBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        fullScreenBottomBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(fullScreenBottomBarLayout, fullScreenBottomBarLayoutParams);
        fullScreenBottomBarLayout.setVisibility(View.GONE);
        // 添加播放暂停button
        playButton = new CheckBox(context);
        playButton.setChecked(false);
        playButton.setButtonDrawable(null);
        playButton.setBackgroundResource(R.drawable.video_play_button);
        playButton.setOnClickListener(this);
        playButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        playButtonParams.setMargins(20,0,18,0);
        fullScreenBottomBarLayout.addView(playButton,playButtonParams);
        playButton.setOnClickListener(this);
        // 添加进度条
        seekBar = new SeekBar(context);
        seekBarParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        seekBarParams.weight = 1;
        fullScreenBottomBarLayout.addView(seekBar,seekBarParams);
        // 播放时间
        playTime = new TextView(context);
        playTime.setText("00:00:00/00:00:00");
        playTime.setTextColor(Color.parseColor("#B3ffffff"));
        playTime.setOnClickListener(this);
        playTimeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        playTimeParams.setMargins(10,0,10,0);
        fullScreenBottomBarLayout.addView(playTime,playTimeParams);

        /**
         * 添加下边栏布局
         */
        /*
           添加全屏下边栏布局
         */
        fullScreenTopBarLayout =  new LinearLayout(context);
        fullScreenTopBarLayout.setBackgroundColor(Color.parseColor("#80383838"));
        fullScreenTopBarLayout.setGravity(Gravity.CENTER_VERTICAL);
        fullScreenTopBarLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        fullScreenTopBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        fullScreenTopBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(fullScreenTopBarLayout, fullScreenTopBarLayoutParams);
        fullScreenTopBarLayout.setVisibility(View.GONE);
        animationVideoLayoutTopBarOpen = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_layout_topbar_open);
        animationVideoLayoutTopBarClose = (Animation) AnimationUtils.loadAnimation(context,R.anim.animation_video_layout_topbar_close);
        // 添加返回button
        fullScreenBackButton = new LinearLayout(context);
        fullScreenBackButton.setGravity(Gravity.CENTER);
        fullScreenBackButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fullScreenTopBarLayout.addView(fullScreenBackButton,fullScreenBackButtonParams);
        fullScreenBackButton.setOnClickListener(this);
        // 添加返回image
        ImageView backImage = new ImageView(context);
        backImage.setImageResource(R.drawable.video_play_back_image);
        LinearLayout.LayoutParams backImageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        backImageParams.setMargins(20,0,20,0);
        fullScreenBackButton.addView(backImage,backImageParams);
        // 添加返回textView
        TextView backText = new TextView(context);
        backText.setText("历史回放");
        backText.setTextColor(Color.parseColor("#ffffff"));
        fullScreenBackButton.addView(backText);

        // 添加日历Layout
        LinearLayout dateButtonLayout = new LinearLayout(context);
        dateButtonLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dateButtonLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        dateButtonLayoutParams.weight = 1;
        fullScreenTopBarLayout.addView(dateButtonLayout,dateButtonLayoutParams);
        // 添加日历
        fullScreenDateButton = new LinearLayout(context);
        fullScreenDateButton.setPadding(40,0,40,0);
        fullScreenDateButton.setGravity(Gravity.CENTER);
        fullScreenDateButton.setBackgroundResource(R.drawable.video_play_date_button);
        fullScreenDateButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fullScreenDateButtonParams.setMargins(0,10,0,10);
        dateButtonLayout.addView(fullScreenDateButton,fullScreenDateButtonParams);
        fullScreenDateButton.setOnClickListener(this);
        // 添加日历image
        ImageView dateImage = new ImageView(context);
        dateImage.setImageResource(R.drawable.video_play_date);
        fullScreenDateButton.addView(dateImage);
        // 添加日历text
        dateText = new TextView(context);
        dateText.setTextColor(Color.parseColor("#ffffff"));
        dateText.setTextSize(16);
        dateText.setText(new DateUtil().getStringDate(new Date(),"yyyy-MM-dd"));
        LinearLayout.LayoutParams dateTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateTextParams.setMargins(16,0,0,0);
        fullScreenDateButton.addView(dateText,dateTextParams);

        // 添加更多button
        fullScreenMoreButton = new LinearLayout(context);
        fullScreenMoreButton.setGravity(Gravity.CENTER);
        fullScreenMoreButtonParams = new LinearLayout.LayoutParams(120, ViewGroup.LayoutParams.MATCH_PARENT);
        fullScreenTopBarLayout.addView(fullScreenMoreButton,fullScreenMoreButtonParams);
        fullScreenMoreButton.setOnClickListener(this);
        // 添加更多image
        ImageView moreImage = new ImageView(context);
        moreImage.setImageResource(R.drawable.video_play_more);
        fullScreenMoreButton.addView(moreImage);

        /**
         * 添加右边视频列表布局
         */
        videoPlayListLayout = new LinearLayout(context);
        videoPlayListLayout.setPadding(10,10,10,10);
        videoPlayListLayout.setBackgroundColor(Color.parseColor("#806a6a70"));
        videoPlayListLayoutParams = new LayoutParams(420, ViewGroup.LayoutParams.MATCH_PARENT);
        videoPlayListLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        videoPlayListLayoutParams.setMargins(0,100,0,100);
        addView(videoPlayListLayout,videoPlayListLayoutParams);
        videoPlayListLayout.setVisibility(View.GONE);
        animationVideoPlayDataListOpen = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_datalist_open);
        animationVideoPlayDataListClose = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_datalist_close);
        // 添加视频列表
        listView = new ListView(context);
        listView.setDrawingCacheBackgroundColor(Color.parseColor("#00000000"));
        listView.setDividerHeight(2);
        listViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoPlayListLayout.addView(listView,listViewParams);
    }

    /**
     * 初始化布局
     * ---------------------------------------------------------------------------------------------
     */

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
        videoPlayButton.setVisibility(View.GONE);
        videoPlayFoward.setVisibility(View.GONE);
        switch (type){
            case VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR:
                progressBar.setVisibility(View.VISIBLE);
                Log.v(LOG, "videoPlayButtonSmail1");
                break;
            case VIDEOLAYOUT_CENTER_STATE_SMAILSTOPBUTTON:
                animationPlayButtonRestart.setFillBefore(true);
                animationPlayButtonRestart.setFillAfter(false);
                videoPlayButton.setImageResource(R.drawable.video_stop_button);
                videoPlayButton.setVisibility(View.VISIBLE);
                Log.v(LOG, "videoPlayButtonSmail2");
                break;
            case VIDEOLAYOUT_CENTER_STATE_SMAILSTARTBUTTON:
                animationPlayButtonRestart.setFillAfter(true);
                animationPlayButtonRestart.setFillBefore(false);
                videoPlayButton.setImageResource(R.drawable.video_start_button);
                videoPlayButton.setVisibility(View.VISIBLE);
                videoPlayButton.startAnimation(animationPlayButtonRestart);
                Log.v(LOG, "videoPlayButtonSmail3");
                break;
            case VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON:
                animationPlayButtonRestart.setFillBefore(true);
                animationPlayButtonRestart.setFillAfter(false);
                videoPlayButton.setImageResource(R.drawable.video_stop_big_button);
                videoPlayButton.setVisibility(View.VISIBLE);
                Log.v(LOG, "videoPlayButtonSmail4");
                break;
            case VIDEOLAYOUT_CENTER_STATE_FULLSTARTBUTTON:
                animationPlayButtonRestart.setFillAfter(true);
                animationPlayButtonRestart.setFillBefore(false);
                videoPlayButton.setImageResource(R.drawable.video_start_big_button);
                videoPlayButton.setVisibility(View.VISIBLE);
                videoPlayButton.startAnimation(animationPlayButtonRestart);
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
     * 点击事件监听
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    public void onClick(View v) {
        if(v == videoPlayButton){ // 重播
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
            setVideoPlayLoadStateVisibility(VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR);
            videoPlayState = VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR;
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
     * 设置初始化布局类型
     * @param type VIDEOLAYOUT_PLAY_TYPE_LIVE:直播; VIDEOLAYOUT_PLAY_TYPE_PLAYBACK:回放;
     */
    public void setInitVideoLayoutType(int type){
        switch (type){
            case VIDEOLAYOUT_PLAY_TYPE_LIVE:
                // 加载布局
                setVideoPlayLoadStateVisibility(VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR);
                videoPlayState = VIDEOLAYOUT_CENTER_STATE_PROGRESSBAR;
                videoLayoutType = VIDEOLAYOUT_PLAY_TYPE_LIVE;
                videoLayoutState = VIDEOLAYOUT_STATE_INIT;
                setShowVideoLayoutState(videoLayoutState);
                break;
            case VIDEOLAYOUT_PLAY_TYPE_PLAYBACK:
                // 加载布局
                setVideoPlayLoadStateVisibility(VIDEOLAYOUT_CENTER_STATE_HIDE);
                videoPlayState = VIDEOLAYOUT_CENTER_STATE_HIDE;
                videoLayoutType = VIDEOLAYOUT_PLAY_TYPE_PLAYBACK;
                videoLayoutState = VIDEOLAYOUT_STATE_HEAD_AND_TAIL;
                setShowVideoLayoutState(videoLayoutState);
                break;
        }
    }

    /**
     * 设置布局显示的状态
     * @param videoLayoutState 
     * VIDEOLAYOUT_STATE_INIT: 无
     * VIDEOLAYOUT_STATE_HEAD_AND_TAIL: 上下栏
     * VIDEOLAYOUT_STATE_VIDEOLIST: 上下栏，视频列表
     */
    public void setShowVideoLayoutState(int videoLayoutState){
        if (videoLayoutType == VIDEOLAYOUT_PLAY_TYPE_LIVE){ // 直播
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
        } else if(videoLayoutType == VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){ // 回放
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
     * 视频播放类型：
     * 1000：直播
     * 1001: 回放
     */
    public static final int VIDEOLAYOUT_PLAY_TYPE_LIVE = 1000;
    public static final int VIDEOLAYOUT_PLAY_TYPE_PLAYBACK = 1001;

    // 视频播放类型
    public static int videoLayoutType = VIDEOLAYOUT_PLAY_TYPE_LIVE;

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
}
