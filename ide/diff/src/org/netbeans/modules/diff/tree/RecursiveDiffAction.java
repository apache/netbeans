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

package org.netbeans.modules.diff.tree;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.NodeAction;

/**
 * Action to create a diff between two folders.
 */
@ActionID(
    category = "Tools",
    id = "org.netbeans.modules.diff.tree.RecursiveDiffAction"
)
@ActionRegistration(
    displayName = "#CTL_RecursiveDiffAction",
    iconInMenu = false,
    lazy = false
)
@ActionReference(
    path = "UI/ToolActions/Files",
    position = 300
)
@Messages("CTL_RecursiveDiffAction=Tree diff")
public final class RecursiveDiffAction extends NodeAction implements ActionListener {

    public RecursiveDiffAction() {
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(Node[] nodes) {
        List<FileObject> dirObjects = extractDirectories(nodes);
        if(dirObjects.size() != 2) {
            return;
        }
        RecursiveDiffer differ = new RecursiveDiffer(dirObjects.get(0), dirObjects.get(1));
        TreeDiffViewerTopComponent tdvtc = new TreeDiffViewerTopComponent(differ);
        tdvtc.open();
        tdvtc.requestActive();
        differ.startScan();
    }

    @Override
    protected boolean enable(Node[] nodes) {
        return extractDirectories(nodes).size() == 2;
    }

    private List<FileObject> extractDirectories(Node[] nodes) {
        List<FileObject> dirObjects = new ArrayList<>(nodes.length);
        for (Node n : nodes) {
            FileObject backingFile = n.getLookup().lookup(FileObject.class);
            if (backingFile != null && backingFile.isFolder()) {
                dirObjects.add(backingFile);
            }
        }
        return dirObjects;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RecursiveDiffAction.class, "CTL_RecursiveDiffAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.diff.tree.RecursiveDiffAction");
    }


}
