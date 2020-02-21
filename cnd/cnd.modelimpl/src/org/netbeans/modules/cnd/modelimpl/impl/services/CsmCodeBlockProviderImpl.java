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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmCondition;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExceptionHandler;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmRangeForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmReturnStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmTryCatchStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.editor.CsmCodeBlockProvider;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.editor.CsmCodeBlockProvider.class)
public class CsmCodeBlockProviderImpl extends CsmCodeBlockProvider {

    @Override
    public Scope getScope(Document doc, int position) {
        final CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, true);
        if (csmFile != null) {
            List<CsmObject> list = new ArrayList<>();
            list.add(csmFile);
            getFunction(list, csmFile, position);
            if (list.size()>1) {
                return new ScopeImpl(list, list.size() - 1);
            }
        }
        return null;
    }
    
    private void getFunction(List<CsmObject> list, CsmFile file, int position) {
        if (file != null) {
            for(CsmOffsetableDeclaration decl : file.getDeclarations()) {
                if (decl.getStartOffset() <= position && position <= decl.getEndOffset()) {
                    list.add(decl);
                    getInternalDeclaration(list, decl, position);
                    return;
                }
            }
        }
    }
    
    private void getInternalDeclaration(List<CsmObject> list, CsmOffsetableDeclaration parent, int position){
        if (CsmKindUtilities.isClass(parent)) {
            CsmClass cls = (CsmClass) parent;
            for(CsmMember decl : cls.getMembers()){
                if (decl.getStartOffset() <= position && position <= decl.getEndOffset()) {
                    list.add(decl);
                    getInternalDeclaration(list, decl, position);
                    return;
                }
            }
        } else if(CsmKindUtilities.isNamespaceDefinition(parent)) {
            CsmNamespaceDefinition ns = (CsmNamespaceDefinition) parent;
            for(CsmOffsetableDeclaration decl : ns.getDeclarations()) {
                if (decl.getStartOffset() < position && position < decl.getEndOffset()) {
                    list.add(decl);
                    getInternalDeclaration(list, decl, position);
                    return;
                }
            }
        } else if (CsmKindUtilities.isFunction(parent)) {
            CsmFunction def = (CsmFunction) parent;
            CsmFunctionParameterList parameterList = def.getParameterList();
            if (parameterList != null && parameterList.getStartOffset() < position && position < parameterList.getEndOffset()) {
                list.add(parameterList);
                for (CsmParameter parameter : parameterList.getParameters()) {
                    if (parameter != null && parameter.getStartOffset() < position && position < parameter.getEndOffset()) {
                        list.add(parameter);
                        return;
                    }
                }
                return;
            }
            if (CsmKindUtilities.isFunctionDefinition(parent)) {
                CsmCompoundStatement body = ((CsmFunctionDefinition)parent).getBody();
                if (body != null) {
                    list.add(body);
                    findInner(list, body, position);
                }
            }
        }
    }

    private void findInner(List<CsmObject> list, CsmCompoundStatement body, int position){
        for(CsmStatement st : body.getStatements()) {
            if (st.getStartOffset() < position && position < st.getEndOffset()) {
                list.add(st);
                findInner(list, st, position);
                return;
            }
        }
    }
    /*
     * finds inner object for given offset and update context
     */
    private void findInner(List<CsmObject> list, CsmStatement stmt, int offset) {
        if( stmt == null ) {
            return;
        }
        CsmStatement.Kind kind = stmt.getKind();
        switch (kind) {
            case COMPOUND:
                findInner(list, (CsmCompoundStatement) stmt, offset);
                return;
            case IF:
                findInner(list, (CsmIfStatement) stmt, offset);
                return;
            case TRY_CATCH:
                findInner(list, (CsmTryCatchStatement) stmt, offset);
                return;
            case CATCH:
                findInner(list, (CsmExceptionHandler) stmt, offset);
                return;
            case DECLARATION:
                findInner(list, (CsmDeclarationStatement) stmt, offset);
                return;
            case WHILE:
            case DO_WHILE:
                findInner(list, (CsmLoopStatement) stmt, offset);
                return;
            case FOR:
                findInner(list, (CsmForStatement) stmt, offset);
                return;
            case RANGE_FOR:
                findInner(list, (CsmRangeForStatement) stmt, offset);
                return;
            case SWITCH:
                findInner(list, (CsmSwitchStatement) stmt, offset);
                return;
            case EXPRESSION:
                findInner(list, ((CsmExpressionStatement) stmt).getExpression(), offset);
                return;
            case RETURN:
                findInner(list, ((CsmReturnStatement) stmt).getReturnExpression(), offset);
                return;
        }
    }
    
    private void findInner(List<CsmObject> list, CsmTryCatchStatement stmt, int offset) {
        CsmStatement tryStatement = stmt.getTryStatement();
        if (tryStatement != null && tryStatement.getStartOffset() < offset && offset < tryStatement.getEndOffset()) {
            list.add(tryStatement);
            findInner(list, tryStatement, offset);
            return;
        }
        for (CsmExceptionHandler handler : stmt.getHandlers()) {
            if (handler.getStartOffset() < offset && offset < handler.getEndOffset()) {
                list.add(handler);
                findInner(list, handler, offset);
                return;
            }
        }
    }

    private void findInner(List<CsmObject> list, CsmExceptionHandler stmt, int offset) {
        findInner(list, (CsmCompoundStatement) stmt, offset);
    }

    private void findInner(List<CsmObject> list, CsmIfStatement stmt, int offset) {
        CsmCondition condition = stmt.getCondition();
        if (condition != null && condition.getStartOffset() < offset && offset < condition.getEndOffset()) {
            list.add(condition);
            return;
        }
        CsmStatement then = stmt.getThen();
        if (then != null && then.getStartOffset() < offset && offset < then.getEndOffset()) {
            list.add(then);
            findInner(list, then, offset);
            return;
        }
        CsmStatement aElse = stmt.getElse();
        if (aElse != null && aElse.getStartOffset() < offset && offset < aElse.getEndOffset()) {
            list.add(aElse);
            findInner(list, aElse, offset);
        }
    }

    private void findInner(List<CsmObject> list, CsmDeclarationStatement stmt, int offset) {
        List<CsmDeclaration> decls = stmt.getDeclarators();
        if (decls != null) {
            for(CsmDeclaration decl : decls) {
                if (CsmKindUtilities.isOffsetableDeclaration(decl)) {
                    CsmOffsetableDeclaration d = (CsmOffsetableDeclaration) decl;
                    if (d.getStartOffset() <= offset && offset <= d.getEndOffset()) {
                        list.add(d);
                        getInternalDeclaration(list, d, offset);
                        return;
                    }
                }
            }
        }
    }

    private void findInner(List<CsmObject> list, CsmLoopStatement stmt, int offset) {
        CsmCondition condition = stmt.getCondition();
        if (condition != null && condition.getStartOffset() < offset && offset < condition.getEndOffset()) {
            list.add(condition);
            return;
        }
        CsmStatement body = stmt.getBody();
        if (body != null && body.getStartOffset() < offset && offset < body.getEndOffset()) {
            list.add(body);
            findInner(list, body, offset);
        }
    }

    private void findInner(List<CsmObject> list, CsmForStatement stmt, int offset) {
        List<CsmOffsetable> forList = new ArrayList<>();
        CsmStatement initStatement = stmt.getInitStatement();
        if (initStatement != null) {
            forList.add(initStatement);
        }
        CsmCondition condition = stmt.getCondition();
        if (condition != null) {
            forList.add(condition);
        }
        CsmExpression iterationExpression = stmt.getIterationExpression();
        if (iterationExpression != null) {
            forList.add(iterationExpression);
        }
        if (initStatement != null && initStatement.getStartOffset() < offset && offset < initStatement.getEndOffset()) {
            if (forList.size() > 1) {
                list.add(new CompoundObject(forList));
            }
            list.add(initStatement);
            findInner(list, initStatement, offset);
            return;
        }
        if (condition != null && condition.getStartOffset() < offset && offset < condition.getEndOffset()) {
            if (forList.size() > 1) {
                list.add(new CompoundObject(forList));
            }
            list.add(condition);
            return;
        }
        if (iterationExpression != null && iterationExpression.getStartOffset() < offset && offset < iterationExpression.getEndOffset()) {
            if (forList.size() > 1) {
                list.add(new CompoundObject(forList));
            }
            list.add(iterationExpression);
            return;
        }
        CsmStatement body = stmt.getBody();
        if (body != null && body.getStartOffset() < offset && offset < body.getEndOffset()) {
            list.add(body);
            findInner(list, body, offset);
        }
    }
    
    private void findInner(List<CsmObject> list, CsmRangeForStatement stmt, int offset) {
        List<CsmOffsetable> forList = new ArrayList<>();
        CsmStatement declStatement = stmt.getDeclaration();
        if (declStatement != null) {
            forList.add(declStatement);
        }
        CsmExpression initializerExpression = stmt.getInitializer();
        if (initializerExpression != null) {
            forList.add(initializerExpression);
        }
        if (declStatement != null && declStatement.getStartOffset() < offset && offset < declStatement.getEndOffset()) {
            if (forList.size() > 1) {
                list.add(new CompoundObject(forList));
            }
            list.add(declStatement);
            findInner(list, declStatement, offset);
            return;
        }
        if (initializerExpression != null && initializerExpression.getStartOffset() < offset && offset < initializerExpression.getEndOffset()) {
            if (forList.size() > 1) {
                list.add(new CompoundObject(forList));
            }
            list.add(initializerExpression);
            return;
        }
        CsmStatement body = stmt.getBody();
        if (body != null && body.getStartOffset() < offset && offset < body.getEndOffset()) {
            list.add(body);
            findInner(list, body, offset);
        }
    }    

    private void findInner(List<CsmObject> list, CsmSwitchStatement stmt, int offset) {
        CsmCondition condition = stmt.getCondition();
        if (condition != null && condition.getStartOffset() < offset && offset < condition.getEndOffset()) {
            list.add(condition);
            return;
        }
        CsmStatement body = stmt.getBody();
        if (body != null && body.getStartOffset() < offset && offset < body.getEndOffset()) {
            list.add(body);
            findInner(list, body, offset);
        }
    }
    
    private void findInner(List<CsmObject> list, CsmExpression expr, int offset) {
        if(expr != null) {
            for (CsmStatement csmStatement : expr.getLambdas()) {
                CsmDeclarationStatement lambda = (CsmDeclarationStatement)csmStatement;
                if (lambda != null && lambda.getStartOffset() < offset && offset < lambda.getEndOffset()) {
                    list.add(lambda);
                    findInner(list, lambda, offset);
                }
            }            
        }
    }
    
    private static final class CompoundObject implements CsmOffsetable {
        private final List<CsmOffsetable> delegate;
        
        private CompoundObject(List<CsmOffsetable> delegate) {
            this.delegate = delegate;
        }

        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStartOffset() {
            return delegate.get(0).getStartOffset();
        }

        @Override
        public int getEndOffset() {
            return delegate.get(delegate.size()-1).getEndOffset();
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ScopeImpl implements Scope {
        private final List<CsmObject> list;
        private final int level;
        
        private ScopeImpl(List<CsmObject> list, int level) {
            this.list = list;
            this.level = level;
        }

        @Override
        public int getStartOffset() {
            CsmObject obj = list.get(level);
            if (CsmKindUtilities.isOffsetable(obj)) {
                return ((CsmOffsetable)obj).getStartOffset();
            }
            return 0;
        }

        @Override
        public int getEndOffset() {
            CsmObject obj = list.get(level);
            if (CsmKindUtilities.isOffsetable(obj)) {
                return ((CsmOffsetable)obj).getEndOffset();
            }
            return 0;
        }

        @Override
        public Scope getParentScope() {
            if (level > 0) {
                CsmObject obj = list.get(level - 1);
                if (CsmKindUtilities.isOffsetable(obj)) {
                    return new ScopeImpl(list, level - 1);
                }
            }
            return null;
        }
    }
}
