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

package org.netbeans.modules.websvc.core.jaxws.projects;


import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsNode;
import org.netbeans.modules.websvc.project.api.ServiceDescriptor;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.api.WebService.Type;
import org.netbeans.modules.websvc.project.spi.WebServiceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
public class AntJAXWSService implements WebServiceImplementation {
    
    private Service service;
    private Project prj;
    private JaxWsModel jaxWsModel;

    /** Constructor.
     *
     * @param service JaxWsService
     * @param prj project
     */
    public AntJAXWSService(JaxWsModel jaxWsModel, Service service, Project prj) {
        this.jaxWsModel = jaxWsModel;
        this.service = service;
        this.prj = prj;
    }

    public String getIdentifier() {
         return service.getImplementationClass();
    }

    public boolean isServiceProvider() {
        return true;
    }

    public Type getServiceType() {
        return WebService.Type.SOAP;
    }

    public ServiceDescriptor getServiceDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Node createNode() {
        SourceGroup[] srcGroups = ProjectUtils.getSources(prj).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        String implClass = service.getImplementationClass();
        for (SourceGroup srcGroup : srcGroups) {
            FileObject srcRoot = srcGroup.getRootFolder();
            FileObject implClassFo = getImplementationClass(implClass, srcRoot);
            if (implClassFo != null) {
                return new JaxWsNode(jaxWsModel, service, srcRoot, implClassFo);
            }
        }
        return null;
    }

    private FileObject getImplementationClass(String implClass, FileObject srcRoot) {
        if (implClass != null && srcRoot != null) {
            return srcRoot.getFileObject(implClass.replace('.', '/') + ".java"); //NOI18N
        }
        return null;
    }

}
