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
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.IdClass;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

/**
 * An entity sub-class can not have Id field or property. Because that will lead
 * to multiple Ids in hierarchy. Thus, an entity subclass can not also have
 * IdClass.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.NoIdClassOnEntitySubclass",
        displayName = "#NoIdClassOnEntitySubclass.display.name",
        description = "#MSG_EntitySubclassHasIdClass",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.ERROR,
        suppressWarnings = "NoIdClassOnEntitySubclass")
@NbBundle.Messages({
    "NoIdClassOnEntitySubclass.display.name=Verify IdClass in entity subclass"})
public class NoIdClassOnEntitySubclass {

    @TriggerPattern(value = JPAAnnotations.ENTITY)
    public static ErrorDescription apply(final HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        final JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }
        final Entity entity = ctx.getModelElement() instanceof Entity ? (Entity) ctx.getModelElement() : null;
        if (entity == null) {
            return null;
        }
        final TypeElement subject = ctx.getJavaClass();

        final ErrorDescription[] ret = {null};

        try {
            MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
            model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                @Override
                public Void run(EntityMappingsMetadata metadata) {
                    IdClass idclass = entity.getIdClass();
                    //
                    if (idclass == null) {
                        TypeMirror superClassType = subject.getSuperclass();

                        if (superClassType.getKind() == TypeKind.DECLARED) {
                            TypeElement superClassElem = (TypeElement) ((DeclaredType) superClassType).asElement();
                            Entity parentEntity = ModelUtils.getEntity(((JPAProblemContext) ctx).getMetaData(), superClassElem);

                            if (parentEntity != null) {
                                Set<String> parentEntityIds = new HashSet<String>(1);

                                for (Id id : parentEntity.getAttributes().getId()) {
                                    parentEntityIds.add(id.getName());
                                }

                                for (Id id : entity.getAttributes().getId()) {
                                    if (!parentEntityIds.contains(id.getName())) {
                                        // Found id defined directly on the child entity
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
                                                NbBundle.getMessage(NoIdClassOnEntitySubclass.class, "MSG_EntitySubclassHasIdClass"));
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                    return null;
                    //
                }
            });
        } catch (IOException ex) {
        }

        return ret[0];
    }
}
