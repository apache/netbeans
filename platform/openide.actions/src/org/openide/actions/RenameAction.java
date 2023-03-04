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
package org.openide.actions;

import java.awt.EventQueue;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.*;


/** Rename a node.
* @see Node#setName
*
* @author   Petr Hamernik, Dafe Simonek
*/
public class RenameAction extends NodeAction {

    private static final RequestProcessor RP = new RequestProcessor(RenameAction.class); // NOI18N

    protected boolean surviveFocusChange() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(RenameAction.class, "Rename");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RenameAction.class);
    }

    protected boolean enable(Node[] activatedNodes) {
        // exactly one node should be selected
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return false;
        }

        // and must support renaming
        return activatedNodes[0].canRename();
    }

    protected void performAction(final Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return;
        }
        Node n = activatedNodes[0]; // we supposed that one node is activated
        
        // for slow FS perform rename out of EDT
        if (EventQueue.isDispatchThread() && Boolean.TRUE.equals(n.getValue("slowRename"))) { // NOI18N
            RP.post(new Runnable() {
                @Override
                public void run() {
                    performAction(activatedNodes);
                }
            });
            return;
        }

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(RenameAction.class, "CTL_RenameLabel"),
                NbBundle.getMessage(RenameAction.class, "CTL_RenameTitle")
            );
        dlg.setInputText(n.getName());

        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            String newname = null;

            try {
                newname = dlg.getInputText();

                if (!newname.equals("")) {
                    n.setName(dlg.getInputText()); // NOI18N
                }
            } catch (IllegalArgumentException e) {
                // determine if "printStackTrace"  and  "new annotation" of this exception is needed
                boolean needToAnnotate = Exceptions.findLocalizedMessage(e) == null;

                // annotate new localized message only if there is no localized message yet
                if (needToAnnotate) {
                    Exceptions.attachLocalizedMessage(e,
                                                      NbBundle.getMessage(RenameAction.class,
                                                                          "MSG_BadFormat",
                                                                          n.getName(),
                                                                          newname));
                }

                Exceptions.printStackTrace(e);
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
