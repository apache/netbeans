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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.persistence.entitygenerator;

/**
 * Abstract information about role in a relationship between 2 entity classes
 * (either CMP or Java Persistence API).
 *
 * @author Chris Webster, Pavel Buzek
 */
public class RelationshipRole {
    
    private String roleName;
    // The Java package the entity is in. Need to remember it - see issue 139804
    private String entityPkgName; 
    private String entityName;
    private String fieldName;
    private boolean many;
    private boolean toMany;
    private boolean cascade;
    private boolean optional;
    
    private EntityRelation parent;
 
    public RelationshipRole (String roleName,
            String entityName,
            String fieldName,
            boolean many,
            boolean toMany,
            boolean cascade) {
        this(roleName, entityName, fieldName, many, toMany, cascade, true);
    }
    
    public RelationshipRole (String roleName,
            String entityName,
            String fieldName,
            boolean many,
            boolean toMany,
            boolean cascade,
            boolean optional) {
        this.setRoleName(roleName);
        this.setEntityName(entityName);
        this.setFieldName(fieldName);
        this.setMany(many);
        this.setToMany(toMany);
        this.setCascade(cascade);
        this.optional = optional;
    }
    
    public RelationshipRole (EntityRelation parentRelation) {
        setParent(parentRelation);
    }
    
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getEntityPkgName() {
        return entityPkgName;
    }

    public void setEntityPkgName(String pkgName) {
        this.entityPkgName = pkgName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isMany() {
        return many;
    }

    public void setMany(boolean many) {
        this.many = many;
    }

    public boolean isToMany() {
        return toMany;
    }

    public void setToMany(boolean toMany) {
        this.toMany = toMany;
    }

    public boolean isCascade() {
        return cascade;
    }

    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }
    
    public boolean isOptional() {
        return this.optional;
    }

    public EntityRelation getParent() {
        return parent;
    }

    public void setParent(EntityRelation parent) {
        this.parent = parent;
    }

}
