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
package org.netbeans.modules.cnd.utils.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class ListEditorPanel<E> extends javax.swing.JPanel {

    private static final RequestProcessor RP = new RequestProcessor(ListEditorPanel.class.getName(), 4);
    private JList targetList = null;
    private List<E> listData = new ArrayList<E>();
    private boolean allowedToRemoveAll = true;
    protected JButton[] extraButtons;
    private boolean isChanged = false;

    public ListEditorPanel(Collection<E> objects) {
        this(objects, null);
    }

    public ListEditorPanel(Collection<E> objects, JButton[] extraButtons) {
        initComponents();

        this.extraButtons = extraButtons;

        scrollPane.getViewport().setBackground(getBackground());

        listLabel.setText(getListLabelText());
        listLabel.setDisplayedMnemonic(getListLabelMnemonic());
        addButton.setText(getAddButtonText());
        addButton.setMnemonic(getAddButtonMnemonics());
        addButton.getAccessibleContext().setAccessibleDescription(getAddButtonAD());
        copyButton.setText(getCopyButtonText());
        copyButton.setMnemonic(getCopyButtonMnemonics());
        copyButton.getAccessibleContext().setAccessibleDescription(getCopyButtonAD());
        renameButton.setText(getRenameButtonText());
        renameButton.setMnemonic(getRenameButtonMnemonics());
        renameButton.getAccessibleContext().setAccessibleDescription(getRenameButtonAD());
        removeButton.setText(getRemoveButtonText());
        removeButton.setMnemonic(getRemoveButtonMnemonics());
        removeButton.getAccessibleContext().setAccessibleDescription(getRemoveButtonAD());
        upButton.setText(getUpButtonText());
        upButton.setMnemonic(getUpButtonMnemonics());
        upButton.getAccessibleContext().setAccessibleDescription(getUpButtonAD());
        downButton.setText(getDownButtonText());
        downButton.setMnemonic(getDownButtonMnemonics());
        downButton.getAccessibleContext().setAccessibleDescription(getDownButtonAD());
        defaultButton.setText(getDefaultButtonText());
        defaultButton.setMnemonic(getDefaultButtonMnemonics());
        defaultButton.getAccessibleContext().setAccessibleDescription(getDefaultButtonAD());

        if (objects != null) {
            for (E o : objects) {
                listData.add(o);
            }
        }
        targetList = new JList();
        targetList.setVisibleRowCount(6);
        targetList.setModel(new MyModel<E>(listData));
        targetList.addListSelectionListener(new TargetSelectionListener());
// VK: NoIZ: keyboard navigation does not work in Predefined Macros and Include Search Path components
//        targetList.addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                targetListKeyPressed(evt);
//            }
//        });
        targetList.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void mouseClicked(MouseEvent me) {
                E ob[] = (E[]) targetList.getSelectedValues();
                if (ob.length != 1) {
                    return;
                }
                if (me.getClickCount() == 2) {
                    me.consume();
                    editObjectAction();
                }
            }
        });
        targetList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane.setViewportView(getViewComponent());
        listLabel.setLabelFor(targetList);

        initAccessibility();

        // Set focus etc.
        if (targetList.getModel().getSize() > 0) {
            setSelectedIndex(0);
            targetList.requestFocus();
        } else {
            addButton.requestFocus();
        }

        // Add extra buttons
        if (extraButtons != null) {
            int index = 1; // strt index
            for (int i = 0; i < extraButtons.length; i++) {
                addExtraButton(extraButtons[i], index++);
            }
        }

        checkSelection();
    }

    public final void addExtraButton(JButton button, int index) {
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        controlsPanel.add(button, gridBagConstraints, index);
    }

    public void setAllowedToRemoveAll(boolean b) {
        allowedToRemoveAll = b;
        checkSelection();
    }

    public boolean getAllowedToRemoveAll() {
        return allowedToRemoveAll;
    }

    public JLabel getListLabel() {
        return listLabel;
    }

    protected final void setCustomCellRenderer(ListCellRenderer renderer) {
        targetList.setCellRenderer(renderer);
    }

    protected String getListLabelText() {
        return getString("TARGET_EDITOR_LIST_LBL");
    }

    protected char getListLabelMnemonic() {
        return getString("TARGET_EDITOR_LIST_MNEMONIC").toCharArray()[0];
    }

    protected String getAddButtonText() {
        return getString("TARGET_EDITOR_ADD_BUTTON_LBL");
    }

    protected char getAddButtonMnemonics() {
        return getString("TARGET_EDITOR_ADD_BUTTON_MNEMONIC").toCharArray()[0];
    }

    protected String getAddButtonAD() {
        return getString("TARGET_EDITOR_ADD_BUTTON_AD");
    }

    protected String getCopyButtonText() {
        return getString("TARGET_EDITOR_COPY_BUTTON_LBL");
    }

    protected char getCopyButtonMnemonics() {
        return getString("TARGET_EDITOR_COPY_BUTTON_MNEMONIC").toCharArray()[0];
    }

    protected String getCopyButtonAD() {
        return getString("TARGET_EDITOR_COPY_BUTTON_AD");
    }

    protected String getRenameButtonText() {
        return getString("TARGET_EDITOR_RENAME_BUTTON_LBL");
    }

    protected char getRenameButtonMnemonics() {
        return getString("TARGET_EDITOR_RENAME_BUTTON_MNEMONIC").toCharArray()[0];
    }

    protected String getRenameButtonAD() {
        return getString("TARGET_EDITOR_RENAME_BUTTON_AD");
    }

    protected String getRemoveButtonText() {
        return getString("TARGET_EDITOR_REMOVE_BUTTON_LBL");
    }

    protected char getRemoveButtonMnemonics() {
        return getString("TARGET_EDITOR_REMOVE_BUTTON_MNEMONIC").toCharArray()[0];
    }

    protected String getRemoveButtonAD() {
        return getString("TARGET_EDITOR_REMOVE_BUTTON_AD");
    }

    protected String getUpButtonText() {
        return getString("TARGET_EDITOR_UP_BUTTON_LBL");
    }

    protected char getUpButtonMnemonics() {
        return getString("TARGET_EDITOR_UP_BUTTON_MNEMONIC").toCharArray()[0];
    }

    protected String getUpButtonAD() {
        return getString("TARGET_EDITOR_UP_BUTTON_AD");
    }

    protected String getDownButtonText() {
        return getString("TARGET_EDITOR_DOWN_BUTTON_LBL");
    }

    protected char getDownButtonMnemonics() {
        return getString("TARGET_EDITOR_DOWN_BUTTON_MNEMONIC").toCharArray()[0];
    }

    protected String getDownButtonAD() {
        return getString("TARGET_EDITOR_DOWN_BUTTON_AD");
    }

    protected String getDefaultButtonText() {
        return getString("TARGET_EDITOR_DEFAULT_BUTTON_LBL");
    }

    protected char getDefaultButtonMnemonics() {
        return getString("TARGET_EDITOR_DEFAULT_BUTTON_MNEMONIC").toCharArray()[0];
    }

    protected String getDefaultButtonAD() {
        return getString("TARGET_EDITOR_DEFAULT_BUTTON_AD");
    }

    public JButton getAddButton() {
        return addButton;
    }

    public JButton getCopyButton() {
        return copyButton;
    }

    public JButton getEditButton() {
        return renameButton;
    }

    public JButton getRemoveButton() {
        return removeButton;
    }

    public JButton getUpButton() {
        return upButton;
    }

    public JButton getDownButton() {
        return downButton;
    }

    public JButton getDefaultButton() {
        return defaultButton;
    }

    @Override
    public void setEnabled(boolean b) {
        listLabel.setEnabled(b);
        targetList.setEnabled(b);
        addButton.setEnabled(b);
        copyButton.setEnabled(b);
        renameButton.setEnabled(b);
        removeButton.setEnabled(b);
        upButton.setEnabled(b);
        downButton.setEnabled(b);
        defaultButton.setEnabled(b);
    }

    private void initAccessibility() {
        AccessibleContext context;

        context = getAccessibleContext();
        context.setAccessibleName(getString("ACSN_TARGET_EDITOR"));
        context.setAccessibleDescription(getString("ACSD_TARGET_EDITOR"));

        context = targetList.getAccessibleContext();
        context.setAccessibleName(getString("ACSN_TARGET_LIST"));
        context.setAccessibleDescription(getString("ACSD_TARGET_LIST"));

        context = scrollPane.getAccessibleContext();
        context.setAccessibleName(getString("ACSN_TARGET_LIST"));
        context.setAccessibleDescription(getString("ACSD_TARGET_LIST"));

        context = scrollPane.getHorizontalScrollBar().getAccessibleContext();
        context.setAccessibleName(getString("ACSN_TARGET_LIST"));
        context.setAccessibleDescription(getString("ACSD_TARGET_LIST"));

        context = scrollPane.getVerticalScrollBar().getAccessibleContext();
        context.setAccessibleName(getString("ACSN_TARGET_LIST"));
        context.setAccessibleDescription(getString("ACSD_TARGET_LIST"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dataPanel = new javax.swing.JPanel();
        listLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        controlsPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        renameButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        defaultButton = new javax.swing.JButton();

        setOpaque(false);
        setRequestFocusEnabled(false);
        setLayout(new java.awt.GridBagLayout());

        dataPanel.setRequestFocusEnabled(false);
        dataPanel.setLayout(new java.awt.GridBagLayout());

        listLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle").getString("TARGET_EDITOR_LIST_MNEMONIC").charAt(0));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle"); // NOI18N
        listLabel.setText(bundle.getString("TARGET_EDITOR_LIST_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        dataPanel.add(listLabel, gridBagConstraints);

        scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        dataPanel.add(scrollPane, gridBagConstraints);

        controlsPanel.setOpaque(false);
        controlsPanel.setRequestFocusEnabled(false);
        controlsPanel.setLayout(new java.awt.GridBagLayout());

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle").getString("TARGET_EDITOR_ADD_BUTTON_MNEMONIC").charAt(0));
        addButton.setText(bundle.getString("TARGET_EDITOR_ADD_BUTTON_LBL")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        controlsPanel.add(addButton, gridBagConstraints);

        copyButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle").getString("TARGET_EDITOR_COPY_BUTTON_MNEMONIC").charAt(0));
        copyButton.setText(bundle.getString("TARGET_EDITOR_COPY_BUTTON_LBL")); // NOI18N
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        controlsPanel.add(copyButton, gridBagConstraints);

        renameButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle").getString("TARGET_EDITOR_RENAME_BUTTON_MNEMONIC").charAt(0));
        renameButton.setText(bundle.getString("TARGET_EDITOR_RENAME_BUTTON_LBL")); // NOI18N
        renameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        controlsPanel.add(renameButton, gridBagConstraints);

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle").getString("TARGET_EDITOR_REMOVE_BUTTON_MNEMONIC").charAt(0));
        removeButton.setText(bundle.getString("TARGET_EDITOR_REMOVE_BUTTON_LBL")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        controlsPanel.add(removeButton, gridBagConstraints);

        upButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle").getString("TARGET_EDITOR_UP_BUTTON_MNEMONIC").charAt(0));
        upButton.setText(bundle.getString("TARGET_EDITOR_UP_BUTTON_LBL")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        controlsPanel.add(upButton, gridBagConstraints);

        downButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle").getString("TARGET_EDITOR_DOWN_BUTTON_MNEMONIC").charAt(0));
        downButton.setText(bundle.getString("TARGET_EDITOR_DOWN_BUTTON_LBL")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        controlsPanel.add(downButton, gridBagConstraints);

        defaultButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/utils/ui/Bundle").getString("TARGET_EDITOR_DEFAULT_BUTTON_MNEMONIC").charAt(0));
        defaultButton.setText(bundle.getString("TARGET_EDITOR_DEFAULT_BUTTON_LBL")); // NOI18N
        defaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        controlsPanel.add(defaultButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        dataPanel.add(controlsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(dataPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public void defaultAction(E o) {
    }

    private synchronized void defaultObjectAction() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        if (selectedIndex >= (listData.size())) {
            return;
        }
        defaultAction(listData.get(selectedIndex));
        // Update gui
        isChanged = true;
        setData(listData);
        setSelectedIndex(selectedIndex);
        defaultButton.requestFocus();
        checkSelection();
        defaultButton.requestFocus();
    }

    private void defaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultButtonActionPerformed
        defaultObjectAction();
    }//GEN-LAST:event_defaultButtonActionPerformed

    public void editAction(E o, int i) {
    }

    private synchronized void editObjectAction() {
        final int selectedIndex = getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        if (selectedIndex >= (listData.size())) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                editAction(listData.get(selectedIndex), selectedIndex);
                // Update gui
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        isChanged = true;
                        setData(listData);
                        setSelectedIndex(selectedIndex);
                        renameButton.requestFocus();
                        checkSelection();
                        renameButton.requestFocus();
                    }
                });
            }
        });

    }

    private void renameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameButtonActionPerformed
        editObjectAction();
    }//GEN-LAST:event_renameButtonActionPerformed

    public E copyAction(E o) {
        return null;
    }

    private synchronized void copyObjectAction() {
        final int selectedIndex = getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        if (selectedIndex >= (listData.size())) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                final E newObject = copyAction(listData.get(selectedIndex));
                if (newObject == null) {
                    return;
                }
                // Update gui
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {                        
                        isChanged = true;
                        int addAtIndex = listData.size();
                        listData.add(addAtIndex, newObject);
                        setData(listData);
                        setSelectedIndex(addAtIndex);
                        copyButton.requestFocus();
                        checkSelection();
                        copyButton.requestFocus();
                    }
                });
            }
        });


    }

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
        copyObjectAction();
    }//GEN-LAST:event_copyButtonActionPerformed

// VK: NoIZ: keyboard navigation does not work in Predefined Macros and Include Search Path components
//    private void targetListKeyPressed(java.awt.event.KeyEvent evt) {
//        // Add your handling code here:
//        processKeyEvent(evt);
//    }
    public void downAction(int from) {
    }

    private synchronized void downAction() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        if (selectedIndex >= (listData.size() - 1)) {
            return;
        }
        downAction(selectedIndex);
        // Update GUI
        isChanged = true;
        E tmp = listData.get(selectedIndex);
        listData.remove(selectedIndex);
        listData.add(++selectedIndex, tmp);
        setData(listData);
        if (selectedIndex >= 0) {
            ensureIndexIsVisible(selectedIndex);
            checkSelection(selectedIndex);
            setSelectedIndex(selectedIndex);
        } else {
            checkSelection();
        }
        if (downButton.isEnabled()) {
            downButton.requestFocus();
        } else {
            upButton.requestFocus();
        }
    }

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        // Add your handling code here:
        downAction();
    }//GEN-LAST:event_downButtonActionPerformed

    public void upAction(int from) {
    }

    private synchronized void upAction() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex <= 0) {
            return;
        }
        if (selectedIndex >= (listData.size())) {
            return;
        }
        upAction(selectedIndex);
        // Update GUI
        isChanged = true;
        E tmp = listData.get(selectedIndex);
        listData.remove(selectedIndex);
        listData.add(--selectedIndex, tmp);
        setData(listData);
        if (selectedIndex >= 0) {
            ensureIndexIsVisible(selectedIndex);
            checkSelection(selectedIndex);
            setSelectedIndex(selectedIndex);
        } else {
            checkSelection();
        }
        if (upButton.isEnabled()) {
            upButton.requestFocus();
        } else {
            downButton.requestFocus();
        }
    }

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        // Add your handling code here:
        upAction();
    }//GEN-LAST:event_upButtonActionPerformed

    /**
     * This action will be invoked in non-UI thread Please be sure you use EDT
     * if you will do some UI updates here
     *
     * @param o object to remove
     * @param i index
     */
    public void removeAction(E o, int i) {
    }

    private synchronized void removeObjectAction() {
        final int selectedIndex = getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        if (selectedIndex >= (listData.size())) {
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                removeAction(listData.get(selectedIndex), selectedIndex);
                // Update GUI
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        isChanged = true;
                        listData.remove(selectedIndex);
                        setData(listData);
                        int newSelectedIndex = (selectedIndex >= listData.size()) ? selectedIndex - 1 : selectedIndex;
                        if (newSelectedIndex >= 0) {
                            ensureIndexIsVisible(newSelectedIndex);
                            checkSelection(newSelectedIndex);
                            setSelectedIndex(newSelectedIndex);
                        } else {
                            checkSelection();
                        }
                        if (removeButton.isEnabled()) {
                            removeButton.requestFocus();
                        } else {
                            addButton.requestFocus();
                        }
                    }
                });

            }
        });

    }

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Add your handling code here:
        removeObjectAction();
    }//GEN-LAST:event_removeButtonActionPerformed

    public E addAction() {
        return null; // "shouldbeoverridden"; // NOI18N
    }

    /* package */ void addObjectAction() {
        addObjectAction(addAction());
    }

    public void addObjectAction(E newObject) {
        if (newObject == null) {
            return;
        }
        ArrayList<E> listToAdd = new ArrayList<E>();
        listToAdd.add(newObject);
        addObjectsAction(listToAdd);
    }

    protected final synchronized void addObjectsAction(List<E> listToAdd) {
        if (listToAdd == null || listToAdd.isEmpty()) {
            return;
        }
        // Update gui
        this.isChanged = true;
        int addAtIndex = listData.size();
        ArrayList<E> newListData = new ArrayList<E>();
        newListData.addAll(listData);
        newListData.addAll(listToAdd);
        listData = newListData;
        setData(listData);
        int newSize = listData.size();
        setSelectedIndex(newSize - 1);
        ensureIndexIsVisible(newSize - 1);
        checkSelection();
        addButton.requestFocus();
    }

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add your handling code here:
        addObjectAction();
    }//GEN-LAST:event_addButtonActionPerformed

    public JPanel getDataPanel() {
        return dataPanel;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JButton copyButton;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JButton defaultButton;
    private javax.swing.JButton downButton;
    private javax.swing.JLabel listLabel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton renameButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    protected void checkSelection() {
        checkSelection(getSelectedIndex());
    }

    protected final synchronized void checkSelection(int i) {
        if (i >= 0 && listData.size() > 0) {
            addButton.setEnabled(true);
            copyButton.setEnabled(true);
            renameButton.setEnabled(true);
            if (allowedToRemoveAll) {
                removeButton.setEnabled(true);
            } else {
                removeButton.setEnabled(listData.size() > 1);
            }
            upButton.setEnabled(i == 0 ? false : true);
            downButton.setEnabled((i >= listData.size() - 1) ? false : true);
            defaultButton.setEnabled(true);
        } else {
            addButton.setEnabled(true);
            copyButton.setEnabled(false);
            renameButton.setEnabled(false);
            removeButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            defaultButton.setEnabled(false);
        }
    }

    private class TargetSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            checkSelection();
        }
    }

    public final synchronized List<E> getListData() {
        return new ArrayList<E>(listData);
    }

    protected final synchronized int getListDataSize() {
        return listData.size();
    }

    protected final synchronized E getElementAt(int i) {
        return listData.get(i);
    }

    protected synchronized void replaceElement(E oldElement, E newElement, int index) {
        Object[] arr = listData.toArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == oldElement) {
                listData.remove(i);
                listData.add(i, newElement);
                break;
            }
        }
    }

    // --- to be overridden
    public synchronized int getSelectedIndex() {
        int index = targetList.getSelectedIndex();
        if (index >= 0 && index < listData.size()) {
            return index;
        } else {
            return 0;
        }
    }

    protected void setSelectedIndex(int i) {
        targetList.setSelectedIndex(i);
    }

    protected void setData(List<E> data) {
        targetList.setModel(new MyModel<E>(data));
    }

    protected void ensureIndexIsVisible(int selectedIndex) {
        targetList.ensureIndexIsVisible(selectedIndex);
    }

    protected Component getViewComponent() {
        return targetList;
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ListEditorPanel.class, key);
    }

    // support to be integrated into Tools->Options
    public boolean isChanged() {
        return isChanged;
    }

    public boolean isDataValid() {
        return true;
    }

    public static abstract class MultipleChoiseListEditorPanel<E> extends ListEditorPanel<E> {

        public MultipleChoiseListEditorPanel(Collection<E> objects) {
            super(objects);
        }

        @Override
        public final E addAction() {
            throw new UnsupportedOperationException("Single choise is not suppoted. Use method addSeveralAction."); // NOI18N
        }

        @Override
        /* package */ void addObjectAction() {
            Collection<E> addMultipleAction = addSeveralAction();
            if (addMultipleAction != null) {
                List<E> list = new ArrayList<E>();
                for (E item : addMultipleAction) {
                    if (item != null) {
                        list.add(item);
                    }
                }
                addObjectsAction(list);
            }
        }

        public abstract Collection<E> addSeveralAction();

    }

    private static final class MyModel<E> extends AbstractListModel {

        private final List<E> listData;

        private MyModel(List<E> listData) {
            this.listData = listData;
        }

        @Override
        public int getSize() {
            return listData.size();
        }

        @Override
        public Object getElementAt(int i) {
            return listData.get(i);
        }
    }
}
