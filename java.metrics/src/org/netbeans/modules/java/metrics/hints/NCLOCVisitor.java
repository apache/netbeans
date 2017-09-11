/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.metrics.hints;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

/**
 * Non-comment line visitor. Counts the number of non-comment
 * lines occupied by the Tree. The implementation assumes, that the tree nodes 
 * come in the textual order. It also counts number of 
 * executable statements in the treee.
 * 
 * @author sdedic
 */
public final class NCLOCVisitor extends TreePathScanner<Object, Object> {
    /**
     * The source text 
     */
    private final CharSequence sourceText;
    private final SourcePositions pos;
    
    /**
     * Position of newline on the line where the last statement was encountered
     */
    private long lastPos = -1;
    
    private int lines;
    private int statements;

    public NCLOCVisitor(CharSequence sourceText, SourcePositions pos) {
        this.sourceText = sourceText;
        this.pos = pos;
    }
    
    public int getLineCount() {
        return lines;
    }
    
    public int getStatementCount() {
        return statements;
    }
    
    @Override
    public Object scan(Tree tree, Object p) {
        long startPos = pos.getStartPosition(getCurrentPath().getCompilationUnit(), tree);
        if (lastPos < startPos) {
            lines++;
            
            int pos = (int)startPos;
            boolean nline = false;
            for (; pos < sourceText.length() && !nline; pos++) {
                char c = sourceText.charAt(pos);
                // the 'lastPos' does not need to be so precise. In the case of \r\n or \n\r line end markers,
                // we can stay at the st position, because no statement on the current line will start after the mark,
                // and all statement on the subsequent line will start after the mark, so they will increase the line count.
                nline = (c == '\n' || c == '\r');
            }
            lastPos = pos;
        }
        return super.scan(tree, p); 
    }

    @Override
    public Object visitAssert(AssertTree node, Object p) {
//      do not count... option ?
//        statements++;
        return super.visitAssert(node, p); 
    }

    
    @Override
    public Object visitVariable(VariableTree node, Object p) {
        TreePath path = getCurrentPath();
        Tree parent = path.getParentPath().getLeaf();
        if (parent instanceof StatementTree) {
            boolean count = true;
            if (parent instanceof ForLoopTree) {
                count = !((ForLoopTree)parent).getInitializer().contains(node);
            } else if (parent instanceof EnhancedForLoopTree) {
                count = ((EnhancedForLoopTree)parent).getVariable() != node;
            }
            if (count) {
                statements++;
            }
        }
        return super.visitVariable(node, p);
    }
    
    

    @Override
    public Object visitExpressionStatement(ExpressionStatementTree node, Object p) {
        boolean count = true;
        TreePath path = getCurrentPath();
        Tree parent = path.getParentPath().getLeaf();
        // do not count the update statement in a for-loop
        if (parent instanceof ForLoopTree) {
            count = !((ForLoopTree)parent).getUpdate().contains(node);
        }
        if (count) {
            statements++;
        }
        return super.visitExpressionStatement(node, p);
    }

    @Override
    public Object visitDoWhileLoop(DoWhileLoopTree node, Object p) {
        statements++;
        return super.visitDoWhileLoop(node, p);
    }

    @Override
    public Object visitForLoop(ForLoopTree node, Object p) {
        statements++;
        return super.visitForLoop(node, p);
    }

    @Override
    public Object visitEnhancedForLoop(EnhancedForLoopTree node, Object p) {
        statements++;
        return super.visitEnhancedForLoop(node, p);
    }

    @Override
    public Object visitSwitch(SwitchTree node, Object p) {
        statements++;
        return super.visitSwitch(node, p);
    }

    @Override
    public Object visitCatch(CatchTree node, Object p) {
        statements++;
        return super.visitCatch(node, p);
    }

    @Override
    public Object visitIf(IfTree node, Object p) {
        statements++;
        return super.visitIf(node, p);
    }

    @Override
    public Object visitBreak(BreakTree node, Object p) {
        statements++;
        return super.visitBreak(node, p);
    }

    @Override
    public Object visitContinue(ContinueTree node, Object p) {
        statements++;
        return super.visitContinue(node, p);
    }

    @Override
    public Object visitReturn(ReturnTree node, Object p) {
        statements++;
        return super.visitReturn(node, p);
    }

    @Override
    public Object visitThrow(ThrowTree node, Object p) {
        statements++;
        return super.visitThrow(node, p);
    }

    @Override
    public Object visitMethodInvocation(MethodInvocationTree node, Object p) {
        TreePath parentPath = getCurrentPath().getParentPath();
        if (parentPath != null) {
            Tree t = parentPath.getLeaf();
            // do not count invocations that are a part of expression (expression statement is counted already)
            if (!ExpressionTree.class.isAssignableFrom(t.getKind().asInterface()) &&
                !ExpressionStatementTree.class.isAssignableFrom(t.getKind().asInterface())) {
                statements++;
            }
        }
        return super.visitMethodInvocation(node, p);
    }

    @Override
    public Object visitWhileLoop(WhileLoopTree node, Object p) {
        statements++;
        return super.visitWhileLoop(node, p);
    }
    
    
}
