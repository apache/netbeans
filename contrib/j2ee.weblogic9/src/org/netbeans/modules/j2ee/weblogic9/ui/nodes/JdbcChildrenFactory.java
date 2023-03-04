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

import java.util.List;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UnregisterCookie;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class JdbcChildrenFactory extends ChildFactory<ResourceNode> implements RefreshModulesCookie {

    enum JdbcNodeTypes {
        RESOURCES,
        POOL;
    }

    private final JdbcNodeTypes type;

    private final Retriever retriever;

    private final UnregisterFactory unregisterFactory;

    private final Lookup lookup;

    public JdbcChildrenFactory(JdbcNodeTypes type, Retriever retriever,
            UnregisterFactory unregisterFactory, Lookup lookup) {
        this.type = type;
        this.retriever = retriever;
        this.unregisterFactory = unregisterFactory;
        this.lookup = lookup;
    }

    @Override
    public final void refresh() {
        retriever.clean();
        refresh(false);
    }

    @Override
    protected boolean createKeys(List<ResourceNode> children) {
        retriever.waitForCompletion();

        List<JDBCDataBean> jdbcDataBeans = retriever.get();
        if (jdbcDataBeans != null) {
            if (type == JdbcNodeTypes.POOL) {
                for (JDBCDataBean jdbcDataBean : jdbcDataBeans) {
                    String name = jdbcDataBean.getName();
                    if (jdbcDataBean.isApplication()) {
                        name = jdbcDataBean.getDeploymentName();
                    }
                    children.add(new ResourceNode(Children.LEAF,
                            ResourceNodeType.JDBC,jdbcDataBean.getName(),
                            unregisterFactory != null ?
                                unregisterFactory.createUnregisterForPool(name, this, lookup) : null));
                }
            } else if (type == JdbcNodeTypes.RESOURCES) {
                for (JDBCDataBean jdbcDataBean : jdbcDataBeans) {
                    String[] jndiNames = jdbcDataBean.getJndiNames();
                    boolean isApplication = jdbcDataBean.isApplication();
                    for (String name : jndiNames) {
                        // no "unregister" action if jdbc data source is deployed application
                        ResourceNode node = new ResourceNode(Children.LEAF,
                                ResourceNodeType.JDBC, name, isApplication ? null :
                                unregisterFactory != null ?
                                    unregisterFactory.createUnregisterForResource(name, this, lookup) : null);
                        children.add(node);
                    }
                }
            }
            return true;
        }
        retriever.retrieve();
        return false;
    }

    @Override
    protected Node createNodeForKey(ResourceNode key) {
        return key;
    }

    public static interface Retriever {

        List<JDBCDataBean> get();

        void retrieve();

        void clean();

        void waitForCompletion();

    }

    public static interface UnregisterFactory {

        UnregisterCookie createUnregisterForPool(
                String name, RefreshModulesCookie refresh, Lookup lookup);

        UnregisterCookie createUnregisterForResource(
                String name, RefreshModulesCookie refresh, Lookup lookup);

    }

    public static final class JDBCDataBean {

        private final String name;

        private final String jndiNames[];

        private final String deploymentName;

        public JDBCDataBean(String poolName, String[] jndiNames ){
            this(poolName, jndiNames, null);
        }

        public JDBCDataBean( String poolName , String[] jndiNames, String deploymentName){
            name = poolName;
            this.jndiNames = jndiNames.clone();
            this.deploymentName = deploymentName;
        }

        String getName(){
            return name;
        }

        String[] getJndiNames(){
            return jndiNames;
        }

        boolean isApplication(){
            return deploymentName != null;
        }

        String getDeploymentName(){
            return deploymentName;
        }
    }
}
