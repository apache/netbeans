
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

/*
 * WebProjectJAXWSVersionProvider.java
 *
 * Created on March 21, 2007, 3:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.javaee.specs.support.api.JaxWsStackSupport;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=JAXWSVersionProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class J2SEJAXWSVersionProvider implements JAXWSVersionProvider{
    
    private Project project;
    /** Creates a new instance of WebProjectJAXWSVersionProvider */
    public J2SEJAXWSVersionProvider(Project project) {
        this.project = project;
    }
    
    public String getJAXWSVersion() {
        String version = "2.1.3"; //NOI18N
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups != null && srcGroups.length > 0) {
            ClassPath classpath = ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.COMPILE);
            FileObject fo = classpath.findResource("com/sun/xml/ws/util/version.properties"); //NOI18N
            if (fo != null) {
                try {
                    InputStream is = fo.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(is, 
                            Charset.forName("UTF-8")));
                    String ln = null;
                    String ver = null;
                    while ((ln=r.readLine()) != null) {
                        String line = ln.trim();
                        if (line.startsWith("major-version=")) { //NOI18N
                            ver = line.substring(14);
                        }
                    }
                    r.close();
                    version = ver;
                } catch (IOException ex) {
                    Logger.getLogger(J2SEJAXWSVersionProvider.class.getName()).log(Level.INFO, 
                            "Failed to detect JKAX-WS version", ex); //NOI18N
                }
            } else {
                WSStack<JaxWs> jdkJaxWsStack = JaxWsStackSupport.getJdkJaxWsStack();
                if (jdkJaxWsStack != null) {
                    return jdkJaxWsStack.getVersion().toString();
                }
            }
        }
        return version;
    }
    
}
