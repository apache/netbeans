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
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.JPAHelper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateId;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

/**
 * An entity hierarchy must have an Id. It could be a simple Id, cmposite Id or
 * embedded Id. In case of simple and composite id, it must have atleast one
 * field or property annotated as Id, where as in case of emdebedded id, it must
 * have a field or property annotated as Embedded.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.IdDefinedInHierarchy",
        displayName = "#IdDefinedInHierarchy.display.name",
        description = "#IdDefinedInHierarchy.desc",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.ERROR,
        suppressWarnings = "IdDefinedInHierarchy")
@NbBundle.Messages({
    "IdDefinedInHierarchy.display.name=Verify entity have defined promary key",
    "IdDefinedInHierarchy.desc=Id is required for entities"})
public class IdDefinedInHierarchy {

    @TriggerPattern(value = JPAAnnotations.ENTITY)//NOI18N
    public static ErrorDescription apply(HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        final JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }
        TypeElement javaClass = ctx.getJavaClass();

        if (ctx.getModelElement() == null) {
            return null;
        }

        final boolean[] haveId = {false};
        try {
            MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
            model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                @Override
                public Void run(EntityMappingsMetadata metadata) {
                    TypeElement javaClass = ctx.getJavaClass();
                    Object modelElement = ctx.getModelElement();
                    do {
                        if (JPAHelper.isAnyMemberAnnotatedAsIdOrEmbeddedId(modelElement)) {
                            haveId[0] = true;
                            return null; // OK
                        }

                        TypeMirror parentType = javaClass.getSuperclass();
                        javaClass = null;

                        if (!"java.lang.Object".equals(parentType.toString())) { //NOI18N
                            if (parentType.getKind() == TypeKind.DECLARED) {
                                Element parent = ((DeclaredType) parentType).asElement();

                                if (parent.getKind() == ElementKind.CLASS) {
                                    javaClass = (TypeElement) parent;
                                    modelElement = ModelUtils.getEntity(metadata, javaClass);

                                    if (modelElement == null) {
                                        modelElement = ModelUtils.getMappedSuperclass(metadata, javaClass);
                                    }
                                }
                            }
                        }

                    } while (javaClass != null && modelElement != null);

                    return null;
                }
            });
        } catch (IOException ex) {
        }

        if (haveId[0]) {
            return null;
        }

        Fix fix = new CreateId(ctx.getFileObject(), ElementHandle.create(javaClass),
                ((JPAProblemContext) ctx).getAccessType());

        TreePath par = hc.getPath();
        while (par != null && par.getParentPath() != null && par.getLeaf().getKind() != Tree.Kind.CLASS) {
            par = par.getParentPath();
        }

        Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                ctx.getCompilationInfo(), par.getLeaf());
        return ErrorDescriptionFactory.forSpan(
                hc,
                underlineSpan.getStartOffset(),
                underlineSpan.getEndOffset(),
                NbBundle.getMessage(ConsistentAccessType.class, "MSG_NoIdDefinedInHierarchy"),
                fix);

    }
}
