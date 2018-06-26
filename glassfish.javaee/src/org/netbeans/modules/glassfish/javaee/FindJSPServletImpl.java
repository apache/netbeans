/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
    
    final private String domainDir;
    final private String domain;
    final private GlassfishModule commonSupport;

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
