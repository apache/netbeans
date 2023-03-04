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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.tools.Diagnostic;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.WHITESPACE;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class FindLocalUsagesQuery extends CancellableTreePathScanner<Void, Void> {
    
    
    private CompilationInfo info;
    private SourcePositions sp;
    private TreeUtilities treeUtils;
    private Set<MutablePositionRegion> usages;
    private Set<MutablePositionRegion> comments;
    private Element toFind;
    private Document doc;
    private DocTreePathScanner docScanner;
    private boolean searchComment;
    
    public FindLocalUsagesQuery() {
    }

    public void findUsages(Element element, CompilationInfo info, Document doc, boolean searchComment) {
        this.info = info;
        this.usages = new HashSet<>();
        this.comments = new HashSet<>();
        this.toFind = element;
        this.doc = doc;
        this.searchComment = searchComment;
        this.treeUtils = info.getTreeUtilities();
        this.sp = info.getTrees().getSourcePositions();
        this.docScanner = new DocTreePathScannerImpl();
        
        scan(info.getCompilationUnit(), null);
    }

    public Set<MutablePositionRegion> getUsages() {
        return usages;
    }

    public Set<MutablePositionRegion> getComments() {
        return comments;
    }
    
    private void handleJavadoc(TreePath el) {
        if(el != null) {
            switch(el.getLeaf().getKind()) {
                case METHOD:
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case VARIABLE:
                    DocCommentTree docCommentTree = info.getDocTrees().getDocCommentTree(el);
                    if(docCommentTree != null) {
                        DocTreePath docTreePath = new DocTreePath(el, docCommentTree);
                        docScanner.scan(docTreePath, null);
                    }
                default:
                    break;
            }
        }
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
        if (!searchComment) {
            return super.visitCompilationUnit(node, p);
        }

        if (toFind.getKind() == ElementKind.PARAMETER) {
            renameParameterInMethodComments(toFind);
        } else {
            String originalName = toFind.getSimpleName().toString();
            if (originalName!=null) {
                TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

                while (ts.moveNext()) {
                    Token<JavaTokenId> t = ts.token();
                    
                    if (isComment(t)) {
                        findAllInComment(t.text().toString(), ts.offset(), originalName);
                    }
                }
            }
        }
        return super.visitCompilationUnit(node, p);
    }
    
    /**
     * Renames the method (or constructor) parameter in comments. This method
     * considers comments before and inside the method declaration, and within
     * the method body.
     *
     * @param parameter the method or constructor parameter {@link Element}
     */
    private void renameParameterInMethodComments(final Element parameter) {
        final Tree method = info.getTrees().getPath(parameter).getParentPath().getLeaf();

        final String originalName = parameter.getSimpleName().toString();
        final int methodStart = (int) info.getTrees().getSourcePositions()
                .getStartPosition(info.getCompilationUnit(), method);
        final TokenSequence<JavaTokenId> tokenSequence = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        //renaming in comments before the method/constructor
        tokenSequence.move(methodStart);
        while (tokenSequence.movePrevious()) {
            final Token<JavaTokenId> token = tokenSequence.token();
            if (isComment(token)) {
                findAllInComment(token.text().toString(), tokenSequence.offset(), originalName);
            } else if (token.id() != WHITESPACE) {
                break;
            }            
        }

        //renaming in comments within the method/constructor declaration and body
        final int methodEnd = (int) info.getTrees().getSourcePositions()
                .getEndPosition(info.getCompilationUnit(), method);

        tokenSequence.move(methodStart);
        while (tokenSequence.moveNext() && tokenSequence.offset() < methodEnd) {
            final Token<JavaTokenId> token = tokenSequence.token();
            if (isComment(token)) {
                findAllInComment(token.text().toString(), tokenSequence.offset(), originalName);
            }
        }
    }
    
    /**
     * Changes all occurrences of {@code originalName} to the new name in the comment {@code text}.
     *
     * @param text the text of the comment token
     * @param offset the offset of the comment token
     * @param originalName the old name to change
     */
    private void findAllInComment(final String text, final int offset, final String originalName) {
        for (int index = text.indexOf(originalName); index != -1; index = text.indexOf(originalName, index + 1)) {
            if (index > 0 && Character.isJavaIdentifierPart(text.charAt(index - 1))) {
                continue;
            }
            if ((index + originalName.length() < text.length()) && Character.isJavaIdentifierPart(text.charAt(index + originalName.length()))) {
                continue;
            }
            //at least do not rename html start and end tags.
            if (text.charAt(index - 1) == '<' || text.charAt(index - 1) == '/') {
                continue;
            }
            try {
                MutablePositionRegion region = createRegion(doc, offset + index, offset + index + originalName.length());
                comments.add(region);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Checks if {@code token} represents a comment.
     * 
     * @param token the {@link Token} to check
     * @return {@code true} if {@code token} represents a line comment, block
     *          comment; {@code false} otherwise or javadoc.
     */
    private boolean isComment(final Token<JavaTokenId> token) {
        switch (token.id()) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
                return true;
            case JAVADOC_COMMENT:
            default:
                return false;
        }
    }
    
    @Override
    public Void visitIdentifier(IdentifierTree tree, Void d) {
        Element el = info.getTrees().getElement(getCurrentPath());
        if (toFind.equals(el)) {
            try {
                long start = sp.getStartPosition(info.getCompilationUnit(), tree);
                long end = sp.getEndPosition(info.getCompilationUnit(), tree);
                if(start != Diagnostic.NOPOS) {
                    MutablePositionRegion region = createRegion(doc, (int) start, (int) end);
                    usages.add(region);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.visitIdentifier(tree, d);
    }

    @Override
    public Void visitMemberReference(MemberReferenceTree node, Void p) {
        Element el = info.getTrees().getElement(getCurrentPath());
        if (toFind.equals(el)) {
            try {
                int[] span = treeUtils.findNameSpan(node);
                if(span != null) {
                    MutablePositionRegion region = createRegion(doc, span[0], span[1]);
                    usages.add(region);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.visitMemberReference(node, p);
    }
    
    @Override
    public Void visitMethod(MethodTree node, Void d) {
        Element el = info.getTrees().getElement(getCurrentPath());
        if (toFind.equals(el)) {
            try {
                int[] span = treeUtils.findNameSpan(node);
                if(span != null) {
                    MutablePositionRegion region = createRegion(doc, span[0], span[1]);
                    usages.add(region);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        handleJavadoc(getCurrentPath());
        return super.visitMethod(node, d);
    }
    
    @Override
    public Void visitMemberSelect(MemberSelectTree node, Void p) {
        Element el = info.getTrees().getElement(getCurrentPath());
        if (toFind.equals(el)) {
            try {
                int[] span = treeUtils.findNameSpan(node);
                if(span != null) {
                    MutablePositionRegion region = createRegion(doc, span[0], span[1]);
                    usages.add(region);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.visitMemberSelect(node, p);
    }
    
    @Override
    public Void visitVariable(VariableTree tree, Void d) {
        Element el = info.getTrees().getElement(getCurrentPath());
        if (toFind.equals(el)) {
            try {
                int[] span = treeUtils.findNameSpan(tree);
                if(span != null) {
                    MutablePositionRegion region = createRegion(doc, span[0], span[1]);
                    usages.add(region);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (el != null && el.getKind().isField()) {
            handleJavadoc(getCurrentPath());
        }
        return super.visitVariable(tree, d);
    }
    
    @Override
    public Void visitClass(ClassTree tree, Void d) {
        Element el = info.getTrees().getElement(getCurrentPath());
        if (toFind.equals(el)) {
            try {
                int[] span = treeUtils.findNameSpan(tree);
                if(span != null) {
                    MutablePositionRegion region = createRegion(doc, span[0], span[1]);
                    usages.add(region);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        handleJavadoc(getCurrentPath());
        return super.visitClass(tree, d);
    }

    @Override
    public Void visitTypeParameter(TypeParameterTree node, Void p) {
        Element el = info.getTrees().getElement(getCurrentPath());
        if (toFind.equals(el)) {
            try {
                int[] span = treeUtils.findNameSpan(node);
                if(span != null) {
                    MutablePositionRegion region = createRegion(doc, span[0], span[1]);
                    usages.add(region);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.visitTypeParameter(node, p);
    }

    @Override
    public Void visitImport(ImportTree node, Void p) {
        if (node.isStatic() && toFind.getModifiers().contains(Modifier.STATIC)) {
            Tree qualIdent = node.getQualifiedIdentifier();
            if (qualIdent.getKind() == Kind.MEMBER_SELECT) {
                MemberSelectTree mst = (MemberSelectTree) qualIdent;
                if (toFind.getSimpleName().contentEquals(mst.getIdentifier())) {
                    Element el = info.getTrees().getElement(new TreePath(getCurrentPath(), mst.getExpression()));
                    if (el != null && el.equals(toFind.getEnclosingElement())) {
                        try {
                            int[] span = treeUtils.findNameSpan(mst);
                            if(span != null) {
                                MutablePositionRegion region = createRegion(doc, span[0], span[1]);
                                usages.add(region);
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        return super.visitImport(node, p);
    }
    
    public static MutablePositionRegion createRegion(final Document doc, int start, int end) throws BadLocationException {
        Position startPos = NbDocument.createPosition(doc, start, Position.Bias.Backward);
        Position endPos = NbDocument.createPosition(doc, end, Position.Bias.Forward);
        MutablePositionRegion current = new MutablePositionRegion(startPos, endPos);
        return current;
    }
    
    private class DocTreePathScannerImpl extends DocTreePathScanner<DocTree, Element> {
        
        @Override
        public DocTree visitReference(ReferenceTree node, Element p) {
            DocTrees trees = info.getDocTrees();
            Element el = trees.getElement(getCurrentPath());
            if (el != null && el.equals(toFind)) {
                int[] span = treeUtils.findNameSpan(getCurrentPath().getDocComment(), node);
                if(span != null) {
                    try {
                        MutablePositionRegion region = createRegion(doc, span[0], span[1]);
                        usages.add(region);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return super.visitReference(node, p);
        }
        
        @Override
        public DocTree visitText(TextTree node, Element p) {
            if(searchComment) {
                DocTrees trees = info.getDocTrees();
                DocSourcePositions sourcePositions = trees.getSourcePositions();
                DocTreePath currentDocPath = getCurrentPath();
                if(toFind.getKind() == ElementKind.PARAMETER) {
                    VariableElement var = (VariableElement) toFind;
                    Element method = trees.getElement(currentDocPath);
                    if(!var.getEnclosingElement().equals(method)) {
                        return super.visitText(node, p);
                    }
                }
                String text = node.getBody();
                String name = toFind.getSimpleName().toString();
                if(text.contains(name)) {
                    int start = (int) sourcePositions.getStartPosition(info.getCompilationUnit(), currentDocPath.getDocComment(), node);
                    int length = name.length();
                    int offset = -1;
                    do {
                        offset = text.indexOf(name, ++offset);
                        if(offset != -1) {
                            try {
                                MutablePositionRegion region = createRegion(doc, start + offset, start + offset + length);
                                comments.add(region);
                            } catch(BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    } while (offset != -1);
                }
            }
            return super.visitText(node, p);
        }

        @Override
        public DocTree visitIdentifier(com.sun.source.doctree.IdentifierTree node, Element p) {
            DocTrees trees = info.getDocTrees();
            Element el = trees.getElement(getCurrentPath());
            if (el != null && el.equals(toFind)) {
                DocSourcePositions sp = trees.getSourcePositions();
                CompilationUnitTree cut = info.getCompilationUnit();
                DocCommentTree docComment = getCurrentPath().getDocComment();
                long start = sp.getStartPosition(cut, docComment, node);
                long end = sp.getEndPosition(cut, docComment, node);
                if(start != Diagnostic.NOPOS && end != Diagnostic.NOPOS) {
                    try {
                        MutablePositionRegion region = createRegion(doc, (int)start, (int)end);
                        usages.add(region);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return super.visitIdentifier(node, p);
        }
    }
}
