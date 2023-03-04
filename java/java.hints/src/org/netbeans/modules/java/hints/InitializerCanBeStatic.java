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
