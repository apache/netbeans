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
