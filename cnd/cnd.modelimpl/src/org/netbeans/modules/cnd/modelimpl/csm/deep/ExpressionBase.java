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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Common base for all expression implementations
 */
public class ExpressionBase extends OffsetableBase implements CsmExpression {

    // This is a marker that expression had declared some lambdas
    private final static List<CsmStatement> NOT_EMPTY = Collections.unmodifiableList(new ArrayList<CsmStatement>(0));

    //private final CsmExpression.Kind kind;
    //private final CsmExpression parent;
    private List<CsmExpression> operands;
    private CsmScope scopeRef;
    private CsmUID<CsmScope> scopeUID;

    private List<CsmStatement> lambdas;

    ExpressionBase(AST ast, CsmFile file,/* CsmExpression parent,*/ CsmScope scope) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        //this.parent = parent;
        if( scope != null ) {
            setScope(scope);
        }
    }

    ExpressionBase(int startOffset, int endOffset, CsmFile file,/* CsmExpression parent,*/ CsmScope scope) {
        super(file, startOffset, endOffset);
        //this.parent = parent;
        if( scope != null ) {
            setScope(scope);
        }
    }

    @Override
    public CsmExpression.Kind getKind() {
        return null; //kind;
    }

    @Override
    public CsmScope getScope() {
        return _getScope();
    }

    @Override
    public List<CsmStatement> getLambdas() {
        if (lambdas == NOT_EMPTY) {
            createLambdas();
        }
        if (lambdas == null) {
            return Collections.<CsmStatement>emptyList();
        }
        return lambdas;
    }

    @Override
    public CharSequence getExpandedText() {
        return getText();
    }

    public void setLambdas(List<CsmStatement> lambdas) {
        this.lambdas = lambdas;
    }

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        if (lambdas != null) {
            Utils.disposeAll(lambdas);
        }
    }

    private synchronized void onDispose() {
        if (this.scopeRef == null) {
            // restore container from it's UID if not directly initialized
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }

    private synchronized CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }

    protected void setScope(CsmScope scope) {
	// within bodies scope is a statement - it is not Identifiable
        if (scope instanceof CsmIdentifiable) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert scopeUID != null;
        } else {
            this.scopeRef = scope;
        }
    }

    @Override
    public List<CsmExpression> getOperands() {
        if( operands == null ) {
            operands = new ArrayList<>(0);
        }
        return Collections.unmodifiableList(operands);
    }

    @Override
    public CsmExpression getParent() {
        return null; //parent;
    }

    /**
     * Creates a list of lambdas
     */
    private synchronized void createLambdas() {
        // code completion tests do work in EDT because otherwise EDT thread is not started by test harness
        CndUtils.assertTrueInConsole(!SwingUtilities.isEventDispatchThread() || CndUtils.isCodeCompletionUnitTestMode(), "Calling Parser in UI Thread"); // NOI18N
        if (lambdas == NOT_EMPTY) {
            lambdas = null; // assigning constant prevents possible recursion because of calling that methiod in the same thread
            List<CsmStatement> list = new ArrayList<>();
            if (renderLambdas(list)) {
                lambdas = list;
            }
        }
    }

    private boolean renderLambdas(List<CsmStatement> list) {
        if (!(getContainingFile() instanceof FileImpl)) {
            return false;
        }
        FileImpl file = (FileImpl) getContainingFile();
        int startOffset = getStartOffset();
        int endOffset = getEndOffset();
        // end offset shifted left because token stream uses inclusive end offset
        TokenStream stream = file.getTokenStream(startOffset, (endOffset > startOffset) ? endOffset - 1 : endOffset, 0, true);
        if (stream == null) {
            int[] lineColumn = file.getLineColumn(startOffset);
            Utils.LOG.log(Level.SEVERE, "Can''t create lambdas: can''t create token stream for file {0} at {1}:{2}", // NOI18N
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
                CsmParserProvider.CsmParserResult result = resolveLazyLambdas(stream);
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

    private CsmParserProvider.CsmParserResult resolveLazyLambdas(TokenStream tokenStream) {
        CsmParserProvider.CsmParser parser = CsmParserProvider.createParser(getContainingFile());
        if (parser != null) {
            parser.init(this, tokenStream, null);
            return parser.parse(CsmParserProvider.CsmParser.ConstructionKind.INITIALIZER);
        }
        assert false : "parser not found"; // NOI18N
        return null;
    }

    public void renderStatements(AST ast, List<CsmStatement> list, Map<Integer, CsmObject> objects) {
        for (; ast != null; ast = ast.getNextSibling()) {
            CsmStatement stmt = AstRenderer.renderStatement(ast, getContainingFile(), this.getScope(), objects);
            if (stmt != null) {
                list.add(stmt);
            }
        }
    }

    public interface ExpressionBuilderContainer {
        public void addExpressionBuilder(ExpressionBuilder builder);
    }

    public static class ExpressionBuilder extends ScopedDeclarationBuilder {

        private int level = 0;
        public void enterExpression() {
            level++;
        }

        public void leaveExpression() {
            level--;
        }

        public boolean isTopExpression() {
            return level == 0;
        }

        public ExpressionBase create() {
            ExpressionBase expr = new ExpressionBase(getStartOffset(), getEndOffset(), getFile(), getScope());
            return expr;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        //PersistentUtils.writeExpression(this.parent, output);
        //PersistentUtils.writeExpressionKind(this.kind, output);
        PersistentUtils.writeExpressions(this.operands, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
        output.writeBoolean((lambdas == NOT_EMPTY) || (lambdas != null && !lambdas.isEmpty()));
    }

    public ExpressionBase(RepositoryDataInput input) throws IOException {
        super(input);
        //this.parent = PersistentUtils.readExpression(input);
        //this.kind = PersistentUtils.readExpressionKind(input);
        this.operands = PersistentUtils.readExpressions(new ArrayList<CsmExpression>(0), input);
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        boolean hadLambdas = input.readBoolean();
        this.lambdas = hadLambdas ? NOT_EMPTY : null;
    }
}
