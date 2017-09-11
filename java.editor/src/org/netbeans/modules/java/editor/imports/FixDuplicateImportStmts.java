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

package org.netbeans.modules.java.editor.imports;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports.CandidateDescription;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports.ImportData;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * JTable with custom renderer, so second column looks editable (JComboBox).
 * Second column also has CellEditor (also a JComboBox).
 *
 * @author  eakle, Martin Roskanin
 */
public class FixDuplicateImportStmts extends javax.swing.JPanel{
    private JComboBox[] combos;
    private JCheckBox checkUnusedImports;
    
    public FixDuplicateImportStmts() {
        initComponents();
    }
    
    public void initPanel(ImportData data, boolean removeUnusedImports) {
        initComponentsMore(data, removeUnusedImports);
        setAccessible();
    }
    
    private void initComponentsMore(ImportData data, boolean removeUnusedImports) {
        contentPanel.setLayout( new GridBagLayout() );
        contentPanel.setBackground( UIManager.getColor("Table.background") ); //NOI18N
        jScrollPane1.setBorder( UIManager.getBorder("ScrollPane.border") ); //NOI18N
        jScrollPane1.getVerticalScrollBar().setUnitIncrement( new JLabel("X").getPreferredSize().height );
        jScrollPane1.getVerticalScrollBar().setBlockIncrement( new JLabel("X").getPreferredSize().height*10 );
        
        if (data.variants.length > 0 ) {
        
            int row = 0;

            combos = new JComboBox[data.variants.length];

            Font monoSpaced = new Font( "Monospaced", Font.PLAIN, new JLabel().getFont().getSize() );
            FocusListener focusListener = new FocusListener() {
                public void focusGained(FocusEvent e) {
                    Component c = e.getComponent();
                    Rectangle r = c.getBounds();
                    contentPanel.scrollRectToVisible( r );
                }
                public void focusLost(FocusEvent arg0) {
                }
            };
            for (int i=0; i < data.variants.length; i++){
                combos[i] = createComboBox(data.variants[i], data.defaults[i], monoSpaced, focusListener );

                JLabel lblSimpleName = new JLabel(data.simpleNames[i]);
                lblSimpleName.setOpaque( false );
                lblSimpleName.setFont( monoSpaced );
                lblSimpleName.setLabelFor( combos[i] );

                contentPanel.add( lblSimpleName, new GridBagConstraints(0,row,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(3,5,2,5),0,0) );
                contentPanel.add( combos[i], new GridBagConstraints(1,row++,1,1,1.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(3,5,2,5),0,0) );
            }

            contentPanel.add( new JLabel(), new GridBagConstraints(2,row,2,1,0.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );

            Dimension d = contentPanel.getPreferredSize();
            d.height = getRowHeight() * Math.min(combos.length, 6);
            jScrollPane1.getViewport().setPreferredSize( d );
        } else {
            contentPanel.add( new JLabel(getBundleString("FixDupImportStmts_NothingToFix")), new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(20,20,20,20),0,0) );
        }
        
        // load localized text into widgets:
        lblTitle.setText(getBundleString("FixDupImportStmts_IntroLbl")); //NOI18N
        lblHeader.setText(getBundleString("FixDupImportStmts_Header")); //NOI18N
        
        checkUnusedImports = new JCheckBox();
        Mnemonics.setLocalizedText(checkUnusedImports, getBundleString("FixDupImportStmts_UnusedImports")); //NOI18N
        bottomPanel.add( checkUnusedImports, BorderLayout.WEST );
        checkUnusedImports.setEnabled(true);
        checkUnusedImports.setSelected(removeUnusedImports);
    }
    
    private JComboBox createComboBox(CandidateDescription[] choices, CandidateDescription defaultValue, Font font, FocusListener listener ) {
        JComboBox combo = new JComboBox(choices);
        combo.setSelectedItem(defaultValue);
        combo.getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_Combo_ACSD")); //NOI18N
        combo.getAccessibleContext().setAccessibleName(getBundleString("FixDupImportStmts_Combo_Name_ACSD")); //NOI18N
        combo.setOpaque(false);
        combo.setFont( font );
        combo.addFocusListener( listener );
        combo.setEnabled( choices.length > 1 );
        combo.setRenderer( new DelegatingRenderer(combo.getRenderer()));
        InputMap inputMap = combo.getInputMap( JComboBox.WHEN_FOCUSED );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_SPACE, 0), "showPopup" ); //NOI18N
        combo.getActionMap().put( "showPopup", new TogglePopupAction() ); //NOI18N
        return combo;
    }
    
    private int getRowHeight() {
        return combos.length == 0 ? 0 :combos[0].getPreferredSize().height+6;
    }
    
    private static String getBundleString(String s) {
        return NbBundle.getMessage(FixDuplicateImportStmts.class, s);
    }
    
    
    private void setAccessible() {
        getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_IntroLbl")); // NOI18N
	checkUnusedImports.getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_checkUnusedImports_a11y")); // NOI18N
    }
    
    public CandidateDescription[] getSelections() {
        CandidateDescription[] res = new CandidateDescription[null == combos ? 0 : combos.length];
        for( int i=0; i<res.length; i++ ) {
            res[i] = (CandidateDescription) combos[i].getSelectedItem();
        }
        return res;
    }
    
    public boolean getRemoveUnusedImports() {
        return checkUnusedImports.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        bottomPanel = new javax.swing.JPanel();
        lblHeader = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(null);
        setLayout(new java.awt.GridBagLayout());

        lblTitle.setText("~Select the fully qualified name to use in the import statement.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(lblTitle, gridBagConstraints);

        jScrollPane1.setBorder(null);

        contentPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(contentPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(bottomPanel, gridBagConstraints);

        lblHeader.setText("~Import Statements:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(lblHeader, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblHeader;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables
    
    private static class DelegatingRenderer implements ListCellRenderer {
        private ListCellRenderer orig;
        public DelegatingRenderer(ListCellRenderer orig) {
            this.orig = orig;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof CandidateDescription) {
                CandidateDescription cd = (CandidateDescription) value;
                Component res = orig.getListCellRendererComponent(list, cd.displayName, index, isSelected, cellHasFocus);
                if( res instanceof JLabel) {
                    ((JLabel)res).setIcon(cd.icon);
                }
                return res;
            } else {
                return orig.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }
    
    private static class TogglePopupAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if( e.getSource() instanceof JComboBox ) {
                JComboBox combo = (JComboBox)e.getSource();
                combo.setPopupVisible( !combo.isPopupVisible() );
            }
        }
    }
}
