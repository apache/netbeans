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
package org.netbeans.modules.j2ee.jpa.verification.rules.attribute;

import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateManyToOneRelationshipHint;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateOneToOneRelationshipHint;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateUnidirManyToOneRelationship;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateUnidirOneToOneRelationship;
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
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class RelationshipForEntityTypeAttrDefined extends JPAEntityAttributeCheck {

    @Override
    public Collection<ErrorDescription> check(final JPAProblemContext ctx, final HintContext hc, final AttributeWrapper attrib) {

        // Not applicable for embeddable classes, which do not have relationships.
        if (ctx.isEmbeddable()) {
            return null;
        }


        final Element typeElement = ctx.getCompilationInfo().getTypes().asElement(attrib.getType());
        final ArrayList<ErrorDescription> ret = new ArrayList<>();

        if (typeElement != null && typeElement.getKind() == ElementKind.CLASS) {
            try {
                MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
                model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                    @Override
                    public Void run(EntityMappingsMetadata metadata) {


                        Entity entity = ModelUtils.getEntity(ctx.getMetaData(), ((TypeElement) typeElement));

                        if (entity != null) {
                            //not appliable to derived ids, it's already attribute and relationship
                            //if id and have one-one and many-one
                            List<? extends AnnotationMirror> anns = attrib.getJavaElement().getAnnotationMirrors();
                            if (anns != null) {
                                boolean id = false;
                                boolean rel = false;
                                for (AnnotationMirror ann : anns) {
                                    ann.getAnnotationType().asElement().toString();
                                    if (ann.getAnnotationType().asElement().toString().equals("javax.persistence.Id")) {//NOI18N
                                        id = true;//NOI18N
                                    } else if (ann.getAnnotationType().asElement().toString().equals("javax.persistence.ManyToOne") || ann.getAnnotationType().asElement().toString().equals("javax.persistence.OneToOne")) {//NOI18N
                                        rel = true;//NOI18N
                                    }
                                    if (id && rel) {
                                        return null;//in future more strict verification may be done
                                    }
                                }

                            }
                            //other cases
                            ElementHandle<TypeElement> classHandle = ElementHandle.create(ctx.getJavaClass());
                            ElementHandle<Element> elemHandle = ElementHandle.create(attrib.getJavaElement());
                            String remoteClassName = ((TypeElement) typeElement).getQualifiedName().toString();

                            Fix fix1 = new CreateUnidirOneToOneRelationship(ctx.getFileObject(),
                                    classHandle, elemHandle);

                            Fix fix2 = new CreateOneToOneRelationshipHint(ctx.getFileObject(),
                                    classHandle,
                                    ctx.getAccessType(),
                                    attrib.getName(),
                                    remoteClassName);

                            Fix fix3 = new CreateUnidirManyToOneRelationship(ctx.getFileObject(),
                                    classHandle,
                                    elemHandle);

                            Fix fix4 = new CreateManyToOneRelationshipHint(ctx.getFileObject(),
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
                                    NbBundle.getMessage(RelationshipForEntityTypeAttrDefined.class, "MSG_EntityRelationNotDefined"),
                                    fix1, fix2, fix3, fix4);
                            ret.add(error);


                        }
                        return null;
                    }
                });
            } catch (IOException ex) {
            }
        }

        return ret;
    }
}
