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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
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

    public static final String YAML_MIME = "text/x-yaml";
    public static final String PROPERTIES_MIME = "text/x-properties";

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
                            if (YAML_MIME.equals(mimeType)) {
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
                                                                    property[0] = getProperty(MicronautConfigProperties.getGroups(project), getPropertyName(context), sources);
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
            if (YAML_MIME.equals(mimeType)) {
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
    
    public static List<int[]> getPropertySpans(Project project, Parser.Result r) {
        List<int[]> spans = new ArrayList<>();
        if (r instanceof ParserResult) {
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(r.getSnapshot().getMimeType());
            if (language != null) {
                StructureScanner scanner = language.getStructure();
                if (scanner != null) {
                    Map<String, ConfigurationMetadataGroup> groups = MicronautConfigProperties.getGroups(project);
                    scan(scanner.scan((ParserResult) r), new Stack<>(), context -> {
                        if (!context.empty()) {
                            String propertyName = getPropertyName(context);
                            List<ConfigurationMetadataSource> sources = new ArrayList<>();
                            ConfigurationMetadataProperty property = getProperty(groups, propertyName, sources);
                            if (property != null || !sources.isEmpty()) {
                                StructureItem item = context.peek();
                                spans.add(new int[] {(int) item.getPosition(), (int) item.getPosition() + item.getName().length()});
                            }
                        }
                        return true;
                    });
                }
            }
        }
        return spans;
    }

    public static ConfigurationMetadataProperty getProperty(Map<String, ConfigurationMetadataGroup> groups, String propertyName, List<ConfigurationMetadataSource> sources) {
        for (Map.Entry<String, ConfigurationMetadataGroup> groupEntry : groups.entrySet()) {
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
        return null;
    }

    public static ElementHandle getElementHandle(ClasspathInfo cpInfo, String typeName, String propertyName, AtomicBoolean cancel) {
        ElementHandle[] handle = new ElementHandle[1];
        if (typeName != null) {
            handle[0] = ElementHandle.createTypeElementHandle(ElementKind.CLASS, typeName);
            if (cpInfo != null && propertyName != null) {
                try {
                    JavaSource.create(cpInfo).runUserActionTask(controller -> {
                        if (cancel != null && cancel.get()) {
                            return;
                        }
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TypeElement te = (TypeElement) handle[0].resolve(controller);
                        if (te != null) {
                            ElementHandle found = null;
                            String name = "set" + propertyName.replace("-", "");
                            for (ExecutableElement executableElement : ElementFilter.methodsIn(te.getEnclosedElements())) {
                                if (name.equalsIgnoreCase(executableElement.getSimpleName().toString())) {
                                    found = ElementHandle.create(executableElement);
                                    break;
                                }
                            }
                            if (found == null) {
                                TypeElement typeElement = controller.getElements().getTypeElement("io.micronaut.context.annotation.Property");
                                for (VariableElement variableElement : ElementFilter.fieldsIn(te.getEnclosedElements())) {
                                    for (AnnotationMirror annotationMirror : variableElement.getAnnotationMirrors()) {
                                        if (typeElement == annotationMirror.getAnnotationType().asElement()) {
                                            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                                                if ("name".contentEquals(entry.getKey().getSimpleName()) && propertyName.equals(entry.getValue().getValue())) {
                                                    found = ElementHandle.create(variableElement);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (found != null) {
                                handle[0] = found;
                            }
                        }
                    }, true);
                } catch (IOException ex) {}
            }
        }
        return handle[0];
    }

    private static void scan(List<? extends StructureItem> structures, Stack<StructureItem> context, Function<Stack<StructureItem>, Boolean> visitor) {
        for (StructureItem structure : structures) {
            if (structure != null) {
                try {
                    context.push(structure);
                    if (visitor.apply(context)) {
                        scan(structure.getNestedItems(), context, visitor);
                    }
                } finally {
                    context.pop();
                }
            }
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
