/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun Microsystems, Inc. All
 * Rights Reserved.
 *  
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
