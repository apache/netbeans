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
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
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
 * If an interface is annotated with @Remote, then value attribute must not be specified. In other words, if value
 * is specified for @Remote, then it must be annotating a class (not an interface).
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 * @author Martin Fousek <marfous@netbeans.org>
 */
@Hint(displayName = "#ValueNotSpecifiedForRemoteAnnotationInterface.display.name",
        description = "#ValueNotSpecifiedForRemoteAnnotationInterface.err",
        id = "o.n.m.j2ee.ejbverification.ValueNotSpecifiedForRemoteAnnotationInterface",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "ValueNotSpecifiedForRemoteAnnotationInterface")
@NbBundle.Messages({
    "ValueNotSpecifiedForRemoteAnnotationInterface.display.name=@Remote uses value in business interface",
    "ValueNotSpecifiedForRemoteAnnotationInterface.err=If an interface is annotated with @Remote, "
    + "then value attribute must not be specified. In other words, if value is specified for @Remote, "
    + "then it must be annotating a class (not an interface)."
})
public final class ValueNotSpecifiedForRemoteAnnotationInterface {

    private ValueNotSpecifiedForRemoteAnnotationInterface() {
    }

    @TriggerTreeKind(Tree.Kind.INTERFACE)
    public static Collection<ErrorDescription> run(HintContext hintContext) {
        final EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintContext);
        if (ctx == null || ctx.getClazz().getKind() != ElementKind.INTERFACE) {
            return Collections.emptyList();
        }

        AnnotationMirror annRemote = JavaUtils.findAnnotation(ctx.getClazz(), EJBAPIAnnotations.REMOTE_JAKARTA);
        if(annRemote == null) {
            annRemote = JavaUtils.findAnnotation(ctx.getClazz(), EJBAPIAnnotations.REMOTE);
        }
        if (annRemote != null && JavaUtils.getAnnotationAttrValue(annRemote, EJBAPIAnnotations.VALUE) != null) {
            ErrorDescription err = HintsUtils.createProblem(
                    ctx.getClazz(),
                    hintContext.getInfo(),
                    Bundle.ValueNotSpecifiedForRemoteAnnotationInterface_err());

            return Collections.singletonList(err);
        }
        return Collections.emptyList();
    }
}
