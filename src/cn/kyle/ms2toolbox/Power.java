package cn.kyle.ms2toolbox;

import java.io.File;
import java.io.IOException;

import cn.kyle.util.C;
import cn.kyle.util.L;
import cn.kyle.util.MultiLang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class Power extends Activity {
	
	public MultiLang ml = new MultiLang(this);
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.power);
		
		Button btnPowerOff = (Button)findViewById(R.id.btnPowerOff); 
		btnPowerOff.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doPower(R.id.btnPowerOff);
			}
		});
		
		Button btnRestart = (Button)findViewById(R.id.btnReboot); 
		btnRestart.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doPower(R.id.btnReboot);
			}
		});
		
		Button btnRestartToRecovery = (Button)findViewById(R.id.btnRebootToRecovery); 
		btnRestartToRecovery.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doPower(R.id.btnRebootToRecovery);
			}
		});
		
		Button btnSystemPoweroff = (Button)findViewById(R.id.btnSystemPoweroff); 
		btnSystemPoweroff.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				C.runSuCommandReturnBoolean("sendevent /dev/input/event2 1 107 1 ;" +
				" sleep 1s ; " +
				" sendevent /dev/input/event2 1 107 0 ;");
			}
		});
	}
	
	public void doPower(final int id){
		AlertDialog ad = new AlertDialog.Builder(this)
			.setTitle(R.string.dialog_title_tip)
			.setMessage(id==R.id.btnPowerOff?ml.t(R.string.power_text_powerOff,null):
						id==R.id.btnReboot?ml.t(R.string.power_text_reboot,null):
						id==R.id.btnRebootToRecovery?ml.t(R.string.power_text_rebootToRecovery,null):
							ml.t(R.string.power_text_systemPoweroff,null))
			.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String cmd = "";
					if (id==R.id.btnPowerOff){
						cmd = "busybox poweroff -f;";
					}else if (id==R.id.btnReboot){
						cmd = "reboot;";
					}else if (id==R.id.btnRebootToRecovery){
						cmd = "reboot recovery;";
					}
					C.runSuCommandReturnBoolean(cmd);
				}
			})
			.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//Power.this.finish();
				}
			})
			.create();
		ad.show();
	}
}
