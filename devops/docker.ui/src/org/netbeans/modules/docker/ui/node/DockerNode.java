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
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.docker.api.DockerSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class DockerNode extends AbstractNode {

    private static final String DOCKER_ICON = "org/netbeans/modules/docker/ui/resources/docker_root.png"; // NOI18N

    private static DockerNode node;

    private DockerNode(DockerChildFactory factory, String displayName, String shortDesc, String iconBase) {
        super(Children.create(factory, true));

        setName(""); // NOI18N
        setDisplayName(displayName);
        setShortDescription(shortDesc);
        setIconBaseWithExtension(iconBase);
    }

    @ServicesTabNodeRegistration(
        name = "docker",
        displayName = "org.netbeans.modules.docker.ui.node.Bundle#Docker_Root_Node_Name",
        shortDescription = "org.netbeans.modules.docker.ui.node.Bundle#Docker_Root_Node_Short_Description",
        iconResource = "org/netbeans/modules/docker/ui/resources/docker_root.png",
        position = 500
    )
    public static synchronized DockerNode getInstance() {
        if (node == null) {
            DockerChildFactory factory = new DockerChildFactory(DockerSupport.getDefault());
            factory.init();

            node = new DockerNode(factory,
                    NbBundle.getMessage(DockerNode.class, "Docker_Root_Node_Name"),
                    NbBundle.getMessage(DockerNode.class, "Docker_Root_Node_Short_Description"),
                    DOCKER_ICON);
        }
        return node;
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> ret = new ArrayList<>();
        ret.addAll(Utilities.actionsForPath("Docker/Wizard")); // NOI18N
        ret.add(null);
        ret.addAll(Utilities.actionsForPath("Docker/Credentials")); // NOI18N
        return ret.toArray(new Action[0]);
    }

}
