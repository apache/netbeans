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

import java.awt.Image;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerContainerDetail;
import org.netbeans.modules.docker.ui.commit.CommitContainerAction;
import org.netbeans.modules.docker.ui.rename.RenameContainerAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class DockerContainerNode extends AbstractNode {

    private static final String DOCKER_INSTANCE_ICON =
            "org/netbeans/modules/docker/ui/resources/docker_image.png"; // NOI18N

    private static final String PAUSED_ICON =
            "org/netbeans/modules/docker/ui/resources/badge_paused.png"; // NOI18N

    private static final String RUNNING_ICON
            = "org/netbeans/modules/docker/ui/resources/badge_running.png"; // NOI18N

    private final StatefulDockerContainer container;

    private final ChangeListener listener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            fireIconChange();
            fireDisplayNameChange(null, null);
        }
    };

    public DockerContainerNode(StatefulDockerContainer container) {
        super(Children.LEAF, Lookups.fixed(container.getContainer(), container));
        this.container = container;
        DockerContainer dockerContainer = container.getContainer();
        setShortDescription(dockerContainer.getShortId());
        setIconBaseWithExtension(DOCKER_INSTANCE_ICON);

        container.addChangeListener(WeakListeners.change(listener, container));
        container.refresh();
    }

    @Override
    public String getDisplayName() {
        DockerContainer dockerContainer = container.getContainer();
        DockerContainerDetail detail = container.getDetail();
        StringBuilder ret = new StringBuilder(dockerContainer.getImage());
        if (detail.getName() != null) {
            ret.append(detail.getName());
        }
        ret.append(" [").append(dockerContainer.getShortId()).append("]");
        return ret.toString();
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(StartContainerAction.class),
            SystemAction.get(StopContainerAction.class),
            SystemAction.get(PauseContainerAction.class),
            SystemAction.get(UnpauseContainerAction.class),
            null,
            SystemAction.get(CommitContainerAction.class),
            SystemAction.get(RenameContainerAction.class),
            null,
            SystemAction.get(AttachContainerAction.class),
            SystemAction.get(ShowLogAction.class),
            null,
            SystemAction.get(CopyIdAction.class),
            SystemAction.get(GetPortMappingsAction.class),
            SystemAction.get(InspectContainerAction.class),
            SystemAction.get(ProcessListAction.class),
            null,
            SystemAction.get(RefreshAction.class),
            null,
            SystemAction.get(RemoveContainerAction.class)
        };
    }

    @Override
    public Image getIcon(int type) {
        Image original = super.getIcon(type);
        return badgeIcon(original, container.getDetail().getStatus());
    }

    private static Image badgeIcon(Image image, DockerContainer.Status status) {
        Image badge = null;
        switch (status) {
            case PAUSED:
                badge = ImageUtilities.loadImage(PAUSED_ICON);
                break;
            case RUNNING:
                badge = ImageUtilities.loadImage(RUNNING_ICON);
                break;
            default:
                break;
        }
        return badge != null ? ImageUtilities.mergeImages(image, badge, 13, 8) : image;
    }
}
