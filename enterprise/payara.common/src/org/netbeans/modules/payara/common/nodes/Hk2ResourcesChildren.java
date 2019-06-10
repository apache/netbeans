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

package org.netbeans.modules.payara.common.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.common.CommonServerSupport;
import org.netbeans.modules.payara.common.ui.AdminObjectCustomizer;
import org.netbeans.modules.payara.common.ui.ConnectionPoolCustomizer;
import org.netbeans.modules.payara.common.ui.ConnectorConnectionPoolCustomizer;
import org.netbeans.modules.payara.common.ui.ConnectorCustomizer;
import org.netbeans.modules.payara.common.ui.JavaMailCustomizer;
import org.netbeans.modules.payara.common.ui.JdbcResourceCustomizer;
import org.netbeans.modules.payara.spi.Decorator;
import org.netbeans.modules.payara.spi.ResourceDecorator;
import org.netbeans.modules.payara.spi.ResourceDesc;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.payara.spi.PayaraModule;


/**
 *
 * @author Peter Williams
 */
public class Hk2ResourcesChildren extends Children.Keys<Object> implements Refreshable {

    private Lookup lookup;
    private String type;
    private final static Node WAIT_NODE = Hk2ItemNode.createWaitNode();

    Hk2ResourcesChildren(Lookup lookup, String type) {
        this.lookup = lookup;
        this.type = type;
    }

    @Override
    public void updateKeys() {
        List<AbstractNode> keys = Collections.synchronizedList(new ArrayList<AbstractNode>());
        String[] childTypes = NodeTypes.getChildTypes(type);
        if ((childTypes != null)) {
            if (childTypes.length > 1) {
                for (int i = 0; i < childTypes.length; i++) {
                    String childtype = childTypes[i];
                    Class customizer = getCustomizer(childtype);
                    keys.add(new Hk2ItemNode(lookup,
                            new Hk2Resources(lookup, childtype, customizer),
                            NbBundle.getMessage(Hk2ResourceContainers.class, "LBL_" + childtype), //TODO
                            Hk2ItemNode.REFRESHABLE_FOLDER));
                }
            } else {
                String childtype = childTypes[0];
                CommonServerSupport commonSupport = lookup.lookup(
                        CommonServerSupport.class);
                if (commonSupport != null) {
                    try {
                        Decorator decorator = DecoratorManager.findDecorator(childtype, null, true);
                        List<ResourceDesc> reslourcesList
                                = ResourceDesc.getResources(commonSupport.getInstance(), childtype);
                        for (ResourceDesc resource : reslourcesList) {
                            keys.add(new Hk2ResourceNode(lookup, resource, (ResourceDecorator) decorator, getCustomizer(childtype)));
                        }
                    } catch (Exception ex) {
                        Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }
        setKeys(keys);

    }

    @Override
    protected void addNotify() {
        updateKeys();
    }

    @Override
    protected void removeNotify() {
        setKeys((Set<? extends Object>) java.util.Collections.EMPTY_SET);
    }

    @Override
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof Hk2ItemNode){
            return new Node [] { (Hk2ItemNode) key };
        }

        if (key instanceof String && key.equals(WAIT_NODE)){
            return new Node [] { WAIT_NODE };
        }

        return null;
    }

    class Hk2Resources extends Children.Keys<Object> implements Refreshable {

        private Lookup lookup;
        private String type;
        private Class customizer;
        private final Node WAIT_NODE = Hk2ItemNode.createWaitNode();

        Hk2Resources(Lookup lookup, String type, Class customizer) {
            this.lookup = lookup;
            this.type = type;
            this.customizer = customizer;
        }

        @Override
        public void updateKeys() {
        RequestProcessor t = new RequestProcessor("resource-child-updater");
        t.post(new Runnable() {

                List<AbstractNode> keys = Collections.synchronizedList(new ArrayList<AbstractNode>());

                @Override
                public void run() {
                    CommonServerSupport commonSupport = lookup.lookup(
                            CommonServerSupport.class);
                    if (commonSupport != null) {
                        try {
                            Decorator decorator = DecoratorManager.findDecorator(type, null,true);
                            if (decorator == null) {
                                if (type.equals(PayaraModule.JDBC_RESOURCE)) {
                                    decorator = Hk2ItemNode.JDBC_MANAGED_DATASOURCES;
                                } else if (type.equals(PayaraModule.JDBC_CONNECTION_POOL)) {
                                    decorator = Hk2ItemNode.CONNECTION_POOLS;
                                }
                            }
                            if (decorator != null) {
                                List<ResourceDesc> reslourcesList
                                        = ResourceDesc.getResources(commonSupport.getInstance(), type);
                                for (ResourceDesc resource : reslourcesList) {
                                    keys.add(new Hk2ResourceNode(lookup, resource, (ResourceDecorator) decorator, customizer));
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }

                        setKeys(keys);
                    }
                }
            }, 0);
        }

        @Override
        protected void addNotify() {
            updateKeys();
        }

        @Override
        protected void removeNotify() {
            setKeys((Set<? extends Object>) java.util.Collections.EMPTY_SET);
        }

        @Override
        protected org.openide.nodes.Node[] createNodes(Object key) {
            if (key instanceof Hk2ItemNode) {
                return new Node[]{(Hk2ItemNode) key};
            }

            if (key instanceof String && key.equals(WAIT_NODE)) {
                return new Node[]{WAIT_NODE};
            }

            return null;
        }
    }

    private Class getCustomizer(String type) {
        Class customizer = null;
        if (type.equals(PayaraModule.JDBC_CONNECTION_POOL)) {
            customizer = ConnectionPoolCustomizer.class;
        } else if (type.equals(PayaraModule.JDBC_RESOURCE)) {
            customizer = JdbcResourceCustomizer.class;
        } else if (type.equals(PayaraModule.CONN_RESOURCE)) {
            customizer = ConnectorCustomizer.class;
        } else if (type.equals(PayaraModule.CONN_CONNECTION_POOL)) {
            customizer = ConnectorConnectionPoolCustomizer.class;
        } else if (type.equals(PayaraModule.ADMINOBJECT_RESOURCE)) {
            customizer = AdminObjectCustomizer.class;
        } else if (type.equals(PayaraModule.JAVAMAIL_RESOURCE)) {
            customizer = JavaMailCustomizer.class;
        }
        return customizer;
    }
}
