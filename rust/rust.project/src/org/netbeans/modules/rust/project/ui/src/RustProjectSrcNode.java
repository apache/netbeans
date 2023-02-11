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
package org.netbeans.modules.rust.project.ui.src;

import java.awt.Image;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustIconFactory;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author antonio
 */
public class RustProjectSrcNode extends FilterNode {

    public static final String NAME = "rust-src"; // NOI18N

    private final RustProject project;

    public RustProjectSrcNode(RustProject project) throws DataObjectNotFoundException {
        this(project, new InstanceContent(), DataObject.find(project.getProjectDirectory().getFileObject("src")).getNodeDelegate());
    }

    RustProjectSrcNode(RustProject project, InstanceContent content, Node folderNode) {
        super(folderNode);
        this.project = project;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return RustIconFactory.getSourceFolderIcon(true);
    }

    @Override
    public Image getIcon(int type) {
        return RustIconFactory.getSourceFolderIcon(false);
    }

}
