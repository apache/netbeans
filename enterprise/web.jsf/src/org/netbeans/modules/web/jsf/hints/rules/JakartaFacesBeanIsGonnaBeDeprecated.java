/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.hints.rules;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.web.jsf.hints.JsfHintsContext;
import org.netbeans.modules.web.jsf.hints.JsfHintsUtils;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * Checks usage of the deprecated package jakarta.faces.bean.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#JakartaFacesBeanIsGonnaBeDeprecated.display.name",
        description = "#JakartaFacesBeanIsGonnaBeDeprecated.err",
        id = "o.n.m.web.jsf.hints.JakartaFacesBeanIsGonnaBeDeprecated",
        category = "javaee/jsf",
        enabled = true,
        suppressWarnings = "JakartaFacesBeanIsGonnaBeDeprecated")
@NbBundle.Messages({
    "JakartaFacesBeanIsGonnaBeDeprecated.display.name=Classes of jakarta.faces.bean are deprecated",
    "JakartaFacesBeanIsGonnaBeDeprecated.err=Annotations from the package jakarta.faces.bean are deprecated. CDI and Java EE ones are recommended instead."
})
public class JakartaFacesBeanIsGonnaBeDeprecated {

    private static final Logger LOG = Logger.getLogger(JakartaFacesBeanIsGonnaBeDeprecated.class.getName());

    private static final String JAKARTA_FACES_BEAN = "jakarta.faces.bean"; //NOI18N
    private static final String MANAGED_BEAN = JAKARTA_FACES_BEAN + ".ManagedBean"; //NOI18N

    /** Classes which can be switched to Java EE or CDI ones. */
    private static final Map<String, String> DEPRECATED_TO_FIX = new HashMap<>();
    static {
        // scopes
        DEPRECATED_TO_FIX.put(JAKARTA_FACES_BEAN + ".RequestScoped", "jakarta.enterprise.context.RequestScoped"); //NOI18N
        DEPRECATED_TO_FIX.put(JAKARTA_FACES_BEAN + ".SessionScoped", "jakarta.enterprise.context.SessionScoped"); //NOI18N
        DEPRECATED_TO_FIX.put(JAKARTA_FACES_BEAN + ".ApplicationScoped", "jakarta.enterprise.context.ApplicationScoped"); //NOI18N
        DEPRECATED_TO_FIX.put(JAKARTA_FACES_BEAN + ".ViewScoped", "jakarta.faces.view.ViewScoped"); //NOI18N
        // beans
        DEPRECATED_TO_FIX.put(MANAGED_BEAN, "jakarta.inject.Named"); //NOI18N
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        List<ErrorDescription> problems = new ArrayList<>();
        final JsfHintsContext ctx = JsfHintsUtils.getOrCacheContext(hintContext);

        if (ctx.getJsfVersion() == null || !ctx.getJsfVersion().isAtLeast(JsfVersion.JSF_2_2)) {
            return problems;
        }

        CompilationInfo info = hintContext.getInfo();
        for (TypeElement typeElement : info.getTopLevelElements()) {
            for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
                if (annotationMirror.getAnnotationType().toString().startsWith(JAKARTA_FACES_BEAN)) {
                    // it's jakarta.faces.bean annotation
                    Tree tree = info.getTrees().getTree(typeElement, annotationMirror);
                    List<Fix> fixes = getFixesForType(info, typeElement, annotationMirror);
                    problems.add(JsfHintsUtils.createProblem(
                            tree,
                            info,
                            Bundle.JakartaFacesBeanIsGonnaBeDeprecated_display_name(),
                            Severity.HINT,
                            fixes));
                }
            }
        }
        return problems;
    }

    private static List<Fix> getFixesForType(CompilationInfo info, TypeElement typeElement, AnnotationMirror am) {
        List<Fix> fixes = new ArrayList<>();
        String annotationType = am.getAnnotationType().toString();
        if (DEPRECATED_TO_FIX.containsKey(annotationType)) {
            TreePath path = info.getTrees().getPath(typeElement, am);
            fixes.add(new ChangeClassFix(info, path, typeElement, am, annotationType, DEPRECATED_TO_FIX.get(annotationType)).toEditorFix());
        }
        return fixes;
    }

    private static final class ChangeClassFix extends JavaFix {

        private final TypeElement element;
        private final AnnotationMirror annotation;
        private final String deprecatedClass;
        private final String replacingClass;

        public ChangeClassFix(CompilationInfo info, TreePath path, TypeElement element, AnnotationMirror annotation, String deprecatedClass, String replacingClass) {
            super(info, path);
            this.element = element;
            this.annotation = annotation;
            this.deprecatedClass = deprecatedClass;
            this.replacingClass = replacingClass;
        }

        @NbBundle.Messages({
            "JakartaChangeClassFix.lbl.change.class.fix=Change {0} to the {1}"
        })
        @Override
        public String getText() {
            return Bundle.JakartaChangeClassFix_lbl_change_class_fix(deprecatedClass, replacingClass);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            wc.toPhase(JavaSource.Phase.RESOLVED);
            TreeMaker make = wc.getTreeMaker();

            // rewrite annotations in case of ManagedBean
            if (MANAGED_BEAN.equals(annotation.getAnnotationType().toString())) {
                ModifiersTree modifiers = ((ClassTree) wc.getTrees().getTree(element)).getModifiers();
                AnnotationTree annotationTree = (AnnotationTree) wc.getTrees().getTree(element, annotation);
                List<ExpressionTree> arguments = new ArrayList<>();
                for (ExpressionTree expressionTree : annotationTree.getArguments()) {
                    if (expressionTree.getKind() == Tree.Kind.ASSIGNMENT) {
                        AssignmentTree at = (AssignmentTree) expressionTree;
                        String varName = ((IdentifierTree) at.getVariable()).getName().toString();
                        if (varName.equals("name")) { //NOI18N
                            ExpressionTree valueTree = make.Identifier(at.getExpression().toString());
                            arguments.add(valueTree);
                        }
                    }
                }
                ModifiersTree newModifiersTree = make.removeModifiersAnnotation(modifiers, (AnnotationTree) wc.getTrees().getTree(element, annotation));
                AnnotationTree newTree = GenerationUtils.newInstance(wc).createAnnotation(replacingClass, arguments);
                newModifiersTree = make.addModifiersAnnotation(newModifiersTree, newTree);
                wc.rewrite(modifiers, newModifiersTree);
            }

            // rewrite imports
            List<? extends ImportTree> imports = wc.getCompilationUnit().getImports();
            ImportTree newImportTree = make.Import(make.QualIdent(replacingClass), false);
            for (ImportTree importTree : imports) {
                if (deprecatedClass.equals(importTree.getQualifiedIdentifier().toString())) {
                    wc.rewrite(importTree, newImportTree);
                }
            }

        }
    }
}
