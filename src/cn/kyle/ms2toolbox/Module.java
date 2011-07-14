package cn.kyle.ms2toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;

import cn.kyle.util.C;
import cn.kyle.util.L;
import cn.kyle.util.PropFile;
import cn.kyle.util.PropFile.FindMode;
import cn.kyle.util.PropFile.ReplaceMode;

public class Module {
	public static String qwerty    = "/system/usr/keylayout/qwerty.kl";
	public static String ms2keypad = "/system/usr/keylayout/umts_milestone2-keypad.kl";
	public static String buildProp = "/system/build.prop";
	public static String defaultmodel = "/system/ms2toolbox.defaultmodel";
	
	
	public static String cameraWakeupUnset_Focus  = "key 211   FOCUS";
	public static String cameraWakeupSet_Focus    = "key 211   FOCUS             WAKE";
	public static String cameraWakeupUnset_Camera = "key 212   CAMERA";
	public static String cameraWakeupSet_Camera   = "key 212   CAMERA            WAKE";
	
	public static String volupWakeupUnset   = "key 115   VOLUME_UP";
	public static String volupWakeupSet     = "key 115   VOLUME_UP         WAKE";
	public static String voldownWakeupUnset = "key 114   VOLUME_DOWN";
	public static String voldownWakeupSet   = "key 114   VOLUME_DOWN       WAKE";
	
	public static String zh2en_key   = "key 167";
	public static String zh2en_voice = "key 167   VOICE             WAKE_DROPPED";
	public static String zh2en_zh2en = "key 167   EXPLORER          WAKE_DROPPED";
	
	public static String DebounceKO_FILE = "debounce.ko";
	public static String DefyMoreKO_FILE = "defy_more.ko";
	
	public static boolean getVoiceZh2EnEnable(){
		return C.runSuCommandReturnBoolean("busybox grep '"+zh2en_zh2en+"' /system/usr/keylayout/umts_milestone2-keypad.kl");
	}
	
	public static boolean setVoiceZh2En(boolean set){
		if (!C.runSuCommandReturnBoolean(
				C.CmdMountSystemRW+" ; "+
				"chmod 666 "+ms2keypad+" ; ")){
			return false;
		}
		if ( PropFile.replacePropLine(
				ms2keypad,
				null,
				zh2en_key,
				set?zh2en_zh2en:zh2en_voice, 
				FindMode.StartsWith, ReplaceMode.Line, false, false) ){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean setVolupWakeup(boolean set){
		if (!C.runSuCommandReturnBoolean(
				C.CmdMountSystemRW+" ; "+
				"chmod 666 "+qwerty+" ; ")){
			return false;
		}
		if ( PropFile.replacePropLine(
				qwerty,
				null,
				volupWakeupUnset,
				set?volupWakeupSet:volupWakeupUnset, 
				FindMode.StartsWith, ReplaceMode.Line, false, false) ){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean setVoldownWakeup(boolean set){
		if (!C.runSuCommandReturnBoolean(
				C.CmdMountSystemRW+" ; "+
				"chmod 666 "+qwerty+" ; ")){
			return false;
		}
		if ( PropFile.replacePropLine(
				qwerty,
				null,
				voldownWakeupUnset,
				set?voldownWakeupSet:voldownWakeupUnset, 
				FindMode.StartsWith, ReplaceMode.Line, false, false) ){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean getCameraKeyWakeupEnable(){
		return C.runSuCommandReturnBoolean("busybox grep '"+cameraWakeupSet_Focus+"' /system/usr/keylayout/umts_milestone2-keypad.kl");
	}
	
	public static boolean setCameraKeyWakeup(boolean set){
		if (!C.runSuCommandReturnBoolean(
				C.CmdMountSystemRW+" ; "+
				"chmod 666 "+qwerty+" ; "+
				"chmod 666 "+ms2keypad+" ; ")){
			return false;
		}
		PropFile pf = new PropFile();
		if (pf.load(ms2keypad)
				&& pf.replaceLine(
					cameraWakeupUnset_Focus, 
					set?cameraWakeupSet_Focus:cameraWakeupUnset_Focus, 
					FindMode.StartsWith, ReplaceMode.Line, false, false)
				&& pf.replaceLine(
					cameraWakeupUnset_Camera, 
					set?cameraWakeupSet_Camera:cameraWakeupUnset_Camera, 
					FindMode.StartsWith, ReplaceMode.Line, false, false)
				&& pf.save() 
				&& PropFile.replacePropLine(
						qwerty,
						qwerty,
						cameraWakeupUnset_Camera, 
						set?cameraWakeupSet_Camera:cameraWakeupUnset_Camera, 
						FindMode.StartsWith, ReplaceMode.Line, false, false)
			)
		{
			//OK
			return true;
		}else{
			//fail
			return false;
		}
	}
	
	public static boolean setStagefright(boolean set){
		if (!C.runSuCommandReturnBoolean(
				C.CmdMountSystemRW+" ; "+
				"chmod 666 "+buildProp+" ; ")){
			return false;
		}
		PropFile pf = new PropFile();
		if (pf.load(buildProp)){
			pf.putValue("media.stagefright.enable-player", ""+set);
			pf.putValue("media.stagefright.enable-meta", ""+set);
			pf.putValue("media.stagefright.enable-scan", ""+set);
			pf.putValue("media.stagefright.enable-http", ""+set);
			if ( pf.save())
				return true;
			else
				return false;
		}
		return false;
	}
	
	public static boolean setDalvikVMHeapMax(int value){
		if (value<30) value = 30;
		if (value>100) value = 30;
		if (!C.runSuCommandReturnBoolean(
				C.CmdMountSystemRW+" ; "+
				"chmod 666 "+buildProp+" ; ")){
			return false;
		}
		PropFile pf = new PropFile();
		if (pf.load(buildProp)){
			pf.putValue("dalvik.vm.heapsize", ""+value+"m");
			if ( pf.save())
				return true;
			else
				return false;
		}
		return false;
	}
	
	public static boolean setModel(String set){
		if (!C.runSuCommandReturnBoolean(
				C.CmdMountSystemRW+" ; "+
				"chmod 666 "+buildProp+" ; ")){
			return false;
		}
		PropFile pf = new PropFile();
		if (pf.load(buildProp)){
			pf.putValue("ro.product.model", ""+set);
			if ( pf.save())
				return true;
			else
				return false;
		}
		return false;
	}
	
	public static boolean setBootupTone(String set){
		if (!C.runSuCommandReturnBoolean(
				C.CmdMountSystemRW+" ; "+
				"chmod 666 "+buildProp+" ; "+
				"busybox cp "+set+" /system/media/bootup.ogg ; ")){
			return false;
		}
		
		PropFile pf = new PropFile();
		if (pf.load(buildProp)){
			pf.putValue("persist.mot.powerup.tone", "/system/media/bootup.ogg");
			if ( pf.save())
				return true;
			else
				return false;
		}
		return false;
	}

	public static boolean setButtonBacklightClosed(boolean checked) {
		return C.runSuCommandReturnBoolean(
				"chmod 666 /sys/devices/platform/ld-button-backlight/leds/button-backlight/brightness; "+
				"echo "+(checked?"0":"255")
				+" > /sys/devices/platform/ld-button-backlight/leds/button-backlight/brightness ; "+
				(checked?"chmod 444 /sys/devices/platform/ld-button-backlight/leds/button-backlight/brightness; ":""));
	}

	public static boolean setKeyboardBacklightClosed(boolean checked) {
		return C.runSuCommandReturnBoolean(
				"chmod 666 /sys/devices/platform/ld-keyboard-backlight/leds/keyboard-backlight/brightness; "+
				"echo "+(checked?"0":"255")
				+" > /sys/devices/platform/ld-keyboard-backlight/leds/keyboard-backlight/brightness ; " +
				(checked?"chmod 444 /sys/devices/platform/ld-keyboard-backlight/leds/keyboard-backlight/brightness; ":""));
	}
	
	public static boolean setFlashLight(int level){
		int value = (int)(255.0/100*level);
		if (value>255) value = 100;
		if (value<0) value = 0;
		L.debug("Open FlashLight Level = "+level+", value="+value);
		return C.runSuCommandReturnBoolean(
				"chmod 666 /sys/devices/platform/i2c_omap.3/i2c-3/3-0053/leds/torch-flash/flash_light; "+
				"echo "+value
				+" > /sys/devices/platform/i2c_omap.3/i2c-3/3-0053/leds/torch-flash/flash_light ;");
	}

	public static int getFlashLightCurrentValue(){
		return Integer.parseInt(
				getPrefFlagValue(
						new File("/sys/devices/platform/i2c_omap.3/i2c-3/3-0053/leds/torch-flash/flash_light"),"0"));
	}
	
	public static boolean setCameraClickDisable(boolean set){
//		if (set){
//			setFileDisable(new File("/system/media/audio/ui/camera_click.ogg"),set);
//			setFileDisable(new File("/system/media/audio/notifications/camera_click.ogg"),set);
//			return true;
//		}else{
//			boolean a = setFileDisable(new File("/system/media/audio/ui/camera_click.ogg"),set);
//			boolean b = setFileDisable(new File("/system/media/audio/notifications/camera_click.ogg"),set);
//			if (a||b){
//				return true;
//			}else{
//				return false;
//			}
//		}
		return setFileDisable(new File("/system/media/audio/ui/camera_click.ogg"),set);
	}
	
	public static boolean getCameraClickDisable(){
//		return getFileDisable(new File("/system/media/audio/ui/camera_click.ogg"))
//			&& getFileDisable(new File("/system/media/audio/notifications/camera_click.ogg"));
		return getFileDisable(new File("/system/media/audio/ui/camera_click.ogg"));
	}
	
	public static boolean setVideoRecordDisable(boolean set){
//		if (set){
//			setFileDisable(new File("/system/media/audio/ui/VideoRecord.ogg"),set);
//			setFileDisable(new File("/system/media/audio/notifications/VideoRecord.ogg"),set);
//			return true;
//		}else{
//			boolean a = setFileDisable(new File("/system/media/audio/ui/VideoRecord.ogg"),set);
//			boolean b = setFileDisable(new File("/system/media/audio/notifications/VideoRecord.ogg"),set);
//			if (a||b){
//				return true;
//			}else{
//				return false;
//			}
//		}
		return setFileDisable(new File("/system/media/audio/ui/VideoRecord.ogg"),set);
	}
	
	public static boolean getVideoRecordDisable(){
//		return getFileDisable(new File("/system/media/audio/ui/VideoRecord.ogg"))
//			&& getFileDisable(new File("/system/media/audio/notifications/VideoRecord.ogg"));
		return getFileDisable(new File("/system/media/audio/ui/VideoRecord.ogg"));
	}
	
	public static boolean setFileDisable(File f ,boolean disable) {
		File fdisable = new File(f.getAbsoluteFile()+".disable");
		if ((!f.exists())&&(!fdisable.exists())) return false;
		if (f.exists()&&disable){
			return C.runSuCommandReturnBoolean(
					C.CmdMountSystemRW + " ; "+
					"chmod 666 "+f.getAbsolutePath()+" ; "+
					"mv "+f.getAbsolutePath()+" "+fdisable.getAbsolutePath()+ " ; ");
		}
		if (fdisable.exists()&&!disable){
			return C.runSuCommandReturnBoolean(
					C.CmdMountSystemRW + " ; "+
					"chmod 666 "+fdisable.getAbsolutePath()+" ; "+
					"mv "+fdisable.getAbsolutePath()+" "+f.getAbsolutePath()+ " ; ");
		}
		return true;
	}
	
	public static boolean getFileDisable(File f ){
		return !f.exists();
	}
	
	public static boolean setPrefFlag(boolean checked, File flag) {
		if (checked) {
			try {
				flag.getParentFile().mkdirs();
				flag.createNewFile();
				return flag.exists();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			flag.delete();
			return !flag.exists();
		}
		return false;
	}
	
	public static boolean setPrefFlagValue(File flag, String value) {
		if (flag.exists()){
			FileWriter fw;
			try {
				fw = new FileWriter(flag);
				fw.write(value);
				fw.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static String getPrefFlagValue(File flag, String defaultValue) {
		if (flag.exists()){
			BufferedReader br=null;
			try {
				br = new BufferedReader(new FileReader(flag));
				String value = br.readLine();
				br.close();
				return value;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return defaultValue;
	}
	
	public static void pressImeSwitch(){
		String cmd = 
			//shift press down
			"sendevent /dev/input/event2 1 107 1 ; " + 
			//space press down
			"sendevent /dev/input/event2 1 107 1 ; " +
			//space press up
			"sendevent /dev/input/event2 1 107 0 ; " +
			//shift press up
			"sendevent /dev/input/event2 1 107 0 ; " ;
		C.runSuCommandReturnBoolean(cmd);	
	}
	
	
	public static void pressPowerButton(){
		/**
		 * sendevent /dev/input/event2 1 107 1
		 * sendevent /dev/input/event2 1 107 0 
		 * 
		 * sendevent [device] [type] [code] [value]
		 * device: /dev/input/event2 maybe physical button
		 * type: 1 maybe physical button
		 * code: key code 
		 * value: 1 press down ,0 press up 
		 */
		C.runSuCommandReturnBoolean("sendevent /dev/input/event2 1 107 1 ; sendevent /dev/input/event2 1 107 0 ;");
	}
	
	public static boolean setLowBatteryOff(boolean setOff){
		return setFileDisable(new File("/system/media/audio/ui/LowBattery.ogg"),setOff);
	}
	
	public static boolean getFixBlurIconOrderEnable(){
		return new File("/system/etc/motorola/com.motorola.blur.home/iconorder.mkitso.bak").exists();
	}
	
	public static boolean setFixBlurIconOrder(Context context , boolean isChecked){
		String initFilePath = "/system/etc/motorola/com.motorola.blur.home/iconorder.mkitso";
		String dbFilePath = "/data/data/com.motorola.blur.home/databases/launcher.db";
		
		StringBuilder sb = new StringBuilder();
		sb.append(C.CmdMountSystemRW+" ; ");
		if (isChecked){
			//backup
			sb.append(" if ! -r /system/etc/motorola/com.motorola.blur.home/iconorder.mkitso.bak \n then \n ")
				.append(" busybox cp -a /system/etc/motorola/com.motorola.blur.home/iconorder.mkitso /system/etc/motorola/com.motorola.blur.home/iconorder.mkitso.bak ; ")
				.append(" fi ; \n ");
			sb.append(" if ! -r /data/data/com.motorola.blur.home/databases/launcher.db.bak \n then \n ")
				.append(" busybox cp -a /data/data/com.motorola.blur.home/databases/launcher.db /data/data/com.motorola.blur.home/databases/launcher.db.bak ; ")
				.append(" fi ; \n ");
			//fix
			sb.append(" echo '' > /system/etc/motorola/com.motorola.blur.home/iconorder.mkitso ; \n");
			sb.append(C.getSqlite3CmdString(context, dbFilePath, "'delete from iconorder;'")).append(" ; \n");
			sb.append(" chmod 777 /data/data/com.motorola.blur.home/databases/launcher.db ; \n");
			L.debug(sb.toString());
		}else{
			//restore
			sb.append(" busybox cp -a /system/etc/motorola/com.motorola.blur.home/iconorder.mkitso.bak /system/etc/motorola/com.motorola.blur.home/iconorder.mkitso ; \n");
			sb.append(" busybox cp -a /data/data/com.motorola.blur.home/databases/launcher.db.bak /data/data/com.motorola.blur.home/databases/launcher.db ; \n");
			sb.append(" rm /system/etc/motorola/com.motorola.blur.home/iconorder.mkitso.bak ; \n");
			sb.append(" rm /data/data/com.motorola.blur.home/databases/launcher.db.bak ; \n");
			sb.append(" chmod 777 /data/data/com.motorola.blur.home/databases/launcher.db ; \n");
			sb.append(" echo 1 ");
		}
		return C.runSuCommandReturnBoolean(sb.toString());
	}
	
	/**
	 * 查看gms连接是否修复
	 * @return true为已经修复，false 尚未修复
	 */
	public static boolean getFixGmsEnable(){
		return C.runSuCommandReturnBoolean("busybox grep -v '#' /system/etc/hosts | busybox grep '74.125.93.113' ;");
	}
	
	public static boolean setFixGms(boolean isChecked){
		StringBuilder sb = new StringBuilder();
		sb.append(C.CmdMountSystemRW+" ; ");
		sb.append(" busybox sed -i '/android.clients.google.com/d' /system/etc/hosts ; ");
		if (isChecked){
			sb.append(" echo '74.125.93.113 android.clients.google.com' >> /system/etc/hosts ; ");
		}
		return C.runSuCommandReturnBoolean(sb.toString());
	}
	
	public static boolean hasFixImoseyon(){
		return new File("/system/etc/init.d/99imoseyon").exists();
	}
	
	public static boolean getFixImoseyonLogEnable(){
		return !C.runSuCommandReturnBoolean("busybox grep -v '#' /system/etc/init.d/99imoseyon | busybox grep -E 'rm.*/dev/log/main' ;");
	}

	public static boolean setFixImoseyonLog(boolean isChecked){
		//   /system/etc/init.d/99imoseyon
		StringBuilder sb = new StringBuilder();
		sb.append(C.CmdMountSystemRW+" ; ");
		if (isChecked){
			sb.append(" busybox sed -i -r 's/.*rm.+dev.log.main/# rm \\/dev\\/log\\/main/g' /system/etc/init.d/99imoseyon ; ");
		}else{
			sb.append(" busybox sed -i -r 's/.*rm.+dev.log.main/rm \\/dev\\/log\\/main/g' /system/etc/init.d/99imoseyon ; ");
		}
		return C.runSuCommandReturnBoolean(sb.toString());
	}
	
	public static File getDebounceKOFile(Context context){
		return new File(context.getFilesDir().getAbsolutePath(),DebounceKO_FILE);
	}
	
	public static String getDebounceKO(Context context){
		File f = getDebounceKOFile(context);
		if (!f.exists() ){
			C.unpackFile(context, DebounceKO_FILE, "555");
		}
		return f.getAbsolutePath();
	}
	
	public static boolean setDebounce(boolean isChecked,Context context){
		StringBuilder sb = new StringBuilder();
		if (isChecked){
			sb.append("rmmod debounce ; \n insmod "+getDebounceKO(context)+" debounce_delay=10 ; \n");
		}else{
			sb.append("rmmod debounce ; \n");
		}
		return C.runSuCommandReturnBoolean(sb.toString());
	}
	
	public static File getDefyMoreKOFile(Context context){
		return new File(context.getFilesDir().getAbsolutePath(),Module.DefyMoreKO_FILE);
	}
	
	public static String getDefyMoreKO(Context context){
		File f = getDefyMoreKOFile(context);
		if (!f.exists() ){
			C.unpackFile(context, DefyMoreKO_FILE, "555");
		}
		return f.getAbsolutePath();
	}
	
	public static boolean setDefyMore(boolean enable,Context context,int n){
		StringBuilder sb = new StringBuilder();
		if (enable){
			sb.append("rmmod defy_more ; \n insmod "+getDefyMoreKO(context)+" touch_num="+n+" ; \n");
		}else{
			sb.append("rmmod defy_more ; \n");
		}
		return C.runSuCommandReturnBoolean(sb.toString());
	}
}
