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

import com.sun.source.tree.*;
import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreeScanner;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.*;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Hint(displayName="#DN_NPECheck", 
        description="#DESC_NPECheck", 
        category="bugs", options=Options.QUERY, suppressWarnings = {"null", "", "NullableProblems"}
)
public class NPECheck {

    static final boolean DEF_ENABLE_FOR_FIELDS = false;
    @BooleanOption(displayName = "#LBL_NPECheck.ENABLE_FOR_FIELDS", tooltip = "#TP_NPECheck.ENABLE_FOR_FIELDS", defaultValue=DEF_ENABLE_FOR_FIELDS)
    static final String KEY_ENABLE_FOR_FIELDS = "enable-for-fields"; // NOI18N
    
    static final boolean DEF_UNBOXING_UNKNOWN_VALUES = true;
    @BooleanOption(displayName = "#LBL_NPECheck.UNBOXING_UNKNOWN_VALUES", tooltip = "#TP_NPECheck.UNBOXING_UNKNOWN_VALUES", defaultValue=DEF_UNBOXING_UNKNOWN_VALUES)
    static final String KEY_UNBOXING_UNKNOWN_VALUES = "unboxing-unknown"; // NOI18N

    private static final State DEFAULT_STATE = new State(StateEnum.POSSIBLE_NULL);

    @TriggerPatterns({
        @TriggerPattern("$mods$ $type $var = $expr;"),
        @TriggerPattern("$var = $expr")
    })
    public static ErrorDescription assignment(HintContext ctx) {
        Element e = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$var"));

        if (!isVariableElement(ctx, e)) {
            return null;
        }
        
        TreePath expr = ctx.getVariables().get("$expr");
        StateEnum r = computeExpressionsState(ctx).getOrDefault(expr.getLeaf(), DEFAULT_STATE).thisTypeState;

        State elementState = getStateFromAnnotations(ctx.getInfo(), e);

        if (elementState != null && elementState.isNotNull()) {
            String key = null;

            if (r == StateEnum.NULL) {
                key = "ERR_AssigningNullToNotNull";
            }

            if (r == StateEnum.POSSIBLE_NULL_REPORT) {
                key = "ERR_PossibleAssigingNullToNotNull";
            }

            if (key != null) {
                return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), NbBundle.getMessage(NPECheck.class, key));
            }
        }

        return null;
    }

    @TriggerPatterns({
        @TriggerPattern(value = "$expr ? $trueExpr : $falseExpr", constraints = {
            @ConstraintVariableType(variable = "$trueExpr", type = "double"),
            @ConstraintVariableType(variable = "$falseExpr", type = "java.lang.Number")
        }),
        
        @TriggerPattern(value = "$expr ? $trueExpr : $falseExpr", constraints = {
            @ConstraintVariableType(variable = "$falseExpr", type = "double"),
            @ConstraintVariableType(variable = "$trueExpr", type = "java.lang.Number")
        }),

        @TriggerPattern(value = "$expr ? $trueExpr : $falseExpr", constraints = {
            @ConstraintVariableType(variable = "$trueExpr", type = "boolean"),
            @ConstraintVariableType(variable = "$falseExpr", type = "java.lang.Boolean")
        }),
        
        @TriggerPattern(value = "$expr ? $trueExpr : $falseExpr", constraints = {
            @ConstraintVariableType(variable = "$falseExpr", type = "boolean"),
            @ConstraintVariableType(variable = "$trueExpr", type = "java.lang.Boolean")
        }),
        
        @TriggerPattern(value = "$expr ? $trueExpr : $falseExpr", constraints = {
            @ConstraintVariableType(variable = "$trueExpr", type = "char"),
            @ConstraintVariableType(variable = "$falseExpr", type = "java.lang.Character")
        }),
        
        @TriggerPattern(value = "$expr ? $trueExpr : $falseExpr", constraints = {
            @ConstraintVariableType(variable = "$falseExpr", type = "char"),
            @ConstraintVariableType(variable = "$trueExpr", type = "java.lang.Character")
        }),
    })
    public static ErrorDescription unboxingConditional(HintContext ctx) {
        CompilationInfo ci = ctx.getInfo();
        TypeMirror leftType = ci.getTrees().getTypeMirror(ctx.getVariables().get("$trueExpr")); // NOI18N
        TypeMirror rightType = ci.getTrees().getTypeMirror(ctx.getVariables().get("$falseExpr")); // NOI18N
        TypeMirror resType = ci.getTrees().getTypeMirror(ctx.getPath()); // NOI18N
        
        if (!Utilities.isValidType(leftType) || !Utilities.isValidType(rightType) || !Utilities.isValidType(resType)) {
            return null;
        }
        if (!resType.getKind().isPrimitive()) {
            return null;
        }
        TreePath npPath;
        TypeMirror np;
        
        if (leftType.getKind().isPrimitive()) {
            if (rightType.getKind().isPrimitive()) {
                npPath = null;
                np = null;
            } else {
                np = rightType;
                npPath = ctx.getVariables().get("$falseExpr"); // NOI18N
            }
        } else if (!rightType.getKind().isPrimitive()) {
            // one more check: if BOTh are declared and BOTH are primitive wrappers and they
            // are not SAME, then unboxing still occurs.
            if (!Utilities.isPrimitiveWrapperType(leftType) ||
                !Utilities.isPrimitiveWrapperType(rightType) ||
                 ci.getTypes().isSameType(leftType, rightType)) {
                return null;
            }
            Object o = ci.getCachedValue(KEY_CONDITIONAL_PARAMETER);
            if (o == null) {
                np = leftType;
                npPath = ctx.getVariables().get("$trueExpr"); // NOI18N
                ci.putCachedValue(KEY_CONDITIONAL_PARAMETER, Boolean.TRUE, CompilationInfo.CacheClearPolicy.ON_TASK_END);
            } else {
                np = rightType;
                npPath = ctx.getVariables().get("$falseExpr"); // NOI18N
            }
        } else {
            np = leftType;
            npPath = ctx.getVariables().get("$trueExpr"); // NOI18N
        }
        if (np == null || !ci.getTypes().isAssignable(np, resType)) {
            return null;
        }
        assert npPath != null;

        Map<Tree, State> expressionsState = computeExpressionsState(ctx);
        StateEnum s = expressionsState.getOrDefault(npPath.getLeaf(), DEFAULT_STATE).thisTypeState;
        String k;
        
        if (s == null || s == StateEnum.POSSIBLE_NULL || s == StateEnum.POSSIBLE_NULL_EXPLICIT_UNSPECIFIED) {
            boolean report = ctx.getPreferences().getBoolean(KEY_UNBOXING_UNKNOWN_VALUES, DEF_UNBOXING_UNKNOWN_VALUES);
            if (!report) {
                return null;
            }
            k =  "ERR_UnboxingPotentialNullValue"; // NOI18N
        } else switch (s) {
            case NULL:
                k = "ERR_UnboxingNullValue"; // NOI18N
                break;
            case POSSIBLE_NULL_REPORT:
                k = "ERR_UnboxingPotentialNullValue"; // NOI18N
                break;
            case NOT_NULL_BE_NPE:
            case NOT_NULL:
                return null;
            default:
                throw new AssertionError(s.name());
            
        }
        return ErrorDescriptionFactory.forTree(ctx, npPath, 
                NbBundle.getMessage(NPECheck.class, k));
    }
    
    
    @TriggerTreeKind({Kind.SWITCH, Kind.SWITCH_EXPRESSION})
    public static ErrorDescription switchExpression(HintContext ctx) {
        ExpressionTree selector;
        List<? extends CaseTree> cases;
        Tree swtch = ctx.getPath().getLeaf();

        switch (swtch.getKind()) {
            case SWITCH -> {
                SwitchTree st = (SwitchTree) swtch;
                selector = st.getExpression();
                cases = st.getCases();
            }
            case SWITCH_EXPRESSION -> {
                SwitchExpressionTree st = (SwitchExpressionTree) swtch;
                selector = st.getExpression();
                cases = st.getCases();
            }
            default -> throw new IllegalStateException("Unexpected tree kind: " + swtch.getKind());
        }

        TreePath select = new TreePath(ctx.getPath(), selector);
        TypeMirror m = ctx.getInfo().getTrees().getTypeMirror(select);
        if (m == null || m.getKind() != TypeKind.DECLARED) {
            return null;
        }

        boolean hasNullCase = cases.stream()
                                   .flatMap(ct -> ct.getLabels().stream())
                                   .filter(clt -> clt.getKind() == Kind.CONSTANT_CASE_LABEL)
                                   .map(ctl -> ((ConstantCaseLabelTree) ctl).getConstantExpression())
                                   .anyMatch(expr -> expr.getKind() == Kind.NULL_LITERAL);

        if (hasNullCase) {
            return null;
        }

        StateEnum r = computeExpressionsState(ctx).getOrDefault(select.getLeaf(), DEFAULT_STATE).thisTypeState;
        if (r == StateEnum.NULL) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_DereferencingNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }

        if (r == StateEnum.POSSIBLE_NULL_REPORT) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_PossiblyDereferencingNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        return null;
    }
    
    @TriggerPattern("for ($type $var : $expr) $stmts$")
    public static ErrorDescription enhancedFor(HintContext ctx) {
        TreePath colExpr = ctx.getVariables().get("$expr");
        if (colExpr == null) {
            return null;
        }
        StateEnum r =  computeExpressionsState(ctx).getOrDefault(colExpr.getLeaf(), DEFAULT_STATE).thisTypeState;
        if (r == StateEnum.NULL) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_DereferencingNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }

        if (r == StateEnum.POSSIBLE_NULL_REPORT) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_PossiblyDereferencingNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        return null;
    }
    
    @TriggerPattern("$select.$variable")
    public static ErrorDescription memberSelect(HintContext ctx) {
        TreePath select = ctx.getVariables().get("$select");
        StateEnum r = computeExpressionsState(ctx).getOrDefault(select.getLeaf(), DEFAULT_STATE).thisTypeState;
        
        if (r == StateEnum.NULL) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_DereferencingNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }

        if (r == StateEnum.POSSIBLE_NULL_REPORT) {
            String displayName = NbBundle.getMessage(NPECheck.class, "ERR_PossiblyDereferencingNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        
        return null;
    }
    
    /**
     * Checks for a possible null dereference - if the path denotes a possibly
     * null expression.
     * 
     */
    public static boolean isSafeToDereference(CompilationInfo info, TreePath path) {
        StateEnum r = computeExpressionsState(info, null).getOrDefault(path.getLeaf(), DEFAULT_STATE).thisTypeState;
        // copied from warning issued on redundant != null.
        return r != null && r.isNotNull();
    }
    
    @TriggerTreeKind(Kind.METHOD_INVOCATION)
    public static List<ErrorDescription> methodInvocation(HintContext ctx) {
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();
        List<State> paramStates = new ArrayList<>(mit.getArguments().size());
        Map<Tree, State> expressionsState = computeExpressionsState(ctx);

        for (Tree param : mit.getArguments()) {
            State r = expressionsState.getOrDefault(param, DEFAULT_STATE);
            paramStates.add(r);
        }

        Element e = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (e == null || e.getKind() != ElementKind.METHOD) {
            return null;
        }

        ExecutableElement ee = (ExecutableElement) e;
        int index = 0;
        List<ErrorDescription> result = new ArrayList<>();
        List<? extends VariableElement> params = ee.getParameters();

        for (VariableElement param : params) {
            State declaredState = getStateFromAnnotations(ctx.getInfo(), param);
            if (!ee.isVarArgs() || param != params.get(params.size() - 1)) {
                switch (statesMatch(declaredState, paramStates.get(index))) {
                    case TOP_LEVEL_NULL_TO_NONNULL:
                        result.add(ErrorDescriptionFactory.forTree(ctx, mit.getArguments().get(index), NbBundle.getMessage(NPECheck.class, "ERR_NULL_TO_NON_NULL_ARG")));
                        break;
                    case TOP_LEVEL_POSSIBLE_NULL_TO_NONNULL:
                        result.add(ErrorDescriptionFactory.forTree(ctx, mit.getArguments().get(index), NbBundle.getMessage(NPECheck.class, "ERR_POSSIBLENULL_TO_NON_NULL_ARG")));
                        break;
                    case MISMATCH:
                        result.add(ErrorDescriptionFactory.forTree(ctx, mit.getArguments().get(index), NbBundle.getMessage(NPECheck.class, "ERR_TYPES_MISMATCH")));
                        break;
                }
            }
            index++;
        }
        
        return result;
    }
    
    @TriggerPatterns({
        @TriggerPattern("$variable != null"),
        @TriggerPattern("null != $variable"),
    })
    public static ErrorDescription notNullTest(HintContext ctx) {
        TreePath variable = ctx.getVariables().get("$variable");
        StateEnum r = computeExpressionsState(ctx).getOrDefault(variable.getLeaf(), DEFAULT_STATE).thisTypeState;
        
        if (r != null && r.isNotNull() && !ignore(ctx, false)) {
            String displayName = NbBundle.getMessage(NPECheck.class, r == StateEnum.NOT_NULL_BE_NPE ? "ERR_NotNullWouldBeNPE" : "ERR_NotNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        
        return null;
    }
    
    @TriggerPatterns({
        @TriggerPattern("$variable == null"),
        @TriggerPattern("null == $variable")
    })
    public static ErrorDescription nullTest(HintContext ctx) {
        TreePath variable = ctx.getVariables().get("$variable");
        StateEnum r = computeExpressionsState(ctx).getOrDefault(variable.getLeaf(), DEFAULT_STATE).thisTypeState;
        
        if (r != null && r.isNotNull() && !ignore(ctx, true)) {
            String displayName = NbBundle.getMessage(NPECheck.class, r == StateEnum.NOT_NULL_BE_NPE ? "ERR_NotNullWouldBeNPE" : "ERR_NotNull");
            
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
        }
        
        return null;
    }
    
    private static boolean ignore(HintContext ctx, boolean equalsToNull) {
        TreePath test = ctx.getPath().getParentPath();
        
        while (test != null && !StatementTree.class.isAssignableFrom(test.getLeaf().getKind().asInterface())) {
            test = test.getParentPath();
        }
        
        if (test == null) return false;
        
        if (test.getLeaf().getKind() == Kind.ASSERT && !equalsToNull) {
            return verifyConditions(ctx, ((AssertTree) test.getLeaf()).getCondition(), equalsToNull);
        } else if (test.getLeaf().getKind() == Kind.IF && equalsToNull) {
            StatementTree last;
            IfTree it = (IfTree) test.getLeaf();

            switch (it.getThenStatement().getKind()) {
                case BLOCK:
                    List<? extends StatementTree> statements = ((BlockTree) it.getThenStatement()).getStatements();
                    last = !statements.isEmpty() ? statements.get(statements.size() - 1) : null;
                    break;
                default:
                    last = it.getThenStatement();
                    break;
            }
            
            return last != null && last.getKind() == Kind.THROW && verifyConditions(ctx, ((IfTree) test.getLeaf()).getCondition(), equalsToNull);
        }
        
        return false;
    }
    
    private static boolean verifyConditions(HintContext ctx, ExpressionTree cond, boolean equalsToNull) {
        switch (cond.getKind()) {
            case PARENTHESIZED: return verifyConditions(ctx, ((ParenthesizedTree) cond).getExpression(), equalsToNull);
            case NOT_EQUAL_TO: return !equalsToNull && hasNull(ctx, (BinaryTree) cond);
            case EQUAL_TO: return equalsToNull && hasNull(ctx, (BinaryTree) cond);
            case CONDITIONAL_OR: case OR:
                return equalsToNull && verifyConditions(ctx, ((BinaryTree) cond).getLeftOperand(), equalsToNull) && verifyConditions(ctx, ((BinaryTree) cond).getRightOperand(), equalsToNull);
            case CONDITIONAL_AND: case AND:
                return !equalsToNull && verifyConditions(ctx, ((BinaryTree) cond).getLeftOperand(), equalsToNull) && verifyConditions(ctx, ((BinaryTree) cond).getRightOperand(), equalsToNull);
        }
        
        return false;
    }
    
    private static boolean hasNull(HintContext ctx, BinaryTree bt) {
        return    bt.getLeftOperand().getKind() == Kind.NULL_LITERAL
               || bt.getRightOperand().getKind() == Kind.NULL_LITERAL;
    }
    
    @TriggerPattern("return $expression;")
    public static ErrorDescription returnNull(HintContext ctx) {
        TreePath expression = ctx.getVariables().get("$expression");
        StateEnum returnState = computeExpressionsState(ctx).getOrDefault(expression.getLeaf(), DEFAULT_STATE).thisTypeState;

        if (returnState == null) return null;

        TreePath method = Utilities.findOwningExecutable(ctx, ctx.getPath(), true);
        if (method == null) return null;

        CompilationInfo info = ctx.getInfo();

        Element el = null;
        switch (method.getLeaf().getKind()) {
            case LAMBDA_EXPRESSION:
                TypeMirror functionalType = info.getTrees().getTypeMirror(method);
                if (!Utilities.isValidType(functionalType) || functionalType.getKind() != TypeKind.DECLARED) return null;
                el = info.getElementUtilities().getDescriptorElement((TypeElement) ((DeclaredType) functionalType).asElement());
                break;
            case METHOD:
                el = info.getTrees().getElement(method);
                break;
        }

        if (el == null || el.getKind() != ElementKind.METHOD) return null;

        State expected = getStateFromAnnotations(info, el);
        String key = null;

        switch (returnState) {
            case NULL:
                if (expected.isNotNull()) key = "ERR_ReturningNullFromNonNull";
                break;
            case POSSIBLE_NULL_REPORT:
                if (expected.isNotNull()) key = "ERR_ReturningPossibleNullFromNonNull";
                break;
        }

        if (key != null) {
            String displayName = NbBundle.getMessage(NPECheck.class, key);
            return ErrorDescriptionFactory.forName(ctx, expression, displayName);
        }
        
        return null;
    }
    
    @TriggerPattern("synchronized ($expression) { $statements$; }")
    @Messages({
        "ERR_SynchronizingOnNull=Synchronizing on null",
        "ERR_SynchronizingOnPossibleNull=Synchronizing on possible null",
    })
    public static ErrorDescription synchronizedNull(HintContext ctx) {
        TreePath expression = ctx.getVariables().get("$expression");
        StateEnum expressionState = computeExpressionsState(ctx).getOrDefault(expression.getLeaf(), DEFAULT_STATE).thisTypeState;

        if (expressionState == null) return null;

        String message = switch (expressionState) {
            case NULL -> Bundle.ERR_SynchronizingOnNull();
            case POSSIBLE_NULL_REPORT ->
                Bundle.ERR_SynchronizingOnPossibleNull();
            default -> null;
        };

        if (message != null) {
            return ErrorDescriptionFactory.forName(ctx, expression, message);
        }

        return null;
    }

    private static final Object KEY_EXPRESSION_STATE = new Object();
    private static final Object KEY_CONDITIONAL_PARAMETER = new Object();
    
    private static Map<Tree, State> computeExpressionsState(CompilationInfo info, HintContext ctx) {
        Map<Tree, State> result = (Map<Tree, State>) info.getCachedValue(KEY_EXPRESSION_STATE);
        
        if (result != null) {
            return result;
        }
        
        VisitorImpl v = new VisitorImpl(ctx, info, null);
        
        v.scan(info.getCompilationUnit(), null);

        result = v.expressionState;
        info.putCachedValue(KEY_EXPRESSION_STATE, result, CompilationInfo.CacheClearPolicy.ON_TASK_END);
        return result;
    }
    //Cancelling:
    private static Map<Tree, State> computeExpressionsState(HintContext ctx) {
        Map<Tree, State> result = (Map<Tree, State>) ctx.getInfo().getCachedValue(KEY_EXPRESSION_STATE);
        
        if (result != null) {
            return result;
        }
        
        VisitorImpl v = new VisitorImpl(ctx);
        
        v.scan(ctx.getInfo().getCompilationUnit(), null);

        result = v.expressionState;
        ctx.getInfo().putCachedValue(KEY_EXPRESSION_STATE, result, CompilationInfo.CacheClearPolicy.ON_TASK_END);
        return result;
    }

    private static StateMatchResult statesMatch(State declaredState, State actualState) {
        return statesMatch(declaredState, actualState, true);
    }

    private static StateMatchResult statesMatch(State declaredState, State actualState, boolean topLevel) {
        if (topLevel) {
            if (declaredState.isNotNull()) {
                switch (actualState.thisTypeState != null ? actualState.thisTypeState : StateEnum.POSSIBLE_NULL) {
                    case NULL: return StateMatchResult.TOP_LEVEL_NULL_TO_NONNULL;
                    case POSSIBLE_NULL_REPORT: return StateMatchResult.TOP_LEVEL_POSSIBLE_NULL_TO_NONNULL;
                }
            }
        } else {
            if (isSubstantial(declaredState) && isSubstantial(actualState)) {
                if (declaredState.isNotNull() ^ actualState.isNotNull()) {
                    return StateMatchResult.MISMATCH;
                }
            }
        }

        if (declaredState.typeParameters != null &&
            actualState.typeParameters != null &&
            declaredState.typeParameters.size() == actualState.typeParameters.size()) {
            for (int i = 0; i < declaredState.typeParameters.size(); i++) {
                if (statesMatch(declaredState.typeParameters.get(i), actualState.typeParameters.get(i), false) != StateMatchResult.MATCHES) {
                    return StateMatchResult.MISMATCH;
                }
            }
        }

        if (declaredState.componentTypeState != null &&
            actualState.componentTypeState != null) {
            if (statesMatch(declaredState.componentTypeState, actualState.componentTypeState, false) != StateMatchResult.MATCHES) {
                return StateMatchResult.MISMATCH;
            }
        }

        return StateMatchResult.MATCHES;
    }

    private static boolean isSubstantial(State s) {
        return s != null && s.thisTypeState != null &&
               s.thisTypeState != StateEnum.POSSIBLE_NULL &&
               s.thisTypeState != StateEnum.POSSIBLE_NULL_EXPLICIT_UNSPECIFIED;
    }

    private enum StateMatchResult {
        MATCHES,
        TOP_LEVEL_NULL_TO_NONNULL,
        TOP_LEVEL_POSSIBLE_NULL_TO_NONNULL,
        MISMATCH
    }

    private static final AnnotationMirrorGetter OVERRIDE_ANNOTATIONS = Lookup.getDefault().lookup(AnnotationMirrorGetter.class);
    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.BINDING_VARIABLE, ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.RESOURCE_VARIABLE);

    private static State getStateFromAnnotations(CompilationInfo info, Element e) {
        if (e == null) return new State(StateEnum.POSSIBLE_NULL);

        StateEnum typeDefault = getDefaultState(e, StateEnum.POSSIBLE_NULL);
        StateEnum declarationDefault = StateEnum.POSSIBLE_NULL;
        Iterable<? extends AnnotationMirror> mirrors = OVERRIDE_ANNOTATIONS != null ? OVERRIDE_ANNOTATIONS.getAnnotationMirrors(info, e) : null;

        if (mirrors == null) mirrors = e.getAnnotationMirrors();

        State result;

        if (e.getKind().isVariable()) {
            //XXX:
            //- should include with OVERRIDE_ANNOTATIONS?
            //- adjust default(!)
            result = getStateFromAnnotations(info, e.asType(), x -> null,
                    LOCAL_VARIABLES.contains(e.getKind()) ? StateEnum.POSSIBLE_NULL : typeDefault, typeDefault);
        } else if (e.getKind() == ElementKind.METHOD) {
            result = getStateFromAnnotations(info, ((ExecutableType) e.asType()).getReturnType(), typeDefault);
        } else {
            result = new State(StateEnum.POSSIBLE_NULL);
        }

        StateEnum fromDeclaration = getStateFromAnnotations(mirrors, declarationDefault);

        if (fromDeclaration != StateEnum.POSSIBLE_NULL) {
            //TODO: correct?
            result = result.setThisState(fromDeclaration);
        }

        return result;
    }

    private static State getStateFromAnnotations(CompilationInfo info, TypeMirror type, StateEnum fallbackState) {
        return getStateFromAnnotations(info, type, ta -> null, fallbackState);
    }

    private static State getStateFromAnnotations(CompilationInfo info, TypeMirror type, Function<TypeMirror, State> type2StateMapper, StateEnum fallbackState) {
        return getStateFromAnnotations(info, type, type2StateMapper, fallbackState, fallbackState);
    }

    private static State getStateFromAnnotations(CompilationInfo info, TypeMirror type, Function<TypeMirror, State> type2StateMapper, StateEnum topLevelFallbackState, StateEnum fallbackState) {
        State state = type2StateMapper.apply(type);

        if (state != null) {
            //TODO: should presumably merge with other aspects?
            return state;
        }

        StateEnum thisTypeState = getStateFromAnnotations(type.getAnnotationMirrors(), topLevelFallbackState);
        List<State> typeParameters = null;
        State arrayComponentState = null;

        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) type;

            typeParameters = dt.getTypeArguments()
                               .stream()
                               .map(ta -> getStateFromAnnotations(info, ta, type2StateMapper, fallbackState))
                               .toList();
        } else if (type.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) type;

            arrayComponentState = getStateFromAnnotations(info, at.getComponentType(), type2StateMapper, fallbackState);
        }

        return new State(thisTypeState, typeParameters, arrayComponentState);
    }

    private static StateEnum getStateFromAnnotations(Iterable<? extends AnnotationMirror> mirrors, StateEnum fallbackState) {
        for (AnnotationMirror am : mirrors) {
            String simpleName = ((TypeElement) am.getAnnotationType().asElement()).getSimpleName().toString();

            if ("Nullable".equals(simpleName) || "NullAllowed".equals(simpleName)) {
                return StateEnum.POSSIBLE_NULL_REPORT;
            }

            if ("CheckForNull".equals(simpleName)) {
                return StateEnum.POSSIBLE_NULL_REPORT;
            }

            if ("NotNull".equals(simpleName) || "NonNull".equals(simpleName) || "Nonnull".equals(simpleName)) {
                return StateEnum.NOT_NULL;
            }

            String fqnName = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().toString();

            if ("org.jspecify.annotations.NullnessUnspecified".equals(fqnName)) {
                return StateEnum.POSSIBLE_NULL_EXPLICIT_UNSPECIFIED;
            }
        }
        return fallbackState;
    }

    private static StateEnum getDefaultState(Element el, StateEnum fallbackState) {
        while (el != null) {
            for (AnnotationMirror am : el.getAnnotationMirrors()) {
                String fqnName = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().toString();

                if ("org.jspecify.annotations.NullMarked".equals(fqnName)) {
                    return StateEnum.NOT_NULL;
                }

                if ("org.jspecify.annotations.NullUnmarked".equals(fqnName)) {
                    return StateEnum.POSSIBLE_NULL_REPORT;
                }

                if ("org.jspecify.annotations.NullnessUnspecified".equals(fqnName)) {
                    return StateEnum.POSSIBLE_NULL_EXPLICIT_UNSPECIFIED;
                }
            }

            el = el.getEnclosingElement();
        }

        return fallbackState;
    }

    public interface AnnotationMirrorGetter {
        public Iterable<? extends AnnotationMirror> getAnnotationMirrors(CompilationInfo info, Element el);
    }
        
    private static final class VisitorImpl extends CancellableTreeScanner<State, Void> {
        
        private final HintContext ctx;
        private final CompilationInfo info;
        private final AtomicBoolean cancelFlag;
        
        /**
         * Tracks variables which are in scope.
         */
        private Map<VariableElement, State> variable2State = new HashMap<>();

        private Map<VariableElement, State> variable2StateWhenTrue = new HashMap<>();
        private Map<VariableElement, State> variable2StateWhenFalse = new HashMap<>();

        private final Map<Tree, Collection<Map<VariableElement, State>>> resumeBefore = new IdentityHashMap<>();
        private final Map<Tree, Collection<Map<VariableElement, State>>> resumeAfter = new IdentityHashMap<>();
        private       Map<TypeMirror, Map<VariableElement, State>> resumeOnExceptionHandler = new IdentityHashMap<>();
        private final Map<Tree, State> expressionState = new IdentityHashMap<>(); //the null state of the top-level type of the expression
        private final List<TreePath> pendingFinally = new LinkedList<>();
        private       List<State> pendingYields = new ArrayList<>();
        private final TypeElement throwableEl;
        private final TypeMirror runtimeExceptionType;
        private final TypeMirror errorType;
 
        /**
         * For a Tree, collects variables scoped into that tree. The Map is used
         * when the traversal exits the tree, to clean up variables that go out of scope from
         * variable2State, so state cloning takes less memory
         */
        private final Map<Tree, Collection<VariableElement>> scopedVariables = new IdentityHashMap<>();
                
        public VisitorImpl(HintContext ctx, CompilationInfo aInfo, AtomicBoolean cancel) {
            this.ctx = ctx;
            if (ctx != null) {
                this.info = ctx.getInfo();
                this.cancelFlag = null;
            } else {
                this.info = aInfo;
                this.cancelFlag = cancel != null ? cancel : new AtomicBoolean(false);
            }

            
            this.throwableEl = this.info.getElements().getTypeElement("java.lang.Throwable"); // NOI18N
            TypeElement tel =  this.info.getElements().getTypeElement("java.lang.RuntimeException"); // NOI18N
            if (tel != null) {
                runtimeExceptionType = tel.asType();
            } else {
                runtimeExceptionType = null;
            }
            tel =  this.info.getElements().getTypeElement("java.lang.Error"); // NOI18N
            if (tel != null) {
                errorType = tel.asType();
            } else {
                errorType = null;
            }
        }
        
        public VisitorImpl(HintContext ctx) {
            this(ctx, null, null);
        }

        @Override
        protected boolean isCanceled() {
            if (ctx != null) {
                return ctx.isCanceled();
            } else {
                return cancelFlag.get();
            }
                
        }

        private TreePath currentPath;

        public TreePath getCurrentPath() {
            return currentPath;
        }

        public State scan(TreePath path, Void p) {
            TreePath oldPath = currentPath;
            try {
                currentPath = path;
                return super.scan(path.getLeaf(), p);
            } finally {
                currentPath = oldPath;
            }
        }

        @Override
        public State scan(Tree tree, Void p) {
            resume(tree, resumeBefore);

            State r;

            if (tree != null) {
                TreePath oldPath = currentPath;
                try {
                    currentPath = new TreePath(currentPath, tree);
                    r = super.scan(tree, p);
                } finally {
                    currentPath = oldPath;
                }
            } else {
                r = null;
            }
            
            TypeMirror currentType = tree != null ? info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), tree)) : null;
            
            if ((tree != null && tree.getKind() == Kind.LAMBDA_EXPRESSION) || (currentType != null && currentType.getKind().isPrimitive())) {
                r = new State(StateEnum.NOT_NULL);
            }
            
            if (r != null) {
//                expressionState.put(tree, r);
                expressionState.put(tree, mergeIn(expressionState, tree, r));
            }
            
            resume(tree, resumeAfter);
            
            Collection<VariableElement> varsOutScope = scopedVariables.get(tree);
            if (varsOutScope != null) {
                variable2State.keySet().removeAll(varsOutScope);
            }
            return r;
        }

        private void resume(Tree tree, Map<Tree, Collection<Map<VariableElement, State>>> resume) {
            Collection<Map<VariableElement, State>> toResume = resume.remove(tree);

            if (toResume != null) {
                for (Map<VariableElement, State> s : toResume) {
                    mergeIntoVariable2State(s);
                }
            }
        }

        @Override
        public State visitAssignment(AssignmentTree node, Void p) {
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));
            State r = scan(node.getExpression(), p);
            
            mergeSplitVariable2State();

            scan(node.getVariable(), p);
            
            TypeMirror variableType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getVariable()));
            TypeMirror expressionType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));

            r = aliasToTargetType(expressionType, variableType, r);

            if (isVariableElement(e)) {
                variable2State.put((VariableElement) e, r);
            }
            
            return r;
        }

        @Override
        public State visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
            scan(node.getExpression(), p);

            mergeSplitVariable2State();

            scan(node.getVariable(), p);

            return null;
        }

        private void addScopedVariable(Tree t, VariableElement ve) {
            Collection<VariableElement> c = scopedVariables.get(t);
            if (c == null) {
                c = new ArrayList<>(3);
                scopedVariables.put(t, c);
            }
            c.add(ve);
        }

        @Override
        public State visitVariable(VariableTree node, Void p) {
            Element e = info.getTrees().getElement(getCurrentPath());
            State r = scan(node.getInitializer(), p);
            
            mergeSplitVariable2State();

            if (node.getInitializer() != null) {
                TypeMirror targetType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getType()));
                TypeMirror initType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getInitializer()));

                r = aliasToTargetType(initType, targetType, r);
            } else {
                r = getStateFromAnnotations(info, e);
            }

            if (e != null) {
                if (e.getKind() == ElementKind.EXCEPTION_PARAMETER) {
                    r = new State(StateEnum.NOT_NULL);
                }
                variable2State.put((VariableElement) e, r);
                TreePath pp = getCurrentPath().getParentPath();
                if (pp != null) {
                    addScopedVariable(pp.getLeaf(), (VariableElement)e);
                }
            }

            return r;
        }

        @Override
        public State visitMemberSelect(MemberSelectTree node, Void p) {
            State derefedState = scan(node.getExpression(), p);
            TreePath derefed = new TreePath(getCurrentPath(), node.getExpression());

            handleDereference(derefedState, derefed);

            Element site = info.getTrees().getElement(derefed);

            // special case: if the memberSelect selects enum field = constant, it is never null.
            if (site != null && site.getKind() == ElementKind.ENUM) {
                Element enumConst = info.getTrees().getElement(getCurrentPath());
                if (enumConst != null && enumConst.getKind() == ElementKind.ENUM_CONSTANT) {
                    return new State(StateEnum.NOT_NULL);
                }
            }
            
            return getStateFromAnnotations(info, info.getTrees().getElement(getCurrentPath()));
        }

        private void handleDereference(State derefState, TreePath derefed) {
            boolean wasNPE = false;

            if (derefState != null && !derefState.isNotNull()) {
                wasNPE = true;
            }

            Element site = info.getTrees().getElement(derefed);

            if (isVariableElement(site) && wasNPE) {
                if (variable2State != null) {
                    if (!isDefinitellyNotNull(variable2State, (VariableElement) site)) {
                        setThisState(variable2State, (VariableElement) site, StateEnum.NOT_NULL_BE_NPE);
                    }
                } else {
                    if (!isDefinitellyNotNull(variable2StateWhenTrue, (VariableElement) site)) {
                        setThisState(variable2StateWhenTrue, (VariableElement) site, StateEnum.NOT_NULL_BE_NPE);
                    }
                    if (!isDefinitellyNotNull(variable2StateWhenFalse, (VariableElement) site)) {
                        setThisState(variable2StateWhenFalse, (VariableElement) site, StateEnum.NOT_NULL_BE_NPE);
                    }
                }
            }
        }

        @Override
        public State visitLiteral(LiteralTree node, Void p) {
            if (node.getValue() == null) {
                return new State(StateEnum.NULL);
            } else {
                return new State(StateEnum.NOT_NULL);
            }
        }

        @Override
        public State visitIf(IfTree node, Void p) {
            scan(node.getCondition(), p);

            Map<VariableElement, State> elseVariable2State = selectVariableStates(true);

            scan(node.getThenStatement(), null);
            
            Map<VariableElement, State> variableStatesAfterThen = new HashMap<>(variable2State);
            
            variable2State = elseVariable2State;
            
            scan(node.getElseStatement(), null);
            
            boolean thenExitsFromAllBranches = Utilities.exitsFromAllBranchers(info, new TreePath(getCurrentPath(), node.getThenStatement()));
            boolean elseExitsFromAllBranches = node.getElseStatement() != null && Utilities.exitsFromAllBranchers(info, new TreePath(getCurrentPath(), node.getElseStatement()));
            
            if (thenExitsFromAllBranches && !elseExitsFromAllBranches) {
                //already set
            } else if (!thenExitsFromAllBranches && elseExitsFromAllBranches) {
                variable2State = variableStatesAfterThen;
            } else {
                mergeIntoVariable2State(variableStatesAfterThen);
            }
            
            return null;
        }

        @Override
        public State visitBinary(BinaryTree node, Void p) {
            Kind kind = node.getKind();
            
            State left = null;
            State right = null;
            
            switch (kind) {
                case CONDITIONAL_AND:
                    scan(node.getLeftOperand(), p);

                    Map<VariableElement, State> afterLeftWhenFalse = selectVariableStates(true);

                    scan(node.getRightOperand(), p);

                    ensureStateSplit();

                    mergeInto(variable2StateWhenFalse, afterLeftWhenFalse);

                    break;

                case AND: case OR: case XOR:
                    scan(node.getLeftOperand(), p);
                    scan(node.getRightOperand(), p);
                    break;

                case CONDITIONAL_OR: {
                    scan(node.getLeftOperand(), p);

                    Map<VariableElement, State> afterLeftWhenTrue = selectVariableStates(false);

                    scan(node.getRightOperand(), p);

                    ensureStateSplit();

                    mergeInto(variable2StateWhenTrue, afterLeftWhenTrue);

                    break;
                }
                
                default: {
                    left = scan(node.getLeftOperand(), p);
                    right = scan(node.getRightOperand(), p);
                }
            }
            
            if (kind == Kind.EQUAL_TO) {
                if (node.getRightOperand().getKind() == Kind.NULL_LITERAL) {
                    handleBinaryComparisonToNull(node.getLeftOperand(), StateEnum.NULL);
                } else if (node.getLeftOperand().getKind() == Kind.NULL_LITERAL) {
                    handleBinaryComparisonToNull(node.getRightOperand(), StateEnum.NULL);
                }
            } else if (kind == Kind.NOT_EQUAL_TO) {
                if (node.getRightOperand().getKind() == Kind.NULL_LITERAL) {
                    handleBinaryComparisonToNull(node.getLeftOperand(), StateEnum.NOT_NULL);
                } else if (node.getLeftOperand().getKind() == Kind.NULL_LITERAL) {
                    handleBinaryComparisonToNull(node.getRightOperand(), StateEnum.NOT_NULL);
                }
            }
            
            return null;
        }

        private void handleBinaryComparisonToNull(ExpressionTree variableOperand, StateEnum trueState) {
            Set<VariableElement> variables = new HashSet<>();
            List<TreePath> expressionTodo = new ArrayList<>();

            expressionTodo.add(new TreePath(getCurrentPath(), variableOperand));

            while (!expressionTodo.isEmpty()) {
                TreePath tp = expressionTodo.remove(expressionTodo.size() - 1);
                Element e = info.getTrees().getElement(tp);

                if (isVariableElement(e)) {
                    variables.add((VariableElement) e);
                }
                switch (tp.getLeaf().getKind()) {
                    case PARENTHESIZED -> expressionTodo.add(new TreePath(tp, ((ParenthesizedTree) tp.getLeaf()).getExpression()));
                    case ASSIGNMENT -> {
                        AssignmentTree at = (AssignmentTree) tp.getLeaf();

                        expressionTodo.add(new TreePath(tp, at.getVariable()));
                        expressionTodo.add(new TreePath(tp, at.getExpression()));
                    }
                }
            }

            for (VariableElement var : variables) {
                if (!isDefinitellyNotNull(var)) {
                    ensureStateSplit();

                    setThisState(variable2StateWhenTrue, var, trueState);
                    setThisState(variable2StateWhenFalse, var, trueState.reverse());
                }
            }
        }

        @Override
        public State visitInstanceOf(InstanceOfTree node, Void p) {
            super.visitInstanceOf(node, p);
            
            Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getExpression()));

            if (isVariableElement(e)) {
                boolean setState = false;
                State currentState = variable2State.get((VariableElement) e);
                if (currentState == null) {
                    setState = !getStateFromAnnotations(info, e).isNotNull();
                } else {
                    setState = !variable2State.get((VariableElement) e).isNotNull();
                }
                if (setState) {
                    ensureStateSplit();

                    setThisState(variable2StateWhenTrue, (VariableElement) e, StateEnum.NOT_NULL);
                }
            }
            
            return null;
        }

        @Override
        public State visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            scan(node.getCondition(), p);
            
            Map<VariableElement, State> elseVariable2State = selectVariableStates(true);

            State thenSection = scan(node.getTrueExpression(), p);

            mergeSplitVariable2State();

            Map<VariableElement, State> variableStatesAfterThen = variable2State;
            
            variable2State = elseVariable2State;
            
            State elseSection = scan(node.getFalseExpression(), p);

            State result = State.strictMerge(thenSection, elseSection);
            
            mergeSplitVariable2State();
            mergeIntoVariable2State(variableStatesAfterThen);
                
            return result;
        }

        @Override
        public State visitNewClass(NewClassTree node, Void p) {
            scan(node.getEnclosingExpression(), p);
            State typeState = scan(node.getIdentifier(), p);
            scan(node.getTypeArguments(), p);
            
            for (Tree param : node.getArguments()) {
                scan(param, p);
                mergeSplitVariable2State();
            }
            
            scan(node.getClassBody(), p);
            
            Element invoked = info.getTrees().getElement(getCurrentPath());

            if (invoked != null && invoked.getKind() == ElementKind.CONSTRUCTOR) {
                recordResumeOnExceptionHandler((ExecutableElement) invoked);
            }

            if (typeState != null) {
                return typeState.setThisState(StateEnum.NOT_NULL);
            } else {
                return new State(StateEnum.NOT_NULL);
            }
        }

        @Override
        public State visitMethodInvocation(MethodInvocationTree node, Void p) {
            scan(node.getTypeArguments(), p);

            State receiverState;
            TypeMirror receiverType;
            ExpressionTree methodSelect = node.getMethodSelect();

            switch (methodSelect.getKind()) {
                case IDENTIFIER -> {
                    receiverState = new State(StateEnum.POSSIBLE_NULL); //TODO - should be "this"
                    receiverType = null; //TODO - should be "this"
                }
                case MEMBER_SELECT -> {
                    TreePath prevPath = this.currentPath;
                    try {
                        this.currentPath = new TreePath(getCurrentPath(), methodSelect);
                        ExpressionTree selected = ((MemberSelectTree) methodSelect).getExpression();
                        TreePath selectedPath = new TreePath(currentPath, selected);

                        receiverState = scan(selected, p);
                        handleDereference(receiverState, selectedPath);
                        receiverType = info.getTrees().getTypeMirror(selectedPath);
                    } finally {
                        this.currentPath = prevPath;
                    }
                }
                default -> {
                    //XXX: should not happen?
                    receiverState = new State(StateEnum.POSSIBLE_NULL);
                    receiverType = null;
                    scan(methodSelect, p);
                }
            }
            
            for (Tree param : node.getArguments()) {
                scan(param, p);
                mergeSplitVariable2State();
            }
            
            Element e = info.getTrees().getElement(getCurrentPath());
            
            if (e == null || e.getKind() != ElementKind.METHOD) {
                return new State(StateEnum.POSSIBLE_NULL);
            } else {
                recordResumeOnExceptionHandler((ExecutableElement) e);
                visitAssertMethods(node, e);
                State s = visitPrimitiveWrapperMethods(node, e);
                if (s != null) {
                    return s;
                }
                if (receiverType != null) { //ideally should be always true
                    if (receiverType.getKind() == TypeKind.DECLARED) {
                        DeclaredType receiver = (DeclaredType) receiverType;
                        Map<TypeMirror, State> marker2State = new HashMap<>();
                        List<? extends TypeMirror> ta = receiver.getTypeArguments();

                        if (receiverState.typeParameters != null && ta.size() == receiverState.typeParameters.size()) {
                            for (int i = 0; i < ta.size(); i++) {
                                marker2State.put(ta.get(i), receiverState.typeParameters.get(i));
                            }
                            TypeMirror instantiatedReturnType = ((ExecutableType) info.getTypes().asMemberOf(receiver, e)).getReturnType();
                            State instantiatedState = getStateFromAnnotations(info, instantiatedReturnType, marker2State::get, StateEnum.POSSIBLE_NULL);
                            TypeMirror declaredReturnType = ((ExecutableElement) e).getReturnType();
                            State declaredState = getStateFromAnnotations(info, declaredReturnType, null);

                            return State.weakMerge(instantiatedState, declaredState);
                        }
                    }
                }
            }
            
            return getStateFromAnnotations(info, e);
        }
        
        private State visitPrimitiveWrapperMethods(MethodInvocationTree node, Element e) {
            if (!Utilities.isPrimitiveWrapperType(e.getEnclosingElement().asType())) {
                return null;
            }
            switch (e.getSimpleName().toString()) {
                case "toString": // NOI18N
                case "toUnsignedString":// NOI18N
                    
                case "toHexString":case "toOctalString": case "toBinaryString": // NOI18N
                case "valueOf":// NOI18N
                case "decode":// NOI18N
                    return new State(StateEnum.NOT_NULL);
                    
                case "getLong": // NOI18N
                case "getShort": // NOI18N
                case "getInteger": // NOI18N
                case "getBoolean": // NOI18N
                case "getFloat": // NOI18N
                case "getDouble": // NOI18N
                case "getCharacter": { // NOI18N
                    // non-null if 2nd argument is primitive
                    if (node.getArguments().size() != 2) {
                        return null;
                    }
                    TreePath parPath = new TreePath(getCurrentPath(), node.getArguments().get(1));
                    TypeMirror m = ctx.getInfo().getTrees().getTypeMirror(parPath);
                    if (!Utilities.isValidType(m)) {
                        return null;
                    }
                    if (m.getKind().isPrimitive()) {
                        return new State(StateEnum.NOT_NULL);
                    } else if (NPECheck.isSafeToDereference(ctx.getInfo(), parPath)) {
                        return new State(StateEnum.NOT_NULL);
                    } else {
                        return null;
                    }
                }
                default:
                    return null;
            }
        }
        
        private void visitAssertMethods(MethodInvocationTree node, Element e) {
            if (!node.getArguments().isEmpty()) {
                String ownerFQN = ((TypeElement) e.getEnclosingElement()).getQualifiedName().toString();
                Tree argument = null;
                StateEnum targetState = null;

                switch (e.getSimpleName().toString()) {
                    case "assertNotNull":
                    case "requireNonNull":
                    case "requireNonNullElse":
                    case "requireNonNullElseGet": targetState = StateEnum.NOT_NULL; break;
                    case "assertNull": targetState = StateEnum.NULL; break;
                }

                switch (ownerFQN) {
                    case "org.testng.Assert":
                    case "java.util.Objects": argument = node.getArguments().get(0); break;
                    case "junit.framework.Assert":
                    case "org.junit.Assert": 
                    case "org.junit.jupiter.api.Assertions": argument = node.getArguments().get(node.getArguments().size() - 1); break;
                }

                Element param = argument != null && targetState != null ? info.getTrees().getElement(new TreePath(getCurrentPath(), argument)) : null;

                if (param != null && isVariableElement(param)) {
                    setThisState(variable2State, (VariableElement) param, targetState);
                }
            }
        }

        @Override
        public State visitIdentifier(IdentifierTree node, Void p) {
            super.visitIdentifier(node, p);
            
            Element e = info.getTrees().getElement(getCurrentPath());

            if (e == null || !isVariableElement(e)) {
                return new State(StateEnum.POSSIBLE_NULL);
            }
            if (e.getKind() == ElementKind.ENUM_CONSTANT) {
                // enum constants are never null
                return new State(StateEnum.NOT_NULL);
            }

            if (variable2State != null) {
                State s = variable2State.get((VariableElement) e);
                if (s != null) {
                    return s;
                }

                return getStateFromAnnotations(info, e);
            } else {
                State whenTrue = variable2StateWhenTrue.get((VariableElement) e);
                State whenFalse = variable2StateWhenFalse.get((VariableElement) e);

                if (whenTrue == null) {
                    whenTrue = getStateFromAnnotations(info, e);
                }

                if (whenFalse == null) {
                    whenFalse = getStateFromAnnotations(info, e);
                }

                return State.strictMerge(whenTrue, whenFalse);
            }
            
        }

        @Override
        public State visitParameterizedType(ParameterizedTypeTree node, Void p) {
            State baseState = scan(node.getType(), p);
            List<State> taStates = node.getTypeArguments().stream().map(ta -> scan(ta, p)).toList();

            return new State(baseState.thisTypeState, taStates);
        }

        @Override
        public State visitAnnotatedType(AnnotatedTypeTree node, Void p) {
            super.visitAnnotatedType(node, p);

            //TODO: merge with underlying state?
            TypeMirror type = info.getTrees().getTypeMirror(getCurrentPath());

            return getStateFromAnnotations(info, type, StateEnum.POSSIBLE_NULL);
        }

        @Override
        public State visitWhileLoop(WhileLoopTree node, Void p) {
            return handleGeneralizedFor(null, node.getCondition(), null, node.getStatement(), p);
        }

        @Override
        public State visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            if (!inCycle) {
                inCycle = true;

                HashMap<VariableElement, State> startState = new HashMap<>(variable2State);

                scan(node.getStatement(), p);

                scan(node.getCondition(), p);

                selectVariableStates(true);

                mergeIntoVariable2State(startState);

                inCycle = false;
            }

            scan(node.getStatement(), p);

            scan(node.getCondition(), p);

            selectVariableStates(false);

            return null;
        }

        @Override
        public State visitUnary(UnaryTree node, Void p) {
            State res = scan(node.getExpression(), p);
            
            if (variable2StateWhenFalse != null) {
                Map<VariableElement, State> temp = variable2StateWhenFalse;

                variable2StateWhenFalse = variable2StateWhenTrue;
                variable2StateWhenTrue = temp;
            }

            return res;
        }

        @Override
        public State visitMethod(MethodTree node, Void p) {
            Map<TypeMirror, Map<VariableElement, State>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;

            resumeOnExceptionHandler = new IdentityHashMap<>();
            
            try {
                variable2State = new HashMap<>();

                Element current = info.getTrees().getElement(getCurrentPath());

                if (current != null && (current.getKind() == ElementKind.METHOD || current.getKind() == ElementKind.CONSTRUCTOR)) {
                    for (VariableElement var : ((ExecutableElement) current).getParameters()) {
                        variable2State.put(var, getStateFromAnnotations(info, var));
                    }
                }

                while (current != null) {
                    for (VariableElement var : ElementFilter.fieldsIn(current.getEnclosedElements())) {
                        variable2State.put(var, getStateFromAnnotations(info, var));
                    }
                    current = current.getEnclosingElement();
                }

                return super.visitMethod(node, p);
            } finally {
                resumeOnExceptionHandler = oldResumeOnExceptionHandler;
            }
        }

        @Override
        public State visitForLoop(ForLoopTree node, Void p) {
            return handleGeneralizedFor(node.getInitializer(), node.getCondition(), node.getUpdate(), node.getStatement(), p);
        }

        @Override
        public State visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
            return handleGeneralizedFor(Arrays.asList(node.getVariable(), node.getExpression()), null, null, node.getStatement(), p);
        }

        /**
         * Helps to disable speculative evaluation of nested cycle bodies, to prevent combinatorial explosion
         * with a lot of nested cycles.
         */
        private boolean inCycle = false;
        
        private State handleGeneralizedFor(Iterable<? extends Tree> initializer, Tree condition, Iterable<? extends Tree> update, Tree statement, Void p) {
            scan(initializer, p);

            if (!inCycle) {
                inCycle = true;

                scan(condition, p);

                Map<VariableElement, State> negConditionVariable2State = selectVariableStates(true);

                scan(statement, p);
                scan(update, p);

                mergeIntoVariable2State(negConditionVariable2State);

                inCycle = false;
            }

            scan(condition, p);

            Map<VariableElement, State> negConditionVariable2State = selectVariableStates(true);

            scan(statement, p);
            scan(update, p);
            
            mergeIntoVariable2State(negConditionVariable2State);
            
            return null;
        }

        @Override
        public State visitAssert(AssertTree node, Void p) {
            scan(node.getCondition(), p);
            selectVariableStates(true);
            scan(node.getDetail(), p);
            return null;
        }

        @Override
        public State visitArrayAccess(ArrayAccessTree node, Void p) {
            State exprState = scan(node.getExpression(), p);
            scan(node.getIndex(), p);

            return exprState != null && exprState.componentTypeState != null ? exprState.componentTypeState
                                                                     : new State(StateEnum.POSSIBLE_NULL);
        }

        @Override
        public State visitSwitch(SwitchTree node, Void p) {
            handleGeneralizedSwitch(node, node.getExpression(), node.getCases());
            return null;
        }

        @Override
        public State visitSwitchExpression(SwitchExpressionTree node, Void p) {
            List<State> oldPendingYields = pendingYields;
            try {
                pendingYields = new ArrayList<>();
                handleGeneralizedSwitch(node, node.getExpression(), node.getCases());
                if (pendingYields.isEmpty()) {
                    //should not happen (for valid source)
                    return new State(StateEnum.POSSIBLE_NULL);
                }
                State result = pendingYields.get(0);
                for (State s : pendingYields.subList(1, pendingYields.size())) {
                    result = State.strictMerge(result, s);
                }
                return result;
            } finally {
                pendingYields = oldPendingYields;
            }
        }

        private void handleGeneralizedSwitch(Tree switchTree, ExpressionTree expression, List<? extends CaseTree> cases) {
            scan(expression, null);

            Element selectorElement = info.getTrees().getElement(new TreePath(getCurrentPath(), expression));
            VariableElement selectorVariable =
                isVariableElement(selectorElement) && !isDefinitellyNotNull((VariableElement) selectorElement) ? (VariableElement) selectorElement
                                                                                                           : null;

            Map<VariableElement, State> origVariable2State = new HashMap<>(variable2State);

            boolean exhaustive = false;

            for (CaseTree ct : cases) {
                mergeIntoVariable2State(origVariable2State);

                boolean hasNull = false;

                for (CaseLabelTree clt : ct.getLabels()) {
                    switch (clt.getKind()) {
                        case DEFAULT_CASE_LABEL -> exhaustive = true;
                        case CONSTANT_CASE_LABEL -> {
                            if (((ConstantCaseLabelTree) clt).getConstantExpression().getKind() == Kind.NULL_LITERAL) {
                                hasNull = true;
                            }
                        }
                    }
                }

                if (selectorVariable != null) {
                    setThisState(variable2State, selectorVariable, hasNull ? StateEnum.NULL : StateEnum.NOT_NULL);
                }

                State caseResult = scan(ct, null);

                if (ct.getCaseKind() == CaseKind.RULE) {
                    if (ct.getBody() != null && ExpressionTree.class.isAssignableFrom(ct.getBody().getKind().asInterface())) {
                        pendingYields.add(caseResult);
                    }
                    breakTo(switchTree);
                }
            }

            if (!exhaustive) {
                mergeIntoVariable2State(origVariable2State);
            }
        }

        @Override
        public State visitBreak(BreakTree node, Void p) {
            super.visitBreak(node, p);

            Tree target = info.getTreeUtilities().getBreakContinueTargetTree(getCurrentPath());
            
            breakTo(target);

            return null;
        }

        @Override
        public State visitYield(YieldTree node, Void p) {
            pendingYields.add(scan(node.getValue(), p));

            Tree target = info.getTreeUtilities().getBreakContinueTargetTree(getCurrentPath());
            
            breakTo(target);

            return null;
        }

        private void breakTo(Tree target) {
            resumeAfter(target, variable2State);

            variable2State = new HashMap<>(); //XXX: fields?
        }

        @Override
        public State visitTry(TryTree node, Void p) {
            Map<TypeMirror, Map<VariableElement, State>> oldResumeOnExceptionHandler = resumeOnExceptionHandler;

            resumeOnExceptionHandler = new IdentityHashMap<>();
            
            try {
            if (node.getFinallyBlock() != null) {
                pendingFinally.add(0, new TreePath(getCurrentPath(), node.getFinallyBlock()));
            }
            
            scan(node.getResources(), null);

            Map<VariableElement, State> oldVariable2State = variable2State;

            variable2State = new HashMap<>(oldVariable2State);

            // resumeOnEx will save states from potential exceptions thrown by try siblings
            // or outer blocks. 
            // resumeOnEx will be later reused for recorded handlers from the try block.
            Map<TypeMirror, Map<VariableElement, State>> resumeOnEx = null;
            List<TypeMirror> caughtTypes = null;
            if (node.getCatches() != null && !node.getCatches().isEmpty()) {
                caughtTypes = new ArrayList<>(node.getCatches().size());
                resumeOnEx = new IdentityHashMap<>(caughtTypes.size());
                for (CatchTree ct : node.getCatches()) {
                    for (TypeMirror exT : Utilities.getUnionExceptions(info, getCurrentPath(), ct)) {
                        Map<VariableElement, State> data = resumeOnExceptionHandler.get(exT);
                        if (data != null) {
                            resumeOnEx.put(exT, data);
                        }
                        resumeOnExceptionHandler.put(exT, new HashMap<>());
                        caughtTypes.add(exT);
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
            HashMap<VariableElement, State> afterBlockVariable2State = new HashMap<>(variable2State);

            // catch handlers and finally block will report exception to outer catches, restore
            // masked exception types
            if (caughtTypes != null) {
                assert resumeOnEx != null;
                for (TypeMirror exT : caughtTypes) {
                    Map<VariableElement, State> oldData = resumeOnEx.remove(exT);
                    Map<VariableElement, State> data = resumeOnExceptionHandler.remove(exT);
                    if (data != null) {
                        resumeOnEx.put(exT, data);
                    }
                    if (oldData != null) {
                        resumeOnExceptionHandler.put(exT, oldData);
                    }
                }

                assert node.getCatches() != null;
                for (CatchTree ct : node.getCatches()) {
                    Map<VariableElement, State> variable2StateBeforeCatch = variable2State;

                    variable2State = new HashMap<>(oldVariable2State);

                    for (TypeMirror cc : Utilities.getUnionExceptions(info, getCurrentPath(), ct)) {
                        Map<VariableElement, State> data = resumeOnEx.get(cc);
                        if (data != null) {
                            mergeIntoVariable2State(data);
                        }
                    }

                    scan(ct, null);

                    if (Utilities.exitsFromAllBranchers(info, new TreePath(getCurrentPath(), ct))) {
                        variable2State = variable2StateBeforeCatch;
                    } else {
                        mergeIntoVariable2State(variable2StateBeforeCatch);
                    }
                }
            }

            if (node.getFinallyBlock() != null) {
                pendingFinally.remove(0);
                mergeIntoVariable2State(oldVariable2State);
                mergeIntoVariable2State(afterBlockVariable2State);

                scan(node.getFinallyBlock(), null);
            }
            } finally {
                Map<TypeMirror, Map<VariableElement, State>> remainingException = resumeOnExceptionHandler;
                resumeOnExceptionHandler = oldResumeOnExceptionHandler;
                for (Entry<TypeMirror, Map<VariableElement, State>> e : remainingException.entrySet()) {
                    recordResumeOnExceptionHandler(e.getKey(), e.getValue());
                }
            }
            
            return null;
        }
        
        private void recordResumeOnExceptionHandler(ExecutableElement invoked) {
            for (TypeMirror tt : invoked.getThrownTypes()) {
                recordResumeOnExceptionHandler(tt);
            }

            recordResumeOnExceptionHandler("java.lang.RuntimeException");
            recordResumeOnExceptionHandler("java.lang.Error");
        }

        private void recordResumeOnExceptionHandler(String exceptionTypeFQN) {
            TypeElement exc = info.getElements().getTypeElement(exceptionTypeFQN);

            if (exc == null) return;

            recordResumeOnExceptionHandler(exc.asType());
        }

        private void recordResumeOnExceptionHandler(TypeMirror thrown) {
            recordResumeOnExceptionHandler(thrown, variable2State);
        }
        
        /**
         * Records continuation to the exception handler if a throwable is raised.
         * Optimization: the resumeOnExceptionHandler must contain an entry for the
         * throwable and/or its superclass. If not, then no enclosing catch handler
         * is interested in the Throwable and no state snapshot is necessary.
         * 
         * @param thrown thrown exception type
         */
        private void recordResumeOnExceptionHandler(TypeMirror thrown, Map<VariableElement, State> variable2State) {
            
            TypeMirror curT = thrown;
            
            do {
                if (curT == null || curT.getKind() != TypeKind.DECLARED) return;
                DeclaredType dtt = (DeclaredType)curT;
                // hack; getSuperclass may provide different type instance for the same element.
                thrown = dtt.asElement().asType();
                Map<VariableElement, State> r = resumeOnExceptionHandler.get(thrown);
                if (r != null) {
                    mergeInto(r, variable2State);
                    break;
                }
                TypeElement tel = (TypeElement)dtt.asElement();
                if (tel == throwableEl) {
                    break;
                }
                curT = tel.getSuperclass();
            } while (curT != null);
        }
        
        private void resumeAfter(Tree target, Map<VariableElement, State> state) {
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

        private static void recordResume(Map<Tree, Collection<Map<VariableElement, State>>> resume, Tree target, Map<VariableElement, State> state) {
            Collection<Map<VariableElement, State>> r = resume.get(target);

            if (r == null) {
                r = new ArrayList<>();
                resume.put(target, r);
            }

            r.add(new HashMap<>(state));
        }
        
        private void mergeIntoVariable2State(Map<VariableElement, State> other) {
            mergeInto(variable2State, other);
        }
        
        private void mergeInto(Map<VariableElement, State> target, Map<VariableElement, State> other) {
            for (Entry<VariableElement, State> e : other.entrySet()) {
                State t = e.getValue();

                target.put(e.getKey(), mergeIn(target, e.getKey(), t));
            }
        }
        
        private <K> State mergeIn(Map<K, State> m, K k, State nue) {
            if (m.containsKey(k)) {
                return State.strictMerge(nue, m.get(k));
            } else {
                return nue;
            }
        }
        
        private void setThisState(Map<VariableElement, State> target, VariableElement forElement, StateEnum newThisState) {
            State existing = target.get(forElement);
            if (existing != null) {
                target.put(forElement, existing.setThisState(newThisState));
            } else {
                target.put(forElement, new State(newThisState));
            }
        }

        private void mergeSplitVariable2State() {
            if (variable2State != null) {
                return ;
            }

            variable2State = new HashMap<>();
            for (Entry<VariableElement, State> e : variable2StateWhenTrue.entrySet()) {
                State trueState = e.getValue();
                State falseState = variable2StateWhenFalse.get(e.getKey());

                variable2State.put(e.getKey(), State.strictMerge(trueState, falseState));
            }

            variable2StateWhenTrue = null;
            variable2StateWhenFalse = null;
        }

        private void ensureStateSplit() {
            if (variable2State == null) {
                return ;
            }
            variable2StateWhenTrue = new HashMap<>(variable2State);
            variable2StateWhenFalse = new HashMap<>(variable2State);
            variable2State = null;
        }

        /**
         * Fill in {@code variable2State} from {@code variable2StateWhenTrue} (iff {@code whenTrue == true}),
         * or {@code variable2StateWhenFalse}  (iff {@code whenTrue == false}),
         * and return the other map.
         */
        private Map<VariableElement, State> selectVariableStates(boolean whenTrue) {
            ensureStateSplit();

            variable2State = whenTrue ? variable2StateWhenTrue : variable2StateWhenFalse;

            Map<VariableElement, State> result = whenTrue ? variable2StateWhenFalse : variable2StateWhenTrue;

            variable2StateWhenTrue = null;
            variable2StateWhenFalse = null;

            return result;
        }

        private boolean isDefinitellyNotNull(VariableElement el) {
            return variable2State != null ? isDefinitellyNotNull(variable2State, el)
                                          : isDefinitellyNotNull(variable2StateWhenTrue, el) && isDefinitellyNotNull(variable2StateWhenFalse, el);
        }

        private boolean isDefinitellyNotNull(Map<VariableElement, State> in, VariableElement el) {
            State s = in.get(el);

            return s != null && s.isNotNull();
        }
        
        private boolean isVariableElement(Element ve) {
            return NPECheck.isVariableElement(ctx, ve);
        }

        private State aliasToTargetType(TypeMirror sourceType, TypeMirror targetType, State state) {
            if (sourceType == null || sourceType.getKind() != TypeKind.DECLARED ||
                targetType == null || targetType.getKind() != TypeKind.DECLARED) {
                return state;
            }
            DeclaredType source = (DeclaredType) sourceType;
            DeclaredType target = (DeclaredType) targetType;

            if (state.typeParameters == null || source.getTypeArguments().size() != state.typeParameters.size()) {
                //anything to reconcile the situation?
                return state;
            }

            Map<TypeMirror, State> marker2State = new HashMap<>();
            List<? extends TypeMirror> ta = source.getTypeArguments();

            for (int i = 0; i < ta.size(); i++) {
                marker2State.put(ta.get(i), state.typeParameters.get(i));
            }

            //TODO: is this a reasonable reliable way to do the remapping?
            TypeMirror remappedTargetType = info.getTypeUtilities().asSuper(source, (TypeElement) target.asElement());

            if (remappedTargetType == null || remappedTargetType.getKind() != TypeKind.DECLARED) {
                return state;
            }

            DeclaredType remappedTarget = (DeclaredType) remappedTargetType;

            if (remappedTarget.getTypeArguments().size() != target.getTypeArguments().size()) {
                return state;
            }

            List<State> typeParams = new ArrayList<>();

            for (int i = 0; i < remappedTarget.getTypeArguments().size(); i++) {
                State nested = marker2State.get(remappedTarget.getTypeArguments().get(i));

                if (nested == null) {
                    nested = new State(StateEnum.POSSIBLE_NULL);
                } else {
                    nested = aliasToTargetType(remappedTarget.getTypeArguments().get(i), target.getTypeArguments().get(i), nested);
                }

                typeParams.add(nested);
            }

            return new State(state.thisTypeState, typeParams);
        }
    }

    static class State {

        public static State strictMerge(State s1, State s2) {
            return new State(StateEnum.strictCollect(s1 != null ? s1.thisTypeState : StateEnum.POSSIBLE_NULL,
                                                     s2 != null ? s2.thisTypeState : StateEnum.POSSIBLE_NULL),
                             mergeTypeParams(s1 != null ? s1.typeParameters : null,
                                             s2 != null ? s2.typeParameters : null,
                                             true),
                             (s1 != null && s1.componentTypeState != null) ||
                             (s2 != null && s2.componentTypeState != null) ?
                             strictMerge(s1 != null ? s1.componentTypeState : null,
                                         s2 != null ? s2.componentTypeState : null) : null);
        }

        public static State weakMerge(State s1, State s2) {
            return new State(StateEnum.weakCollect(s1 != null ? s1.thisTypeState : null,
                                                   s2 != null ? s2.thisTypeState : null),
                             mergeTypeParams(s1 != null ? s1.typeParameters : null,
                                             s2 != null ? s2.typeParameters : null,
                                             false),
                             (s1 != null && s1.componentTypeState != null) ||
                             (s2 != null && s2.componentTypeState != null) ?
                             weakMerge(s1 != null ? s1.componentTypeState : null,
                                       s2 != null ? s2.componentTypeState : null) : null);
        }

        private static List<State> mergeTypeParams(List<State> typeParams1, List<State> typeParams2, boolean strict) {
            if (typeParams1 == null) {
                return typeParams2;
            } else if (typeParams2 == null) {
                return typeParams1;
            } else if (typeParams1.size() == typeParams2.size()) {
                List<State> typeParams = new ArrayList<>();
                for (int i = 0; i < typeParams1.size(); i++) {
                    typeParams.add(strict ? strictMerge(typeParams1.get(i), typeParams2.get(i))
                                          : weakMerge(typeParams1.get(i), typeParams2.get(i)));
                }
                return typeParams;
            } else {
                //TODO: anything can be done here?
                return null;
            }
        }

        private final StateEnum thisTypeState;
        private final List<State> typeParameters;
        private final State componentTypeState;

        public State(StateEnum thisTypeState) {
            this(thisTypeState, null, null);
        }

        public State(StateEnum thisTypeState, List<State> typeParameters) {
            this(thisTypeState, typeParameters, null);
        }

        public State(StateEnum thisTypeState, State componentTypeState) {
            this(thisTypeState, null, componentTypeState);
        }

        private State(StateEnum thisTypeState, List<State> typeParameters, State componentTypeState) {
            if (typeParameters != null && componentTypeState != null) {
                throw new IllegalStateException("Cannot have both type parameters and component type set.");
            }

            this.thisTypeState = thisTypeState;
            this.typeParameters = typeParameters;
            this.componentTypeState = componentTypeState;
        }

        public boolean isNotNull() {
            return thisTypeState.isNotNull();
        }

        public State setThisState(StateEnum newThisState) {
            return new State(newThisState, typeParameters);
        }

        @Override
        public String toString() {
            if (typeParameters != null && !typeParameters.isEmpty()) {
                return thisTypeState + "<" + typeParameters.stream().map(Object::toString).collect(Collectors.joining(", ")) + ">";
            }

            if (componentTypeState != null) {
                return thisTypeState + "[" + componentTypeState + "]";
            }

            return thisTypeState.toString();
        }
    }

    static enum StateEnum {
        NULL,
        POSSIBLE_NULL,
        POSSIBLE_NULL_EXPLICIT_UNSPECIFIED, //mostly to support/help with JSpecify's "NullnessUnspecified"
        POSSIBLE_NULL_REPORT,
        NOT_NULL,
        NOT_NULL_BE_NPE;
        
        public @CheckForNull StateEnum reverse() {
            switch (this) {
                case NULL:
                    return NOT_NULL;
                case POSSIBLE_NULL:
                case POSSIBLE_NULL_EXPLICIT_UNSPECIFIED:
                case POSSIBLE_NULL_REPORT:
                    return this;
                case NOT_NULL:
                case NOT_NULL_BE_NPE:
                    return NULL;
                default: throw new IllegalStateException();
            }
        }
        
        public boolean isNotNull() {
            return this == NOT_NULL || this == NOT_NULL_BE_NPE;
        }

        public boolean isPossibleNulLReport() {
            return this == POSSIBLE_NULL_REPORT;
        }

        public static StateEnum weakCollect(StateEnum s1, StateEnum s2) {
            if (s1 == null) return s2;
            if (s2 == null) return s1;
            return strictCollect(s1, s2);
        }

        public static StateEnum strictCollect(StateEnum s1, StateEnum s2) {
            if (s1 == s2) return s1;
            if (s1 == NULL || s2 == NULL) return POSSIBLE_NULL_REPORT;
            if (s1 == POSSIBLE_NULL_REPORT || s2 == POSSIBLE_NULL_REPORT) return POSSIBLE_NULL_REPORT;
            if (s1 != null && s2 != null && s1.isNotNull() && s2.isNotNull()) return NOT_NULL;
            
            return POSSIBLE_NULL;
        }
    }
    
    private static boolean isVariableElement(HintContext ctx, Element ve) {
        return ve != null && ((ctx != null && ctx.getPreferences().getBoolean(KEY_ENABLE_FOR_FIELDS, DEF_ENABLE_FOR_FIELDS)) ? 
                VARIABLE_ELEMENT_FIELDS : 
                VARIABLE_ELEMENT_NO_FIELDS).contains(ve.getKind());
    }
        
    private static final Set<ElementKind> VARIABLE_ELEMENT_NO_FIELDS = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    private static final Set<ElementKind> VARIABLE_ELEMENT_FIELDS = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
    
}
