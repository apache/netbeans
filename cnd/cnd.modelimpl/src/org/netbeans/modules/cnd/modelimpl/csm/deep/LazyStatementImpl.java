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
package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import java.lang.ref.SoftReference;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Exceptions;

/**
 * Lazy statements
 *
 */
abstract public class LazyStatementImpl extends StatementBase implements CsmScope {
    
    private static final List<CsmStatement> ANTILOOP_EMPTY_LIST = Collections.<CsmStatement>unmodifiableList(new ArrayList<CsmStatement>(0));
    // for PERF reason we use field as lock and container of "in-resolve" state
    private final ThreadLocal<AtomicBoolean> inResolveLazyStatements_AndStatementsRefLoc = new ThreadLocal<AtomicBoolean>() {
        @Override
        protected AtomicBoolean initialValue() {
            return new AtomicBoolean(false);
        }
    };
    private volatile SoftReference<List<CsmStatement>> statements = null;
    
    protected LazyStatementImpl(CsmFile file, int start, int end, int macroStartMarker, CsmFunction scope) {
        super(file, start, end, macroStartMarker, scope);
    }

    @Override
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.COMPOUND;
    }

    public final List<CsmStatement> getStatements() {
        List<CsmStatement> list;
        // check if someone already renedered statements before
        final Object statementsLoc = inResolveLazyStatements_AndStatementsRefLoc;
        synchronized (statementsLoc) {
            list = statements == null ? null : statements.get();
        }
        if (list == null) {
            return createStatements();
        } else {
            return list;
        }
    }

    /**
     * 1) Creates a list of statements
     * 2) If it is created successfully, stores a soft reference to this list
     *	  and returns this list,
     *    otherwise just returns empty list
     */
    private List<CsmStatement> createStatements() {
        // anti-loop check for recursion from renderer's gatherMaps
        final AtomicBoolean statementsAntiLoop = inResolveLazyStatements_AndStatementsRefLoc.get();
        if (statementsAntiLoop.get()) {
            // return empty list to prevent infinite recusion by calling this method in the same thread
            // i.e. from Resoler3 which helps to resolve renderer's AST ambiguity
            return ANTILOOP_EMPTY_LIST;
        }
        // code completion tests do work in EDT because otherwise EDT thread is not started by test harness
        CndUtils.assertTrueInConsole(!SwingUtilities.isEventDispatchThread() || CndUtils.isCodeCompletionUnitTestMode(), "Calling Parser in UI Thread");
        final Object statementsLoc = inResolveLazyStatements_AndStatementsRefLoc;
        synchronized (this) {
            // check if other thread already inited statements
            synchronized (statementsLoc) {
                if (statements != null) {
                    List<CsmStatement> refList = statements.get();
                    if (refList != null) {
                        return refList;
                    }
                }
            }
            boolean prev = inResolveLazyStatements_AndStatementsRefLoc.get().getAndSet(true);
            assert prev == false : Thread.currentThread().getName() + " can not enter in create Statements twice in the same thread:" + this;
            try {
                List<CsmStatement> list = new ArrayList<>();
                if (!renderStatements(list)) {
                    // on error assign empty list to prevent re-creation
                    list = Collections.emptyList();
                } else {
                    ((ArrayList)list).trimToSize();
                }
                synchronized (statementsLoc) {
                    statements = new SoftReference<>(list);
                }
                return list;
            } finally {
                prev = inResolveLazyStatements_AndStatementsRefLoc.get().getAndSet(false);
                assert prev == true : Thread.currentThread().getName() + " can not leave create Statements twice from the same thread:" + this;
            }
        }
    }

    private boolean renderStatements(List<CsmStatement> list) {
        FileImpl file = (FileImpl) getContainingFile();
        TokenStream stream = getStatementTokenStream(file);
        if (stream == null) {
            int startOffset = getStartOffset();
            int[] lineColumn = file.getLineColumn(startOffset);
            Utils.LOG.log(Level.SEVERE, "Can''t create compound statement: can''t create token stream for file {0} at {1}:{2}", // NOI18N
                    new Object[] {file.getAbsolutePath(), lineColumn[0], lineColumn[1]});
            return false;
        } else {
            FileContent tmpFileContent = null;
            try {
                if (TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                    FileImpl fileImpl = (FileImpl)getContainingFile();
                    if (fileImpl.getParsingFileContent() == null) {
                        tmpFileContent = CsmCorePackageAccessor.get().prepareLazyStatementParsingContent(fileImpl);
                    }
                }
                CsmParserProvider.CsmParserResult result = resolveLazyStatement(stream);
                if (result != null) {
                    result.render(list);
                }
                return true;
            } finally {
                if (tmpFileContent != null) {
                    FileImpl fileImpl = (FileImpl) getContainingFile();
                    CsmCorePackageAccessor.get().releaseLazyStatementParsingContent(fileImpl, tmpFileContent);
                }
            }            
        }
    }
    
    private TokenStream getStatementTokenStream(FileImpl file) {
        TokenStream ts = file.getTokenStream(getStartOffset(), getEndOffset(), 0, true);
        if (ts != null && macroStartMarker >= 0) {
            try {
                Token token = ts.nextToken();
                while (!isLastToken(token) && APTUtils.isMacroExpandedToken(token)) {
                    if (APTUtils.getExpandedTokenMarker((APTToken) token) == macroStartMarker) {
                        if (token.getType() != getFirstTokenID()) {
                            // Fallback: if body starts with wrong token,
                            // then report the error and apply old logic.
                            CndUtils.assertTrueInConsole(
                                false, 
                                "File (" + file + "), " // NOI18N
                                + "position (" + ((APTToken) token).getOffset() + ", " + ((APTToken) token).getText() + "), " // NOI18N
                                + "macro index (" + macroStartMarker + ")" // NOI18N
                            );
                            return file.getTokenStream(getStartOffset(), getEndOffset(), getFirstTokenID(), true);
                        }
                        break;
                    }
                    token = ts.nextToken();
                }
                return new PrependedTokenStream(ts, token);
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
                return ts;
            }
        }
        return ts;
    }
    
    private boolean isLastToken(Token token)  {
        return token == null || token.getType() == Token.EOF_TYPE;
    }

    public void renderStatements(AST ast, List<CsmStatement> list, Map<Integer, CsmObject> objects) {
        for (ast = (ast == null ? null : ast.getFirstChild()); ast != null; ast = ast.getNextSibling()) {
            CsmStatement stmt = AstRenderer.renderStatement(ast, getContainingFile(), this, objects);
            if (stmt != null) {
                list.add(stmt);
            }
        }
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        // statements are scope elements
        @SuppressWarnings("unchecked")
        Collection<CsmScopeElement> out = (Collection<CsmScopeElement>) ((List<? extends CsmScopeElement>) getStatements());
        return out;
    }

    abstract protected CsmParserProvider.CsmParserResult resolveLazyStatement(TokenStream tokenStream);
    abstract protected int/*CPPTokenTypes*/ getFirstTokenID();    

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        // HAVE TO BE ONLY DELEGATION INTO SUPER
        // because non-lazy will be deserialized as lazy
        super.write(output);
    }

    public LazyStatementImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.statements = null;
    }
    
    private static final class PrependedTokenStream implements TokenStream {
        
        private final TokenStream delegate;
        
        private final Token token;
        
        private boolean first;

        public PrependedTokenStream(TokenStream delegate, Token token) {
            this.delegate = delegate;
            this.token = token;
            this.first = true;
        }
        
        @Override
        public Token nextToken() throws TokenStreamException {
            if (first) {
                first = false;
                return token;
            }
            return delegate.nextToken();
        }
    }
}
