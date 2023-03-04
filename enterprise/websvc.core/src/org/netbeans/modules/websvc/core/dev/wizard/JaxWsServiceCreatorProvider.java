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

package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.modules.websvc.api.support.ServiceCreator;
import org.netbeans.modules.websvc.core.ProjectInfo;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.ServerType;
import org.netbeans.modules.websvc.spi.support.ServiceCreatorProvider;
import org.openide.WizardDescriptor;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;

/**
 *
 * @author Milan Kuchtiak
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.support.ServiceCreatorProvider.class)
public class JaxWsServiceCreatorProvider implements ServiceCreatorProvider {

    public JaxWsServiceCreatorProvider() {
    }
    
    public ServiceCreator getServiceCreator(Project project, WizardDescriptor wiz) {
        if (JAXWSSupport.getJAXWSSupport(project.getProjectDirectory()) != null) {
            ProjectInfo projectInfo = new ProjectInfo(project);
            int projectType = projectInfo.getProjectType();
            if ((projectType == ProjectInfo.JSE_PROJECT_TYPE && Utils.isSourceLevel16orHigher(project)) ||
                    ((ProjectUtil.isJavaEE5orHigher(project) &&
                    (projectType == ProjectInfo.WEB_PROJECT_TYPE || projectType == ProjectInfo.EJB_PROJECT_TYPE)))
                    ) {
                return new JaxWsServiceCreator(projectInfo, wiz, false);
            } else if (JaxWsUtils.isEjbJavaEE5orHigher(projectInfo)) {
                return new JaxWsServiceCreator(projectInfo, wiz, false);
            } else if (!ProjectUtil.isJavaEE5orHigher(project) &&
                       (projectType == ProjectInfo.WEB_PROJECT_TYPE)) {
                       if (!(projectInfo.isJsr109Supported() || projectInfo.isJsr109oldSupported())) {                   
                           boolean addLibraries = !projectInfo.isWsgenSupported() || !projectInfo.isWsimportSupported();
                           return new JaxWsServiceCreator(projectInfo, wiz, addLibraries);
                       } 
                       if (ServerType.JBOSS == projectInfo.getServerType()) {
                           return new JaxWsServiceCreator(projectInfo, wiz, false);
                       }
            }
        }
        return null;
    }
}
