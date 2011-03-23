package cn.kyle.ms2toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import cn.kyle.util.C;
import cn.kyle.util.L;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Vibrator;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver{

	private Toast myToast = null;
	private static String pre_state = "";
	private static String ExtraPhoneState = "state";
	private static String ExtraPhoneNumber = "android.intent.extra.PHONE_NUMBER";
	private static String PhoneStateCalling = "CALLING";
	private static String PhoneStateRinging = "RINGING";
	private static String PhoneStateOffhook = "OFFHOOK";
	private static String PhoneStateIdle = "IDLE";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		L.debug("Receiver Action: "+action);
		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
			//"android.net.wifi.STATE_CHANGE"
			wifiAutoClose(context,intent);
		}else if (Intent.ACTION_BOOT_COMPLETED.equals(action)){
			setBacklight(context,intent);
			setMinFreeMem(context,intent);
		}else if ("android.intent.action.NEW_OUTGOING_CALL".equals(action)){
			L.debug("PHONE_NUMBER:"+intent.getStringExtra(ExtraPhoneNumber));
			pre_state = PhoneStateCalling;
			callOnVibrate(context,intent);
		}else if ("android.intent.action.PHONE_STATE".equals(action)){
			L.debug("PhoneState:"+intent.getStringExtra("state"));
			callOffVibrate(context,intent);
		}else if ("android.intent.action.SEARCH".equals(action)){
			//
		}else if ("android.intent.action.HEADSET_PLUG".equals(action)){
			lowBatteryOff(context,intent);
		}
	}
	
	private void lowBatteryOff(Context context, Intent intent) {
		//仅当设置为头戴耳机插入时生效，才有此标志位
		if (getPrefFlagFile(context,Pref.pLowBatteryOff).exists()){
			//state - 0 for unplugged, 1 for plugged.
			Module.setLowBatteryOff(intent.getIntExtra("state", 1)==1);
		}
	}
	
	private void setMinFreeMem(Context context, Intent intent) {
		if (getPrefFlagFile(context,Pref.pMinFreeMem).exists()){
			String value = "";
			BufferedReader br;
			try {
				String file = context.getFilesDir().getAbsoluteFile()+"/minfree_lastapply";
				L.debug("file="+file);
				br = new BufferedReader(new FileReader(file));
				value = br.readLine();
				br.close();
			} catch (FileNotFoundException e) {
				value = MinFreeMem.defaultValue;
				e.printStackTrace();
			} catch (IOException e) {
				value = MinFreeMem.defaultValue;
				e.printStackTrace();
			}
			C.runSuCommandReturnBoolean("echo "+ value + " > /sys/module/lowmemorykiller/parameters/minfree ; ");
		}
		
	}

	private boolean bExitCallOn = false;
	/**
	 * 通话接通震动
	 * @param context
	 * @param intent
	 */
	private void callOnVibrate(final Context context, Intent intent) {
		File f = getPrefFlagFile(context,Pref.pCallOnVibrate);
		if (!f.exists())
			return ;
		final int callOnVibTime = Integer.parseInt(Module.getPrefFlagValue(getPrefFlagFile(context,Pref.pCallOnVibrateTime), "100"));
		final File fOn45Sec = getPrefFlagFile(context,Pref.pCallOn45SecVibrate);
		final int callOn45SecVibTime = Integer.parseInt(Module.getPrefFlagValue(getPrefFlagFile(context,Pref.pCallOn45SecVibrateTime), "100"));
		final Vibrator vibrator = (Vibrator)context.getSystemService("vibrator");
		final long lCallTimeStart = System.currentTimeMillis();
		final String strCallTimeStart = timeToStr(lCallTimeStart);
		bExitCallOn = false;
		new Thread(){
			public void run(){
				Process process;
				try {
					process = Runtime.getRuntime().exec("logcat -b radio 2>&1");
					InputStream localInputStream = process.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(localInputStream));
					String line = null;
					boolean bCallOn = false;
					while((line=br.readLine())!=null){
						if ( !((line.contains("GET_CURRENT_CALLS")) 
								|| (line.contains("ACTIVE"))
								|| (line.contains("onDisconnect"))
							)){
							continue;
						}
						if (!before(strCallTimeStart,line)) continue;
						L.debug("line="+line);
						long vibTime = callOnVibTime;
						if (line.contains("onDisconnect")){
							if (line.contains("BUSY")||line.contains("ERROR_UNSPECIFIED")){
								//被拒绝，或无人接听
								vibTime = callOnVibTime*2;
								if (vibTime>500){
									vibTime = 500;
								}
								vibrator.vibrate(vibTime);
								L.debug("vibrate=BUSY or ERROR_UNSPECIFIED");
							}else if (line.contains("LOCAL")||line.contains("NORMAL")){
								//主动挂断，或正常结束
								L.debug("vibrate=LOCAL or NORMAL");
							}else {
								//其它情况
								L.debug("vibrate="+line);
							}
							//结束通话
							bExitCallOn = true;
							break;
						}else if (line.contains("GET_CURRENT_CALLS")&&line.contains("ACTIVE")&&!bCallOn){
							bCallOn = true;
							final long timeCountStart = System.currentTimeMillis();
							vibrator.vibrate(vibTime);
							L.debug("vibrate=ACTIVE");
							final long tipVibTime = vibTime;
							if (fOn45Sec.exists()){
								new Thread(){
									public void run(){
										try {
											while(!bExitCallOn){
												Thread.sleep(1000);
												if ((System.currentTimeMillis()-timeCountStart)/1000%60==45){
													vibrator.vibrate(tipVibTime);
													L.debug("vibrate=45s");
												}
											}
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}.start();
							}
						}
						
					}
					process.destroy();
					br.close();
					localInputStream.close();
					// kill `ps | busybox grep app_108 | cut -c10-14`
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private String timeToStr(long time){
		Date d = new Date(time);
		StringBuffer sb = new StringBuffer();
		int m = d.getMonth()+1;
		if (m<10) sb.append("0");
		sb.append(m).append("-");
		int day = d.getDate();
		if (day<10) sb.append("0");
		sb.append(day).append(" ");
		int hh = d.getHours();
		if (hh<10) sb.append("0");
		sb.append(hh).append(":");
		int mm = d.getMinutes();
		if (mm<10) sb.append("0");
		sb.append(mm).append(":");
		int ss = d.getSeconds();
		if (ss<10) sb.append("0");
		sb.append(ss);
		return sb.toString();
	}
	private boolean before( String time , String str ){
		
		L.debug("before:1:"+time);
		L.debug("before:2:"+str.substring(0, 14));
		if (time.compareTo(str.substring(0, 14))<0){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 通话挂断震动
	 * @param context
	 * @param intent
	 */
	private void callOffVibrate(Context context, Intent intent) {
		if (getPrefFlagFile(context,Pref.pCallOffVibrate).exists()){
			String str = intent.getStringExtra(ExtraPhoneState);
			//if ((pre_state.equals(PhoneStateCalling)) && (str.equals(PhoneStateOffhook))){
			if ((pre_state.equals(PhoneStateOffhook)) && (str.equals(PhoneStateIdle))){
		    	int time = Integer.parseInt(Module.getPrefFlagValue(getPrefFlagFile(context,Pref.pCallOffVibrateTime), "300"));
		        Vibrator vibrator = (Vibrator)context.getSystemService("vibrator");
		        vibrator.vibrate(time);
		    }
		    pre_state = str;
		}
	}
	
	private void setBacklight(Context context, Intent intent){
		if (getPrefFlagFile(context,Pref.pButtonBacklight).exists()){
			Module.setButtonBacklightClosed(true);
		}
		if (getPrefFlagFile(context,Pref.pKeyboardBacklight).exists()){
			Module.setKeyboardBacklightClosed(true);			
		}
	}
	
	private void wifiAutoClose(Context context, Intent intent){
		if (!getPrefFlagFile(context,Pref.pWifiAutoClose).exists()){
			return ;
		}
		final WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		final File flagSingle = new File(context.getFilesDir(),"wifi.flag");
		if (wm.getConnectionInfo().getSupplicantState() != SupplicantState.COMPLETED){
			L.debug("Receiver wifi: not connected");
			if (!wm.isWifiEnabled()) {
				L.debug("wifi is closed !");
				return ;
			}
			
			if (flagSingle.exists())
				return ;
			try {
				flagSingle.getParentFile().mkdirs();
				flagSingle.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			final File min = getPrefFlagFile(context,Pref.pWifiAutoCloseMin);
			new Thread(){
				public void run(){
					int n = Integer.parseInt(Module.getPrefFlagValue(min, "3"));; /*分钟*/
					for (int i=0;i<12*n;i++){
						try {
							Thread.sleep(5*1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						L.debug("Receiver: wifi - check state ?  "+wm.getConnectionInfo().getSupplicantState()+
								" "+((i+1)*5)+"/"+(n*60));
						if (wm.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED ){
							flagSingle.delete();
							return;
						}
					}
					wm.setWifiEnabled(false);
					flagSingle.delete();
				}
			}.start();
		}else{
			L.debug("Receiver wifi: connected");
			flagSingle.delete();
		}
	}
	
	private File getPrefFlagFile(Context c ,Pref p){
		return new File(c.getFilesDir(),p.toString()+".flag");
	}
	
}