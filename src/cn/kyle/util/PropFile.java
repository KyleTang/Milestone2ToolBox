package cn.kyle.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class PropFile {
	public static class DataLine{
		private String value;
		
		DataLine(String value){
			this.value = value;
		}
		
		public String getValue(){
			return value;
		}
		
		public void setValue(String value){
			this.value = value;
		}
	}
	
	public static enum FindMode{
		StartsWith,
		EndsWith,
		Equals,
		EqualsIgnoreCase
	}
		
	public static enum ReplaceMode{
		Matched, /*只替换匹配部分*/
		Line /*替换整行*/
	}
	
	private LinkedList<DataLine> lines = null;
	private File propFile = null;
	
	/**
	 * 替换prop文件中的某一行
	 * @param propFile 原文件
	 * @param newPropFile 新文件，当为null时，表示保存为原文件
	 * @param pattern 匹配字符串
	 * @param replacement 替换字符串
	 * @param findMode 查找模式，四种
	 * @param replaceMode 替换模式，两种
	 * @param replaceAll 是否全文替换
	 * @param trimLine 对文件中的字符串做trim处理
	 * @return
	 */
	public static boolean replacePropLine(String propFile,String newPropFile,
			String pattern, String replacement,
			FindMode findMode, ReplaceMode replaceMode, boolean replaceAll, boolean trimLine){
		PropFile p = new PropFile();
		if (p.load(propFile)
				&& p.replaceLine(pattern, replacement, findMode, replaceMode, replaceAll, trimLine)
			){
			return newPropFile==null?p.save():p.saveAs(newPropFile);
		}else{
			return false;
		}
	}
	
	/**
	 * 替换某一行
	 * @param pattern 匹配字符串
	 * @param replacement 替换字符串
	 * @param findMode 查找模式，四种
	 * @param replaceMode 替换模式，两种
	 * @param replaceAll 是否全文替换
	 * @param trimLine 对文件中的字符串做trim处理
	 * @return
	 */
	public boolean replaceLine(String pattern, String replacement, 
			FindMode findMode, ReplaceMode replaceMode, boolean replaceAll, boolean trimLine){
		Iterator<DataLine> itr = lines.iterator();
		DataLine patternTemp = null ;
		String patternStr = null ;
		
		boolean replaceFlag = false;
		while(itr.hasNext()){
			patternTemp = itr.next();
			patternStr = patternTemp.getValue();
			if (trimLine){
				patternStr = patternTemp.getValue().trim();
			}
			
			if (findMode==FindMode.StartsWith && patternStr.startsWith(pattern)){
				if (replaceMode == ReplaceMode.Matched){
					patternTemp.setValue(patternTemp.getValue().replace(pattern, replacement));
					replaceFlag = true;
				}else if (replaceMode == ReplaceMode.Line){
					patternTemp.setValue(replacement);
					replaceFlag = true;
				}
			}else if (findMode==FindMode.Equals && patternStr.equals(pattern)){
				patternTemp.setValue(replacement);
				replaceFlag = true;
			}else if (findMode==FindMode.EqualsIgnoreCase && patternStr.equalsIgnoreCase(pattern)){
				patternTemp.setValue(replacement);
				replaceFlag = true;
			}
			if (replaceFlag&&!replaceAll){
				break;
			}
		}
		return replaceFlag;
	}
	
	/**
	 * 移除匹配行
	 * @param pattern 查找字符串
	 * @param findMode 查找模式
	 * @param removeAll 全文移除
	 */
	public void removeLine(String pattern,FindMode findMode,boolean removeAll){
		Iterator<DataLine> itr = lines.iterator();
		DataLine patternTemp = null ;
		String patternStr = null ;
		LinkedList<DataLine> removeLines = new LinkedList<DataLine>();
		boolean replaceFlag = false;
		while(itr.hasNext()){
			patternTemp = itr.next();
			patternStr = patternTemp.getValue();
			
			if (findMode==FindMode.StartsWith && patternStr.startsWith(pattern)){
				removeLines.add(patternTemp);
			}else if (findMode==FindMode.Equals && patternStr.equals(pattern)){
				removeLines.add(patternTemp);
			}else if (findMode==FindMode.EqualsIgnoreCase && patternStr.equalsIgnoreCase(pattern)){
				removeLines.add(patternTemp);
			}
			if (replaceFlag&&!removeAll){
				break;
			}
		}
		lines.removeAll(removeLines);
	}
	
	/**
	 * 检查是否匹配
	 * @param patternLeft 比较字符串，左边
	 * @param pattern 比较字符串，右边
	 * @param findMode 比较模式
	 * @param trimLine 对文件中的字符串做trim处理
	 * @return
	 */
	public boolean match(String patternLeft, String pattern, FindMode findMode, boolean trimLine){
		String patternTemp = patternLeft;
		if (trimLine){
			patternTemp = patternLeft.trim();
		}
		
		if ((findMode==FindMode.StartsWith && patternTemp.startsWith(pattern) )
			|| (findMode==FindMode.EndsWith && patternTemp.endsWith(pattern) )
			|| (findMode==FindMode.Equals  && patternTemp.equals(pattern) )
			|| (findMode==FindMode.EqualsIgnoreCase && patternTemp.equalsIgnoreCase(pattern) )
			){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 在某行后边插入新内容
	 * @param pattern 匹配字符
	 * @param line 带插入内容
	 * @param findMode 匹配模式
	 * @param trimLine 对文件中的字符串做trim处理
	 * @return
	 */
	public boolean insertAfterLine(String pattern, String line, FindMode findMode,boolean trimLine){
		Iterator<DataLine> itr = lines.iterator();
		int idx=0;
		DataLine patternTemp = null ;
		boolean find = false;

		while(itr.hasNext()){
			idx++;
			patternTemp = itr.next();
			if (match(patternTemp.getValue(),pattern,findMode,trimLine)){
				find = true;
				break;
			}
		}
		if (find){
			insertLine(line,idx);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 末尾增加一行
	 * @param line
	 */
	public void appendLine(String line){
		insertLine(line,-1);
	}
	
	/**
	 * 在指定位置插入新内容
	 * @param line
	 * @param idx 位置，当idx=-1或内容的行数，在末尾增加
	 * @return
	 */
	public boolean insertLine(String line, int idx){
		DataLine newLine = new DataLine(line);
		if (idx==-1){
			lines.add(newLine);
		}else{
			if (idx>lines.size()-1 || idx<-1){
				if (idx==lines.size()){
					lines.add(newLine);
				}else{
					return false;
				}
			}else{
				lines.add(idx,newLine);
			}
		}
		return true;
	}

	/**
	 * 获取指定key的value
	 * @param key
	 * @return
	 */
	public String getValue(String key){
		Iterator<DataLine> itr = lines.iterator();
		DataLine patternTemp = null ;
		while(itr.hasNext()){
			patternTemp = itr.next();
			if (patternTemp.getValue().startsWith(key+"=")){
				return patternTemp.getValue().substring(key.length()+1);
			}
		}
		return null;
	}
	
	/**
	 * 设置已经存在的key的value
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setValue(String key, String value){
		return this.replaceLine(key+"=", key+"="+value, FindMode.StartsWith, ReplaceMode.Line, false, true);
	}
	
	/**
	 * 增加一对key-value，如果key已经存在，则覆盖原有的
	 * @param key
	 * @param value
	 * @return
	 */
	public void putValue(String key, String value){
		if (!this.replaceLine(key+"=", key+"="+value, FindMode.StartsWith, ReplaceMode.Line, false, true)){
			this.appendLine(key+"="+value);
		}
	}
	
	/**
	 * 移除一个key，通过key查找
	 * @param key
	 * @param removeAllKey
	 */
	public void removeByKey(String key, boolean removeAll){
		this.removeLine(key+"=", FindMode.StartsWith, removeAll);
	}
	
	/**
	 * 移除一个key，通过key-value查找
	 * @param keyValue
	 * @param removeAllKeyValue
	 */
	public void removeByKeyValue(String keyValue, boolean removeAll){
		this.removeLine(keyValue, FindMode.Equals, removeAll);
	}
	
	/**
	 * 注释一个key-value
	 * @param key
	 * @param commentAll
	 */
	public void commentByKey(String key, boolean commentAll){
		Iterator<DataLine> itr = lines.iterator();
		DataLine patternTemp = null ;
		while(itr.hasNext()){
			patternTemp = itr.next();
			if (patternTemp.getValue().startsWith(key+"=")){
				patternTemp.setValue("#"+patternTemp.getValue());
			}
		}
	}
	
	/**
	 * 加载prop文件
	 * @param propFile
	 * @return
	 */
	public boolean load(String propFile){
		this.lines = new LinkedList<DataLine>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(propFile));
			String line = null;
			this.propFile = new File(propFile);
			try {
				while((line=br.readLine())!=null){
					this.lines.add(new DataLine(line));
				}
				br.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.lines = null;
		return false;
	}
	
	/**
	 * 保存
	 * @return
	 */
	public boolean save(){
		return saveAs(this.propFile.getAbsolutePath());
	}
	
	/**
	 * 另存为
	 * @return
	 */
	public boolean saveAs(String newpath){
		try {
			FileWriter fw = new FileWriter(newpath);
			Iterator<DataLine> itr = lines.iterator();
			DataLine line = null ;
			while (itr.hasNext()){
				line = itr.next();
				fw.write(line.getValue()+"\n");
			}
			fw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args){
		String file1 = "e:/tmp/build.prop";
		String file2 = "e:/tmp/build1.prop";
//		PropFile.replacePropLine(file1, null, 
//				"media.stagefright.enable-http", "media.stagefright.enable-http=AAA", 
//				FindMode.StartsWith, ReplaceMode.Line, false, false);
		PropFile pf = new PropFile();
		if (pf.load(file1)){
			//pf.setValue("media.stagefright.enable-http", "false");
			//pf.removeByKey("gsm.sim.mot.simswap", false);
			//pf.commentByKey("ro.config.ringtone", false);
			pf.putValue("media.stagefright.enable-http1", "BBB");
			pf.save();
		}
		
	}
}
