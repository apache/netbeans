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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.FindCapability;

public interface EnterpriseBeans extends CommonDDBean, FindCapability {

        public static final String SESSION = "Session";	// NOI18N
	public static final String ENTITY = "Entity";	// NOI18N
	public static final String MESSAGE_DRIVEN = "MessageDriven";	// NOI18N
        
        public void setSession(int index, Session value);
        
        public void setSession(Session[] value);
        
        public Session getSession(int index);       

        public Session[] getSession();
        
	public int addSession(org.netbeans.modules.j2ee.dd.api.ejb.Session value);

	public int removeSession(org.netbeans.modules.j2ee.dd.api.ejb.Session value);
        
        public int sizeSession();
        
        public Session newSession();
                
        public void setEntity(int index, Entity value);
        
        public void setEntity(Entity[] value);
        
        public Entity getEntity(int index);       

        public Entity[] getEntity();
        
	public int removeEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity value);

	public int addEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity value);
        
        public int sizeEntity();
	
        public Entity newEntity();
        
        public void setMessageDriven(int index, MessageDriven value);

        public MessageDriven getMessageDriven(int index);

        public void setMessageDriven(MessageDriven[] value);

        public MessageDriven[] getMessageDriven();
        
	public int addMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven value);

	public int sizeMessageDriven();

	public int removeMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven value);

        public MessageDriven newMessageDriven();
        
        public Ejb[] getEjbs();
        
        public void removeEjb( Ejb value);
        
}


