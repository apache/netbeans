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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class StdLibPanel extends javax.swing.JPanel {

    private final MyListCellRenderer myListCellRenderer = new MyListCellRenderer();

    /** Creates new form StdLibPanel */
    public StdLibPanel(LibraryItem.StdLibItem[] stdLibs) {
        initComponents();
	libraryList = new JList(stdLibs);
	libraryList.setCellRenderer(myListCellRenderer);
        scrollPane.setViewportView(libraryList);
	setPreferredSize(new java.awt.Dimension(300, 300));
        // Accessibility
        label.setLabelFor(libraryList);
        libraryList.getAccessibleContext().setAccessibleDescription(getString("LIBRARY_LIST_SD"));
        libraryList.getAccessibleContext().setAccessibleName(getString("LIBRARY_LIST_NM"));
        getAccessibleContext().setAccessibleDescription(getString("LIBRARY_LIST_SD"));
    }
    
    static String getLibraryIconResource(LibraryItem libraryItem) {
        String iconName = "org/netbeans/modules/cnd/resources/blank.gif"; // NOI18N
        switch(libraryItem.getType()) {
            case LibraryItem.PROJECT_ITEM:
                iconName = "org/netbeans/modules/cnd/makeproject/resources/makeProject.gif"; // NOI18N
                break;
            case LibraryItem.STD_LIB_ITEM:
                iconName = "org/netbeans/modules/cnd/resources/stdLibrary.gif"; // NOI18N
                break;
            case LibraryItem.LIB_ITEM:
                iconName = "org/netbeans/modules/cnd/loaders/LibraryIcon.gif"; // NOI18N
                break;
            case LibraryItem.LIB_FILE_ITEM:
                if (libraryItem.getPath().endsWith(".so") || libraryItem.getPath().endsWith(".dll") || libraryItem.getPath().endsWith(".dylib")) { // NOI18N
                    iconName = "org/netbeans/modules/cnd/loaders/DllIcon.gif"; // NOI18N
                } else if (libraryItem.getPath().endsWith(".a")) { // NOI18N
                    iconName = "org/netbeans/modules/cnd/loaders/static_library.gif"; // NOI18N
                } else {
                    iconName = "org/netbeans/modules/cnd/loaders/unknown.gif"; // NOI18N
                }
                break;
            case LibraryItem.OPTION_ITEM:
                iconName = "org/netbeans/modules/cnd/makeproject/ui/resources/general.gif"; // NOI18N
                break;
        }
        return iconName;
    }

    private static final class MyListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	    JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    LibraryItem libraryItem = (LibraryItem)value;
	    label.setIcon(ImageUtilities.loadImageIcon(StdLibPanel.getLibraryIconResource(libraryItem), false));
	    label.setToolTipText(libraryItem.getDescription());
            return label;
        }
    }   

    public LibraryItem.StdLibItem[] getSelectedStdLibs() {
    	Object[] selectedValues = libraryList.getSelectedValues();
        LibraryItem.StdLibItem[] selectedLibs = new LibraryItem.StdLibItem[selectedValues.length];
        for (int i = 0; i < selectedValues.length; i++) {
            selectedLibs[i] = (LibraryItem.StdLibItem) selectedValues[i];
        }
        return selectedLibs;
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        libraryList = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        label.setLabelFor(libraryList);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/configurations/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(label, bundle.getString("STANDARD_LIBRARIES_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(label, gridBagConstraints);

        scrollPane.setViewportView(libraryList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 12);
        add(scrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JList libraryList;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
    private static String getString(String s) {
        return NbBundle.getBundle(StdLibPanel.class).getString(s);
    }
}
