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

public interface MappedSuperclass {

    public final String FIELD_ACCESS = "FIELD"; // NOI18N
    public final String PROPERTY_ACCESS = "PROPERTY"; // NOI18N

    public void setClass2(String value);
    
    public String getClass2();
    
    public void setAccess(String value);
    
    public String getAccess();
    
    public void setMetadataComplete(boolean value);
    
    public boolean isMetadataComplete();
    
    public void setDescription(String value);
    
    public String getDescription();
    
    public void setIdClass(IdClass value);
    
    public IdClass getIdClass();
    
    public IdClass newIdClass();
    
    public void setExcludeDefaultListeners(EmptyType value);
    
    public EmptyType getExcludeDefaultListeners();
    
    public EmptyType newEmptyType();
    
    public void setExcludeSuperclassListeners(EmptyType value);
    
    public EmptyType getExcludeSuperclassListeners();
    
    public void setEntityListeners(EntityListeners value);
    
    public EntityListeners getEntityListeners();
    
    public EntityListeners newEntityListeners();
    
    public void setPrePersist(PrePersist value);
    
    public PrePersist getPrePersist();
    
    public PrePersist newPrePersist();
    
    public void setPostPersist(PostPersist value);
    
    public PostPersist getPostPersist();
    
    public PostPersist newPostPersist();
    
    public void setPreRemove(PreRemove value);
    
    public PreRemove getPreRemove();
    
    public PreRemove newPreRemove();
    
    public void setPostRemove(PostRemove value);
    
    public PostRemove getPostRemove();
    
    public PostRemove newPostRemove();
    
    public void setPreUpdate(PreUpdate value);
    
    public PreUpdate getPreUpdate();
    
    public PreUpdate newPreUpdate();
    
    public void setPostUpdate(PostUpdate value);
    
    public PostUpdate getPostUpdate();
    
    public PostUpdate newPostUpdate();
    
    public void setPostLoad(PostLoad value);
    
    public PostLoad getPostLoad();
    
    public PostLoad newPostLoad();
    
    public void setAttributes(Attributes value);
    
    public Attributes getAttributes();
    
    public Attributes newAttributes();
    
}
