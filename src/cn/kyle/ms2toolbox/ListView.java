package cn.kyle.ms2toolbox;

import java.io.File;
import java.io.IOException;

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

public class ListView extends Activity {
	public static final String LIST_TYPE = "ListType" ;
	public static final int TYPE_BOOTANIMATIONS = 1 ;
	public static final int TYPE_FONTS = 2 ;
	
	public static String defaultPath = "/" ;
	public static String previewTempPath = "temp";
	public static String defaultFile = "/system/media/bootanimation.zip.default";
	public static String tempFile = "/system/media/bootanimation.zip.temp";
	
	public int type = -1;
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview);

		Bundle b = this.getIntent().getExtras();
		if (b!=null){
			type = b.getInt(LIST_TYPE);
		}
		if (type==-1){
			this.finish();
		}
		
		TextView tvTip = (TextView)findViewById(R.id.tvTip);
		final RadioGroup llList = (RadioGroup)findViewById(R.id.llList);
		Button btnPreview = (Button)findViewById(R.id.btnPreview);
		Button btnApply = (Button)findViewById(R.id.btnApply);
		btnApply.setText("  应  用  ");
		Button btnCancel = (Button)findViewById(R.id.btnCancel);
		btnCancel.setText("  取  消  ");
		btnCancel.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				ListView.this.finish();
			}
		});
		
		if (type==TYPE_BOOTANIMATIONS){
			this.setTitle("更改系统启动动画");
			tvTip.setText("/sdcard/ms2toolbox/bootanimations");
			btnPreview.setText("10秒钟预览");
			btnPreview.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					final RadioButton rb = (RadioButton)llList.findViewById(llList.getCheckedRadioButtonId());
					if (rb==null) return ;
					final String path = ((File)rb.getTag()).getAbsolutePath();
					L.debug("path="+path);
					new Thread(){
						public void run(){
							if (!path.equals("/tmp"))
								applyBootAnimation(path,true);
							new Thread(){
								public void run(){
									try {
										L.debug("waiting ");
										Thread.sleep(12*1000);
										L.debug("kill bootanimation, 1st");
										C.runSuCommandReturnBoolean("busybox kill `busybox ps | busybox grep bootanimation | busybox cut -c 1-5` ");
										if (!path.equals("/tmp"))
											applyBootAnimation(previewTempPath,false);
										Thread.sleep(20*1000);
										L.debug("kill bootanimation, 2nd");
										C.runSuCommandReturnBoolean("busybox kill `busybox ps | busybox grep bootanimation | busybox cut -c 1-5` ");
										if (!path.equals("/tmp"))
											applyBootAnimation(previewTempPath,false);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									
								}
							}.start();
							L.debug("running: bootanimation ");
							C.runSuCommandReturnBoolean("/system/bin/bootanimation ;");
						}
					}.start();
				}
			});
			btnApply.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					RadioButton rb = (RadioButton)llList.findViewById(llList.getCheckedRadioButtonId());
					if (rb==null) return ;
					String path = ((File)((RadioButton)rb).getTag()).getAbsolutePath();
					applyBootAnimation(path,false);
					myToast("设置成功");
				}
			});
			
			//
			File dir = new File("/sdcard/ms2toolbox/bootanimations");
			if (dir.exists()&&dir.isDirectory()){
				llList.removeAllViews();
//				RadioButton cb1 = new RadioButton(this);
//				cb1.setTag(new File("/tmp"));
//				cb1.setText("当前设置");
//				llList.addView(cb1);
				RadioButton cb2 = new RadioButton(this);
				cb2.setTag(new File("/"));
				cb2.setText("恢复默认");
				llList.addView(cb2);
				for (File f : dir.listFiles()){
					if (hasZhFileName(f.getName())) continue;
					RadioButton cb = new RadioButton(this);
					cb.setTag(f);
					cb.setText(f.getName());
					llList.addView(cb);
				}
			}else{
				myToast("不存在\n" +
						"/sdcard/ms2toolbox/bootanimations");
				ListView.this.finish();
				
			}
			
		}else if (type==TYPE_FONTS){
			this.setTitle("更改系统显示字体");
			btnPreview.setText("预览选择字体");
			//btnPreview.setVisibility(android.view.View.INVISIBLE);
			tvTip.setText("/sdcard/ms2toolbox/fonts");
			btnPreview.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					//
					RadioButton rb = (RadioButton)llList.findViewById(llList.getCheckedRadioButtonId());
					if (rb==null) return ;
					String path = ((File)((RadioButton)rb).getTag()).getAbsolutePath();
					boolean pathIsDefault = false;
					if (path.equals("/")) {
						path = "/system/fonts.bak";
						pathIsDefault = true;
					}
					if (new File(path+"/DroidSansFallback.ttf").exists())
					{
						Intent i = new Intent(ListView.this,FontView.class);
						i.putExtra("fontpath", path+"/DroidSansFallback.ttf");
						startActivity(i);
					}else{
						myToast("不存在\n"+path+"/DroidSansFallback.ttf");
					}
					
				}
			});
			btnApply.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					RadioButton rb = (RadioButton)llList.findViewById(llList.getCheckedRadioButtonId());
					if (rb==null) return ;
					String path = ((File)((RadioButton)rb).getTag()).getAbsolutePath();
					boolean pathIsDefault = false;
					if (path.equals("/")) {
						path = "/system/fonts.bak";
						pathIsDefault = true;
					}
					C.runSuCommandReturnBoolean(
							C.CmdMountSystemRW+" ; "+
							"[ ! -r /system/fonts.bak ] && " +
							" cp -r /system/fonts /system/fonts.bak ; "+
							"busybox cp -f "+path+"/*"+" /system/fonts/ ; " +
							"chmod 644 /system/fonts/* ; "+
							(pathIsDefault?"busybox rm -rf /system/fonts.bak ;":""));
					myToast("设置成功");
				}
			});
			
			//
			File dir = new File("/sdcard/ms2toolbox/fonts");
			if (dir.exists()&&dir.isDirectory()){
				llList.removeAllViews();
				RadioButton cb1 = new RadioButton(this);
				cb1.setTag(new File("/"));
				cb1.setText("恢复默认");
				llList.addView(cb1);
				for (File f : dir.listFiles()){
					if (hasZhFileName(f.getName())) continue;
					RadioButton cb = new RadioButton(this);
					cb.setTag(f);
					cb.setText(f.getName());
					llList.addView(cb);
				}
			}else{
				myToast("不存在\n" +
						"/sdcard/ms2toolbox/fonts");
				ListView.this.finish();
			}
		}
		//
	}
	
	public void applyBootAnimation(String path, boolean preview){
		if (path.equals(defaultPath)) {
			path = defaultFile;
		}else if (path.equals(previewTempPath)){
			path = tempFile;
		}else {
			path = path + "/bootanimation.zip";
		}
		if (!new File(path).exists()) return ;
		StringBuilder sb = new StringBuilder();
		sb.append(C.CmdMountSystemRW+" ; ");
		//检查备份文件
		sb.append(" [ ! -r "+defaultFile+" ] && " +
				" cp /system/media/bootanimation.zip "+defaultFile+" ; ");
		
		//预览模式
		if(preview){
			sb.append(" cp -f /system/media/bootanimation.zip "+tempFile+" ; ");
		}
		
		//复制文件
		sb.append(" busybox cp -f "+ path+ " /system/media/bootanimation.zip ; ");
		sb.append(" chmod 644 /system/media/bootanimation.zip ; ");
		
		C.runSuCommandReturnBoolean(sb.toString());
	}
	
	Toast myToast = null;
	public void myToast(String tipInfo) {
		if (myToast == null)
			myToast = new Toast(this);
		myToast.makeText(this, tipInfo, Toast.LENGTH_SHORT).show();
	}
	
	private boolean hasZhFileName(String filename){
		for (int i=0;i<filename.length();i++){
			if (filename.charAt(i)>255||filename.charAt(i)<0){
				return true;
			}
			if (filename.charAt(i)==' '){
				return true;
			}
		}
		return false;
	}
}
