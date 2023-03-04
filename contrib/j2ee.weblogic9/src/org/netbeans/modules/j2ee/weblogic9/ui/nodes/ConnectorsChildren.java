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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.netbeans.modules.j2ee.weblogic9.WLConnectionSupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class ConnectorsChildren extends WLNodeChildren<ResourceNode> {
    
    private static final Logger LOGGER = Logger.getLogger(ConnectorsChildren.class.getName());
    
    ConnectorsChildren( Lookup lookup ){
        ResourceNode resource = new ResourceNode( new ConnectorResourceChildFactory(lookup),
                ResourceNodeType.CONNECTOR_RESOURCES, NbBundle.getMessage(
                        ConnectorsChildren.class, "LBL_ConnectorResource") );   // NOI18N
        ResourceNode pool = new ResourceNode( new ConnectorPoolChildFactory(lookup),
                ResourceNodeType.CONNECTOR_RESOURCES, NbBundle.getMessage(
                        ConnectorsChildren.class, "LBL_ConnectorConnectionPool") );   // NOI18N
        ResourceNode adminObjects = new ResourceNode( new AdminObjectChildFactory(lookup),
                ResourceNodeType.CONNECTOR_RESOURCES, NbBundle.getMessage(
                        ConnectorsChildren.class, "LBL_ConnectorAdminObject") );   // NOI18N
                
        setKeys( new ResourceNode[] { resource, pool ,adminObjects} );
    }
    
    /* (non-Javadoc)
     * @see org.openide.nodes.Children.Keys#createNodes(java.lang.Object)
     */
    @Override
    protected Node[] createNodes( ResourceNode key ) {
        return new Node[] { key };
    }

    static class ConnectorResourceChildFactory extends ChildFactory<ResourceNode> 
        implements RefreshModulesCookie
    {
        ConnectorResourceChildFactory(Lookup lookup ){
            this.lookup = lookup;
        }

        @Override
        public void refresh() {
            refresh(false);            
        }

        @Override
        protected boolean createKeys( List<ResourceNode> children ) {
            WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

            WLConnectionSupport support = manager.getConnectionSupport();
            try {
                List<String> jndiNames = support
                        .executeAction(new WLConnectionSupport.
                                JMXRuntimeAction<List<String>>()
                        {

                            @Override
                            public List<String> call( MBeanServerConnection con, ObjectName service )
                                    throws Exception
                            {

                                ObjectName objectName = (ObjectName) con
                                        .getAttribute(service,
                                                "DomainConfiguration"); // NOI18N
                                ObjectName objectNames[] = (ObjectName[]) con
                                        .getAttribute(objectName,
                                                "SystemResources"); // NOI18N

                                List<String> result = new LinkedList<String>();
                                for (ObjectName resource : objectNames) {
                                    String type = con.getAttribute(resource,
                                            "Type").toString();// NOI18N
                                    if ("JMSSystemResource".equals(type)) { // NOI18N
                                        ObjectName jmsResource = (ObjectName)con.getAttribute( 
                                                resource, "JMSResource");   // NOI18N
                                        ObjectName factories[] = (ObjectName[])
                                            con.getAttribute( jmsResource, 
                                                "ConnectionFactories");     // NOI18N
                                        for (ObjectName factory : factories) {
                                            String jndiName = con.getAttribute(factory, 
                                                    "JNDIName").toString(); // NOI18N
                                            result.add( jndiName );
                                        }
                                    }
                                }
                                return result;
                            }

                        });
                for (String jndiName : jndiNames) {
                    children.add( new ResourceNode( Children.LEAF, 
                            ResourceNodeType.CONNECTORS, jndiName));
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
                    
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createNodeForKey(java.lang.Object)
         */
        @Override
        protected Node createNodeForKey( ResourceNode key ) {
            return key;
        }
        
        private Lookup lookup;
    }
    
    static class ConnectorPoolChildFactory extends
            ChildFactory<ResourceNode> implements RefreshModulesCookie
    {
        ConnectorPoolChildFactory(Lookup lookup){
            this.lookup = lookup;
        }

        @Override
        public void refresh() {
            refresh(false);
        }

        @Override
        protected boolean createKeys( List<ResourceNode> children ) {
            WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

            WLConnectionSupport support = manager.getConnectionSupport();
            try {
                List<String> jndiNames = support
                        .executeAction(new WLConnectionSupport.
                                JMXRuntimeAction<List<String>>()
                        {

                            @Override
                            public List<String> call( MBeanServerConnection con, ObjectName service )
                                    throws Exception
                            {
                                ObjectName[] adminServers = (ObjectName[]) con
                                        .getAttribute(service, "ServerRuntimes");  // NOI18N
                                List<String> result = new LinkedList<String>();
                                for (ObjectName adminServer : adminServers) {
                                    ObjectName connectorRuntime = (ObjectName)con.getAttribute( adminServer, 
                                        "ConnectorServiceRuntime");                 // NOI18N
                                        ObjectName[] activeRas = (ObjectName[])con.getAttribute( connectorRuntime, 
                                            "ActiveRAs");
                                        addPool(activeRas, result, con);
                                
                                        ObjectName[] inactiveRas = (ObjectName[])con.getAttribute( connectorRuntime, 
                                            "InactiveRAs");
                                        addPool(inactiveRas, result, con);
                                }

                                return result;
                            }

                            private void addPool( ObjectName[] resourceAdapters, 
                                    List<String> names, MBeanServerConnection connection) 
                                throws AttributeNotFoundException, InstanceNotFoundException, 
                                MBeanException, ReflectionException, IOException
                            {
                                for (ObjectName resourceAdapter : resourceAdapters) {
                                    ObjectName[] pools = (ObjectName[])connection.getAttribute( 
                                            resourceAdapter, "ConnectionPools");    // NOI18N
                                    for (ObjectName pool : pools) {
                                        String name = connection.getAttribute(pool, 
                                                "Name").toString();
                                        names.add( name );
                                    }
                                }
                            }
                        }
                        
                        );
                for (String jndiName : jndiNames) {
                    children.add( new ResourceNode( Children.LEAF, 
                            ResourceNodeType.CONNECTORS, jndiName));
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
                    
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createNodeForKey(java.lang.Object)
         */
        @Override
        protected Node createNodeForKey( ResourceNode key ) {
            return key;
        }
        
        private Lookup lookup;

    }
    
    static class AdminObjectChildFactory extends ChildFactory<ResourceNode>
            implements RefreshModulesCookie
    {
        AdminObjectChildFactory(Lookup lookup){
            this.lookup = lookup;
        }

        @Override
        public void refresh() {
            refresh(false);
        }

        @Override
        protected boolean createKeys( List<ResourceNode> children ) {
            WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

            WLConnectionSupport support = manager.getConnectionSupport();
            try {
                List<String> jndiNames = support
                        .executeAction(new WLConnectionSupport.JMXRuntimeAction<List<String>>()
                        {

                            @Override
                            public List<String> call( MBeanServerConnection con, ObjectName service )
                                    throws Exception
                            {

                                ObjectName objectName = (ObjectName) con
                                        .getAttribute(service,
                                                "DomainConfiguration"); // NOI18N
                                ObjectName objectNames[] = (ObjectName[]) con
                                        .getAttribute(objectName,
                                                "SystemResources"); // NOI18N

                                List<String> result = new LinkedList<String>();
                                for (ObjectName resource : objectNames) {
                                    String type = con.getAttribute(resource,
                                            "Type").toString();// NOI18N
                                    if ("JMSSystemResource".equals(type)) { // NOI18N
                                        ObjectName jmsResource = (ObjectName) con
                                                .getAttribute(resource,
                                                        "JMSResource"); // NOI18N
                                        ObjectName queues[] = (ObjectName[]) con
                                                .getAttribute(jmsResource,
                                                        "Queues"); // NOI18N
                                        for (ObjectName queue : queues) {
                                            String jndiName = con.getAttribute(
                                                    queue, "JNDIName")
                                                    .toString(); // NOI18N
                                            result.add(jndiName);
                                        }
                                        ObjectName topics[] = (ObjectName[]) con
                                                .getAttribute(jmsResource,
                                                        "Topics"); // NOI18N
                                        for (ObjectName topic : topics) {
                                            String jndiName = con.getAttribute(
                                                    topic, "JNDIName")
                                                    .toString(); // NOI18N
                                            result.add(jndiName);
                                        }
                                    }
                                }
                                return result;
                            }

                        });
                for (String jndiName : jndiNames) {
                    children.add( new ResourceNode( Children.LEAF, 
                            ResourceNodeType.CONNECTORS, jndiName));
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
                    
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createNodeForKey(java.lang.Object)
         */
        @Override
        protected Node createNodeForKey( ResourceNode key ) {
            return key;
        }

        private Lookup lookup;
    }
}
