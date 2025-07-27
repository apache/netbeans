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
package org.netbeans.modules.java.source.matching;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PatternTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.source.JavaSourceAccessor;

/**TODO: tested by CopyFinderTest in java.hints module.
 *
 * @author Jan Lahoda
 */
public class CopyFinder extends ErrorAwareTreeScanner<Boolean, TreePath> {

    private final TreePath searchingFor;
    private final CompilationInfo info;
    private final Map<TreePath, VariableAssignments> result = new LinkedHashMap<TreePath, VariableAssignments>();
    private boolean allowGoDeeper = true;
    private Set<VariableElement> variablesWithAllowedRemap = Collections.emptySet();
    private State bindState = State.empty();
    private State preinitializeState = State.empty();
    private boolean allowVariablesRemap = false;
    private boolean nocheckOnAllowVariablesRemap = false;
    private final Cancel cancel;
    private static final String CLASS = "class"; //NOI18N
    private final Set<Options> options;

    private Map<String, TypeMirror> designedTypeHack;

    private static Set<Options> options(Options... options) {
        Set<Options> result = EnumSet.noneOf(Options.class);

        result.addAll(Arrays.asList(options));

        return result;
    }

    private CopyFinder(TreePath searchingFor, CompilationInfo info, Cancel cancel, Options... options) {
        this(searchingFor, info, cancel, options(options));
    }

    private CopyFinder(TreePath searchingFor, CompilationInfo info, Cancel cancel, Set<Options> options) {
        this.searchingFor = searchingFor;
        this.info = info;
        this.cancel = cancel;
        this.options = options;
    }

    public static Map<TreePath, VariableAssignments> internalComputeDuplicates(CompilationInfo info, Collection<? extends TreePath> searchingFor, TreePath scope, State preinitializedState, Collection<? extends VariableElement> variablesWithAllowedRemap, Cancel cancel, Map<String, TypeMirror> designedTypeHack, Options... options) {
        TreePath first = searchingFor.iterator().next();
        Set<Options> optionsSet = EnumSet.noneOf(Options.class);

        optionsSet.addAll(Arrays.asList(options));

        if (!optionsSet.contains(Options.ALLOW_GO_DEEPER) && !sameKind(scope.getLeaf(), first.getLeaf())) {
            return Collections.emptyMap();
        }

        CopyFinder f = new CopyFinder(first, info, cancel, options);

        f.allowGoDeeper = optionsSet.contains(Options.ALLOW_GO_DEEPER);
        f.designedTypeHack = designedTypeHack;
        f.variablesWithAllowedRemap = variablesWithAllowedRemap != null ? new HashSet<VariableElement>(variablesWithAllowedRemap) : Collections.<VariableElement>emptySet();
        f.allowVariablesRemap = variablesWithAllowedRemap != null;
        f.nocheckOnAllowVariablesRemap = variablesWithAllowedRemap != null;

        if (preinitializedState != null) {
            f.bindState = State.startFrom(f.preinitializeState = preinitializedState);
        }

        Map<TreePath, VariableAssignments> firstMapping;

        if (optionsSet.contains(Options.ALLOW_GO_DEEPER)) {
            f.scan(scope, null);

            firstMapping = f.result;
        } else {
            if (f.scan(scope, first)) {
                firstMapping = Collections.singletonMap(scope, new VariableAssignments(f.bindState));
            } else {
                return Collections.emptyMap();
            }
        }

        boolean statement = StatementTree.class.isAssignableFrom(first.getLeaf().getKind().asInterface());

        assert statement || searchingFor.size() == 1;

        if (!statement) {
            return firstMapping;
        }
        
        Map<TreePath, VariableAssignments> result = new HashMap<TreePath, VariableAssignments>();

        OUTER: for (Entry<TreePath, VariableAssignments> e : firstMapping.entrySet()) {
            TreePath firstOccurrence = e.getKey();
            List<? extends StatementTree> statements = getStatements(firstOccurrence);
            int occurrenceIndex = statements.indexOf(firstOccurrence.getLeaf());

            if (occurrenceIndex + searchingFor.size() > statements.size()) {
                continue;
            }

            int currentIndex = occurrenceIndex;
            Iterator<? extends TreePath> toProcess = searchingFor.iterator();
            Map<String, TreePath> variables = new HashMap<String, TreePath>(e.getValue().variables);
            Map<String, Collection<? extends TreePath>> multiVariables = new HashMap<String, Collection<? extends TreePath>>(e.getValue().multiVariables);
            Map<String, String> variables2Names = new HashMap<String, String>(e.getValue().variables2Names);
            Map<Element, Element> remapElements = new HashMap<Element, Element>(e.getValue().variablesRemapToElement);
            Map<Element, TreePath> remapTrees = new HashMap<Element, TreePath>(e.getValue().variablesRemapToTrees);

            toProcess.next();

            while (toProcess.hasNext()) {
                currentIndex++;

                TreePath currentToProcess = toProcess.next();
                CopyFinder ver = new CopyFinder(currentToProcess, info, cancel, options);

                ver.allowGoDeeper = false;
                ver.designedTypeHack = designedTypeHack;
                ver.variablesWithAllowedRemap = variablesWithAllowedRemap != null ? new HashSet<VariableElement>(variablesWithAllowedRemap) : Collections.<VariableElement>emptySet();
                ver.allowVariablesRemap = variablesWithAllowedRemap != null;
                ver.nocheckOnAllowVariablesRemap = variablesWithAllowedRemap != null;
                ver.bindState = State.from(variables, multiVariables, variables2Names);

                if (ver.allowVariablesRemap) {
                    ver.bindState = State.from(ver.bindState, remapElements, remapTrees);
                }

                if (!ver.scan(new TreePath(firstOccurrence.getParentPath(), statements.get(currentIndex)), currentToProcess)) {
                    continue OUTER;
                }

                variables = ver.bindState.variables;
                multiVariables = ver.bindState.multiVariables;
                variables2Names = ver.bindState.variables2Names;
                remapElements = ver.bindState.variablesRemapToElement;
                remapTrees = ver.bindState.variablesRemapToTrees;
            }

            result.put(e.getKey(), new VariableAssignments(variables, multiVariables, variables2Names, remapElements, remapTrees));
        }

        return result;
    }

    private static boolean sameKind(Tree t1, Tree t2) {
        Kind k1 = t1.getKind();
        Kind k2 = t2.getKind();

        if (k1 == k2) {
            return true;
        }

        if (isSingleStatemenBlockAndStatement(t1, t2) || isSingleStatemenBlockAndStatement(t2, t1)) {
            return true;
        }

        if (k2 == Kind.BLOCK && StatementTree.class.isAssignableFrom(k1.asInterface())) {
            BlockTree bt = (BlockTree) t2;

            if (bt.isStatic()) {
                return false;
            }

            switch (bt.getStatements().size()) {
                case 1:
                    return true;
                case 2:
                    return    isMultistatementWildcardTree(bt.getStatements().get(0))
                           || isMultistatementWildcardTree(bt.getStatements().get(1));
                case 3:
                    return    isMultistatementWildcardTree(bt.getStatements().get(0))
                           || isMultistatementWildcardTree(bt.getStatements().get(2));
            }

            return false;
        }

        if (    (k1 != Kind.MEMBER_SELECT && k1 != Kind.IDENTIFIER)
             || (k2 != Kind.MEMBER_SELECT && k2 != Kind.IDENTIFIER)) {
            return false;
        }

        return isPureMemberSelect(t1, true) && isPureMemberSelect(t2, true);
    }

    private static boolean isSingleStatemenBlockAndStatement(Tree t1, Tree t2) {
        Kind k1 = t1.getKind();
        Kind k2 = t2.getKind();

        if (k1 == Kind.BLOCK && ((BlockTree) t1).getStatements().size() == 1 && !((BlockTree) t1).isStatic()) {
            return StatementTree.class.isAssignableFrom(k2.asInterface());
        }

        return false;
    }

    private static final Set<TypeKind> IGNORE_KINDS = EnumSet.of(TypeKind.EXECUTABLE, TypeKind.PACKAGE, TypeKind.ERROR, TypeKind.OTHER);

    private TreePath currentPath;

    protected TreePath getCurrentPath() {
        return currentPath;
    }

    protected Boolean scan(TreePath path, TreePath param) {
        currentPath = path.getParentPath();
        try {
            return scan(path.getLeaf(), param);
        } finally {
            currentPath = null;
        }
    }
    
    private boolean typeMatches(TreePath currentPath, String placeholderName) {
        TypeMirror designed = designedTypeHack != null ? designedTypeHack.get(placeholderName) : null;

        boolean bind;

        if (designed != null && designed.getKind() != TypeKind.ERROR) {
            TypeMirror real = info.getTrees().getTypeMirror(currentPath);
            if (real != null && real.getKind() == TypeKind.ERROR) {
                ErrorType et = (ErrorType) real;
                real = info.getTrees().getOriginalType(et);
            }
            if (real != null && !IGNORE_KINDS.contains(real.getKind())) {
                // special hack: if the designed type is DECLARED (assuming a boxed primitive) and the real type is 
                // not DECLARED or is null (assuming a real primitive), do not treat them as assignable.
                // this will stop matching constraint to boxed types against primitive subexpressions. Exclude j.l.Object
                // which will allow to match raw type parameters
                if (designed.getKind() == TypeKind.DECLARED &&
                    real.getKind().ordinal() <= TypeKind.DOUBLE.ordinal() &&
                    !((TypeElement)((DeclaredType)designed).asElement()).getQualifiedName().contentEquals("java.lang.Object")) { //NOI18N
                    bind = false;
                } else {
                    bind = info.getTypes().isAssignable(real, designed);
                }
            } else {
                bind = false;
            }
        } else {
            bind = designed == null;
        }
        return bind;
    }

    @Override
    public Boolean scan(Tree node, TreePath p) {
        if (cancel.isCancelled()) {
            return false;
        }

        if (node == null) {
            if (p == null) return true;
            if (isMultistatementWildcardTree(p.getLeaf())) {
                return true;
            }
            return false;
        }

        String treeName = null;
        
        if (p != null && p.getLeaf().getKind() == Kind.IDENTIFIER) {
            treeName = ((IdentifierTree) p.getLeaf()).getName().toString();
        } else if (p != null && p.getLeaf().getKind() == Kind.TYPE_PARAMETER && ((TypeParameterTree) p.getLeaf()).getBounds().isEmpty()) {
            treeName = ((TypeParameterTree) p.getLeaf()).getName().toString();
        } else if (p != null && p.getLeaf().getKind() == Kind.PARAMETERIZED_TYPE && (node.getKind() == Kind.IDENTIFIER || node.getKind() == Kind.MEMBER_SELECT)) {
            ParameterizedTypeTree ptt = (ParameterizedTypeTree) p.getLeaf();
            
            if (ptt.getTypeArguments().size() == 1 && isMultistatementWildcardTree(ptt.getTypeArguments().get(0))) {
                p = new TreePath(p, ptt.getType());
                bindState.multiVariables.put(getWildcardTreeName(ptt.getTypeArguments().get(0)).toString(), Collections.<TreePath>emptyList());
            }
        }
        
        if (treeName != null) {
            if (treeName.startsWith("$") && options.contains(Options.ALLOW_VARIABLES_IN_PATTERN)) {
                if (bindState.variables2Names.containsKey(treeName)) {
                    if (node.getKind() == Kind.IDENTIFIER)
                        return ((IdentifierTree) node).getName().toString().equals(bindState.variables2Names.get(treeName));
                    else
                        return false; //correct?
                }

                TreePath currentPath = new TreePath(getCurrentPath(), node);
                boolean bind = typeMatches(currentPath, treeName);
                if (bind) {
                    TreePath original = bindState.variables.get(treeName);

                    if (original == null) {
                        bindState.variables.put(treeName, currentPath);
                        return true;
                    } else {
                        boolean oldAllowGoDeeper = allowGoDeeper;

                        try {
                            options.remove(Options.ALLOW_VARIABLES_IN_PATTERN);
                            Boolean success = scan(node, original);
                            if (success) {
                                bindState.variables.put(treeName + "$" + ++bindState.matchCount, currentPath);
                            }
                            return success;
                        } finally {
                            options.add(Options.ALLOW_VARIABLES_IN_PATTERN);
                            allowGoDeeper = oldAllowGoDeeper;
                        }
                    }
                } else {
                    return false;
                }
            }

            //TODO: remap with qualified name?
            Element remappable = info.getTrees().getElement(p);

            if (remappable != null && variablesWithAllowedRemap.contains(remappable) && (options.contains(Options.ALLOW_REMAP_VARIABLE_TO_EXPRESSION) || node.getKind() == Kind.IDENTIFIER)) {
                TreePath existing = bindState.variablesRemapToTrees.get(remappable);

                if (existing != null) {
                    boolean oldAllowGoDeeper = allowGoDeeper;

                    try {
                        allowGoDeeper = false;
                        return superScan(node, existing);
                    } finally {
                        allowGoDeeper = oldAllowGoDeeper;
                   }
                }

                TreePath currPath = new TreePath(getCurrentPath(), node);
                TypeMirror currType = info.getTrees().getTypeMirror(currPath);
                TypeMirror pType = ((VariableElement) remappable).asType();

                if (currType != null && pType != null && (nocheckOnAllowVariablesRemap || isSameTypeForVariableRemap(currType, pType))) {
                    bindState.variablesRemapToTrees.put(remappable, currPath);
                    return true;
                }

                return false;
            }
        }

        if (p != null && getWildcardTreeName(p.getLeaf()) != null) {
            String ident = getWildcardTreeName(p.getLeaf()).toString();

            if (ident.startsWith("$") && StatementTree.class.isAssignableFrom(node.getKind().asInterface())) {
                TreePath original = bindState.variables.get(ident);

                if (original == null) {
                    TreePath currentPath = new TreePath(getCurrentPath(), node);

                    bindState.variables.put(ident, currentPath);
                    return true;
                } else {
                    boolean oldAllowGoDeeper = allowGoDeeper;

                    try {
                        Boolean success = scan(node, original);
                        if (success) {
                            bindState.variables.put(ident + "$" + ++bindState.matchCount, currentPath);
                        }
                        return success;
                    } finally {
                        allowGoDeeper = oldAllowGoDeeper;
                    }
                }
            }
        }

        if (p != null && sameKind(node, p.getLeaf())) {
            //maybe equivalent:
            boolean result = superScan(node, p) == Boolean.TRUE;

            if (result) {
                if (p == searchingFor && node != searchingFor && allowGoDeeper) {
                    this.result.put(new TreePath(getCurrentPath(), node), new VariableAssignments(bindState));
                    bindState = State.startFrom(preinitializeState);
                }

                return true;
            }
        }

        if (!allowGoDeeper)
            return false;

        if ((p != null && p.getLeaf() == searchingFor.getLeaf()) || !sameKind(node, searchingFor.getLeaf())) {
            if (    bindState.multiVariables.isEmpty()
                 || bindState.variables.isEmpty()
                 || bindState.variables2Names.isEmpty()
                 || bindState.variablesRemapToElement.isEmpty()
                 || bindState.variablesRemapToTrees.isEmpty()) {
                bindState = State.startFrom(preinitializeState);
            }
            superScan(node, null);
            return false;
        } else {
            //maybe equivalent:
            allowGoDeeper = false;

            boolean result = superScan(node, searchingFor) == Boolean.TRUE;

            allowGoDeeper = true;

            if (result) {
                if (node != searchingFor.getLeaf()) {
                    this.result.put(new TreePath(getCurrentPath(), node), new VariableAssignments(bindState));
                    bindState = State.startFrom(preinitializeState);
                }

                return true;
            }

            superScan(node, null);
            return false;
        }
    }

    private Boolean superScan(Tree node, TreePath p) {
        if (p == null) {
            return doSuperScan(node, p);
        }

        if (p.getLeaf().getKind() == Kind.IDENTIFIER) {
            String ident = ((IdentifierTree) p.getLeaf()).getName().toString();

            if (ident.startsWith("$") && options.contains(Options.ALLOW_VARIABLES_IN_PATTERN)) {
                return scan(node, p);
            }
        }

        if (p.getLeaf().getKind() == Kind.BLOCK && node.getKind() != Kind.BLOCK /*&& p.getLeaf() != searchingFor.getLeaf()*/) {
            BlockTree bt = (BlockTree) p.getLeaf();

            switch (bt.getStatements().size()) {
                case 1:
                    if (isMultistatementWildcardTree(bt.getStatements().get(0))) {
                        if (!validateMultiVariable(bt.getStatements().get(0), Collections.singletonList(new TreePath(getCurrentPath(), node))))
                                return false;
                        return true;
                    }

                    p = new TreePath(p, bt.getStatements().get(0));
                    break;
                case 2:
                    if (isMultistatementWildcardTree(bt.getStatements().get(0))) {
                        if (!validateMultiVariable(bt.getStatements().get(0), Collections.<TreePath>emptyList()))
                                return false;
                        p = new TreePath(p, bt.getStatements().get(1));
                        break;
                    }
                    if (isMultistatementWildcardTree(bt.getStatements().get(1))) {
                        if (!validateMultiVariable(bt.getStatements().get(1), Collections.<TreePath>emptyList()))
                                return false;
                        p = new TreePath(p, bt.getStatements().get(0));
                        break;
                    }
                    throw new UnsupportedOperationException();
                case 3:
                    if (   isMultistatementWildcardTree(bt.getStatements().get(0))
                        && isMultistatementWildcardTree(bt.getStatements().get(2))) {
                        if (!validateMultiVariable(bt.getStatements().get(0), Collections.<TreePath>emptyList()))
                                return false;
                        if (!validateMultiVariable(bt.getStatements().get(2), Collections.<TreePath>emptyList()))
                                return false;
                        p = new TreePath(p, bt.getStatements().get(1));
                        break;
                    }
                    throw new UnsupportedOperationException();
            }
        }

        if (!sameKind(node, p.getLeaf())) {
            return false;
        }

        return doSuperScan(node, p);
    }

    private Boolean doSuperScan(Tree node, TreePath p) {
        if (node == null) return null;
        TreePath prev = currentPath;
        try {
            currentPath = new TreePath(currentPath, node);
            return super.scan(node, p);
        } finally {
            currentPath = prev;
        }
    }

    private Boolean scan(Tree node, Tree p, TreePath pOrigin) {
        if (node == null && p == null)
            return true;

        if (node != null && p == null)
            return false;
        
        return scan(node, new TreePath(pOrigin, p));
    }

//    public Boolean visitAnnotation(AnnotationTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitMethodInvocation(MethodInvocationTree node, TreePath p) {
        if (p == null)
            return super.visitMethodInvocation(node, p);

        MethodInvocationTree t = (MethodInvocationTree) p.getLeaf();

        if (!scan(node.getMethodSelect(), t.getMethodSelect(), p))
            return false;

        if (!checkLists(node.getTypeArguments(), t.getTypeArguments(), p))
            return false;

        return checkLists(node.getArguments(), t.getArguments(), p);
    }

    private <T extends Tree> boolean checkLists(List<? extends T> one, List<? extends T> other, TreePath otherOrigin) {
        if (one == null || other == null) {
            return one == other;
        }

        if (containsMultistatementTrees(other)) {
            return checkListsWithMultistatementTrees(one, 0, other, 0, otherOrigin);
        }

        if (one.size() != other.size())
            return false;

        for (int cntr = 0; cntr < one.size(); cntr++) {
            if (!scan(one.get(cntr), other.get(cntr), otherOrigin))
                return false;
        }

        return true;
    }

    public Boolean visitAssert(AssertTree node, TreePath p) {
        if (p == null) {
            super.visitAssert(node, p);
            return false;
        }

        AssertTree at = (AssertTree) p.getLeaf();

        if (!scan(node.getCondition(), at.getCondition(), p)) {
            return false;
        }

        return scan(node.getDetail(), at.getDetail(), p);
    }

    public Boolean visitAssignment(AssignmentTree node, TreePath p) {
        if (p == null)
            return super.visitAssignment(node, p);

        AssignmentTree at = (AssignmentTree) p.getLeaf();

        boolean result = scan(node.getExpression(), at.getExpression(), p);

        return result && scan(node.getVariable(), at.getVariable(), p);
    }

    public Boolean visitCompoundAssignment(CompoundAssignmentTree node, TreePath p) {
        if (p == null) {
            super.visitCompoundAssignment(node, p);
            return false;
        }

        CompoundAssignmentTree bt = (CompoundAssignmentTree) p.getLeaf();
        boolean result = scan(node.getExpression(), bt.getExpression(), p);

        return result && scan(node.getVariable(), bt.getVariable(), p);
    }

    public Boolean visitBinary(BinaryTree node, TreePath p) {
        if (p == null) {
            super.visitBinary(node, p);
            return false;
        }

        BinaryTree bt = (BinaryTree) p.getLeaf();
        boolean result = scan(node.getLeftOperand(), bt.getLeftOperand(), p);

        return result && scan(node.getRightOperand(), bt.getRightOperand(), p);
    }

    private boolean validateMultiVariable(Tree t, List<? extends TreePath> tps) {
        String name = getWildcardTreeName(t).toString();
        Collection<? extends TreePath> original = this.bindState.multiVariables.get(name);

        if (original == null) {
            this.bindState.multiVariables.put(name, tps);
            return true;
        } else {
            if (tps.size() != original.size()) {
                return false;
            }

            Iterator<? extends TreePath> orig = original.iterator();
            Iterator<? extends TreePath> current = tps.iterator();

            while (orig.hasNext() && current.hasNext()) {
                if (!scan(current.next(), orig.next())) {
                    return false;
                }
            }
            bindState.variables.put(name + "$" + ++bindState.matchCount, currentPath);

            return true;
        }
    }

    //TODO: currently, only the first matching combination is found:
    private boolean checkListsWithMultistatementTrees(List<? extends Tree> real, int realOffset, List<? extends Tree> pattern, int patternOffset, TreePath p) {
        while (realOffset < real.size() && patternOffset < pattern.size() && !isMultistatementWildcardTree(pattern.get(patternOffset))) {
            if (!scan(real.get(realOffset), pattern.get(patternOffset), p)) {
                return false;
            }

            realOffset++;
            patternOffset++;
        }

        if (realOffset == real.size() && patternOffset == pattern.size()) {
            return true;
        }

        if (patternOffset >= pattern.size()) {
            return false;
        }
        
        if (isMultistatementWildcardTree(pattern.get(patternOffset))) {
            if (patternOffset + 1 == pattern.size()) {
                List<TreePath> tps = new LinkedList<TreePath>();

                for (Tree t : real.subList(realOffset, real.size())) {
                    tps.add(new TreePath(getCurrentPath(), t));
                }

                return validateMultiVariable(pattern.get(patternOffset), tps);
            }

            List<TreePath> tps = new LinkedList<TreePath>();

            while (realOffset < real.size()) {
                State backup = State.copyOf(bindState);

                if (checkListsWithMultistatementTrees(real, realOffset, pattern, patternOffset + 1, p)) {
                    return validateMultiVariable(pattern.get(patternOffset), tps);
                }

                bindState = backup;

                tps.add(new TreePath(getCurrentPath(), real.get(realOffset)));

                realOffset++;
            }

            return false;
        }

        return false;
    }

    @Override
    public Boolean visitEmptyStatement(EmptyStatementTree node, TreePath p) {
        if (p == null) {
            super.visitEmptyStatement(node, p);
            return false;
        }
        return node.getKind() == p.getLeaf().getKind();
    }
    
    

    public Boolean visitBlock(BlockTree node, TreePath p) {
        if (p == null) {
            super.visitBlock(node, p);
            return false;
        }

        if (p.getLeaf().getKind() != Kind.BLOCK) {
            //single-statement blocks are considered to be equivalent to statements
            //TODO: some parents may need to be more strict, esp. synchronized and do-while
            assert node.getStatements().size() == 1;
            assert !node.isStatic();

            if (p.getLeaf() == searchingFor.getLeaf())
                return false;

            return checkLists(node.getStatements(), Collections.singletonList(p.getLeaf()), p.getParentPath());
        }

        BlockTree at = (BlockTree) p.getLeaf();

        if (node.isStatic() != at.isStatic()) {
            return false;
        }

        return checkLists(node.getStatements(), at.getStatements(), p);
    }

    public Boolean visitBreak(BreakTree node, TreePath p) {
        if (p == null) {
            super.visitBreak(node, p);
            return false;
        }

        //XXX: check labels
        return true;
    }

    public Boolean visitCase(CaseTree node, TreePath p) {
        if (p == null) {
            super.visitCase(node, p);
            return false;
        }

        CaseTree ct = (CaseTree) p.getLeaf();

        if (!scan(node.getExpression(), ct.getExpression(), p))
            return false;

        return checkLists(node.getStatements(), ct.getStatements(), p);
    }

    public Boolean visitCatch(CatchTree node, TreePath p) {
        if (p == null) {
            super.visitCatch(node, p);
            return false;
        }

        CatchTree ef = (CatchTree) p.getLeaf();

        if (!scan(node.getParameter(), ef.getParameter(), p))
            return false;

        return scan(node.getBlock(), ef.getBlock(), p);
    }

    public Boolean visitClass(ClassTree node, TreePath p) {
        if (p == null)
            return super.visitClass(node, p);

        ClassTree t = (ClassTree) p.getLeaf();

        String name = t.getSimpleName().toString();

        if (!name.isEmpty()) {
            if (!scan(node.getModifiers(), t.getModifiers(), p))
                return false;

            if (name.startsWith("$")) { //XXX: there should be a utility method for this check
                String existingName = bindState.variables2Names.get(name);
                String currentName = node.getSimpleName().toString();

                if (existingName != null) {
                    if (!existingName.equals(currentName)) {
                        return false;
                    }
                    bindState.variables.put(name + "$" + ++bindState.matchCount, currentPath);
                } else {
                    //XXX: putting the variable into both variables and variable2Names.
                    //variables is needed by the declarative hints to support conditions like
                    //referencedIn($variable, $statements$):
                    //causes problems in JavaFix, see visitIdentifier there.
                    bindState.variables.put(name, getCurrentPath());
                    bindState.variables2Names.put(name, currentName);
                }
            } else {
                if (!node.getSimpleName().contentEquals(name))
                    return false;
            }

            if (!checkLists(node.getTypeParameters(), t.getTypeParameters(), p))
                return false;

            if (!scan(node.getExtendsClause(), t.getExtendsClause(), p))
                return false;

            if (!checkLists(node.getImplementsClause(), t.getImplementsClause(), p))
                return false;
        } else {
            if (node.getSimpleName().length() != 0)
                return false;
        }

        return checkLists(filterHidden(info, getCurrentPath(), node.getMembers()), filterHidden(info, p, t.getMembers()), p);
    }

    public Boolean visitConditionalExpression(ConditionalExpressionTree node, TreePath p) {
        if (p == null) {
            super.visitConditionalExpression(node, p);
            return false;
        }

        ConditionalExpressionTree t = (ConditionalExpressionTree) p.getLeaf();

        if (!scan(node.getCondition(), t.getCondition(), p))
            return false;

        if (!scan(node.getFalseExpression(), t.getFalseExpression(), p))
            return false;

        return scan(node.getTrueExpression(), t.getTrueExpression(), p);
    }

    public Boolean visitContinue(ContinueTree node, TreePath p) {
        if (p == null) {
            super.visitContinue(node, p);
            return false;
        }

        //XXX: check labels
        return true;
    }

    public Boolean visitDoWhileLoop(DoWhileLoopTree node, TreePath p) {
        if (p == null) {
            super.visitDoWhileLoop(node, p);
            return false;
        }

        DoWhileLoopTree t = (DoWhileLoopTree) p.getLeaf();

        if (!scan(node.getStatement(), t.getStatement(), p))
            return false;

        return scan(node.getCondition(), t.getCondition(), p);
    }

//    public Boolean visitErroneous(ErroneousTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitExpressionStatement(ExpressionStatementTree node, TreePath p) {
        if (p == null) {
            super.visitExpressionStatement(node, p);
            return false;
        }

        ExpressionStatementTree et = (ExpressionStatementTree) p.getLeaf();

        return scan(node.getExpression(), et.getExpression(), p);
    }

    public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, TreePath p) {
        if (p == null) {
            super.visitEnhancedForLoop(node, p);
            return false;
        }

        EnhancedForLoopTree ef = (EnhancedForLoopTree) p.getLeaf();

        if (!scan(node.getVariable(), ef.getVariable(), p))
            return false;

        if (!scan(node.getExpression(), ef.getExpression(), p))
            return false;

        return scan(node.getStatement(), ef.getStatement(), p);
    }

    public Boolean visitForLoop(ForLoopTree node, TreePath p) {
        if (p == null)
            return super.visitForLoop(node, p);

        ForLoopTree t = (ForLoopTree) p.getLeaf();

        if (!checkLists(node.getInitializer(), t.getInitializer(), p)) {
            return false;
        }

        if (!scan(node.getCondition(), t.getCondition(), p))
            return false;

        if (!checkLists(node.getUpdate(), t.getUpdate(), p))
            return false;

        return scan(node.getStatement(), t.getStatement(), p);
    }

    @Override
    public Boolean visitLabeledStatement(LabeledStatementTree node, TreePath p) {
        if (p == null) {
            return super.visitLabeledStatement(node, p); 
        }
        LabeledStatementTree lst = (LabeledStatementTree)p.getLeaf();
        
        String ident = lst.getLabel().toString();

        if (ident.startsWith("$")) { // NOI18N
            if (bindState.variables2Names.containsKey(ident)) {
                if (!node.getLabel().contentEquals(bindState.variables2Names.get(ident))) {
                    return false;
                }
            } else {
                bindState.variables2Names.put(ident, node.getLabel().toString());
            }
        } else {
            if (!node.getLabel().toString().equals(ident)) {
                return false;
            }
        }
        return scan(node.getStatement(), lst.getStatement(), p);
    }

    public Boolean visitIdentifier(IdentifierTree node, TreePath p) {
        if (p == null)
            return super.visitIdentifier(node, p);

        switch (verifyElements(getCurrentPath(), p)) {
            case MATCH_CHECK_DEEPER:
                if (node.getKind() == p.getLeaf().getKind()) {
                    return true;
                }

                return deepVerifyIdentifier2MemberSelect(getCurrentPath(), p);
            case MATCH:
                return true;
            default:
            case NO_MATCH:
            case NO_MATCH_CONTINUE:
                return false;
        }
    }

    private boolean deepVerifyIdentifier2MemberSelect(TreePath identifier, TreePath memberSelect) {
        for (TreePath thisPath : prepareThis(identifier)) {
            State origState = State.copyOf(bindState);
            try {
                MemberSelectTree t = (MemberSelectTree) memberSelect.getLeaf();

                if (scan(thisPath.getLeaf(), t.getExpression(), memberSelect) == Boolean.TRUE) {
                    return true;
                }
            } finally {
                if (!options.contains(Options.KEEP_SYNTHETIC_THIS)) {
                    bindState = origState;
                }
            }
        }

        return false;
    }
    
    public Boolean visitIf(IfTree node, TreePath p) {
        if (p == null)
            return super.visitIf(node, p);

        IfTree t = (IfTree) p.getLeaf();

        if (!scan(node.getCondition(), t.getCondition(), p))
            return false;

        if (!scan(node.getThenStatement(), t.getThenStatement(), p))
            return false;

        return scan(node.getElseStatement(), t.getElseStatement(), p);
    }

//    public Boolean visitImport(ImportTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitArrayAccess(ArrayAccessTree node, TreePath p) {
        if (p == null)
            return super.visitArrayAccess(node, p);

        ArrayAccessTree t = (ArrayAccessTree) p.getLeaf();

        if (!scan(node.getExpression(), t.getExpression(), p))
            return false;

        return scan(node.getIndex(), t.getIndex(), p);
    }

//    public Boolean visitLabeledStatement(LabeledStatementTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitLiteral(LiteralTree node, TreePath p) {
        if (p == null)
            return super.visitLiteral(node, p);

        LiteralTree lt = (LiteralTree) p.getLeaf();
        Object nodeValue = node.getValue();
        Object ltValue = lt.getValue();

        if (nodeValue == ltValue)
            return true;

        if (nodeValue == null || ltValue == null)
            return false;
        return nodeValue.equals(ltValue);
    }

    public Boolean visitMethod(MethodTree node, TreePath p) {
        if (p == null)
            return super.visitMethod(node, p);

        MethodTree t = (MethodTree) p.getLeaf();

        if (!scan(node.getModifiers(), t.getModifiers(), p))
            return false;

        if (!checkLists(node.getTypeParameters(), t.getTypeParameters(), p))
            return false;

        Tree normalizedReturnType = "<init>".contentEquals(node.getName()) ? null : node.getReturnType();
        Tree normalizedReturnTypePattern = "<init>".contentEquals(t.getName()) ? null : t.getReturnType();

        if (!scan(normalizedReturnType, normalizedReturnTypePattern, p))
            return false;

        String name = t.getName().toString();

        if (name.startsWith("$")) { //XXX: there should be a utility method for this check
            String existingName = bindState.variables2Names.get(name);
            String currentName = node.getName().toString();

            if (existingName != null) {
                if (!existingName.equals(currentName)) {
                    return false;
                }
                bindState.variables.put(name + "$" + ++bindState.matchCount, currentPath);
            } else {
                //XXX: putting the variable into both variables and variable2Names.
                //variables is needed by the declarative hints to support conditions like
                //referencedIn($variable, $statements$):
                //causes problems in JavaFix, see visitIdentifier there.
                bindState.variables.put(name, getCurrentPath());
                bindState.variables2Names.put(name, currentName);
            }
        } else {
            if (!node.getName().contentEquals(name))
                return false;
        }

        if (!checkLists(node.getParameters(), t.getParameters(), p))
            return false;

        if (!checkLists(node.getThrows(), t.getThrows(), p))
            return false;

        if (!scan(node.getBody(), t.getBody(), p))
            return false;

        return scan(node.getDefaultValue(), t.getDefaultValue(), p);
    }
    
    public Boolean visitModifiers(ModifiersTree node, TreePath p) {
        if (p == null)
            return super.visitModifiers(node, p);

        ModifiersTree t = (ModifiersTree) p.getLeaf();
        List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(t.getAnnotations());
        IdentifierTree ident = !annotations.isEmpty() && annotations.get(0).getAnnotationType().getKind() == Kind.IDENTIFIER ? (IdentifierTree) annotations.get(0).getAnnotationType() : null;

        if (ident != null && options.contains(Options.ALLOW_VARIABLES_IN_PATTERN)) {
            annotations.remove(0);
            
            List<AnnotationTree> real = new ArrayList<AnnotationTree>(node.getAnnotations());
            final Set<Modifier> flags = EnumSet.noneOf(Modifier.class);
            
            flags.addAll(node.getFlags());
            
            if (!flags.containsAll(t.getFlags())) return false;
            
            flags.removeAll(t.getFlags());
            
            for (Iterator<AnnotationTree> it = annotations.iterator(); it.hasNext();) {
                AnnotationTree at = it.next();
                boolean found = false;
                
                for (Iterator<AnnotationTree> it2 = real.iterator(); it2.hasNext();) {
                    AnnotationTree r = it2.next();
                    State orig = State.copyOf(bindState);
                    
                    if (doSuperScan(r, new TreePath(p, at)) == Boolean.TRUE) {
                        it2.remove();
                        it.remove();
                        found = true;
                        break;
                    }
                    
                    bindState = orig;
                }
                
                if (!found) return false;
            }
            
            final boolean[] actualAnnotationsMask = new boolean[node.getAnnotations().size()];
            int ai = 0;
            
            for (AnnotationTree at : node.getAnnotations()) {
                actualAnnotationsMask[ai++] = real.contains(at);
            }
            
            class CallableTreePath extends TreePath implements Callable<Object[]> {
                public CallableTreePath(TreePath tp) {
                    super(tp.getParentPath(), tp.getLeaf());
                }
                @Override public Object[] call() throws Exception {
                    return new Object[] {
                        flags,
                        actualAnnotationsMask
                    };
                }
            }
            
            String name = ident.getName().toString();
            TreePath currentPath = new CallableTreePath(getCurrentPath());
            TreePath original = bindState.variables.get(name);

            if (original == null) {
                bindState.variables.put(name, currentPath);
                return true;
            } else {
                //XXX: not implemented yet...
                bindState.variables.put(name + "$" + ++bindState.matchCount, currentPath);
                return false;
            }
        }
        
        if (!checkLists(node.getAnnotations(), t.getAnnotations(), p))
            return false;

        return node.getFlags().equals(t.getFlags());
    }

    @Override
    public Boolean visitAnnotation(AnnotationTree node, TreePath p) {
        if (p == null)
            return super.visitAnnotation(node, p);

        AnnotationTree t = (AnnotationTree) p.getLeaf();
        
        List<? extends ExpressionTree> arguments = t.getArguments();
        
        if (arguments.size() == 1) {
            ExpressionTree arg = arguments.get(0);
            
            if (arg.getKind() == Kind.ASSIGNMENT) {
                AssignmentTree at = (AssignmentTree) arg;
                
                if (   at.getVariable().getKind() == Kind.IDENTIFIER
                    && isMultistatementWildcardTree(at.getExpression())
                    && ((IdentifierTree) at.getVariable()).getName().contentEquals("value")) {
                    arguments = Collections.singletonList(at.getExpression());
                }
            }
        }
        
        if (!checkLists(node.getArguments(), arguments, p))
            return false;

        return scan(node.getAnnotationType(), t.getAnnotationType(), p);
    }

    public Boolean visitNewArray(NewArrayTree node, TreePath p) {
        if (p == null)
            return super.visitNewArray(node, p);

        NewArrayTree t = (NewArrayTree) p.getLeaf();

        if (!checkLists(node.getDimensions(), t.getDimensions(), p))
            return false;

        if (!checkLists(node.getInitializers(), t.getInitializers(), p))
            return false;

        return scan(node.getType(), t.getType(), p);
    }

    public Boolean visitNewClass(NewClassTree node, TreePath p) {
        if (p == null)
            return super.visitNewClass(node, p);

        NewClassTree t = (NewClassTree) p.getLeaf();

        if (!scan(node.getIdentifier(), t.getIdentifier(), p))
            return false;

        if (!scan(node.getEnclosingExpression(), t.getEnclosingExpression(), p))
            return false;

        if (!checkLists(node.getTypeArguments(), t.getTypeArguments(), p))
            return false;

        if (!checkLists(node.getArguments(), t.getArguments(), p))
            return false;

        return scan(node.getClassBody(), t.getClassBody(), p);
    }

    public Boolean visitParenthesized(ParenthesizedTree node, TreePath p) {
        if (p == null)
            return super.visitParenthesized(node, p);

        ParenthesizedTree t = (ParenthesizedTree) p.getLeaf();

        return scan(node.getExpression(), t.getExpression(), p);
    }

    public Boolean visitReturn(ReturnTree node, TreePath p) {
        if (p == null) {
            super.visitReturn(node, p);
            return false;
        }

        ReturnTree at = (ReturnTree) p.getLeaf();

        return scan(node.getExpression(), at.getExpression(), p);
    }

    public Boolean visitMemberSelect(MemberSelectTree node, TreePath p) {
        if (p == null)
            return super.visitMemberSelect(node, p);

        switch (verifyElements(getCurrentPath(), p)) {
            case MATCH_CHECK_DEEPER:
                if (node.getKind() == p.getLeaf().getKind()) {
                    //to bind any free variables inside:
                    MemberSelectTree t = (MemberSelectTree) p.getLeaf();

                    return scan(node.getExpression(), t.getExpression(), p) == Boolean.TRUE;
                } else {
                    return deepVerifyIdentifier2MemberSelect(p, getCurrentPath());
                }
            case MATCH:
                return true;
            case NO_MATCH:
                return false;
        }

        if (node.getKind() != p.getLeaf().getKind()) {
            return false;
        }

        MemberSelectTree t = (MemberSelectTree) p.getLeaf();

        if (!scan(node.getExpression(), t.getExpression(), p))
            return false;

        String ident = t.getIdentifier().toString();

        if (ident.startsWith("$")) { //XXX: there should be a utility method for this check
            if (bindState.variables2Names.containsKey(ident)) {
                return node.getIdentifier().contentEquals(bindState.variables2Names.get(ident));
            } else {
                bindState.variables2Names.put(ident, node.getIdentifier().toString());
            }
            return true;
        }

        return node.getIdentifier().toString().equals(t.getIdentifier().toString());
    }

//    public Boolean visitEmptyStatement(EmptyStatementTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
    public Boolean visitSwitch(SwitchTree node, TreePath p) {
        if (p == null) {
            super.visitSwitch(node, p);
            return false;
        }

        SwitchTree st = (SwitchTree) p.getLeaf();

        if (!scan(node.getExpression(), st.getExpression(), p)) {
            return false;
        }

        return checkLists(node.getCases(), st.getCases(), p);
    }

    public Boolean visitSynchronized(SynchronizedTree node, TreePath p) {
        if (p == null) {
            super.visitSynchronized(node, p);
            return false;
        }

        SynchronizedTree at = (SynchronizedTree) p.getLeaf();

        if (!scan(node.getExpression(), at.getExpression(), p)) {
            return false;
        }

        return scan(node.getBlock(), at.getBlock(), p);
    }

    public Boolean visitThrow(ThrowTree node, TreePath p) {
        if (p == null) {
            super.visitThrow(node, p);
            return false;
        }

        ThrowTree at = (ThrowTree) p.getLeaf();

        return scan(node.getExpression(), at.getExpression(), p);
    }

//    public Boolean visitCompilationUnit(CompilationUnitTree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    public Boolean visitTry(TryTree node, TreePath p) {
        if (p == null) {
            super.visitTry(node, p);
            return false;
        }

        TryTree at = (TryTree) p.getLeaf();

        if (!checkLists(node.getResources(), at.getResources(), p)) {
            return false;
        }
        
        if (!scan(node.getBlock(), at.getBlock(), p)) {
            return false;
        }

        if (!checkLists(node.getCatches(), at.getCatches(), p)) {
            return false;
        }

        return scan(node.getFinallyBlock(), at.getFinallyBlock(), p);
    }

    public Boolean visitParameterizedType(ParameterizedTypeTree node, TreePath p) {
        if (p == null)
            return super.visitParameterizedType(node, p);

        ParameterizedTypeTree t = (ParameterizedTypeTree) p.getLeaf();

        if (!scan(node.getType(), t.getType(), p))
            return false;

        return checkLists(node.getTypeArguments(), t.getTypeArguments(), p);
    }

    public Boolean visitArrayType(ArrayTypeTree node, TreePath p) {
        if (p == null) {
            super.visitArrayType(node, p);
            return false;
        }

        ArrayTypeTree at = (ArrayTypeTree) p.getLeaf();

        return scan(node.getType(), at.getType(), p);
    }

    public Boolean visitTypeCast(TypeCastTree node, TreePath p) {
        if (p == null)
            return super.visitTypeCast(node, p);

        TypeCastTree t = (TypeCastTree) p.getLeaf();

        if (!scan(node.getType(), t.getType(), p))
            return false;

        return scan(node.getExpression(), t.getExpression(), p);
    }

    public Boolean visitPrimitiveType(PrimitiveTypeTree node, TreePath p) {
        if (p == null)
            return super.visitPrimitiveType(node, p);

        PrimitiveTypeTree t = (PrimitiveTypeTree) p.getLeaf();

        return node.getPrimitiveTypeKind() == t.getPrimitiveTypeKind();
    }

    public Boolean visitTypeParameter(TypeParameterTree node, TreePath p) {
        if (p == null)
            return super.visitTypeParameter(node, p);

        TypeParameterTree t = (TypeParameterTree) p.getLeaf();

        String name = t.getName().toString();

        if (name.startsWith("$")) { //XXX: there should be a utility method for this check
            String existingName = bindState.variables2Names.get(name);
            String currentName = node.getName().toString();

            if (existingName != null) {
                if (!existingName.equals(currentName)) {
                    return false;
                }
                bindState.variables.put(name + "$" + ++bindState.matchCount, currentPath);
            } else {
                //XXX: putting the variable into both variables and variable2Names.
                //variables is needed by the declarative hints to support conditions like
                //referencedIn($variable, $statements$):
                //causes problems in JavaFix, see visitIdentifier there.
                bindState.variables.put(name, getCurrentPath());
                bindState.variables2Names.put(name, currentName);
            }
        } else {
            if (!node.getName().contentEquals(name))
                return false;
        }
        
        return checkLists(node.getBounds(), t.getBounds(), p);
    }

    public Boolean visitInstanceOf(InstanceOfTree node, TreePath p) {
        if (p == null)
            return super.visitInstanceOf(node, p);

        InstanceOfTree t = (InstanceOfTree) p.getLeaf();

        if (!scan(node.getExpression(), t.getExpression(), p))
            return false;

        Tree nodePattern = node.getPattern();
        Tree pPattern = t.getPattern();

        if (nodePattern != null || pPattern != null) {
            return scan(nodePattern, pPattern, p);
        }

        return scan(node.getType(), t.getType(), p);
    }

    public Boolean visitUnary(UnaryTree node, TreePath p) {
        if (p == null)
            return super.visitUnary(node, p);

        UnaryTree t = (UnaryTree) p.getLeaf();

        return scan(node.getExpression(), t.getExpression(), p);
    }

    public Boolean visitVariable(VariableTree node, TreePath p) {
        if (p == null) {
            return super.visitVariable(node, p);
        }

        VariableTree t = (VariableTree) p.getLeaf();

        if (!scan(node.getModifiers(), t.getModifiers(), p))
            return false;

        if (!scan(node.getType(), t.getType(), p))
            return false;

        String name = t.getName().toString();

        if (name.startsWith("$")) { //XXX: there should be a utility method for this check
            // check whether there's a type constraint and if it is, check it for a match
            if (!typeMatches(getCurrentPath(), name)) {
                return false;
            }
            String existingName = bindState.variables2Names.get(name);
            String currentName = node.getName().toString();

            if (existingName != null) {
                if (!existingName.equals(currentName)) {
                    return false;
                }
                bindState.variables.put(name + "$" + ++bindState.matchCount, currentPath);
            } else {
                //XXX: putting the variable into both variables and variable2Names.
                //variables is needed by the declarative hints to support conditions like
                //referencedIn($variable, $statements$):
                //causes problems in JavaFix, see visitIdentifier there.
                bindState.variables.put(name, getCurrentPath());
                bindState.variables2Names.put(name, currentName);
            }
        } else if (allowVariablesRemap) {
            VariableElement nodeEl = (VariableElement) info.getTrees().getElement(getCurrentPath());
            VariableElement pEl = (VariableElement) info.getTrees().getElement(p);

            if (nodeEl != null && pEl != null && isSameTypeForVariableRemap(nodeEl.asType(), pEl.asType())) {
                bindState.variablesRemapToElement.put(pEl, nodeEl);
            }
        } else {
            if (!node.getName().contentEquals(name))
                return false;
        }

        return scan(node.getInitializer(), t.getInitializer(), p);
    }

    public Boolean visitWhileLoop(WhileLoopTree node, TreePath p) {
        if (p == null)
            return super.visitWhileLoop(node, p);

        WhileLoopTree t = (WhileLoopTree) p.getLeaf();

        if (!scan(node.getCondition(), t.getCondition(), p))
            return false;

        return scan(node.getStatement(), t.getStatement(), p);
    }

    public Boolean visitWildcard(WildcardTree node, TreePath p) {
        if (p == null)
            return super.visitWildcard(node, p);

        WildcardTree t = (WildcardTree) p.getLeaf();

        return scan(node.getBound(), t.getBound(), p);
    }

    @Override
    public Boolean visitLambdaExpression(LambdaExpressionTree node, TreePath p) {
        if (p == null)
            return super.visitLambdaExpression(node, p);

        LambdaExpressionTree t = (LambdaExpressionTree) p.getLeaf();

        if (!checkLists(node.getParameters(), t.getParameters(), p)) {
            return false;
        }
        return scan(node.getBody(), t.getBody(), p);
    }

    @Override
    public Boolean visitMemberReference(MemberReferenceTree node, TreePath p) {
        if (p == null)
            return super.visitMemberReference(node, p);

        MemberReferenceTree t = (MemberReferenceTree) p.getLeaf();
        
        if (node.getMode() != t.getMode())
            return false;

        if (!scan(node.getQualifierExpression(), t.getQualifierExpression(), p))
            return false;

        String ident = t.getName().toString();

        if (ident.startsWith("$")) { //XXX: there should be a utility method for this check
            if (bindState.variables2Names.containsKey(ident)) {
                return node.getName().contentEquals(bindState.variables2Names.get(ident));
            } else {
                bindState.variables2Names.put(ident, node.getName().toString());
            }
            return true;
        }

        return node.getName().contentEquals(t.getName());
    }

    public Boolean visitBindingPattern(BindingPatternTree node, TreePath p) {
        if (p == null) {
            return super.visitBindingPattern(node, p);
        }

        BindingPatternTree t = (BindingPatternTree) p.getLeaf();

        return scan(node.getVariable(), t.getVariable(), p);
    }

//
//    public Boolean visitOther(Tree node, TreePath p) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }


    private enum VerifyResult {
        MATCH_CHECK_DEEPER,
        MATCH,
        NO_MATCH_CONTINUE,
        NO_MATCH;
    }
    
    protected @NonNull VerifyResult verifyElements(TreePath node, TreePath p) {
        return options.contains(Options.NO_ELEMENT_VERIFY) ? unattributedVerifyElements(node, p) : fullVerifyElements(node, p);
    }

    private @NonNull VerifyResult fullVerifyElements(TreePath node, TreePath p) {
        Element nodeEl = info.getTrees().getElement(node);
        Element pEl    = info.getTrees().getElement(p);

        if (nodeEl == null) {
            return pEl == null ? VerifyResult.MATCH : VerifyResult.NO_MATCH_CONTINUE; //TODO: correct? shouldn't be MATCH_CHECK_DEEPER?
        }

        VerifyResult matchingResult;
        
        if (!nodeEl.getModifiers().contains(Modifier.STATIC)) {
            if ((nodeEl.getKind().isClass() || nodeEl.getKind().isInterface())) {
                //class:
                matchingResult = VerifyResult.MATCH;
            } else {
                matchingResult = VerifyResult.MATCH_CHECK_DEEPER;
            }
        } else {
            matchingResult = VerifyResult.MATCH;
            // defect #241261; if the current pattern is a member-select and the selector is a variable, it cannot match
            // a static member of node:
            if (p.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT && node.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                Tree selector = ((MemberSelectTree)p.getLeaf()).getExpression();
                if (getWildcardTreeName(selector) != null) {
                    // className.this/super refer to an instance and can be matched to a variable. They resolve to a variable Element, so exclude them
                    Element nodeSelector = info.getTrees().getElement(new TreePath(node, ((MemberSelectTree)node.getLeaf()).getExpression()));
                    if (nodeSelector != null && (nodeSelector.getKind().isClass() || nodeSelector.getKind().isInterface())) {
                        matchingResult = VerifyResult.NO_MATCH;
                    }  else {
                        matchingResult = VerifyResult.MATCH_CHECK_DEEPER;
                    }
                }
            }
        }

        if (nodeEl == pEl) {
            return matchingResult;
        }

        if (nodeEl == null || pEl == null)
            return VerifyResult.NO_MATCH;

        if (nodeEl.getKind() == pEl.getKind() && nodeEl.getKind() == ElementKind.FIELD
                && CLASS.contentEquals(((VariableElement)nodeEl).getSimpleName())
                && CLASS.contentEquals(((VariableElement)pEl).getSimpleName())) {
            return VerifyResult.MATCH_CHECK_DEEPER;
        }

        if (nodeEl.getKind() == pEl.getKind() && nodeEl.getKind() == ElementKind.METHOD) {
            if (info.getElements().overrides((ExecutableElement) nodeEl, (ExecutableElement) pEl, (TypeElement) nodeEl.getEnclosingElement())) {
                return VerifyResult.MATCH_CHECK_DEEPER;
            }
        }

        if (nodeEl.equals(pEl)) {
            return matchingResult;
        }

        if (allowVariablesRemap && nodeEl.equals(bindState.variablesRemapToElement.get(pEl))) {
            return matchingResult;
        }

        TypeMirror nodeTM = info.getTrees().getTypeMirror(node);

        if (nodeTM == null || nodeTM.getKind() == TypeKind.ERROR) {
            return VerifyResult.NO_MATCH_CONTINUE;
        }

        TypeMirror pTM = info.getTrees().getTypeMirror(p);

        if (pTM == null || pTM.getKind() == TypeKind.ERROR) {
            return VerifyResult.NO_MATCH_CONTINUE;
        }

        return VerifyResult.NO_MATCH;
    }

    private @NonNull VerifyResult unattributedVerifyElements(TreePath node, TreePath p) {
        if (getSimpleName(node.getLeaf()).contentEquals(getSimpleName(p.getLeaf()))) {
            boolean pureSelect = isPureMemberSelect(node.getLeaf(), true) && isPureMemberSelect(p.getLeaf(), true);

            return pureSelect ? VerifyResult.MATCH : VerifyResult.MATCH_CHECK_DEEPER;
        } else {
            return VerifyResult.NO_MATCH_CONTINUE;
        }
    }

    protected Iterable<? extends TreePath> prepareThis(TreePath tp) {
        return options.contains(Options.NO_ELEMENT_VERIFY) ? unattributedPrepareThis(tp) : fullPrepareThis(tp);
    }

    private Iterable<? extends TreePath> fullPrepareThis(TreePath tp) {
        //XXX: is there a faster way to do this?
        Enter enter = Enter.instance(JavaSourceAccessor.getINSTANCE().getJavacTask(info).getContext());
        Collection<TreePath> result = new LinkedList<TreePath>();

        while (tp != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                Element currentElement = info.getTrees().getElement(tp);

                if (currentElement == null || !(currentElement instanceof ClassSymbol)) continue;
                Env<AttrContext> env = enter.getEnv((ClassSymbol) currentElement);
                ExpressionTree thisTree = info.getTreeUtilities().parseExpression("this", new SourcePositions[1]);

                info.getTreeUtilities().attributeTree(thisTree, new HackScope(env));

                result.add(new TreePath(tp, thisTree));
            }
            
            tp = tp.getParentPath();
        }

        return result;
    }

    public static final class HackScope implements Scope {

        private final Env<AttrContext> env;

        public HackScope(Env<AttrContext> env) {
            this.env = env;
        }

        public Env<AttrContext> getEnv() {
            return env;
        }

        @Override
        public Scope getEnclosingScope() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public TypeElement getEnclosingClass() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public ExecutableElement getEnclosingMethod() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Iterable<? extends Element> getLocalElements() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }

    private Iterable<? extends TreePath> unattributedPrepareThis(TreePath tp) {
        ExpressionTree thisTree = info.getTreeUtilities().parseExpression("this", new SourcePositions[1]);

        return Collections.singleton(new TreePath(tp, thisTree));
    }

    private boolean isSameTypeForVariableRemap(TypeMirror nodeType, TypeMirror pType) {
        //TODO: subtypes could be OK for remap?
        return info.getTypes().isSameType(nodeType, pType);
    }

    private static Name getSimpleName(Tree t) {
        if (t.getKind() == Kind.IDENTIFIER) {
            return ((IdentifierTree) t).getName();
        }
        if (t.getKind() == Kind.MEMBER_SELECT) {
            return ((MemberSelectTree) t).getIdentifier();
        }

        throw new UnsupportedOperationException();
    }

    //XXX: duplicated in IntroduceHint
    public static List<? extends StatementTree> getStatements(TreePath firstLeaf) {
        switch (firstLeaf.getParentPath().getLeaf().getKind()) {
            case BLOCK:
                return ((BlockTree) firstLeaf.getParentPath().getLeaf()).getStatements();
            case CASE:
                CaseTree caseTree = (CaseTree) firstLeaf.getParentPath().getLeaf();
                if (caseTree.getStatements() != null) {
                    return caseTree.getStatements();
                } else if (caseTree.getBody() instanceof StatementTree) {
                    return Collections.singletonList((StatementTree) caseTree.getBody());
                } else {
                    return null;
                }
            default:
                return Collections.singletonList((StatementTree) firstLeaf.getLeaf());
        }
    }

    public static final class VariableAssignments {
        public final Map<String, TreePath> variables;
        public final Map<String, Collection<? extends TreePath>> multiVariables;
        public final Map<String, String> variables2Names;
        public final Map<Element, Element> variablesRemapToElement;
        public final Map<Element, TreePath> variablesRemapToTrees;

        public VariableAssignments(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names) {
            this.variables = variables;
            this.multiVariables = multiVariables;
            this.variables2Names = variables2Names;
            this.variablesRemapToElement = null;
            this.variablesRemapToTrees = null;
        }

        public VariableAssignments(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
            this.variables = variables;
            this.multiVariables = multiVariables;
            this.variables2Names = variables2Names;
            this.variablesRemapToElement = variablesRemapToElement;
            this.variablesRemapToTrees = variablesRemapToTrees;
        }

        VariableAssignments(State state) {
            this.variables = state.variables;
            this.multiVariables = state.multiVariables;
            this.variables2Names = state.variables2Names;
            this.variablesRemapToElement = state.variablesRemapToElement;
            this.variablesRemapToTrees = state.variablesRemapToTrees;
        }
    }

    public static final class MethodDuplicateDescription {
        public final TreePath firstLeaf;
        public final int dupeStart;
        public final int dupeEnd;
        public final Map<Element, Element> variablesRemapToElement;
        public final Map<Element, TreePath> variablesRemapToTrees;
        public MethodDuplicateDescription(TreePath firstLeaf, int dupeStart, int dupeEnd, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
            this.firstLeaf = firstLeaf;
            this.dupeStart = dupeStart;
            this.dupeEnd = dupeEnd;
            this.variablesRemapToElement = variablesRemapToElement;
            this.variablesRemapToTrees = variablesRemapToTrees;
        }
    }

    public static final class State {
        final Map<String, TreePath> variables;
        final Map<String, Collection<? extends TreePath>> multiVariables;
        final Map<String, String> variables2Names;
        final Map<Element, Element> variablesRemapToElement;
        final Map<Element, TreePath> variablesRemapToTrees;
        int   matchCount;

        private State(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees, int matchCount) {
            this.variables = variables;
            this.multiVariables = multiVariables;
            this.variables2Names = variables2Names;
            this.variablesRemapToElement = variablesRemapToElement;
            this.variablesRemapToTrees = variablesRemapToTrees;
            this.matchCount = matchCount;
        }
        public static State empty() {
            return new State(new HashMap<String, TreePath>(), new HashMap<String, Collection<? extends TreePath>>(), new HashMap<String, String>(), new HashMap<Element, Element>(), new HashMap<Element, TreePath>(), 0);
        }

        static State startFrom(State original) {
            return new State(new HashMap<String, TreePath>(original.variables), new HashMap<String, Collection<? extends TreePath>>(original.multiVariables), new HashMap<String, String>(original.variables2Names), new HashMap<Element, Element>(original.variablesRemapToElement), new HashMap<Element, TreePath>(original.variablesRemapToTrees), 0);
        }

        public static State copyOf(State original) {
            return new State(new HashMap<String, TreePath>(original.variables), new HashMap<String, Collection<? extends TreePath>>(original.multiVariables), new HashMap<String, String>(original.variables2Names), new HashMap<Element, Element>(original.variablesRemapToElement), new HashMap<Element, TreePath>(original.variablesRemapToTrees), 
            original.matchCount);
        }

        public static State from(State original, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
            return new State(new HashMap<String, TreePath>(original.variables), new HashMap<String, Collection<? extends TreePath>>(original.multiVariables), new HashMap<String, String>(original.variables2Names), variablesRemapToElement, variablesRemapToTrees, original.matchCount);
        }

        public static State from(Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names) {
            return new State(variables, multiVariables, variables2Names, new HashMap<Element, Element>(), new HashMap<Element, TreePath>(), 0);
        }
    }

    public enum Options {
        ALLOW_VARIABLES_IN_PATTERN, ALLOW_REMAP_VARIABLE_TO_EXPRESSION, ALLOW_GO_DEEPER, NO_ELEMENT_VERIFY, KEEP_SYNTHETIC_THIS;
    }
    
    //TODO: copied from java.hints' Utilities:
    public static boolean isMultistatementWildcard(@NonNull CharSequence name) {
        return name.charAt(name.length() - 1) == '$';
    }

    public static boolean isMultistatementWildcardTree(Tree tree) {
        CharSequence name = getWildcardTreeName(tree);

        return name != null && isMultistatementWildcard(name);
    }

    public static @CheckForNull CharSequence getWildcardTreeName(@NonNull Tree t) {
        if (t.getKind() == Kind.EXPRESSION_STATEMENT && ((ExpressionStatementTree) t).getExpression().getKind() == Kind.IDENTIFIER) {
            IdentifierTree identTree = (IdentifierTree) ((ExpressionStatementTree) t).getExpression();

            return identTree.getName().toString();
        }

        if (t.getKind() == Kind.IDENTIFIER) {
            IdentifierTree identTree = (IdentifierTree) t;
            String name = identTree.getName().toString();

            if (name.startsWith("$")) {
                return name;
            }
        }
        
        if (t.getKind() == Kind.TYPE_PARAMETER && ((TypeParameterTree) t).getBounds().isEmpty()) {
            String name = ((TypeParameterTree) t).getName().toString();

            if (name.startsWith("$")) {
                return name;
            }
        }

        return null;
    }

    public static boolean isPureMemberSelect(Tree mst, boolean allowVariables) {
        switch (mst.getKind()) {
            case IDENTIFIER: return allowVariables || ((IdentifierTree) mst).getName().charAt(0) != '$';
            case MEMBER_SELECT: return isPureMemberSelect(((MemberSelectTree) mst).getExpression(), allowVariables);
            default: return false;
        }
    }

    /**
     * Only for members (i.e. generated constructor):
     */
    public static List<? extends Tree> filterHidden(CompilationInfo info, TreePath basePath, Iterable<? extends Tree> members) {
        List<Tree> result = new LinkedList<Tree>();

        for (Tree t : members) {
            if (!info.getTreeUtilities().isSynthetic(new TreePath(basePath, t))) {
                result.add(t);
            }
        }

        return result;
    }

    public static boolean containsMultistatementTrees(List<? extends Tree> statements) {
        for (Tree t : statements) {
            if (isMultistatementWildcardTree(t)) {
                return true;
            }
        }

        return false;
    }

    public interface Cancel {
        public boolean isCancelled();
    }
}
