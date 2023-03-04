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
import org.netbeans.modules.db.api.sql.SQLKeywords;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.JPAHelper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

/**
 * Table mapped to the entity class name must not be a reserved JPA-QL keyword
 * 
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.ValidPrimaryTableName",
        displayName = "#ValidPrimaryTableName.display.name",
        description = "#ValidPrimaryTableName.desc",
        category = "javaee/jpa",
        enabled = true,
        suppressWarnings = "ValidPrimaryTableName")
@NbBundle.Messages({
    "ValidPrimaryTableName.display.name=Entity table name verification",
    "ValidPrimaryTableName.desc=Entity table name must be valid QL intentifier"
})
public class ValidPrimaryTableName {
    

    @TriggerPattern(value = JPAAnnotations.ENTITY)
    public static ErrorDescription apply(HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }
        
        JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }
        
        Object me= ctx.getModelElement();
        
        if(me == null || !(me instanceof Entity)) {
            return null;
        }

        String tableName = JPAHelper.getPrimaryTableName((Entity) me);
        if(tableName == null){
            return null;
        }
        String entityName = ((Entity) me).getName();
        TreePath par = hc.getPath();
        while(par!=null && par.getParentPath()!=null && par.getLeaf().getKind()!= Tree.Kind.CLASS){
            par = par.getParentPath();
        }
        
        Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                           ctx.getCompilationInfo(), par.getLeaf());

        if (tableName.length() == 0){
            return ErrorDescriptionFactory.forSpan(
                    hc,
                    underlineSpan.getStartOffset(),
                    underlineSpan.getEndOffset(),
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_InvalidPersistenceQLIdentifier"));
        }
        
        if (SQLKeywords.isSQL99ReservedKeyword(tableName)){
            return ErrorDescriptionFactory.forSpan(
                    hc,
                    underlineSpan.getStartOffset(),
                    underlineSpan.getEndOffset(),
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_ClassNamedWithReservedSQLKeyword"));
        }
        
        if (JavaPersistenceQLKeywords.isKeyword(entityName)){
            return ErrorDescriptionFactory.forSpan(
                    hc,
                    underlineSpan.getStartOffset(),
                    underlineSpan.getEndOffset(),
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_ClassNamedWithJavaPersistenceQLKeyword"));
        }
        
        
        return null;
    }
}
