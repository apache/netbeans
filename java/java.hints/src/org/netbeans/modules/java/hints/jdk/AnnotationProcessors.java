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
