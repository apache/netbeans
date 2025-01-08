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

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerSupport;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Petr Hejl
 */
public class DockerChildFactory extends NodeClosingFactory<StatefulDockerInstance> implements ChangeListener {

    private static final RequestProcessor REFRESH_PROCESSOR = new RequestProcessor("Docker node update/refresh", 5);

    private final DockerSupport registry;

    public DockerChildFactory(DockerSupport registry) {
        this.registry = registry;
    }

    public void init() {
        REFRESH_PROCESSOR.post(new Runnable() {

            @Override
            public void run() {
                synchronized (DockerChildFactory.this) {
                    registry.addChangeListener(
                            WeakListeners.create(ChangeListener.class, DockerChildFactory.this, registry));
                    updateState(new ChangeEvent(registry));
                }
            }
        });
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        REFRESH_PROCESSOR.post(new Runnable() {
            public void run() {
                updateState(e);
            }
        });
    }

    private synchronized void updateState(final ChangeEvent e) {
        refresh();
    }

    protected final void refresh() {
        refresh(false);
    }

    @Override
    protected Node createNodeForKey(StatefulDockerInstance key) {
        return new DockerInstanceNode(key, new DockerInstanceChildFactory(key));
    }

    @Override
    protected boolean createKeys(List<StatefulDockerInstance> toPopulate) {
        List<? extends DockerInstance> fresh = new ArrayList<>(registry.getInstances());
        fresh.sort(UiUtils.getInstanceComparator());
        for (DockerInstance i : fresh) {
            toPopulate.add(new StatefulDockerInstance(i));
        }
        return true;
    }

}
