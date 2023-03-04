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

import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class JdbcChildren extends WLNodeChildren {

    private final JdbcChildrenFactory.Retriever retriever;

    private final JdbcChildrenFactory.UnregisterFactory unregisterFactory;

    JdbcChildren(Lookup lookup) {
        this.retriever = new JdbcRetriever(lookup);
        this.unregisterFactory = new JdbcRetriever.JdbcUnregisterFactory();

        setKeys(new Object[]{
                createJDBCResourcesNode(lookup),
                createJDBCPoolsNode(lookup)});
    }


    private Node createJDBCPoolsNode(Lookup lookup) {
        JdbcChildrenFactory factory = new JdbcChildrenFactory(
                JdbcChildrenFactory.JdbcNodeTypes.POOL, retriever, unregisterFactory, lookup);
        return new ResourceNode(factory, ResourceNodeType.JDBC_POOL,
                NbBundle.getMessage(JdbcChildren.class, "LBL_JDBCPools"));
    }

    private Node createJDBCResourcesNode(Lookup lookup) {
        JdbcChildrenFactory factory = new JdbcChildrenFactory(
                JdbcChildrenFactory.JdbcNodeTypes.RESOURCES, retriever, unregisterFactory, lookup);
        return new ResourceNode(factory, ResourceNodeType.JDBC_RESOURCES,
                NbBundle.getMessage(JdbcChildren.class, "LBL_JDBCResources"));
    }

    @Override
    protected Node[] createNodes(Object key) {
        retriever.clean();
        return super.createNodes(key);
    }
}
