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
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.db.api.sql.SQLKeywords;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidColumnName extends JPAEntityAttributeCheck {

    public Collection<ErrorDescription> check(JPAProblemContext ctx, HintContext hc, AttributeWrapper attrib) {
        String columnName = attrib.getColumn().getName();

        if (columnName.length() == 0) {
            return getErr(ctx, hc, attrib, "MSG_AttrInvalidPersistenceQLIdentifier",
                    columnName);
        }

        if (JavaPersistenceQLKeywords.isKeyword(columnName)) {
            return getErr(ctx, hc, attrib, "MSG_AttrNamedWithJavaPersistenceQLKeyword",
                    columnName);
        }

        if (SQLKeywords.isSQL99ReservedKeyword(columnName)) {
            return getErr(ctx, hc, attrib, "MSG_AttrNamedWithReservedSQLKeyword",
                    columnName);
        }

        return null;
    }

    private static Collection<ErrorDescription> getErr(JPAProblemContext ctx, HintContext hc, AttributeWrapper attrib, String msgKey, String msgPar) {
        Tree elementTree = ctx.getCompilationInfo().getTrees().getTree(attrib.getJavaElement());

        if(elementTree == null) {
            return null;
        }
        
        Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                ctx.getCompilationInfo(), elementTree);

        ErrorDescription error = ErrorDescriptionFactory.forSpan(
                hc,
                underlineSpan.getStartOffset(),
                underlineSpan.getEndOffset(),
                NbBundle.getMessage(ValidColumnName.class, msgKey, msgPar));//TODO: may need to have "error" as default
        return Collections.singleton(error);
    }
}
