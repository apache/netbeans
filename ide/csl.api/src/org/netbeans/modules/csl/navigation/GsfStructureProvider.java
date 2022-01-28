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
import java.util.Set;
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
    
    /** The structure element implementation. Some properties are counted lazy. 
     * 
     */
    private static class GsfStructureElement implements StructureElement {
        
        private final static Set<StructureElement.Tag> DEPRECATED_TAG = Collections.singleton(StructureElement.Tag.Deprecated);
        
        private final Document doc;         // The children are counted lazily and we need the document for it. 
        private final StructureItem origItem;
        private List<GsfStructureElement> children;
        private String signature;
        private int expandedStartOffset;
        private int expandedEndOffset;
        

        public GsfStructureElement(Document doc, StructureItem origItem) {
            this.doc = doc;
            this.origItem = origItem;
            this.signature = null;
            this.children = null;
            this.expandedStartOffset = (int)origItem.getPosition();
            this.expandedEndOffset = (int)origItem.getEndPosition();
        }
        
        @Override
        public String getName() {
            return origItem.getName();
        }

        @Override
        public int getSelectionStartOffset() {
            return (int)origItem.getPosition();
        }

        @Override
        public int getSelectionEndOffset() {
            return (int)origItem.getEndPosition();
        }
        
        @Override
        public int getExpandedStartOffset() {
            return expandedStartOffset;
        }

        protected void setExpandedStartOffset(int enclosedStartOffset) {
            this.expandedStartOffset = enclosedStartOffset;
        }

        @Override
        public int getExpandedEndOffset() {
            return expandedEndOffset;
        }
        
        protected void setExpandedEndOffset(int enclosedEndOffset) {
            this.expandedEndOffset = enclosedEndOffset;
        }

        
        @Override
        public String getDetail() { 
            if (signature == null) {
                createSignature();
            }
            return signature;
        }

        private void createSignature() {
            NoHtmlFormatter formatter = new NoHtmlFormatter();
            String s = origItem.getHtml(formatter);
            signature = s.substring(getName().length()).trim();
        }
        
        @Override
        public StructureElement.Kind getKind() {
            switch(origItem.getKind()) {
                case ATTRIBUTE: return Kind.Property;
                case CALL: return Kind.Event;
                case CLASS: return Kind.Class;
                case CONSTANT: return Kind.Constant;
                case CONSTRUCTOR: return Kind.Constructor;
                case DB: return Kind.File;
                case ERROR: return Kind.Event;
                case METHOD: return Kind.Method;
                case FILE: return Kind.File;
                case FIELD: return Kind.Field;
                case MODULE: return Kind.Module;
                case VARIABLE: return Kind.Variable;
                case GLOBAL: return Kind.Module;
                case INTERFACE: return Kind.Interface;
                case KEYWORD: return Kind.Key;
                case OTHER: return Kind.Object;
                case PACKAGE: return Kind.Package;
                case PARAMETER: return Kind.TypeParameter;
                case PROPERTY: return Kind.Property;
                case RULE: return Kind.Event;
                case TAG: return Kind.Operator;
                case TEST: return Kind.Function;
            }
            return Kind.Object;
        }
        
        @Override
        public Set<Tag> getTags() {
            if (origItem.getModifiers().contains(Modifier.DEPRECATED)) {
                return DEPRECATED_TAG;
            }
            return null;
        }
        
        @Override
        public List<? extends StructureElement> getChildren() {
            if (children == null) {
                if (origItem.isLeaf()) {
                    children = Collections.EMPTY_LIST;
                } else {
                    List <? extends StructureItem> origChildren = origItem.getNestedItems();
                    if (origChildren.isEmpty()) {
                        children = Collections.EMPTY_LIST;
                    } else {
                        children = new ArrayList<>(origChildren.size());
                        convertStructureItems(doc, origChildren, children);
                    }
                    
                }
            }
            return children;
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
            String res = htmlText.replaceAll( "<[^>]*>", "" ); // NOI18N // NOI18N
            res = res.replaceAll( "&nbsp;", " " ); // NOI18N // NOI18N
            res = res.trim();
            return res;
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
    public CompletableFuture<List<? extends StructureElement>> getStructure(Document doc) {
        final List<GsfStructureElement> sElements = new ArrayList<>();
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
            return CompletableFuture.completedFuture(sElements);
        } catch (ParseException ex) {
            LOGGER.log(Level.FINE, null, ex);
            return CompletableFuture.completedFuture(null);
        }
    }
    
    private static void convertStructureItems(Document doc, List<? extends StructureItem> items, List<GsfStructureElement> sElements) {
        GsfStructureElement lastElement = null;
        if (doc instanceof LineDocument) {
            //  if it's line document, we can set the enclosing range for whole line
            LineDocument ldoc = (LineDocument)doc;
            for (StructureItem item : items) {
                int startOffset = (int)item.getPosition();
                int lineStart = startOffset;
                int lineEnd = (int)item.getEndPosition();
                try {
                    String prefix = doc.getText(lineStart, startOffset);
                    lineStart = LineDocumentUtils.getLineStart(ldoc, lineStart);
                    if (prefix.trim().isEmpty()) {
                        lineEnd = LineDocumentUtils.getLineEnd(ldoc, lineEnd);
                    } else {
                        lineStart = startOffset;
                    }
                    
                } catch (BadLocationException ex) {
                    lineStart = (int)item.getPosition();
                    lineEnd = (int)item.getEndPosition();
                }
                GsfStructureElement el = new GsfStructureElement(ldoc, item);
                sElements.add(el);
                if (lastElement == null) {
                    el.setExpandedStartOffset(lineStart);
                    el.setExpandedEndOffset(lineEnd);
                } else {
                    if (lastElement.getExpandedEndOffset() < lineStart) {
                        el.setExpandedStartOffset(lineStart);
                        el.setExpandedEndOffset(lineEnd);
                    } else if (lastElement.getExpandedStartOffset() <= lineStart && lineEnd == lastElement.getExpandedEndOffset()) {
                        // The same line
                        lastElement.setExpandedEndOffset(el.getSelectionStartOffset() - 1);
                        lineStart = el.getSelectionStartOffset();
                    }
                }
                el.setExpandedStartOffset(lineStart);
                el.setExpandedEndOffset(lineEnd);
                lastElement = el;
            }
        } else {
            for (StructureItem item : items) {
                sElements.add(new GsfStructureElement(null, item));
            }
        }
    }
    
}
