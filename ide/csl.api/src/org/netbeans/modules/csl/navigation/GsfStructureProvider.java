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
package org.netbeans.modules.csl.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
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
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.spi.lsp.StructureProvider;

/**
 * Implementation of StructureProvider to supply outline view in VSCode
 * @author Petr Pisl
 */
public class GsfStructureProvider implements StructureProvider {

    private static final Logger LOGGER = Logger.getLogger(GsfStructureProvider.class.getName());
    
    private static StructureElement.Kind convertKind(ElementKind elementKind) {
        switch(elementKind) {
            case ATTRIBUTE: return StructureElement.Kind.Property;
            case CALL: return StructureElement.Kind.Event;
            case CLASS: return StructureElement.Kind.Class;
            case CONSTANT: return StructureElement.Kind.Constant;
            case CONSTRUCTOR: return StructureElement.Kind.Constructor;
            case DB: return StructureElement.Kind.File;
            case ERROR: return StructureElement.Kind.Event;
            case METHOD: return StructureElement.Kind.Method;
            case FILE: return StructureElement.Kind.File;
            case FIELD: return StructureElement.Kind.Field;
            case MODULE: return StructureElement.Kind.Module;
            case VARIABLE: return StructureElement.Kind.Variable;
            case GLOBAL: return StructureElement.Kind.Module;
            case INTERFACE: return StructureElement.Kind.Interface;
            case KEYWORD: return StructureElement.Kind.Key;
            case OTHER: return StructureElement.Kind.Object;
            case PACKAGE: return StructureElement.Kind.Package;
            case PARAMETER: return StructureElement.Kind.Variable;
            case PROPERTY: return StructureElement.Kind.Property;
            case RULE: return StructureElement.Kind.Event;
            case TAG: return StructureElement.Kind.Operator;
            case TEST: return StructureElement.Kind.Function;
        }
        return StructureElement.Kind.Object;
    }
    
    private static void  createDetail(StructureItem item, Builder builder) {
        NoHtmlFormatter formatter = new NoHtmlFormatter();
        String s = item.getHtml(formatter);
        s = s.substring(item.getName().length()).trim();
        if (!s.trim().isEmpty()) {
            builder.detail(s);
        }
    }
    
    private static void createTags(StructureItem item, Builder builder) {
        if (item.getModifiers().contains(Modifier.DEPRECATED)) {
            builder.addTag(StructureElement.Tag.Deprecated);
        }
    }
    
    private static void createChildren(Document doc, StructureItem item, Builder builder) {
        if (!item.isLeaf()) {
            List <? extends StructureItem> origChildren = item.getNestedItems();
            if (!origChildren.isEmpty()) {
                List<StructureElement> children = new ArrayList<>(origChildren.size());
                convertStructureItems(doc, origChildren, children);
                builder.children(children);
            }
        }
    }
    
    /** A formatter that strips the html elements from the text */
    static private class NoHtmlFormatter extends HtmlFormatter {
        StringBuilder sb = new StringBuilder();
        
        @Override
        public void reset() {
            sb = new StringBuilder();
        }

        static String stripHtml( String htmlText ) {
            if( null == htmlText )
                return null;
            return htmlText.replaceAll( "<[^>]*>", "" ) // NOI18N
                           .replace( "&nbsp;", " " ) // NOI18N
                           .trim();
        }
        
        @Override
        public void appendHtml(String html) {
            sb.append(stripHtml(html));
        }

        @Override
        public void appendText(String text, int fromInclusive, int toExclusive) {
            int l = toExclusive - fromInclusive;
            if (sb.length() + l < maxLength) {
                sb.append(text.subSequence(fromInclusive, toExclusive));
            } else {
                sb.append(text.subSequence(fromInclusive, toExclusive - (l - maxLength)));
                sb.append("...");   //NOI18N
            }
        }

        @Override
        public void emphasis(boolean start) {
        }

        @Override
        public void name(ElementKind kind, boolean start) {
        }

        @Override
        public void parameters(boolean start) {
        }

        @Override
        public void active(boolean start) {
        }

        @Override
        public void type(boolean start) {
        }

        @Override
        public void deprecated(boolean start) {
        }

        @Override
        public String getText() {
            return sb.toString();
        }
    }
    
    @Override
    public List<StructureElement> getStructure(Document doc) {
        final List<StructureElement> sElements = new ArrayList<>();
        try {
            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result result = resultIterator.getParserResult(-1);
                    if(result instanceof ParserResult) {
                        ParserResult parserResult = (ParserResult) result;
                        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                        if (language != null) {
                            StructureScanner scanner = language.getStructure();
                            if (scanner != null) {
                                List<? extends StructureItem> items = scanner.scan(parserResult);
                                convertStructureItems(doc, items, sElements);
                            }
                        }
                    }
                }

            });
            return sElements;
        } catch (ParseException ex) {
            LOGGER.log(Level.FINE, null, ex);
            return Collections.EMPTY_LIST;
        }
    }
    
    private static void convertStructureItems(Document doc, List<? extends StructureItem> items, List<StructureElement> sElements) {
        StructureElement lastElement = null;
        if (doc instanceof LineDocument) {
            //  if it's line document, we can set the enclosing range for whole line
            LineDocument ldoc = (LineDocument)doc;
            for (StructureItem item : items) {
                int startOffset = (int)item.getPosition();
                int lineStart = startOffset;
                int lineEnd = (int)item.getEndPosition();
                Builder builder = StructureProvider.newBuilder(item.getName(), convertKind(item.getKind()));
                builder.selectionStartOffset(startOffset).selectionEndOffset(lineEnd);
                createDetail(item, builder);
                createTags(item, builder);

                try {
                    String prefix = doc.getText(lineStart, startOffset);
                    lineStart = LineDocumentUtils.getLineStart(ldoc, lineStart);
                    if (prefix.trim().isEmpty()) {
                        lineEnd = LineDocumentUtils.getLineEndOffset(ldoc, lineEnd);
                    }
                } catch (BadLocationException ex) {
                    lineStart = startOffset;
                    lineEnd = (int)item.getEndPosition();
                }
                if (lastElement == null) {
                    builder.expandedStartOffset(lineStart);
                    builder.expandedEndOffset(lineEnd);
                } else {
                    if (lastElement.getExpandedEndOffset() < lineStart) {
                        builder.expandedStartOffset(lineStart);
                        builder.expandedEndOffset(lineEnd);
                    } else if (lastElement.getExpandedStartOffset() <= lineStart && lineEnd == lastElement.getExpandedEndOffset()) {
                        // The same line
                        sElements.remove(lastElement);
                        Builder leBuilder = StructureProvider.copy(lastElement);
                        leBuilder.expandedEndOffset(startOffset - 1);
                        sElements.add(leBuilder.build());
                    }
                }
                builder.expandedStartOffset(lineStart).expandedEndOffset(lineEnd);
                createChildren(doc, item, builder);
                lastElement = builder.build();
                sElements.add(lastElement);
            }
        } else {
            for (StructureItem item : items) {
                int selectionStart = (int)item.getPosition();
                int selectionEnd = (int)item.getEndPosition();
                Builder builder = StructureProvider.newBuilder(item.getName(), convertKind(item.getKind()));
                builder.selectionStartOffset(selectionStart).selectionEndOffset(selectionEnd);
                builder.expandedStartOffset(selectionStart).expandedEndOffset(selectionEnd);
                createDetail(item, builder);
                createTags(item, builder);
                createChildren(doc, item, builder);
                sElements.add(builder.build());
            }
        }
    }
    
}
