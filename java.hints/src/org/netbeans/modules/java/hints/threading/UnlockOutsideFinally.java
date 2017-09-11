/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.threading;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.threading.Tiny.unlockOutsideTryFinally", description = "#DESC_org.netbeans.modules.java.hints.threading.Tiny.unlockOutsideTryFinally", category="thread", suppressWarnings="LockAcquiredButNotSafelyReleased")
public class UnlockOutsideFinally {
    
    @NbBundle.Messages({
        "FIX_MoveUnlockToFinally=Move unlock() to finally",
        "FIX_RemoveRedundantUnlock=Remove extra unlock() call"
    })
    private static class MoveUnlockFix extends JavaFix {
        private TreePathHandle  finHandle;
        
        public MoveUnlockFix(TreePathHandle handle, TreePathHandle finHandle) {
            super(handle);
            this.finHandle = finHandle;
        }

        @Override
        protected String getText() {
            return finHandle == null ? Bundle.FIX_RemoveRedundantUnlock() : Bundle.FIX_MoveUnlockToFinally();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            TreePath finPath = null;
            if (finHandle != null) {
                finPath = finHandle.resolve(ctx.getWorkingCopy());
                if (finPath == null) {
                    // report ?
                    return;
                }
            }
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker mk = wc.getTreeMaker();
            
            TreePath p = ctx.getPath();
            if (!StatementTree.class.isAssignableFrom(p.getLeaf().getKind().asInterface())) {
                if (p.getParentPath() != null && 
                    p.getParentPath().getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                    p = p.getParentPath();
                } else {
                    return;
                }
            }

            Utilities.removeStatement(wc, p);
            if (finPath != null) {
                Utilities.insertStatement(wc, finPath, null,
                        Collections.singletonList((StatementTree)p.getLeaf()),
                        null, 
                        Utilities.INSERT_POS_CHILD
                );
            }
        }
    }
    
    @TriggerPatterns({
        @TriggerPattern(value="$lock.lock(); $otherStats$; try { $statements$; $lock.unlock(); $rest$; } catch $catches$ finally { $finstats$; } ",
                        constraints=@ConstraintVariableType(variable="$lock", type="java.util.concurrent.locks.Lock")),
        @TriggerPattern(value="$lock.lock(); $otherStats$; try { $statements$; $lock.unlock(); $rest$; } catch $catches$",
                        constraints=@ConstraintVariableType(variable="$lock", type="java.util.concurrent.locks.Lock")),
        @TriggerPattern(value="$lock.lock(); $otherStats$; try { $statements$; } catch $catches$ catch($excType $var) { $catchStats1$; $lock.unlock(); $catchStats2$; } catch $catches2$ finally { $finstmts$; }",
                        constraints=@ConstraintVariableType(variable="$lock", type="java.util.concurrent.locks.Lock")),
    })
    @NbBundle.Messages({
        "ERR_UnlockOutsideTryFinally=Lock.lock() not unlocked in finally",
        "FIX_UnlockOutsideTryFinally=Wrap by try-finally",
        "MSG_ExtraUnlock=Extra unlock() call; lock is already released in finally"
    })
    public static ErrorDescription unlockInsideTry(HintContext ctx) {
        TreePath fin = ctx.getVariables().get("$lock$1");
        if (fin == null) {
            return null;
        }
        TreePath parent = fin.getParentPath();
        if (parent.getLeaf().getKind() != Tree.Kind.MEMBER_SELECT) {
            return null;
        }
        parent = parent.getParentPath();
        if (parent == null || parent.getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION) {
            return null;
        }
        TreePath tPath = parent.getParentPath();
        while (tPath != null && tPath.getLeaf().getKind() != Tree.Kind.TRY) {
            if (tPath.getLeaf().getKind() == Tree.Kind.METHOD || 
                tPath.getLeaf().getKind() == Tree.Kind.CLASS) {
                return null;
            }
            tPath = tPath.getParentPath();
        }
        if (tPath == null) {
            return null;
        }
        TryTree tt = (TryTree)tPath.getLeaf();
        Fix f = null;
        
        String displayName = null;
        
        if (tt.getFinallyBlock() != null) {
            
            TreePath finBlockPath = new TreePath(tPath, tt.getFinallyBlock());
            Collection<? extends Occurrence> occ = Matcher.create(ctx.getInfo()).
                setSearchRoot(finBlockPath).
                match(
                    Pattern.createSimplePattern(parent)
                );
            if (!occ.isEmpty()) {
                f = new MoveUnlockFix(
                        TreePathHandle.create(parent, ctx.getInfo()),
                        null).toEditorFix();
                displayName = Bundle.MSG_ExtraUnlock();
            }
        }
        if (f == null) {
            displayName = Bundle.ERR_UnlockOutsideTryFinally();
            f = new MoveUnlockFix(
                    TreePathHandle.create(parent, ctx.getInfo()),
                    TreePathHandle.create(tPath, ctx.getInfo())).toEditorFix();
        }
        
        return ErrorDescriptionFactory.forName(ctx, parent, displayName, f);
    }
    
    @TriggerPatterns({
        @TriggerPattern(value="$lock.lock(); $statements$; $lock.unlock();",
                        constraints=@ConstraintVariableType(variable="$lock", type="java.util.concurrent.locks.Lock")),
    })

    public static ErrorDescription unlockOutsideTryFinally(HintContext ctx) {
        if (ctx.getMultiVariables().get("$statements$").isEmpty()) return null; //#186434
        String fixDisplayName = NbBundle.getMessage(Tiny.class, "FIX_UnlockOutsideTryFinally");
        String lockString = ctx.getVariables().containsKey("$lock") ? "$lock." : ""; // NOI18N
        
        
        Fix f= JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), 
                lockString + "lock(); try {$statements$;} finally {" + lockString + "unlock();}");
        String displayName = NbBundle.getMessage(Tiny.class, "ERR_UnlockOutsideTryFinally");

        //XXX:
        Tree mark;
        Tree matched = ctx.getPath().getLeaf();

        if (matched.getKind() == Tree.Kind.BLOCK) {
            List<? extends StatementTree> s = ((BlockTree) matched).getStatements();
            int count = ctx.getMultiVariables().get("$$1$").size();

            mark = s.get(count);
        } else {
            mark = matched;
        }

        return ErrorDescriptionFactory.forName(ctx, mark, displayName, f);
    }
}
