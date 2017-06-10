package com.zehin.videosdk;

import android.util.Log;

import static com.zehin.video.constants.Constants.LOG;

/**
 * Created by wlf on 2017/6/6.
 */

public class Video {

    // 单例
    private static volatile Video instance;
    private Video() {}
    public static Video getInstance(){
        if (instance == null){
            synchronized (Video.class) {
                if (instance == null) {
                    instance = new Video();
                }
            }
        }
        return instance;
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

    /**
     * 当前状态
     */
    public int videoState = VIDEO_STATE_NOINIT;

    /**
     * 是否在播放
     */
    public boolean videoIsPlay = false;

    /**
     * 初始化
     * @return
     */
    public boolean initVideo(){
        if(VideoSDK.vPaasSDK_Init()){
            listener.initVideo(true);
            return true;
        }
        listener.videoErrorListener(VIDEO_ERROR_STATE_INIT);
        return false;
    }

    /**
     * 配置参数
     */
    public void setVideoParams(String stunIP, String centerIP){
        VideoSDK.vPaasSDK_SetStunIP(stunIP);
        VideoSDK.vPaasSDK_SetCenterIP(centerIP);
        VideoSDK.vPaasSDK_SetCenterCmdPort(10000);
        VideoSDK.vPaasSDK_SetCenterHeartPort(20000);
        VideoSDK.vPaasSDK_SetCmdCallBack();
    }

    /**
     * 建立连接
     * @return
     */
    public boolean connetVideo(){
        if (VideoSDK.vPaasSDK_ConnetStunSer()){
            listener.connetVideo(true);
            return true;
        }
        listener.videoErrorListener(VIDEO_ERROR_STATE_CONNET);
        return false;
    }

    /**
     * 登录
     * @param userName 用户名
     * @return
     */
    public boolean loginVideo(String userName){
        int login = VideoSDK.vPaasSDK_Login(userName, "admin", 2, 2);
        if(login == 1000){
            listener.loginVideo(true);
            return true;
        }
        listener.videoErrorListener(VIDEO_ERROR_STATE_LOGIN);
        return false;
    }

    /**
     * 请求播放
     * @param camId camId
     * @param streamType 码流类型:0-主码流; 1-子码流
     * @param userName 用户名
     * @return
     */
    public boolean playVideo(int camId,int streamType,String userName){
        if(VideoSDK.vPaasSDK_GetZehinTransferId(camId, streamType, 2, userName, 1, 0, 0)){
            return true;
        }
        listener.videoErrorListener(VIDEO_ERROR_STATE_PLAY);
        return false;
    }

    /**
     * 请求播放失败
     */
    public void playVideoFailed(){
        listener.videoErrorListener(VIDEO_ERROR_STATE_PLAY);
    }

    /**
     * 数据流
     * @param width 宽
     * @param height 高
     * @param data 数据
     */
    public void videoMessageData(int width,int height, byte[] data){
        listener.videoMessageData(width,height,data);
    }

    /**
     * 停止播放
     */
    public void stopVideo(){
        if(0 != VideoSDK.vPaasSDK_StopPlay()){
            Log.e(LOG, "stopPlayStream fail");
        }
    }

    /**
     * 退出登录
     */
    public void logoutVideo(){
        // 0:登录超时; -1:连接中心失败; -2:用户名参数不对; -3:发送函数失败
        int logout = VideoSDK.vPaasSDK_Logout();
        Log.v(LOG, "vPaasSDK_Logout:"+logout);
    }

    // 接口回调
    private VideoClickListener listener;
    public void setOnVideoClickListener(VideoClickListener listener){
        this.listener = listener;
    }

}
