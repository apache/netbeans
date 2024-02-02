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
package org.netbeans.modules.java.hints.control;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({
    "DN_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn=Remove Unnecessary Return Statement",
    "DESC_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn=Remove Unnecessary Return Statement",
    "ERR_UnnecessaryReturnStatement=Unnecessary return statement",
    "FIX_UnnecessaryReturnStatement=Remove unnecessary return statement",
    "DN_RemoveUnnecessaryContinue=Remove Unnecessary Continue Statement",
    "DESC_RemoveUnnecessaryContinue=Remove Unnecessary Continue Statement",
    "ERR_UnnecessaryContinueStatement=Unnecessary continue statement",
    "FIX_UnnecessaryContinueStatement=Remove unnecessary continue statement",
    "DN_RemoveUnnecessaryContinueLabel=Remove Unnecessary Label in continue",
    "DESC_RemoveUnnecessaryContinueLabel=Remove Unnecessary Label in continue statement",
    "ERR_UnnecessaryContinueStatementLabel=Unnecessary label in continue",
    "FIX_UnnecessaryContinueStatementLabel=Remove unnecessary label from continue",
    "DN_RemoveUnnecessaryBreakLabel=Remove Unnecessary Label in break",
    "DESC_RemoveUnnecessaryBreakLabel=Remove Unnecessary Label in break statement",
    "ERR_UnnecessaryBreakStatementLabel=Unnecessary label in break",
    "FIX_UnnecessaryBreakStatementLabel=Remove unnecessary label from break"
})
public class RemoveUnnecessary {

    @Hint(id="org.netbeans.modules.java.hints.RemoveUnnecessaryReturn", displayName = "#DN_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn", description = "#DESC_org.netbeans.modules.java.hints.RemoveUnnecessaryReturn", category="general", suppressWarnings="UnnecessaryReturnStatement")
    @TriggerPattern("return $val$;")
    public static ErrorDescription unnecessaryReturn(HintContext ctx) {
        return unnecessaryReturnContinue(ctx, null, "UnnecessaryReturnStatement", true);
    }
    
    @Hint(displayName="#DN_RemoveUnnecessaryContinue", description="#DESC_RemoveUnnecessaryContinue", category="general", suppressWarnings="UnnecessaryContinue")
    @TriggerPattern("continue $val$;")
    public static ErrorDescription unnecessaryContinue(HintContext ctx) {
        return unnecessaryReturnContinue(ctx, ctx.getInfo().getTreeUtilities().getBreakContinueTargetTree(ctx.getPath()), "UnnecessaryContinueStatement", false);
    }
    
    private static ErrorDescription unnecessaryReturnContinue(HintContext ctx, Tree targetLoop, String key, boolean isReturn) {
        TreePath tp = ctx.getPath();

        OUTER: while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            Tree current = tp.getLeaf();
            List<? extends StatementTree> statements;

            tp = tp.getParentPath();

            switch (tp.getLeaf().getKind()) {
                case METHOD: {
                    if (targetLoop != null) return null; //TODO: unnecessary continue - can happen?
                    MethodTree mt = (MethodTree) tp.getLeaf();

                    if (mt.getReturnType() == null) {
                        if (mt.getName().contentEquals("<init>"))
                            break OUTER;//constructor
                        else
                            return null; //a method without a return type - better ignore
                    }
                    
                    TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(new TreePath(tp, mt.getReturnType()));

                    if (tm == null || tm.getKind() != TypeKind.VOID) return null;
                    break OUTER;
                }
                case LAMBDA_EXPRESSION: {
                    if (targetLoop != null) return null; //TODO: unnecessary continue - can happen?
                    TypeMirror functionalType = ctx.getInfo().getTrees().getTypeMirror(tp);
                    if (functionalType == null || functionalType.getKind() != TypeKind.DECLARED) return null; //unknown, ignore
                    ExecutableType descriptorType = ctx.getInfo().getTypeUtilities().getDescriptorType((DeclaredType) functionalType);
                    TypeMirror returnType = descriptorType != null ? descriptorType.getReturnType() : null;

                    if (returnType == null || returnType.getKind() != TypeKind.VOID) return null;
                    break OUTER;
                }
                case BLOCK: statements = ((BlockTree) tp.getLeaf()).getStatements(); break;
                case CASE: {
                    if (tp.getParentPath().getLeaf().getKind() == Kind.SWITCH) {
                        List<? extends CaseTree> cases = ((SwitchTree) tp.getParentPath().getLeaf()).getCases();
                        List<StatementTree> locStatements = new ArrayList<>();

                        for (int i = cases.indexOf(tp.getLeaf()); i < cases.size(); i++) {
                            List<? extends StatementTree> list = cases.get(i).getStatements();
                            if (list != null) {
                                locStatements.addAll(list);
                            }
                        }

                        statements = locStatements;
                    } else {
                        //???
                        statements = ((CaseTree) tp.getLeaf()).getStatements();
                    }
                    break;
                }
                case DO_WHILE_LOOP:
                case ENHANCED_FOR_LOOP:
                case FOR_LOOP:
                case WHILE_LOOP:
                    if (tp.getLeaf() != targetLoop) return null;
                    else break OUTER;
                case TRY:
                    if (((TryTree) tp.getLeaf()).getFinallyBlock() == current) return null;
                default: continue OUTER;
            }

            assert !statements.isEmpty();

            int i = statements.indexOf(current);

            if (i == (-1)) {
                //XXX: should not happen?
                return null;
            }

            while (i + 1 < statements.size()) {
                StatementTree next = statements.get(i + 1);

                if (next.getKind() == Kind.EMPTY_STATEMENT) {
                    i++;
                    continue;
                }

                if (next.getKind() == Kind.BLOCK) {
                    statements = ((BlockTree) next).getStatements();
                    i = -1;
                    continue;
                }

                if (next.getKind() == Kind.BREAK) {
                    Tree target = ctx.getInfo().getTreeUtilities().getBreakContinueTargetTree(new TreePath(tp, next));
                    
                    if (target == null) return null;
                    
                    tp = TreePath.getPath(ctx.getInfo().getCompilationUnit(), target);
                    continue OUTER;
                }

                return null;
            }
        }
        Fix toExpression = null;
        
        if (isReturn) {
            ExpressionToStatement scanner = new ExpressionToStatement(null, ctx.getInfo());
            scanner.scan(ctx.getPath(), null);
            if (!scanner.remove) {
                toExpression = new MakeExpressionStatement(ctx.getInfo(), ctx.getPath()).toEditorFix();
            }
        }

        String displayName = NbBundle.getMessage(RemoveUnnecessary.class, "ERR_" + key);
        String fixDisplayName = NbBundle.getMessage(RemoveUnnecessary.class, "FIX_" + key);
        
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, 
                JavaFixUtilities.removeFromParent(ctx, fixDisplayName, ctx.getPath()), toExpression);
    }

    @NbBundle.Messages({
        "FIX_MakeExpressionStatement=Retain expression as statement"
    })
    private static class MakeExpressionStatement extends JavaFix {
        public MakeExpressionStatement(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return Bundle.FIX_MakeExpressionStatement();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath retPath = ctx.getPath();
            if (retPath.getLeaf().getKind() != Tree.Kind.RETURN) {
                return;
            }
            ReturnTree rtt = (ReturnTree)retPath.getLeaf();
            if (rtt.getExpression() == null) {
                return;
            }
            WorkingCopy wc = ctx.getWorkingCopy();
            ExpressionToStatement st = new ExpressionToStatement(wc.getTreeMaker(), wc);
            st.scan(new TreePath(retPath, rtt.getExpression()), null);
            if (st.remove || st.statements.isEmpty()) {
                // error, but I don't have an utility to properly remove the statement
                // from its parent now.
                return;
            }
            Utilities.replaceStatement(wc, retPath, st.statements);
        }
    }
    
    /**
     * Transforms expression tree into series of statements.
     * 
     */
    static class ExpressionToStatement extends ErrorAwareTreePathScanner {
        private boolean remove = true;
        private List<StatementTree>    statements = new ArrayList<>(); 
        private final TreeMaker mk;
        private final CompilationInfo cinfo;

        public ExpressionToStatement(TreeMaker mk, CompilationInfo cinfo) {
            this.mk = mk;
            this.cinfo = cinfo;
        }
        
        private Object addExpressionStatement(ExpressionTree node, Object p) {
            if (mk != null) {
                statements.add(mk.ExpressionStatement(node));
            }
            remove = false;
            return null;
        }
        
        @Override
        public Object visitLiteral(LiteralTree node, Object p) {
            return null;
        }

        @Override
        public Object visitIdentifier(IdentifierTree node, Object p) {
            return null;
        }

        @Override
        public Object visitMemberReference(MemberReferenceTree node, Object p) {
            return null;
        }

        @Override
        public Object visitMemberSelect(MemberSelectTree node, Object p) {
            return null;
        }

        @Override
        public Object visitInstanceOf(InstanceOfTree node, Object p) {
            return null;
        }

        @Override
        public Object visitLambdaExpression(LambdaExpressionTree node, Object p) {
            return null;
        }

        @Override
        public Object visitNewArray(NewArrayTree node, Object p) {
            return null;
        }

        @Override
        public Object visitTypeCast(TypeCastTree node, Object p) {
            return scan(node.getExpression(), p);
        }
        
        public Object transformLogAndOr(BinaryTree node, Object p) {
            List<StatementTree> saveStats = this.statements;
            boolean saveRemove = this.remove;
            this.remove = true;
            this.statements = new ArrayList<>();
            scan(node.getRightOperand(), p);
            
            if (remove) {
                // the if statement would be empty; attempt to transform the
                // left operand if it has at least something. Omit the left operand
                // at all.
                this.statements = saveStats;
                scan(node.getLeftOperand(), p);
                if (remove) {
                    this.remove &= saveRemove;
                    return null;
                }
                this.remove &= saveRemove;
            } else {
                List<StatementTree> elseStats = this.statements;
                this.statements = saveStats;
                remove = false;
                if (mk != null) {
                    ExpressionTree condition;
                    if (node.getKind() == Tree.Kind.CONDITIONAL_AND) {
                        condition = node.getLeftOperand();
                    } else {
                        condition = Utilities.negate(mk, node.getLeftOperand(), node);
                    }
                    statements.add(
                            mk.If(condition, 
                                  elseStats.size() == 1 ?
                                          elseStats.get(0) :
                                          mk.Block(elseStats, false),
                                  null
                            )
                    );
                }
            }
            return null;
        }

        @Override
        public Object visitBinary(BinaryTree node, Object p) {
            switch (node.getKind()) {
                case CONDITIONAL_AND:
                case CONDITIONAL_OR:
                    return transformLogAndOr(node, p);
            }
            scan(node.getLeftOperand(), p);
            scan(node.getRightOperand(), p);
            return null;
        }

        @Override
        public Object visitUnary(UnaryTree node, Object p) {
            scan(node.getExpression(), p);
            return null;
        }

        @Override
        public Object visitAssignment(AssignmentTree node, Object p) {
            return addExpressionStatement(node, p);
        }

        @Override
        public Object visitCompoundAssignment(CompoundAssignmentTree node, Object p) {
            return addExpressionStatement(node, p);
        }

        @Override
        public Object visitNewClass(NewClassTree node, Object p) {
            return addExpressionStatement(node, p);
        }

        @Override
        public Object visitMethodInvocation(MethodInvocationTree node, Object p) {
            return addExpressionStatement(node, p);
        }

        /**
         * Conditional expression can be turned into an if-statement
         */
        @Override
        public Object visitConditionalExpression(ConditionalExpressionTree node, Object p) {
            List<StatementTree> saveStat = this.statements;
            boolean saveRemove = this.remove;
            statements = new ArrayList<>();
            
            scan(node.getTrueExpression(), p);
            
            List<StatementTree> trueStat = statements;
            statements = new ArrayList<>();
            
            scan(node.getFalseExpression(), p);
            List<StatementTree> falseStat = statements;
            
            this.statements = saveStat;
            this.remove = saveRemove && remove;
            
            if (trueStat.isEmpty()) {
                if (falseStat.isEmpty()) {
                    return null;
                }
                statements.add(mk.If(
                        mk.Unary(Tree.Kind.LOGICAL_COMPLEMENT, node.getCondition()),
                        falseStat.size() == 1 ? 
                                falseStat.get(0) :
                                mk.Block(falseStat, false),
                        null
                ));
            } else {
                statements.add(mk.If(node.getCondition(),
                        trueStat.size() == 1 ? 
                                trueStat.get(0) :
                                mk.Block(trueStat, false),
                        falseStat.isEmpty() ? null :
                            falseStat.size() == 1 ? 
                                    falseStat.get(0) :
                                    mk.Block(falseStat, false)
                                
                ));
            }
            return null;
        }
    }
    
    @Hint(id="unnecessaryContinueLabel", displayName="#DN_RemoveUnnecessaryContinueLabel", description="#DESC_RemoveUnnecessaryContinueLabel", category="general", suppressWarnings="UnnecessaryLabelOnContinueStatement")
    @TriggerPattern("continue $val;")
    public static ErrorDescription unnecessaryContinueLabel(HintContext ctx) {
        return unnecessaryLabel(ctx, false);
    }
    
    @Hint(id="unnecessaryBreakLabel", displayName="#DN_RemoveUnnecessaryBreakLabel", description="#DESC_RemoveUnnecessaryBreakLabel", category="general", suppressWarnings="UnnecessaryLabelOnBreakStatement")
    @TriggerPattern("break $val;")
    public static ErrorDescription unnecessaryBreakLabel(HintContext ctx) {
        return unnecessaryLabel(ctx, true);
    }
    
    private static ErrorDescription unnecessaryLabel(HintContext ctx, boolean brk) {
        TreePath loop = ctx.getPath();
        
        while (loop != null && !LOOP_KINDS.contains(loop.getLeaf().getKind()) && (!brk || loop.getLeaf().getKind() != Kind.SWITCH)) {
            loop = loop.getParentPath();
        }
        
        if (loop == null) return null;
        
        if (ctx.getInfo().getTreeUtilities().getBreakContinueTargetTree(ctx.getPath()) != loop.getParentPath().getLeaf()) return null;
        
        Fix fix = JavaFixUtilities.rewriteFix(ctx, brk ? Bundle.FIX_UnnecessaryBreakStatementLabel() : Bundle.FIX_UnnecessaryContinueStatementLabel(), ctx.getPath(), brk ? "break;" : "continue;");
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), brk ? Bundle.ERR_UnnecessaryBreakStatementLabel() : Bundle.ERR_UnnecessaryContinueStatementLabel(), fix);
    }
    
    private static final Set<Kind> LOOP_KINDS = EnumSet.of(Kind.DO_WHILE_LOOP, Kind.ENHANCED_FOR_LOOP, Kind.FOR_LOOP, Kind.WHILE_LOOP);
}
