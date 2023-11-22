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
package org.netbeans.modules.micronaut.hyperlink;

import java.awt.Toolkit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.HyperlinkLocation;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.modules.micronaut.completion.MicronautConfigDocumentation;
import org.netbeans.modules.micronaut.expression.MicronautExpressionLanguageParser;
import org.netbeans.modules.micronaut.expression.MicronautExpressionLanguageUtilities;
import org.netbeans.spi.lsp.HyperlinkLocationProvider;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = HyperlinkProviderExt.class, position = 1220)
public class MicronautExpressionHyperlinkProvider implements HyperlinkProviderExt {

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
        AtomicReference<int[]> ret = new AtomicReference<>();
        if (EditorDocumentUtils.getFileObject(doc) != null) {
            doc.render(() -> {
                TokenSequence<JavaTokenId> javaTS = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset);
                if (javaTS != null && javaTS.moveNext() && javaTS.token().id() == JavaTokenId.STRING_LITERAL) {
                    int off = offset - javaTS.offset();
                    Matcher matcher = MicronautExpressionLanguageParser.MEXP_PATTERN.matcher(javaTS.token().text());
                    while (matcher.find() && matcher.groupCount() == 1) {
                        if (off >= matcher.start(1) && off <= matcher.end(1)) {
                            TokenHierarchy<String> th = TokenHierarchy.create(matcher.group(1), Language.find("text/x-micronaut-el"));
                            TokenSequence<?> ts = th != null ? th.tokenSequence() : null;
                            if (ts != null) {
                                int d = ts.move(off - matcher.start(1));
                                if (d == 0 ? ts.movePrevious() : ts.moveNext()) {
                                    List<String> categories = (List<String>) ts.token().getProperty("categories");
                                    if (categories != null && categories.size() > 1) {
                                        String category = categories.get(categories.size() - 1);
                                        switch (category) {
                                            case "string.quoted.single.mexp":
                                                if (categories.size() <= 2 || !"meta.environment-access.argument.mexp".equals(categories.get(categories.size() - 2))) {
                                                    break;
                                                }
                                            case "entity.name.function.mexp":
                                            case "variable.other.object.property.mexp":
                                            case "storage.type.java":
                                                ret.set(new int [] {ts.offset() + matcher.start(1) + javaTS.offset(), ts.offset() + ts.token().length() + matcher.start(1) + javaTS.offset()});
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
        return ret.get();
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        AtomicBoolean cancel = new AtomicBoolean();
        BaseProgressUtils.runOffEventDispatchThread(() -> {
            MicronautExpressionLanguageUtilities.resolve(doc, offset, (info, element) -> {
                return ElementOpen.open(info.getClasspathInfo(), element);
            }, (property, source) -> {
                ClasspathInfo cpInfo = ClasspathInfo.create(doc);
                ElementHandle handle = source != null ? MicronautConfigUtilities.getElementHandle(cpInfo, source.getType(), property.getName(), cancel) : null;
                if (handle == null || !ElementOpen.open(cpInfo, handle)) {
                    Toolkit.getDefaultToolkit().beep();
                }
                return null;
            });
        }, Bundle.LBL_GoToDeclaration(), cancel, false);
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return MicronautExpressionLanguageUtilities.resolve(doc, offset, (info, element) -> {
            return "<html><body>" + MicronautExpressionLanguageUtilities.getJavadocText(info, element, true, 1);
        }, (property, source) -> {
            return "<html><body>" + new MicronautConfigDocumentation(property).getText();
        });
    }

    @MimeRegistration(mimeType = "text/x-java", service = HyperlinkLocationProvider.class)
    public static class LocationProvider implements HyperlinkLocationProvider {

        @Override
        public CompletableFuture<HyperlinkLocation> getHyperlinkLocation(Document doc, int offset) {
            CompletableFuture<ElementOpen.Location> future = MicronautExpressionLanguageUtilities.resolve(doc, offset, (info, element) -> {
                TypeElement typeElement = info.getElementUtilities().outermostTypeElement(element);
                if (typeElement != null) {
                    return ElementOpen.getLocation(info.getClasspathInfo(), ElementHandle.create(element), typeElement.getQualifiedName().toString().replace('.', '/') + ".class");
                }
                return null;
            }, (property, source) -> {
                if (source != null) {
                    ClasspathInfo cpInfo = ClasspathInfo.create(doc);
                    String typeName = source.getType();
                    ElementHandle handle = MicronautConfigUtilities.getElementHandle(cpInfo, typeName, property.getName(), null);
                    if (handle != null) {
                        return ElementOpen.getLocation(cpInfo, handle, typeName.replace('.', '/') + ".class");
                    }
                }
                return null;
            });
            return future != null ? future.thenApply(location -> {
                return HyperlinkLocationProvider.createHyperlinkLocation(location.getFileObject(), location.getStartOffset(), location.getEndOffset());
            }) : CompletableFuture.completedFuture(null);
        }
    }
}
