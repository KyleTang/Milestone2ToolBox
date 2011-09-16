package cn.kyle.ms2toolbox.ui;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import cn.kyle.ms2toolbox.Event;
import cn.kyle.ms2toolbox.Pref;
import cn.kyle.ms2toolbox.R;
import cn.kyle.ms2toolbox.R.layout;
import cn.kyle.util.BitMapUtil;
import cn.kyle.util.C;
import cn.kyle.util.L;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class QuickNavPanel extends Activity {
	Toast myToast = null;
	LinearLayout quickNavPanelList ;
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quicknavpanel );
		Event.count(this, Event.QuickNavPanel);
		quickNavPanelList = (LinearLayout)findViewById(R.id.quickNavPanelList);
		addSearchItems();
		addMs2ToolBoxItems();
		//myToast("Oops. comming soon.");
	}
	
	public void addSearchItems(){
		PackageManager pm = getPackageManager();
    	List<ResolveInfo> activities = pm.queryIntentActivities(
    		new Intent(Intent.ACTION_SEARCH_LONG_PRESS), 0);
    	L.debug("--begin");
    	if (activities.size()>0){
    		Iterator<ResolveInfo> it = activities.iterator();
    		ResolveInfo ri = null;
    		while(it.hasNext()){
    			ri = it.next();
    			if ("cn.kyle.ms2toolbox".equals(ri.activityInfo.packageName)){
    				continue;
    			}
    			L.debug("activity: "+ri.activityInfo.packageName+", "+ri.activityInfo.toString());
    			Button btn = new Button(this);
    	    	btn.setWidth(72);
    	    	btn.setHeight(72);
    	    	final ActivityInfo ai = ri.activityInfo;
    	    	btn.setBackgroundDrawable(BitMapUtil.getResizeDrawable(this,ai.loadIcon(pm),72,72));
    	    	btn.setOnClickListener(new OnClickListener(){
    				public void onClick(View v) {
    					Intent i = new Intent();
    					i.setAction(Intent.ACTION_MAIN);
    					i.setComponent(new ComponentName(ai.packageName, ai.name));
    					startActivity(i);
    					QuickNavPanel.this.finish();
    				}
    	    	});
    	    	btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT));
    	    	LinearLayout llTmp = new LinearLayout(this);
    	    	llTmp.setOrientation(LinearLayout.HORIZONTAL);
    	    	llTmp.setHorizontalScrollBarEnabled(false);
    	    	llTmp.setHorizontalScrollBarEnabled(true);
    	    	llTmp.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT));
    	    	llTmp.addView(btn);
    	    	llTmp.setBackgroundColor(android.R.color.background_dark);
    	    	TextView tvBtnTip = new TextView(this);
    	    	tvBtnTip.setOnClickListener(new OnClickListener(){
    				public void onClick(View v) {
    					Intent i = new Intent();
    					i.setAction(Intent.ACTION_MAIN);
    					i.setComponent(new ComponentName(ai.packageName, ai.name));
    					startActivity(i);
    					QuickNavPanel.this.finish();
    				}
    	    	});
    	    	//tvBtnTip.setTextColor(android.R.color.white);
    	    	try {
					tvBtnTip.setText(pm.getApplicationLabel(pm.getApplicationInfo(ai.packageName, PackageManager.GET_META_DATA)));
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
    	    	llTmp.addView(tvBtnTip);
    	    	quickNavPanelList.addView(llTmp);
    		}
    	}
	}
	
	public void addMs2ToolBoxItems(){
		final String packageName = "cn.kyle.ms2toolbox";
    	LinearLayout llTmp = new LinearLayout(this);
    	llTmp.setOrientation(LinearLayout.HORIZONTAL);
    	llTmp.setHorizontalScrollBarEnabled(false);
    	llTmp.setHorizontalScrollBarEnabled(true);
    	llTmp.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT));
    	llTmp.setBackgroundColor(android.R.color.primary_text_dark);
    	
    	//锁屏
		Button btn1 = new Button(this);
     	btn1.setBackgroundDrawable(BitMapUtil.getDrawableByResid(this, R.drawable.lockscreen, 72, 72));
    	btn1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT));
    	btn1.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(QuickNavPanel.this,LockScreen.class);
				startActivity(i);
				QuickNavPanel.this.finish();
			}
    	});
    	
    	//手电
    	Button btn2 = new Button(this);
     	btn2.setBackgroundDrawable(BitMapUtil.getDrawableByResid(this, R.drawable.flashlight, 72, 72));
    	btn2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT));
    	btn2.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(QuickNavPanel.this,FlashLight.class);
				startActivity(i);
				QuickNavPanel.this.finish();
			}
    	});
    	
    	//亮度调节
    	Button btn3 = new Button(this);
    	btn3.setBackgroundDrawable(BitMapUtil.getDrawableByResid(this, R.drawable.lcdbacklight, 72, 72));
    	btn3.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT));
    	btn3.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(QuickNavPanel.this,LcdBackLight.class);
				startActivity(i);
				QuickNavPanel.this.finish();
			}
    	});
    	
    	//电源
    	Button btn4 = new Button(this);
    	//btn4.setBackgroundDrawable(getDrawableByResid(R.drawable.power));
    	btn4.setBackgroundDrawable(BitMapUtil.getDrawableByResid(this, R.drawable.power, 72, 72));
    	btn4.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT));
    	btn4.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(QuickNavPanel.this,Power.class);
				startActivity(i);
				QuickNavPanel.this.finish();
			}
    	});
    	llTmp.addView(btn1);
    	llTmp.addView(btn2);
    	llTmp.addView(btn3);
    	llTmp.addView(btn4);
    	
    	quickNavPanelList.addView(llTmp);
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
