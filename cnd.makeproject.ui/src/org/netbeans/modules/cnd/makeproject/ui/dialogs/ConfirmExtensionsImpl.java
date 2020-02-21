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

/*
 * ConfirmExtensions.java
 *
 * Created on Dec 30, 2008, 1:49:00 PM
 */

package org.netbeans.modules.cnd.makeproject.ui.dialogs;

import java.text.MessageFormat;
import java.util.Set;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.MimeExtensions;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ConfirmMimeExtensionsFactory;

/**
 *
 */
public class ConfirmExtensionsImpl extends javax.swing.JPanel implements MimeExtensions {
    @org.openide.util.lookup.ServiceProvider(service=ConfirmMimeExtensionsFactory.class)
    public static final class ConfirmExtensionsUiFactoryImpl implements ConfirmMimeExtensionsFactory {

        @Override
        public MimeExtensions create(Set<String> unknownC, Set<String> unknownCpp, Set<String> unknownH) {
            ConfirmExtensionsImpl confirmExtensions = new ConfirmExtensionsImpl(unknownC, unknownCpp, unknownH);
            DialogDescriptor dialogDescriptor = new DialogDescriptor(confirmExtensions,
                    NbBundle.getMessage(ConfirmExtensionsImpl.class, "ConfirmExtensions.dialog.title")); // NOI18N
            DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                return confirmExtensions;
            }
            return null;
        }

        @Override
        public ConfirmSupport.MimeExtension create(Set<String> usedExtension, String type) {
            String message = NbBundle.getMessage(ConfirmExtensionsImpl.class,"ADD_EXTENSION_QUESTION" + type + (usedExtension.size() == 1 ? "" : "S")); // NOI18N
            StringBuilder extensions = new StringBuilder();
            usedExtension.forEach((ext) -> {
                if (extensions.length() > 0) {
                    extensions.append(',');
                }
                extensions.append(ext);
            });
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                    MessageFormat.format(message, new Object[]{extensions.toString()}),
                    NbBundle.getMessage(ConfirmExtensionsImpl.class, "ADD_EXTENSION_DIALOG_TITLE" + type + (usedExtension.size() == 1 ? "" : "S")), // NOI18N
                    NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                return () -> true;
            }
            return null;
        }
    }
    private final Set<String> unknownC;
    private final Set<String> unknownCpp;
    private final Set<String> unknownH;

    /** Creates new form ConfirmExtensions */
    private ConfirmExtensionsImpl(Set<String> unknownC, Set<String> unknownCpp, Set<String> unknownH) {
        this.unknownC = unknownC;
        this.unknownCpp = unknownCpp;
        this.unknownH = unknownH;
        initComponents();
        textPane.setBackground(getBackground());
        if (unknownC.isEmpty()) {
            cCheck.setSelected(false);
            cCheck.setVisible(false);
        } else {
            cCheck.setSelected(true);
        }
        if (unknownCpp.isEmpty()) {
            cppCheck.setSelected(false);
            cppCheck.setVisible(false);
        } else {
            cppCheck.setSelected(true);
        }
        if (unknownH.isEmpty()) {
            headerCheck.setSelected(false);
            headerCheck.setVisible(false);
        } else {
            headerCheck.setSelected(true);
        }
    }

    @Override
    public boolean isC(){
        return cCheck.isSelected();
    }

    private String getCList(){
        return extensionText(unknownC);
    }

    @Override
    public boolean isCpp(){
        return cppCheck.isSelected();
    }

    private String getCppList(){
        return extensionText(unknownCpp);
    }

    @Override
    public boolean isHeader(){
        return headerCheck.isSelected();
    }

    private String getHeaderList(){
        return extensionText(unknownH);
    }

    private String extensionText(Set<String> unknown) {
        StringBuilder extensions = new StringBuilder();
        unknown.forEach((ext) -> {
            if (extensions.length() > 0) {
                extensions.append(','); // NOI18N
            }
            extensions.append(ext);
        });
        return extensions.toString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        headerCheck = new javax.swing.JCheckBox();
        cCheck = new javax.swing.JCheckBox();
        cppCheck = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setMinimumSize(new java.awt.Dimension(200, 200));
        setPreferredSize(new java.awt.Dimension(300, 200));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(headerCheck, org.openide.util.NbBundle.getMessage(ConfirmExtensionsImpl.class, "ConfirmExtensions.headerCheck.text1", getHeaderList()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(headerCheck, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cCheck, org.openide.util.NbBundle.getMessage(ConfirmExtensionsImpl.class, "ConfirmExtensions.cCheck.text1", getCList()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(cCheck, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cppCheck, org.openide.util.NbBundle.getMessage(ConfirmExtensionsImpl.class, "ConfirmExtensions.cppCheck.text1", getCppList()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(cppCheck, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setEnabled(false);
        jScrollPane1.setFocusable(false);

        textPane.setEditable(false);
        textPane.setText(org.openide.util.NbBundle.getMessage(ConfirmExtensionsImpl.class, "ConfirmExtensions.textPane.text1")); // NOI18N
        textPane.setFocusable(false);
        jScrollPane1.setViewportView(textPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 284, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.3;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cCheck;
    private javax.swing.JCheckBox cppCheck;
    private javax.swing.JCheckBox headerCheck;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane textPane;
    // End of variables declaration//GEN-END:variables

}
