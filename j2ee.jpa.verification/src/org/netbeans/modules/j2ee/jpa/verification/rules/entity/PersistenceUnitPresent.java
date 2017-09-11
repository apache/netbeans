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
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.jpa.verification.api.JPAVerificationWarningIds;
import org.netbeans.modules.j2ee.jpa.verification.api.VerificationWarningOverrider;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreatePersistenceUnit;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.PersistenceUnitPresent",
        displayName = "#PersistenceUnitPresent.display.name",
        description = "#PersistenceUnitPresent.desc",
        category = "javaee/jpa",
        enabled = true,
        suppressWarnings = "PersistenceUnitPresent")
@NbBundle.Messages({
    "PersistenceUnitPresent.display.name=Presence of persistence.xml",
    "PersistenceUnitPresent.desc=Check if project with jpa usage contains persistence.xml with persistence unit"
})
public class PersistenceUnitPresent{

    @TriggerPattern(value = JPAAnnotations.ENTITY)
    public static ErrorDescription apply(HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if entity is an annotation
        }
        
        Project project = FileOwnerQuery.getOwner(hc.getInfo().getFileObject());

        if (project == null) {
            // Can't perform this check for a file that does not belong to a project
            return null;
        }

        PersistenceScope[] scopes = PersistenceUtils.getPersistenceScopes(project, hc.getInfo().getFileObject());

        for (PersistenceScope scope : scopes) {
            if (scope.getClassPath().contains(hc.getInfo().getFileObject())) {

                try {
                    FileObject persistenceXML = scope.getPersistenceXml();

                    if (persistenceXML != null) {
                        PersistenceUnit pus[] = PersistenceMetadata.getDefault().getRoot(persistenceXML).getPersistenceUnit();

                        if (pus != null && pus.length > 0) {
                            // persistence unit found, no warning
                            return null;
                        }
                    }
                } catch (        IOException | RuntimeException e) {
                    JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }


        }

        // See if any module has turned off this particular warning, such as, the Hibernate Support module
        for (VerificationWarningOverrider wo : project.getLookup().lookupAll(VerificationWarningOverrider.class)) {
            if (wo.suppressWarning(JPAVerificationWarningIds.NO_PERSISTENCE_UNIT_WARNING)) {
                return null;
            }
        }

        return ErrorDescriptionFactory.forTree(
                    hc,
                    hc.getPath().getParentPath(),
                    NbBundle.getMessage(PersistenceUnitPresent.class, "MSG_MissingPersistenceUnitHint"),
                    project.getLookup().lookup(PersistenceLocationProvider.class) == null
                            ? null : new CreatePersistenceUnit(project));
    }
}
