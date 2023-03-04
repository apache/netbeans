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
