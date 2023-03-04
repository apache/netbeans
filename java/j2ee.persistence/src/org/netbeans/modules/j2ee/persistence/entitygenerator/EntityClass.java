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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType;
import org.openide.filesystems.FileObject;

/**
 * Abstract description of an entity class
 * (either CMP or Java Persistence API).
 *
 * @author Chris Webster, Martin Adamek, Andrei Badea
 */
public class EntityClass {
    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private final FileObject rootFolder;
    private final String className;
    private final String packageName;
    private final Set<List<String>> uniqueConstraints;
    private final UpdateType updateType;
    
    private List<RelationshipRole> roles;
    private List<EntityMember> fields;
    private boolean usePkField;
    private boolean derivedIdCandidate;//derived id should be used in jpa2.0+
    private String pkFieldName;
    private CMPMappingModel mappingModel;
    
    private boolean forTable = true;  // false means forView
    private final boolean useDefaults;
    
    public EntityClass( String catalogName, String schemaName, String tableName, 
            FileObject rootFolder, String packageName, String className, UpdateType updateType, boolean useDefaults, Set<List<String>> uniqueConstraints) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.rootFolder = rootFolder;
        this.packageName = packageName;
        this.className = className;
        this.updateType = updateType;
        this.uniqueConstraints = uniqueConstraints;
        this.useDefaults = useDefaults;
        
        roles = Collections.<RelationshipRole>emptyList();
        fields = new ArrayList<EntityMember>();
        mappingModel = new CMPMappingModel();
    }
    
    public boolean isForTable() {
        return this.forTable;
    }
    
    public void setIsForTable( boolean forTable) {
        this.forTable = forTable;
    }
    
    public Set<List<String>> getUniqueConstraints() {
        return this.uniqueConstraints;
    }
    
    public void addRole(RelationshipRole role) {
        if (roles == Collections.<RelationshipRole>emptyList()) {
            roles = new ArrayList<RelationshipRole>();
        }
        roles.add(role);
    }
    
    public List<RelationshipRole> getRoles() {
        return roles;
    }
    
    public List<EntityMember> getFields() {
        return fields;
    }
    
    public boolean getUseDefaults(){
        return useDefaults;
    }
    
    public void setFields(List<EntityMember> fields) {
        this.fields = fields;
    }
    
    @Override
    public String toString() {
        String cmpFields = ""; // NOI18N
        for (EntityMember entityMember : getFields()) {
            cmpFields += " " + entityMember.getMemberName() + (entityMember.isPrimaryKey() ? " (PK) " : " "); // NOI18N
        }
        return "bean name " + getClassName() + // NOI18N
                "\ncmp-fields "+ cmpFields;  // NOI18N
    }
    
    public FileObject getRootFolder() {
        return rootFolder;
    }
    
    public String getPackage() {
        return packageName;
    }
    
    public String getCatalogName() {
        return catalogName;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getClassName() {
        return className;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public FileObject getPackageFileObject() {
        String relative = packageName.replace('.', '/');
        return rootFolder.getFileObject(relative);
    }
    
    public CMPMappingModel getCMPMapping() {
        mappingModel.getCMPFieldMapping().clear();
        for (EntityMember entityMember : getFields()) {
            mappingModel.setTableName(entityMember.getTableName());
            mappingModel.getCMPFieldMapping().put(entityMember.getMemberName(), entityMember.getColumnName());
        }
        return mappingModel;
    }
    
    public void usePkField(boolean usePkField) {
        this.usePkField = usePkField;
    }
    
    public boolean isUsePkField() {
        return usePkField;
    }
    
    public String getPkFieldName() {
        return pkFieldName;
    }
    
    public void setPkFieldName(String pkFieldName) {
        this.pkFieldName = pkFieldName;
    }

    /**
     * @return the derivedIdCandidate
     */
    public boolean isDerivedIdCandidate() {
        return derivedIdCandidate;
    }

    /**
     * @param derivedIdCandidate the derivedIdCandidate to set
     */
    public void setDerivedIdCandidate(boolean derivedIdCandidate) {
        this.derivedIdCandidate = derivedIdCandidate;
    }
}
