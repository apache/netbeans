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
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
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
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * It describes children nodes of the enterprise application node.
 *
 * @author Michal Mocnak
 */
public class JBEarModulesChildren extends Children.Keys {

    private static final Logger LOGGER = Logger.getLogger(JBEarApplicationsChildren.class.getName());

    private Lookup lookup;
    private String j2eeAppName;
    
    public JBEarModulesChildren(Lookup lookup, String j2eeAppName) {
        this.lookup = lookup;
        this.j2eeAppName = j2eeAppName;
    }
    
    public void updateKeys(){
        setKeys(new Object[] {Util.WAIT_NODE});
        
        RequestProcessor.getDefault().post(new Runnable() {
            Vector keys = new Vector();
            JBDeploymentManager dm = (JBDeploymentManager)lookup.lookup(JBDeploymentManager.class);
            
            public void run() {
                try {
                    dm.invokeRemoteAction(new JBRemoteAction<Void>() {

                        @Override
                        public Void action(MBeanServerConnection connection, JBoss5ProfileServiceProxy profileService) throws Exception {
                            // Query to the jboss4 server
                            ObjectName searchPattern = new ObjectName("jboss.management.local:J2EEApplication="+j2eeAppName+",*");
                            Method method = connection.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class});
                            method = Util.fixJava4071957(method);
                            Set managedObj = (Set) method.invoke(connection, new Object[] {searchPattern, null});

                            Iterator it = managedObj.iterator();

                            // Query results processing
                            while(it.hasNext()) {
                                try {
                                    ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                                    String name = elem.getKeyProperty("name");

                                    if(elem.getKeyProperty("j2eeType").equals("EJBModule"))
                                        keys.add(new JBEjbModuleNode(name, lookup));
                                    else if(elem.getKeyProperty("j2eeType").equals("WebModule")) {
                                        String url = "http://"+dm.getHost()+":"+dm.getPort();
                                        String descr = (String)Util.getMBeanParameter(connection, "jbossWebDeploymentDescriptor", elem.getCanonicalName());
                                        String context = Util.getWebContextRoot(descr, name);
                                        keys.add(new JBWebModuleNode(name, lookup, (context == null) ? null : url+context));
                                    }
                                } catch (Exception ex) {
                                    LOGGER.log(Level.INFO, null, ex);
                                }
                            }
                            return null;
                        }

                    });
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
                
                setKeys(keys);
            }
        }, 0);
        
    }
    
    protected void addNotify() {
        updateKeys();
    }
    
    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }
    
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof JBEjbModuleNode){
            return new Node[]{(JBEjbModuleNode)key};
        }
        
        if (key instanceof JBWebModuleNode){
            return new Node[]{(JBWebModuleNode)key};
        }
        
        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }
        
        return null;
    }
}
