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
package org.netbeans.modules.cnd.refactoring.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmCaseStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmCondition;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExceptionHandler;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmTryCatchStatement;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class StatementFinder {
    private final Document doc;
    private final CsmFile file;
    private final int caretOffset;
    private final int selectionStart;
    private final int selectionEnd;
    private final AtomicBoolean canceled;
    private CsmStatement container;
    
    public StatementFinder(Document doc, CsmFile file, int caretOffset, int selectionStart, int selectionEnd, AtomicBoolean canceled) {
        this.doc = doc;
        this.file = file;
        this.caretOffset = caretOffset;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
        this.canceled = canceled;
    }
    
    public CsmStatement findStatement() {
        container = findStatement(file.getDeclarations());
        return container;
    }
    
    private CsmStatement findStatement(Collection<? extends CsmOffsetableDeclaration> decls) {
        for(CsmOffsetableDeclaration decl : decls) {
            if (canceled.get()) {
                return null;
            }
            if (decl.getStartOffset() < selectionStart && selectionEnd < decl.getEndOffset()) {
                if (CsmKindUtilities.isFunctionDefinition(decl)) {
                    CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
                    return findStatementInBody(def.getBody());
                } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    CsmNamespaceDefinition def = (CsmNamespaceDefinition) decl;
                    return findStatement(def.getDeclarations());
                } else if (CsmKindUtilities.isClass(decl)) {
                    CsmClass cls = (CsmClass) decl;
                    return findStatement(cls.getMembers());
                }
            }
        }
        return null;
    }    

    private CsmStatement findStatementInBody(CsmCompoundStatement body) {
        if (body != null) {
            final List<CsmStatement> statements = body.getStatements();
            for(int i = 0; i < statements.size(); i++) {
                if (canceled.get())  {
                    break;
                }
                final CsmStatement st = statements.get(i);
                final int startOffset = st.getStartOffset();
                if (startOffset > selectionStart) {
                    break;
                }
                final int nexStartOffset;
                if(i+1 < statements.size()) {
                   nexStartOffset = statements.get(i+1).getStartOffset();
                } else {
                   nexStartOffset = body.getEndOffset();
                }
                if (startOffset <= selectionStart && selectionEnd < nexStartOffset) {
                    return findStatement(st, nexStartOffset);
                }
            }
        }
        return null;
    }

    private CsmStatement findStatement(final CsmStatement st, final int nexStartOffset) {
        switch(st.getKind()) {
            case COMPOUND:
                return findStatementInBody((CsmCompoundStatement)st);
            case SWITCH:
            {
                CsmSwitchStatement switchStmt = (CsmSwitchStatement) st;
                CsmCondition condition = switchStmt.getCondition();
                if (condition != null &&
                    condition.getStartOffset() <= selectionStart && selectionEnd <= condition.getEndOffset()) {
                    return st;
                }
                final CsmStatement body = switchStmt.getBody();
                if (body != null) {
                    final int startOffset = body.getStartOffset();
                    if (startOffset <= selectionStart && selectionEnd < nexStartOffset) {
                        CsmStatement res = findStatement(body, nexStartOffset);
                        if (res == null && !canceled.get()) {
                            res = st;
                        }
                        return res;
                    }
                }
                return null;
            }
            case FOR: 
            {
                CsmForStatement forStmt = (CsmForStatement) st;
                CsmStatement initStatement = forStmt.getInitStatement();
                if (initStatement != null && 
                    initStatement.getStartOffset() <= selectionStart && selectionEnd <= initStatement.getEndOffset()) {
                    return st;
                }
                CsmStatement body = forStmt.getBody();
                if (body != null) {
                    final int startOffset = body.getStartOffset();
                    if (startOffset <= selectionStart && selectionEnd < nexStartOffset) {
                        CsmStatement res = findStatement(body, nexStartOffset);
                        if (res == null && !canceled.get()) {
                            res = st;
                        }
                        return res;
                    }
                }
                return null;
            }
            case WHILE:
            case DO_WHILE:
            {
                CsmLoopStatement loopStmt = (CsmLoopStatement) st;
                //CsmCondition condition = loopStmt.getCondition();
                //if (condition != null && 
                //    condition.getStartOffset() <= selectionStart && selectionEnd <= condition.getEndOffset()) {
                //    StatementResult res = new StatementResult();
                //    res.container = st;
                //    return res;
                //}
                CsmStatement body = loopStmt.getBody();
                if (body != null) {
                    final int startOffset = body.getStartOffset();
                    int endOffset = nexStartOffset;
                    if (loopStmt.isPostCheck()) {
                        CsmCondition condition = loopStmt.getCondition();
                        if (condition != null) {
                            endOffset = condition.getStartOffset();
                        }
                    }
                    if (startOffset <= selectionStart && selectionEnd < endOffset) {
                        CsmStatement res = findStatement(body, nexStartOffset);
                        if (res == null && !canceled.get()) {
                            res = st;
                        }
                        return res;
                    }
                }
                return null;
            }
            case TRY_CATCH:
            {
                CsmTryCatchStatement tryStmt = (CsmTryCatchStatement) st;
                CsmStatement tryBody = tryStmt.getTryStatement();
                List<CsmExceptionHandler> handlers = tryStmt.getHandlers();
                if (tryBody != null) {
                    final int startOffset = tryBody.getStartOffset();
                    int endOffset = nexStartOffset;
                    if (handlers != null && handlers.size() > 0) {
                        endOffset = handlers.get(0).getStartOffset();
                    }
                    if (startOffset <= selectionStart && selectionEnd < endOffset) {
                        CsmStatement res = findStatement(tryBody, nexStartOffset);
                        if (res == null && !canceled.get()) {
                            res = st;
                        }
                        return res;
                    }
                }
                if (handlers != null) {
                    for(int i = 0; i < handlers.size(); i++) {
                        CsmExceptionHandler handler = handlers.get(i);
                        final int startOffset = handler.getStartOffset();
                        final int endOffset = handler.getEndOffset();
                        if (startOffset <= selectionStart && selectionEnd < endOffset) {
                            CsmStatement res = findStatement(handler, nexStartOffset);
                            if (res == null && !canceled.get()) {
                                res = st;
                            }
                            return res;
                        }
                    }
                }
                return null;
            }
            case IF:
            {
                CsmIfStatement ifStmt = (CsmIfStatement) st;
                CsmCondition condition = ifStmt.getCondition();
                if (condition != null && 
                    condition.getStartOffset() <= selectionStart && selectionEnd <= condition.getEndOffset()) {
                    return st;
                }
                CsmStatement thenStmt = ifStmt.getThen();
                CsmStatement elseStmt = ifStmt.getElse();
                if (thenStmt != null) {
                    final int startOffset = thenStmt.getStartOffset();
                    int endOffset = thenStmt.getEndOffset();
                    if (elseStmt != null) {
                        endOffset = elseStmt.getStartOffset();
                    }
                    if (startOffset <= selectionStart && selectionEnd < endOffset) {
                        CsmStatement res = findStatement(thenStmt, nexStartOffset);
                        if (res == null && !canceled.get()) {
                            res = st;
                        }
                        return res;
                    }
                }
                if (elseStmt != null) {
                    final int startOffset = elseStmt.getStartOffset();
                    int endOffset = nexStartOffset;
                    if (startOffset <= selectionStart && selectionEnd < endOffset) {
                        CsmStatement res = findStatement(elseStmt, nexStartOffset);
                        if (res == null && !canceled.get()) {
                            res = st;
                        }
                        return res;
                    }
                }
                return null;
            }
            case RETURN:
            {
                return st;
            }
            case DECLARATION:
            {
                CsmDeclarationStatement decls = (CsmDeclarationStatement) st;
                List<CsmDeclaration> declarators = decls.getDeclarators();
                for(CsmDeclaration decl : declarators) {
                    if (decl instanceof CsmVariable) {
                        CsmVariable d = (CsmVariable) decl;
                        if (d.getStartOffset() <= selectionStart && selectionEnd <= d.getEndOffset()) {
                            CsmExpression initialValue = d.getInitialValue();
                            if (initialValue != null) {
                                if (initialValue.getStartOffset() <= selectionStart && selectionEnd <= initialValue.getEndOffset()) {
                                    return st;
                                }
                            }
                        }
                    }
                }
                return null;
            }
            case EXPRESSION:
            {
                final int startOffset = st.getStartOffset();
                final int endOffset = st.getEndOffset();
                if (startOffset <= selectionStart && selectionEnd <= endOffset) {
                    return st;
                }
                return null;
            }
        }
        return null;
    }
    
    static class AddMissingCasesFixImpl implements Fix {
        
        private final CsmStatement st;
        private final FileObject fo;
        private final JTextComponent comp;
        private final BaseDocument doc;
        private final int caretOffset;

        AddMissingCasesFixImpl(CsmStatement st, Document doc, JTextComponent comp, FileObject fo, int caretOffset) {
            this.fo = fo;
            this.st = st;
            this.comp = comp;
            this.doc = (BaseDocument) doc;
            this.caretOffset = caretOffset;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(SuggestionFactoryTask.class, "FIX_AddMissingCases"); //NOI18N
        }

        @Override
        public ChangeInfo implement() throws Exception {
            CsmSwitchStatement sw = (CsmSwitchStatement) st;
            final CsmCondition condition = sw.getCondition();
            CsmStatement body = sw.getBody();
            final int endOfBody = body.getEndOffset()-1;
            final AtomicBoolean hasDefault = new AtomicBoolean(false);
            final Set<CsmEnumerator> caseSet = new HashSet<>();
            final String scope = getCases(body, caseSet, hasDefault);
            final StringBuilder buf = new StringBuilder();
            CsmExpressionResolver.resolveType(condition, null, new CsmExpressionResolver.ResolvedTypeHandler() {

                @Override
                public void process(CsmType resolvedType) {
                    CsmEnum en = null;
                    if (!caseSet.isEmpty()) {
                        CsmScope sc = caseSet.iterator().next().getScope();
                        if (CsmKindUtilities.isEnum(sc)) {
                            en = (CsmEnum) sc;
                        }
                    }
                    if (en == null) {
                        if (resolvedType != null) {
                            CsmClassifier classifier = CsmBaseUtilities.getClassifier(resolvedType, condition.getContainingFile(), condition.getStartOffset(), true);
                            if (CsmKindUtilities.isEnum(classifier)) {
                                en = (CsmEnum) classifier;
                            }
                        }
                    }
                    if (en != null) {
                        String scopeToAdd = scope;
                        if (scopeToAdd == null) {
                             scopeToAdd = getQualifiedName(CsmBaseUtilities.getLastCommonScope(condition.getScope(), en), en);
                             if (scopeToAdd == null) {
                                 scopeToAdd = "";
                             }
                             if (!scopeToAdd.isEmpty() && !scopeToAdd.endsWith("::")) { //NOI18N
                                 scopeToAdd += "::"; //NOI18N
                             }
                        }
                        if (en.isStronglyTyped()) {
                            scopeToAdd += en.getName() + "::";  //NOI18N
                        }
                        for(CsmEnumerator e : en.getEnumerators()) {
                            if (caseSet.contains(e)) {
                                continue;
                            }
                            buf.append("case "); //NOI18N
                            buf.append(scopeToAdd);
                            buf.append(e.getName());
                            buf.append(':');
                            buf.append('\n');
                            buf.append("break;"); //NOI18N
                            buf.append('\n');
                        }
                        if (!hasDefault.get()) {
                            buf.append("default:"); //NOI18N
                            buf.append('\n');
                            buf.append("break;"); //NOI18N
                            buf.append('\n');
                        }
                    }
                }
                
            });
            if (buf.length() == 0) {
                return null;
            }
            final ChangeInfo changeInfo = new ChangeInfo();
            doc.runAtomicAsUser(new Runnable() {
                @Override
                public void run() {
                    try {
                        doc.insertString(endOfBody, buf.toString(), null);
                        Position start = NbDocument.createPosition(doc, endOfBody, Position.Bias.Forward);
                        Position end = NbDocument.createPosition(doc, endOfBody + buf.length(), Position.Bias.Backward);
                        changeInfo.add(fo, start, end);
                        Indent indent = Indent.get(doc);
                        indent.lock();
                        try {
                            indent.reindent(endOfBody, endOfBody+buf.length()+1);
                        } finally {
                            indent.unlock();
                        }
                    }   catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            
            return changeInfo;
        }

        private String getQualifiedName(CsmScope from, CsmScope to) {
            List<CsmScope> scopes = new ArrayList<>();
            while (!Objects.equals(from, to) && CsmKindUtilities.isScopeElement(to)) {
                scopes.add(0, to);
                to = ((CsmScopeElement) to).getScope();
            }
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            for (CsmScope scope : scopes) {
                if (CsmKindUtilities.isEnum(scope)){
                    continue;
                }
                if (CsmKindUtilities.isNamedElement(scope)) {
                    CsmNamedElement named = (CsmNamedElement) scope;
                    if (!CharSequenceUtils.isNullOrEmpty(named.getName())) {
                        if (!first) {
                            sb.append("::"); // NOI18N
                        } else {
                            first = false;
                        }
                        // TODO: handle instantiations here
                        sb.append(named.getName());
                    }
                }
            }
            return sb.toString();
        }
        
        private String getCases(CsmStatement body, final Set<CsmEnumerator> caseSet, final AtomicBoolean hasDefault) {
            String aScope = null;
            if (body != null && body.getKind() == CsmStatement.Kind.COMPOUND) {
                for(CsmStatement c : ((CsmCompoundStatement)body).getStatements()) {
                    if (c.getKind() == CsmStatement.Kind.CASE) {
                        CsmCaseStatement caseSt = (CsmCaseStatement) c;
                        CsmExpression caseEx = caseSt.getExpression();
                        if (caseEx != null) {
                            Collection<CsmObject> resolveObjects = CsmExpressionResolver.resolveObjects(caseEx, null);
                            if (resolveObjects != null && !resolveObjects.isEmpty()) {
                                CsmObject next = resolveObjects.iterator().next();
                                if (CsmKindUtilities.isEnumerator(next)) {
                                    CsmEnumerator enumerator = (CsmEnumerator) next;
                                    caseSet.add(enumerator);
                                }
                            }
                            if (aScope == null) {
                                String t = caseEx.getText().toString();
                                if (t.indexOf("::") >= 0) { //NOI18N
                                    aScope = t.substring(0, t.lastIndexOf("::") + 2); //NOI18N
                                } else {
                                    aScope = ""; //NOI18N
                                }
                            }
                        }
                    } else if (c.getKind() == CsmStatement.Kind.DEFAULT) {
                        hasDefault.set(true);
                    }
                }
            }
            return aScope;
        }
    }


}
