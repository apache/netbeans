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
