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

import org.netbeans.modules.docker.ui.pull.PullImageAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.ui.build2.BuildImageAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class DockerInstanceNode extends AbstractNode {

    private static final String DOCKER_INSTANCE_ICON = "org/netbeans/modules/docker/ui/resources/docker_instance.png"; // NOI18N

    private final StatefulDockerInstance instance;

    private final ChangeListener listener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            fireDisplayNameChange(null, null);
        }
    };

    public DockerInstanceNode(StatefulDockerInstance instance, DockerInstanceChildFactory factory) {
        super(Children.create(factory, true),
                Lookups.fixed(instance.getInstance(), instance, factory));
        this.instance = instance;
        setIconBaseWithExtension(DOCKER_INSTANCE_ICON);
        setShortDescription(instance.getInstance().getUrl());

        instance.addChangeListener(WeakListeners.change(listener, instance));
        instance.refresh();
    }

    @NbBundle.Messages({
        "# {0} - instance name",
        "LBL_Offline={0} [offline]"
    })
    @Override
    public String getDisplayName() {
        String displayName = instance.getInstance().getDisplayName();
        if (instance.isAvailable()) {
            return displayName;
        }
        return Bundle.LBL_Offline(displayName);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(PullImageAction.class),
            null,
            SystemAction.get(BuildImageAction.class),
            null,
            SystemAction.get(RefreshAction.class),
            null,
            SystemAction.get(RemoveInstanceAction.class)
        };
    }
}
