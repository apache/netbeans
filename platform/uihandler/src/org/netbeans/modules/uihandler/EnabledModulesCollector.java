/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.uihandler;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Implementation of deactivator with sends one log with a list
 * of enabled modules.
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.uihandler.api.Deactivated.class)
public class EnabledModulesCollector implements Deactivated {
    private List<ModuleInfo> previouslyEnabled = Collections.emptyList();
    private List<ModuleInfo> previouslyDisabled = Collections.emptyList();
    
    
    /** Creates a new instance of EnabledModulesCollector */
    public EnabledModulesCollector() {
    }

    @Override
    public void deactivated(Logger uiLogger) {
        List<ModuleInfo> enabled = new ArrayList<ModuleInfo>();
        List<ModuleInfo> disabled = new ArrayList<ModuleInfo>();
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (m.isEnabled()) {
                enabled.add(m);
            } else {
                disabled.add(m);
            }
        }

        List<ModuleInfo> newEnabled = new ArrayList<ModuleInfo>(enabled);
        newEnabled.removeAll(previouslyEnabled);
        List<ModuleInfo> newDisabled = new ArrayList<ModuleInfo>(disabled);
        newDisabled.removeAll(previouslyDisabled);
        
        if (!newEnabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.CONFIG, "UI_ENABLED_MODULES");
            String[] enabledNames = getModuleNames(newEnabled);
            rec.setParameters(enabledNames);
            rec.setLoggerName(uiLogger.getName());
            rec.setResourceBundle(NbBundle.getBundle(EnabledModulesCollector.class));
            rec.setResourceBundleName(EnabledModulesCollector.class.getPackage().getName()+".Bundle");
            uiLogger.log(rec);
        }
        if (!newDisabled.isEmpty()) {
            LogRecord rec = new LogRecord(Level.CONFIG, "UI_DISABLED_MODULES");
            String[] disabledNames = getModuleNames(newDisabled);
            rec.setParameters(disabledNames);
            rec.setLoggerName(uiLogger.getName());
            rec.setResourceBundle(NbBundle.getBundle(EnabledModulesCollector.class));
            rec.setResourceBundleName(EnabledModulesCollector.class.getPackage().getName()+".Bundle");
            uiLogger.log(rec);
        }
        
        previouslyEnabled = enabled;
        previouslyDisabled = disabled;
    }
    
    static String[] getModuleNames(List<ModuleInfo> modules) {
        String[] names = new String[modules.size()];
        int i = 0;
        for (ModuleInfo m : modules) {
            SpecificationVersion specVersion = m.getSpecificationVersion();
            if (specVersion != null) {
                names[i++]   = m.getCodeName() + " [" + specVersion.toString() + "]";
            } else {
                names[i++] = m.getCodeName();
            }
        }
        return names;
    }
    
    private static List<String> getClusterNames() {
        String dirs = System.getProperty("netbeans.dirs");                      // NOI18N
        if (dirs != null) {
            String [] dirsArray = dirs.split(File.pathSeparator);
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < dirsArray.length; i++) {
                File f = new File(dirsArray[i]);
                if (f.exists()){
                    list.add(f.getName());
                }
            }
            return list;
        } else {
            return Collections.<String>emptyList();
        }
    }
    
    static LogRecord getClusterList (Logger logger) {
        LogRecord rec = new LogRecord(Level.INFO, "USG_INSTALLED_CLUSTERS");
        rec.setParameters(getClusterNames().toArray());
        rec.setLoggerName(logger.getName());
        return rec;
    }
    
    static LogRecord getUserInstalledModules(Logger logger) {
        LogRecord rec = new LogRecord(Level.INFO, "USG_USER_INSTALLED_MODULES");
        Set<String> clusterNames = new HashSet<String>(getClusterNames());
        clusterNames.add("platform");                                           // NOI18N
        List<ModuleInfo> userInstalledModules = new ArrayList<ModuleInfo>();
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            Object showAttr = mi.getAttribute("AutoUpdate-Show-In-Client");     // NOI18N
            if (!(showAttr instanceof String) ||
                !Boolean.parseBoolean((String) showAttr)) {
                
                continue;
            }
            File moduleJarFile = getModuleJarFile(mi);
            if (moduleJarFile == null) {
                continue;
            }
            File moduleParent = moduleJarFile.getParentFile();
            if (moduleParent.getName().equals("modules")) {                     // NOI18N
                String cn = moduleParent.getParentFile().getName();
                if (!clusterNames.contains(cn)) {
                    userInstalledModules.add(mi);
                }
            }
        }
        rec.setParameters(getModuleNames(userInstalledModules));
        rec.setLoggerName(logger.getName());
        return rec;
    }
    
    private static File getModuleJarFile(ModuleInfo mi) {
        try {
            Method getJarFileMethod = mi.getClass().getMethod("getJarFile");    // NOI18N
            getJarFileMethod.setAccessible(true);
            return (File) getJarFileMethod.invoke(mi);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
