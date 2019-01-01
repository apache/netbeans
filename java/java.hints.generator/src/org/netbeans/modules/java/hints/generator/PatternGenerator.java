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
package org.netbeans.modules.java.hints.generator;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.java.hints.generator.PatternGenerator.OfflineTree.Key;
import org.netbeans.modules.java.hints.generator.PatternGenerator.Result.Item;
import org.netbeans.modules.java.hints.generator.borrowed.JavaFixUtilities;
import org.netbeans.modules.java.hints.generator.borrowed.matching.Matcher;
import org.netbeans.modules.java.hints.generator.borrowed.matching.Occurrence;
import org.netbeans.modules.java.hints.generator.borrowed.matching.Pattern;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Union2;

/**
 *
 * @author lahvac
 */
public class PatternGenerator {

    public static final long CERTAIN = 1000;

    public static PatternGenerator record(Iterable<FileObject> sourceRoots, ProgressHandle progress, AtomicBoolean cancel) throws IOException {
        progress.start();
        progress.progress("Recording current state - determining files...");
        
        List<JavaSource> javaSource = new ArrayList<>();
        int totalFiles = 0;

        for (FileObject root : sourceRoots) {
            //XXX: use preprocessorbridge to get a custom copy of CompilationInfo
            Collection<FileObject> files = recursiveJavaFiles(root, cancel);
            if (files == null || cancel.get())
                return null;
            if (files.isEmpty()) {
                continue;
            }
            javaSource.add(JavaSource.create(ClasspathInfo.create(root), files));
            totalFiles += files.size();
        }

        progress.switchToDeterminate(totalFiles);
        progress.progress("Recording current state...");

        Map<FileObject, OfflineTree> file2Original = new HashMap<>();
        Map<Key, OfflineTree> treeCache = new HashMap<>();
        AtomicInteger done = new AtomicInteger();

        for (JavaSource js : javaSource) {
            js.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController parameter) throws Exception {
                    if (cancel.get())
                        return;
                    if (parameter.toPhase(Phase.UP_TO_DATE).compareTo(Phase.UP_TO_DATE) < 0)
                        return;
                    file2Original.put(parameter.getFileObject(), OfflineTree.of(parameter, new TreePath(parameter.getCompilationUnit()), treeCache, new IdentityHashMap<>()));
                    progress.progress(done.incrementAndGet());
                }
            }, false);
        }

        progress.finish();

        return new PatternGenerator(file2Original, treeCache);
    }

    private final Map<FileObject, OfflineTree> file2Original;
    private final Map<Key, OfflineTree> treeCache;
    private final Map<String, PatternDescription> patterns = new HashMap<>();

    private PatternGenerator(Map<FileObject, OfflineTree> file2Original, Map<Key, OfflineTree> treeCache) {
        this.file2Original = file2Original;
        this.treeCache = treeCache;
    }

    public Result updated(Collection<FileObject> sourceRoots, ProgressHandle progress, AtomicBoolean cancel) throws IOException {
        progress.start();
        progress.progress("Updating - determining files...");

        Pair<List<JavaSource>, Integer> sources = determineSources(sourceRoots, cancel);

        if (cancel.get())
            return null;

        progress.switchToDeterminate(2 * sources.second());
        progress.progress("Determining modified files...");

        PatternStatistics patternStatistics = new PatternStatistics();
        AtomicInteger done = new AtomicInteger();

        for (JavaSource js : sources.first()) {
            js.runModificationTask(new Task<WorkingCopy>() {
                @Override
                public void run(WorkingCopy parameter) throws Exception {
                    if (cancel.get())
                        return ;
                    if (parameter.toPhase(Phase.UP_TO_DATE).compareTo(Phase.UP_TO_DATE) < 0)
                        return;
                    patternStatistics.fileProcessingStarted(parameter.getFileObject());
                    try {
                        Map<Tree, OfflineTree> tree2Offline = new IdentityHashMap<>();
                        OfflineTree ot = OfflineTree.of(parameter, new TreePath(parameter.getCompilationUnit()), treeCache, tree2Offline);
                        diff(patterns, patternStatistics, file2Original.get(parameter.getFileObject()), parameter, tree2Offline); //XXX: should create the "done" items
                    } finally {
                        patternStatistics.fileProcessingFinished();
                    }
                    progress.progress(done.incrementAndGet());
                }
            });
            if (cancel.get())
                return null;
        }

        progress.progress("Applying changes...");

        try {
            return computeResult(sources.first(), patterns.values(), () -> progress.progress(done.incrementAndGet()), cancel);
        } finally {
            progress.finish();
        }
    }

    public static Pair<List<JavaSource>, Integer> determineSources(Collection<FileObject> sourceRoots, AtomicBoolean cancel) {
        List<JavaSource> javaSource = new ArrayList<>();
        int totalFiles = 0;

        for (FileObject root : sourceRoots) {
            //XXX: use preprocessorbridge to get a custom copy of CompilationInfo
            Collection<FileObject> files = recursiveJavaFiles(root, cancel);
            if (files == null || cancel.get())
                return null;
            if (files.isEmpty()) {
                continue;
            }
            javaSource.add(JavaSource.create(ClasspathInfo.create(root), files));
            totalFiles += files.size();
        }

        return Pair.of(javaSource, totalFiles);
    }

    public static Result computeResult(List<JavaSource> sources, Collection<PatternDescription> descs, Runnable progressTick, AtomicBoolean cancel) throws IOException {
        Map<PatternDescription, List<ItemDescription>> changes = new HashMap<>();

        for (JavaSource js : sources) {
            js.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController parameter) throws Exception {
                    if (cancel.get())
                        return ;
                    if (parameter.toPhase(Phase.UP_TO_DATE).compareTo(Phase.UP_TO_DATE) < 0)
                        return;
                    for (PatternDescription pd : descs) { //XXX: intermixed patterns
                        for (Occurrence occ : findMatches(parameter, pd)) {
                            TreePathHandle h = TreePathHandle.create(occ.getOccurrenceRoot(), parameter);
                            JavaSource current = JavaSource.forFileObject(parameter.getFileObject());
                            ModificationResult mr = current.runModificationTask(new Task<WorkingCopy>() {
                                @Override
                                public void run(WorkingCopy parameter) throws Exception {
                                    parameter.toPhase(Phase.UP_TO_DATE);

                                    Scope scope = Utilities.constructScope(parameter, Collections.emptyMap()); //XXX: constraints
                                    Tree  targetTree = Utilities.parseAndAttribute(parameter, pd.targetPattern, scope);
                                    TreePath original = h.resolve(parameter);

                                    parameter.rewrite(original.getLeaf(), targetTree);

                                    Map<String, TreePath> parameters = occ.getVariables();
                                    Map<String, Object> extraParamsData = new HashMap<>();
                                    Map<String, Collection<TreePath>> parametersMulti = new HashMap<>();
                                    Map<String, String> parameterNames = new HashMap<>();
                                    Map<Tree, Tree> rewriteFromTo = new HashMap<>();
                                    List<Tree> order = new ArrayList<>();
                                    Set<Tree> originalTrees = new HashSet<>();

                                    new JavaFixUtilities.ReplaceParameters(parameter, false, false, parameters, extraParamsData, parametersMulti, parameterNames, rewriteFromTo, order, originalTrees).scan(new TreePath(original, targetTree), null);

                                    for (Entry<Tree, Tree> rewriteEntry : rewriteFromTo.entrySet()) {
                                        parameter.rewrite(rewriteEntry.getKey(), rewriteEntry.getValue());
                                    }
                                }
                            });

                            changes.computeIfAbsent(pd, u -> new ArrayList<>()).add(new ItemDescription(mr, TreePathHandle.create(occ.getOccurrenceRoot(), parameter), occurrence2Fact(parameter, occ)));
                        }
                    }
                }
            }, true);
            if (cancel.get())
                return null;
        }

        return computeResult(changes);
    }

    public static Iterable<Occurrence> findMatches(CompilationInfo info, PatternDescription pd) {
        Tree  patternTree = Utilities.parseAndAttribute(info, pd.inputPattern, null);
        TreePath tp = new TreePath(new TreePath(info.getCompilationUnit()), patternTree);
        Pattern pattern = Pattern.createPatternWithFreeVariables(tp, Collections.emptyMap());
        Collection<? extends Occurrence> candidates = Matcher.create(info)
                                                             .setUntypedMatching()
                                                             .match(pattern);
        return () -> {
            return new Iterator<Occurrence>() {
                private final Iterator<? extends Occurrence> it = candidates.iterator();
                private Occurrence current;
                @Override public boolean hasNext() {
                    while (current == null && it.hasNext()) {
                        Occurrence occ = it.next();
                        if (!info.getTreeUtilities().isSynthetic(occ.getOccurrenceRoot())) {
                            current = occ;
                        }
                    }
                    return current != null;
                }
                @Override public Occurrence next() {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    Occurrence res = current;
                    current = null;
                    return res;
                }
            };
        };
    }

    public static void diff(Map<String, PatternDescription> patterns, PatternStatistics patternStatistics, OfflineTree original, WorkingCopy updated, Map<Tree, OfflineTree> tree2OfflineTree) {
        List<Union2<OfflineTree, Collection<? extends OfflineTree>>> originalQueue = new LinkedList<>();
        List<Union2<TreePath, Collection<? extends TreePath>>> updatedQueue = new LinkedList<>();

        originalQueue.add(Union2.<OfflineTree, Collection<? extends OfflineTree>>createFirst(original));
        updatedQueue.add(Union2.<TreePath, Collection<? extends TreePath>>createFirst(new TreePath(updated.getCompilationUnit())));

        while (!originalQueue.isEmpty() && !updatedQueue.isEmpty()) {
            Union2<OfflineTree, Collection<? extends OfflineTree>> originalU = originalQueue.remove(0);
            Union2<TreePath, Collection<? extends TreePath>> updatedU = updatedQueue.remove(0);

            assert originalU.hasFirst() == updatedU.hasFirst();

            if (originalU.hasFirst()) {
                if (diffTree(patterns, patternStatistics, originalU.first(), updated, updatedU.first(), tree2OfflineTree)) {
                    originalQueue.addAll(0, children(originalU.first()));
                    updatedQueue.addAll(0, children(updatedU.first()));
                }
            } else {
                //XXX: proper diffing!!
                if (originalU.second() != null && updatedU.second() != null &&//null; TODO: untested!
                    originalU.second().size() == updatedU.second().size()) { //XXX: hack - should be able to diff lists of different sizes
                    Iterator<? extends OfflineTree> originalTrees = originalU.second().iterator();
                    Iterator<? extends TreePath> updatedTrees = updatedU.second().iterator();

                    while (originalTrees.hasNext() && updatedTrees.hasNext()) {
                        OfflineTree originalTree = originalTrees.next();
                        TreePath updatedTree = updatedTrees.next();
                        if (diffTree(patterns, patternStatistics, originalTree, updated, updatedTree, tree2OfflineTree)) {
                            originalQueue.addAll(0, children(originalTree));
                            updatedQueue.addAll(0, children(updatedTree));
                        }
                    }

                    assert !originalTrees.hasNext() && !updatedTrees.hasNext();
                }
            }
        }
    }

    private static boolean diffTree(Map<String, PatternDescription> patterns, PatternStatistics patternStatistics, OfflineTree originalTree, final WorkingCopy updatedInfo, TreePath updatedPath, Map<Tree, OfflineTree> tree2OfflineTree) {
        if (originalTree == null || updatedPath == null || updatedPath.getParentPath() == null)
            return true;
        if (originalTree == tree2OfflineTree.get(updatedPath.getLeaf())) {
            return false;
        }
        int differingChildren = numberOfDifferingChildren(originalTree, updatedPath, tree2OfflineTree);
        Tree updated = updatedPath.getLeaf();
        if (differingChildren == 1) {
            if (originalTree.kind == updated.getKind()) {
                switch (originalTree.kind) {
                    case METHOD_INVOCATION:
                        ElementHandle<?> originalElement = originalTree.element;
                        Element updatedElement = updatedInfo.getTrees().getElement(updatedPath);

                        if (!Objects.equals(originalElement,
                                            ElementHandle.create(updatedElement)))
                            break;
                        return true;
                    default:
                        return true;
                }
            }
        }
        int start = (int) originalTree.start;
        int end = (int) originalTree.end;
        String originalText = originalTree.text.substring(start, end);
        final Tree originalReparsed = isStatement(originalTree.kind) ? updatedInfo.getTreeUtilities().parseStatement(originalText, new SourcePositions[1])
                                                                     : updatedInfo.getTreeUtilities().parseExpression(originalText, new SourcePositions[1]);
        Scope scope = constructScope(updatedInfo, updatedPath);
        updatedInfo.getTreeUtilities().attributeTree(originalReparsed, scope);

        final int[] replaceVars = new int[1];

        final Map<Tree, Tree> original2Variable = new HashMap<>();
        final Map<Tree, String> original2Remap = new HashMap<>();
        final Map<Tree, Tree> updated2Variable = new HashMap<>();
        Map<Tree, Set<String>> handledEquivalences = new HashMap<>();
        Fact.Builder factBuilder = Fact.Builder.create();

        new TreePathScanner<Void, Void>() {
            int treeIndex = 0;
            @Override
            public Void scan(Tree node, Void p) {
                boolean canReplace = canReplace(getCurrentPath(), node);

                if (canReplace) {
                    TreePath currentPath = new TreePath(getCurrentPath(), node);
                    Collection<? extends Occurrence> matches =
                            Matcher.create(updatedInfo)
                                   .setSearchRoot(updatedPath)
                                   .match(Pattern.createSimplePattern(currentPath));

                    if (!matches.isEmpty()) {
                        String currentVar = "$" + ++replaceVars[0];
                        ExpressionTree varExpr = updatedInfo.getTreeMaker().Identifier(currentVar);
                        Tree var = isStatement(node.getKind()) ? updatedInfo.getTreeMaker().ExpressionStatement(varExpr) : varExpr;
                        original2Variable.put(node, var);
                        original2Remap.put(node, currentVar);
                        for (Occurrence occ : matches) {
                            updated2Variable.put(occ.getOccurrenceRoot().getLeaf(), var);
                        }
                        factBuilder.add(updatedInfo, currentVar, currentPath);
                        handleVariableEquivalenceGroups(factBuilder, 
                                                        updatedInfo,
                                                        new TreePath(updatedPath.getParentPath(), originalReparsed),
                                                        currentPath,
                                                        currentVar,
                                                        handledEquivalences);
                        return null;
                    }

                    recordPossibleMethodInvocation(node, currentPath);
                }
                return super.scan(node, p);
            }
            @Override
            public Void scan(TreePath path, Void p) {
                recordPossibleMethodInvocation(path.getLeaf(), path);
                return super.scan(path, p);
            }
            private void recordPossibleMethodInvocation(Tree node, TreePath currentPath) {
                if (node.getKind() == Tree.Kind.METHOD_INVOCATION) {
                    Element el = updatedInfo.getTrees().getElement(currentPath);
                    if (el != null) {
                        factBuilder.addMethodInvocation(updatedInfo, treeIndex, el);
                    }
                }

                treeIndex++;
            }
        }.scan(new TreePath(updatedPath.getParentPath(), originalReparsed), null);

        for (Entry<Tree, String> e : original2Remap.entrySet()) {
            new TreeScanner<Void, Void>() {
                @Override
                public Void scan(Tree tree, Void p) {
                    if (handledEquivalences.containsKey(tree)) {
                        handledEquivalences.get(tree).add(e.getValue());
                    }
                    return super.scan(tree, p);
                }
            }.scan(e.getKey(), null);
        }

        //expand to FQNs:
        new TreePathScanner<Void, Void>() {
            @Override public Void visitMemberSelect(MemberSelectTree node, Void p) {
                if (handleNode())
                    return super.visitMemberSelect(node, p);
                return null;
            }
            @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                if (handleNode())
                    return super.visitIdentifier(node, p);
                return null;
            }
            @Override public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                Element el = updatedInfo.getTrees().getElement(getCurrentPath());
                if (node.getMethodSelect().getKind() == Tree.Kind.IDENTIFIER &&
                    el != null && el.getModifiers().contains(Modifier.STATIC)) {
                    String methodFullName = ((TypeElement) el.getEnclosingElement()).getQualifiedName() + "." + el.getSimpleName();
                    updated2Variable.put(node.getMethodSelect(), qualIdent(updatedInfo.getTreeMaker(), methodFullName));
                }
                return super.visitMethodInvocation(node, p);
            }
            private boolean handleNode() {
                Element el = updatedInfo.getTrees().getElement(getCurrentPath());
                if (el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
                    updated2Variable.put(getCurrentPath().getLeaf(), qualIdent(updatedInfo.getTreeMaker(), ((TypeElement) el).getQualifiedName()));
                    return false;
                }
                return true;
            }
        }.scan(updatedPath, null);

        Tree generalizedOriginal = updatedInfo.getTreeUtilities().translate(originalReparsed, original2Variable);
        Tree generalizedUpdated = updatedInfo.getTreeUtilities().translate(updated, updated2Variable);
        String originalPattern = generalizedOriginal.toString();

        PatternDescription pd = patterns.computeIfAbsent(originalPattern, input -> new PatternDescription(input, generalizedUpdated.toString()));

        pd.positiveFacts.add(factBuilder.build());

        patternStatistics.patternFactRecorded(originalPattern, updated);
        
        return false;
    }
        private static int numberOfDifferingChildren(OfflineTree originalTree, TreePath updatedPath, Map<Tree, OfflineTree> tree2OfflineTree) {
            Iterator<Union2<OfflineTree, Collection<? extends OfflineTree>>> originalIt = originalTree.children.iterator();
            Iterator<Union2<TreePath, Collection<? extends TreePath>>> updatedIt = children(updatedPath).iterator();
            int c = 0;
            while (originalIt.hasNext() && updatedIt.hasNext()) {
                Union2<OfflineTree, Collection<? extends OfflineTree>> originalC = originalIt.next();
                Union2<TreePath, Collection<? extends TreePath>> updatedC = updatedIt.next();

                Collection<? extends OfflineTree> originalList;
                Collection<? extends TreePath> updatedList;
                if (originalC.hasFirst()) {
                    originalList = Collections.singletonList(originalC.first());
                    updatedList = Collections.singletonList(updatedC.first());
                } else {
                    originalList = originalC.second();
                    updatedList = updatedC.second();
                }

                if (originalList.size() != updatedList.size()) {
                    c++;
                    continue;
                }

                Iterator<? extends OfflineTree> originalListIt = originalList.iterator();
                Iterator<? extends TreePath> updatedListIt = updatedList.iterator();

                while (originalListIt.hasNext() && updatedListIt.hasNext()) {
                    OfflineTree original = originalListIt.next();
                    TreePath updated = updatedListIt.next();
                    if ((original == null ^ updated == null) || (updated != null && original != tree2OfflineTree.get(updated.getLeaf()))) {
                        c++;
                    }
                }
            }

            return c;
        }

    private static boolean isStatement(Kind k) {
        return StatementTree.class.isAssignableFrom(k.asInterface());
    }
    private static Collection<Union2<OfflineTree, Collection<? extends OfflineTree>>> children(OfflineTree t) {
        if (t == null)
            return Collections.emptyList();
        return t.children;
    }

    private static List<Union2<TreePath, Collection<? extends TreePath>>> children(TreePath t) {
        if (t == null)
            return Collections.emptyList();
        final List<Union2<TreePath, Collection<? extends TreePath>>> result = new ArrayList<>();
        t.getLeaf().accept(new TreeScanner<Void, Void>() {
            @Override
            public Void scan(Tree node, Void p) {
                result.add(Union2.<TreePath, Collection<? extends TreePath>>createFirst(node != null ? new TreePath(t, node) : null));
                return null;
            }
            @Override
            public Void scan(Iterable<? extends Tree> nodes, Void p) {
                List<TreePath> resultPaths;
                if (nodes != null) {
                    resultPaths = new ArrayList<>();
                    for (Tree node : nodes) {
                        resultPaths.add(new TreePath(t, node));
                    }
                } else {
                    resultPaths = null;
                }
                result.add(Union2.<TreePath, Collection<? extends TreePath>>createSecond(resultPaths));
                return null;
            }
            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                scan(node.getPackageName(), p);
                scan(node.getTypeDecls(), p);
                return null;
            }
        }, null);
        return result;
    }

    private static ExpressionTree qualIdent(TreeMaker make, CharSequence qualifiedName) {
        ExpressionTree previous = null;
        for (String part : qualifiedName.toString().split("\\.")) {
            if (previous == null) {
                previous = make.Identifier(part);
            } else {
                previous = make.MemberSelect(previous, part);
            }
        }
        return previous;
    }

    public static Fact occurrence2Fact(CompilationInfo info, Occurrence occ) {
        Fact.Builder currentFactBuilder = Fact.Builder.create();
        for (Entry<String, TreePath> varEntry : occ.getVariables().entrySet()) {
            currentFactBuilder.add(info, varEntry.getKey(), varEntry.getValue());
        }

        Map<Tree, String> varTrees = occ.getVariables().entrySet()
                                                       .stream()
                                                       .collect(Collectors.toMap(e -> e.getValue().getLeaf(),e -> e.getKey()));
        Map<Tree, Set<String>> handledEquivalences = new HashMap<>();

        new TreePathScanner<Void, Void>() {
            int treeIndex = 0;
            @Override
            public Void scan(Tree node, Void p) {
                boolean canReplace = canReplace(getCurrentPath(), node);

                if (canReplace) {
                    String varName = varTrees.get(node);
                    if (varName != null) {
                        handleVariableEquivalenceGroups(currentFactBuilder,
                                                        info,
                                                        occ.getOccurrenceRoot(),
                                                        new TreePath(getCurrentPath(), node),
                                                        varName,
                                                        handledEquivalences);
                        return null;
                    }
                    recordPossibleMethodInvocation(node, new TreePath(getCurrentPath(), node));
                }
                return super.scan(node, p);
            }
            @Override
            public Void scan(TreePath path, Void p) {
                recordPossibleMethodInvocation(path.getLeaf(), path);
                return super.scan(path, p);
            }
            private void recordPossibleMethodInvocation(Tree node, TreePath currentPath) {
                if (node.getKind() == Tree.Kind.METHOD_INVOCATION) {
                    currentFactBuilder.addMethodInvocation(info, treeIndex, info.getTrees().getElement(currentPath));
                }

                treeIndex++;
            }
        }.scan(occ.getOccurrenceRoot(), null);

        for (Entry<Tree, String> e : varTrees.entrySet()) {
            new TreeScanner<Void, Void>() {
                @Override
                public Void scan(Tree tree, Void p) {
                    if (handledEquivalences.containsKey(tree)) {
                        handledEquivalences.get(tree).add(e.getValue());
                    }
                    return super.scan(tree, p);
                }
            }.scan(e.getKey(), null);
        }

        return currentFactBuilder.build();
    }
    
    public static Pair<Result.Kind, Long> estimate(PatternDescription pd, Fact currentFact) {
        long bestCertainty = 0;
        boolean someFactMatchesMethods = false;
        Set<String> variables = pd.positiveFacts.iterator().next().variable2Type.keySet(); //XXX
        Map<String, VariableDescription> variable2PositiveDesc = gatherDescriptions(pd.positiveFacts, variables);
        Map<String, VariableDescription> variable2NegativeDesc = gatherDescriptions(pd.negativeFacts, variables);
        Map<String, Double> typeRatio = new HashMap<>();
        Map<String, Double> treeNodeRatio = new HashMap<>();
        for (String var : variables) {
            typeRatio.put(var, ((double) variable2PositiveDesc.get(var).types.size()) / (variable2PositiveDesc.get(var).types.size() + variable2NegativeDesc.get(var).types.size() + 1));
            treeNodeRatio.put(var, ((double) variable2PositiveDesc.get(var).treeNodes.size()) / (variable2PositiveDesc.get(var).treeNodes.size() + variable2NegativeDesc.get(var).treeNodes.size() + 1));
        }
        //check equivalence groups:
        Map<List<Set<String>>, Integer> allDistinctGroups = new HashMap<>();
        for (Fact positive : pd.positiveFacts) {
            allDistinctGroups.put(positive.variableEquivalenceGroups,
                                  allDistinctGroups.getOrDefault(positive.variableEquivalenceGroups, 0) + 1);
        }
        boolean matchesVariableEquivalenceGroup = allDistinctGroups.containsKey(currentFact.variableEquivalenceGroups);
        double downgradeProbability = 1.0;
        if (!matchesVariableEquivalenceGroup) {
            downgradeProbability *= allDistinctGroups.size() / (allDistinctGroups.size() + 1.0);
            downgradeProbability *= 1.0 / allDistinctGroups.values().stream().collect(Collectors.minBy((i1, i2) -> i1 - i2)).get();
        }
        OUTER: for (Fact positive : pd.positiveFacts) {
            if (currentFact.methodInvocation2Method.size() != positive.methodInvocation2Method.size()) {
                //TODO: should this be a blocker?
                continue;
            }
            for (Entry<Integer, String[]> method : currentFact.methodInvocation2Method.entrySet()) {
                if (!Arrays.equals(method.getValue(), positive.methodInvocation2Method.get(method.getKey()))) {
                    //TODO: this should not be a blocker, and difference in overloads should have smaller uncertainty than a completely different method
                    continue OUTER;
                }
            }
            someFactMatchesMethods = true;
            int matching = 0;
            int total = 0;
            double probability = downgradeProbability;
            for (String variable : currentFact.variable2Type.keySet()) {
                total += 2;
                if (currentFact.variable2Type.get(variable).equals(positive.variable2Type.get(variable))) {
                    matching++;
                } else {
                    probability *= typeRatio.get(variable);
                }
                if (currentFact.variable2TreeNode.get(variable).equals(positive.variable2TreeNode.get(variable))) {
                    matching++;
                }  else {
                    probability *= treeNodeRatio.get(variable);
                }
            }
            if (matching == total && matchesVariableEquivalenceGroup) {
                return Pair.of(Result.Kind.POSITIVE, CERTAIN);
            }
            bestCertainty = Math.max(bestCertainty, (long) (CERTAIN * probability));
        }
        OUTER: for (Fact negative : pd.negativeFacts) {
            for (Entry<String, String> currentFactEntry : currentFact.variable2Type.entrySet()) {
                if (!currentFactEntry.getValue().equals(negative.variable2Type.get(currentFactEntry.getKey()))) {
                    continue OUTER;
                }
            }
            for (Entry<String, TreeNodeDesc> currentFactEntry : currentFact.variable2TreeNode.entrySet()) {
                if (!currentFactEntry.getValue().equals(negative.variable2TreeNode.get(currentFactEntry.getKey()))) {
                    continue OUTER;
                }
            }
            //TODO: better diagnostics?
            return Pair.of(Result.Kind.NEGATIVE, CERTAIN);
        }
        if (!someFactMatchesMethods) {
            //TODO: see above - even if the (static) method invocations did not match exactly,
            return Pair.of(Result.Kind.NEGATIVE, CERTAIN);
        }
        //optimistically - rewrite everything unless we know we shouldn't :-)
        return Pair.of(Result.Kind.POSITIVE, bestCertainty);
    }

    private static Map<String, VariableDescription> gatherDescriptions(Iterable<? extends Fact> facts, Set<String> variables) {
        Map<String, VariableDescription> result = new HashMap<>();

        for (String var : variables) {
            VariableDescription varDesc = result.computeIfAbsent(var, VariableDescription :: new);
            for (Fact fact : facts) {
                varDesc.add(fact);
            }

        }
        return result;
    }

    private static boolean canReplace(TreePath currentPath, Tree node) {
        boolean canReplace = node != null;

        canReplace = canReplace && (isStatement(node.getKind()) || currentPath.getLeaf().getKind() != Kind.EXPRESSION_STATEMENT);

        return canReplace;
    }

    private static void handleVariableEquivalenceGroups(Fact.Builder factBuilder, CompilationInfo info, TreePath root, TreePath currentPath, String currentVar, Map<Tree, Set<String>> handledEquivalences) {
        Set<String> group = handledEquivalences.get(currentPath.getLeaf());
        if (group == null) {
            group = new HashSet<>();
            factBuilder.addVariableEquivalenceGroup(group);

            Collection<? extends Occurrence> eqMatches =
                    Matcher.create(info)
                           .setSearchRoot(root)
                           .match(Pattern.createSimplePattern(currentPath));

            for (Occurrence eqOcc : eqMatches) {
                handledEquivalences.put(eqOcc.getOccurrenceRoot().getLeaf(), group);
            }
        }
        group.add(currentVar);
    }

    private static final class VariableDescription {
        private final String variable;
        public final Set<String> types = new HashSet<>();
        public final Set<TreeNodeDesc> treeNodes = new HashSet<>();

        public VariableDescription(String variable) {
            this.variable = variable;
        }

        public void add(Fact f) {
            String type = f.variable2Type.get(variable);
            if (type != null)
                types.add(type);
            TreeNodeDesc treeNode = f.variable2TreeNode.get(variable);
            if (treeNode != null)
                treeNodes.add(treeNode);
        }
    }

    private static Result computeResult(Map<PatternDescription, List<ItemDescription>> changes) {
        List<Item> result = new ArrayList<>();
        for (Entry<PatternDescription, List<ItemDescription>> patternAndChange : changes.entrySet()) {
            for (ItemDescription itemDescription : patternAndChange.getValue()) {
                Pair<Result.Kind, Long> estimate = estimate(patternAndChange.getKey(), itemDescription.fact);
                result.add(new Item(estimate.first(), estimate.second(), itemDescription.diff, patternAndChange.getKey(), itemDescription.location, itemDescription.fact));
            }
        }
        return new Result(result);
    }

    private static String type2String(CompilationInfo info, TypeMirror type) {
        return type.toString();
    }

    public static Result updatePositive(Result previousResult, Item accepted) {
        Map<PatternDescription, List<ItemDescription>> changes = new HashMap<>();

        for (Item item : previousResult.changes) {
            changes.computeIfAbsent(item.pd, u -> new ArrayList<>()).add(new ItemDescription(item.diffs, item.location, item.fact));
        }

        accepted.pd.positiveFacts.add(accepted.fact);

        return computeResult(changes);
    }

    public static Result updateNegative(Result previousResult, Item notAccepted) {
        Map<PatternDescription, List<ItemDescription>> changes = new HashMap<>();

        for (Item item : previousResult.changes) {
            changes.computeIfAbsent(item.pd, u -> new ArrayList<>()).add(new ItemDescription(item.diffs, item.location, item.fact));
        }

        notAccepted.pd.negativeFacts.add(notAccepted.fact);
        
        return computeResult(changes);
    }

    public String getScript() {
        StringBuilder sb = new StringBuilder();

        for (PatternDescription pd : patterns.values()) {
            sb.append(pd.toScript());
        }

        return sb.toString();
    }

    private static FileObject fakeJavaSource;

    static {
        try {
            fakeJavaSource = FileUtil.createMemoryFileSystem().getRoot().createData("Fake", "java"); //to prevent partial reparse
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private static Collection<FileObject> recursiveJavaFiles(FileObject sourceRoot, AtomicBoolean cancel) {
        Collection<FileObject> result = new ArrayList<>();
        Enumeration<? extends FileObject> files = sourceRoot.getChildren(true);

        while (files.hasMoreElements()) {
            if (cancel.get())
                return null;
            FileObject file = files.nextElement();
            if ("java".equals(file.getExt())) //XXX: should use mime types!
                result.add(file);
        }

        result.add(fakeJavaSource);
        
        return result;
    }

    public static void doRefactoring(Result result) throws IOException {
        //XXX: speed
        Map<FileObject, List<Item>> file2Items = result.changes.stream()
                                                               .filter(i -> i.kind == Result.Kind.POSITIVE)
                                                               .collect(Collectors.groupingBy(i -> i.diffs.getModifiedFileObjects().iterator().next()));

        for (Entry<FileObject, List<Item>> changeEntry : file2Items.entrySet()) {
            JavaSource js = JavaSource.forFileObject(changeEntry.getKey());
            js.runModificationTask(new Task<WorkingCopy>() {
                @Override
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.UP_TO_DATE);
//                    if (parameter.toPhase(Phase.UP_TO_DATE).compareTo(Phase.UP_TO_DATE) < 0)
//                        return;
                    for (Item i : changeEntry.getValue()) { //TODO: speed
                        Tree  patternTree = Utilities.parseAndAttribute(parameter, i.pd.inputPattern, null);
                        TreePath tp = new TreePath(new TreePath(parameter.getCompilationUnit()), patternTree);
                        Pattern pattern = Pattern.createPatternWithFreeVariables(tp, Collections.emptyMap());
                        for (Occurrence occ : Matcher.create(parameter)
                                                     .setUntypedMatching()
                                                     .match(pattern)) {
                            if (occ.getOccurrenceRoot().getLeaf() != i.location.resolve(parameter).getLeaf())
                                continue;
                            Scope scope = Utilities.constructScope(parameter, Collections.emptyMap()); //XXX: constraints
                            Tree  targetTree = Utilities.parseAndAttribute(parameter, i.pd.targetPattern, scope);

                            parameter.rewrite(occ.getOccurrenceRoot().getLeaf(), targetTree);

                            Map<String, TreePath> parameters = occ.getVariables();
                            Map<String, Object> extraParamsData = new HashMap<>();
                            Map<String, Collection<TreePath>> parametersMulti = new HashMap<>();
                            Map<String, String> parameterNames = new HashMap<>();
                            Map<Tree, Tree> rewriteFromTo = new HashMap<>();
                            List<Tree> order = new ArrayList<>();
                            Set<Tree> originalTrees = new HashSet<>();

                            new JavaFixUtilities.ReplaceParameters(parameter, false, false, parameters, extraParamsData, parametersMulti, parameterNames, rewriteFromTo, order, originalTrees).scan(new TreePath(occ.getOccurrenceRoot(), targetTree), null);

                            for (Entry<Tree, Tree> rewriteEntry : rewriteFromTo.entrySet()) {
                                parameter.rewrite(rewriteEntry.getKey(), rewriteEntry.getValue());
                            }
                        }
                    }
                }
            }).commit();
        }
    }

    private static Scope constructScope(CompilationInfo info, TreePath tp) {
        TreePath methodPath = tp;

        while (methodPath != null && methodPath.getLeaf().getKind() != Kind.METHOD) //todo: initializers
            methodPath = methodPath.getParentPath();

        if (methodPath == null) {
            return info.getTrees().getScope(tp);
        }

        JavacScope methodScope = (JavacScope) info.getTrees().getScope(methodPath);
        Env<AttrContext> methodEnv = methodScope.getEnv();
        AttrContext attrContext = methodEnv.info;

        try {
            Method dupMethod = attrContext.getClass().getDeclaredMethod("dup", com.sun.tools.javac.code.Scope.WriteableScope.class);
            Field scopeField = attrContext.getClass().getDeclaredField("scope");

            dupMethod.setAccessible(true);
            scopeField.setAccessible(true);

            com.sun.tools.javac.code.Scope.WriteableScope javacScope = ((com.sun.tools.javac.code.Scope.WriteableScope) scopeField.get(attrContext)).dupUnshared();

            new TreePathScanner<Void, Void>() {
                private List<VarSymbol> variables = new ArrayList<>();
                @Override
                public Void visitBlock(BlockTree node, Void p) {
                    int count = variables.size();
                    try {
                        return super.visitBlock(node, p);
                    } finally {
                        variables.subList(count, variables.size()).clear();
                    }
                }
                @Override
                public Void visitVariable(VariableTree node, Void p) {
                    variables.add((VarSymbol) info.getTrees().getElement(getCurrentPath()));
                    return super.visitVariable(node, p);
                }
                @Override
                public Void scan(Tree tree, Void p) {
                    if (tree == tp.getLeaf()) {
                        for (VarSymbol var : variables) {
                            javacScope.enter(var);
                        }
                        //TODO: performance, skip the rest of the AST
                    }
                    return super.scan(tree, p);
                }
            }.scan(new TreePath(methodPath, ((MethodTree) methodPath.getLeaf()).getBody()), null);

            Method createMethod = JavacScope.class.getDeclaredMethod("create", Env.class);
            createMethod.setAccessible(true);
            return (Scope) createMethod.invoke(null, methodEnv.dup(methodEnv.tree, (AttrContext) dupMethod.invoke(methodEnv.info, javacScope)));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return info.getTrees().getScope(tp);
        }
    }

    public static final class PatternDescription {
        private final String inputPattern;
        private final Set<Fact> positiveFacts = new HashSet<>();
        private final Set<Fact> negativeFacts = new HashSet<>();
        private final String targetPattern;

        public PatternDescription(String inputPattern, String targetPattern) {
            this.inputPattern = inputPattern;
            this.targetPattern = targetPattern;
        }

        public String toScript() {
//            StringBuilder script = new StringBuilder();
//
//            script.append(inputPattern);
//
//            String delim = " :: ";
//
//            for (Entry<String, Constraints> e : constraints.entrySet()) {
//                script.append(delim);
//                script.append(e.getKey());
//                script.append(" instanceof ");
//                script.append(e.getValue());
//                delim = " && ";
//            }
//
//            script.append(" => ");
//            script.append(targetPattern);
//            script.append(";;");
//
//            return script.toString();
            throw new IllegalStateException();
        }

        public String getInputPattern() {
            return inputPattern;
        }

        public Set<Fact> getPositiveFacts() {
            return positiveFacts;
        }

        public Set<Fact> getNegativeFacts() {
            return negativeFacts;
        }

        public String getTargetPattern() {
            return targetPattern;
        }

    }

    public static class Fact {
        //TODO: how to handle subtypes?
        private final Map<String, String> variable2Type;
        private final Map<String, TreeNodeDesc> variable2TreeNode;
        public final Map<Integer, String[]> methodInvocation2Method;
        public final List<Set<String>> variableEquivalenceGroups; //TODO: should be a set?

        private Fact(Map<String, String> variable2Type, Map<String, TreeNodeDesc> variable2TreeNode, Map<Integer, String[]> methodInvocation2Method, List<Set<String>> variableEquivalenceGroups) {
            this.variable2Type = variable2Type;
            this.variable2TreeNode = variable2TreeNode;
            this.methodInvocation2Method = methodInvocation2Method;
            this.variableEquivalenceGroups = variableEquivalenceGroups;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + Objects.hashCode(this.variable2Type);
            hash = 41 * hash + Objects.hashCode(this.variable2TreeNode);
            hash = 41 * hash + Objects.hashCode(this.methodInvocation2Method);
            hash = 41 * hash + Objects.hashCode(this.variableEquivalenceGroups);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Fact other = (Fact) obj;
            if (!Objects.equals(this.variable2Type, other.variable2Type)) {
                return false;
            }
            if (!Objects.equals(this.variable2TreeNode, other.variable2TreeNode)) {
                return false;
            }
            if (!Objects.equals(this.methodInvocation2Method, other.methodInvocation2Method)) {
                return false;
            }
            if (!Objects.equals(this.variableEquivalenceGroups, other.variableEquivalenceGroups)) {
                return false;
            }
            return true;
        }

        public int getVariableCount() {
            return variable2Type.size();
        }

        private static class Builder {

            public static Builder create() {
                return new Builder();
            }
            
            private final Map<String, String> variable2Type = new HashMap<>();
            private final Map<String, TreeNodeDesc> variable2TreeNode = new HashMap<>();
            private final Map<Integer, String[]> methodInvocation2Method = new HashMap<>();
            private final List<Set<String>> variableEquivalenceGroups = new ArrayList<>();

            public Builder add(CompilationInfo info, String variableName, TreePath path) {
                TypeMirror type = info.getTrees().getTypeMirror(path);
                if (type != null) {
                    variable2Type.put(variableName, type2String(info, type));
                }
                variable2TreeNode.put(variableName, new TreeNodeDesc(info, path));

                return this;
            }

            public Builder addMethodInvocation(CompilationInfo info, int methodInvocationIndex, Element method) {
                methodInvocation2Method.put(methodInvocationIndex, SourceUtils.getJVMSignature(ElementHandle.create(method)));
                
                return this;
            }

            public Builder addVariableEquivalenceGroup(Set<String> group) {
                variableEquivalenceGroups.add(Collections.unmodifiableSet(group));

                return this;
            }

            public Fact build() {
                return new Fact(Collections.unmodifiableMap(variable2Type),
                                Collections.unmodifiableMap(variable2TreeNode),
                                Collections.unmodifiableMap(methodInvocation2Method),
                                Collections.unmodifiableList(variableEquivalenceGroups));
            }
        }
    }

    private static class TreeNodeDesc {
        private final Tree.Kind kind;
        private final ElementHandle<?> used;

        public TreeNodeDesc(CompilationInfo info, TreePath path) {
            this.kind = path.getLeaf().getKind();
            if (this.kind == Tree.Kind.METHOD_INVOCATION) { //TODO: constructors (etc.)
                this.used = ElementHandle.create(info.getTrees().getElement(path));
            } else {
                this.used = null;
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + Objects.hashCode(this.kind);
            hash = 73 * hash + Objects.hashCode(this.used);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TreeNodeDesc other = (TreeNodeDesc) obj;
            if (this.kind != other.kind) {
                return false;
            }
            if (!Objects.equals(this.used, other.used)) {
                return false;
            }
            return true;
        }

    }

    public static final class Result {

        public final List<Item> changes;

        public Result(List<Item> changes) {
            this.changes = changes;
        }

        public enum Kind {
            DONE,
            POSITIVE,
            NEGATIVE;
        }

        public static final class Item {
            public final Kind kind;
            public final long certainty;
            public final ModificationResult diffs;
            private final PatternDescription pd;
            private final TreePathHandle location; //XXX
            private final Fact fact;

            private Item(Kind kind, long certainty, ModificationResult diffs, PatternDescription pd, TreePathHandle location, Fact fact) {
                this.kind = kind;
                this.certainty = certainty;
                this.diffs = diffs;
                this.pd = pd;
                this.location = location;
                this.fact = fact;
            }

            @Override
            public String toString() {
                return "Item{" + "kind=" + kind + ", certainty=" + certainty + ", diffs=" + diffs + ", fact=" + fact + '}';
            }

        }
    }

    private static final class ItemDescription {

        private final ModificationResult diff;
        private final TreePathHandle location; //XXX
        private final Fact fact;

        public ItemDescription(ModificationResult diff, TreePathHandle location, Fact fact) {
            this.diff = diff;
            this.location = location;
            this.fact = fact;
        }

    }

    public static final class OfflineTree {
        public final Tree.Kind kind;
        public final ElementHandle<?> element;
        public final Collection<Union2<OfflineTree, Collection<? extends OfflineTree>>> children;
        public final long start;
        public final long end;
        public final String text;

        private OfflineTree(Kind kind, ElementHandle<?> element, Collection<Union2<OfflineTree, Collection<? extends OfflineTree>>> children, long start, long end, String text) {
            this.kind = kind;
            this.element = element;
            this.children = children;
            this.start = start;
            this.end = end;
            this.text = text;
        }

        public static OfflineTree of(CompilationInfo info, TreePath path, Map<Key, OfflineTree> treeCache, Map<Tree, OfflineTree> tree2Offline) {
            if (path == null)
                return null;

            OfflineTreeProducer producer = new OfflineTreeProducer(treeCache);
            return producer.compute(info, path, tree2Offline);
        }

        public static final class Key {
            private final byte[] hash;

            public Key(byte[] hash) {
                this.hash = hash;
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash ^= Arrays.hashCode(this.hash);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Key other = (Key) obj;
                return Arrays.equals(hash, other.hash);
            }

        }

        public static final class OfflineTreeProducer {
            private static final Charset UTF8;

            static {
                UTF8 = Charset.forName("UTF-8");
            }

            private final Map<Key, OfflineTree> treeCache;
            private int currentPointer;
            private byte[] data;

            public OfflineTreeProducer(Map<Key, OfflineTree> treeCache) {
                this.treeCache = treeCache;
                data = new byte[1];
            }

            public OfflineTree compute(CompilationInfo info, TreePath path, Map<Tree, OfflineTree> tree2Offline) {
                int pointer = currentPointer;

                ensureCapacity(1);
                data[currentPointer++] = (byte) (path.getLeaf().getKind().ordinal() + 1);
                Element el = info.getTrees().getElement(path);
                ElementHandle<?> eh = el != null && SUPPORTED_HANDLE_KINDS.contains(el.getKind()) ? ElementHandle.create(el) : null;
                if (eh != null) {
                    for (String signaturePart : SourceUtils.getJVMSignature(eh)) {
                        byte[] bytes = signaturePart.getBytes(UTF8);
                        ensureCapacity(bytes.length);
                        System.arraycopy(bytes, 0, data, currentPointer, bytes.length);
                        currentPointer += bytes.length;
                    }
                }

                List<Union2<OfflineTree, Collection<? extends OfflineTree>>> newChildren = new ArrayList<>();

                for (Union2<TreePath, Collection<? extends TreePath>> child : children(path)) {
                    ensureCapacity(1);
                    if (child.hasFirst()) {
                        data[currentPointer++] = 0;
                        if (child.first() == null) {
                            ensureCapacity(1);
                            data[currentPointer++] = 0;
                            newChildren.add(Union2.createFirst(null));
                        } else {
                            OfflineTree c = compute(info, child.first(), tree2Offline);
                            newChildren.add(Union2.createFirst(c));
                        }
                    } else {
                        data[currentPointer++] = 1;
                        if (child.second() == null) {
                            addInt(-1);
                            newChildren.add(Union2.createSecond(null));
                        } else {
                            addInt(child.second().size());
                            Collection<OfflineTree> childrenOT = new ArrayList<>();
                            for (TreePath orig : child.second()) {
                                OfflineTree ot = compute(info, orig, tree2Offline);
                                childrenOT.add(ot);
                            }
                            newChildren.add(Union2.createSecond(childrenOT));
                        }
                    }
                }

                try {
                    MessageDigest md = MessageDigest.getInstance("SHA1");
                    md.update(data, pointer, currentPointer - pointer);
                    Key key = new Key(md.digest());
                    OfflineTree existing = treeCache.get(key);

                    if (existing != null) {
//                        statisticsExisting++;
                        tree2Offline.put(path.getLeaf(), existing);
                        return existing;
                    } else {
//                        statisticsNew++;
                        SourcePositions sp = info.getTrees().getSourcePositions();
                        long start = sp.getStartPosition(info.getCompilationUnit(), path.getLeaf());
                        long end = sp.getEndPosition(info.getCompilationUnit(), path.getLeaf());
                        OfflineTree result = new OfflineTree(path.getLeaf().getKind(), eh, newChildren, start, end, info.getText());

                        treeCache.put(key, result);
                        tree2Offline.put(path.getLeaf(), result);
                        
                        return result;
                    }
                } catch (NoSuchAlgorithmException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            private static final Set<ElementKind> SUPPORTED_HANDLE_KINDS =
                    EnumSet.of(ElementKind.PACKAGE, ElementKind.CLASS,
                               ElementKind.INTERFACE, ElementKind.ENUM,
                               ElementKind.ANNOTATION_TYPE, ElementKind.METHOD,
                               ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT,
                               ElementKind.STATIC_INIT, ElementKind.FIELD,
                               ElementKind.ENUM_CONSTANT);

            private void addInt(int size) {
                ensureCapacity(4);
                data[currentPointer++] = (byte) ((size >> 0) & 0xFF);
                data[currentPointer++] = (byte) ((size >> 8) & 0xFF);
                data[currentPointer++] = (byte) ((size >> 16) & 0xFF);
                data[currentPointer++] = (byte) ((size >> 24) & 0xFF);
            }

            private void ensureCapacity(int additionalCapacityNeeded) {
                while (data.length <= (currentPointer + additionalCapacityNeeded)) {
                    data = Arrays.copyOf(data, 2 * data.length);
                }
            }

        }
    }

    public static final class PatternStatistics {
        private final Map<String, Map<FileObject, Integer>> pattern2File2Count = new HashMap<>();
        private final Map<String, Integer> summary = new HashMap<>();

        private FileObject currentFile;
        private final Map<String, Set<Tree>> currentFileStatistics = new HashMap<>();

        public void fileProcessingStarted(FileObject file) {
            currentFile = file;
        }

        public void patternFactRecorded(String pattern, Tree spot) {
            currentFileStatistics.computeIfAbsent(pattern, p -> Collections.newSetFromMap(new IdentityHashMap<>())).add(spot);
        }

        public void fileProcessingFinished() {
            for (Entry<String, Map<FileObject, Integer>> existingEntry : pattern2File2Count.entrySet()) {
                Set<Tree> currentFileUses = currentFileStatistics.remove(existingEntry.getKey());

                existingEntry.getValue().put(currentFile, currentFileUses != null ? currentFileUses.size() : 0);
            }
            for (Entry<String, Set<Tree>> currentFileEntry : currentFileStatistics.entrySet()) {
                pattern2File2Count.computeIfAbsent(currentFileEntry.getKey(), p -> new WeakHashMap<>())
                                  .put(currentFile, currentFileEntry.getValue().size());
            }
            summary.clear();
            for (Entry<String, Map<FileObject, Integer>> e : pattern2File2Count.entrySet()) {
                summary.put(e.getKey(), e.getValue().values().stream().collect(Collectors.summingInt(v -> v)));
            }
            currentFileStatistics.clear();
            currentFile = null;
        }

        public int getPatternUseCount(String pattern) {
            return summary.getOrDefault(pattern, 0);
        }
    }

    static {
        try {
            //XXX: if Utilities.getScope would use $.$1 as the temp class, it would clash
            //with the auto generated pattern variable:
            Field incField = Utilities.class.getDeclaredField("inc");
            incField.setAccessible(true);
            incField.set(null, Long.MAX_VALUE / 2);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
