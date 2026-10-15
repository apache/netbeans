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
import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PatternCaseLabelTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * Detects that a Throwable is not not actually thrown, or better it does not exit the method. The valid
 * exits are:
 * - assignment to a field
 * - pass as a method parameter
 * - return as a method's return value
 * - throw it
 * Future: comparisons == and != could be accepted as valid exits, as the value potentially affects
 * further processing. 
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "TEXT_ThrowableNotThrown=Throwable instance not thrown",
    "TEXT_ThrowableValueNotThrown=Throwable method result is ignored"
})
public class ThrowableNotThrown {
    @Hint(displayName = "#DN_CreatedThrowableNotThrown", description = "#DESC_DN_CreatedThrowableNotThrown",
            category = "bugs",
            enabled = true,
            suppressWarnings = { "ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown" })
    @TriggerPattern(value = "new $x($params$)", 
            constraints = @ConstraintVariableType(variable = "$x", type = "java.lang.Throwable"
    ))
    public static ErrorDescription newThrowable(HintContext ctx) {
        TreePath enclosingMethodPath = findEnclosingMethodPath(ctx.getPath());
        // for method/constructor, the enclMethodPath should be the block tree statement;
        // for a member variable + initializer, the encMethodPath will be the member itself
        if (enclosingMethodPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
            // skip exceptions created in a field initializer
            return null;
        }
        ThrowableTracer tracer = new ThrowableTracer(ctx.getInfo(), enclosingMethodPath);
        if (!tracer.traceThrowable(ctx.getPath())) {
            return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), 
                    Bundle.TEXT_ThrowableNotThrown());
        }
        return null;
    }
    
    private static TreePath findEnclosingMethodPath(TreePath path) {
        TreePath enclosingMethodPath = path;
        TreePath nextPath = enclosingMethodPath.getParentPath();
        Tree.Kind kind;
        do {
            Tree leaf = nextPath.getLeaf();
            enclosingMethodPath = nextPath;
            nextPath = nextPath.getParentPath();
            kind = leaf.getKind();
        } while (nextPath != null && !(kind == Tree.Kind.METHOD || kind == Tree.Kind.CLASS));
        return enclosingMethodPath;
    }

    @Hint(displayName = "#DN_ThrowableMethodResultIgnored", description = "#DESC_ThrowableMethodResultIgnored",
            category = "bugs",
            enabled = true,
            suppressWarnings = { "ThrowableResultIgnored", "", "ThrowableResultOfMethodCallIgnored" })
    @TriggerPatterns({
        @TriggerPattern(value = "$m($params$)")
    })
    public static ErrorDescription methodInvocation(HintContext ctx) {
        TreePath p = ctx.getPath();
        if (p.getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION) {
            return null;
        }
        TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(p);
        Element el = ctx.getInfo().getElements().getTypeElement("java.lang.Throwable"); // NOI18N
        if (el == null || !Utilities.isValidType(tm)) {
            // bad JDK ?
            return null;
        }
        TypeMirror b = el.asType();
        if (!Utilities.isValidType(b) || !ctx.getInfo().getTypes().isAssignable(tm, b)) {
            // does not return Throwable
            return null;
        }

        ExecutableElement initCause = (ExecutableElement)ctx.getInfo().getElementUtilities().findElement("java.lang.Throwable.initCause(java.lang.Throwable)"); // NOI18N
        if (initCause != null) {
            Element e = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$m")); // NOI18N
            // #246279 possibly e will be an unresolved symbol == a ClassSymbol, which cannot be casted to ExElement.
            if (e == null || (e.getKind() != ElementKind.CONSTRUCTOR && e.getKind() != ElementKind.METHOD)) {
                return null;
            }
            ExecutableElement thisMethod = (ExecutableElement)e;
            if (thisMethod == initCause || ctx.getInfo().getElements().overrides(thisMethod, initCause, (TypeElement)el)) {
                return null;
            }
        }
        TreePath enclosingMethodPath = findEnclosingMethodPath(ctx.getPath());
        ThrowableTracer tracer = new ThrowableTracer(ctx.getInfo(), enclosingMethodPath);
        if (!tracer.traceThrowable(ctx.getPath())) {
            return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), 
                    Bundle.TEXT_ThrowableValueNotThrown());
        }
        return null;
    }

    private static class ThrowableTracer {
        private final CompilationInfo info;
        private FlowResult flowResult;
        private final TreePath enclosingMethodPath;
        
        /**
         * Assignments of a Throwable placed into local variables. Because they need to be found in Flow
         * data, the *expression* Nodes are registered, not the target variables.
         */
        private Set<Tree> varAssignments = new HashSet<Tree>();
        
        /**
         * Processed assignments, so they will not be processed again.
         */
        private Set<Tree> processedVariables = new HashSet<Tree>();

        public ThrowableTracer(CompilationInfo info, TreePath enclosingMethodPath) {
            this.info = info;
            this.enclosingMethodPath = enclosingMethodPath;
        }
        
        private Collection<Tree> getNewAssignments() {
            if (processedVariables.isEmpty()) {
                // do not addAll, just swap. In the next iteration, processed != empty
                Set<Tree> x = processedVariables;
                processedVariables = varAssignments;
                varAssignments = x;
                return processedVariables;
            }
            Set<Tree> nue = new HashSet<Tree>(varAssignments);
            nue.removeAll(processedVariables);
            processedVariables.addAll(varAssignments);
            varAssignments.clear();
            
            return nue;
        }
        
        /**
         * Process variable usages. Usages are found by reversing basic Flow data and translated to Paths.
         * Paths are cached in the Flow for other potential uses.
         */
        private Boolean processVariables() {
            Collection<Tree> vars = getNewAssignments();
            if (vars.isEmpty()) {
                // unless there's a variable using the Throwable, the value is not properly used
                // and will be reported.
                return Boolean.FALSE;
            }
            FlowResult r = getFlowResult();
            
            for (Tree t : vars) {
                
                Iterable<? extends Tree> usages = r.getValueUsers(t);
                if (usages != null) {
                    for (Tree u : usages) {
                        TreePath pu = r.findPath(u, info.getCompilationUnit());
                        if (pu == null) {
                            continue;
                        }
                        Boolean result = processEnclosingStatement(pu);
                        // accept any usage as a final result
                        if (result == Boolean.TRUE) {
                            return result;
                        }
                    }
                }
            }
            return null;
        }
        
        private FlowResult getFlowResult() {
            if (flowResult == null) {
                flowResult = Flow.assignmentsForUse(info, 
                        enclosingMethodPath, new AtomicBoolean(false));
            }
            return flowResult;
        }
        
        boolean traceThrowable(TreePath path) {
            Boolean b = processEnclosingStatement(path);
            if (b != null) {
                return b;
            }
            // recursively process pass through variables.
            while ((b = processVariables()) == null) {
                // OK
            }
                
            return b;
        }

        /**
         * Should start from the expression (i.e. new Throwable) itself, traverses up to the statement level
         * and either signals an exit, or records an expression assignment for further analysis in varAssignments.
         * 
         */
        Boolean processEnclosingStatement(TreePath excPath) {
            Tree prevLeaf = excPath.getLeaf();

            boolean process;
            do {
                excPath = excPath.getParentPath();
                Tree leaf = excPath.getLeaf();
                Tree.Kind kind = leaf.getKind();
                process = false;
                switch (kind) {
                    case THROW:
                        // OK, exception used.
                        return true;
                        
                    case FOR_LOOP:
                        if (prevLeaf == ((ForLoopTree)leaf).getCondition()) {
                            // ok, guards a loop
                            return true;
                        }
                        break;

                    case IF:
                    case WHILE_LOOP:
                    case DO_WHILE_LOOP:
                    case RETURN:
                        return true;
                        
                    case LAMBDA_EXPRESSION:
                        // the Throwable is used as a part of Lambda expression;
                        // ensure that the lambda is reasonably used:
                        process = true;
                        break;
                        
                    case VARIABLE:  {
                        VariableTree var = (VariableTree)leaf;
                        Element el = info.getTrees().getElement(new TreePath(excPath, var));
                        if (el == null || el.getKind() == ElementKind.FIELD) {
                            return true;
                        } else if (el.getKind() == ElementKind.LOCAL_VARIABLE) {
                            varAssignments.add(var.getInitializer());
                        }
                        process = true;
                        break;
                    }
                    case AND_ASSIGNMENT: case OR_ASSIGNMENT: case XOR_ASSIGNMENT:
                    case ASSIGNMENT: {
                        // might be OK, exception assigned to a variable
                        AssignmentTree as = (AssignmentTree)leaf;
                        Tree var = as.getVariable();
                        Element el = info.getTrees().getElement(new TreePath(excPath, var));
                        if (el == null || el.getKind() == ElementKind.FIELD) {
                            return true;
                        } else if (Flow.LOCAL_VARIABLES.contains(el.getKind())) {
                            varAssignments.add(as.getExpression());
                        }
                        process = true;
                        break;
                    }
                    case MEMBER_SELECT: {
                        Element el = info.getTrees().getElement(excPath);
                        if (el == null) {
                            return true;
                        }
                        if (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR) {
                            ExecutableElement xel = (ExecutableElement)el;
                            TypeMirror tm = xel.getReturnType();
                            if (!Utilities.isValidType(tm) || tm.getKind() == TypeKind.VOID) {
                                return true;
                            }
                            // probably method invocation, skip the parent
                            Tree.Kind k = excPath.getParentPath().getLeaf().getKind();
                            if (k == Tree.Kind.NEW_CLASS || k == Tree.Kind.METHOD_INVOCATION) {
                                return true;
                            }
                        }
                        process = true;
                        break;
                    }
                    
                    case METHOD_INVOCATION: {
                        // might be OK, instance passed to a method 
                        MethodInvocationTree invTree = (MethodInvocationTree)leaf;
                        // if not among arguments, then some method is invoked on the Throwable.
                        // should be considered OK ???
                        return invTree.getArguments().contains(prevLeaf);
                    }
                    // the same for ctor invocation
                    case NEW_CLASS: {
                        NewClassTree nct = (NewClassTree)leaf;
                        return nct.getArguments().contains(prevLeaf);
                    }
                    
                    case LOGICAL_COMPLEMENT: 
                    case CONDITIONAL_AND: case CONDITIONAL_OR:
                    case EQUAL_TO: case NOT_EQUAL_TO: case INSTANCE_OF:
                    case PARENTHESIZED:
                    case TYPE_CAST: 
                        // escalate furter
                        process = true;
                        break;

                    // in conditionals, the true/false branch value potentially goes up and can be used/thrown/assigned
                    case CONDITIONAL_EXPRESSION: {
                        ConditionalExpressionTree cond = (ConditionalExpressionTree)leaf;
                        process = cond.getTrueExpression() == prevLeaf || cond.getFalseExpression() == prevLeaf;
                        break;
                    }

                    case SWITCH: {
                        SwitchTree st = (SwitchTree) leaf;

                        if (st.getExpression() == prevLeaf) {
                            collectCaseBindings(st.getCases());
                        }

                        break;
                    }
                    case SWITCH_EXPRESSION: {
                        SwitchExpressionTree st = (SwitchExpressionTree) leaf;

                        if (st.getExpression() == prevLeaf) {
                            collectCaseBindings(st.getCases());
                            process = true;
                        }
                        break;
                    }
                }
                prevLeaf = excPath.getLeaf();
            } while (process);
            return varAssignments.isEmpty() ? Boolean.FALSE : null;
        }

        private void collectCaseBindings(List<? extends CaseTree> cases) {
            //all binding patterns should be considered as new target variables for the selector value:
            cases.stream()
                 .flatMap(cse -> cse.getLabels().stream())
                 .filter(label -> label.getKind() == Kind.PATTERN_CASE_LABEL)
                 .map(label -> ((PatternCaseLabelTree) label).getPattern())
                 .filter(pattern -> pattern.getKind() == Kind.BINDING_PATTERN)
                 .map(pattern -> ((BindingPatternTree) pattern).getVariable())
                 .filter(var -> !var.getName().isEmpty())
                 .forEach(varAssignments::add);
        }
    }
}
