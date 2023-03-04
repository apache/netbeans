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
package org.netbeans.modules.websvc.saas.ui.actions;

import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that displays WADL for a selected web service.
 *
 * @author nam
 * @author Jan Stola
 */
public class ViewWadlAction extends NodeAction {

    @Override
    protected boolean enable(Node[] nodes) {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewWSDLAction.class, "VIEW_WADL"); // NOI18N
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            WadlSaas saas = node.getLookup().lookup(WadlSaas.class);
            if (saas != null) {
                saas.toStateReady(true);
                if (saas.getLocalWadlFile() != null) {
                    try {
                        DataObject wadlDataObject = DataObject.find(saas.getLocalWadlFile());
                        EditorCookie editorCookie = wadlDataObject.getLookup().lookup(EditorCookie.class);
                        editorCookie.open();
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        }
    }

    @Override
    public boolean asynchronous() {
        return true;
    }
}
