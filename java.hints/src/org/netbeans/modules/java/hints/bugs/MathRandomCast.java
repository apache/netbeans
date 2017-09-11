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
