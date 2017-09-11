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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.introduce.InstanceRefFinder;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "TEXT_InitializerCanBeStatic=Initializer can be static",
    "FIX_InitializerCanBeStatic=Make initializer static"
})
@Hint(
    displayName = "#DN_InitializerMayBeStatic",
    description = "#DESC_InitializerMayBeStatic",
    enabled = true,
    category = "class_structure",
    suppressWarnings = { "InitializerMayBeStatic", "", "ClassInitializerMayBeStatic" }
)
public class InitializerCanBeStatic {
    @TriggerTreeKind(Tree.Kind.BLOCK)
    public static ErrorDescription run(HintContext ctx) {
        TreePath path = ctx.getPath();
        if (((BlockTree)path.getLeaf()).isStatic()) {
            return null;
        }
        TreePath parentPath = path.getParentPath();
        if (parentPath == null) {
            return null;
        }
        Tree l = parentPath.getLeaf();
        if (!(l instanceof ClassTree)) {
            return null;
        }
        Element el = ctx.getInfo().getTrees().getElement(parentPath);
        if (el == null || !el.getKind().isClass()) {
            return null;
        }
        TypeElement tel = (TypeElement)el;
        // do not suggest for anonymous classes, local classes or members which are not static.
        if (tel.getNestingKind() != NestingKind.TOP_LEVEL && 
            (tel.getNestingKind() != NestingKind.MEMBER || !tel.getModifiers().contains(Modifier.STATIC))) {
            return null;
        }
        InstanceRefFinder finder = new InstanceRefFinder(ctx.getInfo(), path);
        finder.process();
        if (finder.containsInstanceReferences() || finder.containsReferencesToSuper()) {
            return null;
        }
        
        return ErrorDescriptionFactory.forTree(ctx, path, Bundle.TEXT_InitializerCanBeStatic(),
                new MakeInitStatic(TreePathHandle.create(path, ctx.getInfo())).toEditorFix());
    }
    
    private static class MakeInitStatic extends JavaFix {

        public MakeInitStatic(TreePathHandle handle) {
            super(handle);
        }
        
        @Override
        protected String getText() {
            return Bundle.FIX_InitializerCanBeStatic();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            Tree t = ctx.getPath().getLeaf();
            if (t.getKind() != Tree.Kind.BLOCK) {
                return;
            }
            BlockTree bl = (BlockTree)t;
            WorkingCopy wc = ctx.getWorkingCopy();
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            gu.importComments(bl, wc.getCompilationUnit());
            TreeMaker mk = wc.getTreeMaker();
            BlockTree nbl = mk.Block(bl.getStatements(), true);
            gu.copyComments(bl, nbl, true);
            gu.copyComments(bl, nbl, false);
            wc.rewrite(bl, nbl);
        }
        
    }
}
