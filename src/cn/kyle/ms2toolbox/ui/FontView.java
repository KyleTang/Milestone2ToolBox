package cn.kyle.ms2toolbox.ui;

import java.io.File;
import java.io.IOException;

import com.mobclick.android.MobclickAgent;

import cn.kyle.ms2toolbox.R;
import cn.kyle.ms2toolbox.R.id;
import cn.kyle.ms2toolbox.R.layout;
import cn.kyle.util.C;
import cn.kyle.util.L;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
 * 字体预览界面
 * @author KyleTang
 *
 */
public class FontView extends Activity {
	
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this); 
	}
	
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		setContentView(R.layout.fontview);
		Bundle b = this.getIntent().getExtras();
		if (b!=null){
			String path = b.getString("fontpath");
			this.setTitle("字体预览");
			Typeface tf = Typeface.createFromFile(path);
			TextView tvFontPreview = (TextView)findViewById(R.id.tvFontPreview);
			tvFontPreview.setText(
					"欢迎使用里程碑2工具箱\n"+
					"Welcome to use Milestone2 ToolBox \n"+
					"1234567890\n");
			tvFontPreview.setTextSize(30);
			tvFontPreview.setTypeface(tf);
		}
	}
}
