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

import org.netbeans.modules.websvc.core.ProjectInfo;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.modules.websvc.core.HandlerCreatorProvider;
import org.openide.WizardDescriptor;


/**
 *
 * @author Milan Kuchtiak
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.core.HandlerCreatorProvider.class)
public class JaxWsHandlerCreatorProvider implements HandlerCreatorProvider {

    public JaxWsHandlerCreatorProvider() {
    }
    
    public HandlerCreator getHandlerCreator(Project project, WizardDescriptor wiz) {
        ProjectInfo projectInfo = new ProjectInfo(project);
        int projectType = projectInfo.getProjectType();
        if ((projectType == ProjectInfo.JSE_PROJECT_TYPE && Utils.isSourceLevel16orHigher(project)) ||
                (projectType == ProjectInfo.JSE_PROJECT_TYPE && "1.5".equals(SourceLevelQuery.getSourceLevel(project.getProjectDirectory()))) || //NOI18N
                (ProjectUtil.isJavaEE5orHigher(project) && (projectType == ProjectInfo.WEB_PROJECT_TYPE
                || projectType == ProjectInfo.CAR_PROJECT_TYPE
                || projectType == ProjectInfo.EJB_PROJECT_TYPE)) || //NOI18N
                (!projectInfo.isJsr109Supported() && projectType == ProjectInfo.WEB_PROJECT_TYPE/* && !projectInfo.isJsr109oldSupported()*/)
                ) {
            return new JaxWsHandlerCreator(wiz);
        }
        return null;
    }

}
