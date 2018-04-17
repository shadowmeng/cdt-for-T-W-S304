/*******************************************************************************
 * Copyright (c) 2010, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.WorkbenchJob;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Table;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Page to select existing code location and toolchain.
 *
 * @since 7.0
 */
public class NewMakeProjFromExistingPage2 extends WizardPage {

	Text projectName;
	Text location;
	Button langc;
	Button langcpp;
	IWorkspaceRoot root;
	List tcList;
	Map<String, IToolChain> tcMap = new HashMap<String, IToolChain>();
	ArrayList<String> pathList = new ArrayList<String>();
	List projList = null;
	protected Table table;
	protected CheckboxTableViewer tv;
	public static final String EMPTY_STR = "";
	
	/**
	 * True if the user entered a non-empty string in the project name field. In that state, we avoid
	 * automatically filling the project name field with the directory name (last segment of the location) he
	 * has entered.
	 */
	boolean projectNameSetByUser;

	protected NewMakeProjFromExistingPage2() {
		super(Messages.NewMakeProjFromExistingPage_0);
		System.out.println("enter NewMakeProjFromExistingPage\n");
		setTitle(Messages.NewMakeProjFromExistingPage_1);
		setDescription(Messages.NewMakeProjFromExistingPage_2);

		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public void createControl(Composite parent) {
		System.out.println("enter createControl\n");
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		System.out.println("createControl 1\n");
		addProjectNameSelector(comp);
		System.out.println("createControl 2\n");
		addSourceSelector(comp);
		System.out.println("createControl 3\n");
		addLanguageSelector(comp);
		System.out.println("createControl 4\n");
		addProjectSelector(comp);
		System.out.println("createControl 5\n");
		addToolchainSelector(comp);
		System.out.println("createControl 6\n");
		setControl(comp);
		System.out.println("end createControl\n");
	}

	public void addProjectNameSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.NewMakeProjFromExistingPage_3);

		projectName = new Text(group, SWT.BORDER);
		projectName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		projectName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
				if (getProjectName().isEmpty()) {
					projectNameSetByUser = false;
				}
			}
		});
		
		// Note that the modify listener gets called not only when the user enters text but also when we
		// programatically set the field. This listener only gets called when the user modifies the field
		projectName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				projectNameSetByUser = true;
			}
		});
	}
	
	/**
	 * Validates the contents of the page, setting the page error message and Finish button state accordingly
	 * 
	 * @since 8.1
	 */
	protected void validatePage() {
		// Don't generate an error if project name or location is empty, but do disable Finish button.  
		String msg = null;
		boolean complete = true; // ultimately treated as false if msg != null
		
		String name = getProjectName();
		if (name.isEmpty()) {
			complete = false;
		}
		else {
			IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.PROJECT);
			if (!status.isOK()) {
			    msg = status.getMessage();
			}
			else {
				IProject project = root.getProject(name);
				if (project.exists()) {
					msg = Messages.NewMakeProjFromExistingPage_4;

				}
	        }
		}
		if (msg == null) {
			String loc = getLocation();
			if (loc.isEmpty()) {
				complete = false;
			}
			else {
				final File file= new File(loc);
				if (file.isDirectory()) {
					// Ensure we can create files in the directory.
					if (!file.canWrite())
						msg = Messages.NewMakeProjFromExistingPage_DirReadOnlyError;
					// Set the project name to the directory name but not if the user has supplied a name
					// (bugzilla 368987). Use a job to ensure proper sequence of activity, as setting the Text
					// will invoke the listener, which will invoke this method.
					else if (!projectNameSetByUser && !name.equals(file.getName())) {
						WorkbenchJob wjob = new WorkbenchJob("update project name") { //$NON-NLS-1$
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								if (!projectName.isDisposed()) {
									projectName.setText(file.getName());
								}
								return Status.OK_STATUS;
							}
						};
						wjob.setSystem(true);
						wjob.schedule();
					}
				} else {
					msg = Messages.NewMakeProjFromExistingPage_8;
				}
			}
		}

		setErrorMessage(msg);
		setPageComplete((msg == null) && complete);
	}

	/** @deprecated Replaced by {@link #validatePage()} */
	@Deprecated
	public void validateProjectName() {
		validatePage();
	}

	public void addSourceSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.NewMakeProjFromExistingPage_5);

		location = new Text(group, SWT.BORDER);
		location.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		location.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		validatePage();

		Button browse = new Button(group, SWT.NONE);
		browse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		browse.setText(Messages.NewMakeProjFromExistingPage_6);
		browse.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(location.getShell());
				dialog.setMessage(Messages.NewMakeProjFromExistingPage_7);
				String dir = dialog.open();
				if (dir != null){
					location.setText(dir);
					traverseFolder1(dir, "Module.mk");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	/** @deprecated Replaced by {@link #validatePage()} */
	@Deprecated
	void validateSource() {
		validatePage();
	}

	public void addLanguageSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.NewMakeProjFromExistingPage_9);

		// TODO, should be a way to dynamically list these
		langc = new Button(group, SWT.CHECK);
		langc.setText("C"); //$NON-NLS-1$
		langc.setSelection(true);

		langcpp = new Button(group, SWT.CHECK);
		langcpp.setText("C++"); //$NON-NLS-1$
		langcpp.setSelection(true);
	}
	
	public void traverseFolder1(String path, String filename) {
        int fileNum = 0, folderNum = 0;
        pathList.clear();
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<File>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    System.out.println("folder:" + file2.getAbsolutePath());
                    list.add(file2);
                    folderNum++;
                } else {
                    System.out.println("file:" + file2.getName());
                    if(filename.equals(file2.getName())){
                   	 System.out.println("add:" + file2.getName());
                   	 pathList.add(file2.getAbsolutePath());
                   }
                    fileNum++;
                }
            }
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("folder:" + file2.getAbsolutePath());
                        list.add(file2);
                        folderNum++;
                    } else {
                        System.out.println("file:" + file2.getName());
                        if(filename.equals(file2.getName())){
                        	 System.out.println("add:" + file2.getName());
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
	
	public void refreshProjectItem()
	{
		//if (null == projList)
		//	return;
		System.out.println("refreshProjectItem 1!");
		//projList.removeAll();
		System.out.println("refreshProjectItem 11!");
		//String []strArray = (String [])pathList.toArray();
		//System.out.println("refreshProjectItem 12!");
		//if(pathList.size() == 0)
		//	projList.add("none");
		//System.out.println("refreshProjectItem 13!");
		//for(int i = 0; i < pathList.size();i++){
		//	String str = pathList.get(i);
		//	projList.add(str);
		//}
	
		System.out.println("refreshProjectItem 3!");
		//if(pathList.size() > 0)
		//	projList.setSelection(0); // select <none>
		Object[] data = new Object[pathList.size()];
		for(int i = 0; i < pathList.size();i++){
			data[i] = pathList.get(i);
		}
		
		tv.setInput(data);
		tv.setAllChecked(false);
	}
	
	public void addProjectSelector(Composite parent){
		System.out.println("addProjectSelector 1\n");
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		System.out.println("addProjectSelector 2\n");
		group.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		group.setLayoutData(gd);
		group.setText("Project");
		System.out.println("addProjectSelector 3\n");
		//projList = new List(group, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.CHECK);
		//GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		//gd.heightHint = projList.getItemHeight() * 5;
		
		//projList.setLayoutData(gd);
		//projList.add("none");
		table = new Table(group, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
		gd.heightHint = table.getItemHeight() * 5;
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//handleBinaryParserChanged();
				//updateButtons();
		}});
		tv = new CheckboxTableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		tv.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				String txt = (element != null) ? element.toString() : EMPTY_STR;
				//if (element instanceof BinaryParserConfiguration)
				//	txt = ((BinaryParserConfiguration)element).getName();
				System.out.println("item:" + txt);
				return txt;
			}
		});

		tv.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent e) {
				//saveChecked();
			}});
	}
	
	public String [] getProjectSelection()
	{
		//if (null != projList)
		//	return projList.getSelection();
		Object[] objs = tv.getCheckedElements();
		String[] strs = null;
		if (objs != null) {
			strs = new String[objs.length];
			for (int i=0; i<objs.length; i++) {
				strs[i] = (String)objs[i];
			}
		}
		return strs;
	}

	public void addToolchainSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText(Messages.NewMakeProjFromExistingPage_10);

		tcList = new List(group, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		// Base the List control size on the number of total toolchains, up to 15 entries, but allocate for no
		// less than five (small list boxes look strange). A vertical scrollbar will appear as needed
		updateTcMap(false);
		gd.heightHint = tcList.getItemHeight() * 5; //(1 + Math.max(Math.min(tcMap.size(), 15), 5)); // +1 for <none> 
		tcList.setLayoutData(gd);
		tcList.add(Messages.NewMakeProjFromExistingPage_11);

		final Button supportedOnly = new Button(group, SWT.CHECK);
		supportedOnly.setSelection(false);
		supportedOnly.setText(Messages.NewMakeProjFromExistingPage_show_only_supported);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		supportedOnly.setLayoutData(gd);
		supportedOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTcWidget(supportedOnly.getSelection());
			}});

		supportedOnly.setSelection(true);
		updateTcWidget(true);
	}		

	/**
	 * Load our map and with the suitable toolchains and then populate the List control
	 * 
	 * @param supportedOnly
	 *            if true, consider only supported toolchains
	 */
	private void updateTcWidget(boolean supportedOnly) {
		updateTcMap(supportedOnly);
		ArrayList<String> names = new ArrayList<String>(tcMap.keySet());
		Collections.sort(names);

		tcList.removeAll();
		tcList.add(Messages.NewMakeProjFromExistingPage_11); // <none>
		for (String name : names)
			tcList.add(name);

		tcList.setSelection(0); // select <none>
	}

	/**
	 * Load our map with the suitable toolchains.
	 * 
	 * @param supportedOnly
	 *            if true, add only toolchains that are available and which support the host platform
	 */
	private void updateTcMap(boolean supportedOnly) {
		tcMap.clear();
		IToolChain[] toolChains = ManagedBuildManager.getRealToolChains();
		for (IToolChain toolChain : toolChains) {
			if (toolChain.isAbstract() || toolChain.isSystemObject())
				continue;
			if (supportedOnly) {
				if (!toolChain.isSupported() || !ManagedBuildManager.isPlatformOk(toolChain)) {
					continue;
				}
			}
			tcMap.put(toolChain.getUniqueRealName(), toolChain);
		}
	}

	public String getProjectName() {
		return projectName.getText().trim();
	}

	public String getLocation() {
		return location.getText().trim();
	}

	public boolean isC() {
		return langc.getSelection();
	}

	public boolean isCPP() {
		return langcpp.getSelection();
	}

	public IToolChain getToolChain() {
		String[] selection = tcList.getSelection();
		return selection.length != 0 ? tcMap.get(selection[0]) : null;
	}

}
