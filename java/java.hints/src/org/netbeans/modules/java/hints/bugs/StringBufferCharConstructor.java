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
import com.sun.source.tree.NewClassTree;
import com.sun.source.util.TreePath;
import com.sun.source.tree.Tree;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * Will catch new instances of StringBuffer/StringBuilder initialized with a single 'char' (Character) parameter. The
 * fix will rewrite the expression into new StringBuffer().append(char);
 * 
 * @author sdedic
 */
@Hint(
    displayName = "#DN_StringBufferCharConstructor",
    description = "#DESC_StringBufferCharConstructor",
    category = "bugs",
    options = Hint.Options.QUERY,
    enabled = true,
    suppressWarnings = { "StringBufferCharConstructor", "NewStringBufferWithCharArgument" }
)
@NbBundle.Messages({
    "# {0} - StringBuffer/StringBuilder",
    "TEXT_StringBufferCharConstructor={0} constructor called with `char'' argument",
    "# {0} - StringBuffer/StringBuilder",
    "FIX_StringBufferCharConstructor=Replace with new {0}().append()"
})
public class StringBufferCharConstructor {
    @TriggerPatterns({
        @TriggerPattern(value = "new java.lang.StringBuffer($x)", constraints = @ConstraintVariableType(variable = "$x", type = "char")),
        @TriggerPattern(value = "new java.lang.StringBuilder($x)", constraints = @ConstraintVariableType(variable = "$x", type = "char"))
    })
    public static ErrorDescription run(HintContext ctx) {
        TreePath p = ctx.getPath();
        
        TypeMirror paramType = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$x")); // NOI18N
        if (paramType.getKind() != TypeKind.CHAR) {
            if (paramType.getKind() != TypeKind.DECLARED) {
                return null;
            }
            Element el = ((DeclaredType)paramType).asElement();
            if (el == null || el.getKind() != ElementKind.CLASS) {
                return null;
            }
            if (!((TypeElement)el).getQualifiedName().contentEquals("java.lang.Character")) {
                return null;
            }
        }
        
        TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(p);
        CharSequence tname = ctx.getInfo().getTypeUtilities().getTypeName(tm);
        
        return ErrorDescriptionFactory.forTree(ctx, p, Bundle.TEXT_StringBufferCharConstructor(tname), 
                new NewAndAppendFix(TreePathHandle.create(p, ctx.getInfo()), tname.toString()).toEditorFix());
    }
    
    private static class NewAndAppendFix extends JavaFix {
        final String builder;
        
        public NewAndAppendFix(TreePathHandle handle, String builder) {
            super(handle);
            this.builder = builder;
        }

        @Override
        protected String getText() {
            return Bundle.FIX_StringBufferCharConstructor(builder);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath p = ctx.getPath();
            if (p.getLeaf().getKind() != Tree.Kind.NEW_CLASS) {
                return;
            }
            NewClassTree origNct = (NewClassTree)p.getLeaf();
            if (origNct.getArguments().size() != 1) {
                return;
            }
            NewClassTree nct = GeneratorUtilities.get(ctx.getWorkingCopy()).importComments(origNct, ctx.getWorkingCopy().getCompilationUnit());
            ExpressionTree charArg = nct.getArguments().get(0);
            TreeMaker mk = ctx.getWorkingCopy().getTreeMaker();
            
            ExpressionTree newExpr = mk.NewClass(nct.getEnclosingExpression(), (List<ExpressionTree>)nct.getTypeArguments(), nct.getIdentifier(), 
                    Collections.<ExpressionTree>emptyList(), nct.getClassBody());

            Tree replace = mk.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(), 
                    mk.MemberSelect(newExpr, "append"), // NOI18N
                    Collections.singletonList(charArg));
            ctx.getWorkingCopy().rewrite(nct, replace);
        }
    }
}
