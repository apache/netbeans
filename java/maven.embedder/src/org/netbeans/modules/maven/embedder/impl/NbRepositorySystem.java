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
package org.netbeans.modules.maven.embedder.impl;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.util.graph.visitor.CloningDependencyVisitor;
import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.DefaultRepositorySystemSession;

/**
 * another piece of experimental code meant to replace the dependency-tree codebase
 * and our dependency on it. the MavenExecutionResult contains DependencyResolutionResult
 * with a DependencyNode, resolved tree of dependencies. This code persists also the raw
 * non transformed version of it. (None are most likely exact match of dependency-tree classes
 * but together and with use of some DependencyGraphTransformers we could get there.
 * @author mkleint
 */
public class NbRepositorySystem extends DefaultRepositorySystem {

    @Override
    public CollectResult collectDependencies(RepositorySystemSession session, CollectRequest request) throws DependencyCollectionException {
        DefaultRepositorySystemSession cloned = new DefaultRepositorySystemSession(session);
        DependencyGraphTransformer transformer = session.getDependencyGraphTransformer();
        //need to reset the transformer to prevent the transformation to happen and to it below separately.
        cloned.setDependencyGraphTransformer(null);
        CollectResult res = super.collectDependencies(cloned, request);
        CloningDependencyVisitor vis = new CloningDependencyVisitor();
        res.getRoot().accept(vis);

        //this part copied from DefaultDependencyCollector
        try {
            DefaultDependencyGraphTransformationContext context =
                    new DefaultDependencyGraphTransformationContext(session);
            res.setRoot(transformer.transformGraph(res.getRoot(), context));
        } catch (RepositoryException e) {
            res.addException(e);
        }

        if (!res.getExceptions().isEmpty()) {
            throw new DependencyCollectionException(res);
        }
        res.getRoot().setData("NB_TEST", vis.getRootNode());
        return res;
    }

    public static DependencyNode getNonResolvedNode(DependencyNode node) {
        return (DependencyNode) node.getData().get("NB_TEST");
    }

    private static class DefaultDependencyGraphTransformationContext
            implements DependencyGraphTransformationContext {

        private final RepositorySystemSession session;
        private final Map<Object, Object> map;

        public DefaultDependencyGraphTransformationContext(RepositorySystemSession session) {
            this.session = session;
            this.map = new HashMap<Object, Object>();
        }

        @Override
        public RepositorySystemSession getSession() {
            return session;
        }

        @Override
        public Object get(Object key) {
            return map.get(key);
        }

        @Override
        public Object put(Object key, Object value) {
            return map.put(key, value);
        }

        @Override
        public String toString() {
            return String.valueOf(map);
        }
    }
}
