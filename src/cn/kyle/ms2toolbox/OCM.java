package cn.kyle.ms2toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cn.kyle.util.C;
import android.content.Context;

/**
 * OverClock Module
 * @author KyleTang
 *
 */
public class OCM {
	public static final int FV_default = 0;
	public static final int FV_1100 = 1;
	public static final int FV_1200 = 2;
	public static final int FV_1300 = 3;
	public static final int Freq = 0;
	public static final int Vsel = 1;
	private static String OverClockKO_FILE = "overclock_droidx_22.ko";
	
	public static int FreqVselCurrent[][] = {
			{300,22},
			{600,33},
			{800,40},
			{1000,63}
	};
	
	public static int FreqVsel[][][] = {
			{//defalut
				{300,22},
				{600,33},
				{800,40},
				{1000,48}
			},
			{//1100
				{300,22},
				{600,33},
				{900,48},
				{1100,60}
			},
			{//1200
				{300,22},
				{600,33},
				{900,48},
				{1200,63}
			},
			{//1300
				{300,22},
				{800,40},
				{1000,48},
				{1300,66}
			}
	};
	
	public static File getOverClockKOFile(Context context){
		return new File(context.getFilesDir().getAbsolutePath(),OverClockKO_FILE);
	}
	
	public static String getOverClockKO(Context context){
		File f = getOverClockKOFile(context);
		if (!f.exists() ){
			C.unpackFile(context, OverClockKO_FILE, "555");
		}
		return f.getAbsolutePath();
	}
	
	/**
	 * 保存FreqVselCurrent设置到配置文件
	 */
	public static void ocSaveFreqVsel(Context context){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<4;i++){
			sb.append(FreqVselCurrent[i][Freq]).append(",");
			sb.append(FreqVselCurrent[i][Vsel]).append(";");
		}
		Module.setPrefFlagValue(getConfigFile(context), sb.toString());
	}
	
	/**
	 * 从配置文件中读取到FreqVselCurrent
	 */
	public static void ocLoadFreqVsel(Context context){
		String value = Module.getPrefFlagValue(getConfigFile(context),"");
		if (!value.equals("")){
			String a[] = value.split(";");
			for(int i=0;i<4;i++){
				String fv[] = a[i].split(",");
				FreqVselCurrent[i][Freq] = parseInt(fv[Freq],FreqVsel[FV_default][i][Freq]);
				FreqVselCurrent[i][Vsel] = parseInt(fv[Vsel],FreqVsel[FV_default][i][Vsel]);
			}
		}else{
			//没有配置文件，读取默认设置
			for(int i=0;i<4;i++){
				FreqVselCurrent[i][Freq] = FreqVsel[FV_default][i][Freq];
				FreqVselCurrent[i][Vsel] = FreqVsel[FV_default][i][Vsel];
			}
		}
	}
	
	/**
	 * 禁用超频
	 */
	public static void ocUnLoad(){
		C.runSuCommandReturnBoolean("rmmod overclock");
	}
	
	/**
	 * 应用FreqVselCurrent到系统
	 */
	public static void ocApplyToSystem(Context context){
		// 安全保护，防止由于电压和频率设置过高，造成损害
		for (int i=0;i<4;i++){
			if (OCM.FreqVselCurrent[i][OCM.Freq] > 2000 || OCM.FreqVselCurrent[i][OCM.Freq] < 1)
				OCM.FreqVselCurrent[i][OCM.Freq] = 1000;
			if (OCM.FreqVselCurrent[i][OCM.Vsel] > 100 || OCM.FreqVselCurrent[i][OCM.Vsel] < 1)
				OCM.FreqVselCurrent[i][OCM.Vsel] = 66;
		}
		//
		StringBuilder sb = new StringBuilder();
		sb.append("rmmod overclock ; \n");
		sb.append("insmod "+getOverClockKO(context)+" ; \n");
		sb.append("echo 0x"+findOmapAddr()+" > /proc/overclock/omap2_clk_init_cpufreq_table_addr ; \n");
		sb.append("echo 0x"+findStatsAddr()+" > /proc/overclock/cpufreq_stats_update_addr ; \n");
		sb.append("echo "+OCM.FreqVselCurrent[3][OCM.Vsel]+" > /proc/overclock/max_vsel ; \n");
		sb.append("echo "+(OCM.FreqVselCurrent[3][OCM.Freq]*1000)+" > /proc/overclock/max_rate ; \n");
		for(int i=0;i<4;i++){
			//echo 1 300000000 22 > /proc/overclock/mpu_opps
			sb.append("echo "+(i+1)+" "+(OCM.FreqVselCurrent[i][OCM.Freq]*1000000)+" "+OCM.FreqVselCurrent[i][OCM.Vsel]+" > /proc/overclock/mpu_opps ; \n");
		}
		for(int i=0;i<4;i++){
			//echo 0 1200000 > /proc/overclock/freq_table
			sb.append("echo "+i+" "+(OCM.FreqVselCurrent[3-i][OCM.Freq]*1000)+" > /proc/overclock/freq_table ; \n");
		}
		C.runSuCommandReturnBoolean(sb.toString());
	}
	
	public static File getConfigFile(Context context){
		return new File(context.getFilesDir(),Pref.pOverClock.toString()+".flag.config");
	}
	
	
	public static int parseInt(String value , int defaultValue ){
		try{
			int n = Integer.parseInt(value.toString());
			return n;
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static boolean isLoadKO(){
		return new File("/proc/overclock").exists();
	}

	private static String findOmapAddr(){
		return findKallsymsAddr(" T omap2_clk_init_cpufreq_table");
	}

	private static String findStatsAddr(){
		return findKallsymsAddr(" t cpufreq_stats_update");
	}

	private static String findKallsymsAddr(String para) {
		BufferedReader br = null;
		String retValue = "";
		try {
			br = new BufferedReader(new FileReader("/proc/kallsyms"));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.endsWith(para)) {
					retValue = line.split(" ")[0];
					break;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				br = null;
			}
		}
		return retValue;
	}
}
