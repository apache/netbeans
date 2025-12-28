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
package org.netbeans.modules.j2ee.ejbverification.rules;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 * Hint that checks usage of @PostConstruct annotation. Its return value, singularity per class, parameters etc.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#AnnotationPostContruct.display.name",
            description = "#AnnotationPostContruct.description",
            id = "o.n.m.j2ee.ejbverification.AnnotationPostContruct",
            category = "javaee/ejb",
            enabled = true,
            suppressWarnings = "AnnotationPostContruct")
@Messages({
    "AnnotationPostContruct.display.name=Annotation @PostConstruct",
    "AnnotationPostContruct.description=Checks usage of @PostConstruct annotation. Its return value, singularity per class, parameters etc.",
    "AnnotationPostContruct_too_much_annotations=There cannot be more than one method annotated @PostConstruct",
    "AnnotationPostContruct_wrong_return_type=Return type of @PostConstruct annotated method must be void.",
    "AnnotationPostContruct_thrown_checked_exceptions=@PostConstruct annotated method must not throw a checked exception.",
    "AnnotationPostContruct_wrong_parameters=@PostConstruct annotated method must not have any parameters except in the case of EJB interceptors in which case it takes an InvocationContext subclasses."
})
public final class AnnotationPostContruct {

    private AnnotationPostContruct() { }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static List<ErrorDescription> run(HintContext hintCtx) {
        EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintCtx);
        if (ctx != null) {
            EjbJar ejbModule = ctx.getEjbModule();
            Profile profile = ejbModule.getJ2eeProfile();

            // not EE6+ project
            if (profile == null || !profile.isAtLeast(Profile.JAVA_EE_6_WEB)) {
                return Collections.emptyList();
            }

            List<ExecutableElement> allMethods = ElementFilter.methodsIn(ctx.getClazz().getEnclosedElements());
            List<ExecutableElement> eligibleMethods = new ArrayList<>();
            List<ErrorDescription> problems = new ArrayList<>();

            for (ExecutableElement method : allMethods) {
                if (isEligibleMethod(method)) {
                    eligibleMethods.add(method);
                }
            }

            // more than one annotated method
            if (eligibleMethods.size() > 1) {
                for (ExecutableElement problematicMethods : eligibleMethods) {
                    problems.add(HintsUtils.createProblem(
                            problematicMethods,
                            hintCtx.getInfo(),
                            Bundle.AnnotationPostContruct_too_much_annotations()));
                }
            }

            for (ExecutableElement method : eligibleMethods) {
                // wrong return type
                if (!"void".equals(method.getReturnType().toString())) { //NOI18N
                    problems.add(HintsUtils.createProblem(
                            method,
                            hintCtx.getInfo(),
                            Bundle.AnnotationPostContruct_wrong_return_type()));
                }
                // cannot throw unchecked exceptions
                if (!method.getThrownTypes().isEmpty() && throwsCheckedException(hintCtx.getInfo(), method.getThrownTypes())) {
                    problems.add(HintsUtils.createProblem(
                            method,
                            hintCtx.getInfo(),
                            Bundle.AnnotationPostContruct_thrown_checked_exceptions()));
                }
                // no parameter except in the case of EJB interceptor
                List<? extends VariableElement> parameters = method.getParameters();
                if (!parameters.isEmpty()
                        && (parameters.size() > 1 || !isEjbInterceptor(hintCtx.getInfo(), method))) {
                    problems.add(HintsUtils.createProblem(
                            method,
                            hintCtx.getInfo(),
                            Bundle.AnnotationPostContruct_wrong_parameters()));
                }
            }

            return problems;
        }
        return Collections.emptyList();
    }

    private static boolean isEjbInterceptor(CompilationInfo info, ExecutableElement method) {
        VariableElement parameter = method.getParameters().get(0);
        String paramType = parameter.asType().toString();
        TypeElement element = info.getElements().getTypeElement(paramType);
        if (element != null) { //NOI18N
            if (JavaUtils.isTypeOf(info, element, "javax.interceptor.InvocationContext")  //NOI18N
                    || JavaUtils.isTypeOf(info, element, "jakarta.interceptor.InvocationContext")) { //NOI18N
                return true;
            }
        }
        return false;
    }

    private static boolean throwsCheckedException(CompilationInfo info, List<? extends TypeMirror> thrownTypes) {
        for (TypeMirror typeMirror : thrownTypes) {
            boolean runtimeException = false;
            TypeElement element = info.getElements().getTypeElement(typeMirror.toString());
            if (element != null) { //NOI18N
                if (JavaUtils.isTypeOf(info, element, "java.lang.RuntimeException")) { //NOI18N
                    runtimeException = true;
                }
            }
            if (!runtimeException) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEligibleMethod(ExecutableElement method) {
        boolean knownClasses = HintsUtils.isContainingKnownClasses(method);
        for (AnnotationMirror am : method.getAnnotationMirrors()) {
            if ((EJBAPIAnnotations.POST_CONSTRUCT.equals(am.getAnnotationType().asElement().toString())
                    || EJBAPIAnnotations.POST_CONSTRUCT_JAKARTA.equals(am.getAnnotationType().asElement().toString()))
                    && knownClasses) {
                return true;
            }
        }
        return false;
    }

}
