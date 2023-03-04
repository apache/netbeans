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

package org.netbeans.modules.xml.jaxb.model;

import java.math.BigDecimal;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Mutex;

/**
 *
 * @author gpatil
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType={
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject"
})
public class JAXBWizProjectOpenedHookImpl extends ProjectOpenedHook{
    private Project prj;
    
    public JAXBWizProjectOpenedHookImpl(Project project){
        this.prj = project;
    }
    
    @Override
    protected void projectClosed() {
    }

    @Override
    protected void projectOpened() {
        Schemas scs = ProjectHelper.getXMLBindingSchemas(prj);
        if (scs != null && scs.sizeSchema() > 0){
            BigDecimal v = scs.getVersion();
            if ((v == null) || (JAXBWizModuleConstants.LATEST_CFG_VERSION.compareTo(v) > 0)){
                ProjectHelper.migrateProjectFromPreDot5Version(prj);
            }
            // Set endorsed classpath
            ProjectManager.mutex().writeAccess(
                    new Mutex.Action() {
                        public Object run() {
                            try {
                                ProjectHelper.addJaxbApiEndorsed(prj);
                            } catch (java.io.IOException ex) {
                                ex.printStackTrace();
                            }
                            return null;
                        }
                    });

        }
    }
}
