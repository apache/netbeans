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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.JBRemoteAction;
import org.netbeans.modules.j2ee.jboss4.JBoss5ProfileServiceProxy;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the Web Module node.
 *
 * @author Michal Mocnak
 */
public class JBServletsChildren extends Children.Keys {

    private static Logger LOGGER = Logger.getLogger(JBServletsChildren.class.getName());

    private static final String WAIT_NODE = "wait_node"; //NOI18N
    
    private String name;
    private Lookup lookup;
    private final JBAbilitiesSupport abilitiesSupport;
    
    JBServletsChildren(String name, Lookup lookup) {
        this.lookup = lookup;
        this.name = name;
        this.abilitiesSupport = new JBAbilitiesSupport(lookup);
    }
    
    public void updateKeys(){
        setKeys(new Object[] {WAIT_NODE});
        RequestProcessor.getDefault().post(abilitiesSupport.isJB7x() ? new JB7ServletNodeUpdater() : new JBServletNodeUpdater(), 0);
    }

    class JBServletNodeUpdater implements Runnable {

        List keys = new ArrayList();

        @Override
        public void run() {
                try {
                    // Query to the jboss4 server
                    lookup.lookup(JBDeploymentManager.class).invokeRemoteAction(new JBRemoteAction<Void>() {

                        @Override
                        public Void action(MBeanServerConnection connection, JBoss5ProfileServiceProxy profileService) throws Exception {
                            ObjectName searchPattern = new ObjectName("jboss.management.local:WebModule="+name+",j2eeType=Servlet,*");
                            Method method = connection.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class});
                            method = Util.fixJava4071957(method);
                            Set managedObj = (Set) method.invoke(connection, new Object[] {searchPattern, null});

                            Iterator it = managedObj.iterator();

                            // Query results processing
                            while(it.hasNext()) {
                                ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                                String s = elem.getKeyProperty("name");
                                keys.add(new JBServletNode(s));
                            }
                            return null;
                        }

                    });
                 
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
                
                setKeys(keys);
            }
    }
    
    class JB7ServletNodeUpdater implements Runnable {

        List keys = new ArrayList();

        @Override
        public void run() {
            try {
                // Query to the jboss4 server
                lookup.lookup(JBDeploymentManager.class).invokeLocalAction(new Callable<Void>() {

                    @Override
                    public Void call() {
                        // TODO: add as7 logic here
                        return null;
                    }
                });

            } catch (Exception ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            setKeys(keys);
        }
    }
    
    protected void addNotify() {
        updateKeys();
    }
    
    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }
    
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof JBServletNode){
            return new Node[]{(JBServletNode)key};
        }
        
        if (key instanceof String && key.equals(WAIT_NODE)){
            return new Node[]{createWaitNode()};
        }
        
        return null;
    }
    
    /* Creates and returns the instance of the node
     * representing the status 'WAIT' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    private Node createWaitNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(JBApplicationsChildren.class, "LBL_WaitNode_DisplayName")); //NOI18N
        n.setIconBaseWithExtension("org/netbeans/modules/j2ee/jboss4/resources/wait.gif"); // NOI18N
        return n;
    }
}
