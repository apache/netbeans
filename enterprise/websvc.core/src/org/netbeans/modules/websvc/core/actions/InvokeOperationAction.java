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
package org.netbeans.modules.websvc.core.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.websvc.api.support.InvokeOperationCookie;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Peter Williams
 */
public class InvokeOperationAction extends NodeAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(InvokeOperationAction.class, "LBL_CallWebServiceOperation"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        // If you will provide context help then use:
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }
        FileObject currentFO = getCurrentFileObject(activatedNodes[0]);
        if (currentFO == null || WebServiceActionProvider.getInvokeOperationAction(currentFO) == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if(activatedNodes[0] != null) {
            FileObject currentFO = getCurrentFileObject(activatedNodes[0]);
            if(currentFO != null) {
                // !PW I wrote this code before I knew about NodeOperation.  Anyway, this
                // behaves a bit nicer in that the root node is hidden and the tree opens
                // up expanded.  Both improve usability for this use case I think.
                InvokeOperationCookie invokeOp = WebServiceActionProvider.getInvokeOperationAction(currentFO);
                InvokeOperationCookie.ClientSelectionPanel serviceExplorer = invokeOp.getDialogDescriptorPanel();
                final DialogDescriptor descriptor = new DialogDescriptor(serviceExplorer,
                        NbBundle.getMessage(InvokeOperationAction.class, "TTL_SelectOperation"));

                // !PW FIXME put help context here when known to get a displayed help button on the panel.
//                descriptor.setHelpCtx(new HelpCtx("HelpCtx_J2eePlatformInstallRootQuery"));
//                Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
//                dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
//                dlg.setVisible(true);
                serviceExplorer.addPropertyChangeListener(
                        InvokeOperationCookie.ClientSelectionPanel.PROPERTY_SELECTION_VALID,
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                descriptor.setValid(((Boolean)evt.getNewValue()));
                            }

                        });
                descriptor.setValid(false);
                DialogDisplayer.getDefault().notify(descriptor);
                if (descriptor.getValue().equals(NotifyDescriptor.OK_OPTION)) {
                    // !PW FIXME refactor this as a method implemented in a cookie
                    // on the method node.
                    InvokeOperationCookie invokeCookie = WebServiceActionProvider.getInvokeOperationAction(currentFO);
                    //JTextComponent textComp = activatedNodes[0].getLookup().lookup(JTextComponent.class);

                    if (invokeCookie!=null) {
                        JTextComponent target = Utilities.getFocusedComponent();
                        if (target != null) {
                            invokeCookie.invokeOperation(serviceExplorer.getSelectedClient(), target);
                        }
                        // logging usage of action
                        Object[] params = new Object[2];
                        String cookieClassName = invokeCookie.getClass().getName();
                        if (cookieClassName.contains("jaxrpc")) { // NOI18N
                            params[0] = LogUtils.WS_STACK_JAXRPC;
                        } else {
                            params[0] = LogUtils.WS_STACK_JAXWS;
                        }
                        params[1] = "CALL WS OPERATION"; // NOI18N
                        LogUtils.logWsAction(params);
                    }
                }
            }
        }
    }
    
    private FileObject getCurrentFileObject(Node n) {
        FileObject result = null;
        DataObject dobj = (DataObject) n.getCookie(DataObject.class);
        if(dobj != null) {
            result = dobj.getPrimaryFile();
        }
        return result;
    }
    
}
