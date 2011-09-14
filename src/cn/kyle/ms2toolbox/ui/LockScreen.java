package cn.kyle.ms2toolbox.ui;

import java.io.File;
import java.io.IOException;

import cn.kyle.ms2toolbox.Event;
import cn.kyle.ms2toolbox.Module;
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
 * 锁屏
 * @author KyleTang
 *
 */
public class LockScreen extends Activity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Event.count(this, Event.LockScreen);
		Module.pressPowerButton();
		this.finish();
	}
}
