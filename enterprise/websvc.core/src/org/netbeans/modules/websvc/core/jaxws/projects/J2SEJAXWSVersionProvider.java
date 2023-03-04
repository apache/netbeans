
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
import java.nio.charset.StandardCharsets;
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
                    BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
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
