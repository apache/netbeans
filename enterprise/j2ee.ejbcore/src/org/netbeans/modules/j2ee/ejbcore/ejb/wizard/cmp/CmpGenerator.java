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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 *
 * @author Pavel Buzek
 */
public class CmpGenerator implements PersistenceGenerator {

    private CmpFromDbGenerator generator;
    private org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule;
    
    public CmpGenerator() {
    }
    
    public CmpGenerator(Project project) {
        this.ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project)[0];
        try {
            this.generator = new CmpFromDbGenerator(project, ejbModule.getDeploymentDescriptor());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    public void generateBeans(final ProgressPanel progressPanel,
                              RelatedCMPHelper helper, FileObject dbschemaFile,
                              final ProgressContributor handle) throws IOException {
        CmpFromDbGenerator.ProgressNotifier progressNotifier = new CmpFromDbGenerator.ProgressNotifier() {
            public void switchToDeterminate(int workunits) {
                handle.start(workunits);
            }
            public void progress(int workunit) {
                handle.progress(workunit);
            }
            public void progress(String message) {
                handle.progress(message);
                progressPanel.setText(message);
            }
        };
        generator.generateBeans(helper, dbschemaFile, progressNotifier);
    }

    public String generateEntityName(String name) {
        return name;
    }
    
    public Set<FileObject> createdObjects() {
        return Collections.<FileObject>singleton(ejbModule.getDeploymentDescriptor());
    }
    
    public void init(WizardDescriptor wiz) {
        Project project = Templates.getProject(wiz);
        this.ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project)[0];
        try {
            this.generator = new CmpFromDbGenerator(project, ejbModule.getDeploymentDescriptor());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    public void uninit() {
    }

    public String getFQClassName(String tableName) {
        return null;
    }
    
}
