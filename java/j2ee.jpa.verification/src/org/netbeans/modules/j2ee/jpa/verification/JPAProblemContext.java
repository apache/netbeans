/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.jpa.verification;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;

/**
 * @see ProblemContext
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAProblemContext extends ProblemContext {
    private boolean entity;
    private boolean embeddable;
    private boolean idClass;
    private boolean mappedSuperClass;
    private AccessType accessType;
    private EntityMappingsMetadata metadata;
    private Set<CancelListener> cListeners;
    private final Object cListenersLock = new Object();
    
    public boolean isEntity(){
        return entity;
    }
    
    public void setEntity(boolean entity){
        this.entity = entity;
    }
    
    public boolean isEmbeddable(){
        return embeddable;
    }
    
    public void setEmbeddable(boolean embeddable){
        this.embeddable = embeddable;
    }
    
    public boolean isIdClass(){
        return idClass;
    }
    
    public void setIdClass(boolean idClass){
        this.idClass = idClass;
    }
    
    public boolean isMappedSuperClass(){
        return mappedSuperClass;
    }
    
    public void setMappedSuperClass(boolean mappedSuperClass){
        this.mappedSuperClass = mappedSuperClass;
    }
    
    public AccessType getAccessType(){
        return accessType;
    }
    
    public void setAccessType(AccessType accessType){
        this.accessType = accessType;
    }
    
    public EntityMappingsMetadata getMetaData(){
        return metadata;
    }
    
    public void setMetaData(EntityMappingsMetadata metadata){
        this.metadata = metadata;
    }
    
    public boolean isJPAClass(){
        return entity || embeddable || idClass || mappedSuperClass;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
        if(cancelled && cListeners != null) {
            synchronized(cListenersLock) {
                for(CancelListener cl:cListeners) {
                    cl.cancelled();
                }
            }
        }
    }

    public void addCancelListener(CancelListener aThis) {
        if(cListeners == null) {
            cListeners = new HashSet<>();
        }
        synchronized(cListenersLock) {
            cListeners.add(aThis);
        }
    }
    
    public void removeCancelListener(CancelListener cl) {
        if(cListeners != null) {
            synchronized(cListenersLock) {
                cListeners.remove(cl);
            }
        }
    }
}
