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
package org.netbeans.modules.cnd.highlight.semantic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.highlight.semantic.options.SemanticHighlightingOptions;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class MarkOccurrencesHighlighter extends HighlighterBase {
    private static final String POSITION_BAG = "CndMarkOccurrencesHighlighter"; // NOI18N
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private static final ConcurrentHashMap<String,AttributeSet> defaultColors = new ConcurrentHashMap<>();
    
    private final CancelSupport cancel = CancelSupport.create(this);
    private InterrupterImpl interrupter = new InterrupterImpl();

    public static PositionsBag getHighlightsBag(Document doc) {
        if (doc == null) {
            return null;
        }

        PositionsBag bag = (PositionsBag) doc.getProperty(POSITION_BAG);

        if (bag == null) {
            doc.putProperty(POSITION_BAG, bag = new PositionsBag(doc, false));

            final PositionsBag bagFin = bag;
            DocumentListener l = new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            };

            doc.addDocumentListener(l);
        }

        return bag;
    }

    private void clean(Document doc) {
        if (doc != null) {
            getHighlightsBag(doc).clear();
            OccurrencesMarkProvider.get(doc).setOccurrences(Collections.<Mark>emptySet());
        }
    }

    public MarkOccurrencesHighlighter(String mimeType) {
        init(mimeType);
    }
    public static final Color ES_COLOR = new Color(175, 172, 102);

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        synchronized(this) {
            interrupter.cancel();
            this.interrupter = new InterrupterImpl();
        }
        if (cancel.isCancelled()) {
            return;
        }
        if (!(event instanceof CursorMovedSchedulerEvent)) {
            return;
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "MarkOccurrencesHighlighter started"); //NOI18N
            time = System.currentTimeMillis();
        }
        CsmCacheManager.enter();
        try {
            runImpl((BaseDocument)result.getSnapshot().getSource().getDocument(false), (CursorMovedSchedulerEvent) event, interrupter);
        } finally {
            CsmCacheManager.leave();
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "MarkOccurrencesHighlighter finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
        }
    }

    @Override
    public void cancel() {
        synchronized(this) {
            interrupter.cancel();
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "MarkOccurrencesHighlighter canceled"); //NOI18N
        }
    }

    @Override
    public int getPriority() {return 100;}

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    private void runImpl(final BaseDocument doc, CursorMovedSchedulerEvent event, final InterrupterImpl interrupter) {
        if (!SemanticHighlightingOptions.instance().getEnableMarkOccurrences()) {
            clean(doc);
            return;
        }

        if (doc == null) {
            return;
        }
        final CsmFile file = CsmUtilities.getCsmFile(doc, false, false);
        final FileObject fo = CsmUtilities.getFileObject(doc);

        if (file == null || fo == null) {
            // this can happen if MO was triggered right before closing project
            clean(doc);
            return;
        }
        final String mimeType = DocumentUtilities.getMimeType(doc);
        int lastPosition = event.getCaretOffset();

        // Check existance of related document
        // And if it exist and check should we use its caret position or not
        Document doc2 = (Document) doc.getProperty(Document.class);
        if (doc2 != null) {
            boolean useOwnCarretPosition = true;
            Object obj = doc.getProperty(CsmMacroExpansion.USE_OWN_CARET_POSITION);
            if (obj != null) {
                useOwnCarretPosition = (Boolean) obj;
            }
            if (!useOwnCarretPosition) {
                FileObject fo2 = CsmUtilities.getFileObject(doc2);
                if (fo2 != null) {
                    JTextComponent comp2 = null;
                    for(JTextComponent comp : EditorRegistry.componentList()) {
                        if (doc2.equals(comp.getDocument())) {
                            comp2 = comp;
                            break;
                        }
                    }
                    if (comp2 != null) {
                        lastPosition = getDocumentOffset(doc, getFileOffset(doc2, comp2.getCaretPosition()));
                    }
                }
            }
        }

        if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
            HighlightsSequence hs = getHighlightsBag(doc).getHighlights(0, doc.getLength() - 1);
            while (hs.moveNext()) {
                if (lastPosition >= hs.getStartOffset() && lastPosition <= hs.getEndOffset()) {
                    // cursor is still in the marked area, so previous result is valid
                    return;
                }
            }
        }

        final Collection<CsmReference> out = getOccurrences(doc, file, lastPosition, interrupter);
        if (out.isEmpty() || interrupter.cancelled()) {
            if (!SemanticHighlightingOptions.instance().getKeepMarks()) {
                clean(doc);
            }
        } else {
            final PositionsBag obag = new PositionsBag(doc);
            obag.clear();
            final AttributeSet attrs = defaultColors.get(mimeType);
            if (attrs == null) {
                assert false : "Color attributes set is not found for MIME " + mimeType + ". Document " + doc;
                return;
            }
            for (final CsmReference csmReference : out) {
                if (interrupter.cancelled()) {
                    break;
                }
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            addHighlight(doc, obag, csmReference, attrs);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
                doc.render(runnable);
            }
            if (interrupter.cancelled()) {
                // no need to mark dirty occurrences, they will be recalculated on the next run
                return;
            }
            getHighlightsBag(doc).setHighlights(obag);
            OccurrencesMarkProvider.get(doc).setOccurrences(
                    OccurrencesMarkProvider.createMarks(doc, out, ES_COLOR, NbBundle.getMessage(MarkOccurrencesHighlighter.class, "LBL_ES_TOOLTIP")));
        }
    }
    
    private void addHighlight(Document doc, PositionsBag obag, CsmReference csmReference, AttributeSet attrs) throws BadLocationException {
        int usages[][] = CsmMacroExpansion.getUsages(doc, csmReference.getStartOffset());
        if (usages != null) {
            for (int i = 0; i < usages.length; i++) {
                int startOffset = usages[i][0];
                int endOffset = usages[i][1];
                if (startOffset < doc.getLength() && endOffset > 0 && startOffset < endOffset) {
                    obag.addHighlight(NbDocument.createPosition(doc, (startOffset > 0) ? startOffset : 0, javax.swing.text.Position.Bias.Forward),
                                     NbDocument.createPosition(doc, (endOffset < doc.getLength()) ? endOffset : doc.getLength(), javax.swing.text.Position.Bias.Backward), attrs);
                }
            }
        } else {
            int startOffset = getDocumentOffset(doc, csmReference.getStartOffset());
            int endOffset = getDocumentOffset(doc, csmReference.getEndOffset());
            if (startOffset < doc.getLength() && endOffset > 0 && startOffset < endOffset) {
                obag.addHighlight(NbDocument.createPosition(doc, (startOffset > 0) ? startOffset : 0, javax.swing.text.Position.Bias.Forward),
                                  NbDocument.createPosition(doc, (endOffset < doc.getLength()) ? endOffset : doc.getLength(), javax.swing.text.Position.Bias.Backward), attrs);
            }
        }
    }

    /* package-local */ static Collection<CsmReference> getOccurrences(BaseDocument doc, CsmFile file, int position, InterrupterImpl interrupter) {
        Collection<CsmReference> out = Collections.<CsmReference>emptyList();
        position = getFileOffset(doc, position);
        if (interrupter.cancelled()) {
            return out;
        }
        // check if offset is in preprocessor conditional block
        if (isPreprocessorConditionalBlock(doc, position)) {
            return getPreprocReferences(doc, file, position, interrupter);
        } else {
            Token<TokenId> stringToken = getTokenIfStringLiteral(doc, position);
            if (stringToken != null) {
                return getStringReferences(doc, stringToken, interrupter);
            }
        }
        if (interrupter.cancelled()) {
            return out;
        }
        if (file != null && file.isParsed()) {
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(file, doc, position);
            if (ref != null && ref.getReferencedObject() != null) {
                if (interrupter.cancelled()) {
                    return out;
                }
                out = CsmReferenceRepository.getDefault().getReferences(ref.getReferencedObject(), file, CsmReferenceKind.ALL, interrupter);
            }
        }
        return out;
    }

    private static int getFileOffset(Document doc, int documentOffset) {
        return CsmMacroExpansion.getOffsetInOriginalText(doc, documentOffset);
    }

    private static int getDocumentOffset(Document doc, int fileOffset) {
        return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
    }

    @Override
    protected void updateFontColors(FontColorProvider provider) {
        defaultColors.put(provider.getMimeType(), provider.getColor(FontColorProvider.Entity.MARK_OCCURENCES));
    }

    private static boolean isPreprocessorConditionalBlock(BaseDocument doc, int offset) {
        if (doc == null) {
            return false;
        }
        doc.readLock();
        try {
            TokenSequence<TokenId> ts = cppTokenSequence(doc, offset, false);
            if (ts != null) {
                int[] span = getPreprocConditionalOffsets(ts);
                if (isIn(span, offset)) {
                    return true;
                }
            }
        } finally {
            doc.readUnlock();
        }
        return false;
    }

    private static Token<TokenId> getTokenIfStringLiteral(AbstractDocument doc, int offset) {
        if (doc == null) {
            return null;
        }
        doc.readLock();
        try {
            TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset, true, false);
            if (ts != null) {
                int move = ts.move(offset);
                // check previous token as well if on the boundary of two tokens
                int lastPhase = (move == 0) ? 2 : 1;
                for (int curPhase = 1; curPhase <= lastPhase; curPhase++) {
                    if (curPhase == 2) {
                        ts.move(offset);
                        if (!ts.movePrevious()) {
                            // in the begin of all tokens
                            break;
                        }
                    } else if (!ts.moveNext()) {
                        // at the end of tokens
                        continue;
                    }
                    Token<TokenId> token = ts.token();
                    if(token.id() == CppTokenId.STRING_LITERAL ||
                       token.id() == CppTokenId.RAW_STRING_LITERAL ||
                       token.id() == CppTokenId.CHAR_LITERAL) {
                            return token;
                    }
                }
            }
        } finally {
            doc.readUnlock();
        }
        return null;
    }
    
    /**
     * returns offset pair (#-start, keyword-end), token stream is positioned on keyword token
     * @param ts
     * @return
     */
    private static int[] getPreprocConditionalOffsets(TokenSequence<TokenId> ts) {
        ts.moveStart();
        ts.moveNext(); // move to starting #
        int start = ts.offset();
        while (ts.moveNext()) {
            TokenId tokenID = ts.token().id();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case PREPROCESSOR_START:
                    case PREPROCESSOR_START_ALT:
                    case WHITESPACE:
                    case BLOCK_COMMENT:
                    case ESCAPED_LINE:
                    case ESCAPED_WHITESPACE:
                        // skip them
                        break;
                    case PREPROCESSOR_IF:
                    case PREPROCESSOR_IFDEF:
                    case PREPROCESSOR_IFNDEF:
                    case PREPROCESSOR_ELIF:
                    case PREPROCESSOR_ELSE:
                    case PREPROCESSOR_ENDIF:
                        // found
                        int end = ts.offset() + ts.token().length();
                        return new int[]{start, end};
                    default:
                        // not found interested directive
                        return null;
                }
            }
        }
        return null;
    }

    private static TokenSequence<TokenId> cppTokenSequence(Document doc, int offset, boolean backwardBias) {
        return CndLexerUtilities.getCppTokenSequence(doc, offset, true, backwardBias);
    }

    private static final class ConditionalBlock {

        private final List<int[]> directivePositions = new ArrayList<>(4);
        private final ConditionalBlock parent;

        public ConditionalBlock(ConditionalBlock parent) {
            this.parent = parent;
        }

        public void addDirective(int[] span) {
            directivePositions.add(span);
        }

        public ConditionalBlock startNestedBlock(int[] span) {
            ConditionalBlock nestedBlock = new ConditionalBlock(this);
            nestedBlock.addDirective(span);
            return nestedBlock;
        }

        public ConditionalBlock getParent() {
            return parent;
        }

        public List<int[]> getDirectives() {
            return Collections.unmodifiableList(directivePositions);
        }
    }

    private static Collection<CsmReference> getPreprocReferences(AbstractDocument doc, CsmFile file, int searchOffset, InterrupterImpl interrupter) {
        doc.readLock();
        try {
            TokenSequence<?> origPreprocTS = cppTokenSequence(doc, searchOffset, false);
            if (origPreprocTS == null || origPreprocTS.language() != CppTokenId.languagePreproc()) {
                return Collections.<CsmReference>emptyList();
            }
            TokenHierarchy<AbstractDocument> th = TokenHierarchy.get(doc);
            List<TokenSequence<?>> ppSequences = th.tokenSequenceList(origPreprocTS.languagePath(), 0, doc.getLength());
            ConditionalBlock top = new ConditionalBlock(null);
            ConditionalBlock current = new ConditionalBlock(top);
            ConditionalBlock offsetContainer = null;
            for (TokenSequence<?> ts : ppSequences) {
                if (interrupter.cancelled()) {
                    return Collections.<CsmReference>emptyList();
                }
                @SuppressWarnings("unchecked")
                TokenSequence<TokenId> ppTS = (TokenSequence<TokenId>) ts;
                int[] span = getPreprocConditionalOffsets(ppTS);
                if (span != null) {
                    TokenId tokenID = ppTS.token().id();
                    if(tokenID instanceof CppTokenId) {
                        switch ((CppTokenId)tokenID) {
                            case PREPROCESSOR_IF:
                            case PREPROCESSOR_IFDEF:
                            case PREPROCESSOR_IFNDEF:
                                current = current.startNestedBlock(span);
                                break;
                            case PREPROCESSOR_ELIF:
                            case PREPROCESSOR_ELSE:
                            case PREPROCESSOR_ENDIF:
                                current.addDirective(span);
                                break;
                            default:
                                assert false : "unexpected token " + ts.token();
                        }
                    }
                    if (offsetContainer == null && isIn(span, searchOffset)) {
                        offsetContainer = current;
                    }
                    if (ppTS.token().id() == CppTokenId.PREPROCESSOR_ENDIF) {
                        // finished block, pop previous
                        current = current.getParent();
                        if (current == null) {
                            // unbalanced
                            return toRefs(offsetContainer);
                        }
                    }
                }
            }
            return toRefs(offsetContainer);
        } finally {
            doc.readUnlock();
        }
    }

    private static boolean isIn(int[] span, int offset) {
        return span != null && span[0] <= offset && offset <= span[1];
    }

    private static Collection<CsmReference> toRefs(ConditionalBlock block) {
        if (block == null || block.getDirectives().isEmpty()) {
            return Collections.<CsmReference>emptyList();
        }
        List<int[]> directives = block.getDirectives();
        Collection<CsmReference> out = new ArrayList<>(directives.size());
        for (int[] directive : directives) {
            out.add(new TokenRef(directive[0], directive[1]));
        }
        return out;
    }

    private static Collection<CsmReference> getStringReferences(AbstractDocument doc, Token<TokenId> stringToken, InterrupterImpl interrupter) {
        if (stringToken == null) {
            return Collections.<CsmReference>emptyList();
        }
        CharSequence aText = stringToken.text();
        if (aText == null) {
            return Collections.<CsmReference>emptyList();
        }
        String tokenText = aText.toString();
        doc.readLock();
        try {
            Collection<CsmReference> out = new ArrayList<>(10);
            TokenSequence<?> ts = CndLexerUtilities.getCppTokenSequence(doc, 0, false, false);
            if (ts != null) {
                ts.move(0);
                LinkedList<TokenSequence<?>> tss = new LinkedList<>();
                tss.addFirst(ts);
                while (!tss.isEmpty()) {
                    ts = tss.removeFirst();
                    while (ts.moveNext()) {
                        if (interrupter.cancelled()) {
                            return Collections.<CsmReference>emptyList();
                        }
                        @SuppressWarnings("unchecked")
                        Token<CppTokenId> token = (Token<CppTokenId>) ts.token();
                        switch (token.id()) {
                            case PREPROCESSOR_DIRECTIVE:
                                // jump into preprocsessor
                                TokenSequence<?> embedded = ts.embedded();
                                embedded.move(0);
                                tss.addFirst(embedded);
                                break;
                            case RAW_STRING_LITERAL:
                            case STRING_LITERAL:
                            case CHAR_LITERAL:
                                CharSequence text = token.text();
                                if (tokenText.contentEquals(text)) {
                                    out.add(new TokenRef(ts.offset(), ts.offset() + text.length()));
                                }
                                break;
                        }
                    }
                }
            }
            return out;
        } finally {
            doc.readUnlock();
        }
    }

    @Override
    public String toString() {
        return "MarkOccurrencesHighlighter runner"; //NOI18N
    }
    
    private static final class TokenRef implements CsmReference {
        private final int start;
        private final int end;

        public TokenRef(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public CsmReferenceKind getKind() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        @Override
        public CsmObject getReferencedObject() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        @Override
        public CsmObject getOwner() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        @Override
        public int getStartOffset() {
            return start;
        }

        @Override
        public int getEndOffset() {
            return end;
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }

        @Override
        public String toString() {
            return "tokenRef[" + start + "-" + end + "]";//NOI18N
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            throw new UnsupportedOperationException("Must not be called."); //NOI18N
        }
    }
}
