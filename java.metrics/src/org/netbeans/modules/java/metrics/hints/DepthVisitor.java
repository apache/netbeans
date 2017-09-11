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
import com.sun.source.util.TreePathScanner;

/**
 * Counts the maximum depth of the Tree
 * 
 * @author sdedic
 */
public final class DepthVisitor extends TreePathScanner {
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
