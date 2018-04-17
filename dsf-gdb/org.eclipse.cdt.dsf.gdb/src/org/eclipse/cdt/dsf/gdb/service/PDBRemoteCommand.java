package org.eclipse.cdt.dsf.gdb.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;

import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.SWT;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.core.runtime.Status;


public class PDBRemoteCommand implements IProcessList{
	
	
	public class ProcessInfo implements IProcessInfo {

		int pid;
		String name;
		
		public ProcessInfo(String pidString, String name) {
			try {
				pid = Integer.parseInt(pidString);
			} catch (NumberFormatException e) {
			}
			this.name = name;
		}
		
		public ProcessInfo(int pid, String name) {
			this.pid = pid;
			this.name = name;
		}
		
		/**
		 * @see org.eclipse.cdt.core.IProcessInfo#getName()
		 */
		public String getName() {
			System.out.println("mjs debug enter ProcessInfo.name:" + name + "\n");
			return name;
		}

		/**
		 * @see org.eclipse.cdt.core.IProcessInfo#getPid()
		 */
		public int getPid() {
			System.out.println("mjs debug enter ProcessInfo.pid:" + pid + "\n");
			return pid;
		}

	}

	protected class AlertConnnectFailed extends UIJob{
	public AlertConnnectFailed(String name){
		super(name);
	}

	public IStatus runInUIThread(IProgressMonitor monitor)
	{
		MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
		messageBox.setText("Connect Error");;
		messageBox.setMessage("Please check pdbd process in target device!");
		messageBox.open();
		return Status.OK_STATUS;
	}

	}
	
	public  JSONObject PostCommandToPDB(String command)
	{
		String readline;
		int fieldLen, pktlen;
		JSONObject retObj = null;
		try {
			System.out.println("mjs debug connect to server");
			Socket socket = new Socket("192.168.1.1", 5000);
			PrintWriter write = new PrintWriter(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			write.print(command);
			write.flush();
			
			fieldLen = 0;
			pktlen = 0;
			while(fieldLen < 4){
				pktlen = pktlen << 8| in.read();
				fieldLen++;
			}
			
			fieldLen = 0;
			byte []buf = new byte[pktlen];
			while(fieldLen < pktlen){
				buf[fieldLen] = (byte)in.read();
				fieldLen++;
			}
			
			String str = new String(buf,"UTF-8");
			System.out.println(str);
			write.close();
			in.close();
			retObj = new JSONObject(str);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//new AlertConnnectFailed("pdbd").schedule();
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//new AlertConnnectFailed("pdbd").schedule();
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retObj;
	}

	public void ListProcess(List<ProcessInfo> listProcess){
		JSONObject obj = PostCommandToPDB("listprocess");
		if (null == obj)
                    return;
		int result = -1;
		try {
			result = obj.getInt("Result");
			if (0 == result){
				JSONArray array = obj.getJSONArray("Content");
				for (int idx = 0; idx < array.length(); idx++){
					JSONObject entry = array.getJSONObject(idx);
					Iterator itor = entry.keys();
					if (itor.hasNext()){
						String key = (String) itor.next();
						String value = entry.getString(key);
						if (value.length() > 0){
							listProcess.add(new ProcessInfo(Integer.parseInt(key), value) );
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public IProcessInfo[] getProcessList(){
		// getProcessList() ;
		// JSONObject obj = PostCommandToPDB("listprocess");
		System.out.println("mjs debug enter PDBRemoteCommand getProcessList\n");
		List<ProcessInfo> list = new ArrayList<ProcessInfo>();
		ListProcess(list);
		System.out.println("mjs debug end call ListProcess\n");
		if (list.size() == 0)
                    return new IProcessInfo[0];
		Object[] temps = list.toArray();
		IProcessInfo[] procInfos = new IProcessInfo[temps.length];
		for (int i = 0; i < temps.length; i++){
			procInfos[i] = (IProcessInfo) temps[i];
		}
		
		return procInfos;
	}

	public void attachToRemoteProcess(String pid){
		pid = pid.replace('\r', '\0');
		pid = pid.replace('\n', '\0');
		JSONObject obj = PostCommandToPDB("attachprocess" + pid);
	}
	
}
