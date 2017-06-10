package com.zehin.video.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.zehin.video.R;

/**
 * Created by wlf on 2017/6/5.
 * 视频控件
 */
public class VideoLayout extends RelativeLayout{

    // gl布局
    private GLSurfaceView glSurfaceView; // gl
    private SimpleRenderer mRenderer;
    // gl属性
    private LayoutParams glSurfaceViewParams;

    // 缓冲进度条
    // ProgressBar布局
    private ProgressBar progressBar;
    // ProgressBar属性
    private LayoutParams progressBarParam;

    // Video listener
    private VideoClickListener listener;

    // Video listener 接口
    public interface VideoClickListener{
        public void backClick();
    }

    // 设置Video listener
    public void setOnVideoClickListener(VideoClickListener listener){
        this.listener = listener;
    }

    // 构造方法
    public VideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 获取布局属性值
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoLayout);
        // 释放
        ta.recycle();

        // 添加gl布局
        glSurfaceView = new GLSurfaceView(context);
        glSurfaceViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        glSurfaceViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL, TRUE);
        addView(glSurfaceView,glSurfaceViewParams);
        // gl初始化
        initGLSurfaceView(context);

        // 添加progressBar布局
        progressBar = new ProgressBar(context);
        progressBarParam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBarParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressBarParam.addRule(RelativeLayout.CENTER_VERTICAL);
        addView(progressBar,progressBarParam);
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
            System.out.println("--------->"+e);
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("--------->"+e);
        }
    }

    public void setProgressBarVisibility(int v){
        progressBar.setVisibility(v);
    }
}
