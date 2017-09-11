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
