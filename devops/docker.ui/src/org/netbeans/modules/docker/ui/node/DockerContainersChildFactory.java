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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerEvent;
import org.netbeans.modules.docker.api.DockerAction;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class DockerContainersChildFactory extends NodeClosingFactory<StatefulDockerContainer> implements Refreshable, Closeable {

    private static final Logger LOGGER = Logger.getLogger(DockerContainersChildFactory.class.getName());

    private static final Comparator<DockerContainer> COMPARATOR = new Comparator<DockerContainer>() {

        @Override
        public int compare(DockerContainer o1, DockerContainer o2) {
            return o1.getImage().compareTo(o2.getImage());
        }
    };

    private static final Set<DockerEvent.Status> CHANGE_EVENTS = new HashSet<>();

    static {
        // rename is here because it may reorder nodes
        Collections.addAll(CHANGE_EVENTS, DockerEvent.Status.COPY, DockerEvent.Status.CREATE,
                DockerEvent.Status.DESTROY, DockerEvent.Status.RENAME);
    }

    private final Map<DockerContainer, WeakReference<StatefulDockerContainer>> cache = new WeakHashMap<>();

    private final RequestProcessor requestProcessor = new RequestProcessor(DockerContainersChildFactory.class);

    private final DockerInstance instance;

    private final RequestProcessor.Task refreshTask;

    private final DockerEvent.Listener listener;

    public DockerContainersChildFactory(DockerInstance instance) {
        this.instance = instance;
        this.refreshTask = requestProcessor.create(new Runnable() {
            @Override
            public void run() {
                LOGGER.log(Level.FINE, "Refreshing containers");
                refresh();
            }
        });
        this.listener = new DockerEvent.Listener() {
            @Override
            public void onEvent(DockerEvent event) {
                if (CHANGE_EVENTS.contains(event.getStatus())) {
                    refreshTask.schedule(200);
                }
            }
        };
        instance.addContainerListener(listener);
    }

    @Override
    protected Node createNodeForKey(StatefulDockerContainer key) {
        return new DockerContainerNode(key);
    }

    @Override
    protected boolean createKeys(List<StatefulDockerContainer> toPopulate) {
        DockerAction facade = new DockerAction(instance);
        List<DockerContainer> containers = new ArrayList<>(facade.getContainers());
        containers.sort(COMPARATOR);
        synchronized (cache) {
            List<StatefulDockerContainer> fresh = new ArrayList<>(containers.size());
            for (DockerContainer c : containers) {
                StatefulDockerContainer cached = null;
                WeakReference<StatefulDockerContainer> ref = cache.get(c);
                if (ref != null) {
                    cached = ref.get();
                }
                if (cached == null) {
                    cached = new StatefulDockerContainer(c);
                    cache.put(c, new WeakReference<>(cached));
                } else {
                    cached.attach();
                    cached.refresh();
                }
                fresh.add(cached);
            }
            // we add it all at once to prevent remove-add cycle for existing
            // containers
            toPopulate.addAll(fresh);
        }
        return true;
    }

    @Override
    public final void refresh() {
        refresh(false);
    }

    @Override
    public void close() {
        instance.removeContainerListener(listener);
        synchronized (cache) {
            for (WeakReference<StatefulDockerContainer> r : cache.values()) {
                StatefulDockerContainer c  = r.get();
                if (c != null) {
                    c.close();
                }
            }
            cache.clear();
        }
    }

}
