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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Messages({
    "DN_ExtraCatch=Extra catch clauses",
    "FIX_RemoveCatch=Remove catch clause",
    "# {0} - exception type",
    "FIX_RemoveCatchException=Remove catch {0} clause"
})
public class ExtraCatch implements ErrorRule<Void> {
    
    private static final Set<String> CODES = new HashSet<>(Arrays.asList(
        "compiler.err.except.already.caught",
        "compiler.err.except.never.thrown.in.try"
    ));

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        TreePath catchPath = treePath;
        while (catchPath != null && catchPath.getLeaf().getKind() != Tree.Kind.CATCH) {
            catchPath = catchPath.getParentPath();
        }
        if (catchPath == null) {
            return null;
        }
        String typeName = null;
        // tree path is 'catch' clause or an offending type alternative in the case of multi-catch.
        boolean multiCatch = treePath.getParentPath().getLeaf().getKind() == Tree.Kind.UNION_TYPE;
        if (multiCatch) {
            typeName = compilationInfo.getTypeUtilities().getTypeName(compilationInfo.getTrees().getTypeMirror(treePath)).toString();
        }
        return Arrays.asList(
                new RemoveCatch(compilationInfo, multiCatch ? treePath : catchPath, typeName).toEditorFix());
    }

    @Override
    public String getId() {
        return ExtraCatch.class.getName();
    }

    @Override
    public String getDisplayName() {
        return Bundle.DN_ExtraCatch();
    }

    @Override
    public void cancel() {
    }
    
    private static final class RemoveCatch extends JavaFix {
        final String typeName;
        public RemoveCatch(CompilationInfo info, TreePath tp, String typeName) {
            super(info, tp);
            this.typeName = typeName;
        }

        @Override
        protected String getText() {
            return typeName == null ? Bundle.FIX_RemoveCatch() : Bundle.FIX_RemoveCatchException(typeName);
        }
        
        private void removeAlternativeFromMultiCatch(TransformationContext ctx) throws Exception {
            TreePath unionPath = ctx.getPath().getParentPath();
            UnionTypeTree union = (UnionTypeTree)unionPath.getLeaf();
            TreeMaker mk = ctx.getWorkingCopy().getTreeMaker();
            GeneratorUtilities gen = GeneratorUtilities.get(ctx.getWorkingCopy());
            union = gen.importComments(union, ctx.getWorkingCopy().getCompilationUnit());
            List<? extends Tree> alts = new ArrayList<>(union.getTypeAlternatives());
            alts.remove(ctx.getPath().getLeaf());
            if (alts.size() > 1) {
                // still remains a multi-catch
                
                Tree newUnion = mk.UnionType(alts);
                ctx.getWorkingCopy().rewrite(union, newUnion);
            } else {
                // replace union type with just ordinary type
                ctx.getWorkingCopy().rewrite(union, alts.get(0));
            }
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            Tree t = ctx.getPath().getLeaf();
            if (t.getKind() != Tree.Kind.CATCH) {
                // remove a clause from the multi-catch
                removeAlternativeFromMultiCatch(ctx);
                return;
            }
            CatchTree toRemove = (CatchTree)t;
            TryTree parent = (TryTree) ctx.getPath().getParentPath().getLeaf();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            
            if (parent.getResources().isEmpty() && parent.getCatches().size() == 1) {
                List<StatementTree> repl = new ArrayList<>();
                repl.addAll(parent.getBlock().getStatements());
                if (parent.getFinallyBlock() != null) {
                        repl.addAll(parent.getFinallyBlock().getStatements());
                }
                Utilities.replaceStatement(ctx.getWorkingCopy(), ctx.getPath().getParentPath(), repl);
            } else {
                ctx.getWorkingCopy().rewrite(parent, make.removeTryCatch(parent, toRemove));
            }
        }
        
    }
    
}
