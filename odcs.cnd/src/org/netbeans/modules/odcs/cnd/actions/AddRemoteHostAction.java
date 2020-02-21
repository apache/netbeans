/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.odcs.cnd.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.ui.validation.NotifyDescriptorIndicator;
import org.netbeans.modules.cnd.utils.ui.validation.TextComponentValidator;
import org.netbeans.modules.cnd.utils.ui.validation.Validators;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.odcs.cnd.execution.DevelopVMExecutionEnvironment;
import org.netbeans.modules.odcs.cnd.ui.DevelopVMConnectionPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 */
public class AddRemoteHostAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(AddRemoteHostAction.class.getName());

    private final String serverUrl;
    private final String machineId;

    @NbBundle.Messages({
        "add_remote_host=Add VM as a remote host"
    })
    public AddRemoteHostAction(String serverUrl, String machineId) {
        super(Bundle.add_remote_host());
        this.serverUrl = serverUrl;
        this.machineId = machineId;
    }

    @NbBundle.Messages({
        "add_remote_host_config_title=Connection properties"
    })
    @Override
    public void actionPerformed(ActionEvent e) {
        DevelopVMConnectionPanel panel = new DevelopVMConnectionPanel(machineId + "@" + serverUrl);

        final DialogDescriptor dd = new DialogDescriptor(
                panel,
                Bundle.add_remote_host_config_title(),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null
        );

        dd.setValid(false);

        TextComponentValidator validator = Validators.createTextComponentValidator(new NotifyDescriptorIndicator(dd));
        validator.addTextComponentRule(panel.getUserField(), s -> !s.isEmpty());
        validator.addTextComponentRule(panel.getSshField(), s -> s.matches("[0-9]+"));

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);

        if (dd.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }

        String user = panel.getUserField().getText();
        int port = Integer.valueOf(panel.getSshField().getText());

        selectNode(DevelopVMExecutionEnvironment.encode(user, machineId, port, serverUrl));
    }

    /**
     * Try to select a node somewhere beneath the root node in the Services tab.
     *
     * @param path a path as in {@link NodeOp#findPath(Node, String[])}
     */
    public static void selectNode(final String... path) {
        Mutex.EVENT.readAccess(() -> {
            TopComponent tab = WindowManager.getDefault().findTopComponent("services"); // NOI18N
            if (tab == null) {
                // XXX have no way to open it, other than by calling ServicesTabAction
                LOG.fine("No ServicesTab found");
                return;
            }
            tab.open();
            tab.requestActive();
            if (!(tab instanceof ExplorerManager.Provider)) {
                LOG.fine("ServicesTab not an ExplorerManager.Provider");
                return;
            }
            final ExplorerManager mgr = ((ExplorerManager.Provider) tab).getExplorerManager();
            final Node root = mgr.getRootContext();
            RequestProcessor.getDefault().post(() -> {
                Node buildHosts = NodeOp.findChild(root, "remote");
                if (buildHosts == null) {
                    LOG.fine("ServicesTab does not contain C/C++ Build Hosts");
                    return;
                }
                Node _selected;
                try {
                    _selected = NodeOp.findPath(buildHosts, path);
                } catch (NodeNotFoundException x) {
                    LOG.log(Level.FINE, "Could not find subnode", x);
                    ExecutionEnvironment ee = ExecutionEnvironmentFactory.fromUniqueID(path[0]);
                    if (ee != null && ee instanceof DevelopVMExecutionEnvironment) {
                        ((DevelopVMExecutionEnvironment) ee).initializeOrWait();
                        ServerRecord serverRecord = ServerList.addServer(ee, ee.getDisplayName(), RemoteSyncFactory.getDefault(), false, true);
                        if (serverRecord != null) {
                            try {
                                _selected = NodeOp.findPath(buildHosts, path);
                            } catch (NodeNotFoundException ex) {
                                LOG.log(Level.FINE, "Could not find created subnode", ex);
                                _selected = x.getClosestNode();
                            }
                        } else {
                            LOG.log(Level.FINE, "Could not create subnode", ee.toString());
                            _selected = x.getClosestNode();
                        }
                    } else {
                        _selected = x.getClosestNode();
                    }
                }
                final Node selected = _selected;
                Mutex.EVENT.readAccess(() -> {
                    try {
                        mgr.setSelectedNodes(new Node[]{selected});
                    } catch (PropertyVetoException x) {
                        LOG.log(Level.FINE, "Could not select path", x);
                    }
                });
            });
        });
    }
}
