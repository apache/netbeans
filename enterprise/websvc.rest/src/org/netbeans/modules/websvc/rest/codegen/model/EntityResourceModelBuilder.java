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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author PeterLiu
 * @author ads
 */
public class EntityResourceModelBuilder {

    private Map<String, EntityClassInfo> entitiesInRelationMap;
    private Map<String, EntityClassInfo> allEntitiesClassInfoMap;
    private EntityResourceBeanModel model;

    /** Creates a new instance of ModelBuilder */
    public EntityResourceModelBuilder(Project project, Collection<String> entities) {
        entitiesInRelationMap = new HashMap<String, EntityClassInfo>();
        allEntitiesClassInfoMap = new HashMap<String, EntityClassInfo>();
        for (String entity : entities) {
            try {
                EntityClassInfo info = null;

                ElementHandle<TypeElement> handle = SourceGroupSupport.
                    getHandleClassName(entity, project);
                if ( handle!= null) 
                {
                    info = new EntityClassInfo(entity, handle , project, this);
                }
                if (info != null ){
                    allEntitiesClassInfoMap.put(entity, info);
                    if ( !info.getFieldInfos().isEmpty()) {
                        entitiesInRelationMap.put(entity, info);
                    }
                }
                
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }


    public Set<EntityClassInfo> getEntityInfos() {
        return new HashSet<EntityClassInfo>(allEntitiesClassInfoMap.values());
    }

    public Set<String> getAllEntityNames() {
        return allEntitiesClassInfoMap.keySet();
    }

    public EntityClassInfo getEntityClassInfo(String type) {
        return allEntitiesClassInfoMap.get(type);
    }

    public EntityResourceBeanModel build() {
        model = new EntityResourceBeanModel(this);
        try {
            for (Entry<String, EntityClassInfo> entry : entitiesInRelationMap.entrySet()) {
                String fqn = entry.getKey();
                EntityClassInfo info = entry.getValue();
                model.addEntityInfo( fqn, info);
                computeRelationships( info );
            }

            model.setValid(true);

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            model.setValid(false);
        }

        entitiesInRelationMap.clear();
        return model;
    }

    private void computeRelationships(EntityClassInfo info) {      
        for (FieldInfo fieldInfo : info.getFieldInfos()) {
            if (fieldInfo.isRelationship()) {
                if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                    String typeArg = fieldInfo.getTypeArg();
                    EntityClassInfo classInfo = allEntitiesClassInfoMap.get( typeArg );
                    model.addEntityInfo( typeArg, classInfo);
                } else {
                    String type = fieldInfo.getType();
                    EntityClassInfo classInfo = allEntitiesClassInfoMap.get(type);
                    model.addEntityInfo( type, classInfo);
                }
            }
        }
    }
}
