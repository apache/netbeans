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
/*
 * Contributor(s): Alexandru Gyori <Alexandru.Gyori at gmail.com>
 */
package org.netbeans.modules.java.hints.jdk.mapreduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_ForLoopToFunctionalHint", description = "#DESC_ForLoopToFunctionalHint", category = "general", severity = Severity.HINT, minSourceVersion = "8")
@Messages({
    "DN_ForLoopToFunctionalHint=Use Functional Operations",
    "DESC_ForLoopToFunctionalHint=Use functional operations instead of imperative style loop."
})
public class ForLoopToFunctionalHint {

    static boolean DISABLE_CHECK_FOR_STREAM = false;
    
    @TriggerTreeKind(Tree.Kind.ENHANCED_FOR_LOOP)
    @Messages("ERR_ForLoopToFunctionalHint=Can use functional operations")
    public static ErrorDescription computeWarning(HintContext ctx) {
        if (ctx.getInfo().getElements().getTypeElement("java.util.stream.Streams") == null && !DISABLE_CHECK_FOR_STREAM) return null;
        
        PreconditionsChecker pc = new PreconditionsChecker(ctx.getPath().getLeaf(), ctx.getInfo());
        if (pc.isSafeToRefactor()) {
            EnhancedForLoopTree eflt = (EnhancedForLoopTree)ctx.getPath().getLeaf();
            StatementTree stmt = eflt.getStatement();
            if (stmt == null) {
                return null;
            }
            if (stmt.getKind() == Tree.Kind.BLOCK) {
                BlockTree bt = (BlockTree)stmt;
                if (bt.getStatements() == null || bt.getStatements().isEmpty()) {
                    return null;
                }
            }
            Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath(), null).toEditorFix();
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_ForLoopToFunctionalHint(), fix);
        }
        return null;
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp, PreconditionsChecker pc) {
            super(info, tp);
            this.pc = pc;

        }

        @Override
        @Messages("FIX_ForLoopToFunctionalHint=Use Functional Operation")
        protected String getText() {
            return Bundle.FIX_ForLoopToFunctionalHint();
        }
        private PreconditionsChecker pc;
        private Refactorer refactorer;

        @Override
        protected void performRewrite(TransformationContext ctx) {
            EnhancedForLoopTree loop = (EnhancedForLoopTree) ctx.getPath().getLeaf();
            pc = new PreconditionsChecker(loop, ctx.getWorkingCopy());
            refactorer = new Refactorer(loop, ctx.getWorkingCopy(), pc);
            if (pc.isSafeToRefactor() && refactorer.isRefactorable()) {
                ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), refactorer.refactor(ctx.getWorkingCopy().getTreeMaker()));
            }
        }
    }
}
