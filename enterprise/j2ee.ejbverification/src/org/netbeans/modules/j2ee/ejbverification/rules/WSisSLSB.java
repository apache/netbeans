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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.AnnotationMirror;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * If a class is part of ejb-jar and is annotated as @WebService, then it must be designated as a stateless
 * session bean. A stateful session bean can not be annotated as WebService.
 *
 * @author Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#WSisSLSB.display.name",
        description = "#WSisSLSB.err",
        id = "o.n.m.j2ee.ejbverification.WSisSLSB",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "WSisSLSB")
@NbBundle.Messages({
    "WSisSLSB.display.name=WebService must be designated as session bean",
    "WSisSLSB.err=If a class is part of ejb-jar and is annotated as @WebService, then it must be designated as a stateless or as a singleton session bean."
})
public final class WSisSLSB {

    private WSisSLSB() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx == null) {
            return Collections.emptyList();
        }

        boolean isEJB = false;
        J2eeModuleProvider provider = ctx.getProject().getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            J2eeModule module = provider.getJ2eeModule();
            isEJB = module != null && J2eeModule.Type.EJB.equals(module.getType());
        }
        //disable this rule for non ejb project
        if (!isEJB) {
            return null;
        }
        AnnotationMirror annWebService = JavaUtils.findAnnotation(ctx.getClazz(),
                EJBAPIAnnotations.WEB_SERVICE_JAKARTA);
        if(annWebService == null) {
            annWebService = JavaUtils.findAnnotation(ctx.getClazz(), EJBAPIAnnotations.WEB_SERVICE);
        }

        if (annWebService != null) {
            ClassTree classTree = hintContext.getInfo().getTrees().getTree(ctx.getClazz());
            if (classTree.getKind() == Tree.Kind.INTERFACE) {
                return null; // ok, interfaces can have @WebService without ejb annotations
            }
            if (ctx.getEjb() instanceof Session) {
                if (Session.SESSION_TYPE_STATELESS.equals(ctx.getEjbData().getSessionType())
                        || Session.SESSION_TYPE_SINGLETON.equals(ctx.getEjbData().getSessionType())) {
                    return Collections.emptyList(); //OK
                }
            }
            ErrorDescription err = HintsUtils.createProblem(
                    ctx.getClazz(),
                    hintContext.getInfo(),
                    Bundle.WSisSLSB_err());
            return Collections.singletonList(err);
        }
        return Collections.emptyList();
    }
}
