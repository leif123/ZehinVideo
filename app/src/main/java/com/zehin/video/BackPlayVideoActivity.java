package com.zehin.video;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.zehin.video.utils.DateUtil;
import com.zehin.video.view.VideoLayout;
import com.zehin.videosdk.ListAdapter_Video;
import com.zehin.videosdk.Video;
import com.zehin.videosdk.VideoClickListener;
import com.zehin.videosdk.VideoPlayRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by wlf on 2017/6/11.
 */

public class BackPlayVideoActivity extends Activity implements VideoClickListener, VideoLayout.VideoLayoutClickListener {

    // 视频控件
    private VideoLayout videoLayout;

    // 视频
    private Video video = null;

    // 消息处理
    private Handler videoHandler = null;

    //
    private String IP = "218.201.111.234";
    private String userName = UUID.randomUUID().toString();
    private int camId = 1062043;

    // 时间
    private DateUtil dateUtil = new DateUtil();
    private int mYear;
    private int mMonth;
    private int mDay;

    // 视频列表
    private List<VideoPlayRecord> data;
    private ListAdapter_Video adapter;
    private ListView videoListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_backplay);
        initVideoLayout();

        // 获取视频单例
        video = Video.getInstance();
        video.setOnVideoClickListener(this);

        // 消息处理
        handlerVideoMessage();
    }

    /**
     * 初始化视频布局
     */
    private void initVideoLayout() {
        // 获取视频布局
        videoLayout = (VideoLayout) findViewById(R.id.video);
        videoLayout.setOnVideoLayoutClickListener(this);
        // 播放类型-回放
        videoLayout.setInitVideoLayoutType(VideoLayout.VIDEOLAYOUT_PLAY_TYPE_PLAYBACK);

        // 获取listView
        data = new ArrayList<VideoPlayRecord>();
        adapter = new ListAdapter_Video(BackPlayVideoActivity.this, data);
        videoListView = videoLayout.getVideoLayoutListView();
        videoListView.setAdapter(adapter);
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
                }
            }
        });
        for(int i = 0; i<2; i++){ // 121012
            VideoPlayRecord record = new VideoPlayRecord();
            record.setCamId("111");
            record.setStartTime(new Date());
            record.setStopTime(new Date());
            if(i == 0){
                record.setStatus("1"); // 选中
            } else {
                record.setStatus("0"); // 未选中
            }
            data.add(record);
        }
        adapter.notifyDataSetChanged(); //刷新布局

        // 初始化时间
        mYear = dateUtil.year;
        mMonth = dateUtil.month;
        mDay = dateUtil.day;
    }

    /**
     * 消息处理
     */
    private void handlerVideoMessage() {
        videoHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case Video.VIDEO_ERROR_STATE_INIT:
                    case Video.VIDEO_ERROR_STATE_CONNET:
                        Toast.makeText(BackPlayVideoActivity.this, "连接服务失败！", Toast.LENGTH_SHORT).show();
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON);
                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON;
                        break;
                    case Video.VIDEO_ERROR_STATE_LOGIN:
                    case Video.VIDEO_ERROR_STATE_PLAY:
                        Toast.makeText(BackPlayVideoActivity.this, "连接超时!", Toast.LENGTH_SHORT).show();
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON);
                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_FULLSTOPBUTTON;
                        break;
                    case Video.VIDEO_STATE_NOINIT: // 恢复未初始化状态
//                        videoResumeNoInfoState();
                        break;
                    case Video.VIDEO_STATE_PLAY: // 开始播放
                        videoLayout.setVideoPlayLoadStateVisibility(VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE);
                        videoLayout.videoPlayState = VideoLayout.VIDEOLAYOUT_CENTER_STATE_HIDE;
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * Video监听
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    public void initVideo(boolean arg0) {

    }

    @Override
    public void connetVideo(boolean arg0) {

    }

    @Override
    public void loginVideo(boolean arg0) {

    }

    @Override
    public void playVideo(boolean arg0) {

    }

    @Override
    public void videoMessageData(int width, int height, byte[] data) {

    }

    @Override
    public void videoErrorListener(int keyError) {

    }

    /**
     * VideoLayout监听
     *----------------------------------------------------------------------------------------------
     */

    @Override
    public void videoPlayStartClickLinstener() {

    }

    @Override
    public void videoPlayFullScreenClickLinstener() {

    }

    @Override
    public void videoPlayExitClickLinstener() {
        finish();
    }

    @Override
    public void videoPlayDateClickLinstener() {
        new DatePickerDialog(BackPlayVideoActivity.this, setDateListener, mYear, mMonth, mDay).show();
    }

    // 日历回调监听
    private DatePickerDialog.OnDateSetListener setDateListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            videoLayout.setVideoLayoutDate(dateUtil.getStringDate(dateUtil.getIntToDate(mYear,mMonth+1,mDay),"yyyy-MM-dd"));
        }
    };
}
