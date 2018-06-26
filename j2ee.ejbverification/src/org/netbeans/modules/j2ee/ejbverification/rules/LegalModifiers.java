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
