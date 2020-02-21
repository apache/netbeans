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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.api.toolchain.ui.ServerListUIEx;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * Popup a dialog which lets the user reconnect to an offline remote host.
 *
 */
public class DevelopmentHostCustomizer extends JPanel implements VetoableChangeListener {

    private final DevelopmentHostConfiguration dhconf;
    private final PropertyEnv propertyEnv;
    private final PropertyEditorSupport editor;
    private final ExecutionEnvironment oldExecEnv;
    private final AtomicReference<ExecutionEnvironment> selectedEnv;
    private final ToolsCacheManager cacheManager;
    private final JComponent component;

    /**
     * Show the customizer dialog. If we're already online, show a meaningless message (I don't think
     * we can disable the property editor just because we're online). If we're offline, let the user
     * decide if they want to try and reconnect. If they do, do the same reconnect done via a build or
     * run action.
     *
     * @param dhconf The remote host configuration
     * @param propertyEnv A PropertyEnv where we can control the custom property editor
     */
    public DevelopmentHostCustomizer(DevelopmentHostConfiguration dhconf, PropertyEditorSupport editor, PropertyEnv propertyEnv) {
        this.dhconf = dhconf;
        this.editor = editor;
        this.propertyEnv = propertyEnv;
        this.oldExecEnv = (dhconf == null) ? null : dhconf.getExecutionEnvironment();
        this.selectedEnv = new AtomicReference<>(this.oldExecEnv);
        this.cacheManager = ToolsCacheManager.createInstance(true);
        this.setLayout(new BorderLayout());
        component = ServerListUIEx.getServerListComponent(cacheManager, selectedEnv);
        add(component, BorderLayout.CENTER);
        propertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        propertyEnv.addVetoableChangeListener(this);
    }

    /**
     * Once the user presses OK, we attempt to validate the remote host. We never veto the action
     * because a failure should still close the property editor, but with the host still offline.
     * Set the PropertyEnv state to valid so the dialog is removed.
     *
     * @param evt A PropertyEnv where we can control the custom property editor
     * @throws java.beans.PropertyVetoException
     */
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        boolean changed = false;
        ExecutionEnvironment env = selectedEnv.get();
        if (env == null) {
            throw new PropertyVetoException(NbBundle.getMessage(getClass(), "MSG_Null_Host"), evt);
        }
        //if (env.equals(oldExecEnv)) {
        //    return;
        //}
        ServerListUIEx.save(cacheManager, component);
        cacheManager.applyChanges();
        dhconf.setHost(env, true);
//        if (!dhconf.isConfigured()) {
//            ExecutionEnvironment execEnv = dhconf.getExecutionEnvironment();
//            final ServerRecord record = ServerList.get(execEnv);
//            assert record != null;
//
//            // start validation phase
//            final Frame mainWindow = WindowManager.getDefault().getMainWindow();
//            Runnable csmWorker = new Runnable() {
//                public void run() {
//                    try {
//                        record.validate(true);
//                        // initialize compiler sets for remote host if needed
//                        CompilerSetManager csm = CompilerSetManager.getDefault(record.getExecutionEnvironment());
//                        csm.initialize(true, true);
//                    } catch(Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//            // Note: Messages come from different class bundle...
//            String msg = NbBundle.getMessage(getClass(), "MSG_Configure_Host_Progress", record.getDisplayName());
//            final String title = NbBundle.getMessage(getClass(), "DLG_TITLE_Configure_Host", record.getExecutionEnvironment().getHost());
//            ModalMessageDlg.runLongTask(mainWindow, csmWorker, null, null, title, msg);
//            propertyEnv.removeVetoableChangeListener(this);
//            propertyEnv.setState(PropertyEnv.STATE_VALID);
//            if (!record.isOnline()) {
//                System.err.println("");
//            }
//        }
    }
}
