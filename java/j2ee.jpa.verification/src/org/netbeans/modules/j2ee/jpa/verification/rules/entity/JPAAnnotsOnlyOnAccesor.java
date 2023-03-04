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
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * Only accesor methods can have JPA Annotations
 *
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.JPAAnnotsOnlyOnAccesor",
        displayName = "#JPAAnnotsOnlyOnAccesor.display.name",
        description = "#JPAAnnotsOnlyOnAccesor.desc",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.ERROR,
        suppressWarnings = "JPAAnnotsOnlyOnAccesor")
@NbBundle.Messages({
    "JPAAnnotsOnlyOnAccesor.display.name=Verify jpa annotations on accessors",
    "JPAAnnotsOnlyOnAccesor.desc=JPA annotations should be applied to getter methods only"})
public class JPAAnnotsOnlyOnAccesor {

    @TriggerPatterns(value = {
        @TriggerPattern(value = JPAAnnotations.ENTITY),
        @TriggerPattern(value = JPAAnnotations.EMBEDDABLE),
        @TriggerPattern(value = JPAAnnotations.MAPPED_SUPERCLASS)})
    public static Collection<ErrorDescription> apply(HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        final JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }

        TypeElement subject = ctx.getJavaClass();

        if (((JPAProblemContext) ctx).getAccessType() != AccessType.PROPERTY) {
            return null;
        }

        List<ErrorDescription> problemsFound = new ArrayList<>();

        for (ExecutableElement method : ElementFilter.methodsIn(subject.getEnclosedElements())) {
            if (!isAccessor(method)) {
                for (String annotName : ModelUtils.extractAnnotationNames(method)) {
                    if (JPAAnnotations.MEMBER_LEVEL.contains(annotName)) {
                        Tree elementTree = ctx.getCompilationInfo().getTrees().getTree(method);

                        Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                                ctx.getCompilationInfo(), elementTree);

                        ErrorDescription error = ErrorDescriptionFactory.forSpan(
                                hc,
                                underlineSpan.getStartOffset(),
                                underlineSpan.getEndOffset(),
                                NbBundle.getMessage(LegalCombinationOfAnnotations.class, "MSG_JPAAnnotsOnlyOnAccesor", ModelUtils.shortAnnotationName(annotName)));


                        problemsFound.add(error);
                        break;
                    }
                }
            }
        }

        return problemsFound;
    }

    private static boolean isAccessor(ExecutableElement method) {

        if (!method.getParameters().isEmpty()) {
            return false;
        }

        String methodName = method.getSimpleName().toString();
        if (methodName.startsWith("get")) { //NO18N
            return true;
        }
        if (isBoolean(method.getReturnType().toString()) && methodName.startsWith("is")) { //NO18N
            return true;
        }
        return false;
    }

    private static boolean isBoolean(String type) {
        return "boolean".equals(type) || "java.lang.Boolean".equals(type); //NO18N
    }
}
