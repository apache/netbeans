/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_StringBuilderAppend", description = "#DESC_StringBuilderAppend", id="StringBuilderAppend", category="performance", suppressWarnings="StringConcatenationInsideStringBufferAppend")
public class StringBuilderAppend {

    @TriggerPattern(value="$build.append($app)",
                    constraints={
                        @ConstraintVariableType(variable="$build", type="java.lang.StringBuilder"),
                        @ConstraintVariableType(variable="$app", type="java.lang.String")
                    })
    public static ErrorDescription builder(HintContext ctx) {
        return hint(ctx, "StringBuilder");
    }

    @TriggerPattern(value="$build.append($app)",
                    constraints={
                        @ConstraintVariableType(variable="$build", type="java.lang.StringBuffer"),
                        @ConstraintVariableType(variable="$app", type="java.lang.String")
                    })
    public static ErrorDescription buffer(HintContext ctx) {
        return hint(ctx, "StringBuffer");
    }

    private static ErrorDescription hint(HintContext ctx, String clazzName) {
        CompilationInfo info = ctx.getInfo();
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();
        ExpressionTree param = mit.getArguments().get(0);
        List<List<TreePath>> sorted = Utilities.splitStringConcatenationToElements(info, new TreePath(ctx.getPath(), param));

        if (sorted.size() > 1) {
            String error = NbBundle.getMessage(StringBuilderAppend.class, "ERR_StringBuilderAppend", clazzName);
            return ErrorDescriptionFactory.forTree(ctx, param, error, new FixImpl(info, ctx.getPath()).toEditorFix());
        }

        return null;
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        public String getText() {
            return NbBundle.getMessage(StringBuilderAppend.class, "FIX_StringBuilderAppend");
        }
        
        private ExpressionTree merge(TreeMaker make, ExpressionTree arg1, ExpressionTree arg2) {
            if (arg1 == null) {
                return arg2;
            } else if (arg2 == null) {
                return arg1;
            } else if (arg1 == arg2) {
                return arg1;
            }
            return make.Binary(Kind.PLUS, arg1, arg2);
        } 
        
        private ExpressionTree merge(TreeMaker make, ExpressionTree arg, ExpressionTree singleLeaf, 
                StringBuilder literal, ExpressionTree l) {
            ExpressionTree n;
            if (singleLeaf != null) {
                n = singleLeaf;
                literal.delete(0, literal.length());
                n = merge(make, n, l);
            } else if (literal.length() > 0) {
                n = make.Literal(literal.toString());
                literal.delete(0, literal.length());
                n = merge(make, n, l);
            } else {
                n = l;
            }
            return merge(make, arg, n);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            MethodInvocationTree mit = (MethodInvocationTree) tp.getLeaf();
            ExpressionTree param = mit.getArguments().get(0);
            List<List<TreePath>> sorted = Utilities.splitStringConcatenationToElements(copy, new TreePath(tp, param));
            ExpressionTree site = ((MemberSelectTree) mit.getMethodSelect()).getExpression();
            TreeMaker make = copy.getTreeMaker();
            
            for (List<TreePath> cluster : sorted) {
                StringBuilder literal = new StringBuilder();
                ExpressionTree singleLeaf = null;
                ExpressionTree arg = null;
                
                if (cluster.size() == 1 && 
                    !Utilities.isConstantString(copy, cluster.get(0), true)) {
                    arg = (ExpressionTree)cluster.get(0).getLeaf();
                } else {
                    for (TreePath p : cluster) {
                        ExpressionTree l = (ExpressionTree)p.getLeaf();
                        if (Utilities.isStringOrCharLiteral(l)) {
                            if (literal.length() == 0) {
                                singleLeaf = l;
                            } else {
                                singleLeaf = null;
                            }
                            literal.append(
                                ((LiteralTree)l).getValue().toString()
                            );
                        } else {
                            ExpressionTree n;
                            arg = merge(make, arg, singleLeaf, literal, l);
                            singleLeaf = null;
                        }
                    }
                }
                arg = merge(make, arg, singleLeaf, literal, null);
                site = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(site, "append"), Collections.singletonList(arg));
            }

            copy.rewrite(mit, site);
        }
        
    }

}
