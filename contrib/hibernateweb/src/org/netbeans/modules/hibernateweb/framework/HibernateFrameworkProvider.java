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
package org.netbeans.modules.hibernateweb.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Adds support for Hibernate framework as one of the available web frameworks
 * for Web projects. 
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateFrameworkProvider extends WebFrameworkProvider {

    public HibernateFrameworkProvider() {
        super(NbBundle.getMessage(HibernateFrameworkProvider.class, "HibernateFramework_Name"), 
                NbBundle.getMessage(HibernateFrameworkProvider.class, "HibernateFramework_Description")); 
    }

    @Override
    public boolean isInWebModule(WebModule wm) {
        if (getDefaultHibernateConfigFiles(wm).size() == 0) {
            // There are no Hibernate configuration files found in this project.
            return false;
        } else {
            return true;
        }
    }

    @Override
    public WebModuleExtender createWebModuleExtender(WebModule wm, ExtenderController controller) {
        // Find out wether WFE needs to be shown in Proj. Customizer or in New Project Wizard.
        // (The following is copied from JSFFrameworkProvider
        boolean forNewProjectWizard = (wm == null || !isInWebModule(wm));
        HibernateWebModuleExtender webModuleExtender = 
                new HibernateWebModuleExtender(forNewProjectWizard, wm, controller);
        return webModuleExtender;
    }

    @Override
    public File[] getConfigurationFiles(WebModule wm) {
        return new File[]{};
    }
    
    private List<FileObject> getDefaultHibernateConfigFiles(WebModule wm) {
        List<FileObject> configFiles = new ArrayList<FileObject>();
        Project enclosingProject = Util.getEnclosingProjectFromWebModule(wm);
        // Check for non supported project or non Hibernate aware web projects.
        if(enclosingProject == null) {
            return configFiles;
        }
        HibernateEnvironment he = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
        // Check for non supported project or non Hibernate aware web projects.
        if(he == null) {
            return configFiles;
        }
        
        configFiles.addAll(he.getDefaultHibernateConfigFileObjects());

        return configFiles;
    }
}
