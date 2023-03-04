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

package org.netbeans.modules.groovy.support.wizard.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.groovy.support.wizard.JUnit;
import org.netbeans.modules.groovy.support.wizard.ProjectTypeStrategy;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Janicek
 */
public class MavenProjectTypeStrategy extends ProjectTypeStrategy {

    private static final String JUNIT_GROUP_ID = "junit";     // NOI18N
    private static final String JUNIT_ARTIFACT_ID = "junit";  // NOI18N
    private final FileObject pom;


    public MavenProjectTypeStrategy(Project project) {
        super(project);
        pom = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
    }

    @Override
    public JUnit findJUnitVersion() {
        performOperation(new FindJUnitDependencyVersion());
        return jUnitVersion;
    }

    @Override
    public void addJUnitLibrary(final JUnit jUnit) {
        performOperation(new AddJUnitDependency(jUnit));
    }

    private void performOperation(final ModelOperation<POMModel> operation) {
        try {
            pom.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Finds out if the src/test/groovy folder exist or not.
     *
     * @param groups source groups
     * @return true if the src/test/groovy folder exists, false otherwise
     */
    @Override
    public boolean existsGroovyTestFolder(List<SourceGroup> groups) {
        return existsFolder(groups, "/test/groovy"); //NOI18N
    }

    /**
     * Finds out if the src/main/groovy folder exist or not.
     *
     * @param groups source groups
     * @return true if the src/main/groovy folder exists, false otherwise
     */
    @Override
    public boolean existsGroovySourceFolder(List<SourceGroup> groups) {
        return existsFolder(groups, "/main/groovy"); //NOI18N
    }

    private boolean existsFolder(List<SourceGroup> groups, String suffix) {
        for (SourceGroup group : groups) {
            if (group.getRootFolder().getPath().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createGroovyTestFolder() {
        FileObject childFolder = createFolder(project.getProjectDirectory(), "src"); // NOI18N
        FileObject testFolder = createFolder(childFolder, "test"); // NOI18N
        createFolder(testFolder, "groovy"); // NOI18N
    }

    @Override
    public void createGroovySourceFolder() {
        FileObject childFolder = createFolder(project.getProjectDirectory(), "src"); // NOI18N
        FileObject testFolder = createFolder(childFolder, "main"); // NOI18N
        createFolder(testFolder, "groovy"); // NOI18N
    }

    /**
     * This method basically just move the src/test/groovy folder to the beginning
     * of the list and leave the rest of the source groups in the same order as
     * it was before.
     *
     * @param groups to regroup
     * @return reordered source groups
     */
    @Override
    public List<SourceGroup> moveTestFolderAsFirst(List<SourceGroup> groups) {
        return moveAsFirst(groups, "/test/groovy").subList(0, 1); //NOI18N, #219766
    }

    /**
     * This method basically just move the src/main/groovy folder to the beginning
     * of the list and leave the rest of the source groups in the same order as
     * it was before.
     *
     * @param groups to regroup
     * @return reordered source groups
     */
    @Override
    public List<SourceGroup> moveSourceFolderAsFirst(List<SourceGroup> groups) {
        return moveAsFirst(groups, "/main/groovy"); //NOI18N
    }

    private class FindJUnitDependencyVersion implements ModelOperation<POMModel> {

        @Override
        public void performOperation(POMModel model) {
            Dependency jUnitDependency = ModelUtils.checkModelDependency(model, JUNIT_GROUP_ID, JUNIT_ARTIFACT_ID, false);
            if (jUnitDependency != null) {
                final String declaredVersion = jUnitDependency.getVersion();

                if (declaredVersion != null) {
                    int indexOfFirstDot = declaredVersion.indexOf("."); //NOI18N
                    if (indexOfFirstDot == -1) {
                        indexOfFirstDot = declaredVersion.length();
                    }
                    String majorVersion = declaredVersion.substring(0, indexOfFirstDot);

                    if (Integer.parseInt(majorVersion) < 4) {
                        MavenProjectTypeStrategy.this.jUnitVersion = JUnit.JUNIT3;
                    } else {
                        MavenProjectTypeStrategy.this.jUnitVersion = JUnit.JUNIT4;
                    }
                }
            }

            if (MavenProjectTypeStrategy.this.jUnitVersion == null) {
                MavenProjectTypeStrategy.this.jUnitVersion = JUnit.NOT_DECLARED;
            }
        }
    }

    private static class AddJUnitDependency implements ModelOperation<POMModel> {

        private JUnit jUnit;


        public AddJUnitDependency(JUnit jUnit) {
            this.jUnit = jUnit;
        }

        @Override
        public void performOperation(final POMModel model) {
            Dependency dependency = model.getFactory().createDependency();
            dependency.setArtifactId(JUNIT_ARTIFACT_ID);
            dependency.setGroupId(JUNIT_GROUP_ID);
            dependency.setVersion(jUnit.getVersion());

            model.getProject().addDependency(dependency);
        }
    }
}
