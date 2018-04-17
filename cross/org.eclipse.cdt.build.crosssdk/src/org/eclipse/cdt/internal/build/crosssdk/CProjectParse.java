package org.eclipse.cdt.internal.build.crosssdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CProjectParse {
	
	private Map<String, Map<String, String>> s_archParamPair = new HashMap<String, Map<String, String>>();
	
	static String PLATFORM_MAKE = "make";
	static String PLATORM_ARCH_PATH = "/core/arch/";
	static String TOOLCHAIN_PREFIX = "CROSS_COMPILE";
	String m_strArchPath = null;

	public void parse(String path) {
		Map<String, String> s_addPair = new HashMap<String, String>();
		Map<String, String> s_equalPair = new HashMap<String, String>();
		try {
			File filePath = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = null;
			while ((line = br.readLine()) != null ) {
				
				if (line.length() > 0 && line.charAt(0) == '#') continue;
				String []pair = line.split("\\+=");
				if (pair.length == 2) {
					if (s_addPair.containsKey(pair[0].trim())) {
						String value = s_addPair.get(pair[0].trim()) + " " + pair[1].trim();
						s_addPair.put(pair[0].trim(), value);
					}
					else {
						s_addPair.put(pair[0].trim(), pair[1].trim());
					}
					continue;
				}
				
				pair = line.split("\\:=");
				if (pair.length == 2) {
					s_equalPair.put(pair[0].trim(), pair[1].trim());
					continue;
				}
				
				pair = line.split("=");
				if (pair.length == 2) {
					s_equalPair.put(pair[0].trim(), pair[1].trim());
					continue;
				}
			}
			br.close();
			
			for(String key : s_equalPair.keySet()){
				if (s_addPair.containsKey(key)){
					s_equalPair.put(key, s_equalPair.get(key) + " " + s_addPair.get(key));
					s_addPair.remove(key);
				}
			}
			
			for(String key : s_addPair.keySet()){
				s_equalPair.put(key, s_addPair.get(key));
			}
				
			Pattern p = Pattern.compile("(\\$\\([a-zA-Z_][a-zA-Z_0-9]*\\))");
			for(String key : s_equalPair.keySet()){
				String content = s_equalPair.get(key);
				String []values = content.split(" ");
				for (String value : values){
					Matcher m = p.matcher(value);
					while(m.find()){
						String var = m.group(1);
						var = var.trim();
						var = var.substring(2, var.length() - 1);
						if (s_equalPair.containsKey(var)){
							String realValue = s_equalPair.get(var);
							System.out.println(realValue);
							System.out.println(var);
							String srcValue = m.group(1);
							//srcValue = convertFromPattern(srcValue);
							System.out.println(srcValue);
							content = content.replace(srcValue, realValue);
							System.out.println(content);
						}
						//content.replace(m.group(1), )
					}
				}
				System.out.println(content);
				s_equalPair.put(key, content);
			}
			
			s_archParamPair.put(filePath.getName(), s_equalPair);
			
			String str = null;
			if (s_equalPair.containsKey("CFLAGS")){
				str = s_equalPair.get("CFLAGS");
				System.out.println("cflags:" + str);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	String getPlatformRoot(String strProjectPath)
	{
		File currentFile = null;
		File projRoot = new File(strProjectPath);
		currentFile = projRoot;
		while(null != currentFile && currentFile.exists())
		{
			File []filelist = currentFile.listFiles();
			for (File file : filelist){
				if(file.getName().equals(PLATFORM_MAKE)){
					File archPath = new File(file.getAbsoluteFile() + PLATORM_ARCH_PATH);
					if (archPath.exists())
						return file.getAbsolutePath();
				}
			}
			currentFile = currentFile.getParentFile();
		}
		return null;
	}
	
	public void ParseArchFile(String strProjRoot){
		String strPath = getPlatformRoot(strProjRoot);
		if (strPath != null) {
			 m_strArchPath = strPath + PLATORM_ARCH_PATH;
			 File archPath = new File(m_strArchPath);
			 File[] filelist = archPath.listFiles();
			 for(File file : filelist){
				 parse(file.getAbsolutePath());
			 }
		}
	}

	public String getArchPath() {
		return m_strArchPath;
	}
	
	String getCflags(String arch){
		if(s_archParamPair.containsKey(arch)){
			return s_archParamPair.get(arch).get("CFLAGS");
		}
		return null;
	}
	
	String getTooChainPrefix(String arch){
		if(s_archParamPair.containsKey(arch)){
			return s_archParamPair.get(arch).get("CROSS_COMPILE");
		}
		return null;
	}
}
