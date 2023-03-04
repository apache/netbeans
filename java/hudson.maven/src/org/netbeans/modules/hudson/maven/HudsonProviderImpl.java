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

package org.netbeans.modules.hudson.maven;

import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.CiManagement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectHudsonProvider.class, position=100)
public class HudsonProviderImpl extends ProjectHudsonProvider {

    private static final String HUDSON_SYSTEM = "hudson"; // NOI18N
    private static final String JENKINS_SYSTEM = "jenkins"; // NOI18N

    private FileObject pom(Project p) {
        if (p.getLookup().lookup(NbMavenProject.class) == null) {
            return null;
        }
        return p.getProjectDirectory().getFileObject("pom.xml"); // NOI18N
    }

    public @Override Association findAssociation(Project p) {
        //reading needs to be done from resolved model..
        NbMavenProject prj = p.getLookup().lookup(NbMavenProject.class);
        if (prj != null) {
            org.apache.maven.model.CiManagement cim = prj.getMavenProject().getCiManagement();
            if (cim != null) {
                String system = cim.getSystem();
                if (HUDSON_SYSTEM.equalsIgnoreCase(system) || JENKINS_SYSTEM.equalsIgnoreCase(system)) {
                    return Association.fromString(cim.getUrl());
                }
            }
            // could listen to NbMavenProject.PROP_PROJECT if change firing is supported
        }
        return null;
    }

    public @Override boolean recordAssociation(Project p, final Association a) {
        FileObject pom = pom(p);
        if (pom == null) {
            return false;
        }
        Utilities.performPOMModelOperations(pom, Collections.<ModelOperation<POMModel>>singletonList(new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                CiManagement cim;
                if (a != null) {
                    cim = model.getFactory().createCiManagement();
                    cim.setSystem(HUDSON_SYSTEM);
                    cim.setUrl(a.toString());
                } else {
                    cim = null;
                }
                model.getProject().setCiManagement(cim);
            }
        }));
        return true; // XXX pPOMMO does not rethrow exceptions or have a return value
    }

}
