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

import org.netbeans.modules.docker.ui.run.RunTagAction;
import javax.swing.Action;
import org.netbeans.modules.docker.api.DockerTag;
import org.netbeans.modules.docker.ui.tag.TagTagAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class DockerTagNode extends AbstractNode {

    private static final String DOCKER_INSTANCE_ICON = "org/netbeans/modules/docker/ui/resources/docker_image.png"; // NOI18N

    private final DockerTag tag;

    public DockerTagNode(DockerTag tag) {
        super(Children.LEAF, Lookups.fixed(tag));
        this.tag = tag;
        setDisplayName(tag.getTag() + " [" + tag.getShortId() + "]");
        setShortDescription(tag.getShortId());
        setIconBaseWithExtension(DOCKER_INSTANCE_ICON);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(RunTagAction.class),
            null,
            SystemAction.get(TagTagAction.class),
            null,
            SystemAction.get(PushTagAction.class),
            null,
            SystemAction.get(CopyIdAction.class),
            SystemAction.get(InspectContainerAction.class),
            null,
            SystemAction.get(RemoveTagAction.class)
        };
    }
}
