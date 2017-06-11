package com.zehin.video.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.zehin.video.constants.Constants.LOG;

/**
 * Created by wlf on 2017/6/11.
 */

public class DateUtil {

    public int year;
    public int month; // 范围0-11月份
    public int day;

    private Date date;

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public DateUtil(){
        date = new Date();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Date转String
     * @param date 日期
     * @param format 格式
     * @return
     */
    public String getStringDate(Date date, String format){
        try{
            sdf = new SimpleDateFormat(format);
            calendar.setTime(date);
        }
        catch(Exception e){
            Log.e(LOG,e.toString());
        }
        return sdf.format(calendar.getTime());
    }

    /**
     * int转Date 时间
     * @param year
     * @param month
     * @param day
     * @return
     */
    public Date getIntToDate(int year,int month, int day){
        try{
            date = new SimpleDateFormat("yyyy-MM-dd").parse(year+"-"+month+"-"+day);
        }
        catch(Exception e){
            Log.e(LOG,e.toString());
        }
        return date;
    }
}
