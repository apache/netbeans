/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tomcat5.jsps;

import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;

/**
 *
 * @author Petr Jiricka
 */
public class FindJSPServletImpl implements FindJSPServlet {

    private static final String WEB_INF_TAGS = "WEB-INF/tags/";
    private static final String META_INF_TAGS = "META-INF/tags/";
    
    private TomcatManager tm;
    
    /** Creates a new instance of FindJSPServletImpl */
    public FindJSPServletImpl(DeploymentManager manager) {
        tm = (TomcatManager)manager;
    }
    
    
    @Override
    public File getServletTempDirectory(String moduleContextPath) {
        File baseDir = tm.getTomcatProperties().getCatalinaDir();
        if ((baseDir == null) || !baseDir.exists()) {
            return null;
        }
        File hostBase = new File(baseDir, "work/Catalina/localhost".replace('/', File.separatorChar));
        File workDir = new File(hostBase, getContextRootString(moduleContextPath));
        //System.out.println("returning servlet root " + workDir);
        return workDir;
    }
    
    private String getContextRootString(String moduleContextPath) {
        String contextRootPath = moduleContextPath;
        if (contextRootPath.startsWith("/")) {
            contextRootPath = contextRootPath.substring(1);
        }
        if (contextRootPath.equals("")) {
            return "_";
        }
        else {
            return contextRootPath;
        }
    }
    
    @Override
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        //String path = module.getWebURL();
        
        /* .tag file support should be added back after Apache code donation.
          we should use jasper JspUtil apis.

        //we expect .tag file; in other case, we expect .jsp file
        String path = getTagHandlerClassName(jspResourcePath);
        if (path != null) //.tag
            path = path.replace('.', '/') + ".java";
        else //.jsp*/
        String path = null;
        String extension = jspResourcePath.substring(jspResourcePath.lastIndexOf("."));
        if (".jsp".equals(extension)) { // NOI18N
            path = getServletPackageName(jspResourcePath).replace('.', '/') + '/' +
                   getServletClassName(jspResourcePath) + ".java";
        }
        return path;
        
        //int lastDot = jspResourcePath.lastIndexOf('.');
        //return jspResourcePath.substring(0, lastDot) + "$jsp.java"; // NOI18N
    }

    // After Apache code donation, should use org.apache.jasper utilities in
    // JspUtil and JspCompilationContext
    public String getServletPackageName(String jspUri) {
        String jspBasePackageName = "org/apache/jsp";//NOI18N
        int iSep = jspUri.lastIndexOf("/");
        String packageName = (iSep > 0) ? jspUri.substring(0, iSep) : "";//NOI18N
        if (packageName.length() == 0) {
            return jspBasePackageName;
        }
        return jspBasePackageName + "/" + packageName.substring(1);//NOI18N

    }

    // After Apache code donation, should use org.apache.jasper utilities in
    // JspUtil and JspCompilationContext
    public String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf("/") + 1;
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
 
    @Override
    public String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return "UTF8"; // NOI18N
    }
    
}
