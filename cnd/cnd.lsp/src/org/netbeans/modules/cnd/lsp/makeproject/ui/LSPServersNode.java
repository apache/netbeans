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
package org.netbeans.modules.cnd.lsp.makeproject.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * The "LSP Servers" node in a make based project. This may hold
 * different LSP servers. Currently only CLANGD_LSP_SERVER is supported.
 * @author antonio
 */
public class LSPServersNode extends AbstractNode {

    // TODO: Add some more C/C++ LSP servers in the future.
    public static final String CLANGD_LSP_SERVER = "clangd"; // NOI18N
    public static final String[] SERVER_TYPES = {CLANGD_LSP_SERVER};

    private static class LSPServersNodeChildren extends Children.Keys<String> {

        private final Project project;

        private LSPServersNodeChildren(Project project) {
            this.project = project;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(SERVER_TYPES);
        }

        @Override
        protected Node[] createNodes(String key) {
            switch (key) {
                case CLANGD_LSP_SERVER:
                    return new Node[]{new ClangdLSPServerNode(project)};
            }
            return new Node[]{};
        }

    }

    private final Project project;

    public LSPServersNode(Project project) {
        this(project, new InstanceContent());
    }

    private LSPServersNode(Project project, InstanceContent instanceContent) {
        super(new LSPServersNodeChildren(project), new AbstractLookup(instanceContent));
        this.project = project;
        instanceContent.add(project);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(LSPServersNode.class, "LSPServersNode.shortDescription"); // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(LSPServersNode.class, "LSPServersNode.displayName"); // NOI18N
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/lsp/makeproject/ui/resources/importantFolder.gif"); // NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/lsp/makeproject/ui/resources/importantFolderOpened.gif"); // NOI18N
    }

}
