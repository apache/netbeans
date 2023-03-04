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
