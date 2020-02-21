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

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmCaseStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExceptionHandler;
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
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class StatementNode implements BreadcrumbsElement {
    private final List<CsmTrueElement> trueCsmElements;
    private List<BreadcrumbsElement> children;
    private final BreadcrumbsElement parent;
    private final CharSequence displayName;
    private final Lookup lookup;
    private final DataObject cdo;
    private final int openOffset;
    private int startOffset;
    private int endOffset;
    private final Image icon;
    private final AtomicBoolean canceled;
    private final CharSequence text;

    static StatementNode createStatementNode(CsmOffsetable ofsetable, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        if (CsmKindUtilities.isStatement(ofsetable)) {
            CsmStatement statement = (CsmStatement) ofsetable;
            switch (statement.getKind()) {
                case COMPOUND:
                    return createBodyNode((CsmCompoundStatement) statement, decoration, parent, cdo, canceled, text);
                case IF:
                    return createIfNode((CsmIfStatement) statement, decoration, parent, cdo, canceled, text);
                case TRY_CATCH:
                    return createTryNode((CsmTryCatchStatement) statement, decoration, parent, cdo, canceled, text);
                case CATCH:
                    return createBodyNode((CsmExceptionHandler) statement, decoration, parent, cdo, canceled, text);
                case DECLARATION:
                    return createDeclarationNode((CsmDeclarationStatement) statement, decoration, parent, cdo, canceled, text);
                case WHILE:
                case DO_WHILE:
                    return createLoopNode((CsmLoopStatement) statement, decoration, parent, cdo, canceled, text);
                case FOR:
                    return createForNode((CsmForStatement) statement, decoration, parent, cdo, canceled, text);
                case RANGE_FOR:
                    return createRangeForNode((CsmRangeForStatement) statement, decoration, parent, cdo, canceled, text);
                case SWITCH:
                    return createSwitchNode((CsmSwitchStatement) statement, decoration, parent, cdo, canceled, text);
                case EXPRESSION:
                    return createExpressionNode((CsmExpressionStatement) statement, decoration, parent, cdo, canceled, text);
                case RETURN:
                    return createReturnNode((CsmReturnStatement) statement, decoration, parent, cdo, canceled, text);
                case CASE:
                    return createCaseNode((CsmCaseStatement) statement, decoration, parent, cdo, canceled, text);
                case DEFAULT:
                case BREAK:
                case CONTINUE:
                case GOTO:
                case LABEL:
                default:
                    return null;
            }
        } else if (CsmKindUtilities.isDeclaration(ofsetable)) {
            if (CsmKindUtilities.isClass(ofsetable)) {
                List<CsmTrueElement> inner = new ArrayList<CsmTrueElement>();
                CsmClass cls = (CsmClass) ofsetable;
                for(CsmMember member : cls.getMembers()) {
                    if (canceled != null && canceled.get()) {
                        break;
                    }
                    CsmTrueElement csmTrueElement = new CsmTrueElement((CsmOffsetable)member);
                    inner.add(csmTrueElement);
                }
                return new StatementNode(ofsetable, decoration, inner, parent, cdo, canceled, text);
            } else  if (CsmKindUtilities.isEnum(ofsetable)) {
                List<CsmTrueElement> inner = new ArrayList<CsmTrueElement>();
                CsmEnum cls = (CsmEnum) ofsetable;
                for(CsmEnumerator member : cls.getEnumerators()) {
                    if (canceled != null && canceled.get()) {
                        break;
                    }
                    CsmTrueElement csmTrueElement = new CsmTrueElement((CsmOffsetable)member);
                    inner.add(csmTrueElement);
                }
                return new StatementNode(ofsetable, decoration, inner, parent, cdo, canceled, text);

            } else if (CsmKindUtilities.isFunctionDefinition(ofsetable)) {
                List<CsmTrueElement> inner = new ArrayList<CsmTrueElement>();
                CsmCompoundStatement body = ((CsmFunctionDefinition)ofsetable).getBody();
                if (body != null) {
                    for(CsmStatement st : body.getStatements()) {
                        if (canceled != null && canceled.get()) {
                            break;
                        }
                        CsmTrueElement csmTrueElement = new CsmTrueElement((CsmOffsetable)st);
                        inner.add(csmTrueElement);
                    }
                }
                return new StatementNode(ofsetable, decoration, inner, parent, cdo, canceled, text);
            } else {
                return new StatementNode(ofsetable, decoration, Collections.<CsmTrueElement>emptyList(), parent, cdo, canceled, text);
            }
        }
        return null;
    }

    private static StatementNode createBodyNode(CsmCompoundStatement body, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        for(CsmStatement s : body.getStatements()) {
            if (canceled != null && canceled.get()) {
                break;
            }
            st.add(new CsmTrueElement(s));
        }
        return new StatementNode(body, decoration, st, parent, cdo, canceled, text);
    }

    private static StatementNode createIfNode(CsmIfStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        final CsmStatement thenStmt = stmt.getThen();
        int lastThenOffset = -1;
        if (thenStmt != null) {
            CsmTrueElement csmTrueElement = new CsmTrueElement(thenStmt);
            st.add(csmTrueElement);
            csmTrueElement.decoration = "then "; //NOI18N
            final ArrayList<CsmOffsetable> list = new ArrayList<CsmOffsetable>();
            list.add(thenStmt);
            csmTrueElement.body = list;
            lastThenOffset = thenStmt.getEndOffset();
        }
        CsmStatement elseStmt = stmt.getElse();
        while(elseStmt != null && elseStmt.getKind() == CsmStatement.Kind.IF) {
            CsmIfStatement elseIfStmt =  (CsmIfStatement) elseStmt;
            CsmTrueElement csmTrueElement = new CsmTrueElement(elseIfStmt);
            st.add(csmTrueElement);
            csmTrueElement.decoration = "else ";//NOI18N
            final ArrayList<CsmOffsetable> list = new ArrayList<CsmOffsetable>();
            csmTrueElement.body = list;
            CsmStatement elifThenStmt = elseIfStmt.getThen();
            if (elifThenStmt != null) {
                list.add(elifThenStmt);
                if (lastThenOffset >= 0) {
                    csmTrueElement.startOffset = lastThenOffset + 1;
                } else {
                    csmTrueElement.startOffset = elseStmt.getStartOffset();
                }
                csmTrueElement.endOffset = elifThenStmt.getEndOffset();
                lastThenOffset = elifThenStmt.getEndOffset();
            }
            elseStmt = elseIfStmt.getElse();
        }
        if (elseStmt != null) {
            CsmTrueElement csmTrueElement = new CsmTrueElement(elseStmt);
            st.add(csmTrueElement);
            csmTrueElement.decoration = "else ";//NOI18N
            if (lastThenOffset >= 0) {
                csmTrueElement.startOffset = lastThenOffset + 1;
            }
            final ArrayList<CsmOffsetable> list = new ArrayList<CsmOffsetable>();
            list.add(elseStmt);
            csmTrueElement.body = list;
        }
        return new StatementNode(stmt, decoration, st, parent, cdo, canceled, text);
    }

    private static StatementNode createTryNode(CsmTryCatchStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        if (stmt.getTryStatement() != null) {
            st.add(new CsmTrueElement(stmt.getTryStatement()));
        }
        for (CsmExceptionHandler handler : stmt.getHandlers()) {
            if (canceled != null && canceled.get()) {
                break;
            }
            st.add(new CsmTrueElement(handler));
        }
        return new StatementNode(stmt, decoration, st, parent, cdo, canceled, text);
    }

    private static StatementNode createLoopNode(CsmLoopStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            for(CsmStatement s : ((CsmCompoundStatement)body).getStatements()) {
                st.add(new CsmTrueElement(s));
            }
        } else if (body != null) {
            st.add(new CsmTrueElement(stmt.getBody()));
        }
        return new StatementNode(stmt, decoration, st, parent, cdo, canceled, text);
    }

    private static StatementNode createForNode(CsmForStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            for(CsmStatement s : ((CsmCompoundStatement)body).getStatements()) {
                if (canceled != null && canceled.get()) {
                    break;
                }
                st.add(new CsmTrueElement(s));
            }
        } else if (body != null) {
            st.add(new CsmTrueElement(stmt.getBody()));
        }
        return new StatementNode(stmt, decoration, st, parent, cdo, canceled, text);
    }

    private static StatementNode createRangeForNode(CsmRangeForStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        if (stmt.getDeclaration() != null) {
            st.add(new CsmTrueElement(stmt.getDeclaration()));
        }
        if (stmt.getBody() != null) {
            st.add(new CsmTrueElement(stmt.getBody()));
        }
        return new StatementNode(stmt, decoration, st, parent, cdo, canceled, text);
    }

    private static StatementNode createSwitchNode(CsmSwitchStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            CsmTrueElement currElement = null;
            int lastTrueStart = body.getStartOffset()+1;
            for (CsmStatement c : ((CsmCompoundStatement) body).getStatements()) {
                if (canceled != null && canceled.get()) {
                    break;
                }
                if (c.getKind() == CsmStatement.Kind.CASE || c.getKind() == CsmStatement.Kind.DEFAULT) {
                    if (currElement != null) {
                        st.add(currElement);
                    }
                    currElement = new CsmTrueElement(c);
                    currElement.startOffset = lastTrueStart;
                    if (c.getKind() == CsmStatement.Kind.CASE) {
                        currElement.decoration = "case "; //NOI18N
                    } else {
                        currElement.decoration = ""; //NOI18N
                    }
                    currElement.body = new ArrayList<CsmOffsetable>();
                    lastTrueStart = c.getEndOffset()+1;
                } else {
                    if (currElement != null) {
                        currElement.body.add(c);
                        currElement.endOffset = c.getEndOffset();
                        lastTrueStart = c.getEndOffset()+1;
                    }
                }
            }
            if (currElement != null) {
                st.add(currElement);
            }
        }
        return new StatementNode(stmt, decoration, st, parent, cdo, canceled, text);
    }

    private static StatementNode createExpressionNode(CsmExpressionStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        return new StatementNode(stmt, decoration, Collections.<CsmTrueElement>emptyList(), parent, cdo, canceled, text);
    }

    private static StatementNode createReturnNode(CsmReturnStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        return new StatementNode(stmt, decoration, Collections.<CsmTrueElement>emptyList(), parent, cdo, canceled, text);
    }

    private static StatementNode createDeclarationNode(CsmDeclarationStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        List<CsmTrueElement> inner = new ArrayList<CsmTrueElement>();
        for (CsmDeclaration decl : stmt.getDeclarators()) {
            if (CsmKindUtilities.isClass(decl)) {
                CsmTrueElement csmTrueElement = new CsmTrueElement((CsmOffsetable)decl);
                inner.add(csmTrueElement);
                CsmClass cls = (CsmClass) decl;
                csmTrueElement.body = new ArrayList<CsmOffsetable>();
                for(CsmMember member : cls.getMembers()) {
                    csmTrueElement.body.add(member);
                }
            } else if (CsmKindUtilities.isEnum(decl)) {
                CsmTrueElement csmTrueElement = new CsmTrueElement((CsmOffsetable)decl);
                inner.add(csmTrueElement);
                CsmEnum en = (CsmEnum) decl;
                csmTrueElement.body = new ArrayList<CsmOffsetable>();
                for(CsmEnumerator member : en.getEnumerators()) {
                    csmTrueElement.body.add(member);
                }
            } else if (CsmKindUtilities.isFunctionDefinition(decl)) {
                CsmTrueElement csmTrueElement = new CsmTrueElement((CsmOffsetable)decl);
                inner.add(csmTrueElement);
                CsmOffsetable body = ((CsmFunctionDefinition)decl).getBody();
                csmTrueElement.body = Collections.singletonList(body);
            }
        }
        if (inner.isEmpty()) {
            inner = Collections.<CsmTrueElement>emptyList();
        }
        return new StatementNode(stmt, decoration, inner, parent, cdo, canceled, text);
    }

    private static StatementNode createCaseNode(CsmCaseStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        return new StatementNode(stmt, decoration, Collections.<CsmTrueElement>emptyList(), parent, cdo, canceled, text);
    }

    private StatementNode(CsmTrueElement owner, String decoration, List<CsmTrueElement> trueCsmElements, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        this(owner.ofsetable, decoration, trueCsmElements, parent, cdo, canceled, text);
        startOffset = owner.startOffset;
        endOffset = owner.endOffset;
    }

    private StatementNode(CsmOffsetable owner, String decoration, List<CsmTrueElement> trueCsmElements, BreadcrumbsElement parent, DataObject cdo, AtomicBoolean canceled, CharSequence text) {
        if (CsmKindUtilities.isDeclaration(owner)) {
            icon = CsmImageLoader.getImage(owner);
        } else {
            icon = BreadcrumbsController.NO_ICON;
        }
        this.trueCsmElements = trueCsmElements;
        this.parent = parent;
        this.canceled = canceled;
        StringBuilder buf = new StringBuilder();
        if (decoration != null) {
            buf.append(decoration);
        }
        this.text = text;
        openOffset = owner.getStartOffset();
        startOffset = owner.getStartOffset();    
        int end = owner.getEndOffset();
        if (canceled == null || !canceled.get()) {
            CharSequence aText;
            //CharSequence aText = owner.getText();
            //loop: for(int i = 0; i < text.length(); i++) {
            int shift = 0;
            if (text == null) {
                aText = owner.getText();
                shift = -startOffset;
            } else {
                aText = text;
            }
            loop: for(int i = startOffset+shift; i < aText.length() && i < end+shift; i++) {
                char c = aText.charAt(i);
                switch(c) {
                    case ' ':
                        if (buf.length() > 0) {
                            buf.append(c);
                        }
                        break;
                    case '\t':
                        if (buf.length() > 0) {
                            buf.append(' ');
                        }
                        break;
                    case '\n':
                        break loop;
                    case '<':
                        buf.append("&lt;"); //NOI18N
                        break;
                    case '>':
                        buf.append("&gt;"); //NOI18N
                        break;
                    case '&':
                        buf.append("&amp;"); //NOI18N
                        break;
                    case '/':
                        if (i+1 < aText.length() && aText.charAt(i) == '/')  {
                            break loop;
                        }
                        break;
                    default:
                        buf.append(c);
                }
            }
        }
        for(CsmTrueElement s : trueCsmElements) {
            if (s.endOffset > end) {
                end = s.endOffset;
            }
        }
        endOffset = end;
        int i = buf.indexOf("{"); //NOI18N
        if (i > 0) {
            buf.setLength(i);
        }
        displayName = CharSequences.create(buf.toString().trim());
        this.cdo = cdo;
        lookup =  Lookups.fixed(new OpenableImpl(this));
    }

    @Override
    public String getHtmlDisplayName() {
        return displayName.toString();
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return icon;
    }

    @Override
    public List<BreadcrumbsElement> getChildren() {
        if (children == null) {
            children = new ArrayList<BreadcrumbsElement>();
            for(CsmTrueElement s : trueCsmElements) {
                if (canceled != null && canceled.get()) {
                    break;
                }
                List<? extends CsmOffsetable> body = s.body;
                String decoration = s.decoration;
                StatementNode node;
                if (body != null) {
                    if (body.size() == 1) {
                        CsmOffsetable content = body.get(0);
                        if (CsmKindUtilities.isStatement(content)) {
                            CsmStatement statement = (CsmStatement) content;
                            if (statement.getKind() == CsmStatement.Kind.COMPOUND) {
                                body = ((CsmCompoundStatement)statement).getStatements();
                            }
                        }
                    }
                    if (body.size() == 1 && s.ofsetable == body.get(0)) {
                        node = createStatementNode(s.ofsetable, decoration, this, cdo, canceled, text);
                        if (node != null) {
                            node.startOffset = s.startOffset;
                            node.endOffset = s.endOffset;
                        }
                    } else {
                        List<CsmTrueElement> sts = new ArrayList<CsmTrueElement>();
                        for(CsmOffsetable st : body) {
                            if (canceled != null && canceled.get()) {
                                break;
                            }
                            sts.add(new CsmTrueElement(st));
                        }
                        node = new StatementNode(s, decoration, sts, this, cdo, canceled, text);
                    }
                } else {
                    node = createStatementNode(s.ofsetable, decoration, this, cdo, canceled, text);
                }
                if (node != null) {
                    children.add(node);
                }
            }
        }
        return children;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public BreadcrumbsElement getParent() {
        return parent;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    private static final class OpenableImpl implements Openable, OpenCookie {

        private final StatementNode node;

        public OpenableImpl(StatementNode node) {
            this.node = node;
        }

        @Override
        public void open() {
            CsmUtilities.openSource(node.cdo.getPrimaryFile(), node.openOffset);
        }
    }
    
    private static final class CsmTrueElement {
        final CsmOffsetable ofsetable;
        List<CsmOffsetable> body;
        String decoration;
        int startOffset;
        int endOffset;
        
        private CsmTrueElement(CsmOffsetable offsetable) {
            this.ofsetable = offsetable;
            startOffset = offsetable.getStartOffset();
            endOffset = offsetable.getEndOffset();
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append(Integer.toString(startOffset));
            buf.append('-');
            buf.append(Integer.toString(endOffset));
            buf.append(' ');
            if (decoration != null) {
                buf.append(decoration);
            }
            return buf.toString();
        }
    }
}
