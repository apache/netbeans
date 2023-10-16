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
package org.netbeans.modules.java.lsp.server.debugging.attach;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.LaunchConfigurationCompletion;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeClientCapabilities;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Completion of debugger attach configurations.
 *
 * @author Martin Entlicher
 */
public class AttachConfigurationCompletion implements LaunchConfigurationCompletion {

    private final NbCodeClientCapabilities capa;

    public AttachConfigurationCompletion(NbCodeClientCapabilities capa) {
        this.capa = capa;
    }

    @Override
    public CompletableFuture<List<CompletionItem>> configurations(Supplier<CompletableFuture<Project>> projectSupplier) {
        return CompletableFuture.supplyAsync(() -> {
            return createCompletion(AttachConfigurations.get(capa));
        }, AttachConfigurations.RP);
    }

    @Override
    public CompletableFuture<List<CompletionItem>> attributes(Supplier<CompletableFuture<Project>> projectSupplier, Map<String, Object> currentAttributes) {
        return CompletableFuture.supplyAsync(() -> {
            return createAttributesCompletion(AttachConfigurations.get(capa), currentAttributes);
        }, AttachConfigurations.RP);
    }

    @Override
    public CompletableFuture<List<CompletionItem>> attributeValues(Supplier<CompletableFuture<Project>> projectSupplier, Map<String, Object> currentAttributes, String attribute) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    private static List<CompletionItem> createCompletion(AttachConfigurations attachConfigurations) {
        return attachConfigurations.getConfigurations().stream().map(configAttrs -> createCompletion(configAttrs)).collect(Collectors.toList());
    }

    private static CompletionItem createCompletion(ConfigurationAttributes configAttrs) {
        CompletionItem ci = new CompletionItem("Java+: " + configAttrs.getName());    // NOI18N
        ci.setKind(CompletionItemKind.Module);
        StringWriter sw = new StringWriter();
        try (JsonWriter w = new JsonWriter(sw)) {
            w.setIndent("\t");                                              // NOI18N
            w.beginObject();
            w.name("name").jsonValue("\"${1:" + Utils.escapeCompletionSnippetSpecialChars(Utils.encode2JSON(configAttrs.getName())) + "}\""); // NOI18N
            w.name("type").value(AttachConfigurations.CONFIG_TYPE);         // NOI18N
            w.name("request").value(AttachConfigurations.CONFIG_REQUEST);   // NOI18N
            int locationIndex = 2;
            for (Map.Entry<String, ConfigurationAttribute> entry : configAttrs.getAttributes().entrySet()) {
                ConfigurationAttribute ca = entry.getValue();
                if (ca.isMustSpecify()) {
                    String value = ca.getDefaultValue();
                    if (value.startsWith("${command:")) { // Do not suggest to customize values provided by commands    // NOI18N
                        value = Utils.escapeCompletionSnippetSpecialChars(Utils.encode2JSON(value));
                    } else {
                        value = "${" + (locationIndex++) + (value.isEmpty() ? "}" : ":" + Utils.escapeCompletionSnippetSpecialChars(Utils.encode2JSON(value)) + "}"); // NOI18N
                    }
                    // We have pre-encoded the value in order not to encode the completion snippet escape characters
                    w.name(entry.getKey()).jsonValue("\"" + value + "\"");
                }
            }
            w.endObject();
            w.flush();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        ci.setInsertText(sw.toString());
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDocumentation(configAttrs.getDescription());
        return ci;
    }

    private static List<CompletionItem> createAttributesCompletion(AttachConfigurations attachConfigurations, Map<String, Object> currentAttributes) {
        List<CompletionItem> completionItems = null;
        ConfigurationAttributes currentConfiguration = attachConfigurations.findConfiguration(currentAttributes);
        if (currentConfiguration != null) {
            Map<String, ConfigurationAttribute> attributes = currentConfiguration.getAttributes();
            for (Map.Entry<String, ConfigurationAttribute> entry : attributes.entrySet()) {
                String attrName = entry.getKey();
                if (!currentAttributes.containsKey(attrName)) {
                    StringWriter sw = new StringWriter();
                    try (JsonWriter w = new JsonWriter(sw)) {
                        w.beginObject();
                        w.name(attrName).value(entry.getValue().getDefaultValue());
                        w.endObject();
                        w.flush();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    CompletionItem ci = new CompletionItem(attrName);
                    String text = sw.toString();
                    text = text.substring(1, text.length() - 1); // Remove { and }
                    ci.setInsertText(text);
                    ci.setDocumentation(entry.getValue().getDescription());
                    if (completionItems == null) {
                        completionItems = new ArrayList<>(3);
                    }
                    completionItems.add(ci);
                }
            }
        }
        if (completionItems != null) {
            return completionItems;
        } else {
            return Collections.emptyList();
        }
    }

    @ServiceProvider(service = Factory.class, position = 200)
    public static final class FactoryImpl implements Factory {

        @Override
        public LaunchConfigurationCompletion createLaunchConfigurationCompletion(NbCodeClientCapabilities capa) {
            return new AttachConfigurationCompletion(capa);
        }

    }
}
