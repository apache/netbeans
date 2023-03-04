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

package org.netbeans.modules.j2ee.jboss4.ide;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;

/**
 *
 * @author Libor Kotouc
 */
public class JBFindJSPServlet implements FindJSPServlet {
    
    JBDeploymentManager dm;
    
    public JBFindJSPServlet(JBDeploymentManager manager) {
        dm = manager;
    }

    public File getServletTempDirectory(String moduleContextPath) {
        InstanceProperties ip = dm.getInstanceProperties();
        String domainPath = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
        File servletRoot = new File(domainPath, "work/jboss.web/localhost".replace('/', File.separatorChar)); // NOI18N
        String contextRootPath = getContextRootPath(moduleContextPath);
        File workDir = new File(servletRoot, contextRootPath);
        return workDir;
    }

    private String getContextRootPath(String moduleContextPath) {
        if (moduleContextPath.startsWith("/")) {
            moduleContextPath = moduleContextPath.substring(1);
        }
        if (moduleContextPath.length() == 0) {
            moduleContextPath = "/";
        }
        
        return moduleContextPath.replace('/', '_');
    }
    
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {

        String path = null;
        
        String extension = jspResourcePath.substring(jspResourcePath.lastIndexOf("."));
        if (".jsp".equals(extension)) { // NOI18N
            String pkgName = getServletPackageName(jspResourcePath);
            String pkgPath = pkgName.replace('.', '/');
            String clzName = getServletClassName(jspResourcePath);
            path = pkgPath + '/' + clzName + ".java"; // NOI18N
        }
        
        return path;
    }

    // After Apache code donation, should use org.apache.jasper utilities in
    // JspUtil and JspCompilationContext
    public String getServletPackageName(String jspUri) {
        String jspBasePackageName = "org/apache/jsp";//NOI18N
        int iSep = jspUri.lastIndexOf('/');
        String packageName = (iSep > 0) ? jspUri.substring(0, iSep) : "";//NOI18N
        if (packageName.length() == 0) {
            return jspBasePackageName;
        }
        return jspBasePackageName + "/" + packageName.substring(1);//NOI18N

    }

    // After Apache code donation, should use org.apache.jasper utilities in
    // JspUtil and JspCompilationContext
    public String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        String className = jspUri.substring(iSep);
        StringBuilder modClassName = new StringBuilder("");//NOI18N
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (c == '.') {
                modClassName.append('_');
            } else {
                modClassName.append(c);
            }
        }
        return modClassName.toString();
    }
      
    public String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return "UTF8"; // NOI18N
    }
    
    
    
}
