/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.hudson.ui.nodes;

import java.awt.Image;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Node which displays the remote workspace for a job.
 * XXX ought to show unversioned files in grey, artifacts in boldface, etc. etc.
 */
class HudsonWorkspaceNode extends AbstractNode {

    HudsonWorkspaceNode(HudsonJob job) {
        super(DataFolder.findFolder(job.getRemoteWorkspace().getRoot()).createNodeChildren(DataFilter.ALL));
        setName("ws"); // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(HudsonWorkspaceNode.class, "HudsonWorkspaceNode.displayName");
    }

    private static final Node iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();

    public @Override Image getIcon(int type) {
        return iconDelegate.getIcon(type);
    }

    public @Override Image getOpenedIcon(int type) {
        return iconDelegate.getOpenedIcon(type);
    }

}
