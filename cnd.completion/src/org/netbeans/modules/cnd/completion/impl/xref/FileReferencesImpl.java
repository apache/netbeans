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
package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndAbstractTokenProcessor;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmExpandedTokenProcessor;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmFileReferences.class)
public final class FileReferencesImpl extends CsmFileReferences  {

    public FileReferencesImpl() {
        /*System.err.println("FileReferencesImpl registered");
        CsmModelAccessor.getModel().addProgressListener(new CsmProgressAdapter() {

            @Override
            public void fileParsingStarted(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }

            public @Override void fileInvalidated(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }
        });*/
    }

//    private final Map<CsmFile, List<CsmReference>> cache = new HashMap<CsmFile, List<CsmReference>>();

    @Override
    public void accept(CsmScope csmScope, Document doc, Visitor visitor) {
        accept(csmScope, doc, visitor, CsmReferenceKind.ALL);
    }

    @Override
    public void accept(CsmScope csmScope, Document doc, Visitor visitor, Set<CsmReferenceKind> preferedKinds) {
        FileReferencesContext fileReferencesContext = new FileReferencesContext(csmScope);
        try {
            CsmCacheManager.enter();
            _accept(csmScope, doc, visitor, preferedKinds, fileReferencesContext);
        } finally {
            fileReferencesContext.clean();
            CsmCacheManager.leave();
        }
    }

    private void _accept(CsmScope csmScope, Document doc, Visitor visitor, Set<CsmReferenceKind> kinds, FileReferencesContext fileReferncesContext) {
        if (!CsmKindUtilities.isOffsetable(csmScope) && !CsmKindUtilities.isFile(csmScope)){
            return;
        }
        CsmFile csmFile;

        int start, end;

        if (CsmKindUtilities.isFile(csmScope)){
            csmFile = (CsmFile) csmScope;
        } else {
            csmFile = ((CsmOffsetable)csmScope).getContainingFile();
        }
        if (doc == null) {
            doc = CsmReferenceRepository.getDocument(csmFile);
        }
        if (!(doc instanceof BaseDocument) || !csmFile.isValid()) {
            // This rarely can happen:
            // 1. if file was put on reparse and scope we have here is already obsolete
            // TODO: find new scope if API would allow that one day
            // 2. renamed
            // TODO: search by unique name
            // 3. deleted
            return;
        }
        if (CsmKindUtilities.isFile(csmScope)) {
            start = 0;
            end = Math.max(0, doc.getLength() - 1);
        } else {
            start = ((CsmOffsetable)csmScope).getStartOffset();
            end = ((CsmOffsetable)csmScope).getEndOffset();
        }

        List<CsmReferenceContext> refs = getIdentifierReferences(csmFile, (BaseDocument) doc, start,end, kinds, fileReferncesContext, visitor);

        for (CsmReferenceContext context : refs) {
            if (visitor.cancelled()) {
                return;
            }
            // skip 'this' if possible
            if (!isThis(context.getReference())) {
                visitor.visit(context);
            }
        }
    }

    @Override
    protected boolean isThis(CsmReference ref) {
        TokenItem<TokenId> refToken = ReferencesSupport.getRefTokenIfPossible(ref);
        if (refToken != null) {
            return refToken.id() == CppTokenId.THIS;
        } else {
            return super.isThis(ref);
        }
    }

    @Override
    public void visit(Collection<CsmReference> refs, ReferenceVisitor visitor) {
        FileReferencesContext fileReferencesContext = null;
        try {
            CsmCacheManager.enter();
            for(CsmReference ref : refs) {
                if (visitor.cancelled()) {
                    return;
                }
                if (fileReferencesContext == null){
                    fileReferencesContext = new FileReferencesContext(ref.getContainingFile());
                }
                if (ref instanceof ReferenceImpl) {
                    ((ReferenceImpl)ref).setFileReferencesContext(fileReferencesContext);
                }
                visitor.visit(ref);
            }
        } finally {
            if (fileReferencesContext != null) {
                fileReferencesContext.clean();
            }
            CsmCacheManager.leave();
        }
    }

    private List<CsmReferenceContext> getIdentifierReferences(CsmFile csmFile, final BaseDocument doc,
            final int start, final int end,
            Set<CsmReferenceKind> kinds, FileReferencesContext fileReferncesContext, Interrupter canceled) {
        ExpandedReferencesProcessor merp = ExpandedReferencesProcessor.create(doc, csmFile, kinds, fileReferncesContext, canceled);
        doc.readLock();
        try {
            CndTokenUtilities.processTokens(merp, doc, start, end);
        } finally {
            doc.readUnlock();
        }
        return merp.getReferences();
    }

    private static final class ExpandedReferencesProcessor extends CndAbstractTokenProcessor<Token<TokenId>> {

        private CsmExpandedTokenProcessor expandedTokenProcessor;
        private ReferencesProcessor originalReferencesProcessor;
        private ReferencesProcessor macroReferencesProcessor;
        private boolean inMacro = false;
        private final Interrupter canceled;

        public static ExpandedReferencesProcessor create(BaseDocument doc, CsmFile file, Set<CsmReferenceKind> kinds, FileReferencesContext fileReferncesContext, Interrupter canceled) {
            boolean skipPreprocDirectives = !kinds.contains(CsmReferenceKind.IN_PREPROCESSOR_DIRECTIVE);
            Collection<CsmOffsetable> deadBlocks;
            if (!kinds.contains(CsmReferenceKind.IN_DEAD_BLOCK)) {
                deadBlocks = CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(file, canceled);
            } else {
                deadBlocks = Collections.<CsmOffsetable>emptyList();
            }
            ReferencesProcessor rp = new ReferencesProcessor(file, doc, skipPreprocDirectives, deadBlocks, fileReferncesContext);
            // disabling experimental macro context
//            CndTokenProcessor<Token<CppTokenId>> etp = CsmExpandedTokenProcessor.create(doc, file, rp, -1, CsmFileInfoQuery.getDefault().getMacroUsages(file));
//            if (etp instanceof CsmExpandedTokenProcessor) {
//                return new ExpandedReferencesProcessor(rp, (CsmExpandedTokenProcessor) etp);
//            }
            return new ExpandedReferencesProcessor(rp, null, canceled);
        }

        public List<CsmReferenceContext> getReferences() {
            return originalReferencesProcessor.references;
        }

        private ExpandedReferencesProcessor(ReferencesProcessor rp, CsmExpandedTokenProcessor etp, Interrupter canceled) {
            this.originalReferencesProcessor = rp;
            this.expandedTokenProcessor = etp;
            this.canceled = canceled;
        }

        @Override
        public boolean isStopped() {
            return canceled.cancelled();
        }

        @Override
        public boolean token(Token<TokenId> token, int tokenOffset) {
            if (expandedTokenProcessor == null) {
                return originalReferencesProcessor.token(token, tokenOffset);
            }
            boolean res;
            if (expandedTokenProcessor.isMacro(token, tokenOffset)) {
                if (inMacro) {
                    // end of previous macro and start of new one
                    originalReferencesProcessor.references.addAll(macroReferencesProcessor.references);
                }
                // create additional references processor for macro
                macroReferencesProcessor = new ReferencesProcessor(originalReferencesProcessor);
                originalReferencesProcessor.skipReferences(true);
                res = expandedTokenProcessor.token(token, tokenOffset);
                originalReferencesProcessor.skipReferences(false);
                inMacro = true;
            } else {
                res = expandedTokenProcessor.token(token, tokenOffset);
            }
            if (inMacro && !expandedTokenProcessor.isMacroExpansion()) {
                // end of macro
                originalReferencesProcessor.references.addAll(macroReferencesProcessor.references);
                inMacro = false;
            }
            if (inMacro) {
                // processing macro
                macroReferencesProcessor.token(token, tokenOffset);
            }
            return res;
        }
    }

    private static final class ReferencesProcessor extends CndAbstractTokenProcessor<Token<TokenId>> {
        /*package*/ final List<CsmReferenceContext> references = new ArrayList<CsmReferenceContext>();
        private final Collection<CsmOffsetable> deadBlocks;
        private final boolean skipPreprocDirectives;
        private final CsmFile csmFile;
        private final BaseDocument doc;
        private final ReferenceContextBuilder contextBuilder;
        private final FileReferencesContext fileReferncesContext;
        private CppTokenId derefToken;
        private BlockConsumer blockConsumer;
        private boolean afterParen = false;
        private boolean afterBracket = false;
        private boolean skipReferences = false;

        ReferencesProcessor(CsmFile csmFile, BaseDocument doc,
                boolean skipPreprocDirectives,
                Collection<CsmOffsetable> deadBlocks, FileReferencesContext fileReferncesContext) {
            this.deadBlocks = deadBlocks;
            this.skipPreprocDirectives = skipPreprocDirectives;
            this.csmFile = csmFile;
            this.doc = doc;
            this.contextBuilder = new ReferenceContextBuilder();
            this.fileReferncesContext = fileReferncesContext;
        }

        private ReferencesProcessor(ReferencesProcessor p) {
            this.deadBlocks = p.deadBlocks;
            this.skipPreprocDirectives = p.skipPreprocDirectives;
            this.csmFile = p.csmFile;
            this.doc = p.doc;
            this.fileReferncesContext = p.fileReferncesContext;
            this.contextBuilder = new ReferenceContextBuilder(p.contextBuilder);
            this.derefToken = p.derefToken;
            this.afterParen = p.afterParen;
            this.afterBracket = p.afterBracket;
            this.skipReferences = p.skipReferences;
        }

        private void skipReferences(boolean skip) {
            skipReferences = skip;
        }

        @Override
        public boolean token(Token<TokenId> token, int tokenOffset) {
            if (blockConsumer != null) {
                if (blockConsumer.isLastToken(token)) {
                    blockConsumer = null;
                }
                return false;
            }
            boolean skip = false;
            boolean needEmbedding = false;
            TokenId id = token.id();
            if(id instanceof CppTokenId) {
                switch ((CppTokenId)id) {
                    case PREPROCESSOR_DIRECTIVE:
                        needEmbedding = !skipPreprocDirectives;
                        break;
                    case IDENTIFIER:
                    case PREPROCESSOR_IDENTIFIER:
                    case THIS: {
                        if (!deadBlocks.isEmpty()) {
                            skip = isInDeadBlock(tokenOffset, deadBlocks);
                        }
                        // do not use CsmReferenceKind.AFTER_DEREFERENCE_USAGE, because it could be fun definition like
                        // void AAA::foo() {
                        ReferenceImpl ref = ReferencesSupport.createReferenceImpl(
                                csmFile, doc, tokenOffset, CndTokenUtilities.createTokenItem(token, tokenOffset), derefToken == null ? null : null /*CsmReferenceKind.AFTER_DEREFERENCE_USAGE*/);
                        contextBuilder.reference(ref, derefToken);
                        ref.setFileReferencesContext(fileReferncesContext);
                        derefToken = null;
                        if (!skip && !skipReferences) {
                            references.add(contextBuilder.getContext());
                        }
                        break;
                    }
                    case DOT:
                    case DOTMBR:
                    case ARROW:
                    case ARROWMBR:
                    case SCOPE:
                        derefToken = (CppTokenId)id;
                        break;
                    case LBRACE:
                        if (afterParen) {
                            // Compiler extension "({...})"
                            blockConsumer = new BlockConsumer(CppTokenId.LBRACE, CppTokenId.RBRACE);
                        } else {
                            contextBuilder.open((CppTokenId)id);
                        }
                        derefToken = null;
                        break;
                    case LBRACKET:
                        if (afterBracket) {                            
                            if (CsmFileInfoQuery.getDefault().isCpp11OrLater(csmFile)) {
                                blockConsumer = new BlockConsumer(CppTokenId.LBRACKET, CppTokenId.RBRACKET);
                                derefToken = null;
                                break;
                            }
                        }
                        // Fall through
                    case LPAREN:
                    case LT:
                        contextBuilder.open((CppTokenId)id);
                        derefToken = null;
                        break;
                    case RBRACE:
                    case RBRACKET:
                    case RPAREN:
                    case GT:
                        contextBuilder.close((CppTokenId)id);
                        derefToken = null;
                        break;
                    case __ATTRIBUTE__:
                    case __ATTRIBUTE:
                    case _DECLSPEC:
                    case __DECLSPEC:
                    case ASM:
                    case __ASM:
                    case __ASM__:
                        blockConsumer = new BlockConsumer(CppTokenId.LPAREN, CppTokenId.RPAREN);
                        derefToken = null;
                        break;
                    case _ASM:
                        blockConsumer = new BlockConsumer(CppTokenId.LBRACE, CppTokenId.RBRACE);
                        derefToken = null;
                        break;
                    case WHITESPACE:
                    case NEW_LINE:
                    case BLOCK_COMMENT:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case TEMPLATE:
                        // OK, do nothing
                        break;
                    default:
                        contextBuilder.other((CppTokenId)id);
                        derefToken = null;
                }

                // Initializing afterParen flag
                // This flag is used for detection of compiler extensions "({...})"
                switch ((CppTokenId)id) {
                    case LBRACKET:
                        afterBracket = true;
                        break;
                    case LPAREN:
                        afterParen = true;
                        break;
                    case WHITESPACE:
                    case NEW_LINE:
                    case BLOCK_COMMENT:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                        break;
                    default:
                        afterBracket = false;
                        afterParen = false;
                }
            }

            return needEmbedding;
        }
    }

    private static final class ReferenceContextBuilder {

        private static final int FULLCOPY_INTERVAL = 50;
        private ReferenceContextImpl context;
        private final List<CppTokenId> brackets;
        private final List<Integer> pushes;
        private int snapshots;

        public ReferenceContextBuilder() {
            context = new ReferenceContextImpl();
            brackets = new ArrayList<CppTokenId>();
            pushes = new ArrayList<Integer>();
            pushes.add(0);
        }

        public ReferenceContextBuilder(ReferenceContextBuilder b) {
            context = new ReferenceContextImpl(b.context);
            brackets = new ArrayList<CppTokenId>(b.brackets);
            pushes = new ArrayList<Integer>(b.pushes);
            snapshots = b.snapshots;
        }

        public void open(CppTokenId leftBracket) {
            if (peek(pushes) == 0 && peek(brackets) != null) {
                // insert a dummy reference if needed
                context.push(peek(brackets), null);
                pop(pushes);
                pushes.add(1);
            }
            brackets.add(leftBracket);
            pushes.add(0);
        }

        public void close(CppTokenId rightBracket) {
            if (match(peek(brackets), rightBracket)) {
                // close corresponding bracket if possible
                pop(brackets);
                for (int i = 0; i < peek(pushes); ++i) {
                    context.pop();
                }
                pop(pushes);
            }
        }

        public void other(CppTokenId token) {
            if (token == CppTokenId.SEMICOLON && peek(brackets) == CppTokenId.LT) {
                // semicolon can't appear inside angle brackets
                close(CppTokenId.GT);
            }
            for (int i = 0; i < peek(pushes); ++i) {
                context.pop();
            }
            pop(pushes);
            pushes.add(0);
        }

        public void reference(CsmReference ref, CppTokenId derefToken) {
            int pushCount = 0;
            if (derefToken == null) {
                other(CppTokenId.IDENTIFIER);
                if (peek(brackets) == null) {
                    context.push(CppTokenId.IDENTIFIER, ref);
                } else {
                    context.push(peek(brackets), ref);
                }
                ++pushCount;
            } else {
                if (peek(pushes) == 0 && peek(brackets) != null) {
                    context.push(peek(brackets), null);
                    ++pushCount;
                }
                context.push(derefToken, ref);
                ++pushCount;
            }
            pushes.add(pop(pushes) + pushCount);
        }

        private static<T> T peek(List<T> list) {
            if (list.isEmpty()) {
                return null;
            } else {
                return list.get(list.size() - 1);
            }
        }

        private static<T> T pop(List<T> list) {
            if (list == null || list.isEmpty()) {
                return null;
            } else {
                return list.remove(list.size() - 1);
            }
        }

        private static boolean match(CppTokenId l, CppTokenId r) {
            return l == CppTokenId.LBRACE && r == CppTokenId.RBRACE
                    || l == CppTokenId.LBRACKET && r == CppTokenId.RBRACKET
                    || l == CppTokenId.LPAREN && r == CppTokenId.RPAREN
                    || l == CppTokenId.LT && r == CppTokenId.GT;
        }

        public CsmReferenceContext getContext() {
            CsmReferenceContext snapshot;
            if (FULLCOPY_INTERVAL <= snapshots++) {
                snapshot = new ReferenceContextImpl(context, true);
                snapshots = 0;
            } else {
                snapshot = context;
            }
            context = new ReferenceContextImpl(snapshot, false);
            return snapshot;
        }

        @Override
        public String toString() {
            return String.valueOf(context);
        }

    }

    private static boolean isInDeadBlock(int startOffset, Collection<CsmOffsetable> deadBlocks) {
        for (CsmOffsetable csmOffsetable : deadBlocks) {
            if (csmOffsetable.getStartOffset() > startOffset) {
                return false;
            }
            if (csmOffsetable.getEndOffset() > startOffset) {
                return true;
            }
        }
        return false;
    }

    private static class BlockConsumer {
        private final CppTokenId openBracket;
        private final CppTokenId closeBracket;
        private int depth;
        public BlockConsumer(CppTokenId openBracket, CppTokenId closeBracket) {
            this.openBracket = openBracket;
            this.closeBracket = closeBracket;
            depth = 0;
        }

        public boolean isLastToken(Token<TokenId> token) {
            boolean stop = false;
            if (token.id() == openBracket) {
                ++depth;
            } else if (token.id() == closeBracket) {
                --depth;
                stop = depth <= 0;
            }
            return stop;
        }
    }
}
