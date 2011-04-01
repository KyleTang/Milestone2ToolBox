package cn.kyle.ms2toolbox;

import android.content.Context;

import com.mobclick.android.MobclickAgent;

public enum Event {
	Main("0","主程序"),
	BootAnimation("1","切换启动画面"),
	Font("2","切换字体"),
	FlashLight("3","手电筒"),
	MinFreeMem("4","最小内存管理"),
	LockScreen("5","锁屏"),
	WifiAutoClose("6","WiFi自动管理"),
	CallOnVibrate("7","接通震动"),
	CallOffVibrate("8","挂断震动"),
	CallOn45SecVibrate("9","45秒震动");
	
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
