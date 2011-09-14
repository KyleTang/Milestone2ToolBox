package cn.kyle.ms2toolbox.ui;

import java.io.File;
import java.io.IOException;

import com.mobclick.android.MobclickAgent;

import cn.kyle.ms2toolbox.Event;
import cn.kyle.ms2toolbox.Module;
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
	private EditText etValue = null;
	private SeekBar sbValue = null;
	private MultiLang ml = null;
	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		setContentView(R.layout.lcdbacklight);
		Event.count(this, Event.LcdBackLight);
		ml = new MultiLang(this);
		this.setTitle(R.string.lcd_title);
		int mode = 0;
		try {
			mode = Settings.System.getInt(getContentResolver(), "screen_brightness_mode");
			
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		
		tvTip = (TextView)findViewById(R.id.lbl_tvCurrentLcdLightValue);
		etValue = (EditText)findViewById(R.id.lbl_etValue);
		
		btnApply = (Button)findViewById(R.id.lbl_btnApply);
		btnApply.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				try{
					int value = Integer.parseInt(etValue.getText().toString());
					if (value<2) value=2;
					if (value>255) value=255;
					Settings.System.putInt(getContentResolver(), "screen_brightness_mode", 0);
					sbValue.setProgress(value);
					Module.setLcdBackLight(value);
				}catch(Exception e){
					etValue.setText(""+sbValue.getProgress());
				}
			}
		});
		
		btnAuto = (Button)findViewById(R.id.lbl_btnAuto);
		btnAuto.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Settings.System.putInt(getContentResolver(), "screen_brightness_mode", 1);
				Module.setLcdBackLightAuto(40);
			}
		});

		sbValue = (SeekBar)findViewById(R.id.lbl_sbSelectValue);
		sbValue.setMax(255);
		
		int value = Module.getLcdBackLightCurrentValue();
		sbValue.setProgress(value);
		tvTip.setText(ml.t(R.string.lcd_text_currentLcdLightValue, new String[]{""+value}));
		sbValue.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress<2) progress = 2;
				tvTip.setText(ml.t(R.string.lcd_text_currentLcdLightValue, new String[]{""+progress}));
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			public void onStopTrackingTouch(SeekBar seekBar) {
				int value=seekBar.getProgress();
				if (value<2) value=2;
				if (value>255) value=255;
				Settings.System.putInt(getContentResolver(), "screen_brightness_mode", 0);
				Module.setLcdBackLight(value);
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
}
