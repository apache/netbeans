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

package org.netbeans.modules.j2ee.jboss4.config;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.jboss4.config.EjbDeploymentConfiguration.BEAN_TYPE;
import org.netbeans.modules.j2ee.jboss4.config.gen.EnterpriseBeans;
import org.netbeans.modules.j2ee.jboss4.config.gen.Entity;
import org.netbeans.modules.j2ee.jboss4.config.gen.Jboss;
import org.netbeans.modules.j2ee.jboss4.config.gen.MessageDriven;
import org.netbeans.modules.j2ee.jboss4.config.gen.ResourceRef;
import org.netbeans.modules.j2ee.jboss4.config.gen.Session;

/**
 * This class implements the core of the jboss.xml file modifications.
 *
 * @author lkotouc
 */
final class JBossDataSourceRefModifier {

    /**
     * Add a reference to the given resource to the enterprise beans of the given type if it does not exist yet.
     *
     * @param modifiedJboss Jboss graph instance being modified
     * @param resRefName resource reference name
     * @param beanNames the beans (ejb-name value) which might need to add resource reference specified by resRefName
     * @param beanType type of bean to add resource reference to
     * @param jndiName JNDI name of the resource
     */
    static void modify(Jboss modifiedJboss, String resRefName, Set beanNames, BEAN_TYPE beanType, String jndiName) {

        assert(beanNames.size() > 0);

        if (modifiedJboss.getEnterpriseBeans() == null)
            modifiedJboss.setEnterpriseBeans(new EnterpriseBeans());

        if (beanType == BEAN_TYPE.SESSION) {
            addSessionResReference(modifiedJboss, resRefName, beanNames, jndiName);
        } else
        if (beanType == BEAN_TYPE.ENTITY) {
            addEntityResReference(modifiedJboss, resRefName, beanNames, jndiName);
        }
    }
    
    /**
     * Add a new resource reference to the session beans without it.
     * 
     * @param modifiedJboss Jboss instance being modified
     * @param resRefName resource reference name
     * @param sessionNames the sessions (ejb-name value) which might need to add resource reference specified by resRefName
     * @param jndiName JNDI name of the resource
     */
    private static void addSessionResReference(Jboss modifiedJboss, String resRefName, Set sessionNames, String jndiName) {

        List/*<Session>*/ sesssionsWithoutReference = new LinkedList();

        EnterpriseBeans eb = modifiedJboss.getEnterpriseBeans();
        
        Session[] sessions = eb.getSession();
        for (int i = 0; i < sessions.length; i++) {
            String ejbName = sessions[i].getEjbName();
            if (sessionNames.contains(ejbName)) { // session found -> check whether it has the resource-ref
                sessionNames.remove(ejbName);     // we don't care about it anymore
                ResourceRef[] resourceRefs = sessions[i].getResourceRef();
                int j = 0;
                for ( ; j < resourceRefs.length; j++) {
                    String rrn = resourceRefs[j].getResRefName();
                    if (resRefName.equals(rrn))
                        break; // resource-ref found, continuing with the next session
                }
                if (j == resourceRefs.length) // resource-ref not found
                    sesssionsWithoutReference.add(sessions[i]);
            }
        }

        //no session tag yet (sessions.length == 0) or 
        //there are sessions in sessionNames which were not found among the existing ones (those were not removed)
        for (Iterator it = sessionNames.iterator(); it.hasNext(); ) {
            String sessionName = (String)it.next();
            Session session = new Session();
            session.setEjbName(sessionName);
            session.setJndiName(sessionName);

            //add the new session to enterprise-beans
            eb.addSession(session);

            //add the new session to the list of sessions without the resource reference
            sesssionsWithoutReference.add(session);
        }

        //the resource reference will be added to each session without it
        for (Iterator it = sesssionsWithoutReference.iterator(); it.hasNext(); ) {
            ResourceRef newRR = new ResourceRef();
            newRR.setResRefName(resRefName);
            newRR.setJndiName(jndiName);
            Session session = (Session)it.next();
            session.addResourceRef(newRR);
        }

    }
    
    /**
     * Add a new resource reference to the entity beans without it.
     * 
     * @param modifiedJboss Jboss instance being modified
     * @param resRefName resource reference name
     * @param entityNames the entities (ejb-name value) which might need to add resource reference specified by resRefName
     * @param jndiName JNDI name of the resource
     */
    private static void addEntityResReference(Jboss modifiedJboss, String resRefName, Set entityNames, String jndiName) {

        List/*<Entity>*/ entitiesWithoutReference = new LinkedList();

        EnterpriseBeans eb = modifiedJboss.getEnterpriseBeans();

        Entity[] entities = eb.getEntity();
        for (int i = 0; i < entities.length; i++) {
            String ejbName = entities[i].getEjbName();
            if (entityNames.contains(ejbName)) { // entity found -> check whether it has the resource-ref
                entityNames.remove(ejbName);     // we don't care about it anymore
                ResourceRef[] resourceRefs = entities[i].getResourceRef();
                int j = 0;
                for ( ; j < resourceRefs.length; j++) {
                    String rrn = resourceRefs[j].getResRefName();
                    if (resRefName.equals(rrn))
                        break; // resource-ref found, continuing with the next entity
                }
                if (j == resourceRefs.length) // resource-ref not found
                    entitiesWithoutReference.add(entities[i]);
            }
        }

        //no entity tag yet (entities.length == 0) or 
        //there are entities in entityNames which were not found among the existing ones (those were not removed)
        for (Iterator it = entityNames.iterator(); it.hasNext(); ) {
            String entityName = (String)it.next();
            Entity entity = new Entity();
            entity.setEjbName(entityName);
            entity.setJndiName(entityName);

            //add the new entity to enterprise-beans
            eb.addEntity(entity);

            //add the new entity to the list of entities without the resource reference
            entitiesWithoutReference.add(entity);
        }

        //the resource reference will be added to each entity without it
        for (Iterator it = entitiesWithoutReference.iterator(); it.hasNext(); ) {
            ResourceRef newRR = new ResourceRef();
            newRR.setResRefName(resRefName);
            newRR.setJndiName(jndiName);
            Entity entity = (Entity)it.next();
            entity.addResourceRef(newRR);
        }

    }
    
    /**
     * Add a reference to the given resource to the message-driven beans if it does not exist yet.
     *
     * @param modifiedJboss Jboss graph instance being modified
     * @param resRefName resource reference name
     * @param beans the bean names (ejb-name) mapped to the message destinations (message-destination-link)
     * which might need to add resource reference specified by resRefName
     * @param jndiName JNDI name of the resource
     * 
     * @deprecated
     */
    @Deprecated
    static void modifyMsgDrv(Jboss modifiedJboss, String resRefName, Map beans, String jndiName) {

        assert(beans.size() > 0);

        if (modifiedJboss.getEnterpriseBeans() == null)
            modifiedJboss.setEnterpriseBeans(new EnterpriseBeans());

        addMsgDrvResReference(modifiedJboss, resRefName, beans, jndiName);
    }
    
    /**
     * Add a new resource reference to the message-driven beans without it.
     * 
     * @param modifiedJboss Jboss instance being modified
     * @param resRefName resource reference name
     * @param beans the bean names (ejb-name) mapped to the message destinations (message-destination-link)
     * which might need to add resource reference specified by resRefName
     * @param jndiName JNDI name of the resource
     * 
     * @deprecated
     */
    @Deprecated
    private static void addMsgDrvResReference(Jboss modifiedJboss, String resRefName, Map beans, String jndiName) {

        List/*<Entity>*/ msgdrvsWithoutReference = new LinkedList();

        EnterpriseBeans eb = modifiedJboss.getEnterpriseBeans();

        MessageDriven[] msgDrivens = eb.getMessageDriven();
        for (int i = 0; i < msgDrivens.length; i++) {
            String ejbName = msgDrivens[i].getEjbName();
            if (beans.containsKey(ejbName)) { // msgdrv found -> check whether it has the resource-ref
                beans.remove(ejbName);        // we don't care about it anymore
                ResourceRef[] resourceRefs = msgDrivens[i].getResourceRef();
                int j = 0;
                for ( ; j < resourceRefs.length; j++) {
                    String rrn = resourceRefs[j].getResRefName();
                    if (resRefName.equals(rrn))
                        break; // resource-ref found, continuing with the next mdb
                }
                if (j == resourceRefs.length) // resource-ref not found
                    msgdrvsWithoutReference.add(msgDrivens[i]);
            }
        }

        //no message-driven tag yet (msgDrivens.length == 0) or 
        //there are MDBs in beans map which were not found among the existing ones (those were not removed)
        for (Iterator it = beans.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            MessageDriven mdb = new MessageDriven();
            mdb.setEjbName((String) entry.getKey());
            mdb.setDestinationJndiName((String) entry.getValue());

            //add the new mdb to enterprise-beans
            eb.addMessageDriven(mdb);

            //add the new mdb to the list of mdbs without the resource reference
            msgdrvsWithoutReference.add(mdb);
        }

        //the resource reference will be added to each mdb without it
        for (Iterator it = msgdrvsWithoutReference.iterator(); it.hasNext(); ) {
            ResourceRef newRR = new ResourceRef();
            newRR.setResRefName(resRefName);
            newRR.setJndiName(jndiName);
            MessageDriven mdb = (MessageDriven)it.next();
            mdb.addResourceRef(newRR);
        }

    }

    /**
     * Add a reference to the given resource to the message-driven bean if it does not exist yet.
     *
     * @param modifiedJboss Jboss graph instance being modified
     * @param resRefName resource reference name
     * @param mdbName the MDB (ejb-name) which might need to add resource reference specified by resRefName
     * @param jndiName JNDI name of the resource
     */
    static void modifyMsgDrv(Jboss modifiedJboss, String resRefName, String mdbName, String jndiName) {

        if (modifiedJboss.getEnterpriseBeans() == null)
            modifiedJboss.setEnterpriseBeans(new EnterpriseBeans());

        addMsgDrvResReference(modifiedJboss, resRefName, mdbName, jndiName);
    }
    
    private static void addMsgDrvResReference(Jboss modifiedJboss, String resRefName, String mdbName, String jndiName) {

        EnterpriseBeans eb = modifiedJboss.getEnterpriseBeans();

        for (MessageDriven mdb : eb.getMessageDriven()) {
            String ejbName = mdb.getEjbName();
            if (mdbName.equals(ejbName)) { // msgdrv found -> check whether it has the resource-ref
                ResourceRef[] resourceRefs = mdb.getResourceRef();
                int j = 0;
                for ( ; j < resourceRefs.length; j++) {
                    String rrn = resourceRefs[j].getResRefName();
                    if (resRefName.equals(rrn))
                        return; // resource-ref found
                }
                if (j == resourceRefs.length) {// resource-ref not found
                    ResourceRef newRR = new ResourceRef();
                    newRR.setResRefName(resRefName);
                    newRR.setJndiName(jndiName);
                    mdb.addResourceRef(newRR);
                }
            }
        }
    }
    
}
