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
package org.netbeans.spi.java.hints;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.PathConversionMode;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.hints.spiimpl.Hacks;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.netbeans.modules.java.hints.spiimpl.ipi.upgrade.ProjectDependencyUpgrader;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;

/**Factory methods for various predefined {@link JavaFix} implementations.
 *
 * @author lahvac
 */
public class JavaFixUtilities {

    /**Prepare a fix that will replace the given tree node ({@code what}) with the
     * given code. Any variables in the {@code to} pattern will be replaced with their
     * values from {@link HintContext#getVariables() }, {@link HintContext#getMultiVariables() }
     * and {@link HintContext#getVariableNames() }.
     *
     * @param ctx basic context for which the fix should be created
     * @param displayName the display name of the fix
     * @param what the tree node that should be replaced
     * @param to the new code that should replaced the {@code what} tree node
     * @return an editor fix that performs the required transformation
     */
    public static Fix rewriteFix(HintContext ctx, String displayName, TreePath what, final String to) {
        return rewriteFix(ctx.getInfo(), displayName, what, to, ctx.getVariables(), ctx.getMultiVariables(), ctx.getVariableNames(), ctx.getConstraints(), Collections.<String, String>emptyMap());
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    static Fix rewriteFix(CompilationInfo info, String displayName, TreePath what, final String to, Map<String, TreePath> parameters, Map<String, Collection<? extends TreePath>> parametersMulti, final Map<String, String> parameterNames, Map<String, TypeMirror> constraints, Map<String, String> options, String... imports) {
        final Map<String, TreePathHandle> params = new HashMap<>();
        final Map<String, Object> extraParamsData = new HashMap<>();
        final Map<String, ElementHandle<?>> implicitThis = new HashMap<>();

        for (Entry<String, TreePath> e : parameters.entrySet()) {
            TreePath tp = e.getValue();
            if (tp.getParentPath() != null && !immediateChildren(tp.getParentPath().getLeaf()).contains(tp.getLeaf())) {
                Element el = info.getTrees().getElement(tp);
                if (el != null && el.getSimpleName().contentEquals("this")) {
                    implicitThis.put(e.getKey(), ElementHandle.create(el.getEnclosingElement()));
                    continue;
                }
            }
            params.put(e.getKey(), TreePathHandle.create(e.getValue(), info));
            if (e.getValue() instanceof Callable) {
                try {
                    extraParamsData.put(e.getKey(), ((Callable) e.getValue()).call());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        final Map<String, Collection<TreePathHandle>> paramsMulti = new HashMap<>();

        for (Entry<String, Collection<? extends TreePath>> e : parametersMulti.entrySet()) {
            Collection<TreePathHandle> tph = new LinkedList<>();

            for (TreePath tp : e.getValue()) {
                TreePathHandle x = TreePathHandle.create(tp, info);
                // resolve back, to save some problems later:
                if (x.resolve(info) == null) {
                    continue;
                }
                tph.add(x);
            }

            paramsMulti.put(e.getKey(), tph);
        }

        final Map<String, TypeMirrorHandle<?>> constraintsHandles = new HashMap<>();

        for (Entry<String, TypeMirror> c : constraints.entrySet()) {
            constraintsHandles.put(c.getKey(), TypeMirrorHandle.create(c.getValue()));
        }

        Supplier<String> lazyNamer;
        if (displayName == null) {
            lazyNamer = new Supplier<String>() {
                private String dn = null;
                @Override public String get() {
                    if(dn == null) {
                        dn = defaultFixDisplayName(parameters, parametersMulti, to);
                    }
                    return dn;
                }
            };
        } else {
            lazyNamer = () -> displayName;
        }

        return new JavaFixRealImpl(info, what, options, lazyNamer, to, params, extraParamsData, implicitThis, paramsMulti, parameterNames, constraintsHandles, Arrays.asList(imports)).toEditorFix();
    }

    private static Set<Tree> immediateChildren(Tree t) {
        Set<Tree> children = new HashSet<>();

        t.accept(new TreeScanner<Void, Void>() {
            @Override
            public Void scan(Tree tree, Void p) {
                if (tree != null)
                    children.add(tree);
                return null;
            }
        }, null);

        return children;
    }

    /**Creates a fix that removes the given code corresponding to the given tree
     * node from the source code.
     * 
     * @param ctx basic context for which the fix should be created
     * @param displayName the display name of the fix
     * @param what the tree node that should be removed
     * @return an editor fix that removes the give tree from the source code
     */
    public static Fix removeFromParent(HintContext ctx, String displayName, TreePath what) {
        return new RemoveFromParent(displayName, ctx.getInfo(), what, false).toEditorFix();
    }

    /**Creates a fix that removes the given code corresponding to the given tree
     * node together with all its usages from the source code
     * 
     * @param ctx basic context for which the fix should be created
     * @param displayName the display name of the fix
     * @param what the tree node that should be removed
     * @return an editor fix that removes the give tree from the source code
     * 
     * @since 1.48
     */
    public static Fix safelyRemoveFromParent(HintContext ctx, String displayName, TreePath what) {
        return RemoveFromParent.canSafelyRemove(ctx.getInfo(), what) ? new RemoveFromParent(displayName, ctx.getInfo(), what, true).toEditorFix() : null;
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private static String defaultFixDisplayName(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> parametersMulti, String replaceTarget) {
        Map<String, String> stringsForVariables = new LinkedHashMap<>();

        // replace multi vars first
        for (Entry<String, Collection<? extends TreePath>> e : parametersMulti.entrySet()) {
            if (e.getKey().startsWith("$$")) {
                continue;
            }
            if (e.getValue().isEmpty()) {
                stringsForVariables.put(e.getKey()+";", "");    // could be a statement
                stringsForVariables.put(", "+e.getKey(), "");   // or parameter
                stringsForVariables.put(e.getKey(), "");
            } else if (e.getValue().size() == 1) {
                String text = treePathToString(e.getValue().iterator().next(), false, e.getKey());
                stringsForVariables.put(e.getKey(), text);
            } else {
                // keep the variable in the text for more complex cases, but we have to escape it somehow
                stringsForVariables.put(e.getKey(), e.getKey().replace("$", "♦"));
            }
        }

        // regular vars next, longest first in case a var is a prefix of another var
        variables.entrySet().stream()
                            .sorted((e1, e2) -> e2.getKey().length() - e1.getKey().length())
                            .forEach(e -> stringsForVariables.put(e.getKey(), treePathToString(e.getValue(), true, e.getKey())));

        if (!stringsForVariables.containsKey("$this")) {
            //XXX: is this correct?
            stringsForVariables.put("$this", "this");
        }

        for (Entry<String, String> e : stringsForVariables.entrySet()) {
            replaceTarget = replaceTarget.replace(e.getKey(), e.getValue());
        }

        // cleanup and escape java code for html renderer
        return "Rewrite to " + replaceTarget.replace("♦", "$")
                                            .replace(";;", ";")
                                            .replace("\n", " ")
                                            .replaceAll("\\s{2,}", " ")
                                            .replace("<", "&lt;");
    }

    private static String treePathToString(TreePath tp, boolean preferVarName, String fallback) {
        Tree leaf = tp.getLeaf();
        if (preferVarName && leaf.getKind() == Kind.VARIABLE) {
            return ((VariableTree) leaf).getName().toString();
        }
        String str = leaf.toString();
        return str.equals("(ERROR)") ? fallback : str;
    }

    private static void checkDependency(CompilationInfo copy, Element e, boolean canShowUI) {
        SpecificationVersion sv = computeSpecVersion(copy, e);

        while (sv == null && e.getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
            sv = computeSpecVersion(copy, e);
        }

        if (sv == null) {
            return ;
        }

        Project currentProject = FileOwnerQuery.getOwner(copy.getFileObject());

        if (currentProject == null) {
            return ;
        }

        FileObject file = getFile(copy, e);

        if (file == null) {
            return ;
        }

        FileObject root = findRootForFile(file, copy.getClasspathInfo());

        if (root == null) {
            return ;
        }

        Project referedProject = FileOwnerQuery.getOwner(file);

        if (referedProject != null && currentProject.getProjectDirectory().equals(referedProject.getProjectDirectory())) {
            return ;
        }

        for (ProjectDependencyUpgrader pdu : Lookup.getDefault().lookupAll(ProjectDependencyUpgrader.class)) {
            if (pdu.ensureDependency(currentProject, root, sv, canShowUI)) {
                return ;
            }
        }
    }

    private static java.util.regex.Pattern SPEC_VERSION = java.util.regex.Pattern.compile("[0-9]+(\\.[0-9]+)+");

    static SpecificationVersion computeSpecVersion(CompilationInfo info, Element el) {
        if (!Utilities.isJavadocSupported(info)) return null;

        DocCommentTree javaDoc = info.getDocTrees().getDocCommentTree(el);

        if (javaDoc == null) return null;

        for (DocTree tag : javaDoc.getBlockTags()) {
            if (tag.getKind() != DocTree.Kind.SINCE) {
                continue;
            }

            String text = ((SinceTree) tag).getBody().toString();

            Matcher m = SPEC_VERSION.matcher(text);

            if (!m.find()) {
                continue;
            }

            return new SpecificationVersion(m.group()/*ver.toString()*/);
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    private static FileObject getFile(CompilationInfo copy, Element e) {
        return SourceUtils.getFile(e, copy.getClasspathInfo());
    }

    private static FileObject findRootForFile(final FileObject file, final ClasspathInfo cpInfo) {
        ClassPath cp = ClassPathSupport.createProxyClassPath(
            new ClassPath[] {
                cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),
                cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT),
                cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE),
            });

        FileObject root = cp.findOwnerRoot(file);

        if (root != null) {
            return root;
        }

        for (ClassPath.Entry e : cp.entries()) {
            FileObject[] sourceRoots = SourceForBinaryQuery.findSourceRoots(e.getURL()).getRoots();

            if (sourceRoots.length == 0) continue;

            ClassPath sourcePath = ClassPathSupport.createClassPath(sourceRoots);

            root = sourcePath.findOwnerRoot(file);

            if (root != null) {
                return root;
            }
        }
        return null;
    }

    private static boolean isStaticElement(Element el) {
        if (el == null) return false;

        if (el.asType() == null || el.asType().getKind() == TypeKind.ERROR) {
            return false;
        }

        if (el.getModifiers().contains(Modifier.STATIC)) {
            //XXX:
            if (!el.getKind().isClass() && !el.getKind().isInterface()) {
                return false;
            }

            return true;
        }

        if (el.getKind().isClass() || el.getKind().isInterface()) {
            return el.getEnclosingElement().getKind() == ElementKind.PACKAGE;
        }

        return false;
    }
    
    private static class JavaFixRealImpl extends JavaFix {
        private final Supplier<String> displayName;
        private final Map<String, TreePathHandle> params;
        private final Map<String, Object> extraParamsData;
        private final Map<String, ElementHandle<?>> implicitThis;
        private final Map<String, Collection<TreePathHandle>> paramsMulti;
        private final Map<String, String> parameterNames;
        private final Map<String, TypeMirrorHandle<?>> constraintsHandles;
        private final Iterable<? extends String> imports;
        private final String to;

        public JavaFixRealImpl(CompilationInfo info, TreePath what, Map<String, String> options, Supplier<String> displayName, String to, Map<String, TreePathHandle> params, Map<String, Object> extraParamsData, Map<String, ElementHandle<?>> implicitThis, Map<String, Collection<TreePathHandle>> paramsMulti, final Map<String, String> parameterNames, Map<String, TypeMirrorHandle<?>> constraintsHandles, Iterable<? extends String> imports) {
            super(info, what, options);

            this.displayName = displayName;
            this.to = to;
            this.params = params;
            this.extraParamsData = extraParamsData;
            this.implicitThis = implicitThis;
            this.paramsMulti = paramsMulti;
            this.parameterNames = parameterNames;
            this.constraintsHandles = constraintsHandles;
            this.imports = imports;
        }

        @Override
        protected String getText() {
            return displayName.get();
        }

        @Override
        @SuppressWarnings("NestedAssignment")
        protected void performRewrite(TransformationContext ctx) {
            final WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            
            final GeneratorUtilities gen = GeneratorUtilities.get(wc);
            tp = new TreePath(tp.getParentPath(), gen.importComments(tp.getLeaf(), tp.getCompilationUnit()));
            final Map<String, TreePath> parameters = new HashMap<>();

            for (Entry<String, TreePathHandle> e : params.entrySet()) {
                TreePath p = e.getValue().resolve(wc);

                if (p == null) {
                    Logger.getLogger(JavaFix.class.getName()).log(Level.SEVERE, "Cannot resolve handle={0}", e.getValue());
                }

                parameters.put(e.getKey(), p);
            }

            Map<String, Element> implicitThis = new HashMap<>();

            for (Entry<String, ElementHandle<?>> e : this.implicitThis.entrySet()) {
                Element clazz = e.getValue().resolve(wc);

                if (clazz == null) {
                    Logger.getLogger(JavaFix.class.getName()).log(Level.SEVERE, "Cannot resolve handle={0}", e.getValue());
                    continue;
                }

                implicitThis.put(e.getKey(), clazz);
            }

            final Map<String, Collection<TreePath>> parametersMulti = new HashMap<>();

            for (Entry<String, Collection<TreePathHandle>> e : paramsMulti.entrySet()) {
                Collection<TreePath> tps = new LinkedList<>();

                for (TreePathHandle tph : e.getValue()) {
                    TreePath p = tph.resolve(wc);
                    if (p == null) {
                        Logger.getLogger(JavaFix.class.getName()).log(Level.SEVERE, "Cannot resolve handle={0}", e.getValue());
                        continue;
                    }

                    tps.add(p);
                }

                parametersMulti.put(e.getKey(), tps);
            }

            Map<String, TypeMirror> constraints = new HashMap<>();

            for (Entry<String, TypeMirrorHandle<?>> c : constraintsHandles.entrySet()) {
                constraints.put(c.getKey(), c.getValue().resolve(wc));
            }

            Scope scope = Utilities.constructScope(wc, constraints, imports);

            assert scope != null;

            Tree parsed = Utilities.parseAndAttribute(wc, to, scope);
            
            if (parsed.getKind() == Kind.EXPRESSION_STATEMENT && ExpressionTree.class.isAssignableFrom(tp.getLeaf().getKind().asInterface())) {
                parsed = ((ExpressionStatementTree) parsed).getExpression();
            }
            
            Map<Tree, Tree> rewriteFromTo = new IdentityHashMap<>();
            List<Tree> order = new ArrayList<>(7);
            Tree original;

            if (Utilities.isFakeBlock(parsed)) {
                TreePath parent = tp.getParentPath();
                List<? extends StatementTree> statements = ((BlockTree) parsed).getStatements();
                
                if (tp.getLeaf().getKind() == Kind.BLOCK) {
                    BlockTree real = (BlockTree) tp.getLeaf();
                    rewriteFromTo.put(original = real, wc.getTreeMaker().Block(statements, real.isStatic()));
                } else {
                    statements = statements.subList(1, statements.size() - 1);

                    if (parent.getLeaf().getKind() == Kind.BLOCK) {
                        List<StatementTree> newStatements = new LinkedList<>();

                        for (StatementTree st : ((BlockTree) parent.getLeaf()).getStatements()) {
                            if (st == tp.getLeaf()) {
                                newStatements.addAll(statements);
                            } else {
                                newStatements.add(st);
                            }
                        }

                        rewriteFromTo.put(original = parent.getLeaf(), wc.getTreeMaker().Block(newStatements, ((BlockTree) parent.getLeaf()).isStatic()));
                    } else {
                        rewriteFromTo.put(original = tp.getLeaf(), wc.getTreeMaker().Block(statements, false));
                    }
                }
            } else if (Utilities.isFakeClass(parsed)) {
                TreePath parent = tp.getParentPath();
                List<? extends Tree> members = ((ClassTree) parsed).getMembers();

                members = members.subList(1, members.size());

                assert parent.getLeaf().getKind() == Kind.CLASS;

                List<Tree> newMembers = new LinkedList<>();

                ClassTree ct = (ClassTree) parent.getLeaf();

                for (Tree t : ct.getMembers()) {
                    if (t == tp.getLeaf()) {
                        newMembers.addAll(members);
                    } else {
                        newMembers.add(t);
                    }
                }

                rewriteFromTo.put(original = parent.getLeaf(), wc.getTreeMaker().Class(ct.getModifiers(), ct.getSimpleName(), ct.getTypeParameters(), ct.getExtendsClause(), ct.getImplementsClause(), newMembers));
            } else if (tp.getLeaf().getKind() == Kind.BLOCK && parametersMulti.containsKey("$$1$") && parsed.getKind() != Kind.BLOCK && StatementTree.class.isAssignableFrom(parsed.getKind().asInterface())) {
                List<StatementTree> newStatements = new LinkedList<>();

                newStatements.add(wc.getTreeMaker().ExpressionStatement(wc.getTreeMaker().Identifier("$$1$")));
                newStatements.add((StatementTree) parsed);
                newStatements.add(wc.getTreeMaker().ExpressionStatement(wc.getTreeMaker().Identifier("$$2$")));

                parsed = wc.getTreeMaker().Block(newStatements, ((BlockTree) tp.getLeaf()).isStatic());

                rewriteFromTo.put(original = tp.getLeaf(), parsed);
            } else {
                while (   tp.getParentPath().getLeaf().getKind() == Kind.PARENTHESIZED
                       && tp.getLeaf().getKind() != parsed.getKind()
                       && tp.getParentPath() != null
                       && tp.getParentPath().getParentPath() != null
                       && !requiresParenthesis(parsed, tp.getParentPath().getLeaf(), tp.getParentPath().getParentPath().getLeaf())
                       && requiresParenthesis(tp.getLeaf(), tp.getParentPath().getLeaf(), tp.getParentPath().getParentPath().getLeaf()))
                    tp = tp.getParentPath();
                rewriteFromTo.put(original = tp.getLeaf(), parsed);
            }
            order.add(original);

            //prevent generating QualIdents inside import clauses - might be better to solve that inside ImportAnalysis2,
            //but that seems not to be straightforward:
            boolean inImport = parsed.getKind() == Kind.IMPORT;
            boolean inPackage = false;
            TreePath w = tp;

            while (!inImport && w != null) {
                inImport |= w.getLeaf().getKind() == Kind.IMPORT;
                inPackage |= w.getParentPath() != null && w.getParentPath().getLeaf().getKind() == Kind.COMPILATION_UNIT && ((CompilationUnitTree) w.getParentPath().getLeaf()).getPackageName() == w.getLeaf();
                w = w.getParentPath();
            }

            final Set<Tree> originalTrees = Collections.newSetFromMap(new IdentityHashMap<>());
            
            new ErrorAwareTreeScanner<Void, Void>() {
                @Override public Void scan(Tree tree, Void p) {
                    originalTrees.add(tree);
                    return super.scan(tree, p);
                }
            }.scan(original, null);
            
            new ReplaceParameters(wc, ctx.isCanShowUI(), inImport, parameters, extraParamsData, implicitThis, parametersMulti, parameterNames, rewriteFromTo, order, originalTrees).scan(new TreePath(tp.getParentPath(), rewriteFromTo.get(original)), null);

            if (inPackage) {
                String newPackage = wc.getTreeUtilities().translate(wc.getCompilationUnit().getPackageName(), new IdentityHashMap<>(rewriteFromTo))./*XXX: not correct*/toString();

                ClassPath source = wc.getClasspathInfo().getClassPath(PathKind.SOURCE);
                FileObject ownerRoot = source.findOwnerRoot(wc.getFileObject());

                if (ownerRoot != null) {
                    ctx.getFileChanges().add(new MoveFile(wc.getFileObject(), ownerRoot, newPackage.replace('.', '/')));
                } else {
                    Logger.getLogger(JavaFix.class.getName()).log(Level.WARNING, "{0} not on its source path ({1})", new Object[] {FileUtil.getFileDisplayName(wc.getFileObject()), source.toString(PathConversionMode.PRINT)});
                }
            }
            for (Tree from : order) {
                Tree to = rewriteFromTo.get(from);
//                gen.copyComments(from, to, true);
//                gen.copyComments(from, to, false);
                wc.rewrite(from, to);
            }
        }
    }
    
    private static class IK {
        private final Object o;

        public IK(Object o) {
            this.o = o;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            if (o != null) {
                hash = 59 * hash + System.identityHashCode(o);
            }
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IK)) {
                return false;
            }
            final IK other = (IK) obj;
            return other.o == this.o;
        }
    }

    private static final Set<Kind> NUMBER_LITERAL_KINDS = EnumSet.of(Kind.FLOAT_LITERAL, Kind.DOUBLE_LITERAL, Kind.INT_LITERAL, Kind.LONG_LITERAL);

    private static class ReplaceParameters extends ErrorAwareTreePathScanner<Number, Void> {

        private final CompilationInfo info;
        private final TreeMaker make;
        private final boolean canShowUI;
        private final boolean inImport;
        private final Map<String, TreePath> parameters;
        private final Map<String, Object> extraParamsData;
        private final Map<String, Element> implicitThis;
        private final Map<String, Collection<TreePath>> parametersMulti;
        private final Map<String, String> parameterNames;
        private final Map<Tree, Tree> rewriteFromTo;
        private final Set<Tree> originalTrees;
        private final List<Tree> order;
        private final List<Element> nestedTypes = new ArrayList<>();

        public ReplaceParameters(WorkingCopy wc, boolean canShowUI, boolean inImport, Map<String, TreePath> parameters, Map<String, Object> extraParamsData, Map<String, Element> implicitThis, Map<String, Collection<TreePath>> parametersMulti, Map<String, String> parameterNames, Map<Tree, Tree> rewriteFromTo, List<Tree> order, Set<Tree> originalTrees) {
            this.parameters = parameters;
            this.info = wc;
            this.make = wc.getTreeMaker();
            this.canShowUI = canShowUI;
            this.inImport = inImport;
            this.extraParamsData = extraParamsData;
            this.implicitThis = implicitThis;
            this.parametersMulti = parametersMulti;
            this.parameterNames = parameterNames;
            this.rewriteFromTo = rewriteFromTo;
            this.order = order;
            this.originalTrees = originalTrees;
        }

        @Override
        public Number visitIdentifier(IdentifierTree node, Void p) {
            String name = node.getName().toString();
            Tree newNode = handleIdentifier(name, node);
            
            if (newNode != null) {
                rewrite(node, newNode);
                if (NUMBER_LITERAL_KINDS.contains(newNode.getKind())) {
                    return (Number) ((LiteralTree) newNode).getValue();
                }
            } else {
                Element implicitThisClass = implicitThis.get(name);
                if (implicitThisClass != null) {
                    Element enclClass = findEnclosingClass();
                    if (enclClass == implicitThisClass) {
                        rewrite(node, make.Identifier("this"));
                    } else {
                        rewrite(node, make.MemberSelect(make.QualIdent(implicitThisClass), "this"));
                    }
                    return null;
                }
            }

            Element e = info.getTrees().getElement(getCurrentPath());

            if (e != null && isStaticElement(e) && !inImport) {
                rewrite(node, make.QualIdent(e));
            }

            return super.visitIdentifier(node, p);
        }

        private Element findEnclosingClass() {
            TreePath findClass = getCurrentPath();
            while (findClass != null && !TreeUtilities.CLASS_TREE_KINDS.contains(findClass.getLeaf().getKind())) {
                findClass = findClass.getParentPath();
            }
            return findClass != null ? info.getTrees().getElement(findClass) : null;
        }

        @Override
        public Number visitTypeParameter(TypeParameterTree node, Void p) {
            String name = node.getName().toString();
            Tree newNode = handleIdentifier(name, node);
            
            if (newNode != null) {
                rewrite(node, newNode);
                if (NUMBER_LITERAL_KINDS.contains(newNode.getKind())) {
                    return (Number) ((LiteralTree) newNode).getValue();
                }
            }
            
            return super.visitTypeParameter(node, p);
        }
        
        private static final EnumSet<Kind>  COMPLEX_OPS = EnumSet.of(Kind.CONDITIONAL_AND, Kind.CONDITIONAL_OR);
        
        private Tree handleIdentifier(String name, Tree node) {
            TreePath tp = parameters.get(name);

            if (tp != null) {
                if (tp.getLeaf() instanceof Hacks.RenameTree) {
                    Hacks.RenameTree rt = (Hacks.RenameTree) tp.getLeaf();
                    return make.setLabel(rt.originalTree, rt.newName);
                }
                if (!parameterNames.containsKey(name)) {
                    Tree target = tp.getLeaf();
                    if (NUMBER_LITERAL_KINDS.contains(target.getKind())) {
                        return target;
                    }
                    //TODO: might also remove parenthesis, but needs to ensure the diff will still be minimal
//                    while (target.getKind() == Kind.PARENTHESIZED
//                           && !requiresParenthesis(((ParenthesizedTree) target).getExpression(), getCurrentPath().getParentPath().getLeaf())) {
//                        target = ((ParenthesizedTree) target).getExpression();
//                    }
                    if (   getCurrentPath().getParentPath() != null
                        && getCurrentPath().getParentPath().getLeaf().getKind() == Kind.LOGICAL_COMPLEMENT) {
                        boolean rewriteNegated;
                        
                        if (tp.getParentPath() == null) {
                            rewriteNegated = true;
                        } else {
                            Tree parent = tp.getParentPath().getLeaf();
                            TreePath aboveNewComplement = getCurrentPath().getParentPath().getParentPath();
                            // Do not try to optimize too complex expressions, following the principle of the least surprise.
                            // 1/ optimization is OK if the target is without parenthesis - extra level of parens can be avoided
                            // 2/ if both target and original are parenthesized, then optimization should be done to avoid one extra paren level
                            // 3/ do not optimize complex expressions - least surprise
                            rewriteNegated = (parent.getKind() != Kind.LOGICAL_COMPLEMENT) && 
                                             (!COMPLEX_OPS.contains(target.getKind()) || 
                                                parent.getKind() != Kind.PARENTHESIZED || 
                                                (aboveNewComplement != null && aboveNewComplement.getLeaf().getKind() == Kind.PARENTHESIZED));
                        }
                        
                        if (rewriteNegated) {
                            Tree negated = negate((ExpressionTree) tp.getLeaf(), getCurrentPath().getParentPath().getParentPath().getLeaf(), true);

                            if (negated != null) {
                                rewrite(getCurrentPath().getParentPath().getLeaf(), negated);
                            }
                        }
                    }
                    if (requiresParenthesis(target, node, getCurrentPath().getParentPath().getLeaf())) {
                        target = make.Parenthesized((ExpressionTree) target);
                    }
                    return target;
                }
            }

            String variableName = parameterNames.get(name);

            if (variableName != null) {
                return make.Identifier(variableName);
            }
            
            return null;
        }

        @Override
        public Number visitMemberSelect(MemberSelectTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());

            if (e != null && (e.getKind() != ElementKind.CLASS || ((TypeElement) e).asType().getKind() != TypeKind.ERROR)) {
                //check correct dependency:
                checkDependency(info, e, canShowUI);

                if (isStaticElement(e) && !inImport) {
                    rewrite(node, make.QualIdent(e));

                    return null;
                }
            }
            
            MemberSelectTree nue = node;
            String selectedName = node.getIdentifier().toString();

            if (selectedName.startsWith("$") && parameterNames.get(selectedName) != null) {
                nue = make.MemberSelect(node.getExpression(), parameterNames.get(selectedName));
            }

            if (nue.getExpression().getKind() == Kind.IDENTIFIER) {
                String name = ((IdentifierTree) nue.getExpression()).getName().toString();

                if (name.startsWith("$") && parameters.get(name) == null) {
                    Element implicitThisClass = implicitThis.get(name);
                    if (implicitThisClass != null) {
                        TreePath findClass = getCurrentPath();
                        OUTER: while (findClass != null) {
                            if (TreeUtilities.CLASS_TREE_KINDS.contains(findClass.getLeaf().getKind())) {
                                Element clazz = info.getTrees().getElement(findClass);
                                if (implicitThisClass.equals(clazz)) {
                                    //this.<...>, the this may be implicit:
                                    rewrite(node, make.Identifier(nue.getIdentifier()));
                                    return null;
                                }
                                if (clazz.getKind().isClass() || clazz.getKind().isInterface()) {
                                    for (Element currentClassElement : info.getElements().getAllMembers((TypeElement) clazz)) {
                                        if (currentClassElement.getSimpleName().equals(node.getIdentifier())) {
                                            //there may be a resolution conflict, let the member select be qualified
                                            //TODO: no conflicts between fields and methods of the same name
                                            //but we current still qualify the name
                                            break OUTER;
                                        }
                                    }
                                }
                            }
                            findClass = findClass.getParentPath();
                        }
                        //let visitIdent handle this
                    } else {
                        //XXX: unbound variable, use identifier instead of member select - may cause problems?
                        rewrite(node, make.Identifier(nue.getIdentifier()));
                        return null;
                    }
                }
            }

            if (nue != node) {
                rewrite(node, nue);
            }
            
            return super.visitMemberSelect(node, p);
        }

        @Override
        public Number visitVariable(VariableTree node, Void p) {
            String name = node.getName().toString();

            if (name.startsWith("$")) {
                String nueName = parameterNames.get(name);

                if (nueName != null) {
                    name = nueName;
                }
            }
            
            VariableTree nue = make.Variable(node.getModifiers(), name, node.getType(), resolveOptionalValue(node.getInitializer()));

            rewrite(node, nue);

            return super.visitVariable(nue, p);
        }

        @Override
        public Number visitIf(IfTree node, Void p) {
            IfTree nue = make.If(node.getCondition(), node.getThenStatement(), resolveOptionalValue(node.getElseStatement()));
            
            rewrite(node, nue);
            
            return super.visitIf(nue, p);
        }

        @Override
        public Number visitMethod(MethodTree node, Void p) {
            String name = node.getName().toString();
            String newName = name;

            if (name.startsWith("$")) {
                if (parameterNames.containsKey(name)) {
                    newName = parameterNames.get(name);
                }
            }

            List<? extends TypeParameterTree> typeParams = resolveMultiParameters(node.getTypeParameters());
            List<? extends VariableTree> params = resolveMultiParameters(node.getParameters());
            List<? extends ExpressionTree> thrown = resolveMultiParameters(node.getThrows());
            
            MethodTree nue = make.Method(node.getModifiers(), newName, node.getReturnType(), typeParams, params, thrown, node.getBody(), (ExpressionTree) node.getDefaultValue());
            
            rewrite(node, nue);
            
            return super.visitMethod(nue, p);
        }

        @Override
        public Number visitClass(ClassTree node, Void p) {
            String name = node.getSimpleName().toString();
            String newName = name;

            if (name.startsWith("$")) {
                if (parameterNames.containsKey(name)) {
                    newName = parameterNames.get(name);
                }
            }

            List<? extends TypeParameterTree> typeParams = resolveMultiParameters(node.getTypeParameters());
            List<? extends Tree> implementsClauses = resolveMultiParameters(node.getImplementsClause());
            List<? extends Tree> members = resolveMultiParameters(Utilities.filterHidden(getCurrentPath(), node.getMembers()));
            Tree extend = resolveOptionalValue(node.getExtendsClause());
            ClassTree nue = make.Class(node.getModifiers(), newName, typeParams, extend, implementsClauses, members);
            
            rewrite(node, nue);
            
            Element el = info.getTrees().getElement(getCurrentPath());

            nestedTypes.add(el);

            try {
                return super.visitClass(nue, p);
            } finally {
                nestedTypes.remove(nestedTypes.size() - 1);
            }
        }

        @Override
        public Number visitExpressionStatement(ExpressionStatementTree node, Void p) {
            CharSequence name = Utilities.getWildcardTreeName(node);

            if (name != null) {
                TreePath tp = parameters.get(name.toString());

                if (tp != null && StatementTree.class.isAssignableFrom(tp.getLeaf().getKind().asInterface())) {
                    rewrite(node, tp.getLeaf());
                    return null;
                }
            }

            return super.visitExpressionStatement(node, p);
        }

        @Override
        public Number visitLiteral(LiteralTree node, Void p) {
            if (node.getValue() instanceof Number) {
                return (Number) node.getValue();
            }

            return super.visitLiteral(node, p);
        }

        @Override
        public Number visitBinary(BinaryTree node, Void p) {
            Number left  = scan(node.getLeftOperand(), p);
            Number right = scan(node.getRightOperand(), p);

            if (left != null && right != null) {
                Number result = null;
                switch (node.getKind()) {
                    case MULTIPLY:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() * right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() * right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() * right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() * right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case DIVIDE:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() / right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() / right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() / right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() / right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case REMAINDER:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() % right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() % right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() % right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() % right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case PLUS:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() + right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() + right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() + right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() + right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case MINUS:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() - right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() - right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() - right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() - right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case LEFT_SHIFT:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() << right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() << right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case RIGHT_SHIFT:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() >> right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() >> right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case UNSIGNED_RIGHT_SHIFT:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() >>> right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() >>> right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case AND:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() & right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() & right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case XOR:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() ^ right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() ^ right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case OR:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() | right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() | right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;
                }

                if (result != null) {
                    rewrite(node, make.Literal(result));

                    return result;
                }
            }

            return null;
        }

        @Override
        public Number visitUnary(UnaryTree node, Void p) {
            Number op  = scan(node.getExpression(), p);

            if (op != null) {
                Number result = null;
                switch (node.getKind()) {
                    case UNARY_MINUS:
                            if (op instanceof Double) {
                                result = -op.doubleValue();
                            } else if (op instanceof Float) {
                                result = -op.floatValue();
                            } else if (op instanceof Long) {
                                result = -op.longValue();
                            } else if (op instanceof Integer) {
                                result = -op.intValue();
                            } else {
                                throw new IllegalStateException("op=" + op.getClass());
                            }
                            break;
                    case UNARY_PLUS:
                        result = op;
                        break;
                }

                if (result != null) {
                    rewrite(node, make.Literal(result));

                    return result;
                }
            }

            return super.visitUnary(node, p);
        }

        @Override
        public Number visitBlock(BlockTree node, Void p) {
            List<? extends StatementTree> nueStatement = resolveMultiParameters(node.getStatements());
            BlockTree nue = make.Block(nueStatement, node.isStatic());

            rewrite(node, nue);

            return super.visitBlock(nue, p);
        }

        @Override
        public Number visitCase(CaseTree node, Void p) {
            List<? extends StatementTree> statements = (List<? extends StatementTree>) resolveMultiParameters(node.getStatements());
            CaseTree nue = make.Case(node.getExpression(), statements);

            rewrite(node, nue);
            return super.visitCase(node, p);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Number visitMethodInvocation(MethodInvocationTree node, Void p) {
            List<? extends ExpressionTree> typeArgs = (List<? extends ExpressionTree>) resolveMultiParameters(node.getTypeArguments());
            List<? extends ExpressionTree> args = resolveMultiParameters(node.getArguments());
            MethodInvocationTree nue = make.MethodInvocation(typeArgs, node.getMethodSelect(), args);

            rewrite(node, nue);

            return super.visitMethodInvocation(nue, p);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Number visitNewClass(NewClassTree node, Void p) {
            List<? extends ExpressionTree> typeArgs = (List<? extends ExpressionTree>) resolveMultiParameters(node.getTypeArguments());
            List<? extends ExpressionTree> args = resolveMultiParameters(node.getArguments());
            NewClassTree nue = make.NewClass(node.getEnclosingExpression(), typeArgs, node.getIdentifier(), args, node.getClassBody());

            rewrite(node, nue);
            return super.visitNewClass(nue, p);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Number visitParameterizedType(ParameterizedTypeTree node, Void p) {
            List<? extends ExpressionTree> typeArgs = (List<? extends ExpressionTree>) resolveMultiParameters(node.getTypeArguments());
            ParameterizedTypeTree nue = make.ParameterizedType(node.getType(), typeArgs);

            rewrite(node, nue);
            return super.visitParameterizedType(node, p);
        }

        @Override
        public Number visitSwitch(SwitchTree node, Void p) {
            List<? extends CaseTree> cases = (List<? extends CaseTree>) resolveMultiParameters(node.getCases());
            SwitchTree nue = make.Switch(node.getExpression(), cases);

            rewrite(node, nue);
            return super.visitSwitch(node, p);
        }

        @Override
        public Number visitTry(TryTree node, Void p) {
            List<? extends Tree> resources = (List<? extends Tree>) resolveMultiParameters(node.getResources());
            List<? extends CatchTree> catches = (List<? extends CatchTree>) resolveMultiParameters(node.getCatches());
            TryTree nue = make.Try(resources, node.getBlock(), catches, node.getFinallyBlock());

            rewrite(node, nue);
            return super.visitTry(node, p);
        }

        @Override
        public Number visitModifiers(ModifiersTree node, Void p) {
            List<AnnotationTree> annotations = new ArrayList<>(node.getAnnotations());
            IdentifierTree ident = !annotations.isEmpty() && annotations.get(0).getAnnotationType().getKind() == Kind.IDENTIFIER ? (IdentifierTree) annotations.get(0).getAnnotationType() : null;

            if (ident != null) {
                annotations.remove(0);
                
                String name = ident.getName().toString();
                TreePath orig = parameters.get(name);
                ModifiersTree nue;
                
                if (orig != null && orig.getLeaf().getKind() == Kind.MODIFIERS) {
                    ModifiersTree origMods = (ModifiersTree) orig.getLeaf();
                    Object actualContent = extraParamsData.get(name);
                    Set<Modifier> actualFlags = EnumSet.noneOf(Modifier.class);
                    boolean[] actualAnnotationsMask = new boolean[0];
                    
                    if (actualContent instanceof Object[] && ((Object[]) actualContent)[0] instanceof Set) {
                        actualFlags.addAll(NbCollections.checkedSetByFilter((Set) ((Object[]) actualContent)[0], Modifier.class, false));
                    }
                    
                    if (actualContent instanceof Object[] && ((Object[]) actualContent)[1] instanceof boolean[]) {
                        actualAnnotationsMask = (boolean[]) ((Object[]) actualContent)[1];
                    }
                    
                    nue = origMods;
                    
                    for (Modifier m : origMods.getFlags()) {
                        if (actualFlags.contains(m)) continue;
                        nue = make.removeModifiersModifier(nue, m);
                    }
                    
                    for (Modifier m : node.getFlags()) {
                        nue = make.addModifiersModifier(nue, m);
                    }
                    
                    int ai = 0;
                    
                    OUTER: for (AnnotationTree a : origMods.getAnnotations()) {
                        if (actualAnnotationsMask.length <= ai || actualAnnotationsMask[ai++]) continue;
                        for (Iterator<AnnotationTree> it = annotations.iterator(); it.hasNext();) {
                            AnnotationTree toCheck = it.next();
                            Collection<? extends Occurrence> match = org.netbeans.api.java.source.matching.Matcher.create(info).setTreeTopSearch().setSearchRoot(new TreePath(getCurrentPath(), a)).match(Pattern.createSimplePattern(new TreePath(getCurrentPath(), toCheck)));
                            
                            if (!match.isEmpty()) {
                                //should be kept:
                                it.remove();
                                break OUTER;
                            }
                        }
                        
                        nue = make.removeModifiersAnnotation(nue, a);
                    }
                    
                    for (AnnotationTree a : annotations) {
                        nue = make.addModifiersAnnotation(nue, a);
                        scan(a, p);
                    }
                } else {
                    nue = make.removeModifiersAnnotation(node, 0);
                }
                
                rewrite(node, nue);
                
                return null;
            }
            
            return super.visitModifiers(node, p);
        }

        @Override
        public Number visitNewArray(NewArrayTree node, Void p) {
            List<? extends ExpressionTree> dimensions = (List<? extends ExpressionTree>) resolveMultiParameters(node.getDimensions());
            List<? extends ExpressionTree> initializers = (List<? extends ExpressionTree>) resolveMultiParameters(node.getInitializers());
            NewArrayTree nue = make.NewArray(node.getType(), dimensions, initializers);

            rewrite(node, nue);
            return super.visitNewArray(node, p);
        }

        @Override
        public Number visitLambdaExpression(LambdaExpressionTree node, Void p) {
            List<? extends VariableTree> args = resolveMultiParameters(node.getParameters());
            LambdaExpressionTree nue = make.LambdaExpression(args, node.getBody());

            Hacks.copyLambdaKind(node, nue);

            rewrite(node, nue);

            return super.visitLambdaExpression(node, p);
        }

        @Override
        public Number visitAnnotation(AnnotationTree node, Void p) {
            List<? extends ExpressionTree> args = resolveMultiParameters(node.getArguments());
            AnnotationTree nue = make.Annotation(node.getAnnotationType(), args);

            rewrite(node, nue);

            return super.visitAnnotation(node, p);
        }

        @SuppressWarnings("unchecked")
        private <T extends Tree> List<T> resolveMultiParameters(List<T> list) {
            if (list == null) return null;
            if (!Utilities.containsMultistatementTrees(list)) return list;

            List<T> result = new LinkedList<>();

            for (T t : list) {
                if (Utilities.isMultistatementWildcardTree(t)) {
                    Collection<TreePath> embedded = parametersMulti.get(Utilities.getWildcardTreeName(t).toString());

                    if (embedded != null) {
                        for (TreePath tp : embedded) {
                            if (tp != null) {
                                result.add((T) tp.getLeaf());
                            }
                        }
                    }
                } else {
                    result.add(t);
                }
            }

            return result;
        }
        
        @SuppressWarnings("unchecked")
        private <T extends Tree> T resolveOptionalValue(T in) {
            if (in != null && Utilities.isMultistatementWildcardTree(in)) {
                TreePath out = parameters.get(Utilities.getWildcardTreeName(in).toString());
                if (out != null) return (T) out.getLeaf();
                return null;
            }
            
            return in;
        }

        private ExpressionTree negate(ExpressionTree original, Tree parent, boolean nullOnPlainNeg) {
            ExpressionTree newTree;
            switch (original.getKind()) {
                case PARENTHESIZED:
                    ExpressionTree expr = ((ParenthesizedTree) original).getExpression();
                    ExpressionTree negatedOrNull = negate(expr, original, nullOnPlainNeg);
                    if (negatedOrNull != null) {
                        if (negatedOrNull.getKind() != Kind.PARENTHESIZED) {
                            negatedOrNull = make.Parenthesized(negatedOrNull);
                        }
                    }
                    return negatedOrNull;
                    /**
                    if (nullOnPlainNeg) {
                        return null;
                    } else {
                        return make.Unary(Kind.LOGICAL_COMPLEMENT, original);
                    }
                    */
                    
                case INSTANCE_OF:
                    return make.Unary(Kind.LOGICAL_COMPLEMENT, make.Parenthesized(original));
                    
                case LOGICAL_COMPLEMENT:
                    newTree = ((UnaryTree) original).getExpression();
                    while (newTree.getKind() == Kind.PARENTHESIZED && !JavaFixUtilities.requiresParenthesis(((ParenthesizedTree) newTree).getExpression(), original, parent)) {
                        newTree = ((ParenthesizedTree) newTree).getExpression();
                    }
                    break;
                case NOT_EQUAL_TO:
                    newTree = negateBinaryOperator(original, Kind.EQUAL_TO, false);
                    break;
                case EQUAL_TO:
                    newTree = negateBinaryOperator(original, Kind.NOT_EQUAL_TO, false);
                    break;
                case BOOLEAN_LITERAL:
                    newTree = make.Literal(!(Boolean) ((LiteralTree) original).getValue());
                    break;
                case CONDITIONAL_AND:
                    newTree = negateBinaryOperator(original, Kind.CONDITIONAL_OR, true);
                    break;
                case CONDITIONAL_OR:
                    newTree = negateBinaryOperator(original, Kind.CONDITIONAL_AND, true);
                    break;
                case LESS_THAN:
                    newTree = negateBinaryOperator(original, Kind.GREATER_THAN_EQUAL, false);
                    break;
                case LESS_THAN_EQUAL:
                    newTree = negateBinaryOperator(original, Kind.GREATER_THAN, false);
                    break;
                case GREATER_THAN:
                    newTree = negateBinaryOperator(original, Kind.LESS_THAN_EQUAL, false);
                    break;
                case GREATER_THAN_EQUAL:
                    newTree = negateBinaryOperator(original, Kind.LESS_THAN, false);
                    break;
                default:
                    if (nullOnPlainNeg)
                        return null;
                    newTree = make.Unary(Kind.LOGICAL_COMPLEMENT, original);
            }
         
            if (JavaFixUtilities.requiresParenthesis(newTree, original, parent)) {
                newTree = make.Parenthesized(newTree);
            }
            
            return newTree;
        }
        
        private ExpressionTree negateBinaryOperator(Tree original, Kind newKind, boolean negateOperands) {
            BinaryTree bt = (BinaryTree) original;
            BinaryTree nonNegated = make.Binary(newKind,
                                                bt.getLeftOperand(),
                                                bt.getRightOperand());
            if (negateOperands) {
                ExpressionTree lo = negate(bt.getLeftOperand(), nonNegated, false);
                ExpressionTree ro = negate(bt.getRightOperand(), nonNegated, false);
                return make.Binary(newKind,
                                   lo != null ? lo : bt.getLeftOperand(),
                                   ro != null ? ro : bt.getRightOperand());
            }
            return nonNegated;
        }
        
        private void rewrite(Tree from, Tree to) {
            if (originalTrees.contains(from)) return ;
            rewriteFromTo.put(from, to);
            order.add(from);
        }
    }

    private static final Map<Kind, Integer> OPERATOR_PRIORITIES;
    
    static {
        OPERATOR_PRIORITIES = new EnumMap<>(Kind.class);

        OPERATOR_PRIORITIES.put(Kind.IDENTIFIER, 0);

        for (Kind k : Kind.values()) {
            if (k.asInterface() == LiteralTree.class) {
                OPERATOR_PRIORITIES.put(k, 0);
            }
        }

        OPERATOR_PRIORITIES.put(Kind.ARRAY_ACCESS, 1);
        OPERATOR_PRIORITIES.put(Kind.METHOD_INVOCATION, 1);
        OPERATOR_PRIORITIES.put(Kind.MEMBER_REFERENCE, 1);
        OPERATOR_PRIORITIES.put(Kind.MEMBER_SELECT, 1);
        OPERATOR_PRIORITIES.put(Kind.POSTFIX_DECREMENT, 1);
        OPERATOR_PRIORITIES.put(Kind.POSTFIX_INCREMENT, 1);
        OPERATOR_PRIORITIES.put(Kind.NEW_ARRAY, 1);
        OPERATOR_PRIORITIES.put(Kind.NEW_CLASS, 1);

        OPERATOR_PRIORITIES.put(Kind.BITWISE_COMPLEMENT, 2);
        OPERATOR_PRIORITIES.put(Kind.LOGICAL_COMPLEMENT, 2);
        OPERATOR_PRIORITIES.put(Kind.PREFIX_DECREMENT, 2);
        OPERATOR_PRIORITIES.put(Kind.PREFIX_INCREMENT, 2);
        OPERATOR_PRIORITIES.put(Kind.UNARY_MINUS, 2);
        OPERATOR_PRIORITIES.put(Kind.UNARY_PLUS, 2);

        OPERATOR_PRIORITIES.put(Kind.TYPE_CAST, 3);

        OPERATOR_PRIORITIES.put(Kind.DIVIDE, 4);
        OPERATOR_PRIORITIES.put(Kind.MULTIPLY, 4);
        OPERATOR_PRIORITIES.put(Kind.REMAINDER, 4);

        OPERATOR_PRIORITIES.put(Kind.MINUS, 5);
        OPERATOR_PRIORITIES.put(Kind.PLUS, 5);

        OPERATOR_PRIORITIES.put(Kind.LEFT_SHIFT, 6);
        OPERATOR_PRIORITIES.put(Kind.RIGHT_SHIFT, 6);
        OPERATOR_PRIORITIES.put(Kind.UNSIGNED_RIGHT_SHIFT, 6);

        OPERATOR_PRIORITIES.put(Kind.INSTANCE_OF, 7);
        OPERATOR_PRIORITIES.put(Kind.GREATER_THAN, 7);
        OPERATOR_PRIORITIES.put(Kind.GREATER_THAN_EQUAL, 7);
        OPERATOR_PRIORITIES.put(Kind.LESS_THAN, 7);
        OPERATOR_PRIORITIES.put(Kind.LESS_THAN_EQUAL, 7);

        OPERATOR_PRIORITIES.put(Kind.EQUAL_TO, 8);
        OPERATOR_PRIORITIES.put(Kind.NOT_EQUAL_TO, 8);

        OPERATOR_PRIORITIES.put(Kind.AND, 9);
        OPERATOR_PRIORITIES.put(Kind.OR, 11);
        OPERATOR_PRIORITIES.put(Kind.XOR, 10);

        OPERATOR_PRIORITIES.put(Kind.CONDITIONAL_AND, 12);
        OPERATOR_PRIORITIES.put(Kind.CONDITIONAL_OR, 13);

        OPERATOR_PRIORITIES.put(Kind.CONDITIONAL_EXPRESSION, 14);

        OPERATOR_PRIORITIES.put(Kind.AND_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.DIVIDE_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.LEFT_SHIFT_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.MINUS_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.MULTIPLY_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.OR_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.PLUS_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.REMAINDER_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.RIGHT_SHIFT_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, 15);
        OPERATOR_PRIORITIES.put(Kind.XOR_ASSIGNMENT, 15);
    }

    /**
     * Checks whether {@code tree} can be used in places where a Primary is
     * required (for instance, as the receiver expression of a method invocation).
     * <p>This is a friend API intended to be used by the java.hints module.
     * Other modules should not use this API because it might not be stabilized.
     * @param tree the tree to check
     * @return {@code true} iff {@code tree} can be used where a Primary is
     * required.
     * @since 1.31
     */
    public static boolean isPrimary(@NonNull Tree tree) {
        final Integer treePriority = OPERATOR_PRIORITIES.get(tree.getKind());
        return (treePriority != null && treePriority <= 1);
    }

    /**Checks whether putting {@code inner} tree into {@code outter} tree,
     * when {@code original} is being replaced with {@code inner} requires parentheses.
     *
     * @param inner    the new tree node that will be placed under {@code outter}
     * @param original the tree node that is being replaced with {@code inner}
     * @param outter   the future parent node of {@code inner}
     * @return true if and only if inner needs to be wrapped using {@link TreeMaker#Parenthesized(com.sun.source.tree.ExpressionTree) }
     *              to keep the original meaning.
     */
    public static boolean requiresParenthesis(Tree inner, Tree original, Tree outter) {
        if (!ExpressionTree.class.isAssignableFrom(inner.getKind().asInterface()) || outter == null) return false;
        if (!ExpressionTree.class.isAssignableFrom(outter.getKind().asInterface())) {
            boolean condition = false;
            switch (outter.getKind()) {
                case IF:
                    condition = original == ((IfTree)outter).getCondition();
                    break;
                case WHILE_LOOP:
                    condition = original == ((WhileLoopTree)outter).getCondition();
                    break;
                case DO_WHILE_LOOP:
                    condition = original == ((DoWhileLoopTree)outter).getCondition();
                    break;
            }
            return condition && inner.getKind() != Tree.Kind.PARENTHESIZED;
        }

        if (outter.getKind() == Kind.PARENTHESIZED || inner.getKind() == Kind.PARENTHESIZED) return false;

        if (outter.getKind() == Kind.METHOD_INVOCATION) {
            if (((MethodInvocationTree) outter).getArguments().contains(original)) return false;
        }
        
        if (outter.getKind() == Kind.LAMBDA_EXPRESSION) {
            LambdaExpressionTree lt = ((LambdaExpressionTree)outter);
            if (lt.getParameters().contains(original)) {
                return false;
            }
            if (lt.getBodyKind() == LambdaExpressionTree.BodyKind.STATEMENT) {
                return false;
            }
            return original.getKind() == Tree.Kind.PARENTHESIZED;
        }

        if (outter.getKind() == Kind.NEW_CLASS) {
            if (((NewClassTree) outter).getArguments().contains(original)) return false;
        }

        Integer innerPriority = OPERATOR_PRIORITIES.get(inner.getKind());
        Integer outterPriority = OPERATOR_PRIORITIES.get(outter.getKind());

        if (innerPriority == null || outterPriority == null) {
            Logger.getLogger(JavaFix.class.getName()).log(Level.WARNING, "Unknown tree kind(s): {0}/{1}", new Object[] {inner.getKind(), outter.getKind()});
            return true;
        }

        if (innerPriority > outterPriority) {
            return true;
        }

        if (innerPriority < outterPriority) {
            return false;
        }

        //associativity
        if (BinaryTree.class.isAssignableFrom(outter.getKind().asInterface())) {
            BinaryTree ot = (BinaryTree) outter;

            //TODO: for + it might be possible to skip the parenthesis:
            return ot.getRightOperand() == original;
        }

        if (CompoundAssignmentTree.class.isAssignableFrom(outter.getKind().asInterface())) {
            CompoundAssignmentTree ot = (CompoundAssignmentTree) outter;

            return ot.getVariable() == original;
        }

        if (AssignmentTree.class.isAssignableFrom(outter.getKind().asInterface())) {
            AssignmentTree ot = (AssignmentTree) outter;

            return ot.getVariable() == original;
        }

        return false;
    }

    private static final class RemoveFromParent extends JavaFix {

        private final String displayName;
        private final boolean safely;

        public RemoveFromParent(String displayName, CompilationInfo info, TreePath toRemove, boolean safely) {
            super(info, toRemove);
            this.displayName = displayName;
            this.safely = safely;
        }

        @Override
        protected String getText() {
            return displayName;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            
            doRemoveFromParent(wc, tp);
            if (safely) {
                Element el = wc.getTrees().getElement(tp);
                if (el != null) {
                    new TreePathScanner<Void, Void>() {
                        @Override
                        public Void scan(Tree tree, Void p) {
                            if (tree != null && tree != tp.getLeaf()) {
                                TreePath treePath = new TreePath(getCurrentPath(), tree);
                                Element e = wc.getTrees().getElement(treePath);
                                if (el == e) {
                                    doRemoveFromParent(wc, treePath);
                                }
                            }
                            return super.scan(tree, p);
                        }
                    }.scan(new TreePath(wc.getCompilationUnit()), null);
                }
            }
        }
        
        private void doRemoveFromParent(WorkingCopy wc, TreePath what) {
            TreeMaker make = wc.getTreeMaker();
            Tree leaf = what.getLeaf();
            Tree parentLeaf = what.getParentPath().getLeaf();

            switch (parentLeaf.getKind()) {
                case ANNOTATION:
                    AnnotationTree at = (AnnotationTree) parentLeaf;
                    AnnotationTree newAnnot;

                    newAnnot = make.removeAnnotationAttrValue(at, (ExpressionTree) leaf);

                    wc.rewrite(at, newAnnot);
                    break;
                case BLOCK:
                    BlockTree bt = (BlockTree) parentLeaf;

                    wc.rewrite(bt, make.removeBlockStatement(bt, (StatementTree) leaf));
                    break;
                case CASE:
                    CaseTree caseTree = (CaseTree) parentLeaf;

                    wc.rewrite(caseTree, make.removeCaseStatement(caseTree, (StatementTree) leaf));
                    break;
                case CLASS:
                    ClassTree classTree = (ClassTree) parentLeaf;
                    ClassTree nueClassTree;

                    if (classTree.getTypeParameters().contains(leaf)) {
                        nueClassTree = make.removeClassTypeParameter(classTree, (TypeParameterTree) leaf);
                    } else if (classTree.getExtendsClause() == leaf) {
                        nueClassTree = make.Class(classTree.getModifiers(), classTree.getSimpleName(), classTree.getTypeParameters(), null, classTree.getImplementsClause(), classTree.getMembers());
                    } else if (classTree.getImplementsClause().contains(leaf)) {
                        nueClassTree = make.removeClassImplementsClause(classTree, leaf);
                    } else if (classTree.getMembers().contains(leaf)) {
                        nueClassTree = make.removeClassMember(classTree, leaf);
                    } else {
                        throw new UnsupportedOperationException();
                    }

                    wc.rewrite(classTree, nueClassTree);
                    break;
                case UNION_TYPE:
                    UnionTypeTree disjunct = (UnionTypeTree) parentLeaf;
                    List<? extends Tree> alternatives = new LinkedList<Tree>(disjunct.getTypeAlternatives());

                    alternatives.remove(leaf);

                    wc.rewrite(disjunct, make.UnionType(alternatives));
                    break;
                case METHOD:
                    MethodTree mTree = (MethodTree) parentLeaf;
                    MethodTree newMethod;

                    if (mTree.getTypeParameters().contains(leaf)) {
                        newMethod = make.removeMethodTypeParameter(mTree, (TypeParameterTree) leaf);
                    } else if (mTree.getParameters().contains(leaf)) {
                        newMethod = make.removeMethodParameter(mTree, (VariableTree) leaf);
                    } else if (mTree.getThrows().contains(leaf)) {
                        newMethod = make.removeMethodThrows(mTree, (ExpressionTree) leaf);
                    } else {
                        throw new UnsupportedOperationException();
                    }

                    wc.rewrite(mTree, newMethod);
                    break;
                case METHOD_INVOCATION:
                    MethodInvocationTree iTree = (MethodInvocationTree) parentLeaf;
                    MethodInvocationTree newInvocation;

                    if (iTree.getTypeArguments().contains(leaf)) {
                        newInvocation = make.removeMethodInvocationTypeArgument(iTree, (ExpressionTree) leaf);
                    } else if (iTree.getArguments().contains(leaf)) {
                        newInvocation = make.removeMethodInvocationArgument(iTree, (ExpressionTree) leaf);
                    } else {
                        throw new UnsupportedOperationException();
                    }

                    wc.rewrite(iTree, newInvocation);
                    break;
                case MODIFIERS:
                    ModifiersTree modsTree = (ModifiersTree) parentLeaf;

                    wc.rewrite(modsTree, make.removeModifiersAnnotation(modsTree, (AnnotationTree) leaf));
                    break;
                case NEW_CLASS:
                    NewClassTree newCTree = (NewClassTree) parentLeaf;
                    NewClassTree newNCT;

                    if (newCTree.getTypeArguments().contains(leaf)) {
                        newNCT = make.removeNewClassTypeArgument(newCTree, (ExpressionTree) leaf);
                    } else if (newCTree.getArguments().contains(leaf)) {
                        newNCT = make.removeNewClassArgument(newCTree, (ExpressionTree) leaf);
                    } else {
                        throw new UnsupportedOperationException();
                    }

                    wc.rewrite(newCTree, newNCT);
                    break;
                case PARAMETERIZED_TYPE:
                    ParameterizedTypeTree parTree = (ParameterizedTypeTree) parentLeaf;

                    wc.rewrite(parTree, make.removeParameterizedTypeTypeArgument(parTree, (ExpressionTree) leaf));
                    break;
                case SWITCH:
                    SwitchTree switchTree = (SwitchTree) parentLeaf;
                    SwitchTree newSwitch;

                    if (switchTree.getCases().contains(leaf)) {
                        newSwitch = make.removeSwitchCase(switchTree, (CaseTree) leaf);
                    } else {
                        throw new UnsupportedOperationException();
                    }

                    wc.rewrite(switchTree, newSwitch);
                    break;
                case TRY:
                    TryTree tryTree = (TryTree) parentLeaf;
                    TryTree newTry;

                    if (tryTree.getResources().contains(leaf)) {
                        LinkedList<Tree> resources = new LinkedList<>(tryTree.getResources());

                        resources.remove(leaf);

                        newTry = make.Try(resources, tryTree.getBlock(), tryTree.getCatches(), tryTree.getFinallyBlock());
                    } else if (tryTree.getCatches().contains(leaf)) {
                        newTry = make.removeTryCatch(tryTree, (CatchTree) leaf);
                    } else {
                        throw new UnsupportedOperationException();
                    }

                    wc.rewrite(tryTree, newTry);
                    break;
                case EXPRESSION_STATEMENT:
                    doRemoveFromParent(wc, what.getParentPath());
                    break;
                case ASSIGNMENT:
                    AssignmentTree assignmentTree = (AssignmentTree) parentLeaf;
                    if (leaf == assignmentTree.getVariable()) {
                        if (wc.getTreeUtilities().isExpressionStatement(assignmentTree.getExpression())) {
                            wc.rewrite(parentLeaf, assignmentTree.getExpression());
                        } else {
                            doRemoveFromParent(wc, what.getParentPath());
                        }
                    } else {
                        throw new UnsupportedOperationException();
                    }
                    break;
                case AND_ASSIGNMENT:
                case DIVIDE_ASSIGNMENT:
                case LEFT_SHIFT_ASSIGNMENT:
                case MINUS_ASSIGNMENT:
                case MULTIPLY_ASSIGNMENT:
                case OR_ASSIGNMENT:
                case PLUS_ASSIGNMENT:
                case REMAINDER_ASSIGNMENT:
                case RIGHT_SHIFT_ASSIGNMENT:
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                case XOR_ASSIGNMENT:
                    CompoundAssignmentTree compoundAssignmentTree = (CompoundAssignmentTree) parentLeaf;
                    if (leaf == compoundAssignmentTree.getVariable()) {
                        if (wc.getTreeUtilities().isExpressionStatement(compoundAssignmentTree.getExpression())) {
                            wc.rewrite(parentLeaf, compoundAssignmentTree.getExpression());
                        } else {
                            doRemoveFromParent(wc, what.getParentPath());
                        }
                    } else {
                        throw new UnsupportedOperationException();
                    }
                    break;
                default:
                    wc.rewrite(what.getLeaf(), make.Block(Collections.<StatementTree>emptyList(), false));
                    break;
            }
        }

        private static boolean canSafelyRemove(CompilationInfo info, TreePath tp) {
            AtomicBoolean ret = new AtomicBoolean(true);
            Element el = info.getTrees().getElement(tp);
            if (el != null) {
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void scan(Tree tree, Void p) {
                        if (tree != null && tree != tp.getLeaf()) {
                            TreePath treePath = new TreePath(getCurrentPath(), tree);
                            Element e = info.getTrees().getElement(treePath);
                            if (el == e) {
                                Tree parentLeaf = treePath.getParentPath().getLeaf();
                                switch (parentLeaf.getKind()) {
                                    case ASSIGNMENT:
                                        AssignmentTree assignmentTree = (AssignmentTree) parentLeaf;
                                        if (tree == assignmentTree.getVariable()) {
                                            if (!info.getTreeUtilities().isExpressionStatement(assignmentTree.getExpression()) && canHaveSideEffects(assignmentTree.getExpression())) {
                                                ret.set(false);
                                            }
                                        } else {
                                            ret.set(false);
                                        }
                                        break;
                                    case AND_ASSIGNMENT:
                                    case DIVIDE_ASSIGNMENT:
                                    case LEFT_SHIFT_ASSIGNMENT:
                                    case MINUS_ASSIGNMENT:
                                    case MULTIPLY_ASSIGNMENT:
                                    case OR_ASSIGNMENT:
                                    case PLUS_ASSIGNMENT:
                                    case REMAINDER_ASSIGNMENT:
                                    case RIGHT_SHIFT_ASSIGNMENT:
                                    case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                                    case XOR_ASSIGNMENT:
                                        CompoundAssignmentTree compoundAssignmentTree = (CompoundAssignmentTree) parentLeaf;
                                        if (tree == compoundAssignmentTree.getVariable()) {
                                            if (!info.getTreeUtilities().isExpressionStatement(compoundAssignmentTree.getExpression()) && canHaveSideEffects(compoundAssignmentTree.getExpression())) {
                                                ret.set(false);
                                            }
                                        } else {
                                            ret.set(false);
                                        }
                                        break;
                                    default:
                                        ret.set(false);
                                }
                            }
                        }
                        return super.scan(tree, p);
                    }
                }.scan(new TreePath(info.getCompilationUnit()), null);
            }
            return ret.get();
        }
        
        private static boolean canHaveSideEffects(Tree tree) {
            AtomicBoolean ret = new AtomicBoolean();
            new TreeScanner<Void, Void>() {
                @Override
                public Void scan(Tree tree, Void p) {
                    if (tree != null) {
                        switch (tree.getKind()) {
                            case METHOD_INVOCATION:
                            case NEW_CLASS:
                            case POSTFIX_DECREMENT:
                            case POSTFIX_INCREMENT:
                            case PREFIX_DECREMENT:
                            case PREFIX_INCREMENT:
                                ret.set(true);
                                break;
                        }
                    }
                    return super.scan(tree, p);
                }
            }.scan(tree, null);
            return ret.get();
        }
    }

    //TODO: from FileMovePlugin
    private static class MoveFile extends SimpleRefactoringElementImplementation {

        private FileObject toMove;
        private final FileObject sourceRoot;
        private final String targetFolderName;

        public MoveFile(FileObject toMove, FileObject sourceRoot, String targetFolderName) {
            this.toMove = toMove;
            this.sourceRoot = sourceRoot;
            this.targetFolderName = targetFolderName;
        }

        @Override
        @Messages({"#{0} - original file name", "TXT_MoveFile=Move {0}"})
        public String getText() {
            return Bundle.TXT_MoveFile(toMove.getNameExt());
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        DataFolder sourceFolder;
        DataObject source;
        @Override
        public void performChange() {
            try {
                FileObject target = FileUtil.createFolder(sourceRoot, targetFolderName);
                DataFolder targetFolder = DataFolder.findFolder(target);
                if (!toMove.isValid()) {
                    String path = FileUtil.getFileDisplayName(toMove);
                    Logger.getLogger(JavaFix.class.getName()).fine("Invalid FileObject " + path + "trying to recreate...");
                    toMove = FileUtil.toFileObject(FileUtil.toFile(toMove));
                    if (toMove==null) {
                        Logger.getLogger(JavaFix.class.getName()).severe("Invalid FileObject " + path + "\n. File not found.");
                        return;
                    }
                }
                source = DataObject.find(toMove);
                sourceFolder = source.getFolder();
                source.move(targetFolder);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void undoChange() {
            try {
                source.move(sourceFolder);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return toMove;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }
    }
}
