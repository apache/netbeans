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
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 * Checks that @Asynchronous are annotated methods of 3.1+ Full profile or methods declared in Local or No-Interface
 * View of the EJB3.2 Lite project (project targeting 3.2 Web profile).
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#AsynchronousMethodInvocation.display.name",
            description = "#AsynchronousMethodInvocation.description",
            id = "o.n.m.j2ee.ejbverification.AsynchronousMethodInvocation",
            category = "javaee/ejb",
            enabled = true,
            suppressWarnings = "AsynchronousMethodInvocation")
@Messages({
    "AsynchronousMethodInvocation.display.name=Asynchronous method invocation",
    "AsynchronousMethodInvocation.description=Checks usage of @Asynchronous. Tests whether it's used within supported project and interface type.",
    "AsynchronousMethodInvocation.err.asynchronous.in.ejb31=Asynchronous method invocation is not allowed in project targeting JavaEE 6 Lite profile"
})
public final class AsynchronousMethodInvocation {

    private AsynchronousMethodInvocation() { }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        List<ErrorDescription> problems = new ArrayList<>();
        EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx != null && ctx.getEjb() instanceof Session) {
            J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(ctx.getProject());
            if (capabilities == null
                    || (!capabilities.isEjb31LiteSupported() && !capabilities.isEjb32LiteSupported())) {
                return problems;
            }

            List<ExecutableElement> candidates = new ArrayList<>();
            for (ExecutableElement method : ElementFilter.methodsIn(ctx.getClazz().getEnclosedElements())) {
                if (isAsynchronousAnnotated(method)) {
                    candidates.add(method);
                }
            }
            if (candidates.isEmpty()) {
                return problems;
            }

            // EJB 3.1, 3.2 Full Profiles
            if (!capabilities.isEjb31Supported()
                    // EJB 3.2 Lite Profile
                    && !capabilities.isEjb32LiteSupported()) {
                for (ExecutableElement method : candidates) {
                    problems.add(HintsUtils.createProblem(
                            method,
                            hintContext.getInfo(),
                            Bundle.AsynchronousMethodInvocation_err_asynchronous_in_ejb31()));
                }
            }
        }
        return problems;
    }

    private static boolean isAsynchronousAnnotated(ExecutableElement method) {
        boolean knownClasses = HintsUtils.isContainingKnownClasses(method);
        for (AnnotationMirror am : method.getAnnotationMirrors()) {
            if ((EJBAPIAnnotations.ASYNCHRONOUS.equals(am.getAnnotationType().asElement().toString())
                    || EJBAPIAnnotations.ASYNCHRONOUS_JAKARTA.equals(am.getAnnotationType().asElement().toString()))
                    && knownClasses) {
                return true;
            }
        }
        return false;
    }

}
