package com.zehin.videosdk;

import com.zehin.videosdk.utils.DateUtil;
import com.zehin.videosdk.view.VideoLayout;
import com.zehin.videosdk.entity.VideoPlayRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * 时间
     */
    public Date nowTime; // 现在时间

    public void setNowTime(Date nowTime){
        this.nowTime = nowTime;
    }

    /**
     * 播放失败返回
     * @param type 0:连接超时 1：云终端不在线  2：镜头不在线  3:大洞成功
     */
    public void playVideoFailed(int type){
        switch (type){
            case 0:
                listener.videoErrorListener(VideoLayout.VIDEO_ERROR_STATE_PLAY);
                break;
            case 1:
                listener.videoErrorListener(VideoLayout.VIDEO_ERROR_STATE_SEARCH1);
                break;
            case 2:
                listener.videoErrorListener(VideoLayout.VIDEO_ERROR_STATE_SEARCH2);
                break;
            case 3:
                listener.videoErrorListener(VideoLayout.VIDEO_ERROR_STATE_SUCCESS);
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
                record.setStatus("0"); // 未选中
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
        nowTime = dateUtil.intToDate(nowTime,hour,minute,second);
        listener.videoUpDateTime(nowTime);
    }

    // 接口回调
    private VideoClickListener listener;
    public void setOnVideoClickListener(VideoClickListener listener){
        this.listener = listener;
    }

}
