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

package org.netbeans.modules.project.libraries.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;

public class NewLibraryPanel extends javax.swing.JPanel {

    private LibrariesModel model;
    private Map<Integer,String> typeMap;
    private LibraryStorageArea area;

    private DialogDescriptor dd;
    

    public NewLibraryPanel (LibrariesModel model, String preselectedLibraryType, LibraryStorageArea area) {
        this.model = model;
        this.area = area;
        initComponents();
        this.name.setColumns(25);
        this.name.getDocument().addDocumentListener(new javax.swing.event.DocumentListener () {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                nameChanged();
            }

        });
        initModel(preselectedLibraryType);
        Color c = javax.swing.UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null) {
            c = new Color(89,79,191);  // RGB suggested by Bruce in #28466
        }
        status.setForeground(c);
    }
    
    public void setDialogDescriptor(DialogDescriptor dd) {
        assert this.dd == null;
        this.dd = dd;
        nameChanged();
    }
    
    public String getLibraryType () {
        return typeMap.get(libraryType.getSelectedIndex());
    }
    
    public String getLibraryName () {
        return this.name.getText();
    }

    public void addNotify() {
        super.addNotify();
        this.name.selectAll();
    }


    private void initModel(String preselectedLibraryType) {
        this.typeMap = new HashMap<Integer,String>();
        this.name.setText (NbBundle.getMessage (NewLibraryPanel.class,"TXT_NewLibrary"));
        LibraryTypeProvider[] providers = LibrariesSupport.getLibraryTypeProviders();
        int index = 0;
        for (int i=0; i< providers.length; i++) {
            String type = providers[i].getLibraryType();
            if (type.equals(preselectedLibraryType)) {
                index = i;
            }
            typeMap.put(i ,type);
            String displayName = providers[i].getDisplayName();
            if (displayName == null) {
                displayName = providers[i].getLibraryType();
            }            
            this.libraryType.addItem (displayName);
        }
        if (this.libraryType.getItemCount() > 0) {
            this.libraryType.setSelectedIndex(index);
        }
    }


    private void nameChanged () {
        String name = this.name.getText();
        boolean valid = false;
        String message = "";    //NOI18N
        if (name.length() == 0) {
            message = NbBundle.getMessage(NewLibraryPanel.class,"ERR_InvalidName");
        } else if (LibrariesCustomizer.isExistingDisplayName(model, name, area)){
            message = NbBundle.getMessage(NewLibraryPanel.class, "ERR_ExistingName", name);
        } else {
            valid = true;
        }
        if (dd != null) {
            dd.setValid(valid);
        }
        this.status.setText(message);
    }
        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        libraryType = new javax.swing.JComboBox();
        status = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabel2.setLabelFor(name);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NewLibraryPanel.class, "CTL_LibraryName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 6);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 6, 12);
        add(name, gridBagConstraints);
        name.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewLibraryPanel.class, "AD_LibraryName")); // NOI18N

        jLabel1.setLabelFor(libraryType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NewLibraryPanel.class, "CTL_LibraryType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 6);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        add(libraryType, gridBagConstraints);
        libraryType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewLibraryPanel.class, "AD_LibraryType")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        add(status, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle"); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("AD_NewLibraryPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox libraryType;
    private javax.swing.JTextField name;
    private javax.swing.JLabel status;
    // End of variables declaration//GEN-END:variables
    
}
