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
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Node which displays the artifacts for a build.
 */
class HudsonArtifactsNode extends AbstractNode {

    HudsonArtifactsNode(HudsonJobBuild build) {
        this(build.getArtifacts());
    }

    HudsonArtifactsNode(HudsonMavenModuleBuild module) {
        this(module.getArtifacts());
    }

    private HudsonArtifactsNode(FileSystem fs) {
        super(DataFolder.findFolder(fs.getRoot()).createNodeChildren(DataFilter.ALL));
        setName("artifact"); // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(HudsonArtifactsNode.class, "HudsonArtifactsNode.displayName");
    }

    private static final Node iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();

    public @Override Image getIcon(int type) {
        return iconDelegate.getIcon(type);
    }

    public @Override Image getOpenedIcon(int type) {
        return iconDelegate.getOpenedIcon(type);
    }

}
