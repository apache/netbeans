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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerInstance;
import org.openide.nodes.Node;

/**
 *
 * @author Petr Hejl
 */
public class DockerInstanceChildFactory extends NodeClosingFactory<Boolean> implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(DockerInstanceChildFactory.class.getName());

    private final StatefulDockerInstance instance;

    private final Set<Node> current = new HashSet<>();

    public DockerInstanceChildFactory(StatefulDockerInstance instance) {
        this.instance = instance;

        instance.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                refresh(false);
            }
        });
        instance.refresh();
    }

    @Override
    protected Node[] createNodesForKey(Boolean key) {
        Node[] ret;
        if (key) {
            DockerInstance dockerInstance = instance.getInstance();
            DockerImagesChildFactory factoryRepo = new DockerImagesChildFactory(dockerInstance);
            DockerContainersChildFactory factoryCont = new DockerContainersChildFactory(dockerInstance);
            ret = new Node[]{new DockerImagesNode(dockerInstance, factoryRepo),
                new DockerContainersNode(dockerInstance, factoryCont)};
        } else {
            ret = new Node[] {};
        }
        synchronized (current) {
            current.clear();
            Collections.addAll(current, ret);
        }
        return ret;
    }

    @Override
    protected boolean createKeys(List<Boolean> toPopulate) {
        toPopulate.add(instance.isAvailable());
        return true;
    }

    @Override
    public void close() {
        Set<Node> nodes;
        synchronized (current) {
            nodes = new HashSet<>(current);
        }
        for (Node n : nodes) {
            for (Closeable c : n.getLookup().lookupAll(Closeable.class)) {
                try {
                    c.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }
    }
}
