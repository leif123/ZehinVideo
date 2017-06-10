package com.zehin.video.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.zehin.video.R;

import static com.zehin.video.constants.Constants.LOG;

/**
 * Created by wlf on 2017/6/5.
 * 视频控件
 */
public class VideoLayout extends RelativeLayout implements View.OnClickListener{

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
     停止按钮，用于小屏重播
     */
    // 小屏播放暂停button控件
    private ImageView videoPlayButtonSmail;
    // 小屏播放暂停button属性
    private LayoutParams videoPlayButtonSmailParam;
    // 播放button放大效果
    private Animation animationPlayButtonRestartSmail;


    /**
     * top布局
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * bottom布局
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * right布局
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * 构造方法
     * @param context
     * @param attrs
     */
    public VideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        progressBarParam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressBarParam.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(progressBar,progressBarParam);

        // 添加小屏播放暂停button布局
        videoPlayButtonSmail = new ImageView(context);
        videoPlayButtonSmail.setImageResource(R.drawable.video_stop_button);
        videoPlayButtonSmail.setOnClickListener(this);
        videoPlayButtonSmailParam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoPlayButtonSmailParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        videoPlayButtonSmailParam.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(videoPlayButtonSmail, videoPlayButtonSmailParam);
        animationPlayButtonRestartSmail = (Animation) AnimationUtils.loadAnimation(context, R.anim.animation_video_restart);
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

    // type: 0:全隐藏  1:ProgressBar 2:stopButtonSmail 3:startButtonSmail
    public void setVideoPlayStateVisibility(int type){
        progressBar.setVisibility(View.GONE);
        videoPlayButtonSmail.setVisibility(View.GONE);
        animationPlayButtonRestartSmail.setFillBefore(true);
        animationPlayButtonRestartSmail.setFillAfter(false);
        switch (type){
            case 1:
                progressBar.setVisibility(View.VISIBLE);
                Log.v(LOG, "videoPlayButtonSmail1");
                break;
            case 2:
                videoPlayButtonSmail.setImageResource(R.drawable.video_stop_button);
                videoPlayButtonSmail.setVisibility(View.VISIBLE);
                Log.v(LOG, "videoPlayButtonSmail2");
                break;
            case 3:
                animationPlayButtonRestartSmail.setFillAfter(true);
                animationPlayButtonRestartSmail.setFillBefore(false);
                videoPlayButtonSmail.setVisibility(View.VISIBLE);
                videoPlayButtonSmail.setImageResource(R.drawable.video_start_button);
                videoPlayButtonSmail.startAnimation(animationPlayButtonRestartSmail);
                Log.v(LOG, "videoPlayButtonSmail3");
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
        public void videoPlayButtonRestartSmailClickLinstener();
    }

    /**
     * 设置Video listener
     * @param listener
     */
    public void setOnVideoLayoutClickListener(VideoLayoutClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if(v == videoPlayButtonSmail){
            listener.videoPlayButtonRestartSmailClickLinstener();
        }
    }
}
