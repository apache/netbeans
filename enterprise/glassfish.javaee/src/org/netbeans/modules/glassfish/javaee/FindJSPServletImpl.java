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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.TaskState;
import org.netbeans.modules.glassfish.tooling.admin.CommandGetProperty;
import org.netbeans.modules.glassfish.tooling.admin.ResultMap;
import org.netbeans.modules.glassfish.eecommon.api.FindJSPServletHelper;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;

class FindJSPServletImpl implements FindJSPServlet {
    
    private final String domainDir;
    private final String domain;
    private final GlassfishModule commonSupport;

    public FindJSPServletImpl(Hk2DeploymentManager dm, Hk2OptionalFactory aThis) {
        commonSupport = dm.getCommonServerSupport();
        domainDir = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR);
        domain = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR);            
    }

    @Override
    public File getServletTempDirectory(final String moduleContextPath) {
        // todo -- map the moduleContextPath to the name of the conext root or vice versa...
        File retVal = new File(domainDir, "/" + domain + "/generated/jsp/" + moduleContextPath); // NOI18N
        // the straight up war file case
        if (retVal.exists()) {
            return retVal;
        }

        // the war in ear case
        //   Note: this is not a 100% fix, since a user can put a war file into
        //      multiple ear files... this algorithm will not detect the right one
        File t = new File(domainDir, "/" + domain + "/generated/jsp/");
        File[] subdirs = t.listFiles(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

        });
        if (null != subdirs) {
            final List<File> candidates = new ArrayList<File>();
            for (File z : subdirs) {
                z.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory() && f.getAbsolutePath().endsWith(moduleContextPath+"_war")) {
                            candidates.add(f);
                            return true;
                        }
                        return false;
                    }

                });
            }
            if (candidates.size() == 1) {
                return candidates.get(0);
            } else if (candidates.size() > 1) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO,
                        "multiple candidates ({0}) for {1}",
                        new Object[]{candidates.size(), moduleContextPath});
            }
        }

        // the web app with customized context root case
        String remappedMCP = moduleContextPath;
        try {
            ResultMap<String, String> resultMap = CommandGetProperty
                    .getProperties(commonSupport.getInstance(),
                    "applications.application.*.context-root", 60000);
            if (resultMap.getState() == TaskState.COMPLETED) {
                for (Entry<String, String> e : resultMap.getValue().entrySet()) {
                    if (moduleContextPath.equals(e.getValue())) {
                        remappedMCP = e.getKey().replace(
                                "applications.application.", "")
                                .replace(".context-root", "");
                        break;
                    }
                }
            }
        } catch (GlassFishIdeException gfie) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO,
                    "Could not retrieve property from server.", gfie);
        }
        return new File(domainDir, "/" + domain + "/generated/jsp/" + remappedMCP); // NOI18N
    }

    @Override
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        return FindJSPServletHelper.getServletResourcePath(moduleContextPath, jspResourcePath);
    }

    @Override
    public String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return FindJSPServletHelper.getServletEncoding(moduleContextPath, jspResourcePath);
    }

}
