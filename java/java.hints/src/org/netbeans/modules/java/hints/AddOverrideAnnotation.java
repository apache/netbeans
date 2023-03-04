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
