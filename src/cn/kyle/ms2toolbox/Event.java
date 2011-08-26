package cn.kyle.ms2toolbox;

import android.content.Context;

import com.mobclick.android.MobclickAgent;

public enum Event {
	Main("ms2toolbox","主程序"),
	BootAnimation("ms2toolbox","切换启动画面"),
	Font("ms2toolbox","切换字体"),
	FlashLight("ms2toolbox","手电筒"),
	MinFreeMem("ms2toolbox","最小内存管理"),
	OverClock("ms2toolbox","超频工具箱"),
	LockScreen("ms2toolbox","锁屏"),
	WifiAutoClose("ms2toolbox","WiFi自动管理"),
	CallOnVibrate("ms2toolbox","接通震动"),
	CallOffVibrate("ms2toolbox","挂断震动"),
	CallOn45SecVibrate("ms2toolbox","45秒震动"),
	CheckUpdate("ms2toolbox","检测版本"),
	FeedBack("ms2toolbox","反馈意见"),
	DefyMore("ms2toolbox","多点触控"),
	QuickNavPanel("ms2toolbox","快速导航面板");
	
	public String event_id;
	public String event_label;
	Event(String id,String label){
		this.event_id = id;
		this.event_label = label;
	}
	
	public static void count(Context context,Event event){
		MobclickAgent.onEvent(context, event.event_id, event.event_label); 
	}
}
