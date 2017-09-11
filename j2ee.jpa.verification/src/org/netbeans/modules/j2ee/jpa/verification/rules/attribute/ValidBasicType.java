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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.util.NbBundle;

/**
 * The following types are supported for Basic properties: Java primitive types,
 * wrapper of primitive types, java.lang.String, java.math.BigInteger,
 * java.math.BigDecimal, java.util.Date, java.util.Calendar, java.sql.Date,
 * java.sql.Time, java.sql.TimeStamp, byte[], Byte[], char[], Character[],
 * enums, and any other type that implements Serializable.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidBasicType extends JPAEntityAttributeCheck {

    private static Collection<String> fixedBasicTypes = new TreeSet<String>(Arrays.asList(
            "java.lang.Byte", "java.lang.Character", "java.lang.Short", "java.lang.Integer", // NOI18N
            "java.lang.Long", "byte", "char", "short", "int", "long", // NOI18N
            "float", "double", "java.lang.Float", "java.lang.Double", // NOI18N
            "java.util.Date", "java.util.Calendar", // NOI18N
            "java.sql.Date", "java.sql.Time", "java.sql.Timestamp", // NOI18N
            "byte[]", "java.lang.Byte[]", "char[]", "java.lang.Character[]" // NOI18N
            ));

    public Collection<ErrorDescription> check(JPAProblemContext ctx, HintContext hc, AttributeWrapper attrib) {
        if (!(attrib.getModelElement() instanceof Basic)) {
            return null;
        }

        TreeUtilities treeUtils = ctx.getCompilationInfo().getTreeUtilities();
        Types types = ctx.getCompilationInfo().getTypes();
        TypeMirror attrType = attrib.getType();

        TypeMirror typeSerializable = treeUtils.parseType("java.io.Serializable", //NOI18N
                ctx.getJavaClass());

        TypeMirror typeEnum = treeUtils.parseType("java.lang.Enum", //NOI18N
                ctx.getJavaClass());

        TypeMirror typeCollection = treeUtils.parseType("java.util.Collection", //NOI18N
                ctx.getJavaClass());

        if (types.isAssignable(attrType, typeSerializable)
                || types.isAssignable(attrType, typeEnum)
                || types.isAssignable(attrType, typeCollection)) {
            return null;
        }

        for (String typeName : fixedBasicTypes) {
            TypeMirror type = treeUtils.parseType(typeName,
                    ctx.getJavaClass());

            if (type != null && types.isSameType(attrType, type)) {
                return null;
            }
        }

        if (Utilities.hasAnnotation(attrib.getJavaElement(), JPAAnnotations.ELEMENT_COLLECTION)) {
            //according to annotation it's not basic  type and need to be verified in appropriate validator
            return null;
        }
        if (Utilities.hasAnnotation(attrib.getJavaElement(), JPAAnnotations.EMBEDDED)) {
            //@Embedded, see also #167419
            return null;
        }
        Tree elementTree = ctx.getCompilationInfo().getTrees().getTree(attrib.getJavaElement());

        Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                ctx.getCompilationInfo(), elementTree);

        ErrorDescription error = ErrorDescriptionFactory.forSpan(
                hc,
                underlineSpan.getStartOffset(),
                underlineSpan.getEndOffset(),
                NbBundle.getMessage(ValidBasicType.class, "MSG_ValidBasicType"));//TODO: may need to have "error" as default
        return Collections.singleton(error);
    }
}
