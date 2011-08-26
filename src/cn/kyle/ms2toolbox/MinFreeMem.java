package cn.kyle.ms2toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.mobclick.android.MobclickAgent;

import cn.kyle.util.C;
import cn.kyle.util.L;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 最小内存管理
 * @author KyleTang
 *
 */
public class MinFreeMem extends Activity {
	private Toast myToast =null;
	public static String defaultValue="3072,4608,9216,10752,12288,12288";
	public static String normalValue="1536,2048,4096,6144,8192,12288";
	public static String fastValue="1536,2048,4096,8192,16384,24576";
	private String manualValue=defaultValue;
	private String currentValue="";
	
	private String sys = "/sys/module/lowmemorykiller/parameters/minfree";
	private String lastapply = null;
	private String manual = null;
	
	private EditText[] etMem = new EditText[7];
	
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
		Event.count(this, Event.MinFreeMem);
		setContentView(R.layout.minfreemem);
		lastapply = this.getFilesDir().getAbsoluteFile()+"/minfree_lastapply";
		manual = this.getFilesDir().getAbsoluteFile()+"/minfree_manual";
		etMem[1] = (EditText)findViewById(R.id.etMem1);
		etMem[2] = (EditText)findViewById(R.id.etMem2);
		etMem[3] = (EditText)findViewById(R.id.etMem3);
		etMem[4] = (EditText)findViewById(R.id.etMem4);
		etMem[5] = (EditText)findViewById(R.id.etMem5);
		etMem[6] = (EditText)findViewById(R.id.etMem6);
		
		Button btnMemDefault = (Button)findViewById(R.id.btnMemDefault);
		btnMemDefault.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				loadValueStr(defaultValue);
			}
		});
		Button btnMemNormal = (Button)findViewById(R.id.btnMemNormal);
		btnMemNormal.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				loadValueStr(normalValue);
			}
		});
		Button btnMemFast = (Button)findViewById(R.id.btnMemFast);
		btnMemFast.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				loadValueStr(fastValue);
			}
		});
		Button btnMemManual = (Button)findViewById(R.id.btnMemManual);
		btnMemManual.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				loadValue(manual);
			}
		});
		Button btnMemSaveDefault = (Button)findViewById(R.id.btnMemSaveDefault);
		btnMemSaveDefault.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				File f = new File(manual);
				if (!f.getParentFile().exists()){
					f.getParentFile().mkdirs();
				}
				C.runSuCommandReturnBoolean("echo "+
					getCurrentValue() +
					" > "+manual);
			}
		});
		Button btnMemApply = (Button)findViewById(R.id.btnMemApply);
		btnMemApply.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				File f = new File(lastapply);
				if (!f.getParentFile().exists()){
					f.getParentFile().mkdirs();
				}
				String value = getCurrentValue();
				C.runSuCommandReturnBoolean("echo "+ value + " > "+sys +" ; "+
						"echo "+ value + " > "+lastapply +" ; "+
						"chmod 666 "+lastapply +" ; ");
				loadValue(sys);
			}
		});
		CheckBox cbMemAutoApply = (CheckBox)findViewById(R.id.cbMemAutoApply);
		cbMemAutoApply.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Module.setPrefFlag(isChecked, getPrefFlagFile(Pref.pMinFreeMem));
			}
		});
		loadValue(sys);
		cbMemAutoApply.setChecked(getPrefFlagFile(Pref.pMinFreeMem).exists());
	}
	
	public String getCurrentValue(){
		int[] n = new int[7];
		try{
			n[1] = Integer.parseInt(etMem[1].getText().toString());
		}catch(Exception e){
			n[1] = 12;
		}
		etMem[1].setText(""+n[1]);
		n[2] = parseInt(etMem[2],n[1]);
		etMem[2].setText(""+n[2]);
		n[3] = parseInt(etMem[3],n[2]);
		etMem[3].setText(""+n[3]);
		n[4] = parseInt(etMem[4],n[3]);
		etMem[4].setText(""+n[4]);
		n[5] = parseInt(etMem[5],n[4]);
		etMem[5].setText(""+n[5]);
		n[6] = parseInt(etMem[6],n[5]);
		etMem[6].setText(""+n[6]);
		String a ="";
		for (int i=1;i<6;i++){
			a=a+n[i]*256+",";
		}
		a=a+n[6]*256;
		return a;
	}
	
	public void loadValue(String file){
		
		String value = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			value = br.readLine();
			br.close();
		} catch (FileNotFoundException e) {
			value = defaultValue;
			e.printStackTrace();
		} catch (IOException e) {
			value = defaultValue;
			e.printStackTrace();
		}
		loadValueStr(value);
	}
	
	public void loadValueStr(String value){
		String n[] = value.split(",");
		for (int i=0;i<6;i++){
			etMem[i+1].setText(""+(Integer.parseInt(n[i])/256));
		}
	}
	
	public int parseInt(EditText etMem , int defaultValue ){
		try{
			int n = Integer.parseInt(etMem.getText().toString());
			return n<defaultValue?defaultValue:n;
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public File getPrefFlagFile(Pref p){
		return new File(this.getFilesDir(),p.toString()+".flag");
	}
}
