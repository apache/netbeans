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
class TuxedoChildren extends WLNodeChildren<ResourceNode> {
    
    private static final Logger LOGGER = Logger.getLogger(TuxedoChildren.class.getName());
    
    TuxedoChildren( Lookup lookup){
        this.lookup = lookup;
        ResourceNode wtcServers = new ResourceNode( new WTCServersChildrenFactory(),
               ResourceNodeType.WTC_SERVER, NbBundle.getMessage(
                       TuxedoChildren.class, "LBL_WtcServers") );
        ResourceNode pools = new ResourceNode( new JoltPoolsChildrenFactory(),
                ResourceNodeType.WTC_SERVER
                , NbBundle.getMessage(TuxedoChildren.class, "LBL_JoltConnectionPools"));
        setKeys( new ResourceNode[]{ wtcServers , pools });
    }

    /* (non-Javadoc)
     * @see org.openide.nodes.Children.Keys#createNodes(java.lang.Object)
     */
    @Override
    protected Node[] createNodes( ResourceNode key ) {
        return new Node[] { key };
    }
    
    private abstract class TuxedoChildrenFactory extends ChildFactory<ResourceNode> 
        implements RefreshModulesCookie
    {
     
        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie#refresh()
         */
        @Override
        public void refresh() {
            refresh( false );
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
         */
        @Override
        protected boolean createKeys( final List<ResourceNode> keys ) {
            WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

            WLConnectionSupport support = manager.getConnectionSupport();
            try {
            support.executeAction(new WLConnectionSupport.
                    JMXRuntimeAction<Void>()
            {

                @Override
                public Void call( MBeanServerConnection con ,
                        ObjectName service )
                        throws Exception
                {
                    ObjectName domainConfig = (ObjectName) con
                            .getAttribute(service,
                                    "DomainConfiguration"); // NOI18N
                    ObjectName deployments[] = (ObjectName[]) con
                        .getAttribute(domainConfig, "Deployments"); // NOI18N

                    for (ObjectName deployment : deployments) {
                        String type = con.getAttribute(deployment, "Type").toString();// NOI18N
                        ResourceNode node = createNode( con, deployment, type );
                        if ( node != null ){
                            keys.add( node );
                        }
                    }
                    return null;
                }
            });
            }
            catch( Exception e ){
                LOGGER.log(Level.INFO, null, e);
            }
            return true;
        }
        
        /*
         * (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createNodeForKey(java.lang.Object)
         */
        @Override
        protected Node createNodeForKey( ResourceNode key ) {
            return key;
        }
        
        protected abstract ResourceNode createNode( MBeanServerConnection connection,
                ObjectName bean, String type ) throws AttributeNotFoundException, 
                InstanceNotFoundException, MBeanException, ReflectionException, 
                    IOException;
    }
    
    private class WTCServersChildrenFactory extends TuxedoChildrenFactory 
    {
        protected ResourceNode createNode(MBeanServerConnection connection,
                ObjectName bean,String type) throws AttributeNotFoundException, 
                InstanceNotFoundException, MBeanException, ReflectionException, 
                IOException
        {
            if ( "WTCServer".equals( type )){
                String name = (String)connection.getAttribute(bean, "Name"); // NOI18N
                // TODO : there should be a children for WTC server
                return new ResourceNode( Children.LEAF, ResourceNodeType.WTC_SERVER,
                        name );
            }
            else {
                return null;
            }
        }
    }
    
    private class JoltPoolsChildrenFactory extends TuxedoChildrenFactory
    {
        protected ResourceNode createNode(MBeanServerConnection connection,
                ObjectName bean,String type) throws AttributeNotFoundException, 
                InstanceNotFoundException, MBeanException, ReflectionException, 
                IOException
        {
            if ( "JoltConnectionPool".equals( type )){
                String name = (String)connection.getAttribute(bean, "Name"); // NOI18N
                return new ResourceNode( Children.LEAF, ResourceNodeType.WTC_SERVER,
                        name );
            }
            else {
                return null;
            }
        }
    }
    
    private Lookup lookup;

}
