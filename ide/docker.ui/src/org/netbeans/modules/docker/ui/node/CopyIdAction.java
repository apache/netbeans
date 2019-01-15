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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.docker.api.DockerEntity;

/**
 *
 * @author Petr Hejl
 */
public class CopyIdAction extends NodeAction {

    @NbBundle.Messages({
        "# {0} - copied ID",
        "MSG_StatusCopyToClipboard=Copy to Clipboard: {0}",
        "MSG_CouldNotCopy=Could not copy the ID"})
    @Override
    protected void performAction(Node[] activatedNodes) {
        assert activatedNodes.length == 1;
        DockerEntity idProvider = activatedNodes[0].getLookup().lookup(DockerEntity.class);
        if (idProvider != null) {
            Clipboard clipboard = getClipboard();
            if (clipboard != null) {
                try {
                    clipboard.setContents(new StringSelection(idProvider.getId()), null);
                    StatusDisplayer.getDefault().setStatusText(Bundle.MSG_StatusCopyToClipboard(idProvider.getId()));
                } catch (IllegalStateException ex) {
                    StatusDisplayer.getDefault().setStatusText(Bundle.MSG_CouldNotCopy());
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes.length == 1
                && activatedNodes[0].getLookup().lookup(DockerEntity.class) != null;
    }

    @NbBundle.Messages("LBL_CopyIdAction=Copy ID")
    @Override
    public String getName() {
        return Bundle.LBL_CopyIdAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    private static Clipboard getClipboard() {
        Clipboard ret = Lookup.getDefault().lookup(Clipboard.class);
        if (ret == null) {
            ret = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return ret;
    }
}
