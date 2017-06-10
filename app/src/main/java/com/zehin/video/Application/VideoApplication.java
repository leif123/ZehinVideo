package com.zehin.video.Application;

import android.app.Application;

import com.zehin.video.VideoActivity;

/**
 * Created by wlf on 2017/6/6.
 */

public class VideoApplication extends Application{

    private VideoApplication mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        if(mInstance == null){
            mInstance = this;
        }
    }

    public VideoApplication getApplication() {
        if (mInstance == null) {
            mInstance = new VideoApplication();
        }
        return mInstance;
    }
}
