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

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 * Detects usage of this in anonymous class
 * @author Max Sauer
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.ThisInAnonymous", description = "#DESC_org.netbeans.modules.java.hints.ThisInAnonymous", category="bugs")
public class ThisInAnonymous {
    private static final String THIS_KEYWORD = "this"; // NOI18N

    @TriggerPattern(value="synchronized ($this) { $stmts$; }")
    public static ErrorDescription hint(HintContext ctx) {
        TreePath thisVariable = ctx.getVariables().get("$this");
        if (thisVariable.getLeaf().getKind() != Kind.IDENTIFIER || !((IdentifierTree) thisVariable.getLeaf()).getName().contentEquals(THIS_KEYWORD)) {
            return null;
        }
        
        TreePath anonClassTP = getParentClass(ctx.getPath());
        Element annonClass = ctx.getInfo().getTrees().getElement(anonClassTP);
        String key = getKey(annonClass);
        if (key != null) {
            Element parent = ctx.getInfo().getTrees().getElement(getParentClass(anonClassTP.getParentPath()));

            if (parent == null || (!parent.getKind().isClass() && !parent.getKind().isInterface())) {
                return null;
            }
            
            Fix fix = new FixImpl(TreePathHandle.create(thisVariable, ctx.getInfo()),
                         ElementHandle.create((TypeElement) parent)).toEditorFix();

            String displayName = NbBundle.getMessage(ThisInAnonymous.class, key);
            return ErrorDescriptionFactory.forName(ctx, thisVariable, displayName, fix);
        }

        return null;
    }

    private static TreePath getParentClass(TreePath tp) {
        while (!TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
            tp = tp.getParentPath();
        }
        return tp;
    }

    private static final Set<ElementKind> LOCAL_CLASS_CONTAINERS = EnumSet.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR);
    private static String getKey(Element e) {
        if (e == null) {
            return null;
        }
        ElementKind enclosingKind = e.getKind();
        if (enclosingKind == ElementKind.CLASS) {
            if (e.getSimpleName().length() == 0) return "ERR_ThisInAnonymous";
            if (LOCAL_CLASS_CONTAINERS.contains(e.getEnclosingElement().getKind())) return "ERR_ThisInAnonymousLocal";
        }
        return null;
    }

    private static final class FixImpl extends JavaFix {

        private final ElementHandle<TypeElement> parentClassElementHandle;

        public FixImpl(TreePathHandle thisHandle, ElementHandle<TypeElement> parentClassElementHandle) {
            super(thisHandle);
            this.parentClassElementHandle = parentClassElementHandle;
        }

        public String getText() {
            return NbBundle.getMessage(ThisInAnonymous.class, "FIX_ThisInAnonymous"); // NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            TypeElement parentClass = parentClassElementHandle.resolve(wc);

            assert tp != null;
            assert parentClass != null;
            
            TreeMaker make = wc.getTreeMaker();

            wc.rewrite(tp.getLeaf(), make.MemberSelect(make.QualIdent(parentClass), THIS_KEYWORD));
        }

    }

}
