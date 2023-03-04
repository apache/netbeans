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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;

/**
 * Inspects Math.random and StrictMath.random conversion to int or long.
 * If found, it takes the parent arithmetic expression and casts it to long/int
 * 
 * @author sdedic
 */
@Hint(
    displayName="#DN_MathRandomCastInt",
    description="#DESC_MathRandomCastInt",
    category="bugs",
    suppressWarnings = { "MathRandomCastToInt" },
    enabled = true
)
@NbBundle.Messages({
    "TEXT_MathRandomCastInt=Math.int() immediately casted to int or long",
    "FIX_MathRandomCastInt=Cast whole expression that contains Math.random()"
})
public class MathRandomCast {
    @TriggerPatterns(value = {
        @TriggerPattern(value = "(int)java.lang.Math.random()"),
        @TriggerPattern(value = "(int)java.lang.StrictMath.random()"),
        @TriggerPattern(value = "(long)java.lang.Math.random()"),
        @TriggerPattern(value = "(long)java.lang.StrictMath.random()"),

        @TriggerPattern(value = "(java.lang.Integer)java.lang.Math.random()"),
        @TriggerPattern(value = "(java.lang.Integer)java.lang.StrictMath.random()"),
        @TriggerPattern(value = "(java.lang.Long)java.lang.Math.random()"),
        @TriggerPattern(value = "(java.lang.Long)java.lang.StrictMath.random()")
    })
    public static ErrorDescription mathRandomCast(HintContext ctx) {
        TreePath path = ctx.getPath();

        if (path.getLeaf().getKind() != Tree.Kind.TYPE_CAST) {
            return null;
        }
        
        // traverse up to the expression chain
        TreePath expr = path;
        for (TreePath parent = path.getParentPath(); parent != null && EXPRESSION_KINDS.contains(parent.getLeaf().getKind()); parent = parent.getParentPath()) {
            expr = parent;
        }
        Fix fix = null;
        
        // do not provide hints if the cast was immediately used in a method call, assignment etc
        if (expr != path) {
            fix = new FixImpl(
                    TreePathHandle.create(ctx.getPath(), ctx.getInfo()),
                    TreePathHandle.create(expr, ctx.getInfo())).toEditorFix();
        }
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), TEXT_MathRandomCastInt(), fix);
    }
    
    private static final EnumSet EXPRESSION_KINDS = EnumSet.of(
            Tree.Kind.DIVIDE,
            Tree.Kind.MINUS,
            Tree.Kind.MULTIPLY,
            Tree.Kind.PLUS,
            Tree.Kind.UNARY_MINUS,
            Tree.Kind.UNARY_PLUS,
            Tree.Kind.PARENTHESIZED
    );
    
    private static class FixImpl extends JavaFix {
        private final TreePathHandle exprHandle;
        
        public FixImpl(TreePathHandle handle, TreePathHandle exprHandle) {
            super(handle);
            this.exprHandle = exprHandle;
        }

        @Override
        protected String getText() {
            return FIX_MathRandomCastInt();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            // path should be the typecast expression
            TreePath path = ctx.getPath();
            TreePath exprPath = exprHandle.resolve(ctx.getWorkingCopy());
            
            if (path.getLeaf().getKind() != Tree.Kind.TYPE_CAST || exprPath == null || !EXPRESSION_KINDS.contains(exprPath.getLeaf().getKind())) {
                // PENDING - some message ?
                return;
            }
            WorkingCopy copy = ctx.getWorkingCopy();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            TypeCastTree cast = (TypeCastTree)path.getLeaf();
            // rewrite the type cast to the casted Math.random()
            copy.rewrite(path.getLeaf(), cast.getExpression());
            // rewrite the outermost expression to a typecast of it
            ExpressionTree expr = (ExpressionTree)exprPath.getLeaf();
            if (expr.getKind() != Tree.Kind.PARENTHESIZED) {
                expr = make.Parenthesized(expr);
            }
            copy.rewrite(exprPath.getLeaf(), make.TypeCast(cast.getType(), (ExpressionTree)expr));
        }
    }
}
