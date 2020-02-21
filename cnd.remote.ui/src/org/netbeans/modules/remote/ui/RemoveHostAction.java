/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.RemoveHostAction", category = "NativeRemote")
@ActionRegistration(displayName = "#RemoveHostMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "RemoveHostAction", position = 400)
public class RemoveHostAction extends SingleHostAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "RemoveHostMenuItem");
    }

    @Override
    public boolean isVisible(Node node) {
        return isRemote(node);
    }

    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        ServerRecord record = ServerList.get(env);
        String title = NbBundle.getMessage(HostNode.class, "RemoveHostCaption");
        String message = NbBundle.getMessage(HostNode.class, "RemoveHostQuestion", record.getDisplayName());

        if (JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                message, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
            List<ServerRecord> hosts = new ArrayList<ServerRecord>(ServerList.getRecords());
            hosts.remove(record);
            ConnectionManager.getInstance().forget(env);
            cacheManager.setHosts(hosts);
            ServerRecord defaultRecord = ServerList.getDefaultRecord();
            if (defaultRecord.getExecutionEnvironment().equals(env)) {
                defaultRecord = ServerList.get(ExecutionEnvironmentFactory.getLocal());
            }
            cacheManager.setDefaultRecord(defaultRecord);
            cacheManager.applyChanges();
            ConnectionManager.getInstance().disconnect(env);
        }
    }
}
