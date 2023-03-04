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
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
public class MicronautConfigValidator implements HintsProvider {

    @Override
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
    }

    @Override
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {
    }

    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        FileObject fo = EditorDocumentUtils.getFileObject(context.doc);
        if (MicronautConfigUtilities.isMicronautConfigFile(fo)) {
            final ParserResult parserResult = context.parserResult;
            final List<? extends Error> diagnostics = parserResult.getDiagnostics();
            if (diagnostics != null && !diagnostics.isEmpty()) {
                unhandled.addAll(diagnostics);
            }
            final Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                if (MicronautConfigProperties.hasConfigMetadata(project)) {
                    final Language language = LanguageRegistry.getInstance().getLanguageByMimeType(parserResult.getSnapshot().getMimeType());
                    if (language != null) {
                        final StructureScanner scanner = language.getStructure();
                        if (scanner != null) {
                            final List<? extends StructureItem> structures = scanner.scan(parserResult);
                            if (!structures.isEmpty()) {
                                final Map<String, ConfigurationMetadataProperty> properties = MicronautConfigProperties.getProperties(project);
                                if (!properties.isEmpty()) {
                                    validate(parserResult, structures, "", properties, unhandled);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public List<Rule> getBuiltinRules() {
        return null;
    }

    @Override
    public RuleContext createRuleContext() {
        return new RuleContext();
    }

    @NbBundle.Messages({
        "MSG_InvalidValue=Invalid value \"{0}\", must be of type \"{1}\"",
    })
    private static void validate(ParserResult parserResult, List<? extends StructureItem> structure, String name, Map<String, ConfigurationMetadataProperty> properties, List<Error> errors) {
        final FileObject fileObject = parserResult.getSnapshot().getSource().getFileObject();
        final CharSequence text = parserResult.getSnapshot().getText();
        for (StructureItem item : structure) {
            final String fullName = name.isEmpty() ? item.getName() : name + '.' + item.getName();
            List<? extends StructureItem> nestedItems = item.getNestedItems();
            if (nestedItems.isEmpty()) {
                final ConfigurationMetadataProperty property = properties.get(fullName);
                if (property != null) {
                    final String type = property.getType();
                    if (type != null && !"java.lang.String".equals(type)) {
                        try {
                            final String itemText = text.subSequence((int) item.getPosition(), (int) item.getEndPosition()).toString();
                            int idx = itemText.lastIndexOf(':');
                            if (idx >= 0) {
                                final String value = itemText.substring(idx + 1).trim();
                                if (!value.isEmpty()) {
                                    int start = (int) item.getPosition() + itemText.indexOf(value, idx + 1);
                                    int end = start + value.length();
                                    switch (type) {
                                        case "boolean":
                                            if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                                                errors.add(DefaultError.createDefaultError(null, Bundle.MSG_InvalidValue(value, type), null, fileObject, start, end, false, Severity.ERROR));
                                            }
                                            break;
                                        case "java.lang.Integer":
                                            try {
                                                Integer.parseInt(value);
                                            } catch (NumberFormatException e) {
                                                errors.add(DefaultError.createDefaultError(null, Bundle.MSG_InvalidValue(value, type), null, fileObject, start, end, false, Severity.ERROR));
                                            }
                                            break;
                                        case "java.lang.Long":
                                            try {
                                                Long.parseLong(value);
                                            } catch (NumberFormatException e) {
                                                errors.add(DefaultError.createDefaultError(null, Bundle.MSG_InvalidValue(value, type), null, fileObject, start, end, false, Severity.ERROR));
                                            }
                                            break;
                                        default:
                                            JavaSource js = JavaSource.create(ClasspathInfo.create(fileObject));
                                            if (js != null) {
                                                try {
                                                    js.runUserActionTask(cc -> {
                                                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                                        TypeElement typeElement = cc.getElements().getTypeElement(type);
                                                        if (typeElement != null && typeElement.getKind() == ElementKind.ENUM) {
                                                            if (!typeElement.getEnclosedElements().stream().anyMatch(e -> e.getKind() == ElementKind.ENUM_CONSTANT && value.contentEquals(e.getSimpleName()))) {
                                                                errors.add(DefaultError.createDefaultError(null, Bundle.MSG_InvalidValue(value, type), null, fileObject, start, end, false, Severity.ERROR));
                                                            }
                                                        }
                                                    }, true);
                                                } catch (IOException ioe) {
                                                    Exceptions.printStackTrace(ioe);
                                                }
                                            }
                                    }
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {
                        }
                    }
                }
            } else {
                validate(parserResult, nestedItems, fullName, properties, errors);
            }
        }
    }
}
