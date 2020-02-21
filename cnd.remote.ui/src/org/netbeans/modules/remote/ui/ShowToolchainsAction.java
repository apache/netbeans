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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 */
public class ShowToolchainsAction extends AbstractAction {

    private final ExecutionEnvironment env;
    private final CompilerSet compilerSet;

    public ShowToolchainsAction(ExecutionEnvironment env, CompilerSet compilerSet) {
        super(NbBundle.getMessage(ToolchainListRootNode.class, "PropertiesMenuItem"));
        this.env = env;
        this.compilerSet = compilerSet;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //OptionsDisplayer.getDefault().open(CndUIConstants.TOOLS_OPTIONS_CND_TOOLS_PATH);
        JComponent tpc = ToolsPanelSupport.getToolsPanelComponent(env, compilerSet == null ? null : compilerSet.getName());
        String title = NbBundle.getMessage(ToolchainListRootNode.class, "CompilerSetPropertieesDlgTitile",
                ServerList.get(env).getDisplayName());
        tpc.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        DialogDescriptor dd = new DialogDescriptor(tpc, title);
        dd.setModal(true);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.pack();
        try {
            dlg.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dlg.dispose();
        }
        if (dd.getValue() == NotifyDescriptor.OK_OPTION) {
            VetoableChangeListener okL = (VetoableChangeListener) tpc.getClientProperty(ToolsPanelSupport.OK_LISTENER_KEY);
            CndUtils.assertNotNull(okL, "VetoableChangeListener shouldn't be null"); //NOI18N
            if (okL != null) {
                try {
                    okL.vetoableChange(null);
                } catch (PropertyVetoException ex) {
                }
            }
        }
    }
}
