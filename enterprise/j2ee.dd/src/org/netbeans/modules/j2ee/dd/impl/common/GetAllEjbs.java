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

/**
 * Superclass that implements DescriptionInterface for Servlet2.4 beans.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public abstract class GetAllEjbs extends EnclosingBean {
    
    public GetAllEjbs(java.util.Vector comps, Version version) {
        super(comps, version);
    }
    
    public abstract Entity[] getEntity();
    public abstract MessageDriven[] getMessageDriven();
    public abstract Session[] getSession();
    
    public abstract int sizeSession();
    public abstract int sizeEntity();
    public abstract int sizeMessageDriven();
    public abstract int removeSession(Session s);
    public abstract int removeEntity(Entity e);
    public abstract int removeMessageDriven(MessageDriven m);
    
    public void removeEjb(Ejb value){
        
        if(value instanceof Entity){
            removeEntity((Entity) value);
        }
        else  if(value instanceof Session){
            removeSession((Session) value);
        }
        else  if(value instanceof MessageDriven){
            removeMessageDriven((MessageDriven) value);
        }
        
        
    }
    public Ejb[] getEjbs(){
        int sizeEntity = sizeEntity();
        int sizeSession = sizeSession();
        int sizeMessageDriven = sizeMessageDriven();
        int size = sizeEntity + sizeSession + sizeMessageDriven;
        
        Ejb[] ejbs = new Ejb[size];
        Entity[] enBeans = getEntity();
        Session[] ssbeans = getSession();
        MessageDriven[] mdbeans = getMessageDriven();
        int addindex=0;
        for(int i=0; i<sizeEntity ; i++){
            ejbs[addindex] = (Ejb)enBeans[i];
            addindex++;
        }
        for(int j=0; j<sizeSession ; j++){
            ejbs[addindex] = (Ejb)ssbeans[j];
            addindex++;
        }
        
        for(int j=0; j<sizeMessageDriven ; j++){
            ejbs[addindex] = (Ejb)mdbeans[j];
            addindex++;
        }
        return ejbs;
    }
    
}
