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

import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataSource;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-yaml", service = HyperlinkProviderExt.class)
public class MicronautConfigHyperlinkProvider implements HyperlinkProviderExt {

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        int[] span = new int[2];
        ConfigurationMetadataProperty property = resolve(doc, offset, span, null);
        return property != null ? span : null;
    }

    @Override
    @NbBundle.Messages("LBL_GoToDeclaration=Go to Declaration")
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        AtomicBoolean cancel = new AtomicBoolean();
        BaseProgressUtils.runOffEventDispatchThread(() -> {
            List<ConfigurationMetadataSource> sources = new ArrayList<>();
            ConfigurationMetadataProperty property = resolve(doc, offset, null, sources);
            if (property != null && !sources.isEmpty()) {
                ClasspathInfo cpInfo = ClasspathInfo.create(doc);
                if (cpInfo != null) {
                    ElementHandle[] handle = new ElementHandle[1];
                    try {
                        JavaSource.create(cpInfo).runUserActionTask(controller -> {
                            if (cancel.get()) {
                                return;
                            }
                            handle[0] = ElementHandle.createTypeElementHandle(ElementKind.CLASS, sources.get(0).getType());
                            TypeElement te = (TypeElement) handle[0].resolve(controller);
                            if (te != null) {
                                String name = "set" + property.getName().replaceAll("-", "");
                                for (ExecutableElement executableElement : ElementFilter.methodsIn(te.getEnclosedElements())) {
                                    if (name.equalsIgnoreCase(executableElement.getSimpleName().toString())) {
                                        handle[0] = ElementHandle.create(executableElement);
                                        break;
                                    }
                                }
                            }
                        }, true);
                    } catch (IOException ex) {}
                    if (handle[0] != null && ElementOpen.open(cpInfo, handle[0])) {
                        return;
                    }
                }
            }
            Toolkit.getDefaultToolkit().beep();
        }, Bundle.LBL_GoToDeclaration(), cancel, false);
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        ConfigurationMetadataProperty property = resolve(doc, offset, null, null);
        if (property != null) {
            StringBuilder sb = new StringBuilder("<html><body>");
            sb.append("<b>").append(property.getId().replace(".", /* ZERO WIDTH SPACE */".&#x200B;")).append("</b>");
            String propertyType = property.getType();
            if (propertyType != null) {
                sb.append("<pre>").append(propertyType).append("</pre>");
            }
            return sb.toString();
        }
        return null;
    }

    static ConfigurationMetadataProperty resolve(Document doc, int offset, int[] span, List<ConfigurationMetadataSource> sources) {
        LineDocument lineDocument = LineDocumentUtils.as(doc, LineDocument.class);
        if (lineDocument != null) {
            FileObject fo = EditorDocumentUtils.getFileObject(doc);
            if (fo != null && "application.yml".equalsIgnoreCase(fo.getNameExt())) {
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    final MicronautConfigProperties configProperties = project.getLookup().lookup(MicronautConfigProperties.class);
                    if (configProperties != null) {
                        try {
                            int lineStart = LineDocumentUtils.getLineStart(lineDocument, offset);
                            String text = lineDocument.getText(lineStart, offset - lineStart);
                            if (!text.contains("#")) {
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
                                                                Map<String, ConfigurationMetadataGroup> groups = configProperties.getGroups();
                                                                for (Map.Entry<String, ConfigurationMetadataGroup> groupEntry : configProperties.getGroups().entrySet()) {
                                                                    String groupKey = groupEntry.getKey();
                                                                    if (Pattern.matches(groupKey.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\w*") + ".*", propertyName)) {
                                                                        ConfigurationMetadataGroup group = groupEntry.getValue();
                                                                        for (Map.Entry<String, ConfigurationMetadataProperty> propertyEntry : group.getProperties().entrySet()) {
                                                                            String propertyKey = propertyEntry.getKey();
                                                                            if (Pattern.matches(propertyKey.replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\\\w*"), propertyName)) {
                                                                                property[0] = propertyEntry.getValue();
                                                                                if (sources != null) {
                                                                                    sources.addAll(group.getSources().values());
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
                                        }
                                    });
                                    return property[0];
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
}
