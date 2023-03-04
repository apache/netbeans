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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.util.NbBundle;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidVersionType extends JPAEntityAttributeCheck {

    private static Collection<String> validVersionTypes = new TreeSet<String>(Arrays.asList(
            "java.lang.Short", "java.lang.Integer", // NOI18N
            "java.lang.Long", "short", "int", "long", // NOI18N
            "java.sql.Timestamp" // NOI18N
            ));

    public Collection<ErrorDescription> check(JPAProblemContext ctx, HintContext hc, AttributeWrapper attrib) {
        if (attrib.getModelElement() instanceof Version) {
            TreeUtilities treeUtils = ctx.getCompilationInfo().getTreeUtilities();
            Types types = ctx.getCompilationInfo().getTypes();
            TypeMirror attrType = attrib.getType();

            for (String typeName : validVersionTypes) {
                TypeMirror type = treeUtils.parseType(typeName,
                        ctx.getJavaClass());

                if (type != null && types.isSameType(attrType, type)) {
                    return null;
                }
            }
            Tree elementTree = ctx.getCompilationInfo().getTrees().getTree(attrib.getJavaElement());

            Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                    ctx.getCompilationInfo(), elementTree);

            ErrorDescription error = ErrorDescriptionFactory.forSpan(
                    hc,
                    underlineSpan.getStartOffset(),
                    underlineSpan.getEndOffset(),
                    NbBundle.getMessage(ValidVersionType.class, "MSG_InvalidVersionType"));//TODO: may need to have "error" fo some/ warning for another
            return Collections.singleton(error);

        }

        return null;
    }
}
