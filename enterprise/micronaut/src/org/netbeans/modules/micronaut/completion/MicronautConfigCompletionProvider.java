/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.micronaut.completion;

import java.awt.Color;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
public class MicronautConfigCompletionProvider implements CompletionProvider {

    public static final String PROPERTY_NAME_COLOR = getHTMLColor(64, 64, 217);

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

    private static String getHTMLColor(int r, int g, int b) {
        Color c = LFCustoms.shiftColor(new Color(r, g, b));
        return "<font color=#" //NOI18N
                + LFCustoms.getHexString(c.getRed())
                + LFCustoms.getHexString(c.getGreen())
                + LFCustoms.getHexString(c.getBlue())
                + ">"; //NOI18N
    }

    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {}
        }
        return s;
    }

    private static class MicronautConfigCompletionQuery extends AsyncCompletionQuery {

        private static final String FIELD_ICON = "org/netbeans/modules/editor/resources/completion/field_16.png"; //NOI18N
        private static final String KEYWORD_ICON = "org/netbeans/modules/java/editor/resources/javakw_16.png"; //NOI18N
        private static final String FIELD_COLOR = getHTMLColor(64, 198, 88);
        private static final String KEYWORD_COLOR = getHTMLColor(64, 64, 217);
        private static final Pattern FQN = Pattern.compile("(\\w+\\.)+(\\w+)");

        private final Project project;

        public MicronautConfigCompletionQuery(Project project) {
            this.project = project;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            resultSet.addAllItems(new MicronautConfigCompletionTask().query(doc, caretOffset, project, new MicronautConfigCompletionTask.ItemFactory<CompletionItem>() {
                @Override
                public CompletionItem createPropertyItem(ConfigurationMetadataProperty property, int offset, int baseIndent, int indentLevelSize, int idx) {
                    resultSet.setAnchorOffset(offset);
                    String propName = property.getId();
                    String propType = property.getType();
                    CompletionUtilities.CompletionItemBuilder builder = CompletionUtilities.newCompletionItemBuilder(propName)
                            .iconResource(FIELD_ICON)
                            .leftHtmlText(property.isDeprecated()
                                    ? PROPERTY_NAME_COLOR + "<s>" + propName + "</s></font>"
                                    : PROPERTY_NAME_COLOR + propName + "</font>")
                            .sortPriority(property.isDeprecated() ? 30 : 20)
                            .documentationTask(() -> {
                                return new AsyncCompletionTask(new MicronautConfigDocumentationQuery(property));
                            })
                            .onSelect(ctx -> {
                                try {
                                    Document doc = ctx.getComponent().getDocument();
                                    LineDocument lineDocument = LineDocumentUtils.as(doc, LineDocument.class);
                                    if (lineDocument != null) {
                                        int caretOffset = ctx.getComponent().getCaretPosition();
                                        int end = LineDocumentUtils.getWordEnd(lineDocument, caretOffset);
                                        if (ctx.isOverwrite() && LineDocumentUtils.getWordStart(lineDocument, end) == offset) {
                                            String textEnd = doc.getText(end, 1);
                                            while(baseIndent < 0 && textEnd.endsWith(".")) {
                                                end = LineDocumentUtils.getWordEnd(lineDocument, end + 1);
                                                textEnd = doc.getText(end, 1);
                                            }
                                            if (baseIndent < 0 && textEnd.endsWith("=") || textEnd.endsWith(":")) {
                                                end++;
                                            }
                                            doc.remove(offset, Math.max(caretOffset, end) - offset);
                                        } else if (offset < caretOffset) {
                                            doc.remove(offset, caretOffset - offset);
                                        }
                                        StringBuilder sb = new StringBuilder();
                                        String name = propName.substring(idx);
                                        String[] parts = name.split("\\.");
                                        if (baseIndent < 0) {
                                            int num = 1;
                                            for (int i = 0; i < parts.length; i++) {
                                                String part = parts[i];
                                                if ("*".equals(part)) {
                                                    sb.append("${PAR#" + num++ + " default=\"\"}");
                                                } else {
                                                    sb.append(part);
                                                }
                                                if (i < parts.length - 1) {
                                                    sb.append(".");
                                                } else {
                                                    sb.append("=${cursor}");
                                                }
                                            }
                                        } else {
                                            int lineStart = LineDocumentUtils.getLineStart(lineDocument, caretOffset);
                                            int lineIndent = IndentUtils.lineIndent(doc, lineStart);
                                            ArrayUtilities.appendSpaces(sb, baseIndent - lineIndent);
                                            int indent = baseIndent;
                                            int num = 1;
                                            for (int i = 0; i < parts.length; i++) {
                                                String part = parts[i];
                                                if ("*".equals(part)) {
                                                    sb.append("${PAR#" + num++ + " default=\"\"}");
                                                } else {
                                                    sb.append(part);
                                                }
                                                if (i < parts.length - 1) {
                                                    sb.append(":\n");
                                                    ArrayUtilities.appendSpaces(sb, (indent = indent + indentLevelSize));
                                                } else {
                                                    sb.append(": ${cursor}");
                                                }
                                            }
                                        }
                                        CodeTemplateManager.get(doc).createTemporary(sb.toString()).insert(ctx.getComponent());
                                    }
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            });
                    if (propType != null) {
                        builder.rightHtmlText(escape(FQN.matcher(propType).replaceAll("$2")));
                    }
                    return builder.build();
                }

                @Override
                public CompletionItem createTopLevelPropertyItem(String propName, int offset, int baseIndent, int indentLevelSize) {
                    resultSet.setAnchorOffset(offset);
                    return CompletionUtilities.newCompletionItemBuilder(propName)
                            .iconResource(FIELD_ICON)
                            .leftHtmlText(PROPERTY_NAME_COLOR + "<b>" + propName + "</b></font>")
                            .sortPriority(10)
                            .onSelect(ctx -> {
                                try {
                                    Document doc = ctx.getComponent().getDocument();
                                    LineDocument lineDocument = LineDocumentUtils.as(doc, LineDocument.class);
                                    if (lineDocument != null) {
                                        int caretOffset = ctx.getComponent().getCaretPosition();
                                        int end = LineDocumentUtils.getWordEnd(lineDocument, caretOffset);
                                        if (ctx.isOverwrite() && LineDocumentUtils.getWordStart(lineDocument, end) == offset) {
                                            String textEnd = doc.getText(end, 1);
                                            if (baseIndent < 0 && textEnd.endsWith(".") || textEnd.endsWith(":")) {
                                                end++;
                                            }
                                            doc.remove(offset, Math.max(caretOffset, end) - offset);
                                        } else if (offset < caretOffset) {
                                            doc.remove(offset, caretOffset - offset);
                                        }
                                        StringBuilder sb = new StringBuilder();
                                        if (baseIndent < 0) {
                                            sb.append("*".equals(propName) ? "${PAR#1 default=\"\"}" : propName).append(".${cursor completionInvoke}");
                                        } else {
                                            int lineStart = LineDocumentUtils.getLineStart(lineDocument, caretOffset);
                                            int lineIndent = IndentUtils.lineIndent(doc, lineStart);
                                            ArrayUtilities.appendSpaces(sb, baseIndent - lineIndent);
                                            sb.append("*".equals(propName) ? "${PAR#1 default=\"\"}" : propName).append(":\n");
                                            ArrayUtilities.appendSpaces(sb, baseIndent + indentLevelSize);
                                            sb.append("${cursor completionInvoke}");
                                        }
                                        CodeTemplateManager.get(doc).createTemporary(sb.toString()).insert(ctx.getComponent());
                                    }
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            })
                            .build();
                }

                @Override
                public CompletionItem createValueItem(String value, int offset, boolean isEnum) {
                    resultSet.setAnchorOffset(offset);
                    return CompletionUtilities.newCompletionItemBuilder(value)
                            .iconResource(isEnum ? FIELD_ICON : KEYWORD_ICON)
                            .leftHtmlText(isEnum ? KEYWORD_COLOR + "<b>" + value + "</b></font>" : FIELD_COLOR + value + "</font>")
                            .sortPriority(5)
                            .onSelect(ctx -> {
                                try {
                                    Document doc = ctx.getComponent().getDocument();
                                    LineDocument lineDocument = LineDocumentUtils.as(doc, LineDocument.class);
                                    if (lineDocument != null) {
                                        int caretOffset = ctx.getComponent().getCaretPosition();
                                        int end = LineDocumentUtils.getWordEnd(lineDocument, caretOffset);
                                        if (ctx.isOverwrite() && LineDocumentUtils.getWordStart(lineDocument, end) == offset) {
                                            doc.remove(offset, Math.max(caretOffset, end) - offset);
                                        } else if (offset < caretOffset) {
                                            doc.remove(offset, caretOffset - offset);
                                        }
                                        doc.insertString(offset, value, null);
                                    }
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            })
                            .build();
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
