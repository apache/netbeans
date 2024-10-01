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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.developer.BearerTokenCommand;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryNode;
import org.netbeans.modules.cloud.oracle.steps.ProjectStep;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "ImageTag=Enter an image tag"
})
@ServiceProvider(service = CommandProvider.class)
public class ImageBuilderCommand implements CommandProvider {
    private static final Logger LOG = Logger.getLogger(ImageBuilderCommand.class.getName());

    private static final String COMMAND_BUILD_PUSH_IMAGE = "nbls.cloud.assets.buildPushImage"; //NOI18N
    private static final String COMMAND_BUILD_PUSH_NATIVE_IMAGE = "nbls.cloud.assets.buildPushNativeImage"; //NOI18N

    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_BUILD_PUSH_IMAGE,
            COMMAND_BUILD_PUSH_NATIVE_IMAGE
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
                    Project project = values.getValueForStep(ProjectStep.class);
                    ProjectManager.Result r = ProjectManager.getDefault().isProject2(project.getProjectDirectory());

                    ContainerRepositoryItem repository = CloudAssets.getDefault().getItem(ContainerRepositoryItem.class);
                    if (repository == null) {
                        result.cancel(true);
                        return;
                    }
                    Project ociProject = null;
                    Set<Project> subprojects = ProjectUtils.getContainedProjects(project, false);
                    for (Project subproject : subprojects) {
                        if ("oci".equals(subproject.getProjectDirectory().getName())) {
                            ociProject = subproject;
                            break;
                        }
                    }
                    if (r != null && "org-netbeans-modules-gradle".equals(r.getProjectType())) { //NOI18N
                        GradleBaseProject gradleBaseProject;
                        if (ociProject != null) {
                            gradleBaseProject = GradleBaseProject.get(ociProject);
                        } else {
                            gradleBaseProject = GradleBaseProject.get(project);
                        }
                        String version = "";
                        if (gradleBaseProject != null) {
                            version = gradleBaseProject.getVersion();
                        }
                        if (COMMAND_BUILD_PUSH_NATIVE_IMAGE.equals(command)) {
                            version += "-ni";
                        }
                        confirmVersion(version).thenAccept(v -> {
                            try {
                                if (!dockerLogin(repository)) {
                                    result.cancel(true);
                                    return;
                                }
                                
                                Path tempFile = Files.createTempFile("init", ".gradle");

                                String init = "allprojects {\n"
                                        + "    afterEvaluate {\n"
                                        + "        tasks.matching { it.name == 'dockerBuild' }.configureEach {\n"
                                        + "            images = [\"" + repository.getUrl() + ":" + v + "\"]\n"
                                        + "        }\n"
                                        + "    }\n"
                                        + "}";
                                
                                String buildTarget;
                                if (COMMAND_BUILD_PUSH_NATIVE_IMAGE.equals(command)) {
                                    buildTarget = "dockerBuildNative";
                                } else {
                                    buildTarget = "dockerBuild";
                                }
                                Files.write(tempFile, init.getBytes(StandardCharsets.UTF_8));
                                RunConfig runConfig = RunUtils.createRunConfig(
                                        project,
                                        "",
                                        "Build container image",
                                        Collections.emptySet(),
                                        "--init-script", tempFile.toAbsolutePath().toString(), buildTarget, "dockerPush"
                                );
                                ExecutorTask task = RunUtils.executeGradle(runConfig, "");
                                task.addTaskListener((t) -> {
                                    refresh();
                                    try {
                                        Files.delete(tempFile);
                                    } catch (IOException e) {
                                        LOG.log(Level.WARNING, "Error deleting temporary file {0}", new Object[]{tempFile.toAbsolutePath().toString()});
                                        result.completeExceptionally(e);
                                    }
                                    result.complete(null);
                                });
                                task.waitFinished();
                            } catch (IOException ex) {
                                result.completeExceptionally(ex);
                                Exceptions.printStackTrace(ex);
                            }
                        }).exceptionally(ex -> {
                            result.completeExceptionally(ex);
                            return null;
                        });
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

                        nbMavenProject.getFreshProject().thenAccept(mvnProject -> {
                            String version = mvnProject.getVersion();
                            if (COMMAND_BUILD_PUSH_NATIVE_IMAGE.equals(command)) {
                                version += "-ni";
                            }
                            confirmVersion(version).thenAccept(v -> {
                                if (!dockerLogin(repository)) {
                                    result.cancel(true);
                                    return;
                                }
                                String packaging;
                                if (COMMAND_BUILD_PUSH_NATIVE_IMAGE.equals(command)) {
                                    packaging = "docker-native";
                                } else {
                                    packaging = "docker";
                                }
                                List<String> goals;
                                if (isGdk) {
                                    goals = List.of("deploy", "-pl", "oci", "-Dpackaging=" + packaging, "-Djib.to.image=" + repository.getUrl() + ":" + v);
                                } else {
                                    goals = List.of("deploy", "-Dpackaging=" + packaging, "-Djib.to.image=" + repository.getUrl() + ":" + v);
                                }
                                //TODO Update when RunConfig.setReactorStyle(ALSO_MAKE) is available
                                org.netbeans.modules.maven.api.execute.RunConfig runConfig = org.netbeans.modules.maven.api.execute.RunUtils.createRunConfig(
                                        FileUtil.toFile(project.getProjectDirectory()),
                                        project,
                                        "Build container image",
                                        goals
                                );
                                ExecutorTask task = org.netbeans.modules.maven.api.execute.RunUtils.executeMaven(runConfig);
                                task.addTaskListener((t) -> {
                                    refresh();
                                    result.complete(null);
                                });
                            }).exceptionally(ex -> {
                                result.completeExceptionally(ex);
                                return null;
                            });
                        });
                    }
                });
        return result;
    }

    private boolean dockerLogin(ContainerRepositoryItem repo) {
        try {
            String path = (String) BearerTokenCommand.generateBearerToken(OCIManager.getDefault().getActiveProfile(), repo.getRegistry());
            String[] command;
            if (BaseUtilities.isWindows()) {
                command = new String[]{"cmd.exe", "/c",
                    "type \"" + path + "\" | docker login --username=BEARER_TOKEN --password-stdin " + repo.getRegistry()
                };
            } else {
                command = new String[]{"bash", "-c",
                    "cat \"" + path + "\" | docker login --username=BEARER_TOKEN --password-stdin " + repo.getRegistry()
                };
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    reader.lines().forEach(LOG::warning);
                }
                return false;
            }
        } catch (InterruptedException | ExecutionException | IOException | URISyntaxException ex) {
            return false;
        }
        return true;
    }

    private CompletableFuture<String> confirmVersion(String version) {
        NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(Bundle.ImageTag(), Bundle.ImageTag());
        desc.setInputText(version);
        return DialogDisplayer.getDefault().notifyFuture(desc).thenApply(input -> input.getInputText());
    }

    private void refresh() {
        for (Node child : RootNode.instance().getChildren().getNodes()) {
            if (child instanceof ContainerRepositoryNode) {
                ((ContainerRepositoryNode) child).refresh();
            }
        }
    }

}
