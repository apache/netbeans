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
import com.sun.source.util.TreePath;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.UniqueEntityName",
        displayName = "#UniqueEntityName.display.name",
        description = "#UniqueEntityName.desc",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.ERROR,
        suppressWarnings = "UniqueEntityName")
@NbBundle.Messages({
    "UniqueEntityName.display.name=Verify entity name is unique",
    "UniqueEntityName.desc=Entity names must not be the same in one persistence unit"})
public class UniqueEntityName {

    @TriggerPattern(value = JPAAnnotations.ENTITY)
    public static ErrorDescription apply(final HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        final JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled() || ctx.getModelElement() == null || !(ctx.getModelElement() instanceof Entity)) {
            return null;
        }


        final ErrorDescription[] ret = {null};

        MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
        try {
            model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                @Override
                public Void run(EntityMappingsMetadata metadata) {
                    String thisEntityName = ((Entity) ctx.getModelElement()).getName();

                    TypeElement subject = ctx.getJavaClass();
                    for (Entity entity : ((JPAProblemContext) ctx).getMetaData().getRoot().getEntity()) {
                        if (entity.getName().contentEquals(thisEntityName)
                                && !subject.getQualifiedName().contentEquals(entity.getClass2())) {

                            TreePath par = hc.getPath();
                            while (par != null && par.getParentPath() != null && par.getLeaf().getKind() != Tree.Kind.CLASS) {
                                par = par.getParentPath();
                            }

                            Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                                    ctx.getCompilationInfo(), par.getLeaf());

                            ret[0] = ErrorDescriptionFactory.forSpan(
                                    hc,
                                    underlineSpan.getStartOffset(),
                                    underlineSpan.getEndOffset(),
                                    NbBundle.getMessage(UniqueEntityName.class,
                                    "MSG_NonUniqueEntityName", entity.getClass2()));
                            break;

                        }
                    }
                    return null;
                }
            });
        } catch (IOException ex) {
            
        }

        return ret[0];
    }
}
