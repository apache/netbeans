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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.GeneratedValueIsId;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.MVRelationshipForEntityTypeAttrDefined;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.RelationshipForEntityTypeAttrDefined;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.TemporalFieldsAnnotated;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.ValidBasicType;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.ValidColumnName;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.ValidModifiers;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.ValidVersionType;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * Perform checks on the attributes
 *
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.ValidAttributes",
        displayName = "#ValidAttributes.display.name",
        description = "#ValidAttributes.desc",
        category = "javaee/jpa",
        enabled = true,
        suppressWarnings = "ValidAttributes")
@NbBundle.Messages({
    "ValidAttributes.display.name=Verify attributes of jpa classes",
    "ValidAttributes.desc=This validation cover nimber of issues like valid column name, valid attribute modifiers, valid basic type, relationships etc"})
public class ValidAttributes {//TODO: split at least problems with default error level with problems with warning level

    private static JPAEntityAttributeCheck[] attribChecks = new JPAEntityAttributeCheck[]{
        new ValidColumnName(),
        new ValidModifiers(),
        new ValidBasicType(),
        new RelationshipForEntityTypeAttrDefined(),
        new MVRelationshipForEntityTypeAttrDefined(),
        new ValidVersionType(),
        new GeneratedValueIsId(),
        new TemporalFieldsAnnotated()
    };

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

        List<ErrorDescription> problemsFound = new ArrayList<>();
        final List<AttributeWrapper> attrs = new ArrayList<>();

        try {
            MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
            model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                @Override
                public Void run(EntityMappingsMetadata metadata) {
//
                    Basic basicFields[] = null;
                    Id idFields[] = null;
                    Version versionFields[] = null;
                    if (ctx.getModelElement() instanceof Entity) {
                        Entity entity = (Entity) ctx.getModelElement();
                        basicFields = entity.getAttributes().getBasic();

                        idFields = entity.getAttributes().getId();
                        versionFields = entity.getAttributes().getVersion();
                    } else if (ctx.getModelElement() instanceof Embeddable) {
                        Embeddable embeddable = (Embeddable) ctx.getModelElement();
                        basicFields = embeddable.getAttributes().getBasic();

                    } else if (ctx.getModelElement() instanceof MappedSuperclass) {
                        MappedSuperclass mappedSuperclass = (MappedSuperclass) ctx.getModelElement();
                        basicFields = mappedSuperclass.getAttributes().getBasic();

                        idFields = mappedSuperclass.getAttributes().getId();
                        versionFields = mappedSuperclass.getAttributes().getVersion();
                    }

                    if (basicFields != null) {
                        for (Basic basic : basicFields) {
                            attrs.add(new AttributeWrapper(basic));
                        }
                    }

                    if (idFields != null) {
                        for (Id id : idFields) {
                            attrs.add(new AttributeWrapper(id));
                        }
                    }

                    if (versionFields != null) {
                        for (Version version : versionFields) {
                            attrs.add(new AttributeWrapper(version));
                        }
                    }


                    //                  
                    return null;
                }
            });
        } catch (IOException ex) {
        }

        JPAProblemContext jpaCtx = (JPAProblemContext) ctx;

        if (jpaCtx.getAccessType() == AccessType.INCONSISTENT) {
            JPAProblemFinder.LOG.fine("Skipping attribute checks due to inconsistent access type");
        } else {
            for (AttributeWrapper attr : attrs) {
                ModelUtils.resolveJavaElementFromModel(jpaCtx, attr);

                if (attr.getJavaElement() == null) {
                    JPAProblemFinder.LOG.severe("Failed to resolve java model element for JPA merged model element "
                            + subject.getSimpleName() + "." + attr.getName());
                } else {
                    for (JPAEntityAttributeCheck check : attribChecks) {
                        Collection<ErrorDescription> attrProblems = check.check(jpaCtx, hc, attr);

                        if (attrProblems != null) {
                            problemsFound.addAll(attrProblems);
                        }
                    }
                }
            }
        }

        return problemsFound;
    }
}
