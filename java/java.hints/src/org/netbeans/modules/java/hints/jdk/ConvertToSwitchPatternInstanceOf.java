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
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author aksinsin
 */
@NbBundle.Messages({
    "DN_ConvertToSwitchPatternInstanceOf=Convert to switch pattern matching <pattern>",
    "DESC_ConvertToSwitchPatternInstanceOf=Convert to switch pattern matching <pattern>",
    "ERR_ConvertToSwitchPatternInstanceOf=switch pattern matching <pattern> can be used here",
    "FIX_ConvertToSwitchPatternInstanceOf=Use switch pattern matching <pattern>"
})
@Hint(displayName="#DN_ConvertToSwitchPatternInstanceOf", description="#DESC_ConvertToSwitchPatternInstanceOf", category="rules15",
        minSourceVersion = "17")
public class ConvertToSwitchPatternInstanceOf {

    @TriggerPatterns({
        @TriggerPattern(value = "if ($expr0 instanceof $typeI0) { $statements0$;} else if ($expr1 instanceof $typeI1) { $statements1$;} else $else$;")
    })
    public static ErrorDescription trivial(HintContext ctx) {
        TreePath parent = ctx.getPath().getParentPath();
        if (parent.getLeaf().getKind() == Tree.Kind.IF) {
            return null;
        }
        Tree ifPath = ctx.getPath().getLeaf();
        String expr0 = null;
        expr0 = ctx.getVariables().get("$expr0").getLeaf().toString();
        int matchVarIndex = 1;
        while (ifPath != null && ifPath.getKind() == Tree.Kind.IF) {
            matchVarIndex++;
            IfTree it = (IfTree) ifPath;
            if (MatcherUtilities.matches(ctx, new TreePath(ctx.getPath(), it.getCondition()), "($expr" + matchVarIndex + " instanceof $typeI" + matchVarIndex + ")", true)
                    && MatcherUtilities.matches(ctx, new TreePath(ctx.getPath(), it.getThenStatement()), "{ $typeV" + matchVarIndex + " $var" + matchVarIndex + " = ($typeC" + matchVarIndex + ") $expr" + matchVarIndex + "; $other" + matchVarIndex + "$;}", true)) {
                if (!ctx.getVariables().get("$expr" + matchVarIndex).getLeaf().toString().equals(expr0)) {
                    return null;
                }
                for (TreePath tp : ctx.getMultiVariables().get("$other" + matchVarIndex + "$")) {
                    if (tp.getLeaf().getKind() == Tree.Kind.BREAK || tp.getLeaf().getKind() == Tree.Kind.CONTINUE) {
                        return null;
                    }
                }
                TypeMirror typeI = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$typeI" + matchVarIndex + ""));
                TypeMirror typeC = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$typeC" + matchVarIndex + ""));
                if (!ctx.getInfo().getTypes().isSameType(typeI, typeC)) {
                    System.err.println("different types (" + typeI + ", " + typeC + ") in " + ctx.getInfo().getFileObject());
                    return null;
                }
            } else {
                return null;
            }
            ifPath = it.getElseStatement();
        }
        if (ifPath != null && ifPath.getKind() == Tree.Kind.BLOCK) {
            Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), true, Collections.emptySet()).toEditorFix();
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToSwitchPatternInstanceOf(), fix);
        } else {
            return null;
        }

    }

    private static final class FixImpl extends JavaFix {

        private final boolean removeFirst;

        public FixImpl(CompilationInfo info, TreePath main, boolean removeFirst, Set<TreePath> replaceOccurrences) {
            super(info, main);
            this.removeFirst = removeFirst;
        }

        @Override
        protected String getText() {
            return Bundle.FIX_ConvertToSwitchPatternInstanceOf();
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath main = ctx.getPath();
            List<CaseTree> ctl = new LinkedList<>();

            Tree ifPath = ctx.getPath().getLeaf();
            int matchVarIndex = 1;
            List<IfTree> ifTrees = new ArrayList<>();
            while (ifPath != null && ifPath.getKind() == Tree.Kind.IF) {
                matchVarIndex++;
                IfTree it = (IfTree) ifPath;
                ifTrees.add(it);
                ifPath = it.getElseStatement();
            }

            InstanceOfTree iot = null;
            for (IfTree ifTree : ifTrees) {
                List<Tree> caseBindPattern = new LinkedList<>();
                iot = (InstanceOfTree) ((ParenthesizedTree) ifTree.getCondition()).getExpression();
                StatementTree bt = ifTree.getThenStatement();
                VariableTree var = (VariableTree) ((BlockTree) bt).getStatements().get(0);
                StatementTree thenBlock = removeFirst ? wc.getTreeMaker().removeBlockStatement((BlockTree) bt, 0) : bt;
                caseBindPattern.add(wc.getTreeMaker().BindingPattern(wc.getTreeMaker().Variable(wc.getTreeMaker().Modifiers(EnumSet.noneOf(Modifier.class)), var.getName().toString(), iot.getType(), null)));
                BlockTree blockTree = (BlockTree) thenBlock;
                Tree statementTree = blockTree.getStatements().size() == 1 && isValidCaseTree(blockTree.getStatements().get(0))? blockTree.getStatements().get(0) : thenBlock;                
                CaseTree caseMultipleSwitchPatterns = wc.getTreeMaker().CasePatterns(caseBindPattern, statementTree);
                ctl.add(caseMultipleSwitchPatterns);
            }
            BlockTree elseTree = (BlockTree) ifPath;
            if (elseTree == null) {
                elseTree = wc.getTreeMaker().Block(new ArrayList<>(), false);
            }
            
            Tree defaultTree = elseTree.getStatements().size() == 1 && isValidCaseTree(elseTree.getStatements().get(0))? elseTree.getStatements().get(0) : elseTree;
            CaseTree caseMultipleSwitchPatterns = wc.getTreeMaker().Case(Collections.emptyList(), defaultTree);
            ctl.add(caseMultipleSwitchPatterns);
            wc.rewrite((IfTree) main.getLeaf(), wc.getTreeMaker().Switch(iot.getExpression(), ctl));
        }

    }

    @TriggerPatterns({
        @TriggerPattern(value = "if ($expr0 instanceof $typeI0 $var0) { $statements0$;} else if ($expr1 instanceof $typeI1 $var1) { $statements1$;} else $else$;")
    })
    public static ErrorDescription patternMatchToSwitch(HintContext ctx) {
        TreePath parent = ctx.getPath().getParentPath();
        if (parent.getLeaf().getKind() == Tree.Kind.IF) {
            return null;
        }
        Tree ifPath = ctx.getPath().getLeaf();
        String expr0 = null;
        expr0 = ctx.getVariables().get("$expr0").getLeaf().toString();
        int matchVarIndex = 1;
        while (ifPath != null && ifPath.getKind() == Tree.Kind.IF) {
            matchVarIndex++;
            IfTree it = (IfTree) ifPath;
            if (MatcherUtilities.matches(ctx, new TreePath(ctx.getPath(), it.getCondition()), "($expr" + matchVarIndex + " instanceof $typeI" + matchVarIndex + " $var" + matchVarIndex + ")", true)
                    && MatcherUtilities.matches(ctx, new TreePath(ctx.getPath(), it.getThenStatement()), "{ $other" + matchVarIndex + "$;}", true)) {
                if (ctx.getMultiVariables().get("$other" + matchVarIndex + "$").isEmpty()) {
                    return null;
                }
                if (!(ctx.getVariables().get("$expr" + matchVarIndex).getLeaf().toString().equals(expr0))) {
                    return null;
                }
                for (TreePath tp : ctx.getMultiVariables().get("$other" + matchVarIndex + "$")) {
                    if (tp.getLeaf().getKind() == Tree.Kind.BREAK || tp.getLeaf().getKind() == Tree.Kind.CONTINUE) {
                        return null;
                    }
                }
            } else {
                return null;
            }
            ifPath = it.getElseStatement();
        }

        Fix fix = new FixPatternMatchToSwitch(ctx.getInfo(), ctx.getPath(), false, Collections.emptySet()).toEditorFix();
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToSwitchPatternInstanceOf(), fix);

    }

    private static final class FixPatternMatchToSwitch extends JavaFix {
        private final boolean removeFirst;

        public FixPatternMatchToSwitch(CompilationInfo info, TreePath main,  boolean removeFirst, Set<TreePath> replaceOccurrences) {
            super(info, main);
            this.removeFirst = removeFirst;
        }

        @Override
        protected String getText() {
            return Bundle.FIX_ConvertToSwitchPatternInstanceOf();
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath main = ctx.getPath();
            List<CaseTree> ctl = new LinkedList<>();
            InstanceOfTree iot = null;

            Tree ifPath = ctx.getPath().getLeaf();
            int matchVarIndex = 1;
            List<IfTree> ifTrees = new ArrayList<>();
            while (ifPath != null && ifPath.getKind() == Tree.Kind.IF) {
                matchVarIndex++;
                IfTree it = (IfTree) ifPath;
                ifTrees.add(it);
                ifPath = it.getElseStatement();
            }

            for (IfTree ifTree : ifTrees) {
                List<Tree> caseBindPattern = new LinkedList<>();
                iot = (InstanceOfTree) ((ParenthesizedTree) ifTree.getCondition()).getExpression();
                StatementTree bt = ifTree.getThenStatement();
                StatementTree thenBlock = removeFirst ? wc.getTreeMaker().removeBlockStatement((BlockTree) bt, 0) : bt;
                Tree pattern = iot.getPattern();
                caseBindPattern.add(pattern);
                BlockTree blockTree = (BlockTree) thenBlock;
                Tree statementTree = blockTree.getStatements().size() == 1 && isValidCaseTree(blockTree.getStatements().get(0))? blockTree.getStatements().get(0) : thenBlock;
                CaseTree caseMultipleSwitchPatterns = wc.getTreeMaker().CasePatterns(caseBindPattern, statementTree);
                ctl.add(caseMultipleSwitchPatterns);
            }
            BlockTree elseTree = (BlockTree) ifPath;
            if (elseTree == null) {
                elseTree = wc.getTreeMaker().Block(new ArrayList<>(), false);
            }

            Tree defaultTree = elseTree.getStatements().size() == 1 && isValidCaseTree(elseTree.getStatements().get(0))? elseTree.getStatements().get(0) : elseTree;
            CaseTree casePatterns = wc.getTreeMaker().Case(Collections.emptyList(), defaultTree);
            ctl.add(casePatterns);
            wc.rewrite((IfTree) main.getLeaf(), wc.getTreeMaker().Switch(iot.getExpression(), ctl));
        }

    }


    @TriggerTreeKind(Tree.Kind.SWITCH)
    public static ErrorDescription switchPatternMatchToSwitchNull(HintContext ctx) {

        SwitchTree switchTree = (SwitchTree) ctx.getPath().getLeaf();
        if (!isPatternMatch(switchTree)) {
            return null;
        }
        Tree expression = ((ParenthesizedTree) switchTree.getExpression()).getExpression();
        Tree parent = (Tree) ctx.getPath().getParentPath().getLeaf();
        int indexOf;
        if (parent instanceof BlockTree) {
            indexOf = ((BlockTree) parent).getStatements().indexOf(switchTree) - 1;
        } else {
            return null;
        }
        Tree ifTree = ((BlockTree) parent).getStatements().get(indexOf);
        if ((!(ifTree instanceof IfTree) || !MatcherUtilities.matches(ctx, new TreePath(ctx.getPath(), ((IfTree) ifTree).getCondition()), "($expr0 == null)", true))
                || !(ctx.getVariables().get("$expr0").getLeaf().toString().equals(expression.toString()))) {
            return null;
        }
        Fix fix = new FixSwitchPatternMatchToSwitchNull(ctx.getInfo(), ctx.getPath().getParentPath(), indexOf).toEditorFix();
        return ErrorDescriptionFactory.forTree(ctx, ifTree, Bundle.ERR_ConvertToSwitchPatternInstanceOf(), fix);

    }

    public static boolean isPatternMatch(Tree node) {
        try {
            return node.getClass().getField("patternSwitch").getBoolean(node);
        } catch (NoSuchFieldException e){
            return false;
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final class FixSwitchPatternMatchToSwitchNull extends JavaFix {

        private final int indexOf;

        public FixSwitchPatternMatchToSwitchNull(CompilationInfo info, TreePath path, int indexOf) {
            super(info, path);
            this.indexOf = indexOf;
        }

        @Override
        protected String getText() {
            return Bundle.FIX_ConvertToSwitchPatternInstanceOf();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath main = ctx.getPath();
            TreeMaker make = wc.getTreeMaker();
            List<Tree> caseNullLabel = new LinkedList<>();
            SwitchTree switchTree = (SwitchTree) ((BlockTree) main.getLeaf()).getStatements().get(indexOf + 1);

            Tree ifTree = ((BlockTree) main.getLeaf()).getStatements().get(indexOf);
            StatementTree thenStatement = ((IfTree) ifTree).getThenStatement();
            caseNullLabel.add(wc.getTreeMaker().Identifier("null"));
            BlockTree blockTree = (BlockTree)thenStatement;
            Tree statementTree = blockTree.getStatements().size() == 1 && isValidCaseTree(blockTree.getStatements().get(0))? blockTree.getStatements().get(0) : blockTree;
            CaseTree caseMultipleSwitchPatterns = wc.getTreeMaker().CasePatterns(caseNullLabel, statementTree);
            SwitchTree insertSwitchCase = make.insertSwitchCase(switchTree, 0, caseMultipleSwitchPatterns);
            wc.rewrite(switchTree, insertSwitchCase);
            BlockTree removeBlockStatement = make.removeBlockStatement((BlockTree) main.getLeaf(), indexOf);
            wc.rewrite(main.getLeaf(), removeBlockStatement);
        }
    }
    
    private static boolean isValidCaseTree(Tree tree){
        return ((tree instanceof BlockTree)
                || (tree instanceof ExpressionStatementTree)
                || (tree instanceof ThrowTree));
    }
}
