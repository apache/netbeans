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
package org.netbeans.modules.jakarta.web.beans;

import java.lang.ref.WeakReference;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;


/**
 * @author ads
 *
 */
@ProjectServiceProvider(service={ProjectOpenedHook.class}, projectType = {
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-j2ee-clientproject",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-maven/jar",
    "org-netbeans-modules-maven/war",
    "org-netbeans-modules-maven/ejb",
    "org-netbeans-modules-maven/app-client"}
    )
public class CdiProjectOpenHook extends ProjectOpenedHook {
    
    public CdiProjectOpenHook(Project project){
        myProject = new WeakReference<Project>( project );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.spi.project.ui.ProjectOpenedHook#projectClosed()
     */
    @Override
    protected void projectClosed() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.ui.ProjectOpenedHook#projectOpened()
     */
    @Override
    protected void projectOpened() {
        Project project = myProject.get();
        if ( project == null ){
            return;
        }
        CdiUtil util = project.getLookup().lookup(CdiUtil.class);
        if ( util!= null && util.isCdiEnabled() ){
            util.log("USG_CDI_BEANS_OPENED_PROJECT", CdiProjectOpenHook.class, 
                    new Object[]{project.getClass().getName()});  // NOI18N
        }
    }
    
    private WeakReference<Project> myProject;
}
