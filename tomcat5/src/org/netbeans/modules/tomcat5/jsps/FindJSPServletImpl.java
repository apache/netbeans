/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
