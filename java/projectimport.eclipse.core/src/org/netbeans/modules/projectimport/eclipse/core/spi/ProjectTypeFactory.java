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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;

/**
 * NetBeans project type factory.
 */
public interface ProjectTypeFactory {

    /* Preferably should be moved and handled in web module but it would
     * require too much SPI so for now core will handle this and resolve
     * org.eclipse.jst.j2ee.internal.web.container container.
     */
    String FILE_LOCATION_TOKEN_WEBINF = "webinf"; // NOI18N
    
    /**
     * Returns true if this factory understands given eclipse natures and can
     * create corresponding NetBeans project for it.
     */
    boolean canHandle(ProjectDescriptor descriptor);
    
    /**
     * Create NetBeans project for given eclipse data. Add any problems to 
     * importProblems list.
     * 
     * <p>Always called under project write mutex.
     */
    Project createProject(ProjectImportModel model, List<String> importProblems) throws IOException;
    
    /**
     * Returns project type icon.
     */
    Icon getProjectTypeIcon();

    /**
     * Returns project type name.
     */
    String getProjectTypeName();
    
    /**
     * Provide additional wizard panels to import wizard. The panels
     * provided by a factory are shown only if a project(s) selected in wizard for 
     * import is going to be imported via that factory.
     * Panels can ask for additional global import data; for example
     * web project may ask to choose a J2EE application server. At the moment no data
     * about are passed to wizard panels; can be changed if needed.
     * 
     * <p>Wizard panel instances with initialized data are available to 
     * ProjectTypeFactory via {@link ProjectImportModel#getExtraWizardPanels}.
     * @return never null; can be empty array
     */
    List<WizardDescriptor.Panel<WizardDescriptor>> getAdditionalImportWizardPanels();
    
    /**
     * Return location of a file identified by the token in the given project.
     * Used for resolving or replacing of Eclipse classpath containers.
     * @param token textual representation of some area within a project; could
     *  be for example 'sources', 'tests', etc.; at the moment only 
     *  FILE_LOCATION_TOKEN_WEBINF is defined and required to be provided if 
     *  appropriate;
     */
    File getProjectFileLocation(ProjectDescriptor descriptor, String token);

    /**
     * Eclipse project descriptor.
     */
    public static final class ProjectDescriptor {
        
        private Set<String> natures;
        private Facets facets;
        private File eclipseProject;

        public ProjectDescriptor(File eclipseProject, Set<String> natures, Facets facets) {
            this.natures = natures;
            this.facets = facets;
            this.eclipseProject = eclipseProject;
        }

        public Facets getFacets() {
            return facets;
        }

        public Set<String> getNatures() {
            return natures;
        }

        public File getEclipseProjectFolder() {
            return eclipseProject;
        }
        
        @Override
        public String toString() {
            return "ProjectDescriptor[project="+eclipseProject+", natures="+natures+", facets="+facets+"]"; // NOI18N
        }
        
    }
}
