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
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.web.jsf.hints.JsfHintsContext;
import org.netbeans.modules.web.jsf.hints.JsfHintsUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * Checks usage of the deprecated package javax.faces.bean.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#JavaxFacesBeanIsGonnaBeDeprecated.display.name",
        description = "#JavaxFacesBeanIsGonnaBeDeprecated.err",
        id = "o.n.m.web.jsf.hints.JavaxFacesBeanIsGonnaBeDeprecated",
        category = "javaee/jsf",
        enabled = true,
        suppressWarnings = "JavaxFacesBeanIsGonnaBeDeprecated")
@NbBundle.Messages({
    "JavaxFacesBeanIsGonnaBeDeprecated.display.name=Classes of javax.faces.bean are gonna be deprecated",
    "JavaxFacesBeanIsGonnaBeDeprecated.err=Annotations from the package javax.faces.bean will be deprecated in the next JSF version. CDI and Java EE ones are recommended instead."
})
public class JavaxFacesBeanIsGonnaBeDeprecated {

    private static final Logger LOG = Logger.getLogger(JavaxFacesBeanIsGonnaBeDeprecated.class.getName());

    private static final String JAVAX_FACES_BEAN = "javax.faces.bean"; //NOI18N
    private static final String MANAGED_BEAN = JAVAX_FACES_BEAN + ".ManagedBean"; //NOI18N

    /** Classes which can be switched to Java EE or CDI ones. */
    private static final Map<String, String> DEPRECATED_TO_FIX = new HashMap<>();
    static {
        // scopes
        DEPRECATED_TO_FIX.put(JAVAX_FACES_BEAN + ".RequestScoped", "javax.enterprise.context.RequestScoped"); //NOI18N
        DEPRECATED_TO_FIX.put(JAVAX_FACES_BEAN + ".SessionScoped", "javax.enterprise.context.SessionScoped"); //NOI18N
        DEPRECATED_TO_FIX.put(JAVAX_FACES_BEAN + ".ApplicationScoped", "javax.enterprise.context.ApplicationScoped"); //NOI18N
        DEPRECATED_TO_FIX.put(JAVAX_FACES_BEAN + ".ViewScoped", "javax.faces.view.ViewScoped"); //NOI18N
        // beans
        DEPRECATED_TO_FIX.put(MANAGED_BEAN, "javax.inject.Named"); //NOI18N
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        List<ErrorDescription> problems = new ArrayList<>();
        final JsfHintsContext ctx = JsfHintsUtils.getOrCacheContext(hintContext);

        if (ctx.getJsfVersion() == null || !ctx.getJsfVersion().isAtLeast(JSFVersion.JSF_2_2)) {
            return problems;
        }

        CompilationInfo info = hintContext.getInfo();
        for (TypeElement typeElement : info.getTopLevelElements()) {
            for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
                if (annotationMirror.getAnnotationType().toString().startsWith(JAVAX_FACES_BEAN)) {
                    // it's javax.faces.bean annotation
                    Tree tree = info.getTrees().getTree(typeElement, annotationMirror);
                    List<Fix> fixes = getFixesForType(info, typeElement, annotationMirror);
                    problems.add(JsfHintsUtils.createProblem(
                            tree,
                            info,
                            Bundle.JavaxFacesBeanIsGonnaBeDeprecated_display_name(),
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
            "ChangeClassFix.lbl.change.class.fix=Change {0} to the {1}"
        })
        @Override
        public String getText() {
            return Bundle.ChangeClassFix_lbl_change_class_fix(deprecatedClass, replacingClass);
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
