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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class OverClock extends Activity {
	private Toast myToast =null;
	private EditText[][] edFreqVsel = new EditText[4][2];
	
	protected void onResume() {
		this.ocLoadSystemFreqVsel();
		this.fillCurrentFreqVselIntoEditText();
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
		Event.count(this, Event.OverClock);
		setContentView(R.layout.overclock);
		
		//
		this.edFreqVsel[0][0] = (EditText)findViewById(R.id.etF1);
		this.edFreqVsel[0][1] = (EditText)findViewById(R.id.etV1);
		this.edFreqVsel[1][0] = (EditText)findViewById(R.id.etF2);
		this.edFreqVsel[1][1] = (EditText)findViewById(R.id.etV2);
		this.edFreqVsel[2][0] = (EditText)findViewById(R.id.etF3);
		this.edFreqVsel[2][1] = (EditText)findViewById(R.id.etV3);
		this.edFreqVsel[3][0] = (EditText)findViewById(R.id.etF4);
		this.edFreqVsel[3][1] = (EditText)findViewById(R.id.etV4);
		
		//
		Button btnOcDefault = (Button)findViewById(R.id.btnOcDefault);
		btnOcDefault.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				fillFreqVselIntoEditText(OCM.FV_default);
			}
		});
		Button btnOc1100 = (Button)findViewById(R.id.btnOc1100);
		btnOc1100.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				fillFreqVselIntoEditText(OCM.FV_1100);
			}
		});
		Button btnOc1200 = (Button)findViewById(R.id.btnOc1200);
		btnOc1200.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				fillFreqVselIntoEditText(OCM.FV_1200);
			}
		});
		Button btnOc1300 = (Button)findViewById(R.id.btnOc1300);
		btnOc1300.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				fillFreqVselIntoEditText(OCM.FV_1300);
			}
		});
		
		Button btnOcCurrentSystem = (Button)findViewById(R.id.btnOcCurrentSystem);
		btnOcCurrentSystem.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				ocLoadSystemFreqVsel();
				fillCurrentFreqVselIntoEditText();
			}
		});
		
		CheckBox cbOcAutoApply = (CheckBox)findViewById(R.id.cbOcAutoApply);
		cbOcAutoApply.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				File f = getPrefFlagFile(Pref.pOverClock);
				Module.setPrefFlag(isChecked, f);
			}
		});
		
		Button btnOcTest = (Button)findViewById(R.id.btnOcTest);
		btnOcTest.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Oc
				ocSettingToFreqVselCurrent();
				OCM.ocApplyToSystem();
			}
		});
		
		Button btnOcApply = (Button)findViewById(R.id.btnOcApply);
		btnOcApply.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Oc and save setting
				ocSettingToFreqVselCurrent();
				OCM.ocApplyToSystem();
				OCM.ocSaveFreqVsel(OverClock.this);
			}
		});
	}
	
	public void fillFreqVselIntoEditText(int profile){
		for(int i=0;i<4;i++){
			this.edFreqVsel[i][0].setText(getFreq(profile,i));
			this.edFreqVsel[i][1].setText(getVsel(profile,i)); 
		}
	}
	
	public String getFreq(int profile, int level){
		return ""+OCM.FreqVsel[profile][level][OCM.Freq];
	}
	
	public String getVsel(int profile, int level){
		return ""+OCM.FreqVsel[profile][level][OCM.Vsel];
	}
	
	/**
	 * 将文本框中的设置值保存到FreqVselCurrent
	 */
	public void ocSettingToFreqVselCurrent(){
		for(int i=0;i<4;i++){
			OCM.FreqVselCurrent[i][OCM.Freq] = parseEditTextToInt(this.edFreqVsel[i][OCM.Freq],OCM.FreqVsel[OCM.FV_default][i][OCM.Freq]);
			OCM.FreqVselCurrent[i][OCM.Vsel] = parseEditTextToInt(this.edFreqVsel[i][OCM.Vsel],OCM.FreqVsel[OCM.FV_default][i][OCM.Vsel]);
		}
	}
	
	/**
	 * 从系统中读取配置到FreqVselCurrent
	 */
	public void ocLoadSystemFreqVsel(){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("/proc/overclock/mpu_opps"));
			String line = null;
			int level = 0;
			while((line=br.readLine())!=null){
				String kv[] = line.split(" ");
				//mpu_opps[4] rate=1200000000 opp_id=4 vsel=63 sr_adjust_vsel=63
				if (kv.length>0){
					level = OCM.parseInt(kv[2].substring(7),0)-1;
					OCM.FreqVselCurrent[level][OCM.Freq]=OCM.parseInt(kv[1].substring(5),OCM.FreqVsel[OCM.FV_default][level][OCM.Freq]);
					if (OCM.FreqVselCurrent[level][OCM.Freq]>10000){
						OCM.FreqVselCurrent[level][OCM.Freq] = OCM.FreqVselCurrent[level][OCM.Freq]/1000000;
					}
					OCM.FreqVselCurrent[level][OCM.Vsel]=OCM.parseInt(kv[3].substring(5),OCM.FreqVsel[OCM.FV_default][level][OCM.Vsel]);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if (br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				br = null;
			}
		}
	}
	
	/**
	 * 将FreqVselCurrent填写到EditText
	 */
	public void fillCurrentFreqVselIntoEditText(){
		for(int i=0;i<4;i++){
			this.edFreqVsel[i][0].setText(""+OCM.FreqVselCurrent[i][OCM.Freq]);
			this.edFreqVsel[i][1].setText(""+OCM.FreqVselCurrent[i][OCM.Vsel]); 
		}
	}
	
	public int parseEditTextToInt(EditText et , int defaultValue ){
		try{
			int n = Integer.parseInt(et.getText().toString());
			return n;
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public File getPrefFlagFile(Pref p){
		return new File(this.getFilesDir(),p.toString()+".flag");
	}
	
	public void myToast(String tipInfo) {
		if (myToast == null)
			myToast = new Toast(this);
		myToast.makeText(this, tipInfo, Toast.LENGTH_SHORT).show();
	}
	
}
