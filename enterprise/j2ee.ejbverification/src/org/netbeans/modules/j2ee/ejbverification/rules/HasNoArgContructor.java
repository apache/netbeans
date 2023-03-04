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
package org.netbeans.modules.j2ee.ejbverification.rules;

import com.sun.source.tree.Tree;
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.fixes.CreateDefaultConstructor;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#HasNoArgContructor.display.name",
        description = "#HasNoArgContructor.err",
        id = "o.n.m.j2ee.ejbverification.HasNoArgContructor",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "HasNoArgContructor")
@NbBundle.Messages({
    "HasNoArgContructor.display.name=No-arg contructor in the EJB bean",
    "HasNoArgContructor.err=EJB class must have a public or protected no-arg constructor."
})
public final class HasNoArgContructor {

    private HasNoArgContructor() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx == null || ctx.getEjb() == null) {
            return Collections.emptyList();
        }

        boolean hasDefaultContructor = true;
        for (ExecutableElement constr : ElementFilter.constructorsIn(ctx.getClazz().getEnclosedElements())) {
            hasDefaultContructor = false;
            if (constr.getParameters().isEmpty()
                    && (constr.getModifiers().contains(Modifier.PUBLIC)
                    || constr.getModifiers().contains(Modifier.PROTECTED))) {
                return Collections.emptyList(); // found appropriate constructor
            }
        }

        if (hasDefaultContructor) {
            return Collections.emptyList(); // OK
        }

        Fix fix = new CreateDefaultConstructor(ctx.getFileObject(), ElementHandle.create(ctx.getClazz()));
        ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), hintContext.getInfo(),
                Bundle.HasNoArgContructor_err(), fix);

        return Collections.singletonList(err);
    }
}
