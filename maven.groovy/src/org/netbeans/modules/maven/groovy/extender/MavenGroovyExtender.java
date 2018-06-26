/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.groovy.extender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.support.spi.GroovyExtenderImplementation;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(
    service = {
        GroovyExtenderImplementation.class
    },
    projectType = {
        "org-netbeans-modules-maven"
    }
)
public class MavenGroovyExtender implements GroovyExtenderImplementation {

    private final FileObject pom;

    
    public MavenGroovyExtender(Project project) {
        this.pom = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
    }

    @Override
    public boolean isActive() {
        final Boolean[] retValue = new Boolean[1];
        retValue[0] = false;
        try {
            pom.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    Utilities.performPOMModelOperations(pom, Collections.singletonList(new ModelOperation<POMModel>() {

                        @Override
                        public void performOperation(POMModel model) {
                            if (ModelUtils.hasModelDependency(model, MavenConstants.GROOVY_GROUP_ID, MavenConstants.GROOVY_ARTIFACT_ID)) {
                                retValue[0] = true;
                            } else {
                                retValue[0] = false;
                            }
                        }
                    }));
                }
            });
        } catch (IOException ex) {
            return retValue[0];
        }
        return retValue[0];
    }

    @Override
    public boolean activate() {
        try {
            pom.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    List<ModelOperation<POMModel>> operations = new ArrayList<ModelOperation<POMModel>>();
                    operations.add(new AddGroovyDependency());
                    operations.add(new AddMavenCompilerPlugin());
                    operations.add(new AddGroovyEclipseCompiler());
                    Utilities.performPOMModelOperations(pom, operations);
                }
            });
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deactivate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
