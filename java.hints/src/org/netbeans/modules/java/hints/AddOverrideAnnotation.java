/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.overridden.AnnotationType;
import org.netbeans.modules.java.editor.overridden.ComputeOverriding;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@Hint(displayName="#DN_AddOverrideAnnotation", description="#DESC_AddOverrideAnnotation", category="rules15", suppressWarnings="override")
public class AddOverrideAnnotation {

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD);
    }

    @TriggerTreeKind(Kind.METHOD)
    public static ErrorDescription run(HintContext ctx) {
        CompilationInfo compilationInfo = ctx.getInfo();
        TreePath treePath = ctx.getPath();
        TypeElement el = compilationInfo.getElements().getTypeElement("java.lang.Override"); //NOI18N

        if (el == null || !GeneratorUtils.supportsOverride(compilationInfo))
            return null;

        Element e = compilationInfo.getTrees().getElement(treePath);

        if (e != null && e.getKind() == ElementKind.METHOD) {
            ExecutableElement ee = (ExecutableElement) e;
            List<ElementDescription> result = new ArrayList<ElementDescription>();

            Element enclEl = ee.getEnclosingElement();
            if (!enclEl.getKind().isClass() && !enclEl.getKind().isInterface())
                return null;

            AnnotationType type = ComputeOverriding.detectOverrides(compilationInfo, (TypeElement) enclEl, ee, result);

            boolean hasOverriddenAnnotation = false;

            for (AnnotationMirror am : ee.getAnnotationMirrors()) {
                if (compilationInfo.getTypes().isSameType(am.getAnnotationType(), el.asType())) {
                    hasOverriddenAnnotation = true;
                    break;
                }
            }

            if (hasOverriddenAnnotation) {
                return null;
            }

            boolean addHint = false;

            if (type == AnnotationType.OVERRIDES) {
                addHint = true;
            } else {
                if (type == AnnotationType.IMPLEMENTS) {
                    addHint = compilationInfo.getSourceVersion() != SourceVersion.RELEASE_5;
                }
            }

            if (addHint) {
                String desc = NbBundle.getMessage(AddOverrideAnnotation.class, "HINT_AddOverrideAnnotation");
                return ErrorDescriptionFactory.forName(ctx, treePath, desc, new FixImpl(compilationInfo, treePath).toEditorFix());
            }
        }
        
        return null;
    }

    private static final class FixImpl extends JavaFix {
        
        public FixImpl(CompilationInfo info, TreePath treePath) {
            super(info, treePath);
        }
        
        public String getText() {
            return NbBundle.getMessage(AddOverrideAnnotation.class, "FIX_AddOverrideAnnotation");
        }
        
        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.METHOD, Kind.VARIABLE);

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath path = ctx.getPath();
            while (path.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(path.getLeaf().getKind())) {
                path = path.getParentPath();
            }

            if (path.getLeaf().getKind() == Kind.COMPILATION_UNIT) {
                return ;
            }

            Tree top = path.getLeaf();
            ModifiersTree modifiers = null;

            switch (top.getKind()) {
            case METHOD:
                modifiers = ((MethodTree) top).getModifiers();
                break;
            default: assert false : "Unhandled Tree.Kind"; //NOI18N
            }

            if (modifiers == null) {
                return ;
            }

            TypeElement el = copy.getElements().getTypeElement("java.lang.Override"); //NOI18N

            if (el == null) {
                return ;
            }

            //verify @Override annotation still does not exist:
            for (AnnotationTree at : modifiers.getAnnotations()) {
                TreePath tp = new TreePath(new TreePath(path, at), at.getAnnotationType());
                Element  e  = copy.getTrees().getElement(tp);

                if (el.equals(e)) {
                    //found existing :
                    return ;
                }
            }

            List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(modifiers.getAnnotations());
            annotations.add(copy.getTreeMaker().Annotation(copy.getTreeMaker().QualIdent(el), Collections.<ExpressionTree>emptyList()));

            ModifiersTree nueMods = copy.getTreeMaker().Modifiers(modifiers, annotations);

            copy.rewrite(modifiers, nueMods);
        }
    }
    
}
