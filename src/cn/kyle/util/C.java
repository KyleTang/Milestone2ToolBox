package cn.kyle.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Build;

public class C {
	public final static String SCRIPT_NAME = "surunner.sh";
	public static String CmdMountSystemRW = "mount -o remount,rw /dev/block/mmcblk1p21 /system ";
	
	public static boolean isModelMilestone2Compatible() {
		L.debug("isModelMilestone2Compatible: model is "+Build.MODEL);
		if ( Build.MODEL.equalsIgnoreCase("MotoA953")||
				Build.MODEL.equalsIgnoreCase("A953") || 
				Build.MODEL.equalsIgnoreCase("DROIDX") || 
				Build.MODEL.equalsIgnoreCase("ME722") ||
				Build.MODEL.equalsIgnoreCase("droid2") || 
				Build.MODEL.equalsIgnoreCase("droid2 Global"))
			return true;
		else
			return false;
	}
	public static Process runSuCommandAsync(Context context, String command) throws IOException
	{
		DataOutputStream fout = new DataOutputStream(context.openFileOutput(SCRIPT_NAME, 0));
		fout.writeBytes(command);
		fout.close();
		
		String[] args = new String[] { "su", "-c", ". " + context.getFilesDir().getAbsolutePath() + "/" + SCRIPT_NAME };
		Process proc = Runtime.getRuntime().exec(args);
		return proc;
	}

	public static int runSuCommand(Context context, String command) throws IOException, InterruptedException
	{
		return runSuCommandAsync(context, command).waitFor();
	}
	
	public static int runSuCommandNoScriptWrapper(String command) throws IOException, InterruptedException
	{
		String[] args = new String[] { "su","-c",command};
		Process proc = Runtime.getRuntime().exec(args);
		return proc.waitFor();
	}
	
	public static boolean runSuCommandReturnBoolean(String command)
	{
		try {
			Process process = Runtime.getRuntime().exec("su");
			OutputStream out = process.getOutputStream();
			DataOutputStream dataOut = new DataOutputStream(out);
			dataOut.writeBytes(command + "\n");
			dataOut.flush();
			dataOut.writeBytes("exit\n");
			dataOut.flush();
			int i = process.waitFor();
			//L.debug("process.waitFor="+i+", cmd="+command);
			if (i==0)
				return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean haveRoot(){
		return runSuCommandReturnBoolean("echo 1");
//		try{
//			int ret = runSuCommandNoScriptWrapper("echo 1");
//			if (ret==0)
//				return true;
//			else
//				return false;
//		}catch(Exception e){
//			return false;
//		}
	}

	public static boolean hasUnpackBusyBox(Context context){
		return new File(getBusyBoxPath(context)).exists();
	}
	
	public static String getBusyBoxPath(Context context){
		return new File(context.getFilesDir().getAbsolutePath(),"busybox").getAbsolutePath();
	}
	
	public static boolean installBusyBox(Context context){
		//if (!mountSystemRW()) return false;
		if (!hasUnpackBusyBox(context))
			C.unpackFile(context, "busybox", "777");
		String busybox = getBusyBoxPath(context);
		StringBuilder sb = new StringBuilder();
		sb.append(C.CmdMountSystemRW+" ; ");
		sb.append(busybox+" cp "+ busybox +" /system/bin/busybox ; ");
		sb.append("chmod 777 /system/bin/busybox ; ");
		sb.append("chown root.root /system/bin/busybox ; ");
		sb.append("busybox ; ");
		return C.runSuCommandReturnBoolean(sb.toString());
	}
	
	public static boolean mountSystemRW(){
		try {
			int ret = C.runSuCommandNoScriptWrapper(CmdMountSystemRW);
			if (ret==0){
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static String warpCmdWithBusybox(Context context, String cmd){
		return getBusyBoxPath(context) + " " + cmd;
	}
	
	public static void unpackFile(Context context, String filename, String chmod){
		if (chmod==null || chmod.trim().equals("")){
			chmod = "777";
		}
		String dir = "files/";
		File file = new File(context.getFilesDir().getAbsolutePath(),filename);
		int count=0;
		byte[] bs = new byte[10240];
		try {
			InputStream in = context.getResources().getAssets().open(dir+filename);
			FileOutputStream out = new FileOutputStream(file,false);
			while((count = in.read(bs))>=0){
				out.write(bs,0,count);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		runSuCommandReturnBoolean("chmod "+chmod+" "+file.getAbsolutePath());
	}

	public static boolean hasUnpacksSqlite3(Context context){
		return getSqlite3File(context).exists();
	}
	
	public static File getSqlite3File(Context context){
		return new File(context.getFilesDir().getAbsolutePath(),"sqlite3");
	}
	
	public static void unpacksSqlite3(Context context){
		C.unpackFile(context, "sqlite3", "777");
	}
	
	public static String getSqlite3CmdString(Context context, String dbFilePath, String sqls){
		String sqlite3 = getSqlite3File(context).getAbsolutePath();
		if (!hasUnpacksSqlite3(context)){
			unpacksSqlite3(context);
		}
		return sqlite3+" "+dbFilePath+" "+sqls;
	}
}
