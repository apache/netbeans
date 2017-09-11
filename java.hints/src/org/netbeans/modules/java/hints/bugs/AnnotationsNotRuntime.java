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

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.util.TreePath;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map.Entry;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.AnnotationsNotRuntime", description = "#DESC_org.netbeans.modules.java.hints.bugs.AnnotationsNotRuntime", category="bugs", suppressWarnings="ReflectionForUnavailableAnnotation", options=Options.QUERY)
public class AnnotationsNotRuntime {

    @TriggerPattern(value="$el.isAnnotationPresent($annotation.class)",
                    constraints={
                        @ConstraintVariableType(variable="$el", type="java.lang.reflect.AnnotatedElement")
                    }
                   )
//    @TriggerPattern(value="$el.isAnnotationPresent($annotationClass)",
//                    constraints={
//                        @Constraint(variable="$el", type="java.lang.reflect.AnnotatedElement"),
//                        @Constraint(variable="$annotationClass", type="java.lang.Class")
//                    }
//                   )
    public static ErrorDescription isAnnotation(HintContext ctx) {
        return hint(ctx, "DN_AnnotationsNotRuntime_isAnnotation");
    }

    @TriggerPattern(value="$el.getAnnotation($annotation.class)",
                    constraints={
                        @ConstraintVariableType(variable="$el", type="java.lang.reflect.AnnotatedElement")
                    }
                   )
//    @TriggerPattern(value="$el.getAnnotation($annotationClass)",
//                    constraints={
//                        @Constraint(variable="$el", type="java.lang.reflect.AnnotatedElement"),
//                        @Constraint(variable="$annotationClass", type="java.lang.Class")
//                    }
//                   )
    public static ErrorDescription getAnnotation(HintContext ctx) {
        return hint(ctx, "DN_AnnotationsNotRuntime_getAnnotation");
    }

    @TriggerPattern(value="$ann instanceof $annotation",
                    constraints={
                        @ConstraintVariableType(variable="$ann", type="java.lang.annotation.Annotation")
                    }
                   )
    public static ErrorDescription instanceOf(HintContext ctx) {
        return hint(ctx, "DN_AnnotationsNotRuntime_instanceof");
    }

    private static ErrorDescription hint(HintContext ctx, String bundleKey) {
        TreePath annotationPath = ctx.getVariables().get("$annotation");
        Element annotation = ctx.getInfo().getTrees().getElement(annotationPath);

        if (annotation == null || annotation.getKind() != ElementKind.ANNOTATION_TYPE) {
            return null;
        }

        for (AnnotationMirror am : annotation.getAnnotationMirrors()) {
            Name fqn = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();

            if (fqn.contentEquals(Retention.class.getName())) {
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    if (e.getKey().getSimpleName().contentEquals("value")) {
                        Object val = e.getValue().getValue();

                        if (val instanceof VariableElement && ((VariableElement) val).getKind() == ElementKind.ENUM_CONSTANT) {
                            VariableElement ve = (VariableElement) val;

                            if (ve.getSimpleName().contentEquals(RetentionPolicy.RUNTIME.name())) {
                                return null;
                            }
                        }

                        break;
                    }
                }
                break;
            }
        }

        String fqn = ((TypeElement) annotation).getQualifiedName().toString();
        
        return ErrorDescriptionFactory.forName(ctx, annotationPath, NbBundle.getMessage(AnnotationsNotRuntime.class, bundleKey, fqn));
    }
}
