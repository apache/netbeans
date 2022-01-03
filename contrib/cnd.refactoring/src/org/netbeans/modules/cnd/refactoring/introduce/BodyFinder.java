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
package org.netbeans.modules.cnd.refactoring.introduce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExceptionHandler;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmTryCatchStatement;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences.Visitor;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.api.IntroduceMethodRefactoring.IntroduceMethodContext;
import org.netbeans.modules.cnd.refactoring.api.IntroduceMethodRefactoring.VariableContext;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 */
public class BodyFinder {
    private final Document doc;
    private final FileObject fileObject;
    private final CsmFile file;
    private final int caretOffset;
    private final int selectionStart;
    private final int selectionEnd;
    private final AtomicBoolean canceled;
    private BodyResult result;

    public BodyFinder(Document doc, FileObject fileObject, CsmFile file, int caretOffset, int selectionStart, int selectionEnd, AtomicBoolean canceled) {
        this.doc = doc;
        this.fileObject = fileObject;
        this.file = file;
        this.caretOffset = caretOffset;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
        this.canceled = canceled;
    }

    public BodyResult findBody() {
        result = findBody(file.getDeclarations());
        return result;
    }

    private BodyResult findBody(Collection<? extends CsmOffsetableDeclaration> decls) {
        for(CsmOffsetableDeclaration decl : decls) {
            if (canceled.get()) {
                return null;
            }
            if (decl.getStartOffset() < selectionStart && selectionEnd < decl.getEndOffset()) {
                if (CsmKindUtilities.isFunctionDefinition(decl)) {
                    CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
                    final BodyResult res = findBody(def.getBody());
                    if (res != null) {
                        res.setFunction(def);
                    }
                    return res;
                } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    CsmNamespaceDefinition def = (CsmNamespaceDefinition) decl;
                    return findBody(def.getDeclarations());
                } else if (CsmKindUtilities.isClass(decl)) {
                    CsmClass cls = (CsmClass) decl;
                    return findBody(cls.getMembers());
                }
            }
        }
        return null;
    }

    private BodyResult findBody(CsmCompoundStatement body) {
        if (body != null) {
            if (body.getStartOffset() <= selectionStart && selectionEnd <= body.getEndOffset()) {
                // selection inside body
                final List<CsmStatement> statements = body.getStatements();
                int begStatement = -1;
                int endStatement = -1;
                for(int i = 0; i < statements.size(); i++) {
                    if (canceled.get())  {
                        break;
                    }
                    final CsmStatement st = statements.get(i);
                    if (st.getStartOffset() < selectionStart && selectionEnd < st.getEndOffset()) {
                        // selection inside statement
                        return findBodyInStatement(st);
                    }
                    if (st.getStartOffset() < selectionStart &&  selectionStart < st.getEndOffset()) {
                        return null;
                    }
                    if (st.getStartOffset() < selectionEnd &&  selectionEnd < st.getEndOffset()) {
                        return null;
                    }
                    if (begStatement == -1 && selectionStart <= st.getStartOffset()) {
                        begStatement = i;
                    }
                    if (endStatement == -1 && selectionEnd < st.getStartOffset()) {
                        endStatement = i;
                        break;
                    }
                }
                if (endStatement == -1) {
                    endStatement = statements.size();
                }
                if (begStatement >= 0 && endStatement > begStatement) {
                    //found selection in body
                    return new BodyResult(doc, fileObject, body, begStatement, endStatement);
                }
            }
        }
        return null;
    }

    private BodyResult findBodyInStatement(final CsmStatement st) {
        switch(st.getKind()) {
            case CATCH:
            case COMPOUND:
                return findBody((CsmCompoundStatement)st);
            case SWITCH:
            {
                CsmSwitchStatement switchStmt = (CsmSwitchStatement) st;
                final CsmStatement body = switchStmt.getBody();
                if (body != null) {
                    return findBodyInStatement(body);
                }
                return null;
            }
            case FOR:
            case RANGE_FOR:
            case WHILE:
            case DO_WHILE:
            {
                CsmLoopStatement loopStmt = (CsmLoopStatement) st;
                CsmStatement body = loopStmt.getBody();
                if (body != null) {
                    return findBodyInStatement(body);
                }
                return null;
            }
            case TRY_CATCH:
            {
                BodyResult res;
                CsmTryCatchStatement tryStmt = (CsmTryCatchStatement) st;
                CsmStatement body = tryStmt.getTryStatement();
                if (body != null) {
                    res = findBodyInStatement(body);
                    if (res != null) {
                        return res;
                    }
                }
                List<CsmExceptionHandler> handlers = tryStmt.getHandlers();
                if (handlers != null) {
                    for(int i = 0; i < handlers.size(); i++) {
                        CsmExceptionHandler handler = handlers.get(i);
                        res = findBody(handler);
                        if (res != null) {
                            return res;
                        }
                    }
                }
                return null;
            }
            case IF:
            {
                CsmIfStatement ifStmt = (CsmIfStatement) st;
                CsmStatement thenStmt = ifStmt.getThen();
                CsmStatement elseStmt = ifStmt.getElse();
                if (thenStmt != null) {
                    BodyResult res = findBodyInStatement(thenStmt);
                    if (res != null) {
                        return res;
                    }
                }
                if (elseStmt != null) {
                    BodyResult res = findBodyInStatement(elseStmt);
                    if (res != null) {
                        return res;
                    }
                }
                return null;
            }
        }
        return null;
    }

    public static final class VariableInfo implements VariableContext {
        private final CsmVariable variable;
        private final List<CsmReference> refs = new ArrayList<>();
        private boolean accessBefore;
        private boolean accessAfter;
        private boolean accessInside;
        private boolean topLevelDeclaration;
        private boolean writeAccessInside;
        private VariableInfo(CsmVariable variable, boolean topLevelDeclaration) {
            this.variable = variable;
            this.topLevelDeclaration = topLevelDeclaration;
        }

        @Override
        public CsmVariable getVariable() {
            return variable;
        }
        @Override
        public boolean isAccessBefore() {
            return accessBefore;
        }
        @Override
        public boolean isAccessAfter() {
            return accessAfter;
        }
        @Override
        public boolean isAccessInside() {
            return accessInside;
        }
        @Override
        public boolean isTopLevelDeclaration() {
            return topLevelDeclaration;
        }
        @Override
        public List<CsmReference> getReferences() {
            return refs;
        }
        public boolean isWriteAccessInside() {
            return writeAccessInside;
        }
        private void setAccessBefore() {
            accessBefore = true;
        }
        private void setAccessAfter() {
            accessAfter = true;
        }
        private void setAccessInside() {
            accessInside = true;
        }
        private void addReference(CsmReference reference) {
            refs.add(reference);
        }
        private void setWriteAccessInside() {
            this.writeAccessInside = true;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(variable.getName());
            buf.append(" Access "); //NOI18N
            if (accessBefore) {
                buf.append('*');
            } else {
                buf.append('-');
            }
            buf.append('[');
            if (accessInside) {
                buf.append('*');
            } else {
                buf.append('-');
            }
            buf.append(']');
            if (accessAfter) {
                buf.append('*');
            } else {
                buf.append('-');
            }
            buf.append(" Refs "+refs.size()); //NOI18N
            return buf.toString();
        }
    }

    public static final class VariablesInfo {
        private final List<VariableInfo> variables;
        private VariablesInfo() {
            variables = new ArrayList<>();
        }
        private VariableInfo add(CsmVariable variable, boolean topLevelDeclaration) {
            for(VariableInfo info: variables) {
                if (info.variable.equals(variable)) {
                    return info;
                }
            }
            VariableInfo res = new VariableInfo(variable, topLevelDeclaration);
            variables.add(res);
            return res;
        }
        private List<VariableContext> getImportantVariables() {
            List<VariableContext> res = new ArrayList<>();
            for(VariableInfo info: variables) {
                if (info.isTopLevelDeclaration()) {
                    continue;
                }
                if (info.isAccessInside()) {
                    res.add(info);
                }
            }
            return res;
        }

        private List<VariableInfo> topLevelVariablesUsedOutside() {
            List<VariableInfo> res = new ArrayList<>();
            for(VariableInfo info: variables) {
                if (info.isTopLevelDeclaration()) {
                    if(info.isAccessAfter() || info.accessBefore) {
                        res.add(info);
                    }
                }
            }
            return res;
        }

        private void calculateReferencedVariables(final Document doc, final int startSelection, final int endSelection) {
            doc.render(new Runnable() {

                @Override
                public void run() {
                    TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = hi.tokenSequence();
                    calculateReferencedVariables(ts, startSelection, endSelection);
                }
            });
        }

        private void calculateReferencedVariables(TokenSequence<?> ts, int startSelection, int endSelection) {
            for(VariableInfo info: variables) {
                if (info.isTopLevelDeclaration()) {
                    continue;
                }
                if (info.isAccessInside()) {
                    boolean writeAccess = false;
                    for(CsmReference references : info.getReferences()) {
                        if (startSelection <= references.getStartOffset() && references.getEndOffset() <= endSelection) {
                            if (isWriteAccess(ts, references.getStartOffset())) {
                                writeAccess = true;
                                break;
                            }
                        }
                    }
                    if (writeAccess) {
                        info.setWriteAccessInside();
                    }
                }
            }
        }

        private boolean isWriteAccess(TokenSequence<?> ts, int offset) {
            ts.move(offset);
            if (ts.moveNext()) {
                Token<?> token = lookNextImportant(ts);
                // id -- or -=
                if (token != null &&(
                    token.id() == CppTokenId.PLUSPLUS ||
                    token.id() == CppTokenId.MINUSMINUS ||
                    token.id() == CppTokenId.EQ ||
                    token.id() == CppTokenId.PLUSEQ ||
                    token.id() == CppTokenId.MINUSEQ ||
                    token.id() == CppTokenId.STAREQ ||
                    token.id() == CppTokenId.SLASHEQ ||
                    token.id() == CppTokenId.AMPEQ ||
                    token.id() == CppTokenId.BAREQ ||
                    token.id() == CppTokenId.CARETEQ ||
                    token.id() == CppTokenId.PERCENTEQ ||
                    token.id() == CppTokenId.LTLTEQ ||
                    token.id() == CppTokenId.GTGTEQ)) {
                    return true;
                }
                token = lookPrevImportant(ts, 1);
                if (token != null &&(
                    token.id() == CppTokenId.PLUSPLUS ||
                    token.id() == CppTokenId.MINUSMINUS)) {
                    return true;
                }
                if (token != null &&(
                    token.id() == CppTokenId.AMP)) {
                    token = lookPrevImportant(ts, 2);
                    if (token != null &&(
                        token.id() == CppTokenId.LPAREN ||
                        token.id() == CppTokenId.EQ ||
                        token.id() == CppTokenId.COMMA)) {
                        return true;
                    }

                    return true;
                }
            }
            return false;
        }

        private Token<?> lookNextImportant(TokenSequence<?> ts){
            int index = ts.index();
            try {
                while(ts.moveNext()){
                    if (ts.token().id() == CppTokenId.WHITESPACE ||
                        ts.token().id() == CppTokenId.ESCAPED_WHITESPACE ||
                        ts.token().id() == CppTokenId.NEW_LINE ||
                        ts.token().id() == CppTokenId.LINE_COMMENT ||
                        ts.token().id() == CppTokenId.BLOCK_COMMENT ||
                        ts.token().id() == CppTokenId.DOXYGEN_COMMENT ||
                        ts.token().id() == CppTokenId.DOXYGEN_LINE_COMMENT ||
                        ts.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                        continue;

                    }
                    return ts.token();
                }
                return null;
            } finally {
                ts.moveIndex(index);
                ts.moveNext();
            }
        }

        private Token<?> lookPrevImportant(TokenSequence<?> ts, int which){
            int index = ts.index();
            try {
                while(ts.movePrevious()){
                    if (ts.token().id() == CppTokenId.WHITESPACE ||
                        ts.token().id() == CppTokenId.ESCAPED_WHITESPACE ||
                        ts.token().id() == CppTokenId.NEW_LINE ||
                        ts.token().id() == CppTokenId.LINE_COMMENT ||
                        ts.token().id() == CppTokenId.BLOCK_COMMENT ||
                        ts.token().id() == CppTokenId.DOXYGEN_COMMENT ||
                        ts.token().id() == CppTokenId.DOXYGEN_LINE_COMMENT ||
                        ts.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                        continue;

                    }
                    which--;
                    if (which == 0) {
                        return ts.token();
                    }
                }
                return null;
            } finally {
                ts.moveIndex(index);
                ts.moveNext();
            }
        }
    }

    public static final class BodyResult implements IntroduceMethodContext {
        private final Document doc;
        private final FileObject fileObject;
        private final CsmCompoundStatement body;
        private final int startStatment;
        private final int endStatement;
        private int startSelection = -1;
        private int endSelection = -1;
        private int insertionPoint = -1;
        private CsmFunctionDefinition function;
        private CsmFunction functionDeclaration;
        private CsmClass enclosingClass;
        private FunctionKind functionKind;
        private CsmScope insertScope;
        private AtomicBoolean canceled;
        private LinkedList<CsmStatement.Kind> stack;
        private ArrayList<Pair<Integer,Integer>> innerBlocks;
        private VariablesInfo vars;

        private BodyResult(Document doc, FileObject fileObject, CsmCompoundStatement body, int startStatment, int endStatement) {
            this.doc = doc;
            this.fileObject = fileObject;
            this.body = body;
            this.startStatment = startStatment;
            this.endStatement = endStatement;
        }

        @Override
        public Document getDocument() {
            return doc;
        }

        @Override
        public boolean isC() {
            return MIMENames.C_MIME_TYPE.equals(fileObject.getMIMEType());
        }

        @Override
        public boolean isApplicable(AtomicBoolean canceled) {
            this.canceled = canceled;
            stack = new LinkedList<>();
            innerBlocks = new ArrayList<>();
            vars = new VariablesInfo();
            // check flow
            List<CsmStatement> statements = body.getStatements();
            for(int i = startStatment; i < endStatement; i++) {
                if (canceled.get()){
                    return false;
                }
                final CsmStatement st = statements.get(i);
                if (startSelection == -1) {
                    startSelection = st.getStartOffset();
                }
                endSelection = st.getEndOffset();
                if (!isApplicableStatement(st)) {
                    return false;
                }
            }
            extendEndSelection();
            if (canceled.get()) {
                return false;
            }
            // gather in-out references
            visit();
            // check deferred "local" variables
            if(!vars.topLevelVariablesUsedOutside().isEmpty()) {
                return false;
            }
            if (canceled.get()) {
                return false;
            }
            vars.calculateReferencedVariables(doc, startSelection, endSelection);
            if (canceled.get()) {
                return false;
            }
            return true;
        }

        @Override
        public List<VariableContext> getImportantVariables() {
            return vars.getImportantVariables();
        }

        @Override
        public int getSelectionFrom() {
            return startSelection;
        }

        @Override
        public int getSelectionTo() {
            return endSelection;
        }

        @Override
        public CsmFunctionDefinition getFunction() {
            return function;
        }

        @Override
        public CsmFunction getFunctionDeclaration() {
            return functionDeclaration;
        }

        @Override
        public CsmClass getEnclosingClass() {
            return enclosingClass;
        }

        @Override
        public CsmScope getInsertScope() {
            return insertScope;
        }

        @Override
        public FunctionKind getFunctionKind() {
            return functionKind;
        }

        private void extendEndSelection() {
            doc.render(new Runnable() {

                @Override
                public void run() {
                    TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = hi.tokenSequence();
                    ts.move(endSelection);
                    boolean newLineFound = false;
                    while (ts.moveNext()) {
                        Token<?> token = ts.token();
                        if (token.id() == CppTokenId.WHITESPACE) {
                            endSelection = ts.offset()+token.length();
                            continue;
                        } else if (token.id() == CppTokenId.SEMICOLON) {
                            endSelection = ts.offset()+token.length();
                            continue;
                        } else if (CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                            endSelection = ts.offset()+token.length();
                            continue;
                        } else {
                            return;
                        }
                    }
                }
            });
        }

        // point before containing function
        @Override
        public int getInsetionOffset() {
            if (insertionPoint == -1) {
                final AtomicInteger point = new AtomicInteger(function.getStartOffset());
                doc.render(new Runnable() {

                    @Override
                    public void run() {
                        TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
                        TokenSequence<?> ts = hi.tokenSequence();
                        ts.move(function.getStartOffset());
                        boolean newLineFound = false;
                        while (ts.movePrevious()) {
                            Token<?> token = ts.token();
                            if (token.id() == CppTokenId.WHITESPACE) {
                                point.set(ts.offset());
                                continue;
                            } else if (token.id() == CppTokenId.NEW_LINE) {
                                if (!newLineFound) {
                                    point.set(ts.offset());
                                    newLineFound = true;
                                    continue;
                                } else {
                                    return;
                                }
                            } else if (CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                                point.set(ts.offset());
                                newLineFound = false;
                                continue;
                            } else {
                                return;
                            }
                        }
                        point.set(0);
                    }
                });
                insertionPoint = point.get();
            }
            return insertionPoint;
        }

        private void setFunction(CsmFunctionDefinition function) {
            this.function = function;
            insertScope = function.getScope();
            functionDeclaration = function.getDeclaration();
            if (CsmKindUtilities.isClassMember(functionDeclaration)) {
                enclosingClass = ((CsmMember)functionDeclaration).getContainingClass();
            }
            if (enclosingClass != null) {
                if (function.equals(functionDeclaration)) {
                    functionKind = FunctionKind.MethodDeclarationDefinition;
                } else {
                    functionKind = FunctionKind.MethodDefinition;
                }
            } else {
                functionKind = FunctionKind.Function;
            }
        }

        private void visit() {

            Visitor visitor = new Visitor() {
                @Override
                public boolean cancelled() {
                    return canceled.get();
                }

                @Override
                public void visit(CsmReferenceContext context) {
                    CsmReference reference = context.getReference();
                    CsmObject referencedObject = reference.getReferencedObject();
                    if (!CsmKindUtilities.isVariable(referencedObject)) {
                        return;
                    }
                    // It is a variable
                    CsmVariable var = (CsmVariable) referencedObject;
                    if (!function.getContainingFile().equals(var.getContainingFile())) {
                        return;
                    }
                    // Variable is in the same file
                    if (!(function.getStartOffset() < var.getStartOffset() && var.getEndOffset() < function.getEndOffset())) {
                        return;
                    }
                    // It is local variable or parameter
                    if (isBlockLocalVariable(var)) {
                        return;
                    }
                    // Variable is visible outside selection
                    VariableInfo info = vars.add(var, false);
                    info.addReference(reference);
                    if (reference.getEndOffset() < startSelection) {
                        // reference before selection
                        info.setAccessBefore();
                    } else if (endSelection < reference.getStartOffset()) {
                        // reference after selection
                        info.setAccessAfter();
                    } else {
                        // reference inside selection
                        info.setAccessInside();
                    }
                }
                private boolean isBlockLocalVariable(CsmVariable var) {
                    for (Pair<Integer,Integer> pair : innerBlocks) {
                        if (pair.first() < var.getStartOffset() && var.getEndOffset() < pair.second()) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            CsmFileReferences.getDefault().accept(function, doc, visitor);
        }

        private boolean isApplicableStatement(CsmStatement st) {
            switch(st.getKind()) {
                case LABEL:
                case GOTO:
                    // TODO check references
                    return false;
                case DECLARATION:
                    if (hasKind(CsmStatement.Kind.COMPOUND)) {
                        return true;
                    } else {
                        // top level declaration
                        // check later that it is a true "local" variable
                        CsmDeclarationStatement decl = (CsmDeclarationStatement) st;
                        for(CsmDeclaration var : decl.getDeclarators()) {
                            if (CsmKindUtilities.isVariable(var)) {
                                vars.add((CsmVariable) var, true);
                            }
                        }
                        return true;
                    }
                case EXPRESSION:
                    return true;
                case CATCH:
                case COMPOUND:
                    return isApplicableBlock((CsmCompoundStatement)st);
                case IF:
                    return isApplicableIf((CsmIfStatement)st);
                case SWITCH:
                    return isApplicableSwitch((CsmSwitchStatement)st);
                case RANGE_FOR:
                case WHILE:
                case DO_WHILE:
                case FOR:
                    return isApplicableLoop((CsmLoopStatement)st);
                case CASE:
                case DEFAULT:
                    return hasKind(CsmStatement.Kind.SWITCH);
                case BREAK:
                    if (hasKind(CsmStatement.Kind.SWITCH, CsmStatement.Kind.WHILE, CsmStatement.Kind.DO_WHILE, CsmStatement.Kind.FOR, CsmStatement.Kind.RANGE_FOR)) {
                        return true;
                    }
                    // TODO: need an additional analysis
                    return false;
                case CONTINUE:
                    if (hasKind(CsmStatement.Kind.WHILE, CsmStatement.Kind.DO_WHILE, CsmStatement.Kind.FOR, CsmStatement.Kind.RANGE_FOR)) {
                        return true;
                    }
                    // TODO: need an additional analysis
                    return false;
                case RETURN:
                    // TODO: need an additional analysis
                    return false;
                case TRY_CATCH:
                    return isApplicableTry((CsmTryCatchStatement)st);
                case THROW:
                    // TODO: need an additional analysis
                    return false;
            }
            return true;
        }

        private boolean isApplicableBlock(CsmCompoundStatement statement) {
            stack.addLast(CsmStatement.Kind.COMPOUND);
            try {
                innerBlocks.add(Pair.of(statement.getStartOffset(), statement.getEndOffset()));
                for(CsmStatement st: statement.getStatements()) {
                    if (canceled.get()){
                        return false;
                    }
                    if (!isApplicableStatement(st)) {
                        return false;
                    }
                }
            } finally {
                stack.removeLast();
            }
            return true;
        }

        private boolean isApplicableTry(CsmTryCatchStatement tryStmt) {
            CsmStatement aBody = tryStmt.getTryStatement();
            if (aBody != null) {
                if (!isApplicableStatement(aBody)) {
                    return false;
                }
            }
            List<CsmExceptionHandler> handlers = tryStmt.getHandlers();
            if (handlers != null) {
                for(int i = 0; i < handlers.size(); i++) {
                    CsmExceptionHandler handler = handlers.get(i);
                    if (!isApplicableStatement(handler)) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isApplicableSwitch(CsmSwitchStatement statement) {
            stack.addLast(CsmStatement.Kind.SWITCH);
            try {
                CsmStatement st = statement.getBody();
                if (st != null) {
                    return isApplicableStatement(st);
                }
                return true;
            } finally {
                stack.removeLast();
            }
        }

        private boolean isApplicableLoop(CsmLoopStatement statement) {
            stack.addLast(statement.getKind());
            try {
                // loops can define local variables in init statement
                innerBlocks.add(Pair.of(statement.getStartOffset(), statement.getEndOffset()));
                CsmStatement st = statement.getBody();
                if (st != null) {
                    return isApplicableStatement(st);
                }
                return true;
            } finally {
                stack.removeLast();
            }
        }

        private boolean isApplicableIf(CsmIfStatement statement) {
            CsmStatement aElse = statement.getElse();
            if (aElse != null) {
                if (!isApplicableStatement(aElse)) {
                    return false;
                }
            }
            CsmStatement then = statement.getThen();
            if (then != null) {
                if (!isApplicableStatement(then)) {
                    return false;
                }
            }
            return true;
        }

        private boolean hasKind(CsmStatement.Kind ... kinds) {
            for(CsmStatement.Kind kind : kinds) {
                if (stack.contains(kind)) {
                    return true;
                }
            }
            return false;
        }

    }
}
