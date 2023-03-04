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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ConstantCaseLabelTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;

/**
 * Checks that the method recurses indefinitely and the only way how they could end is throwing an exception.
 * 
 * PENDING: the analysis could be generalized, so it serves also infinite cycle detection etc. Could be also factored out
 * of Flow, which also joins various alternatives and could build a control flow graph.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - method name",
    "ERR_MethodWillRecurse=The method {0} will recurse infinitely",
    "# {0} - method name",
    "ERR_MethodMayRecurse=The method {0} may recurse if not overriden in subclasses"
})
public class InfiniteRecursion {
    private static final boolean DEFAUL_EXCLUDE_OVERRIDABLES = false;

    @BooleanOption(
        displayName = "#OPTNAME_InfiniteRecursionNoOverridables",
        tooltip = "#OPTDESC_InfiniteRecursionNoOverridables",
        defaultValue = DEFAUL_EXCLUDE_OVERRIDABLES
    )
    public static final String OPTION_EXCLUDE_OVERRIDABLES = "no.overridables"; // NOI18N
    
    @Hint(
        displayName = "#DN_InfiniteRecursion",
        description = "#DESCR_InfiniteRecursion",
        enabled = true,
        category = "bugs",
        suppressWarnings = { "InfiniteRecursion" },
        options = Hint.Options.QUERY
    )
    @UseOptions({OPTION_EXCLUDE_OVERRIDABLES})
   @TriggerTreeKind(Tree.Kind.METHOD)
    public static ErrorDescription run(HintContext ctx) {
        Tree leaf = ctx.getPath().getLeaf();
        if (leaf.getKind() != Tree.Kind.METHOD) {
            return null;
        }
        MethodTree mt = (MethodTree)leaf;
        Element ee = ctx.getInfo().getTrees().getElement(ctx.getPath());
        if (ee == null || ee.getKind() != ElementKind.METHOD || mt.getBody() == null) {
            return null;
        }
        boolean overridable = false;
        boolean excludeOverridables = ctx.getPreferences().getBoolean(
                OPTION_EXCLUDE_OVERRIDABLES, DEFAUL_EXCLUDE_OVERRIDABLES);
        Set<Modifier> mods = ee.getModifiers();
        // if a method is static or final, it cannot resolve to other code at runtime
        // if a method is just private, it could be overriden within the file, but that
        // can be effectively checked.
        if (!(mods.contains(Modifier.STATIC) ||
             (mods.contains(Modifier.PRIVATE) ||
             (mods.contains(Modifier.FINAL))))) {
            // check that the class is instantiable - has some non-private constructor, or no
            // constructor, is not abstract
            TypeElement clazz = ctx.getInfo().getElementUtilities().enclosingTypeElement(ee);
            if (clazz == null) {
                return null;
            }
            
            // if the class is private, its subclasses cannot escape outside the file; worth checking
            if (!clazz.getModifiers().contains(Modifier.PRIVATE) && !clazz.getModifiers().contains(Modifier.FINAL)) {
                for (ExecutableElement ce : ElementFilter.constructorsIn(clazz.getEnclosedElements())) {
                    if (!ce.getModifiers().contains(Modifier.PRIVATE)) {
                        // suppress, can be subclassed and overriden. Do not index-search for actual
                        // overrides for performance reasons.
                        if (excludeOverridables) {
                            return null;
                        }
                        overridable = true;
                    }
                }
                
            }
        }
        // PENDING - in the case the class is private, the parent's member types
        // should be checked that they actually override the suspicious method
        
        RecursionVisitor visitor = new RecursionVisitor(ctx.getInfo(), (ExecutableElement)ee);
        State result = visitor.scan(new TreePath(ctx.getPath(), mt.getBody()), null);
        if (result == State.MUST) {
            // just in case, recursion points should have allways been filled.
            TreePath somePoint = visitor.recursionPoints.isEmpty() ? ctx.getPath() : visitor.recursionPoints.get(0);
            return ErrorDescriptionFactory.forTree(ctx, somePoint, 
                    overridable ?
                    ERR_MethodMayRecurse(mt.getName().toString()):
                    ERR_MethodWillRecurse(mt.getName().toString())
            );
        }
        return null;
    }
    
    /**
     * Placeholders for reference values that are null or non-null
     */
    private enum State {
        UNKNOWN {
            public State join(State other) {
                return this;
            }
            
            public State append(State other) {
                return other;
            }
        },
        
        NO {
            public State join(State other) {
                return this;
            }

            public State append(State other) {
                return other;
            }
        },
        
        //MAY,
        MUST {
            public State join(State other) {
                return other;
            }
            public State append(State other) {
                return this;
            }
        },
        
        RETURN {
            public State join(State other) {
                return this;
            }
            
            public State append(State other) {
                return this;
            }
        }
        ;

        public abstract State join(State other);
        public abstract State append(State other);
        
    }

    /**
     * The Visitor tries to determine whether a method (tree) definitely recurses - calls the same method.
     * There are several cases that need to be covered:
     * - break/continue statement will transfer control to its target
     * 
     * PENDING - throw / catch is not handled well;
     * 
     * PEDNING - continue causes for-update and do-condition to be evaluated. If a branch A recurses, but all other
     * alternative branches continues up to for/do cycle X, then that condition might cause recursion as well. Would apply
     * only in the speculative for loop evaluation, otherwise for/while bodies incl any continue statements do not count.
     * 
     */
    private static class RecursionVisitor extends ErrorAwareTreePathScanner<State, Void> {
        private final CompilationInfo ci;
        private final ExecutableElement checkMethod;

        /**
         * If not null, it contains a result that may terminate the current processing;
         * the visitor should unroll up to the nearest branching point, where the result will
         * be processed as one of the alternatives.
         */
        private State knownResult;
        
        /**
         * Break and continue jumps encountered. Since each recursion or return causes the processing
         * to roll back to nearest branch point, if the jumps collection is not empty, break/continue was
         * encountered prior to the return/recurse.
         */
        private Set breakContinueJumps = new HashSet<Tree>();
        
        /**
         * Possible break/continue targets in the scope
         */
        private final Deque<Tree> breakContinueTargets = new LinkedList<Tree>();
        
        /**
         * The immediate break/continue target. Actually could be a part of the breakContinueTargets,
         * but it is compared often so it is in a separate variable.
         */
        private Tree currentBreakTarget;
        
        /**
         * If true, the current path is definitely reached, it is not just an alternative.
         */
        private boolean definitePath;
        
        /**
         * Accumulates calls that will cause recursion. Reset at branch point once a non-recursive
         * alternative is found.
         */
        private final  List<TreePath>  recursionPoints = new LinkedList<TreePath>();
        
        private void registerBreakTarget(Tree t) {
            if (currentBreakTarget != null && currentBreakTarget != t) {
                breakContinueTargets.offer(currentBreakTarget);
            }
            currentBreakTarget = t;
        }
        
        public RecursionVisitor(CompilationInfo ci, ExecutableElement checkMethod) {
            this.ci = ci;
            this.checkMethod = checkMethod;
        }
        
        @Override
        public State scan(Tree tree, Void p) {
            // terminate everything
            if (knownResult != null) {
                return knownResult;
            }
            State s = super.scan(tree, p);
            if (s == State.NO) {
                knownResult = null;
                recursionPoints.clear();
            }
            if (tree == currentBreakTarget) {
                currentBreakTarget = breakContinueTargets.poll();
                breakContinueTargets.remove(tree);
            }
            return s == null ? State.NO : s;
        }
        
        public State scan(Iterable<? extends Tree> nodes, Void p) {
            State s = super.scan(nodes, p);
            return s == null ? State.NO : s;
        }
        
        @Override
        public State reduce(State r1, State r2) {
            if (knownResult != null) {
                return knownResult;
            }
            if (r1 == null) {
                return r2;
            } else if (r2 == null) {
                return r1;
            }
            return r1.append(r2);
        }
        
        private boolean returnIfRecurse(State s) {
            if (s == State.MUST || s == State.RETURN) {
                this.knownResult = s;
            }
            if (s == State.MUST) {
                return true;
            } else {
                return false;
            }
        }
        
        @Override
        public State visitBinary(BinaryTree node, Void p) {
            State s1, s2;

            if (returnIfRecurse(s1 = scan(node.getLeftOperand(), p))) {
                return s1;
            }
            Object result = null;
            if (node.getKind() == Tree.Kind.CONDITIONAL_AND || node.getKind() == Tree.Kind.CONDITIONAL_OR) {
                // does not recurse, but there's a chance the first part of the conditional will complete the condition
                // and will avoid the recursion.
                result = ArithmeticUtilities.compute(ci,
                        new TreePath(getCurrentPath(), node.getLeftOperand()), true, true);
                // return if:
                // - we have OR and the left op is const true
                // - we have AND and the left op is const false;
                if (result != null && 
                    ((result == Boolean.TRUE) == (node.getKind() == Tree.Kind.CONDITIONAL_OR))) {
                    return State.NO;
                }
            }
            boolean saveDefinite = definitePath;
            definitePath = result != null;
            returnIfRecurse(s2 = scan(node.getRightOperand(), p));
            definitePath = saveDefinite;
            thisTree = null;
            return s2;
        }
        
        private Tree thisTree;
        
        

        @Override
        public State visitIdentifier(IdentifierTree node, Void p) {
            if (node.getName().contentEquals("this")) { // NOI18N
                thisTree = node;
            }
            return super.visitIdentifier(node, p);
        }

        @Override
        public State visitMemberSelect(MemberSelectTree node, Void p) {
            this.thisTree = null;
            State s = super.visitMemberSelect(node, p); 
            if (this.thisTree != null) {
                Element el = ci.getTrees().getElement(getCurrentPath());
                if (el == null || el.getKind() != ElementKind.METHOD) {
                    this.thisTree = null;
                }
            }
            return s;
        }

        @Override
        public State visitMethodInvocation(MethodInvocationTree node, Void p) {
            Element target = ci.getTrees().getElement(getCurrentPath());
            if (target == null) {
                return State.NO;
            }
            State r = null;
            if (target == checkMethod && breakContinueJumps.isEmpty()) {
                if (node.getMethodSelect().getKind() != Tree.Kind.IDENTIFIER) {
                    this.thisTree = null;

                    returnIfRecurse(r = scan(node.getMethodSelect(), p));
                    if (this.thisTree != null) {
                        r = State.MUST;
                    }
                } else {
                    r = State.MUST;
                }
            } else {
                r = scan(node.getMethodSelect(), p);
            }
            if (r == null) {
                for (Tree arg : node.getArguments()) {
                    if (returnIfRecurse(r = scan(arg, p))) {
                        break;
                    }
                }
            }
            this.thisTree = null;
            if (r == null || r == State.NO) {
                return State.NO;
            }
            recursionPoints.add(getCurrentPath());
            return knownResult = State.MUST;
        }

        @Override
        public State visitContinue(ContinueTree node, Void p) {
            StatementTree target = ci.getTreeUtilities().getBreakContinueTarget(getCurrentPath());
            breakContinueJumps.add(target);
            return State.NO;
        }

        @Override
        public State visitBreak(BreakTree node, Void p) {
            Tree target = ci.getTreeUtilities().getBreakContinueTargetTree(getCurrentPath());
            breakContinueJumps.add(target);
            return State.NO;
        }

        @Override
        public State visitReturn(ReturnTree node, Void p) {
            State s;
            if (returnIfRecurse(s = scan(node.getExpression(), p))) {
                return s;
            }
            return knownResult = State.RETURN;
        }

        @Override
        public State visitLambdaExpression(LambdaExpressionTree node, Void p) {
            // lambda expressions are not evaluated immediately
            return State.NO;
        }

        @Override
        public State visitLabeledStatement(LabeledStatementTree node, Void p) {
            registerBreakTarget(node);
            registerBreakTarget(node.getStatement());
            return super.visitLabeledStatement(node, p);
        }

        @Override
        public State visitIf(IfTree node, Void p) {
            return visitConditional(node.getCondition(), node.getThenStatement(), node.getElseStatement(), p);
        }
        
        private State visitConditional(Tree conditionTree, Tree trueTree, Tree falseTree, Void p) {
            State s;

            if (returnIfRecurse(s= scan(conditionTree, p))) {
                return s;
            }
            Object conditionValue = ArithmeticUtilities.compute(ci, 
                    new TreePath(getCurrentPath(), conditionTree), true, true);
            if (conditionValue == Boolean.TRUE) {
                return scan(trueTree, p);
            } else if (conditionValue == Boolean.FALSE) {
                if (falseTree == null) {
                    return State.NO;
                }
                return scan(falseTree, p);
            } else {
                boolean saveDefinite = definitePath;
                definitePath = false;
                Set<Tree> saveBreaks = new HashSet<Tree>(breakContinueJumps);
                breakContinueJumps = new HashSet<Tree>();
                State s1 = scan(trueTree, p);
                knownResult = null;
                State s2 = scan(falseTree, p);
                definitePath = saveDefinite;
                if (!breakContinueJumps.isEmpty()) {
                    breakContinueJumps.addAll(saveBreaks);
                    // possible break/continue transfers control away from ifs
                    return State.NO;
                }
                returnIfRecurse(s = s1.join(s2));
                return s;
            }
        }

        @Override
        public State visitConditionalExpression(ConditionalExpressionTree node, Void p) {
            return visitConditional(node.getCondition(), node.getTrueExpression(), node.getFalseExpression(), p);
        }
        
        @Override
        public State visitSwitch(SwitchTree node, Void p) {
            return handleSwitch(node, node.getExpression(), node.getCases(), p);
        }

        @Override
        public State visitSwitchExpression(SwitchExpressionTree node, Void p) {
            return handleSwitch(node, node.getExpression(), node.getCases(), p);
        }

        private State handleSwitch(Tree node, ExpressionTree expression, List<? extends CaseTree> cases, Void p) {
            registerBreakTarget(node);
            State s;
            if (returnIfRecurse(s= scan(expression, p))) {
                return s;
            }
            // look for the default case, but cannot return immediately as some return / break inside could
            // slip unnoticed.
            boolean exhaustive = false;
            
            Set<Tree> saveBreaks = breakContinueJumps;
            Set<Tree> collectBreaks = Collections.emptySet();
            
            State lastState = State.NO;
            
            boolean saveDefinite = definitePath;
            definitePath = false;
            for (CaseTree ct : cases) {
                if (ct.getExpression() == null) {
                    exhaustive = true;
                }
                knownResult = null;
                breakContinueJumps = new HashSet<Tree>();
            
                s = scan(ct, p);
                if (s == State.RETURN) {
                    // possible return reachable from the branch, bail out
                    definitePath = saveDefinite;
                    return knownResult = s;
                    // the branch just jumped off
                } else {
                    // the branch recurses. But if the branch also contains breaks out of the switch, or out of outer
                    // cycles, those breaks must have been found before the recursion instruction. Any jumps to
                    // nested statements should have been cleared by scan().
                    boolean self = breakContinueJumps.remove(node);
                    if (self || !breakContinueJumps.isEmpty() || (ct.getCaseKind() == CaseKind.RULE && s != State.MUST)) {
                        // at least one way out
                        saveBreaks.addAll(breakContinueJumps);
                        saveBreaks.addAll(collectBreaks);
                        breakContinueJumps = saveBreaks;
                        definitePath = saveDefinite;
                        recursionPoints.clear();
                        return State.NO;
                    }
                    lastState = s;
                }
            }
            definitePath = saveDefinite;
            if (exhaustive) {
                return lastState;
            } else {
                recursionPoints.clear();
                return State.NO;
            }
        }

        @Override
        public State visitConstantCaseLabel(ConstantCaseLabelTree node, Void p) {
            return super.scan(node.getConstantExpression(), p);
        }

        @Override
        public State visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
            State s;

            registerBreakTarget((node));
            returnIfRecurse(s = scan(node.getExpression(), p));
            // if the expression does not recurse, it might evaluate to an empty Iterable, and skip the entire body.
            
            // PENDING: speculatively report recursions, if unconditionally reachable from cycles ?
            return s;
        }

        @Override
        public State visitForLoop(ForLoopTree node, Void p) {
            State s;

            registerBreakTarget((node));
            if (returnIfRecurse(s = scan(node.getInitializer(), p))) {
                return s;
            }
            returnIfRecurse(s = s.append(scan(node.getCondition(), p)));
            // the body + update might be skipped if condition evaluates to false immediately.

            // PENDING: speculatively report recursions, if unconditionally reachable from cycles ?
            return s;
        }

        @Override
        public State visitWhileLoop(WhileLoopTree node, Void p) {
            State s;
            registerBreakTarget((node));
            returnIfRecurse(s = scan(node.getCondition(), p));
            return s;
        }

        @Override
        public State visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            State s;

            registerBreakTarget((node));
            if (returnIfRecurse(s = scan(node.getStatement(), p))) {
                return s;
            }
            returnIfRecurse(s = s.append(scan(node.getCondition(), p)));
            return s;
        }

        @Override
        public State visitClass(ClassTree node, Void p) {
            // do not recurse into localy declared classes
            return State.NO;
        }

        @Override
        public State visitNewClass(NewClassTree node, Void p) {
            State r1 = scan(node.getEnclosingExpression(), p);
            if (returnIfRecurse(r1)) {
                return r1;
            }
            State r = r1;
            // skip identifier and type arguments, no executable code there
            returnIfRecurse(r = r.append(scan(node.getArguments(), p)));
            // skip class body
            
            // PENDING - if the class contains a definitive recursion in its initializer, or the invoked constructor,
            // should be reported.
            return r;
        }
    }
}
