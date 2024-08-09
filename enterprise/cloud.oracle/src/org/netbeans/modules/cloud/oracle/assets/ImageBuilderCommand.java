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
package org.netbeans.modules.cloud.oracle.assets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryNode;
import org.netbeans.modules.cloud.oracle.steps.ProjectStep;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.lsp.CommandProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = CommandProvider.class)
public class ImageBuilderCommand implements CommandProvider {

    private static final Logger LOG = Logger.getLogger(ImageBuilderCommand.class.getName());

    private static final String COMMAND_BUILD_PUSH_IMAGE = "nbls.cloud.assets.buildPushImage"; //NOI18N

    private static final RequestProcessor RP = new RequestProcessor("PoliciesCommand"); //NOI18N

    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_BUILD_PUSH_IMAGE
    ));

    @Override
    public Set<String> getCommands() {
        return Collections.unmodifiableSet(COMMANDS);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        CompletableFuture result = new CompletableFuture();
        Steps.getDefault()
                .executeMultistep(new ProjectStep(), Lookup.EMPTY)
                .thenAccept(values -> {
                    try {
                        Project project = values.getValueForStep(ProjectStep.class);
                        ProjectManager.Result r = ProjectManager.getDefault().isProject2(project.getProjectDirectory());

                        ContainerRepositoryItem repository = CloudAssets.getDefault().getItem(ContainerRepositoryItem.class);
                        if (repository == null) {
                            result.cancel(true);
                            return;
                        }
                        Project ociProject = null;
                        SubprojectProvider subprojectProvider = project.getLookup().lookup(SubprojectProvider.class);
                        if (subprojectProvider != null) {
                            Set<? extends Project> subprojects = subprojectProvider.getSubprojects();
                            for (Project subproject : subprojects) {
                                if ("oci".equals(subproject.getProjectDirectory().getName())) {
                                    ociProject = subproject;
                                    break;
                                }
                            }
                        }
                        if (r != null && "org-netbeans-modules-gradle".equals(r.getProjectType())) { //NOI18N
                            Path tempFile = Files.createTempFile("init", ".gradle");

                            String init = "allprojects {\n" +
                                    "    afterEvaluate {\n" +
                                    "        tasks.matching { it.name == 'dockerBuild' }.configureEach {\n" +
                                    "            images = [\"" + repository.getUrl() + ":$project.version\"]\n" +
                                    "        }\n" +
                                    "    }\n" +
                                    "}";

                            Files.write(tempFile, init.getBytes(StandardCharsets.UTF_8));
                            RunConfig runConfig = RunUtils.createRunConfig(
                                    project,
                                    "",
                                    "Build container image",
                                    Collections.emptySet(),
                                    "--init-script", tempFile.toAbsolutePath().toString(), "dockerBuild", "dockerPush"
                            );
                            ExecutorTask task = RunUtils.executeGradle(runConfig, "");
                            task.waitFinished();
                            refresh();
                            result.complete(null);
                        }
                        if (r != null && "org-netbeans-modules-maven".equals(r.getProjectType())) { //NOI18N
                            NbMavenProject nbMavenProject;
                            final boolean isGdk;
                            if (ociProject != null) {
                                nbMavenProject = ociProject.getLookup().lookup(NbMavenProject.class);
                                isGdk = true;
                            } else {
                                nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
                                isGdk = false;
                            }
                            
                            // Workaround until GCN-4792 is fixed
                            FileObject projectDirectory = FileUtil.toFileObject(nbMavenProject.getMavenProject().getBasedir());
                            FileObject pomFile = projectDirectory.getFileObject("pom.xml");
                            if (pomFile != null) {
                                pomFile.refresh();
                            }
                            
                            nbMavenProject.getFreshProject().thenAccept(mvnProject -> {
                                List<String> goals;
                                String version = mvnProject.getVersion();
                                if (isGdk) {
                                    goals = List.of("compile", "deploy", "-pl", "oci", "-Dpackaging=docker", "-Djib.to.image=" + repository.getUrl()+ ":" + version);
                                } else {
                                    goals = List.of("compile", "deploy", "-Dpackaging=docker", "-Djib.to.image=" + repository.getUrl()+ ":" + version);
                                }
                                org.netbeans.modules.maven.api.execute.RunConfig runConfig = org.netbeans.modules.maven.api.execute.RunUtils.createRunConfig(
                                        FileUtil.toFile(project.getProjectDirectory()),
                                        project,
                                        "Build container image",
                                        goals
                                );
                                ExecutorTask task = org.netbeans.modules.maven.api.execute.RunUtils.executeMaven(runConfig);
                                task.waitFinished();
                                refresh();
                                result.complete(null);
                            });
                        }
                    } catch (IOException ex) {
                        result.completeExceptionally(ex);
                        Exceptions.printStackTrace(ex);
                    }

                });
        return result;
    }
    
    private void refresh() {
        for (Node child : RootNode.instance().getChildren().getNodes()) {
            if (child instanceof ContainerRepositoryNode) {
                ((ContainerRepositoryNode) child).refresh();
            }
        }
    }
    
}
