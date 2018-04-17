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

import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspace;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;

public class CrossGCCBuildCommandParser extends GCCBuildCommandParser {
	
	/**
	 * "foo"
	 * Using look-ahead and look-behind to resolve ambiguity with "\" {@link #QUOTE_BSLASH_QUOTE}
	 */
	private static final String QUOTE = "(\"(?!\\\\).*?(?<!\\\\)\")"; //$NON-NLS-1$
	/** \"foo\" */
	private static final String BSLASH_QUOTE = "(\\\\\".*?\\\\\")"; //$NON-NLS-1$
	/** 'foo' */
	private static final String SINGLE_QUOTE = "('.*?')"; //$NON-NLS-1$
	/** "\"foo\"" */
	private static final String QUOTE_BSLASH_QUOTE = "(\"\\\\\".*?\\\\\"\")"; //$NON-NLS-1$

	private static final Pattern OPTIONS_PATTERN = Pattern.compile("-[^\\s\"'\\\\]*(\\s*(" + QUOTE +"|" + QUOTE_BSLASH_QUOTE + "|" + BSLASH_QUOTE + "|" + SINGLE_QUOTE + "|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$<
	private static final int OPTION_GROUP = 0;

	protected List<ICLanguageSettingEntry> detectedSettingEntries = null;
	@Override
	public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker) throws CoreException {
		super.startup(cfgDescription, cwdTracker);
		detectedSettingEntries = new ArrayList<ICLanguageSettingEntry>();
	}

	@Override
	public void shutdown() {
		// release resources for garbage collector
		// but keep currentCfgDescription for AbstractBuiltinSpecsDetector flow
		if (detectedSettingEntries != null && detectedSettingEntries.size() > 0) {
			//collected = detectedSettingEntries.size();
			setSettingEntries(currentCfgDescription, currentResource, currentLanguageId, detectedSettingEntries);
		}
		detectedSettingEntries = null;

		super.shutdown();
	}
	
	@Override
	protected void setSettingEntries(List<? extends ICLanguageSettingEntry> entries) {
		// Built-in specs detectors collect entries not per line but for the whole output
		// so collect them to save later when output finishes
		if (entries != null) {
			for (ICLanguageSettingEntry entry : entries) {
				if (detectedSettingEntries != null && !detectedSettingEntries.contains(entry)) {
					detectedSettingEntries.add(entry);
				}
			}
		}
	}

	@Override
	protected List<String> parseOptions(String line) {
		System.out.println("enter CrossGCCBuildCommandParser:" + line);
		List<String> options = new ArrayList<String>();
		if (line != null && line.length() > 0){
		Matcher optionMatcher = OPTIONS_PATTERN.matcher(line);
		while (optionMatcher.find()) {
			String option = optionMatcher.group(OPTION_GROUP);
			if (option!=null) {
				options.add(option);
			}
		}
		}
		return options;
	}
}
