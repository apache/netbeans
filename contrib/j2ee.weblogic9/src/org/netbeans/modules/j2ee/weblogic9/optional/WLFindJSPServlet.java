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
