package cn.kyle.util;

import android.content.Context;
import android.content.res.Resources;

/**
 * Multi-language tool
 * @author KyleTang
 *
 */
public class MultiLang {
	private Resources res;
	
	public MultiLang(Context context){
		this.res = context.getResources();
	}
	
	/**
	 * 使用id为resid的字符串模板翻译字符串数组（translate）
	 * @param resid
	 * @param values
	 * @return
	 */
	public String t(int resid, String[] values){
		return t((String)res.getText(resid),values);
	}
	
	/**
	 * 使用字符串模板翻译字符串数组（translate）
	 * @param resid
	 * @param values
	 * @return
	 */
	public String t(String template, String[] values){
		if (values!=null){
			for (int i=0;i<values.length;i++){
				template=template.replace("{"+i+"}", values[i]);
			}
		}
		return template;
	}
	
	public static String t(Context context,int resid, String[] values){
		return new MultiLang(context).t(resid, values);
	}
}
