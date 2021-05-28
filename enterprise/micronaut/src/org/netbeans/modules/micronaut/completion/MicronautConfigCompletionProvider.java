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

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
public class MicronautConfigCompletionProvider implements CompletionProvider {

    @MimeRegistration(mimeType = "text/x-yaml", service = CompletionProvider.class)
    public static MicronautConfigCompletionProvider createYamlProvider() {
        return new MicronautConfigCompletionProvider();
    }

    @MimeRegistration(mimeType = "text/x-properties", service = CompletionProvider.class)
    public static MicronautConfigCompletionProvider createPropertiesProvider() {
        return new MicronautConfigCompletionProvider();
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        FileObject fo = EditorDocumentUtils.getFileObject(component.getDocument());
        if (MicronautConfigUtilities.isMicronautConfigFile(fo)) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                if (MicronautConfigProperties.hasConfigMetadata(project)) {
                    switch (queryType) {
                        case COMPLETION_ALL_QUERY_TYPE:
                        case COMPLETION_QUERY_TYPE:
                            return new AsyncCompletionTask(new MicronautConfigCompletionQuery(project), component);
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

    static CompletionTask createDocTask(ConfigurationMetadataProperty element) {
        return new AsyncCompletionTask(new MicronautConfigDocumentationQuery(element));
    }

    private static class MicronautConfigCompletionQuery extends AsyncCompletionQuery {

        private final Project project;

        public MicronautConfigCompletionQuery(Project project) {
            this.project = project;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            resultSet.addAllItems(new MicronautConfigCompletionTask().query(doc, caretOffset, project, new MicronautConfigCompletionTask.ItemFactory<MicronautConfigCompletionItem>() {
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
                element = MicronautConfigUtilities.resolveProperty(doc, caretOffset, null, null);
            }
            resultSet.setDocumentation(element != null ? new MicronautConfigDocumentation(element) : null);
            resultSet.finish();
        }
    }
}
