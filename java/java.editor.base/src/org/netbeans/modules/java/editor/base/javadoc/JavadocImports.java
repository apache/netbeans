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

package org.netbeans.modules.java.editor.base.javadoc;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.lexer.JavaTokenId;

import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Jan Pokorsky
 */
public final class JavadocImports {

    private JavadocImports() {
    }
    
    /**
     * Computes all unresolved (not imported) {@link Element}s referenced by
     * all javadocs of all class members of the passed java context {@code javac}.
     * 
     * @param javac a java context to search for all top level classes and
     *          all their members
     * @return names that have to be resoved (imported).
     */
    public static Set<String> computeUnresolvedImports(CompilationInfo javac) {
        UnresolvedImportScanner scanner = new UnresolvedImportScanner(javac);
        scanner.scan(javac.getCompilationUnit(), null);
        return scanner.unresolved;
    }
    
    /**
     * Computes all {@link Element}s referenced by javadoc of the passed element
     * {@code el}.
     * 
     * @param javac a java context
     * @param el an element to search
     * @return referenced elements.
     */
    public static Set<TypeElement> computeReferencedElements(final CompilationInfo javac, final TreePath tp) {
        final DocTrees trees = javac.getDocTrees();
        DocCommentTree docComment = trees.getDocCommentTree(tp);
        
        if (docComment == null) return Collections.emptySet();
        
        final Set<TypeElement> result = new HashSet<TypeElement>();
        
        new DocTreePathScanner<Void, Void>() {
            @Override public Void visitReference(ReferenceTree node, Void p) {
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                        Element el = trees.getElement(getCurrentPath());
                        
                        if (el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
                            result.add((TypeElement) el);
                        }
                        return super.visitIdentifier(node, p);
                    }
                    public Void scan(Iterable<? extends TreePath> toAnalyze, Void p) {
                        for (TreePath tp : toAnalyze) {
                            scan(tp, p);
                        }
                        return null;
                    }
                }.scan(referenceEmbeddedSourceNodes(javac, getCurrentPath()), null);
                return super.visitReference(node, p);
            }
            @Override public Void visitSee(SeeTree node, Void p) {
                return super.visitSee(node, p);
            }
        }.scan(new DocTreePath(tp, docComment), null);
        
        return result;
    }
    
    /**
     * Computes all {@link Element}s referenced by javadoc of the passed element
     * {@code el}.
     * 
     * @param javac a java context
     * @param el an element to search
     * @param toFind an element to find in favadoc
     * @return referenced elements.
     */
    public static List<Token> computeTokensOfReferencedElements(final CompilationInfo javac, final TreePath forElement, final Element toFind) {
        final DocTrees trees = javac.getDocTrees();
        final DocCommentTree docComment = javac.getDocTrees().getDocCommentTree(forElement);
        
        if (docComment == null) return Collections.emptyList();
        
        final List<Token> result = new ArrayList<Token>();
        
        new DocTreePathScanner<Void, Void>() {
            @Override public Void visitReference(ReferenceTree node, Void p) {
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                        if (toFind.equals(trees.getElement(getCurrentPath()))) {
                            handleUsage((int) trees.getSourcePositions().getStartPosition(javac.getCompilationUnit(), node));
                        }
                        return null;
                    }
                    @Override public Void visitMemberSelect(MemberSelectTree node, Void p) {
                        if (toFind.equals(trees.getElement(getCurrentPath()))) {
                            int[] span = javac.getTreeUtilities().findNameSpan(node);
                            if (span != null) {
                                handleUsage(span[0]);
                            }
                            return null;
                        }
                        return super.visitMemberSelect(node, p);
                    }
                    public Void scan(Iterable<? extends TreePath> toAnalyze, Void p) {
                        for (TreePath tp : toAnalyze) {
                            scan(tp, p);
                        }
                        return null;
                    }
                }.scan(referenceEmbeddedSourceNodes(javac, getCurrentPath()), null);
                if (toFind.equals(trees.getElement(getCurrentPath()))) {
                    int[] span = javac.getTreeUtilities().findNameSpan(docComment, node);
                    if (span != null) {
                        handleUsage(span[0]);
                    }
                    return null;
                }
                return super.visitReference(node, p);
            }
            private TokenSequence<JavadocTokenId> javadoc;
            private void handleUsage(int start) {
                if (javadoc == null) {
                    javadoc = getJavadocTS(javac, start);
                        
                    if (javadoc == null) {
                        //not really expected:
                        return ;
                    }
                }
                javadoc.move(start);
                if (javadoc.moveNext()) {
                    result.add(javadoc.token());
                }
            }
            @Override
            public Void visitParam(ParamTree node, Void p) {
                if (   node.getName() != null
                    && toFind.equals(paramElementFor(trees.getElement(forElement), node))) {
                    handleUsage((int) trees.getSourcePositions().getStartPosition(javac.getCompilationUnit(), docComment, node.getName()));
                    return null;
                }
                return super.visitParam(node, p);
            }
            @Override public Void visitSee(SeeTree node, Void p) {
                return super.visitSee(node, p);
            }
        }.scan(new DocTreePath(forElement, docComment), null);
        
        return result;
    }

    private static Element paramElementFor(Element methodOrClass, ParamTree ptag) {
        ElementKind kind = methodOrClass.getKind();
        List<? extends Element> params = Collections.emptyList();
        if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
            ExecutableElement ee = (ExecutableElement) methodOrClass;
            params = ptag.isTypeParameter()
                    ? ee.getTypeParameters()
                    : ee.getParameters();
        } else if (kind.isClass() || kind.isInterface()) {
            TypeElement te = (TypeElement) methodOrClass;
            params = te.getTypeParameters();
        }

        for (Element param : params) {
            if (param.getSimpleName().contentEquals(ptag.getName().getName())) {
                return param;
            }
        }
        return null;
    }
    
    /**
     * Resolves class or member of the reference {@code (class#member)},
     * parameter {@code (@param parameter)} or type parameter {@code (@param <type_param>)}
     * with respect to the passed {@code offset}.
     * 
     * @param javac a java context
     * @param offset offset pointing to javadoc part to resolve
     * @return the found element or {@code null}.
     */
    public static Element findReferencedElement(final CompilationInfo javac, final int offset) {
        final DocTrees trees = javac.getDocTrees();
        final TreePath tp = JavadocCompletionUtils.findJavadoc(javac, offset);
        
        if (tp == null) return null;
        
        final DocCommentTree docComment = javac.getDocTrees().getDocCommentTree(tp);
        
        if (docComment == null) return null;
        
        final DocSourcePositions positions = trees.getSourcePositions();
        final Element[] result = new Element[1];
        
        new DocTreePathScanner<Void, Void>() {
            @Override public Void scan(DocTree node, Void p) {
                if (   node != null
                    && positions.getStartPosition(javac.getCompilationUnit(), docComment, node) <= offset
                    && positions.getEndPosition(javac.getCompilationUnit(), docComment, node) >= offset) {
                    return super.scan(node, p);
                }
                
                return null;
            }
            @Override public Void visitReference(ReferenceTree node, Void p) {
                int[] span = javac.getTreeUtilities().findNameSpan(docComment, node);
                if (   span != null
                    && span[0] <= offset
                    && span[1] >= offset) {
                    result[0] = trees.getElement(getCurrentPath());
                    return null;
                }
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                        if (   positions.getStartPosition(javac.getCompilationUnit(), node) <= offset
                            && positions.getEndPosition(javac.getCompilationUnit(), node) >= offset) {
                            result[0] = trees.getElement(getCurrentPath());
                        }
                        return null;
                    }
                    @Override public Void visitMemberSelect(MemberSelectTree node, Void p) {
                        int[] span = javac.getTreeUtilities().findNameSpan(node);
                        if (   span != null
                            && span[0] <= offset
                            && span[1] >= offset) {
                            result[0] = trees.getElement(getCurrentPath());
                            return null;
                        }
                        return super.visitMemberSelect(node, p);
                    }
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        return super.visitMemberReference(node, p);
                    }
                    public Void scan(Iterable<? extends TreePath> toAnalyze, Void p) {
                        for (TreePath tp : toAnalyze) {
                            scan(tp, p);
                        }
                        return null;
                    }
                }.scan(referenceEmbeddedSourceNodes(javac, getCurrentPath()), null);
                return super.visitReference(node, p);
            }
            @Override
            public Void visitParam(ParamTree node, Void p) {
                //XXX: getElement for the param's identifier???
                if (   node.getName() != null
                    && positions.getStartPosition(javac.getCompilationUnit(), docComment, node.getName()) <= offset
                    && positions.getEndPosition(javac.getCompilationUnit(), docComment, node.getName()) >= offset) {
                    result[0] = paramElementFor(trees.getElement(tp), node);
                    
                    return null;
                }
                return super.visitParam(node, p);
            }
            @Override public Void visitSee(SeeTree node, Void p) {
                return super.visitSee(node, p);
            }
        }.scan(new DocTreePath(tp, docComment), null);
        
        return result[0];
    }
    
    public static Token findNameTokenOfReferencedElement(final CompilationInfo javac, final int offset) {
        final DocTrees trees = javac.getDocTrees();
        final TreePath tp = JavadocCompletionUtils.findJavadoc(javac, offset);
        
        if (tp == null) return null;
        
        final DocCommentTree docComment = javac.getDocTrees().getDocCommentTree(tp);
        
        if (docComment == null) return null;
        
        final DocSourcePositions positions = trees.getSourcePositions();
        final Token[] result = new Token[1];
        
        new DocTreePathScanner<Void, Void>() {
            @Override public Void scan(DocTree node, Void p) {
                if (   node != null
                    && positions.getStartPosition(javac.getCompilationUnit(), docComment, node) <= offset
                    && positions.getEndPosition(javac.getCompilationUnit(), docComment, node) >= offset) {
                    return super.scan(node, p);
                }
                
                return null;
            }
            @Override public Void visitReference(ReferenceTree node, Void p) {
                int[] span = javac.getTreeUtilities().findNameSpan(docComment, node);
                if (   span != null
                    && span[0] <= offset
                    && span[1] >= offset) {
                    handleUsage(offset);
                    return null;
                }
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                        if (   positions.getStartPosition(javac.getCompilationUnit(), node) <= offset
                            && positions.getEndPosition(javac.getCompilationUnit(), node) >= offset) {
                            handleUsage(offset);
                        }
                        return null;
                    }
                    @Override public Void visitMemberSelect(MemberSelectTree node, Void p) {
                        int[] span = javac.getTreeUtilities().findNameSpan(node);
                        if (   span != null
                            && span[0] <= offset
                            && span[1] >= offset) {
                            handleUsage(offset);
                            return null;
                        }
                        return super.visitMemberSelect(node, p);
                    }
                    @Override public Void visitMemberReference(MemberReferenceTree node, Void p) {
                        return super.visitMemberReference(node, p);
                    }
                    public Void scan(Iterable<? extends TreePath> toAnalyze, Void p) {
                        for (TreePath tp : toAnalyze) {
                            scan(tp, p);
                        }
                        return null;
                    }
                }.scan(referenceEmbeddedSourceNodes(javac, getCurrentPath()), null);
                return super.visitReference(node, p);
            }
            private void handleUsage(int start) {
                TokenSequence<JavadocTokenId> javadoc = getJavadocTS(javac, start);

                if (javadoc == null) {
                    //not really expected:
                    return ;
                }

                javadoc.move(start);

                if (javadoc.moveNext()) {
                    result[0] = javadoc.token();
                }
            }
            @Override
            public Void visitParam(ParamTree node, Void p) {
                //XXX: getElement for the param's identifier???
                if (   node.getName() != null
                    && positions.getStartPosition(javac.getCompilationUnit(), docComment, node.getName()) <= offset
                    && positions.getEndPosition(javac.getCompilationUnit(), docComment, node.getName()) >= offset) {
                    result[0] = findNameTokenOfParamTag(offset, getJavadocTS(javac, offset));
                    
                    return null;
                }
                return super.visitParam(node, p);
            }
            @Override public Void visitSee(SeeTree node, Void p) {
                return super.visitSee(node, p);
            }
        }.scan(new DocTreePath(tp, docComment), null);
        
        return result[0];
    }

    private static Token<JavadocTokenId> findNameTokenOfParamTag(int startPos, TokenSequence<JavadocTokenId> jdTokenSequence) {
        Token<JavadocTokenId> result = null;
        if (isInsideParamName(jdTokenSequence, startPos)) {
            int delta = jdTokenSequence.move(startPos);
            if (jdTokenSequence.moveNext() && (JavadocTokenId.IDENT == jdTokenSequence.token().id() || JavadocTokenId.HTML_TAG == jdTokenSequence.token().id())
                    || delta == 0 && jdTokenSequence.movePrevious() && (JavadocTokenId.IDENT == jdTokenSequence.token().id() || JavadocTokenId.HTML_TAG == jdTokenSequence.token().id())) {
                result = jdTokenSequence.token();
            }
        }
        return result;
    }
    
    /**
     * Checks if the passed position {@code pos} is inside java reference of
     * some javadoc tag. This lightweight implementation ignores method parameters
     * 
     * @param jdts javadoc token sequence to search
     * @param pos position to check
     * @return {@code true} if the position is inside the reference.
     */
    public static boolean isInsideReference(TokenSequence<JavadocTokenId> jdts, int pos) {
        int delta = jdts.move(pos);
        if (jdts.moveNext() && JavadocTokenId.IDENT == jdts.token().id()
                || delta == 0 && jdts.movePrevious() && JavadocTokenId.IDENT == jdts.token().id()) {
            // go back and find tag
            boolean isBeforeWS = false; // is current tage before white space?
            while (jdts.movePrevious()) {
                Token<JavadocTokenId> jdt = jdts.token();
                switch (jdt.id()) {
                    case DOT:
                    case HASH:
                    case IDENT:
                        if (isBeforeWS) {
                            return false;
                        } else {
                            continue;
                        }
                    case OTHER_TEXT:
                        isBeforeWS |= JavadocCompletionUtils.isWhiteSpace(jdt);
                        isBeforeWS |= JavadocCompletionUtils.isLineBreak(jdt);
                        if (isBeforeWS) {
                            continue;
                        } else {
                            return false;
                        }
                    case TAG:
                        return isBeforeWS && isReferenceTag(jdt);
                    case HTML_TAG:
                        return false;
                    default:
                        return false;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the passed position {@code pos} is inside name part of
     * some javadoc param tag.
     *
     * @param jdts javadoc token sequence to search
     * @param pos position to check
     * @return {@code true} if the position is inside the param name.
     */
    public static boolean isInsideParamName(TokenSequence<JavadocTokenId> jdts, int pos) {
        int delta = jdts.move(pos);
        if ((jdts.moveNext() && (JavadocTokenId.IDENT == jdts.token().id() || JavadocTokenId.HTML_TAG == jdts.token().id())
                || delta == 0 && jdts.movePrevious() && (JavadocTokenId.IDENT == jdts.token().id() || JavadocTokenId.HTML_TAG == jdts.token().id()))
                && jdts.movePrevious() && JavadocTokenId.OTHER_TEXT == jdts.token().id()
                && jdts.movePrevious() && JavadocTokenId.TAG == jdts.token().id()) {
            return "@param".contentEquals(jdts.token().text());
        }
        return false;
    }
    
    private static final Set<String> ALL_REF_TAG_NAMES = new HashSet<String>(
            Arrays.asList("@link", "@linkplain", "@value", "@see", "@throws")); // NOI18N
    
    private static boolean isReferenceTag(Token<JavadocTokenId> tag) {
        String tagName = tag.text().toString().intern();
        return tag.id() == JavadocTokenId.TAG && ALL_REF_TAG_NAMES.contains(tagName);
    }
    
    private static TokenSequence<JavadocTokenId> getJavadocTS(CompilationInfo javac, int start) {
        TokenSequence<JavadocTokenId> javadoc = null;
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(javac.getTokenHierarchy(), start);

        if (ts.moveNext() && ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
            javadoc = ts.embedded(JavadocTokenId.language());
        }
        
        return javadoc;
    }

    private static final class UnresolvedImportScanner extends ErrorAwareTreePathScanner<Void, Void> {
        
        private final CompilationInfo javac;
        private Set<String> unresolved = new HashSet<String>();

        public UnresolvedImportScanner(CompilationInfo javac) {
            this.javac = javac;
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            resolveElement();
            return super.visitClass(node, p);
        }

        @Override
        public Void visitMethod(MethodTree node, Void p) {
            resolveElement();
            return super.visitMethod(node, p);
        }

        @Override
        public Void visitVariable(VariableTree node, Void p) {
            resolveElement();
            return super.visitVariable(node, p);
        }
        
        private void resolveElement() {
            final DocTrees trees = javac.getDocTrees();
            DocCommentTree dcComment = trees.getDocCommentTree(getCurrentPath());
            
            if (dcComment == null) return ;
            
            new DocTreePathScanner<Void, Void>() {
                @Override public Void visitReference(ReferenceTree node, Void p) {
                    new ErrorAwareTreePathScanner<Void, Void>() {
                        @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                            Element el = trees.getElement(getCurrentPath());
                            
                            if (el == null || el.asType().getKind() == TypeKind.ERROR) {
                                unresolved.add(node.getName().toString());
                            } else if (el.getKind() == ElementKind.PACKAGE) {
                                //does the package really exists? (see ComputeImports)
                                String s = ((PackageElement) el).getQualifiedName().toString();
                                if (javac.getElements().getPackageElement(s) == null) {
                                    //probably situation like:
                                    //Map.Entry e;
                                    //where Map is not imported
                                    unresolved.add(node.getName().toString());
                                }
                            }
                            return super.visitIdentifier(node, p);
                        }
                        public Void scan(Iterable<? extends TreePath> toAnalyze, Void p) {
                            for (TreePath tp : toAnalyze) {
                                scan(tp, p);
                            }
                            return null;
                        }
                    }.scan(referenceEmbeddedSourceNodes(javac, getCurrentPath()), null);
                    return super.visitReference(node, p);
                }
            }.scan(new DocTreePath(getCurrentPath(), dcComment), null);
        }
    }
    
    private static Iterable<? extends TreePath> referenceEmbeddedSourceNodes(CompilationInfo info, DocTreePath ref) {
        List<TreePath> result = new ArrayList<TreePath>();
        
        if (info.getTreeUtilities().getReferenceClass(ref) != null) {
            result.add(new TreePath(ref.getTreePath(), info.getTreeUtilities().getReferenceClass(ref)));
        }
        
        List<? extends Tree> params = info.getTreeUtilities().getReferenceParameters(ref);
        
        if (params != null) {
            for (Tree et : params) {
                result.add(new TreePath(ref.getTreePath(), et));
            }
        }
        
        return result;
    }
    
}
