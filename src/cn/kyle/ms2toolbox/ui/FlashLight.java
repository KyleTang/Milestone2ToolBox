package cn.kyle.ms2toolbox.ui;

import java.io.File;
import java.io.IOException;

import cn.kyle.ms2toolbox.Event;
import cn.kyle.ms2toolbox.Module;
import cn.kyle.ms2toolbox.Pref;
import cn.kyle.util.C;
import cn.kyle.util.L;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 手电筒
 * @author KyleTang
 *
 */
public class FlashLight extends Activity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Event.count(this, Event.FlashLight);
		SharedPreferences sp = this.getSharedPreferences("cn.kyle.ms2toolbox_preferences",Context.MODE_PRIVATE);
		int level = Integer.parseInt(sp.getString(Pref.pFlashLightLevel.toString(), "80"))	;
		final int time = Integer.parseInt(sp.getString(Pref.pFlashLightTime.toString(), "120"))	;
		L.debug("level="+level);
		int value = Module.getFlashLightCurrentValue();
		if (Module.setFlashLight(value==0?level:0)){
			if (value==0){
				new Thread(){
					public void run(){
						try {
							Thread.sleep(1000*time);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Module.setFlashLight(0);
					}
				}.start();
			}
		}
		this.finish();
	}
	
}
