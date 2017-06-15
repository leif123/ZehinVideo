package com.zehin.videosdk;

import android.util.Log;

import com.zehin.video.utils.DateUtil;
import com.zehin.video.view.VideoLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * 当前视频状态
     */
    public int videoState = VIDEO_STATE_NOINIT;

    /**
     * 是否在播放
     */
    public boolean videoIsPlay = false;

    /**
     * 时间
     */
    public Date startTime; // 开始时间
    public Date endTime; // 结束时间
    public Date nowTime; // 现在时间
    public Date tempTime = new Date();

    /**
     * 获取视频列表
     * @param camId camId
     * @param date 日期 格式：20170606（2017年6月6号）
     * @return 请求命令是否成功
     */
    public boolean searchVideoList(int camId, int date) {
        if(VideoSDK.vPaasSDK_BackSearch(camId, date, 000000, 235959)){
            return true;
        }
        listener.videoErrorListener(VIDEO_ERROR_STATE_QUERY);
        return false;
    }

    /**
     * 播放失败返回
     * @param type 0:连接超时 1：云终端不在线  2：镜头不在线
     */
    public void playVideoFailed(int type){
        switch (type){
            case 0:
                listener.videoErrorListener(VIDEO_ERROR_STATE_PLAY);
                break;
            case 1:
                listener.videoErrorListener(VIDEO_ERROR_STATE_SEARCH1);
                break;
            case 2:
                listener.videoErrorListener(VIDEO_ERROR_STATE_SEARCH2);
                break;
        }
    }

    /**
     * 返回视频列表回调
     * @param iCamID
     * @param iDate
     * @param iResultSize
     * @param iStartTime
     * @param iStopTime
     */
    public void videoPlayRecord(int iCamID, int iDate, int iResultSize, int[] iStartTime, int[] iStopTime){
        videoRecordList.clear();
        try {
            for(int i = 0; i<iResultSize; i++){ // 121012
                VideoPlayRecord record = new VideoPlayRecord();
                record.setCamId(iCamID+"");
                record.setStartTime(dateUtil.intToDate(iDate, iStartTime[i]));
                record.setStopTime(dateUtil.intToDate(iDate, iStopTime[i]));
                if(i == 0){
                    record.setStatus("1"); // 选中
                } else {
                    record.setStatus("0"); // 未选中
                }
                videoRecordList.add(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        listener.videoPlayRecord(videoRecordList);
    }
    // 保存视频列表
    private List<VideoPlayRecord> videoRecordList  = new ArrayList<VideoPlayRecord>();
    private DateUtil dateUtil = new DateUtil();

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
     * 视频播放时间
     * @param hour
     * @param minute
     * @param second
     */
    public void upDateTime(int hour, int minute, int second){
//        if(VideoLayout.videoPlayType == VideoLayout.VIDEOLAYOUT_PLAY_TYPE_PLAYBACK){
//            Log.v(LOG, "回放时间");
//            if(Math.abs(nowTime.getTime()-dateUtil.intToDate(nowTime,hour,minute,second).getTime())<10*60*1000){ // 时间差小于10分钟
//                nowTime = dateUtil.intToDate(nowTime,hour,minute,second);
//                listener.videoUpDateTime(nowTime);
//            } else{
//                Log.v(LOG, "时间差不小于10分钟");
//                Log.v(LOG, nowTime.toString());
//                Log.v(LOG, dateUtil.intToDate(nowTime,hour,minute,second).toString());
//
//            }
//        }
    }

    /**
     * 播放视频控制
     * @param type 类型：0-录像暂停/恢复  1-录像Seek
     * @param content 命令内容：当CmdType为0:CmdContent代表是否暂停（0-暂停  1-恢复）；当CmdType为1：CmdContent代表录像跳转seek位置；
     * @param camId 镜头ID
     */
    public boolean videoPlayControl(int type, int content, int camId){
        if(VideoSDK.vPaasSDK_BackStreamControl(type,content,camId,0)){
            Log.v(LOG, "type:"+type+"content:"+content+"camId:"+camId);
            return true;
        }
        if(type == 0){
            listener.videoErrorListener(VIDEO_ERROR_STATE_PAUSE);
        } else if(type == 1){
            listener.videoErrorListener(VIDEO_ERROR_STATE_SEEK);
        }
        return false;
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
