package cn.kyle.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import android.util.Log;

public class L {
	private static final String LOG_TAG = "ms2toolbox";

	public static void debug(String msg) {
		Log.d(LOG_TAG, msg);
	}

	public static void debug(String msg, Throwable throwable) {
		Log.d(LOG_TAG, msg, throwable);
	}

	public static void info(String msg) {
		Log.i(LOG_TAG, msg);
	}

	public static void info(String msg, Throwable throwable) {
		Log.i(LOG_TAG, msg, throwable);
	}

	public static void warn(String msg) {
		Log.w(LOG_TAG, msg);
	}

	public static void warn(String msg, Throwable throwable) {
		Log.w(LOG_TAG, msg, throwable);
	}

	public static void error(String msg) {
		Log.e(LOG_TAG, msg);
	}

	public static void error(String msg, Throwable throwable) {
		Log.e(LOG_TAG, msg, throwable);
	}

	public static void printLog(int priority, String msg) {
		Log.println(priority, LOG_TAG, msg);
	}

	public static void writeLine(OutputStream os, PrintWriter logWriter,
			String value) throws IOException {
		String line = value + "\n";
		os.write(line.getBytes());
		if (logWriter != null) {
			logWriter.println(value);
		}
	}

	public static void savelogtofile(String logtype, String logfmt,
			String filename, boolean alllog) {

		try {
			String cmd = "logcat -f " + filename + " -v " + logfmt + " "
					+ LOG_TAG + ":" + logtype;
			if (!alllog) {
				cmd += " *:S";
			}

			Process process = Runtime.getRuntime().exec("su -c sh");
			OutputStream os = process.getOutputStream();
			writeLine(os, null, cmd);

		} catch (Exception e) {
			throw new RuntimeException("Write log file error!");
		}

	}

}
