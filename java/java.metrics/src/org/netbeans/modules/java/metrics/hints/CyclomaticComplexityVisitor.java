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

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.WhileLoopTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;

/**
 * This Visitor counts a Cyclomatic complexity on the visited tree. 
 * The edges and vertexes are not counted, it is far more complex than just
 * accumulating the complexity itself. Most of the constructions contribute
 * evenly to both vertexes and edges, so I accumulate just the diff.
 * 
 * @author sdedic
 */
public final class CyclomaticComplexityVisitor extends ErrorAwareTreePathScanner<Object, Object> {
    private int complexity;
    
    private boolean inClass;

    /**
     * True, if the nearest enclosing unlabeled break target is a switch case statement.
     * Value is saved on the stack in the visit* methods.
     */
    private boolean switchCase;

    @Override
    public Object visitClass(ClassTree node, Object p) {
        if (!inClass) {
            inClass = true;
            Object r = super.visitClass(node, p); 
            inClass = false;
            return r;
        } else {
            return null;
        }
        
    }
    
    /**
     * Do-while loop adds: 
     * - no vertex, as it is a member of the linear code.
     * - an additional vertex for the code after the while
     * - one edge for unsatisfied condition that skips the cycle
     * - one edge for satisfied condition which at least re-evaluates the condition
     * - an additional vertex and an edge if the body contains a non-empty statement
     * = +1 to complexity
     */
    @Override
    public Object visitDoWhileLoop(DoWhileLoopTree node, Object p) {
        boolean saveFlag = switchCase;
        switchCase = false;
        complexity++;
        Object o = super.visitDoWhileLoop(node, p);
        this.switchCase = saveFlag;
        return o;
    }

    @Override
    public Object visitWhileLoop(WhileLoopTree node, Object p) {
        boolean saveFlag = switchCase;
        switchCase = false;
        complexity++;
        Object o = super.visitWhileLoop(node, p);
        this.switchCase = saveFlag;
        return o;
    }

    @Override
    public Object visitForLoop(ForLoopTree node, Object p) {
        boolean saveFlag = switchCase;
        switchCase = false;
        complexity++;
        Object o = super.visitForLoop(node, p);
        this.switchCase = saveFlag;
        return o;
    }
    
    @Override
    public Object visitEnhancedForLoop(EnhancedForLoopTree node, Object p) {
        boolean saveFlag = switchCase;
        switchCase = false;
        complexity++;
        Object o = super.visitEnhancedForLoop(node, p);
        this.switchCase = saveFlag;
        return o;
    }

    /**
     * Do not add complexity for empty cases - they merge with the
     * following one.
     */
    @Override
    public Object visitCase(CaseTree node, Object p) {
        if (node.getStatements() != null) {
            complexity++;
        }
        boolean saveFlag = switchCase;
        switchCase = true;
        Object o = super.visitCase(node, p);
        this.switchCase = saveFlag;
        return o;
    }

    @Override
    public Object visitCatch(CatchTree node, Object p) {
        complexity++;
        return super.visitCatch(node, p);
    }

    @Override
    public Object visitConditionalExpression(ConditionalExpressionTree node, Object p) {
        complexity++;
        return super.visitConditionalExpression(node, p);
    }

    @Override
    public Object visitIf(IfTree node, Object p) {
        complexity++;
        return super.visitIf(node, p);
    }

    @Override
    public Object visitBreak(BreakTree node, Object p) {
        // do not count the break, if it is nested in a switch 'case' statement.
        if (node.getLabel() != null || !switchCase) {
            complexity++;
        }
        return super.visitBreak(node, p);
    }

    @Override
    public Object visitContinue(ContinueTree node, Object p) {
        complexity++;
        return super.visitContinue(node, p);
    }

    @Override
    public Object visitThrow(ThrowTree node, Object p) {
        complexity++;
        return super.visitThrow(node, p);
    }

    public int getComplexity() {
        return complexity;
    }
    
    public void reset() {
        this.complexity = 0;
        this.switchCase = false;
    }
}
