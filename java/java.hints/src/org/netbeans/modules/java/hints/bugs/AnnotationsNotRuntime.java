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
