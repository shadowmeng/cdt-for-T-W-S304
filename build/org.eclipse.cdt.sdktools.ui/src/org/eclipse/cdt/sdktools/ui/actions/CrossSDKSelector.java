package org.eclipse.cdt.sdktools.ui.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class CrossSDKSelector extends
WorkbenchWindowControlContribution  {
	
	private Combo mReader;
	ArrayList<String> pathList = new ArrayList<String>();
	ArrayList<String> productList = new ArrayList<String>();
	
	public static String s_CurrentSelection = "";
	
	public final static String PREFIX = "sdk_prefix";
	static Map<String, Map<String, String>> s_sdkList = new HashMap<String, Map<String, String>>();
	
	/**
	 * The constructor.
	 */
	public CrossSDKSelector() {

	}

	public final static String BUILDSERVER = "BUILDSERVER";
	public final static String REMOTEWORKPATH = "REMOTEWORKPATH";
	public final static String MOUNTPATH = "MOUNTPATH";
	public final static String BUILDPATH = "BUILDPATH";
	public final static String PRODUCTPATH = "PRODUCTPATH";
	
	String []KeySet = new String[]{BUILDSERVER, REMOTEWORKPATH, MOUNTPATH, BUILDPATH, PRODUCTPATH};
	static Map<String, String> s_VarSet = new HashMap<String, String>();
	
	static public String GetEclipseConfigValue(String key){
		return s_VarSet.get(key);
	}
	
	private void ReadSDKConf(String filePath){

		List<String> varKey = new ArrayList<String>();
		for(String str : KeySet){
		    varKey.add(str);
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = null;
			Map<String, String> sdkItem = null;
			String sdkKey = null;
			while ((line = br.readLine()) != null ) {
				if (line.length() > 0 && line.charAt(0) != '#'){
					String []pair = line.split(":");
					if(pair.length == 2 ){
						if(pair[0].equals("SDKNAME")){		
							if ( sdkItem != null){
								s_sdkList.put(sdkKey, sdkItem);
							}
							sdkKey = pair[1];
							sdkItem = new HashMap<String, String>();
							mReader.add(sdkKey);
						}
						else if (pair[0].equals("PREFIX")){
							sdkItem.put(PREFIX, pair[1]);
						}
						else if(varKey.contains(pair[0])){
							s_VarSet.put(pair[0], pair[1]);
						}
					}
				}
			}
			if ( sdkItem != null){
				s_sdkList.put(sdkKey, sdkItem);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (s_sdkList.size() > 0){
			mReader.select(0);
			int idx = mReader.getSelectionIndex();
			String item = mReader.getItem(idx);
			s_CurrentSelection = item;
		}
	}
	

	@Override
	protected Control createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite container = new Composite(parent, SWT.NONE);
	    GridLayout glContainer = new GridLayout(1, false);
	    glContainer.marginTop = -1;
	    glContainer.marginHeight = 0;
	    glContainer.marginWidth = 0;
	    container.setLayout(glContainer);
	    GridData glReader = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
	    glReader.widthHint = 200;
	    mReader = new Combo(container, SWT.BORDER | SWT.READ_ONLY
	            | SWT.DROP_DOWN);
	    mReader.setLayoutData(glReader);
	    
	    mReader.addSelectionListener(new SelectionListener(){
	    	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				int idx = mReader.getSelectionIndex();
				String item = mReader.getItem(idx);
				s_CurrentSelection = item;
			}
			
	    });
	   // AddItem();
	   // Map<String,String> abc = null;
	   // CheckProdcutSDK("/home/mengjingshi/remote10/S204/app/device/");
	    String rootPath = System.getenv("HOME");
	    ReadSDKConf(rootPath + "/eclipse.conf");
	    return container;
	}
	
	public static String GetPrefix(){
		Map<String, String> map = s_sdkList.get(s_CurrentSelection);
		return map.get(PREFIX);
	}
	
	@Override
	protected int computeWidth(Control control) {
	    return 100;
	}
}
