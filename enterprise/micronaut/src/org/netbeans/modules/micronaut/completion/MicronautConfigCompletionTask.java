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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import static javax.lang.model.element.ElementKind.ENUM_CONSTANT;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.micronaut.MicronautConfigProperties;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
public class MicronautConfigCompletionTask {

    public static interface ItemFactory<T> {
        T createTopLevelPropertyItem(String propName, int offset, int baseIndent, int indentLevelSize);
        T createPropertyItem(ConfigurationMetadataProperty property, int offset, int baseIndent, int indentLevelSize, int idx);
        T createValueItem(String value, int offset, boolean isEnum);
    }

    public <T> List<T> query(Document doc, int caretOffset, Project project, ItemFactory<T> factory) {
        List<T> items = new ArrayList<>();
        LineDocument lineDocument = LineDocumentUtils.as(doc, LineDocument.class);
        if (lineDocument != null) {
            int lineStart = LineDocumentUtils.getLineStart(lineDocument, caretOffset);
            try {
                String text = lineDocument.getText(lineStart, caretOffset - lineStart);
                String mimeType = DocumentUtilities.getMimeType(doc);
                if ("text/x-yaml".equals(mimeType)) {
                    if (!text.startsWith("#")) {
                        int idx = text.indexOf(':');
                        if (idx < 0) {
                            final int lineIndent = Math.min(IndentUtils.lineIndent(lineDocument, lineStart), caretOffset - lineStart);
                            final int wordStart = LineDocumentUtils.getPreviousWhitespace(lineDocument, caretOffset) + 1;
                            final String prefix = wordStart < caretOffset ? text.substring(wordStart - lineStart) : null;
                            final int anchorOffset = wordStart < caretOffset ? wordStart : caretOffset;
                            ParserManager.parse(Collections.singleton(Source.create(lineDocument)), new UserTask() {
                                public @Override void run(ResultIterator resultIterator) throws Exception {
                                    Parser.Result r = resultIterator.getParserResult();
                                    if (r instanceof ParserResult) {
                                        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                                        if (language != null) {
                                            StructureScanner scanner = language.getStructure();
                                            if (scanner != null) {
                                                List<? extends StructureItem> structures = scanner.scan((ParserResult) r);
                                                int indentLevelSize = getIndentLevelSize(lineDocument, structures);
                                                List<? extends StructureItem> context = getContext(structures, wordStart);
                                                String filter = "";
                                                StructureItem currentItem = null;
                                                for (StructureItem item : context) {
                                                    int itemLineStart = LineDocumentUtils.getLineStart(lineDocument, (int) item.getPosition());
                                                    int itemLineIndent = IndentUtils.lineIndent(lineDocument, itemLineStart);
                                                    if (itemLineIndent < lineIndent) {
                                                        filter += item.getName() + '.';
                                                        currentItem = item;
                                                    }
                                                }
                                                if (prefix != null) {
                                                    filter += prefix;
                                                }
                                                int currentItemLineIndent = currentItem != null ? IndentUtils.lineIndent(lineDocument, LineDocumentUtils.getLineStart(lineDocument, (int) currentItem.getPosition())) : -indentLevelSize;
                                                Map<String, ConfigurationMetadataProperty> properties = MicronautConfigProperties.getProperties(project);
                                                Set<String> topLevels = new HashSet<>();
                                                for (Map.Entry<String, ConfigurationMetadataProperty> entry : properties.entrySet()) {
                                                    String propName = entry.getKey();
                                                    int idx = match(propName, filter);
                                                    if (idx >= 0) {
                                                        int dotIdx = propName.indexOf('.', idx);
                                                        String simpleName = dotIdx < 0 ? propName.substring(idx) : propName.substring(idx, dotIdx);
                                                        if (filter(currentItem != null ? currentItem.getNestedItems() : structures, simpleName)) {
                                                            items.add(factory.createPropertyItem(entry.getValue(), anchorOffset, currentItemLineIndent + indentLevelSize, indentLevelSize, idx));
                                                            if (dotIdx > 0) {
                                                                topLevels.add(simpleName);
                                                            }
                                                        }
                                                    }
                                                }
                                                for (String topLevel : topLevels) {
                                                    items.add(factory.createTopLevelPropertyItem(topLevel, anchorOffset, currentItemLineIndent + indentLevelSize, indentLevelSize));
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        } else {
                            final int prevWS = LineDocumentUtils.getPreviousWhitespace(lineDocument, caretOffset);
                            final int wordStart = (prevWS < lineStart + idx ? lineStart + idx : prevWS) + 1;
                            final String prefix = wordStart < caretOffset ? text.substring(wordStart - lineStart) : "";
                            final int anchorOffset = wordStart < caretOffset ? wordStart : caretOffset;
                            ParserManager.parse(Collections.singleton(Source.create(lineDocument)), new UserTask() {
                                public @Override void run(ResultIterator resultIterator) throws Exception {
                                    Parser.Result r = resultIterator.getParserResult();
                                    if (r instanceof ParserResult) {
                                        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                                        if (language != null) {
                                            StructureScanner scanner = language.getStructure();
                                            if (scanner != null) {
                                                List<? extends StructureItem> structures = scanner.scan((ParserResult) r);
                                                List<? extends StructureItem> context = getContext(structures, wordStart);
                                                String propName = "";
                                                for (StructureItem structureItem : context) {
                                                    propName += propName.isEmpty() ? structureItem.getName() : '.' + structureItem.getName();
                                                }
                                                if (!propName.isEmpty()) {
                                                    items.addAll(completeValues(propName, prefix, anchorOffset, project, factory));
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                } else {
                    if (!text.startsWith("#") && !text.startsWith("!")) {
                        int colIdx = text.indexOf(':');
                        int eqIdx = text.indexOf('=');
                        if (colIdx < 0) {
                            if (eqIdx < 0) {
                                final int wordStart = LineDocumentUtils.getPreviousWhitespace(lineDocument, caretOffset) + 1;
                                final String prefix = wordStart < caretOffset ? text.substring(wordStart - lineStart) : "";
                                final int lastDotIdx = prefix.lastIndexOf('.');
                                final int anchorOffset = wordStart < caretOffset ? wordStart + (lastDotIdx < 0 ? 0 : lastDotIdx + 1) : caretOffset;
                                final Properties props = new Properties();
                                props.load(new StringReader(doc.getText(0, doc.getLength())));
                                Map<String, ConfigurationMetadataProperty> properties = MicronautConfigProperties.getProperties(project);
                                Set<String> topLevels = new HashSet<>();
                                for (Map.Entry<String, ConfigurationMetadataProperty> entry : properties.entrySet()) {
                                    String propName = entry.getKey();
                                    int idx = match(propName, prefix);
                                    if (idx >= 0) {
                                        int dotIdx = propName.indexOf('.', idx);
                                        String simpleName = dotIdx < 0 ? propName.substring(idx) : propName.substring(idx, dotIdx);
                                        if (props.getProperty(propName) == null) {
                                            items.add(factory.createPropertyItem(entry.getValue(), anchorOffset, -1, -1, idx));
                                            if (dotIdx > 0) {
                                                topLevels.add(simpleName);
                                            }
                                        }
                                    }
                                }
                                for (String topLevel : topLevels) {
                                    items.add(factory.createTopLevelPropertyItem(topLevel, anchorOffset, -1, -1));
                                }
                            } else {
                                int wordStart = LineDocumentUtils.getPreviousWhitespace(lineDocument, caretOffset) + 1;
                                if (wordStart < lineStart + eqIdx) {
                                    wordStart = lineStart + eqIdx + 1;
                                }
                                final String prefix = wordStart < caretOffset ? text.substring(wordStart - lineStart) : "";
                                final int anchorOffset = wordStart < caretOffset ? wordStart : caretOffset;
                                final String propName = text.substring(0, eqIdx).trim();
                                if (!propName.isEmpty()) {
                                    items.addAll(completeValues(propName, prefix, anchorOffset, project, factory));
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return items;
    }

    private <T> List<T> completeValues(String propName, String prefix, int anchorOffset, Project project, ItemFactory<T> factory) {
        List<T> items = new ArrayList<>();
        Map<String, ConfigurationMetadataProperty> properties = MicronautConfigProperties.getProperties(project);
        ConfigurationMetadataProperty property = properties.get(propName);
        if (property != null) {
            String type = property.getType();
            if (type != null) {
                if ("boolean".equals(type)) {
                    if ("true".startsWith(prefix)) {
                        items.add(factory.createValueItem("true", anchorOffset, false));
                    }
                    if ("false".startsWith(prefix)) {
                        items.add(factory.createValueItem("false", anchorOffset, false));
                    }
                } else {
                    SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    AtomicBoolean resolved = new AtomicBoolean();
                    for (SourceGroup srcGroup : srcGroups) {
                        if (!resolved.get()) {
                            JavaSource js = JavaSource.create(ClasspathInfo.create(srcGroup.getRootFolder()));
                            if (js != null) {
                                try {
                                    js.runUserActionTask(cc -> {
                                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                        TypeElement typeElement = cc.getElements().getTypeElement(type);
                                        if (typeElement != null) {
                                            resolved.set(true);
                                            if (typeElement.getKind() == ElementKind.ENUM) {
                                                for (Element e : typeElement.getEnclosedElements()) {
                                                    if (e.getKind() == ENUM_CONSTANT) {
                                                        String name = e.getSimpleName().toString();
                                                        if (name.startsWith(prefix)) {
                                                            items.add(factory.createValueItem(name, anchorOffset, true));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }, true);
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
            }
        }
        return items;
    }

    private int match(String propName, String filter) {
        int len = 0;
        String[] parts = propName.split("\\*");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (filter.length() <= part.length()) {
                if (part.startsWith(filter)) {
                    int idx = filter.lastIndexOf('.');
                    return idx < 0 ? len : len + idx + 1;
                } else {
                    return -1;
                }
            }
            if (i < parts.length - 1) {
                if (!part.equals(filter.substring(0, part.length()))) {
                    return -1;
                }
                len += part.length();
                int idx = filter.indexOf('.', part.length());
                if (idx < 0) {
                    return -1;
                }
                len += 1;
                filter = filter.substring(idx);
            } else {
                return filter.startsWith(part) ? len : -1;
            }
        }
        return -1;
    }

    private boolean filter(List<? extends StructureItem> structures, String name) {
        for (StructureItem item : structures) {
            if (name.equals(item.getName())) {
                return false;
            }
        }
        return true;
    }

    private List<StructureItem> getContext(List<? extends StructureItem> structure, int offset) {
        List<StructureItem> items = new ArrayList<>();
        while (structure != null && !structure.isEmpty()) {
            StructureItem currentItem = null;
            for (StructureItem item : structure) {
                if (item.getPosition() < offset) {
                    currentItem = item;
                }
            }
            if (currentItem != null) {
                items.add(currentItem);
                structure = currentItem.getNestedItems();
            } else {
                structure = null;
            }
        }
        return items;
    }

    private int getIndentLevelSize(LineDocument lineDocument, List<? extends StructureItem> structures) {
        int indentLevel = IndentUtils.indentLevelSize(lineDocument);
        try {
            for (StructureItem structure : structures) {
                int baseStart = LineDocumentUtils.getLineStart(lineDocument, (int) structure.getPosition());
                int baseIndent = IndentUtils.lineIndent(lineDocument, baseStart);
                for (StructureItem nestedItem : structure.getNestedItems()) {
                    int lineStart = LineDocumentUtils.getLineStart(lineDocument, (int) nestedItem.getPosition());
                    int lineIndent = IndentUtils.lineIndent(lineDocument, lineStart) - baseIndent;
                    if (lineIndent > 0 && lineIndent < indentLevel) {
                        indentLevel = lineIndent;
                    }
                }
            }
        } catch (BadLocationException ble) {}
        return indentLevel;
    }
}
