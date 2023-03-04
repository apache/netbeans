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
