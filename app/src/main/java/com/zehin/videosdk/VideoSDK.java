package com.zehin.videosdk;

public class VideoSDK {
	
	static {  
		System.loadLibrary("ffmpeg"); 
        System.loadLibrary("videoSDK");  
    } 
	
	/** Native methods, implemented in jni folder */
	
	/**
	 * 测试
	 * @return
	 */
    public static native String Test();
    
    /**
     * 初始化
     * @return
     */
    public static native boolean vPaasSDK_Init();
    
    /**
     * 设置STUN
     * @param stunIP 服务IP地址或域名
     */
    public static native void vPaasSDK_SetStunIP(String stunIP);
    
    /**
     * 设置vPaas中心ip地址或域名
     * @param centerIP  vPaas中心ip地址或域名
     */
    public static native void vPaasSDK_SetCenterIP(String centerIP);

    /**
     * 设置中心信令端口
     * @param iCenterCmdPort 端口号 
     */
    public static native void vPaasSDK_SetCenterCmdPort(int iCenterCmdPort);
    
    /**
     * 设置中心心跳端口
     * @param iCenterHeartPort 端口号 
     */
    public static native void vPaasSDK_SetCenterHeartPort(int iCenterHeartPort);
    
    /**
     * 设置心跳周期 单位毫秒
     * @param imSec  单位毫秒  默认为5000 ,最大不超过20000
     */
    public static native void vPaasSDK_SetHeartTime(int imSec);
    
    /**
     * 设置和获取登录是否自动掉线重连
     * @param bAutoReLogin 1-重连；0-不重连 ;  true-重连；false-不重连
     */
    public static native void vPaasSDK_SetAutoReLogin(int bAutoReLogin);
    
    /**
     * 获取是否重连成功  
     * true-连接成功；false-连接失败
     */
    public static native void vPaasSDK_GetAutoReLogin();
    
    /**
     * 验证登录端是否在线
     */
    public static native boolean vPaasSDK_GetIsOnLine();
    
    /**
     * 设置命令接收的回调函数
     */
    public static native void vPaasSDK_SetCmdCallBack();
    
    /**
     * 连接Stun服务 
     * @return  true-连接Stun服务成功；false-连接Stun服务失败
     */
    public static native boolean vPaasSDK_ConnetStunSer();
    
    /**
     * 登陆服务
     * @param userName  用户名
     * @param passWord  密码
     * @param state  用户状态(0-忙碌，1-隐身，2-在线，3-下线)
     * @param type  云终端类型 (0-pc客户端, 1-手机IOS App客户端, 2-手机Android App客户端, 3-Web客户端, 4-云终端, 5-流媒体, 6-存储)
     * @return  1000-登录成功; 1001-用户不存在; 1002-用户已经存在; 1003-用户密码不正确; 1004-服务器错误
     */
    public static native int vPaasSDK_Login(String userName, String passWord, int state, int type);
    
    /**
     * 登录注销
     * @return 1:注销成功; 0:登录超时; -1：连接中心失败; -2：用户名参数不对; -3：发送函数失败
     */
    public static native int vPaasSDK_Logout();
    
    
/**
 * -------------------------------------------------------------------------------------------------------------
 * Transfer方法
 */
    
    /**
     * 获取一路传输ID
     * @param iCamId  摄像头id 
     * @param iStreamType 码流类型:0-主码流; 1-子码流
     * @param iUserType 用户类型: （0-pc客户端，1-手机IOS App，2-手机android App,3-ocx控件，4-云终端）
     * @param sUser 用户名
     * @param iVideoType  视频播放类型（1-实时流播放  2-回放流播放 3-实时对讲 4-远程虚拟串口）
     * @param iDate  格式：yyMMdd
     * @param iTime  格式：HHmmss
     * @return true-成功（获取云终端SDP成功） false-超时（获取云终端SDP失败）
     */
    public static native boolean vPaasSDK_GetZehinTransferId(int iCamId, int iStreamType, int iUserType, String sUser, int iVideoType, int iDate, int iTime);
    
    /**
     * 停止播放
     * @return
     */
    public static native int vPaasSDK_StopPlay();
    
    /**
     * 查询回放
     * @param iCamId 镜头id
     * @param iDate 日期
     * @param iBeginTime 开始时间
     * @param iEndTime 结束时间
     * @return
     */
    public static native boolean vPaasSDK_BackSearch(int iCamId, int iDate, int iBeginTime, int	iEndTime);
    
    /**
     * 录像回放控制命令
     * @param iCmdType 录像控制命令类型（0-录像暂停/恢复  1-录像Seek  2-待定 3-待定  4-待定）
     * @param iCmdContent 录像控制命令内容。根据CmdType，当CmdType为0:CmdContent代表是否暂停（0-暂停  1-恢复）；
												 //当CmdType为1：CmdContent代表录像跳转seek位置；其他命令根据实际应用进行补充。
     * @param iCamID 镜头ID
     * @param iParam1 录像控制参数1（预留）
     * @return
     */
    public static native boolean vPaasSDK_BackStreamControl(int iCmdType, int iCmdContent,int iCamID, int iParam1);
    
    /**
     * 视频流
     * @param width 宽
     * @param height 高
     * @param data 数据
     */
    public static void vPaasSDK_upData(int width,int height, byte[] data){
        Video.getInstance().videoMessageData(width, height, data);
    }

    /**
     * 当前视频播放时间
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @param second
     */
    public static void vPaasSDK_PlayBackTime(int year, int month, int day, int hour, int minute, int second){
        Video.getInstance().upDateTime(hour,minute,second);
    }

    /**
     * 播放失败返回
     * @param type  0:连接超时 1：云终端不在线  2：镜头不在线  3:大洞成功
     */
    public static void vPaasSDK_PlayFailed(int type){
        Video.getInstance().playVideoFailed(type);
    }
    
    /**
     * 查询
     * @param iCamID id
     * @param iDate 日期
     * @param iResultSize 长度
     * @param iStartTime 开始时间
     * @param iStopTime 结束时间
     */
    public static void vPaasSDK_VideoPlayRecord(int iCamID,int iDate, int iResultSize, int[] iStartTime, int[] iStopTime){
        Video.getInstance().videoPlayRecord(iCamID,iDate,iResultSize,iStartTime,iStopTime);
    }
}
