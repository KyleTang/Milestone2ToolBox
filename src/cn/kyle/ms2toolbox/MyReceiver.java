package cn.kyle.ms2toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

	/**
	 * 通话接通震动
	 * @param context
	 * @param intent
	 */
	private void callOnVibrate(final Context context, Intent intent) {
		File f = getPrefFlagFile(context,Pref.pCallOnVibrate);
		if (!f.exists())
			return ;
		final int time = Integer.parseInt(Module.getPrefFlagValue(getPrefFlagFile(context,Pref.pCallOnVibrateTime), "0"));
		final Vibrator vibrator = (Vibrator)context.getSystemService("vibrator");
		new Thread(){
			public void run(){
				Process process;
				try {
					process = Runtime.getRuntime().exec("logcat -b radio ");
					final InputStream localErrorStream = process.getErrorStream();
					new Thread(){
						public void run(){
							BufferedReader br = new BufferedReader(new InputStreamReader(localErrorStream));
							String line = null;
							try {
								while((line=br.readLine())!=null){
									//L.debug("Error: "+line);
								}
								br.close();
								localErrorStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}.start();
					InputStream localInputStream = process.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(localInputStream));
					String line = null;
					boolean bCalling = false;
					while((line=br.readLine())!=null){
						//L.debug("line= "+line);
						if ((!line.contains("GET_CURRENT_CALLS")) || (!line.contains("ACTIVE")))
					          continue;
						vibrator.vibrate(time);
						//if (line.indexOf("GET_CURRENT_CALLS")<0) continue;
						//L.debug(">> "+line);
//						if (!bCalling){
//							if (line.indexOf("ALERTING")>0){
//								bCalling = true;
//							}
//						}else{
//							if (line.indexOf("ACTIVE")>0||
//								line.indexOf("onDisconnect")>0 ){
//								//ACTIVE接通,
//								//onDisconnect断开（
//								//cause=LOCAL主动挂断
//								//cause=BUSY被挂断，
//								//cause=ERROR_UNSPECIFIED无人接听
//								//cause=NORMAL通话结束）
//								//震动并退出循环
//						        vibrator.vibrate(time);
//								//break;
//							}
//						}
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
		    	int time = Integer.parseInt(Module.getPrefFlagValue(getPrefFlagFile(context,Pref.pCallOffVibrateTime), "0"));
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