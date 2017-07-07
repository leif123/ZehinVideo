package com.zehin.videosdk;

import com.zehin.videosdk.entity.VideoPlayRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by wlf on 2017/6/6.
 */

public interface VideoClickListener {

    /**
     * 返回视频记录列表
     * @param list
     */
    public void videoPlayRecord(List<VideoPlayRecord> list);

    /**
     * 数据流
     * @param width 宽
     * @param height 高
     * @param data 数据
     */
    public void videoMessageData(int width,int height, byte[] data);

    /**
     * 视频播放时间
     * @param date
     */
    public void videoUpDateTime(Date date);

    /**
     * 播放错误
     * @param keyError
     */
    public void videoErrorListener(int keyError);
}
