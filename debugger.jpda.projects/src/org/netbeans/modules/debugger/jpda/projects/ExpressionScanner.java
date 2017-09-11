/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.tools.Diagnostic;

/**
 * A tree scanner, which collects expressions on a given line.
 * 
 * @author Martin Entlicher
 */
class ExpressionScanner extends TreeScanner<List<Tree>, ExpressionScanner.ExpressionsInfo> {
    
    private int lineNumber;
    private int statementStart;
    private int statementEnd;
    private CompilationUnitTree tree;
    private SourcePositions positions;
    private LineMap lineMap;
    private boolean checkBounds = true;

    public ExpressionScanner(int lineNumber, int statementStart, int statementEnd,
                             CompilationUnitTree tree, SourcePositions positions) {
        this.tree = tree;
        this.lineNumber = lineNumber;
        this.statementStart = statementStart;
        this.statementEnd = statementEnd;
        this.positions = positions;
        this.lineMap = tree.getLineMap();
    }
    
    private boolean acceptsTree(Tree aTree) {
        /*
        int start = (int) positions.getStartPosition(tree, aTree);
        int end = (int) positions.getEndPosition(tree, aTree);
        return start <= offset && offset < end;
         */
        if (!checkBounds) {
            return true;
        }
        int startLine = (int) lineMap.getLineNumber(positions.getStartPosition(tree, aTree));
        if (startLine == lineNumber) {
            return true;
        } else {
            //return false;
            return startLine < lineNumber &&
                   lineMap.getLineNumber(positions.getEndPosition(tree, aTree)) >= lineNumber;
        }
    }
    
    private boolean isCurrentTree(Tree aTree) {
        int startLine = (int) lineMap.getLineNumber(positions.getStartPosition(tree, aTree));
        if (startLine == Diagnostic.NOPOS) {
            return false;
        }
        int endLine = (int) lineMap.getLineNumber(positions.getEndPosition(tree, aTree));
        if (endLine == Diagnostic.NOPOS) {
            return false;
        }
        return startLine <= lineNumber && lineNumber <= endLine;
    }

    @Override
    public List<Tree> reduce(List<Tree> r1, List<Tree> r2) {
        if (r1 == null || r1.isEmpty()) {
            return r2;
        }
        if (r2 == null || r2.isEmpty()) {
            return r1;
        }
        r1.addAll(r2);
        return r1;
    }

    @Override
    public List<Tree> scan(Iterable<? extends Tree> nodes, ExpressionScanner.ExpressionsInfo p) {
	List<Tree> r = null;
	if (nodes != null) {
            boolean first = true;
            for (Tree node : nodes) {
                r = (first ? scan(node, p) : reduce(r, scan(node, p)));
                first = false;
            }
        }
        return r;
    }
    

    
    private List<Tree> scan(Tree t1, Tree t2, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> result = scan(t1, p);
        result = reduce(result, scan(t2, p));
        return result;
    }

    @Override
    public List<Tree> visitAnnotation(AnnotationTree node, ExpressionScanner.ExpressionsInfo p) {
        return null;
    }

    @Override
    public List<Tree> visitMethodInvocation(MethodInvocationTree node, ExpressionScanner.ExpressionsInfo p) {
	List<Tree> result = scan(node.getTypeArguments(), p);
        result = reduce(result, scan(node.getMethodSelect(), p));
        result = reduce(result, scan(node.getArguments(), p));
        if (result == null) {
            result = new ArrayList<Tree>();
        }
        result.add(node);
        return result;
    }

    @Override
    public List<Tree> visitAssert(AssertTree node, ExpressionScanner.ExpressionsInfo p) {
        if (acceptsTree(node)) {
            List<Tree> result = scan(node.getCondition(), p);
            result = reduce(result, scan(node.getDetail(), p));
            return result;
        } else {
            return null;
        }
    }

    @Override
    public List<Tree> visitAssignment(AssignmentTree node, ExpressionScanner.ExpressionsInfo p) {
        return scan(node.getVariable(), node.getExpression(), p);
    }

    @Override
    public List<Tree> visitCompoundAssignment(CompoundAssignmentTree node, ExpressionScanner.ExpressionsInfo p) {
        return scan(node.getVariable(), node.getExpression(), p);
    }

    @Override
    public List<Tree> visitBinary(BinaryTree node, ExpressionScanner.ExpressionsInfo p) {
        return scan(node.getLeftOperand(), node.getRightOperand(), p);
    }

    //public List<Tree> visitBlock(BlockTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitBreak(BreakTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitCase(CaseTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> result = scan(node.getExpression(), p);
        result = reduce(result, scan(node.getStatements(), p));
        return result;
    }

    //public List<Tree> visitCatch(CatchTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitClass(ClassTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitConditionalExpression(ConditionalExpressionTree node, ExpressionScanner.ExpressionsInfo p) {
        ExpressionTree condition = node.getCondition();
        List<Tree> cond = scan(condition, p);
        Tree lastCond = null;
        Boolean resolvedCondition = null;
        if (cond != null) {
            lastCond = cond.get(cond.size() - 1);
        } else {
            if (condition.getKind() == Tree.Kind.BOOLEAN_LITERAL) {
                resolvedCondition = Boolean.parseBoolean(condition.toString());
            }
        }
        List<Tree> rT;
        List<Tree> rF;
        if (resolvedCondition != null) {
            if (resolvedCondition) {
                rT = scan(node.getTrueExpression(), p);
                rF = null;
            } else {
                rT = null;
                rF = scan(node.getFalseExpression(), p);
            }
        } else {
            rT = scan(node.getTrueExpression(), p);
            rF = scan(node.getFalseExpression(), p);
        }
        if (lastCond != null) {
            if (rT != null) {
                p.addNextExpression(lastCond, rT.get(0));
            }
            if (rF != null) {
                p.addNextExpression(lastCond, rF.get(0));
            }
        }
        return reduce(reduce(cond, rT), rF);
    }

    //public List<Tree> visitContinue(ContinueTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitDoWhileLoop(DoWhileLoopTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> statements = scan(node.getStatement(), p);
        List<Tree> cond = null;
        if (acceptsTree(node.getCondition())) {
            cond = scan(node.getCondition(), p);
        }
        if (cond != null && cond.size() > 0 && statements != null && statements.size() > 0) {
            Tree lastCond = cond.get(cond.size() - 1);
            p.addNextExpression(lastCond, statements.get(0));
        }
        return reduce(statements, cond);
    }

    //public List<Tree> visitErroneous(ErroneousTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitExpressionStatement(ExpressionStatementTree node, ExpressionScanner.ExpressionsInfo p) {
        if (acceptsTree(node)) {
            return scan(node.getExpression(), p);
        } else {
            return null;
        }
    }

    @Override
    public List<Tree> visitEnhancedForLoop(EnhancedForLoopTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> expr = null;
        if (acceptsTree(node.getExpression())) {
            expr = scan(node.getExpression(), p);
        }
        List<Tree> bodyr = scan(node.getStatement(), p);
        if (expr != null && expr.size() > 0 &&
            bodyr != null && bodyr.size() > 0) {
            p.addNextExpression(expr.get(expr.size() - 1), bodyr.get(0));
            p.addNextExpression(bodyr.get(bodyr.size() - 1), expr.get(0));
        }
        return reduce(expr, bodyr);
    }

    @Override
    public List<Tree> visitForLoop(ForLoopTree node, ExpressionScanner.ExpressionsInfo p) {
        if (!isCurrentTree(node)) {
            return null;
        }
        List<Tree> initr = scan(node.getInitializer(), p);
        checkBounds = false; // Scan all parts to be able to set the next operations
        List<Tree> condra = scan(node.getCondition(), p);
        List<Tree> updtra = scan(node.getUpdate(), p);
        //List<Tree> bodyra = scan(node.getStatement(), p);
        checkBounds = true;
        
        // And then scan the current (accepted) trees
        List<Tree> condr = null;
        if (acceptsTree(node.getCondition())) {
            condr = scan(node.getCondition(), p);
        }
        List<Tree> updtr = scan(node.getUpdate(), p);
        List<Tree> bodyr = scan(node.getStatement(), p);
        
        if (initr != null) {
            if (condra != null) {
                p.addNextExpression(initr.get(initr.size() - 1), condra.get(0));
            } else if (bodyr != null) {
                p.addNextExpression(initr.get(initr.size() - 1), bodyr.get(0));
            } else if (updtra != null) {
                p.addNextExpression(initr.get(initr.size() - 1), updtra.get(0));
            }
        }
        if (condr != null) {
            if (bodyr != null) {
                p.addNextExpression(condr.get(condr.size() - 1), bodyr.get(0));
            } else if (updtra != null) {
                p.addNextExpression(condr.get(condr.size() - 1), updtra.get(0));
            }
        }
        if (bodyr != null) {
            if (updtra != null) {
                p.addNextExpression(bodyr.get(bodyr.size() - 1), updtra.get(0));
            } else if (condra != null) {
                p.addNextExpression(bodyr.get(bodyr.size() - 1), condra.get(0));
            }
        }
        if (updtr != null) {
            if (condra != null) {
                p.addNextExpression(updtr.get(updtr.size() - 1), condra.get(0));
            } else if (bodyr != null) {
                p.addNextExpression(updtr.get(updtr.size() - 1), bodyr.get(0));
            }
        }
        return reduce(reduce(reduce(initr, condr), bodyr), updtr);
    }

    //public List<Tree> visitIdentifier(IdentifierTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitIf(IfTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> cond = null;
        Tree lastCond = null;
        if (acceptsTree(node)) {
            cond = scan(node.getCondition(), p);
            if (cond != null) {
                lastCond = cond.get(cond.size() - 1);
            }
        }
        StatementTree thent = node.getThenStatement();
        StatementTree elset = node.getElseStatement();
        List<Tree> thenr = null;
        if (isCurrentTree(thent)) {
            thenr = scan(thent, p);
            if (lastCond != null && thenr != null) {
                p.addNextExpression(lastCond, thenr.get(0));
            }
        }
        List<Tree> elser = null;
        if (isCurrentTree(elset)) {
            elser = scan(elset, p);
            if (lastCond != null && elser != null) {
                p.addNextExpression(lastCond, elser.get(0));
            }
        }
        return reduce(reduce(cond, thenr), elser);
    }

    //public List<Tree> visitImport(ImportTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitArrayAccess(ArrayAccessTree node, ExpressionScanner.ExpressionsInfo p) {
        return scan(node.getExpression(), node.getIndex(), p);
    }

    //public List<Tree> visitLabeledStatement(LabeledStatementTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitLiteral(LiteralTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitMethod(MethodTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitModifiers(ModifiersTree node, ExpressionScanner.ExpressionsInfo p) {
        return null;
    }

    @Override
    public List<Tree> visitNewArray(NewArrayTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> result = scan(node.getType(), p);
        result = reduce(result, scan(node.getDimensions(), p));
        result = reduce(result, scan(node.getInitializers(), p));
        return result;
    }

    @Override
    public List<Tree> visitNewClass(NewClassTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> result = scan(node.getEnclosingExpression(), node.getIdentifier(), p);
        result = reduce(result, scan(node.getArguments(), p));
        result = reduce(result, scan(node.getClassBody(), p));
        if (result == null) {
            result = new ArrayList<Tree>();
        }
        result.add(node);
        return result;
    }

    //public List<Tree> visitParenthesized(ParenthesizedTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitReturn(ReturnTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitMemberSelect(MemberSelectTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitEmptyStatement(EmptyStatementTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitSwitch(SwitchTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> result = null;
        if (acceptsTree(node)) {
            result = scan(node.getExpression(), p);
        }
        return reduce(result, scan(node.getCases(), p));
    }

    @Override
    public List<Tree> visitSynchronized(SynchronizedTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> result = null;
        if (acceptsTree(node)) {
            result = scan(node.getExpression(), p);
        }
        return reduce(result, scan(node.getBlock(), p));
    }

    @Override
    public List<Tree> visitThrow(ThrowTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> result = null;
        if (acceptsTree(node)) {
            result = scan(node.getExpression(), p);
        }
        return result;
    }

    //public List<Tree> visitCompilationUnit(CompilationUnitTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitTry(TryTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitParameterizedType(ParameterizedTypeTree node, ExpressionScanner.ExpressionsInfo p) {
        return null;
    }

    //public List<Tree> visitArrayType(ArrayTypeTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitTypeCast(TypeCastTree node, ExpressionScanner.ExpressionsInfo p) {
        return scan(node.getExpression(), p);
    }

    //public List<Tree> visitPrimitiveType(PrimitiveTypeTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    //public List<Tree> visitTypeParameter(TypeParameterTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitInstanceOf(InstanceOfTree node, ExpressionScanner.ExpressionsInfo p) {
        return scan(node.getExpression(), node.getType(), p);
    }

    @Override
    public List<Tree> visitUnary(UnaryTree node, ExpressionScanner.ExpressionsInfo p) {
        return scan(node.getExpression(), p);
    }

    @Override
    public List<Tree> visitVariable(VariableTree node, ExpressionScanner.ExpressionsInfo p) {
        if (acceptsTree(node)) {
            return scan(node.getInitializer(), p);
        } else {
            return null;
        }
    }

    @Override
    public List<Tree> visitWhileLoop(WhileLoopTree node, ExpressionScanner.ExpressionsInfo p) {
        List<Tree> cond = null;
        if (acceptsTree(node.getCondition())) {
            cond = scan(node.getCondition(), p);
        }
        List<Tree> statements = scan(node.getStatement(), p);
        if (cond != null && statements != null && statements.size() > 0) {
            p.addNextExpression(statements.get(statements.size() - 1), cond.get(0));
        }
        return reduce(cond, statements);
    }

    @Override
    public List<Tree> visitReturn(ReturnTree node, ExpressionsInfo p) {
        if (acceptsTree(node)) {
            return scan(node.getExpression(), p);
        } else {
            return null;
        }
    }



    //public List<Tree> visitWildcard(WildcardTree node, ExpressionScanner.ExpressionsInfo p) {
    //}

    @Override
    public List<Tree> visitOther(Tree node, ExpressionScanner.ExpressionsInfo p) {
        return null;
    }
    
    

    /** Provides further information about the expressions. */
    public static final class ExpressionsInfo extends Object {
        
        private final Map<Tree, Set<Tree>> nextExpressions = new HashMap<Tree, Set<Tree>>();
        final Stack<StatementTree> wrappingStatements = new Stack<StatementTree>();
        
        
        synchronized void addNextExpression(Tree expression, Tree next) {
            Set<Tree> nexts = nextExpressions.get(expression);
            if (nexts == null) {
                nexts = new HashSet<Tree>();
                nextExpressions.put(expression, nexts);
            }
            nexts.add(next);
        }
        
        synchronized Set<Tree> getNextExpressions(Tree expression) {
            Set<Tree> nexts = nextExpressions.get(expression);
            if (nexts == null) {
                return Collections.emptySet();
            } else {
                return nexts;
            }
        }
    }
}
