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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;

/**
 * Counts the maximum depth of the Tree
 * 
 * @author sdedic
 */
public final class DepthVisitor extends ErrorAwareTreePathScanner {
    private int depth;
    private int maxDepth;
    
    public int getDepth() {
        return maxDepth;
    }

    @Override
    public Object scan(Tree tree, Object p) {
        maxDepth = Math.max(depth, maxDepth);
        return super.scan(tree, p);
    }

    @Override
    public Object visitClass(ClassTree node, Object p) {
        depth++;
        Object o = super.visitClass(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitDoWhileLoop(DoWhileLoopTree node, Object p) {
        depth++;
        Object o = super.visitDoWhileLoop(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitWhileLoop(WhileLoopTree node, Object p) {
        depth++;
        Object o = super.visitWhileLoop(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitForLoop(ForLoopTree node, Object p) {
        depth++;
        Object o = super.visitForLoop(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitEnhancedForLoop(EnhancedForLoopTree node, Object p) {
        depth++;
        Object o = super.visitEnhancedForLoop(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitSwitch(SwitchTree node, Object p) {
        depth++;
        Object o = super.visitSwitch(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitTry(TryTree node, Object p) {
        depth++;
        Object o = super.visitTry(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitConditionalExpression(ConditionalExpressionTree node, Object p) {
        depth++;
        Object o = super.visitConditionalExpression(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitIf(IfTree node, Object p) {
        depth++;
        Object o = super.visitIf(node, p); 
        depth--;
        return o;
    }

    @Override
    public Object visitLambdaExpression(LambdaExpressionTree node, Object p) {
        depth++;
        Object o = super.visitLambdaExpression(node, p); 
        depth--;
        return o;
    }
}
