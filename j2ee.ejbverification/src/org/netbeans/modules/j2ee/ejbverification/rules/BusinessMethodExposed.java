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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.j2ee.ejbverification.rules;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.modules.j2ee.ejbverification.fixes.ExposeBusinessMethod;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * Offer a hint to expose a method in business interface.
 *
 * @author Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#BusinessMethodExposed.display.name",
        description = "#BusinessMethodExposed.hint",
        id = "o.n.m.j2ee.ejbverification.BusinessMethodExposed",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "BusinessMethodExposed")
@NbBundle.Messages({
    "BusinessMethodExposed.display.name=Method not exposed in business interface.",
    "BusinessMethodExposed.hint=Method is not exposed in any business interface."
})
public class BusinessMethodExposed {

    private static final Logger LOG = Logger.getLogger(BusinessMethodExposed.class.getName());

    private BusinessMethodExposed() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        final List<ErrorDescription> problems = new ArrayList<>();
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx != null && ctx.getEjb() instanceof Session) {
            Collection<TypeElement> localInterfaces = null;
            Collection<TypeElement> remoteInterfaces = null;

            EjbJar ejbModule = ctx.getEjbModule();
            Profile profile = ejbModule.getJ2eeProfile();
            if (profile != null && profile.isAtLeast(Profile.JAVA_EE_6_WEB)) {
                int intfCount = ctx.getEjbData().getBusinessLocal().length + ctx.getEjbData().getBusinessRemote().length;
                localInterfaces = new ArrayList(resolveClasses(hintContext.getInfo(), ctx.getEjbData().getBusinessLocal()));
                remoteInterfaces = new ArrayList(resolveClasses(hintContext.getInfo(), ctx.getEjbData().getBusinessRemote()));
                if (intfCount == 0 || JavaUtils.hasAnnotation(ctx.getClazz(), EJBAPIAnnotations.LOCAL_BEAN)) {
                    return null;
                }
            }
            // if an EJB is annotated with "@javax.jws.WebService"
            // then no business interface is needed, see issue #147512
            if (JavaUtils.hasAnnotation(ctx.getClazz(), EJBAPIAnnotations.WEB_SERVICE)) {
                return null;
            }

            if (localInterfaces == null) {
                localInterfaces = new ArrayList(resolveClasses(hintContext.getInfo(), ctx.getEjbData().getBusinessLocal()));
            }
            if (remoteInterfaces == null) {
                remoteInterfaces = new ArrayList(resolveClasses(hintContext.getInfo(), ctx.getEjbData().getBusinessRemote()));
            }

            Collection<ExecutableElement> definedMethods = new ArrayList<>();
            for (TypeElement iface : localInterfaces) {
                definedMethods.addAll(ElementFilter.methodsIn(iface.getEnclosedElements()));
            }
            for (TypeElement iface : remoteInterfaces) {
                definedMethods.addAll(ElementFilter.methodsIn(iface.getEnclosedElements()));
            }

            Map<String, ArrayList<ExecutableElement>> definedMethodsByName = new HashMap<>();

            for (ExecutableElement method : definedMethods) {
                String hashName = method.getSimpleName().toString();
                if (!definedMethodsByName.containsKey(hashName)) {
                    definedMethodsByName.put(hashName, new ArrayList<ExecutableElement>(1));
                }
                definedMethodsByName.get(hashName).add(method);
            }

            for (ExecutableElement method : ElementFilter.methodsIn(ctx.getClazz().getEnclosedElements())) {
                if (isEligibleMethod(method)) {
                    ArrayList<ExecutableElement> potentialMatches = definedMethodsByName.get(method.getSimpleName().toString());

                    if (potentialMatches != null && !potentialMatches.isEmpty()) {
                        if (isFoundMatchingMethodSignature(hintContext.getInfo(), method, potentialMatches)) {
                            continue;
                        }
                    }

                    ArrayList<Fix> fixes = new ArrayList<>();
                    for (TypeElement iface : localInterfaces) {
                        Fix fix = new ExposeBusinessMethod(
                                ctx.getFileObject(),
                                ElementHandle.create(iface),
                                ElementHandle.create(method),
                                true);
                        fixes.add(fix);
                    }

                    for (TypeElement iface : remoteInterfaces) {
                        Fix fix = new ExposeBusinessMethod(
                                ctx.getFileObject(),
                                ElementHandle.create(iface),
                                ElementHandle.create(method),
                                false);
                        fixes.add(fix);
                    }

                    ErrorDescription err = HintsUtils.createProblem(method, hintContext.getInfo(),
                            Bundle.BusinessMethodExposed_hint(), Severity.HINT, fixes);

                    problems.add(err);
                }
            }
            return problems;
        }

        return problems;
    }

    private static boolean isFoundMatchingMethodSignature(CompilationInfo cinfo, ExecutableElement method, ArrayList<ExecutableElement> potentialMatches) {
        for (ExecutableElement potentialMatch : potentialMatches) {
            if (JavaUtils.isMethodSignatureSame(cinfo, method, potentialMatch)) {
                return true;
            }
        }
        return false;
    }

    private static Collection<TypeElement> resolveClasses(CompilationInfo info, String classNames[]) {
        Collection<TypeElement> result = new ArrayList<>();

        if (classNames != null) {
            for (String className : classNames) {
                TypeElement clazz = info.getElements().getTypeElement(className);

                if (clazz != null) {
                    result.add(clazz);
                    addInterfaces(info, result, clazz.getInterfaces());
                }
            }
        }
        return result;
    }

    private static void addInterfaces(CompilationInfo info, Collection<TypeElement> result, List<? extends TypeMirror> interfaces) {
        for (TypeMirror inter : interfaces) {
            TypeElement te = (TypeElement) info.getTypes().asElement(inter);
            result.add(te);
            addInterfaces(info, result, te.getInterfaces());
        }
    }

    private static boolean isEligibleMethod(ExecutableElement method) {
        // if ThrownTypes, Parameters, ReturnType are unknown
        // then don't offer the hint, see issue #195061
        return method.getModifiers().contains(Modifier.PUBLIC)
                && !method.getModifiers().contains(Modifier.STATIC)
                && HintsUtils.isContainingKnownClasses(method);
    }
}
