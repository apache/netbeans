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
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;

/**
 * Non-comment line visitor. Counts the number of non-comment
 * lines occupied by the Tree. The implementation assumes, that the tree nodes 
 * come in the textual order. It also counts number of 
 * executable statements in the treee.
 * 
 * @author sdedic
 */
public final class NCLOCVisitor extends ErrorAwareTreePathScanner<Object, Object> {
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
