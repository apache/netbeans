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
package org.netbeans.modules.j2ee.jpa.verification.rules.attribute;

import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateManyToManyRelationshipHint;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateOneToManyRelationshipHint;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateUnidirOneToManyRelationshipHint;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.util.NbBundle;

/**
 * if there is attr of type Collection<EntityType>
 * a multi-valued relationship should be defined for it
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class MVRelationshipForEntityTypeAttrDefined extends JPAEntityAttributeCheck {

    @Override
    public Collection<ErrorDescription> check(final JPAProblemContext ctx, final HintContext hc, final AttributeWrapper attrib) {

        // Not applicable for embeddable classes, which do not have relationships.
        if (ctx.isEmbeddable()) {
            return null;
        }

        final TypeMirror type = attrib.getType();
        final ArrayList<ErrorDescription> ret = new ArrayList<>();

        if (type.getKind() == TypeKind.DECLARED) {
            List<? extends TypeMirror> typeArgs = ((DeclaredType) type).getTypeArguments();

            if (typeArgs.size() == 1) {
                final Element typeElement = ctx.getCompilationInfo().getTypes().asElement(typeArgs.get(0));

                if (typeElement != null && typeElement.getKind() == ElementKind.CLASS) {

                    try {
                        MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
                        model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                            @Override
                            public Void run(EntityMappingsMetadata metadata) {


                                Entity entity = ModelUtils.getEntity(ctx.getMetaData(), ((TypeElement) typeElement));
                                String remoteClassName = ((TypeElement) typeElement).getQualifiedName().toString();

                                if (entity != null) {
                                    ElementHandle<TypeElement> classHandle = ElementHandle.create(ctx.getJavaClass());
                                    ElementHandle<Element> elemHandle = ElementHandle.create(attrib.getJavaElement());

                                    Fix fix1 = new CreateUnidirOneToManyRelationshipHint(ctx.getFileObject(),
                                            classHandle, elemHandle);

                                    Fix fix2 = new CreateOneToManyRelationshipHint(ctx.getFileObject(),
                                            classHandle, ctx.getAccessType(), attrib.getName(), remoteClassName);

                                    Fix fix3 = new CreateManyToManyRelationshipHint(ctx.getFileObject(),
                                            classHandle,
                                            ctx.getAccessType(),
                                            attrib.getName(),
                                            remoteClassName);

                                    Tree elementTree = ctx.getCompilationInfo().getTrees().getTree(attrib.getJavaElement());

                                    Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                                            ctx.getCompilationInfo(), elementTree);

                                    ErrorDescription error = ErrorDescriptionFactory.forSpan(
                                            hc,
                                            underlineSpan.getStartOffset(),
                                            underlineSpan.getEndOffset(),
                                            NbBundle.getMessage(MVRelationshipForEntityTypeAttrDefined.class, "MSG_MVEntityRelationNotDefined"),
                                            fix1, fix2, fix3);
                                    ret.add(error);
                                }
                                return null;
                            }
                        });
                    } catch (IOException ex) {
                    }
                }
            }
        }

        return ret;
    }
}
