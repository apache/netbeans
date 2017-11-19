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

package org.netbeans.modules.hibernate.service.listener;

import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 * Class that listens for (global) project open/close operations and 
 * registeres or un-registeres Hibernate specific artifacts.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class ProjectOpenedHookImpl extends ProjectOpenedHook{

    private Project project;
    private HibernateEnvironment hibernateEnvironment;
    
    private Logger logger = Logger.getLogger(ProjectOpenedHookImpl.class.getName());
    
    public ProjectOpenedHookImpl(Project project, HibernateEnvironment hibernateEnvironment) {
        this.project = project;
        this.hibernateEnvironment = hibernateEnvironment;
    }

    
    @Override
    protected void projectOpened() {
        // Check for Hibernate files in this project.
        List<HibernateConfiguration> hibernateConfigurations = HibernateUtil.getAllHibernateConfigurations(project);
        if(hibernateConfigurations.size() != 0) {
            hibernateEnvironment = project.getLookup().lookup(HibernateEnvironment.class);
        }
        logger.info("project opened .. " + project);
        logger.info("config : " + project.getLookup().lookup(HibernateEnvironment.class).getAllHibernateConfigurationsFromProject());
        

//        // Three cases exists..
//        //1. This web project do not have hibernate files. fine.. NOP
//        //2. This web project already has hibernate files.. search and find them.
//        //3. The web project already has hibernate files and its lookup has the ojb..
//        // Does this third case occur? I think no.
        // this one I need to take care of it..
    }

    @Override
    protected void projectClosed() {
        //TODO clean up here.
    }

}
