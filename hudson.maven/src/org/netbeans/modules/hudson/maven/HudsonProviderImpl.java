/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
