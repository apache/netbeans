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

import org.netbeans.modules.cloud.oracle.steps.SuggestedStep;
import org.netbeans.modules.cloud.oracle.steps.ProjectStep;
import org.netbeans.modules.cloud.oracle.steps.CompartmentStep;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cloud.oracle.actions.AddADBAction;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.steps.ItemTypeStep;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.steps.DatabaseConnectionStep;
import org.netbeans.modules.cloud.oracle.steps.TenancyStep;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
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
        Steps.NextStepProvider nsProvider = Steps.NextStepProvider.builder()
                    .stepForClass(ItemTypeStep.class, (s) -> {
                        if ("Databases".equals(s.getValue())) {
                            return new DatabaseConnectionStep();
                        } 
                        return new TenancyStep();
                    }).stepForClass(TenancyStep.class, (s) -> new CompartmentStep())
                    .stepForClass(CompartmentStep.class, (s) -> new SuggestedStep(null))
                    .stepForClass(SuggestedStep.class, (s) -> new ProjectStep())
                    .build();
        Steps.getDefault()
                .executeMultistep(new ItemTypeStep(), Lookups.fixed(nsProvider))
                .thenAccept(values -> {
                    Project project = values.getValueForStep(ProjectStep.class);
                    OCIItem item;
                    if ("Databases".equals(values.getValueForStep(ItemTypeStep.class))) {
                        item = values.getValueForStep(DatabaseConnectionStep.class);
                        if (item == null) {
                            item = new AddADBAction().addADB();
                        }
                    } else {
                        item = values.getValueForStep(SuggestedStep.class);
                    }
                    if (item == null) {
                        future.cancel(true);
                        return;
                    }
                    CloudAssets.getDefault().addItem(item);
                    String[] art = DEP_MAP.get(item.getKey().getPath());
                    try {
                        DependencyUtils.addDependency(project, art[0], art[1]);
                        future.complete(null);
                    } catch (IllegalStateException e) {
                        future.completeExceptionally(e);
                    }
                });
        return future;
    }

}
