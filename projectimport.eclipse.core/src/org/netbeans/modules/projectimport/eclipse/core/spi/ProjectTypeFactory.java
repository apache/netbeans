/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
