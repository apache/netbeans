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

import javax.swing.Action;
import org.netbeans.modules.docker.api.DockerInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class DockerImagesNode extends AbstractNode {

    private static final String DOCKER_INSTANCE_ICON = "org/netbeans/modules/docker/ui/resources/docker_instance.png"; // NOI18N

    @NbBundle.Messages("LBL_Repository=Images")
    public DockerImagesNode(DockerInstance instance, DockerImagesChildFactory factory) {
        super(Children.create(factory, true), Lookups.fixed(instance, factory));
        setDisplayName(Bundle.LBL_Repository());
        setIconBaseWithExtension(DOCKER_INSTANCE_ICON);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { 
            SystemAction.get(RefreshAction.class)
        };
    }
}
