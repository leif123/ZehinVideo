package com.zehin.videosdk.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by wlf on 2017/6/11.
 */

public class APPScreen {

    private Context context;
    private DisplayMetrics dm = null;

    public APPScreen(Context context){
        this.context = context;
    }

    /**
     * 屏幕宽
     * @return
     */
    public int getAPPScreenWidth(){
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 屏幕高
     * @return
     */
    public int getAPPScreenHeight(){
        return context.getResources().getDisplayMetrics().heightPixels;
    }

}
