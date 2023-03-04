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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;
import static org.netbeans.modules.j2ee.jpa.model.JPAAnnotations.*;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.LegalCombinationOfAnnotations",
        displayName = "#LegalCombinationOfAnnotations.display.name",
        description = "#LegalCombinationOfAnnotations.desc",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.ERROR,
        suppressWarnings = "LegalCombinationOfAnnotations")
@NbBundle.Messages({
    "LegalCombinationOfAnnotations.display.name=Verify combinations of jpa annitations",
    "LegalCombinationOfAnnotations.desc=Some JPA annotations may not be applied to the same element at the same time"})
public class LegalCombinationOfAnnotations {
    //TODO: Add more rules

    private static Collection<IllegalCombination> illegalClassAnnotationCombinations = Arrays.asList(
            new IllegalCombination(Collections.singleton(ENTITY), Arrays.asList(EMBEDDABLE, MAPPED_SUPERCLASS)),
            new IllegalCombination(Collections.singleton(TABLE), Collections.singleton(MAPPED_SUPERCLASS)));
    private static Collection<IllegalCombination> illegalAttrAnnotationCombinations = Arrays.asList();

    @TriggerPatterns(value = {
        @TriggerPattern(value = JPAAnnotations.ENTITY), //@TriggerPattern(value = JPAAnnotations.EMBEDDABLE),
    //@TriggerPattern(value = JPAAnnotations.MAPPED_SUPERCLASS)
    })
    public static Collection<ErrorDescription> apply(HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        final JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }

        TypeElement subject = ctx.getJavaClass();

        List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();

        Collection<String> annotationsOnClass = ModelUtils.extractAnnotationNames(subject);

        for (IllegalCombination ic : illegalClassAnnotationCombinations) {
            ic.check(ctx, hc, subject, problemsFound, annotationsOnClass);
        }

        for (Element elem : subject.getEnclosedElements()) {
            Collection<String> annotationsOnElement = ModelUtils.extractAnnotationNames(elem);

            for (IllegalCombination ic : illegalAttrAnnotationCombinations) {
                ic.check(ctx, hc, elem, problemsFound, annotationsOnElement);
            }
        }

        return problemsFound;
    }

    private static class IllegalCombination {

        private Collection<String> set1;
        private Collection<String> set2;

        IllegalCombination(Collection<String> set1, Collection<String> set2) {
            this.set1 = set1;
            this.set2 = set2;
        }

        void check(ProblemContext ctx,
                HintContext hc,
                Element elem,
                Collection<ErrorDescription> errorList,
                Collection<String> annotations) {

            for (String ann : annotations) {
                if (set1.contains(ann)) {
                    for (String forbiddenAnn : set2) {
                        if (annotations.contains(forbiddenAnn)) {
                            Tree elementTree = ctx.getCompilationInfo().getTrees().getTree(elem);

                            Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                                    ctx.getCompilationInfo(), elementTree);

                            ErrorDescription error = ErrorDescriptionFactory.forSpan(
                                    hc,
                                    underlineSpan.getStartOffset(),
                                    underlineSpan.getEndOffset(),
                                    NbBundle.getMessage(LegalCombinationOfAnnotations.class, "MSG_IllegalAnnotationCombination", ModelUtils.shortAnnotationName(ann), ModelUtils.shortAnnotationName(forbiddenAnn)));

                            errorList.add(error);
                        }
                    }
                }
            }
        }
    }
}
