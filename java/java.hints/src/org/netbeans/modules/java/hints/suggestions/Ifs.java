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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.Collections;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyle.BracesGenerationStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.util.NbBundle.Messages;

@Messages({
    "DN_InvertIf=Invert If",
    "DESC_InvertIf=Will invert an if statement; negate the condition and switch the statements from the then and else sections.",
    "DN_JoinElseIf=Join consecutive ifs into if-else",
    "DESC_JoinElseIf=Converts cases like <code>if - else { if }</code> info <code>if - else if</code>",
    "DN_ToOrIf=Join if conditions using ||",
    "DESC_ToOrIf=Converts cases like <code>if (cond1) statement; else if (cond2) statement;</code> into <code>if (cond1 || cond2) statement;</code>",
    "DN_org.netbeans.modules.java.hints.suggestions.Tiny.mergeIfs=Combine nested if statements",
    "DESC_org.netbeans.modules.java.hints.suggestions.Tiny.mergeIfs=Combines two nested if statements, like <code>if (cond1) if (cond2) statement;</code>, into one if statement, like <code>if (cond1 && cond2) statement;</code>.",
    "ERR_org.netbeans.modules.java.hints.suggestions.Tiny.mergeIfs=",
    "FIX_org.netbeans.modules.java.hints.suggestions.Tiny.mergeIfs=Combine nested ifs",
    "DN_org.netbeans.modules.java.hints.suggestions.Tiny.extractIf=Split if statement condition",
    "DESC_org.netbeans.modules.java.hints.suggestions.Tiny.extractIf=Splits an if statement with a complex condition, like <code>if (cond1 || cond2) statement;</code>, into two if statements, like <code>if (cond1) statement; else if (cond2) statement;</code>.",
    "ERR_org.netbeans.modules.java.hints.suggestions.Tiny.extractIf=",
    "FIX_org.netbeans.modules.java.hints.suggestions.Tiny.extractIf=Split if into two ifs",
    "DN_splitIfCondition=Split if condition",
    "DESC_splitIfCondition=Splits if whose condition is || into two ifs",
    "ERR_splitIfCondition=",
    "FIX_splitIfCondition=Split if into two ifs",
    "LBL_InvertIfShowWhenElseMissing=Show when else branch is missing",
    "DESC_InvertifShowWhenElseMissing=Shows the suggestion even though else part of the statement is missing. The hint can be used to aid code rewrite in such case."
})
public class Ifs {

    static final boolean SHOW_ELSE_MISSING_DEFAULT = true;

    @BooleanOption(displayName = "#LBL_InvertIfShowWhenElseMissing", 
            tooltip = "#DESC_InvertifShowWhenElseMissing",
            defaultValue=SHOW_ELSE_MISSING_DEFAULT)
    static final String SHOW_ELSE_MISSING = "show.noelse";

    @Hint(id="org.netbeans.modules.java.hints.suggestions.InvertIf", displayName = "#DN_InvertIf", description = "#DESC_InvertIf", category = "suggestions", hintKind= Hint.Kind.ACTION)
    @UseOptions(SHOW_ELSE_MISSING)
    @TriggerPattern(value = "if ($cond) $then; else $else$;")
    @Messages({"ERR_InvertIf=Invert If",
               "FIX_InvertIf=Invert If"})
    public static ErrorDescription computeWarning(HintContext ctx) {
        TreePath cond = ctx.getVariables().get("$cond");
        long conditionEnd = ctx.getInfo().getTrees().getSourcePositions().getEndPosition(cond.getCompilationUnit(), cond.getParentPath().getLeaf());
        if (ctx.getCaretLocation() > conditionEnd) return null;

        // parenthesized, then if
        TreePath ifPath = cond.getParentPath().getParentPath();
        if (ifPath.getLeaf().getKind() != Tree.Kind.IF) {
            return null;
        }
        IfTree iv = (IfTree)ifPath.getLeaf();
        if (iv.getElseStatement() == null && 
            !ctx.getPreferences().getBoolean(SHOW_ELSE_MISSING, SHOW_ELSE_MISSING_DEFAULT)) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_InvertIf(), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }
    
    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_InvertIf();
        }

        @Override
        protected void performRewrite(final TransformationContext ctx) throws Exception {
            IfTree toRewrite = (IfTree) ctx.getPath().getLeaf();
            StatementTree elseStatement = toRewrite.getElseStatement();
            if (toRewrite.getCondition() == null ||
                toRewrite.getCondition().getKind() != Tree.Kind.PARENTHESIZED) {
                return;
            }
            ParenthesizedTree ptt = (ParenthesizedTree)toRewrite.getCondition();
            if (ptt.getExpression() == null) {
                return;
            }
            if (elseStatement == null) elseStatement = ctx.getWorkingCopy().getTreeMaker().Block(Collections.<StatementTree>emptyList(), false);
            
            ctx.getWorkingCopy().rewrite(toRewrite, ctx.getWorkingCopy().getTreeMaker().If(toRewrite.getCondition(), elseStatement, toRewrite.getThenStatement()));
            ExpressionTree negated = Utilities.negate(
                    ctx.getWorkingCopy().getTreeMaker(), ptt.getExpression(), ptt);
            ctx.getWorkingCopy().rewrite(ptt.getExpression(), negated);
        }
        
        //TODO: should be done automatically:
        private void negate(WorkingCopy copy, ExpressionTree original, Tree parent) {
            TreeMaker make = copy.getTreeMaker();
            ExpressionTree newTree;
            switch (original.getKind()) {
                case PARENTHESIZED:
                    ExpressionTree expr = ((ParenthesizedTree) original).getExpression();
                    negate(copy, expr, original);
                    return ;
                case LOGICAL_COMPLEMENT:
                    newTree = ((UnaryTree) original).getExpression();
                    while (newTree.getKind() == Kind.PARENTHESIZED && !JavaFixUtilities.requiresParenthesis(((ParenthesizedTree) newTree).getExpression(), original, parent)) {
                        newTree = ((ParenthesizedTree) newTree).getExpression();
        }
                    break;
                case NOT_EQUAL_TO:
                    newTree = negateBinaryOperator(copy, original, Kind.EQUAL_TO, false);
                    break;
                case EQUAL_TO:
                    newTree = negateBinaryOperator(copy, original, Kind.NOT_EQUAL_TO, false);
                    break;
                case BOOLEAN_LITERAL:
                    newTree = make.Literal(!(Boolean) ((LiteralTree) original).getValue());
                    break;
                case CONDITIONAL_AND:
                    newTree = negateBinaryOperator(copy, original, Kind.CONDITIONAL_OR, true);
                    break;
                case CONDITIONAL_OR:
                    newTree = negateBinaryOperator(copy, original, Kind.CONDITIONAL_AND, true);
                    break;
                case LESS_THAN:
                    newTree = negateBinaryOperator(copy, original, Kind.GREATER_THAN_EQUAL, false);
                    break;
                case LESS_THAN_EQUAL:
                    newTree = negateBinaryOperator(copy, original, Kind.GREATER_THAN, false);
                    break;
                case GREATER_THAN:
                    newTree = negateBinaryOperator(copy, original, Kind.LESS_THAN_EQUAL, false);
                    break;
                case GREATER_THAN_EQUAL:
                    newTree = negateBinaryOperator(copy, original, Kind.LESS_THAN, false);
                    break;
                default:
                    newTree = make.Unary(Kind.LOGICAL_COMPLEMENT, original);
                    if (JavaFixUtilities.requiresParenthesis(original, original, newTree)) {
                        newTree = make.Unary(Kind.LOGICAL_COMPLEMENT, make.Parenthesized(original));
    }
                    break;
            }

            if (JavaFixUtilities.requiresParenthesis(newTree, original, parent)) {
                newTree = make.Parenthesized(newTree);
            }
            
            copy.rewrite(original, newTree);
        }
        
        private ExpressionTree negateBinaryOperator(WorkingCopy copy, Tree original, Kind newKind, boolean negateOperands) {
            BinaryTree bt = (BinaryTree) original;
            if (negateOperands) {
                negate(copy, bt.getLeftOperand(), original);
                negate(copy, bt.getRightOperand(), original);
            }
            return copy.getTreeMaker().Binary(newKind, bt.getLeftOperand(), bt.getRightOperand());
        }
        
    }

    //TODO: does not produce correctly formatted output (change testJoinIfs1 from assertOutput to assertVerbatimOutput):
    @Hint(displayName="#DN_JoinElseIf", description="#DESC_JoinElseIf", category="suggestions", hintKind=Hint.Kind.ACTION)
    @TriggerPattern("if ($cond1) $then1; else { if ($cond2) $then2; else $else$; }")
    @Messages({"ERR_JoinElseIf=",
               "FIX_JoinElseIf=Join nested if into the enclosing if"})
    public static ErrorDescription joinElseIf(HintContext ctx) {
        IfTree it = (IfTree) ctx.getPath().getLeaf();
        if (it.getElseStatement().getKind() != Kind.BLOCK) return null;
        if (!caretInsideToLevelElseKeyword(ctx)) return null;
        return ErrorDescriptionFactory.forSpan(ctx, ctx.getCaretLocation(), ctx.getCaretLocation(), Bundle.ERR_JoinElseIf(), new JoinIfFix(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }

    private static class JoinIfFix extends JavaFix {

        public JoinIfFix(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override protected String getText() {
            return Bundle.FIX_JoinElseIf();
        }

        @Override protected void performRewrite(TransformationContext ctx) throws Exception {
            IfTree it = (IfTree) ctx.getPath().getLeaf();
            ctx.getWorkingCopy().rewrite(it.getElseStatement(), ((BlockTree) it.getElseStatement()).getStatements().get(0));
        }
    }

    @Hint(displayName="#DN_ToOrIf", description="#DESC_ToOrIf", category="suggestions", hintKind=Hint.Kind.ACTION)
    @TriggerPattern("if ($cond1) $then; else if ($cond2) $then; else $else$;")
    @Messages({"ERR_ToOrIf=",
               "FIX_ToOrIf=Join ifs using ||"})
    public static ErrorDescription toOrIf(HintContext ctx) {
        SourcePositions sp = ctx.getInfo().getTrees().getSourcePositions();
        CompilationUnitTree cut = ctx.getInfo().getCompilationUnit();
        boolean caretAccepted = ctx.getCaretLocation() <= sp.getStartPosition(cut, ctx.getPath().getLeaf()) + 2 || caretInsideToLevelElseKeyword(ctx);
        if (!caretAccepted) return null;
        return ErrorDescriptionFactory.forSpan(ctx, ctx.getCaretLocation(), ctx.getCaretLocation(), Bundle.ERR_ToOrIf(), JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_ToOrIf(), ctx.getPath(), "if ($cond1 || $cond2) $then; else $else$;"));
    }
    
    @Hint(displayName="#DN_splitIfCondition", description="#DESC_splitIfCondition", category="suggestions", hintKind=Hint.Kind.ACTION)
    @TriggerPattern("if ($cond1 || $cond2) $then; else $else$;")
    public static ErrorDescription splitIfCondition(HintContext ctx) {
        SourcePositions sp = ctx.getInfo().getTrees().getSourcePositions();
        CompilationUnitTree cut = ctx.getInfo().getCompilationUnit();
        if (!caretInsidePreviousToken(ctx, sp.getStartPosition(cut, ctx.getVariables().get("$cond2").getLeaf()), JavaTokenId.BARBAR)) return null;
        String target = "if ($cond1) $then; else if ($cond2) $then; else $else$;";
        return ErrorDescriptionFactory.forSpan(ctx, ctx.getCaretLocation(), ctx.getCaretLocation(), Bundle.ERR_splitIfCondition(), JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_splitIfCondition(), ctx.getPath(), target));
    }
    
    private static boolean caretInsideToLevelElseKeyword(HintContext ctx) {
        IfTree it = (IfTree) ctx.getPath().getLeaf();
        SourcePositions sp = ctx.getInfo().getTrees().getSourcePositions();
        CompilationUnitTree cut = ctx.getInfo().getCompilationUnit();
        int elsePos = (int) sp.getStartPosition(cut, it.getElseStatement());
        
        
        return caretInsidePreviousToken(ctx, elsePos, JavaTokenId.ELSE);
    }
    
    private static boolean caretInsidePreviousToken(HintContext ctx, long startAt, JavaTokenId lookFor) {
        if (startAt < 0) {
            return false;
        }
        
        TokenSequence<JavaTokenId> ts = ctx.getInfo().getTokenHierarchy().tokenSequence(JavaTokenId.language());
        
        ts.move((int) startAt);
        
        while (ts.movePrevious()) {
            if (ts.token().id() == lookFor) {
                int start = ts.offset();
                int end = ts.offset() + ts.token().length();

                return ctx.getCaretLocation() >= start && ctx.getCaretLocation() <= end;
            }
        }
        
        return false;
    }

    @Hint(id="org.netbeans.modules.java.hints.suggestions.Tiny.mergeIfs", displayName = "#DN_org.netbeans.modules.java.hints.suggestions.Tiny.mergeIfs", description = "#DESC_org.netbeans.modules.java.hints.suggestions.Tiny.mergeIfs", category="suggestions", hintKind=org.netbeans.spi.java.hints.Hint.Kind.ACTION, severity=Severity.HINT)
    @TriggerPattern(value="if ($firstCondition) if ($secondCondition) $body;")
    public static ErrorDescription mergeIfs(HintContext ctx) {
        int caret = ctx.getCaretLocation();
        IfTree st = (IfTree) ctx.getPath().getLeaf();
        int conditionEnd = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getPath().getCompilationUnit(), st.getCondition());
        
        if (caret > conditionEnd) return null;
        
        Fix f = JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_org_netbeans_modules_java_hints_suggestions_Tiny_mergeIfs(), ctx.getPath(), "if ($firstCondition && $secondCondition) $body;");
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_mergeIfs(), f);
    }
    
    @Hint(id="org.netbeans.modules.java.hints.suggestions.Tiny.extractIf", displayName = "#DN_org.netbeans.modules.java.hints.suggestions.Tiny.extractIf", description = "#DESC_org.netbeans.modules.java.hints.suggestions.Tiny.extractIf", category="suggestions", hintKind=org.netbeans.spi.java.hints.Hint.Kind.ACTION, severity=Severity.HINT)
    @TriggerPattern(value="if ($firstCondition && $secondCondition) $body;")
    public static ErrorDescription extractIf(HintContext ctx) {
        int caret = ctx.getCaretLocation();
        boolean braces = CodeStyle.getDefault(ctx.getInfo().getFileObject()).redundantIfBraces() != BracesGenerationStyle.ELIMINATE;
        TreePath toSplit = null;
        TreePath left = ctx.getVariables().get("$firstCondition"); // NOI18N
        long leftStart = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getPath().getCompilationUnit(), left.getLeaf());
        long leftEnd   = ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getPath().getCompilationUnit(), left.getLeaf());

        if (leftStart <= caret && caret <= leftEnd) {
            toSplit = left;
        }

        TreePath right = ctx.getVariables().get("$secondCondition"); // NOI18N
        
        if (toSplit == null) {
            long rightStart = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getPath().getCompilationUnit(), right.getLeaf());
            long rightEnd   = ctx.getInfo().getTrees().getSourcePositions().getEndPosition(ctx.getPath().getCompilationUnit(), right.getLeaf());

            if (rightStart > caret || caret > rightEnd) {
                return null;
            }
        }
        
        ctx.getVariables().put("$firstCondition", unwrapParenthesized(left)); // NOI18N
        ctx.getVariables().put("$secondCondition", unwrapParenthesized(right)); // NOI18N

        String targetPattern = braces ? "if ($firstCondition) { if ($secondCondition) $body; }" : "if ($firstCondition) if ($secondCondition) $body;"; // NOI18N
        Fix f = JavaFixUtilities.rewriteFix(ctx, Bundle.FIX_org_netbeans_modules_java_hints_suggestions_Tiny_extractIf(), ctx.getPath(), targetPattern);
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_extractIf(), f);
    }
    
    private static TreePath unwrapParenthesized(TreePath x) {
        while (x.getLeaf().getKind() == Tree.Kind.PARENTHESIZED) {
            x = new TreePath(x, ((ParenthesizedTree)x.getLeaf()).getExpression());
        }
        return x;
    }

}
