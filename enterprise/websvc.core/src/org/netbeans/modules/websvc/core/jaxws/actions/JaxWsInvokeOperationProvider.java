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

package org.netbeans.modules.websvc.core.jaxws.actions;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.support.InvokeOperationActionProvider;
import org.netbeans.modules.websvc.api.support.InvokeOperationCookie;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.ProjectInfo;
import org.openide.filesystems.FileObject;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.support.InvokeOperationActionProvider.class)
public class JaxWsInvokeOperationProvider implements InvokeOperationActionProvider {
	public InvokeOperationCookie getInvokeOperationCookie(FileObject targetSource) {
        if (JAXWSClientSupport.getJaxWsClientSupport(targetSource) != null) {
            Project project = FileOwnerQuery.getOwner(targetSource);
            ProjectInfo projectInfo = new ProjectInfo(project);
            int projectType = projectInfo.getProjectType();
            if ((projectType == ProjectInfo.JSE_PROJECT_TYPE && !isJAXRPCProject(project) && !isJAXWSProject(project))
                    ||(projectType == ProjectInfo.JSE_PROJECT_TYPE && isJAXWSProject(project) && isJaxWsLibraryOnClasspath(targetSource)) ||
                    (ProjectUtil.isJavaEE5orHigher(project) && (projectType == ProjectInfo.WEB_PROJECT_TYPE ||
                    projectType == ProjectInfo.CAR_PROJECT_TYPE || projectType == ProjectInfo.EJB_PROJECT_TYPE))
                    ) {
                return new JaxWsInvokeOperation(targetSource);
            } else if (JaxWsUtils.isEjbJavaEE5orHigher(projectInfo)) {
                return new JaxWsInvokeOperation(targetSource);
            }
            // Tomcat on J2EE14 project Case
            if (projectType == ProjectInfo.WEB_PROJECT_TYPE && !ProjectUtil.isJavaEE5orHigher(project) && isJaxWsLibraryOnRuntimeClasspath(targetSource)) {
                return new JaxWsInvokeOperation(targetSource);
            }
        }
        return null;
    }
        
    private boolean isJaxWsLibraryOnRuntimeClasspath(FileObject targetSource){
        ClassPath classPath = ClassPath.getClassPath(targetSource,ClassPath.EXECUTE);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null &&
                     classPath.findResource("javax/xml/rpc/Service.class") == null ) {
                return true;
            }
        }
        return false;
    } 
    
    private boolean isJAXRPCProject(Project project){
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath;
        FileObject wscompileFO = null;
        if (sgs.length > 0) {
            classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
            if (classPath != null) {
                wscompileFO = classPath.findResource("com/sun/xml/rpc/tools/ant/Wscompile.class"); //NOI18N
            }
        }
        return wscompileFO != null;
    }
    
    private boolean isJAXWSProject(Project project){
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath;
        FileObject wsimportFO = null;
        if (sgs.length > 0) {
            classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
            if (classPath != null) {
                wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); //NOI18N
            }
        }
        return wsimportFO != null;
    }
    
    private boolean isJaxWsLibraryOnClasspath(FileObject targetSource) {
        //test on javax.xml.ws.Service.class
        // checking COMPILE classpath
        ClassPath classPath = ClassPath.getClassPath(targetSource,ClassPath.COMPILE);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null) return true;
        }
        //checking BOOT classpath
        classPath = ClassPath.getClassPath(targetSource,ClassPath.BOOT);
        if (classPath != null) {
            if (classPath.findResource("javax/xml/ws/Service.class")!=null) return true;
        }
        return false;
    }

}
