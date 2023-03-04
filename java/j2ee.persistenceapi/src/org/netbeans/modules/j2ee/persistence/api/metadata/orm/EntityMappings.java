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

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface EntityMappings {

    public void setVersion(String value);

    public String getVersion();
    
    public void setDescription(String value);
    
    public String getDescription();
    
    public void setPersistenceUnitMetadata(PersistenceUnitMetadata value);
    
    public PersistenceUnitMetadata getPersistenceUnitMetadata();
    
    public PersistenceUnitMetadata newPersistenceUnitMetadata();
    
    public void setPackage(String value);
    
    public String getPackage();
    
    public void setSchema(String value);
    
    public String getSchema();
    
    public void setCatalog(String value);
    
    public String getCatalog();
    
    public void setAccess(String value);
    
    public String getAccess();
    
    public void setSequenceGenerator(int index, SequenceGenerator value);
    
    public SequenceGenerator getSequenceGenerator(int index);
    
    public int sizeSequenceGenerator();
    
    public void setSequenceGenerator(SequenceGenerator[] value);
    
    public SequenceGenerator[] getSequenceGenerator();
    
    public int addSequenceGenerator(SequenceGenerator value);
    
    public int removeSequenceGenerator(SequenceGenerator value);
    
    public SequenceGenerator newSequenceGenerator();
    
    public void setTableGenerator(int index, TableGenerator value);
    
    public TableGenerator getTableGenerator(int index);
    
    public int sizeTableGenerator();
    
    public void setTableGenerator(TableGenerator[] value);
    
    public TableGenerator[] getTableGenerator();
    
    public int addTableGenerator(TableGenerator value);
    
    public int removeTableGenerator(TableGenerator value);
    
    public TableGenerator newTableGenerator();
    
    public void setNamedQuery(int index, NamedQuery value);
    
    public NamedQuery getNamedQuery(int index);
    
    public int sizeNamedQuery();
    
    public void setNamedQuery(NamedQuery[] value);
    
    public NamedQuery[] getNamedQuery();
    
    public int addNamedQuery(NamedQuery value);
    
    public int removeNamedQuery(NamedQuery value);
    
    public NamedQuery newNamedQuery();
    
    public void setNamedNativeQuery(int index, NamedNativeQuery value);
    
    public NamedNativeQuery getNamedNativeQuery(int index);
    
    public int sizeNamedNativeQuery();
    
    public void setNamedNativeQuery(NamedNativeQuery[] value);
    
    public NamedNativeQuery[] getNamedNativeQuery();
    
    public int addNamedNativeQuery(NamedNativeQuery value);
    
    public int removeNamedNativeQuery(NamedNativeQuery value);
    
    public NamedNativeQuery newNamedNativeQuery();
    
    public void setSqlResultSetMapping(int index, SqlResultSetMapping value);
    
    public SqlResultSetMapping getSqlResultSetMapping(int index);
    
    public int sizeSqlResultSetMapping();
    
    public void setSqlResultSetMapping(SqlResultSetMapping[] value);
    
    public SqlResultSetMapping[] getSqlResultSetMapping();
    
    public int addSqlResultSetMapping(SqlResultSetMapping value);
    
    public int removeSqlResultSetMapping(SqlResultSetMapping value);
    
    public SqlResultSetMapping newSqlResultSetMapping();
    
    public void setMappedSuperclass(int index, MappedSuperclass value);
    
    public MappedSuperclass getMappedSuperclass(int index);
    
    public int sizeMappedSuperclass();
    
    public void setMappedSuperclass(MappedSuperclass[] value);
    
    public MappedSuperclass[] getMappedSuperclass();
    
    public int addMappedSuperclass(MappedSuperclass value);
    
    public int removeMappedSuperclass(MappedSuperclass value);
    
    public MappedSuperclass newMappedSuperclass();
    
    public void setEntity(int index, Entity value);
    
    public Entity getEntity(int index);
    
    public int sizeEntity();
    
    public void setEntity(Entity[] value);
    
    public Entity[] getEntity();
    
    public int addEntity(Entity value);
    
    public int removeEntity(Entity value);
    
    public Entity newEntity();
    
    public void setEmbeddable(int index, Embeddable value);
    
    public Embeddable getEmbeddable(int index);
    
    public int sizeEmbeddable();
    
    public void setEmbeddable(Embeddable[] value);
    
    public Embeddable[] getEmbeddable();
    
    public int addEmbeddable(Embeddable value);
    
    public int removeEmbeddable(Embeddable value);
    
    public Embeddable newEmbeddable();
    
}
