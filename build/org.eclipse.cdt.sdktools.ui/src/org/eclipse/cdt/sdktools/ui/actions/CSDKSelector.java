package org.eclipse.cdt.sdktools.ui.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

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
public class CSDKSelector extends
WorkbenchWindowControlContribution  {
	
	private Combo mReader;
	ArrayList<String> pathList = new ArrayList<String>();
	ArrayList<String> productList = new ArrayList<String>();
	/**
	 * The constructor.
	 */
	public CSDKSelector() {
	}

	
	public void CheckProdcutSDK(String productPath){
		new Thread(new Runnable(){

			public void traverseFolder1(String path, String filename) {
		        int fileNum = 0, folderNum = 0;
		        pathList.clear();
		        File file = new File(path);
		        if (file.exists()) {
		            LinkedList<File> list = new LinkedList<File>();
		            File[] files = file.listFiles();
		            if (files == null)
	                	return;
		            for (File file2 : files) {
		                if (file2.isDirectory()) {
		                    list.add(file2);
		                    folderNum++;
		                } else {
		                    if(filename.equals(file2.getName())){
		                   	 pathList.add(file2.getAbsolutePath());
		                   }
		                    fileNum++;
		                }
		            }
		            File temp_file;
		            while (!list.isEmpty()) {
		                temp_file = list.removeFirst();
		                files = temp_file.listFiles();
		                if (files == null)
		                	continue;
		                for (File file2 : files) {
		                    if (file2.isDirectory()) {
		                        list.add(file2);
		                        folderNum++;
		                    } else {
		                        if(filename.equals(file2.getName())){
		                        	pathList.add(file2.getAbsolutePath());
		                        }
		                        fileNum++;
		                    }
		                }
		            }
		        } else {
		            System.out.println("file not found!");
		        }
		        //System.out.println("文件夹共有:" + folderNum + ",文件共有:" + fileNum);
		        refreshProjectItem();
		    }
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				traverseFolder1(productPath, "vendorsetup.sh");
			}
			
		}).start();
	}
	
	
	private void readProductDefFile(String filePath){
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = br.readLine()) != null ) {
				if (line.length() > 0 && line.charAt(0) != '#'){
					String []pair = line.split(" ");
					if(pair.length == 2 && pair[0].trim().equals("add_lunch_combo")){
						pair = pair[1].split("\\-");
						if(pair.length == 2)
							productList.add(pair[0]);
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void refreshProjectItem(){
		productList.clear();
		for (int i = 0; i < pathList.size(); i++){
			readProductDefFile(pathList.get(i));
		}
		Display.getDefault().syncExec(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mReader.removeAll();
				for (int i = 0; i < productList.size(); i++){
					mReader.add(productList.get(i));
				}
				if (productList.size() > 0){
					mReader.select(0);
					int idx = mReader.getSelectionIndex();
					String item = mReader.getItem(idx);
					s_CurrentSelection = item;
				}
			}
			
		});
	}
	
	public static String s_CurrentSelection = "";
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
	    CheckProdcutSDK("/home/mengjingshi/remote10/S204/app/device/");
	    return container;
	}
	
	@Override
	protected int computeWidth(Control control) {
	    return 100;
	}
}
