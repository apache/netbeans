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
package org.netbeans.modules.java.lsp.server.protocol;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Completion of project configurations in launch.json.
 *
 * @author Martin Entlicher
 */
public class ProjectConfigurationCompletion implements LaunchConfigurationCompletion {

    private static final String CONFIG_TYPE = "java+";     // NOI18N

    @Override
    public CompletableFuture<List<CompletionItem>> configurations(Supplier<CompletableFuture<Project>> projectSupplier) {
        return projectSupplier.get().thenApply(p -> createConfigurationsCompletion(p));
    }

    @Override
    public CompletableFuture<List<CompletionItem>> attributes(Supplier<CompletableFuture<Project>> projectSupplier, Map<String, Object> currentAttributes) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    @Override
    public CompletableFuture<List<CompletionItem>> attributeValues(Supplier<CompletableFuture<Project>> projectSupplier, Map<String, Object> currentAttributes, String attribute) {
        if ("launchConfiguration".equals(attribute)) {      // NOI18N
            return projectSupplier.get().thenApply(p -> createLaunchConfigCompletion(p));
        } else {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    @NbBundle.Messages({"# {0} - Configuration name", "LBL_LaunchJavaConfig=Launch Java: {0}",
                        "# {0} - Configuration name", "LBL_LaunchJavaConfig_desc=Launch a Java application using {0}."})
    private static List<CompletionItem> createConfigurationsCompletion(Project p) {
        Collection<ProjectConfiguration> configurations = getConfigurations(p);
        int size = configurations.size();
        if (size <= 1) {
            return Collections.emptyList();
        }
        List<CompletionItem> completionItems = new ArrayList<>(size - 1);
        boolean skipFirst = true;
        for (ProjectConfiguration c : configurations) {
            if (skipFirst) {
                skipFirst = false;
                continue;
            }
            String configDisplayName = c.getDisplayName();
            String launchName = Bundle.LBL_LaunchJavaConfig(configDisplayName);
            CompletionItem ci = new CompletionItem("Java+: " + launchName);   // NOI18N
            ci.setKind(CompletionItemKind.Module);
            StringWriter sw = new StringWriter();
            try (JsonWriter w = new JsonWriter(sw)) {
                w.setIndent("\t");                                          // NOI18N
                w.beginObject();
                w.name("name").value(launchName);                           // NOI18N
                w.name("type").value(CONFIG_TYPE);                          // NOI18N
                w.name("request").value("launch");                          // NOI18N
                w.name("mainClass").value("${file}");                       // NOI18N
                w.name("launchConfiguration").value(configDisplayName);     // NOI18N
                w.endObject();
                w.flush();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            ci.setInsertText(sw.toString());
            ci.setDocumentation(Bundle.LBL_LaunchJavaConfig_desc(configDisplayName));
            completionItems.add(ci);
        }
        return completionItems;
    }

    private List<CompletionItem> createLaunchConfigCompletion(Project p) {
        Collection<ProjectConfiguration> configurations = getConfigurations(p);
        int size = configurations.size();
        if (size <= 1) {
            return Collections.emptyList();
        }
        List<CompletionItem> completionItems = new ArrayList<>(size - 1);
        boolean skipFirst = true;
        for (ProjectConfiguration c : configurations) {
            if (skipFirst) {
                skipFirst = false;
                continue;
            }
            String configDisplayName = c.getDisplayName();
            CompletionItem ci = new CompletionItem(configDisplayName);
            ci.setInsertText("\"" + Utils.encode2JSON(configDisplayName) + "\"");
            completionItems.add(ci);
        }
        return completionItems;
    }

    private static Collection<ProjectConfiguration> getConfigurations(Project p) {
        ProjectConfigurationProvider<ProjectConfiguration> provider = (p != null) ? p.getLookup().lookup(ProjectConfigurationProvider.class) : null;
        if (provider == null) {
            return Collections.emptyList();
        }
        return provider.getConfigurations();
    }

    @ServiceProvider(service = Factory.class, position = 100)
    public static final class FactoryImpl implements Factory {

        @Override
        public LaunchConfigurationCompletion createLaunchConfigurationCompletion(NbCodeClientCapabilities capa) {
            return new ProjectConfigurationCompletion();
        }

    }
}
