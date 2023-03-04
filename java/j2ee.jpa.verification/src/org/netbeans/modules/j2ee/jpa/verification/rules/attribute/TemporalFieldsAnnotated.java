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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateTemporalAnnotationHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class TemporalFieldsAnnotated extends JPAEntityAttributeCheck {

    private static Collection<String> temporalTypes =
            Arrays.asList("java.util.Calendar", "java.util.Date"); //NOI18N

    public Collection<ErrorDescription> check(JPAProblemContext ctx, HintContext hc, AttributeWrapper attrib) {
        String temporal = attrib.getTemporal();

        if (temporal == null || temporal.length() == 0) {
            if (temporalTypes.contains(attrib.getType().toString())) {
                Fix fix = new CreateTemporalAnnotationHint(ctx.getFileObject(),
                        ElementHandle.create(attrib.getJavaElement()));

                Tree elementTree = ctx.getCompilationInfo().getTrees().getTree(attrib.getJavaElement());

                Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                        ctx.getCompilationInfo(), elementTree);

                ErrorDescription error = ErrorDescriptionFactory.forSpan(
                        hc,
                        underlineSpan.getStartOffset(),
                        underlineSpan.getEndOffset(),
                        NbBundle.getMessage(TemporalFieldsAnnotated.class, "MSG_TemporalAttrNotAnnotatedProperly"),
                        fix);//TODO: may need to have "error" as default
                return Collections.singleton(error);
            }
        }

        return null;
    }
}
