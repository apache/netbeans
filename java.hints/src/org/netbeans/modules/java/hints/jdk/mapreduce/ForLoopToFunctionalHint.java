/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012-2013 Sun Microsystems, Inc.
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
import javax.lang.model.SourceVersion;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_ForLoopToFunctionalHint", description = "#DESC_ForLoopToFunctionalHint", category = "general",
        minSourceVersion = "8")
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
