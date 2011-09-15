package cn.kyle.ms2toolbox.ui;

import java.io.File;
import java.io.IOException;

import com.mobclick.android.MobclickAgent;

import cn.kyle.ms2toolbox.Event;
import cn.kyle.ms2toolbox.Module;
import cn.kyle.ms2toolbox.MyReceiver;
import cn.kyle.ms2toolbox.R;
import cn.kyle.ms2toolbox.R.id;
import cn.kyle.ms2toolbox.R.layout;
import cn.kyle.ms2toolbox.R.string;
import cn.kyle.util.C;
import cn.kyle.util.L;
import cn.kyle.util.MultiLang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 屏幕背光调节(突破超低亮度)
 * @author KyleTang
 *
 */
public class LcdBackLight extends Activity {
	private Toast myToast = null;
	private Button btnApply = null;
	private Button btnAuto = null;
	private Button btnUp = null;
	private Button btnDown = null;
	private TextView tvTip = null;
	private TextView tvMode = null;
	private EditText etValue = null;
	private SeekBar sbValue = null;
	private MultiLang ml = null;
	private static int BRIGHTNESS_MODE_AUTO=1;
	private static int BRIGHTNESS_MODE_MANUAL=0;
	private int mode = 0;
	private String modeStringAuto = null;
	private String modeStringManual = null;
	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		setContentView(R.layout.lcdbacklight);
		Event.count(this, Event.LcdBackLight);
		
		//myRegisterReceiver();
		
		ml = new MultiLang(this);
		this.setTitle(R.string.lcd_title);
		try {
			mode = Settings.System.getInt(getContentResolver(), "screen_brightness_mode");
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		
		modeStringAuto = (String)this.getResources().getText(R.string.lcd_text_currentMode_auto);
		modeStringManual = (String)this.getResources().getText(R.string.lcd_text_currentMode_manual);
		
		tvTip = (TextView)findViewById(R.id.lbl_tvCurrentLcdLightValue);
		tvMode = (TextView)findViewById(R.id.lbl_tvCurrentMode);
		etValue = (EditText)findViewById(R.id.lbl_etValue);
		
		btnApply = (Button)findViewById(R.id.lbl_btnApply);
		btnApply.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				try{
					int value = Integer.parseInt(etValue.getText().toString());
					if (value<2) value=2;
					if (value>255) value=255;
					Settings.System.putInt(getContentResolver(), "screen_brightness_mode", BRIGHTNESS_MODE_MANUAL);
					sbValue.setProgress(value);
					Module.setLcdBackLight(value,false);
				}catch(Exception e){
					etValue.setText(""+sbValue.getProgress());
				}
			}
		});
		
		btnAuto = (Button)findViewById(R.id.lbl_btnAuto);
		btnAuto.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Settings.System.putInt(getContentResolver(), "screen_brightness_mode", BRIGHTNESS_MODE_AUTO);
				Module.setLcdBackLightAuto(40);
			}
		});

		sbValue = (SeekBar)findViewById(R.id.lbl_sbSelectValue);
		sbValue.setMax(255);
		
		int value = Module.getLcdBackLightCurrentValue();
		sbValue.setProgress(value);
		tvTip.setText(ml.t(R.string.lcd_text_currentLcdLightValue, new String[]{""+value}));
		tvMode.setText(ml.t(R.string.lcd_text_currentMode_tip, new String[]{(mode==1?modeStringAuto:modeStringManual)}));
		
		sbValue.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress<2) progress = 2;
				tvTip.setText(ml.t(R.string.lcd_text_currentLcdLightValue, new String[]{""+progress}));
				tvMode.setText(ml.t(R.string.lcd_text_currentMode_tip, new String[]{(mode==1?modeStringAuto:modeStringManual)}));
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
				int value=seekBar.getProgress();
				if (value<2) value=2;
				if (value>255) value=255;
				Settings.System.putInt(getContentResolver(), "screen_brightness_mode", BRIGHTNESS_MODE_MANUAL);
				Module.setLcdBackLight(value,false);
				
			}
		});
		
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
	
	private void myRegisterReceiver(){
		IntentFilter inf = new IntentFilter();
		inf.addAction("android.intent.action.SCREEN_ON");
		inf.addAction("android.intent.action.SCREEN_OFF");
		registerReceiver(new MyReceiver(), inf);
	}
}
