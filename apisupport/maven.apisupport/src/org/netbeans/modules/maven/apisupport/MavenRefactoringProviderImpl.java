/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.apisupport;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.NbRefactoringContext;
import org.netbeans.modules.apisupport.project.spi.NbRefactoringProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkozeny
 */
@ProjectServiceProvider(service = NbRefactoringProvider.class, projectType = "org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class MavenRefactoringProviderImpl implements NbRefactoringProvider {

    private Project project;

    public MavenRefactoringProviderImpl(Project project) {
        this.project = project;
    }

    @Override
    public List<ProjectFileRefactoring> getProjectFilesRefactoring(final NbRefactoringContext context) {
        List<ProjectFileRefactoring> result = new ArrayList<ProjectFileRefactoring>();

        FileObject fo = project.getProjectDirectory();
        final FileObject pomFileObject = fo.getFileObject("pom.xml");
        ModelSource source = Utilities.createModelSource(pomFileObject);

        final POMModel pomModel = POMModelFactory.getDefault().getModel(source);
        Build build = pomModel.getProject().getBuild();

        if (build != null) {
            Plugin nbmPlugin = PluginBackwardPropertyUtils.findPluginFromBuild(build);
            if (nbmPlugin != null) {
                Configuration config = nbmPlugin.getConfiguration();
                if (config != null) {
                    List<POMExtensibilityElement> configElems = config.getConfigurationElements();
                    POMExtensibilityElement packages = null;
                    for (POMExtensibilityElement elem : configElems) {
                        if ("publicPackages".equals(elem.getQName().getLocalPart())) {
                            packages = elem;
                            break;
                        }
                    }

                    if (packages != null) {
                        List<POMExtensibilityElement> elems = packages.getAnyElements();
                        for (final POMExtensibilityElement elem : elems) {
                            if (elem.getElementText().equals(context.getOldPackagePath())) {
                                result.add(new ProjectFileRefactoring(pomFileObject) {

                                    @Override
                                    public void performChange() {
                                        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

                                            @Override
                                            public void performOperation(POMModel model) {
                                                Build build = model.getProject().getBuild();

                                                if (build != null) {
                                                    Plugin nbmPlugin = PluginBackwardPropertyUtils.findPluginFromBuild(build);
                                                    if (nbmPlugin != null) {
                                                        Configuration config = nbmPlugin.getConfiguration();
                                                        if (config != null) {
                                                            List<POMExtensibilityElement> configElems = config.getConfigurationElements();
                                                            POMExtensibilityElement packages = null;
                                                            for (POMExtensibilityElement elem : configElems) {
                                                                if ("publicPackages".equals(elem.getQName().getLocalPart())) {
                                                                    packages = elem;
                                                                    break;
                                                                }
                                                            }

                                                            if (packages != null) {
                                                                List<POMExtensibilityElement> elems = packages.getAnyElements();
                                                                for (final POMExtensibilityElement elem : elems) {
                                                                    if (elem.getElementText().equals(context.getOldPackagePath())) {
                                                                        elem.setElementText(context.getNewPackagePath());

                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        };
                                        List<ModelOperation<POMModel>> operations = new ArrayList<ModelOperation<POMModel>>();
                                        operations.add(operation);
                                        Utilities.performPOMModelOperations(pomFileObject, operations);
                                    }

                                    @Override

                                    public String getDisplayText() {
                                        return NbBundle.getMessage(MavenRefactoringProviderImpl.class, "TXT_ProjectXmlFileElementValueRename", "publicPackage", context.getOldPackagePath());
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

}
