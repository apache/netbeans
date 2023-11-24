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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.hints.SideEffectVisitor;
import org.netbeans.modules.java.hints.StopProcessing;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author lahvac
 */
public class UnusedAssignmentOrBranch {
    
    private static final String UNUSED_ASSIGNMENT_ID = "org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.unusedAssignment";    // NOI18N
    private static final String UNUSED_COMPOUND_ASSIGNMENT_ID = "org.netbeans.modules.java.hints.bugs.UnusedCompoundAssignment";    // NOI18N
    private static final String DEAD_BRANCH_ID = "org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.deadBranch";    // NOI18N
    private static final Object KEY_COMPUTED_ASSIGNMENTS = new Object();

    private static Pair<Set<Tree>, Set<Element>> computeUsedAssignments(final HintContext ctx) {
        final CompilationInfo info = ctx.getInfo();
        Pair<Set<Tree>, Set<Element>> result = (Pair<Set<Tree>, Set<Element>>) info.getCachedValue(KEY_COMPUTED_ASSIGNMENTS);

        if (result != null) return result;

        FlowResult flow = Flow.assignmentsForUse(ctx);

        if (flow == null) return null;

        final Set<Tree> usedAssignments = new HashSet<Tree>();

        for (Iterable<? extends TreePath> i : flow.getAssignmentsForUse().values()) {
            for (TreePath tp : i) {
                if (tp == null) continue;

                usedAssignments.add(tp.getLeaf());
            }
        }

        final Set<Element> usedVariables = new HashSet<Element>();

        new CancellableTreePathScanner<Void, Void>() {
            @Override public Void visitAssignment(AssignmentTree node, Void p) {
                Element var = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

                if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(node.getExpression())) {
                    scan(node.getExpression(), null);
                    return null;
                }

                return super.visitAssignment(node, p);
            }
            @Override public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
                Element var = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

                if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(node.getExpression())) {
                    scan(node.getExpression(), null);
                    return null;
                }

                return super.visitCompoundAssignment(node, p);
            }
            @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                Element var = info.getTrees().getElement(getCurrentPath());

                if (var != null && LOCAL_VARIABLES.contains(var.getKind())) {
                    usedVariables.add(var);
                }
                return super.visitIdentifier(node, p);
            }
            @Override protected boolean isCanceled() {
                return ctx.isCanceled();
            }
        }.scan(info.getCompilationUnit(), null);

        info.putCachedValue(KEY_COMPUTED_ASSIGNMENTS, result = Pair.<Set<Tree>, Set<Element>>of(usedAssignments, usedVariables), CacheClearPolicy.ON_TASK_END);

        return result;
    }

    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.unusedAssignment", description = "#DESC_org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.unusedAssignment", category="bugs", id=UNUSED_ASSIGNMENT_ID, options={Options.QUERY}, suppressWarnings="UnusedAssignment")
    @TriggerPatterns({
        @TriggerPattern("$var = $value"),
        @TriggerPattern("$mods$ $type $var = $value;")
    })
    public static ErrorDescription unusedAssignment(final HintContext ctx) {
        final CompilationInfo info = ctx.getInfo();
        Element var = info.getTrees().getElement(ctx.getVariables().get("$var"));

        if (var == null || !LOCAL_VARIABLES.contains(var.getKind()) ||
            isImplicitParamOfRecordCanonicalConstructor(info, var)) {
            return null;
        }

        final String unusedAssignmentLabel = NbBundle.getMessage(UnusedAssignmentOrBranch.class, "LBL_UNUSED_ASSIGNMENT_LABEL");
        Pair<Set<Tree>, Set<Element>> computedAssignments = computeUsedAssignments(ctx);
        
        if (ctx.isCanceled() || computedAssignments == null) return null;

        final Set<Tree> usedAssignments = computedAssignments.first();
        final Set<Element> usedVariables = computedAssignments.second();
        TreePath valuePath = ctx.getVariables().get("$value");
        Tree value = (valuePath == null ? ctx.getPath() : valuePath).getLeaf();

        if (!usedAssignments.contains(value) && usedVariables.contains(var)) {
            return ErrorDescriptionFactory.forTree(ctx, value, unusedAssignmentLabel);
        }

        return null;
    }

    private static boolean isImplicitParamOfRecordCanonicalConstructor(CompilationInfo info, Element el) {
        Element enclosingElement = el.getEnclosingElement();

        if (enclosingElement.getKind() != ElementKind.CONSTRUCTOR) {
            return false;
        }

        ExecutableElement constr = (ExecutableElement) enclosingElement;

        return info.getElements().isCompactConstructor(constr) &&
               constr.getParameters().contains(el);
    }

    private static boolean mayHaveSideEffects(HintContext ctx, TreePath path) {
        SideEffectVisitor visitor = new SideEffectVisitor(ctx).stopOnUnknownMethods(true);
        Tree culprit = null;
        try {
            visitor.scan(path, null);
            return false;
        } catch (StopProcessing stop) {
            culprit = stop.getValue();
        }
        return culprit != null;
    }
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.unusedCompoundAssignment", 
            description = "#DESC_org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.unusedCompoundAssignment", 
            category="bugs", 
            id=UNUSED_COMPOUND_ASSIGNMENT_ID, 
            options={Options.QUERY}, suppressWarnings="UnusedAssignment")
    @TriggerPatterns({
        @TriggerPattern("$var |= $expr"),
        @TriggerPattern("$var &= $expr"),
        @TriggerPattern("$var += $expr"),
        @TriggerPattern("$var -= $expr"),
        @TriggerPattern("$var *= $expr"),
        @TriggerPattern("$var /= $expr"),
        @TriggerPattern("$var %= $expr"),
        @TriggerPattern("$var >>= $expr"),
        @TriggerPattern("$var <<= $expr"),
        @TriggerPattern("$var >>>= $expr")
    })
    @NbBundle.Messages({
        "LBL_UnusedCompoundAssignmentLabel=The target variable's value is never used",
        "FIX_ChangeCompoundAssignmentToOperation=Change compound assignment to operation"
    })
    public static ErrorDescription unusedCompoundAssignment(final HintContext ctx) {
        final String unusedAssignmentLabel = Bundle.LBL_UnusedCompoundAssignmentLabel();
        Pair<Set<Tree>, Set<Element>> computedAssignments = computeUsedAssignments(ctx);
        
        if (ctx.isCanceled() || computedAssignments == null) return null;

        final CompilationInfo info = ctx.getInfo();
        final Set<Tree> usedAssignments = computedAssignments.first();
        final Set<Element> usedVariables = computedAssignments.second();
        final Element var = info.getTrees().getElement(ctx.getVariables().get("$var")); // NOI18N
        final TreePath valuePath = ctx.getVariables().get("$expr"); // NOI18N
        final Tree value = ctx.getPath().getLeaf();
        final TypeMirror tm = info.getTrees().getTypeMirror(ctx.getPath());
        final boolean sideEffects;
        final boolean booleanOp = Utilities.isValidType(tm) && tm.getKind() == TypeKind.BOOLEAN;
        Tree.Kind kind = value.getKind();
        if (booleanOp) {
            sideEffects = mayHaveSideEffects(ctx, valuePath);
        } else {
            sideEffects = false;
        }

        if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(value) && usedVariables.contains(var)) {
            String replace;
            switch (kind) {
                case AND_ASSIGNMENT:
                    if (booleanOp) {
                        replace = sideEffects ? "$expr && $var" : "$var && $expr"; // NOI18N
                    } else {
                        replace = "$var & $expr"; // NOI18N
                    }
                    break;
                case OR_ASSIGNMENT:
                    if (booleanOp) {
                        replace = sideEffects ? "$expr || $var" : "$var || $expr"; // NOI18N
                    } else {
                        replace = "$var | $expr"; // NOI18N
                    }
                    break;
                case PLUS_ASSIGNMENT:
                    replace = "$var + $expr"; // NOI18N
                    break;
                case MINUS_ASSIGNMENT:
                    replace = "$var - $expr"; // NOI18N
                    break;
                case MULTIPLY_ASSIGNMENT:
                    replace = "$var * $expr"; // NOI18N
                    break;
                case DIVIDE_ASSIGNMENT:
                    replace = "$var / $expr"; // NOI18N
                    break;
                case REMAINDER_ASSIGNMENT:
                    replace = "$var % $expr"; // NOI18N
                    break;
                case LEFT_SHIFT_ASSIGNMENT:
                    replace = "$var << $expr"; // NOI18N
                    break;
                case RIGHT_SHIFT_ASSIGNMENT:
                    replace = "$var >> $expr"; // NOI18N
                    break;
                case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
                    replace = "$var >>> $expr"; // NOI18N
                    break;
                default:
                    return null;
            }
            
            return ErrorDescriptionFactory.forTree(ctx, value, unusedAssignmentLabel,
                    JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_ChangeCompoundAssignmentToOperation(), ctx.getPath(), replace)
            );
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.deadBranch", description = "#DESC_org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.deadBranch", category="bugs", id=DEAD_BRANCH_ID, options=Options.QUERY, suppressWarnings="DeadBranch")
    @TriggerTreeKind(Tree.Kind.IF)
    public static List<ErrorDescription> deadBranch(HintContext ctx) {
        String deadBranchLabel = NbBundle.getMessage(UnusedAssignmentOrBranch.class, "LBL_DEAD_BRANCH");
        FlowResult flow = Flow.assignmentsForUse(ctx);

        if (flow == null) return null;

        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        Set<? extends Tree> flowResult = flow.getDeadBranches();
        IfTree it = (IfTree) ctx.getPath().getLeaf();
        
        if (flowResult.contains(it.getThenStatement())) {
            result.add(ErrorDescriptionFactory.forTree(ctx, it.getThenStatement(), deadBranchLabel));
        }
        Tree t = it.getElseStatement();
        if (flowResult.contains(t)) {
            result.add(ErrorDescriptionFactory.forTree(ctx, t, deadBranchLabel));
            while (t != null && t.getKind() == Tree.Kind.IF) {
                it = (IfTree)t;
                t = it.getElseStatement();
                result.add(ErrorDescriptionFactory.forTree(ctx, t, deadBranchLabel));
            }
        }
        return result;
    }

}
