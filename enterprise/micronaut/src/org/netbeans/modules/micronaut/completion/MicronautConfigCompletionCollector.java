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
package org.netbeans.modules.micronaut.completion;

import java.util.function.Consumer;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.filesystems.FileObject;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
public class MicronautConfigCompletionCollector implements CompletionCollector {

    @MimeRegistration(mimeType = MicronautConfigUtilities.YAML_MIME, service = CompletionCollector.class)
    public static MicronautConfigCompletionCollector createYamlCollector() {
        return new MicronautConfigCompletionCollector();
    }

    @MimeRegistration(mimeType = MicronautConfigUtilities.PROPERTIES_MIME, service = CompletionCollector.class)
    public static MicronautConfigCompletionCollector createPropertiesCollector() {
        return new MicronautConfigCompletionCollector();
    }

    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        FileObject fo = EditorDocumentUtils.getFileObject(doc);
        if (MicronautConfigUtilities.isMicronautConfigFile(fo)) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                if (MicronautConfigProperties.hasConfigMetadata(project)) {
                    new MicronautConfigCompletionTask().query(doc, offset, project, new MicronautConfigCompletionTask.ItemFactory<Completion>() {
                        @Override
                        public Completion createTopLevelPropertyItem(String propName, int offset, int baseIndent, int indentLevelSize) {
                            StringBuilder insertText = new StringBuilder();
                            Completion.TextFormat insertTextFormat = Completion.TextFormat.PlainText;
                            if (baseIndent < 0) {
                                if ("*".equals(propName)) {
                                    insertText.append("$1.");
                                    insertTextFormat = Completion.TextFormat.Snippet;
                                } else {
                                    insertText.append(propName).append(".");
                                }
                            } else {
                                if ("*".equals(propName)) {
                                    insertText.append("$1:\n");
                                    ArrayUtilities.appendSpaces(insertText, baseIndent + indentLevelSize);
                                    insertTextFormat = Completion.TextFormat.Snippet;
                                } else {
                                    insertText.append(propName).append(":\n");
                                    ArrayUtilities.appendSpaces(insertText, indentLevelSize);
                                }
                            }
                            return CompletionCollector.newBuilder(propName).kind(Completion.Kind.Property).sortText(String.format("%04d%s", 10, propName))
                                    .insertText(insertText.toString()).insertTextFormat(insertTextFormat).build();
                        }

                        @Override
                        public Completion createPropertyItem(ConfigurationMetadataProperty property, int offset, int baseIndent, int indentLevelSize, int idx) {
                            String[] parts = property.getId().substring(idx).split("\\.");
                            StringBuilder insertText = new StringBuilder();
                            int num = 1;
                            int indent = 0;
                            Completion.TextFormat insertTextFormat = Completion.TextFormat.PlainText;
                            for (int i = 0; i < parts.length; i++) {
                                String part = parts[i];
                                if ("*".equals(part)) {
                                    insertText.append("$" + num++);
                                    insertTextFormat = Completion.TextFormat.Snippet;
                                } else {
                                    insertText.append(part);
                                }
                                if (baseIndent < 0) {
                                    if (i < parts.length - 1) {
                                        insertText.append(".");
                                    } else {
                                        insertText.append("=");
                                    }
                                } else {
                                    if (i < parts.length - 1) {
                                        insertText.append(":\n");
                                        ArrayUtilities.appendSpaces(insertText, (indent = indent + indentLevelSize));
                                    } else {
                                        insertText.append(": ");
                                    }
                                }
                            }
                            CompletionCollector.Builder builder = CompletionCollector.newBuilder(property.getId()).kind(Completion.Kind.Property)
                                    .sortText(String.format("%04d%s", property.isDeprecated() ? 30 : 20, property.getId())).insertText(insertText.toString())
                                    .insertTextFormat(insertTextFormat).documentation(new MicronautConfigDocumentation(property).getText());
                            if (property.isDeprecated()) {
                                builder.addTag(Completion.Tag.Deprecated);
                            }
                            return builder.build();
                        }

                        @Override
                        public Completion createValueItem(String value, int offset, boolean isEnum) {
                            return CompletionCollector.newBuilder(value)
                                    .kind(isEnum ? Completion.Kind.EnumMember : Completion.Kind.Keyword)
                                    .sortText(String.format("%04d%s", 5, value))
                                    .build();
                        }
                    }).stream().forEach(consumer);
                }
            }
        }
        return true;
    }
}
