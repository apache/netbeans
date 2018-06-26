/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.optional;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet2;
import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;

/**
 *
 * @author Petr Hejl
 */
// FIXME user can configure the directory for JSP servlets
public class WLFindJSPServlet implements FindJSPServlet2 {

    private static final Logger LOGGER = Logger.getLogger(WLFindJSPServlet.class.getName());

    private final WLDeploymentManager deploymentManager;

    public WLFindJSPServlet(WLDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public File getServletTempDirectory(String moduleContextPath) {
        // XXX should it be always existing directory ?
        ApplicationDescriptor desc = getApplicationDescriptor(moduleContextPath);
        if (desc == null) {
            return null;
        }
        String domainDir = deploymentManager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        assert domainDir != null;

        // FIXME can we get this via JMX ?
        File tmpPath = new File(new File(domainDir), "servers" + File.separator + desc.getServerName() + File.separator + "tmp" // NOI18N
               + File.separator + "_WL_user" + File.separator + desc.getName()); // NOI18N
        if (tmpPath.exists() && tmpPath.isDirectory()) {
            File[] subDirs = tmpPath.listFiles();
            if (subDirs != null) {
                for (File subdir : subDirs) {
                    // FIXME multiple subdirs - what does that mean
                    File servletDir = new File(subdir, "jsp_servlet"); // NOI18N
                    if (servletDir.exists() && servletDir.isDirectory()) {
                        // FIXME make simpler
                        return servletDir.getParentFile();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        String fixedJspResourcePath = jspResourcePath.startsWith("/") // NOI18N
                ? jspResourcePath.substring(1)
                : jspResourcePath;

        String[] parts = fixedJspResourcePath.split("/"); // NOI18N
        String jspFile = parts[parts.length - 1];

        StringBuilder result = new StringBuilder("jsp_servlet/"); // NOI18N
        for (int i = 0; i < (parts.length - 1); i++) {
            result.append("_").append(parts[i]).append("/"); // NOI18N
        }
        result.append("__"); // NOI18N

        int dotIndex = jspFile.lastIndexOf('.'); // NOI18N
        if (dotIndex < 0) {
            result.append(jspFile).append(".java"); // NOI18N
        } else {
            result.append(jspFile.substring(0, dotIndex)).append(".java"); // NOI18N
        }
        return result.toString();
    }

    @Override
    public String getServletBasePackageName(String moduleContextPath) {
        return "jsp_servlet"; // NOI18N
    }

    @Override
    public String getServletSourcePath(String moduleContextPath, String jspRelativePath) {
        StringBuilder builder = new StringBuilder("jsp_servlet/"); // NOI18N
        String parts[] = jspRelativePath.split("/"); // NOI18N
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.length() > 0 && i < (parts.length - 1)) {
                builder.append("_"); // NOI18N
            }
            builder.append(part);
            if (i < (parts.length - 1)) {
                builder.append("/"); // NOI18N
            }
        }
        return builder.toString();
    } 

    @Override
    public String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return "UTF8"; // NOI18N
    }

    private ApplicationDescriptor getApplicationDescriptor(final String moduleContextPath) {
        WLConnectionSupport support = deploymentManager.getConnectionSupport();
        try {
            return support.executeAction(new WLConnectionSupport.JMXRuntimeAction<ApplicationDescriptor>() {

                @Override
                public ApplicationDescriptor call(MBeanServerConnection con, ObjectName service) throws Exception {
                    ObjectName pattern = new ObjectName(
                            "com.bea:Type=WebAppComponentRuntime,*"); // NOI18N

                    Set<ObjectName> runtimes = con.queryNames(pattern, null);
                    for (ObjectName runtime : runtimes) {
                        String contextRoot = (String) con.getAttribute(runtime, "ContextRoot"); // NOI18N
                        if (moduleContextPath.equals(contextRoot)) {
                            ObjectName application = (ObjectName) con.getAttribute(runtime, "Parent"); // NOI18N
                            if (application != null) {
                                String name = (String) con.getAttribute(application, "ApplicationName"); // NOI18N
                                ObjectName server = (ObjectName) con.getAttribute(application, "Parent"); // NOI18N
                                if (server != null) {
                                    String serverName = (String) con.getAttribute(server, "Name"); // NOI18N
                                    return new ApplicationDescriptor(name, serverName);
                                }
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }

    private static class ApplicationDescriptor {

        private final String name;

        private final String serverName;

        public ApplicationDescriptor(String name, String serverName) {
            this.name = name;
            this.serverName = serverName;
        }

        public String getName() {
            return name;
        }

        public String getServerName() {
            return serverName;
        }
    }
}
