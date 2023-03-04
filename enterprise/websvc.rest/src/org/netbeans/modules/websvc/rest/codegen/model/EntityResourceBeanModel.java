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

package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PeterLiu
 * @author ads
 */
public class EntityResourceBeanModel {
    private EntityResourceModelBuilder builder;
    private Map<String, EntityClassInfo> entityInfos;
    private boolean valid;
    
    
    /** Creates a new instance of ResourceBeanModel */
    public EntityResourceBeanModel(EntityResourceModelBuilder builder) {
        this.builder = builder;
        entityInfos = new HashMap<String, EntityClassInfo>();
    }
    
    public EntityClassInfo getEntityInfo(String fqn ){
        return entityInfos.get( fqn );
    }
    
    public Collection<EntityClassInfo> getEntityInfos() {
        List<EntityClassInfo> infos = new ArrayList<EntityClassInfo>();
        
        infos.addAll( entityInfos.values());
        return infos;
    }
    
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean v) {
        valid = v;
    }
    
    public EntityResourceModelBuilder getBuilder() {
        return builder;
    }
    
    void addEntityInfo( String fqn , EntityClassInfo info ){
        if ( info == null ){
            return;
        }
        entityInfos.put( fqn , info );
    }
}
