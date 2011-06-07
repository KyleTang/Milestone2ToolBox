package cn.kyle.ms2toolbox;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cn.kyle.util.C;
import cn.kyle.util.L;
import cn.kyle.util.PropFile;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.mobclick.android.MobclickAgent;
import com.mobclick.android.UmengUpdateListener;
import com.mobclick.android.UpdateStatus;

public class SettingsPreferenceActivity extends PreferenceActivity {
	ProgressDialog pBar = null;
	Toast myToast = null;
	boolean haveRoot = false;

//	public String update_txt_url = "http://124.207.182.168:9080/downapk/ms2toolbox.txt";
//	public String update_xml_url = "http://124.207.182.168:9080/downapk/ms2toolbox.xml";
//	public String update_url = "http://124.207.182.168:9080/downapk/ms2toolbox.apk";
//	public String update_apk = "/mnt/sdcard/tmp_ms2toolbox.apk";

//	public String bpsw_url = "http://124.207.182.168:9080/downapk/BPSW-Switcher.apk";
	public String bpsw_url = "http://bbs.gfan.com/android-663382-1-1.html";
	public String bpsw_apk = "/mnt/sdcard/BPSW-Switcher.apk";
	
//	public String sd2rom_url = "http://124.207.182.168:9080/down/sd2rom-hack.apk";
	public String sd2rom_url = "http://bbs.gfan.com/android-460981-1-1.html";
	public String sd2rom_apk = "/mnt/sdcard/tmp_sd2rom-hack.apk";
	
	
	
	public Handler handler = new Handler();
	
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this); 
	}
	
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		Event.count(this, Event.Main);
		haveRoot = C.haveRoot();
		if (!haveRoot){
			AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle("提示")
				.setMessage("没有ROOT权限，程序即将退出")
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						System.exit(0);
					}
				}).create();
			ad.show();
			return ;
		}
		
		if (C.runSuCommandReturnBoolean("busybox")){
			L.debug("check: installed busybox");
		}else{
			L.debug("check: not install busybox");
			if ( C.installBusyBox(this)){
				L.debug("auto install busybox finished");
			}else{
				L.debug("auto install busybox fail, exit");
				AlertDialog ad = new AlertDialog.Builder(this)
					.setTitle("提示")
					.setMessage("检测的没有安装busybox，自动安装失败，程序即将退出")
					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}
					}).create();
				ad.show();
				return ;
			}
		}
		
		// 所的的值将会自动保存到SharePreferences
		addPreferencesFromResource(R.xml.preference);
		
//		RingtonePreference pBootupTone = (RingtonePreference)this.getPreferenceScreen().findPreference(Pref.pBootupTone.toString());
//		pBootupTone.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
//			public boolean onPreferenceChange(Preference preference,
//					Object newValue) {
//				Uri uri = (Uri)newValue;
//				L.debug("pBootupTone="+newValue);
//				return true;
//			}
//		});
		
		ListPreference pModel = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pModel.toString());
		pModel.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if ("default".equals(newValue)){
					BufferedReader br;
					try {
						br = new BufferedReader(new FileReader(Module.defaultmodel));
						String line = br.readLine();
						br.close();
						Module.setModel(line);
						myToast("恢复成默认");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else if (Module.setModel((String)newValue)){
					myToast("设置成功");
				}else{
					myToast("设置失败");
				}
				L.debug("pMultiMedia: stagefright= "+newValue);
				return true;
			}
			
		});
		
		ListPreference pMultiMedia = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pMultiMedia.toString());
		pMultiMedia.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean set = false;
				if ("stagefright".equals(newValue)){
					set = true;
				}
				if (Module.setStagefright(set)){
					myToast("设置成功");
				}else{
					myToast("设置失败");
				}
				L.debug("pMultiMedia: stagefright= "+set);
				return true;
			}
			
		});
		
		ListPreference pDalvikVMHeapMax = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pDalvikVMHeapMax.toString());
		pDalvikVMHeapMax.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				int value = Integer.parseInt((String)newValue);
				if (Module.setDalvikVMHeapMax(value)){
					myToast("设置成功");
				}else{
					myToast("设置失败");
				}
				L.debug("pDalvikVMHeapMax: value= "+value);
				return true;
			}
			
		});
		
		ListPreference pLowBatteryOff = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pLowBatteryOff.toString());
		pLowBatteryOff.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				//never, always, headseton
				if ("headseton".equalsIgnoreCase((String) newValue)){
					Module.setLowBatteryOff(false);
					Module.setPrefFlag(true,getPrefFlagFile(Pref.pLowBatteryOff));
				}else {
					if ("always".equalsIgnoreCase((String) newValue)){
						Module.setLowBatteryOff(true);
					}else {
						Module.setLowBatteryOff(false);
					}
					Module.setPrefFlag(false,getPrefFlagFile(Pref.pLowBatteryOff));
				}
				return true;
			}
			
		});
		
		ListPreference pWifiAutoCloseMin = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pWifiAutoCloseMin.toString());
		pWifiAutoCloseMin.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				File flag = getPrefFlagFile(Pref.pWifiAutoCloseMin);
				if (!flag.exists()){
					flag.getParentFile().mkdirs();
					try {
						flag.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return Module.setPrefFlagValue(flag, (String)newValue);
			}
			
		});
		
		ListPreference pCallOnVibrateTime = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pCallOnVibrateTime.toString());
		pCallOnVibrateTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				File flag = getPrefFlagFile(Pref.pCallOnVibrateTime);
				if (!flag.exists()){
					flag.getParentFile().mkdirs();
					try {
						flag.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return Module.setPrefFlagValue(flag, (String)newValue);
			}
			
		});
		
		ListPreference pCallOffVibrateTime = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pCallOffVibrateTime.toString());
		pCallOffVibrateTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				File flag = getPrefFlagFile(Pref.pCallOffVibrateTime);
				if (!flag.exists()){
					flag.getParentFile().mkdirs();
					try {
						flag.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return Module.setPrefFlagValue(flag, (String)newValue);
			}
			
		});
		
		ListPreference pCallOn45SecVibrateTime = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pCallOn45SecVibrateTime.toString());
		pCallOn45SecVibrateTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				File flag = getPrefFlagFile(Pref.pCallOn45SecVibrateTime);
				if (!flag.exists()){
					flag.getParentFile().mkdirs();
					try {
						flag.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return Module.setPrefFlagValue(flag, (String)newValue);
			}
			
		});
//		不好用，废弃
//		SharedPreferences sp = this.getPreferenceManager().getDefaultSharedPreferences(this);
//		sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener(){
//			public void onSharedPreferenceChanged(
//					SharedPreferences sharedPreferences, String key) {
//				L.debug("changeKey= "+key);
//				if (key.equals(Pref.pMultiMedia.toString())){
//					String value = sharedPreferences.getString(key, "stagefright");
//					boolean set = false;
//					if (value.equals("opencore")){
//						set = false;
//					}else if (value.equals("stagefright")){
//						set = true;
//					}
//					if (Module.setStagefright(set)){
//						myToast("设置成功");
//					}else{
//						myToast("设置失败");
//					}	
//				}
//			}
//        	
//        });
		
		//
		refreshItemAll();
	}
	
	public static enum DownFileError{
		None("成功"),
		NotAvailableServer("服务器不可用"),
		NotHttpServer("不是HTTP服务器"),
		InvalidURL("无效的URL地址"),
		DownloadBreak("下载中断，请检查网络"),
		FileNotFound("文件不存在");
		
		final String info;
		DownFileError(String info){
			this.info = info;
		}
		public String toString(){
			return info;
		}
	}
	
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		String key = preference.getKey();
		if (key==null) return false;
		L.debug("key="+key);
		if (key.equals(Pref.pTitle.toString())){
//			L.debug("test-");
//			Vibrator vibrator = (Vibrator)this.getSystemService("vibrator");
//	        vibrator.vibrate(300);
		}
		
		//myToast("clickKey="+key);
		if (key.equals(Pref.pBpsw.toString())){
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setComponent(new ComponentName("cn.kyle.bpswswitcher", "cn.kyle.bpswswitcher.Switcher"));
			try{
				startActivity(i);
			}catch(android.content.ActivityNotFoundException e){
				AlertDialog d = new AlertDialog.Builder(this)
					.setTitle("没有安装，请从论坛下载")
					.setMessage(bpsw_url)
					.setPositiveButton("确定", null)
					.create();
				d.show();
				//installPluginTip(bpsw_url, bpsw_apk);
			}
		}
		
		if (key.equals(Pref.pBootAnimation.toString())){
			Intent i = new Intent(this,ListView.class);
			i.putExtra(ListView.LIST_TYPE, ListView.TYPE_BOOTANIMATIONS);
			startActivity(i);
		}
		
		if (key.equals(Pref.pFont.toString())){
			Intent i = new Intent(this,ListView.class);
			i.putExtra(ListView.LIST_TYPE, ListView.TYPE_FONTS);
			startActivity(i);
		}
		
		if (key.equals(Pref.pBootupTone.toString())){
			
		}
		
		if (key.equals(Pref.pModel.toString())){
			//保存默认值
			if (!new File(Module.defaultmodel).exists()){
				C.runSuCommandReturnBoolean(
						C.CmdMountSystemRW+" ; "+
						"echo "+Build.MODEL+" > "+Module.defaultmodel+" ; ");
			}
		}

		//
		if (key.equals(Pref.pCameraKey.toString())){
			if (Module.setCameraKeyWakeup(((CheckBoxPreference)preference).isChecked())){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}		
		}
		
		if (key.equals(Pref.pVolUpKey.toString())){
			if (Module.setVolupWakeup(((CheckBoxPreference)preference).isChecked())){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}		
		}
		
		if (key.equals(Pref.pVolDownKey.toString())){
			if (Module.setVoldownWakeup(((CheckBoxPreference)preference).isChecked())){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}		
		}
		
		if (key.equals(Pref.pVoiceKey.toString())){
			if (Module.setVoiceZh2En(((CheckBoxPreference)preference).isChecked())){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}
		}
		
		if (key.equals(Pref.pDebounce.toString())){
			boolean checked = ((CheckBoxPreference)preference).isChecked();
			if (Module.setDebounce(checked,this)){
				myToast("设置成功");
				Module.setPrefFlag(true, this.getPrefFlagFile(Pref.pDebounce));
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				Module.setPrefFlag(false, this.getPrefFlagFile(Pref.pDebounce));
				myToast("设置失败");
			}
			
		}
		
		if (key.equals(Pref.pButtonBacklight.toString())){
			if (Module.setButtonBacklightClosed(((CheckBoxPreference)preference).isChecked())
					&& Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pButtonBacklight))){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}
		}
		
		if (key.equals(Pref.pKeyboardBacklight.toString())){
			if (Module.setKeyboardBacklightClosed(((CheckBoxPreference)preference).isChecked())
					&& Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pKeyboardBacklight))){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}
		}
		
		if (key.equals(Pref.pCameraClick.toString())){
			if (Module.setCameraClickDisable(((CheckBoxPreference)preference).isChecked())){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(true);
				myToast("设置失败, 声音文件已经不存在！");
			}
		}
		
		if (key.equals(Pref.pVideoRecord.toString())){
			if (Module.setVideoRecordDisable(((CheckBoxPreference)preference).isChecked())){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(true);
				myToast("设置失败, 声音文件已经不存在！");
			}
		}
		
		//blur icon order
		if (key.equals(Pref.pFixBlurHomeIconOrder.toString())){
			boolean set = ((CheckBoxPreference)preference).isChecked();
			Module.setFixBlurIconOrder(this, set);
			boolean result = Module.getFixBlurIconOrderEnable();
			if (set==result){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败！");
			}
		}
		//gms
		if (key.equals(Pref.pFixGms.toString())){
			boolean set = ((CheckBoxPreference)preference).isChecked();
			Module.setFixGms(set);
			boolean result = Module.getFixGmsEnable();
			if (set==result){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败！");
			}
		}
		//main log
		if (key.equals(Pref.pFixImoseyonLog.toString())){
			boolean set = ((CheckBoxPreference)preference).isChecked();
			Module.setFixImoseyonLog(set);
			boolean result = Module.getFixImoseyonLogEnable();
			if (set==result){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败！");
			}
		}
		
		if (key.equals(Pref.pCallOnVibrate.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.CallOnVibrate);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pCallOnVibrate))){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}
		}
		
		if (key.equals(Pref.pCallOffVibrate.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.CallOffVibrate);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pCallOffVibrate))){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}
		}
		
		if (key.equals(Pref.pCallOn45SecVibrate.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.CallOn45SecVibrate);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pCallOn45SecVibrate))){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}
		}
		
		if (key.equals(Pref.pWifiAutoClose.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.WifiAutoClose);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pWifiAutoClose))){
				myToast("设置成功");
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}
		}
		
		//
		if (key.equals(Pref.pMinFreeMem.toString())){
			Intent i = new Intent(this,MinFreeMem.class);
			startActivity(i);
		}
		
		if (key.equals(Pref.pFlashLight.toString())){
			boolean set = ((CheckBoxPreference)preference).isChecked();
			ListPreference pFlashLightLevel = (ListPreference)this.getPreferenceScreen().
				findPreference(Pref.pFlashLightLevel.toString());
			//int level = pFlashLightLevel.getSharedPreferences().getInt(Pref.pFlashLightLevel.toString(), 80);
			int level = Integer.parseInt(pFlashLightLevel.getSharedPreferences().getString(Pref.pFlashLightLevel.toString(), "80"))	;
			final int time = Integer.parseInt(pFlashLightLevel.getSharedPreferences().getString(Pref.pFlashLightTime.toString(), "120"));
			final Preference pref = preference;
			if (Module.setFlashLight(set?level:0)){
				myToast("设置成功");
				if (set){
					new Thread(){
						public void run(){
							try {
								Thread.sleep(1000*time);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							Module.setFlashLight(0);
							handlerFlashLightClose(pref);
						}
					}.start();
				}
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast("设置失败");
			}
		}
		
		//
		if (key.equals(Pref.pOverClock.toString())){
			Intent i = new Intent(this,OverClock.class);
			startActivity(i);
		}
		
		//
		if (key.equals(Pref.pSd2romHack.toString())){
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.setComponent(new ComponentName("cn.kyle.sd2romhack", "cn.kyle.sd2romhack.Sd2RomHack"));  
			try{
				startActivity(i);
			}catch(android.content.ActivityNotFoundException e){
				//installPluginTip(sd2rom_url, sd2rom_apk);
				AlertDialog d = new AlertDialog.Builder(this)
					.setTitle("没有安装，请从论坛下载")
					.setMessage(sd2rom_url)
					.setPositiveButton("确定", null)
					.create();
				d.show();
			}
		}
		
//		if (key.equals(Pref.pBusyBox.toString())){
//			if (!new File("/system/bin/busybox").exists()){
//				if(C.installBusyBox(this))
//					myToast("BusyBox安装成功");
//				else
//					myToast("BusyBox安装失败");
//			}
//		}
		//
		if (key.equals(Pref.pReboot.toString())){
			AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle("提示")
				.setMessage("是否立即重启")
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						C.runSuCommandReturnBoolean("reboot;");
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).create();
			ad.show();
		}
		if (key.equals(Pref.pRebootToRecovery.toString())){
			AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle("提示")
				.setMessage("是否立即重启到恢复模式")
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						C.runSuCommandReturnBoolean("reboot recovery;");
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						
					}
				}).create();
			ad.show();
		}
		
		if (key.equals(Pref.pCheckUpdate.toString())){
//			MobclickAgent.setUpdateListener(new UmengUpdateListener(){
//				public void onUpdateReturned(int status) {
//					if (status==UpdateStatus.Yes){
//						
//					}
//				}
//			});
			MobclickAgent.update(this);
			
		}
		
		if (key.equals(Pref.pFeedBack.toString())){
			MobclickAgent.openFeedbackActivity(this);
		}
		
		return false;
	}
	
	public void handlerFlashLightClose(final Preference preference){
		handler.post(new Runnable(){
			public void run(){
				((CheckBoxPreference)preference).setChecked(false);
				//myToast("手电筒自动关闭");
			}
		});
		
	}
	
	public void installPluginTip(final String url, final String apk){
		AlertDialog ad = new AlertDialog.Builder(this)
		.setTitle("提示")
		.setMessage("尚未安装该组件，是否下载安装")
		.setPositiveButton("下载安装", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				pBar = new ProgressDialog(SettingsPreferenceActivity.this);
				pBar.setTitle("正在下载");
				pBar.setMessage("请稍候...");
				pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				pBar.show();
				new Thread() {
					public void run() {
						boolean downOK = false;
						DownFileError ret = downFile(url, apk);
						if (ret==DownFileError.None){
							downOK = true;
						}
						//L.debug("下载文件：error="+ret.toString()+", url="+url+", apk="+apk);
						if (downOK){
							handlerDownloadFinished(apk);
						} else {
							handlerDownloadFailed("下载文件失败！");
						}
					}
				}.start();
			}
		}).setNegativeButton("暂不安装", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).create();
		ad.show();
	}

	DownFileError downFile(final String url, final String tmpFile) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		L.debug("downFile - begin URL=" + url);
		long startDown = System.currentTimeMillis();
		HttpResponse response = null;
		DownFileError result = null;
		try {
			response = client.execute(get);
			//client.execute 执行失败会跑异常，不会有空指针，所以response不需要判断
			if (response.getStatusLine()==null||response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){
				if (response.getStatusLine().getStatusCode()!=HttpStatus.SC_NOT_FOUND){
					return DownFileError.FileNotFound;
				}
				return DownFileError.InvalidURL;
			}
			result = DownFileError.None;
			HttpEntity entity = response.getEntity();
			long length = entity.getContentLength();
			InputStream is = entity.getContent();
			FileOutputStream fileOutputStream = null;
			if (is != null) {
				File file = null;
				if (tmpFile.startsWith("/"))
					file = new File(tmpFile);
				else
					file = new File(Environment.getExternalStorageDirectory(),
						tmpFile);
				if (file.exists())
					file.delete();
				fileOutputStream = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				int ch = -1;
				int count = 0;
				while ((ch = is.read(buf)) != -1) {
					fileOutputStream.write(buf, 0, ch);
					count += ch;
					if (length > 0) {
					}
				}
			}
			fileOutputStream.flush();
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
			L.debug("downFile - end , time= "
							+ (System.currentTimeMillis() - startDown)
							+ ", URL=" + url);
			return DownFileError.None;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			L.debug("downFile URL=" + url + ", IOException=" + e.getMessage());
			e.printStackTrace();
			if(result==null)
				return DownFileError.NotAvailableServer; /*连接超时*/
			else
				return DownFileError.DownloadBreak;
		}
		return DownFileError.NotHttpServer;
	}
	
	void installApk(String file) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(file)),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}
	
	void handlerDownloadFinished(final String file) {
		handler.post(new Runnable() {
			public void run() {
				if (pBar != null)
					pBar.cancel();
				installApk(file);
			}
		});
	}

	void handlerDownloadFailed(final String errorInfo) {
		handler.post(new Runnable() {
			public void run() {
				AlertDialog d = new AlertDialog.Builder(SettingsPreferenceActivity.this)
						.setTitle("警告").setMessage(errorInfo)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
															
							}
						})
						.create();
				d.show();
				pBar.dismiss();
			}
		});
	}
	
	public void myToast(String tipInfo) {
		if (myToast == null)
			myToast = new Toast(this);
		myToast.makeText(this, tipInfo, Toast.LENGTH_SHORT).show();
	}
	
	public File getPrefFlagFile(Pref p){
		return new File(this.getFilesDir(),p.toString()+".flag");
	}
	
	public void refreshItemAll(){
		this.setTitle(this.getTitle()+(haveRoot?"(已ROOT)":"(未ROOT)"));
		Preference pTitle = this.findPreference(Pref.pTitle.toString());
		String versionName = null;
		try{
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		}catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		pTitle.setSummary("版本："+versionName+"  作者：Kyle Tang");
		
		//
		PropFile prop = new PropFile();
		prop.load("/system/build.prop");
		String strModel = prop.getValue("ro.product.model");
		String strMultiMedia = prop.getValue("media.stagefright.enable-player");
		String strDalvikVMHeapMax = prop.getValue("dalvik.vm.heapsize").replace("m", "");
		
		ListPreference lp = ((ListPreference)findPreference(Pref.pModel.toString()));
		if (strModel.equalsIgnoreCase("MotoA953")){
			lp.setValue("MotoA953");
		}else if (strModel.equalsIgnoreCase("A953")){
			lp.setValue("A953");
		}else if (strModel.equalsIgnoreCase("ME722")){
			lp.setValue("ME722");
		}
		
		lp = ((ListPreference)findPreference(Pref.pMultiMedia.toString()));
		if (strMultiMedia.equalsIgnoreCase("false")){
			lp.setValue("opencore");
		}else if (strMultiMedia.equalsIgnoreCase("true")){
			lp.setValue("stagefright");
		}
		
		lp = ((ListPreference)findPreference(Pref.pDalvikVMHeapMax.toString()));
		if (strDalvikVMHeapMax.equalsIgnoreCase("30")){
			lp.setValue("30");
		}else if (strDalvikVMHeapMax.equalsIgnoreCase("48")){
			lp.setValue("48");
		}
		
		//设置
		boolean bCameraKeyWakeupEnable = Module.getCameraKeyWakeupEnable();
		((CheckBoxPreference)findPreference(Pref.pCameraKey.toString()))
			.setChecked(bCameraKeyWakeupEnable);
		
		//设置
		boolean bVoiceZh2EnEnable = Module.getVoiceZh2EnEnable();
		((CheckBoxPreference)findPreference(Pref.pVoiceKey.toString()))
			.setChecked(bVoiceZh2EnEnable);
		
		//设置
		((CheckBoxPreference)findPreference(Pref.pDebounce.toString()))
			.setChecked(this.getPrefFlagFile(Pref.pDebounce).exists());
		
		//设置
		((CheckBoxPreference)findPreference(Pref.pButtonBacklight.toString()))
			.setChecked(getPrefFlagFile(Pref.pButtonBacklight).exists());
		
		//设置
		((CheckBoxPreference)findPreference(Pref.pKeyboardBacklight.toString()))
			.setChecked(getPrefFlagFile(Pref.pKeyboardBacklight).exists());
		
		//设置
		boolean CameraClickDisable = Module.getCameraClickDisable();
		L.debug("CameraClickDisable="+CameraClickDisable);
		((CheckBoxPreference)findPreference(Pref.pCameraClick.toString()))
			.setChecked(CameraClickDisable);
		
		//设置
		((CheckBoxPreference)findPreference(Pref.pVideoRecord.toString()))
			.setChecked(Module.getVideoRecordDisable());
		
		//
		CheckBoxPreference cp = ((CheckBoxPreference)findPreference(Pref.pFixBlurHomeIconOrder.toString()));
		cp.setChecked(Module.getFixBlurIconOrderEnable());
		
		//
		cp = ((CheckBoxPreference)findPreference(Pref.pFixGms.toString()));
		cp.setChecked(Module.getFixGmsEnable());
		
		//
		cp = ((CheckBoxPreference)findPreference(Pref.pFixImoseyonLog.toString()));
		if (Module.hasFixImoseyon()){
			cp.setChecked(Module.getFixImoseyonLogEnable());
		}else{
			cp.setEnabled(false);
			cp.setSummary("没有安装imoseyon");
		}
		
		//设置
		((CheckBoxPreference)findPreference(Pref.pCallOnVibrate.toString()))
			.setChecked(getPrefFlagFile(Pref.pCallOnVibrate).exists());
		
		//设置
		((CheckBoxPreference)findPreference(Pref.pCallOffVibrate.toString()))
			.setChecked(getPrefFlagFile(Pref.pCallOffVibrate).exists());
		
		//设置
		((CheckBoxPreference)findPreference(Pref.pCallOn45SecVibrate.toString()))
			.setChecked(getPrefFlagFile(Pref.pCallOn45SecVibrate).exists());
		
		//设置wifi自动关闭状态
		((CheckBoxPreference)findPreference(Pref.pWifiAutoClose.toString()))
			.setChecked(getPrefFlagFile(Pref.pWifiAutoClose).exists());
		
	}

	public void refreshItem(String key){
		
	}
	
	private abstract class Task{
		public String name = null;
		public Task(String name){
			this.name = name;
		}
		public abstract Integer run();
	}
	
	private class MyAsyncTask extends AsyncTask<Void, Void, Integer>{
		Task task = null;
		
		MyAsyncTask(Task task){
			this.task = task;
		}
		
		protected Integer doInBackground(Void... params) {
			return task.run();
		}
		
		protected void onPreExecute() {
			
		}
		
		protected void onPostExecute(Integer a) {
			
		}
	}
}