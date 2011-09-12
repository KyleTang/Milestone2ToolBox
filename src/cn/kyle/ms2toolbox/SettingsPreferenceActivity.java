package cn.kyle.ms2toolbox;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cn.kyle.util.C;
import cn.kyle.util.L;
import cn.kyle.util.MultiLang;
import cn.kyle.util.PropFile;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.preference.Preference.OnPreferenceClickListener;
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
	
	private MultiLang ml = null;
	
	public Handler handler = new Handler();
	
	protected void onResume() {
		super.onResume();
		//设置键盘防抖动
		this.setDebouncePreference();
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
		ml = new MultiLang(this);
		haveRoot = C.haveRoot();
		if (!haveRoot){
			AlertDialog ad = new AlertDialog.Builder(this)
				.setTitle(R.string.dialog_title_tip)
				.setMessage(R.string.msg_noRoot)
				.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
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
					.setTitle(R.string.dialog_title_tip)
					.setMessage(R.string.msg_noBusybox)
					.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							System.exit(0);
						}
					}).create();
				ad.show();
				return ;
			}
		}
		
		//设置主界面的菜单语言
		if (this.getResources().getConfiguration().locale.getLanguage().equals(Locale.CHINESE.getLanguage())){
			// 所的的值将会自动保存到SharePreferences
			addPreferencesFromResource(R.xml.preference_chs);
		}else{
			// 所的的值将会自动保存到SharePreferences
			addPreferencesFromResource(R.xml.preference);
		}
		
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
						myToast(R.string.msg_restoreToDefault);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else if (Module.setModel((String)newValue)){
					myToast(R.string.msg_successfullyApplied);
				}else{
					myToast(R.string.msg_failedToApply);
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
					myToast(R.string.msg_successfullyApplied);
				}else{
					myToast(R.string.msg_failedToApply);
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
					myToast(R.string.msg_successfullyApplied);
				}else{
					myToast(R.string.msg_failedToApply);
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
		
		ListPreference pWifiScanInterval = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pWifiScanInterval.toString());
		pWifiScanInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				int value = Integer.parseInt((String)newValue);
				if (Module.setWifiScanInterval(value)){
					myToast(ml.t(R.string.msg_wifiScanInterval_applyAndNewValue, new String[]{(String)newValue}));
				}else{
					myToast(R.string.msg_failedToApply);
				}
				L.debug("wifi.supplicant_scan_interval: value= "+value);
				return true;
			}
			
		});
		
		ListPreference pDefyMoreNum = (ListPreference)this.getPreferenceScreen().findPreference(Pref.pDefyMoreNum.toString());
		pDefyMoreNum.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				File flag = getPrefFlagFile(Pref.pDefyMoreNum);
				if (!flag.exists()){
					flag.getParentFile().mkdirs();
					try {
						flag.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				boolean ret = Module.setPrefFlagValue(flag, (String)newValue);
				if (ret){
					if ("2".equalsIgnoreCase((String)newValue)){
						ret = Module.setDefyMore(false,null, 0);
						myToast(R.string.msg_multiTouch_restore);
					}else{
						ret = Module.setDefyMore(true, SettingsPreferenceActivity.this, Integer.parseInt((String)newValue));
						if(ret){
							myToast(ml.t(R.string.msg_multiTouch_success, new String[]{(String)newValue}));
						}else{
							myToast(ml.t(R.string.msg_multiTouch_fail, new String[]{(String)newValue}));
						}
					}
				}
				return ret ;
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
		
		refreshItemAll();
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
				installApk(Module.getBpswApkFile(this).getAbsolutePath());
//				AlertDialog d = new AlertDialog.Builder(this)
//					.setTitle(R.string.msg_notInstallPlugin)
//					.setMessage(bpsw_url)
//					.setPositiveButton(R.string.btn_confirm, null)
//					.create();
//				d.show();
//				//installPluginTip(bpsw_url, bpsw_apk);
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
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}		
		}
		
		if (key.equals(Pref.pVolUpKey.toString())){
			if (Module.setVolupWakeup(((CheckBoxPreference)preference).isChecked())){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}		
		}
		
		if (key.equals(Pref.pVolDownKey.toString())){
			if (Module.setVoldownWakeup(((CheckBoxPreference)preference).isChecked())){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}		
		}
		
		if (key.equals(Pref.pVoiceKey.toString())){
			if (Module.setVoiceZh2En(((CheckBoxPreference)preference).isChecked())){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		
		if (key.equals(Pref.pDefyMore.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.DefyMore);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pDefyMore))){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		
		if (key.equals(Pref.pButtonBacklight.toString())){
			if (Module.setButtonBacklightClosed(((CheckBoxPreference)preference).isChecked())
					&& Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pButtonBacklight))){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		
		if (key.equals(Pref.pKeyboardBacklight.toString())){
			if (Module.setKeyboardBacklightClosed(((CheckBoxPreference)preference).isChecked())
					&& Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pKeyboardBacklight))){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		
		if (key.equals(Pref.pCameraClick.toString())){
			if (Module.setCameraClickDisable(((CheckBoxPreference)preference).isChecked())){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(true);
				myToast(R.string.msg_soundDisable_fail);
			}
		}
		
		if (key.equals(Pref.pVideoRecord.toString())){
			if (Module.setVideoRecordDisable(((CheckBoxPreference)preference).isChecked())){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(true);
				myToast(R.string.msg_soundDisable_fail);
			}
		}
		
		//blur icon order
		if (key.equals(Pref.pFixBlurHomeIconOrder.toString())){
			boolean set = ((CheckBoxPreference)preference).isChecked();
			Module.setFixBlurIconOrder(this, set);
			boolean result = Module.getFixBlurIconOrderEnable();
			if (set==result){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		//gms
		if (key.equals(Pref.pFixGms.toString())){
			boolean set = ((CheckBoxPreference)preference).isChecked();
			Module.setFixGms(set);
			boolean result = Module.getFixGmsEnable();
			if (set==result){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		//main log
		if (key.equals(Pref.pFixImoseyonLog.toString())){
			boolean set = ((CheckBoxPreference)preference).isChecked();
			Module.setFixImoseyonLog(set);
			boolean result = Module.getFixImoseyonLogEnable();
			if (set==result){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		
		if (key.equals(Pref.pCallOnVibrate.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.CallOnVibrate);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pCallOnVibrate))){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		
		if (key.equals(Pref.pCallOffVibrate.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.CallOffVibrate);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pCallOffVibrate))){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		
		if (key.equals(Pref.pCallOn45SecVibrate.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.CallOn45SecVibrate);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pCallOn45SecVibrate))){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
			}
		}
		
		if (key.equals(Pref.pWifiAutoClose.toString())){
			if (((CheckBoxPreference)preference).isChecked()){
				Event.count(this, Event.WifiAutoClose);
			}
			if (Module.setPrefFlag(((CheckBoxPreference)preference).isChecked(),getPrefFlagFile(Pref.pWifiAutoClose))){
				myToast(R.string.msg_successfullyApplied);
			}else{
				((CheckBoxPreference)preference).setChecked(false);
				myToast(R.string.msg_failedToApply);
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
				myToast(R.string.msg_successfullyApplied);
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
				myToast(R.string.msg_failedToApply);
			}
		}
		
		//
		if (key.equals(Pref.pOverClock.toString())){
			Intent i = new Intent(this,OverClock.class);
			startActivity(i);
		}
		
		if (key.equals(Pref.pPower.toString())){
			Intent i = new Intent(this,Power.class);
			startActivity(i);
		}
		
		if (key.equals(Pref.pLcdBacklight.toString())){
			Intent i = new Intent(this,LcdBackLight.class);
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
				installApk(Module.getSd2romApkFile(this).getAbsolutePath());
				//installPluginTip(sd2rom_url, sd2rom_apk);
//				AlertDialog d = new AlertDialog.Builder(this)
//					.setTitle(R.string.msg_notInstallPlugin)
//					.setMessage(sd2rom_url)
//					.setPositiveButton(R.string.btn_confirm, null)
//					.create();
//				d.show();
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
						.setTitle(R.string.dialog_title_warning).setMessage(errorInfo)
						.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
															
							}
						})
						.create();
				d.show();
				pBar.dismiss();
			}
		});
	}

	public void refreshItemAll(){
		ListPreference lp = null;
		CheckBoxPreference cp = null;
		Preference p = null;
		
		this.setTitle(this.getTitle()+(haveRoot?ml.t(R.string.msg_tip_root, null):ml.t(R.string.msg_tip_noRoot, null)));
		Preference pTitle = this.findPreference(Pref.pTitle.toString());
		String versionName = "null";
		try{
			versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		}catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		pTitle.setSummary(ml.t(R.string.text_version, new String[]{versionName})+"  "+ml.t(R.string.text_author, new String[]{"Kyle Tang"}));
		
		//
		PropFile prop = new PropFile();
		prop.load("/system/build.prop");
		
		lp = ((ListPreference)findPreference(Pref.pModel.toString()));
		String strModel = prop.getValue("ro.product.model");
		if (strModel.equalsIgnoreCase("MotoA953")){
			lp.setValue("MotoA953");
		}else if (strModel.equalsIgnoreCase("A953")){
			lp.setValue("A953");
		}else if (strModel.equalsIgnoreCase("ME722")){
			lp.setValue("ME722");
		}
		
		lp = ((ListPreference)findPreference(Pref.pMultiMedia.toString()));
		if (android.os.Build.VERSION.RELEASE.contains("2.3")){
			lp.setEnabled(false);
			lp.setSummary(R.string.text_noSetting_2_3_x);
		}else{
			String strMultiMedia = prop.getValue("media.stagefright.enable-player");
			if ("false".equalsIgnoreCase(strMultiMedia)){
				lp.setValue("opencore");
			}else if ("true".equalsIgnoreCase(strMultiMedia)){
				lp.setValue("stagefright");
			}
		}
		
		lp = ((ListPreference)findPreference(Pref.pDalvikVMHeapMax.toString()));
		String strDalvikVMHeapMax = prop.getValue("dalvik.vm.heapsize").replace("m", "");
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
		((CheckBoxPreference)findPreference(Pref.pDefyMore.toString()))
			.setChecked(getPrefFlagFile(Pref.pDefyMore).exists());
		
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
		cp = ((CheckBoxPreference)findPreference(Pref.pFixBlurHomeIconOrder.toString()));
		if (android.os.Build.VERSION.RELEASE.contains("2.3")){
			//2.3系统不需要
			cp.setEnabled(false);
			cp.setSummaryOff(R.string.text_noNeedSetting_2_3_x);
			cp.setSummaryOn(R.string.text_noNeedSetting_2_3_x);
		}else{
			cp.setChecked(Module.getFixBlurIconOrderEnable());
		}
		
		//
		cp = ((CheckBoxPreference)findPreference(Pref.pFixGms.toString()));
		cp.setChecked(Module.getFixGmsEnable());
		
		//
		cp = ((CheckBoxPreference)findPreference(Pref.pFixImoseyonLog.toString()));
		if (Module.hasFixImoseyon()){
			cp.setChecked(Module.getFixImoseyonLogEnable());
		}else{
			cp.setEnabled(false);
			cp.setSummaryOff(ml.t(R.string.text_noInstall,new String[]{"imoseyon"}));
			cp.setSummaryOn(ml.t(R.string.text_noInstall,new String[]{"imoseyon"}));
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
		
		//设置键盘防抖动
		setDebouncePreference();
		
		//根据当前系统环境，关闭不兼容功能
		if (Module.isCMSeriesROM()){
			cp = ((CheckBoxPreference)findPreference(Pref.pDefyMore.toString()));
			cp.setEnabled(false);
			cp.setSummaryOff(R.string.text_basedCmNotSupported);
			cp.setSummaryOn(R.string.text_basedCmNotSupported);
			lp = ((ListPreference)findPreference(Pref.pDefyMoreNum.toString()));
			lp.setEnabled(false);
			lp.setSummary(R.string.text_basedCmNotSupported);
		}
	}
	
	public void setDebouncePreference(){
		//设置键盘防抖动
		Preference p = ((Preference)findPreference(Pref.pDebounce.toString()));
		Intent intent = null;
		try{
			intent = this.getPackageManager().getLaunchIntentForPackage("de.rmdir.ms2debounce");
			if (intent!=null) p.setSummary("Powered by 'Milestone 2 Debounce', from XDA");
		}catch(Exception exx){
			
		}
		if (intent==null){
			File file = Module.getDebounceApkFile(this);
			Uri uri = Uri.fromFile(file);
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri,"application/vnd.android.package-archive");
		}
		p.setIntent(intent);
	}
	
	public void myToast(String tipInfo) {
		if (myToast == null)
			myToast = new Toast(this);
		myToast.makeText(this, tipInfo, Toast.LENGTH_SHORT).show();
	}
	
	public void myToast(int tipInfo) {
		if (myToast == null)
			myToast = new Toast(this);
		myToast.makeText(this, tipInfo, Toast.LENGTH_SHORT).show();
	}
	
	public File getPrefFlagFile(Pref p){
		return new File(this.getFilesDir(),p.toString()+".flag");
	}
	
}