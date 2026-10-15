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
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
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
 * If a session bean is annotated as @Local, then it must have a local business interface.
 *
 * @author Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#LocalAnnotatedBeanHasLBI.display.name",
        description = "#LocalAnnotatedBeanHasLBI.err",
        id = "o.n.m.j2ee.ejbverification.LocalAnnotatedBeanHasLBI",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "LocalAnnotatedBeanHasLBI")
@NbBundle.Messages({
    "LocalAnnotatedBeanHasLBI.display.name=Uncomplete LBI Session bean",
    "LocalAnnotatedBeanHasLBI.err=If a session bean is annotated as @Local, then it must have a local business interface."
})
public final class LocalAnnotatedBeanHasLBI {

    private static final Logger LOG = Logger.getLogger(LocalAnnotatedBeanHasLBI.class.getName());

    private LocalAnnotatedBeanHasLBI() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        final List<ErrorDescription> problems = new ArrayList<>();
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx != null && ctx.getEjb() instanceof Session) {
            if (JavaUtils.hasAnnotation(ctx.getClazz(), EJBAPIAnnotations.LOCAL)
                    || JavaUtils.hasAnnotation(ctx.getClazz(), EJBAPIAnnotations.LOCAL_JAKARTA)) {
                if (ctx.getEjbData().getBusinessLocal().length == 0) {
                    ErrorDescription err = HintsUtils.createProblem(
                            ctx.getClazz(),
                            hintContext.getInfo(),
                            Bundle.LocalAnnotatedBeanHasLBI_err());
                    problems.add(err);
                }
            }
        }
        return problems;
    }
}
