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

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.text.BadLocationException;
import javax.tools.JavaFileObject;

import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.DocTrees;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import java.util.Collection;
import java.util.Comparator;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.modules.java.source.parsing.JavacParserResult;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import static org.netbeans.api.java.source.ModificationResult.*;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.FileObjectFromTemplateCreator;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.ParsingUtils;
import org.netbeans.modules.java.source.pretty.ImportAnalysis2;
import org.netbeans.modules.java.source.query.CommentSet.RelativePosition;
import org.netbeans.modules.java.source.save.CasualDiff;
import org.netbeans.modules.java.source.save.CasualDiff.Diff;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.java.source.save.DiffUtilities;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.netbeans.modules.java.source.save.ElementOverlay.FQNComputer;
import org.netbeans.modules.java.source.transform.ImmutableDocTreeTranslator;
import org.netbeans.modules.java.source.transform.ImmutableTreeTranslator;
import org.netbeans.modules.java.source.transform.TreeDuplicator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;

/**XXX: extends CompilationController now, finish method delegation
 *
 * @author Dusan Balek, Petr Hrebejk, Tomas Zezul
 */
public class WorkingCopy extends CompilationController {

    static Reference<WorkingCopy> instance;
    private Map<Tree, Tree> changes;
    private Map<Tree, Map<DocTree, DocTree>> docChanges;
    private Map<JavaFileObject, CompilationUnitTree> externalChanges;
    private List<Diff> textualChanges;
    private Map<Integer, String> userInfo;
    private boolean afterCommit = false;
    private TreeMaker treeMaker;
    private Map<Tree, Object> tree2Tag;
    private final ElementOverlay overlay;
    /**
     * Trees introduced by rewriting. Some checks (now: comments) are done whether a Tree is known to the system
     * and may fail on newly added trees if they're rewritten recursively
     */
    private Map<Tree, Boolean> introducedTrees;
    
    /**
     * Hint information on rewrites done. Can mark a Tree as `new' which means comments will not be copied for it
     * and it will not assume other tree's formatting on output. Can link a Tree to one or more other Trees, which
     * causes the old trees comments to be preserved (copied) should the old trees be removed from the source.
     */
    private Map<Tree, Tree> rewriteHints = new HashMap<Tree, Tree>();
    
    WorkingCopy(final CompilationInfoImpl impl, ElementOverlay overlay) {
        super(impl);
        this.overlay = overlay;
    }

    private synchronized void init() {
        if (changes != null) //already initialized
            return;
        
        treeMaker = new TreeMaker(this, TreeFactory.instance(getContext()));
        changes = new IdentityHashMap<Tree, Tree>();
        docChanges = new IdentityHashMap<Tree, Map<DocTree, DocTree>>();
        tree2Tag = new IdentityHashMap<Tree, Object>();
        externalChanges = null;
        textualChanges = new ArrayList<Diff>();
        userInfo = new HashMap<Integer, String>();
        introducedTrees = new IdentityHashMap<Tree, Boolean>();

        //#208490: force the current ElementOverlay:
        getContext().put(ElementOverlay.class, (ElementOverlay) null);
        getContext().put(ElementOverlay.class, overlay);
    }
    
    private Context getContext() {
        return impl.getJavacTask().getContext();
    }
    
    // API of the class --------------------------------------------------------

    /**
     * Returns an instance of the {@link WorkingCopy} for
     * given {@link org.netbeans.modules.parsing.spi.Parser.Result} if it is a result
     * of a java parser.
     * @param result for which the {@link WorkingCopy} should be
     * returned.
     * @return a {@link WorkingCopy} or null when the given result
     * is not a result of java parsing.
     * @since 0.42
     */
    public static @NullUnknown WorkingCopy get (final @NonNull Parser.Result result) {
        Parameters.notNull("result", result); //NOI18N
        WorkingCopy copy = instance != null ? instance.get() : null;
        if (copy != null && result instanceof JavacParserResult) {
            final JavacParserResult javacResult = (JavacParserResult)result;
            CompilationController controller = javacResult.get(CompilationController.class);
            if (controller != null && controller.impl == copy.impl)
                return copy;
        }
        return null;
    }

    @Override
    public @NonNull JavaSource.Phase toPhase(@NonNull JavaSource.Phase phase) throws IOException {
        //checkConfinement() called by super
        JavaSource.Phase result = super.toPhase(phase);
        
        if (result.compareTo(JavaSource.Phase.PARSED) >= 0) {
            init();
        }
        
        return result;
    }        
    
    public synchronized @NonNull TreeMaker getTreeMaker() throws IllegalStateException {
        checkConfinement();
        if (treeMaker == null)
            throw new IllegalStateException("Cannot call getTreeMaker before toPhase.");
        return treeMaker;
    }
    
    Map<Tree, Tree> getChangeSet() {
        return changes;
    }
    
    /**
     * Replaces the original tree <code>oldTree</code> with the new one -
     * <code>newTree</code>.
     * <p>
     * To create a new file, use
     * <code>rewrite(null, compilationUnitTree)</code>. Use
     * {@link GeneratorUtilities#createFromTemplate GeneratorUtilities.createFromTemplate()}
     * to create a new compilation unit tree from a template.
     * <p>
     * <code>newTree</code> cannot be <code>null</code>, use methods in
     * {@link TreeMaker} for tree element removal. If <code>oldTree</code> is
     * null, <code>newTree</code> must be of kind
     * {@link Kind#COMPILATION_UNIT COMPILATION_UNIT}.
     * <p>
     * Since 0.137, comments in the rewritten node will be automatically assigned to the newTree
     * node. Use {@link TreeMaker#asRemoved} to discard comments from the oldTree explicitly.
     * 
     * 
     * @param oldTree  tree to be replaced, use tree already represented in
     *                 source code. <code>null</code> to create a new file.
     * @param newTree  new tree, either created by <code>TreeMaker</code>
     *                 or obtained from different place. <code>null</code>
     *                 values are not allowed.
     * @throws IllegalStateException if <code>toPhase()</code> method was not
     *         called before.
     * @throws IllegalArgumentException when <code>null</code> was passed to the 
     *         method.
     * @see GeneratorUtilities#createFromTemplate
     * @see TreeMaker
     * @since 0.137
     */
    public synchronized void rewrite(@NullAllowed Tree oldTree, @NonNull Tree newTree) {
        checkConfinement();
        if (changes == null) {
            throw new IllegalStateException("Cannot call rewrite before toPhase.");
        }
        if (oldTree == newTree) {
            // no change operation called.
            return;
        }
        if (oldTree == null && Kind.COMPILATION_UNIT == newTree.getKind()) {
            createCompilationUnit((JCTree.JCCompilationUnit) newTree);
            return;
        }
        if (oldTree == null || newTree == null)
            throw new IllegalArgumentException("Null values are not allowed.");
        Tree t = rewriteHints.get(oldTree);
        if (t == null) {
            // if the old tree does not have any association to a new generated node, make an implicit association
            associateTree(newTree, oldTree, false);
        }
        // Perf: trees are collected just when asserts are enabled.
        assert new TreeCollector(introducedTrees, true).scan(newTree, null) != null;
        changes.put(oldTree, newTree);
    }
    
    /**
     * Returns false when asserts enabled and the tree is not among the trees that rewrite original contents. Returns
     * true if asserts disabled (saves time, trees are not collected)
     * 
     * @param t the tree to check.
     * @return false, if the tree is not among replacements
     */
    boolean validateIsReplacement(Tree t) {
        boolean ok = true;
        assert !(ok = false);
        return ok || introducedTrees.containsKey(t);
    }
    
    private static class TreeCollector extends ErrorAwareTreeScanner {
        private final Map<Tree, Boolean> collectTo;
        private final boolean add;

        public TreeCollector(Map<Tree, Boolean> collectTo, boolean add) {
            this.collectTo = collectTo;
            this.add = add;
        }


        @Override
        public Object scan(Tree node, Object p) {
            if (add) {
                collectTo.put(node, true);
            } else {
                collectTo.remove(node);
            }
            super.scan(node, p);
            return true;
        }
        
    }
    
    /**
     * Replaces the original doctree <code>oldTree</code> with the new one -
     * <code>newTree</code> for a specific tree.
     * <p>
     * To create a new javadoc comment, use
     * <code>rewrite(tree, null, docCommentTree)</code>.
     * <p>
     * <code>tree</code> and <code>newTree</code> cannot be <code>null</code>.
     * If <code>oldTree</code> is null, <code>newTree</code> must be of kind
     * {@link com.sun.source.doctree.DocTree.Kind#DOC_COMMENT DOC_COMMENT}.
     * 
     * @param tree     the tree to which the doctrees belong.
     * @param oldTree  tree to be replaced, use tree already represented in
     *                 source code. <code>null</code> to create a new file.
     * @param newTree  new tree, either created by <code>TreeMaker</code>
     *                 or obtained from different place. <code>null</code>
     *                 values are not allowed.
     * @throws IllegalStateException if <code>toPhase()</code> method was not
     *         called before.
     * @since 0.124
     */
    public synchronized void rewrite(@NonNull Tree tree, @NonNull DocTree oldTree, @NonNull DocTree newTree) {
        checkConfinement();
        if (docChanges == null) {
            throw new IllegalStateException("Cannot call rewrite before toPhase.");
        }
        
        if (oldTree == newTree) {
            // no change operation called.
            return;
        }
        
        Map<DocTree, DocTree> changesMap = docChanges.get(tree);
        if(changesMap == null) {
            changesMap = new IdentityHashMap<DocTree, DocTree>();
            docChanges.put(tree, changesMap);
        }
        
        changesMap.put(oldTree, newTree);
    }
              
    /**
     * Replace a part of a comment token with the given text.
     * 
     * Please note that this is a special purpose method to handle eg.
     * "Apply Rename in Comments" option in the Rename refactoring.
     * 
     * It is caller's responsibility to ensure that replacements done by this method
     * will not clash with replacements done by the general-purpose method
     * {@link #rewrite(Tree,Tree)}.
     * 
     * @param start absolute offset in the original text to start the replacement
     * @param length how many characters should be deleted from the original text
     * @param newText new text to be inserted at the specified offset
     * @throws java.lang.IllegalArgumentException when an attempt is made to replace non-comment text
     * @since 0.23
     */
    public synchronized void rewriteInComment(int start, int length, @NonNull String newText) throws IllegalArgumentException {
        checkConfinement();
        TokenSequence<JavaTokenId> ts = getTokenHierarchy().tokenSequence(JavaTokenId.language());
        
        ts.move(start);
        
        if (!ts.moveNext()) {
            throw new IllegalArgumentException("Cannot rewriteInComment start=" + start + ", text length=" + getText().length());
        }
        
        if (ts.token().id() != JavaTokenId.LINE_COMMENT && ts.token().id() != JavaTokenId.BLOCK_COMMENT && ts.token().id() != JavaTokenId.JAVADOC_COMMENT) {
            throw new IllegalArgumentException("Cannot rewriteInComment: attempt to rewrite non-comment token: " + ts.token().id());
        }
        
        if (ts.offset() + ts.token().length() < start + length) {
            throw new IllegalArgumentException("Cannot rewriteInComment: attempt to rewrite text after comment token. Token end offset: " + (ts.offset() + ts.token().length()) + ", rewrite end offset: " + (start + length));
        }
        
        int commentPrefix;
        int commentSuffix;
        
        switch (ts.token().id()) {
            case LINE_COMMENT: commentPrefix = 2; commentSuffix = 0; break;
            case BLOCK_COMMENT: commentPrefix = 2; commentSuffix = 2; break;
            case JAVADOC_COMMENT: commentPrefix = 3; commentSuffix = 2; break;
            default: throw new IllegalStateException("Internal error");
        }
        
        if (ts.offset() + commentPrefix > start) {
            throw new IllegalArgumentException("Cannot rewriteInComment: attempt to rewrite comment prefix");
        }
        
        if (ts.offset() + ts.token().length() - commentSuffix < start + length) {
            throw new IllegalArgumentException("Cannot rewriteInComment: attempt to rewrite comment suffix");
        }
        
        textualChanges.add(Diff.delete(start, start + length));
        textualChanges.add(Diff.insert(start + length, newText));
        userInfo.put(start, NbBundle.getMessage(CasualDiff.class,"TXT_RenameInComment")); //NOI18N
    }
    
    /**
     * Tags a tree. Used in {@code ModificationResult} to determine position of tree inside document.
     * @param t the tree to be tagged
     * @param tag an {@code Object} used as tag
     * @since 0.37
     */
    public synchronized void tag(@NonNull Tree t, @NonNull Object tag) {
        tree2Tag.put(t, tag);
    }

    /**Returns the tree into which the given tree was rewritten using the
     * {@link #rewrite(com.sun.source.tree.Tree, com.sun.source.tree.Tree) } method,
     * transitively.
     * Will return the input tree if the input tree was never passed as the first
     * parameter of the {@link #rewrite(com.sun.source.tree.Tree, com.sun.source.tree.Tree) }
     * method.
     *
     * <p>Note that the returned tree will be exactly equivalent to a tree passed as
     * the second parameter to {@link #rewrite(com.sun.source.tree.Tree, com.sun.source.tree.Tree) }.
     * No attribution or other information will be added (or removed) to (or from) the tree.
     *
     * @param in the tree to inspect
     * @return tree into which the given tree was rewritten using the
     * {@link #rewrite(com.sun.source.tree.Tree, com.sun.source.tree.Tree) } method,
     * transitively
     * @since 0.102
     */
    public synchronized @NonNull Tree resolveRewriteTarget(@NonNull Tree in) {
        Map<Tree, Tree> localChanges = new IdentityHashMap<Tree, Tree>(changes);

        while (localChanges.containsKey(in)) {
            in = localChanges.remove(in);
        }

        return in;
    }
    
    // Package private methods -------------------------------------------------        
    
//    static final Collection<Tree>   NOT_LINKED = new ArrayList<Tree>(0);
    static final Tree NOT_LINKED = new Tree() {

        @Override
        public Kind getKind() {
            return Kind.OTHER;
        }

        @Override
        public <R, D> R accept(TreeVisitor<R, D> visitor, D data) {
            return visitor.visitOther(this, data);
        }
    };
    
    /**
     * Associates a new tree with the original. Only one association is supported,
     * the last association wins. 
     * 
     * @param nue the new tree
     * @param original the to-be-replaced original
     */
    synchronized void associateTree(Tree nue, Tree original, boolean force) {
        if (rewriteHints == null) {
            rewriteHints = new HashMap<Tree, Tree>(7);
        }
        
        if (original == null) {
            Tree ex = rewriteHints.get(nue);
            if (ex == null || force) {
                rewriteHints.put(nue, NOT_LINKED);
            }
        } else if (original == nue) {
            return;
        } else {
            Tree ex = rewriteHints.get(original);
            if (ex == null || force) {
                rewriteHints.put(original, nue);
            }
        }
    }
    
    private static String codeForCompilationUnit(CompilationUnitTree topLevel) throws IOException {
        return ((JCTree.JCCompilationUnit) topLevel).sourcefile.getCharContent(true).toString();
    }
    
    class Translator extends ImmutableTreeTranslator {
        private Map<Tree, Tree> changeMap;

        public Translator() {
            super(WorkingCopy.this);
        }

        Tree translate(Tree tree, Map<Tree, Tree> changeMap) {
            this.changeMap = new HashMap<Tree, Tree>(changeMap);
            return translate(tree);
        }

        @Override
        public Tree translate(Tree tree) {
            assert changeMap != null;
            if (tree == null) {
                return null;
            }
            Tree repl = changeMap.remove(tree);
            Tree newRepl;
            if (repl != null) {
                newRepl = translate(repl);
            } else {
                newRepl = super.translate(tree);
            }
            return newRepl;
        }
    }
            
    private static boolean REWRITE_WHOLE_FILE = Boolean.getBoolean(WorkingCopy.class.getName() + ".rewrite-whole-file");

    private void addSyntheticTrees(DiffContext diffContext, Tree node) {
        if (node == null) return ;
        
        if (((JCTree) node).pos == (-1)) {
            diffContext.syntheticTrees.add(node);
            return ;
        }
        
        if (node.getKind() == Kind.EXPRESSION_STATEMENT) {
            ExpressionTree est = ((ExpressionStatementTree) node).getExpression();

            if (est.getKind() == Kind.METHOD_INVOCATION) {
                ExpressionTree select = ((MethodInvocationTree) est).getMethodSelect();

                if (select.getKind() == Kind.IDENTIFIER && ((IdentifierTree) select).getName().contentEquals("super")) {
                    if (getTreeUtilities().isSynthetic(diffContext.origUnit, node)) {
                        diffContext.syntheticTrees.add(node);
                    }
                }
            }
        }
        if (node.getKind() == Kind.VARIABLE) {
            JCVariableDecl var = (JCVariableDecl) node;

            if (var.declaredUsingVar()) {
                diffContext.syntheticTrees.add(var.vartype);
            }
        }
        if (node.getKind() == Kind.LAMBDA_EXPRESSION) {
            JCLambda lambda = (JCLambda) node;

            if (lambda.paramKind == JCLambda.ParameterKind.IMPLICIT) {
                for (JCVariableDecl param : lambda.params) {
                    diffContext.syntheticTrees.add(param.vartype);
                }
            }
        }
    }

    /**
     * Resolves all fields that belong to the same field group. 
     */
    private static @NonNull List<? extends Tree> collectFieldGroup(@NonNull CompilationInfo info, 
            @NonNull TreePath parentPath, Tree leaf) {
        Iterable<? extends Tree> children;

        switch (parentPath.getLeaf().getKind()) {
            case BLOCK: children = ((BlockTree) parentPath.getLeaf()).getStatements(); break;
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                children = ((ClassTree) parentPath.getLeaf()).getMembers(); break;
            case CASE:  children = ((CaseTree) parentPath.getLeaf()).getStatements(); break;
            default:    children = Collections.singleton(leaf); break;
        }

        List<Tree> result = new LinkedList<>();
        ModifiersTree currentModifiers = ((VariableTree) leaf).getModifiers();

        for (Tree c : children) {
            if (c.getKind() != Kind.VARIABLE) continue;

            if (((VariableTree) c).getModifiers() == currentModifiers) {
                result.add(c);
            }
        }
        
        return result;
    }

    private List<Difference> processCurrentCompilationUnit(final DiffContext diffContext, final Map<?, int[]> tag2Span) throws IOException, BadLocationException {
        final Set<TreePath> pathsToRewrite = new LinkedHashSet<TreePath>();
        final Map<TreePath, Map<Tree, Tree>> parent2Rewrites = new IdentityHashMap<TreePath, Map<Tree, Tree>>();
        final Map<Tree, DocCommentTree> tree2Doc = new IdentityHashMap<Tree, DocCommentTree>();
        boolean fillImports = true;
        
        final Map<Integer, String> userInfo = new HashMap<Integer, String>();
        final Set<Tree> oldTrees = new HashSet<Tree>();

        final Map<Tree, Boolean> presentInResult = new IdentityHashMap<Tree, Boolean>();
        if (CasualDiff.OLD_TREES_VERBATIM) {
            new ErrorAwareTreeScanner<Void, Void>() {
                private boolean synthetic = false;
                @Override
                public Void scan(Tree node, Void p) {
                    if (node == null) return null;
                    boolean oldSynthetic = synthetic;
                    try {
                        synthetic |= getTreeUtilities().isSynthetic(diffContext.origUnit, node) ||
                                     diffContext.syntheticTrees.contains(node);
                        if (!synthetic) {
                            oldTrees.add(node);
                        }
                        addSyntheticTrees(diffContext, node);
                        return super.scan(node, p);
                    } finally {
                        synthetic = oldSynthetic;
                    }
                }

                @Override
                public Void visitForLoop(ForLoopTree node, Void p) {
                    try {
                        return super.visitForLoop(node, p);
                    } finally {
                        oldTrees.removeAll(node.getInitializer());
                    }
                }

                @Override
                public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
                    try {
                        return super.visitEnhancedForLoop(node, p);
                    } finally {
                        oldTrees.remove(node.getVariable());
                    }
                }

                @Override
                public Void visitTry(TryTree node, Void p) {
                    try {
                        return super.visitTry(node, p);
                    } finally {
                        oldTrees.removeAll(node.getResources());
                    }
                }
                
            }.scan(diffContext.origUnit, null);
        } else {
            new ErrorAwareTreeScanner<Void, Void>() {
                @Override
                public Void scan(Tree node, Void p) {
                    addSyntheticTrees(diffContext, node);
                    addPresentInResult(presentInResult, node, true);
                    return super.scan(node, p);
                }
            }.scan(diffContext.origUnit, null);
        }

        if (!REWRITE_WHOLE_FILE) {
            new ErrorAwareTreePathScanner<Void, Void>() {
                private TreePath currentParent;
                private final Map<Tree, TreePath> tree2Path = new IdentityHashMap<Tree, TreePath>();
                private final FQNComputer fqn = new FQNComputer();
                private final Set<Tree> rewriteTarget;
                
                {
                    rewriteTarget = Collections.newSetFromMap(new IdentityHashMap<Tree, Boolean>());
                    rewriteTarget.addAll(changes.values());
                }

                private TreePath getParentPath(TreePath tp, Tree t) {
                    Tree parent;
                    
                    if (tp != null) {
                        while (tp.getLeaf().getKind() != Kind.COMPILATION_UNIT && getTreeUtilities().isSynthetic(tp)) {
                            tp = tp.getParentPath();
                        }
                        parent = tp.getLeaf();
                    } else {
                        parent = t;
                    }
                    TreePath c = tree2Path.get(parent);

                    if (c == null) {
                        c = tp != null ? tp : new TreePath((CompilationUnitTree) t);
                        tree2Path.put(parent, c);
                    }

                    return c;
                }

                /**
                 * True, if inside variable type or modifers part of a variable
                 * or field (not parameter). More efficient detection of field groups.
                 */
                private boolean beginVariableDeclarator;

                /**
                 * Will be set to non-null when traversing through variable, which
                 * is a part of variable group.
                 */
                private TreePath variableParent;
                
                private Tree     lastVariableItem;
                
                @Override
                public Void scan(Tree tree, Void p) {
                    boolean saveVarDec = beginVariableDeclarator;
                    boolean lastVar = false;
                    if (tree != null) {
                        List<? extends Tree> group;
                        
                        if (tree.getKind() == Tree.Kind.VARIABLE &&
                            (group = collectFieldGroup(WorkingCopy.this, 
                                    getCurrentPath(), tree)).size() > 1) {
                            // start of a variable, which is a part of a variable group
                            variableParent = getCurrentPath();
                            lastVar = group.get(group.size() - 1) == tree;
                        } else if (variableParent != null && getCurrentPath().getLeaf().getKind() == Tree.Kind.VARIABLE) {
                            VariableTree vt = (VariableTree)getCurrentPath().getLeaf();
                            beginVariableDeclarator = vt.getModifiers() == tree || vt.getType() == tree;
                        }
                    }
                    if (changes.containsKey(tree) || docChanges.containsKey(tree)) {
                        if (currentParent == null) {
                            if (beginVariableDeclarator) {
                                // use common variable group parent instead of computed parent,
                                // if rewriting inside common pat of a field group.
                                currentParent = variableParent;
                            } else {
                                currentParent = getParentPath(getCurrentPath(), tree);
                                if (currentParent.getParentPath() != null && currentParent.getParentPath().getLeaf().getKind() == Kind.COMPILATION_UNIT) {
                                    currentParent = currentParent.getParentPath();
                                }
                            }
                            pathsToRewrite.add(currentParent);
                            if (!parent2Rewrites.containsKey(currentParent)) {
                                parent2Rewrites.put(currentParent, new IdentityHashMap<Tree, Tree>());
                            }
                        }
                        Tree rev = changes.get(tree);
                        Tree hint = resolveRewriteHint(tree);
                        if(rev != null) {
                            Map<Tree, Tree> rewrites = parent2Rewrites.get(currentParent);

                            changes.remove(tree);

                            if (hint == null) {
                                addPresentInResult(presentInResult, rev, false);
                            }
                            //presentInResult.remove(tree);
                            rewrites.put(tree, rev);

                            scan(rev, p);
                        } else {
                            if (hint == null) {
                                addPresentInResult(presentInResult, rev, false);
                            }
                            addPresentInResult(presentInResult, tree, true);
                            super.scan(tree, p);
                        }
                    } else {
                        addPresentInResult(presentInResult, tree, true);
                        super.scan(tree, p);
                    }
                    if (currentParent != null && currentParent.getLeaf() == tree) {
                        currentParent = null;
                    }
                    if (lastVar) {
                        variableParent = null;
                    }
                    beginVariableDeclarator = saveVarDec;
                    return null;
                }

                @Override
                public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                    fqn.setCompilationUnit(node);
                    return super.visitCompilationUnit(node, p);
                }

                @Override
                public Void visitClass(ClassTree node, Void p) {
                    String parent = fqn.getFQN();
                    fqn.enterClass(node);
                    overlay.registerClass(parent, fqn.getFQN(), node, rewriteTarget.contains(node));
                    super.visitClass(node, p);
                    fqn.leaveClass();
                    return null;
                }

            }.scan(diffContext.origUnit, null);
        } else {
            TreePath topLevel = new TreePath(diffContext.origUnit);
            
            pathsToRewrite.add(topLevel);
            parent2Rewrites.put(topLevel, changes);
            fillImports = false;
        }
        
        final List<Diff> diffs = new ArrayList<Diff>();
        final ImportAnalysis2 ia = new ImportAnalysis2(this);
        
        boolean importsFilled = false;
        for (final TreePath path : pathsToRewrite) {
            List<ClassTree> classes = new ArrayList<ClassTree>();

            if (path.getParentPath() != null) {
                for (Tree t : path.getParentPath()) {
                    if (t.getKind() == Kind.COMPILATION_UNIT && !importsFilled) {
                        CompilationUnitTree cutt = (CompilationUnitTree) t;
                        ia.setCompilationUnit(cutt);
                        ia.setPackage(cutt.getPackageName());
                        ia.setImports(cutt.getImports());
                        importsFilled = true;
                    }
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                        classes.add((ClassTree) t);
                    }
                }
            } else if (path.getLeaf().getKind() == Kind.COMPILATION_UNIT && parent2Rewrites.get(path).size() == 1) { // XXX: not true if there are doc changes.
                //short-circuit import-only changes:
                CompilationUnitTree origCUT = (CompilationUnitTree) path.getLeaf();
                Tree nue = parent2Rewrites.get(path).get(origCUT);

                if (nue != null && nue.getKind() == Kind.COMPILATION_UNIT) {
                    CompilationUnitTree nueCUT = (CompilationUnitTree) nue;

                    if (   BaseUtilities.compareObjects(origCUT.getPackageAnnotations(), nueCUT.getPackageAnnotations())
                        && BaseUtilities.compareObjects(origCUT.getPackageName(), nueCUT.getPackageName())
                        && BaseUtilities.compareObjects(origCUT.getTypeDecls(), nueCUT.getTypeDecls())) {
                        fillImports = false;
                        diffs.addAll(CasualDiff.diff(getContext(), diffContext, getTreeUtilities(), origCUT.getImports(), nueCUT.getImports(), userInfo, tree2Tag, tree2Doc, tag2Span, oldTrees));
                        continue;
                    }
                }
            }

            Collections.reverse(classes);
            
            for (ClassTree ct : classes) {
                ia.classEntered(ct);
                ia.enterVisibleThroughClasses(ct);
            }
            final Map<Tree, Tree> rewrites = parent2Rewrites.get(path);
            
            ImmutableDocTreeTranslator itt = new ImmutableDocTreeTranslator(this) {
                private @NonNull Map<Tree, Tree> map = new HashMap<Tree, Tree>(rewrites);
                private @NonNull Map<DocTree, DocTree> docMap = null;
                private final TreeVisitor<Tree, Void> duplicator = new TreeDuplicator(getContext());

                @Override
                public Tree translate(Tree tree) {
                    if(docChanges.containsKey(tree)) {
                        Tree newTree = null;
                        if(!map.containsKey(tree)) {
                            Tree importComments = GeneratorUtilities.get(WorkingCopy.this).importComments(tree, getCompilationUnit());
                            newTree = importComments.accept(duplicator, null);
                            map.put(tree, newTree);
                        }
                        docMap = docChanges.remove(tree);
                        DocCommentTree newDoc;
                        if(docMap.size() == 1 && docMap.containsKey(null)) {
                            newDoc = (DocCommentTree) translate((DocCommentTree) docMap.get(null)); // Update QualIdent Trees
                        } else {
                            newDoc = (DocCommentTree) translate(((DocTrees)getTrees()).getDocCommentTree(new TreePath(path, tree)));
                        }
                        tree2Doc.put(tree, newDoc);
                        if(newTree != null && tree != newTree) {
                            tree2Doc.put(newTree, newDoc);
                        }
                    }
                    Tree translated = map.remove(tree);
                    
                    if(docChanges.containsKey(translated)) {
                        docMap = docChanges.remove(translated);
                        assert docMap.size() == 1;
                        assert docMap.containsKey(null);
                        DocCommentTree newDoc = (DocCommentTree) translate((DocCommentTree) docMap.get(null)); // Update QualIdent Trees
                        tree2Doc.put(translated, newDoc);
                    }

                    Tree t;
                    if (translated != null) {
                        t = translate(translated);
                    } else {
                        t = super.translate(tree);
                    }
                    if (tree2Doc != null && tree != t && tree2Doc.containsKey(tree)) {
                        tree2Doc.put(t, tree2Doc.remove(tree));
                    }
                    if (tree2Doc != null && translated != t && tree2Doc.containsKey(translated)) {
                        tree2Doc.put(t, tree2Doc.remove(translated));
                    }
                    return t;
                }
                
                @Override
                public DocTree translate(DocTree tree) {
                    if(docMap != null) {
                        DocTree translated = docMap.remove(tree);
                        if (translated != null) {
                            return translate(translated);
                        }
                    }
                    return super.translate(tree);
                }
            };
            Context c = impl.getJavacTask().getContext();
            itt.attach(c, ia, tree2Tag);
            final Tree brandNew = itt.translate(path.getLeaf());

            //tagging debug
            //System.err.println("brandNew=" + brandNew);
            new CommentReplicator(presentInResult.keySet()).process(diffContext.origUnit);
            addCommentsToContext(diffContext);
            for (ClassTree ct : classes) {
                ia.classLeft();
            }
            
            if (brandNew.getKind() == Kind.COMPILATION_UNIT) {
                fillImports = false;
            }
            
            diffs.addAll(
                    CasualDiff.diff(getContext(), diffContext, getTreeUtilities(), path, (JCTree) brandNew, 
                            userInfo, tree2Tag, tree2Doc, tag2Span, oldTrees
                    )
            );
        }
        
        List<Diff> additionalDiffs = new ArrayList<>();

        if (fillImports) {
            Set<? extends Element> nueImports = ia.getImports();

            if (nueImports != null && !nueImports.isEmpty()) { //may happen if no changes, etc.
                CompilationUnitTree ncut = GeneratorUtilities.get(this).addImports(diffContext.origUnit, nueImports);
                additionalDiffs.addAll(CasualDiff.diff(getContext(), diffContext, getTreeUtilities(), diffContext.origUnit.getImports(), ncut.getImports(), userInfo, tree2Tag, tree2Doc, tag2Span, oldTrees));
            }
        }
        
        // textual changes may affect tag2Span
        if (!textualChanges.isEmpty()) {
            additionalDiffs.addAll(textualChanges);
        }
        adjustTag2Span(tag2Span, additionalDiffs);
        diffs.addAll(additionalDiffs);
        
        userInfo.putAll(this.userInfo);
        
        try {
            return DiffUtilities.diff2ModificationResultDifference(diffContext.file, diffContext.positionConverter, userInfo, codeForCompilationUnit(diffContext.origUnit), diffs,
                    getFileObject() != null ? getSnapshot().getSource() : null);
        } catch (IOException ex) {
            if (!diffContext.file.isValid()) {
                Logger.getLogger(WorkingCopy.class.getName()).log(Level.FINE, null, ex);
                return Collections.emptyList();
            }
            throw ex;
        }
    }
    
    /**
     * Adjusts existing spans assigned to tags, using additional diffs. Must be called for all diffs computed
     * AFTER the tag2Span is populated.
     */
    private void adjustTag2Span(final Map<?, int[]> tag2Span, Collection<Diff> textChanges) {
        if (textChanges.isEmpty()) {
            return;
        }
        List<Diff> orderedDiffs = new ArrayList<>(textChanges);
        orderedDiffs.sort(new Comparator<Diff>() {
            @Override
            public int compare(Diff o1, Diff o2) {
                return o1.getPos() - o2.getPos();
            }
        });
        
        List<int[]> spans = new ArrayList<>(tag2Span.values());
        spans.sort(new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o1[0] - o2[0];
            }
        });
        
        int i = 0, j = 0;
        
        while (i < orderedDiffs.size() && j < spans.size()) {
            Diff d = orderedDiffs.get(i);
            int[] s = spans.get(j);
            
            if (d.getPos() >= s[1]) {
                i++;
            } else {
                // move the entire span
                int l;
                switch (d.type) {
                    case DELETE: {
                        l = d.getPos() - d.getEnd();
                        break;
                    }
                    case INSERT: {
                        l = d.getText().length();
                        break;
                    }
                    case MODIFY: {
                        l = d.getText().length() - (d.getEnd() - d.getPos());
                        break;
                    }
                    default:
                        throw new IllegalStateException();
                }
                if (d.getPos() <= s[0]) {
                    s[0] += l;
                }
                s[1] += l;
                j++;
                // the diff applies into the MIDDLE of the range, so 
            }
        }
    }
    
    private void addCommentsToContext(DiffContext context) {
        Map<Integer, Comment> m = new HashMap<>(usedComments.size());
        for (Comment c : usedComments) {
            m.put(c.pos(), c);
        }
        context.usedComments = m;
    }
    
    private void addPresentInResult(Map<Tree, Boolean> present, Tree t, boolean mark) {
        present.put(t, Boolean.valueOf(mark));
        CommentSetImpl csi = CommentHandlerService.instance(impl.getJavacTask().getContext()).getComments(t);
        for (RelativePosition pos : RelativePosition.values()) {
            useComments(csi.getComments(pos));
        }
    }
    
    private Set<Comment> usedComments;
    
    /* package-private */ List<Comment> useComments(List<Comment> comments) {
        if (usedComments == null) {
            usedComments = new HashSet<>();
        }
        usedComments.addAll(comments);
        return comments;
    }
    
    /**
     * Copies comments according to rewrite hints:<ul>
     * <li>copies comments from a tree to an associated tree
     * <li>
     */
    private class CommentReplicator extends ErrorAwareTreePathScanner<Object, Object> {
        private final Set<Tree>   stillPresent;
        
        private boolean collectCommentsFromRemovedNodes;
        private Map<Tree, Tree> copyTo = new IdentityHashMap<Tree, Tree>();
        private final CommentHandlerService commentHandler;
        private Tree parentToCopy;
        private Set<Comment>    retained = new HashSet<Comment>();
        
        CommentReplicator(Set<Tree> presentNodes) {
            this.stillPresent = presentNodes;
            this.commentHandler = CommentHandlerService.instance(impl.getJavacTask().getContext());
        }
        
        private void process(CompilationUnitTree unit) {
            commentHandler.freeze();
            try {
                scan(unit, null);
            } finally {
                commentHandler.unFreeze();
            }
        }
        
        private Object scanAndReduce(Tree node, Object p, Object r) {
            return reduce(scan(node, p), r);
        }
        
        private RelativePosition collectToPosition;

        /** Scan a list of nodes.
         */
        @Override
        public Object scan(Iterable<? extends Tree> nodes, Object p) {
            Object r = null;
            if (nodes != null) {
                boolean first = true;
                Tree saveParent = parentToCopy;
                RelativePosition savePosition = this.collectToPosition;
                collectToPosition = RelativePosition.INNER;
                if (collectCommentsFromRemovedNodes) {
                    // find first such node that it either survived, or has a mapping to the new state. Join all 
                    // comments to PRECEDING of such node.
                    for (Tree node : nodes) {
                        Tree target = resolveRewriteHint(node);
                        if (target != null) {
                            if (target != NOT_LINKED) {
                                parentToCopy = target;
                                collectToPosition = RelativePosition.PRECEDING;
                                break;
                            }
                        } else if (stillPresent.contains(node)) {
                            parentToCopy = node;
                            collectToPosition = RelativePosition.PRECEDING;
                            break;
                        }
                    }
                }
                boolean reset = false;
                for (Tree node : nodes) {
                    if (collectCommentsFromRemovedNodes) {
                        Tree target = resolveRewriteHint(node);
                        if (target != null && target != NOT_LINKED) {
                            parentToCopy = target;
                            this.collectToPosition = RelativePosition.INNER;
                            reset = true;
                        } else if (stillPresent.contains(node)) {
                            parentToCopy = node;
                            this.collectToPosition = RelativePosition.INNER;
                            reset = true;
                        }
                    }
                    r = (first ? scan(node, p) : scanAndReduce(node, p, r));
                    first = false;
                    // reset to trailing, output goes to the anchor node.
                    if (reset) {
                        this.collectToPosition = RelativePosition.TRAILING;
                    }
                }
                parentToCopy = saveParent;
                collectToPosition = savePosition;
            }
            return r;
        }

        @Override
        public Object scan(Tree l, Object p) {
            boolean collectChildren = false;
            Tree newParentCopy = null;
            
            boolean saveCollect = this.collectCommentsFromRemovedNodes;
            Tree target = resolveRewriteHint(l);
            if (target == NOT_LINKED) {
                // do not copy anything from this node and its children
                collectChildren = false;
            } else if (target != null) {
                if (!commentHandler.getComments(target).hasComments()) {
                    if (!stillPresent.contains(l)) {
                        commentHandler.copyComments(l, target, null, usedComments, false);
                        newParentCopy = target;
                        collectChildren = true;
                    }
                }
            } else if (!stillPresent.contains(l)) { // target == null, node removed
                collectChildren = collectCommentsFromRemovedNodes;
                if (collectCommentsFromRemovedNodes) {
                    if (parentToCopy != null) {
                        commentHandler.copyComments(l, parentToCopy, collectToPosition, usedComments, true);
                    }
                }
            }
            if (stillPresent.contains(l)) {
                newParentCopy = l;
            }
            Tree saveParent = parentToCopy;
            this.collectCommentsFromRemovedNodes = collectChildren;
            if (newParentCopy != null) {
                parentToCopy = newParentCopy;
            }
            Object v = super.scan(l, p);
            this.parentToCopy = saveParent;
            this.collectCommentsFromRemovedNodes = saveCollect;
            return v;
        }
    }
    
    private static class CopyEntry {
        private RelativePosition pos;
        private Tree commentSource;
        private boolean copyNonEmpty;
    }
    
    private Tree resolveRewriteHint(Tree orig) {
        Tree last;
        Tree target = null;
        Tree from = orig;
        do {
            last = target;
            target = from;
            target = rewriteHints.get(target);
            if (target == NOT_LINKED) {
                return target;
            }
            from = target;
        } while (target != null);
        return last;
    }
    
    private List<Difference> processExternalCUs(Map<?, int[]> tag2Span, Set<Tree> syntheticTrees) {
        if (externalChanges == null) {
            return Collections.<Difference>emptyList();
        }
        
        List<Difference> result = new LinkedList<Difference>();
        
        for (CompilationUnitTree t : externalChanges.values()) {
            try {
                FileObject targetFile = doCreateFromTemplate(t);
                CompilationUnitTree templateCUT = ParsingUtils.parseArbitrarySource(impl.getJavacTask(), FileObjects.sourceFileObject(targetFile, targetFile.getParent()));
                CompilationUnitTree importComments = GeneratorUtilities.get(this).importComments(templateCUT, templateCUT);

                rewrite(importComments, getTreeMaker().asRemoved(t));
                //changes.put(importComments, t);

                StringWriter target = new StringWriter();

                ModificationResult.commit(targetFile, processCurrentCompilationUnit(new DiffContext(this, templateCUT, codeForCompilationUnit(templateCUT), new PositionConverter(), targetFile, syntheticTrees, getFileObject() != null ? getCompilationUnit() : null, getFileObject() != null ? getText() : null), tag2Span), target);
                result.add(new CreateChange(t.getSourceFile(), target.toString()));
                target.close();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return result;
    }

    String template(ElementKind kind) {
        if(kind == null) {
            return "Templates/Classes/Empty.java"; // NOI18N
        }
        switch (kind) {
            case CLASS: return "Templates/Classes/Class.java"; // NOI18N
            case INTERFACE: return "Templates/Classes/Interface.java"; // NOI18N
            case ANNOTATION_TYPE: return "Templates/Classes/AnnotationType.java"; // NOI18N
            case ENUM: return "Templates/Classes/Enum.java"; // NOI18N
            case PACKAGE: return "Templates/Classes/package-info.java"; // NOI18N
            default:
                Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, "Cannot resolve template for {0}", kind);
                return "Templates/Classes/Empty.java"; // NOI18N
        }
    }

    FileObject doCreateFromTemplate(CompilationUnitTree cut) throws IOException {
        ElementKind kind;
        if ("package-info.java".equals(cut.getSourceFile().getName())) {
            kind = ElementKind.PACKAGE;
        } else if (cut.getTypeDecls().isEmpty()) {
            kind = null;
        } else {
            switch (cut.getTypeDecls().get(0).getKind()) {
                case CLASS:
                    kind = ElementKind.CLASS;
                    break;
                case INTERFACE:
                    kind = ElementKind.INTERFACE;
                    break;
                case ANNOTATION_TYPE:
                    kind = ElementKind.ANNOTATION_TYPE;
                    break;
                case ENUM:
                    kind = ElementKind.ENUM;
                    break;
                default:
                    Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, "Cannot resolve template for {0}", cut.getTypeDecls().get(0).getKind());
                    kind = null;
            }
        }
        FileObject template = FileUtil.getConfigFile(template(kind));
        return doCreateFromTemplate(template, cut.getSourceFile());
    }

    FileObject doCreateFromTemplate(FileObject template, JavaFileObject sourceFile) throws IOException {
        FileObject scratchFolder = FileUtil.createMemoryFileSystem().getRoot();
        String name = FileObjects.getName(sourceFile, false);

        if (template == null) {
            return FileUtil.createData(scratchFolder, name);
        }

        FileObjectFromTemplateCreator creator = Lookup.getDefault().lookup(FileObjectFromTemplateCreator.class);
        if (creator == null) {
            return FileUtil.createData(scratchFolder, name);
        }

        File pack = BaseUtilities.toFile(sourceFile.toUri()).getParentFile();

        while (FileUtil.toFileObject(pack) == null) {
            pack = pack.getParentFile();
        }

        FileObject targetFolder = FileUtil.toFileObject(pack);
        scratchFolder.setAttribute(FileObjectFromTemplateCreator.ATTR_ORIG_FILE, targetFolder);

        return creator.create(template, scratchFolder, name);
    }

    boolean invalidateSourceAfter = false;

    List<Difference> getChanges(Map<?, int[]> tag2Span) throws IOException, BadLocationException {
        if (afterCommit)
            throw new IllegalStateException("The commit method can be called only once on a WorkingCopy instance");   //NOI18N
        afterCommit = true;
        
        if (changes == null) {
            //may happen when the modification task does not call toPhase at all.
            return null;
        }
        
        if (externalChanges != null) {
            for (CompilationUnitTree t : externalChanges.values()) {
                final FQNComputer fqn = new FQNComputer();

                fqn.setCompilationUnit(t);
                overlay.registerPackage(fqn.getFQN());

                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        String parent = fqn.getFQN();
                        fqn.enterClass(node);
                        overlay.registerClass(parent, fqn.getFQN(), node, true);
                        super.visitClass(node, p);
                        fqn.leaveClass();
                        return null;
                    }
                }.scan(t, null);
            }
        }
        List<Difference> result = new LinkedList<Difference>();
        Set<Tree> syntheticTrees = new HashSet<Tree>();
        
        if (getFileObject() != null) {
            result.addAll(processCurrentCompilationUnit(new DiffContext(this, syntheticTrees), tag2Span));
        }
        
        result.addAll(processExternalCUs(tag2Span, syntheticTrees));

        overlay.clearElementsCache();

        if (invalidateSourceAfter) {
            Source source = impl.getSnapshot() != null ? impl.getSnapshot().getSource() : null;
            if (source != null) {
                SourceAccessor.getINSTANCE().invalidate(source, true);
            }
        }

        return result;
    }
    
    private void createCompilationUnit(JCTree.JCCompilationUnit unitTree) {
        if (externalChanges == null) externalChanges = new HashMap<JavaFileObject, CompilationUnitTree>();
        externalChanges.put(unitTree.getSourceFile(), unitTree);
        return;
    }
    
}
