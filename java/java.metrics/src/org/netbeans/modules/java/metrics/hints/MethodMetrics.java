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
package org.netbeans.modules.java.metrics.hints;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Collection;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.metrics.hints.Bundle.*;


/**
 * Inspections based on metrics computed for individual methods.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - method name",
    "# {1} - cyclomatic complexity of the method",
    "TEXT_MethodTooComplex=The method ''{0}'' is too complex; cyclomatic complexity: {1}",
    "# {0} - method name",
    "# {1} - maximum depth of statements in method",
    "TEXT_MethodTooDeepNesting=Method ''{0}'' contains too deep statement structure: {1}",
    "# {0} - method name",
    "# {1} - number of lines in method",
    "TEXT_MethodTooLongLines=Method ''{0}'' is too long: {1} lines",
    "# {0} - method name",
    "# {1} - number of lines in method",
    "TEXT_MethodTooLongStatements=Method ''{0}'' is too long: {1} statements",
    "# {0} - method name",
    "# {1} - number of exceptions declared by the method",
    "TEXT_MethodTooManyExceptions=Method ''{0}'' declares too many exceptions: {1}",
    "# {0} - method name",
    "# {1} - number of parameters declared by the method",
    "TEXT_MethodTooManyParameters=Method ''{0}'' takes too many parameters: {1}",
    "# {0} - method name",
    "# {1} - number of return points",
    "TEXT_MethodMultipleReturns=Method ''{0}'' has multiple return points: {1}",
    "# {0} - method name",
    "# {1} - number of negations",
    "TEXT_MethodMultipleNegations=Method ''{0}'' contains too many negations: {1}",
    "# {0} - method name",
    "# {1} - number of loops",
    "TEXT_MethodMultipleLoops=Method ''{0}'' contains {1} loops",
    "# {0} - method name",
    "# {1} - number of dependencies",
    "TEXT_MethodTooCoupled=Method ''{0}'' is too coupled. References {1} types",

    "# {0} - cyclomatic complexity of the method",
    "TEXT_ConstructorTooComplex=Constructor is too complex; cyclomatic complexity: {0}",
    "# {0} - maximum depth of statements in method",
    "TEXT_ConstructorTooDeepNesting=Constructor contains too deep statement structure: {0}",
    "# {0} - number of lines in method",
    "TEXT_ConstructorTooLongLines=Constructor is too long: {0} lines",
    "# {0} - number of lines in method",
    "TEXT_ConstructorTooLongStatements=Constructor is too long: {0} statements",
    "# {0} - number of exceptions declared by the method",
    "TEXT_ConstructorTooManyExceptions=Constructor declares too many exceptions: {0}",
    "# {0} - number of parameters declared by the method",
    "TEXT_ConstructorTooManyParameters=Constructor takes too many parameters: {0}",
    "# {0} - number of return points",
    "TEXT_ConstructorMultipleReturns=Constructor has multiple return points: {0}",
    "# {0} - number of negations",
    "TEXT_ConstructorMultipleNegations=Constructor contains too many negations: {0}",
    "# {0} - number of loops",
    "TEXT_ConstructorMultipleLoops=Constructor contains {0} loops",
    "# {0 - number of dependencies",
    "TEXT_ConstructorTooCoupled=Constructor is too coupled. References {0} types"
})
public class MethodMetrics {
    static final int DEFAULT_COMPLEXITY_LIMIT = 10;
    static final int DEFAULT_NESTING_LIMIT = 6;
    static final int DEFAULT_LINES_LIMIT = 60;
    static final int DEFAULT_EXCEPTIONS_LIMIT = 3;
    static final int DEFAULT_STATEMENTS_LIMIT = 30;
    static final int DEFAULT_METHOD_PARAMETERS_LIMIT = 8;
    static final int DEFAULT_CTOR_PARAMETERS_LIMIT = 12;
    static final boolean DEFAULT_IGNORE_RETURN_GUARDS = true;
    static final boolean DEFAULT_IGNORE_EQUALS = true;
    static final int DEFAULT_RETURN_LIMIT = 2;
    static final int DEFAULT_NEGATIONS_LIMIT = 3;
    static final boolean DEFAULT_NEGATIONS_IGNORE_ASSERT = true;
    static final boolean DEFAULT_NEGATIONS_IGNORE_EQUALS = true;
    static final boolean DEFAULT_COUPLING_IGNORE_JAVA = true;
    static final int DEFAULT_LOOPS_LIMIT = 3;
    static final int DEFAULT_COUPLING_LIMIT = 15;
    
    @IntegerOption(
        displayName = "#OPTNAME_MethodComplexityLimit",
        tooltip = "#OPTDESC_MethodComplexityLimit",
        maxValue = 1000,
        step = 1,
        defaultValue = DEFAULT_COMPLEXITY_LIMIT
    )
    public static final String OPTION_COMPLEXITY_TRESHOLD = "metrics.method.complexity.limit"; // NOI18N
    
    @IntegerOption(
        displayName = "#OPTNAME_MethodDepthLimit",
        tooltip = "#OPTDESC_MethodDepthLimit",
        maxValue = 100,
        step = 1,
        defaultValue = DEFAULT_NESTING_LIMIT
    )
    public static final String OPTION_NESTING_LIMIT = "metrics.method.depth.limit"; // NOI18N
    
    @IntegerOption(
        displayName = "#OPTNAME_MethodLinesLimit",
        tooltip = "#OPTDESC_MethodLinesLimit",
        maxValue = 60,
        step = 50,
        defaultValue = DEFAULT_LINES_LIMIT
    )
    public static final String OPTION_LINES_LIMIT = "metrics.method.lines.limit"; // NOI18N

    @IntegerOption(
        displayName = "#OPTNAME_MethodStatementsLimit",
        tooltip = "#OPTDESC_MethodStatementsLimit",
        maxValue = 30,
        step = 5,
        defaultValue = DEFAULT_LINES_LIMIT
    )
    public static final String OPTION_STATEMENTS_LIMIT = "metrics.method.statements.limit"; // NOI18N

    @IntegerOption(
        displayName = "#OPTNAME_MethodExceptionsLimit",
        tooltip = "#OPTDESC_MethodExceptionsLimit",
        maxValue = 50,
        minValue = 1,
        step = 1,
        defaultValue = DEFAULT_EXCEPTIONS_LIMIT
    )
    public static final String OPTION_EXCEPTIONS_LIMIT = "metrics.method.exceptions.limit"; // NOI18N

    @IntegerOption(
        displayName = "#OPTNAME_MethodParametersLimit",
        tooltip = "#OPTDESC_MethodParametersLimit",
        maxValue = 100,
        minValue = 2,
        step = 1,
        defaultValue = DEFAULT_METHOD_PARAMETERS_LIMIT
    )
    public static final String OPTION_METHOD_PARAMETERS_LIMIT = "metrics.method.parameters.limit"; // NOI18N
    
    @IntegerOption(
        displayName = "#OPTNAME_CtorParametersLimit",
        tooltip = "#OPTDESC_CtorParametersLimit",
        maxValue = 100,
        minValue = 2,
        step = 1,
        defaultValue = DEFAULT_CTOR_PARAMETERS_LIMIT
    )
    public static final String OPTION_CTOR_PARAMETERS_LIMIT = "metrics.constructor.parameters.limit"; // NOI18N

    @IntegerOption(
        displayName = "#OPTNAME_MethodReturnLimit",
        tooltip = "#OPTDESC_MethodReturnLimit",
        maxValue = 100,
        minValue = 1,
        step = 1,
        defaultValue = DEFAULT_RETURN_LIMIT
    )
    public static final String OPTION_RETURN_LIMIT = "metrics.method.return.limit"; // NOI18N

    @BooleanOption(
        displayName = "#OPTNAME_MethodIgnoreReturnGuards",
        tooltip = "#OPTDESC_MethodIgnoreReturnGuards",
        defaultValue = DEFAULT_IGNORE_RETURN_GUARDS
    )
    public static final String OPTION_RETURN_IGNORE_GUARDS = "metrics.method.returns.ignoreguards"; // NOI18N

    @BooleanOption(
        displayName = "#OPTNAME_MethodIgnoreReturnEquals",
        tooltip = "#OPTDESC_MethodIgnoreReturnEquals",
        defaultValue = DEFAULT_IGNORE_EQUALS
    )
    public static final String OPTION_RETURN_IGNORE_EQUALS = "metrics.method.returns.ignoreequals"; // NOI18N

    @IntegerOption(
        displayName = "#OPTNAME_MethodNegationsLimit",
        tooltip = "#OPTDESC_MethodNegationsLimit",
        maxValue = 100,
        step = 1,
        defaultValue = DEFAULT_NEGATIONS_LIMIT
    )
    public static final String OPTION_NEGATIONS_LIMIT = "metrics.method.negations.limit"; // NOI18N
    
    @BooleanOption(
        displayName = "#OPTNAME_MethodNegationsIgnoreEquals",
        tooltip = "#OPTDESC_MethodNegationsIgnoreEquals",
        defaultValue = DEFAULT_NEGATIONS_IGNORE_EQUALS
    )
    public static final String OPTION_NEGATIONS_IGNORE_EQUALS = "metrics.method.negations.ignoreequals"; // NOI18N

    @BooleanOption(
        displayName = "#OPTNAME_MethodNegationsIgnoreAsserts",
        tooltip = "#OPTDESC_MethodNegationsIgnoreAsserts",
        defaultValue = DEFAULT_NEGATIONS_IGNORE_ASSERT
    )
    public static final String OPTION_NEGATIONS_IGNORE_ASSERT = "metrics.method.negations.ignoreassert"; // NOI18N
    
    @IntegerOption(
        displayName = "#OPTNAME_MethodLoopsLimit",
        tooltip = "#OPTDESC_MethodLoopsLimit",
        maxValue = 100,
        step = 1,
        defaultValue = DEFAULT_LOOPS_LIMIT
    )
    public static final String OPTION_LOOPS_LIMIT = "metrics.method.loops.limit"; // NOI18N
    
    
    @IntegerOption(
        displayName = "#OPTNAME_MethodCouplingLimit",
        tooltip = "#OPTDESC_MethodCouplingLimit",
        maxValue = 1000,
        step = 1,
        defaultValue = DEFAULT_COUPLING_LIMIT
    )
    public static final String OPTION_COUPLING_LIMIT = "metrics.method.coupling.limit"; // NOI18N
    
    @BooleanOption(
        displayName = "#OPTNAME_MethodCouplingIgnoreJava",
        tooltip = "#OPTDESC_MethodCouplingIgnoreJava",
        defaultValue = DEFAULT_COUPLING_IGNORE_JAVA
    )
    public static final String OPTION_COUPLING_IGNORE_JAVA = "metrics.method.coupling.nojava"; // NOI18N
    
    public static final String OPTION_COUPLING_IGNORE_LIBS = "metrics.method.coupling.nolibraries"; // NOI18N

    private static boolean methodOrConstructor(HintContext ctx) {
        Element el = ctx.getInfo().getTrees().getElement(ctx.getPath());
        return el.getKind() == ElementKind.CONSTRUCTOR;
    }
    
    @Hint(category = "metrics",
          displayName = "#DN_MethodTooComplex",
          description = "#DESC_MethodTooComplex",
          options = { Hint.Options.QUERY, Hint.Options.HEAVY },
          enabled = false
    )
    @TriggerTreeKind(Tree.Kind.METHOD)
    @UseOptions(value = { OPTION_COMPLEXITY_TRESHOLD })
    public static ErrorDescription methodTooComplex(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        CyclomaticComplexityVisitor v = new CyclomaticComplexityVisitor();
        v.scan(ctx.getPath(), v);
        int complexity = v.getComplexity();
        
        int treshold = ctx.getPreferences().getInt(OPTION_COMPLEXITY_TRESHOLD, DEFAULT_COMPLEXITY_LIMIT);
        if (complexity <= treshold) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, t, 
                methodOrConstructor(ctx) ?
                TEXT_ConstructorTooComplex(complexity) :
                TEXT_MethodTooComplex(method.getName().toString(), complexity)
        );
    }
    
    @Hint(
         category = "metrics",
         displayName = "#DN_MethodTooDeepNesting",
         description = "#DESC_MethodTooDeepNesting",
         options = { Hint.Options.QUERY, Hint.Options.HEAVY },
         enabled = false
    )
    @TriggerTreeKind(Tree.Kind.METHOD)
    @UseOptions(value = { OPTION_NESTING_LIMIT })
    public static ErrorDescription tooDeepNesting(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        DepthVisitor v = new DepthVisitor();
        v.scan(ctx.getPath(), null);
        
        int depth = v.getDepth();
        int treshold = ctx.getPreferences().getInt(OPTION_NESTING_LIMIT, DEFAULT_NESTING_LIMIT);
        if (depth <= treshold) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, t, 
                methodOrConstructor(ctx) ?
                TEXT_ConstructorTooDeepNesting(depth) :
                TEXT_MethodTooDeepNesting(method.getName().toString(), depth)
        );
    }
    
    @Hint(
        category = "metrics",
        displayName = "#DN_MethodTooLong",
        description = "#DESC_MethodTooLong",
        options = { Hint.Options.QUERY, Hint.Options.HEAVY },
        enabled = false
    )
    @TriggerTreeKind(Tree.Kind.METHOD)
    @UseOptions({ OPTION_LINES_LIMIT, OPTION_STATEMENTS_LIMIT })
    public static ErrorDescription tooLong(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        NCLOCVisitor v = new NCLOCVisitor(ctx.getInfo().getSnapshot().getText(), ctx.getInfo().getTrees().getSourcePositions());
        v.scan(ctx.getPath(), null);
        
        int count = v.getLineCount();
        int treshold = ctx.getPreferences().getInt(OPTION_LINES_LIMIT, DEFAULT_LINES_LIMIT);
        if (count > treshold) {
            return ErrorDescriptionFactory.forName(ctx, t, 
                    methodOrConstructor(ctx) ?
                    TEXT_ConstructorTooLongLines(count) :
                    TEXT_MethodTooLongLines(method.getName().toString(), count)
            );
        }
        count = v.getStatementCount();
        treshold = ctx.getPreferences().getInt(OPTION_STATEMENTS_LIMIT, DEFAULT_STATEMENTS_LIMIT);
        if (count > treshold) {
            return ErrorDescriptionFactory.forName(ctx, t, 
                    methodOrConstructor(ctx) ?
                    TEXT_ConstructorTooLongStatements(count) :
                    TEXT_MethodTooLongStatements(method.getName().toString(), count)
            );
        } else {
            return null;
        }
    }
    
    @Hint(
        category = "metrics",
        displayName = "#DN_MethodTooManyExceptions",
        description = "#DESC_MethodTooManyExceptions",
        options = { Hint.Options.QUERY, Hint.Options.HEAVY },
        enabled = false
    )
    @UseOptions(value = { OPTION_EXCEPTIONS_LIMIT })
    @TriggerPatterns({
        @TriggerPattern("$modifiers$ <$typeParams$> $returnType $name($args$) throws $thrown1, $thrown2$ { $body$; }"),
        @TriggerPattern("$modifiers$ <$typeParams$> $name($args$) throws $thrown1, $thrown2$ { $body$; }"),
    })
    public static ErrorDescription tooManyExceptions(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        Collection<? extends TreePath> exc2 = ctx.getMultiVariables().get("$thrown2$"); // NOI18N
        
        int limit = ctx.getPreferences().getInt(OPTION_EXCEPTIONS_LIMIT, DEFAULT_EXCEPTIONS_LIMIT);
        int count = exc2 == null ? 1 :exc2.size() + 1;
        if (count <= limit) {
            return null;
        }
        Element el = ctx.getInfo().getTrees().getElement(ctx.getPath());
        return ErrorDescriptionFactory.forName(ctx, t,
                methodOrConstructor(ctx) ?
                TEXT_ConstructorTooManyExceptions(count) :
                TEXT_MethodTooManyExceptions(method.getName().toString(), count)
        );
    }

    @Hint(
        category = "metrics",
        displayName = "#DN_MethodTooManyParameters",
        description = "#DESC_MethodTooManyParameters",
        options = { Hint.Options.QUERY, Hint.Options.HEAVY },
        enabled = false
    )
    @UseOptions(value = { OPTION_METHOD_PARAMETERS_LIMIT })
    @TriggerPattern("$modifiers$ <$typeParams$> $returnType $name($args1, $arg2, $args$) throws $whatever$ { $body$; }")
    public static ErrorDescription tooManyParameters(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        
        Collection<? extends TreePath> args = ctx.getMultiVariables().get("$args$"); // NOI18N
        int limit = ctx.getPreferences().getInt(OPTION_METHOD_PARAMETERS_LIMIT, DEFAULT_METHOD_PARAMETERS_LIMIT);
        int count = args.size() + 2;
        if (count <= limit) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, t, 
                methodOrConstructor(ctx) ?
                TEXT_ConstructorTooManyParameters(count) :
                TEXT_MethodTooManyParameters(method.getName().toString(), count)
        );
    }
    
    @Hint(
        category = "metrics",
        displayName = "#DN_CtorTooManyParameters",
        description = "#DESC_CtorTooManyParameters",
        options = { Hint.Options.QUERY, Hint.Options.HEAVY },
        enabled = false
    )
    @UseOptions(value = { OPTION_METHOD_PARAMETERS_LIMIT })
    @TriggerPattern("$modifiers$ <$typeParams$> $name($args1, $arg2, $args$) throws $whatever$ { $body$; }")
    public static ErrorDescription tooManyParametersCtor(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        
        Collection<? extends TreePath> args = ctx.getMultiVariables().get("$args$"); // NOI18N
        int limit = ctx.getPreferences().getInt(OPTION_METHOD_PARAMETERS_LIMIT, DEFAULT_METHOD_PARAMETERS_LIMIT);
        int count = args.size() + 2;
        if (count <= limit) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, t, 
                TEXT_MethodTooManyParameters(method.getName().toString(), count)
        );
    }
    
    @Hint(
        category = "metrics",
        displayName = "#DN_MethodMultipleReturns",
        description = "#DESC_MethodMultipleReturns",
        options = { Hint.Options.QUERY, Hint.Options.HEAVY },
        enabled = false
    )
    @UseOptions({ OPTION_RETURN_LIMIT, OPTION_RETURN_IGNORE_EQUALS, OPTION_RETURN_IGNORE_GUARDS })
    @TriggerTreeKind(Tree.Kind.METHOD)
    public static ErrorDescription multipleReturnPoints(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        
        boolean ignoreEquals = ctx.getPreferences().getBoolean(OPTION_RETURN_IGNORE_EQUALS, DEFAULT_IGNORE_EQUALS);
        if (ignoreEquals && method.getName().contentEquals("equals")) { // NOI18N
            return null;
        }
        
        ReturnCountVisitor v = new ReturnCountVisitor(
            ctx.getPreferences().getBoolean(OPTION_RETURN_IGNORE_GUARDS, DEFAULT_IGNORE_RETURN_GUARDS)
        );
        v.scan(ctx.getPath(), null);
        
        int count = v.getReturnCount();
        int limit = ctx.getPreferences().getInt(OPTION_RETURN_LIMIT, DEFAULT_RETURN_LIMIT);
        
        if (count > limit) {
            return ErrorDescriptionFactory.forName(ctx, t, 
                    TEXT_MethodMultipleReturns(method.getName().toString(), count)
            );
        } else {
            return null;
        }
    }
    
    /**
     * The visitor will ignore returns, which are the *sole* statement in a if-branch.
     * Such branches are considered to be guards, which abort further processing.
     */
    private static class ReturnCountVisitor extends ErrorAwareTreePathScanner {
        /**
         * Suppressed in local classes
         */
        private boolean suppress;
        
        private int returnCount;
        
        /**
         * If true, ignores guard returns
         */
        private final boolean ignoreGuards;

        public ReturnCountVisitor(boolean ignoreGuards) {
            this.ignoreGuards = ignoreGuards;
        }
        
        public int getReturnCount() {
            return returnCount;
        }
        
        @Override
        public Object visitClass(ClassTree node, Object p) {
            boolean s = this.suppress;
            this.suppress = true;
            Object o = super.visitClass(node, p); 
            this.suppress = s;
            return o;
        }

        @Override
        public Object visitReturn(ReturnTree node, Object p) {
            TreePath path = getCurrentPath();
            TreePath parentPath = path.getParentPath();
            if (suppress) {
                return  super.visitReturn(node, p);
            }
            if (ignoreGuards && parentPath != null) {
                Tree parentTree = parentPath.getLeaf();
                TreePath branchPath = path;
                while (parentTree.getKind() == Tree.Kind.BLOCK) {
                    branchPath = parentPath;
                    parentPath = parentPath.getParentPath();
                    parentTree = parentPath.getLeaf();
                }
                if (parentTree.getKind() == Tree.Kind.IF) {
                    IfTree ifTree = (IfTree)parentTree;
                    StatementTree trueTree = ifTree.getThenStatement() == branchPath.getLeaf() ? 
                            ifTree.getThenStatement() : ifTree.getElseStatement();
                    if (trueTree == node) {
                        return  super.visitReturn(node, p);
                    }
                    if (trueTree.getKind() == Tree.Kind.BLOCK) {
                        BlockTree bt = (BlockTree)trueTree;
                        if (bt.getStatements().size() == 1) {
                            return  super.visitReturn(node, p);
                        }
                    }
                }
            }
            returnCount++;
            return super.visitReturn(node, p);
        }
    }
    
    @Hint(
        category = "metrics",
        displayName = "#DN_MethodMultipleNegations",
        description = "#DESC_MethodMultipleNegations",
        options = { Hint.Options.QUERY, Hint.Options.HEAVY },
        enabled = false
    )
    @TriggerTreeKind(Tree.Kind.METHOD)
    @UseOptions(value = { OPTION_NEGATIONS_IGNORE_ASSERT, OPTION_NEGATIONS_IGNORE_EQUALS, OPTION_NEGATIONS_LIMIT })
    public static ErrorDescription multipleNegations(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        
        boolean ignoreEquals = ctx.getPreferences().getBoolean(OPTION_NEGATIONS_IGNORE_EQUALS, DEFAULT_IGNORE_EQUALS);
        if (ignoreEquals && method.getName().contentEquals("equals")) { // NOI18N
            return null;
        }
        boolean ignoreAsserts = ctx.getPreferences().getBoolean(OPTION_NEGATIONS_IGNORE_ASSERT, DEFAULT_NEGATIONS_IGNORE_ASSERT);
        
        NegationsVisitor v = new NegationsVisitor(ignoreAsserts);
        v.scan(ctx.getPath(), null);
        int limit = ctx.getPreferences().getInt(OPTION_NEGATIONS_LIMIT, DEFAULT_NEGATIONS_LIMIT);
        int count = v.getNegationsCount();
        if (count > limit) {
            return ErrorDescriptionFactory.forName(ctx, t, 
                    TEXT_MethodMultipleNegations(method.getName().toString(), count)
            );
        } else {
            return null;
        }
    }
    
    /**
     * Counts number of 'negations' in a Tree. A negation is either unary ! or binary != inequality
     * operator.
     */
    private static class NegationsVisitor extends ErrorAwareTreePathScanner {
        private int negationsCount;
        private final boolean ignoreAsserts;

        public NegationsVisitor(boolean ignoreAsserts) {
            this.ignoreAsserts = ignoreAsserts;
        }
        
        public int getNegationsCount() {
            return negationsCount;
        }

        @Override
        public Object visitUnary(UnaryTree node, Object p) {
            if (node.getKind() == Tree.Kind.LOGICAL_COMPLEMENT) {
                negationsCount++;
            }
            return super.visitUnary(node, p);
        }

        @Override
        public Object visitBinary(BinaryTree node, Object p) {
            if (node.getKind() == Tree.Kind.NOT_EQUAL_TO) {
                negationsCount++;
            }
            return super.visitBinary(node, p);
        }

        @Override
        public Object visitAssert(AssertTree node, Object p) {
            int saveCount = negationsCount;
            Object o = super.visitAssert(node, p);
            if (ignoreAsserts) {
                this.negationsCount = saveCount;
            }
            return o;
        }
    }
    
    /**
     * Utility scanner class, which counts all kinds of loops in the scanned subtree 
     */
    private static class LoopFinder extends ErrorAwareTreePathScanner {
        private int loopCount;

        public int getLoopCount() {
            return loopCount;
        }

        @Override
        public Object visitClass(ClassTree node, Object p) {
            int save = loopCount;
            Object o = super.visitClass(node, p);
            this.loopCount = save;
            return o;
        }

        @Override
        public Object visitDoWhileLoop(DoWhileLoopTree node, Object p) {
            loopCount++;
            return super.visitDoWhileLoop(node, p);
        }

        @Override
        public Object visitWhileLoop(WhileLoopTree node, Object p) {
            loopCount++;
            return super.visitWhileLoop(node, p);
        }

        @Override
        public Object visitForLoop(ForLoopTree node, Object p) {
            loopCount++;
            return super.visitForLoop(node, p);
        }

        @Override
        public Object visitEnhancedForLoop(EnhancedForLoopTree node, Object p) {
            loopCount++;
            return super.visitEnhancedForLoop(node, p);
        }
        
        
    }

    @Hint(
        category = "metrics",
        displayName = "#DN_MethodMultipleLoops",
        description = "#DESC_MethodMultipleLoops",
        options = { Hint.Options.QUERY, Hint.Options.HEAVY },
        enabled = false
    )
    @TriggerTreeKind(Tree.Kind.METHOD)
    @UseOptions({ OPTION_LOOPS_LIMIT })
    public static ErrorDescription multipleLoops(HintContext ctx) {
        Tree t = ctx.getPath().getLeaf();
        MethodTree method = (MethodTree)t;
        
        LoopFinder v = new LoopFinder();
        v.scan(ctx.getPath(), null);
        int count = v.getLoopCount();
        int limit = ctx.getPreferences().getInt(OPTION_LOOPS_LIMIT, DEFAULT_LOOPS_LIMIT);
        if (count > limit) {
            return ErrorDescriptionFactory.forName(ctx, t, 
                    TEXT_MethodMultipleLoops(method.getName().toString(), count)
            );
        } else {
            return null;
        }
    }
    
    @Hint(
        category = "metrics",
        displayName = "#DN_MethodCoupled",
        description = "#DESC_MethodCoupled",
        options = { Hint.Options.QUERY, Hint.Options.HEAVY },
        enabled = false
    )
    @TriggerTreeKind(Tree.Kind.METHOD)
    @UseOptions({ OPTION_COUPLING_LIMIT, OPTION_COUPLING_IGNORE_JAVA })
    public static ErrorDescription tooManyDependencies(HintContext ctx) {
        MethodTree m = (MethodTree)ctx.getPath().getLeaf();
        boolean ignoreJava = ctx.getPreferences().getBoolean(OPTION_COUPLING_IGNORE_JAVA, DEFAULT_COUPLING_IGNORE_JAVA);
        TypeElement outermost = ctx.getInfo().getElementUtilities().outermostTypeElement(ctx.getInfo().getTrees().getElement(ctx.getPath()));
        
        DependencyCollector col = new DependencyCollector(ctx.getInfo());
        col.setIgnoreJavaLibraries(ignoreJava);
        col.setOutermostClass(outermost);
        
        /*
         left for the case that superclass references should be excluded optionally
        ExecutableElement el = (ExecutableElement)ctx.getInfo().getTrees().getElement(ctx.getPath());
        Element parent = el.getEnclosingElement();
        while (parent != null && 
               (parent.getKind() == ElementKind.INTERFACE || parent.getKind() == ElementKind.CLASS || parent.getKind() == ElementKind.ENUM)) {
            
            Element p = parent;
            while (true) {
                TypeElement tel = (TypeElement)p;
                col.addIgnoredQName(tel.getQualifiedName());
                TypeMirror tm = tel.getSuperclass();
                if (tm.getKind() == TypeKind.DECLARED) {
                    p = ctx.getInfo().getTypes().asElement(tm);
                } else {
                    break;
                }
            } 
            parent = parent.getEnclosingElement();
        }*/
        
        col.scan(ctx.getPath(), null);
        
        int deps = col.getSeenQNames().size();
        int limit = ctx.getPreferences().getInt(OPTION_COUPLING_LIMIT, DEFAULT_COUPLING_LIMIT);
        if (deps > limit) {
            return ErrorDescriptionFactory.forName(ctx, m, TEXT_MethodTooCoupled(m.getName().toString(), deps));
        } else {
            return null;
        }
    }
}
