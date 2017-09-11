/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.finalize;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
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
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.finalize.FinalizeNotProtected", description = "#DESC_org.netbeans.modules.java.hints.finalize.FinalizeNotProtected", category="finalization",suppressWarnings={"FinalizeNotProtected"},enabled=false)    //NOI18N
public class FinalizeNotProtected {

    @TriggerTreeKind(Kind.METHOD)
    public static ErrorDescription hint(final HintContext ctx) {
        assert ctx != null;
        final TreePath tp = ctx.getPath();
        final MethodTree tree = (MethodTree) tp.getLeaf();
        if (Util.isFinalize(tree)) {
            final Set<Modifier> modifiers = tree.getModifiers().getFlags();
            if (modifiers.contains(Modifier.PUBLIC)) {
                return ErrorDescriptionFactory.forName(ctx, tp,
                        NbBundle.getMessage(FinalizeNotProtected.class, "TXT_FinalizeNotProtected"),
                        new FixImpl(TreePathHandle.create(tp, ctx.getInfo())).toEditorFix());    //NOI18N
            }
        }
        return null;
    }

    static class FixImpl extends JavaFix {

        FixImpl(final TreePathHandle handle) {
            super(handle);
            assert handle != null;
        }

        public String getText() {
            return NbBundle.getMessage(FinalizeNotProtected.class, "FIX_FinalizeNotProtected_MakePublic");
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            final Tree tree = tp.getLeaf();
            if (tree.getKind() != Tree.Kind.METHOD) {
                return;
            }
            final TreeMaker tm = wc.getTreeMaker();
            wc.rewrite(((MethodTree)tree).getModifiers(), tm.addModifiersModifier(
                    tm.removeModifiersModifier(((MethodTree)tree).getModifiers(), Modifier.PUBLIC),
                    Modifier.PROTECTED));
        }
    }
}
