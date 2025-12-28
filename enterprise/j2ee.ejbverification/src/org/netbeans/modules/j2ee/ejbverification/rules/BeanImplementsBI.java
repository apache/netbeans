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
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
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
 * In EJB 3.0, it is recommended that a bean class implements its business interface.
 * That way, user does not have to worry about method matching, compiler will take care of that.
 *
 * @author Tomasz.Slota@Sun.COM, Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#BeanImplementsBI.display.name",
        description = "#BeanImplementsBI.err",
        id = "o.n.m.j2ee.ejbverification.BeanImplementsBI",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "BeanImplementsBI")
@NbBundle.Messages({
    "BeanImplementsBI.display.name=Bean doesn't implement business interface",
    "BeanImplementsBI.err=It is recommended that a bean class implement its business interface."
})
public final class BeanImplementsBI {

    private BeanImplementsBI() {
    }

    @TriggerTreeKind(Tree.Kind.CLASS)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        final List<ErrorDescription> problems = new ArrayList<>();
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx != null && ctx.getEjb() instanceof Session) {
            Collection<String> businessInterFaces = new ArrayList<>();

            processAnnotation(businessInterFaces, ctx.getClazz(), EJBAPIAnnotations.LOCAL);
            processAnnotation(businessInterFaces, ctx.getClazz(), EJBAPIAnnotations.LOCAL_JAKARTA);
            processAnnotation(businessInterFaces, ctx.getClazz(), EJBAPIAnnotations.REMOTE);
            processAnnotation(businessInterFaces, ctx.getClazz(), EJBAPIAnnotations.REMOTE_JAKARTA);

            if (businessInterFaces.size() > 0) {
                Collection<String> implementedInterfaces = new TreeSet<>();

                for (TypeMirror interfaceType : ctx.getClazz().getInterfaces()) {
                    String iface = JavaUtils.extractClassNameFromType(interfaceType);

                    if (iface != null) {
                        implementedInterfaces.add(iface);
                    }
                }

                for (String businessInterface : businessInterFaces) {
                    if (!implementedInterfaces.contains(businessInterface)) {
                        ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), hintContext.getInfo(),
                                Bundle.BeanImplementsBI_err(), Severity.WARNING);
                        return Collections.singletonList(err);
                    }
                }
            }
        }
        return problems;
    }

    private static void processAnnotation(Collection<String> businessInterFaces, TypeElement clazz, String annotClass) {
        AnnotationMirror annLocal = JavaUtils.findAnnotation(clazz, annotClass);
        AnnotationValue value = JavaUtils.getAnnotationAttrValue(annLocal, EJBAPIAnnotations.VALUE);
        if (value != null) {
            for (AnnotationValue ifaceAttr : (List<? extends AnnotationValue>) value.getValue()) {
                if (ifaceAttr.getValue() instanceof TypeMirror) {
                    String className = JavaUtils.extractClassNameFromType((TypeMirror) ifaceAttr.getValue());
                    businessInterFaces.add(className);
                }
            }
        }
    }
}
