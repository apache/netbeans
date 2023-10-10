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

package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
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
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.tree.YieldTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.support.CancellableTreeScanner;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.HintContext;

/**
 *
 * @author lahvac
 */
public class Flow {

    public static FlowResult assignmentsForUse(CompilationInfo info, AtomicBoolean cancel) {
        return assignmentsForUse(info, new AtomicBooleanCancel(cancel));
    }

    public static FlowResult assignmentsForUse(CompilationInfo info, TreePath from, AtomicBoolean cancel) {
        return assignmentsForUse(info, from, new AtomicBooleanCancel(cancel));
    }

    public static FlowResult assignmentsForUse(final HintContext ctx) {
        return Flow.assignmentsForUse(ctx.getInfo(), new Cancel() {
            @Override
            public boolean isCanceled() {
                return ctx.isCanceled();
            }
        });
    }
    
    private static final Object KEY_FLOW = new Object();
    
    public static FlowResult assignmentsForUse(CompilationInfo info, Cancel cancel) {
        FlowResult result = (FlowResult) info.getCachedValue(KEY_FLOW);
        
        if (result == null) {
            result = assignmentsForUse(info, new TreePath(info.getCompilationUnit()), cancel);
            
            if (result != null && !cancel.isCanceled()) {
                info.putCachedValue(KEY_FLOW, result, CacheClearPolicy.ON_TASK_END);
            }
        }
        
        return result;
    }
    
    public static FlowResult assignmentsForUse(CompilationInfo info, TreePath from, Cancel cancel) {
        VisitorImpl v = new VisitorImpl(info, null, cancel);

        v.scan(from, null);

        if (cancel.isCanceled()) return null;
        return new FlowResult(v);
    }
    
    public static final class FlowResult {
        /**
         * Contains map of identifier usage (Tree) -> potential values
         */
        private final Map<Tree, Iterable<? extends TreePath>> assignmentsForUse;
        private final Set<? extends Tree> deadBranches;
        private final Set<VariableElement> finalCandidates;
        private Map<Tree, TreePath> locations;
        private volatile Map<Tree, Collection<Tree>> dataFlow;
        private final Map<Element, Collection<Element>> finalFieldConstructors;
        
        private FlowResult(VisitorImpl v) { // Map<Tree, Iterable<? extends TreePath>> assignmentsForUse, Set<Tree> deadBranches, Set<VariableElement> finalCandidates) {
            Map<Tree, Iterable<? extends TreePath>> result = new HashMap<Tree, Iterable<? extends TreePath>>();
            for (Entry<Tree, State> e : v.use2Values.entrySet()) {
                result.put(e.getKey(), e.getValue() != null ? e.getValue().assignments : Collections.<TreePath>emptyList());
            }
            Set<Element> fc = v.finalCandidates;
            // PENDING: optimize case with empty fc
            Set<VariableElement> finalCandidates = new HashSet<VariableElement>(fc.size());
            for (Element e : fc) {
                if (SUPPORTED_VARIABLES.contains(e.getKind())) {
                    finalCandidates.add((VariableElement)e);
                }
            }
            v.deadBranches.remove(null);
            finalCandidates.removeAll(v.usedWhileUndefined);
            v.finalFieldConstructors.keySet().retainAll(finalCandidates);
            
            this.assignmentsForUse = Collections.unmodifiableMap(result);
            this.deadBranches = Collections.unmodifiableSet(v.deadBranches);
            this.finalCandidates = Collections.unmodifiableSet(finalCandidates);
            // the collection is never served out of the FlowResult
            this.finalFieldConstructors = v.finalFieldConstructors;
        }
        
        public Collection<Element> getFieldInitConstructors(Element var) {
            Collection<Element> els = finalFieldConstructors.get(var);
            return els == null ? Collections.<Element>emptyList() : Collections.unmodifiableCollection(els);
        }
        
        public Map<Tree, Iterable<? extends TreePath>> getAssignmentsForUse() {
            return assignmentsForUse;
        }
        public Set<? extends Tree> getDeadBranches() {
            return deadBranches;
        }
        public Set<VariableElement> getFinalCandidates() {
            return finalCandidates;
        }
        
        public Collection<Tree> getValueUsers(Tree val) {
            if (dataFlow == null) {
                computeDataFlow();
            }
            Collection<Tree> c = dataFlow.get(val);
            return c == null ? Collections.<Tree>emptyList(): c;
        }
        
        public TreePath findPath(Tree node, CompilationUnitTree cut) {
            if (locations == null) {
                Map<Tree, TreePath> locs = new IdentityHashMap<Tree, TreePath>(assignmentsForUse.size());
                PathFinder pf = new PathFinder(locs, assignmentsForUse.keySet());
                pf.scan(new TreePath(cut), null);
                locations = locs;
            }
            return locations.get(node);
        }
        
        private void computeDataFlow() {
            Map<Tree, Collection<Tree>> res = new IdentityHashMap<Tree, Collection<Tree>>(7);
            for (Map.Entry<Tree, Iterable<? extends TreePath>> e : assignmentsForUse.entrySet()) {
                Tree k = e.getKey();
                for (TreePath p : e.getValue()) {
                    if (p == null) {
                        continue;
                    }
                    Tree l = p.getLeaf();
                    Collection<Tree> users = res.get(l);
                    if (users == null) {
                        users = new ArrayList<Tree>(2);
                        res.put(l, users);
                    }
                    users.add(k);
                }
            }
            dataFlow = res;
        }

    }
    
    private static class PathFinder extends ErrorAwareTreePathScanner {
        final Map<Tree, TreePath> node2Path;
        final Set<Tree> interestingNodes;

        public PathFinder(Map<Tree, TreePath> node2Path, Set<Tree> interestingNodes) {
            this.node2Path = node2Path;
            this.interestingNodes = interestingNodes;
        }
        
        @Override
        public Object scan(Tree tree, Object p) {
            if (interestingNodes.contains(tree)) {
                node2Path.put(tree, new TreePath(getCurrentPath(), tree));
            }
            return super.scan(tree, p);
        }
        
    }

    public interface Cancel {
        public boolean isCanceled();
    }

    public static final class AtomicBooleanCancel implements Cancel {

        private final AtomicBoolean cancel;

        public AtomicBooleanCancel(AtomicBoolean cancel) {
            this.cancel = cancel;
        }

        @Override
        public boolean isCanceled() {
            return cancel.get();
        }

    }

    public static boolean definitellyAssigned(CompilationInfo info, VariableElement var, Iterable<? extends TreePath> trees, AtomicBoolean cancel) {
        return definitellyAssigned(info, var, trees, new AtomicBooleanCancel(cancel));
    }

    /**
     * This is a variant of {@link #definitellyAssigned(org.netbeans.api.java.source.CompilationInfo, javax.lang.model.element.VariableElement, java.lang.Iterable, java.util.concurrent.atomic.AtomicBoolean)},
     * that allows to inspect a pseudo-variable that corresponds to an undefined symbol.
     * 
     * @param scope scope where the undefined symbol will be created. It's used to identify
     * aliases in nested classes.
     * @return true, if the 'var' symbol is definitely assigned.
     */
    public static boolean unknownSymbolFinalCandidate(CompilationInfo info, 
            Element var, TypeElement scope, Iterable<? extends TreePath> trees, AtomicBoolean cancel) {
        return definitellyAssignedImpl(info, var, scope, trees, false, new AtomicBooleanCancel(cancel));
    }
    
    private static boolean definitellyAssignedImpl(CompilationInfo info, 
            Element var, TypeElement scope, 
            Iterable<? extends TreePath> trees, boolean reassignAllowed, Cancel cancel) {
        VisitorImpl v = new VisitorImpl(info, scope, cancel);
        if (scope != null) {
            v.canonicalUndefined(var);
        }
        v.variable2State.put(var, State.create(null, false));

        for (TreePath tp : trees) {
            if (cancel.isCanceled()) return false;
            
            v.scan(tp, null);
            
            TreePath toResume = tp;
            
            while (toResume != null) {
                v.resume(toResume.getLeaf(), v.resumeAfter);
                toResume = toResume.getParentPath();
            }

            State s = v.variable2State.get(var);
            if (s == null) {
                s = v.variable2StateFinal.get(var);
            }
            if (s != null && !s.assignments.contains(null)) {
                return reassignAllowed || !s.reassigned;
            }
        }

        return false;
    }
    
    public static boolean definitellyAssigned(CompilationInfo info, VariableElement var, Iterable<? extends TreePath> trees, Cancel cancel) {
        return definitellyAssignedImpl(info, var, null, trees, true, cancel);
    }
    
    private static class AV {
        final TreePath path;
        final State   state;

        public AV(TreePath path, State state) {
            this.path = path;
            this.state = state;
        }
        
    }

    /**
     * Unresolved variable analysis: currently only the definitive assignment analysis can actually work
     * with undefined symbols. First the {@link #undefinedSymbolScope} is set up. Each L-value unresolved symbol
     * without a qualifier is thought of as belonging to that scope - symbols are aliased by name. Unresolved symbols
     * that are qualified to belong to other scopes are ignored at the moment.
     * 
     */
    private static final class VisitorImpl extends CancellableTreeScanner<Boolean, ConstructorData> {
        
        private final CompilationInfo info;
        private final TypeElement undefinedSymbolScope;
        // just an optimization for comparisons
        private final Name thisName;
        /**
         * Undefined variables found in the {@link #undefinedSymbolScope}. Does not contain entries
         * for undefined variables in other scopes.
         */
        private Map<Name, Element> undefinedVariables = new HashMap<Name, Element>();

        /**
         * Tracks variables which are in scope. When a variable goes out of scope, its state (if any) moves
         * to {@link #variable2StateFinal}.
         */
        private Map<Element, State> variable2State = new HashMap<Element, Flow.State>();

        /**
         * Finalized state of variables. Records for variables, which go out of scope is collected here.
         */
        private Map<Element, State> variable2StateFinal = new HashMap<Element, Flow.State>();
        private Map<Tree, State> use2Values = new IdentityHashMap<Tree, State>();
        private Map<Tree, Collection<Map<Element, State>>> resumeBefore = new IdentityHashMap<Tree, Collection<Map<Element, State>>>();
        private Map<Tree, Collection<Map<Element, State>>> resumeAfter = new IdentityHashMap<Tree, Collection<Map<Element, State>>>();
        private Map<TypeMirror, Map<Element, State>> resumeOnExceptionHandler = new IdentityHashMap<TypeMirror, Map<Element, State>>();
        private boolean inParameters;
        private Tree nearestMethod;
        private Set<Element> currentMethodVariables = Collections.newSetFromMap(new IdentityHashMap<Element, Boolean>());
        private final Set<Tree> deadBranches = new HashSet<Tree>();
        private final List<TreePath> pendingFinally = new LinkedList<TreePath>();
        private final Cancel cancel;
        private boolean doNotRecord;
        private /*Map<ClassTree, */Set<Element> finalCandidates = new ReluctantSet<>();
        private final Set<Element> usedWhileUndefined = new HashSet<Element>();
        /**
         * For field final candidates, this Map contains all constructors, which initialize the field. Used
         * for checking whether the final transformation can be done easily.
         */
        private final Map<Element, Collection<Element>> finalFieldConstructors = new HashMap<Element, Collection<Element>>();

        /**
         * The target type for a qualified L-value reference. Null for unqualified symbols.
         * Undefined (null or non-null) in other cases.
         */
        private TypeElement referenceTarget;

        /**
         * Blocks 'use' marks for visited variables; indicates that lValue is being processed.
         */
        private boolean lValueDereference;

        private final TypeElement throwableEl;
        private final TypeMirror runtimeExceptionType;
        private final TypeMirror errorType;
        
        /**
         * For a Tree, collects variables scoped into that tree. The Map is used
         * when the traversal exits the tree, to clean up variables that go out of scope from
         * variable2State, so state cloning takes less memory
         */
        private final Map<Tree, Collection<Element>> scopedVariables = new IdentityHashMap<Tree, Collection<Element>>();
                

        public VisitorImpl(CompilationInfo info, TypeElement undefinedSymbolScope, Cancel cancel) {
            super();
            this.info = info;
            this.cancel = cancel;
            this.undefinedSymbolScope = undefinedSymbolScope;
            this.thisName = info.getElements().getName("this");

            this.throwableEl = info.getElements().getTypeElement("java.lang.Throwable"); // NOI18N
            TypeElement tel =  info.getElements().getTypeElement("java.lang.RuntimeException"); // NOI18N
            if (tel != null) {
                runtimeExceptionType = tel.asType();
            } else {
                runtimeExceptionType = null;
            }
            tel =  info.getElements().getTypeElement("java.lang.Error"); // NOI18N
            if (tel != null) {
                errorType = tel.asType();
            } else {
                errorType = null;
            }
        }

        @Override
        protected boolean isCanceled() {
            return cancel.isCanceled();
        }

        private TreePath currentPath;

        public TreePath getCurrentPath() {
            return currentPath;
        }

        public Boolean scan(TreePath path, ConstructorData p) {
            TreePath oldPath = currentPath;
            try {
                currentPath = path;
                return super.scan(path.getLeaf(), p);
            } finally {
                currentPath = oldPath;
            }
        }

        @Override
        public Boolean scan(Tree tree, ConstructorData p) {
            resume(tree, resumeBefore);
            
            Boolean result;

            if (tree != null) {
                TreePath oldPath = currentPath;
                try {
                    currentPath = new TreePath(currentPath, tree);
                    result = super.scan(tree, p);
                } finally {
                    currentPath = oldPath;
                }
            } else {
                result = null;
            }

            resume(tree, resumeAfter);
            
            Collection<Element> varsOutScope = scopedVariables.get(tree);
            if (varsOutScope != null) {
                for (Element ve : varsOutScope) {
                    State s = variable2State.get(ve);
                    if (s != null) {
                        variable2StateFinal.put(ve, s);
                    }
                }
                variable2State.keySet().removeAll(varsOutScope);
            }
            return result;
        }

        private void resume(Tree tree, Map<Tree, Collection<Map<Element, State>>> resume) {
            // note: the 'tree' might be just a head of a Tree list, i.e. 1st item of update statment list in a for-loop.
            Collection<Map<Element, State>> toResume = resume.remove(tree);

            if (toResume != null) {
                for (Map<Element, State> s : toResume) {
                    variable2State = mergeOr(variable2State, s);
                }
            }
        }

        @Override
        public Boolean visitAssignment(AssignmentTree node, ConstructorData p) {
            TypeElement oldQName = this.referenceTarget;
            this.referenceTarget = null;
            lValueDereference = true;
            scan(node.getVariable(), null);
            lValueDereference = false;

            Boolean constVal = scan(node.getExpression(), p);

            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
            
            if (e != null) {
                if (SUPPORTED_VARIABLES.contains(e.getKind())) {
                    recordVariableState(e, new TreePath(getCurrentPath(), node.getExpression()));
                } else if (shouldProcessUndefined(e)) {
                    Element cv = canonicalUndefined(e);
                    recordVariableState(e, new TreePath(getCurrentPath(), node.getExpression()));
                }
            }
            this.referenceTarget = oldQName;
            return constVal;
        }
        
        
        private void recordVariableState(Element ve, TreePath tp) {
             State prevState = variable2State.get(ve);
             variable2State.put(ve, State.create(tp, prevState));
        }
 
        /**
         * Returns an alias for the Element, if the undefined name was already found. Returns e
         * and causes further names like 'e' to be aliased to e instance. This cannonicalization is
         * used to support collection operations throughout the flow
         */
        private Element canonicalUndefined(Element e) {
            Name n = e.getSimpleName();
            Element prev = undefinedVariables.get(n);
            if (prev != null) {
                return prev;
            } else {
                undefinedVariables.put(n, e);
                return e;
            }
        }
        
        private boolean isUndefinedVariable(Element e) {
            Name n = e.getSimpleName();
            return undefinedVariables.containsKey(n);
        }
        
        /**
         * Determines if the Element should be processed as an undefined variable. Currently the implementation
         * only returns true if an undefinedSymbolScope is set, AND the undefined symbol might belong to that scope.
         * If the undefined symbol is qualified (by this. or class.this. or whatever else manner), the method will
         * only return true if the qualifier corresponds to the undefinedSymbolScope.
         * 
         */
        private boolean shouldProcessUndefined(Element e) {
            if (e == null || undefinedSymbolScope == null ||
                e.asType().getKind() != TypeKind.ERROR) {
                return false;
            }
            if (e.getKind() == ElementKind.CLASS) {
                return referenceTarget == null || 
                    referenceTarget == undefinedSymbolScope;
            }
            return false;
        }
        
        private void addUse2Values(Tree place, State prevState) {
            State s = use2Values.get(place);
            if (true && s == null) {
                use2Values.put(place, prevState);
            } else {
                use2Values.put(place, s.merge(prevState));
            }
        }
        
        @Override
        public Boolean visitCompoundAssignment(CompoundAssignmentTree node, ConstructorData p) {
            TypeElement oldQName = this.referenceTarget;
            this.referenceTarget = null;

            lValueDereference = true;
            scan(node.getVariable(), null);
            lValueDereference = false;

            Boolean constVal = scan(node.getExpression(), p);

            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

            if (e != null) {
                if (SUPPORTED_VARIABLES.contains(e.getKind())) {
                    VariableElement ve = (VariableElement) e;
                    State prevState = variable2State.get(ve);
                    if (LOCAL_VARIABLES.contains(e.getKind())) {
                        addUse2Values(node.getVariable(), prevState);
                    } else if (e.getKind() == ElementKind.FIELD && prevState != null && prevState.hasUnassigned() && !finalCandidates.contains(ve)) {
                        usedWhileUndefined.add(ve);
                    }
                    recordVariableState(ve, getCurrentPath());
                } else if (shouldProcessUndefined(e)) {
                    Element cv = canonicalUndefined(e);
                    recordVariableState(cv, getCurrentPath());
                }
            }

            this.referenceTarget = oldQName;
            boolean retain = false;
            switch (node.getKind()) {
                case OR_ASSIGNMENT:
                    retain = constVal == Boolean.TRUE;
                    break;
                case AND_ASSIGNMENT:
                    retain = constVal == Boolean.FALSE;
                    break;
            }
            return retain ? constVal : null;
        }

        
        private void addScopedVariable(Tree t, Element ve) {
            Collection<Element> c = scopedVariables.get(t);
            if (c == null) {
                c = new ArrayList<Element>(3);
                scopedVariables.put(t, c);
            }
            c.add(ve);
        }
 
        @Override
        public Boolean visitVariable(VariableTree node, ConstructorData p) {
            super.visitVariable(node, p);

            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e != null && SUPPORTED_VARIABLES.contains(e.getKind())) {
                // note: if the variable does not have an initializer and is not a parameter (inParameters = true),
                // the State will receive null as an assignment to indicate an uninitializer
                TreePath tp = 
                            node.getInitializer() != null ? 
                                    new TreePath(getCurrentPath(), node.getInitializer()) : 
                                    inParameters ? getCurrentPath() : null;
                recordVariableState(e, tp);
                currentMethodVariables.add((Element) e);
                TreePath pp = getCurrentPath().getParentPath();
                if (pp != null) {
                    addScopedVariable(pp.getLeaf(), e);
                }
            }
            
            return null;
        }
        
        @Override
        public Boolean visitMemberSelect(MemberSelectTree node, ConstructorData p) {
            boolean lVal = lValueDereference;
            // selector is only read despite the member select is on the assignment's left,
            // but keep for recursive MEMBER_SELECT to reach the innermost tree.
            if (node.getExpression().getKind() != Tree.Kind.MEMBER_SELECT) {
                lValueDereference = false;
            }
            if (lVal) {
                // reset the target in top-down direction, the innermost select will populate it.
                referenceTarget = null;
            }
            super.visitMemberSelect(node, p);
            lValueDereference = lVal;
            
            // for LValue reference, note the target class' Name
            if (lVal && referenceTarget == null) {
                // TODO: intern Name in constructor, compare using ==
                Element e = null;
                
                if (node.getIdentifier() == thisName) {
                    // class.this
                    e = info.getTrees().getElement(getCurrentPath());
                } else if (node.getExpression().getKind() == Tree.Kind.IDENTIFIER) {
                    e = info.getTrees().getElement(
                            new TreePath(getCurrentPath(), node.getExpression()));
                    if (((IdentifierTree)node.getExpression()).getName() == thisName) {
                        // this.whatever
                        if (e != null) {
                            e = e.getEnclosingElement();
                        }
                    }
                }
                if (e != null && (e.getKind().isClass() || e.getKind().isInterface())) {
                    referenceTarget = (TypeElement)e;
                }
            }
            handleCurrentAccess();
            return null;
        }
        
        private void handleCurrentAccess() {
            if (lValueDereference) {
                return;
            }
            Element e = info.getTrees().getElement(getCurrentPath());

            if (e != null && SUPPORTED_VARIABLES.contains(e.getKind())) {
                VariableElement ve = (VariableElement) e;
                State prevState = variable2State.get(ve);
                
                if (LOCAL_VARIABLES.contains(e.getKind())) {
                    addUse2Values(getCurrentPath().getLeaf(), prevState);
                } else if (e.getKind() == ElementKind.FIELD && (prevState == null || prevState.hasUnassigned()) && !finalCandidates.contains(ve)) {
                    usedWhileUndefined.add(ve);
                }
            }
        }

        @Override
        public Boolean visitLiteral(LiteralTree node, ConstructorData p) {
            Object val = node.getValue();

            if (val instanceof Boolean) {
                return (Boolean) val;
            } else {
                return null;
            }
        }

        @Override
        public Boolean visitIf(IfTree node, ConstructorData p) {
            generalizedIf(node.getCondition(), node.getThenStatement(), node.getElseStatement() != null ? Collections.singletonList(node.getElseStatement()) : Collections.<Tree>emptyList(), true);
            return null;
        }
        
        public void generalizedIf(Tree condition, Tree thenSection, Iterable<? extends Tree> elseSection, boolean realElse) {
            Boolean result = scan(condition, null);

            if (result != null) {
                if (result) {
                    scan(thenSection, null);
                    if (realElse && elseSection.iterator().hasNext())
                        deadBranches.add(elseSection.iterator().next());
                } else {
                    scan(elseSection, null);
                    deadBranches.add(thenSection);
                }

                return ;
            }

            Map<Element, State> oldVariable2State = variable2State;
            
            variable2State = new HashMap<Element, Flow.State>(oldVariable2State);

            scan(thenSection, null);
            
            Map<Element, State> variableStatesAfterThen = new HashMap<Element, Flow.State>(variable2State);

            variable2State = new HashMap<Element, Flow.State>(oldVariable2State);

            scan(elseSection, null);

            variable2State = mergeOr(variable2State, variableStatesAfterThen);
        }

        @Override
        public Boolean visitBinary(BinaryTree node, ConstructorData p) {
            Boolean left = scan(node.getLeftOperand(), p);

            if (left != null && (node.getKind() == Kind.CONDITIONAL_AND || node.getKind() == Kind.CONDITIONAL_OR)) {
                if (left) {
                    if (node.getKind() == Kind.CONDITIONAL_AND) {
                        return scan(node.getRightOperand(), p);
                    } else {
                        return true;
                    }
                } else {
                    if (node.getKind() == Kind.CONDITIONAL_AND) {
                        return false;
                    } else {
                        return scan(node.getRightOperand(), p);
                    }
                }
            }

            Map<Element, State> oldVariable2State = variable2State;

            variable2State = new HashMap<Element, Flow.State>(oldVariable2State);
            
            Boolean right = scan(node.getRightOperand(), p);

            variable2State = mergeOr(variable2State, oldVariable2State);

            if (left == null || right == null) {
                return null;
            }

            switch (node.getKind()) {
                case AND: case CONDITIONAL_AND: return left && right;
                case OR: case CONDITIONAL_OR: return left || right;
                case EQUAL_TO: return left == right;
                case NOT_EQUAL_TO: return left != right;
            }
            
            return null;
        }

        @Override
        public Boolean visitConditionalExpression(ConditionalExpressionTree node, ConstructorData p) {
            Boolean result = scan(node.getCondition(), p);

            if (result != null) {
                if (result) {
                    scan(node.getTrueExpression(), null);
                } else {
                    scan(node.getFalseExpression(), null);
                }

                return null;
            }

            Map<Element, State> oldVariable2State = variable2State;

            variable2State = new HashMap<Element, Flow.State>(oldVariable2State);

            scan(node.getTrueExpression(), null);

            if (node.getFalseExpression() != null) {
                Map<Element, State> variableStatesAfterThen = new HashMap<Element, Flow.State>(variable2State);

                variable2State = new HashMap<Element, Flow.State>(oldVariable2State);

                scan(node.getFalseExpression(), null);

                variable2State = mergeOr(variable2State, variableStatesAfterThen);
            } else {
                variable2State = mergeOr(variable2State, oldVariable2State);
            }

            return null;
        }

        @Override
        public Boolean visitIdentifier(IdentifierTree node, ConstructorData p) {
            super.visitIdentifier(node, p);
            handleCurrentAccess();
            return null;
        }

        @Override
        public Boolean visitUnary(UnaryTree node, ConstructorData p) {
            Boolean val = super.visitUnary(node, p);

            if (val != null && node.getKind() == Kind.LOGICAL_COMPLEMENT) {
                return !val;
            }

            if (    node.getKind() == Kind.PREFIX_DECREMENT
                 || node.getKind() == Kind.PREFIX_INCREMENT
                 || node.getKind() == Kind.POSTFIX_DECREMENT
                 || node.getKind() == Kind.POSTFIX_INCREMENT) {
                Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));

                if (e != null) {
                    if (SUPPORTED_VARIABLES.contains(e.getKind())) {
                        VariableElement ve = (VariableElement) e;
                        State prevState = variable2State.get(ve);

                        if (LOCAL_VARIABLES.contains(e.getKind())) {
                            addUse2Values(node.getExpression(), prevState);
                        } else if (e.getKind() == ElementKind.FIELD && prevState != null && prevState.hasUnassigned() && !finalCandidates.contains(ve)) {
                            usedWhileUndefined.add(ve);
                        }
                        variable2State.put(ve, State.create(getCurrentPath(), prevState));
                    } else if (shouldProcessUndefined(e)) {
                        Element cv = canonicalUndefined(e);
                        State prevState = variable2State.get(cv);
                        variable2State.put(cv, State.create(getCurrentPath(), prevState));
                    }
                }
            }


            return null;
        }
        
        private void addFieldConstructor(Element field, Element ctor) {
            Collection<Element> ctors = finalFieldConstructors.get(field);
            if (ctors == null) {
                ctors = new HashSet<Element>();
                finalFieldConstructors.put(field, ctors);
            }
            ctors.add(ctor);
        }

        @Override
        public Boolean visitMethod(MethodTree node, ConstructorData p) {
            Tree oldNearestMethod = nearestMethod;
            Set<Element> oldCurrentMethodVariables = currentMethodVariables;
            Map<TypeMirror, Map<Element, State>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;
            Map<Element, State> oldVariable2State = variable2State;

            nearestMethod = node;
            currentMethodVariables = Collections.newSetFromMap(new IdentityHashMap<Element, Boolean>());
            resumeOnExceptionHandler = new IdentityHashMap<TypeMirror, Map<Element, State>>();
            variable2State = new HashMap<>(variable2State);
            
            for (Iterator<Entry<Element, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                Entry<Element, State> e = it.next();
                
                if (e.getKey().getKind().isField()) it.remove();
            }
            
            try {
                scan(node.getModifiers(), null);
                scan(node.getReturnType(), null);
                scan(node.getTypeParameters(), null);

                inParameters = true;

                try {
                    scan(node.getParameters(), null);
                } finally {
                    inParameters = false;
                }

                scan(node.getThrows(), null);
                
                List<Tree> additionalTrees = p != null ? p.initializers : Collections.<Tree>emptyList();
                handleInitializers(additionalTrees);
                
                scan(node.getBody(), null);
                scan(node.getDefaultValue(), null);
            
                //constructor check:
                boolean isConstructor = isConstructor(getCurrentPath());
                Set<Element> definitellyAssignedOnce = new HashSet<Element>();
                Set<Element> assigned = new HashSet<Element>();
                Element methodEl = info.getTrees().getElement(getCurrentPath());
                Element classEl = methodEl != null ? methodEl.getEnclosingElement() : null;

                for (Iterator<Entry<Element, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                    Entry<Element, State> e = it.next();

                    if (e.getKey().getKind() == ElementKind.FIELD) {
                        if (isConstructor && !e.getValue().hasUnassigned() && !e.getValue().reassigned && !e.getKey().getModifiers().contains(Modifier.STATIC) && e.getKey().getEnclosingElement().equals(classEl)) {
                            definitellyAssignedOnce.add(e.getKey());
                        }

                        assigned.add(e.getKey());

                        it.remove();
                    }
                }

                if (isConstructor && p != null/*IntroduceHint may bypass visitClass - TODO: test in FlowTest missing for this*/) {
                    if (p.first) {
                        finalCandidates.addAll(definitellyAssignedOnce);
                    } else {
                        finalCandidates.retainAll(definitellyAssignedOnce);
                    }
                    
                    if (methodEl != null) {
                        for (Element e : definitellyAssignedOnce) {
                            addFieldConstructor(e, methodEl);
                        }
                    }
                    
                    for (Element var : assigned) {
                        if (var.getModifiers().contains(Modifier.STATIC) || !definitellyAssignedOnce.contains(var)) {
                            finalCandidates.remove(var);
                        }
                    }
                } else {
                    finalCandidates.removeAll(assigned);
                }
            } finally {
                nearestMethod = oldNearestMethod;
                currentMethodVariables = oldCurrentMethodVariables;
                resumeOnExceptionHandler = oldResumeOnExceptionHandler;
                variable2State = mergeOr(variable2State, oldVariable2State, false);
            }
            
            return null;
        }
        
        private boolean isConstructor(TreePath what) {
            return what.getLeaf().getKind() == Kind.METHOD && ((MethodTree) what.getLeaf()).getName().contentEquals("<init>"); //TODO: not really a proper way to detect constructors
        }

        @Override
        public Boolean visitWhileLoop(WhileLoopTree node, ConstructorData p) {
            return handleGeneralizedForLoop(null, node.getCondition(), null, node.getStatement(), node.getCondition(), p);
        }

        @Override
        public Boolean visitDoWhileLoop(DoWhileLoopTree node, ConstructorData p) {
            Map< Element, State> beforeLoop = variable2State;

            variable2State = new HashMap< Element, Flow.State>(beforeLoop);

            scan(node.getStatement(), null);
            Boolean condValue = scan(node.getCondition(), null);

            if (condValue != null) {
                if (condValue) {
                    //XXX: handle possibly infinite loop
                } else {
                    //will not run more than once, skip:
                    return null;
                }
            }

            variable2State = mergeOr(beforeLoop, variable2State);

            if (!doNotRecord) {
                boolean oldDoNotRecord = doNotRecord;
                doNotRecord = true;
                
                beforeLoop = new HashMap< Element, State>(variable2State);
                scan(node.getStatement(), null);
                scan(node.getCondition(), null);
                
                doNotRecord = oldDoNotRecord;
                variable2State = mergeOr(beforeLoop, variable2State);
//                variable2State = beforeLoop;
            }

            return null;
        }

        @Override
        public Boolean visitForLoop(ForLoopTree node, ConstructorData p) {
            return handleGeneralizedForLoop(node.getInitializer(), node.getCondition(), node.getUpdate(), node.getStatement(), node.getCondition(), p);
        }
        
        private Boolean handleGeneralizedForLoop(Iterable<? extends Tree> initializer, Tree condition, Iterable<? extends Tree> update, Tree statement, Tree resumeOn, ConstructorData p) {
            scan(initializer, null);
            
            Map< Element, State> beforeLoop = variable2State;

            variable2State = new HashMap< Element, Flow.State>(beforeLoop);

            Boolean condValue = scan(condition, null);

            if (condValue != null) {
                if (condValue) {
                    //XXX: handle possibly infinite loop
                } else {
                    //will not run at all, skip:
                    return null;
                }
            }
            
            if (!doNotRecord) {
                boolean oldDoNotRecord = doNotRecord;
                doNotRecord = true;

                scan(statement, null);
                scan(update, null);

                variable2State = mergeOr(beforeLoop, variable2State);
                resume(resumeOn, resumeBefore);
                beforeLoop = new HashMap< Element, State>(variable2State);
                scan(condition, null);
                doNotRecord = oldDoNotRecord;
            }

            scan(statement, null);
            scan(update, null);

            variable2State = mergeOr(beforeLoop, variable2State);

            return null;
        }
        
        public Boolean visitTry(TryTree node, ConstructorData p) {
            if (node.getFinallyBlock() != null) {
                pendingFinally.add(0, new TreePath(getCurrentPath(), node.getFinallyBlock()));
            }
            
            scan(node.getResources(), null);

            Map< Element, State> oldVariable2State = variable2State;

            variable2State = new HashMap< Element, Flow.State>(oldVariable2State);

            // resumeOnEx will save states from potential exceptions thrown by try siblings
            // or outer blocks. 
            // resumeOnEx will be later reused for recorded handlers from the try block.
            Map<TypeMirror, Map<Element, State>> resumeOnEx = null;
            List<TypeMirror> caughtTypes = null;
            if (node.getCatches() != null && !node.getCatches().isEmpty()) {
                caughtTypes = new ArrayList<TypeMirror>(node.getCatches().size());
                resumeOnEx = new IdentityHashMap<TypeMirror, Map<Element, State>>(caughtTypes.size());
                for (CatchTree ct : node.getCatches()) {
                    for (TypeMirror caught : Utilities.getUnionExceptions(info, getCurrentPath(), ct)) {
                        Map<Element, State> data = resumeOnExceptionHandler.get(caught);
                        if (data != null) {
                            resumeOnEx.put(caught, data);
                        }
                        resumeOnExceptionHandler.put(caught, new HashMap<Element, State>());
                        caughtTypes.add(caught);
                    }
                }
                // make an implicit transition since Error can happen on any allocation and RTE on virtually any
                // arithmetic/dereference
                recordResumeOnExceptionHandler(runtimeExceptionType);
                recordResumeOnExceptionHandler(errorType);
            }

            scan(node.getBlock(), null);

            if (caughtTypes != null) {
                recordResumeOnExceptionHandler(runtimeExceptionType);
                recordResumeOnExceptionHandler(errorType);
            }

            HashMap< Element, State> afterBlockVariable2State = new HashMap< Element, Flow.State>(variable2State);

            // catch handlers and finally block will report exception to outer catches, restore
            // masked exception types
            if (caughtTypes != null) {
                for (TypeMirror exT : caughtTypes) {
                    Map<Element, State> oldData = resumeOnEx.remove(exT);
                    Map<Element, State> data = resumeOnExceptionHandler.remove(exT);
                    if (data != null) {
                        resumeOnEx.put(exT, data);
                    }
                    if (oldData != null) {
                        resumeOnExceptionHandler.put(exT, oldData);
                    }
                }
            }

            for (CatchTree ct : node.getCatches()) {
                Map< Element, State> variable2StateBeforeCatch = variable2State;

                variable2State = new HashMap< Element, Flow.State>(oldVariable2State);

                for (TypeMirror cc : Utilities.getUnionExceptions(info, getCurrentPath(), ct)) {
                    Map<Element, State> data = resumeOnEx.get(cc);
                    if (data != null) {
                        variable2State = mergeOr(variable2State, data);
                    }
                }
                
                scan(ct, null);

                variable2State = mergeOr(variable2StateBeforeCatch, variable2State);
            }

            if (node.getFinallyBlock() != null) {
                pendingFinally.remove(0);
                variable2State = mergeOr(mergeOr(oldVariable2State, variable2State), afterBlockVariable2State);

                scan(node.getFinallyBlock(), null);
            }
            
            return null;
        }
        
        /**
         * Removes definitions of variables, as a result of e.g. return, throw or System.exit.
         */
        private void removeAllDefinitions() {
            variable2State = new HashMap< Element, State>(variable2State);
            for (Iterator< Element> it = variable2State.keySet().iterator(); it.hasNext();) {
                 Element k = it.next();
                
                if (!k.getKind().isField() &&
                    !isUndefinedVariable(k)) {
                    it.remove();
                }
            }
        }

        public Boolean visitReturn(ReturnTree node, ConstructorData p) {
            super.visitReturn(node, p);
            variable2State = new HashMap< Element, State>(variable2State);

            if (pendingFinally.isEmpty()) {
                //performance: limit amount of held variables and their mapping:
                variable2State.keySet().removeAll(currentMethodVariables);
            }
            
            resumeAfter(nearestMethod, variable2State);

            removeAllDefinitions();
            return null;
        }

        public Boolean visitBreak(BreakTree node, ConstructorData p) {
            super.visitBreak(node, p);

            Tree target = info.getTreeUtilities().getBreakContinueTargetTree(getCurrentPath());
            
            breakTo(target);

            return null;
        }

        @Override
        public Boolean visitYield(YieldTree node, ConstructorData p) {
            scan(node.getValue(), p);

            Tree target = info.getTreeUtilities().getBreakContinueTargetTree(getCurrentPath());
            
            breakTo(target);

            return null;
        }

        private void breakTo(Tree target) {
            resumeAfter(target, variable2State);

            variable2State = new HashMap<>();
        }

        public Boolean visitSwitch(SwitchTree node, ConstructorData p) {
            generalizedSwitch(node, node.getExpression(), node.getCases());
            return null;
        }

        @Override
        public Boolean visitSwitchExpression(SwitchExpressionTree node, ConstructorData p) {
            generalizedSwitch(node, node.getExpression(), node.getCases());
            return null; //never a constant expression
        }

        private void generalizedSwitch(Tree switchTree, ExpressionTree expression, List<? extends CaseTree> cases) {
            scan(expression, null);

            Map< Element, State> origVariable2State = new HashMap< Element, State>(variable2State);

            variable2State = new HashMap< Element, State>();

            boolean exhaustive = false;

            for (CaseTree ct : cases) {
                variable2State = mergeOr(variable2State, origVariable2State);

                if (ct.getExpression() == null) {
                    exhaustive = true;
                }

                scan(ct, null);

                if (ct.getCaseKind() == CaseKind.RULE) {
                    breakTo(switchTree);
                }
            }

            if (!exhaustive) {
                variable2State = mergeOr(variable2State, origVariable2State);
            }
        }

        public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, ConstructorData p) {
            return handleGeneralizedForLoop(Arrays.asList(node.getVariable(), node.getExpression()), null, null, node.getStatement(), node.getStatement(), p);
        }

        @Override
        public Boolean visitAssert(AssertTree node, ConstructorData p) {
            Map< Element, State> oldVariable2State = variable2State;

            variable2State = new HashMap< Element, Flow.State>(oldVariable2State);

            scan(node.getCondition(), null);

            if (node.getDetail() != null) {
                Map< Element, State> beforeDetailState = new HashMap< Element, Flow.State>(variable2State);

                scan(node.getDetail(), null);

                variable2State = mergeOr(variable2State, beforeDetailState);
            }
            
            variable2State = mergeOr(variable2State, oldVariable2State);

            recordResumeOnExceptionHandler("java.lang.AssertionError");
            return null;
        }

        public Boolean visitContinue(ContinueTree node, ConstructorData p) {
            StatementTree loop = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());

            if (loop == null) {
                super.visitContinue(node, p);
                return null;
            }

            Tree resumePoint;

            if (loop.getKind() == Kind.LABELED_STATEMENT) {
                loop = ((LabeledStatementTree) loop).getStatement();
            }
            
            switch (loop.getKind()) {
                case WHILE_LOOP:
                    resumePoint = ((WhileLoopTree) loop).getCondition();
                    break;
                case FOR_LOOP: {
                        ForLoopTree flt = (ForLoopTree)loop;
                        resumePoint = null;
                        if (flt.getUpdate() != null && !flt.getUpdate().isEmpty()) {
                            // resume will react on the 1st Tree of the update statement (always processed left to right)
                            resumePoint = flt.getUpdate().get(0);
                        } 
                        if (resumePoint == null) {
                            resumePoint = flt.getCondition();
                        }
                        if (resumePoint == null) {
                            resumePoint = flt.getStatement();
                        }
                }
                    break;
                case DO_WHILE_LOOP:
                    resumePoint = ((DoWhileLoopTree) loop).getCondition();
                    break;
                case ENHANCED_FOR_LOOP:
                    resumePoint = ((EnhancedForLoopTree) loop).getStatement();
                    break;
                default:
                    resumePoint = null;
                    break;
            }

            if (resumePoint != null) {
                recordResume(resumeBefore, resumePoint, variable2State);
            }

            variable2State = new HashMap< Element, State>();

            super.visitContinue(node, p);
            return null;
        }

        public Boolean visitThrow(ThrowTree node, ConstructorData p) {
            super.visitThrow(node, p);

            if (node.getExpression() != null) {
                TypeMirror thrown = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));

                recordResumeOnExceptionHandler(thrown);
            }

            return null;
        }

        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, ConstructorData p) {
            super.visitMethodInvocation(node, p);

            Element invoked = info.getTrees().getElement(getCurrentPath());
            if (invoked != null && invoked.getKind() == ElementKind.METHOD) {
                // Special handling for System.exit: no defined value will escape this code path.
                if (Utilities.isSystemExit(info, invoked)) {
                    removeAllDefinitions();
                    return null;
                }
                recordResumeOnExceptionHandler((ExecutableElement) invoked);    
            }
            return null;
        }

        @Override
        public Boolean visitLambdaExpression(LambdaExpressionTree node, ConstructorData p) {
            TypeElement oldTarget = this.referenceTarget;
            this.referenceTarget = null;
            Boolean b;

            Tree oldNearestMethod = nearestMethod;
            Set<Element> oldCurrentMethodVariables = currentMethodVariables;
            Map<TypeMirror, Map<Element, State>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;
            Map<Element, State> oldVariable2State = variable2State;

            nearestMethod = node;
            currentMethodVariables = Collections.newSetFromMap(new IdentityHashMap<Element, Boolean>());
            resumeOnExceptionHandler = new IdentityHashMap<TypeMirror, Map<Element, State>>();
            variable2State = new HashMap<>(variable2State);
            
            for (Iterator<Entry<Element, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                Entry<Element, State> e = it.next();
                
                if (e.getKey().getKind().isField()) it.remove();
            }
            
            try {
                inParameters = true;

                try {
                    scan(node.getParameters(), null);
                } finally {
                    inParameters = false;
                }
                b = scan(node.getBody(), p);
            
                Set<Element> assigned = new HashSet<Element>();

                for (Iterator<Entry<Element, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                    Entry<Element, State> e = it.next();

                    if (e.getKey().getKind() == ElementKind.FIELD) {
                        assigned.add(e.getKey());
                        it.remove();
                    }
                }
                finalCandidates.removeAll(assigned);
            } finally {
                referenceTarget = oldTarget;
                nearestMethod = oldNearestMethod;
                currentMethodVariables = oldCurrentMethodVariables;
                resumeOnExceptionHandler = oldResumeOnExceptionHandler;
                variable2State = mergeOr(variable2State, oldVariable2State, false);
            }
            return b;
        }

        public Boolean visitNewClass(NewClassTree node, ConstructorData p) {
            TypeElement oldTarget = referenceTarget;
            super.visitNewClass(node, p);

            Element invoked = info.getTrees().getElement(getCurrentPath());

            if (invoked != null && invoked.getKind() == ElementKind.CONSTRUCTOR) {
                recordResumeOnExceptionHandler((ExecutableElement) invoked);
            }
            referenceTarget = oldTarget;
            return null;
        }

        public Boolean visitClass(ClassTree node, ConstructorData p) {
            TypeElement oldTarget = referenceTarget;
            List<Tree> staticInitializers = new ArrayList<Tree>(node.getMembers().size());
            List<Tree> instanceInitializers = new ArrayList<Tree>(node.getMembers().size());
            List<MethodTree> constructors = new ArrayList<MethodTree>(node.getMembers().size());
            List<Tree> others = new ArrayList<Tree>(node.getMembers().size());

            for (Tree member : node.getMembers()) {
                if (member.getKind() == Kind.BLOCK) {
                    if (((BlockTree) member).isStatic()) {
                        staticInitializers.add(member);
                    } else {
                        instanceInitializers.add(member);
                    }
                } else if (member.getKind() == Kind.VARIABLE && ((VariableTree) member).getInitializer() != null) {
                    // If the field is either marked static or is implicitly static,
                    // then add to staticInitializers; otherwise add to instanceInitializers.
                    //
                    // §8.9.3 Enum Members
                    // "For each enum constant c declared in the body of the declaration of E,
                    // E has an implicitly declared public static final field of type E that has
                    // the same name as c. The field has a variable initializer which instantiates
                    // E and passes any arguments of c to the constructor chosen for E."
                    //
                    // §9.3 Field (Constant) Declarations
                    // "Every field declaration in the body of an interface is implicitly public,
                    // static, and final. It is permitted to redundantly specify any or all of
                    // these modifiers for such fields."
                    if (node.getKind() == Kind.INTERFACE || ((VariableTree) member).getModifiers().getFlags().contains(Modifier.STATIC)) {
                        {
                            final Element e;
                            assert (e = info.getTrees().getElement(TreePath.getPath(getCurrentPath(), member))) == null || e.getModifiers().contains(Modifier.STATIC);
                        }
                        staticInitializers.add(member);
                    } else {
                        {
                            final Element e;
                            assert (e = info.getTrees().getElement(TreePath.getPath(getCurrentPath(), member))) == null || !e.getModifiers().contains(Modifier.STATIC);
                        }
                        instanceInitializers.add(member);
                    }
                } else if (isConstructor(new TreePath(getCurrentPath(), member))) {
                    constructors.add((MethodTree) member);
                } else {
                    others.add(member);
                }
            }

            Map< Element, State> oldVariable2State = variable2State;

            variable2State = new HashMap<>(variable2State);
            
            for (Iterator<Entry< Element, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                Entry< Element, State> e = it.next();
                
                if (e.getKey().getKind().isField()) it.remove();
            }
            
            try {
                handleInitializers(staticInitializers);
            
                //constructor check:
                Set< Element> definitellyAssignedOnce = new HashSet< Element>();
                Set< Element> assigned = new HashSet< Element>();

                for (Iterator<Entry< Element, State>> it = variable2State.entrySet().iterator(); it.hasNext();) {
                    Entry< Element, State> e = it.next();

                    if (e.getKey().getKind() == ElementKind.FIELD) {
                        if (!e.getValue().hasUnassigned() && !e.getValue().reassigned && e.getKey().getModifiers().contains(Modifier.STATIC)) {
                            definitellyAssignedOnce.add(e.getKey());
                        }

                        assigned.add(e.getKey());

                        it.remove();
                    }
                }

                finalCandidates.addAll(definitellyAssignedOnce);
                //TODO: support for erroneous source code, we should prevent marking instance fields written in static blocks as final-able (i.e. none of "assigned" - "definitellyAssignedOnce" should ever be final candidates
            } finally {
                variable2State = mergeOr(variable2State, oldVariable2State, false);
            }

            boolean first = true;
            
            for (MethodTree constructor : constructors) {
                scan(constructor, new ConstructorData(first, instanceInitializers));
                first = false;
            }
            
            scan(others, p);

            referenceTarget = oldTarget;
            return null;
        }

        public Boolean visitBlock(BlockTree node, ConstructorData p) {
            List<? extends StatementTree> statements = new ArrayList<StatementTree>(node.getStatements());
            
            for (int i = 0; i < statements.size(); i++) {
                StatementTree st = statements.get(i);
                
                if (st.getKind() == Kind.IF) {
                    IfTree it = (IfTree) st; 
                    if (it.getElseStatement() == null && Utilities.exitsFromAllBranchers(info, new TreePath(new TreePath(getCurrentPath(), it), it.getThenStatement()))) {
                        generalizedIf(it.getCondition(), it.getThenStatement(), statements.subList(i + 1, statements.size()), false);
                        break;
                    }
                }
                
                scan(st, null);
            }
            
            return null;
        }

        private void recordResumeOnExceptionHandler(ExecutableElement invoked) {
            for (TypeMirror tt : invoked.getThrownTypes()) {
                recordResumeOnExceptionHandler(tt);
            }

//            recordResumeOnExceptionHandler(runtimeExceptionType);
//            recordResumeOnExceptionHandler(errorType);
        }

        private void recordResumeOnExceptionHandler(String exceptionTypeFQN) {
            TypeElement exc = info.getElements().getTypeElement(exceptionTypeFQN);

            if (exc == null) return;

            recordResumeOnExceptionHandler(exc.asType());
        }

        /**
         * Records continuation to the exception handler if a throwable is raised.
         * Optimization: the resumeOnExceptionHandler must contain an entry for the
         * throwable and/or its superclass. If not, then no enclosing catch handler
         * is interested in the Throwable and no state snapshot is necessary.
         * 
         * @param thrown thrown exception type
         */
        private void recordResumeOnExceptionHandler(TypeMirror thrown) {
            if (thrown == null || thrown.getKind() != TypeKind.DECLARED) return;
            DeclaredType dtt = (DeclaredType)thrown;
            do {
                // hack; getSuperclass may provide different type instance for the same element.
                thrown = dtt.asElement().asType();
                Map<Element, State> r = resumeOnExceptionHandler.get(thrown);
                if (r != null) {
                    mergeOr(r, variable2State);
                    break;
                }
                TypeElement tel = (TypeElement)dtt.asElement();
                if (tel == throwableEl) {
                    break;
                }
                TypeMirror sup = tel.getSuperclass();
                if (sup.getKind() != TypeKind.DECLARED) {
                    break;
                }
                dtt = (DeclaredType)tel.getSuperclass();
            } while (dtt != null);
        }

        public Boolean visitParenthesized(ParenthesizedTree node, ConstructorData p) {
            return super.visitParenthesized(node, p);
        }

        private void resumeAfter(Tree target, Map< Element, State> state) {
            for (TreePath tp : pendingFinally) {
                boolean shouldBeRun = false;

                for (Tree t : tp) {
                    if (t == target) {
                        shouldBeRun = true;
                        break;
                    }
                }

                if (shouldBeRun) {
                    recordResume(resumeBefore, tp.getLeaf(), state);
                } else {
                    break;
                }
            }

            recordResume(resumeAfter, target, state);
        }

        private static void recordResume(Map<Tree, Collection<Map< Element, State>>> resume, Tree target, Map< Element, State> state) {
            Collection<Map< Element, State>> r = resume.get(target);

            if (r == null) {
                resume.put(target, r = new ArrayList<Map< Element, State>>());
            }

            r.add(new HashMap< Element, State>(state));
        }

        public Boolean visitWildcard(WildcardTree node, ConstructorData p) {
            super.visitWildcard(node, p);
            return null;
        }

        public Boolean visitUnionType(UnionTypeTree node, ConstructorData p) {
            super.visitUnionType(node, p);
            return null;
        }

        public Boolean visitTypeParameter(TypeParameterTree node, ConstructorData p) {
            super.visitTypeParameter(node, p);
            return null;
        }

        public Boolean visitTypeCast(TypeCastTree node, ConstructorData p) {
            super.visitTypeCast(node, p);
            return null;
        }

        public Boolean visitSynchronized(SynchronizedTree node, ConstructorData p) {
            super.visitSynchronized(node, p);
            return null;
        }

        public Boolean visitPrimitiveType(PrimitiveTypeTree node, ConstructorData p) {
            super.visitPrimitiveType(node, p);
            return null;
        }

        public Boolean visitParameterizedType(ParameterizedTypeTree node, ConstructorData p) {
            super.visitParameterizedType(node, p);
            return null;
        }

        public Boolean visitOther(Tree node, ConstructorData p) {
            super.visitOther(node, p);
            return null;
        }

        public Boolean visitNewArray(NewArrayTree node, ConstructorData p) {
            super.visitNewArray(node, p);
            return null;
        }

        public Boolean visitModifiers(ModifiersTree node, ConstructorData p) {
            super.visitModifiers(node, p);
            return null;
        }

        public Boolean visitLabeledStatement(LabeledStatementTree node, ConstructorData p) {
            super.visitLabeledStatement(node, p);
            return null;
        }

        public Boolean visitInstanceOf(InstanceOfTree node, ConstructorData p) {
            super.visitInstanceOf(node, p);
            return null;
        }

        public Boolean visitImport(ImportTree node, ConstructorData p) {
            super.visitImport(node, p);
            return null;
        }

        public Boolean visitExpressionStatement(ExpressionStatementTree node, ConstructorData p) {
            super.visitExpressionStatement(node, p);
            return null;
        }

        public Boolean visitErroneous(ErroneousTree node, ConstructorData p) {
            super.visitErroneous(node, p);
            return null;
        }

        public Boolean visitEmptyStatement(EmptyStatementTree node, ConstructorData p) {
            super.visitEmptyStatement(node, p);
            return null;
        }

        public Boolean visitCompilationUnit(CompilationUnitTree node, ConstructorData p) {
            super.visitCompilationUnit(node, p);
            return null;
        }

        public Boolean visitCatch(CatchTree node, ConstructorData p) {
            inParameters = true;
            scan(node.getParameter(), p);
            inParameters = false;
            scan(node.getBlock(), p);
            return null;
        }

        public Boolean visitCase(CaseTree node, ConstructorData p) {
            super.visitCase(node, p);
            return null;
        }

        public Boolean visitArrayType(ArrayTypeTree node, ConstructorData p) {
            super.visitArrayType(node, p);
            return null;
        }

        public Boolean visitArrayAccess(ArrayAccessTree node, ConstructorData p) {
            boolean lv = lValueDereference;
            // even the array reference is just read from. There's no support to track array-item lvalues.
            this.lValueDereference = false;
            scan(node.getExpression(), p);
            scan(node.getIndex(), p);
            this.lValueDereference = lv;
            return null;
        }

        public Boolean visitAnnotation(AnnotationTree node, ConstructorData p) {
            super.visitAnnotation(node, p);
            return null;
        }

        private Map<Element, State> mergeOr(Map< Element, State> into, Map< Element, State> what) {
            return mergeOr(into, what, true);
        }
        
        private Map<Element, State> mergeOr(Map< Element, State> into, Map< Element, State> what, boolean markMissingAsUnassigned) {
            for (Entry< Element, State> e : what.entrySet()) {
                State stt = into.get(e.getKey());

                if (stt != null) {
                    into.put(e.getKey(), stt.merge(e.getValue()));
                } else if (e.getKey().getKind() == ElementKind.FIELD && markMissingAsUnassigned) {
                    into.put(e.getKey(), e.getValue().merge(UNASSIGNED));
                } else {
                    into.put(e.getKey(), e.getValue());
                }
            }
            
            if (markMissingAsUnassigned) {
                for (Entry< Element, State> e : into.entrySet()) {
                    if (e.getKey().getKind() == ElementKind.FIELD && !what.containsKey(e.getKey())) {
                        into.put(e.getKey(), e.getValue().merge(UNASSIGNED));
                    }
                }
            }

            return into;
        }

        private void handleInitializers(List<Tree> additionalTrees) {
            for (Tree additionalTree : additionalTrees) {
                switch (additionalTree.getKind()) {
                    case BLOCK:
                        Tree oldNearestMethod = nearestMethod;
                        Set< Element> oldCurrentMethodVariables = currentMethodVariables;
                        Map<TypeMirror, Map<Element, State>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;

                        nearestMethod = additionalTree;
                        currentMethodVariables = Collections.newSetFromMap(new IdentityHashMap< Element, Boolean>());
                        resumeOnExceptionHandler = new IdentityHashMap<TypeMirror, Map<Element, State>>();

                        try {
                            scan(((BlockTree) additionalTree).getStatements(), null);
                        } finally {
                            nearestMethod = oldNearestMethod;
                            currentMethodVariables = oldCurrentMethodVariables;
                            resumeOnExceptionHandler = oldResumeOnExceptionHandler;
                        }
                        break;
                    case VARIABLE: scan(additionalTree, null); break;
                    default: assert false : additionalTree.getKind(); break;
                }
            }
        }
    }
    
    private static final Set<ElementKind> SUPPORTED_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER, ElementKind.FIELD);
    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    
    static class State {
        private final Set<TreePath> assignments;
        private final boolean reassigned;
        private State(Set<TreePath> assignments, boolean reassigned) {
            this.assignments = assignments;
            this.reassigned = reassigned;
        }
        public static State create(TreePath assignment, boolean reassigned) {
            return new State(Collections.singleton(assignment), reassigned);
        }
        public static State create(TreePath assignment, State previous) {
            return new State(Collections.singleton(assignment), previous != null && (previous.assignments.size() > 1 || !previous.assignments.contains(null)));
        }
        public State merge(State value) {
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Set<TreePath> assignments = new HashSet<TreePath>(this.assignments);

            assignments.addAll(value.assignments);

            return new State(assignments, this.reassigned || value.reassigned);
        }
        
        public boolean hasUnassigned() {
            return assignments.contains(null);
        }
    }
    
    static class ConstructorData {
        final boolean first;
        final List<Tree> initializers;
        public ConstructorData(boolean first, List<Tree> initializers) {
            this.first = first;
            this.initializers = initializers;
        }
    }
    
    private static final class ReluctantSet<T> implements Set<T> {
        private final Set<T> included = new HashSet<>();
        private final Set<Object> removed = new HashSet<>();
        @Override public int size() {
            return included.size();
        }
        @Override public boolean isEmpty() {
            return included.isEmpty();
        }
        @Override public boolean contains(Object o) {
            return included.contains(o);
        }
        @Override public Iterator<T> iterator() {
            return Collections.synchronizedSet(included).iterator();
        }
        @Override public Object[] toArray() {
            return included.toArray();
        }
        @Override public <T> T[] toArray(T[] a) {
            return included.toArray(a);
        }
        @Override public boolean add(T e) {
            if (removed.contains(e)) {
                return false;
            }
            return included.add(e);
        }
        @Override public boolean remove(Object o) {
            removed.add(o);
            return included.remove(o);
        }
        @Override public boolean containsAll(Collection<?> c) {
            return included.containsAll(c);
        }
        @Override public boolean addAll(Collection<? extends T> c) {
            boolean result = false;
            for (T t : c) result |= add(t);
            return result;
        }
        @Override public boolean retainAll(Collection<?> c) {
            return included.retainAll(c);
        }
        @Override public boolean removeAll(Collection<?> c) {
            boolean result = false;
            for (Object o : c) result |= remove(o);
            return result;
        }
        @Override public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    private static final State UNASSIGNED = State.create(null, false);

}
