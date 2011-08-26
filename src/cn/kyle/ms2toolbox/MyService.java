package cn.kyle.ms2toolbox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cn.kyle.util.L;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * （未使用）
 * @author KyleTang
 *
 */
public class MyService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		L.debug("onBind:");
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		L.debug("onStart:");
		new Thread(){
			public void run(){
				//send event
				DataInputStream dataIn = null;
				DataOutputStream dataOut = null;
				try {
					Process p = Runtime.getRuntime().exec("su");
					dataIn = new DataInputStream(p.getInputStream());
					dataOut = new DataOutputStream(p.getOutputStream());
					dataOut.writeBytes("getevent /dev/input/event4\n");
					dataOut.flush();
					String line = null;
					while((line=dataIn.readLine())!=null){
						L.debug("getevent: "+line);
					}
					if (dataIn!=null) dataIn.close();
					if (dataOut!=null) dataOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally{
					try {
						if (dataIn!=null)
							dataIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						if (dataOut!=null)
							dataOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		super.onStart(intent, startId);
	}

}
