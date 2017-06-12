package com.zehin.videosdk;

import java.util.Date;
import java.util.List;

/**
 * Created by wlf on 2017/6/6.
 */

public interface VideoClickListener {

    /**
     * 初始化
     * @param arg0 true:成功
     */
    public void initVideo(boolean arg0);

    /**
     * 连接
     * @param arg0 true:成功
     */
    public void connetVideo(boolean arg0);

    /**
     * 登陆
     * @param arg0 true:成功
     */
    public void loginVideo(boolean arg0);

    /**
     * 播放
     * @param arg0 true:成功
     */
    public void playVideo(boolean arg0);

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
     * @param keyError 1:初始化 2:连接 3:登陆 4:播放
     */
    public void videoErrorListener(int keyError);
}
