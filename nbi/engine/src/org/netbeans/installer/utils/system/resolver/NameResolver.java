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

package org.netbeans.installer.utils.system.resolver;

import java.io.File;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;

/**
 *
 * @author Dmitry Lipin
 */
public class NameResolver implements StringResolver{

    public String resolve(String string, ClassLoader cl) {
        // N for Name
        String parsed = string;
        LogManager.log("NameResolver - to parse " + parsed);
        if (parsed.contains("$N{install}")) {
            try {
                parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{install\\}", 
                        StringUtils.escapeRegExp(SystemUtils.getDefaultApplicationsLocation().getAbsolutePath()));
                LogManager.log("      --- parsed to " + parsed);
            } catch (NativeException e) {                
                ErrorManager.notifyError(ResourceUtils.getString(SystemUtils.class,
                        ERROR_CANNOT_GET_DEFAULT_APPS_LOCATION_KEY), e);
            }
        }   
        if(SystemUtils.isWindows()) {
            File defaultApplicationsLocation = null;
            if (parsed.contains("$N{install_x86}")) {
                try {
                    String path = SystemUtils.getEnvironmentVariable("ProgramFiles(x86)");
                    LogManager.log("      --- Path " + path);
                    if (path != null) {
                        defaultApplicationsLocation = new File(path).getAbsoluteFile();
                    } else {
                        defaultApplicationsLocation = SystemUtils.getDefaultApplicationsLocation();
                    }
                    parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{install_x86\\}",
                            StringUtils.escapeRegExp(defaultApplicationsLocation.getAbsolutePath()));
                    LogManager.log("      --- parsed to " + parsed);
                } catch (NativeException e) {
                    ErrorManager.notifyError(ResourceUtils.getString(SystemUtils.class,
                            ERROR_CANNOT_GET_DEFAULT_APPS_LOCATION_KEY), e);
                }
            }
            if (parsed.contains("$N{install_x64}")) {
                try {
                    String path = SystemUtils.getEnvironmentVariable("ProgramW6432");
                    LogManager.log("      --- Path " + path);
                    if (path != null) {
                        defaultApplicationsLocation = new File(path).getAbsoluteFile();
                    } else {
                        defaultApplicationsLocation = SystemUtils.getDefaultApplicationsLocation();
                    }
                    parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{install_x64\\}",
                            StringUtils.escapeRegExp(defaultApplicationsLocation.getAbsolutePath()));
                    LogManager.log("      --- parsed to " + parsed);
                } catch (NativeException e) {
                    ErrorManager.notifyError(ResourceUtils.getString(SystemUtils.class,
                            ERROR_CANNOT_GET_DEFAULT_APPS_LOCATION_KEY), e);
                }
            }
        }
        if (parsed.contains("$N{home}")) {
            parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{home\\}", 
                    StringUtils.escapeRegExp(SystemUtils.getUserHomeDirectory().getAbsolutePath()));
        }
        if (parsed.contains("$N{temp}")) {
            parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{temp\\}", 
                    StringUtils.escapeRegExp(SystemUtils.getTempDirectory().getAbsolutePath()));
        }
        if (parsed.contains("$N{current}")) {
            parsed = parsed.replaceAll("(?<!\\\\)\\$N\\{current\\}", 
                    StringUtils.escapeRegExp(SystemUtils.getCurrentDirectory().getAbsolutePath()));
        }
        return parsed;
    }
    
    public static final String ERROR_CANNOT_GET_DEFAULT_APPS_LOCATION_KEY =
            "NR.error.cannot.get.default.apps.location";//NOI18N
}
