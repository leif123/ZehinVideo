package com.zehin.videosdk.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.zehin.videosdk.constants.VideoConstants.LOG;

/**
 * Created by wlf on 2017/6/11.
 */

public class DateUtil {

    public int year;
    public int month; // 范围0-11月份
    public int day;

    private Date date;

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat sdf = null;

    public static final String DATE_FORMAT_YMD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_YMD1 = "yyyyMMdd";
    public static final String DATE_FORMAT_HMS = "HH:mm:ss";
    public static final String DATE_FORMAT_HMS1 = "HHmmss";
    public static final String DATE_FORMAT_YMDHMS = "yyyy-MM-dd HH:mm:ss";


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
            Log.v(LOG,"---------->时间："+year+":"+month+":"+day);
            date = new SimpleDateFormat(DATE_FORMAT_YMD).parse(year+"-"+month+"-"+day);
        }
        catch(Exception e){
            Log.e(LOG,e.toString());
        }
        return date;
    }

    /**
     * int转Date
     * @param data
     * @param time
     * @return
     */
    public Date intToDate(int data,int time){ //date = 20170526
        Date tDate = null;
        String tData = data+"";
        int tSeconds = time%100;
        int tMinutes = (time/100)%100;
        int tHours = (time/10000)%100;
        if(tData.length() == 8){
            try {
                sdf = new SimpleDateFormat(DATE_FORMAT_YMDHMS);
                tDate = sdf.parse(tData.substring(0,4)+"-"+tData.substring(4,6)+"-"+tData.substring(6,8)+" "+tHours+":"+tMinutes+":"+tSeconds);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tDate;
    }

    /**
     * 时间转int
     * @param date
     * @return
     */
    public int dateToInt(Date date){
        sdf = new SimpleDateFormat(DATE_FORMAT_YMD1);
        calendar.setTime(date);
        return Integer.valueOf(sdf.format(calendar.getTime())).intValue();
    }

    /**
     * time转int
     * @param date
     * @return
     */
    public int timeToInt(Date date){
        sdf = new SimpleDateFormat(DATE_FORMAT_HMS1);
        calendar.setTime(date);
        return Integer.valueOf(sdf.format(calendar.getTime())).intValue();
    }

    /**
     * int转Date
     * @param d
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public Date intToDate(Date d, int hour, int minute, int second){
        Date date = null;
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = sdf.parse(getStringDate(d,DATE_FORMAT_YMD)+" "+hour+":"+minute+":"+second);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }
}
