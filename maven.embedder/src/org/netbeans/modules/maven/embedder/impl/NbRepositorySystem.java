/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
