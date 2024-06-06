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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.assets.Steps.ItemTypeStep;
import org.netbeans.modules.cloud.oracle.assets.Steps.ProjectStep;
import org.netbeans.modules.cloud.oracle.assets.Steps.SuggestedContext;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.refactoring.spi.ModificationResult;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "NoProjects=No Project Found",
    "SelectProject=Select Project to Update Dependencies",
    "SelectResourceType=Select Resource Type"})
@ServiceProvider(service = CommandProvider.class)
public class AddNewAssetCommand implements CommandProvider {

    private static final String COMMAND_ADD_NEW_ASSET = "nbls.cloud.assets.add.new"; //NOI18N

    private static final Set COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_ADD_NEW_ASSET
    ));

    private static final Map<String, String[]> DEP_MAP = new HashMap() {
        {
            put("Databases", new String[]{"io.micronaut.oraclecloud", "micronaut-oraclecloud-atp"}); //NOI18N
            put("Bucket", new String[]{"io.micronaut.objectstorage", "micronaut-object-storage-oracle-cloud"}); //NOI18N
            put("Vault", new String[]{"io.micronaut.oraclecloud", "micronaut-oraclecloud-vault"}); //NOI18N
        }
    };

    @Override
    public Set<String> getCommands() {
        return Collections.unmodifiableSet(COMMANDS);
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        CompletableFuture future = new CompletableFuture();
        NewSuggestedContext context = new NewSuggestedContext(OpenProjectsFinder.getDefault().findTopLevelProjects());
        Lookup lookup = Lookups.fixed(context);
        Steps.getDefault()
                .executeMultistep(new ItemTypeStep(), lookup)
                .thenAccept(result -> {
                    Project project = ((Pair<Project, OCIItem>) result).first();
                    OCIItem item = ((Pair<Project, OCIItem>) result).second();
                    CloudAssets.getDefault().addItem(item);
                    Project projectToModify = null;
                    Set<Project> subProjects = ProjectUtils.getContainedProjects(project, false);
                    for (Project subProject : subProjects) {
                        if ("oci".equals(subProject.getProjectDirectory().getName())) { //NOI18N
                            projectToModify = subProject;
                            break;
                        }
                    }
                    if (projectToModify == null) {
                        projectToModify = project;
                    }
                    if (projectToModify != null) {
                        String[] art = DEP_MAP.get(context.getItemType());
                        ArtifactSpec spec = ArtifactSpec.make(art[0], art[1]);
                        Dependency dep = Dependency.make(spec, Scope.named("implementation")); //NOI18N
                        DependencyChange change = DependencyChange.add(Collections.singletonList(dep), DependencyChange.Options.skipConflicts);
                        try {
                            ModificationResult mod = ProjectDependencies.modifyDependencies(projectToModify, change);
                            mod.commit();
                        } catch (IOException ex) {
                            future.completeExceptionally(ex);
                        } catch (DependencyChangeException ex) {
                            future.completeExceptionally(ex);
                        } catch (ProjectOperationException ex) {
                            future.completeExceptionally(ex);
                        }
                    }
                });
        future.complete(null);
        return future;
    }

    private final class NewSuggestedContext implements SuggestedContext {

        private final CompletableFuture<Project[]> projects;
        private String itemType = null;

        public NewSuggestedContext(CompletableFuture<Project[]> projects) {
            this.projects = projects;
        }

        @Override
        public String getItemType() {
            return itemType;
        }

        @Override
        public Step getNextStep() {
            return new ProjectStep(projects);
        }

        @Override
        public void setItemType(String itemType) {
            this.itemType = itemType;
        }
    }

}
