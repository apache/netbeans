/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
