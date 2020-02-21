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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.ui.server;

import java.awt.Dialog;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.ui.ServerListUIEx;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.ui.EditServerListDialog;
import org.netbeans.modules.cnd.remote.ui.HostPropertiesDialog;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * ServerListDisplayer implementation
 */
@ServiceProvider(service = ServerListUI.class)
public class RemoteServerListUI extends ServerListUIEx {

    @Override
    protected boolean showServerListDialogImpl() {
        return showServerListDialogImpl((AtomicReference<ExecutionEnvironment>) null);
    }

    @Override
    protected boolean showServerListDialogImpl(AtomicReference<ExecutionEnvironment> selectedEnv) {
        ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
        if (showServerListDialog(cacheManager, selectedEnv)) {
            cacheManager.applyChanges();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean showServerRecordPropertiesDialogImpl(ExecutionEnvironment env) {
        ServerRecord record = RemoteServerList.getInstance().get(env);
        if (record instanceof RemoteServerRecord) {
            if (HostPropertiesDialog.invokeMe((RemoteServerRecord)record)) {
                return true;
            }
        }
        return false;
    }

    @Override    
    protected JComponent getServerListComponentImpl(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv) {
        EditServerListDialog dlg = new EditServerListDialog(cacheManager, selectedEnv, true);
        return dlg;
    }

    @Override
    protected boolean showServerListDialogImpl(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv) {
        EditServerListDialog dlg = new EditServerListDialog(cacheManager, selectedEnv);
        DialogDescriptor dd = new DialogDescriptor(dlg, NbBundle.getMessage(getClass(), "TITLE_EditServerList"), true,
                    DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        dlg.setDialogDescriptor(dd);
        dd.addPropertyChangeListener(dlg);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setMinimumSize(dialog.getPreferredSize());
        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dialog.dispose();
        }
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            cacheManager.setHosts(dlg.getHosts());
            cacheManager.setDefaultRecord(dlg.getDefaultRecord());
            return true;
        } else {
            return false;
        }
    }

    public static boolean showConfirmDialog(final String message, final String title) {
        final AtomicBoolean res = new AtomicBoolean(false);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    int option = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), 
                            message, title, JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        res.set(true);                                
                    }
                }
            });
        } catch (InterruptedException ex) {
        } catch (InvocationTargetException ex) {
        }
        return res.get();
    }

    @Override
    protected boolean ensureRecordOnlineImpl(ExecutionEnvironment env, String customMessage) {
        CndUtils.assertNonUiThread();
        if (env.isLocal()) {
            return true;
        }
        ServerRecord record = ServerList.get(env);
        boolean result = false;
        if (record.isDeleted()) {
            String message = MessageFormat.format(
                    NbBundle.getMessage(getClass(), "ERR_RequestingDeletedConnection"),
                    record.getDisplayName());
            boolean res = showConfirmDialog(message, NbBundle.getMessage(getClass(), "DLG_TITLE_DeletedConnection"));
            if (res) {
                ServerList.addServer(record.getExecutionEnvironment(), record.getDisplayName(), record.getSyncFactory(), false, true);
                result = true;
            }
        } else if (record.isOnline()) {
            result = true;
        } else { //  !record.isOnline()
            String message = MessageFormat.format(
                    NbBundle.getMessage(getClass(), "ERR_NeedToConnectToRemoteHost"),
                    record.getDisplayName());
            boolean res = showConfirmDialog(message, NbBundle.getMessage(getClass(), "DLG_TITLE_Connect"));
            if (res) {
                if (ConnectionManager.getInstance().connect(record.getExecutionEnvironment())) {
                    record.validate(true);
                    result = true;
                }
            }
        }
        return result;
    }

    @Override
    protected boolean ensureRecordOnlineImpl(ExecutionEnvironment env) {
        return ensureRecordOnlineImpl(env, null);
    }
}
