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

import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.spi.support.AddOperationActionProvider;
import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.support.AddOperationActionProvider.class)
public class JaxWsAddOperationProvider implements AddOperationActionProvider {
    
    public AddOperationCookie getAddOperationCookie(FileObject fileObject) {
        if ( !fileObject.isValid() ){
            return null;
        }
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport(fileObject);
        if (support != null) {
            String packageName = getPackageName(fileObject);
            if (packageName != null) {
                Service service = getService(support, packageName);
                if (service != null && service.getWsdlUrl() == null) return new JaxWsAddOperation(fileObject);
            }
        }
        return null;
    }
        
    private Service getService(JAXWSSupport support, String packageName) {
        List services = support.getServices();
        for (Object service:services) {
            if (packageName.equals(((Service)service).getImplementationClass())) {
                return (Service)service;
            } 
        }
        return null;
    }
    
    private String getPackageName(FileObject fo) {
        Project project = FileOwnerQuery.getOwner(fo);
        SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups!=null) {
            for (SourceGroup group: groups) {
                FileObject rootFolder = group.getRootFolder();
                if (FileUtil.isParentOf(rootFolder, fo)) {
                    String relativePath = FileUtil.getRelativePath(rootFolder, fo).replace('/', '.');
                    return (relativePath.endsWith(".java")? //NOI18N
                        relativePath.substring(0,relativePath.length()-5):
                        relativePath);
                }
            }
        }
        return null;
    }


}
