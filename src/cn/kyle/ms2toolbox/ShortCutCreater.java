package cn.kyle.ms2toolbox;

import java.io.File;
import java.io.IOException;

import cn.kyle.util.C;
import cn.kyle.util.L;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
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
 * 桌面快捷方式生成器
 * @author KyleTang
 *
 */
public class ShortCutCreater extends Activity {
	Toast myToast = null;
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shortcutcreater);
		
		Button btnPower = (Button)findViewById(R.id.btnPower); 
		btnPower.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onClickCreateShortCut(v);
			}
		});
		
		Button btnFlashLight = (Button)findViewById(R.id.btnFlashLight); 
		btnFlashLight.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onClickCreateShortCut(v);
			}
		});
		
		Button btnLockScreen = (Button)findViewById(R.id.btnLockScreen); 
		btnLockScreen.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onClickCreateShortCut(v);
			}
		});
		
		Button btnLcdBackLight = (Button)findViewById(R.id.btnLcdBackLight); 
		btnLcdBackLight.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onClickCreateShortCut(v);
			}
		});
	}
	
	public void onClickCreateShortCut(View v){
		Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		String title = null;
		Parcelable icon = null;
		Intent myIntent = null;
        switch (v.getId()) {
        case R.id.btnPower:
        	title=""+this.getResources().getText(R.string.power);
        	icon=Intent.ShortcutIconResource.fromContext(this, R.drawable.power); //获取快捷键的图标
            myIntent=new Intent(this, Power.class);
        	break;
        case R.id.btnFlashLight:
        	title=""+this.getResources().getText(R.string.flashlight);
        	icon=Intent.ShortcutIconResource.fromContext(this, R.drawable.flashlight); //获取快捷键的图标
            myIntent=new Intent(this, FlashLight.class);
        	break;
        case R.id.btnLockScreen:
        	title=""+this.getResources().getText(R.string.lockscreen);
        	icon=Intent.ShortcutIconResource.fromContext(this, R.drawable.lockscreen); //获取快捷键的图标
            myIntent=new Intent(this, LockScreen.class);
        	break;
        case R.id.btnLcdBackLight:
        	title=""+this.getResources().getText(R.string.lcdbacklight);
        	icon=Intent.ShortcutIconResource.fromContext(this, R.drawable.lcdbacklight); //获取快捷键的图标
            myIntent=new Intent(this, LcdBackLight.class);
        	//myToast("Oops. comming soon.");
        	break;
        }
    	addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);//快捷方式的标题
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);//快捷方式的图标
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, myIntent);//快捷方式的动作
        sendBroadcast(addIntent);//发送广播
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
	
	public File getPrefFlagFile(Pref p){
		return new File(this.getFilesDir(),p.toString()+".flag");
	}
}
