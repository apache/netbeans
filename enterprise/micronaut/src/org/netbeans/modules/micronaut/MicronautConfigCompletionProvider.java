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
package org.netbeans.modules.micronaut;

import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.ItemFactory;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-yaml", service = CompletionProvider.class)
public class MicronautConfigCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        FileObject fo = EditorDocumentUtils.getFileObject(component.getDocument());
        if (fo != null && "application.yml".equalsIgnoreCase(fo.getNameExt())) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                MicronautConfigProperties configProperties = project.getLookup().lookup(MicronautConfigProperties.class);
                if (configProperties != null) {
                    switch (queryType) {
                        case COMPLETION_ALL_QUERY_TYPE:
                        case COMPLETION_QUERY_TYPE:
                            return new AsyncCompletionTask(new MicronautConfigCompletionQuery(configProperties), component);
                        case DOCUMENTATION_QUERY_TYPE:
                            return new AsyncCompletionTask(new MicronautConfigDocumentationQuery(null), component);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    @Override
    public <T> List<T> getCompletionItems(Document doc, int caretOffset, ItemFactory<T> factory) {
        FileObject fo = EditorDocumentUtils.getFileObject(doc);
        if (fo != null && "application.yml".equalsIgnoreCase(fo.getNameExt())) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                MicronautConfigProperties configProperties = project.getLookup().lookup(MicronautConfigProperties.class);
                if (configProperties != null) {
                    return new MicronautConfigCompletionTask().query(doc, caretOffset, configProperties, new MicronautConfigCompletionTask.ItemFactory<T>() {
                        @Override
                        public T createTopLevelPropertyItem(String propName, int offset, int baseIndent, int indentLevelSize) {
                            StringBuilder insertText = new StringBuilder();
                            int insertTextFormat = 1;
                            if ("*".equals(propName)) {
                                insertText.append("$1:\n");
                                ArrayUtilities.appendSpaces(insertText, baseIndent + indentLevelSize);
                                insertTextFormat = 2;
                            } else {
                                insertText.append(propName).append(":\n");
                                ArrayUtilities.appendSpaces(insertText, indentLevelSize);
                            }
                            return factory.create(propName, 10, null, String.format("%4d%s", 10, propName), insertText.toString(), insertTextFormat, null);
                        }

                        @Override
                        public T createPropertyItem(ConfigurationMetadataProperty property, int offset, int baseIndent, int indentLevelSize, int idx) {
                            String[] parts = property.getId().substring(idx).split("\\.");
                            StringBuilder insertText = new StringBuilder();
                            int num = 1;
                            int indent = 0;
                            int insertTextFormat = 1;
                            for (int i = 0; i < parts.length; i++) {
                                String part = parts[i];
                                if ("*".equals(part)) {
                                    insertText.append("$" + num++);
                                    insertTextFormat = 2;
                                } else {
                                    insertText.append(part);
                                }
                                if (i < parts.length - 1) {
                                    insertText.append(":\n");
                                    ArrayUtilities.appendSpaces(insertText, (indent = indent + indentLevelSize));
                                } else {
                                    insertText.append(": ");
                                }
                            }
                            return factory.create(property.getId(), 10, property.isDeprecated() ? new int[] {1} : null,
                                    String.format("%4d%s", property.isDeprecated() ? 30 : 20, property.getId()),
                                    insertText.toString(), insertTextFormat, new MicronautConfigDocumentation(property).getText());
                        }
                    });
                }
            }
        }
        return Collections.emptyList();
    }

    static CompletionTask createDocTask(ConfigurationMetadataProperty element) {
        return new AsyncCompletionTask(new MicronautConfigDocumentationQuery(element));
    }

    private static class MicronautConfigCompletionQuery extends AsyncCompletionQuery {

        private final MicronautConfigProperties configProperties;

        public MicronautConfigCompletionQuery(MicronautConfigProperties configProperties) {
            this.configProperties = configProperties;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            resultSet.addAllItems(new MicronautConfigCompletionTask().query(doc, caretOffset, configProperties, new MicronautConfigCompletionTask.ItemFactory<MicronautConfigCompletionItem>() {
                @Override
                public MicronautConfigCompletionItem createPropertyItem(ConfigurationMetadataProperty property, int offset, int baseIndent, int indentLevelSize, int idx) {
                    resultSet.setAnchorOffset(offset);
                    return MicronautConfigCompletionItem.createPropertyItem(property, offset, baseIndent, indentLevelSize, idx);
                }

                @Override
                public MicronautConfigCompletionItem createTopLevelPropertyItem(String propName, int offset, int baseIndent, int indentLevelSize) {
                    resultSet.setAnchorOffset(offset);
                    return MicronautConfigCompletionItem.createTopLevelPropertyItem(propName, offset, baseIndent, indentLevelSize);
                }
            }));
            resultSet.finish();
        }
    }

    private static class MicronautConfigDocumentationQuery extends AsyncCompletionQuery {

        private ConfigurationMetadataProperty element;

        private MicronautConfigDocumentationQuery(ConfigurationMetadataProperty element) {
            this.element = element;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (element == null) {
                element = MicronautConfigHyperlinkProvider.resolve(doc, caretOffset, null, null);
            }
            resultSet.setDocumentation(new MicronautConfigDocumentation(element));
            resultSet.finish();
        }
    }
}
