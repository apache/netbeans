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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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


/**
 * @author ads
 *
 */
public class LibrariesChildrenFactory extends ChildFactory<ResourceNode>
        implements RefreshModulesCookie {

    private static final Logger LOGGER = Logger.getLogger(LibrariesChildrenFactory.class.getName());

    private final Lookup lookup;

    LibrariesChildrenFactory(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void refresh() {
        refresh(false);
    }

    @Override
    protected boolean createKeys(List<ResourceNode> nodes) {
        WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);

        WLConnectionSupport support = manager.getConnectionSupport();
        try {
            Map<String,String> libraries = support.executeAction(
                    new WLConnectionSupport.JMXRuntimeAction<Map<String,String>>() {

                @Override
                public Map<String,String> call( MBeanServerConnection con,
                        ObjectName service) throws Exception {
                    Map<String,String> map = new HashMap<String, String>();

                    ObjectName domainConfig = (ObjectName) con
                            .getAttribute(service,
                                    "DomainConfiguration"); // NOI18N

                    fillLibraries( con, domainConfig,
                            "Libraries", map);              // NOI18N
                    fillLibraries( con, domainConfig,
                            "InternalLibraries", map);      // NOI18N
                    fillLibraries( con, domainConfig,
                            "InternalAppDeployments", map); // NOI18N
                    fillLibraries( con, domainConfig,
                            "AppDeployments", map);         // NOI18N
                    return map;
                }
            });
            for (Entry<String, String > entry : libraries.entrySet()) {
                String path = entry.getKey();
                String name = entry.getValue();
                nodes.add(new ResourceNode(Children.LEAF, ResourceNodeType.LIBRARY,
                        name, path ));
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return true;
    }

    private void fillLibraries(MBeanServerConnection connection, ObjectName domainConfig,
            String attrName, Map<String,String> libraries)
        throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {

        ObjectName beans[] = (ObjectName[]) connection.getAttribute(domainConfig, attrName);
        for (ObjectName bean : beans) {
            String type = connection.getAttribute(bean, "Type").toString(); // NOI18N
            if ("Library".equals( type )) { // NOI18N
                String name = connection.getAttribute(bean, "Name").toString(); // NOI18N
                Object path = connection.getAttribute(bean, "AbsoluteInstallDir"); // NOI18N
                if (path == null) {
                    path = connection.getAttribute(bean, "AbsoluteSourcePath");      // NOI18N
                }
                if (path == null) {
                    path = name ;
                }
                libraries.put(path.toString(), name);
            }
        }
    }

    @Override
    protected Node createNodeForKey( ResourceNode key ) {
        return key;
    }

}
