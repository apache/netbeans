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
package org.netbeans.modules.docker.ui.node;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.DestroyableNodesFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Petr Hejl
 */
public abstract class NodeClosingFactory<T> extends DestroyableNodesFactory<T> {

    private static final Logger LOGGER = Logger.getLogger(NodeClosingFactory.class.getName());

    @Override
    protected void destroyNodes(Node[] arr) {
        for (Node n : arr) {
            for (Closeable c : n.getLookup().lookupAll(Closeable.class)) {
                try {
                    LOGGER.log(Level.FINE, "Closing {0}", c.getClass().getName());
                    c.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        }
    }
}
