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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.fixes.MakeClassPublic;
import org.netbeans.modules.j2ee.ejbverification.fixes.RemoveModifier;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * Checks legal modifiers of the EJB bean.
 * @author Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#LegalModifiers.display.name",
        description = "#LegalModifiers.desc",
        id = "o.n.m.j2ee.ejbverification.LegalModifiers",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "LegalModifiers")
@NbBundle.Messages({
    "LegalModifiers.display.name=Modifiers of the EJB bean",
    "LegalModifiers.desc=Checks whether the defined EJB beans have correct modifiers - are public, not final and not abstract.",
    "LegalModifiers.BeanClassMustBePublic=EJB class must be public",
    "LegalModifiers.BeanClassNotBeFinal=EJB class must not be final",
    "LegalModifiers.BeanClassNotBeAbstract=EJB class must not be abstract"
})
public final class LegalModifiers {

    private LegalModifiers() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx != null && ctx.getEjb() != null) {
            Collection<ErrorDescription> problemsFounds = new LinkedList<>();
            Set<Modifier> modifiers = ctx.getClazz().getModifiers();
            if (!modifiers.contains(Modifier.PUBLIC)) {
                Fix fix = new MakeClassPublic(ctx.getFileObject(), ElementHandle.create(ctx.getClazz()));
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), hintContext.getInfo(),
                        Bundle.LegalModifiers_BeanClassMustBePublic(), fix);
                problemsFounds.add(err);
            }

            if (modifiers.contains(Modifier.FINAL)) {
                Fix fix = new RemoveModifier(ctx.getFileObject(),
                        ElementHandle.create(ctx.getClazz()),
                        Modifier.FINAL);
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), hintContext.getInfo(),
                        Bundle.LegalModifiers_BeanClassNotBeFinal(), fix);
                problemsFounds.add(err);
            }

            if (modifiers.contains(Modifier.ABSTRACT)) {
                if (isInterface(hintContext.getInfo(), ctx.getClazz())) {
                    // no fix for interfaces, just a warning
                    ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), hintContext.getInfo(),
                            Bundle.LegalModifiers_BeanClassNotBeAbstract());
                    problemsFounds.add(err);
                } else {
                    Fix fix = new RemoveModifier(ctx.getFileObject(), ElementHandle.create(ctx.getClazz()), Modifier.ABSTRACT);
                    ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), hintContext.getInfo(),
                            Bundle.LegalModifiers_BeanClassNotBeAbstract(), fix);
                    problemsFounds.add(err);
                }
            }
            return problemsFounds;
        }
        return Collections.emptyList();
    }

    private static boolean isInterface(CompilationInfo info, TypeElement clazz) {
        ClassTree classTree = info.getTrees().getTree(clazz);
        return classTree.getKind() == Tree.Kind.INTERFACE;
    }
}
