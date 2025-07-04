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
package org.netbeans.api.java.source;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.CharBuffer;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.DCTree.DCReference;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import com.sun.source.util.DocTrees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import java.lang.reflect.Method;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.nbjavac.services.NBAttr;
import org.netbeans.lib.nbjavac.services.NBResolve;
import org.netbeans.modules.java.source.TreeUtilitiesAccessor;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.matching.CopyFinder.HackScope;
import org.netbeans.modules.java.source.pretty.ImportAnalysis2;
import org.netbeans.modules.java.source.transform.ImmutableDocTreeTranslator;
import org.netbeans.modules.java.source.transform.ImmutableTreeTranslator;

/**
 *
 * @author Jan Lahoda, Dusan Balek, Tomas Zezula
 */
public final class TreeUtilities {
    
    private static final Logger LOG = Logger.getLogger(TreeUtilities.class.getName());

    /**{@link Kind}s that are represented by {@link ClassTree}.
     * 
     * @since 0.67
     */
    public static final Set<Kind> CLASS_TREE_KINDS = EnumSet.of(Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.RECORD);
    private final CompilationInfo info;
    private final CommentHandlerService handler;
    
    /** Creates a new instance of CommentUtilities */
    TreeUtilities(final CompilationInfo info) {
        assert info != null;
        this.info = info;
        this.handler = CommentHandlerService.instance(info.impl.getJavacTask().getContext());
    }
    
    /**Checks whether the given tree represents a class.
     * @deprecated since 0.67, <code>Tree.getKind() == Kind.CLASS</code> should be used instead.
     */
    @Deprecated
    public boolean isClass(ClassTree tree) {
        return (((JCTree.JCModifiers)tree.getModifiers()).flags & (Flags.INTERFACE | Flags.ENUM | Flags.ANNOTATION)) == 0;
    }
    
    /**Checks whether the given tree represents an interface.
     * @deprecated since 0.67, <code>Tree.getKind() == Kind.INTERFACE</code> should be used instead.
     */
    @Deprecated
    public boolean isInterface(ClassTree tree) {
        final long flags = ((JCTree.JCModifiers) tree.getModifiers()).flags;
        return (flags & Flags.INTERFACE) != 0 && (flags & Flags.ANNOTATION) == 0;
    }
    
    /**Checks whether the given tree represents an enum.
     * @deprecated since 0.67, <code>Tree.getKind() == Kind.ENUM</code> should be used instead.
     */
    @Deprecated
    public boolean isEnum(ClassTree tree) {
        return (((JCTree.JCModifiers)tree.getModifiers()).flags & Flags.ENUM) != 0;
    }

    /**
     * Checks whether given variable tree represents an enum constant.
     */
    public boolean isEnumConstant(VariableTree tree) {
        return (((JCTree.JCModifiers) tree.getModifiers()).flags & Flags.ENUM) != 0;
    }
    
    /**Checks whether the given tree represents an annotation.
     * @deprecated since 0.67, <code>Tree.getKind() == Kind.ANNOTATION_TYPE</code> should be used instead.
     */
    @Deprecated
    public boolean isAnnotation(ClassTree tree) {
        return (((JCTree.JCModifiers)tree.getModifiers()).flags & Flags.ANNOTATION) != 0;
    }
    
    /**
     * Checks whether given compilation unit represents a package-info.
     * @since 2.23
    */
    public boolean isPackageInfo(CompilationUnitTree tree) {
        return TreeInfo.isPackageInfo((JCTree.JCCompilationUnit)tree);
    }
    
    /**
     * Checks whether given compilation unit represents a module-info.
     * @since 2.23
     */
    public boolean isModuleInfo(CompilationUnitTree tree) {
        return TreeInfo.isModuleInfo((JCTree.JCCompilationUnit)tree);
    }
    
    /**
     * Checks whether given expression represents an expression statement.
     * @since 2.56
     */
    public boolean isExpressionStatement(ExpressionTree tree) {
        return TreeInfo.isExpressionStatement((JCTree.JCExpression)tree);
    }
    
    /**Returns whether or not the given tree is synthetic - generated by the parser.
     * Please note that this method does not check trees transitively - a child of a synthetic tree
     * may be considered non-synthetic.
     * 
     * @return true if the given tree is synthetic, false otherwise
     * @throws NullPointerException if the given tree is null
     */
    public boolean isSynthetic(TreePath path) throws NullPointerException {
        if (path == null)
            throw new NullPointerException();
        
        while (path != null) {
            if (isSynthetic(path.getCompilationUnit(), path.getLeaf()))
                return true;
            if (path.getParentPath() != null && path.getParentPath().getParentPath() != null) {
                TreePath grandpa = path.getParentPath().getParentPath();
                if (grandpa.getLeaf().getKind() == Kind.NEW_CLASS) {
                    NewClassTree nct = (NewClassTree) grandpa.getLeaf();
                    ClassTree body = nct.getClassBody();
                    if (body != null &&
                        (body.getExtendsClause() == path.getLeaf() ||
                         body.getImplementsClause().contains(path.getLeaf()))) {
                        return true;
                    }
                } else if (grandpa.getLeaf().getKind() == Kind.RECORD &&
                           path.getLeaf().getKind() == Kind.VARIABLE &&
                           path.getParentPath().getLeaf().getKind() == Kind.METHOD) {
                    JCMethodDecl m = (JCMethodDecl) path.getParentPath().getLeaf();
                    if ((m.mods.flags & Flags.COMPACT_RECORD_CONSTRUCTOR) != 0 && m.getParameters().contains(path.getLeaf())) {
                        return true;
                    }
                }
            }
            if (path.getParentPath() != null) {
                Tree parentLeaf = path.getParentPath().getLeaf();
                if (parentLeaf.getKind() == Kind.VARIABLE) {
                    JCVariableDecl var = (JCVariableDecl) parentLeaf;
                    if (var.declaredUsingVar() && var.vartype == path.getLeaf()) {
                        return true;
                    }
                    if (path.getParentPath().getParentPath() != null) {
                        Tree parentParentLeaf = path.getParentPath().getParentPath().getLeaf();

                        if (parentParentLeaf.getKind() == Kind.LAMBDA_EXPRESSION) {
                            JCLambda let = (JCLambda) parentParentLeaf;

                            if (let.paramKind == JCLambda.ParameterKind.IMPLICIT && let.getParameters().contains(parentLeaf)) {
                                return true;
                            }
                        }
                    }
                } else if (parentLeaf.getKind() == Kind.LAMBDA_EXPRESSION
                        && (path.getLeaf().getKind() == Kind.MEMBER_SELECT || path.getLeaf().getKind() == Kind.PRIMITIVE_TYPE)) {
                    JCLambda let = (JCLambda) parentLeaf;
                    if (let.paramKind == JCLambda.ParameterKind.IMPLICIT) {
                        return true;
                    }
                }
            }
            path = path.getParentPath();
        }
        
        return false;
    }
    
    boolean isSynthetic(CompilationUnitTree cut, Tree leaf) throws NullPointerException {
        JCTree tree = (JCTree) leaf;
        
        if (tree.pos == (-1))
            return true;
        
        if (leaf.getKind() == Kind.METHOD) {
            //check for synthetic constructor:
            return (((JCMethodDecl)leaf).mods.flags & Flags.GENERATEDCONSTR) != 0L;
        }
        
        //check for synthetic superconstructor call:
        if (leaf.getKind() == Kind.EXPRESSION_STATEMENT) {
            ExpressionStatementTree est = (ExpressionStatementTree) leaf;
            
            if (est.getExpression().getKind() == Kind.METHOD_INVOCATION) {
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                
                if (mit.getMethodSelect().getKind() == Kind.IDENTIFIER) {
                    IdentifierTree it = (IdentifierTree) mit.getMethodSelect();
                    
                    if ("super".equals(it.getName().toString())) {
                        SourcePositions sp = info.getTrees().getSourcePositions();
                        
                        return sp.getEndPosition(cut, leaf) == (-1);
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Returns path to the deepest tree of the given kind containing the given
     * starting tree.
     * @param kind requested tree kind
     * @param path path to the starting tree
     * @return requested path or null if no tree of the given kind encloses
     * the given starting tree
     *
     * @since 0.136
     */
    public TreePath getPathElementOfKind(Tree.Kind kind, TreePath path) {
        return getPathElementOfKind(EnumSet.of(kind), path);
    }
    
    /**
     * Returns path to the deepest tree of one of the given kinds containing
     * the given starting tree.
     * @param kinds requested tree kinds
     * @param path path to the starting tree
     * @return requested path or null if no tree of the given kinds encloses
     * the given starting tree
     *
     * @since 0.136
     */
    public TreePath getPathElementOfKind(Set<Tree.Kind> kinds, TreePath path) {
        while (path != null) {
            if (kinds.contains(path.getLeaf().getKind())) {
                return path;
            }
            path = path.getParentPath();
        }
        return null;        
    }        
    
    /**Returns list of comments attached to a given tree. Can return either
     * preceding or trailing comments.
     *
     * @param tree for which comments should be returned
     * @param preceding true if preceding comments should be returned, false if trailing comments should be returned.
     * @return list of preceding/trailing comments attached to the given tree
     */
    public List<Comment> getComments(Tree tree, boolean preceding) {
        CommentSetImpl set = handler.getComments(tree);

        ensureCommentsMapped(info, tree, set);

        List<Comment> comments = preceding ? set.getPrecedingComments() : set.getTrailingComments();
        
        return Collections.unmodifiableList(comments);
    }

    static void ensureCommentsMapped(CompilationInfo info, @NullAllowed Tree tree, CommentSetImpl set) {
        if (!set.areCommentsMapped() && tree != null) {
            boolean assertsEnabled = false;
            boolean automap = true;

            assert assertsEnabled = true;

            TreePath tp = info.getCompilationUnit() == tree ? new TreePath(info.getCompilationUnit()) : TreePath.getPath(info.getCompilationUnit(), tree);

            if (tp == null) {
                if (assertsEnabled && !info.getTreeUtilities().isSynthetic(info.getCompilationUnit(), tree)) {
                    // HACK: if info is a working copy, the tree might be introduced by rewriting; 
                    // in that case, no log should be printed
                    if (!(info instanceof WorkingCopy) || !((WorkingCopy)info).validateIsReplacement(tree)) {
                        Logger.getLogger(TreeUtilities.class.getName()).log(assertsEnabled ? Level.WARNING : Level.FINE, "Comment automap requested for Tree not from the root compilation info. Please, make sure to call GeneratorUtilities.importComments before Treeutilities.getComments. Tree: {0}", tree);
                        Logger.getLogger(TreeUtilities.class.getName()).log(assertsEnabled ? Level.INFO : Level.FINE, "Caller", new Exception());
                    }
                }
                automap = false;
            }

            if (automap) {
                GeneratorUtilities.importComments(info, tree, info.getCompilationUnit());
            }
        }
    }

    public TreePath pathFor(int pos) {
        return pathFor(new TreePath(info.getCompilationUnit()), pos);
    }

    public TreePath pathFor(TreePath path, int pos) {
        return pathFor(path, pos, info.getTrees().getSourcePositions());
    }

    public TreePath pathFor(TreePath path, int pos, SourcePositions sourcePositions) {
        if (info == null || path == null || sourcePositions == null)
            throw new IllegalArgumentException();
        
        class Result extends Error {
            TreePath path;
            Result(TreePath path) {
                this.path = path;
            }
        }
        
        class PathFinder extends ErrorAwareTreePathScanner<Void,Void> {
            private int pos;
            private SourcePositions sourcePositions;
            
            private PathFinder(int pos, SourcePositions sourcePositions) {
                this.pos = pos;
                this.sourcePositions = sourcePositions;
            }
            
            public Void scan(Tree tree, Void p) {
                if (tree != null) {
                    long startPos = sourcePositions.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
                    long endPos = sourcePositions.getEndPosition(getCurrentPath().getCompilationUnit(), tree);
                    if (endPos == (-1)) {
                        switch (tree.getKind()) {
                            case ASSIGNMENT:
                                if (getCurrentPath().getLeaf().getKind() == Kind.ANNOTATION) {
                                    ExpressionTree value = ((AssignmentTree) tree).getExpression();
                                    startPos = sourcePositions.getStartPosition(getCurrentPath().getCompilationUnit(), value);
                                    endPos = sourcePositions.getEndPosition(getCurrentPath().getCompilationUnit(), value);
                                }
                                break;
                            case CLASS:
                                ClassTree clazz = (ClassTree) tree;

                                if (!clazz.getMembers().isEmpty()) {
                                    endPos = sourcePositions.getEndPosition(getCurrentPath().getCompilationUnit(), clazz.getMembers().get(clazz.getMembers().size() - 1));
                                }
                                break;
                        }
                    }
                    if (startPos < pos && endPos >= pos) {
                        if (tree.getKind() == Tree.Kind.ERRONEOUS) {
                            tree.accept(this, p);
                            throw new Result(getCurrentPath());
                        }
                        super.scan(tree, p);
                        throw new Result(new TreePath(getCurrentPath(), tree));
                    }
                }
                return null;
            }

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                int[] span = findNameSpan(node);
                
                if (span != null && span[0] <= pos && pos < span[1]) {
                    throw new Result(getCurrentPath());
                }
                
                return super.visitVariable(node, p);
            }

            @Override
            public Void visitMethod(MethodTree node, Void p) {
                int[] span = findNameSpan(node);
                
                if (span != null && span[0] <= pos && pos < span[1]) {
                    throw new Result(getCurrentPath());
                }
                
                return super.visitMethod(node, p);
            }

            @Override
            public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
                int exprEndPos = (int) sourcePositions.getEndPosition(getCurrentPath().getCompilationUnit(), node.getExpression());
                if (exprEndPos < pos) {
                    TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language()).subSequence(exprEndPos, pos);
                    boolean hasNonWhiteSpace;
                    while (hasNonWhiteSpace = ts.moveNext()) {
                        if (!IGNORE_TOKENS.contains(ts.token().id()))
                            break;
                    }
                    if (!hasNonWhiteSpace) {
                        pos = exprEndPos;
                    }
                }
                return super.visitEnhancedForLoop(node, p);
            }

        }
        
        try {
            new PathFinder(pos, sourcePositions).scan(path, null);
        } catch (Result result) {
            path = result.path;
        }
        
        if (path.getLeaf() == path.getCompilationUnit())
            return path;
        
        TokenSequence<JavaTokenId> tokenList = tokensFor(path.getLeaf(), sourcePositions, pos);
        tokenList.moveEnd();
        if (tokenList.movePrevious() && tokenList.offset() < pos) {
            switch (tokenList.token().id()) {
                case GTGTGT:
                case GTGT:
                case GT:
                    if (path.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT || TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind()) ||
                            path.getLeaf().getKind() == Tree.Kind.MEMBER_REFERENCE || path.getLeaf().getKind() == Tree.Kind.GREATER_THAN)
                        break;
                case RPAREN:
                    if (path.getLeaf().getKind() == Tree.Kind.PARENTHESIZED) {
                        path = path.getParentPath();
                        break;
                    } else if (path.getLeaf().getKind() == Tree.Kind.ENHANCED_FOR_LOOP || path.getLeaf().getKind() == Tree.Kind.FOR_LOOP ||
                            path.getLeaf().getKind() == Tree.Kind.IF || path.getLeaf().getKind() == Tree.Kind.WHILE_LOOP ||
                            path.getLeaf().getKind() == Tree.Kind.DO_WHILE_LOOP || path.getLeaf().getKind() == Tree.Kind.TYPE_CAST ||
                            path.getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION)
                        break;
                case SEMICOLON:
                    if (path.getLeaf().getKind() == Tree.Kind.EMPTY_STATEMENT ||
                            path.getLeaf().getKind() == Tree.Kind.TRY ||
                            (path.getLeaf().getKind() == Tree.Kind.FOR_LOOP &&
                            tokenList.offset() <= sourcePositions.getStartPosition(path.getCompilationUnit(), ((ForLoopTree)path.getLeaf()).getUpdate().get(0))))
                        break;
                    if (path.getParentPath().getLeaf().getKind() == Tree.Kind.TRY &&
                        ((TryTree) path.getParentPath().getLeaf()).getResources().contains(path.getLeaf())) {
                        path = path.getParentPath();
                        break;
                    }
                case RBRACE:
                    path = path.getParentPath();
                    switch (path.getLeaf().getKind()) {
                        case CATCH:
                            path = path.getParentPath();
                        case METHOD:
                        case FOR_LOOP:
                        case ENHANCED_FOR_LOOP:
                        case SYNCHRONIZED:
                        case WHILE_LOOP:
                        case TRY:
                            path = path.getParentPath();
                            break;
                        case IF:
                            do {
                                path = path.getParentPath();
                            } while (path != null && path.getLeaf().getKind() == Tree.Kind.IF);
                            break;
                    }
                    break;
            }
        }
        return path;
    }
    
    private static final Set<JavaTokenId> IGNORE_TOKENS = EnumSet.of(
            JavaTokenId.BLOCK_COMMENT, JavaTokenId.JAVADOC_COMMENT,
            JavaTokenId.LINE_COMMENT, JavaTokenId.WHITESPACE
    );

    /**Return the deepest DocTreePath at the given position.
     * 
     * @param treepath for which the {@code doc} comment was determined
     * @param doc the documentation comment inside which the search should be performed
     * @param pos the position to search for
     * @return the deepest DocTreePath at the given position
     * @since 0.124
     */
    public DocTreePath pathFor(TreePath treepath, DocCommentTree doc, int pos) {
        return pathFor(new DocTreePath(treepath, doc), pos);
    }

    /**Return the deepest DocTreePath at the given position.
     * 
     * @param path where the search should start
     * @param pos the position to search for
     * @return the deepest DocTreePath at the given position
     * @since 0.124
     */
    public DocTreePath pathFor(DocTreePath path, int pos) {
        return pathFor(path, pos, info.getDocTrees().getSourcePositions());
    }
    
    /**Return the deepest DocTreePath at the given position.
     * 
     * @param path where the search should start
     * @param pos the position to search for
     * @param sourcePositions to determine spans of {@link DocTree}s
     * @return the deepest DocTreePath at the given position
     * @since 0.124
     */
    public DocTreePath pathFor(DocTreePath path, int pos, DocSourcePositions sourcePositions) {
        if (info == null || path == null || sourcePositions == null)
            throw new IllegalArgumentException();
        
        class Result extends Error {
            DocTreePath path;
            Result(DocTreePath path) {
                this.path = path;
            }
        }
        
        class PathFinder extends DocTreePathScanner<Void,TreePath> {
            private int pos;
            private DocSourcePositions sourcePositions;
            
            private PathFinder(int pos, DocSourcePositions sourcePositions) {
                this.pos = pos;
                this.sourcePositions = sourcePositions;
            }
            
            public Void scan(DocTree tree, TreePath p) {
                if (tree != null) {
                    if (sourcePositions.getStartPosition(p.getCompilationUnit(), getCurrentPath().getDocComment(), tree) < pos && sourcePositions.getEndPosition(p.getCompilationUnit(), getCurrentPath().getDocComment(), tree) >= pos) {
                        if (tree.getKind() == DocTree.Kind.ERRONEOUS) {
                            tree.accept(this, p);
                            throw new Result(getCurrentPath());
                        }
                        super.scan(tree, p);
                        throw new Result(new DocTreePath(getCurrentPath(), tree));
                    }
                }
                return null;
            }

//            @Override
//            public Void visitVariable(VariableTree node, Void p) {
//                int[] span = findNameSpan(node);
//                
//                if (span != null && span[0] <= pos && pos < span[1]) {
//                    throw new Result(getCurrentPath());
//                }
//                
//                return super.visitVariable(node, p);
//            }
//
//            @Override
//            public Void visitMethod(MethodTree node, Void p) {
//                int[] span = findNameSpan(node);
//                
//                if (span != null && span[0] <= pos && pos < span[1]) {
//                    throw new Result(getCurrentPath());
//                }
//                
//                return super.visitMethod(node, p);
//            }
        }
        
        try {
            new PathFinder(pos, sourcePositions).scan(path, path.getTreePath());
        } catch (Result result) {
            path = result.path;
        }
        
        if (path.getLeaf() == path.getDocComment())
            return path;
        
        return path;
    }
    
    /**Parses given type in given context.
     * 
     * @param expr type specification
     * @param scope in which simple names should be resolved
     * @return parsed {@link TypeMirror} or null if the given specification cannot be parsed
     */
    public TypeMirror parseType(String expr, TypeElement scope) {
        Enter enter = Enter.instance(info.impl.getJavacTask().getContext());
        com.sun.tools.javac.tree.TreeMaker jcMaker = com.sun.tools.javac.tree.TreeMaker.instance(info.impl.getJavacTask().getContext());
        int oldPos = jcMaker.pos;        
        try {
            if (enter.getClassEnv((Symbol.TypeSymbol)scope) == null) {
                if (info.getTrees().getTree(scope) == null)
                    return null;
            }
            return info.impl.getJavacTask().parseType(expr, scope);
        } finally {
            jcMaker.pos = oldPos;
        }
    }

    /**Parses given type in given context.
     *
     * @param expr type specification
     * @param scope in which simple names should be resolved
     * @return parsed {@link TypeMirror} or null if the given specification cannot be parsed
     */
    Tree parseType(String expr) {
        return doParse(expr, null, 0, Parser::parseType);
    }
    
    /**Parses given statement.
     * 
     * @param stmt statement code
     * @param sourcePositions return value - new SourcePositions for the new tree
     * @return parsed {@link StatementTree} or null?
     */
    public StatementTree parseStatement(String stmt, SourcePositions[] sourcePositions) {
        return parseStatementImpl(info.impl.getJavacTask(), stmt, sourcePositions);
    }

    private static StatementTree parseStatementImpl(JavacTaskImpl task, String stmt, SourcePositions[] sourcePositions) {
        return doParse(task, stmt, sourcePositions, 0, Parser::parseStatement);
    }

    /**Parses given expression.
     * 
     * @param expr expression code
     * @param sourcePositions return value - new SourcePositions for the new tree
     * @return parsed {@link ExpressionTree} or null?
     */
    public ExpressionTree parseExpression(String expr, SourcePositions[] sourcePositions) {
        return doParse(expr, sourcePositions, 0, Parser::parseExpression);
    }
    
    /**Parses given variable initializer.
     * 
     * @param init initializer code
     * @param sourcePositions return value - new SourcePositions for the new tree
     * @return parsed {@link ExpressionTree} or null?
     */
    public ExpressionTree parseVariableInitializer(String init, SourcePositions[] sourcePositions) {
        return doParse(init, sourcePositions, 0, p -> ((JavacParser) p).variableInitializer());
    }

    /**Parses given static block.
     * 
     * @param block block code
     * @param sourcePositions return value - new SourcePositions for the new tree
     * @return parsed {@link BlockTree} or null?
     */
    public BlockTree parseStaticBlock(String block, SourcePositions[] sourcePositions) {
        return doParse("{ class C { " + block + " }", sourcePositions, "{ class C { ".length(), p -> {
            JCClassDecl decl = (JCClassDecl) ((BlockTree) p.parseStatement()).getStatements().get(0);
            return decl.defs.head.getKind() == Kind.BLOCK ? (BlockTree) decl.defs.head : null; //check result
        });
    }

    /**Parses given statement.
     * 
     * @param text code
     * @param sourcePositions return value - new SourcePositions for the new tree
     * @return parsed {@link T} or null?
     */
    private <T extends Tree> T doParse(String text, SourcePositions[] sourcePositions, int offset, Function<Parser, T> actualParse) {
        return doParse(info.impl.getJavacTask(), text, sourcePositions, offset, actualParse);
    }

    private static <T extends Tree> T doParse(JavacTaskImpl task, String text, SourcePositions[] sourcePositions, int offset, Function<Parser, T> actualParse) {
        if (text == null || (sourcePositions != null && sourcePositions.length != 1))
            throw new IllegalArgumentException();
        //use a working init order:
        com.sun.tools.javac.main.JavaCompiler.instance(task.getContext());
        com.sun.tools.javac.tree.TreeMaker jcMaker = com.sun.tools.javac.tree.TreeMaker.instance(task.getContext());
        int oldPos = jcMaker.pos;
        
        try {
            //from org/netbeans/modules/java/hints/spiimpl/Utilities.java:
            Context context = task.getContext();
            JavaCompiler compiler = JavaCompiler.instance(context);
            JavaFileObject prev = compiler.log.useSource(new DummyJFO());
            Log.DiagnosticHandler discardHandler = compiler.log.new DiscardDiagnosticHandler();
            try {
                CharBuffer buf = CharBuffer.wrap((text+"\u0000").toCharArray(), 0, text.length());
                ParserFactory factory = ParserFactory.instance(context);
                Parser parser = factory.newParser(buf, false, true, false, false);
                if (parser instanceof JavacParser) {
                    if (sourcePositions != null)
                        sourcePositions[0] = new ParserSourcePositions((JavacParser)parser, offset);
                    return actualParse.apply(parser);
                }
                return null;
            } finally {
                compiler.log.useSource(prev);
                compiler.log.popDiagnosticHandler(discardHandler);
            }
        } finally {
            jcMaker.pos = oldPos;
        }
    }
    
    //from org/netbeans/modules/java/hints/spiimpl/Utilities.java:
    private static final class DummyJFO extends SimpleJavaFileObject {
        private DummyJFO() {
            super(URI.create("dummy.java"), JavaFileObject.Kind.SOURCE);
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return "";
        }
    }

    //from org/netbeans/modules/java/hints/spiimpl/Utilities.java:
    private static class ParserSourcePositions implements SourcePositions {

        private final JavacParser parser;
        private final int offset;

        private ParserSourcePositions(JavacParser parser, int offset) {
            this.parser = parser;
            this.offset = offset;
        }

        public long getStartPosition(CompilationUnitTree file, Tree tree) {
            return parser.getStartPos((JCTree)tree) - offset;
        }

        public long getEndPosition(CompilationUnitTree file, Tree tree) {
            return parser.getEndPos((JCTree)tree) - offset;
        }
    }

    //XXX: parseAnnotationValue
    
    /**Computes {@link Scope} for the given position.
     */
    public Scope scopeFor(int pos) {
        List<? extends StatementTree> stmts = null;
        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
        TreePath path = pathFor(pos);
        CompilationUnitTree root = path.getCompilationUnit();
        switch (path.getLeaf().getKind()) {
            case BLOCK:
                stmts = ((BlockTree)path.getLeaf()).getStatements();
                break;
            case FOR_LOOP:
                stmts = ((ForLoopTree)path.getLeaf()).getInitializer();
                break;
            case ENHANCED_FOR_LOOP:
                stmts = Collections.singletonList(((EnhancedForLoopTree)path.getLeaf()).getStatement());
                break;
            case CASE:
                stmts = ((CaseTree)path.getLeaf()).getStatements();
                break;
            case METHOD:
                stmts = ((MethodTree)path.getLeaf()).getParameters();
                break;
        }
        if (stmts != null) {
            Tree tree = null;
            for (StatementTree st : stmts) {
                if (sourcePositions.getStartPosition(root, st) < pos)
                    tree = st;
            }
            if (tree != null)
                path = new TreePath(path, tree);
        }
        Scope scope = info.getTrees().getScope(path);
        if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind())) {
            TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
            ts.move(pos);
            while(ts.movePrevious()) {
                switch (ts.token().id()) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case JAVADOC_COMMENT:
                        break;
                    case EXTENDS:
                    case IMPLEMENTS:
                        ((JavacScope)scope).getEnv().baseClause = true;
                    default:
                        return scope;
                }
            }
        }
        return scope;
    }
    
    /**
     * Creates {@link Scope} capable to access all private methods and fields when
     * parsing and evaluating expressions. When using this Scope, the accessibility
     * checks would not be enforced during a tree attribution.
     * @param scope an existing scope
     * @return scope with disabled accessibility checks
     * @since 0.129
     */
    public Scope toScopeWithDisabledAccessibilityChecks(Scope scope) {
        return new NBScope((JavacScope)scope);
    }

    private static Env<AttrContext> getEnv(Scope scope) {
        if (scope instanceof NBScope) {
            scope = ((NBScope) scope).delegate;
        }
        
        if (scope instanceof HackScope) {
            return ((HackScope) scope).getEnv();
        }

        return ((JavacScope) scope).getEnv();
    }
    
    /**Attribute the given tree in the given context.
     */
    public TypeMirror attributeTree(Tree tree, Scope scope) {
        Env<AttrContext> env = getEnv(scope);
        if (scope instanceof NBScope && ((NBScope)scope).areAccessibilityChecksDisabled()) {
            NBResolve.instance(info.impl.getJavacTask().getContext()).disableAccessibilityChecks();
        }
        try {
            return attributeTree(info.impl.getJavacTask(), env.toplevel, (JCTree) tree, scope, new ArrayList<>());
        } finally {
            NBResolve.instance(info.impl.getJavacTask().getContext()).restoreAccessbilityChecks();
        }
    }
    
    /**Attribute the given tree until the given <code>to</code> tree is reached.
     * Returns scope valid at point when <code>to</code> is reached.
     */
    public Scope attributeTreeTo(Tree tree, Scope scope, Tree to) {
        Env<AttrContext> env = getEnv(scope);
        if (scope instanceof NBScope && ((NBScope)scope).areAccessibilityChecksDisabled()) {
            NBResolve.instance(info.impl.getJavacTask().getContext()).disableAccessibilityChecks();
        }
        try {
            return attributeTreeTo(info.impl.getJavacTask(), env.toplevel, (JCTree)tree, scope, (JCTree)to, new ArrayList<>());
        } finally {
            NBResolve.instance(info.impl.getJavacTask().getContext()).restoreAccessbilityChecks();
        }
    }
    
    public TypeMirror reattributeTree(Tree tree, Scope scope) {
        Env<AttrContext> env = getEnv(scope);
        if (scope instanceof NBScope && ((NBScope)scope).areAccessibilityChecksDisabled()) {
            NBResolve.instance(info.impl.getJavacTask().getContext()).disableAccessibilityChecks();
        }
        try {
            return attributeTree(info.impl.getJavacTask(), env.toplevel, (JCTree)tree, scope, new ArrayList<>());
        } finally {
            NBResolve.instance(info.impl.getJavacTask().getContext()).restoreAccessbilityChecks();
        }
    }
    
    public Scope reattributeTreeTo(Tree tree, Scope scope, Tree to) {
        Env<AttrContext> env = getEnv(scope);
        if (scope instanceof NBScope && ((NBScope)scope).areAccessibilityChecksDisabled()) {
            NBResolve.instance(info.impl.getJavacTask().getContext()).disableAccessibilityChecks();
        }
        try {
            return attributeTreeTo(info.impl.getJavacTask(), env.toplevel, (JCTree)tree, scope, (JCTree)to, new ArrayList<>());
        } finally {
            NBResolve.instance(info.impl.getJavacTask().getContext()).restoreAccessbilityChecks();
        }
    }
    
    //from org/netbeans/modules/java/hints/spiimpl/Utilities.java:
    private static TypeMirror attributeTree(JavacTaskImpl jti, CompilationUnitTree cut, Tree tree, Scope scope, final List<Diagnostic<? extends JavaFileObject>> errors) {
        Log log = Log.instance(jti.getContext());
        JavaFileObject prev = log.useSource(new DummyJFO());
        Log.DiagnosticHandler discardHandler = log.new DiscardDiagnosticHandler() {
            @Override
            public void report(JCDiagnostic diag) {
                errors.add(diag);
            }            
        };
        NBResolve resolve = NBResolve.instance(jti.getContext());
        resolve.disableAccessibilityChecks();
//        Enter enter = Enter.instance(jti.getContext());
//        enter.shadowTypeEnvs(true);
//        ArgumentAttr argumentAttr = ArgumentAttr.instance(jti.getContext());
//        ArgumentAttr.LocalCacheContext cacheContext = argumentAttr.withLocalCacheContext();
        try {
            Attr attr = Attr.instance(jti.getContext());
            Env<AttrContext> env = getEnv(scope);
            if (tree instanceof JCExpression)
                return attr.attribExpr((JCTree) tree,env, Type.noType);
            return attr.attribStat((JCTree) tree,env);
        } finally {
            unenter(jti.getContext(), (JCCompilationUnit) cut, (JCTree) tree);
//            cacheContext.leave();
            log.useSource(prev);
            log.popDiagnosticHandler(discardHandler);
            resolve.restoreAccessbilityChecks();
//            enter.shadowTypeEnvs(false);
        }
    }

    private static void unenter(Context ctx, JCCompilationUnit cut, JCTree tree) {
        try {
            Method m = Enter.class.getDeclaredMethod("unenter", JCCompilationUnit.class, JCTree.class);
            m.invoke(Enter.instance(ctx), cut, tree);
        } catch (Throwable t) {
            LOG.log(Level.FINE, null, t);
        }
    }

    private static Scope attributeTreeTo(JavacTaskImpl jti, CompilationUnitTree cut, Tree tree, Scope scope, Tree to, final List<Diagnostic<? extends JavaFileObject>> errors) {
        Log log = Log.instance(jti.getContext());
        JavaFileObject prev = log.useSource(new DummyJFO());
        Log.DiagnosticHandler discardHandler = log.new DiscardDiagnosticHandler() {
            @Override
            public void report(JCDiagnostic diag) {
                errors.add(diag);
            }            
        };
        NBResolve resolve = NBResolve.instance(jti.getContext());
        resolve.disableAccessibilityChecks();
//        Enter enter = Enter.instance(jti.getContext());
//        enter.shadowTypeEnvs(true);
//        ArgumentAttr argumentAttr = ArgumentAttr.instance(jti.getContext());
//        ArgumentAttr.LocalCacheContext cacheContext = argumentAttr.withLocalCacheContext();
        try {
            NBAttr attr = (NBAttr) NBAttr.instance(jti.getContext());
            Env<AttrContext> env = getEnv(scope);
            Env<AttrContext> result = attr.attributeAndCapture((JCTree) tree, env, (JCTree) to);
            try {
                Constructor<JavacScope> c = JavacScope.class.getDeclaredConstructor(Env.class);
                c.setAccessible(true);
                return c.newInstance(result);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new IllegalStateException(ex);
            }
        } finally {
            unenter(jti.getContext(), (JCCompilationUnit) cut, (JCTree) tree);
//            cacheContext.leave();
            log.useSource(prev);
            log.popDiagnosticHandler(discardHandler);
            resolve.restoreAccessbilityChecks();
//            enter.shadowTypeEnvs(false);
        }
    }

    /**Returns tokens for a given tree.
     */
    public TokenSequence<JavaTokenId> tokensFor(Tree tree) {
        return tokensFor(tree, info.getTrees().getSourcePositions());
    }
    
    /**Returns tokens for a given tree. Uses specified {@link SourcePositions}.
     */
    public TokenSequence<JavaTokenId> tokensFor(Tree tree, SourcePositions sourcePositions) {
        return tokensFor(tree, sourcePositions, -1);
    }

    private TokenSequence<JavaTokenId> tokensFor(Tree tree, SourcePositions sourcePositions, int farEnd) {
        int start = (int)sourcePositions.getStartPosition(info.getCompilationUnit(), tree);
        int end   = (int)sourcePositions.getEndPosition(info.getCompilationUnit(), tree);
        
        return info.getTokenHierarchy().tokenSequence(JavaTokenId.language()).subSequence(start, Math.max(end, farEnd));
    }
    
    /**
     * Checks whether the given element is accessible as a member of the given
     * type in a given scope.
     * @param scope the scope to be checked
     * @param member the member to be checked
     * @param type the type for which to check if the member is accessible
     * @return true if {@code member} is accessible in {@code type}
     * @deprecated since 0.111, {@link Trees#isAccessible(Scope, Element, DeclaredType)} should be used instead.
     */
    @Deprecated
    public boolean isAccessible(Scope scope, Element member, TypeMirror type) {
        return type instanceof DeclaredType ? info.getTrees().isAccessible(scope, member, (DeclaredType)type) : false;
    }
    
    /**Checks whether the given scope is in "static" context.
     */
    public boolean isStaticContext(Scope scope) {
        return NBResolve.isStatic(getEnv(scope));
    }
    
    /**Returns uncaught exceptions inside the given tree path.
     */
    public Set<TypeMirror> getUncaughtExceptions(TreePath path) {
        Set<TypeMirror> set = new UnrelatedTypeMirrorSet(info.getTypes());
        new UncaughtExceptionsVisitor(info).scan(path, set);
        return set;
    }
    
    /**
     * Returns all uninitialized fields of the given class.
     * @param path to class to inspect
     * @return set of all uninitialized fields
     *
     * @since 0.136
     */
    public Set<? extends VariableElement> getUninitializedFields(TreePath path) {
        final Set<VariableElement> fields = new LinkedHashSet<>();
        if (path == null) {
            return fields;
        }
        final Trees trees = info.getTrees();
        Element element = trees.getElement(path);
        if (element == null || !element.getKind().isClass()) {
            return fields;
        }
        for (VariableElement ve : ElementFilter.fieldsIn(((TypeElement)element).getEnclosedElements())) {
            if (ve instanceof Symbol && (((Symbol)ve).flags() & (Flags.HASINIT | Flags.STATIC)) == 0) {
                fields.add(ve);
            }
        }
        new ErrorAwareTreePathScanner<Void, Boolean>() {
            @Override
            public Void visitAssignment(AssignmentTree node, Boolean p) {
                Element el = trees.getElement(new TreePath(getCurrentPath(), node.getVariable()));
                fields.remove(el);
                return null;
            }
            @Override
            public Void visitClass(ClassTree node, Boolean p) {
                //do not analyse the inner classes:
                return p ? super.visitClass(node, Boolean.FALSE) : null;
            }
            @Override
            public Void visitMethod(MethodTree node, Boolean p) {
                return null;
            }
        }.scan(path, Boolean.TRUE);
        return fields;
    }

    /**Find span of the {@link ClassTree}'s body in the source.
     * Returns starting and ending offset of the body in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param clazz class which body should be searched for
     * @return the span of the body, or null if cannot be found
     * @since 0.127
     */
    public int[] findBodySpan(ClassTree clazz) {
        JCTree jcTree = (JCTree) clazz;
        int pos = jcTree.pos;
        
        if (pos < 0)
            return null;
        
        TokenSequence<JavaTokenId> tokenSequence = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        tokenSequence.move(pos);
        
        int startPos = -1;
        int endPos = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), clazz);
        while(tokenSequence.moveNext()) {
            if(tokenSequence.token().id() == JavaTokenId.LBRACE) {
                startPos = tokenSequence.offset();
                break;
            }
        }
        
        if(startPos == -1 || endPos == -1) {
            return null;
        }
        
        return new int[] {
            startPos,
            endPos
        };
    }
    
    /**Find span of the {@link ClassTree#getSimpleName()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param clazz class which name should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.25
     */
    public int[] findNameSpan(ClassTree clazz) {
        return findNameSpan(clazz.getSimpleName().toString(), clazz, JavaTokenId.CLASS, JavaTokenId.INTERFACE, JavaTokenId.ENUM, JavaTokenId.AT, JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT);
    }
    
    /**Find span of the {@link MethodTree#getName()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param method method which name should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.25
     */
    public int[] findNameSpan(MethodTree method) {
        if (isSynthetic(info.getCompilationUnit(), method)) {
            return null;
        }
        JCMethodDecl jcm = (JCMethodDecl) method;
        String name;
        if (jcm.name == jcm.name.table.names.init) {
            TreePath path = info.getTrees().getPath(info.getCompilationUnit(), jcm);
            if (path == null) {
                return null;
            }
            Element em = info.getTrees().getElement(path);
            Element clazz;
            if (em == null || (clazz = em.getEnclosingElement()) == null || !clazz.getKind().isClass()) {
                return null;
            }
            
            name = clazz.getSimpleName().toString();
        } else {
            name = method.getName().toString();
        }
        return findNameSpan(name, method);
    }
    
    /**Find span of the {@link VariableTree#getName()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param var variable which name should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.25
     */
    public int[] findNameSpan(VariableTree var) {
        return findNameSpan(var.getName().toString(), var);
    }
    
    /**Find span of the {@link LabeledStatementTree#getLabel()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param lst labeled statement which name should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.131
     */
    public int[] findNameSpan(LabeledStatementTree lst) {
        return findNameSpan(lst.getLabel().toString(), lst);
    }
    
    /**Find span of the {@link TypeParameterTree#getName()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param tpt type parameter which name should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.131
     */
    public int[] findNameSpan(TypeParameterTree tpt) {
        return findNameSpan(tpt.getName().toString(), tpt);
    }
    
    /**Find span of the {@link LabeledStatementTree#getLabel()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param brk labeled statement which name should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.131
     */
    public int[] findNameSpan(BreakTree brk) {
        return findNameSpan(brk.getLabel().toString(), brk);
    }
    
    /**Find span of the {@link LabeledStatementTree#getLabel()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param cont labeled statement which name should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.131
     */
    public int[] findNameSpan(ContinueTree cont) {
        return findNameSpan(cont.getLabel().toString(), cont);
    }
    
    /**Find span of the {@link MethodTree#getParameters()} parameter list in the source.
     * Returns the position of the opening and closing parentheses of the parameter list
     * in the source code that was parsed (ie. {@link CompilationInfo#getText()}, which
     * may differ from the positions in the source document if it has been already altered.
     * 
     * @param method method which parameter list should be searched for
     * @return the span of the parameter list, or null if cannot be found
     * @since 0.81
     */
    public @CheckForNull int[] findMethodParameterSpan(@NonNull MethodTree method) {
        JCTree jcTree = (JCTree) method;
        int pos = jcTree.pos;
        
        if (pos < 0)
            return null;
        
        TokenSequence<JavaTokenId> tokenSequence = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        tokenSequence.move(pos);
        
        int startPos = -1;
        int endPos = -1;
        while(tokenSequence.moveNext()) {
            if(tokenSequence.token().id() == JavaTokenId.LPAREN) {
                startPos = tokenSequence.offset();
            }
            if(tokenSequence.token().id() == JavaTokenId.RPAREN) {
                endPos = tokenSequence.offset();
                break;
            }
            if(tokenSequence.token().id() == JavaTokenId.LBRACE) {
                return null;
            }
        }
        
        if(startPos == -1 || endPos == -1) {
            return null;
        }
        
        return new int[] {
            startPos,
            endPos
        };
    }
    
    /**Find span of the {@link MemberSelectTree#getIdentifier()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param mst member select which identifier should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.25
     */
    public int[] findNameSpan(MemberSelectTree mst) {
        return findNameSpan(mst.getIdentifier().toString(), mst, JavaTokenId.DOT, JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT);
    }
    
    /**Find span of the {@link MemberReferenceTree#getName()} identifier in the source.
     * Returns starting and ending offset of the name in the source code that was parsed
     * (ie. {@link CompilationInfo#getText()}, which may differ from the positions in the source
     * document if it has been already altered.
     * 
     * @param mst member reference for which the identifier should be searched for
     * @return the span of the name, or null if cannot be found
     * @since 0.124
     */
    public int[] findNameSpan(MemberReferenceTree mst) {
        return findNameSpan(mst.getName().toString(), mst, JavaTokenId.DOT, JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT);
    }
    
    /**Find span of the name in the DocTree's reference tree (see {@link #getReferenceName(com.sun.source.util.DocTreePath)}
     * identifier in the source. Returns starting and ending offset of the name in
     * the source code that was parsed (ie. {@link CompilationInfo#getText()}, which
     * may differ from the positions in the source document if it has been already
     * altered.
     * 
     * @param ref reference for which the identifier should be found
     * @return the span of the name, or null if cannot be found
     * @since 0.124
     */
    public int[] findNameSpan(DocCommentTree docTree, ReferenceTree ref) {
        Name name = ((DCReference) ref).memberName;
        if (name == null || !SourceVersion.isIdentifier(name)) {
            //names like "<error>", etc.
            return null;
        }
        
        int pos = (int) info.getDocTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), docTree, ref);
        
        if (pos < 0)
            return null;
        
        TokenSequence<JavaTokenId> tokenSequence = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        
        tokenSequence.move(pos);
        
        if (!tokenSequence.moveNext() ||
            (tokenSequence.token().id() != JavaTokenId.JAVADOC_COMMENT &&
             tokenSequence.token().id() != JavaTokenId.JAVADOC_COMMENT_LINE_RUN)) {
            return null;
        }
        
        TokenSequence<JavadocTokenId> jdocTS = tokenSequence.embedded(JavadocTokenId.language());
        
        jdocTS.move(pos);
        
        boolean wasNext;
        
        while ((wasNext = jdocTS.moveNext()) && jdocTS.token().id() != JavadocTokenId.HASH)
            ;
        
        if (wasNext && jdocTS.moveNext()) {
            if (jdocTS.token().id() == JavadocTokenId.IDENT &&
                name.contentEquals(jdocTS.token().text())) {
                return new int[] {
                    jdocTS.offset(),
                    jdocTS.offset() + jdocTS.token().length()
                };
            }
        }
        
        return null;
    }
    
    private int[] findNameSpan(String name, Tree t, JavaTokenId... allowedTokens) {
        if (!SourceVersion.isIdentifier(name)) {
            //names like "<error>", etc.
            return null;
        }
        
        JCTree jcTree = (JCTree) t;
        int pos = jcTree.pos;
        
        if (pos < 0)
            return null;
        
        Set<JavaTokenId> allowedTokensSet = EnumSet.noneOf(JavaTokenId.class);
        
        allowedTokensSet.addAll(Arrays.asList(allowedTokens));
        
        TokenSequence<JavaTokenId> tokenSequence = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        
        tokenSequence.move(pos);
        
        boolean wasNext;
        
        while ((wasNext = tokenSequence.moveNext()) && allowedTokensSet.contains(tokenSequence.token().id()))
            ;
        
        if (wasNext) {
            if (tokenSequence.token().id() == JavaTokenId.IDENTIFIER) {
                boolean nameMatches;
                
                if (!(nameMatches = name.contentEquals(tokenSequence.token().text()))) {
                    ExpressionTree expr = info.getTreeUtilities().parseExpression(tokenSequence.token().text().toString(), new SourcePositions[1]);
                    
                    nameMatches = expr.getKind() == Kind.IDENTIFIER && name.contentEquals(((IdentifierTree) expr).getName());
                }

                if (nameMatches) {
                    return new int[] {
                        tokenSequence.offset(),
                        tokenSequence.offset() + tokenSequence.token().length()
                    };
                }
            }
        }
        
        tokenSequence.move(pos);
        
        if (tokenSequence.moveNext() && tokenSequence.token().id() == JavaTokenId.JAVADOC_COMMENT) {
            //TODO: this is not precise enough
            return new int[] {
                pos + 1,
                pos + name.length() + 1
            };
        }
        return null;
    }
    
    /**Find the target of <code>break</code> or <code>continue</code>. The given
     * {@link CompilationInfo} has to be at least in the {@link Phase#RESOLVED} phase.
     * 
     * @param breakOrContinue {@link TreePath} to the tree that should be inspected.
     *                        The <code>breakOrContinue.getLeaf().getKind()</code>
     *                        has to be one of {@link Kind#BREAK}, {@link Kind#CONTINUE}, or {@link Kind#YIELD}, or
     *                        an IllegalArgumentException is thrown
     * @return the tree that is the "target" for the given break or continue statement, or null if there is none. Tree can be of type StatementTree or ExpressionTree
     * @throws IllegalArgumentException if the given tree is not a break or continue tree or if the given {@link CompilationInfo}
     *         is not in the {@link Phase#RESOLVED} phase.
     * @since 2.40
     */
    public Tree getBreakContinueTargetTree(TreePath breakOrContinue) throws IllegalArgumentException {
        if (info.getPhase().compareTo(Phase.RESOLVED) < 0)
            throw new IllegalArgumentException("Not in correct Phase. Required: Phase.RESOLVED, got: Phase." + info.getPhase().toString());
        
        Tree leaf = breakOrContinue.getLeaf();
        
        switch (leaf.getKind()) {
            case BREAK:
                return (Tree) ((JCTree.JCBreak) leaf).target;
            case CONTINUE:
                Tree target = (Tree) ((JCTree.JCContinue) leaf).target;
                
                if (target == null)
                    return null;
                
                if (((JCTree.JCContinue) leaf).label == null)
                    return target;
                
                TreePath tp = breakOrContinue;
                
                while (tp.getLeaf() != target) {
                    tp = tp.getParentPath();
                }
                
                Tree parent = tp.getParentPath().getLeaf();
                
                if (parent.getKind() == Kind.LABELED_STATEMENT) {
                    return (StatementTree) parent;
                } else {
                    return target;
                }
            case YIELD:
                return (Tree) ((JCTree.JCYield) leaf).target;
            default:
                throw new IllegalArgumentException("Unsupported kind: " + leaf.getKind());
        }
    }
    
    /**Find the target of <code>break</code> or <code>continue</code>. The given
     * {@link CompilationInfo} has to be at least in the {@link Phase#RESOLVED} phase.
     * 
     * @param breakOrContinue {@link TreePath} to the tree that should be inspected.
     *                        The <code>breakOrContinue.getLeaf().getKind()</code>
     *                        has to be either {@link Kind#BREAK} or {@link Kind#CONTINUE}, or
     *                        an IllegalArgumentException is thrown
     * @return the tree that is the "target" for the given break or continue statement, or null if there is none.
     * @throws IllegalArgumentException if the given tree is not a break or continue tree or if the given {@link CompilationInfo}
     *         is not in the {@link Phase#RESOLVED} phase.
     * @since 0.16
     */
    public StatementTree getBreakContinueTarget(TreePath breakOrContinue) throws IllegalArgumentException {
        if (info.getPhase().compareTo(Phase.RESOLVED) < 0)
            throw new IllegalArgumentException("Not in correct Phase. Required: Phase.RESOLVED, got: Phase." + info.getPhase().toString());
        
        Tree leaf = breakOrContinue.getLeaf();
        
        switch (leaf.getKind()) {
            case BREAK:
                Tree breakTarget = (Tree) ((JCTree.JCBreak) leaf).target;
                if (breakTarget == null || !(breakTarget instanceof StatementTree)) {
                    return null;
                }
                return (StatementTree) breakTarget;
            case CONTINUE:
                StatementTree target = (StatementTree) ((JCTree.JCContinue) leaf).target;
                
                if (target == null)
                    return null;
                
                if (((JCTree.JCContinue) leaf).label == null)
                    return target;
                
                TreePath tp = breakOrContinue;
                
                while (tp.getLeaf() != target) {
                    tp = tp.getParentPath();
                }
                
                Tree parent = tp.getParentPath().getLeaf();
                
                if (parent.getKind() == Kind.LABELED_STATEMENT) {
                    return (StatementTree) parent;
                } else {
                    return target;
                }
            default:
                throw new IllegalArgumentException("Unsupported kind: " + leaf.getKind());
        }
    }

    /**Decode escapes defined in: https://openjdk.org/projects/coin, 3.1-3.9.
     * Must be a full token text, including possible #".
     *
     * @param text to decode
     * @return decoded escapes from the identifier
     * @see <a href="https://openjdk.org/projects/coin">ProjectCoinProposal</a>
     * @since 0.56
     */
    public @NonNull CharSequence decodeIdentifier(@NonNull CharSequence text) {
        return decodeIdentifierInternal(text);
    }

    /**Encode identifier using escapes defined in: https://openjdk.org/projects/coin, 3.1-3.9.
     *
     * @param ident to encode
     * @return encoded identifier, including #" if necessary
     * @see <a href="https://openjdk.org/projects/coin">ProjectCoinProposal</a>
     * @since 0.56
     */
    public @NonNull CharSequence encodeIdentifier(@NonNull CharSequence ident) {
        return encodeIdentifierInternal(ident);
    }

    static @NonNull CharSequence decodeIdentifierInternal(@NonNull CharSequence text) {
        if (text.charAt(0) != '#') {
            return text;
        }

        int count = text.charAt(text.length() - 1) == '"' ? text.length() - 1 : text.length();
        StringBuilder sb = new StringBuilder(text.length());

        for (int c = 2; c < count; c++) {
            if (text.charAt(c) == '\\' && ++c < count) {
                if (EXOTIC_ESCAPE.contains(text.charAt(c))) {
                    sb.append('\\');
                    sb.append(text.charAt(c));
                } else {
                    //XXX: handle \012
                    Character remaped = ESCAPE_UNENCODE.get(text.charAt(c));

                    if (remaped != null) {
                        sb.append(remaped);
                    } else {
                        //TODO: illegal?
                        sb.append(text.charAt(c));
                    }
                }
            } else {
                sb.append(text.charAt(c));
            }
        }

        return sb.toString();
    }

    static @NonNull CharSequence encodeIdentifierInternal(@NonNull CharSequence ident) {
        if (ident.length() == 0) {
            //???
            return ident;
        }

        StringBuilder sb = new StringBuilder(ident.length());
        boolean needsExotic = Character.isJavaIdentifierStart(ident.charAt(0));

        //XXX: code points?
        for (int i = 0; i < ident.length(); i++) {
            char c = ident.charAt(i);

            if (Character.isJavaIdentifierPart(c)) {
                sb.append(c);
                continue;
            }

            needsExotic = true;

            Character target = ESCAPE_ENCODE.get(c);

            if (target != null) {
                sb.append('\\');
                sb.append(target);
            } else {
                sb.append(c);
            }
        }

        if (needsExotic) {
            sb.append("\"");
            sb.insert(0, "#\"");

            return sb.toString();
        } else {
            return ident;
        }
    }

    static Set<Character> EXOTIC_ESCAPE = new HashSet<Character>(
            Arrays.<Character>asList('!', '#', '$', '%', '&', '(', ')', '*', '+', ',', '-',
                                     ':', '=', '?', '@', '^', '_', '`', '{', '|', '}')
    );

    private static final Map<Character, Character> ESCAPE_UNENCODE;
    private static final Map<Character, Character> ESCAPE_ENCODE;

    static {
        Map<Character, Character> unencode = new HashMap<Character, Character>();

        unencode.put('n', '\n');
        unencode.put('t', '\t');
        unencode.put('b', '\b');
        unencode.put('r', '\r');

        ESCAPE_UNENCODE = Collections.unmodifiableMap(unencode);

        Map<Character, Character> encode = new HashMap<Character, Character>();

        encode.put('\n', 'n');
        encode.put('\t', 't');
        encode.put('\b', 'b');
        encode.put('\r', 'r');

        ESCAPE_ENCODE = Collections.unmodifiableMap(encode);
    }

    /**Returns new tree based on {@code original}, such that each visited subtree
     * that occurs as a key in {@code original2Translated} is replaced by the corresponding
     * value from {@code original2Translated}. The value is then translated using the same
     * algorithm. Each key from {@code original2Translated} is used at most once.
     * Unless the provided {@code original} tree is a key in {@code original2Translated},
     * the resulting tree has the same type as {@code original}.
     *
     * Principally, the method inner workings are:
     * <pre>
     * translate(original, original2Translated) {
     *      if (original2Translated.containsKey(original))
     *          return translate(original2Translated.remove(original));
     *      newTree = copyOf(original);
     *      for (Tree child : allChildrenOf(original)) {
     *          newTree.replace(child, translate(child, original2Translated));
     *      }
     *      return newTree;
     * }
     * </pre>
     * 
     * @param original the tree that should be translated
     * @param original2Translated map containing trees that should be translated
     * @return translated tree.
     * @since 0.64
     */
    public @NonNull Tree translate(final @NonNull Tree original, final @NonNull Map<? extends Tree, ? extends Tree> original2Translated) {
        return translate(original, original2Translated, new NoImports(info), null);
    }
    
    @NonNull Tree translate(final @NonNull Tree original, final @NonNull Map<? extends Tree, ? extends Tree> original2Translated, ImportAnalysis2 ia, Map<Tree, Object> tree2Tag) {
        ImmutableTreeTranslator itt = new ImmutableTreeTranslator(info instanceof WorkingCopy ? (WorkingCopy)info : null) {
            private @NonNull Map<Tree, Tree> map = new HashMap<Tree, Tree>(original2Translated);
            @Override
            public Tree translate(Tree tree) {
                Tree translated = map.remove(tree);

                if (translated != null) {
                    return translate(translated);
                } else {
                    return super.translate(tree);
                }
            }
        };

        Context c = info.impl.getJavacTask().getContext();

        itt.attach(c, ia, tree2Tag);

        return itt.translate(original);
    }
    
    /**Returns new tree based on {@code original}, such that each visited subtree
     * that occurs as a key in {@code original2Translated} is replaced by the corresponding
     * value from {@code original2Translated}. The value is then translated using the same
     * algorithm. Each key from {@code original2Translated} is used at most once.
     * Unless the provided {@code original} tree is a key in {@code original2Translated},
     * the resulting tree has the same type as {@code original}.
     *
     * Principally, the method inner workings are:
     * <pre>
     * translate(original, original2Translated) {
     *      if (original2Translated.containsKey(original))
     *          return translate(original2Translated.remove(original));
     *      newTree = copyOf(original);
     *      for (Tree child : allChildrenOf(original)) {
     *          newTree.replace(child, translate(child, original2Translated));
     *      }
     *      return newTree;
     * }
     * </pre>
     * 
     * @param original the tree that should be translated
     * @param original2Translated map containing trees that should be translated
     * @return translated tree.
     * @since 0.124
     */
    public @NonNull DocTree translate(final @NonNull DocTree original, final @NonNull Map<? extends DocTree, ? extends DocTree> original2Translated) {
        return translate(original, original2Translated, new NoImports(info), null);
    }
    
    @NonNull DocTree translate(final @NonNull DocTree original, final @NonNull Map<? extends DocTree, ? extends DocTree> original2Translated, ImportAnalysis2 ia, Map<Tree, Object> tree2Tag) {
        ImmutableDocTreeTranslator itt = new ImmutableDocTreeTranslator(info instanceof WorkingCopy ? (WorkingCopy)info : null) {
            private @NonNull Map<DocTree, DocTree> map = new HashMap<DocTree, DocTree>(original2Translated);
            @Override
            public DocTree translate(DocTree tree) {
                DocTree translated = map.remove(tree);

                if (translated != null) {
                    return translate(translated);
                } else {
                    return super.translate(tree);
                }
            }
        };

        Context c = info.impl.getJavacTask().getContext();

        itt.attach(c, ia, tree2Tag);

        return itt.translate(original);
    }

    private static final class NoImports extends ImportAnalysis2 {

        public NoImports(CompilationInfo info) {
            super(info);
        }

        @Override
        public void classEntered(ClassTree clazz) {}

        @Override
        public void enterVisibleThroughClasses(ClassTree clazz) {}

        @Override
        public void classLeft() {}

        @Override
        public ExpressionTree resolveImport(MemberSelectTree orig, Element element) {
            return orig;
        }

        @Override
        public void setCompilationUnit(CompilationUnitTree cut) {}

        @Override
        public void setImports(List<? extends ImportTree> importsToAdd) {
        }

        @Override
        public Set<? extends Element> getImports() {
            return Collections.emptySet();
        }

        @Override
        public void setPackage(ExpressionTree packageNameTree) {}

    }
    
    private static class UncaughtExceptionsVisitor extends ErrorAwareTreePathScanner<Void, Set<TypeMirror>> {
        
        private final CompilationInfo info;
        
        private UncaughtExceptionsVisitor(final CompilationInfo info) {
            this.info = info;
        }
    
        public Void visitMethodInvocation(MethodInvocationTree node, Set<TypeMirror> p) {
            super.visitMethodInvocation(node, p);
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el != null && el.getKind() == ElementKind.METHOD)
                p.addAll(((ExecutableElement)el).getThrownTypes());
            return null;
        }

        public Void visitNewClass(NewClassTree node, Set<TypeMirror> p) {
            super.visitNewClass(node, p);
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el != null && el.getKind() == ElementKind.CONSTRUCTOR)
                p.addAll(((ExecutableElement)el).getThrownTypes());
            return null;
        }

        public Void visitThrow(ThrowTree node, Set<TypeMirror> p) {
            super.visitThrow(node, p);
            TypeMirror tm = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));
            if (tm != null) {
                if (tm.getKind() == TypeKind.DECLARED)
                    p.add(tm);
                else if (tm.getKind() == TypeKind.UNION)
                    p.addAll(((UnionType)tm).getAlternatives());
            }
            return null;
        }

        public Void visitTry(TryTree node, Set<TypeMirror> p) {
            Set<TypeMirror> s = new LinkedHashSet<TypeMirror>();
            Trees trees = info.getTrees();
            Types types = info.getTypes();
            Elements elements = info.getElements();
            for (Tree res : node.getResources()) {
                TypeMirror resType = trees.getTypeMirror(new TreePath(getCurrentPath(), res));
                if (resType != null && resType.getKind() == TypeKind.DECLARED) {
                    for (ExecutableElement method : ElementFilter.methodsIn(elements.getAllMembers((TypeElement)((DeclaredType)resType).asElement()))) {
                        if ("close".contentEquals(method.getSimpleName()) //NOI18N
                                && method.getParameters().isEmpty()
                                && method.getTypeParameters().isEmpty()) {
                            s.addAll(method.getThrownTypes());
                        }
                    }
                }
            }
            scan(node.getBlock(), s);
            Set<TypeMirror> c = new LinkedHashSet<TypeMirror>();
            for (CatchTree ct : node.getCatches()) {
                TypeMirror t = trees.getTypeMirror(new TreePath(getCurrentPath(), ct.getParameter().getType()));
                if (t != null) {
                    if (t.getKind() == TypeKind.UNION) {
                        for (TypeMirror tm : ((UnionType)t).getAlternatives()) {
                            if (tm != null && tm.getKind() != TypeKind.ERROR)
                                c.add(tm);
                        }
                    } else if (t.getKind() != TypeKind.ERROR) {
                        c.add(t);
                    }
                }
            }
            for (TypeMirror t : c) {
                for (Iterator<TypeMirror> it = s.iterator(); it.hasNext();) {
                    if (types.isSubtype(it.next(), t))
                        it.remove();
                }
            }
            p.addAll(s);
            scan(node.getCatches(), p);
            scan(node.getFinallyBlock(), p);
            return null;            
        }

        public Void visitMethod(MethodTree node, Set<TypeMirror> p) {
            Set<TypeMirror> s = new LinkedHashSet<TypeMirror>();
            scan(node.getBody(), s);
            for (ExpressionTree et : node.getThrows()) {
                TypeMirror t = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), et));
                if (t != null && t.getKind() != TypeKind.ERROR) {
                    for (Iterator<TypeMirror> it = s.iterator(); it.hasNext();)
                        if (info.getTypes().isSubtype(it.next(), t))
                            it.remove();
                }
            }
            p.addAll(s);
            return null;
        }

        @Override
        public Void visitClass(ClassTree node, Set<TypeMirror> p) {
            return null;
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, Set<TypeMirror> p) {
            return null;
        }
    }
    
    private static class UnrelatedTypeMirrorSet extends AbstractSet<TypeMirror> {

        private Types types;
        private LinkedList<TypeMirror> list = new LinkedList<TypeMirror>();

        public UnrelatedTypeMirrorSet(Types types) {
            this.types = types;
        }

        @Override
        public boolean add(TypeMirror typeMirror) {
            for (ListIterator<TypeMirror> it = list.listIterator(); it.hasNext(); ) {
                TypeMirror tm = it.next();
                if (types.isSubtype(typeMirror, tm))
                    return false;
                if (types.isSubtype(tm, typeMirror))
                    it.remove();                    
            }
            return list.add(typeMirror);
        }
                
        @Override
        public Iterator<TypeMirror> iterator() {
            return list.iterator();
        }

        @Override
        public int size() {
            return list.size();
        }
    }

    /**Checks whether the given expression is a compile-time constant, as per JLS 15.28.
     *
     * @param expression the expression to check
     * @return true if and only if the given expression represents a compile-time constant value
     * @since 0.91
     */
    public boolean isCompileTimeConstantExpression(TreePath expression) {
        TypeMirror attributeTree = info.getTrees().getTypeMirror(expression);
        Type attributeTreeImpl = (Type) attributeTree;

        return attributeTreeImpl != null && attributeTreeImpl.constValue() != null;
    }
    
    /**Find the type (the part before {@code #}) that is being referenced by the given {@link ReferenceTree}.
     * 
     * @param path the leaf must be {@link ReferenceTree}
     * @return the referred type, or {@code null} if none.
     * @since 0.124
     */
    public @CheckForNull ExpressionTree getReferenceClass(@NonNull DocTreePath path) {
        TreePath tp = path.getTreePath();
        DCReference ref = (DCReference) path.getLeaf();
        
        ((DocTrees) this.info.getTrees()).getElement(path);
//        was:
//        ((JavacTrees) this.info.getTrees()).ensureDocReferenceAttributed(tp, ref);
        
        return (ExpressionTree) ref.qualifierExpression;
    }
    
    /**Find the name (the name after {@code #}) that is being referenced by the given {@link ReferenceTree}.
     * 
     * @param path the leaf must be {@link ReferenceTree}
     * @return the referred member name, or {@code null} if none.
     * @since 0.124
     */
    public @CheckForNull Name getReferenceName(@NonNull DocTreePath path) {
        return ((DCReference) path.getLeaf()).memberName;
    }
    
    /**Find the parameters that are specified in the given {@link ReferenceTree}.
     * 
     * @param path the leaf must be {@link ReferenceTree}
     * @return the parameters for the referred method, or {@code null} if none.
     * @since 0.124
     */
    public @CheckForNull List<? extends Tree> getReferenceParameters(@NonNull DocTreePath path) {
        TreePath tp = path.getTreePath();
        DCReference ref = (DCReference) path.getLeaf();
        
        ((DocTrees) this.info.getTrees()).getElement(path);
//        was:
//        ((JavacTrees) this.info.getTrees()).ensureDocReferenceAttributed(tp, ref);
        
        return ref.paramTypes;
    }
   
    /**Check the var type variable in given tree path {@link TreePath}.
     * 
     * @param path the path of tree {@link TreePath}
     * @return the true if tree contains var keyword else return false
     * @since 2.31.0
     */
    public boolean isVarType(@NonNull TreePath path) {
        TokenSequence<JavaTokenId> tokenSequence = tokensFor(path.getLeaf());
        tokenSequence.moveStart();
        while(tokenSequence.moveNext() && tokenSequence.token().id() != JavaTokenId.EQ && tokenSequence.token().id() != JavaTokenId.COLON && tokenSequence.token().id() != JavaTokenId.RPAREN && tokenSequence.token().id() != JavaTokenId.SEMICOLON){
            if(tokenSequence.token().id() == JavaTokenId.VAR){
                return true;
            }
        }
        return false;
    }

    /**Check the tree is the end of compound declaration. {@link Tree}.
     *
     * @param tree the tree {@link Tree}
     * @return the true if tree is end of compound declaration else return false
     * @since 2.33.0
     */
    public boolean isEndOfCompoundVariableDeclaration(@NonNull Tree tree) {
        TokenSequence<JavaTokenId> tokenSequence = tokensFor(tree);
        tokenSequence.moveEnd();
        if (tokenSequence.movePrevious() && tokenSequence.token().id() != JavaTokenId.COMMA) {
            return true;
        }
        return false;
    }
 
    /**
     * Checks whether tree is part of compound variable declaration.
     *
     * @param tree tree to examine.
     * @return true if {@code tree} is part of compound var declaration.
     * @since 2.34.0
     */
    public boolean isPartOfCompoundVariableDeclaration(@NonNull Tree tree) {
        TokenSequence<JavaTokenId> tokenSequence = tokensFor(tree);

        if (tree.getKind() != Tree.Kind.VARIABLE) {
            return false;
        }

        // If tree ends with comma then tree is part of compound variable declaration.
        tokenSequence.moveEnd();
        if (tokenSequence.movePrevious() && tokenSequence.token().id() == JavaTokenId.COMMA) {
            return true;
        }

        int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
        tokenSequence.moveStart();

        int tokensLength = 0;

        // To find out the first subtree from compound varaible declaration statement(if any).
        while (tokenSequence.moveNext()) {
            tokensLength += tokenSequence.token().length();
            if (tokenSequence.token().id() == JavaTokenId.IDENTIFIER) {

                Tree path = pathFor(startPos + tokensLength).getLeaf();
                TokenSequence<JavaTokenId> TokenSeq = tokensFor(path);
                TokenSeq.moveEnd();

                if (TokenSeq.movePrevious() && TokenSeq.token().id() == JavaTokenId.COMMA) {
                    return true;
                }
                break;
            }
        }

        return false;
    }

    /**
     * Check the tree has compile error in given errors.
     *
     * @param tree compilation tree
     * @param errors Array of error code
     * @return true if tree has compile error present in list of errors.
     * @since 2.37
     */
    public boolean hasError(@NonNull Tree tree, String... errors) {
        long startPos = info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
        long endPos = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree);

        List<Diagnostic> diagnosticsList = info.getDiagnostics();
        for (Diagnostic d : diagnosticsList) {
            if ((d.getKind() == Diagnostic.Kind.ERROR) && ((d.getStartPosition() >= startPos) && (d.getEndPosition() <= endPos))) {
                if (errors == null || errors.length == 0) {
                    return true;
                } else {
                    for (String error : errors) {
                        if (error.equals(d.getCode())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static final class NBScope implements Scope {

        private final JavacScope delegate;

        private NBScope(JavacScope delegate) {
            this.delegate = delegate;
        }
        
        private boolean areAccessibilityChecksDisabled() {
            return true;
        }

        @Override
        public Scope getEnclosingScope() {
            //TODO: wrap?
            return delegate.getEnclosingScope();
        }

        @Override
        public TypeElement getEnclosingClass() {
            return delegate.getEnclosingClass();
        }

        @Override
        public ExecutableElement getEnclosingMethod() {
            return delegate.getEnclosingMethod();
        }

        @Override
        public Iterable<? extends Element> getLocalElements() {
            return delegate.getLocalElements();
        }
    }

    static {
        TreeUtilitiesAccessor.setInstance(new TreeUtilitiesAccessor() {
            @Override
            public StatementTree parseStatement(JavacTaskImpl task, String stmt, SourcePositions[] sourcePositions) {
                return parseStatementImpl(task, stmt, sourcePositions);
            }
        });
    }
}
