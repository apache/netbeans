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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * The invocation semantics of a remote business method is very different from that of a local business method. For this
 * reason, when a session bean has remote as well as local business method, there should not be any method common to
 * both the interfaces.
 *
 * Example below is an incorrect use case:
 * Remote public interface I1 { void foo();},
 * Local public interface I2 { void foo();},
 * Stateless public class Foo implements I1, I2 { ... }
 *
 * @author Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#BMnotPartOfRBIandLBI.display.name",
        description = "#BMnotPartOfRBIandLBI.err",
        id = "o.n.m.j2ee.ejbverification.BMnotPartOfRBIandLBI",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "BMnotPartOfRBIandLBI")
@NbBundle.Messages({
    "BMnotPartOfRBIandLBI.display.name=Method definition in local and remote interface",
    "BMnotPartOfRBIandLBI.err=When a session bean has remote and local business interfaces, there should not be method common to both of them."
})
public final class BMnotPartOfRBIandLBI {

    private static final Logger LOG = Logger.getLogger(BMnotPartOfRBIandLBI.class.getName());

    private BMnotPartOfRBIandLBI() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        List<ErrorDescription> problems = new ArrayList<>();
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx != null && ctx.getEjb() instanceof Session) {
            final Collection<ExecutableElement> localMethods = new ArrayList<>();
            final Map<String, ExecutableElement> remoteMethods = new HashMap<>();

            // local methods
            localMethods.addAll(getMethodsFromClasses(hintContext.getInfo(), ctx.getEjbData().getBusinessLocal()));
            // remote methods
            for (ExecutableElement method : getMethodsFromClasses(hintContext.getInfo(), ctx.getEjbData().getBusinessRemote())) {
                remoteMethods.put(method.getSimpleName().toString(), method);
            }

            for (ExecutableElement localMethod : localMethods) {
                ExecutableElement sameNameRemoteMethod = remoteMethods.get(
                        localMethod.getSimpleName().toString());

                if (sameNameRemoteMethod != null) {
                    if (JavaUtils.isMethodSignatureSame(hintContext.getInfo(),
                            localMethod, sameNameRemoteMethod)) {
                        ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), hintContext.getInfo(),
                                Bundle.BMnotPartOfRBIandLBI_err(), Severity.WARNING);

                        return Collections.singletonList(err);
                    }
                }
            }
        }
        return problems;
    }

    private static Collection<ExecutableElement> getMethodsFromClasses(CompilationInfo cinfo, String[] classNames) {
        Collection<ExecutableElement> methods = new LinkedList<>();
        if (classNames != null) {
            for (String className : classNames) {
                TypeElement clazz = cinfo.getElements().getTypeElement(className);
                if (clazz != null) {
                    methods.addAll(ElementFilter.methodsIn(clazz.getEnclosedElements()));
                }
            }
        }
        return methods;
    }
}
