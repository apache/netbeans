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
package org.netbeans.modules.css.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import javax.swing.text.BadLocationException;
import org.netbeans.lib.editor.util.CharSubSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Instances of this class represents a css model associated with a snapshot of
 * the file content.
 *
 * TODO: make it CssIndexModel so it uses the generic mechanism.
 *
 * @author mfukala@netbeans.org
 */
public class CssFileModel {

    private Collection<Entry> classes, ids, htmlElements, imports, colors;
    private final Snapshot snapshot;
    private final Snapshot topLevelSnapshot;

    public static CssFileModel create(Source source) throws ParseException {
        final AtomicReference<CssFileModel> model = new AtomicReference<>();
        ParserManager.parse(Collections.singletonList(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator cssRi = WebUtils.getResultIterator(resultIterator, CssLanguage.CSS_MIME_TYPE);
                Snapshot topLevelSnapshot = resultIterator.getSnapshot();
                if(cssRi != null) {
                    Parser.Result parserResult = cssRi.getParserResult();
                    if(parserResult != null) {
                        model.set(new CssFileModel((CssParserResult)parserResult, topLevelSnapshot));
                        return ;
                    }
                }
                model.set(new CssFileModel(topLevelSnapshot));
            }
        });
        return model.get();
    }

    public static CssFileModel create(CssParserResult result) {
        return new CssFileModel(result, null);
    }

    private CssFileModel(Snapshot topLevelSnapshot) {
        this.snapshot = this.topLevelSnapshot = topLevelSnapshot;
    }

    private CssFileModel(CssParserResult parserResult, Snapshot topLevelSnapshot) {
        this.snapshot = parserResult.getSnapshot();
        this.topLevelSnapshot = topLevelSnapshot;
        if (parserResult.getParseTree() != null) {
            ParseTreeVisitor visitor = new ParseTreeVisitor();
            visitor.visitChildren(parserResult.getParseTree());
        } //else broken source, no parse tree

    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public Snapshot getTopLevelSnapshot() {
        return topLevelSnapshot;
    }

    public FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    public Collection<Entry> get(RefactoringElementType type) {
        switch (type) {
            case CLASS:
                return getClasses();
            case ID:
                return getIds();
            case COLOR:
                return getColors();
            case ELEMENT:
                return htmlElements;
            case IMPORT:
                return imports;
        }

        return null;
    }

    public Collection<Entry> getClasses() {
        return classes == null ? Collections.<Entry>emptyList() : classes;
    }

    public Collection<Entry> getIds() {
        return ids == null ? Collections.<Entry>emptyList() : ids;
    }

    public Collection<Entry> getHtmlElements() {
        return htmlElements == null ? Collections.<Entry>emptyList() : htmlElements;
    }

    public Collection<Entry> getImports() {
        return imports == null ? Collections.<Entry>emptyList() : imports;
    }

    public Collection<Entry> getColors() {
        return colors == null ? Collections.<Entry>emptyList() : colors;
    }

    /**
     *
     * @return true if the model is empty - nothing interesting found in the
     * page.
     */
    public boolean isEmpty() {
        return null == classes && null == ids && null == htmlElements && null == imports && null == colors;
    }

    //single threaded - called from constructor only, no need for synch
    private Collection<Entry> getClassesCollectionInstance() {
        if (classes == null) {
            classes = new ArrayList<>();
        }
        return classes;
    }

    private Collection<Entry> getIdsCollectionInstance() {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        return ids;
    }

    private Collection<Entry> getHtmlElementsCollectionInstance() {
        if (htmlElements == null) {
            htmlElements = new ArrayList<>();
        }
        return htmlElements;
    }

    private Collection<Entry> getImportsCollectionInstance() {
        if (imports == null) {
            imports = new ArrayList<>();
        }
        return imports;
    }

    private Collection<Entry> getColorsCollectionInstance() {
        if (colors == null) {
            colors = new ArrayList<>();
        }
        return colors;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(":");
        for (Entry c : getImports()) {
            buf.append(" imports=");
            buf.append(c);
            buf.append(',');
        }
        for (Entry c : getClasses()) {
            buf.append('.');
            buf.append(c);
            buf.append(',');
        }
        for (Entry c : getIds()) {
            buf.append('#');
            buf.append(c);
            buf.append(',');
        }
        for (Entry c : getHtmlElements()) {
            buf.append(c);
            buf.append(',');
        }

        return buf.toString();
    }

    private class ParseTreeVisitor extends NodeVisitor {

        private int[] currentBodyRange;

        @Override
        public boolean visit(Node node) {
            switch (node.type()) {
                case resourceIdentifier:
                    getImportsCollectionInstance().addAll(getImportsFromString(node));
                    //fallback to term
                case term:
                    getImportsCollectionInstance().addAll(getImportsFromURI(node)); //take imports from term only from uris, not strings!
                    break;
                case rule:
                    currentBodyRange = NodeUtil.getRuleBodyRange(node);
                    break;
                case hexColor:
                    CharSequence image = node.image();
                    int[] wsLens = getTextWSPreAndPostLens(image);
                    image = image.subSequence(wsLens[0], image.length() - wsLens[1]);
                    OffsetRange range = new OffsetRange(node.from() + wsLens[0], node.to() - wsLens[1]);
                    Entry e = createEntry(image.toString(), range, false);
                    if (e != null) {
                        getColorsCollectionInstance().add(e);
                    }
                    break;
                default:
                    if (NodeUtil.isSelectorNode(node)) {
                    if (!NodeUtil.containsError(node)) {
                        Collection<Entry> collection;
                        int start_offset_diff;

                        switch (node.type()) {
                            case cssClass:
                                collection = getClassesCollectionInstance();
                                start_offset_diff = 1; //cut off the dot (.)
                                break;
                            case cssId:
                                collection = getIdsCollectionInstance();
                                start_offset_diff = 1; //cut of the hash (#)
                                break;
                            case elementName:
                                collection = getHtmlElementsCollectionInstance();
                                start_offset_diff = 0;
                                break;
                            default:
                                throw new IllegalStateException();
                        }

                        image = node.image().subSequence(start_offset_diff, node.image().length());
                        range = new OffsetRange(node.from() + start_offset_diff, node.to());

                        //check if the real start offset can be translated to the original offset
                        boolean isVirtual = getSnapshot().getOriginalOffset(node.from()) == -1;

                        OffsetRange body = currentBodyRange != null ? new OffsetRange(currentBodyRange[0], currentBodyRange[1]) : OffsetRange.NONE;
                        e = createEntry(image.toString(), range, body, isVirtual);
                        if (e != null) {
                            collection.add(e);
                        }
                    }
                }
                    break;

            }

            return false;
        }

        private Collection<Entry> getImportsFromString(Node resourceIdentifier) {
            Collection<Entry> files = new ArrayList<>();
            //string value only from resourceIdentifier

            Node token = NodeUtil.getChildTokenNode(resourceIdentifier, CssTokenId.STRING);
            if (token != null) {
                CharSequence image = token.image();
                boolean quoted = WebUtils.isValueQuoted(image);
                files.add(createEntry(WebUtils.unquotedValue(image),
                        new OffsetRange(token.from() + (quoted ? 1 : 0),
                        token.to() - (quoted ? 1 : 0)),
                        false));
            }
            return files;
        }

        private Collection<Entry> getImportsFromURI(Node resourceIdentifier) {
            Collection<Entry> files = new ArrayList<>();
            //@import url("another.css");
            Node token = NodeUtil.getChildTokenNode(resourceIdentifier, CssTokenId.URI);
            if (token != null) {
                Matcher m = Css3Utils.URI_PATTERN.matcher(token.image());
                if (m.matches()) {
                    int groupIndex = 1;
                    String content = m.group(groupIndex);
                    boolean quoted = WebUtils.isValueQuoted(content);
                    int from = token.from() + m.start(groupIndex) + (quoted ? 1 : 0);
                    int to = token.from() + m.end(groupIndex) - (quoted ? 1 : 0);
                    files.add(createEntry(WebUtils.unquotedValue(content),
                            new OffsetRange(from, to),
                            false));
                }
            }
            return files;
        }
    }

    private Entry createEntry(String name, OffsetRange range, boolean isVirtual) {
        return createEntry(name, range, null, isVirtual);
    }

    private Entry createEntry(String name, OffsetRange range, OffsetRange bodyRange, boolean isVirtual) {
        //do not create entries for virtual generated code
//        if (CssGSFParser.containsGeneratedCode(name)) {
//            return null;
//        }

        return new LazyEntry(getSnapshot(), getTopLevelSnapshot(), name, range, bodyRange, isVirtual);
    }

    private static int[] getTextWSPreAndPostLens(CharSequence text) {
        int preWSlen = 0;
        int postWSlen = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                preWSlen++;
            } else {
                break;
            }
        }

        for (int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                postWSlen++;
            } else {
                break;
            }
        }

        return new int[]{preWSlen, postWSlen};
    }

    private static class LazyEntry implements Entry {

        private final String name;
        private final OffsetRange range, bodyRange;
        private final boolean isVirtual;

        //computed lazily
        private OffsetRange documentRange, documentBodyRange;
        private CharSequence elementText, elementLineText;
        private int lineOffset = -1;
        private final CharSequence snapshotText;
        private CharSequence topLevelSnapshotText;
        private final int documentFrom;
        private final int documentTo;
        private int bodyDocFrom;
        private int bodyDocTo;

        public LazyEntry(Snapshot snapshot, Snapshot topLevelSnapshot, String name, OffsetRange range, OffsetRange bodyRange, boolean isVirtual) {
            this.snapshotText = snapshot.getText();
            if (topLevelSnapshot != null) {
                this.topLevelSnapshotText = topLevelSnapshot.getText();
            }
            this.name = name;
            this.range = range;
            this.bodyRange = bodyRange;
            this.isVirtual = isVirtual;
            documentFrom = snapshot.getOriginalOffset(range.getStart());
            documentTo = snapshot.getOriginalOffset(range.getEnd());
            if (bodyRange != null) {
                bodyDocFrom = snapshot.getOriginalOffset(bodyRange.getStart());
                bodyDocTo = snapshot.getOriginalOffset(bodyRange.getEnd());
            }
        }

        @Override
        public boolean isVirtual() {
            return isVirtual;
        }

        @Override
        public boolean isValidInSourceDocument() {
            return getDocumentRange() != OffsetRange.NONE;
        }

        @Override
        public synchronized int getLineOffset() {
            if (lineOffset == -1) {
                if (topLevelSnapshotText != null && isValidInSourceDocument()) {
                    try {
                        lineOffset = LexerUtils.getLineOffset(topLevelSnapshotText, getDocumentRange().getStart());
                    } catch (BadLocationException ex) {
                        //no-op
                    }
                }
            }
            return lineOffset;
        }

        @Override
        public synchronized CharSequence getText() {
            if (elementText == null) {
                //delegate to the underlying source charsequence, do not duplicate any chars!
                elementText = new CharSubSequence(snapshotText, range.getStart(), range.getEnd());
            }
            return elementText;
        }

        @Override
        public synchronized CharSequence getLineText() {
            if (elementLineText == null) {
                try {
                    int astLineStart = GsfUtilities.getRowStart(snapshotText, range.getStart());
                    int astLineEnd = GsfUtilities.getRowEnd(snapshotText, range.getStart());

                    elementLineText = astLineStart != -1 && astLineEnd != -1
                            ? snapshotText.subSequence(astLineStart, astLineEnd)
                            : null;

                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return elementLineText;

        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public synchronized OffsetRange getDocumentRange() {
            if (documentRange == null) {
                documentRange = documentFrom != -1 && documentTo != -1 ? new OffsetRange(documentFrom, documentTo) : OffsetRange.NONE;
            }
            return documentRange;
        }

        @Override
        public OffsetRange getRange() {
            return range;
        }

        @Override
        public OffsetRange getBodyRange() {
            return bodyRange;
        }

        @Override
        public synchronized OffsetRange getDocumentBodyRange() {
            if (documentBodyRange == null) {
                if (bodyRange != null) {
                    documentBodyRange = bodyDocFrom != -1 && bodyDocTo != -1
                            ? new OffsetRange(bodyDocFrom, bodyDocTo)
                            : OffsetRange.NONE;
                }
            }

            return documentBodyRange;
        }

        @Override
        public String toString() {
            return "Entry[" + (!isValidInSourceDocument() ? "INVALID! " : "") + getName() + "; " + getRange().getStart() + " - " + getRange().getEnd() + "]"; //NOI18N
        }

    }

}
