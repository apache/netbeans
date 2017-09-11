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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.jpa.verification.fixes.ImplementSerializable;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.IdClass;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.SerializableClass",
        displayName = "#SerializableClass.display.name",
        description = "#SerializableClass.desc",
        category = "javaee/jpa",
        enabled = true,
        suppressWarnings = "SerializableClass")
@NbBundle.Messages({
    "SerializableClass.display.name=Entity impleents Serializable verification",
    "SerializableClass.desc=If an entity instance is to be passed by value as a detached object (e.g., through a remote interface), the entity class must implement the Serializable interface."
})
public class SerializableClass {

    @TriggerPatterns(value = {
        @TriggerPattern(value = JPAAnnotations.ENTITY),
        @TriggerPattern(value = JPAAnnotations.EMBEDDABLE),
        @TriggerPattern(value = JPAAnnotations.ID_CLASS)})
    public static ErrorDescription apply(HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        final JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }

        TypeElement subject = ctx.getJavaClass();

        boolean idClass = "IdClass".equals(hc.getPath().getLeaf().toString());
        if (idClass) {
            final IdClass[] idclass = {null};
            try {
                MetadataModel<EntityMappingsMetadata> model = ModelUtils.getModel(hc.getInfo().getFileObject());
                model.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                    @Override
                    public Void run(EntityMappingsMetadata metadata) {
                        if (ctx.getModelElement() instanceof Entity) {
                            idclass[0] = ((Entity) ctx.getModelElement()).getIdClass();

                        } else if (ctx.getModelElement() instanceof MappedSuperclass) {
                            idclass[0] = ((MappedSuperclass) ctx.getModelElement()).getIdClass();
                        }
                        return null;
                    }
                });
            } catch (IOException ex) {
            }



            if (idclass[0] == null || idclass[0].getClass2() == null) {
                return null;
            }

            subject = hc.getInfo().getElements().getTypeElement(idclass[0].getClass2());
        }

        // Does the entity directly implement java.io.Serializable
        if (subject == null || extendsFromSerializable(subject)) {
            return null;
        }

        // Check if the super class implements java.io.Serializable
        // See issue 139751
        TypeMirror superCls = subject.getSuperclass();
        while (superCls != null && (superCls instanceof DeclaredType)) {
            TypeElement superElem = (TypeElement) ((DeclaredType) superCls).asElement();
            if (extendsFromSerializable(superElem)) {
                return null;
            }
            superCls = superElem.getSuperclass();
        }


        Fix fix = new ImplementSerializable(ctx.getFileObject(), ElementHandle.create(subject));


        if (idClass) {
            return ErrorDescriptionFactory.forTree(
                    hc,
                    hc.getPath().getParentPath(),
                    NbBundle.getMessage(SerializableClass.class, "MSG_NonSerializableClass"),
                    fix);
        } else {
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
                    NbBundle.getMessage(SerializableClass.class, "MSG_NonSerializableClass"),
                    fix);
        }

    }

    private static boolean extendsFromSerializable(TypeElement subject) {
        for (TypeMirror iface : subject.getInterfaces()) {
            if ("java.io.Serializable".equals(iface.toString())) { //NOI18N
                return true;
            } else if (iface instanceof DeclaredType) {
                DeclaredType iType = (DeclaredType) iface;
                if (extendsFromSerializable((TypeElement) iType.asElement())) {
                    return true;
                }
            }
        }
        return false;
    }
}
