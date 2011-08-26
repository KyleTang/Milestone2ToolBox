package cn.kyle.ms2toolbox;

import android.app.Activity;
import android.os.Bundle;

/**
 * （尚未开始，计划）
 * 改进的字体修改工具
 * 支持"英数/英数粗/中文"单独更改
 * 导入字体
 * 保存为配置文件
 * @author KyleTang
 *
 */
public class FontMod extends Activity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fontview);
	}
}
