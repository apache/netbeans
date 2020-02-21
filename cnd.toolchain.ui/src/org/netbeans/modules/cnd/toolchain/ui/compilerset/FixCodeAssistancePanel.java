/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

/*
 * FixCodeAssistancePanel.java
 *
 * Created on Jan 21, 2011, 3:13:22 PM
 */
package org.netbeans.modules.cnd.toolchain.ui.compilerset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public class FixCodeAssistancePanel extends javax.swing.JPanel {

    /** Creates new form FixCodeAssistancePanel */
    private FixCodeAssistancePanel(Map<Tool, List<List<String>>> needReset) {
        initComponents();
        StringBuilder buf = new StringBuilder();
        for(Map.Entry<Tool, List<List<String>>> entry : needReset.entrySet()) {
            Tool key = entry.getKey();
            if (buf.length() > 0) {
                buf.append('\n'); // NOI18N
            }
            buf.append(key.getCompilerSet()).append(" ").append(key.getDisplayName()); // NOI18N
        }
        textArea.setText(NbBundle.getMessage(FixCodeAssistancePanel.class, "FixCodeAssistancePanel.message", buf.toString()));
    }

    public static void showNotification(final Map<Tool, List<List<String>>> needReset, final CompilerSetManager csm) {
        final String title = NbBundle.getMessage(FixCodeAssistancePanel.class, "FixCodeAssistancePanel.title.text"); // NOI18N
        ActionListener onClickAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JButton btnOk = new JButton( NbBundle.getMessage(FixCodeAssistancePanel.class, "FixCodeAssistancePanel.action.OK")); // NOI18N
                JButton btnCancel = new JButton( NbBundle.getMessage(FixCodeAssistancePanel.class, "FixCodeAssistancePanel.action.CANCEL")); // NOI18N
                FixCodeAssistancePanel panel = new FixCodeAssistancePanel(needReset);
                DialogDescriptor descriptor = new DialogDescriptor(panel, title,
                        true, new Object[]{btnOk,btnCancel}, DialogDescriptor.CLOSED_OPTION,
                        DialogDescriptor.DEFAULT_ALIGN, null, null);
                if (DialogDisplayer.getDefault().notify(descriptor) == btnOk) {
                    ToolchainUtilities.fixCSM(needReset, csm);
                }
            }
        };
        ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/toolchain/compilerset/exclamation.gif", false); // NOI18N
        NotificationDisplayer.getDefault().notify(title, icon,
                NbBundle.getMessage(FixCodeAssistancePanel.class, "FixCodeAssistancePanel.action.text"), // NOI18N
                onClickAction, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 0, 6));
        setPreferredSize(new java.awt.Dimension(350, 100));
        setLayout(new java.awt.BorderLayout());

        textArea.setColumns(20);
        textArea.setEditable(false);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
