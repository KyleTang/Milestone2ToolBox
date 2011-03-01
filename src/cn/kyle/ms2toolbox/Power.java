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
		
		Button btnRestart = (Button)findViewById(R.id.btnRestart); 
		btnRestart.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doPower(R.id.btnRestart);
			}
		});
		
		Button btnRestartToRecovery = (Button)findViewById(R.id.btnRestartToRecovery); 
		btnRestartToRecovery.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				doPower(R.id.btnRestartToRecovery);
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
			.setTitle("提示")
			.setMessage("是否要执行"+
					(id==R.id.btnPowerOff?" 关机":
					id==R.id.btnRestart?" 重启":
					id==R.id.btnRestartToRecovery?" 重启至恢复模式":"系统关机选项"))
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String cmd = "";
					if (id==R.id.btnPowerOff){
						cmd = "busybox poweroff -f;";
					}else if (id==R.id.btnRestart){
						cmd = "reboot;";
					}else if (id==R.id.btnRestartToRecovery){
						cmd = "reboot recovery;";
					}
					C.runSuCommandReturnBoolean(cmd);
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//Power.this.finish();
				}
			})
			.create();
		ad.show();
	}
}
