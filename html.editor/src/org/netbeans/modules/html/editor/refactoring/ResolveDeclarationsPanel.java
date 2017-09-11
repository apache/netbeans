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
package org.netbeans.modules.html.editor.refactoring;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

/**
 * The class is based on a modified copy of
 * org.netbeans.modules.java.editor.imports.FixDuplicateImportStmts class from java.editor module
 *
 * @author  mfukala@netbeans.org;
 */
public class ResolveDeclarationsPanel extends javax.swing.JPanel {

    private List<ResolveDeclarationItem> items;
    private List<JComboBox> combos;

    public ResolveDeclarationsPanel(Collection<ResolveDeclarationItem> items) {
        this.items = new ArrayList<>(items);
        initComponents();
        initComponentsMore();
        setAccessible();
    }

    private void initComponentsMore() {
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(UIManager.getColor("Table.background")); //NOI18N
        
        int row = 0;
        combos = new ArrayList<>(items.size());

        Font monoSpaced = new Font("Monospaced", Font.PLAIN, new JLabel().getFont().getSize()); //NOI18N
        FocusListener focusListener = new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                Component c = e.getComponent();
                Rectangle r = c.getBounds();
                contentPanel.scrollRectToVisible(r);
            }

        };
        for (int i = 0; i < items.size(); i++) {
            ResolveDeclarationItem item = items.get(i);
            JComboBox jComboBox = createComboBox(item, monoSpaced, focusListener);
            combos.add(jComboBox);

            JLabel lblSimpleName = new JLabel(item.getName());
            lblSimpleName.setOpaque(false);
            lblSimpleName.setFont(monoSpaced);
            lblSimpleName.setLabelFor(jComboBox);

            contentPanel.add(lblSimpleName, new GridBagConstraints(0, row, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 5, 2, 5), 0, 0));
            contentPanel.add(jComboBox, new GridBagConstraints(1, row++, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(3, 5, 2, 5), 0, 0));
        }

        contentPanel.add(new JLabel(), new GridBagConstraints(2, row, 2, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        Dimension d = contentPanel.getPreferredSize();
        d.height = getRowHeight() * Math.min(combos.size(), 6);

    }

    private JComboBox createComboBox(ResolveDeclarationItem resolveDeclarationItem, Font font, FocusListener listener) {
        List<DeclarationItem> declarations = resolveDeclarationItem.getPossibleDeclarations();
        if(declarations.isEmpty()) {
            //there's no definitions for the selector declaration in the project
            //show just some empty combo with a warning message
            JComboBox combo = new JComboBox(new String[]{NbBundle.getMessage(ResolveDeclarationItem.class, "MSG_No_Selector_Definion")}); //NOI18N
            combo.setEnabled(false);
            return combo;

        } else {
            //there are options
            String[] choices = new String[declarations.size()];
            for (int i = 0; i < choices.length; i++) {
                DeclarationItem item = declarations.get(i);
                StringBuilder b = new StringBuilder();
                b.append(item.getSource().getNameExt());
                int line = item.getDeclaration().entry().getLineOffset();
                if (line != -1) {
                    b.append(':');
                    b.append(line);
                }
                String lineText = item.getDeclaration().entry().getLineText().toString();
                if(lineText != null) {
                    b.append(" (");
                    b.append(lineText.trim());
                    b.append(')');
                }
                choices[i] = b.toString();
            }

            JComboBox combo = new JComboBox(choices);
    //        combo.getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_Combo_ACSD")); //NOI18N
    //        combo.getAccessibleContext().setAccessibleName(getBundleString("FixDupImportStmts_Combo_Name_ACSD")); //NOI18N
            combo.setOpaque(false);
            combo.setFont(font);
            combo.addFocusListener(listener);

            combo.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        //set the choosen combobox item to the corresponding ResolveDeclarationItem
                        JComboBox source = (JComboBox)e.getSource();
                        //get corresponding RDI
                        int sourceComboIndex = combos.indexOf(source);
                        ResolveDeclarationItem resolveDeclarationItem = items.get(sourceComboIndex);
                        //and set the selected DeclarationItem according to the selected combo's selected item index
                        int selectedTargetIndex = source.getSelectedIndex();
                        DeclarationItem selectedItem = resolveDeclarationItem.getPossibleDeclarations().get(selectedTargetIndex);
                        //set the choosed declaration item to the model
                        resolveDeclarationItem.resolve(selectedItem);
                    }
                }
            });

            combo.setEnabled(choices.length > 1);
            //select first item
            combo.setSelectedIndex(0);
            //unfortunatelly this won't fire the itemStateChanged event, se we need to
            //set the default resolved item manually
            resolveDeclarationItem.resolve(resolveDeclarationItem.getPossibleDeclarations().get(0));

            InputMap inputMap = combo.getInputMap(JComboBox.WHEN_FOCUSED);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "showPopup"); //NOI18N
            combo.getActionMap().put("showPopup", new TogglePopupAction()); //NOI18N
            return combo;
        }
    }

    private int getRowHeight() {
        return combos.size() == 0 ? 0 : combos.get(0).getPreferredSize().height + 6;
    }

    private void setAccessible() {
//        getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_IntroLbl")); // NOI18N
//	checkUnusedImports.getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_checkUnusedImports_a11y")); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        contentPanel.setLayout(new java.awt.GridBagLayout());
        add(contentPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    // End of variables declaration//GEN-END:variables

    private static class TogglePopupAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JComboBox) {
                JComboBox combo = (JComboBox) e.getSource();
                combo.setPopupVisible(!combo.isPopupVisible());
            }
        }
    }
}
