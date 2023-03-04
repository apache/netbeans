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
package org.netbeans.modules.java.editor.base.fold;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.base.semantic.Utilities;

/**
 *
 * @author Dusan Balek
 */
public final class JavaElementFoldVisitor<T> extends CancellableTreePathScanner<Object, Object> {

    private final List<Integer> anchors = new ArrayList<>();
    private final List<T> folds = new ArrayList<>();
    private final CompilationInfo info;
    private final CompilationUnitTree cu;
    private final SourcePositions sp;
    private boolean stopped;
    private int initialCommentStopPos = Integer.MAX_VALUE;
    private final Document doc;
    private final FoldCreator<T> creator;

    public JavaElementFoldVisitor(CompilationInfo info, CompilationUnitTree cu, SourcePositions sp, Document doc, FoldCreator<T> creator) {
        this.info = info;
        this.cu = cu;
        this.sp = sp;
        this.doc = doc;
        this.creator = creator;
    }

    private void addFold(T f, int anchor) {
        if (f != null) {
            this.folds.add(f);
            this.anchors.add(anchor);
        }
    }

    public List<Integer> getAnchors() {
        return anchors;
    }

    public List<T> getFolds() {
        return folds;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void checkInitialFold() {
        try {
            TokenHierarchy<?> th = info.getTokenHierarchy();
            TokenSequence<JavaTokenId>  ts = th.tokenSequence(JavaTokenId.language());

            while (ts.moveNext()) {
                if (ts.offset() >= initialCommentStopPos)
                    break;

                Token<JavaTokenId> token = ts.token();

                if (token.id() == JavaTokenId.BLOCK_COMMENT || token.id() == JavaTokenId.JAVADOC_COMMENT) {
                    int startOffset = ts.offset();
                    addFold(creator.createInitialCommentFold(startOffset, startOffset + token.length()), startOffset);
                    break;
                }
            }
        } catch (ConcurrentModificationException e) {
            //from TokenSequence, document probably changed, stop
            stopped = true;
        }
    }

    private void handleJavadoc(Tree t) throws BadLocationException, ConcurrentModificationException {
        int start = (int) sp.getStartPosition(cu, t);

        if (start == (-1))
            return ;

        if (start < initialCommentStopPos)
            initialCommentStopPos = start;

        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId>  ts = th.tokenSequence(JavaTokenId.language());

        if (ts.move(start) == Integer.MAX_VALUE) {
            return;//nothing
        }

        while (ts.movePrevious()) {
            Token<JavaTokenId> token = ts.token();

            if (token.id() == JavaTokenId.JAVADOC_COMMENT) {
                int startOffset = ts.offset();
                addFold(creator.createJavadocFold(startOffset, startOffset + token.length()), startOffset);
                if (startOffset < initialCommentStopPos)
                    initialCommentStopPos = startOffset;
            }
            if (   token.id() != JavaTokenId.WHITESPACE
                && token.id() != JavaTokenId.BLOCK_COMMENT
                && token.id() != JavaTokenId.LINE_COMMENT)
                break;
        }
    }

    private void handleTree(Tree node, Tree javadocTree, boolean handleOnlyJavadoc) {
        handleTree((int)sp.getStartPosition(cu, node), node, javadocTree, handleOnlyJavadoc);
    }

    private void handleTree(int symStart, Tree node, Tree javadocTree, boolean handleOnlyJavadoc) {
        try {
            if (!handleOnlyJavadoc) {
                int start = (int)sp.getStartPosition(cu, node);
                int end   = (int)sp.getEndPosition(cu, node);

                if (start != (-1) && start < end) {
                    addFold(creator.createCodeBlockFold(start, end), symStart);
                }
            }

            handleJavadoc(javadocTree != null ? javadocTree : node);
        } catch (BadLocationException e) {
            //the document probably changed, stop
            stopped = true;
        } catch (ConcurrentModificationException e) {
            //from TokenSequence, document probably changed, stop
            stopped = true;
        }
    }

    @Override
    public Object visitMethod(MethodTree node, Object p) {
        super.visitMethod(node, p);
        handleTree((int)sp.getStartPosition(cu, node), node.getBody(), node, false);
        return null;
    }

    @Override
    public Object visitClass(ClassTree node, Object p) {
        super.visitClass(node, Boolean.TRUE);
        try {
            if (p == Boolean.TRUE) {
                int start = Utilities.findBodyStart(info, node, cu, sp, doc);
                int end   = (int)sp.getEndPosition(cu, node);

                if (start != (-1) && start < end) {
                    addFold(creator.createInnerClassFold(start, end), (int)sp.getStartPosition(cu, node));
                  }
            }

            handleJavadoc(node);
        } catch (BadLocationException e) {
            //the document probably changed, stop
            stopped = true;
        } catch (ConcurrentModificationException e) {
            //from TokenSequence, document probably changed, stop
            stopped = true;
        }
        return null;
    }

    @Override
    public Object visitVariable(VariableTree node,Object p) {
        super.visitVariable(node, p);
        if (TreeUtilities.CLASS_TREE_KINDS.contains(getCurrentPath().getParentPath().getLeaf().getKind()))
            handleTree(node, null, true);
        return null;
    }

    @Override
    public Object visitBlock(BlockTree node, Object p) {
        super.visitBlock(node, p);
        //check static/dynamic initializer:
        TreePath path = getCurrentPath();

        if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
            handleTree(node, null, false);
        }

        return null;
    }

    @Override
    @SuppressWarnings("fallthrough")
    public Object visitCompilationUnit(CompilationUnitTree node, Object p) {
        int importsStart = Integer.MAX_VALUE;
        int importsEnd   = -1;

        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId>  ts = th.tokenSequence(JavaTokenId.language());
        
        IMP: for (ImportTree imp : node.getImports()) {
            // eliminate invalid imports; erroenous imports at the start/end of import block
            // will not be folder.
            Tree qualIdent = imp.getQualifiedIdentifier();
            if (qualIdent == null) {
                continue;
            }
            while (qualIdent.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree mst = (MemberSelectTree)qualIdent;
                if (mst.getIdentifier().contentEquals("<error>")) { // NOI18N
                    // ignore erroneous imports
                    continue IMP;
                }
                qualIdent = mst.getExpression();
            }

            // don't rely on Javac for the end position: in case of missing semicolon the import consumes all whitespace
            // including comments / javadocs up to the following declaration or text. Rather scan tokens and consume only the import
            // identifier + semi.
            int start = (int) sp.getStartPosition(cu, imp);
            int identPos = (int) sp.getStartPosition(cu, qualIdent);
            int end = identPos;
            boolean firstNewline = true;
            ts.move(identPos);
            IDENT: while (ts.moveNext()) {
                Token<JavaTokenId>  tukac = ts.token();
                switch (tukac.id()) {
                    case IDENTIFIER:
                    case DOT:
                    case STAR:
                        firstNewline = false;
                        end = ts.offset() + tukac.length();
                        break;
                    case SEMICOLON:
                        end = (int) sp.getEndPosition(cu, imp);
                        break IDENT;
                    case WHITESPACE: {
                        if (firstNewline) {
                            int endl = tukac.text().toString().indexOf("\n"); // NOI18N
                            if (endl > -1) {
                                // remember the first newline after some ident/star/dot content
                                end = ts.offset() + endl; 
                                firstNewline = true;
                            }
                        }
                        // fall through
                    }
                    case LINE_COMMENT: case BLOCK_COMMENT: 
                        continue;
                    default:
                        break IDENT;
                }
            }

            if (importsStart > start)
                importsStart = start;

            if (end > importsEnd) {
                importsEnd = end;
            }
        }

        if (importsEnd != (-1) && importsStart != (-1)) {
            if (importsStart < initialCommentStopPos) {
                initialCommentStopPos = importsStart;
            }
            importsStart += 7/*"import ".length()*/;

            if (importsStart < importsEnd) {
                addFold(creator.createImportsFold(importsStart, importsEnd), importsStart);
            }
        }
        return super.visitCompilationUnit(node, p);
    }

    public static interface FoldCreator<T> {

        T createImportsFold(int start, int end);
        T createInnerClassFold(int start, int end);
        T createCodeBlockFold(int start, int end);
        T createJavadocFold(int start, int end);
        T createInitialCommentFold(int start, int end);
    }
}
