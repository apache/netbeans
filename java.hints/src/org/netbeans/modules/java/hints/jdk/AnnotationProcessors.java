/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
public class AnnotationProcessors {

    @Hint(displayName="#DN_AnnotationProcessors.overridingGetSupportedAnnotations",
          description="#DESC_AnnotationProcessors.overridingGetSupportedAnnotations",
          category="rules15")
    @Messages({/*"DN_AnnotationProcessors.overridingGetSupportedAnnotations=AbstractProcessor.getSupportedAnnotations() is overridden",
               "DESC_AnnotationProcessors.overridingGetSupportedAnnotations=Overriding Processor.getSupportedAnnotations() may lead to " +
                                                                           "unnecessary classloading during development, and may prevent important optimalizations. " +
                                                                           "consider using @javax.annotation.processing.SupportedAnnotationTypes",*/
               "ERR_AnnotationProcessors.overridingGetSupportedAnnotations=AbstractProcessor.getSupportedAnnotationTypes() overridden, may cause performance problems during development"})
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription oGSA(HintContext ctx) {
        Element clazz = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (clazz == null || !clazz.getKind().isClass()) return null;

        TypeElement ap = ctx.getInfo().getElements().getTypeElement("javax.annotation.processing.AbstractProcessor");

        if (ap == null) return null;

        Types types = ctx.getInfo().getTypes();

        if (!types.isSubtype(types.erasure(clazz.asType()), types.erasure(ap.asType()))) return null;

        for (ExecutableElement ee : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            if (ee.getSimpleName().contentEquals("getSupportedAnnotationTypes") && ee.getParameters().isEmpty()) {
                Tree t = ctx.getInfo().getTrees().getTree(ee);

                if (t != null) {
                    return ErrorDescriptionFactory.forName(ctx, t, Bundle.ERR_AnnotationProcessors_overridingGetSupportedAnnotations());
                }
            }
        }

        return null;
    }
}
