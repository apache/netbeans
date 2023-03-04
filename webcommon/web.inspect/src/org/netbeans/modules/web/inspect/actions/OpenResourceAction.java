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
package org.netbeans.modules.web.inspect.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.actions.Openable;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that opens a resource in the editor.
 *
 * @author Jan Stola
 */
public class OpenResourceAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            Resource resource = activatedNode.getLookup().lookup(Resource.class);
            FileObject fob = resource.toFileObject();
            if (fob != null) {
                try {
                    DataObject dob = DataObject.find(fob);
                    Openable openable = dob.getLookup().lookup(Openable.class);
                    if (openable != null) {
                        openable.open();
                    }
                } catch (DataObjectNotFoundException ex) {
                    Logger.getLogger(OpenResourceAction.class.getName()).log(Level.INFO, null, ex);
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        for (Node activatedNode : activatedNodes) {
            Resource resource = activatedNode.getLookup().lookup(Resource.class);
            if ((resource != null) && (resource.toFileObject() == null)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(OpenResourceAction.class, "OpenResourceAction.displayName"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
