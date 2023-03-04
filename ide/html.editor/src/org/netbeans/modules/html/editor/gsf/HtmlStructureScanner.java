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
package org.netbeans.modules.html.editor.gsf;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import static org.netbeans.modules.html.editor.gsf.Bundle.*;
import org.netbeans.modules.web.common.api.Lines;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author mfukala@netbeans.org
 */
public class HtmlStructureScanner implements StructureScanner {

    /**
     * Tag fold type. Overrides the default label.
     */
    @NbBundle.Messages("FT_Tag=Tags")
    public static final FoldType TYPE_TAG = FoldType.TAG.override(
            FT_Tag(), FoldType.TAG.getTemplate());
    /**
     * HTML comments
     */
    public static final FoldType TYPE_COMMENT = FoldType.COMMENT;
    private static final Logger LOGGER = Logger.getLogger(HtmlStructureScanner.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private static final long MAX_SNAPSHOT_SIZE = 4 * 1024 * 1024;
    
    private Reference<Pair<ParserResult, List<HtmlStructureItem>>> cache;
    
    private static boolean isOfSupportedSize(ParserResult info) {
        Snapshot snapshot = info.getSnapshot();
        int slen = snapshot.getText().length();
        return slen < MAX_SNAPSHOT_SIZE;
    }
    
    @Override
    public List<? extends StructureItem> scan(final ParserResult info) {
        //temporary workaround for 
        //Bug 211139 - HtmlStructureScanner.scan() called twice with the same ParserResult 
        //so it is easier to debug
        if (cache != null) {
            Pair<ParserResult, List<HtmlStructureItem>> pair = cache.get();
            if (pair != null) {
                if (info == pair.first()) {
                    return pair.second();
                }
            }
        }

        if (!isOfSupportedSize(info)) {
            return Collections.emptyList();
        }

        HtmlParserResult presult = (HtmlParserResult) info;
        Node root = ((HtmlParserResult) presult).root();

        if (LOG) {
            LOGGER.log(Level.FINE, "HTML parser tree output:");
            LOGGER.log(Level.FINE, root.toString());
        }

        Snapshot snapshot = info.getSnapshot();
        FileObject file = snapshot.getSource().getFileObject();
        List<HtmlStructureItem> elements = new ArrayList<>();
        for (OpenTag tag : root.children(OpenTag.class)) {
            if (!(ElementUtils.isVirtualNode(tag) && HtmlStructureItem.gatherNonVirtualChildren(tag).isEmpty())) { //ignore childless virtual elements
                HtmlElementHandle handle = new HtmlElementHandle(tag, file);
                HtmlStructureItem si = new HtmlStructureItem(tag, handle, snapshot);
                elements.add(si);
            }
        }

        //cache
        Pair<ParserResult, List<HtmlStructureItem>> pair = Pair.of(info, elements);
        cache = new WeakReference<>(pair);

        return elements;

    }

    @Override
    public Map<String, List<OffsetRange>> folds(final ParserResult info) {
        //this method needs to run under the document readlock
        if (!isOfSupportedSize(info)) {
            return Collections.emptyMap();
        }
        final BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(true);
        if (doc == null) {
            return Collections.emptyMap();
        }
        final int maxLen = doc.getLength();
        final Map<String, List<OffsetRange>> folds = new HashMap<>();
        final List<OffsetRange> tags = new ArrayList<>();

        final Lines lines = new Lines(info.getSnapshot().getText()); //lines for embedded source
        ElementVisitor foldsSearch = new ElementVisitor() {
            @Override
            public void visit(Element node) {
                if (node.type() == ElementType.OPEN_TAG) {
                    try {

                        int from = node.from();
                        int to = node.type() == ElementType.OPEN_TAG
                                ? ((OpenTag) node).semanticEnd()
                                : node.to();

                        OffsetRange range = convertAndCheck(from, to, info.getSnapshot(), lines, maxLen);
                        if (range != null) {
                            tags.add(range);
                        }

                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    }
                }
            }
        };

        Collection<Node> roots = ((HtmlParserResult) info).roots().values();
        for (Node root : roots) {
            ElementUtils.visitChildren(root, foldsSearch);
        }
        folds.put(TYPE_TAG.code(), tags);

        //comments are not present in the parse trees so needs to be handle separately
        List<OffsetRange> comments = new ArrayList<>();
        HtmlParserResult result = (HtmlParserResult) info;
        Iterator<Element> elementsIterator = result.getSyntaxAnalyzerResult().getElementsIterator();
        while (elementsIterator.hasNext()) {
            Element element = elementsIterator.next();
            if (ElementType.COMMENT == element.type()) {
                try {
                    OffsetRange range = convertAndCheck(element.from(), element.to(), info.getSnapshot(), lines, maxLen);
                    if (range != null) {
                        comments.add(range);
                    }
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }
        folds.put(TYPE_COMMENT.code(), comments);

        return folds;
    }

    private OffsetRange convertAndCheck(int embeddedFrom, int embeddedTo, Snapshot snapshot, Lines lines, int maxLen) throws BadLocationException {
        if(embeddedFrom == -1 || embeddedTo == -1) {
            return null;
        }
        if (lines.getLineIndex(embeddedFrom) == lines.getLineIndex(embeddedTo)) { //comparing embedded offsets
            //do not create one line folds
            //XXX this logic could possibly seat in the GSF folding impl.
            return null;
        }
        int so = snapshot.getOriginalOffset(embeddedFrom);
        int eo = snapshot.getOriginalOffset(embeddedTo);
        if (so == -1 || eo == -1) {
            //cannot be mapped back properly
            return null;
        }
        if (eo > maxLen) {
            eo = maxLen;
        }
        if (so > eo) {
            so = eo;
        }
        return new OffsetRange(so, eo);
    }

    private static int documentPosition(int astOffset, Snapshot snapshot) {
        return snapshot.getOriginalOffset(astOffset);
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(false, false, 0);
    }
}
