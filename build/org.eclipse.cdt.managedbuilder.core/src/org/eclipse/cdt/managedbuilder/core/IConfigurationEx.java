package org.eclipse.cdt.managedbuilder.core;

import java.util.Map;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;

public interface IConfigurationEx extends IConfiguration
{
    Map<String,String> getModuleInfo();
}
