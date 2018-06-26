/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
