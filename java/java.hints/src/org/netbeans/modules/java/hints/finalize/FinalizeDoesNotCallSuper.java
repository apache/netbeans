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

package org.netbeans.modules.java.hints.finalize;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.finalize.FinalizeDoesNotCallSuper", description = "#DESC_org.netbeans.modules.java.hints.finalize.FinalizeDoesNotCallSuper", category="finalization",suppressWarnings={"FinalizeDoesntCallSuperFinalize"})  //NOI18N
public class FinalizeDoesNotCallSuper {

    private static final String SUPER = "super";    //NOI18N
    private static final String FINALIZE = "finalize";  //NOI18N

    @TriggerTreeKind(Tree.Kind.METHOD)
    public static ErrorDescription hint(final HintContext ctx) {
        assert ctx != null;
        final TreePath tp = ctx.getPath();
        final MethodTree method = (MethodTree) tp.getLeaf();
        if (method.getBody() == null) return null;
        if (!Util.isFinalize(method)) {
            return null;
        }
        final FindSuper scanner = new FindSuper();
        scanner.scan(method, null);
        if (scanner.found) {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, method, NbBundle.getMessage(FinalizeDoesNotCallSuper.class, "TXT_FinalizeDoesNotCallSuper"),
                new FixImpl(TreePathHandle.create(ctx.getPath(), ctx.getInfo())).toEditorFix());
    }

    static final class FindSuper extends ErrorAwareTreeScanner<Void, Void> {

        boolean found;

        @Override
        public Void scan(Tree node, Void p) {
            return found ? null : super.scan(node, p);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
            if (!node.getArguments().isEmpty()) {
                return null;
            }
            final ExpressionTree et = node.getMethodSelect();
            if (et.getKind() != Tree.Kind.MEMBER_SELECT) {
                return null;
            }
            final MemberSelectTree mst = (MemberSelectTree) et;
            if (!FINALIZE.contentEquals(mst.getIdentifier())) {
                return null;
            }
            if (mst.getExpression().getKind() != Tree.Kind.IDENTIFIER) {
                return null;
            }
            if (!SUPER.contentEquals(((IdentifierTree)mst.getExpression()).getName())) {
                return null;
            }
            found = true;
            return null;
        }
    }

    static class FixImpl extends JavaFix {

        public FixImpl(final TreePathHandle handle) {
            super(handle);
            assert handle != null;
        }


        @Override
        public String getText() {
            return NbBundle.getMessage(FinalizeDoesNotCallSuper.class, "FIX_FinalizeDoesNotCallSuper");
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            final TreeMaker tm = wc.getTreeMaker();
            TreePath tp = ctx.getPath();
            final BlockTree oldBody = ((MethodTree)tp.getLeaf()).getBody();
            if (oldBody == null) {
                return;
            }
            final List<StatementTree> newStatements = new ArrayList<StatementTree>(2);
            BlockTree superFinalize = tm.Block(
                                        Collections.singletonList(
                                            tm.ExpressionStatement(
                                                tm.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                                    tm.MemberSelect(
                                                        tm.Identifier(SUPER),
                                                        FINALIZE), Collections.<ExpressionTree>emptyList()))),
                                        false);
            if (oldBody.getStatements().isEmpty()) {
                wc.rewrite(oldBody, superFinalize);
            } else {
                TryTree soleTry = soleTryWithoutFinally(oldBody);
                
                if (soleTry != null) {
                    wc.rewrite(soleTry, tm.Try(soleTry.getBlock(), soleTry.getCatches(), superFinalize));
                } else {
                    wc.rewrite(oldBody, tm.Block(Collections.singletonList(tm.Try(oldBody, Collections.<CatchTree>emptyList(), superFinalize)), false));
                }
            }
        }
        
        private TryTree soleTryWithoutFinally(BlockTree block) {
            if (block.getStatements().size() != 1) return null;
            StatementTree first = block.getStatements().get(0);
            if (first.getKind() != Kind.TRY) return null;
            TryTree tt = (TryTree) first;
            if (tt.getFinallyBlock() != null) return null;
            return tt;
        }
    }
}
