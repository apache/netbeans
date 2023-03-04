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
package org.netbeans.modules.micronaut;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataSource;

/**
 *
 * @author Dusan Balek
 */
public class MicronautConfigUtilities {

    private static final Pattern REGEXP = Pattern.compile("^(application|bootstrap)(-\\w*)*\\.(yml|properties)$", Pattern.CASE_INSENSITIVE);

    public static boolean isMicronautConfigFile(FileObject fo) {
        return fo != null && REGEXP.matcher(fo.getNameExt()).matches();
    }

    public static ConfigurationMetadataProperty resolveProperty(Document doc, int offset, int[] span, List<ConfigurationMetadataSource> sources) {
        LineDocument lineDocument = LineDocumentUtils.as(doc, LineDocument.class);
        if (lineDocument != null) {
            FileObject fo = EditorDocumentUtils.getFileObject(doc);
            if (isMicronautConfigFile(fo)) {
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    if (MicronautConfigProperties.hasConfigMetadata(project)) {
                        try {
                            int lineStart = LineDocumentUtils.getLineStart(lineDocument, offset);
                            String mimeType = DocumentUtilities.getMimeType(doc);
                            if ("text/x-yaml".equals(mimeType)) {
                                String text = lineDocument.getText(lineStart, offset - lineStart);
                                if (!text.startsWith("#")) {
                                    int idx = text.indexOf(':');
                                    if (idx < 0) {
                                        final ConfigurationMetadataProperty[] property = new ConfigurationMetadataProperty[]{null};
                                        ParserManager.parse(Collections.singleton(Source.create(lineDocument)), new UserTask() {
                                            public @Override void run(ResultIterator resultIterator) throws Exception {
                                                Parser.Result r = resultIterator.getParserResult();
                                                if (r instanceof ParserResult) {
                                                    Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                                                    if (language != null) {
                                                        StructureScanner scanner = language.getStructure();
                                                        if (scanner != null) {
                                                            List<? extends StructureItem> structures = scanner.scan((ParserResult) r);
                                                            List<StructureItem> context = getContext(structures, offset);
                                                            if (!context.isEmpty()) {
                                                                StructureItem item = context.get(context.size() - 1);
                                                                int start = (int) item.getPosition();
                                                                int end = (int) item.getPosition() + item.getName().length();
                                                                if (span != null && span.length == 2) {
                                                                    span[0] = start;
                                                                    span[1] = end;
                                                                }
                                                                if (start <= offset && offset <= end && item.getName().equals(lineDocument.getText(start, end - start))) {
                                                                    String propertyName = getPropertyName(context);
                                                                    for (Map.Entry<String, ConfigurationMetadataGroup> groupEntry : MicronautConfigProperties.getGroups(project).entrySet()) {
                                                                        String groupKey = groupEntry.getKey();
                                                                        if (groupKey.endsWith(".*")) {
                                                                            groupKey = groupKey.substring(0, groupKey.length() - 2);
                                                                        }
                                                                        if (Pattern.matches(groupKey.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\w*") + ".*", propertyName)) {
                                                                            ConfigurationMetadataGroup group = groupEntry.getValue();
                                                                            if (sources != null) {
                                                                                sources.addAll(group.getSources().values());
                                                                            }
                                                                            for (Map.Entry<String, ConfigurationMetadataProperty> propertyEntry : group.getProperties().entrySet()) {
                                                                                String propertyKey = propertyEntry.getKey();
                                                                                if (Pattern.matches(propertyKey.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\w*"), propertyName)) {
                                                                                    property[0] = propertyEntry.getValue();
                                                                                    return;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        return property[0];
                                    }
                                }
                            } else {
                                int lineEnd = LineDocumentUtils.getLineEnd(lineDocument, offset);
                                String text = lineDocument.getText(lineStart, lineEnd - lineStart);
                                if (!text.startsWith("#") && !text.startsWith("!")) {
                                    int colIdx = text.indexOf(':');
                                    int eqIdx = text.indexOf('=');
                                    int endIdx = Math.min(colIdx, eqIdx);
                                    if (endIdx < 0) {
                                        endIdx = Math.max(colIdx, eqIdx);
                                    }
                                    if (endIdx < 0 || offset < lineStart + endIdx) {
                                        if (endIdx > 0) {
                                            text = text.substring(0, endIdx);
                                        }
                                        String propertyName = text.trim();
                                        int idx = text.indexOf(propertyName);
                                        if (propertyName != null && !propertyName.isEmpty() && lineStart + idx <= offset && lineStart + idx + propertyName.length() >= offset) {
                                            for (Map.Entry<String, ConfigurationMetadataGroup> groupEntry : MicronautConfigProperties.getGroups(project).entrySet()) {
                                                String groupKey = groupEntry.getKey();
                                                if (groupKey.endsWith(".*")) {
                                                    groupKey = groupKey.substring(0, groupKey.length() - 2);
                                                }
                                                if (Pattern.matches(groupKey.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\w*") + ".*", propertyName)) {
                                                    ConfigurationMetadataGroup group = groupEntry.getValue();
                                                    if (sources != null) {
                                                        sources.addAll(group.getSources().values());
                                                    }
                                                    for (Map.Entry<String, ConfigurationMetadataProperty> propertyEntry : group.getProperties().entrySet()) {
                                                        String propertyKey = propertyEntry.getKey();
                                                        if (Pattern.matches(propertyKey.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\w*"), propertyName)) {
                                                            return propertyEntry.getValue();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void collectUsages(FileObject fo, String propertyName, Consumer<Usage> consumer) {
        try {
            String mimeType = fo.getMIMEType();
            if ("text/x-yaml".equals(mimeType)) {
                ParserManager.parse(Collections.singleton(Source.create(fo)), new UserTask() {
                    public @Override void run(ResultIterator resultIterator) throws Exception {
                        Parser.Result r = resultIterator.getParserResult();
                        if (r instanceof ParserResult) {
                            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                            if (language != null) {
                                StructureScanner scanner = language.getStructure();
                                if (scanner != null) {
                                    find(fo, propertyName, scanner.scan((ParserResult) r), r.getSnapshot().getText(), consumer);
                                }
                            }
                        }
                    }
                });
            } else {
                String[] lines = fo.asText().split("\n");
                Pattern pattern = Pattern.compile("^\\s*(" + propertyName.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\w*") + ")\\s*([=:].*)?$");
                int off = 0;
                for (String line : lines) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        consumer.accept(new Usage(fo, off + matcher.start(1), off + matcher.end(1), line.trim()));
                    }
                    off += line.length() + 1;
                }
            }
        } catch (ParseException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void find(FileObject fo, String propertyName, List<? extends StructureItem> structures, CharSequence content, Consumer<Usage> consumer) {
        int idx = propertyName.indexOf('.');
        String name = idx < 0 ? propertyName : propertyName.substring(0, idx);
        for (StructureItem structure : structures) {
            if ("*".equals(name) || name.equals(structure.getName())) {
                if (idx < 0) {
                    int start = (int) structure.getPosition();
                    int end = (int) structure.getEndPosition();
                    String text = content.subSequence(start, end).toString();
                    consumer.accept(new Usage(fo, start, end, text));
                } else {
                    find(fo, propertyName.substring(idx + 1), structure.getNestedItems(), content, consumer);
                }
            }
        }
    }

    private static List<StructureItem> getContext(List<? extends StructureItem> structure, int offset) {
        List<StructureItem> context = new ArrayList<>();
        loop: while (structure != null && !structure.isEmpty()) {
            for (StructureItem item : structure) {
                if (item.getPosition() <= offset && offset <= item.getEndPosition()) {
                    context.add(item);
                    structure = item.getNestedItems();
                    continue loop;
                }
            }
            structure = null;
        }
        return context;
    }

    private static String getPropertyName(List<? extends StructureItem> context) {
        StringBuilder sb = new StringBuilder();
        for (StructureItem item : context) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(item.getName());
        }
        return sb.toString();
    }

    public static final class Usage {

        private final FileObject fileObject;
        private final int startOffset;
        private final int endOffset;
        private final String text;

        public Usage(FileObject fileObject, int startOffset, int endOffset, String text) {
            this.fileObject = fileObject;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.text = text;
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public String getText() {
            return text;
        }
    }
}
