/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.build.crosssdk;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;

import java.net.URI;
import java.net.URL;
import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.cdt.internal.build.crosssdk.CProjectParse;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.resources.IResource;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import java.util.List;

public class CrossGCCBuiltinSpecsDetector extends GCCBuiltinSpecsDetector implements IMarkerGenerator{

	private static final String GCC_BUILD_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.build.crosssdk.CrossGCCBuildCommandParser"; //$NON-NLS-1$ 
	CProjectParse m_projectParser = null;
	private CProjectParse getProjectParser()
	{
		if ( currentProject != null)
		{
		try{
		    IProjectDescription projDesc = currentProject.getDescription();
		    if ( projDesc != null )
		    {
			System.out.println("mjs debug proj loc=" + projDesc.getLocation().toString());
			if (m_projectParser == null ){
                		m_projectParser = new CProjectParse();
				m_projectParser.ParseArchFile(projDesc.getLocation().toString());
            		}
                    }
		}
		catch(CoreException e)
		{
                }
		}
		return m_projectParser;
            
        }

	@Override
	protected String getCompilerCommand(String languageId) {
		// Include the cross command prefix (tool option) in the ${COMMAND} macro
		// For example: "arch-os-" + "gcc"
		String prefix = ""; //$NON-NLS-1$
		IToolChain toolchain = null;
		if (currentCfgDescription != null) {
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(currentCfgDescription);
			toolchain = cfg != null ? cfg.getToolChain() : null;
			if (toolchain != null) {
				IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.crosssdk.prefix"); //$NON-NLS-1$
				if (option != null) {
					prefix = (String) option.getValue();
				}
			}
		}
		CProjectParse projParse = getProjectParser();
		if(projParse != null)
			prefix = getProjectParser().getTooChainPrefix("target-arm.mk");

		System.out.println("mjs debug prefix command=" + prefix + super.getCompilerCommand(languageId));
		return prefix + super.getCompilerCommand(languageId);
	}
  
        protected int runProgramForLanguage(String languageId, String command, String[] envp, URI workingDirectoryURI, OutputStream consoleOut, OutputStream consoleErr, IProgressMonitor monitor) throws CoreException, IOException {
		super.runProgramForLanguage(languageId, command, envp, workingDirectoryURI, consoleOut, consoleErr, monitor);
		System.out.println("mjs debug enter CrossGCCBuiltinSpecsDetector.runProgramForLanguage\n");
		/*
		CProjectParse projParse = getProjectParser();
		if(projParse != null){
			String str = getProjectParser().getCflags("target-arm.mk");
			processLine(str);
                }*/
/*
		File buildLogFile = new File(getProjectParser().getArchPath() + "/target-arm.mk");
		currentProject.getWorkspace().run(new BuildOptionsParser(currentProject, buildLogFile), 
							ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new 			NullProgressMonitor());*/
		ICProjectDescriptionManager projDescManager = CCorePlugin
					.getDefault().getProjectDescriptionManager();
			ICProjectDescription projDesc = projDescManager
					.getProjectDescription(currentProject,
							false);
		ICConfigurationDescription ccdesc = projDesc
					.getActiveConfiguration();
			CrossGCCBuildCommandParser parser = null;
			if (ccdesc instanceof ILanguageSettingsProvidersKeeper) {
				ILanguageSettingsProvidersKeeper keeper = (ILanguageSettingsProvidersKeeper)ccdesc;
				List<ILanguageSettingsProvider> list = keeper.getLanguageSettingProviders();
				for (ILanguageSettingsProvider p : list) {
					//						System.out.println("language settings provider " + p.getId());
					if (p.getId().equals(GCC_BUILD_OPTIONS_PROVIDER_ID)) {
						parser = (CrossGCCBuildCommandParser)p;
					}
				}
			}
			String str = getProjectParser().getCflags("target-arm.mk");
			ErrorParserManager epm = new ErrorParserManager(currentProject, this, new String[]{"org.eclipse.cdt.core.CWDLocator"}); //$NON-NLS-1$
			// Start up the parser and process lines generated from the .debug_macro section.
			parser.startup(ccdesc, epm);
			System.out.println("mjsdebugstr=" + str);
			String[] lines = str.split(" ");
			for(String line : lines)
				parser.processLine(line.trim());
			parser.shutdown();

		return ICommandLauncher.OK;
	}

	@Override
	public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar)
	{
	}

	@Override
	public void addMarker(ProblemMarkerInfo problemMarkerInfo)
	{
	}
}
