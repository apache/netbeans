/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author mjayan
 */
@NbBundle.Messages({
    "DN_ConvertToVT=Convert to Virtual Thread Executor",
    "DESC_ConvertToVT=Convert to Virtual Thread Executor",
    "ERR_ConvertToVT=Convert to Virtual Thread Executor",
    "FIX_ConvertToVT=Convert to Virtual Thread Executor",
    "ERR_ConvertToThreadPerTask=Convert to newThreadPerTask executor"
})
@Hint(displayName = "#DN_ConvertToVT", description = "#DESC_ConvertToVT", category = "rules15",
        minSourceVersion = "19")
public class ConvertToVT {

    private static final int VT_PREVIEW_JDK_VERSION = 19;

    @TriggerPatterns({
        @TriggerPattern(value = "$var = $expr2.$meth1($var1$);"),
        @TriggerPattern(value = "$type $var = $expr2.$meth1($var1$);")
    })
    public static ErrorDescription compute(HintContext ctx) {
        if (Utilities.isJDKVersionLower(VT_PREVIEW_JDK_VERSION) && !CompilerOptionsQuery.getOptions(ctx.getInfo().getFileObject()).getArguments().contains("--enable-preview")) {
            return null;
        }
        List<String> methList = Arrays.asList("newFixedThreadPool", "newCachedThreadPool");
        String type1 = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$expr2")).getSimpleName().toString();
        String method = ctx.getVariableNames().get("$meth1");
        Collection<? extends TreePath> treePaths = ctx.getMultiVariables().get("$var1$");
        boolean factory = false;
        for (TreePath treePath : treePaths) {
            if (treePath.getLeaf() instanceof IdentifierTree) {
                factory = true;
            }
        }
        if (type1.equals("Executors") && methList.contains(method)) {
            Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), factory).toEditorFix();
            if (factory) {
                return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToThreadPerTask(), fix);
            } else {
                return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ConvertToVT(), fix);
            }
        }
        return null;
    }

    private static final class FixImpl extends JavaFix {

        boolean factory;

        public FixImpl(CompilationInfo info, TreePath main, boolean factory) {
            super(info, main);
            this.factory = factory;
        }

        @Override
        protected String getText() {
            if (!factory) {
                return Bundle.FIX_ConvertToVT();
            } else {
                return Bundle.ERR_ConvertToThreadPerTask();
            }
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            if (!factory) {
                WorkingCopy wc = ctx.getWorkingCopy();
                TreePath main = ctx.getPath();
                MethodInvocationTree method = null;
                if (main.getLeaf() instanceof ExpressionStatementTree) {
                    ExpressionStatementTree expTree = (ExpressionStatementTree) main.getLeaf();
                    AssignmentTree assignTree = (AssignmentTree) expTree.getExpression();
                    method = (MethodInvocationTree) assignTree.getExpression();
                } else if (main.getLeaf() instanceof VariableTree) {
                    VariableTree varTree = (VariableTree) main.getLeaf();
                    method = (MethodInvocationTree) varTree.getInitializer();
                }
                if (method != null) {
                    MemberSelectTree fieldTree = (MemberSelectTree) method.getMethodSelect();
                    wc.rewrite(method, wc.getTreeMaker().MethodInvocation(Collections.emptyList(),
                            (MemberSelectTree) wc.getTreeMaker().MemberSelect(fieldTree.getExpression(), "newVirtualThreadPerTaskExecutor"), Collections.emptyList()));
                }
            }
        }
    }
}
